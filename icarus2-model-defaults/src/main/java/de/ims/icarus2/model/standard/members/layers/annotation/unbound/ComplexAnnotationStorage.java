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
package de.ims.icarus2.model.standard.members.layers.annotation.unbound;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.standard.members.layers.annotation.AbstractObjectMapStorage;
import de.ims.icarus2.util.MutablePrimitives.GenericTypeAwareMutablePrimitive;
import de.ims.icarus2.util.MutablePrimitives.MutablePrimitive;
import de.ims.icarus2.util.Wrapper;
import de.ims.icarus2.util.lang.ClassUtils;
import de.ims.icarus2.util.lang.Primitives;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class ComplexAnnotationStorage extends AbstractObjectMapStorage<ComplexAnnotationStorage.AnnotationBundle> {

	// Factory for creating annotation bundles (constructor assigns default implementation)
	private Supplier<AnnotationBundle> bundleFactory;

	private AnnotationBundle noEntryValues; //FIXME use this

	public static final Supplier<AnnotationBundle> LARGE_BUNDLE_FACTORY = LargeAnnotationBundle::new;
	public static final Supplier<AnnotationBundle> COMPACT_BUNDLE_FACTORY = CompactAnnotationBundle::new;
	public static final Supplier<AnnotationBundle> GROWING_BUNDLE_FACTORY = GrowingAnnotationBundle::new;


	public ComplexAnnotationStorage(Supplier<AnnotationBundle> bundleFactory) {
		this(false, -1, bundleFactory);
	}

	public ComplexAnnotationStorage(int initialCapacity, Supplier<AnnotationBundle> bundleFactory) {
		this(false, initialCapacity, bundleFactory);
	}

	/**
	 * @param weakKeys
	 * @param initialCapacity
	 * @param bundleFactory the factory that creates new {@link AnnotationBundle} instances for this storage. Note that unlike
	 * the general {@link Supplier} contract it is required that the given factory creates a <b>new</b> bundle instance for
	 * each invocation of {@link Supplier#get()}!
	 */
	public ComplexAnnotationStorage(boolean weakKeys, int initialCapacity, Supplier<AnnotationBundle> bundleFactory) {
		super(weakKeys, initialCapacity);

		requireNonNull(bundleFactory);

		this.bundleFactory = bundleFactory;
	}

	@Override
	protected AnnotationBundle createBuffer() {
		return bundleFactory.get();
	}

	@Override
	public Object getValue(Item item, String key) {
		AnnotationBundle bundle = getBuffer(item);
		if(bundle==null) {
			return null;
		}

		Object value = bundle.getValue(key);

		if(value instanceof Wrapper) {
			value = ((Wrapper<?>)value).get();
		}

		return value;
	}

	private static final Supplier<MutablePrimitive<?>> DEFAULT_STORAGE_FACTORY = GenericTypeAwareMutablePrimitive::new;

	/**
	 * Assigns {@code value} as the mapping for {@code key} on the given {@code item}.
	 * If {@code value} is {@code null}, the mapping will be erased if present. Otherwise
	 * the following procedure is executed:
	 * <p>
	 * If the {@code value} is a {@link ClassUtils#isPrimitiveWrapperClass(Class) primitive wrapper}
	 * it will be stored as a {@link GenericTypeAwareMutablePrimitive} instance in the respective
	 * {@link AnnotationBundle bundle}. Note however, that in case there has already been a previous
	 * mapping for the given {@code item} and {@code key} with a value that is not assignment compatible
	 * to {@code GenericTypeAwareMutablePrimitive} the method will fail with a {@link ClassCastException}!
	 * In case the given {@code value} is not a primitive wrapper it will simply be
	 * {@link AnnotationBundle#setValue(String, Object) stored} in the annotation mapping.
	 *
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, Object value) {
		requireNonNull(item);
		requireNonNull(key);

		if(value==null) {
			AnnotationBundle bundle = getBuffer(item);
			if(bundle!=null) {
				bundle.setValue(key, null);
			}
		} else {
			AnnotationBundle bundle = getBuffer(item, true);

			if(Primitives.isPrimitiveWrapperClass(value.getClass())) {
				// Will fail with ClassCastException in case previous mappings didn't use correct wrapper!
				MutablePrimitive<?> current = bundle.getValue(key, DEFAULT_STORAGE_FACTORY);
				// Let storage implementation handle primitive conversion
				current.fromWrapper(value);
			} else {
				// Covers both general classes and MutablePrimitive instances
				bundle.setValue(key, value);
			}
		}
	}

	/**
	 * Fetch current value for given item and key and cast to
	 * {@link MutablePrimitive} if present.
	 */
	private MutablePrimitive<?> getPrimitive(Item item, String key) {
		AnnotationBundle bundle = getBuffer(item);
		return bundle==null ? null : (MutablePrimitive<?>)bundle.getValue(key);
	}

	/**
	 * Fetch current value for given item and key and cast to
	 * {@link MutablePrimitive} if present.
	 */
	private MutablePrimitive<?> ensurePrimitive(Item item, String key) {
		return getBuffer(item, true).getValue(key, DEFAULT_STORAGE_FACTORY);
	}

	@Override
	public int getIntegerValue(Item item, String key) {
		MutablePrimitive<?> primitive = getPrimitive(item, key);
		return primitive==null ? 0 : primitive.intValue();
	}

	@Override
	public float getFloatValue(Item item, String key) {
		MutablePrimitive<?> primitive = getPrimitive(item, key);
		return primitive==null ? 0F : primitive.floatValue();
	}

	@Override
	public double getDoubleValue(Item item, String key) {
		MutablePrimitive<?> primitive = getPrimitive(item, key);
		return primitive==null ? 0D : primitive.doubleValue();
	}

	@Override
	public long getLongValue(Item item, String key) {
		MutablePrimitive<?> primitive = getPrimitive(item, key);
		return primitive==null ? 0L : primitive.longValue();
	}

	@Override
	public boolean getBooleanValue(Item item, String key) {
		MutablePrimitive<?> primitive = getPrimitive(item, key);
		return primitive==null ? false : primitive.booleanValue();
	}

	@Override
	public void setIntegerValue(Item item, String key, int value) {
		ensurePrimitive(item, key).setInt(value);
	}

	@Override
	public void setLongValue(Item item, String key, long value) {
		ensurePrimitive(item, key).setLong(value);
	}

	@Override
	public void setFloatValue(Item item, String key, float value) {
		ensurePrimitive(item, key).setFloat(value);
	}

	@Override
	public void setDoubleValue(Item item, String key, double value) {
		ensurePrimitive(item, key).setDouble(value);
	}

	@Override
	public void setBooleanValue(Item item, String key, boolean value) {
		ensurePrimitive(item, key).setBoolean(value);
	}

	/**
	 * Models a set of key-value pairs describing annotations for a single item.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface AnnotationBundle {

		@SuppressWarnings("unchecked")
		default <T extends Object> T getValue(String key, Supplier<T> defaultValue) {
			T value = (T) getValue(key);
			if(value==null) {
				value = defaultValue.get();
				setValue(key, value);
			}

			return value;
		}

		/**
		 * Fetches the mapped annotation value for the specified {@code key} or
		 * {@code null} if no value is stored for that {@code key}.
		 *
		 * @param key
		 * @return
		 */
		Object getValue(String key);

		/**
		 * Maps the given {@code value} (allowed to be {@code null}) to
		 * the specified {@code key}.
		 *
		 * @param key
		 * @param value
		 * @return {@code true} iff the content of this bundle was changed by
		 * this method (i.e. there either was no mapping for {@code key} prior
		 * to calling this method or the previously mapped value did not equal the
		 * new {@code value} parameter).
		 */
		boolean setValue(String key, Object value);

		/**
		 * Collects and sends all the currently used keys in this
		 * bundle to the external {@code buffer} collection.
		 *
		 * @param buffer
		 */
		void collectKeys(Consumer<String> buffer);
	}

	public static class LargeAnnotationBundle extends HashMap<String, Object> implements AnnotationBundle {

		private static final long serialVersionUID = -3058615796981616593L;

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#getValue(java.lang.String)
		 */
		@Override
		public Object getValue(String key) {
			return get(key);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#setByte(java.lang.String, java.lang.Object)
		 */
		@Override
		public boolean setValue(String key, Object value) {
			put(key, value);
			return true;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#collectKeys(java.util.Collection)
		 */
		@Override
		public void collectKeys(Consumer<String> buffer) {
			keySet().forEach(buffer);
		}
	}

	public static class CompactAnnotationBundle implements AnnotationBundle {

		public static final int DEFAULT_CAPACITY = 6;

		private final Object[] data;

		public CompactAnnotationBundle() {
			this(DEFAULT_CAPACITY);
		}

		/**
		 * Creates a new compact bundle with initial storage for a number
		 * of entries equal to the {@code capacity} parameter.
		 */
		public CompactAnnotationBundle(int capacity) {
			data = new Object[capacity*2];
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#getValue(java.lang.String)
		 */
		@Override
		public Object getValue(String key) {
			for(int i=0; i<data.length-1; i+=2) {
				if(data[i]!=null && data[i].equals(key)) {
					return data[i+1];
				}
			}

			return null;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#setByte(java.lang.String, java.lang.Object)
		 */
		@Override
		public boolean setValue(String key, Object value) {
			for(int i=0; i<data.length-1; i+=2) {
				if(data[i]==null || data[i].equals(key)) {
					data[i] = key;
					data[i+1] = value;
					return true;
				}
			}

			return false;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#collectKeys(java.util.Collection)
		 */
		@Override
		public void collectKeys(Consumer<String> buffer) {
			for(int i=0; i<data.length-1; i+=2) {
				if(data[i]!=null) {
					buffer.accept((String) data[i]);
				}
			}
		}
	}

	public static class GrowingAnnotationBundle implements AnnotationBundle {

		public static final int DEFAULT_CAPACITY = 8;
		public static final int ARRAY_THRESHOLD = 16;

		private Object data;

		public GrowingAnnotationBundle() {
			this(DEFAULT_CAPACITY);
		}

		/**
		 * Creates a new compact bundle with initial storage for a number
		 * of entries equal to the {@code capacity} parameter.
		 */
		public GrowingAnnotationBundle(int capacity) {
			data = new Object[capacity*2];
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#getValue(java.lang.String)
		 */
		@Override
		public Object getValue(String key) {
			if(data instanceof Object[]) {
				Object[] arary = (Object[]) data;
				for(int i=1; i<arary.length-1; i+=2) {
					if(arary[i]!=null && arary[i].equals(key)) {
						return arary[i+1];
					}
				}
				return null;
			} else {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>)data;
				return map.get(key);
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#setByte(java.lang.String, java.lang.Object)
		 */
		@Override
		public boolean setValue(String key, Object value) {
			if(data instanceof Object[]) {
				Object[] array = (Object[]) data;
				if(value==null) {
					// Seek and remove entry
					for(int i=0; i<array.length-1; i+=2) {
						if(array[i]!=null && array[i].equals(key)) {
							array[i] = null;
							array[i+1] = null;
							return true;
						}
					}
				} else {
					// Seek and insert or update entry
					for(int i=0; i<array.length-1; i+=2) {
						if(array[i]==null || array[i].equals(key)) {
							array[i] = key;
							array[i+1] = value;
							return true;
						}
					}

					// No slot found, so we have to grow the buffer

					// If we've already exceeded array space go and transform into map
					if(array.length>=ARRAY_THRESHOLD) {
						growToMap();
						doMapOp(key, value);
					} else {
						// Otherwise stay in array form and grow
						int newSlot = array.length;
						array = growArray();
						array[newSlot] = key;
						array[newSlot+1] = value;
					}
				}
			} else {
				doMapOp(key, value);
			}

			return false;
		}

		private Object[] growArray() {
			Object[] array = (Object[]) data;
			array = Arrays.copyOf(array, array.length*2);
			data = array;
			return array;
		}

		private void growToMap() {
			Map<String, Object> map = new Object2ObjectOpenHashMap<>();
			Object[] array = (Object[]) data;
			for(int i=0; i<array.length-1; i+=2) {
				map.put((String) array[i], array[i+1]);
			}
			data = map;
		}

		private void maybeShrink() {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)data;

			checkState(map.size()>ARRAY_THRESHOLD);
		}

		private void doMapOp(String key, Object value) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)data;
			if(value==null) {
				map.remove(key);
				maybeShrink();
			} else {
				//TODO this assignment was missing. need to check if simply throwing the mapping into the map is ok
				map.put(key, value);
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#collectKeys(java.util.Collection)
		 */
		@Override
		public void collectKeys(Consumer<String> buffer) {
			if(data instanceof Object[]) {
				Object[] arary = (Object[]) data;
				for(int i=0; i<arary.length-1; i+=2) {
					if(arary[i]!=null) {
						buffer.accept((String) arary[i]);
					}
				}
			} else {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>)data;
				map.keySet().forEach(buffer);
			}
		}
	}
}
