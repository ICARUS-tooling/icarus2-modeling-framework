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
package de.ims.icarus2.model.api.driver.indices.standard;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.util.classes.ClassUtils;

/**
 * @author Markus Gärtner
 *
 */
public class SpanIndexSet implements IndexSet {

	private final long minValue, maxValue;
	private final IndexValueType valueType;

	public SpanIndexSet(long minValue, long maxValue) {
		if(minValue<0)
			throw new IllegalArgumentException("Min value is negative: "+minValue); //$NON-NLS-1$
		if(maxValue<0)
			throw new IllegalArgumentException("Max value is negative: "+maxValue); //$NON-NLS-1$
		if(minValue>maxValue)
			throw new IllegalArgumentException("Min value exceeds max value: "+maxValue); //$NON-NLS-1$

		this.minValue = minValue;
		this.maxValue = maxValue;

		valueType = ClassUtils.max(IndexValueType.forValue(minValue), IndexValueType.forValue(maxValue));
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#getSize()
	 */
	@Override
	public int size() {
		return (int) (maxValue-minValue+1);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#indexAt(int)
	 */
	@Override
	public long indexAt(int index) {
		return minValue+index;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#firstIndex()
	 */
	@Override
	public long firstIndex() {
		return minValue;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#lastIndex()
	 */
	@Override
	public long lastIndex() {
		return maxValue;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#externalize()
	 */
	@Override
	public IndexSet externalize() {
		return this;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#subSet(int, int)
	 */
	@Override
	public IndexSet subSet(int fromIndex, int toIndex) {
		//TODO sanity check for boundary violations

		if(fromIndex==0 && toIndex==size()-1) {
			return this;
		} else {
			return new SpanIndexSet(minValue+fromIndex, minValue+toIndex);
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#getIndexValueType()
	 */
	@Override
	public IndexValueType getIndexValueType() {
		return valueType;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#isSorted()
	 */
	@Override
	public boolean isSorted() {
		return true;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#sort()
	 */
	@Override
	public boolean sort() {
		return true;
	}
}
