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
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

import javax.annotation.Nullable;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.QuerySwitch;
import de.ims.icarus2.query.api.engine.ext.EngineConfigurator;
import de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.util.AbstractBuilder;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * Utility class(es) to store the final configuration and bindings for a query evaluation.
 *
 * @author Markus Gärtner
 *
 */
public abstract class EvaluationContext {

	public static Builder builder() {
		return new Builder(null); // Root context
	}

	private static class RootContext extends EvaluationContext {

		/** Maps the usable raw names or aliases to corpus entries. */
		private final Map<String, Corpus> corpora;

		/** Maps the usable raw names or aliases to layer entries. */
		private final Map<String, Layer> layers;

		/** Flags that have been switched on for the query. */
		private final Set<String> switches = new ObjectOpenHashSet<>();

		/** Additional properties that have been set for this query. */
		private final Map<String, Object> properties = new Object2ObjectOpenHashMap<>();

		private RootContext(RootContextBuilder builder) {
			super(builder);

			//TODO
		}

	}

	private static class SubContext extends EvaluationContext {

		private final EvaluationContext parent;

		private final IqlLane lane;
		private final IqlProperElement element;

		private SubContext(SubContextBuilder builder) {
			super(builder);

			this.parent = builder.parent;

			//TODO
		}
	}

	private final Stack<ContextInfo> trace = new ObjectArrayList<>();

	//TODO add mechanisms to obtain root namespace and to navigate namespace hierarchies

	//TODO add mechanism to register callbacks for stages of matching process?

	private EvaluationContext(BuilderBase builder) {
		requireNonNull(builder);
		//TODO
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

	/**
	 * Effectively disables this context. After invoking this method, further calling
	 * any of method on this instance can cause an exception.
	 */
	public void dispose() {
		//TODO
	}

	protected RootContext getRootContext() {

	}

	protected SubContext getLastSubContext() {
		return null;
	}

	private static <T, C extends EvaluationContext> Optional<T> getInheritable(C ctx,
			Function<C, T> getter) {
		while(ctx!=null) {
			T value = getter.apply(ctx);
			if(value!=null) {
				return Optional.of(value);
			}
			ctx = ctx.parent;
		}
		return Optional.empty();
	}

	public Optional<IqlLane> getLane() {
		return getInheritable(this, ctx -> ctx.lane);
	}

	public Optional<IqlProperElement> getElement() {
		return getInheritable(this, ctx -> ctx.element);
	}

	public Optional<EvaluationContext> getParent() {
		return Optional.ofNullable(parent);
	}

	public Optional<Corpus> resolveCorpus(String name) {
		return getInheritable(this, ctx -> ctx.lookupCorpus(name));
	}

	private Corpus lookupCorpus(String name) {
		return corpora==null ? null : corpora.get(name);
	}

	public Optional<Layer> resolveLayer(String name) {
		return getInheritable(this, ctx -> ctx.lookupLayer(name));
	}

	private Layer lookupLayer(String name) {
		return layers==null ? null : layers.get(name);
	}

	public boolean isSwitchSet(String name) {
		return switches.contains(checkNotEmpty(name));
	}

	public boolean isSwitchSet(QuerySwitch qs) {
		return switches.contains(qs.getKey());
	}

	/**
	 * Returns the environment that provides all the globally available fields and methods
	 * that can be used inside expressions.
	 * @return
	 */
	public Environment getRootEnvironment() {

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

	}

	/**
	 * Enter the scope of the given {@code context} class and translate it into a
	 * new {@link Environment}.
	 * @param context
	 * @return
	 */
	public Environment enter(Class<?> context) {

	}

	public void exit(Class<?> context) {

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
	public Expression<?> resolve(String name, @Nullable TypeFilter filter) {

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
	public Expression<?> resolve(String name, @Nullable TypeFilter resultFilter,
			Expression<?>[] arguments) {

	}

	/**
	 * Tries to resolve the specified annotation {@code key} to a unique source of
	 * annotation values and all associated info data.
	 * <p>
	 * Note that this method will only consider those annotation sources that are
	 * available through the scope defined by the current
	 *
	 * @param key
	 * @return
	 */
	public AnnotationInfo findAnnotation(String key) {
		requireNonNull(key);

		//TODO do we need to use the Optional.orElseThrow() method here?
		Map<String, Layer> layers = getInheritable(this, ctx -> ctx.layers).get();

		// Option 1: aliased or directly referenced layer
		Layer layer = layers.get(key);
		if(layer!=null) {
			if(!ModelUtils.isAnnotationLayer(layer))
				throw new QueryException(QueryErrorCode.INCOMPATIBLE_REFERENCE, String.format(
						"Not an annotation layer for key '%s': %s", key, getName(layer)));

			return fromDefault(key, (AnnotationLayer) layer);
		}

		List<AnnotationManifest> manifests = new ArrayList<>();
		for (Layer l : layers.values()) {
			if(ModelUtils.isAnnotationLayer(l)) {
				AnnotationLayer annotationLayer = (AnnotationLayer)l;

			}
		}
	}

	private AnnotationInfo fromDefault(String key, AnnotationLayer layer) {
		AnnotationLayerManifest layerManifest = layer.getManifest();
		Optional<String> defaultKey = layerManifest.getDefaultKey();
		if(defaultKey.isPresent()) {
			String actualKey = defaultKey.get();
			return fromManifest(key, layerManifest.getAnnotationManifest(
					actualKey).orElseThrow(ManifestException.missing(layerManifest,
							"annotation manifest for key "+actualKey)));
		}

		Set<String> keys = layerManifest.getAvailableKeys();
		if(keys.size()>1)
			throw new QueryException(QueryErrorCode.INCORRECT_USE, String.format(
					"Annotation key '%s' points to layer with more than 1 annotation manifest: %s",
							key, getName(layer)));

		String actualKey = keys.iterator().next();
		return fromManifest(key, layerManifest.getAnnotationManifest(actualKey)
				.orElseThrow(ManifestException.missing(layerManifest,
				"annotation manifest for key "+actualKey)));
	}

	private AnnotationInfo fromManifest(String key, AnnotationManifest manifest) {

	}

	public static class AnnotationInfo {
		private final String rawKey;
		private final String key;
		private final ValueType valueType;
		private final TypeInfo type;

		// HARD BINDING
		private AnnotationManifest manifest;
		private AnnotationStorage storage;
		// END HARD BINDING

		Function<Item, Object> objectSource;
		ToLongFunction<Item> integerSource;
		ToDoubleFunction<Item> floatingPointSource;
		Predicate<Item> booleanSource;

		private AnnotationInfo(String rawKey, String key, ValueType valueType, TypeInfo type) {
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

	private static final class ContextInfo {
		private final Class<?> type;
		private final Environment environment;
	}

	public static abstract class BuilderBase<B extends BuilderBase<B, C>, C extends EvaluationContext>
			extends AbstractBuilder<B, C> {
		//TODO

		@Override
		protected void validate() {
			// TODO Auto-generated method stub
			super.validate();
		}
	}

	public static class RootContextBuilder extends BuilderBase<RootContextBuilder, RootContext>
			implements EngineConfigurator{

		/** Maps the usable raw names or aliases to corpus entries. */
		private Map<String, Corpus> corpora = new Object2ObjectOpenHashMap<>(); //TODO keep nul ltill populated

		/** Maps the usable raw names or aliases to layer entries. */
		private Map<String, Layer> layers = new Object2ObjectOpenHashMap<>();

		/** Flags that have been switched on for the query. */
		private final Set<String> switches = new ObjectOpenHashSet<>();

		/** Additional properties that have been set for this query. */
		private final Map<String, Object> properties = new Object2ObjectOpenHashMap<>();

		/** {@inheritDoc} */
		@Override
		public void setProperty(String key, Object value) {
			requireNonNull(key);
			if(value==null) {
				properties.remove(key);
			} else {
				properties.put(key, value);
			}
		}

		/** {@inheritDoc} */
		@Override
		public void setSwitch(String name, boolean active) {
			requireNonNull(name);
			if(active) {
				switches.add(name);
			} else {
				switches.remove(name);
			}
		}

		/** {@inheritDoc} */
		@Override
		public void registerEnvironment(Environment environment) {
			// TODO Auto-generated method stub

		}

		/** {@inheritDoc} */
		@Override
		public void registerEnvironment(Class<?> context, Environment environment) {
			// TODO Auto-generated method stub

		}

		@Override
		protected RootContext create() { return new RootContext(this); }

	}

	public static class SubContextBuilder extends BuilderBase<SubContextBuilder, SubContext> {

		private final EvaluationContext parent;

		private SubContextBuilder(EvaluationContext parent) {
			this.parent = requireNonNull(parent);
		}

		@Override
		protected SubContext create() { return new SubContext(this); }

	}
}
