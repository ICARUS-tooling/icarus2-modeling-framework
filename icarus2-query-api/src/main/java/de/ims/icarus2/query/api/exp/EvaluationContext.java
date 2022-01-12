/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.QuerySwitch;
import de.ims.icarus2.query.api.engine.CorpusData;
import de.ims.icarus2.query.api.engine.CorpusData.LayerRef;
import de.ims.icarus2.query.api.engine.ext.EngineConfigurator;
import de.ims.icarus2.query.api.exp.Environment.NsEntry;
import de.ims.icarus2.query.api.iql.IqlBinding;
import de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.concurrent.CloseableThreadLocal;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
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

	public static RootContextBuilder rootBuilder(CorpusData corpusData) {
		return new RootContextBuilder(corpusData);
	}

	private enum ContextType {
		ROOT,
		LANE,
		ELEMENT,
		;
	}

	@NotThreadSafe
	public static class RootContext extends EvaluationContext {

		private final CorpusData corpusData;

		/** Flags that have been switched on for the query. */
		private final Set<String> switches;

		/** Additional properties that have been set for the query. */
		private final Map<String, Object> properties;

		/**
		 * Dynamically populated lookup for variable expressions.
		 */
		private final Map<String, CloseableThreadLocal<Assignable<?>>> variables
			= new Object2ObjectOpenHashMap<>();
		/** Maps member labels to layers */
		private final Map<String, BindingInfo> bindings;
		/** Maps member names to assignable expressions */
		private final Map<String, CloseableThreadLocal<Assignable<? extends Item>>> members;

		private final Object lock = "<ROOTLOCK>"; //TODO use a "real" object with adjusted toString() method

		private RootContext(RootContextBuilder builder) {
			super(builder);
			corpusData = builder.corpusData;
			switches = new ObjectOpenHashSet<>(builder.switches);
			properties = new Object2ObjectOpenHashMap<>(builder.properties);

			bindings = new Object2ObjectOpenHashMap<>(builder.bindings);

			members = new Object2ObjectOpenHashMap<>();
			for(Entry<String, BindingInfo> entry : bindings.entrySet()) {
				TypeInfo type = entry.getValue().getType();
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

		@Override
		protected ContextType getType() { return ContextType.ROOT; }

		@SuppressWarnings("unchecked")
		@Override
		public LaneContextBuilder derive() { return new LaneContextBuilder(this); }

		@Override
		public CorpusData getCorpusData() { return corpusData; }

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
		public Optional<LayerRef> resolveMember(String name) {
			return Optional.ofNullable(bindings.get(requireNonNull(name)))
					.map(BindingInfo::getLayer);
		}
	}

	private static class ContainerStore implements Assignable<Container> {

		static ContainerStore from(LaneInfo info) {
			if(info==null) {
				return null;
			}

			return new ContainerStore(info.getType());
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

	private static class ItemStore implements Assignable<Item> {

		static ItemStore from(ElementInfo info) {
			if(info==null) {
				return null;
			}
			return new ItemStore(info.getType());
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

	@NotThreadSafe
	public static class LaneContext extends EvaluationContext {

		/** Uplink to the mandatory RootContext */
		private final RootContext parent;
		/** Encapsulates all the information about the underlying lane */
		private final LaneInfo lane;
		/**  */
		private final CloseableThreadLocal<ContainerStore> containerStore;

		private LaneContext(LaneContextBuilder builder) {
			super(builder);

			parent = builder.parent;

			lane = requireNonNull(getCorpusData().resolveLane(builder.lane));
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

		public LaneInfo getLaneInfo() { return lane; }

		@SuppressWarnings("unchecked")
		@Override
		public ElementContextBuilder derive() { return new ElementContextBuilder(this); }

		@Override
		public Optional<EvaluationContext> getParent() { return Optional.of(parent); }

		@Override
		public Optional<IqlLane> getLane() { return Optional.of(lane.getLane()); }

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

		private ElementContext(ElementContextBuilder builder) {
			super(builder);

			// First assign the fields we need for resolving other parts
			parent = builder.parent;

			ElementInfo parentElement = parent instanceof ElementContext ?
					((ElementContext)parent).requireElementInfo() : null;
			element = getCorpusData().resolveElement(requireLaneInfo(), builder.element, parentElement);

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

		public ElementInfo getElementInfo() { return element; }

		@SuppressWarnings("unchecked")
		@Override
		public ElementContextBuilder derive() { return new ElementContextBuilder(this); }

		@Override
		public Optional<AnnotationInfo> findAnnotation(QualifiedIdentifier identifier) {
			return getCorpusData().findAnnotation(requireElementInfo(), identifier);
		}

		private LaneInfo requireLaneInfo() {
			LaneInfo lane = getLaneContext().getLaneInfo();
			if(lane==null)
				throw new QueryException(GlobalErrorCode.INTERNAL_ERROR, "No lane info available");
			return lane;
		}

		private ElementInfo requireElementInfo() {
			return getInheritable(this, ctx -> ctx.element).orElseThrow(
					() -> new QueryException(GlobalErrorCode.INTERNAL_ERROR,
					"No element available"));
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

	public final @Nullable RootContext getRootContext() {
		EvaluationContext ctx = this;
		while(!ctx.isRoot()) {
			ctx = ctx.getParent().orElseThrow(EvaluationContext::forMissingRoot);
		}
		if(!ctx.isRoot())
			throw forMissingRoot();
		return (RootContext) ctx;
	}

	@SuppressWarnings("null")
	public final @Nullable LaneContext getLaneContext() {
		EvaluationContext ctx = this;
		while(!ctx.isLane()) {
			ctx = getParent().orElse(null);
		}
		if(ctx==null || !ctx.isLane())
			throw new QueryException(GlobalErrorCode.ILLEGAL_STATE,  "No lane context available");
		return (LaneContext) ctx;
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
	public CorpusData getCorpusData() { return getRootContext().getCorpusData(); }

	/** Resolve given {@code name} to arbitrary layer. */
	public final Optional<LayerRef> findLayer(String name) {
		return getCorpusData().findLayer(checkNotEmpty(name));
	}

	/** Ensure a layer is present for given {@code name} or throw exception */
	public final LayerRef requireLayer(String name) {
		requireNonNull(name);
		return findLayer(name).orElseThrow(
				() -> EvaluationUtils.forUnknownIdentifier(name, "layer"));
	}

	public Optional<AnnotationInfo> findAnnotation(QualifiedIdentifier identifier) { return Optional.empty(); }

	public Optional<Assignable<Container>> getContainerStore() { return getLaneContext().getContainerStore(); }
	public Optional<Assignable<Item>> getElementStore() { return Optional.empty(); }

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

	/** Resolves {@code name} to a layer suitable for hosting items/containers. */
	public Optional<LayerRef> resolveMember(String name) { return getRootContext().resolveMember(name); }

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

		private final CorpusData corpusData;

		/** Flags that have been switched on for the query. */
		private final Set<String> switches = new ObjectOpenHashSet<>();

		/** Additional properties that have been set for this query. */
		private final Map<String, Object> properties = new Object2ObjectOpenHashMap<>();
		/** Fixed bindings from identifiers to item layers or their elements */
		private final Map<String, BindingInfo> bindings = new Object2ObjectOpenHashMap<>();

		private RootContextBuilder(CorpusData corpusData) {
			this.corpusData = requireNonNull(corpusData);
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

		public RootContextBuilder bind(IqlBinding binding) {
			requireNonNull(binding);

			bindings.putAll(corpusData.bind(binding));

			return this;
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
