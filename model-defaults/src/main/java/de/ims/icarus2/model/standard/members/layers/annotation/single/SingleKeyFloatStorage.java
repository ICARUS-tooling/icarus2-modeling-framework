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
package de.ims.icarus2.model.standard.members.layers.annotation.single;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.util.ModelUtils;

/**
 * @author Markus Gärtner
 *
 */
public class SingleKeyFloatStorage extends AbstractSingleKeyStorage {

	private final static Logger log = LoggerFactory.getLogger(SingleKeyFloatStorage.class);

	private Object2FloatMap<Item> annotations;
	private float noEntryValue = DEFAULT_NO_ENTRY_VALUE;

	public static final float DEFAULT_NO_ENTRY_VALUE = -1F;

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

	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		AnnotationLayerManifest manifest = layer.getManifest();
		AnnotationManifest annotationManifest = manifest.getAnnotationManifest(manifest.getDefaultKey());

		Object declaredNoEntryValue = annotationManifest.getNoEntryValue();

		noEntryValue = declaredNoEntryValue==null ? DEFAULT_NO_ENTRY_VALUE : ((Float) declaredNoEntryValue).floatValue();
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
		return Float.valueOf(getFloatValue(item, key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, Object value) {
		setFloatValue(item, key, ((Number) value).floatValue());
	}

	@Override
	public float getFloatValue(Item item, String key) {
		checkKey(key);

		return annotations.getFloat(item);
	}

	@Override
	public double getDoubleValue(Item item, String key) {
		return getFloatValue(item, key);
	}

	@Override
	public void setFloatValue(Item item, String key, float value) {
		checkKey(key);

		if(Float.compare(value, noEntryValue)==0) {
			annotations.remove(item);
		} else {
			annotations.put(item, value);
		}
	}

	@Override
	public void removeAllValues() {
		annotations.clear();
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
		return Float.compare(annotations.removeFloat(item), noEntryValue)!=0;
	}

	public float getNoEntryValue() {
		return noEntryValue;
	}
}
