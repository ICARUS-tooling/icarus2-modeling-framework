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
package de.ims.icarus2.model.standard.members.layers.annotation.fixed;

import java.util.BitSet;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;

/**
 * @author Markus Gärtner
 *
 */
public class FixedKeysBooleanBitSetStorage extends AbstractFixedKeysStorage<BitSet> {

	public FixedKeysBooleanBitSetStorage() {
		this(-1);
	}

	public FixedKeysBooleanBitSetStorage(int initialCapacity) {
		this(false, initialCapacity);
	}

	public FixedKeysBooleanBitSetStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	@Override
	protected BitSet createNoEntryValues(AnnotationLayer layer,
			IndexLookup indexLookup) {

		AnnotationLayerManifest layerManifest = layer.getManifest();

		BitSet noEntryValues = new BitSet(indexLookup.keyCount());
		for(int i=0; i<indexLookup.keyCount(); i++) {
			String key = indexLookup.keyAt(i);
			AnnotationManifest annotationManifest = layerManifest.getAnnotationManifest(key);

			Object noEntryValue = annotationManifest.getNoEntryValue();
			if(noEntryValue==null) {
				continue;
			}

			noEntryValues.set(i, (boolean) noEntryValue);
		}

		return noEntryValues;
	}

	@Override
	public boolean collectKeys(Item item, Consumer<String> action) {
		BitSet buffer = getBuffer(item);
		BitSet noEntryValues = getNoEntryValues();

		if(buffer==null || noEntryValues.equals(buffer)) {
			return false;
		}

		IndexLookup indexLookup = getIndexLookup();

		for(int i=0; i<indexLookup.keyCount(); i++) {
			if(buffer.get(i)) {
				action.accept(indexLookup.keyAt(i));
			}
		}

		return true;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public Object getValue(Item item, String key) {
		return getBooleanValue(item, key);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, Object value) {
		setBooleanValue(item, key, (boolean) value);
	}

	@Override
	public boolean getBooleanValue(Item item, String key) {
		int index = checkKeyAndGetIndex(key);
		BitSet buffer = getBuffer(item);

		if(buffer==null) {
			buffer = getNoEntryValues();
		}

		return buffer.get(index);
	}

	@Override
	public void setBooleanValue(Item item, String key, boolean value) {
		int index = checkKeyAndGetIndex(key);
		BitSet buffer = getBuffer(item, true);

		buffer.set(index, value);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layers.annotation.AbstractObjectMapStorage#createBuffer()
	 */
	@Override
	protected BitSet createBuffer() {
		return new BitSet(getKeyCount());
	}

}
