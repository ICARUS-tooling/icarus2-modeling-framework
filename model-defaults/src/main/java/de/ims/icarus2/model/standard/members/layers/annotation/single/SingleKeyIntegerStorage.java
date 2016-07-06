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

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public class SingleKeyIntegerStorage extends AbstractSingleKeyStorage {

	private final static Logger log = LoggerFactory.getLogger(SingleKeyIntegerStorage.class);

	private TObjectIntMap<Item> annotations;
	private int noEntryValue = DEFAULT_NO_ENTRY_VALUE;

	public static final int DEFAULT_NO_ENTRY_VALUE = -1;

	public SingleKeyIntegerStorage() {
		this(-1);
	}

	public SingleKeyIntegerStorage(int initialCapacity) {
		this(false, initialCapacity);
	}

	public SingleKeyIntegerStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	protected TObjectIntMap<Item> buildBuffer(AnnotationLayer layer) {
		if(isWeakKeys()) {
			log.warn("Storage implementation does not support weak key references to stored items in layer {}", ModelUtils.getUniqueId(layer));
		}

		return new TObjectIntHashMap<>(getInitialCapacity(layer), 0.75F, getNoEntryValue());
	}

	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		AnnotationLayerManifest manifest = layer.getManifest();
		AnnotationManifest annotationManifest = manifest.getAnnotationManifest(manifest.getDefaultKey());

		Object declaredNoEntryValue = annotationManifest.getNoEntryValue();

		noEntryValue = declaredNoEntryValue==null ? DEFAULT_NO_ENTRY_VALUE : (int) declaredNoEntryValue;
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
		return getIntegerValue(item, key);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, Object value) {
		setIntegerValue(item, key, (int) value);
	}

	@Override
	public int getIntegerValue(Item item, String key) {
		checkKey(key);

		return annotations.get(item);
	}

	@Override
	public float getFloatValue(Item item, String key) {
		return getIntegerValue(item, key);
	}

	@Override
	public double getDoubleValue(Item item, String key) {
		return getIntegerValue(item, key);
	}

	@Override
	public long getLongValue(Item item, String key) {
		return getIntegerValue(item, key);
	}

	@Override
	public void setIntegerValue(Item item, String key, int value) {
		checkKey(key);

		if(value==noEntryValue) {
			annotations.remove(item);
		} else {
			annotations.put(item, value);
		}
	}

	@Override
	public void setLongValue(Item item, String key, long value) {
		setIntegerValue(item, key, IcarusUtils.ensureIntegerValueRange(value));
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
		return annotations.remove(item)!=noEntryValue;
	}

	public int getNoEntryValue() {
		return noEntryValue;
	}
}
