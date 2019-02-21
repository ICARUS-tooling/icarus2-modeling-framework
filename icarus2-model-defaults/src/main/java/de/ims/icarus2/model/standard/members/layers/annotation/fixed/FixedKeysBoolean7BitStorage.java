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
package de.ims.icarus2.model.standard.members.layers.annotation.fixed;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.util.ModelUtils;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class FixedKeysBoolean7BitStorage extends AbstractFixedKeysBooleanStorage {

	private static final Logger log = LoggerFactory
			.getLogger(FixedKeysBoolean7BitStorage.class);

	public static final int MAX_KEY_COUNT = 7;

	private Object2ByteMap<Item> annotations;
	private byte noEntryValues;

	private static final byte EMPTY_BUFFER = (byte) (0x1<<7);

	public FixedKeysBoolean7BitStorage() {
		this(-1);
	}

	public FixedKeysBoolean7BitStorage(int initialCapacity) {
		this(false, initialCapacity);
	}

	public FixedKeysBoolean7BitStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	protected Object2ByteMap<Item> createMap(AnnotationLayer layer) {
		if(isWeakKeys()) {
			log.warn("Storage implementation does not support weak key references to stored items.");
		}

		Object2ByteMap<Item> result = new Object2ByteOpenHashMap<>(getInitialCapacity(layer));
		result.defaultReturnValue(EMPTY_BUFFER);

		return result;
	}

	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		IndexLookup indexLookup = getIndexLookup();
		if(indexLookup.keyCount()>MAX_KEY_COUNT)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Byte buffer only holds 7 value bits - cannot represent annotations for layer: "+ModelUtils.getName(layer));

		AnnotationLayerManifest manifest = layer.getManifest();

		int noEntryValues = 0;

		for(int i=0; i<indexLookup.keyCount(); i++) {
			AnnotationManifest annotationManifest = requireAnnotationsManifest(manifest, indexLookup.keyAt(i));

			Optional<Object> declaredNoEntryValue = annotationManifest.getNoEntryValue();

			if(!declaredNoEntryValue.isPresent() && ((Boolean)declaredNoEntryValue.get()).booleanValue()) {
				noEntryValues |= (1<<i);
			}
		}

		this.noEntryValues = (byte)noEntryValues;

		annotations = createMap(layer);
	}

	@Override
	public void removeNotify(AnnotationLayer layer) {
		super.removeNotify(layer);

		noEntryValues = 0x0;
		annotations = null;
	}

	@Override
	public boolean collectKeys(Item item, Consumer<String> action) {
		byte data = annotations.getByte(item);

		if(data==EMPTY_BUFFER || data==noEntryValues) {
			return false;
		}

		IndexLookup indexLookup = getIndexLookup();

		for(int i=0; i<indexLookup.keyCount(); i++) {
			int mask = (1<<i);
			if((data & mask) != (noEntryValues & mask)) {
				action.accept(indexLookup.keyAt(i));
			}
		}

		return true;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layers.annotation.fixed.AbstractFixedKeysBooleanStorage#getNoEntryValue(java.lang.String)
	 */
	@Override
	protected boolean getNoEntryValue(String key) {
		int index = checkKeyAndGetIndex(key);
		return (noEntryValues & (1<<index))!=0x0;
	}

	@Override
	public boolean getBooleanValue(Item item, String key) {
		int index = checkKeyAndGetIndex(key);
		byte b = annotations.getByte(item);

		if(b==EMPTY_BUFFER) {
			b = noEntryValues;
		}

		return (b & (1<<index))!=0x0;
	}

	@Override
	public void setBooleanValue(Item item, String key, boolean value) {
		int index = checkKeyAndGetIndex(key);
		byte b = annotations.getByte(item);

		if(value) {
			b |= (1<<index);
		} else {
			b &= (EMPTY_BUFFER | ~(1<<index));
		}

		annotations.put(item, b);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#removeAllValues(java.util.function.Supplier)
	 */
	@Override
	public void removeAllValues(Supplier<? extends Item> source) {
		Item item;
		while((item=source.get())!=null) {
			annotations.removeByte(item);
		}
	}

	@Override
	public boolean hasAnnotations() {
		return !annotations.isEmpty();
	}

	@Override
	public boolean hasAnnotations(Item item) {
		return annotations.getByte(item)!=EMPTY_BUFFER;
	}

	@Override
	public boolean containsItem(Item item) {
		return annotations.containsKey(item);
	}

	@Override
	public boolean addItem(Item item) {
		byte b = annotations.getByte(item);

		if(b!=EMPTY_BUFFER) {
			annotations.put(item, EMPTY_BUFFER);
			return true;
		}

		return false;
	}

	@Override
	public boolean removeItem(Item item) {
		return annotations.removeByte(item)!=EMPTY_BUFFER;
	}

}
