/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 419 $
 * $Date: 2015-07-23 22:36:36 +0200 (Do, 23 Jul 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/layers/annotation/fixed/FixedKeysIntStorage.java $
 *
 * $LastChangedDate: 2015-07-23 22:36:36 +0200 (Do, 23 Jul 2015) $
 * $LastChangedRevision: 419 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.layers.annotation.fixed;

import java.util.function.Consumer;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.standard.util.CorpusUtils;

/**
 * @author Markus Gärtner
 * @version $Id: FixedKeysIntStorage.java 419 2015-07-23 20:36:36Z mcgaerty $
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
			AnnotationManifest annotationManifest = layerManifest.getAnnotationManifest(key);

			Object noEntryValue = annotationManifest.getNoEntryValue();
			if(noEntryValue==null) {
				continue;
			}

			noEntryValues[i] = (int) noEntryValue;
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
		int index = checkKeyAndGetIndex(key);
		int[] buffer = getBuffer(item);

		if(buffer==null) {
			buffer = getNoEntryValues();
		}

		return buffer[index];
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
		int index = checkKeyAndGetIndex(key);
		int[] buffer = getBuffer(item, true);

		buffer[index] = value;
	}

	@Override
	public void setLongValue(Item item, String key, long value) {
		setIntegerValue(item, key, CorpusUtils.ensureIntegerValueRange(value));
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layers.annotation.AbstractObjectMapStorage#createBuffer()
	 */
	@Override
	protected int[] createBuffer() {
		return new int[getKeyCount()];
	}

}
