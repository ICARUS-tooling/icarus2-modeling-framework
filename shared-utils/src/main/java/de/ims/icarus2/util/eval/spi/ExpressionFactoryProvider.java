/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 */
package de.ims.icarus2.util.eval.spi;

import static de.ims.icarus2.util.Conditions.checkNotNull;

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
		checkNotNull(name);

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
