/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine.result;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.function.ToIntFunction;

import de.ims.icarus2.query.api.exp.Expression;
import de.ims.icarus2.query.api.exp.TypeInfo;

/**
 * Models the extraction of data from a live collection of model framework
 * members and stores the resulting data point inside the payload of a
 * {@link ResultEntry} instance.
 * <p>
 * Extraction is supported for the following {@link TypeInfo types}:
 * <ul>
 * <li>{@link TypeInfo#INTEGER}</li>
 * <li>{@link TypeInfo#FLOATING_POINT}</li>
 * <li>{@link TypeInfo#TEXT}</li>
 * <li>{@link TypeInfo#BOOLEAN}</li>
 * </ul>
 * In addition {@link TypeInfo#ARRAY} instances of all the types above are
 * also allowed.
 *
 * @author Markus Gärtner
 *
 */
public abstract class Extractor {

	public static double decode(long value) {
		return Double.longBitsToDouble(value);
	}

	public static long encode(double value) {
		return Double.doubleToLongBits(value);
	}

	/** The expression used to extract data */
	protected final Expression<?> expression;

	protected Extractor(Expression<?> expression) {
		this.expression = requireNonNull(expression);
	}

	public abstract long extract();

	public final Expression<?> getExpression() { return expression; }

	public static final class IntegerExtractor extends Extractor {

		public IntegerExtractor(Expression<?> expression) {
			super(expression);
			checkArgument("Expression must evaluate to integer result", expression.isInteger());
		}

		@Override
		public long extract() {
			return expression.computeAsLong();
		}
	}

	public static final class FloatingPointExtractor extends Extractor {

		public FloatingPointExtractor(Expression<?> expression) {
			super(expression);
			checkArgument("Expression must evaluate to floating point result", expression.isFloatingPoint());
		}

		@Override
		public long extract() {
			return encode(expression.computeAsDouble());
		}
	}

	public static final class BooleanExtractor extends Extractor {

		static final long FALSE = 0L;
		static final long TRUE = 1L;

		public BooleanExtractor(Expression<?> expression) {
			super(expression);
			checkArgument("Expression must evaluate to boolean result", expression.isBoolean());
		}

		@Override
		public long extract() {
			return expression.computeAsBoolean() ? TRUE : FALSE;
		}
	}

	public static final class TextExtractor extends Extractor {

		private final ToIntFunction<CharSequence> encoder;

		public TextExtractor(Expression<?> expression, ToIntFunction<CharSequence> encoder) {
			super(expression);
			this.encoder = requireNonNull(encoder);
			checkArgument("Expression must evaluate to text result", expression.isText());
		}

		@Override
		public long extract() {
			return encoder.applyAsInt((CharSequence) expression.compute());
		}
	}
}
