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
package de.ims.icarus2.model.standard.members.layers.annotation.single;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.annotations.TestableImplementation;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(AnnotationStorage.class)
public class SingleKeyFloatStorage extends AbstractSingleKeyStorage {

	private final static Logger log = LoggerFactory.getLogger(SingleKeyFloatStorage.class);

	private Object2FloatMap<Item> annotations;
	private float noEntryValue = DEFAULT_NO_ENTRY_VALUE;

	public static final float DEFAULT_NO_ENTRY_VALUE = IcarusUtils.UNSET_FLOAT;

	public SingleKeyFloatStorage() {
		this(-1);
	}

	public SingleKeyFloatStorage(int initialCapacity) {
		this(false, initialCapacity);
	}

	public SingleKeyFloatStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	protected Object2FloatMap<Item> buildBuffer(AnnotationLayer layer) {
		if(isWeakKeys()) {
			log.warn("Storage implementation does not support weak key references to stored items in layer {}", ModelUtils.getUniqueId(layer));
		}

		Object2FloatMap<Item> result =  new Object2FloatOpenHashMap<>(getInitialCapacity(layer));
		result.defaultReturnValue(getNoEntryValue());

		return result;
	}

	@SuppressWarnings("boxing")
	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		AnnotationLayerManifest manifest = layer.getManifest();
		String key = requireDefaultKey(manifest);
		AnnotationManifest annotationManifest = requireAnnotationsManifest(manifest, key);

		noEntryValue = annotationManifest.getNoEntryValue()
				.map(Float.class::cast)
				.orElse(DEFAULT_NO_ENTRY_VALUE);
		annotations = buildBuffer(layer);
	}

	@Override
	public void removeNotify(AnnotationLayer layer) {
		super.removeNotify(layer);

		noEntryValue = DEFAULT_NO_ENTRY_VALUE;
		annotations = null;
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
		checkKey(key);

		return annotations.getFloat(item);
	}

	@Override
	public double getDouble(Item item, String key) {
		return getFloat(item, key);
	}

	@Override
	public int getInteger(Item item, String key) {
		return (int) getFloat(item, key);
	}

	@Override
	public long getLong(Item item, String key) {
		return (long) getFloat(item, key);
	}

	@Override
	public void setFloat(Item item, String key, float value) {
		checkKey(key);

		if(Float.compare(value, noEntryValue)==0) {
			annotations.removeFloat(item);
		} else {
			annotations.put(item, value);
		}
	}

	@Override
	public void setDouble(Item item, String key, double value) {
		if(value > Float.MAX_VALUE || value < -Float.MAX_VALUE)
			throw new ModelException(GlobalErrorCode.VALUE_OVERFLOW,
					"Double value exceeds float limits: "+value);

		setFloat(item, key, (float) value);
	}

	@Override
	public void setInteger(Item item, String key, int value) {
		setFloat(item, key, value);
	}

	@Override
	public void setLong(Item item, String key, long value) {
		setFloat(item, key, value);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#removeAllValues(java.util.function.Supplier)
	 */
	@Override
	public void removeAllValues(Supplier<? extends Item> source) {
		Item item;
		while((item=source.get())!=null) {
			annotations.removeFloat(item);
		}
	}

	@Override
	public boolean hasAnnotations() {
		return !annotations.isEmpty();
	}

	@Override
	public boolean hasAnnotations(Item item) {
		return annotations.containsKey(item);
	}

	@Override
	public boolean removeItem(Item item) {
		if(annotations.containsKey(item)) {
			annotations.removeFloat(item);
			return true;
		}

		return false;
	}

	@Override
	public boolean addItem(Item item) {
		if(!annotations.containsKey(item)) {
			annotations.put(item, noEntryValue);
			return true;
		}

		return false;
	}

	public float getNoEntryValue() {
		return noEntryValue;
	}
}
