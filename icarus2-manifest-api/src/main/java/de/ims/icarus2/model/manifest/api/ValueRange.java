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
package de.ims.icarus2.model.manifest.api;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.eval.Expression;


/**
 * Implements a bounded range with an lower and upper bound,
 * both of which can be defined as inclusive or exclusive.
 * <p>
 *
 *
 * @author Markus Gärtner
 *
 */
public interface ValueRange extends Lockable, TypedManifest {

	public static final Set<ValueType> SUPPORTED_VALUE_TYPES =
			Collections.unmodifiableSet(ValueType.filterIncluding(
					type -> !Boolean.class.equals(type.getBaseClass())
						&& (Comparable.class.isAssignableFrom(type.getBaseClass())
								|| type.getComparator()!=null)));

	public static final boolean DEFAULT_LOWER_INCLUSIVE_VALUE = true;
	public static final boolean DEFAULT_UPPER_INCLUSIVE_VALUE = true;

	/**
	 * Returns the lower bound of this range. The returned value must either
	 * be an object matching the {@link #getValueType() value-type} specified for
	 * this range or an {@link Expression} that returns a compatible value.
	 *
	 * @return
	 */
	<V> Optional<V> getLowerBound();

	/**
	 * Returns the {@link #getLowerBound() lower bound} as a {@link Comparable}
	 * object and throws an exception if it is set to an {@link Expression}.
	 * @return
	 * @throws ManifestException of type {@link ManifestErrorCode#MANIFEST_TYPE_CAST}
	 * when the value is set to an {@link Expression}
	 */
	@SuppressWarnings("unchecked")
	default <V extends Comparable<?>> Optional<V> getLowerBoundComparable() {
		Optional<?> lower = getLowerBound();
		if(lower.isPresent()) {
			Object value = lower.get();
			if(value instanceof Expression)
				throw new ManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
						"Lower bound is of type "+Expression.class.getName());
			return Optional.of((V) value);
		}
		return Optional.empty();
	}

	/**
	 * Returns the upper bound of this range. The returned value must either
	 * be an object matching the {@link #getValueType() value-type} specified for
	 * this range or an {@link Expression} that returns a compatible value.
	 *
	 * @return
	 */
	<V> Optional<V> getUpperBound();

	/**
	 * Returns the {@link #getUpperBound() upper bound} as a {@link Comparable}
	 * object and throws an exception if it is set to an {@link Expression}.
	 * @return
	 * @throws ManifestException of type {@link ManifestErrorCode#MANIFEST_TYPE_CAST}
	 * when the value is set to an {@link Expression}
	 */
	@SuppressWarnings("unchecked")
	default <V extends Comparable<?>> Optional<V> getUpperBoundComparable() {
		Optional<?> upper = getUpperBound();
		if(upper.isPresent()) {
			Object value = upper.get();
			if(value instanceof Expression)
				throw new ManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
						"Upper bound is of type "+Expression.class.getName());
			return Optional.of((V) value);
		}
		return Optional.empty();
	}

	/**
	 * Returns the interval size used when stepping through this range.
	 *
	 * @return
	 */
	<V> Optional<V> getStepSize();

	/**
	 * Returns the {@link #getStepSize() step size} as a {@link Comparable}
	 * object and throws an exception if it is set to an {@link Expression}.
	 * @return
	 * @throws ManifestException of type {@link ManifestErrorCode#MANIFEST_TYPE_CAST}
	 * when the value is set to an {@link Expression}
	 */
	@SuppressWarnings("unchecked")
	default <V extends Comparable<?>> Optional<V> getStepSizeComparable() {
		Optional<?> stepSize = getStepSize();
		if(stepSize.isPresent()) {
			Object value = stepSize.get();
			if(value instanceof Expression)
				throw new ManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
						"Step size is of type "+Expression.class.getName());
			return Optional.of((V) value);
		}
		return Optional.empty();
	}

	boolean isLowerBoundInclusive();

	boolean isUpperBoundInclusive();

	ValueType getValueType();

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifest#getManifestType()
	 */
	@Override
	default public ManifestType getManifestType() {
		return ManifestType.VALUE_RANGE;
	}

	// Modification method

	ValueRange setLowerBound(Object lowerBound);

	ValueRange setUpperBound(Object upperBound);

	ValueRange setStepSize(Object stepSize);

	ValueRange setLowerBoundInclusive(boolean lowerBoundInclusive);

	ValueRange setUpperBoundInclusive(boolean upperBoundInclusive);
}
