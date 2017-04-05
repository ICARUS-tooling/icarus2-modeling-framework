/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 */
package de.ims.icarus2.filedriver.schema.resolve.standard;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.ObjLongConsumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.Converter.ReadMode;
import de.ims.icarus2.filedriver.FileDriver;
import de.ims.icarus2.filedriver.FileDriverMetadata.ContainerKey;
import de.ims.icarus2.filedriver.schema.resolve.BatchResolver;
import de.ims.icarus2.filedriver.schema.resolve.ResolverContext;
import de.ims.icarus2.filedriver.schema.resolve.ResolverOptions;
import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache;
import de.ims.icarus2.model.standard.members.structure.builder.StructureBuilder;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.strings.StringPrimitives;
import de.ims.icarus2.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 *
 */
public class DependencyStructureResolver implements BatchResolver, ResolverOptions, ModelConstants {

	public static final String OPTION_OFFSET = "offset";
	public static final String OPTION_ROOT_LABEL = "root";

	private StructureLayer dependencyLayer;

	private ObjLongConsumer<Item> structureSaveAction;

	// Configuration

	private static final int ROOT_POINTER = -1;
	private static final int UNDEFINED_POINTER = -2;

	/**
	 * Offset to subtract from parsed head index values.
	 */
	private int offset;

	/**
	 * The label indicating that the current item is  directly connected
	 * to the artificial root node.
	 */
	private String rootLabel;

	private StructureBuilder structureBuilder;

	// Buffer

	/**
	 * Holds index pointer of source terminal for respective edges in 'edges' array.
	 * <p>
	 * Value of {@code -1} means the node should be connected to the artificial root node
	 */
	private int[] heads;

	/**
	 * Holds partly initialized edges (only target terminal set, source and structure missing)
	 */
	private Edge[] edges;

	private boolean dynamicBufferSize = false;

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#init(de.ims.icarus2.util.Options)
	 */
	@Override
	public void prepareForReading(Converter converter, ReadMode mode, Function<ItemLayer, InputCache> caches, Options options) {
		dependencyLayer = (StructureLayer) options.get(LAYER);
		if(dependencyLayer==null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "No layer assigned to this resolver "+getClass());

		ItemLayer sentenceLayer = dependencyLayer.getBoundaryLayer();

		FileDriver driver = converter.getDriver();

		// Determine required size of internal buffer arrays
		MetadataRegistry metadataRegistry = driver.getMetadataRegistry();

		int maxItemCount = metadataRegistry.getIntValue(
				ContainerKey.MAX_ITEM_COUNT.getKey(sentenceLayer.getManifest(), 1), -1);

		if(maxItemCount==-1) {
			dynamicBufferSize = true;
			maxItemCount = 100;
		}

		edges = new Edge[maxItemCount];
		heads = new int[maxItemCount];

		// Link with back-end storage
		InputCache cache = caches.apply(dependencyLayer);
		structureSaveAction = cache::offer;

		// Read options
		offset = options.getInteger(OPTION_OFFSET, 0);
		rootLabel = options.get(OPTION_ROOT_LABEL, (String)null);

	}

	/**
	 * Stores the given information, expanding the internal buffer
	 * arrays if required
	 */
	private void saveHeadInfo(int index, Edge edge, int head) {
		if(dynamicBufferSize && index>=edges.length) {
			int newSize = index<<1;
			if(newSize<0) {
				newSize = (int) IcarusUtils.MAX_INTEGER_INDEX;
			}

			edges = Arrays.copyOf(edges, newSize);
			heads = Arrays.copyOf(heads, newSize);
		}

		edges[index] = edge;
		heads[index] = head;
	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#process(java.lang.CharSequence, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public Item process(ResolverContext context) {

		CharSequence data = context.rawData();

		int head = ROOT_POINTER;

		if(rootLabel==null || !StringUtil.equals(data, rootLabel)) {
			head = StringPrimitives.parseInt(data) - offset;
			if(head<0)
				throw new ModelException(ModelErrorCode.DRIVER_INVALID_CONTENT,
						"Illegal 'head' value for dependency: "+data+" (resulting in head index "+head+")");
		}

		Edge edge = structureBuilder.newEdge(UNSET_LONG);
		edge.setTarget(context.currentItem());

		saveHeadInfo(IcarusUtils.ensureIntegerValueRange(context.currentIndex()), edge, head);

		return edge;
	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.BatchResolver#beginBatch()
	 */
	@Override
	public void beginBatch(ResolverContext context) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.BatchResolver#endBatch()
	 */
	@Override
	public void endBatch(ResolverContext context) {

		Container sentence = context.currentContainer();
		int length = IcarusUtils.ensureIntegerValueRange(sentence.getItemCount());

		Item root = structureBuilder.getRoot();

		// Finalize preparations
		for(int i=0; i<length; i++) {
			int head = heads[i];

			switch (head) {
			case UNDEFINED_POINTER: break;
			case ROOT_POINTER:
				edges[i].setSource(root);
				break;

			default:
				edges[i].setSource(sentence.getItemAt(head));
				break;
			}
		}

		// Now delegate work to builder
		structureBuilder.augmented(false);
		structureBuilder.addNodes(sentence);
		structureBuilder.addEdges(edges, 0, IcarusUtils.ensureIntegerValueRange(context.currentIndex()));
		structureBuilder.setBoundaryContainer(sentence);
		structureBuilder.setBaseContainer(sentence);
		Structure dependencyTree = structureBuilder.build();

		/*
		 * Push structure into back-end storage.
		 *
		 * Mirror index value of sentence, since syntax structures
		 * are expected to  have a 1-to-1 mapping to their boundary
		 * sentences.
		 */
		structureSaveAction.accept(dependencyTree, sentence.getIndex());

		// Reset buffers
		Arrays.fill(heads, 0, length, UNDEFINED_POINTER);
		Arrays.fill(edges, 0, length, null);
	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#close()
	 */
	@Override
	public void close() {

		// Make sure we don't hold any pending references to old edges
		Arrays.fill(edges, null);
		structureSaveAction = null;
	}
}
