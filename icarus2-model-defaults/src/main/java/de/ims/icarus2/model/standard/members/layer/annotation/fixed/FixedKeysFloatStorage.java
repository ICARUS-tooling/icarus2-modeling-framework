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
package de.ims.icarus2.model.standard.members.layer.annotation.fixed;

import static de.ims.icarus2.util.lang.Primitives._float;

import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(AnnotationStorage.class)
public class FixedKeysFloatStorage extends AbstractFixedKeysStorage<float[]> {

	public FixedKeysFloatStorage() {
		this(-1);
	}

	public FixedKeysFloatStorage(int initialCapacity) {
		this(false, initialCapacity);
	}

	public FixedKeysFloatStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	@Override
	protected float[] createNoEntryValues(AnnotationLayer layer,
			IndexLookup indexLookup) {

		AnnotationLayerManifest layerManifest = layer.getManifest();

		float[] noEntryValues = new float[indexLookup.keyCount()];
		for(int i=0; i<indexLookup.keyCount(); i++) {
			String key = indexLookup.keyAt(i);
			AnnotationManifest annotationManifest = requireAnnotationsManifest(layerManifest, key);


			noEntryValues[i] = annotationManifest.getNoEntryValue()
					.map(Float.class::cast)
					.orElse(_float(IcarusUtils.UNSET_FLOAT))
					.floatValue();
		}

		return noEntryValues;
	}

	@Override
	public boolean collectKeys(Item item, Consumer<String> action) {
		float[] buffer = getBuffer(item);
		float[] noEntryValues = getNoEntryValues();

		if(buffer==null) {
			return false;
		}

		IndexLookup indexLookup = getIndexLookup();

		boolean keysReported = false;

		for(int i=0; i<indexLookup.keyCount(); i++) {
			if(Float.compare(buffer[i], noEntryValues[i])!=0) {
				action.accept(indexLookup.keyAt(i));
				keysReported = true;
			}
		}

		return keysReported;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public Object getValue(Item item, String key) {
		return Float.valueOf(getFloat(item, key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)
	 */
	@Unguarded(Unguarded.DELEGATE)
	@Override
	public void setValue(Item item, String key, Object value) {
		setFloat(item, key, ((Number) value).floatValue());
	}

	@Override
	public float getFloat(Item item, String key) {
		int index = checkKeyAndGetIndex(key);
		float[] buffer = getBuffer(item);

		if(buffer==null) {
			buffer = getNoEntryValues();
		}

		return buffer[index];
	}

	@Override
	public double getDouble(Item item, String key) {
		return getFloat(item, key);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractManagedAnnotationStorage#getInteger(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public int getInteger(Item item, String key) {
		return (int) getFloat(item, key);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractManagedAnnotationStorage#getLong(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public long getLong(Item item, String key) {
		return (long) getFloat(item, key);
	}

	@Override
	public void setFloat(Item item, String key, float value) {
		int index = checkKeyAndGetIndex(key);
		float[] buffer = getBuffer(item, true);

		buffer[index] = value;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractManagedAnnotationStorage#setDouble(de.ims.icarus2.model.api.members.item.Item, java.lang.String, double)
	 */
	@Override
	public void setDouble(Item item, String key, double value) {
		if(value > Float.MAX_VALUE || value < -Float.MAX_VALUE)
			throw new ModelException(GlobalErrorCode.VALUE_OVERFLOW,
					"Double value exceeds float limits: "+value);
		setFloat(item, key, (float) value);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractManagedAnnotationStorage#setInteger(de.ims.icarus2.model.api.members.item.Item, java.lang.String, int)
	 */
	@Override
	public void setInteger(Item item, String key, int value) {
		setFloat(item, key, value);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractManagedAnnotationStorage#setLong(de.ims.icarus2.model.api.members.item.Item, java.lang.String, long)
	 */
	@Override
	public void setLong(Item item, String key, long value) {
		setFloat(item, key, value);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractObjectMapStorage#createBuffer()
	 */
	@Override
	protected float[] createBuffer() {
		return new float[getKeyCount()];
	}

}
