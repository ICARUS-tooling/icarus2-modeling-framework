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
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.DependencyType;
import de.ims.icarus2.model.api.layer.HighlightLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * Abstracts away from the actual {@link Corpus} structure and allows
 * to inject virtual corpus content or alternative models to be used
 * in the matching process.
 *
 * @author Markus Gärtner
 *
 */
public abstract class CorpusData {

	public abstract LaneInfo resolveLane(IqlLane lane);
	public abstract ElementInfo resolveElement(LaneInfo lane, IqlProperElement element);
	public abstract Map<String, BindingInfo> bind(IqlBinding binding);

	/** Lookup a single annotation for single identifier */
	public abstract Optional<AnnotationInfo> findAnnotation(ElementInfo element, QualifiedIdentifier identifier);
	public abstract Optional<LayerRef> findLayer(String name);

	public static abstract class LayerRef {
		private final String id;

		protected LayerRef(String id) { this.id = requireNonNull(id); }

		public final String getId() { return id; }

//		public CorpusData getSource() { return CorpusData.this; }
	}

	public static final class CorpusBacked extends CorpusData {

		private static final Map<ManifestType, TypeInfo> typeTranslation = new EnumMap<>(ManifestType.class);
		static {
			typeTranslation.put(ManifestType.ITEM_LAYER_MANIFEST, TypeInfo.ITEM_LAYER);
			typeTranslation.put(ManifestType.STRUCTURE_LAYER_MANIFEST, TypeInfo.STRUCTURE_LAYER);
			typeTranslation.put(ManifestType.FRAGMENT_LAYER_MANIFEST, TypeInfo.FRAGMENT_LAYER);
			typeTranslation.put(ManifestType.ANNOTATION_LAYER_MANIFEST, TypeInfo.ANNOTATION_LAYER);
			typeTranslation.put(ManifestType.HIGHLIGHT_LAYER_MANIFEST, TypeInfo.of(HighlightLayer.class));
		}

		private final Corpus corpus;

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

			corpus = builder.corpus;
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
		public ElementInfo resolveElement(LaneInfo laneInfo, IqlProperElement element) {
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

			// Unbound element can stem from any base layer of current lane
			List<ItemLayer> baseLayers = layer(laneInfo.getLayer()).getBaseLayers().toList();
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
				throw EvaluationUtils.forUnknownIdentifier(name, "layer");
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
				checkState("Name already bound: "+name, !bindings.containsKey(name));
				bindings.put(name, new BindingInfo(layerRef, type));
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

		private class AnnotationCache {

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

			private Corpus corpus;

			private Scope scope;

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

			public Builder corpus(Corpus corpus) {
				requireNonNull(corpus);
				checkState("Corpus already set", this.corpus==null);
				this.corpus = corpus;
				return this;
			}

			@Override
			protected void validate() {
				checkState("Corpus not set", corpus!=null);
				checkState("Scope not set", scope!=null);
			}

			@Override
			protected CorpusBacked create() { return new CorpusBacked(this); }
		}
	}

	public static final class Virtual extends CorpusData {

	}
}
