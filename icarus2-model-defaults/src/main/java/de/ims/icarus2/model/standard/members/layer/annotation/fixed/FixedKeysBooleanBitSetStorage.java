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

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;

import java.util.BitSet;
import java.util.function.Consumer;

import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.mem.ByteAllocator;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(AnnotationStorage.class)
public class FixedKeysBooleanBitSetStorage extends AbstractFixedKeysStorage<BitSet> {

	public FixedKeysBooleanBitSetStorage() {
		this(UNSET_INT);
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
			AnnotationManifest annotationManifest = requireAnnotationsManifest(layerManifest, key);

			int index = i;
			annotationManifest.getNoEntryValue().ifPresent(noEntryValue ->
					noEntryValues.set(index, ((Boolean) noEntryValue).booleanValue()));
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

		boolean result = false;

		for(int i=0; i<indexLookup.keyCount(); i++) {
			if(buffer.get(i) != noEntryValues.get(i)) {
				result = true;
				action.accept(indexLookup.keyAt(i));
			}
		}

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getValue(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int)
	 */
	@Override
	public Object getValue(Item item, String key) {
		return Boolean.valueOf(getBoolean(item, key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int, java.lang.Object)
	 */
	@Unguarded(Unguarded.DELEGATE)
	@Override
	public void setValue(Item item, String key, Object value) {
		setBoolean(item, key, ((Boolean) value).booleanValue());
	}

	@Override
	public boolean getBoolean(Item item, String key) {
		int index = checkKeyAndGetIndex(key);
		BitSet buffer = getBuffer(item);

		if(buffer==null) {
			buffer = getNoEntryValues();
		}

		return buffer.get(index);
	}

	@Override
	public void setBoolean(Item item, String key, boolean value) {
		int index = checkKeyAndGetIndex(key);
		BitSet buffer = getBuffer(item, true);

		buffer.set(index, value);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractObjectMapStorage#createBuffer()
	 */
	@Override
	protected BitSet createBuffer() {
		return new BitSet(getKeyCount());
	}

}
