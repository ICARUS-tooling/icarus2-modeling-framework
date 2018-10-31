/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.IcarusUtils;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class SingleKeyLongStorage extends AbstractSingleKeyStorage {

	private final static Logger log = LoggerFactory.getLogger(SingleKeyLongStorage.class);

	private Object2LongMap<Item> annotations;
	private long noEntryValue = DEFAULT_NO_ENTRY_VALUE;

	public static final long DEFAULT_NO_ENTRY_VALUE = IcarusUtils.UNSET_LONG;

	public SingleKeyLongStorage() {
		this(-1);
	}

	public SingleKeyLongStorage(int initialCapacity) {
		this(false, initialCapacity);
	}

	public SingleKeyLongStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	protected Object2LongMap<Item> buildBuffer(AnnotationLayer layer) {
		if(isWeakKeys()) {
			log.warn("Storage implementation does not support weak key references to stored items in layer {}", ModelUtils.getUniqueId(layer));
		}

		Object2LongMap<Item> result = new Object2LongOpenHashMap<>(getInitialCapacity(layer));
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
				.map(Long.class::cast)
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
		return Long.valueOf(getLongValue(item, key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, Object value) {
		setLongValue(item, key, ((Number) value).longValue());
	}

	@Override
	public int getIntegerValue(Item item, String key) {
		return (int) getLongValue(item, key);
	}

	@Override
	public float getFloatValue(Item item, String key) {
		return getLongValue(item, key);
	}

	@Override
	public double getDoubleValue(Item item, String key) {
		return getLongValue(item, key);
	}

	@Override
	public long getLongValue(Item item, String key) {
		checkKey(key);

		return annotations.getLong(item);
	}

	@Override
	public void setIntegerValue(Item item, String key, int value) {
		setLongValue(item, key, value);
	}

	@Override
	public void setLongValue(Item item, String key, long value) {
		checkKey(key);

		if(value==noEntryValue) {
			annotations.removeLong(item);
		} else {
			annotations.put(item, value);
		}
	}

	@Override
	public void removeAllValues() {
		annotations.clear();
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#removeAllValues(java.util.function.Supplier)
	 */
	@Override
	public void removeAllValues(Supplier<? extends Item> source) {
		Item item;
		while((item=source.get())!=null) {
			annotations.removeLong(item);
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
		return annotations.removeLong(item)!=noEntryValue;
	}

	public long getNoEntryValue() {
		return noEntryValue;
	}
}
