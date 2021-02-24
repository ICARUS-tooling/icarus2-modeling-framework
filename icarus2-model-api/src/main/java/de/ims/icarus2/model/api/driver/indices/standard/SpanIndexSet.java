/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.model.api.driver.indices.IndexUtils.checkIndex;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.util.lang.ClassUtils;

/**
 * @author Markus Gärtner
 *
 */
public class SpanIndexSet implements IndexSet {

	private final long minValue, maxValue;
	private final IndexValueType valueType;

	public SpanIndexSet(long minValue, long maxValue) {
		if(minValue<0)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Min value is negative: "+minValue); //$NON-NLS-1$
		if(maxValue<0)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Max value is negative: "+maxValue); //$NON-NLS-1$
		if(minValue>maxValue)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Min value exceeds max value: "+maxValue); //$NON-NLS-1$

		this.minValue = minValue;
		this.maxValue = maxValue;

		valueType = ClassUtils.max(
				IndexValueType.forValue(minValue),
				IndexValueType.forValue(maxValue));
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
		checkIndex(this, index);
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
		}

		return new SpanIndexSet(minValue+fromIndex, minValue+toIndex);
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

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "["+valueType+":"+minValue+"-"+maxValue+"]";
	}
}
