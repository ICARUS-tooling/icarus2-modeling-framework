/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
					type -> Comparable.class.isAssignableFrom(type.getBaseClass())));

	public static final boolean DEFAULT_LOWER_INCLUSIVE_VALUE = true;
	public static final boolean DEFAULT_UPPER_INCLUSIVE_VALUE = true;

	/**
	 * Returns the lower bound of this range. The returned value must either
	 * be an object matching the {@link #getValueType() value-type} specified for
	 * this range or an {@link Expression} that returns a compatible value.
	 *
	 * @return
	 */
	<V extends Comparable<?>> Optional<V> getLowerBound();

	/**
	 * Returns the upper bound of this range. The returned value must either
	 * be an object matching the {@link #getValueType() value-type} specified for
	 * this range or an {@link Expression} that returns a compatible value.
	 *
	 * @return
	 */
	<V extends Comparable<?>> Optional<V> getUpperBound();

	/**
	 * Returns the interval size used when stepping through this range.
	 *
	 * @return
	 */
	<V extends Comparable<?>> Optional<V> getStepSize();

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

	void setLowerBound(Comparable<?> lowerBound);

	void setUpperBound(Comparable<?> upperBound);

	void setStepSize(Comparable<?> stepSize);

	void setLowerBoundInclusive(boolean lowerBoundInclusive);

	void setUpperBoundInclusive(boolean upperBoundInclusive);
}
