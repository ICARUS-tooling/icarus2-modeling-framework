/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.query.api.engine;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.DependencyType;
import de.ims.icarus2.model.api.layer.HighlightLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationFlag;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.util.Graph;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.exp.AnnotationInfo;
import de.ims.icarus2.query.api.exp.BindingInfo;
import de.ims.icarus2.query.api.exp.ElementInfo;
import de.ims.icarus2.query.api.exp.EvaluationUtils;
import de.ims.icarus2.query.api.exp.LaneInfo;
import de.ims.icarus2.query.api.exp.QualifiedIdentifier;
import de.ims.icarus2.query.api.exp.TypeInfo;
import de.ims.icarus2.query.api.iql.IqlBinding;
import de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlReference;
import de.ims.icarus2.query.api.iql.IqlType;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.set.DataSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * Abstracts away from the actual {@link Corpus} structure and allows
 * to inject virtual corpus content or alternative models to be used
 * in the matching process.
 * <p>
 * An implementation is responsible for both providing the abstract
 * representations/pointers to corpus elements (e.g. via {@link #resolveLane(IqlLane)}
 * and also offering efficient direct access to the underlying data
 * when requested.
 *
 * @author Markus Gärtner
 *
 */
public abstract class CorpusData implements AutoCloseable {

	/** Retrieve information about the specified lane */
	public abstract LaneInfo resolveLane(IqlLane lane);
	/** Retrieve information about the specified element inside the given lane.
	 * For nested elements, the immediate parent must be supplied. */
	// TODO should be make it explicit that 'parent' must be of type IqlTreeNode ?
	public abstract ElementInfo resolveElement(LaneInfo lane, IqlProperElement element, @Nullable ElementInfo parentElement);
	/** Resolve the given binding and return mappings for all the contained members */
	public abstract Map<String, BindingInfo> bind(IqlBinding binding);

	/** Lookup a single annotation for single identifier */
	public abstract Optional<AnnotationInfo> findAnnotation(ElementInfo element, QualifiedIdentifier identifier);
	/** Lookup a layer by name. This only resolved native layers! */
	public abstract Optional<LayerRef> findLayer(String name);
	/** Provide actual access to the given layer via a simple lookup interface */
	public abstract LongFunction<Container> access(LayerRef layer);
	/** Provide (lazy) access to the mapping facilities between {@code source} and {@code target} */
	public abstract LaneMapper map(LayerRef source, LayerRef target);

	@Override
	public abstract void close();

	public static class LayerRef {
		private final String id;

		LayerRef(String id) { this.id = requireNonNull(id); }

		public final String getId() { return id; }

		@Override
		public int hashCode() { return id.hashCode(); }

		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} else if(obj instanceof LayerRef) {
				return id.equals(((LayerRef)obj).id);
			}
			return false;
		}

		@Override
		public String toString() { return "LayerRef@"+id; }

	}

	public static final class CorpusBacked extends CorpusData {

		public static Builder builder() { return new Builder(); }

		private static final Map<ManifestType, TypeInfo> typeTranslation = new EnumMap<>(ManifestType.class);
		static {
			typeTranslation.put(ManifestType.ITEM_LAYER_MANIFEST, TypeInfo.ITEM_LAYER);
			typeTranslation.put(ManifestType.STRUCTURE_LAYER_MANIFEST, TypeInfo.STRUCTURE_LAYER);
			typeTranslation.put(ManifestType.FRAGMENT_LAYER_MANIFEST, TypeInfo.FRAGMENT_LAYER);
			typeTranslation.put(ManifestType.ANNOTATION_LAYER_MANIFEST, TypeInfo.ANNOTATION_LAYER);
			typeTranslation.put(ManifestType.HIGHLIGHT_LAYER_MANIFEST, TypeInfo.of(HighlightLayer.class));
		}

		/**
		 *  Contains additional layers that have not received a dedicated reference in the
		 *  original query definition. This includes for instance those layers that got
		 *  added transitively via dependencies on other (named) layers.
		 */
		private final Scope scope;

		/** Efficient lookup for collecting annotation layer keys */
		private final Graph<Layer> layerGraph;

		/**
		 * Maps all the usable raw names and/or aliases to layer entries.
		 * Note that a single layer can end up twice in this lookup map if
		 * it has been assigned an alias!
		 */
		private final Map<String, Layer> layers;

		/**
		 * For every item layer in the scope lists all the annotation layers that
		 * can produce annotations for items in that layer, including catch-all layers.
		 */
		private final Map<ItemLayer, Set<AnnotationLayer>> annotationSources;

		/**
		 * All the layers that have bound member declarations in the query.
		 */
		private final Map<String, ItemLayer> boundLayers = new Object2ObjectOpenHashMap<>();

		private final Map<ElementInfo, AnnotationCache> annotationCaches = new Object2ObjectOpenHashMap<>();

		private CorpusBacked(Builder builder) {

			scope = builder.scope;
			layers = new Object2ObjectOpenHashMap<>(builder.namedLayers);

			for(Layer layer : scope.getLayers()) {
				String key = key(layer);
				Layer previous = layers.putIfAbsent(key, layer);

				// Make sure users can't (accidentally) shadow layers with new aliases
				if(previous!=null && previous!=layer)
					throw new QueryException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
							String.format("Reference to layer '%s' is shadowed by (aliased) entry '%s' for id: %s",
									getName(layer), getName(previous), key));
			}

			// Create a complete dependency graph of all (and only those) layers included in the scope
			layerGraph = Graph.layerGraph(scope.getLayers(), scope::containsLayer, DependencyType.STRONG);

			annotationSources = new Reference2ObjectOpenHashMap<>();

			// For every annotation layer record the dependency tree
			for(AnnotationLayer aLayer : scope.getLayers().stream()
					.filter(ModelUtils::isAnnotationLayer)
					.map(AnnotationLayer.class::cast)
					.toArray(AnnotationLayer[]::new)) {

				// Transitive closure of dependency relation if any form of deep annotations is allowed
				AnnotationLayerManifest manifest = aLayer.getManifest();
				boolean allowsTransitiveAnnotation =
						manifest.isAnnotationFlagSet(AnnotationFlag.DEEP_ANNOTATION)
						|| manifest.isAnnotationFlagSet(AnnotationFlag.ELEMENT_ANNOTATION)
						|| manifest.isAnnotationFlagSet(AnnotationFlag.EDGE_ANNOTATION)
						|| manifest.isAnnotationFlagSet(AnnotationFlag.NODE_ANNOTATION);

				layerGraph.walkGraph(singleton(aLayer), false, layer -> {
					if(ModelUtils.isItemLayer(layer)) {
						ItemLayer iLayer = (ItemLayer)layer;
						annotationSources.computeIfAbsent(iLayer, k -> new ReferenceOpenHashSet<>()).add(aLayer);
					}
					return layer==aLayer || allowsTransitiveAnnotation;
				});
			}
		}

		private static String key(Layer layer) {
			return ManifestUtils.requireId(layer.getManifest());
		}

		private static <L extends Layer> L layer(LayerRef ref) {
			return ((LayerRefImpl)ref).getLayer();
		}

		@Override
		public LaneInfo resolveLane(IqlLane lane) {
			Layer layer;
			LayerRef ref;
			// If lane is a proxy, we have no other lanes and need to use the scope's primary layer
			if(lane.isProxy()) {
				layer = scope.getPrimaryLayer();
				ref = new LayerRefImpl(layer);
			} else {
				String name = lane.getName();
				layer = layers.get(name);
				if(layer==null)
					throw EvaluationUtils.forUnknownIdentifier(name, "layer");
				ref = new LayerRefImpl(name, layer);
			}

			if(!ModelUtils.isItemLayer(layer))
				throw new QueryException(QueryErrorCode.INCOMPATIBLE_REFERENCE,
						"Lane name must reference an item or structure layer: "+getName(layer));
			TypeInfo type = typeTranslation.get(layer.getManifest().getManifestType());
			requireNonNull(type, "unknown layer type for "+layer.getName());

			return new LaneInfo(lane, type, ref);
		}

		@Override
		public ElementInfo resolveElement(LaneInfo laneInfo, IqlProperElement element,
				@Nullable ElementInfo parentElement) {
			requireNonNull(element);

			// Edges can only be referenced from the surrounding structure
			if(element.getType()==IqlType.EDGE) {
				ItemLayer layer = layer(laneInfo.getLayer());
				if(!ModelUtils.isStructureLayer(layer))
					throw EvaluationUtils.forIncorrectUse(
							"Edge outside of structure layer scope in lane: %s", getName(layer));
				LayerRef ref = new LayerRefImpl(layer);
				return new ElementInfo(element, TypeInfo.EDGE, list(ref));
			}

			String label = element.getLabel().orElse(null);
			if(label!=null) {
				ItemLayer layer = resolveBoundLayer(label);
				// Explicitly bound element -> can only have 1 source layer
				LayerRef ref = new LayerRefImpl(label, layer);
				TypeInfo type = ModelUtils.isFragmentLayer(layer) ? TypeInfo.FRAGMENT : TypeInfo.ITEM;
				return new ElementInfo(element, type, list(ref));
			}

			// Unbound element can stem from any base layer of current lane or parent node
			List<ItemLayer> baseLayers;
			if(parentElement!=null) {
				Set<ItemLayer> layerCandidates = new ObjectLinkedOpenHashSet<>();
				parentElement.getLayers().stream()
					.<ItemLayer>map(CorpusBacked::layer)
					.map(ItemLayer::getBaseLayers)
					.map(DataSet::toSet)
					.flatMap(Set::stream)
					.filter(scope::containsLayer)
					.forEach(layerCandidates::add);
				baseLayers = new ObjectArrayList<>(layerCandidates);
			} else {
				baseLayers = layer(laneInfo.getLayer()).getBaseLayers()
						.toList()
						.stream()
						.filter(scope::containsLayer)
						.collect(Collectors.toList());
			}
			if(baseLayers.isEmpty())
				throw EvaluationUtils.forIncorrectUse("Cannot use non-aggregating layer as lane: %s",
						getName(laneInfo.getLayer()));
			List<LayerRef> refs = new ObjectArrayList<>();
			baseLayers.forEach(layer -> refs.add(new LayerRefImpl(layer)));

			// Need to consider individual layer types
			Set<ManifestType> layerTypes = baseLayers.stream()
					.map(ItemLayer::getManifest)
					.map(TypedManifest::getManifestType)
					.distinct()
					.collect(Collectors.toSet());

			TypeInfo type;
			if(layerTypes.size()==1 && layerTypes.contains(ManifestType.FRAGMENT_LAYER_MANIFEST)) {
				type = TypeInfo.FRAGMENT;
			} else {
				// "Fall-through" type if we get mixed sources or not enough info
				type = TypeInfo.ITEM;
			}

			return new ElementInfo(element, type, refs);
		}

		@Override
		public Optional<AnnotationInfo> findAnnotation(ElementInfo element, QualifiedIdentifier identifier) {
			requireNonNull(element);
			requireNonNull(identifier);

			return annotationCaches.computeIfAbsent(element, AnnotationCache::new).findAnnotation(identifier);
		}

		@Override
		public Optional<LayerRef> findLayer(String name) {
			return Optional.ofNullable(layers.get(name))
					.map(layer -> new LayerRefImpl(name, layer));
		}

		@SuppressWarnings("unchecked")
		private <L extends Layer> L findLayer(String name, Class<L> requiredType) {
			Layer layer = layers.get(name);
			if(layer==null)
				throw EvaluationUtils.forUnknownIdentifier(name, "layer");

			if(!requiredType.isInstance(layer))
				throw new QueryException(QueryErrorCode.INCOMPATIBLE_REFERENCE,
						String.format("Expected layer compatible to %s at name '%s', but got %s",
								requiredType, name, layer.getClass()));
			return (L) layer;
		}

		private ItemLayer resolveBoundLayer(String name) {
			ItemLayer layer = boundLayers.get(name);
			if(layer==null)
				throw EvaluationUtils.forUnknownIdentifier(name, "bound layer");
			return layer;
		}

		@Override
		public Map<String, BindingInfo> bind(IqlBinding binding) {
			requireNonNull(binding);

			Map<String, BindingInfo> bindings = new Object2ObjectOpenHashMap<>();

			String target = binding.getTarget();
			Layer layer = layers.get(target);
			if(layer==null)
				throw EvaluationUtils.forUnknownIdentifier(target, "bound layer");
			if(!ModelUtils.isItemLayer(layer))
				throw EvaluationUtils.forIncorrectUse("Binding target for '%s' must be an item layer - got %s",
						target, layer.getManifest().getManifestType());
			ItemLayer targetLayer = (ItemLayer)layer;
			TypeInfo type = resolveMemberType(targetLayer, binding);
			LayerRef layerRef = new LayerRefImpl(target, targetLayer);

			for(IqlReference ref : binding.getMembers()) {
				String name = ref.getName();
				checkState("Name already bound: "+name, !boundLayers.containsKey(name));
				bindings.put(name, new BindingInfo(layerRef, type));
				boundLayers.put(name, targetLayer);
			}

			return bindings;
		}

		private TypeInfo resolveMemberType(ItemLayer layer, IqlBinding binding) {
			// Easy mode: specifically declared edge type means we don't need to run further checks
			if(binding.isEdges()) {
				return TypeInfo.EDGE;
			}

			if(ModelUtils.isFragmentLayer(layer)) {
				// Fragment layers can only contain fragments as top-level elements
				return TypeInfo.FRAGMENT;
			} else if(ModelUtils.isStructureLayer(layer)) {
				// Structure layer can only contain structures as top-level elements
				//TODO verify that assumption!!
				return TypeInfo.STRUCTURE;
			}

			/*
			 * Leftover cases: Layer can contain structures, containers or bare items
			 * as top-level elements. We need to inspect the manifest to answer this
			 * question properly.
			 */

			Optional<ContainerManifestBase<?>> root = layer.getManifest().getRootContainerManifest();
			if(root.isPresent()) {
				// Some kind of nested hierarchy, so must be container or structure
				if(root.get().getManifestType()==ManifestType.STRUCTURE_MANIFEST) {
					return TypeInfo.STRUCTURE;
				}
				return TypeInfo.CONTAINER;
			}

			// Nothing fancy, layer only holds generic items
			return TypeInfo.ITEM;
		}

		@Override
		public LongFunction<Container> access(LayerRef layerRef) {
			ItemLayer layer = layer(layerRef);
			ItemLayerManager itemLayerManager = layer.getContext().getDriver();
			return index  -> (Container) itemLayerManager.getItem(layer, index);
		}

		@Override
		public LaneMapper map(LayerRef sourceRef, LayerRef targetRef) {
			ItemLayer source = layer(sourceRef);
			ItemLayer target = layer(targetRef);

			Mapping mapping = source.getContext().getDriver().getMapping(source, target);
			return LaneMapper.forMapping(mapping.newReader(), 1024);
		}

		@Override
		public void close() {
			layers.clear();
			boundLayers.clear();
			annotationCaches.values().forEach(AnnotationCache::close);
		}

		private static class AnnotationLink {
			private final AnnotationManifest manifest;
			private final AnnotationLayer layer;
			private final boolean isAlias;

			public AnnotationLink(AnnotationManifest manifest, AnnotationLayer layer, boolean isAlias) {
				this.manifest = requireNonNull(manifest);
				this.layer = requireNonNull(layer);
				this.isAlias = isAlias;
			}

			public AnnotationManifest getManifest() { return manifest; }

			public AnnotationLayer getLayer() { return layer; }

			public boolean isNotAlias() { return !isAlias; }
		}

		private class AnnotationCache implements AutoCloseable {

			private final ElementInfo elementInfo;

			/**
			 * Lazily constructed lookup to map from annotation keys to actual sources.
			 * This map contains all the annotations that target instances of this context's
			 * element and that can be referenced by name.
			  */
			private Map<String, List<AnnotationLink>> annotationLookup;

			/** Layers that allow unknown keys for annotations */
			private List<AnnotationLayer> catchAllLayers;

			/** Maps variable names to assignable expressions */
			private final Map<String, AnnotationInfo> cache = new Object2ObjectOpenHashMap<>();

			public AnnotationCache(ElementInfo elementInfo) {
				this.elementInfo = requireNonNull(elementInfo);
			}

			private void ensureLayerLookups(ElementInfo elementInfo) {
				if(catchAllLayers==null || annotationLookup==null) {

					List<AnnotationLayer> catchAllLayers = new ArrayList<>();
					Map<String, List<AnnotationLink>> annotationLookup = new Object2ObjectOpenHashMap<>();

					Consumer<AnnotationLayer> feedLayer = layer -> {
						layer.getManifest().forEachAnnotationManifest(manifest -> {
							// Fetch raw key
							String key = ManifestUtils.require(manifest, AnnotationManifest::getKey, "key");
							// Store a "hard" link via raw key
							annotationLookup.computeIfAbsent(key, k -> new ArrayList<>())
								.add(new AnnotationLink(manifest, layer, false));
							// For every alias store a soft link
							manifest.forEachAlias(alias -> annotationLookup.computeIfAbsent(alias, k -> new ArrayList<>())
									.add(new AnnotationLink(manifest, layer, true)));
						});
						// Keep track of every annotation layer that allows unknown keys
						if(layer.getManifest().isAnnotationFlagSet(AnnotationFlag.UNKNOWN_KEYS)) {
							catchAllLayers.add(layer);
						}
					};

					boolean isEdge = elementInfo.isEdge();

					for(LayerRef layerRef : elementInfo.getLayers()) {
						ItemLayer itemLayer = layer(layerRef);
						for(AnnotationLayer aLayer : annotationSources.getOrDefault(itemLayer, Collections.emptySet())) {

							AnnotationLayerManifest manifest = aLayer.getManifest();

							// Edges can only receive annotations from specially marked annotation layers
							if(!isEdge || manifest.isAnnotationFlagSet(AnnotationFlag.DEEP_ANNOTATION)
									|| manifest.isAnnotationFlagSet(AnnotationFlag.ELEMENT_ANNOTATION)
									|| manifest.isAnnotationFlagSet(AnnotationFlag.EDGE_ANNOTATION)) {
								feedLayer.accept(aLayer);
							}
						}
					}

					// Only transfer collections over if they're not empty
					this.catchAllLayers = catchAllLayers.isEmpty() ? Collections.emptyList() : catchAllLayers;
					this.annotationLookup = annotationLookup.isEmpty() ? Collections.emptyMap() : annotationLookup;
				}
			}

			private Optional<AnnotationInfo> findAnnotation(QualifiedIdentifier identifier) {

				String rawKey = identifier.getRawText();

				// Give our cache a chance first
				AnnotationInfo info = cache.get(rawKey);
				if(info!=null) {
					return Optional.of(info);
				}

				// No luck with the cache -> run actual search now
				ensureLayerLookups(elementInfo);
				String key = identifier.getElement();
				AnnotationLayer expectedLayer;
				if(identifier.hasHost()) {
					String layerId = identifier.getHost().get();
					// If layer is explicitly stated, it must resolve to an annotation layer
					expectedLayer = findLayer(layerId, AnnotationLayer.class);
				} else {
					expectedLayer = null;
				}

				// If present use the explicitly specified layer as filter
				List<AnnotationLink> hits = annotationLookup.getOrDefault(key, Collections.emptyList())
						.stream()
						.filter(link -> !identifier.hasHost() || link.getLayer()==expectedLayer)
						.collect(Collectors.toList());

				AnnotationInfo result = null;

				if(hits.size()==1) {
					// Easy mode: only 1 annotation in total linked to key
					result = fromLink(rawKey, hits.get(0));
				} if(!hits.isEmpty()) {
					// Filter only proper (non aliased) entries
					List<AnnotationLink> properLinks = hits.stream()
							.filter(AnnotationLink::isNotAlias)
							.collect(Collectors.toList());

					if(properLinks.size()==1) {
						// Key uniquely matches a single proper annotation
						result = fromLink(rawKey, properLinks.get(0));
					} else if(!properLinks.isEmpty()) {
						// Key not unique, one of the only error occasions in this resolution process
						String[] names = properLinks.stream()
								.map(ModelUtils::getName)
								.toArray(String[]::new);
						throw new QueryException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
								String.format("Key '%s' is ambiguous and links to multiple annotations: %s",
										rawKey, Arrays.toString(names)));
					}
				} else {
					// Directly accessible links failed, now we need to try catch-all layers

					/*
					 *  Can't do the shortcut and grab 'expectedLayer' directly, as we still need
					 *  to make sure that we only consider layers that can target this context's element.
					 */
					List<AnnotationLayer> layers = catchAllLayers.stream()
							.filter(layer -> !identifier.hasHost() || layer==expectedLayer)
							.collect(Collectors.toList());

					if(layers.size()==1) {
						result = forCatchAll(rawKey, key, layers.get(0));
					} else if(!layers.isEmpty()) {
						String[] names = layers.stream()
								.map(ModelUtils::getName)
								.toArray(String[]::new);
						throw new QueryException(QueryErrorCode.INCOMPATIBLE_REFERENCE,
								String.format("Key '%s' can be handled by multiple catch-all layers: %s",
										rawKey, Arrays.toString(names)));
					}
				}

				if(identifier.hasHost() && result==null)
					throw new QueryException(QueryErrorCode.UNKNOWN_IDENTIFIER, String.format(
							"Qualified identifier '%s' could not be resolved to an annotation", rawKey));

				if(result!=null) {
					cache.put(rawKey, result);
				}

				return Optional.ofNullable(result);
			}

			private AnnotationInfo fromLink(String rawKey, AnnotationLink link) {
				AnnotationManifest manifest = link.getManifest();
				final String key = ManifestUtils.require(manifest, AnnotationManifest::getKey, "key");
				final TypeInfo type = EvaluationUtils.typeFor(manifest.getValueType());
				AnnotationInfo.Builder builder = AnnotationInfo.builer()
						.rawKey(rawKey)
						.key(key)
						.type(type)
						.valueType(manifest.getValueType());

				createSources(builder, type, key, link.getLayer().getAnnotationStorage());

				return builder.build();
			}

			private AnnotationInfo forCatchAll(String rawKey, String key,
					AnnotationLayer layer) {
				AnnotationInfo.Builder builder = AnnotationInfo.builer()
						.rawKey(rawKey)
						.key(key)
						.type(TypeInfo.GENERIC)
						.valueType(ValueType.UNKNOWN);

				createSources(builder, TypeInfo.GENERIC, key, layer.getAnnotationStorage());

				return builder.build();
			}

			private void createSources(AnnotationInfo.Builder builder, TypeInfo type,
					String key, AnnotationStorage storage) {
				if(TypeInfo.isInteger(type)) {
					builder.integerSource(item -> storage.getLong(item, key));
				} else if(TypeInfo.isFloatingPoint(type)) {
					builder.floatingPointSource(item -> storage.getDouble(item, key));
				} else if(TypeInfo.isBoolean(type)) {
					builder.booleanSource(item -> storage.getBoolean(item, key));
				} else if(TypeInfo.isText(type)) {
					builder.objectSource(item -> storage.getString(item, key));
				} else {
					builder.objectSource(item -> storage.getValue(item, key));
				}
			}

			@Override
			public void close() {
				annotationLookup.clear();
				catchAllLayers.clear();
				cache.clear();
			}
		}

		private static final class LayerRefImpl extends LayerRef {
			private final Layer layer;
			private LayerRefImpl(Layer layer) {
				super(layer.getName());
				this.layer = layer;
			}
			private LayerRefImpl(String id, Layer layer) {
				super(id);
				this.layer = requireNonNull(layer);
			}
			@SuppressWarnings("unchecked")
			public <L extends Layer> L getLayer() { return (L) layer; }
		}

		public static class Builder extends AbstractBuilder<Builder, CorpusBacked> {

			/** Maps the usable raw names or aliases to layer entries. */
			private final Map<String, Layer> namedLayers = new Object2ObjectOpenHashMap<>();

			private Scope scope;

			private Builder() { /* no-op */ }

			public Builder namedLayer(String alias, Layer layer) {
				requireNonNull(alias);
				requireNonNull(layer);
				checkState("Alias already used: "+alias, !namedLayers.containsKey(alias));
				namedLayers.put(alias, layer);
				return this;
			}

			public Builder scope(Scope scope) {
				requireNonNull(scope);
				checkState("Scope already set", this.scope==null);
				this.scope = scope;
				return this;
			}


			@Override
			protected void validate() {
				checkState("Scope not set", scope!=null);
			}

			@Override
			protected CorpusBacked create() { return new CorpusBacked(this); }
		}
	}

	public static final class Virtual extends CorpusData {

		public static Builder builder() { return new Builder(); }

		/** Maps registered (item-)layers to descriptor (including content) */
		private final Map<String, LayerInfo> layers = new Object2ObjectOpenHashMap<>();
		/** Maps (src_id+dest_id) to proper mapper objects */
		private final Map<String, LaneMapper> mappers = new Object2ObjectOpenHashMap<>();
		/** Direct lookup for annotation keys. Mapped values might be duplicates if keys are aliased */
		private final Map<String, AnnotationInfo> annotations = new Object2ObjectOpenHashMap<>();
		/** Maps aliases to original layer ids */
		private final Map<String, String> boundLayers = new Object2ObjectOpenHashMap<>();

		private Virtual(Builder builder) {
			layers.putAll(builder.layers);
			mappers.putAll(builder.mappers);
			annotations.putAll(builder.annotations);
		}

		private static String mappingKey(String source, String target) {
			return source + "->" + target;
		}

		private LayerInfo requireLayer(String name) {
			return Optional.ofNullable(layers.get(name)).orElseThrow(() -> EvaluationUtils.forUnknownIdentifier(name, "layer"));
		}

		@Override
		public LaneInfo resolveLane(IqlLane lane) {
			LayerInfo info = requireLayer(lane.getName());
			return new LaneInfo(lane, info.type, info.ref);
		}

		@Override
		public ElementInfo resolveElement(LaneInfo lane, IqlProperElement element, @Nullable ElementInfo parentElement) {
			String label = element.getLabel().orElse(null);
			if(label!=null) {
				String name = boundLayers.get(label);
				if(name==null)
					throw EvaluationUtils.forUnknownIdentifier(name, "bound layer");
				 return new ElementInfo(element, TypeInfo.ITEM, list(new LayerRef(name)));
			}

			LayerInfo info = requireLayer(lane.getLayer().getId());
			return new ElementInfo(element, TypeInfo.ITEM, info.sources);
		}

		@Override
		public Map<String, BindingInfo> bind(IqlBinding binding) {

			Map<String, BindingInfo> bindings = new Object2ObjectOpenHashMap<>();

			LayerInfo info = requireLayer(binding.getTarget());

			for(IqlReference ref : binding.getMembers()) {
				String name = ref.getName();
				checkState("Name already bound: "+name, !boundLayers.containsKey(name));
				bindings.put(name, new BindingInfo(info.ref, info.type));
				boundLayers.put(name, info.ref.getId());
			}

			return bindings;
		}

		/** Looks up the annotation with the full {@link QualifiedIdentifier#toString() identifier string}. */
		@Override
		public Optional<AnnotationInfo> findAnnotation(ElementInfo element, QualifiedIdentifier identifier) {
			return Optional.ofNullable(annotations.get(identifier.toString()));
		}

		@Override
		public Optional<LayerRef> findLayer(String name) {
			return Optional.ofNullable(layers.get(requireNonNull(name))).map(info -> info.ref);
		}

		@Override
		public LongFunction<Container> access(LayerRef layer) {
			List<Item> buffer = requireNonNull(layers.get(layer.getId())).elements;
			return index -> (Container)buffer.get(strictToInt(index));
		}

		@Override
		public LaneMapper map(LayerRef source, LayerRef target) {
			String key = mappingKey(source.getId(), target.getId());
			return requireNonNull(mappers.get(key), "no such mapping");
		}

		@Override
		public void close() {
			// TODO clear all lookup structures
		}

		/** Special descriptor for item layers. */
		private static class LayerInfo {
			/** Top-level members of the layer */
			private final List<Item> elements = new ObjectArrayList<>();
			/** Type of top-level members */
			private TypeInfo type = TypeInfo.CONTAINER;
			/** Reference to the abstract layer */
			private final LayerRef ref;
			/** Source layers for elements in this layer */
			private final List<LayerRef> sources = new ObjectArrayList<>();
			/** Keys of annotation layers available for this layer */
			private final Set<String> annotations = new ObjectOpenHashSet<>();

			private LayerInfo(String name) { ref = new LayerRef(name); }
		}

		public static class Builder extends AbstractBuilder<Builder, Virtual> {

			/** Maps registered (item-)layers to descriptor (including content) */
			private final Map<String, LayerInfo> layers = new Object2ObjectOpenHashMap<>();
			/** Maps (src_id+dest_id) to proper mapper objects */
			private final Map<String, LaneMapper> mappers = new Object2ObjectOpenHashMap<>();
			/** Direct lookup for annotation keys. Mapped values might be duplicates if keys are aliased */
			private final Map<String, AnnotationInfo> annotations = new Object2ObjectOpenHashMap<>();

			/** Layers that have their element lists locked due to usage of indirect mapping methods for annotations */
			private final Set<String> lockedLayers = new ObjectOpenHashSet<>();


			private void addLayer(LayerInfo info) { layers.put(info.ref.getId(), info); }

			private void addAnnotation(AnnotationInfo info, Set<String> lockedLayers) {
				annotations.put(info.getRawKey(), info);
				this.lockedLayers.addAll(lockedLayers);
			}

			public LayerBuilder layer(String name) {
				requireNonNull(name);
				checkState("layer already registered: "+name, !layers.containsKey(name));
				return new LayerBuilder(this, name);
			}

			public AnnotationBuilder annotation() {
				return new AnnotationBuilder(this);
			}

			public Builder mapper(String source, String target, LaneMapper mapper) {
				String key = mappingKey(source, target);
				checkState("mapping already registered: "+key, !mappers.containsKey(key));
				mappers.put(key, requireNonNull(mapper));
				return this;
			}


			@Override
			protected Virtual create() { return new Virtual(this); }

		}

		public static class LayerBuilder {
			private final Builder host;

			private LayerInfo info;

			private LayerBuilder(Builder host, String name) {
				this.host = requireNonNull(host);
				info = new LayerInfo(name);
			}

			public LayerBuilder elements(Item...containers) {
				CollectionUtils.feedItems(info.elements, containers);
				return this;
			}

			public LayerBuilder elements(List<? extends Item> containers) {
				info.elements.addAll(containers);
				return this;
			}

			public LayerBuilder type(TypeInfo type) {
				info.type = requireNonNull(type);
				return this;
			}

			/** Assign base layer ids */
			public LayerBuilder sources(String...layerIds) {
				Stream.of(layerIds).map(LayerRef::new).forEach(info.sources::add);
				return this;
			}

			/** Assign base layer ids */
			public LayerBuilder sources(Collection<String> layerIds) {
				layerIds.stream().map(LayerRef::new).forEach(info.sources::add);
				return this;
			}

			/** Assign annotation layer ids */
			public LayerBuilder annotations(String...layerIds) {
				checkArgument("layer id array is empty", layerIds.length>0);
				CollectionUtils.feedItems(info.annotations, layerIds);
				return this;
			}

			/** Assign annotation layer ids */
			public LayerBuilder annotations(Collection<String> layerIds) {
				checkArgument("layer id list is empty", !layerIds.isEmpty());
				info.annotations.addAll(layerIds);
				return this;
			}

			public Builder commit() {
				// Validate built layer info
				checkState("layer empty", !info.elements.isEmpty());
				if(info.type==TypeInfo.CONTAINER) {
					checkState("container layer must have at least 1 base layer", !info.sources.isEmpty());
				}
				// Add to surrounding builder
				host.addLayer(info);
				// Invalidate local copy to prevent future modifications leaking through
				info = null;
				return host;
			}
		}

		public static class AnnotationBuilder {
			private final Builder host;
			private final Set<String> targets = new ObjectOpenHashSet<>();

			private AnnotationInfo.Builder builder;

			private AnnotationBuilder(Builder host) {
				this.host = requireNonNull(host);

				builder = AnnotationInfo.builer();
			}

			/** Sets both the raw key and actual annotation key to the given value */
			public AnnotationBuilder key(String key) {
				builder.rawKey(key);
				builder.key(key);
				return this;
			}

			/** Sets the raw key and annotation key independently */
			public AnnotationBuilder key(String rawKey, String key) {
				builder.rawKey(rawKey);
				builder.key(key);
				return this;
			}

			public AnnotationBuilder targets(String...names) {
				requireNonNull(names);
				checkArgument("array of target names is empty", names.length>0);
				CollectionUtils.feedItems(targets, names);
				return this;
			}

			public AnnotationBuilder targets(Set<String> names) {
				requireNonNull(names);
				checkArgument("set of target names is empty", !names.isEmpty());
				targets.addAll(names);
				return this;
			}

			private void checkSingularTarget() {
				checkState("no target layers defined", !targets.isEmpty());
				checkState("can't use mapping shortcuts with more than one target", targets.size()==1);
			}

			private List<Item> items(int expectedSize) {
				checkSingularTarget();
				String target = targets.iterator().next();
				LayerInfo info = host.layers.get(target);
				requireNonNull(info, "target layer not found: "+target);
				checkState("target layer contains no items: "+target, !info.elements.isEmpty());
				checkState(Messages.sizeMismatch("target layer size mismatch", expectedSize, info.elements.size()),
						info.elements.size()==expectedSize);

				return info.elements;
			}

			// Externalized methods

			public AnnotationBuilder integers(ToLongFunction<Item> lookup) {
				builder.type(TypeInfo.INTEGER);
				builder.integerSource(lookup);
				return this;
			}

			public AnnotationBuilder floatingPoints(ToDoubleFunction<Item> lookup) {
				builder.type(TypeInfo.FLOATING_POINT);
				builder.floatingPointSource(lookup);
				return this;
			}

			public AnnotationBuilder booleans(Predicate<Item> lookup) {
				builder.type(TypeInfo.BOOLEAN);
				builder.booleanSource(lookup);
				return this;
			}

			public AnnotationBuilder generics(Function<Item,Object> lookup) {
				builder.type(TypeInfo.GENERIC);
				builder.objectSource(lookup);
				return this;
			}

			// Shortcut methods

			/** Maps members of the assigned target layer to the given long values, in
			 * the same order. Will fail if any annotations have been set for the target
			 * layer already or if the number of annotation values and items in the target
			 * layer do not match. */
			public AnnotationBuilder integers(long...values) {
				builder.type(TypeInfo.INTEGER);
				List<Item> items = items(values.length);
				Object2LongMap<Item> map = new Object2LongOpenHashMap<>(values.length);
				for (int i = 0; i < values.length; i++) {
					map.put(items.get(i), values[i]);
				}
				builder.integerSource(map::getLong);
				return this;
			}

			/** Maps members of the assigned target layer to the given boolean values, in
			 * the same order. Will fail if any annotations have been set for the target
			 * layer already or if the number of annotation values and items in the target
			 * layer do not match. */
			public AnnotationBuilder booleans(boolean...values) {
				builder.type(TypeInfo.INTEGER);
				List<Item> items = items(values.length);
				Object2BooleanMap<Item> map = new Object2BooleanOpenHashMap<>(values.length);
				for (int i = 0; i < values.length; i++) {
					map.put(items.get(i), values[i]);
				}
				builder.booleanSource(map::getBoolean);
				return this;
			}

			/** Maps members of the assigned target layer to the given string values, in
			 * the same order. Will fail if any annotations have been set for the target
			 * layer already or if the number of annotation values and items in the target
			 * layer do not match. */
			public AnnotationBuilder floatingPoints(double...values) {
				builder.type(TypeInfo.FLOATING_POINT);
				List<Item> items = items(values.length);
				Object2DoubleMap<Item> map = new Object2DoubleOpenHashMap<>(values.length);
				for (int i = 0; i < values.length; i++) {
					map.put(items.get(i), values[i]);
				}
				builder.floatingPointSource(map::getDouble);
				return this;
			}

			/** Maps members of the assigned target layer to the given string values, in
			 * the same order. Will fail if any annotations have been set for the target
			 * layer already or if the number of annotation values and items in the target
			 * layer do not match. */
			public AnnotationBuilder strings(String...values) {
				builder.type(TypeInfo.TEXT);
				List<Item> items = items(values.length);
				Map<Item,String> map = new Object2ObjectOpenHashMap<>(values.length);
				for (int i = 0; i < values.length; i++) {
					map.put(items.get(i), values[i]);
				}
				builder.objectSource(map::get);
				return this;
			}

			/** Maps members of the assigned target layer to the given generic values, in
			 * the same order. Will fail if any annotations have been set for the target
			 * layer already or if the number of annotation values and items in the target
			 * layer do not match. */
			public AnnotationBuilder generic(Object...values) {
				builder.type(TypeInfo.GENERIC);
				List<Item> items = items(values.length);
				Map<Item,Object> map = new Object2ObjectOpenHashMap<>(values.length);
				for (int i = 0; i < values.length; i++) {
					map.put(items.get(i), values[i]);
				}
				builder.objectSource(map::get);
				return this;
			}

			public Builder commit() {
				host.addAnnotation(builder.build(), targets);
				builder = null;
				return host;
			}
		}
	}
}
