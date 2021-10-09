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
/**
 *
 */
package de.ims.icarus2.query.api.exp;

import javax.annotation.Nullable;

/**
 * @author Markus Gärtner
 *
 */
public interface Assignable<T> extends Expression<T> {

	void assign(@Nullable Object value);

	default void assignInt(int value) {
		throw EvaluationUtils.forUnsupportedCast(getResultType(), TypeInfo.INTEGER);
	}

	default void assignFloat(float value) {
		throw EvaluationUtils.forUnsupportedCast(getResultType(), TypeInfo.FLOATING_POINT);
	}

	default void assignLong(long value) {
		throw EvaluationUtils.forUnsupportedCast(getResultType(), TypeInfo.INTEGER);
	}

	default void assignDouble(double value) {
		throw EvaluationUtils.forUnsupportedCast(getResultType(), TypeInfo.FLOATING_POINT);
	}

	default void assignBoolean(boolean value) {
		throw EvaluationUtils.forUnsupportedCast(getResultType(), TypeInfo.BOOLEAN);
	}

	void clear();

	@Override
	Assignable<T> duplicate(EvaluationContext context);
}
