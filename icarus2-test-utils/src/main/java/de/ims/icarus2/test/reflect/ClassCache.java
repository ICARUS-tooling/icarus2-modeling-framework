/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.test.reflect;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.opentest4j.TestAbortedException;

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

	final Consumer<String> log;

	private ClassCache(Builder<T> builder) {
		this.targetClass = builder.targetClass;
		this.methodFilter = builder.methodFilter==null ?
				(m) -> true : builder.methodFilter;

		this.log = builder.log;

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

	private static final String SEP = "======================================";

	private void populateCache() {
		final Map<Method, Set<Method>> methodsBySig = new Object2ObjectOpenCustomHashMap<>(STRATEGY);
		final Set<Method> EMPTY = Collections.emptySet();

		// Prepare buffer to hold only methods matching proper property methods from target class
		Stream.of(targetClass.getMethods())
			.filter(methodFilter)
			.filter(m -> !Modifier.isAbstract(m.getModifiers()))
			.filter(m -> {
				if(log!=null)
					log.accept("Expecting method signature: "+m);
				return true;
			})
			.forEach(m -> methodsBySig.put(m, EMPTY));

		if(log!=null)
			log.accept(SEP);

		final Queue<Class<?>> pending = new LinkedList<>();
		pending.add(targetClass);

		while(!pending.isEmpty()) {
			Class<?> c = pending.remove();
			if(c==Object.class || inherited.contains(c)) {
				continue;
			}

			// Push super class and interfaces first
			Optional.ofNullable(c.getSuperclass())
				.ifPresent(pending::add);
			Collections.addAll(pending, c.getInterfaces());

			// Keep track of all ancestors
			inherited.add(c);

			if(log!=null)
				log.accept("Adding class: "+c.getName());
		}

		if(log!=null)
			log.accept(SEP);

		// Extract methods from all those classes
		List<Class<?>> classesToVisit = new ArrayList<>(inherited);
		classesToVisit.sort(RefUtils.INHERITANCE_ORDER);

		if(log!=null)
			log.accept("Class hierarchy: "+classesToVisit);

		// Finally add all the methods of current class/interface
		classesToVisit.stream()
			.map(Class::getDeclaredMethods)
			.flatMap(Stream::of)
			.filter(methodFilter)
			.filter(m -> {
				if(log!=null)
					log.accept("Adding method: "+m);
				return true;
			})
			.forEach(m -> Optional.ofNullable(methodsBySig.computeIfPresent(m,
					(k, set) -> set==EMPTY ? new HashSet<>() : set))
					.orElseThrow(() -> new TestAbortedException("Unexpected method sig: "+m))
					.add(m));

		// Now convert the stored method sets into proper caches
		methodsBySig.forEach((method, set) -> {
			if(log!=null)
				log.accept(String.format("Overrides for method '%s': %s", method, set));

			methodCaches.put(method, new MethodCache(method, set, log));
		});

		if(log!=null)
			log.accept(SEP);
	}

	static Strategy<Method> STRATEGY = new Strategy<Method>() {

		@Override
		public int hashCode(Method m) {
			return Objects.hash(m.getName(),
//					m.getReturnType(),
					Integer.valueOf(Arrays.hashCode(m.getParameterTypes())
//					^ Arrays.hashCode(m.getExceptionTypes())
					));
		}

		@Override
		public boolean equals(Method m1, Method m2) {
			requireNonNull(m1);
			boolean result =
					(m1==m2) ||
					(m2!=null
					&& m1.getName().equals(m2.getName())
					&& m1.getParameterCount()==m2.getParameterCount()
					&& (m1.getReturnType().isAssignableFrom(m2.getReturnType())
							|| m2.getReturnType().isAssignableFrom(m1.getReturnType()))
					&& Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes())
					// Ignore exceptions in signature, as
//					&& Arrays.equals(m1.getExceptionTypes(), m2.getExceptionTypes())
					);

//			if(result)
//			System.out.printf("%b - %s vs. %s%n",result, m1, m2);

			return result;
		}
	};

	static class Sig {
		private final String name;
		private final Class<?> resultType;
		private final Class<?>[] parameterTypes;
		private final Class<?>[] exceptionTypes;

		Sig(Method method) {
			requireNonNull(method);

			name = method.getName();
			resultType = method.getReturnType();
			parameterTypes = method.getParameterTypes();
			exceptionTypes = method.getExceptionTypes();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj==this) {
				return true;
			} else if(obj instanceof Sig) {
				Sig other = (Sig) obj;
				return name.equals(other.name)
						&& resultType.equals(other.resultType)
						&& Arrays.equals(parameterTypes, other.parameterTypes)
						&& Arrays.equals(exceptionTypes, other.exceptionTypes);
			}
			return false;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(name, resultType, parameterTypes, exceptionTypes);
		}
	}

	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	public static class Builder<T> {
		Class<T> targetClass;
		Predicate<? super Method> methodFilter;
		Consumer<String> log;

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

		public Builder<T> log(Consumer<String> log) {
			this.log = requireNonNull(log);
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
