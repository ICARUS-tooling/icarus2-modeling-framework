/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.model.util;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static java.util.Objects.requireNonNull;

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
import java.util.Optional;
import java.util.Set;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.edit.CorpusEditManager;
import de.ims.icarus2.model.api.edit.change.AtomicChange;
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
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.raster.Metric;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.raster.RasterAxis;
import de.ims.icarus2.model.api.raster.Rasterizer;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.Hierarchy;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LocationType;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestOwner;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 *
 */
public final class ModelUtils {

	private ModelUtils() {
		throw new AssertionError();
	}

	public static String getUniqueId(Layer layer) {
		return getUniqueId(layer, '.');
	}

	public static String getUniqueId(Layer layer, char separator) {
		requireNonNull(layer);

		LayerManifest<?> manifest = layer.getManifest();

		String id = null;

		// If manifest is null then the layer must be an overlay layer!
		if(manifest==null) {
			Corpus corpus = layer.getCorpus();

			id = corpus.getManifest().getId().orElse("unnamed-corpus")+separator+"layer-overlay";
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

	public static boolean isValidValue(Object value, AnnotationManifest manifest) {
		if(value==null) {
			return true;
		}

		ValueType type = manifest.getValueType();

		if(type==ValueType.UNKNOWN)
			throw new IllegalArgumentException("Manifest declares annotation value type as unknown: "+manifest);

		return type.isValidValue(value);
	}

	public static ContextManifest getContextManifest(MemberManifest<?> manifest) {
		requireNonNull(manifest);

		switch (manifest.getManifestType()) {
		case ANNOTATION_LAYER_MANIFEST:
		case ITEM_LAYER_MANIFEST:
		case STRUCTURE_LAYER_MANIFEST:
		case HIGHLIGHT_LAYER_MANIFEST:
			return ManifestUtils.requireHost((LayerManifest<?>)manifest);

		case CONTEXT_MANIFEST:
			return (ContextManifest) manifest;

		case CONTAINER_MANIFEST:
		case STRUCTURE_MANIFEST:
			return ManifestUtils.requireGrandHost((ContainerManifest)manifest);

		default:
			throw new IllegalArgumentException("MemberManifest does not provide scope to a context: "+manifest);
		}
	}

	public static <L extends ItemLayer> boolean isPrimaryLayer(L layer) {
		return layer.getContext().getPrimaryLayer()==layer;
	}

	public static <L extends ItemLayer> boolean isFoundationLayer(L layer) {
		return layer.getContext().getFoundationLayer()==layer;
	}

	public static boolean isVirtual(Item item) {
		return item.getBeginOffset()==UNSET_LONG || item.getEndOffset()==UNSET_LONG;
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

	public static boolean isStructureLayer(Layer layer) {
		ManifestType type = layer.getManifest().getManifestType();
		return type==ManifestType.STRUCTURE_LAYER_MANIFEST;
	}

	/**
	 * Returns {@code true} if the given manifest describes a layer belonging
	 * to one of the three <i>item layer</i> types:
	 * <ul>
	 * <li>{@link ManifestType#ITEM_LAYER_MANIFEST}</li>
	 * <li>{@link ManifestType#STRUCTURE_LAYER_MANIFEST}</li>
	 * <li>{@link ManifestType#FRAGMENT_LAYER_MANIFEST}</li>
	 * </ul>
	 * @param manifest
	 * @return
	 */
	public static boolean isAnyItemLayer(LayerManifest<?> manifest) {
		ManifestType type = manifest.getManifestType();
		return type==ManifestType.ITEM_LAYER_MANIFEST
				|| type==ManifestType.STRUCTURE_LAYER_MANIFEST
				|| type==ManifestType.FRAGMENT_LAYER_MANIFEST;
	}

	public static boolean isItemLayer(LayerManifest<?> manifest) {
		ManifestType type = manifest.getManifestType();
		return type==ManifestType.ITEM_LAYER_MANIFEST;
	}

	public static boolean isStructureLayer(LayerManifest<?> manifest) {
		ManifestType type = manifest.getManifestType();
		return type==ManifestType.STRUCTURE_LAYER_MANIFEST;
	}

	public static boolean isFragmentLayer(LayerManifest<?> manifest) {
		ManifestType type = manifest.getManifestType();
		return type==ManifestType.FRAGMENT_LAYER_MANIFEST;
	}

	public static boolean isFragmentLayer(Layer layer) {
		ManifestType type = layer.getManifest().getManifestType();
		return type==ManifestType.FRAGMENT_LAYER_MANIFEST;
	}

	public static boolean isAnnotationLayer(LayerManifest<?> manifest) {
		ManifestType type = manifest.getManifestType();
		return type==ManifestType.ANNOTATION_LAYER_MANIFEST;
	}

	public static boolean isAnnotationLayer(Layer layer) {
		ManifestType type = layer.getManifest().getManifestType();
		return type==ManifestType.ANNOTATION_LAYER_MANIFEST;
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

	public static boolean isStructure(CorpusMember member) {
		return member.getMemberType()==MemberType.STRUCTURE;
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
		requireNonNull(source);
		requireNonNull(change);

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

	/**
	 * @deprecated makes unreasonable assumptions on the linking and stacking of manifests
	 */
	@Deprecated
	public static ContainerManifestBase<?> getContainerManifest(Container container) {
		requireNonNull(container);

		// Fetch the container level and ask the
		// hosting item layer manifest for the container
		// manifest at the specific level
		int level = 0;

		Container parent;
		while((parent = container.getContainer())!=null && !parent.isProxy()) {
			level++;
			container = parent;
		}

		ItemLayerManifestBase<?> manifest = container.getLayer().getManifest();

		Hierarchy<ContainerManifestBase<?>> hierarchy = manifest.getContainerHierarchy()
				.orElseThrow(() -> new ManifestException(ManifestErrorCode.MANIFEST_CORRUPTED_STATE,
					"Host manifest has no container hierarchy: "+ManifestUtils.getName(manifest)));

		return hierarchy.atLevel(level);
	}

	public static Layer getLayer(Corpus corpus, LayerManifest<?> manifest) {
		ContextManifest contextManifest = ManifestUtils.requireHost(manifest);
		return corpus.getContext(ManifestUtils.requireId(contextManifest))
				.getLayer(ManifestUtils.requireId(manifest));
	}

	public static String getName(Object obj) {
		if(obj==null) {
			return "<null>";
		}

		String result = null;

		if(obj instanceof PrerequisiteManifest) {
			PrerequisiteManifest prerequisite = (PrerequisiteManifest)obj;
			String id = prerequisite.getLayerId().orElse(null);
			if(id!=null) {
				result = "Required layer-id: "+id;
			} else {
				String typeName = prerequisite.getTypeId().orElse(null);
				if(typeName!=null && !typeName.isEmpty())
					result = "Required type-id: "+typeName;
				else
					result = prerequisite.toString();
			}
		} else if (obj instanceof ManifestOwner) {
			result = Optional.ofNullable((MemberManifest<?>)((ManifestOwner<?>)obj).getManifest())
						.flatMap(Identity::getName)
						.orElse(null);
		} else if (obj instanceof LayerGroup) {
			result = Optional.ofNullable(((LayerGroup)obj).getManifest())
						.flatMap(Identity::getName)
						.orElse(null);
		} else if (obj instanceof NamedCorpusMember) {
			result = ((NamedCorpusMember)obj).getName();
		} else {
			result = obj.toString();
		}

		if(result==null) {
			result = "<unnamed "+obj.getClass()+">";
		}

		return result;
	}

	public static Set<ItemLayer> getItemLayers(Corpus corpus) {
		return getLayers(ItemLayer.class, corpus.getLayers());
	}

	public static Set<AnnotationLayer> getAnnotationLayers(Corpus corpus) {
		return getLayers(AnnotationLayer.class, corpus.getLayers());
	}

	public static <L extends Layer> Set<L> getLayers(Class<L> clazz, Collection<Layer> layers) {
		requireNonNull(clazz);
		requireNonNull(layers);

		Set<L> result = new HashSet<>();

		for(Layer layer : layers) {
			if(clazz.isAssignableFrom(layer.getClass())) {
				result.add(clazz.cast(layer));
			}
		}

		return result;
	}

	public static Map<String, Object> getProperties(MemberManifest<?> manifest) {
		requireNonNull(manifest);

		Map<String, Object> result = new HashMap<>();

		for(String name : manifest.getPropertyNames()) {
			result.put(name, manifest.getProperty(name));
		}

		return result;
	}

	public static Context getContext(CorpusMember member) {
		requireNonNull(member);

		Layer layer = null;

		if(member instanceof Item) {
			layer = ((Item)member).getLayer();
		} else if(member instanceof Layer) {
			layer = (Layer)member;
		}

		return layer==null ? null : layer.getContext();
	}

	private static void dumpTree0(Structure tree, Item node, StringBuilder sb) {
		sb.append('[');
		sb.append(node.toString());
		if(tree.getOutgoingEdgeCount(node)>0) {
			sb.append(' ');
			tree.forEachOutgoingEdge(node, e -> dumpTree0(tree, e.getTarget(), sb));
		}
		sb.append(']');
	}

	public static String dumpTree(Structure tree) {
		checkArgument("structure is not a tree", tree.getStructureType()==StructureType.TREE
				|| tree.getStructureType()==StructureType.CHAIN);

		StringBuilder sb = new StringBuilder();
		tree.forEachOutgoingEdge(tree.getVirtualRoot(), e -> dumpTree0(tree, e.getTarget(), sb));
		return sb.toString();
	}

	public static int compare(Item m1, Item m2) {
		ItemLayer fLayer1 = m1.getLayer().getFoundationLayer();
		ItemLayer fLayer2 = m2.getLayer().getFoundationLayer();
		if(fLayer1!=fLayer2)
			throw new ModelException(ModelErrorCode.MODEL_INCOMPATIBLE_FOUNDATIONS,
					Messages.incompatibleFoundationLayers(null, m1, m1, fLayer1, fLayer2));

		long result = m1.getBeginOffset()-m2.getBeginOffset();

		if(result==0) {
			result = m1.getEndOffset()-m2.getEndOffset();
		}

		return (int) result;
	}

	public static int compare(Fragment f1, Fragment f2) {
		if(f1.getLayer()!=f2.getLayer())
			throw new IllegalArgumentException("Cannot compare fragments from different fragment layers");

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
		ItemLayer fLayer1 = m1.getLayer().getFoundationLayer();
		ItemLayer fLayer2 = m2.getLayer().getFoundationLayer();
		if(fLayer1!=fLayer2)
			throw new ModelException(ModelErrorCode.MODEL_INCOMPATIBLE_FOUNDATIONS,
					Messages.incompatibleFoundationLayers(null, m1, m1, fLayer1, fLayer2));

		return m2.getBeginOffset()>=m1.getBeginOffset()
				&& m2.getEndOffset()<=m1.getEndOffset();
	}

	/**
	 * Validates that the given {@code begin} and {@code end} positions are valid wrt the
	 * rasterization used for the given {@code fragment}.
	 * <p>
	 * Note that this method will always abort without an exception for fragments that
	 * are <b>not</b> embedded in an actual layer.
	 *
	 * @param fragment
	 * @param begin
	 * @param end
	 *
	 * @throws ModelException of type {@link GlobalErrorCode#INVALID_INPUT} if both position
	 * arguments are {@code null}.
	 * @throws ModelException of type {@link ModelErrorCode#MODEL_INVALID_POSITION} if either
	 * {@code begin} or {@code end} are of incorrect {@link Position#getDimensionality() dimensionality}
	 * or if {@code end} is located before {@code begin} wrt the underlying {@link Metric}
	 * @throws ModelException of type {@link ModelErrorCode#MODEL_POSITION_OUT_OF_BOUNDS} if either
	 * of the two position arguments declares a {@link Position#getValue(int)} value which violates
	 * the {@link RasterAxis#getRasterSize(Item, FragmentLayer, Object) bounds} calculated for item
	 * {@link Fragment#getItem() associated with} the {@code fragment}.
	 */
	public static void checkFragmentPositions(Fragment fragment, Position begin, Position end) {
		FragmentLayer layer = fragment.getLayer();

		if(layer==null) {
			return;
		}

		if(begin==null && end==null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"At least one position must be non-null!");

		Rasterizer rasterizer = layer.getRasterizer();
		int dimensionality = rasterizer.getAxisCount();

		if(begin!=null && begin.getDimensionality()!=dimensionality)
			throw new ModelException(ModelErrorCode.MODEL_INVALID_POSITION,
					"Begin position dimensionality mismatch: expected "
					+dimensionality+" - got "+begin.getDimensionality());
		if(end!=null && end.getDimensionality()!=dimensionality)
			throw new ModelException(ModelErrorCode.MODEL_INVALID_POSITION,
					"End position dimensionality mismatch: expected "
					+dimensionality+" - got "+end.getDimensionality());

		Item item = fragment.getItem();

		for(int axis=0; axis<dimensionality; axis++) {
			long size = layer.getRasterSize(item, axis);
			checkPosition(size, begin, axis);
			checkPosition(size, end, axis);
		}

		if(begin!=null && end!=null && rasterizer.getMetric().compare(begin, end)>0)
			throw new ModelException(ModelErrorCode.MODEL_INVALID_POSITION,
					"Begin position must not exceed end position: "+begin+" - "+end);  //$NON-NLS-2$
	}

	private static void checkPosition(long size, Position p, int axis) {
		if(p==null) {
			return;
		}

		long value = p.getValue(axis);

		if(value<0 || value>=size)
			throw new ModelException(ModelErrorCode.MODEL_POSITION_OUT_OF_BOUNDS,
					"Invalid value for axis "+axis+" on position "+p+" - max size "+size);  //$NON-NLS-2$ //$NON-NLS-3$
	}

	public static Path pathToFile(ResourcePath path) {
		requireNonNull(path);
		if(path.getType()!=LocationType.LOCAL)
			throw new IllegalArgumentException("ResourcePath needs to be a file: "+path.getPath());

		return Paths.get(path.getPath());
	}

	public static URL pathToURL(ResourcePath path) throws MalformedURLException {
		requireNonNull(path);
		if(path.getType()!=LocationType.REMOTE)
			throw new IllegalArgumentException("ResourcePath needs to be a url: "+path.getPath());

		return new URL(path.getPath());
	}

	public static InputStream openPath(ResourcePath path) throws IOException {
		requireNonNull(path);

		switch (path.getType()) {
		case LOCAL:
			return Files.newInputStream(pathToFile(path));

		case REMOTE:
			return pathToURL(path).openStream();

		default:
			throw new IllegalArgumentException("Cannot handle source type: "+path.getType());
		}
	}
}
