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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/layers/annotation/fixed/FixedKeysFloatStorage.java $
 *
 * $LastChangedDate: 2015-07-23 22:36:36 +0200 (Do, 23 Jul 2015) $
 * $LastChangedRevision: 419 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.layers.annotation.fixed;

import java.util.function.Consumer;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus2.model.api.manifest.AnnotationManifest;
import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 * @version $Id: FixedKeysFloatStorage.java 419 2015-07-23 20:36:36Z mcgaerty $
 *
 */
public class FixedKeysFloatStorage extends AbstractFixedKeysStorage<float[]> {

	public FixedKeysFloatStorage() {
		this(-1);
	}

	public FixedKeysFloatStorage(int initialCapacity) {
		this(false, initialCapacity);
	}

	public FixedKeysFloatStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	@Override
	protected float[] createNoEntryValues(AnnotationLayer layer,
			IndexLookup indexLookup) {

		AnnotationLayerManifest layerManifest = layer.getManifest();

		float[] noEntryValues = new float[indexLookup.keyCount()];
		for(int i=0; i<indexLookup.keyCount(); i++) {
			String key = indexLookup.keyAt(i);
			AnnotationManifest annotationManifest = layerManifest.getAnnotationManifest(key);

			Object noEntryValue = annotationManifest.getNoEntryValue();
			if(noEntryValue==null) {
				continue;
			}

			noEntryValues[i] = (float) noEntryValue;
		}

		return noEntryValues;
	}

	@Override
	public boolean collectKeys(Item item, Consumer<String> action) {
		float[] buffer = getBuffer(item);
		float[] noEntryValues = getNoEntryValues();

		if(buffer==null) {
			return false;
		}

		IndexLookup indexLookup = getIndexLookup();

		boolean keysReported = false;

		for(int i=0; i<indexLookup.keyCount(); i++) {
			if(Float.compare(buffer[i], noEntryValues[i])!=0) {
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
		return getFloatValue(item, key);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, Object value) {
		setFloatValue(item, key, (float) value);
	}

	@Override
	public float getFloatValue(Item item, String key) {
		int index = checkKeyAndGetIndex(key);
		float[] buffer = getBuffer(item);

		if(buffer==null) {
			buffer = getNoEntryValues();
		}

		return buffer[index];
	}

	@Override
	public double getDoubleValue(Item item, String key) {
		return getFloatValue(item, key);
	}

	@Override
	public void setFloatValue(Item item, String key, float value) {
		int index = checkKeyAndGetIndex(key);
		float[] buffer = getBuffer(item, true);

		buffer[index] = value;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layers.annotation.AbstractObjectMapStorage#createBuffer()
	 */
	@Override
	protected float[] createBuffer() {
		return new float[getKeyCount()];
	}

}
