/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.lang;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Class to lazily initialize a value based on provided Factory method or
 * class's default constructor inspired from C# Lazy&lt;T&gt; using Lambda and
 * Method reference capabilities of Java 8. All exceptions resulting from
 * factory methods are cascaded to caller. Exceptions from default constructor
 * is wrapped as a RuntimeException. Throws NullPointerException if the factory
 * method itself is null or returns null on execution to fail fast. Available as
 * both Thread safe(default) and unsafe versions. Usage examples
 *
 * <pre>
 * public Lazy&lt;IntensiveResource&gt; r = Lazy.create(IntensiveResource.class, false);
 *
 * public Lazy&lt;IntensiveResource&gt; r1 = Lazy.create(IntensiveResource::buildResource);
 *
 * public Lazy&lt;IntensiveResource&gt; r2 = Lazy.create(() -&gt; return new IntensiveResource());
 * </pre>
 *
 * Invoking toString() will cause the object to be initialized. Accessing the
 * value of the Lazy object using Lazy.value() method causes the object to
 * initialize and execute the Factory method supplied. Values can also be
 * obtained as {@link java.util.Optional} to avoid NPEs
 *
 * @author raam
 *
 * @author Markus Gärtner
 *
 * @param <V> Type of object to be created
 */
public class Lazy<V> implements Serializable {

	/**
	 * Default UID instead of generated one, because proxy
	 */
	private static final long serialVersionUID = 1L;

	private static final String FACTORY_NULL_RETURN_MESSAGE = "Factory method returns null for Lazy value";

	private V value;

	private boolean created = false;

	protected Supplier<V> factory;

	public static <V> Lazy<V> create(Class<V> valueClass, boolean threadSafe) {
		if (threadSafe) {
			return new ThreadSafeLazy<V>(valueClass);
		}

		return new Lazy<V>(valueClass);
	}

	public static <V> Lazy<V> create(Class<V> valueClass) {
		return create(valueClass, true);
	}

	public static <V> Lazy<V> create(Supplier<V> factoryMethod,
			boolean threadSafe) {
		if (threadSafe) {
			return new ThreadSafeLazy<V>(factoryMethod);
		}

		return new Lazy<V>(factoryMethod);
	}

	public static <V> Lazy<V> create(Supplier<V> factoryMethod) {
		return create(factoryMethod, true);
	}

	private Lazy(Class<V> valueClass) {
		factory = () -> {
			try {
				return valueClass.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	private Lazy(Supplier<V> factoryMethod) {
		if (factoryMethod == null) {
			throw new NullPointerException();
		}
		this.factory = factoryMethod;
	}

	public boolean created() {
		return created;
	}

	public V value() {
		if (!created()) {
			initialize();
			if (getValue0() == null) {
				throw new NullPointerException(FACTORY_NULL_RETURN_MESSAGE);
			}
		}
		return getValue0();
	}

	protected V getValue0() {
		return value;
	}

	public Optional<V> optional() {
		if (!created()) {
			initialize();
		}
		return Optional.ofNullable(getValue0());
	}

	protected void initialize() {
		value = factory.get();
		created = true;
	}

	@Override
	public String toString() {
		return value().toString();
	}

	private static class ThreadSafeLazy<V> extends Lazy<V> {

		private static final long serialVersionUID = 1L;

		private AtomicBoolean created = new AtomicBoolean(false);
		private AtomicReference<V> value = new AtomicReference<V>();
		private ReentrantLock writeLock = new ReentrantLock();

		private ThreadSafeLazy(Class<V> valueClass) {
			super(valueClass);
		}

		private ThreadSafeLazy(Supplier<V> factoryMethod) {
			super(factoryMethod);
		}

		@Override
		public boolean created() {
			return created.get();
		}

		@Override
		protected V getValue0() {
			return value.get();
		}

		@Override
		protected void initialize() {
			writeLock.lock();
			try {
				if (value.get() == null) {
					if (value.compareAndSet(null, factory.get())) {
						created.set(true);
					}
				}
			} finally {
				writeLock.unlock();
			}
		}

	}
}