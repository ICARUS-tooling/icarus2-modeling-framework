/**
 *
 */
package de.ims.icarus2.test.reflect;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class ClassCache<T> {

	final Class<T> targetClass;

	final Set<Class<?>> inherited = new HashSet<>();

	final Map<Method, MethodCache> methodCaches = new HashMap<>();

	final Predicate<? super Method> methodFilter;

	private ClassCache(Builder<T> builder) {
		this.targetClass = builder.targetClass;
		this.methodFilter = builder.methodFilter==null ?
				(m) -> true : builder.methodFilter;

		populateCache();
	}

	public Set<Class<?>> getAllSuperClassesAndInterfaces() {
		return Collections.unmodifiableSet(inherited);
	}

	public Set<Method> getMethods() {
		return Collections.unmodifiableSet(methodCaches.keySet());
	}

	public MethodCache getMethodCache(Method method) {
		return Optional.ofNullable(methodCaches.get(method))
				.orElseThrow(() -> new IllegalArgumentException("Unknown method: "+method));
	}

	public Collection<MethodCache> getMethodCaches() {
		return Collections.unmodifiableCollection(methodCaches.values());
	}

	private void populateCache() {
		final Map<Method, Set<Method>> methodsBySig = new Object2ObjectOpenCustomHashMap<>(STRATEGY);
		final Set<Method> EMPTY = Collections.emptySet();

		Stream.of(targetClass.getMethods())
			.filter(methodFilter)
			.forEach(m -> methodsBySig.put(m, EMPTY));

		final Stack<Class<?>> pending = new Stack<>();
		pending.push(targetClass);

		while(!pending.empty()) {
			Class<?> c = pending.pop();
			if(c==Object.class) {
				continue;
			}

			// Push super class and interfaces first
			Class<?> p = c.getSuperclass();
			if(p!=null) {
				pending.push(p);
			}
			Collections.addAll(pending, c.getInterfaces());

			// We already added methods from target class
			if(c==targetClass) {
				continue;
			}

			inherited.add(c);

			// Finally add all the methods of current class/interface
			Stream.of(c.getMethods())
				.sequential()
				.filter(methodFilter)
				.forEach(m -> methodsBySig.computeIfPresent(m,
						(k, set) -> set==EMPTY ? new HashSet<>() : set).add(m));
		}

		// Now convert the stored method sets into proper caches
		methodsBySig.forEach((method, set) ->
			methodCaches.put(method, new MethodCache(method, set)));
	}

	private static Strategy<Method> STRATEGY = new Strategy<Method>() {

		@Override
		public int hashCode(Method m) {
			return Objects.hash(m.getReturnType(),
					m.getName(), m.getParameterTypes(), m.getExceptionTypes());
		}

		@Override
		public boolean equals(Method m1, Method m2) {
			return m1.getName().equals(m2.getName())
					&& m1.getParameterCount()==m2.getParameterCount()
					&& Objects.equals(m1.getReturnType(), m2.getReturnType())
					&& Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes())
					&& Arrays.equals(m1.getExceptionTypes(), m2.getExceptionTypes());
		}
	};

	public static <T> Builder<T> newBuilder() {
		return new Builder<>();
	}

	public static class Builder<T> {
		Class<T> targetClass;
		Predicate<? super Method> methodFilter;

		private Builder() {
			// not for the public
		}

		public Builder<T> targetClass(Class<T> targetClass) {
			this.targetClass = requireNonNull(targetClass);
			return this;
		}

		public Builder<T> methodFilter(Predicate<? super Method> methodFilter) {
			this.methodFilter = requireNonNull(methodFilter);
			return this;
		}

		private void validate() {
			requireNonNull(targetClass, "Target class missing");
		}

		public ClassCache<T> build() {
			validate();
			return new ClassCache<>(this);
		}
	}
}
