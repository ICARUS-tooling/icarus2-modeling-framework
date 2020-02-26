/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.eval;

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
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.DependencyType;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationFlag;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
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
import de.ims.icarus2.query.api.iql.IqlBinding;
import de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlReference;
import de.ims.icarus2.query.api.iql.IqlType;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.Mutable;
import de.ims.icarus2.util.collections.set.DataSet;
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
public abstract class EvaluationContext {

	public static RootContextBuilder rootBuilder() {
		return new RootContextBuilder();
	}

	private static class RootContext extends EvaluationContext {

		/** The corpus this context refers to. */
		private final Corpus corpus;

		/**
		 * Maps all the usable raw names and/or aliases to layer entries.
		 * Note that a single layer can end up twice in this lookup map if
		 * it has been assigned an alias!
		 */
		private final Map<String, Layer> layers;

		/** Stores all the alias names assigned to layers by the query */
		private final Set<String> aliases;

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

		private RootContext(RootContextBuilder builder) {
			super(builder);

			corpus = builder.corpus;
			scope = builder.scope;
			switches = new ObjectOpenHashSet<>(builder.switches);
			properties = new Object2ObjectOpenHashMap<>(builder.properties);

			layers = new Object2ObjectOpenHashMap<>(builder.namedLayers);
			aliases = new ObjectOpenHashSet<>(builder.namedLayers.keySet());

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
		public Corpus getCorpus() { return corpus; }

		@Override
		public Scope getScope() { return scope; }
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

		public boolean isAlias() { return isAlias; }

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

	private static class ContainerStore implements Mutable<Container>, Expression<Container> {

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
		public Container get() { return container; }

		@Override
		public void set(Object value) { setContainer((Container) value);}

		public void setContainer(Container container) { this.container = requireNonNull(container); }

		@Override
		public void clear() { container = null;}

		@Override
		public boolean isPrimitive() { return false; }

		@Override
		public boolean isEmpty() { return container!=null; }

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public Container compute() { return container; }

		@Override
		public Expression<Container> duplicate(EvaluationContext context) {
			return new ContainerStore(type);
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

	private static class ItemStore implements Mutable<Item>, Expression<Item> {

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

		private ItemStore(TypeInfo type) {
			this.type = requireNonNull(type);
		}

		private Item item;
		private final TypeInfo type;

		@Override
		public Item get() { return item; }

		@Override
		public void set(Object value) { setItem((Item) value);}

		public void setItem(Item item) { this.item = requireNonNull(item); }

		@Override
		public void clear() { item = null;}

		@Override
		public boolean isPrimitive() { return false; }

		@Override
		public boolean isEmpty() { return item!=null; }

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public Item compute() { return item; }

		@Override
		public Expression<Item> duplicate(EvaluationContext context) {
			return new ItemStore(type);
		}
	}

	private static ItemLayer ensureItemLayer(Layer layer) {
		if(!ModelUtils.isItemLayer(layer))
			throw new QueryException(QueryErrorCode.INCOMPATIBLE_REFERENCE,
					"Lane name must reference an item or structure layer: "+getName(layer));
		return (ItemLayer) layer;
	}

	private static class SubContext extends EvaluationContext {

		/** Uplink to another SubContext or an instance of RootContext */
		private final EvaluationContext parent;

		private final LaneInfo lane;
		private final ElementInfo element;

		private final ContainerStore containerStore;
		private final ItemStore itemStore;

		/** Maps member labels to layers */
		private final Map<String, ItemLayer> bindings;

		/**
		 * Lazily constructed lookup to map from annotation keys to actual sources.
		 * This map contains all the annotations that target instances of this context's
		 * element and that can be referenced by name.
		  */
		private Map<String, List<AnnotationLink>> annotationLookup;

		/** Layers that allow unknown keys for annotations */
		private List<AnnotationLayer> catchAllLayers;

		private final Map<String, AnnotationInfo> annotationCache = new Object2ObjectOpenHashMap<>();

		private SubContext(SubContextBuilder builder) {
			super(builder);

			// First assign the fields we need for resolving other parts
			parent = builder.parent;
			bindings = new Object2ObjectOpenHashMap<>(builder.bindings);

			lane = resolve(builder.lane);
			element = resolve(builder.element);

			containerStore = ContainerStore.from(lane);
			itemStore = ItemStore.from(element);
		}

		private LaneInfo resolve(IqlLane lane) {
			if(lane==null) {
				return null;
			}

			// If lane is a proxy, we have no other lanes and need to use the scope's primary layer
			if(lane.isProxy()) {
				return new LaneInfo(lane, getScope().getPrimaryLayer());
			}

			// Non-proxy lane means we need  to resolve the names layer
			Layer layer = requireLayer(lane.getName());

			return new LaneInfo(lane, ensureItemLayer(layer));
		}

		private ElementInfo resolve(IqlProperElement element) {
			if(element==null) {
				return null;
			}

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
				// Explicitly bound element -> can only have 1 source layer
				return new ElementInfo(element, list(forMemberLabel(label)));
			}

			// Unbound element can stem from any base layer of current lane
			DataSet<ItemLayer> baseLayers = laneInfo.getLayer().getBaseLayers();
			if(baseLayers.isEmpty())
				throw EvaluationUtils.forIncorrectUse("Cannot use non-aggregating layer as lane: %s",
						getName(laneInfo.getLayer()));

			return new ElementInfo(element, baseLayers.toList());
		}

		private ItemLayer forMemberLabel(String label) {
			ItemLayer layer = bindings.get(label);
			if(layer==null)
				throw EvaluationUtils.forUnknownIdentifier(label, "layer");
			return layer;
		}

		private LaneInfo requireLaneInfo() {
			return getInheritable(this, ctx -> ctx.lane).orElseThrow(
					() -> new QueryException(GlobalErrorCode.INTERNAL_ERROR,
					"No lane available"));
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

		private static @Nullable <T> Optional<T> getInheritable(SubContext ctx,
				Function<SubContext, T> getter) {
			while(ctx!=null) {
				T value = getter.apply(ctx);
				if(value!=null) {
					return Optional.of(value);
				}
				ctx = ctx.parent instanceof SubContext ? (SubContext)ctx.parent : null;
			}
			return Optional.empty();
		}

		@Override
		public Optional<EvaluationContext> getParent() { return Optional.of(parent); }

		@Override
		public Optional<IqlLane> getLane() {
			return getInheritable(this, ctx -> ctx.lane).map(LaneInfo::getLane);
		}

		@Override
		public Optional<IqlProperElement> getElement() {
			return getInheritable(this, ctx -> ctx.element).map(ElementInfo::getElement);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Expression<Container> & Mutable<Container>> Optional<T> getContainerStore() {
			return getInheritable(this, ctx -> (T)ctx.containerStore);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Expression<Item> & Mutable<Item>> Optional<T> getElementStore() {
			return Optional.ofNullable((T)itemStore);
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
						String.format("Key '%s' can be handled by multiple cath-all layers: %s",
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

	private final List<Environment> environments;

	//TODO add mechanism to register callbacks for stages of matching process?

	private EvaluationContext(BuilderBase<?,?> builder) {
		requireNonNull(builder);

		this.environments = new ArrayList<>(builder.environments);

		//TODO
	}

	public boolean isRoot() {
		return !getParent().isPresent();
	}

	public @Nullable EvaluationContext getRootContext() {
		EvaluationContext ctx = this;
		while(!ctx.isRoot()) {
			ctx = getParent().orElse(null);
		}
		return ctx;
	}

	/**
	 * Creates a builder that can be used to derive a new context from this
	 * instance, inheriting all the settings, but with e.g. new bound item
	 * contexts etc...
	 * @return
	 */
	public SubContextBuilder derive() {
		return new SubContextBuilder(this);
	}

	protected void cleanup() { /* no-op */ }

	/**
	 * Effectively disables this context. After invoking this method, further calling
	 * any of method on this instance can cause an exception.
	 */
	public void dispose() {
		disabled = true;

		// Ensure our basic state is reset
		environments.clear();
		// Let subclasses do their housekeeping
		cleanup();
	}

	public Optional<IqlLane> getLane() { return Optional.empty(); }

	public Optional<IqlProperElement> getElement() { return Optional.empty(); }

	public Optional<EvaluationContext> getParent() { return Optional.empty(); }

	public Optional<?> getProperty(String name) { return getRootContext().getProperty(name); }
	public boolean isSwitchSet(String name) { return getRootContext().isSwitchSet(name); }
	public Corpus getCorpus() { return getRootContext().getCorpus(); }
	public Scope getScope() { return getRootContext().getScope(); }

	public Optional<Layer> findLayer(String name) { return getRootContext().findLayer(name); }

	public <T extends Expression<Container> & Mutable<Container>> Optional<T> getContainerStore() { return Optional.empty(); }

	public <T extends Expression<Item> & Mutable<Item>> Optional<T> getElementStore() { return Optional.empty(); }

	public Layer requireLayer(String name) {
		return findLayer(name).orElseThrow(
				() -> EvaluationUtils.forUnknownIdentifier(name, "layer"));
	}

	protected Graph<Layer> getLayerGraph() { return getRootContext().getLayerGraph(); }

	protected Map<ItemLayer, Set<AnnotationLayer>> getAnnotationSources() {
		return getRootContext().getAnnotationSources();
	}

	public boolean isSwitchSet(QuerySwitch qs) {
		return isSwitchSet(qs.getKey());
	}

	/**
	 * Return all the currently active and available environments that can be
	 * used to resolve identifiers to fields or methods. The environments are
	 * ordered based on their priority: the environment on position {@code 0}
	 * is considered the most important one for any resolution process, with
	 * the importance of subsequent entries decreasing.
	 * @return
	 */
	public List<Environment> getActiveEnvironments() {
		//TODO implement
		throw new UnsupportedOperationException();
	}

	/**
	 * Tries to resolve the given {@code name} to a field or no-args method
	 * equivalent. Using the {@code resultFilter} argument, returned expressions
	 * can be restricted to be return type compatible to a desired target type.
	 *
	 * @throws QueryException of type {@link QueryErrorCode#UNKNOWN_IDENTIFIER} iff
	 * the specified {@code name} could not be resolved to a target that satisfies
	 * the given {@code filter} (if present).
	 */
	public Expression<?> resolve(@Nullable Class<?> scope, String name,
			@Nullable TypeFilter filter) {
		//TODO implement
		throw new UnsupportedOperationException();
	}

	/**
	 * Tries to resolve the given {@code name} to a method that takes the
	 * specified {@code arguments} as input.
	 * If the {@code resultFilter} argument is provided, it will be used to
	 * restrict the pool of methods to be considered to those that return
	 * a compatible value.
	 *
	 * @throws QueryException of type {@link QueryErrorCode#UNKNOWN_IDENTIFIER} iff
	 * the specified {@code name} could not be resolved to a method that satisfies
	 * the given {@code argument} specification and {@code resultFilter} (if present).
	 */
	public Expression<?> resolve(@Nullable Class<?> scope, String name,
			@Nullable TypeFilter resultFilter, Expression<?>[] arguments) {
		//TODO implement
		throw new UnsupportedOperationException();
	}

	public Optional<AnnotationInfo> findAnnotation(QualifiedIdentifier identifier) { return Optional.empty(); }

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

	public static abstract class BuilderBase<B extends BuilderBase<B, C>, C extends EvaluationContext>
			extends AbstractBuilder<B, C> {
		//TODO

		private final List<Environment> environments = new ArrayList<>();

		public B registerEnvironment(Environment environment) {
			// TODO Auto-generated method stub

			return thisAsCast();
		}

		@Override
		protected void validate() {
			// TODO Auto-generated method stub
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

		@Override
		protected void validate() {
			// TODO Auto-generated method stub
			super.validate();
		}

		@Override
		protected RootContext create() { return new RootContext(this); }

	}

	public static final class SubContextBuilder extends BuilderBase<SubContextBuilder, SubContext> {

		private final EvaluationContext parent;

		private IqlLane lane;
		private IqlProperElement element;
		private Map<String, ItemLayer> bindings = new Object2ObjectOpenHashMap<>();

		private SubContextBuilder(EvaluationContext parent) {
			this.parent = requireNonNull(parent);
		}

		public SubContextBuilder lane(IqlLane lane) {
			requireNonNull(lane);
			checkState("Lane already set", this.lane==null);
			this.lane = lane;
			return this;
		}

		public SubContextBuilder element(IqlProperElement element) {
			requireNonNull(element);
			checkState("Element already set", this.element==null);
			this.element = element;
			return this;
		}

		public SubContextBuilder bind(String name, ItemLayer layer) {
			requireNonNull(name);
			requireNonNull(layer);
			checkNotEmpty(name);
			checkState("Name already bound: "+name, !bindings.containsKey(name));
			bindings.put(name, layer);
			return this;
		}

		public SubContextBuilder bind(IqlBinding binding) {
			requireNonNull(binding);
			ItemLayer targetLayer = ensureItemLayer(parent.requireLayer(binding.getTarget()));

			for(IqlReference ref : binding.getMembers()) {
				String name = ref.getName();
				checkState("Name already bound: "+name, !bindings.containsKey(name));
				bindings.put(name, targetLayer);
			}
			return this;
		}

		@Override
		protected void validate() {
			// TODO Auto-generated method stub
			super.validate();
		}

		@Override
		protected SubContext create() { return new SubContext(this); }

	}
}
