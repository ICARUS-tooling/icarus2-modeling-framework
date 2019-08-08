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
package de.ims.icarus2.model.standard.members.layers.annotation.fixed;

import static de.ims.icarus2.model.util.ModelUtils.getName;

import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.MutablePrimitives;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableFloat;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.MutablePrimitive;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.lang.Primitives;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(AnnotationStorage.class)
public class FixedKeysMixedObjectStorage extends AbstractFixedKeysStorage<Object[]> {

	private boolean[] primitivesMask;

	public FixedKeysMixedObjectStorage() {
		this(-1);
	}

	public FixedKeysMixedObjectStorage(int initialCapacity) {
		this(false, initialCapacity);
	}

	public FixedKeysMixedObjectStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		primitivesMask = createPrimitivesMask();
	}

	@Override
	public void removeNotify(AnnotationLayer layer) {
		super.removeNotify(layer);

		primitivesMask = null;
	}

	protected boolean[] createPrimitivesMask() {
		IndexLookup indexLookup = getIndexLookup();

		boolean[] mask = new boolean[indexLookup.keyCount()];
		Object[] noEntryValues = getNoEntryValues();

		for(int i=0; i<mask.length; i++) {
			mask[i] = isPrimitiveWrapper(noEntryValues[i]);
		}

		return mask;
	}

	@Override
	protected Object[] createNoEntryValues(AnnotationLayer layer,
			IndexLookup indexLookup) {

		AnnotationLayerManifest layerManifest = layer.getManifest();

		// Create and populate 'default' values
		Object[] noEntryValues = new Object[indexLookup.keyCount()];
		for(int i=0; i<noEntryValues.length; i++) {
			String key = indexLookup.keyAt(i);
			AnnotationManifest annotationManifest = requireAnnotationsManifest(layerManifest, key);

			final int index = i;
			annotationManifest.getNoEntryValue().ifPresent(noEntryValue -> {
				ValueType valueType = annotationManifest.getValueType();

				//TODO maybe ensure a kind of 'default' noEntryValue if manifest misses to declare one?
				noEntryValues[index] = wrapMutable(noEntryValue, valueType);
			});
		}

		return noEntryValues;
	}

	protected boolean isPrimitive(int keyIndex) {
		return primitivesMask[keyIndex];
	}

	protected Object wrapMutable(Object value, ValueType valueType) {
		if(valueType==ValueType.INTEGER) {
			value = new MutablePrimitives.MutableInteger(((Number) value).intValue());
		} else if(valueType==ValueType.LONG) {
			value = new MutablePrimitives.MutableLong(((Number) value).longValue());
		} else if(valueType==ValueType.FLOAT) {
			value = new MutablePrimitives.MutableFloat(((Number) value).floatValue());
		} else if(valueType==ValueType.DOUBLE) {
			value = new MutablePrimitives.MutableDouble(((Number) value).doubleValue());
		} else if(valueType==ValueType.BOOLEAN) {
			value = new MutablePrimitives.MutableBoolean(((Boolean) value).booleanValue());
		}

		return value;
	}

	protected MutablePrimitive<?> wrapMutable(Object value) {
		Class<?> clazz = value.getClass();

		if(clazz==Integer.class || clazz==Short.class || clazz==Byte.class) {
			value = new MutablePrimitives.MutableInteger(((Number) value).intValue());
		} else if(clazz==Long.class) {
			value = new MutablePrimitives.MutableLong(((Number) value).longValue());
		} else if(clazz==Float.class) {
			value = new MutablePrimitives.MutableFloat(((Number) value).floatValue());
		} else if(clazz==Double.class) {
			value = new MutablePrimitives.MutableDouble(((Number) value).doubleValue());
		} else if(clazz==Boolean.class) {
			value = new MutablePrimitives.MutableBoolean(((Boolean) value).booleanValue());
		} else if(clazz==Void.class) {
			value = null;
		}

		return (MutablePrimitive<?>) value;
	}

	protected Object unwrapMutable(Object value) {
		if(value instanceof Primitive) {
			value = ((Primitive<?>)value).get();
		}

		return value;
	}

	@Override
	public boolean collectKeys(Item item, Consumer<String> action) {
		Object[] buffer = getBuffer(item);
		Object[] noEntryValues = getNoEntryValues();

		if(buffer==null) {
			return false;
		}

		IndexLookup indexLookup = getIndexLookup();

		boolean keysReported = false;

		for(int i=0; i<indexLookup.keyCount(); i++) {
			if(buffer[i]!=null && !buffer[i].equals(noEntryValues[i])) {
				action.accept(indexLookup.keyAt(i));
				keysReported = true;
			}
		}

		return keysReported;
	}

	@Override
	protected Object[] createBuffer() {
		return new Object[getKeyCount()];
	}

	@Override
	public Object getValue(Item item, String key) {
		int index = checkKeyAndGetIndex(key);
		Object[] buffer = getBuffer(item);

		if(buffer==null) {
			buffer = getNoEntryValues();
		}

		return unwrapMutable(buffer[index]);
	}

	@Override
	public void setValue(Item item, String key, Object value) {
		int index = checkKeyAndGetIndex(key);
		Object[] buffer = getBuffer(item, value!=null);

		if(buffer==null) {
			if(value==null) {
				return;
			}

			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Missing value buffer for item "+getName(item));
		}

		assert buffer!=null;

		/*
		 * Somewhat expensive strategy:
		 *
		 * If key is declared to map to primitive values, we need to either make
		 * sure the given value already is a valid primitive wrapper or that we
		 * can properly wrap it in such.
		 *
		 * Note that in case the given value is the Void wrapper, the value
		 * will be treated as 'null'.
		 */
		if(value!=null && isPrimitive(index) && !isPrimitiveWrapper(value)) {
			if(Primitives.isPrimitiveWrapperClass(value.getClass())) {
				MutablePrimitive<?> wrapper = (MutablePrimitive<?>) buffer[index];

				if(wrapper==null) {
					wrapper = wrapMutable(value);
				} else {
					wrapper.fromWrapper(value);
				}

				value = wrapper;
			} else
				throw new ModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
						"Key "+key+" is declared to map to primitive values - got: "+value);
		}

		buffer[index] = value;
	}

	protected boolean isPrimitiveWrapper(Object value) {
		return MutablePrimitive.class.isInstance(value);
	}

	@Override
	public int getInteger(Item item, String key) {
		return getPrimitive(item, key).intValue();
	}

	@Override
	public float getFloat(Item item, String key) {
		return getPrimitive(item, key).floatValue();
	}

	@Override
	public double getDouble(Item item, String key) {
		return getPrimitive(item, key).doubleValue();
	}

	@Override
	public long getLong(Item item, String key) {
		return getPrimitive(item, key).longValue();
	}

	@Override
	public boolean getBoolean(Item item, String key) {
		return getPrimitive(item, key).booleanValue();
	}

	protected MutablePrimitive<?> getPrimitive(Item item, String key) {

		Object value = getValue(item, key);

		if(value==null)
			throw new ModelException(ModelErrorCode.MODEL_INVALID_REQUEST,
					"Given key is not declared to map to primitive values or a 'noEntryValue' declraration is missing from the manifest: "+key);

		return (MutablePrimitive<?>) value;
	}

	/**
	 * Looks up the current entry in the buffer array for the given {@code item} and
	 * {@code key} and if it is {@code null} creates a new one based on the provided
	 * class.
	 *
	 * @param item
	 * @param key
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends MutablePrimitive<?>> T ensureAndGetPrimitive(Item item, String key, Class<T> clazz) {
		int index = checkKeyAndGetIndex(key);
		Object[] buffer = getBuffer(item);

		Object value = buffer[index];

		if(value==null) {
			try {
				value = clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Unable to instantiate primitive buffer object");
			}

			buffer[index] = value;
		}

		return (T)value;
	}

	@Override
	public void setInteger(Item item, String key, int value) {
		ensureAndGetPrimitive(item, key, MutableInteger.class).setInt(value);
	}

	@Override
	public void setLong(Item item, String key, long value) {
		ensureAndGetPrimitive(item, key, MutableLong.class).setLong(value);
	}

	@Override
	public void setFloat(Item item, String key, float value) {
		ensureAndGetPrimitive(item, key, MutableFloat.class).setFloat(value);
	}

	@Override
	public void setDouble(Item item, String key, double value) {
		ensureAndGetPrimitive(item, key, MutableDouble.class).setDouble(value);
	}

	@Override
	public void setBoolean(Item item, String key, boolean value) {
		ensureAndGetPrimitive(item, key, MutableBoolean.class).setBoolean(value);
	}

	/**
	 * Traverses the buffer array that holds annotation values for the specified item
	 * and returns {@code true} if that buffer holds at least {@code 1} non null entry.
	 * Note that this check does not compare entries to their respective {@code noEntryValue}s
	 * to only consider <i>real</i> annotation values.
	 *
	 * @see de.ims.icarus2.model.standard.members.layers.annotation.AbstractAnnotationStorage#hasAnnotations(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean hasAnnotations(Item item) {
		Object[] buffer = getBuffer(item);

		if(buffer==null) {
			return false;
		}

		for(int i=0; i<buffer.length; i++) {
			if(buffer[i]!=null) {
				return true;
			}
		}

		return false;
	}
}
