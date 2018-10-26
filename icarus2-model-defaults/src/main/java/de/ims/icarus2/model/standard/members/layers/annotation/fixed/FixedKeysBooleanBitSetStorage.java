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

			noEntryValues.set(i, ((Boolean) noEntryValue).booleanValue());
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
		return Boolean.valueOf(getBooleanValue(item, key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, Object value) {
		setBooleanValue(item, key, ((Boolean) value).booleanValue());
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
