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
package de.ims.icarus2.model.standard.members.layer.annotation.unbound;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationFlag;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.standard.members.layer.annotation.AbstractObjectMapStorage;
import de.ims.icarus2.util.MutablePrimitives.GenericTypeAwareMutablePrimitive;
import de.ims.icarus2.util.MutablePrimitives.MutablePrimitive;
import de.ims.icarus2.util.Wrapper;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.lang.ClassUtils;
import de.ims.icarus2.util.lang.Primitives;
import de.ims.icarus2.util.mem.ByteAllocator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(AnnotationStorage.class)
public class ComplexAnnotationStorage extends AbstractObjectMapStorage<ComplexAnnotationStorage.AnnotationBundle> {

	public static ComplexAnnotationStorage forManifest(AnnotationLayerManifest manifest) {
		int keyCount = manifest.getAvailableKeys().size();
		boolean unbound = manifest.isAnnotationFlagSet(AnnotationFlag.UNKNOWN_KEYS);

		Supplier<AnnotationBundle> bundleFactory = GROWING_BUNDLE_FACTORY;

		if(!unbound) {
			if(keyCount<=CompactAnnotationBundle.DEFAULT_CAPACITY) {
				bundleFactory = COMPACT_BUNDLE_FACTORY;
			} else {
				bundleFactory = LARGE_BUNDLE_FACTORY;
			}
		}

		return new ComplexAnnotationStorage(bundleFactory);
	}

	/** Produces {@link Map}-based bundles for arbitrarily large amounts of annotations */
	public static final Supplier<AnnotationBundle> LARGE_BUNDLE_FACTORY = LargeAnnotationBundle::new;
	/** Produces array-based bundles for small numbers of annotations */
	public static final Supplier<AnnotationBundle> COMPACT_BUNDLE_FACTORY = CompactAnnotationBundle::new;
	/**
	 * Produces special wrapper bundles that start out as as array-based,
	 * but grow into (or subsequently shrink from) {@link Map}-based storage
	 * depending on the number of annotation entries they hold.
	 */
	public static final Supplier<AnnotationBundle> GROWING_BUNDLE_FACTORY = GrowingAnnotationBundle::new;


	/** Factory for creating annotation bundles (constructor assigns default implementation) */
	private Supplier<AnnotationBundle> bundleFactory;

	/** Fallback bundle for items without a mapped entry */
	private AnnotationBundle noEntryValues;

	/** Lookup to check when setting object values */
	private Map<String, ValueType> valueTypes;

	private boolean allowUnknownKeys;

	/**
	 * Creates a new {@link ComplexAnnotationStorage} that does not use weak keys,
	 * defaults to an internally determined initial capacity and uses the
	 * {@link #GROWING_BUNDLE_FACTORY} factory to create new annotation bundles.
	 */
	public ComplexAnnotationStorage() {
		this(false, -1, GROWING_BUNDLE_FACTORY);
	}

	/**
	 * Creates a new {@link ComplexAnnotationStorage} that does not use weak keys,
	 * defaults to an internally determined initial capacity and uses the
	 * specified factory to create new annotation bundles.
	 */
	public ComplexAnnotationStorage(Supplier<AnnotationBundle> bundleFactory) {
		this(false, -1, bundleFactory);
	}

	/**
	 * Creates a new {@link ComplexAnnotationStorage} that does not use weak keys,
	 * uses the given initial capacity and uses the
	 * specified factory to create new annotation bundles.
	 */
	public ComplexAnnotationStorage(int initialCapacity, Supplier<AnnotationBundle> bundleFactory) {
		this(false, initialCapacity, bundleFactory);
	}

	/**
	 * @param weakKeys
	 * @param initialCapacity
	 * @param bundleFactory the factory that creates new {@link AnnotationBundle} instances for
	 * this storage. Note that unlike the general {@link Supplier} contract it is required that
	 * the given factory creates a <b>new</b> bundle instance for each invocation
	 * of {@link Supplier#get()} and never returns {@code null}!
	 */
	public ComplexAnnotationStorage(boolean weakKeys, int initialCapacity, Supplier<AnnotationBundle> bundleFactory) {
		super(weakKeys, initialCapacity);

		requireNonNull(bundleFactory);

		this.bundleFactory = bundleFactory;
	}

	@VisibleForTesting
	Supplier<AnnotationBundle> getBundleFactory() { return bundleFactory; }

	@VisibleForTesting
	AnnotationBundle getNoEntryValues() { return noEntryValues; }

	@VisibleForTesting
	Map<String, ValueType> getValueTypes() { return valueTypes; }

	@VisibleForTesting
	boolean isAllowUnknownKeys() { return allowUnknownKeys; }

	/**
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractObjectMapStorage#addNotify(de.ims.icarus2.model.api.layer.AnnotationLayer)
	 */
	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		// Read manifest to construct bundle of noEntryValues and our type lookup

		valueTypes = new Object2ObjectOpenHashMap<>();

		AnnotationLayerManifest manifest = layer.getManifest();
		AnnotationBundle noEntryValues = createBuffer();

		for(AnnotationManifest annotationManifest : manifest.getAnnotationManifests()) {
			String key = annotationManifest.getKey().orElseThrow(
					ManifestException.error("Missing key"));

			// Needs to happen first, as our internal sanity checks depend on it!
			valueTypes.put(key, annotationManifest.getValueType());

			annotationManifest.getNoEntryValue().ifPresent(
					value -> setValuePrimitiveAware(noEntryValues, key, value));
		}

		this.noEntryValues = noEntryValues;

		allowUnknownKeys = manifest.isAnnotationFlagSet(AnnotationFlag.UNKNOWN_KEYS);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractObjectMapStorage#removeNotify(de.ims.icarus2.model.api.layer.AnnotationLayer)
	 */
	@Override
	public void removeNotify(AnnotationLayer layer) {
		super.removeNotify(layer);

		noEntryValues = null;
		valueTypes.clear();
		valueTypes = null;
		allowUnknownKeys = false;
	}

	@Override
	protected AnnotationBundle createBuffer() {
		return bundleFactory.get();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractObjectMapStorage#getFallbackBuffer()
	 */
	@Override
	protected AnnotationBundle getFallbackBuffer() {
		return noEntryValues;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractManagedAnnotationStorage#collectKeys(de.ims.icarus2.model.api.members.item.Item, java.util.function.Consumer)
	 */
	@Override
	public boolean collectKeys(Item item, Consumer<String> action) {
		AnnotationBundle bundle = getBuffer(item, false);

		if(bundle==null || bundle==noEntryValues) {
			return false;
		}

		return bundle.collectKeys(action);
	}

	private ValueType checkKey(String key) {
		requireNonNull(key);
		ValueType type = valueTypes.get(key);
		if(type==null && !allowUnknownKeys)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Key not supported "+key);
		return type;
	}

	private void checkKeyAndValue(String key, Object value) {
		requireNonNull(key);
		ValueType type = valueTypes.get(key);
		if(type!=null) {
			type.checkValue(value);
		} else if(!allowUnknownKeys)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Key not supported "+key);

	}

	@Override
	public Object getValue(Item item, String key) {
		AnnotationBundle bundle = getBuffer(item);
		checkKey(key);
		Object value = bundle.getValue(key);

		if(value instanceof Wrapper) {
			value = ((Wrapper<?>)value).get();
		}

		return value;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractManagedAnnotationStorage#getString(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public String getString(Item item, String key) {
		return (String) getValue(item, key);
	}

	private final Function<String, MutablePrimitive<?>> DEFAULT_STORAGE_FACTORY = key -> {
		ValueType type = checkKey(key);
		GenericTypeAwareMutablePrimitive primitive = new GenericTypeAwareMutablePrimitive();

		if(type!=null) {
			if(!type.isPrimitiveType())
				throw new ModelException(ManifestErrorCode.MANIFEST_TYPE_CAST,
						"Key "+key+" does not support primitive values: "+type);

			primitive.setType(GenericTypeAwareMutablePrimitive.typeOf(type.getBaseClass()));
		}

		return primitive;
	};


	private void setValuePrimitiveAware(AnnotationBundle bundle, String key, Object value) {

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
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, Object value) {
		requireNonNull(item);
		requireNonNull(key);

		if(value==null) {
			AnnotationBundle bundle = getBuffer(item);
			if(bundle!=null && bundle!=noEntryValues) {
				bundle.setValue(key, null);
			}
		} else {
			checkKeyAndValue(key, value);
			AnnotationBundle bundle = getBuffer(item, true);
			setValuePrimitiveAware(bundle, key, value);
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractManagedAnnotationStorage#setString(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.String)
	 */
	@Override
	public void setString(Item item, String key, String value) {
		setValue(item, key, value);
	}

	/**
	 * Fetch current value for given item and key and cast to
	 * {@link MutablePrimitive} if present.
	 */
	private MutablePrimitive<?> getPrimitive(Item item, String key, ValueType type) {
		AnnotationBundle bundle = getBuffer(item);
		checkKey(key);
		MutablePrimitive<?> primitive = bundle==null ? null
				: (MutablePrimitive<?>)bundle.getValue(key);
		if(primitive==null)
			throw forUnsupportedGetter(type, key);
		return primitive;
	}

	/**
	 * Fetch current value for given item and key and cast to
	 * {@link MutablePrimitive} if present.
	 */
	private MutablePrimitive<?> ensurePrimitive(Item item, String key, ValueType type) {
		checkKey(key);
		return getBuffer(item, true).getValue(key, DEFAULT_STORAGE_FACTORY);
	}

	@Override
	public int getInteger(Item item, String key) {
		return getPrimitive(item, key, ValueType.INTEGER).intValue();
	}

	@Override
	public float getFloat(Item item, String key) {
		return getPrimitive(item, key, ValueType.FLOAT).floatValue();
	}

	@Override
	public double getDouble(Item item, String key) {
		return getPrimitive(item, key, ValueType.DOUBLE).doubleValue();
	}

	@Override
	public long getLong(Item item, String key) {
		return getPrimitive(item, key, ValueType.LONG).longValue();
	}

	@Override
	public boolean getBoolean(Item item, String key) {
		return getPrimitive(item, key, ValueType.BOOLEAN).booleanValue();
	}

	@Override
	public void setInteger(Item item, String key, int value) {
		ensurePrimitive(item, key, ValueType.INTEGER).setInt(value);
	}

	@Override
	public void setLong(Item item, String key, long value) {
		ensurePrimitive(item, key, ValueType.LONG).setLong(value);
	}

	@Override
	public void setFloat(Item item, String key, float value) {
		ensurePrimitive(item, key, ValueType.FLOAT).setFloat(value);
	}

	@Override
	public void setDouble(Item item, String key, double value) {
		ensurePrimitive(item, key, ValueType.DOUBLE).setDouble(value);
	}

	@Override
	public void setBoolean(Item item, String key, boolean value) {
		ensurePrimitive(item, key, ValueType.BOOLEAN).setBoolean(value);
	}

	/**
	 * Models a set of key-value pairs describing annotations for a single item.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface AnnotationBundle {

		@SuppressWarnings("unchecked")
		default <T extends Object> T getValue(String key, Function<String,T> defaultValue) {
			T value = (T) getValue(key);
			if(value==null) {
				value = defaultValue.apply(key);
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
		 * bundle to the external {@code buffer} consumer.
		 *
		 * @param buffer
		 */
		boolean collectKeys(Consumer<String> buffer);
	}

	public static final class LargeAnnotationBundle extends Object2ObjectOpenHashMap<String, Object> implements AnnotationBundle {

		private static final long serialVersionUID = -3058615796981616593L;

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#getValue(java.lang.String)
		 */
		@Override
		public Object getValue(String key) {
			return get(key);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#setByte(java.lang.String, java.lang.Object)
		 */
		@Override
		public boolean setValue(String key, Object value) {
			put(key, value);
			return true;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#collectKeys(java.util.Collection)
		 */
		@Override
		public boolean collectKeys(Consumer<String> buffer) {
			keySet().forEach(buffer);
			return !isEmpty();
		}
	}

	public static final class CompactAnnotationBundle implements AnnotationBundle {

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
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#getValue(java.lang.String)
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
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#setByte(java.lang.String, java.lang.Object)
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
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#collectKeys(java.util.Collection)
		 */
		@Override
		public boolean collectKeys(Consumer<String> buffer) {
			boolean result = false;
			for(int i=0; i<data.length-1; i+=2) {
				if(data[i]!=null) {
					result = true;
					buffer.accept((String) data[i]);
				}
			}
			return result;
		}
	}

	public static final class GrowingAnnotationBundle implements AnnotationBundle {

		public static final int DEFAULT_CAPACITY = 8;
		public static final int ARRAY_THRESHOLD = 16;

		private Object data;

		public GrowingAnnotationBundle() {
			this(DEFAULT_CAPACITY);
		}

		/**
		 * Creates a new compact bundle with initial storage for a number
		 * of entries equal to the {@code capacity} parameter or {@link #DEFAULT_CAPACITY}
		 * if the supplied {@code capacity} is too small.
		 */
		public GrowingAnnotationBundle(int capacity) {
			if(capacity<DEFAULT_CAPACITY) {
				capacity = DEFAULT_CAPACITY;
			}
			data = new Object[capacity*2];
		}

		@VisibleForTesting
		boolean isMap() {
			return data instanceof Map;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#getValue(java.lang.String)
		 */
		@Override
		public Object getValue(String key) {
			if(data instanceof Object[]) {
				Object[] arary = (Object[]) data;
				for(int i=0; i<arary.length-1; i+=2) {
					if(arary[i]!=null && arary[i].equals(key)) {
						return arary[i+1];
					}
				}
				return null;
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)data;
			return map.get(key);
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#setByte(java.lang.String, java.lang.Object)
		 */
		@Override
		public boolean setValue(String key, Object value) {
			if(data instanceof Object[]) {
				Object[] array = (Object[]) data;
				if(value==null) {
					// Seek and remove entry
					for(int i=0; i<array.length-1; i+=2) {
						if(array[i]!=null && array[i].equals(key)) {
							if(array[i+1] == null) {
								return false;
							}
							array[i] = null;
							array[i+1] = null;
							return true;
						}
					}
				} else {
					// Seek and insert or update entry
					for(int i=0; i<array.length-1; i+=2) {
						if(array[i]==null || array[i].equals(key)) {
							if(value.equals(array[i+1])) {
								return false;
							}
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
				return doMapOp(key, value);
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

			if(map.size()>ARRAY_THRESHOLD) {
				return;
			}

			int size = Math.max(DEFAULT_CAPACITY, map.size());
			Object[] array = new Object[size*2];
			int pos = 0;
			for(Map.Entry<String, Object> e : map.entrySet()) {
				array[pos++] = e.getKey();
				array[pos++] = e.getValue();
			}

			data = array;
		}

		private boolean doMapOp(String key, Object value) {
			boolean result;
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)data;
			if(value==null) {
				result = map.remove(key) != null;
				maybeShrink();
			} else {
				//TODO we test for object identity here, not content, maybe change to "!value.equals(map.put(key, value))" ?
				result = map.put(key, value) != value;
			}
			return result;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationLayer.AnnotationBundle#collectKeys(java.util.Collection)
		 */
		@Override
		public boolean collectKeys(Consumer<String> buffer) {
			if(data instanceof Object[]) {
				Object[] arary = (Object[]) data;
				boolean result = false;
				for(int i=0; i<arary.length-1; i+=2) {
					if(arary[i]!=null) {
						result = true;
						buffer.accept((String) arary[i]);
					}
				}
				return result;
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)data;
			map.keySet().forEach(buffer);
			return !map.isEmpty();
		}
	}
}
