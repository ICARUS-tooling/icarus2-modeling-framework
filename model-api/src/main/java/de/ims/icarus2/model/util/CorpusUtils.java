/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 448 $
 * $Date: 2016-01-19 17:30:06 +0100 (Di, 19 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/util/CorpusUtils.java $
 *
 * $LastChangedDate: 2016-01-19 17:30:06 +0100 (Di, 19 Jan 2016) $
 * $LastChangedRevision: 448 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.util;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.mods.DriverModule;
import de.ims.icarus2.model.api.edit.CorpusEditManager;
import de.ims.icarus2.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus2.model.api.io.ResourcePath;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.NamedCorpusMember;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.raster.Metric;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.raster.Rasterizer;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.HighlightLayerManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LocationType;
import de.ims.icarus2.model.manifest.api.ManifestOwner;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.Messages;

/**
 * @author Markus Gärtner
 * @version $Id: CorpusUtils.java 448 2016-01-19 16:30:06Z mcgaerty $
 *
 */
public final class CorpusUtils implements ModelConstants {

	private CorpusUtils() {
		throw new AssertionError();
	}

	public static String getUniqueId(Layer layer) {
		return getUniqueId(layer, '@');
	}

	public static String getUniqueId(Layer layer, char separator) {
		checkNotNull(layer);

		LayerManifest manifest = layer.getManifest();

		String id = null;

		// If manifest is null then the layer must be an overlay layer!
		if(manifest==null) {
			Corpus corpus = layer.getCorpus();

			id = corpus.getManifest().getId()+"@layer-overlay";
		} else {

			LayerGroup layerGroup = layer.getLayerGroup();
			Context context = layer.getContext();

			StringBuilder sb = new StringBuilder();

			if(context!=null) {
				Corpus corpus = context.getCorpus();
				if(corpus!=null) {
					sb.append(corpus.getManifest().getId()).append(separator);
				}

				sb.append(context.getManifest().getId()).append(separator);
			}

			if(layerGroup!=null) {
				sb.append(layerGroup.getManifest().getId()).append(separator);
			}

			sb.append(manifest.getId());

			id = sb.toString();
		}

		if(id==null) {
			id = "unnamed-layer";
		}

		return id;
	}

	public static String getUniqueId(LayerManifest manifest) {
		return getUniqueId(manifest, '@');
	}

	public static String getUniqueId(LayerManifest manifest, char separator) {
		checkNotNull(manifest);

		LayerGroupManifest groupManifest = manifest.getGroupManifest();
		ContextManifest contextManifest = manifest.getContextManifest();

		StringBuilder sb = new StringBuilder();

		if(contextManifest!=null) {
			CorpusManifest corpusManifest = contextManifest.getCorpusManifest();
			if(corpusManifest!=null) {
				sb.append(corpusManifest.getId()).append(separator);
			}

			sb.append(contextManifest.getId()).append(separator);
		}

		if(groupManifest!=null) {
			sb.append(groupManifest.getId()).append(separator);
		}

		sb.append(manifest.getId());

		return sb.toString();
	}

	public static boolean isValidValue(Object value, AnnotationManifest manifest) {
		if(value==null) {
			return true;
		}

		ValueType type = manifest.getValueType();

		if(type==ValueType.UNKNOWN)
			throw new IllegalArgumentException("Manifest declares annotation value type as unknown: "+manifest); //$NON-NLS-1$

		return type.isValidValue(value);
	}

	public static ContextManifest getContextManifest(MemberManifest manifest) {
		checkNotNull(manifest);

		switch (manifest.getManifestType()) {
		case ANNOTATION_LAYER_MANIFEST:
			return ((AnnotationLayerManifest)manifest).getContextManifest();
		case ITEM_LAYER_MANIFEST:
			return ((ItemLayerManifest)manifest).getContextManifest();
		case STRUCTURE_LAYER_MANIFEST:
			return ((StructureLayerManifest)manifest).getContextManifest();
		case HIGHLIGHT_LAYER_MANIFEST:
			return ((HighlightLayerManifest)manifest).getContextManifest();

		case CONTEXT_MANIFEST:
			return (ContextManifest) manifest;

		case CONTAINER_MANIFEST:
			return ((ContainerManifest) manifest).getLayerManifest().getContextManifest();
		case STRUCTURE_MANIFEST:
			return ((StructureManifest) manifest).getLayerManifest().getContextManifest();

		default:
			throw new IllegalArgumentException("MemberManifest does not procide scope to a context: "+manifest); //$NON-NLS-1$
		}
	}

	public static <L extends ItemLayer> boolean isPrimaryLayer(L layer) {
		return layer.getContext().getPrimaryLayer()==layer;
	}

	public static <L extends ItemLayer> boolean isFoundationLayer(L layer) {
		return layer.getContext().getFoundationLayer()==layer;
	}

	public static boolean isVirtual(Item item) {
		return item.getBeginOffset()==-1 || item.getEndOffset()==-1;
	}

	public static boolean isOverlayContainer(Container container) {
		return container.getCorpus().getOverlayContainer()==container;
	}

	public static boolean isItemLayer(Layer layer) {
		ManifestType type = layer.getManifest().getManifestType();
		return type==ManifestType.ITEM_LAYER_MANIFEST
				|| type==ManifestType.STRUCTURE_LAYER_MANIFEST
				|| type==ManifestType.FRAGMENT_LAYER_MANIFEST;
	}

	public static boolean isItemLayer(LayerManifest manifest) {
		ManifestType type = manifest.getManifestType();
		return type==ManifestType.ITEM_LAYER_MANIFEST
				|| type==ManifestType.STRUCTURE_LAYER_MANIFEST
				|| type==ManifestType.FRAGMENT_LAYER_MANIFEST;
	}

	public static boolean isStructureLayer(LayerManifest manifest) {
		ManifestType type = manifest.getManifestType();
		return type==ManifestType.STRUCTURE_LAYER_MANIFEST;
	}

	public static boolean isFragmentLayer(LayerManifest manifest) {
		ManifestType type = manifest.getManifestType();
		return type==ManifestType.FRAGMENT_LAYER_MANIFEST;
	}

	public static boolean isOverlayLayer(ItemLayer layer) {
		return layer.getCorpus().getOverlayLayer()==layer;
	}

	public static boolean isOverlayMember(Item item) {
		return isOverlayLayer(item.getLayer());
	}

	public static boolean isLayer(CorpusMember member) {
		return member.getMemberType()==MemberType.LAYER;
	}

	public static boolean isNonLayer(CorpusMember member) {
		return member.getMemberType()!=MemberType.LAYER;
	}

	public static boolean isContainerOrStructure(CorpusMember member) {
		return member.getMemberType()==MemberType.CONTAINER
				|| member.getMemberType()==MemberType.STRUCTURE;
	}

	public static boolean isElement(CorpusMember member) {
		return member.getMemberType()==MemberType.ITEM
				|| member.getMemberType()==MemberType.EDGE
				|| member.getMemberType()==MemberType.FRAGMENT;
	}

	public static boolean isResolvedPrerequisite(PrerequisiteManifest manifest) {
		return manifest.getContextId()!=null && manifest.getLayerId()!=null;
	}

	public static void dispatchChange(CorpusMember source, AtomicChange change) {
		checkNotNull(source);
		checkNotNull(change);

		Corpus corpus = source.getCorpus();

		if(corpus==null) {
			change.execute();
			return;
		}

		CorpusEditManager editModel = corpus.getEditManager();

		if(editModel==null) {
			change.execute();
			return;
		}

		editModel.execute(change);
	}

	public static ContainerManifest getContainerManifest(Container container) {
		checkNotNull(container);

		// Fetch the container level and ask the
		// hosting markable layer manifest for the container
		// manifest at the specific level
		int level = 0;

		Container parent;
		while((parent = container.getContainer())!=null) {
			level++;
			container = parent;
		}

		ItemLayerManifest manifest = container.getLayer().getManifest();

		return manifest.getContainerManifest(level);
	}

	public static Layer getLayer(Corpus corpus, LayerManifest manifest) {
		ContextManifest contextManifest = manifest.getContextManifest();
		Context context = corpus.getContext(contextManifest.getId());
		return context.getLayer(manifest.getId());
	}

	public static String getName(Object obj) {
		if(obj instanceof PrerequisiteManifest) {
			PrerequisiteManifest prerequisite = (PrerequisiteManifest)obj;
			String id = prerequisite.getLayerId();
			if(id!=null)
				return "Required layer-id: "+id; //$NON-NLS-1$

			String typeName = prerequisite.getTypeId();
			if(typeName!=null && !typeName.isEmpty())
				return "Required type-id: "+typeName; //$NON-NLS-1$

			return prerequisite.toString();
		} else if (obj instanceof ManifestOwner) {
			return ((ManifestOwner<?>)obj).getManifest().getName();
		} else if (obj instanceof LayerGroup) {
			return ((LayerGroup)obj).getManifest().getName();
		} else if (obj instanceof NamedCorpusMember) {
			return ((NamedCorpusMember)obj).getName();
		} else {
			return obj.toString();
		}
	}

	public static Set<ItemLayer> getItemLayers(Corpus corpus) {
		return getLayers(ItemLayer.class, corpus.getLayers());
	}

	public static Set<AnnotationLayer> getAnnotationLayers(Corpus corpus) {
		return getLayers(AnnotationLayer.class, corpus.getLayers());
	}

	public static <L extends Layer> Set<L> getLayers(Class<L> clazz, Collection<Layer> layers) {
		checkNotNull(clazz);
		checkNotNull(layers);

		Set<L> result = new HashSet<>();

		for(Layer layer : layers) {
			if(clazz.isAssignableFrom(layer.getClass())) {
				result.add(clazz.cast(layer));
			}
		}

		return result;
	}

	public static Map<String, Object> getProperties(MemberManifest manifest) {
		checkNotNull(manifest);

		Map<String, Object> result = new HashMap<>();

		for(String name : manifest.getPropertyNames()) {
			result.put(name, manifest.getProperty(name));
		}

		return result;
	}

	public static Context getContext(CorpusMember member) {
		checkNotNull(member);

		Layer layer = null;

		if(member instanceof Item) {
			layer = ((Item)member).getLayer();
		} else if(member instanceof Layer) {
			layer = (Layer)member;
		}

		return layer==null ? null : layer.getContext();
	}

	private static char getTypePrefix(MemberType type) {
		switch (type) {
		case ITEM:
			return 'I';
		case FRAGMENT:
			return 'F';
		case CONTAINER:
			return 'C';
		case STRUCTURE:
			return 'S';
		case LAYER:
			return 'L';
		case EDGE:
			return 'E';

		default:
			throw new IllegalArgumentException();
		}
	}

	public static String toString(CorpusMember m) {
		MemberType type = m.getMemberType();

		if(type==MemberType.LAYER) {
			Layer layer = (Layer)m;
			return "[Layer: "+layer.getName()+"]"; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			Item item = (Item)m;
			Layer layer = item.getLayer();
			long index = item.getIndex();
			return "["+layer.getName()+"_"+getTypePrefix(type)+"_"+index+"<"+item.getBeginOffset()+"-"+item.getEndOffset()+">]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
	}

	public static int compare(Item m1, Item m2) {
		if(m1.getLayer().getFoundationLayer()!=m2.getLayer().getFoundationLayer())
			throw new ModelException(ModelErrorCode.MODEL_INCOMPATIBLE_FOUNDATIONS,
					Messages.incompatibleFoundationLayersMessage(null, m1, m1));

		long result = m1.getBeginOffset()-m2.getBeginOffset();

		if(result==0) {
			result = m1.getEndOffset()-m2.getEndOffset();
		}

		return (int) result;
	}

	public static int compare(Fragment f1, Fragment f2) {
		if(f1.getLayer()!=f2.getLayer())
			throw new IllegalArgumentException("Cannot compare fragments from different fragment layers"); //$NON-NLS-1$

		if(f1.getItem()!=f2.getItem()) {
			return compare(f1.getItem(), f2.getItem());
		}

		Rasterizer rasterizer = f1.getLayer().getRasterizer();

		Metric<Position> metric = rasterizer.getMetric();

		int result = metric.compare(f1.getFragmentBegin(), f2.getFragmentBegin());

		if(result==0) {
			result = metric.compare(f1.getFragmentEnd(), f2.getFragmentEnd());
		}

		return result;
	}

	/**
	 * Returns {@code true} if {@code m2} is located within the span
	 * defined by {@code m1}.
	 */
	public static boolean contains(Item m1, Item m2) {
		if(m1.getLayer().getFoundationLayer()!=m2.getLayer().getFoundationLayer())
			throw new ModelException(ModelErrorCode.MODEL_INCOMPATIBLE_FOUNDATIONS,
					Messages.incompatibleFoundationLayersMessage(null, m1, m1));

		return m2.getBeginOffset()>=m1.getBeginOffset()
				&& m2.getEndOffset()<=m1.getEndOffset();
	}

	public static void checkFragmentPositions(Fragment fragment, Position begin, Position end) {
		if(begin==null && end==null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"At least one position must be non-null!"); //$NON-NLS-1$

		Item item = fragment.getItem();
		FragmentLayer layer = fragment.getLayer();
		Rasterizer rasterizer = layer.getRasterizer();

		int dimensionality = rasterizer.getAxisCount();
		if(begin!=null && begin.getDimensionality()!=dimensionality)
			throw new ModelException(ModelErrorCode.MODEL_INVALID_POSITION,
					"Begin position dimensionality mismatch: expected " //$NON-NLS-1$
					+dimensionality+" - got "+begin.getDimensionality()); //$NON-NLS-1$
		if(end!=null && end.getDimensionality()!=dimensionality)
			throw new ModelException(ModelErrorCode.MODEL_INVALID_POSITION,
					"End position dimensionality mismatch: expected " //$NON-NLS-1$
					+dimensionality+" - got "+end.getDimensionality()); //$NON-NLS-1$

		for(int axis=0; axis<dimensionality; axis++) {
			long size = layer.getRasterSize(item, axis);
			checkPosition(size, begin, axis);
			checkPosition(size, end, axis);
		}

		if(begin!=null && end!=null && rasterizer.getMetric().compare(begin, end)>0)
			throw new ModelException(ModelErrorCode.MODEL_INVALID_POSITION,
					"Begin position must not exceed end position: "+begin+" - "+end); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static void checkPosition(long size, Position p, int axis) {
		if(p==null) {
			return;
		}

		long value = p.getValue(axis);

		if(value<0 || value>=size)
			throw new ModelException(ModelErrorCode.MODEL_POSITION_OUT_OF_BOUNDS,
					"Invalid value for axis "+axis+" on position "+p+" - max size "+size); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Iterates over all modules of all drivers that are responsible for data
	 * int the given corpus and makes sure they are all {@link DriverModule#isReady() ready}.
	 *
	 * @param corpus
	 * @throws InterruptedException
	 */
	public static void prepareCorpus(Corpus corpus) throws InterruptedException {
		for(Context context : corpus.getContexts()) {
			Driver driver = context.getDriver();
			if(!driver.isReady()) {
				for(DriverModule module : driver.getModules()) {
					if(!module.isReady()) {
						module.prepare(null);
					}
				}
			}
		}
	}

	public static Path pathToFile(ResourcePath path) {
		checkNotNull(path);
		if(path.getType()!=LocationType.LOCAL)
			throw new IllegalArgumentException("ResourcePath needs to be a file: "+path.getPath()); //$NON-NLS-1$

		return Paths.get(path.getPath());
	}

	public static URL pathToURL(ResourcePath path) throws MalformedURLException {
		checkNotNull(path);
		if(path.getType()!=LocationType.REMOTE)
			throw new IllegalArgumentException("ResourcePath needs to be a url: "+path.getPath()); //$NON-NLS-1$

		return new URL(path.getPath());
	}

	public static InputStream openPath(ResourcePath path) throws IOException {
		checkNotNull(path);

		switch (path.getType()) {
		case LOCAL:
			return Files.newInputStream(pathToFile(path));

		case REMOTE:
			return new URL(path.getPath()).openStream();

		default:
			throw new IllegalArgumentException("Cannot handle source type: "+path.getType()); //$NON-NLS-1$
		}
	}
}
