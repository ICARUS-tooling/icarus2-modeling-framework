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
package de.ims.icarus2.model.standard.members.layer.annotation.fixed;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;

import java.util.function.Consumer;
import java.util.function.Supplier;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableFloat;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.MutablePrimitives.MutablePrimitive;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(AnnotationStorage.class)
public class FixedKeysMixedObjectStorage extends AbstractFixedKeysStorage<Object[]> {

	private ValueType[] valueTypes;
	private Supplier<MutablePrimitive<?>>[] wrappers;

	public FixedKeysMixedObjectStorage() {
		this(UNSET_INT);
	}

	public FixedKeysMixedObjectStorage(int initialCapacity) {
		this(false, initialCapacity);
	}

	public FixedKeysMixedObjectStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	@Override
	public void removeNotify(AnnotationLayer layer) {
		super.removeNotify(layer);

		valueTypes = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object[] createNoEntryValues(AnnotationLayer layer,
			IndexLookup indexLookup) {

		AnnotationLayerManifest layerManifest = layer.getManifest();

		valueTypes = new ValueType[indexLookup.keyCount()];
		wrappers = new Supplier[indexLookup.keyCount()];

		// Create and populate 'default' values
		Object[] noEntryValues = new Object[indexLookup.keyCount()];
		for(int i=0; i<noEntryValues.length; i++) {
			String key = indexLookup.keyAt(i);
			AnnotationManifest annotationManifest = requireAnnotationsManifest(layerManifest, key);

			ValueType valueType = annotationManifest.getValueType();

			valueTypes[i] = valueType;
			Supplier< MutablePrimitive<?>> wrapper = wrapper(valueType);
			wrappers[i] = wrapper;

			final int index = i;
			annotationManifest.getNoEntryValue().ifPresent(noEntryValue -> {
				if(wrapper!=null) {
					MutablePrimitive<?> primitive = wrapper.get();
					primitive.set(noEntryValue);
					noEntryValue = primitive;
				}
				noEntryValues[index] = noEntryValue;
			});
		}

		return noEntryValues;
	}

	protected boolean isPrimitive(int keyIndex) {
		return valueTypes[keyIndex].isPrimitiveType();
	}

	private Supplier<MutablePrimitive<?>> wrapper(ValueType valueType) {
		if(valueType==ValueType.INTEGER) {
			return MutableInteger::new;
		} else if(valueType==ValueType.LONG) {
			return MutableLong::new;
		} else if(valueType==ValueType.FLOAT) {
			return MutableFloat::new;
		} else if(valueType==ValueType.DOUBLE) {
			return MutableDouble::new;
		} else if(valueType==ValueType.BOOLEAN) {
			return MutableBoolean::new;
		} else if(valueType.isPrimitiveType())
			throw new ModelException(ManifestErrorCode.MANIFEST_UNKNOWN_TYPE,
					"Unknown primitive value type: "+valueType);

		return null;
	}

	private Object unwrapMutable(Object value) {
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
		return unwrapMutable(getBuffer(item)[index]);
	}

	@Override
	public String getString(Item item, String key) {
		return (String) getValue(item, key);
	}

	@Override
	public void setValue(Item item, String key, Object value) {
		int index = checkKeyAndGetIndex(key);
		Object[] buffer = getBuffer(item, value!=null);

		if(buffer==null || buffer==getNoEntryValues()) {
			if(value==null) {
				return;
			}

			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Missing value buffer for item "+getName(item));
		}

		assert buffer!=null;

		ValueType valueType = valueTypes[index];

		if(valueType.isPrimitiveType()) {
			MutablePrimitive<?> wrapper = (MutablePrimitive<?>) buffer[index];

			if(wrapper==null) {
				wrapper = wrappers[index].get();
			}

			wrapper.fromWrapper(value);
			value = wrapper;
		}

		buffer[index] = value;
	}

	@Override
	public void setString(Item item, String key, String value) {
		setValue(item, key, value);
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

	private MutablePrimitive<?> getPrimitive(Item item, String key) {
		int index = checkKeyAndGetIndex(key);
		Object value = getBuffer(item)[index];

		if(value==null)
			throw new ModelException(ManifestErrorCode.MANIFEST_TYPE_CAST,
					"Given key is not declared to map to primitive values or"
					+ " a 'noEntryValue' declaration is missing from the manifest: "+key);

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
	private <T extends MutablePrimitive<?>> MutablePrimitive<?> ensureAndGetPrimitive(
			Item item, String key) {
		int index = checkKeyAndGetIndex(key);
		Object[] buffer = getBuffer(item);

		Object value = buffer[index];

		if(value==null) {
			value = wrappers[index].get();
			buffer[index] = value;
		}

		return (MutablePrimitive<?>)value;
	}

	@Override
	public void setInteger(Item item, String key, int value) {
		ensureAndGetPrimitive(item, key).setInt(value);
	}

	@Override
	public void setLong(Item item, String key, long value) {
		ensureAndGetPrimitive(item, key).setLong(value);
	}

	@Override
	public void setFloat(Item item, String key, float value) {
		ensureAndGetPrimitive(item, key).setFloat(value);
	}

	@Override
	public void setDouble(Item item, String key, double value) {
		ensureAndGetPrimitive(item, key).setDouble(value);
	}

	@Override
	public void setBoolean(Item item, String key, boolean value) {
		ensureAndGetPrimitive(item, key).setBoolean(value);
	}

	/**
	 * Traverses the buffer array that holds annotation values for the specified item
	 * and returns {@code true} if that buffer holds at least {@code 1} non null entry.
	 * Note that this check does not compare entries to their respective {@code noEntryValue}s
	 * to only consider <i>real</i> annotation values.
	 *
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractManagedAnnotationStorage#hasAnnotations(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean hasAnnotations(Item item) {
		Object[] buffer = getBuffer(item);

		if(buffer==null || buffer==getNoEntryValues()) {
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
