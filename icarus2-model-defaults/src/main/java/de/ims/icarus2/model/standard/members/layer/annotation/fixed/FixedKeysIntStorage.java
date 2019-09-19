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

import static de.ims.icarus2.util.lang.Primitives._int;

import java.util.function.Consumer;

import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.mem.ByteAllocator;

/**
 * @author Markus Gärtner
 *
 */
public class FixedKeysIntStorage extends AbstractFixedKeysStorage<int[]> {

	public FixedKeysIntStorage() {
		this(-1);
	}

	public FixedKeysIntStorage(int initialCapacity) {
		this(false, initialCapacity);
	}

	public FixedKeysIntStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	@Override
	protected int[] createNoEntryValues(AnnotationLayer layer,
			IndexLookup indexLookup) {

		AnnotationLayerManifest layerManifest = layer.getManifest();

		int[] noEntryValues = new int[indexLookup.keyCount()];
		for(int i=0; i<indexLookup.keyCount(); i++) {
			String key = indexLookup.keyAt(i);
			AnnotationManifest annotationManifest = requireAnnotationsManifest(layerManifest, key);


			noEntryValues[i] = annotationManifest.getNoEntryValue()
					.map(Integer.class::cast)
					.orElse(_int(IcarusUtils.UNSET_INT))
					.intValue();
		}

		return noEntryValues;
	}

	@Override
	public boolean collectKeys(Item item, Consumer<String> action) {
		int[] buffer = getBuffer(item);
		int[] noEntryValues = getNoEntryValues();

		if(buffer==null) {
			return false;
		}

		IndexLookup indexLookup = getIndexLookup();

		boolean keysReported = false;

		for(int i=0; i<indexLookup.keyCount(); i++) {
			if(buffer[i]!=noEntryValues[i]) {
				action.accept(indexLookup.keyAt(i));
				keysReported = true;
			}
		}

		return keysReported;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getValue(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int)
	 */
	@Override
	public Object getValue(Item item, String key) {
		return Integer.valueOf(getInteger(item, key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int, java.lang.Object)
	 */
	@Unguarded(Unguarded.DELEGATE)
	@Override
	public void setValue(Item item, String key, Object value) {
		setInteger(item, key, ((Number) value).intValue());
	}

	@Override
	public int getInteger(Item item, String key) {
		int index = checkKeyAndGetIndex(key);
		int[] buffer = getBuffer(item);

		if(buffer==null) {
			buffer = getNoEntryValues();
		}

		return buffer[index];
	}

	@Override
	public float getFloat(Item item, String key) {
		return getInteger(item, key);
	}

	@Override
	public double getDouble(Item item, String key) {
		return getInteger(item, key);
	}

	@Override
	public long getLong(Item item, String key) {
		return getInteger(item, key);
	}

	@Override
	public void setInteger(Item item, String key, int value) {
		int index = checkKeyAndGetIndex(key);
		int[] buffer = getBuffer(item, true);

		buffer[index] = value;
	}

	@Override
	public void setLong(Item item, String key, long value) {
		setInteger(item, key, IcarusUtils.ensureIntegerValueRange(value));
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractObjectMapStorage#createBuffer()
	 */
	@Override
	protected int[] createBuffer() {
		return new int[getKeyCount()];
	}

}
