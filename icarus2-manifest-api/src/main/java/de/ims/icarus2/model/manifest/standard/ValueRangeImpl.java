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
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.types.UnsupportedValueTypeException;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.lang.ClassUtils;

public class ValueRangeImpl extends AbstractLockable implements ValueRange {

	private final ValueType valueType;
	private Optional<Comparable<?>> lower = Optional.empty();
	private Optional<Comparable<?>> upper = Optional.empty();
	private Optional<Comparable<?>> stepSize = Optional.empty();
	private boolean lowerIncluded = DEFAULT_LOWER_INCLUSIVE_VALUE;
	private boolean upperIncluded = DEFAULT_UPPER_INCLUSIVE_VALUE;


	public ValueRangeImpl(ValueType valueType) {
		requireNonNull(valueType);

		if(!SUPPORTED_VALUE_TYPES.contains(valueType))
			throw new UnsupportedValueTypeException(valueType);

		this.valueType = valueType;
	}

	public ValueRangeImpl(ValueType valueType, boolean lowerIncluded, boolean upperIncluded) {
		this(valueType);

		this.lowerIncluded = lowerIncluded;
		this.upperIncluded = upperIncluded;
	}

	public ValueRangeImpl(ValueType valueType, Comparable<?> lower, Comparable<?> upper, boolean lowerIncluded, boolean upperIncluded) {
		this(valueType, lowerIncluded, upperIncluded);

		requireNonNull(lower);
		requireNonNull(upper);

		setLowerBound(lower);
		setUpperBound(upper);
	}

	public ValueRangeImpl(ValueType valueType, Comparable<?> lower, Comparable<?> upper) {
		this(valueType, lower, upper, true, true);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(valueType, lower, upper, stepSize);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof ValueRange) {
			ValueRange other = (ValueRange)obj;

			return valueType.equals(other.getValueType())
					&& lowerIncluded==other.isLowerBoundInclusive()
					&& upperIncluded==other.isUpperBoundInclusive()
					&& ClassUtils.equals(lower, other.getLowerBound())
					&& ClassUtils.equals(upper, other.getUpperBound());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ValueRange@").append(valueType.getStringValue()); //$NON-NLS-1$

		if(lowerIncluded) {
			sb.append('[');
		} else {
			sb.append('(');
		}

		if(lower==null && upper==null) {
			sb.append('-');
		} else {
			sb.append(lower).append(',').append(upper);
		}

		if(upperIncluded) {
			sb.append('[');
		} else {
			sb.append(')');
		}


		return sb.toString();
	}

	/**
	 * @return the valueType
	 */
	@Override
	public ValueType getValueType() {
		return valueType;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ValueRange#getLowerBound()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <V extends Comparable<?>> Optional<V> getLowerBound() {
		return (Optional<V>) lower;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ValueRange#getUpperBound()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <V extends Comparable<?>> Optional<V> getUpperBound() {
		return (Optional<V>) upper;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ValueRange#getStepSize()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <V extends Comparable<?>> Optional<V> getStepSize() {
		return (Optional<V>) stepSize;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ValueRange#isLowerBoundInclusive()
	 */
	@Override
	public boolean isLowerBoundInclusive() {
		return lowerIncluded;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ValueRange#isUpperBoundInclusive()
	 */
	@Override
	public boolean isUpperBoundInclusive() {
		return upperIncluded;
	}

	protected void checkValue(Object value) {
		valueType.checkValue(value);
	}

	/**
	 * @param lower the lower to set
	 */
	@Override
	public ValueRange setLowerBound(Comparable<?> lower) {
		checkNotLocked();

		setLowerBound0(lower);

		return this;
	}

	protected void setLowerBound0(Comparable<?> lower) {
		requireNonNull(lower);

		checkValue(lower);

		this.lower = Optional.of(lower);
	}

	/**
	 * @param upper the upper to set
	 */
	@Override
	public ValueRange setUpperBound(Comparable<?> upper)  {
		checkNotLocked();

		setUpperBound0(upper);

		return this;
	}

	protected void setUpperBound0(Comparable<?> upper) {
		requireNonNull(upper);

		checkValue(upper);

		this.upper = Optional.of(upper);
	}

	/**
	 * @param upper the upper to set
	 */
	@Override
	public ValueRange setStepSize(Comparable<?> stepSize) {
		checkNotLocked();

		setStepSize0(stepSize);

		return this;
	}

	protected void setStepSize0(Comparable<?> stepSize) {
		requireNonNull(stepSize);

		checkValue(stepSize);

		this.stepSize = Optional.of(stepSize);
	}

	/**
	 * @param lowerIncluded the lowerIncluded to set
	 */
	@Override
	public ValueRange setLowerBoundInclusive(boolean lowerIncluded) {
		checkNotLocked();

		setLowerBoundIncluded0(lowerIncluded);

		return this;
	}

	protected void setLowerBoundIncluded0(boolean lowerIncluded) {
		this.lowerIncluded = lowerIncluded;
	}

	/**
	 * @param upperIncluded the upperIncluded to set
	 */
	@Override
	public ValueRange setUpperBoundInclusive(boolean upperIncluded) {
		checkNotLocked();

		setUpperBoundIncluded0(upperIncluded);

		return this;
	}

	protected void setUpperBoundIncluded0(boolean upperIncluded) {
		this.upperIncluded = upperIncluded;
	}

}