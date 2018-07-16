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

import static de.ims.icarus2.util.lang.Primitives._double;

import java.util.function.Consumer;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public class FixedKeysDoubleStorage extends AbstractFixedKeysStorage<double[]> {

	public FixedKeysDoubleStorage() {
		this(-1);
	}

	public FixedKeysDoubleStorage(int initialCapacity) {
		this(false, initialCapacity);
	}

	public FixedKeysDoubleStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	@Override
	protected double[] createNoEntryValues(AnnotationLayer layer,
			IndexLookup indexLookup) {

		AnnotationLayerManifest layerManifest = layer.getManifest();

		double[] noEntryValues = new double[indexLookup.keyCount()];
		for(int i=0; i<indexLookup.keyCount(); i++) {
			String key = indexLookup.keyAt(i);
			AnnotationManifest annotationManifest = layerManifest.getAnnotationManifest(key);

			Object noEntryValue = annotationManifest.getNoEntryValue();
			if(noEntryValue==null) {
				noEntryValue = _double(IcarusUtils.UNSET_DOUBLE);
			}

			noEntryValues[i] = ((Number) noEntryValue).doubleValue();
		}

		return noEntryValues;
	}

	@Override
	public boolean collectKeys(Item item, Consumer<String> action) {
		double[] buffer = getBuffer(item);
		double[] noEntryValues = getNoEntryValues();

		if(buffer==null) {
			return false;
		}

		IndexLookup indexLookup = getIndexLookup();

		boolean keysReported = false;

		for(int i=0; i<indexLookup.keyCount(); i++) {
			if(Double.compare(buffer[i], noEntryValues[i])!=0) {
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
		return Double.valueOf(getDoubleValue(item, key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, Object value) {
		setDoubleValue(item, key, ((Number) value).doubleValue());
	}

	@Override
	public float getFloatValue(Item item, String key) {
		return (float) getDoubleValue(item, key);
	}

	@Override
	public int getIntegerValue(Item item, String key) {
		return (int) getDoubleValue(item, key);
	}

	@Override
	public long getLongValue(Item item, String key) {
		return (long) getDoubleValue(item, key);
	}

	@Override
	public double getDoubleValue(Item item, String key) {
		int index = checkKeyAndGetIndex(key);
		double[] buffer = getBuffer(item);

		if(buffer==null) {
			buffer = getNoEntryValues();
		}

		return buffer[index];
	}

	@Override
	public void setDoubleValue(Item item, String key, double value) {
		int index = checkKeyAndGetIndex(key);
		double[] buffer = getBuffer(item, true);

		buffer[index] = value;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layers.annotation.AbstractObjectMapStorage#createBuffer()
	 */
	@Override
	protected double[] createBuffer() {
		return new double[getKeyCount()];
	}

}
