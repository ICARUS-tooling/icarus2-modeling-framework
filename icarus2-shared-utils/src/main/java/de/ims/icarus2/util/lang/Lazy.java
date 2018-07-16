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
 *
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
 * class's default constructor inspired from C# Lazy&lt;T> using Lambda and
 * Method reference capabilities of Java 8. All exceptions resulting from
 * factory methods are cascaded to caller. Exceptions from default constructor
 * is wrapped as a RuntimeException. Throws NullPointerException if the factory
 * method itself is null or returns null on execution to fail fast. Available as
 * both Thread safe(default) and unsafe versions. Usage examples
 *
 * <pre>
 * public Lazy&lt;IntensiveResource> r = Lazy.create(IntensiveResource.class, false);
 *
 * public Lazy&lt;IntensiveResource> r1 = Lazy.create(IntensiveResource::buildResource);
 *
 * public Lazy&lt;IntensiveResource> r2 = Lazy.create(() -> return new IntensiveResource());
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
		} else {
			return new Lazy<V>(valueClass);
		}
	}

	public static <V> Lazy<V> create(Class<V> valueClass) {
		return create(valueClass, true);
	}

	public static <V> Lazy<V> create(Supplier<V> factoryMethod,
			boolean threadSafe) {
		if (threadSafe) {
			return new ThreadSafeLazy<V>(factoryMethod);
		} else {
			return new Lazy<V>(factoryMethod);
		}
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