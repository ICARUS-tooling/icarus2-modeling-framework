/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.eval.spi;

import static java.util.Objects.requireNonNull;

import java.util.ServiceLoader;

import javax.tools.JavaCompiler;

import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.eval.ExpressionFactory;

/**
 * @author Markus Gärtner
 *
 */
public abstract class ExpressionFactoryProvider {

	public static final String GENERIC_JAVA_TYPE = "java";


	private static final ServiceLoader<ExpressionFactoryProvider> serviceLoader = ServiceLoader.load(ExpressionFactoryProvider.class);

	/**
	 * Returns the name that identifies this provider.
	 *
	 * @return
	 */
	public abstract String getName();

	/**
	 * Returns {@code true} iff this provider is able to work in the current
	 * JVM environment.
	 * <p>
	 * Implementations of this provider interface might heavily rely on third-party
	 * software or require certain JVM features to be present or activated. For example
	 * a provider that compiles native java code might use the {@link JavaCompiler} which
	 * requires the Java SDK instead of the SE.
	 *
	 * @return
	 */
	public abstract boolean isSupported();

	/**
	 * Creates a new {@link ExpressionFactory factory} instance.
	 * @return
	 */
	public abstract ExpressionFactory newFactory();

	/**
	 * Returns all the providers that are {@link #isSupported() supported} under the
	 * current environment.
	 *
	 * @return
	 */
	public static String[] supportedProviders() {
		//TODO either enforce unique names for providers ore otherwise solve the problem of duplicates in the returned array
		LazyCollection<String> result = LazyCollection.lazyList();

		synchronized (serviceLoader) {
			serviceLoader.forEach(p -> {
				if(p.isSupported())
					result.add(p.getName());
			});
		}

		return result.getAsArray(new String[result.size()]);
	}

	/**
	 * Returns all the providers available to the current environment, whether they
	 * are {@link #isSupported() supported} or not.
	 *
	 * @return
	 */
	public static String[] availableProviders() {
		LazyCollection<String> result = LazyCollection.lazyList();

		synchronized (serviceLoader) {
			serviceLoader.forEach(p -> result.add(p.getName()));
		}

		return result.getAsArray(new String[result.size()]);
	}

	/**
	 * Tries to find a provider for the given {@code name} that is supported under
	 * the current environment and requests it to instantiate a new {@link ExpressionFactory factory}.
	 *
	 * @param name
	 * @return
	 *
	 * @throws IllegalArgumentException iff no supported provider could be found for the given {@code name}
	 */
	public static ExpressionFactory newFactory(String name) {
		requireNonNull(name);

		synchronized (serviceLoader) {
			for(ExpressionFactoryProvider provider : serviceLoader) {
				if(name.equals(provider.getName()) && provider.isSupported()) {
					return provider.newFactory();
				}
			}
		}

		throw new IllegalArgumentException("No supported expression factory provider available for name: "+name);
	}
}
