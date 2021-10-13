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
package de.ims.icarus2.query.api.exp;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.DependencyType;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
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
import de.ims.icarus2.query.api.QuerySwitch;
import de.ims.icarus2.query.api.engine.ext.EngineConfigurator;
import de.ims.icarus2.query.api.exp.Environment.NsEntry;
import de.ims.icarus2.query.api.iql.IqlBinding;
import de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlReference;
import de.ims.icarus2.query.api.iql.IqlType;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.set.DataSet;
import de.ims.icarus2.util.concurrent.CloseableThreadLocal;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * Utility class(es) to store the final configuration and bindings for a query evaluation
 * on a single stream/corpus.
 *
 * @author Markus Gärtner
 *
 */
@NotThreadSafe
public abstract class EvaluationContext {

	private static final Logger log = LoggerFactory.getLogger(EvaluationContext.class);

	public static RootContextBuilder rootBuilder() {
		return new RootContextBuilder();
	}

	private enum ContextType {
		ROOT,
		LANE,
		ELEMENT,
		;
	}

	private static class BindingInfo {
		private final ItemLayer layer;
		private final boolean edges;

		public BindingInfo(ItemLayer layer, boolean edges) {
			this.layer = requireNonNull(layer);
			this.edges = edges;
		}

		/** The layer this binding referrs to */
		public ItemLayer getLayer() { return layer; }

		/** In case of {@link StructureLayer}s, bindings can target edges */
		public boolean isEdges() { return edges; }
	}

	@NotThreadSafe
	public static class RootContext extends EvaluationContext {

		/** The corpus this context refers to. */
		private final Corpus corpus;

		/**
		 * Maps all the usable raw names and/or aliases to layer entries.
		 * Note that a single layer can end up twice in this lookup map if
		 * it has been assigned an alias!
		 */
		private final Map<String, Layer> layers;

		private final Scope scope;

		/** Efficient lookup for collecting  */
		private final Graph<Layer> layerGraph;

		/** Flags that have been switched on for the query. */
		private final Set<String> switches;

		/** Additional properties that have been set for the query. */
		private final Map<String, Object> properties;

		/**
		 * For every item layer in the scope lists all the annotation layers that
		 * can produce annotations for items in that layer, including catch-all layers.
		 */
		private final Map<ItemLayer, Set<AnnotationLayer>> annotationSources;

		/**
		 * Dynamically populated lookup for variable expressions.
		 */
		private final Map<String, CloseableThreadLocal<Assignable<?>>> variables
			= new Object2ObjectOpenHashMap<>();
		/** Maps member labels to layers */
		private final Map<String, BindingInfo> bindings;
		/** Maps member names to assignable expressions */
		//TODO make sure that any implementation properly duplicates on the target context
		private final Map<String, CloseableThreadLocal<Assignable<? extends Item>>> members;

		private final Object lock = "<ROOTLOCK>";

		private RootContext(RootContextBuilder builder) {
			super(builder);

			corpus = builder.corpus;
			scope = builder.scope;
			switches = new ObjectOpenHashSet<>(builder.switches);
			properties = new Object2ObjectOpenHashMap<>(builder.properties);

			layers = new Object2ObjectOpenHashMap<>(builder.namedLayers);
			bindings = new Object2ObjectOpenHashMap<>(builder.bindings);

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

			members = new Object2ObjectOpenHashMap<>();
			for(Entry<String, BindingInfo> entry : bindings.entrySet()) {
				TypeInfo type = resolveMemberType(entry.getValue());
				String name = entry.getKey();
				members.put(entry.getKey(), CloseableThreadLocal.withInitial(
						() -> References.member(name, type)));
			}
		}

		@Override
		protected void cleanup() {
			variables.values().forEach(CloseableThreadLocal::close);
			members.values().forEach(CloseableThreadLocal::close);
		}

		@Override
		public Object getLock() { return lock; }

		private TypeInfo resolveMemberType(BindingInfo info) {
			// Easy mode: specifically declared edge type means we don't need to run further checks
			if(info.isEdges()) {
				return TypeInfo.EDGE;
			}

			ItemLayer layer = info.getLayer();

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
				// SOme kind of nested hierarchy, so must be container or structure
				if(root.get().getManifestType()==ManifestType.STRUCTURE_MANIFEST) {
					return TypeInfo.STRUCTURE;
				}
				return TypeInfo.CONTAINER;
			}

			// Nothing fancy, layer only holds generic items
			return TypeInfo.ITEM;
		}

		@Override
		protected ContextType getType() { return ContextType.ROOT; }

		@SuppressWarnings("unchecked")
		@Override
		public LaneContextBuilder derive() { return new LaneContextBuilder(this); }

		@Override
		public Corpus getCorpus() { return corpus; }

		@Override
		public Scope getScope() { return scope; }

		private static String key(Layer layer) {
			return ManifestUtils.requireId(layer.getManifest());
		}

		@Override
		protected Graph<Layer> getLayerGraph() { return layerGraph; }

		@Override
		protected Map<ItemLayer, Set<AnnotationLayer>> getAnnotationSources() { return annotationSources; }

		@Override
		public Optional<Layer> findLayer(String name) {
			return Optional.ofNullable(layers.get(checkNotEmpty(name)));
		}

		@Override
		public boolean isSwitchSet(String name) {
			return switches.contains(checkNotEmpty(name));
		}

		@Override
		public Optional<?> getProperty(String name) {
			return Optional.ofNullable(properties.get(checkNotEmpty(name)));
		}

		@Override
		public Assignable<?> getVariable(String name) {
			return variables.computeIfAbsent(name,
					k -> CloseableThreadLocal.withInitial(() -> References.variable(k))).get();
		}

		@Override
		public Optional<Assignable<? extends Item>> getMember(String name) {
			return Optional.ofNullable(members.get(requireNonNull(name)))
					.map(CloseableThreadLocal::get);
		}

		@Override
		public Optional<ItemLayer> resolveMember(String name) {
			return Optional.ofNullable(bindings.get(requireNonNull(name)))
					.map(BindingInfo::getLayer);
		}
	}

	public static class AnnotationInfo {
		private final String rawKey;
		private final String key;
		private final ValueType valueType;
		private final TypeInfo type;

		Function<Item, Object> objectSource;
		ToLongFunction<Item> integerSource;
		ToDoubleFunction<Item> floatingPointSource;
		Predicate<Item> booleanSource;

		AnnotationInfo(String rawKey, String key, ValueType valueType, TypeInfo type) {
			this.rawKey = requireNonNull(rawKey);
			this.key = requireNonNull(key);
			this.valueType = requireNonNull(valueType);
			this.type = requireNonNull(type);
		}

		public String getRawKey() { return rawKey; }

		public String getKey() { return key; }

		public ValueType getValueType() { return valueType; }

		public TypeInfo getType() { return type; }


		public Function<Item, Object> getObjectSource() {
			checkState("No object source defined", objectSource!=null);
			return objectSource;
		}

		public ToLongFunction<Item> getIntegerSource() {
			checkState("No integer source defined", integerSource!=null);
			return integerSource;
		}

		public ToDoubleFunction<Item> getFloatingPointSource() {
			checkState("No floating point source defined", floatingPointSource!=null);
			return floatingPointSource;
		}

		public Predicate<Item> getBooleanSource() {
			checkState("No boolean source defined", booleanSource!=null);
			return booleanSource;
		}

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

	private static class LaneInfo {
		private final IqlLane lane;
		private final ItemLayer layer;

		public LaneInfo(IqlLane lane, ItemLayer layer) {
			this.lane = requireNonNull(lane);
			this.layer = requireNonNull(layer);

		}

		public IqlLane getLane() { return lane; }
		public ItemLayer getLayer() { return layer; }
	}

	private static class ContainerStore implements Assignable<Container> {

		static ContainerStore from(LaneInfo info) {
			if(info==null) {
				return null;
			}
			TypeInfo type;

			ManifestType layerType = info.getLayer().getManifest().getManifestType();
			switch (layerType) {
			case FRAGMENT_LAYER_MANIFEST:
			case ITEM_LAYER_MANIFEST: type = TypeInfo.CONTAINER; break;

			case STRUCTURE_LAYER_MANIFEST: type = TypeInfo.STRUCTURE; break;

			default:
				throw new IcarusRuntimeException(GlobalErrorCode.INTERNAL_ERROR,
						"Unknown layer type: "+layerType);
			}

			return new ContainerStore(type);
		}

		private Container container;
		private final TypeInfo type;

		private ContainerStore(TypeInfo type) {
			this.type = requireNonNull(type);
		}

		@Override
		public void assign(Object value) { setContainer((Container) value);}

		public void setContainer(Container container) { this.container = requireNonNull(container); }

		@Override
		public void clear() { container = null;}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public Container compute() { return container; }

		@Override
		public Assignable<Container> duplicate(EvaluationContext context) {
			return context.getContainerStore().orElseThrow(
					() -> EvaluationUtils.forInternalError(
					"Target context does not provide container store"));
		}
	}

	private static class ElementInfo {
		private final IqlProperElement element;
		private final List<ItemLayer> layers;

		public ElementInfo(IqlProperElement element, List<ItemLayer> layers) {
			this.element = requireNonNull(element);
			this.layers = requireNonNull(layers);
			checkArgument(!layers.isEmpty());
		}

		public IqlProperElement getElement() { return element; }
		public List<ItemLayer> getLayers() { return layers; }

		public boolean isEdge() { return element.getType()==IqlType.EDGE; }
	}

	private static class ItemStore implements Assignable<Item> {

		static ItemStore from(ElementInfo info) {
			if(info==null) {
				return null;
			}
			TypeInfo type;
			if(info.isEdge()) {
				type = TypeInfo.EDGE;
			} else {
				Set<ManifestType> layerTypes = info.getLayers().stream()
						.map(ItemLayer::getManifest)
						.map(TypedManifest::getManifestType)
						.distinct()
						.collect(Collectors.toSet());

				if(layerTypes.size()==1 && layerTypes.contains(ManifestType.FRAGMENT_LAYER_MANIFEST)) {
					type = TypeInfo.FRAGMENT;
				} else {
					// "Fall-through" type if we get mixed sources or not enough info
					type = TypeInfo.ITEM;
				}
			}
			return new ItemStore(type);
		}

		private Item item;
		private final TypeInfo type;

		private ItemStore(TypeInfo type) {
			this.type = requireNonNull(type);
		}

		@Override
		public void assign(Object value) { setItem((Item) value);}

		public void setItem(Item item) { this.item = requireNonNull(item); }

		@Override
		public void clear() { item = null;}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public Item compute() { return item; }

		@Override
		public Assignable<Item> duplicate(EvaluationContext context) {
			return context.getElementStore()
					.map(EvaluationUtils::castMember)
					.orElseThrow(() -> EvaluationUtils.forInternalError(
							"Target context does not provide element store"));
		}
	}

	private static ItemLayer ensureItemLayer(Layer layer) {
		if(!ModelUtils.isItemLayer(layer))
			throw new QueryException(QueryErrorCode.INCOMPATIBLE_REFERENCE,
					"Lane name must reference an item or structure layer: "+getName(layer));
		return (ItemLayer) layer;
	}

	@NotThreadSafe
	public static class LaneContext extends EvaluationContext {

		/** Uplink to the mandatory RootContext */
		private final RootContext parent;
		private final LaneInfo lane;
		private final CloseableThreadLocal<ContainerStore> containerStore;

		private LaneContext(LaneContextBuilder builder) {
			super(builder);

			parent = builder.parent;

			lane = resolve(builder.lane);
			containerStore = CloseableThreadLocal.withInitial(this::createStore);
		}

		@Override
		protected void cleanup() {
			containerStore.close();
		}

		private ContainerStore createStore() {
			return ContainerStore.from(lane);
		}

		@Override
		protected ContextType getType() { return ContextType.LANE; }

		@SuppressWarnings("unchecked")
		@Override
		public ElementContextBuilder derive() { return new ElementContextBuilder(this); }

		@Override
		public Optional<EvaluationContext> getParent() { return Optional.of(parent); }

		private LaneInfo resolve(IqlLane lane) {
			requireNonNull(lane);

			// If lane is a proxy, we have no other lanes and need to use the scope's primary layer
			if(lane.isProxy()) {
				return new LaneInfo(lane, getScope().getPrimaryLayer());
			}

			// Non-proxy lane means we need to resolve the named layer
			Layer layer = requireLayer(lane.getName());

			return new LaneInfo(lane, ensureItemLayer(layer));
		}

		@Override
		public Optional<IqlLane> getLane() {
			return Optional.of(lane.getLane());
		}

		@Override
		public Optional<Assignable<Container>> getContainerStore() {
			return Optional.ofNullable(containerStore.get());
		}
	}

	@NotThreadSafe
	public static class ElementContext extends EvaluationContext {

		/** Uplink to an arbitrary ElementContext */
		private final EvaluationContext parent;

		private final ElementInfo element;

		private final CloseableThreadLocal<ItemStore> itemStore;

		/**
		 * Lazily constructed lookup to map from annotation keys to actual sources.
		 * This map contains all the annotations that target instances of this context's
		 * element and that can be referenced by name.
		  */
		private Map<String, List<AnnotationLink>> annotationLookup;

		/** Layers that allow unknown keys for annotations */
		private List<AnnotationLayer> catchAllLayers;

		/** Maps variable names to assignable expressions */
		private final Map<String, AnnotationInfo> annotationCache = new Object2ObjectOpenHashMap<>();

		private ElementContext(ElementContextBuilder builder) {
			super(builder);

			// First assign the fields we need for resolving other parts
			parent = builder.parent;

			element = resolve(builder.element);

			itemStore = CloseableThreadLocal.withInitial(this::createStore);
		}

		@Override
		protected void cleanup() {
			itemStore.close();
		}

		private ItemStore createStore() {
			return ItemStore.from(element);
		}

		@Override
		protected ContextType getType() { return ContextType.ELEMENT; }

		@SuppressWarnings("unchecked")
		@Override
		public ElementContextBuilder derive() { return new ElementContextBuilder(this); }

		private ElementInfo resolve(IqlProperElement element) {
			requireNonNull(element);

			LaneInfo laneInfo = requireLaneInfo();

			// Edges can only be referenced from the surrounding structure
			if(element.getType()==IqlType.EDGE) {
				ItemLayer layer = laneInfo.getLayer();
				if(!ModelUtils.isStructureLayer(layer))
					throw EvaluationUtils.forIncorrectUse(
							"Edge outside of structure layer scope in lane: %s", getName(layer));
				return new ElementInfo(element, list(layer));
			}

			String label = element.getLabel().orElse(null);
			if(label!=null) {
				ItemLayer layer = resolveMember(label).orElseThrow(
						() -> EvaluationUtils.forUnknownIdentifier(label, "layer"));
				// Explicitly bound element -> can only have 1 source layer
				return new ElementInfo(element, list(layer));
			}

			// Unbound element can stem from any base layer of current lane
			DataSet<ItemLayer> baseLayers = laneInfo.getLayer().getBaseLayers();
			if(baseLayers.isEmpty())
				throw EvaluationUtils.forIncorrectUse("Cannot use non-aggregating layer as lane: %s",
						getName(laneInfo.getLayer()));

			return new ElementInfo(element, baseLayers.toList());
		}

		private LaneInfo requireLaneInfo() {
			LaneInfo lane = ((LaneContext)getLaneContext()).lane;
			if(lane==null)
				throw new QueryException(GlobalErrorCode.INTERNAL_ERROR, "No lane info available");
			return lane;
		}

		private ElementInfo requireElementInfo() {
			return getInheritable(this, ctx -> ctx.element).orElseThrow(
					() -> new QueryException(GlobalErrorCode.INTERNAL_ERROR,
					"No element available"));
		}

		private void ensureLayerLookups() {
			if(catchAllLayers==null || annotationLookup==null) {
				Map<ItemLayer, Set<AnnotationLayer>> annotationSources = getAnnotationSources();

				List<AnnotationLayer> catchAllLayers = new ArrayList<>();
				Map<String, List<AnnotationLink>> annotationLookup = new Object2ObjectOpenHashMap<>();
				ElementInfo elementInfo = requireElementInfo();

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

				for(ItemLayer iLayer : elementInfo.getLayers()) {
					for(AnnotationLayer aLayer : annotationSources.getOrDefault(iLayer, Collections.emptySet())) {

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

		private static @Nullable <T> Optional<T> getInheritable(ElementContext ctx,
				Function<ElementContext, T> getter) {
			while(ctx!=null) {
				T value = getter.apply(ctx);
				if(value!=null) {
					return Optional.of(value);
				}
				ctx = ctx.parent instanceof ElementContext ? (ElementContext)ctx.parent : null;
			}
			return Optional.empty();
		}

		@Override
		public Optional<EvaluationContext> getParent() { return Optional.of(parent); }

		@Override
		public Optional<IqlProperElement> getElement() {
			return getInheritable(this, ctx -> ctx.element).map(ElementInfo::getElement);
		}

		@Override
		public Optional<Assignable<Item>> getElementStore() {
			return Optional.ofNullable(itemStore.get());
		}

		@Override
		public Optional<AnnotationInfo> findAnnotation(QualifiedIdentifier identifier) {
			requireNonNull(identifier);

			String rawKey = identifier.getRawText();

			// Give our cache a chance first
			AnnotationInfo info = annotationCache.get(rawKey);
			if(info!=null) {
				return Optional.of(info);
			}

			// No luck with the cache -> run actual search now
			ensureLayerLookups();
			String key = identifier.getElement();
			AnnotationLayer expectedLayer;
			if(identifier.hasHost()) {
				String layerId = identifier.getHost().get();
				// If layer is explicitly stated, it must resolve
				Layer layer = findLayer(layerId).orElseThrow(
						() -> EvaluationUtils.forUnknownIdentifier(layerId, "annotation layer"));
				if(!ModelUtils.isAnnotationLayer(layer))
					throw new QueryException(QueryErrorCode.INCOMPATIBLE_REFERENCE, String.format(
							"Specified layer id '%s' does not resovle to an annotation layer: %s",
							layerId, getName(layer)));
				expectedLayer = (AnnotationLayer) layer;
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
			}

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

			if(identifier.hasHost() && result==null)
				throw new QueryException(QueryErrorCode.UNKNOWN_IDENTIFIER, String.format(
						"Qualified identifier '%s' could not be resolved to an annotation", rawKey));

			if(result!=null) {
				annotationCache.put(rawKey, result);
			}

			return Optional.ofNullable(result);
		}

		private static AnnotationInfo fromLink(String rawKey, AnnotationLink link) {
			AnnotationManifest manifest = link.getManifest();
			final String key = ManifestUtils.require(manifest, AnnotationManifest::getKey, "key");
			AnnotationInfo info = new AnnotationInfo(rawKey, key, manifest.getValueType(),
					EvaluationUtils.typeFor(manifest.getValueType()));

			createSources(info, link.getLayer().getAnnotationStorage());

			return info;
		}

		private static AnnotationInfo forCatchAll(String rawKey, String key,
				AnnotationLayer layer) {
			AnnotationInfo info = new AnnotationInfo(rawKey, key,
					ValueType.UNKNOWN, TypeInfo.GENERIC);

			createSources(info, layer.getAnnotationStorage());

			return info;
		}

		private static void createSources(AnnotationInfo info, AnnotationStorage storage) {
			String key = info.key;
			if(TypeInfo.isInteger(info.type)) {
				info.integerSource = item -> storage.getLong(item, key);
			} else if(TypeInfo.isFloatingPoint(info.type)) {
				info.floatingPointSource = item -> storage.getDouble(item, key);
			} else if(TypeInfo.isBoolean(info.type)) {
				info.booleanSource = item -> storage.getBoolean(item, key);
			} else if(TypeInfo.isText(info.type)) {
				info.objectSource = item -> storage.getString(item, key);
			} else {
				info.objectSource = item -> storage.getValue(item, key);
			}
		}
	}

	/** Flag to signal that this context shouldn't be used any further */
	private volatile boolean disabled = false;

	private final EnvironmentCache environmentCache;

	//TODO add storage for flags set by special marker fields or methods inside expressions

	//TODO add mechanism to register callbacks for stages of matching process?

	private EvaluationContext(BuilderBase<?,?> builder) {
		requireNonNull(builder);

		environmentCache = EnvironmentCache.of(builder.environments);
	}

	protected abstract ContextType getType();

	public final boolean isRoot() { return getType()==ContextType.ROOT; }

	public final boolean isLane() { return getType()==ContextType.LANE; }

	public final boolean isElement() { return getType()==ContextType.ELEMENT; }

	private static QueryException forMissingRoot() {
		return new QueryException(GlobalErrorCode.ILLEGAL_STATE,  "No root context available");
	}

	public final @Nullable EvaluationContext getRootContext() {
		EvaluationContext ctx = this;
		while(!ctx.isRoot()) {
			ctx = ctx.getParent().orElseThrow(EvaluationContext::forMissingRoot);
		}
		if(!ctx.isRoot())
			throw forMissingRoot();
		return ctx;
	}

	@SuppressWarnings("null")
	public final @Nullable EvaluationContext getLaneContext() {
		EvaluationContext ctx = this;
		while(!ctx.isLane()) {
			ctx = getParent().orElse(null);
		}
		if(ctx==null || !ctx.isLane())
			throw new QueryException(GlobalErrorCode.ILLEGAL_STATE,  "No lane context available");
		return ctx;
	}

	/**
	 * Creates a builder that can be used to derive a new context from this
	 * instance, inheriting all the settings, but with e.g. new bound item
	 * contexts etc...
	 * @return
	 */
	public abstract <B extends BuilderBase<B, EvaluationContext>> B derive();

	protected void cleanup() { /* no-op */ }

	/**
	 * Effectively disables this context. After invoking this method, further calling
	 * any of method on this instance can cause an exception.
	 */
	public final void dispose() {
		disabled = true;

		// Ensure our basic state is reset
		environmentCache.dispose();
		// Let subclasses do their housekeeping
		cleanup(); //TODO have subclasses actually override this!!
	}

	public Optional<IqlLane> getLane() { return Optional.empty(); }

	public Optional<IqlProperElement> getElement() { return Optional.empty(); }

	public Optional<EvaluationContext> getParent() { return Optional.empty(); }

	public Optional<?> getProperty(String name) { return getRootContext().getProperty(name); }
	public boolean isSwitchSet(String name) { return getRootContext().isSwitchSet(name); }
	public Corpus getCorpus() { return getRootContext().getCorpus(); }
	public Scope getScope() { return getRootContext().getScope(); }

	public Optional<Layer> findLayer(String name) { return getRootContext().findLayer(name); }

	public Layer requireLayer(String name) {
		requireNonNull(name);
		return findLayer(name).orElseThrow(
				() -> EvaluationUtils.forUnknownIdentifier(name, "layer"));
	}

	public Optional<AnnotationInfo> findAnnotation(QualifiedIdentifier identifier) { return Optional.empty(); }

	public Optional<Assignable<Container>> getContainerStore() { return getLaneContext().getContainerStore(); }
	public Optional<Assignable<Item>> getElementStore() { return Optional.empty(); }

	protected Graph<Layer> getLayerGraph() { return getRootContext().getLayerGraph(); }

	protected Map<ItemLayer, Set<AnnotationLayer>> getAnnotationSources() {
		return getRootContext().getAnnotationSources();
	}

	//TODO rethink the entire variable scoping concept
	/*
	 * With embedded expression scopes such as loop constraints, we need a way to
	 * restrict the variables to that scope, but also make them properly adhere to
	 * the expression mechanics of duplication etc...
	 * One option could be to use a UUID-style suffix in the identifier for a variable
	 * when embedded.
	 */
	public Assignable<?> getVariable(String name) { return getRootContext().getVariable(name); }

	public Optional<ItemLayer> resolveMember(String name) { return getRootContext().resolveMember(name); }

	public Optional<Assignable<? extends Item>> getMember(String name) { return getRootContext().getMember(name); }

	public boolean isSwitchSet(QuerySwitch qs) {
		return isSwitchSet(qs.getKey());
	}

	// BEGIN

	/**
	 * @see #getLock()
	 */
	public <T> Expression<T> duplicate(Expression<T> source) {
		return source.duplicate(this);
	}

	/**
	 * @see #getLock()
	 */
	public <T> Assignable<T> duplicate(Assignable<T> source) {
		return source.duplicate(this);
	}

	public <T> Expression<T> optimize(Expression<T> source) {
		return source.optimize(this);
	}

	/**
	 * Returns an object that can be used to synchronize client calls
	 * after the setup phase. Note that {@link Expression} implementations
	 * need not use this lock for synchronization in their {@link Expression#duplicate(EvaluationContext)}
	 * method. The same is true for other members of the expression framework,
	 * such as {@link Assignable}. Instead client code should synchronize the
	 * top-most code section that invokes the duplication process.
	 */
	public Object getLock() { return getRootContext().getLock(); }

	private static final TypeInfo[] NO_ARGS = {};

	private TypeInfo[] argTypes(@Nullable Expression<?>[] arguments) {
		if(arguments==null || arguments.length==0) {
			return NO_ARGS;
		}
		return Stream.of(arguments)
				.map(Expression::getResultType)
				.toArray(TypeInfo[]::new);

	}

	private @Nullable TypeInfo type(@Nullable Expression<?> exp) {
		return exp==null ? null : exp.getResultType();
	}

	/**
	 * Tries to resolve the given {@code name} to a field or method.
	 * Using the {@code resultFilter} argument, returned expressions
	 * can be restricted to be return type compatible to a desired target type.
	 * If the
	 *
	 * @param scope the optional context that defines the scope of resolution
	 * @param name the identifier to be used for resolution
	 * @param filter optional restriction on the allowed types of obejcts to be resolved
	 *
	 * @throws QueryException of type {@link QueryErrorCode#UNKNOWN_IDENTIFIER} iff
	 * the specified {@code name} could not be resolved to a target that satisfies
	 * the given {@code argument} specification and {@code resultFilter} (if present).
	 */
	public Optional<Expression<?>> resolve(@Nullable Expression<?> scope, String name,
			@Nullable TypeFilter filter, Expression<?>...arguments) {
		checkNotEmpty(name);

		EvaluationContext ctx = this;
		while(ctx!=null) {
			if(ctx.environmentCache!=null) {
				List<NsEntry> candidates = ctx.environmentCache.resolve(
						type(scope), name, filter, argTypes(arguments));

				if(candidates.size()>1) {
					log.debug("Ambiguous method name '{}' leading to entries: {}", name,
							CollectionUtils.toString(candidates));
				} else if(!candidates.isEmpty()) {
					return Optional.of(candidates.get(0).instantiate(this, scope, arguments));
				}
			}

			ctx = ctx.getParent().orElse(null);
		}

		return Optional.empty();
	}

	public static abstract class BuilderBase<B extends BuilderBase<B, C>, C extends EvaluationContext>
			extends AbstractBuilder<B, C> {

		private final Set<Environment> environments = new ReferenceOpenHashSet<>();

		public B addEnvironment(Environment...environments) {
			requireNonNull(environments);
			checkArgument("Must provide at least 1 environment to add", environments.length>0);
			for (Environment environment : environments) {
				checkState("Environment already added: "+environment, !this.environments.contains(environment));
				this.environments.add(environment);
			}

			return thisAsCast();
		}

		@Override
		protected void validate() {
			// nothing to do here, but keep for next feature iteration
			super.validate();
		}
	}

	public static final class RootContextBuilder extends BuilderBase<RootContextBuilder, RootContext>
			implements EngineConfigurator{

		private Corpus corpus;

		/** Maps the usable raw names or aliases to layer entries. */
		private final Map<String, Layer> namedLayers = new Object2ObjectOpenHashMap<>();

		/**
		 *  Contains additional layers that have not received a dedicated reference in the
		 *  original query definition. This includes for instance those layers that got
		 *  added transitively via dependencies on other (named) layers.
		 */
		private Scope scope;

		/** Flags that have been switched on for the query. */
		private final Set<String> switches = new ObjectOpenHashSet<>();

		/** Additional properties that have been set for this query. */
		private final Map<String, Object> properties = new Object2ObjectOpenHashMap<>();
		/** Fixed bindings from identifiers to item layers or their elements */
		private final Map<String, BindingInfo> bindings = new Object2ObjectOpenHashMap<>();

		public RootContextBuilder namedLayer(String alias, Layer layer) {
			requireNonNull(alias);
			requireNonNull(layer);
			checkState("Alias already used: "+alias, !namedLayers.containsKey(alias));
			namedLayers.put(alias, layer);
			return this;
		}

		public RootContextBuilder scope(Scope scope) {
			requireNonNull(scope);
			checkState("Scope already set", this.scope==null);
			this.scope = scope;
			return this;
		}

		public RootContextBuilder corpus(Corpus corpus) {
			requireNonNull(corpus);
			checkState("Corpus already set", this.corpus==null);
			this.corpus = corpus;
			return this;
		}

		/** {@inheritDoc} */
		@Override
		public RootContextBuilder setProperty(String key, Object value) {
			requireNonNull(key);
			if(value==null) {
				properties.remove(key);
			} else {
				properties.put(key, value);
			}
			return this;
		}

		/** {@inheritDoc} */
		@Override
		public RootContextBuilder setSwitch(String name, boolean active) {
			requireNonNull(name);
			if(active) {
				switches.add(name);
			} else {
				switches.remove(name);
			}
			return this;
		}

		public RootContextBuilder bind(String name, ItemLayer layer, boolean edges) {
			requireNonNull(name);
			requireNonNull(layer);
			checkNotEmpty(name);
			checkState("Name already bound: "+name, !bindings.containsKey(name));
			bindings.put(name, new BindingInfo(layer, edges));
			return this;
		}

		public RootContextBuilder bind(IqlBinding binding) {
			requireNonNull(binding);

			String target = binding.getTarget();
			Layer layer = namedLayers.get(target);
			if(layer==null)
				throw EvaluationUtils.forUnknownIdentifier(target, "bound layer");
			if(!ModelUtils.isItemLayer(layer))
				throw EvaluationUtils.forIncorrectUse("Binding target for '%s' must be an item layer - got %s",
						target, layer.getManifest().getManifestType());
			ItemLayer targetLayer = (ItemLayer)layer;

			for(IqlReference ref : binding.getMembers()) {
				String name = ref.getName();
				checkState("Name already bound: "+name, !bindings.containsKey(name));
				bindings.put(name, new BindingInfo(targetLayer, binding.isEdges()));
			}
			return this;
		}

		@Override
		protected void validate() {
			super.validate();
			checkState("Corpus not set", corpus!=null);
			checkState("Scope not set", scope!=null);
		}

		@Override
		protected RootContext create() { return new RootContext(this); }

	}

	public static final class LaneContextBuilder extends BuilderBase<LaneContextBuilder, LaneContext> {

		private final RootContext parent;

		private IqlLane lane;

		private LaneContextBuilder(RootContext parent) {
			this.parent = requireNonNull(parent);
		}

		public LaneContextBuilder lane(IqlLane lane) {
			requireNonNull(lane);
			checkState("Lane already set", this.lane==null);
			this.lane = lane;
			return this;
		}

		@Override
		protected void validate() {
			super.validate();

			checkState("Lane not set", lane!=null);
		}

		@Override
		protected LaneContext create() { return new LaneContext(this); }

	}

	public static final class ElementContextBuilder extends BuilderBase<ElementContextBuilder, ElementContext> {

		private final EvaluationContext parent;

		private IqlProperElement element;

		private ElementContextBuilder(EvaluationContext parent) {
			this.parent = requireNonNull(parent);
		}

		public ElementContextBuilder element(IqlProperElement element) {
			requireNonNull(element);
			checkState("Element already set", this.element==null);
			this.element = element;
			return this;
		}

		@Override
		protected void validate() {
			super.validate();

			checkState("Element not set", element!=null);
		}

		@Override
		protected ElementContext create() { return new ElementContext(this); }

	}
}
