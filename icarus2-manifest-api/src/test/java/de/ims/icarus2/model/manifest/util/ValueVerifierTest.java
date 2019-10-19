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
/**
 *
 */
package de.ims.icarus2.model.manifest.util;

import static de.ims.icarus2.test.TestUtils.EMOJI;
import static de.ims.icarus2.test.TestUtils.displayString;
import static de.ims.icarus2.util.lang.Primitives._double;
import static de.ims.icarus2.util.lang.Primitives._float;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.api.ValueSet;
import de.ims.icarus2.model.manifest.standard.ValueRangeImpl;
import de.ims.icarus2.model.manifest.standard.ValueSetImpl;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ValueVerifier.DoubleVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.FloatVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.IntVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.LongVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.ObjectVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.VerificationResult;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
class ValueVerifierTest {

	private static ValueSet createValueSet(ValueType valueType, Stream<?> values) {
		return new ValueSetImpl(valueType).addAll(values);
	}

	private static <C extends Comparable<?>> ValueRange createValueRange(ValueType valueType,
			C lower, C upper, boolean lowerInc, boolean upperInc) {
		return new ValueRangeImpl(valueType, lower, upper, lowerInc, upperInc);
	}

	private static <C extends Comparable<?>> ValueRange createOpenValueRange(ValueType valueType,
			C bound, boolean inclusive, boolean isUpper) {
		ValueRange valueRange = new ValueRangeImpl(valueType);
		if(isUpper) {
			valueRange.setUpperBound(bound)
				.setUpperBoundInclusive(inclusive);
		} else {
			valueRange.setLowerBound(bound)
				.setLowerBoundInclusive(inclusive);
		}
		return valueRange;
	}

	@Nested
	class ForObjectVerifier {

		@TestFactory
		@DisplayName("empty")
		@RandomizedTest
		Stream<DynamicContainer> testEmpty(RandomGenerator rand) {
			return config()
					.expect(VerificationResult.VALID, rand.stringStream(10, 20).limit(10))
					.createTests(ObjectVerifier::new);
		}

		@TestFactory
		@RandomizedTest
		@DisplayName("with ValueSet of random strings")
		Stream<DynamicContainer> testWithSet(RandomGenerator rand) {
			// Use 10 random strings of length 10 to 20
			return config()
					.valueSet(createValueSet(ValueType.STRING,
							rand.stringStream(10, 20).limit(10)))
					.expect(VerificationResult.VALUE_NOT_IN_SET,
							Stream.of("a", "", EMOJI, "xxxxxxxxxxxxxxxxxx$"))
					.createTests(ObjectVerifier::new);
		}

		@TestFactory
		@RandomizedTest
		@DisplayName("with ValueRange [c,k]")
		Stream<DynamicContainer> testWithClosedRangeInclusive() {
			// Use c to k as bounds
			return config()
					.valueRange(createValueRange(ValueType.STRING, "c", "k", true, true))
					.expect(VerificationResult.VALID,
							IntStream.range('c', 'l').mapToObj(i -> String.valueOf((char)i)))
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							Stream.of("a", "b"))
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							Stream.of("l", "m"))
					.createTests(ObjectVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange (c,k)")
		Stream<DynamicContainer> testWithClosedRangeExclusive() {
			// Use c to k as bounds
			return config()
					.valueRange(createValueRange(ValueType.STRING, "c", "k", false, false))
					.expect(VerificationResult.VALID,
							IntStream.range('d', 'k').mapToObj(i -> String.valueOf((char)i)))
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							Stream.of("b", "c"))
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							Stream.of("k", "l"))
					.createTests(ObjectVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange [c..")
		Stream<DynamicContainer> testWithRightOpenRangeInclusive() {
			// Use c to k as bounds
			return config()
					.valueRange(createOpenValueRange(ValueType.STRING, "c", true, false))
					.expect(VerificationResult.VALID,
							Stream.of("c", "d", "x"))
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							Stream.of("a", "b"))
					.createTests(ObjectVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange ..k]")
		Stream<DynamicContainer> testWithLeftOpenRangeInclusive() {
			// Use c to k as bounds
			return config()
					.valueRange(createOpenValueRange(ValueType.STRING, "k", true, true))
					.expect(VerificationResult.VALID,
							Stream.of("k", "j", "a"))
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							Stream.of("l", "m"))
					.createTests(ObjectVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange (c..")
		Stream<DynamicContainer> testWithRightOpenRangeExclusive() {
			// Use c to k as bounds
			return config()
					.valueRange(createOpenValueRange(ValueType.STRING, "c", false, false))
					.expect(VerificationResult.VALID,
							Stream.of("d", "e", "x"))
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							Stream.of("b", "c"))
					.createTests(ObjectVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange ..k)")
		Stream<DynamicContainer> testWithLeftOpenRangeExclusive() {
			// Use c to k as bounds
			return config()
					.valueRange(createOpenValueRange(ValueType.STRING, "k", false, true))
					.expect(VerificationResult.VALID,
							Stream.of("j", "i", "a"))
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							Stream.of("k", "l"))
					.createTests(ObjectVerifier::new);
		}
	}

	@Nested
	class ForIntVerifier {

		@TestFactory
		@RandomizedTest
		@DisplayName("empty")
		Stream<DynamicContainer> testEmpty(RandomGenerator rand) {
			return config()
					.expect(VerificationResult.VALID, rand.ints(10).boxed())
					.createTests(IntVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueSet [1,10]")
		Stream<DynamicContainer> testWithSet() {
			// Use 1 to 10 as valid values
			return config()
					.valueSet(createValueSet(ValueType.INTEGER, IntStream.range(1, 11).boxed()))
					.expect(VerificationResult.VALUE_NOT_IN_SET,
							IntStream.of(-1, 0, 11, Integer.MIN_VALUE, Integer.MAX_VALUE).boxed())
					.createTests(IntVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange [1,10]")
		Stream<DynamicContainer> testWithClosedRangeInclusive() {
			int lower = 1;
			int upper = 10;

			return config()
					.valueRange(createValueRange(ValueType.INTEGER,
							_int(lower), _int(upper), true, true))
					.expect(VerificationResult.VALID, IntStream.range(lower, upper).boxed())
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							IntStream.of(lower-2, lower-1, Integer.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							IntStream.of(upper+1, upper+2, Integer.MAX_VALUE).boxed())
					.createTests(IntVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange [1,10] stepSize=2")
		Stream<DynamicContainer> testWithClosedRangeInclusiveAndStepSize() {
			int lower = 1;
			int upper = 9;
			int step = 2;

			return config()
					.valueRange(createValueRange(ValueType.INTEGER,
							_int(lower), _int(upper), true, true)
							.setStepSize(_int(step)))
					.expect(VerificationResult.VALID,
							IntStream.range(lower, upper).filter(i -> (i-lower)%step==0).boxed())
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							IntStream.of(lower-2, lower-1, Integer.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							IntStream.of(upper+1, upper+2, Integer.MAX_VALUE).boxed())
					.expect(VerificationResult.PRECISION_MISMATCH,
							IntStream.range(lower, upper).filter(i -> i%step==0).boxed())
					.createTests(IntVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange (1,10)")
		Stream<DynamicContainer> testWithClosedRangeExclusive() {
			int lower = 1;
			int upper = 10;

			return config()
					.valueRange(createValueRange(ValueType.INTEGER,
							_int(lower), _int(upper), false, false))
					.expect(VerificationResult.VALID, IntStream.range(lower+1, upper-1).boxed())
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							IntStream.of(lower-1, lower, Integer.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							IntStream.of(upper, upper+1, Integer.MAX_VALUE).boxed())
					.createTests(IntVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange (1,10) stepSize=2")
		Stream<DynamicContainer> testWithClosedRangeExclusiveAndStepSize() {
			int lower = 1;
			int upper = 9;
			int step = 2;

			return config()
					.valueRange(createValueRange(ValueType.INTEGER,
							_int(lower), _int(upper), false, false)
							.setStepSize(_int(step)))
					.expect(VerificationResult.VALID,
							IntStream.range(lower+1, upper-1).filter(i -> (i-lower)%step==0).boxed())
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							IntStream.of(lower-1, lower, Integer.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							IntStream.of(upper, upper+1, Integer.MAX_VALUE).boxed())
					.expect(VerificationResult.PRECISION_MISMATCH,
							IntStream.range(lower, upper).filter(i -> i%step==0).boxed())
					.createTests(IntVerifier::new);
		}
	}

	@Nested
	class ForLongVerifier {

		@TestFactory
		@RandomizedTest
		@DisplayName("empty")
		Stream<DynamicContainer> testEmpty(RandomGenerator rand) {
			return config()
					.expect(VerificationResult.VALID, rand.longs(10).boxed())
					.createTests(LongVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueSet [1,10]")
		Stream<DynamicContainer> testWithSet() {
			// Use 1 to 10 as valid values
			return config()
					.valueSet(createValueSet(ValueType.LONG, LongStream.range(1, 11).boxed()))
					.expect(VerificationResult.VALUE_NOT_IN_SET,
							LongStream.of(-1, 0, 11, Long.MIN_VALUE, Long.MAX_VALUE).boxed())
					.createTests(LongVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange [1,10]")
		Stream<DynamicContainer> testWithClosedRangeInclusive() {
			long lower = 1;
			long upper = 10;

			return config()
					.valueRange(createValueRange(ValueType.LONG,
							_long(lower), _long(upper), true, true))
					.expect(VerificationResult.VALID, LongStream.range(lower, upper).boxed())
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							LongStream.of(lower-2, lower-1, Long.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							LongStream.of(upper+1, upper+2, Long.MAX_VALUE).boxed())
					.createTests(LongVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange [1,9] stepSize=2")
		Stream<DynamicContainer> testWithClosedRangeInclusiveAndStepSize() {
			long lower = 1;
			long upper = 9;
			long step = 2;

			return config()
					.valueRange(createValueRange(ValueType.LONG,
							_long(lower), _long(upper), true, true)
							.setStepSize(_long(step)))
					.expect(VerificationResult.VALID,
							LongStream.range(lower, upper).filter(i -> (i-lower)%step==0).boxed())
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							LongStream.of(lower-2, lower-1, Long.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							LongStream.of(upper+1, upper+2, Long.MAX_VALUE).boxed())
					.expect(VerificationResult.PRECISION_MISMATCH,
							LongStream.range(lower, upper).filter(i -> i%step==0).boxed())
					.createTests(LongVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange (1,9)")
		Stream<DynamicContainer> testWithClosedRangeExclusive() {
			long lower = 1;
			long upper = 10;

			return config()
					.valueRange(createValueRange(ValueType.LONG,
							_long(lower), _long(upper), false, false))
					.expect(VerificationResult.VALID, LongStream.range(lower+1, upper-1).boxed())
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							LongStream.of(lower-1, lower, Long.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							LongStream.of(upper, upper+1, Long.MAX_VALUE).boxed())
					.createTests(LongVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange (1,10) stepSize=2")
		Stream<DynamicContainer> testWithClosedRangeExclusiveAndStepSize() {
			long lower = 1;
			long upper = 9;
			long step = 2;

			return config()
					.valueRange(createValueRange(ValueType.LONG,
							_long(lower), _long(upper), false, false)
							.setStepSize(_long(step)))
					.expect(VerificationResult.VALID,
							LongStream.range(lower+1, upper-1).filter(i -> (i-lower)%step==0).boxed())
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							LongStream.of(lower-1, lower, Long.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							LongStream.of(upper, upper+1, Long.MAX_VALUE).boxed())
					.expect(VerificationResult.PRECISION_MISMATCH,
							LongStream.range(lower, upper).filter(i -> i%step==0).boxed())
					.createTests(LongVerifier::new);
		}
	}

	private static final DoubleFunction<Float> toFloat =
		v -> Float.valueOf((float)v);

	@Nested
	class ForFloatVerifier {

		@TestFactory
		@RandomizedTest
		@DisplayName("empty")
		Stream<DynamicContainer> testEmpty(RandomGenerator rand) {
			return config()
					.expect(VerificationResult.VALID,
							rand.doubles(10, -Float.MAX_VALUE, Float.MAX_VALUE)
							.mapToObj(toFloat))
					.createTests(FloatVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueSet [1.5,3,...,15]")
		Stream<DynamicContainer> testWithSet() {
			// Use 1 to 10 as valid values
			return config()
					.valueSet(createValueSet(ValueType.FLOAT,
							Stream.iterate(_float(1.5F), v -> _float(v.floatValue()+1.5F))
							.limit(10)))
					.expect(VerificationResult.VALUE_NOT_IN_SET,
							DoubleStream.of(-1.1, 0.0, 1.4, 1.7, 3.5, 15.1, -Float.MAX_VALUE, Float.MAX_VALUE)
							.mapToObj(toFloat))
					.createTests(FloatVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange [1.5,9.5]")
		Stream<DynamicContainer> testWithClosedRangeInclusive() {
			return config()
					.valueRange(createValueRange(ValueType.FLOAT, _float(1.5F), _float(9.5F), true, true))
					.expect(VerificationResult.VALID,
							Stream.iterate(_float(1.5F), v -> _float(v.floatValue()+2F))
							.limit(5))
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							DoubleStream.of(-1, 0, 1.499, -Float.MAX_VALUE).mapToObj(toFloat))
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							DoubleStream.of(9.501, 12, Float.MAX_VALUE).mapToObj(toFloat))
					.createTests(FloatVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange [1.5,9.5] stepSize=2")
		Stream<DynamicContainer> testWithClosedRangeInclusiveAndStepSize() {
			return config()
					.valueRange(createValueRange(ValueType.FLOAT, _float(1.5F), _float(9.5F), true, true)
							.setStepSize(_float(2)))
					.expect(VerificationResult.VALID,
							Stream.iterate(_float(1.5F), v -> _float(v.floatValue()+2F))
							.limit(5))
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							DoubleStream.of(-1, 0, 1.499, -Float.MAX_VALUE).mapToObj(toFloat))
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							DoubleStream.of(9.501, 12, Float.MAX_VALUE).mapToObj(toFloat))
					.expect(VerificationResult.PRECISION_MISMATCH,
							DoubleStream.of(1.6, 2.9, 3.1).mapToObj(toFloat)) //TODO
					.createTests(FloatVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange (1.5,9.5)")
		Stream<DynamicContainer> testWithClosedRangeExclusive() {
			return config()
					.valueRange(createValueRange(ValueType.FLOAT,
							_float(1.5F), _float(9.5F), false, false))
					.expect(VerificationResult.VALID,
							Stream.iterate(_float(3.5F), v -> _float(v.floatValue()+2F))
							.limit(3))
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							DoubleStream.of(-1, 0, 1.5, -Float.MAX_VALUE).mapToObj(toFloat))
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							DoubleStream.of(9.5, 12, Float.MAX_VALUE).mapToObj(toFloat))
					.createTests(FloatVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange (1.5,9.5) stepSize=2")
		Stream<DynamicContainer> testWithClosedRangeExclusiveAndStepSize() {
			return config()
					.valueRange(createValueRange(ValueType.FLOAT,
							_float(1.5F), _float(9.5F), false, false)
							.setStepSize(_float(2)))
					.expect(VerificationResult.VALID,
							Stream.iterate(_float(3.5F), v -> _float(v.floatValue()+2F))
							.limit(3))
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							DoubleStream.of(-1, 0, 1.5, -Float.MAX_VALUE).mapToObj(toFloat))
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							DoubleStream.of(9.5, 12, Float.MAX_VALUE).mapToObj(toFloat))
					.expect(VerificationResult.PRECISION_MISMATCH,
							DoubleStream.of(1.6, 2.9, 3.1, 9.499).mapToObj(toFloat)) //TODO
					.createTests(FloatVerifier::new);
		}
	}

	@Nested
	class ForDoubleVerifier {

		@TestFactory
		@RandomizedTest
		@DisplayName("empty")
		Stream<DynamicContainer> testEmpty(RandomGenerator rand) {
			return config()
					.expect(VerificationResult.VALID,
							rand.doubles(10, Double.MIN_VALUE, Double.MAX_VALUE).boxed())
					.createTests(DoubleVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueSet [1.5,3,...,15]")
		Stream<DynamicContainer> testWithSet() {
			// Use 1 to 10 as valid values
			return config()
					.valueSet(createValueSet(ValueType.DOUBLE,
							Stream.iterate(_double(1.5F), v -> _double(v.doubleValue()+1.5F))
							.limit(10)))
					.expect(VerificationResult.VALUE_NOT_IN_SET,
							DoubleStream.of(-1.1, 0.0, 1.4, 1.7, 3.5, 15.1,
									Double.MIN_VALUE, Double.MAX_VALUE).boxed())
					.createTests(DoubleVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange [1.5,9.5]")
		Stream<DynamicContainer> testWithClosedRangeInclusive() {
			return config()
					.valueRange(createValueRange(ValueType.DOUBLE, _double(1.5), _double(9.5), true, true))
					.expect(VerificationResult.VALID,
							Stream.iterate(_double(1.5), v -> _double(v.doubleValue()+2))
							.limit(5))
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							DoubleStream.of(-1, 0, 1.499, Double.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							DoubleStream.of(9.501, 12, Double.MAX_VALUE).boxed())
					.createTests(DoubleVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange [1.5,9.5] stepSize=2")
		Stream<DynamicContainer> testWithClosedRangeInclusiveAndStepSize() {
			return config()
					.valueRange(createValueRange(ValueType.DOUBLE, _double(1.5), _double(9.5), true, true)
							.setStepSize(_double(2)))
					.expect(VerificationResult.VALID,
							Stream.iterate(_double(1.5), v -> _double(v.doubleValue()+2))
							.limit(5))
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							DoubleStream.of(-1, 0, 1.499, Double.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							DoubleStream.of(9.501, 12, Double.MAX_VALUE).boxed())
					.expect(VerificationResult.PRECISION_MISMATCH,
							DoubleStream.of(1.6, 2.9, 3.1).boxed()) //TODO
					.createTests(DoubleVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange (1.5,9.5)")
		Stream<DynamicContainer> testWithClosedRangeExclusive() {
			return config()
					.valueRange(createValueRange(ValueType.DOUBLE,
							_double(1.5), _double(9.5), false, false))
					.expect(VerificationResult.VALID,
							Stream.iterate(_double(3.5), v -> _double(v.doubleValue()+2))
							.limit(3))
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							DoubleStream.of(-1, 0, 1.5, -Double.MAX_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							DoubleStream.of(9.5, 12, Double.MAX_VALUE).boxed())
					.createTests(DoubleVerifier::new);
		}

		@TestFactory
		@DisplayName("with ValueRange (1.5,9.5) stepSize=2")
		Stream<DynamicContainer> testWithClosedRangeExclusiveAndStepSize() {
			return config()
					.valueRange(createValueRange(ValueType.DOUBLE,
							_double(1.5), _double(9.5), false, false)
							.setStepSize(_double(2)))
					.expect(VerificationResult.VALID,
							Stream.iterate(_double(3.5), v -> _double(v.doubleValue()+2))
							.limit(3))
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							DoubleStream.of(-1, 0, 1.5, -Double.MAX_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							DoubleStream.of(9.5, 12, Double.MAX_VALUE).boxed())
					.expect(VerificationResult.PRECISION_MISMATCH,
							DoubleStream.of(1.6, 2.9, 3.1, 9.499).boxed()) //TODO
					.createTests(DoubleVerifier::new);
		}
	}


	private static TestConfig config() {
		return new TestConfig();
	}

	/**
	 * Bundles all parameters for a sequence of tests
	 *
	 * @author Markus Gärtner
	 *
	 */
	static class TestConfig {
		/**
		 * Fixed set of allowed values, may be null
		 */
		private ValueSet valueSet;
		/**
		 * Dynamic range of allowed values, may be null
		 */
		private ValueRange valueRange;

		/**
		 * Expected verification results for certain values,
		 * grouped by expected result.
		 */
		private EnumMap<VerificationResult, Set<Object>> expected = new EnumMap<>(VerificationResult.class);

		private Set<Object> expectedByResult(VerificationResult result) {
			return expected.computeIfAbsent(result, k -> new HashSet<>());
		}

		/**
		 * Set the {@link ValueSet} to be used and automatically
		 * add all its values as {@link VerificationResult#VALID}.
		 * @param valueSet
		 * @return
		 */
		TestConfig valueSet(ValueSet valueSet) {
			requireNonNull(valueSet);
			assumeTrue(this.valueSet==null, "Value set already set");
			this.valueSet = valueSet;

			// All members of ValueSet are valid!
			valueSet.forEach(expectedByResult(VerificationResult.VALID)::add);

			return this;
		}

		TestConfig valueRange(ValueRange valueRange) {
			requireNonNull(valueRange);
			assumeTrue(this.valueRange==null, "Value range already set");
			this.valueRange = valueRange;

			// Add the bounds as valid if they are defined and inclusive
			// For now we do NOT automatically add the bounds!!
//			if(valueRange.isLowerBoundInclusive())
//				valueRange.getLowerBound().ifPresent(expectedByResult(VerificationResult.VALID)::add);
//			if(valueRange.isUpperBoundInclusive())
//				valueRange.getUpperBound().ifPresent(expectedByResult(VerificationResult.VALID)::add);

			return this;
		}

		<T>TestConfig expect(VerificationResult result,
				@SuppressWarnings("unchecked") T...values) {
			Collections.addAll(
					expectedByResult(result),
					values);
			return this;
		}

		<T> TestConfig expect(VerificationResult result, Collection<T> values) {
			expectedByResult(result).addAll(values);
			return this;
		}

		<T> TestConfig expect(VerificationResult result, Stream<? extends T> values) {
			values.forEach(expectedByResult(result)::add);
			return this;
		}

		<V extends ValueVerifier> Stream<DynamicContainer> createTests(
				BiFunction<ValueRange, ValueSet, V> constructor) {

			final V verifier = constructor.apply(valueRange, valueSet);

			return expected.entrySet()
					.stream()
					.sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
					.map(entry -> dynamicContainer(
							entry.getKey()+" "+displayString(entry.getValue()),
							entry.getValue()
							.stream()
							.map(value -> dynamicTest(
									"value='"+displayString(value)+"'",
									() -> assertEquals(entry.getKey(),
											verifier.verify(value)))))
					);
		}
	}
}
