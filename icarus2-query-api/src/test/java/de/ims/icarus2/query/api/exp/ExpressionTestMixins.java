/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import de.ims.icarus2.query.api.exp.TypeInfo;
import de.ims.icarus2.query.api.exp.ExpressionTest.BooleanListExpressionTest;
import de.ims.icarus2.query.api.exp.ExpressionTest.FloatingPointListExpressionTest;
import de.ims.icarus2.query.api.exp.ExpressionTest.IntegerListExpressionTest;
import de.ims.icarus2.query.api.exp.ExpressionTest.TextListExpressionTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
public interface ExpressionTestMixins {

	public interface LongArrayMixin extends IntegerListExpressionTest<long[]> {
		@Override
		default long[] sized(int size) {
			return LongStream.range(0, size).toArray();
		}

		@Override
		default long[] constant() {
			return new long[] {
					0,
					1,
					Integer.MAX_VALUE,
					Long.MAX_VALUE,
					Integer.MIN_VALUE,
					Long.MIN_VALUE,
			};
		}

		@Override
		default long[] random(RandomGenerator rng) {
			return new long[] {
					rng.nextLong(),
					rng.nextLong(),
					rng.nextLong(),
					rng.nextLong(),
					rng.nextLong(),
					rng.nextLong(),
			};
		}

		@Override
		default TypeInfo getExpectedType() { return TypeInfo.of(long[].class, true); }
	}

	public interface DoubleArrayMixin extends FloatingPointListExpressionTest<double[]> {
		@Override
		default double[] sized(int size) {
			return DoubleStream.iterate(1.0, i -> -i*2.5).limit(size).toArray();
		}

		@Override
		default double[] constant() {
			return new double[] {
					0.1,
					1.1,
					Integer.MAX_VALUE+0.5,
					Long.MAX_VALUE-0.5,
					Integer.MIN_VALUE+.05,
					Long.MIN_VALUE+0.5,
			};
		}

		@Override
		default double[] random(RandomGenerator rng) {
			return new double[] {
					rng.nextDouble(),
					rng.nextDouble(),
					rng.nextDouble(),
					rng.nextDouble(),
					rng.nextDouble(),
					rng.nextDouble(),
			};
		}

		@Override
		default TypeInfo getExpectedType() { return TypeInfo.of(double[].class, true); }
	}

	public interface BooleanArrayMixin extends BooleanListExpressionTest<boolean[]> {

		@Override
		default boolean[] sized(int size) {
			boolean[] data = new boolean[size];
			for (int i = 0; i < data.length; i++) {
				data[i] = i%2==0;
			}
			return data;
		}

		@Override
		default boolean[] constant() {
			return new boolean[] {
					false,
					true,
					true,
					false,
					true,
					false,
			};
		}

		@Override
		default boolean[] random(RandomGenerator rng) {
			return new boolean[] {
					rng.nextBoolean(),
					rng.nextBoolean(),
					rng.nextBoolean(),
					rng.nextBoolean(),
					rng.nextBoolean(),
					rng.nextBoolean(),
			};
		}

		@Override
		default TypeInfo getExpectedType() { return TypeInfo.of(boolean[].class, true); }
	}

	public interface TextArrayMixin extends TextListExpressionTest<CharSequence[]> {

		@Override
		default CharSequence[] sized(int size) {
			return IntStream.range(0, size)
				.mapToObj(i -> "item_"+i)
				.toArray(CharSequence[]::new);
		}

		@Override
		default CharSequence[] constant() {
			return new CharSequence[] {
					"test",
					"test2",
					CodePointUtilsTest.test,
					CodePointUtilsTest.test_hebrew,
					CodePointUtilsTest.test_mixed2,
					CodePointUtilsTest.test_mixed3,
			};
		}

		@Override
		default CharSequence[] random(RandomGenerator rng) {
			return new CharSequence[] {
					rng.randomUnicodeString(10),
					rng.randomUnicodeString(5),
					rng.randomUnicodeString(15),
					rng.randomUnicodeString(22),
					rng.randomUnicodeString(25),
					rng.randomUnicodeString(2),
			};
		}

		@Override
		default TypeInfo getExpectedType() { return TypeInfo.of(CharSequence[].class, true); }

		@Override
		default TypeInfo getExpectedElementType() { return TypeInfo.TEXT; }

		@Override
		default CharSequence[] randomForGet(RandomGenerator rng) { return random(rng); }
	}
}
