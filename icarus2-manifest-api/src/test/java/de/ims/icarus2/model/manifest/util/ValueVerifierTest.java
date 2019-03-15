/**
 *
 */
package de.ims.icarus2.model.manifest.util;

import static de.ims.icarus2.test.TestTags.RANDOMIZED;
import static de.ims.icarus2.test.TestUtils.EMOJI;
import static de.ims.icarus2.test.TestUtils.displayString;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.stringStream;
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
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.api.ValueSet;
import de.ims.icarus2.model.manifest.standard.ValueRangeImpl;
import de.ims.icarus2.model.manifest.standard.ValueSetImpl;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ValueVerifier.IntVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.LongVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.ObjectVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.VerificationResult;

/**
 * @author Markus Gärtner
 *
 */
class ValueVerifierTest {

	private static ValueSet createValueSet(ValueType valueType, Object...values) {
		return new ValueSetImpl(valueType).addAll(values);
	}

	private static ValueSet createValueSet(ValueType valueType, Stream<?> values) {
		return new ValueSetImpl(valueType).addAll(values);
	}

	private static <C extends Comparable<?>> ValueRange createValueRange(ValueType valueType,
			C lower, C upper, boolean lowerInc, boolean upperInc) {
		return new ValueRangeImpl(valueType, lower, upper, lowerInc, upperInc);
	}

	@Nested
	class ForObjectVerifier {

		@Tag(RANDOMIZED)
		@TestFactory
		@DisplayName("empty")
		Stream<DynamicContainer> testEmpty() {
			return config()
					.expect(VerificationResult.VALID, stringStream(10, 20).limit(10))
					.createTests(ObjectVerifier::new);
		}

		@Tag(RANDOMIZED)
		@TestFactory
		@DisplayName("with ValueSet of random strings")
		Stream<DynamicContainer> testWithSet() {
			// Use 10 random strings of length 10 to 20
			return config()
					.valueSet(createValueSet(ValueType.STRING,
							stringStream(10, 20).limit(10)))
					.expect(VerificationResult.VALUE_NOT_IN_SET,
							Stream.of("a", "", EMOJI, "xxxxxxxxxxxxxxxxxx$"))
					.createTests(ObjectVerifier::new);
		}

		@Tag(RANDOMIZED)
		@TestFactory
		@DisplayName("with ValueRange [c,k]")
		Stream<DynamicContainer> testWithRangeInclusive() {
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

		@Tag(RANDOMIZED)
		@TestFactory
		@DisplayName("with ValueRange (c,k)")
		Stream<DynamicContainer> testWithRangeExclusive() {
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
	}

	@Nested
	class ForIntVerifier {

		@Tag(RANDOMIZED)
		@TestFactory
		@DisplayName("empty")
		Stream<DynamicContainer> testEmpty() {
			return config()
					.expect(VerificationResult.VALID, random().ints(10).boxed())
					.createTests(IntVerifier::new);
		}

		@Tag(RANDOMIZED)
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

		@Tag(RANDOMIZED)
		@TestFactory
		@DisplayName("with ValueRange [1,10]")
		Stream<DynamicContainer> testWithRangeInclusive() {
			// Use 1 to 10 as bounds
			return config()
					.valueRange(createValueRange(ValueType.INTEGER, _int(1), _int(10), true, true))
					.expect(VerificationResult.VALID, IntStream.range(1, 10).boxed())
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							IntStream.of(-1, 0, Integer.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							IntStream.of(11, 12, Integer.MAX_VALUE).boxed())
					.createTests(IntVerifier::new);
		}

		@Tag(RANDOMIZED)
		@TestFactory
		@DisplayName("with ValueRange [1,10] stepSize=2")
		Stream<DynamicContainer> testWithRangeInclusiveAndStepSize() {
			// Use 1 to 10 as bounds
			return config()
					.valueRange(createValueRange(ValueType.INTEGER, _int(1), _int(10), true, true)
							.setStepSize(_int(2)))
					.expect(VerificationResult.VALID,
							IntStream.range(1, 10).filter(i -> i%2==0).boxed())
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							IntStream.of(-1, 0, Integer.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							IntStream.of(11, 12, Integer.MAX_VALUE).boxed())
					.createTests(IntVerifier::new);
		}

		@Tag(RANDOMIZED)
		@TestFactory
		@DisplayName("with ValueRange (1,10)")
		Stream<DynamicContainer> testWithRangeExclusive() {
			// Use 1 to 10 as bounds
			return config()
					.valueRange(createValueRange(ValueType.INTEGER, _int(1), _int(10), false, false))
					.expect(VerificationResult.VALID, IntStream.range(2, 9).boxed())
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							IntStream.of(-1, 0, Integer.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							IntStream.of(11, 12, Integer.MAX_VALUE).boxed())
					.createTests(IntVerifier::new);
		}

		@Tag(RANDOMIZED)
		@TestFactory
		@DisplayName("with ValueRange (1,10) stepSize=2")
		Stream<DynamicContainer> testWithRangeExclusiveAndStepSize() {
			// Use 1 to 10 as bounds
			return config()
					.valueRange(createValueRange(ValueType.INTEGER, _int(1), _int(10), false, false)
							.setStepSize(_int(2)))
					.expect(VerificationResult.VALID,
							IntStream.range(2, 9).filter(i -> i%2==0).boxed())
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							IntStream.of(-1, 0, Integer.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							IntStream.of(11, 12, Integer.MAX_VALUE).boxed())
					.createTests(IntVerifier::new);
		}
	}

	@Nested
	class ForLongVerifier {

		@Tag(RANDOMIZED)
		@TestFactory
		@DisplayName("empty")
		Stream<DynamicContainer> testEmpty() {
			return config()
					.expect(VerificationResult.VALID, random().longs(10).boxed())
					.createTests(LongVerifier::new);
		}

		@Tag(RANDOMIZED)
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

		@Tag(RANDOMIZED)
		@TestFactory
		@DisplayName("with ValueRange [1,10]")
		Stream<DynamicContainer> testWithRangeInclusive() {
			// Use 1 to 10 as bounds
			return config()
					.valueRange(createValueRange(ValueType.LONG, _long(1), _long(10), true, true))
					.expect(VerificationResult.VALID, LongStream.range(1, 10).boxed())
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							LongStream.of(-1, 0, Long.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							LongStream.of(11, 12, Long.MAX_VALUE).boxed())
					.createTests(LongVerifier::new);
		}

		@Tag(RANDOMIZED)
		@TestFactory
		@DisplayName("with ValueRange [1,10] stepSize=2")
		Stream<DynamicContainer> testWithRangeInclusiveAndStepSize() {
			// Use 1 to 10 as bounds
			return config()
					.valueRange(createValueRange(ValueType.LONG, _long(1), _long(10), true, true)
							.setStepSize(_long(2)))
					.expect(VerificationResult.VALID,
							LongStream.range(1, 10).filter(i -> i%2==0).boxed())
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							LongStream.of(-1, 0, Long.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							LongStream.of(11, 12, Long.MAX_VALUE).boxed())
					.createTests(LongVerifier::new);
		}

		@Tag(RANDOMIZED)
		@TestFactory
		@DisplayName("with ValueRange (1,10)")
		Stream<DynamicContainer> testWithRangeExclusive() {
			// Use 1 to 10 as bounds
			return config()
					.valueRange(createValueRange(ValueType.LONG, _long(1), _long(10), false, false))
					.expect(VerificationResult.VALID, LongStream.range(2, 9).boxed())
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							LongStream.of(-1, 0, Long.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							LongStream.of(11, 12, Long.MAX_VALUE).boxed())
					.createTests(LongVerifier::new);
		}

		@Tag(RANDOMIZED)
		@TestFactory
		@DisplayName("with ValueRange (1,10) stepSize=2")
		Stream<DynamicContainer> testWithRangeExclusiveAndStepSize() {
			// Use 1 to 10 as bounds
			return config()
					.valueRange(createValueRange(ValueType.LONG, _long(1), _long(10), false, false)
							.setStepSize(_long(2)))
					.expect(VerificationResult.VALID,
							LongStream.range(2, 9).filter(i -> i%2==0).boxed())
					.expect(VerificationResult.LOWER_BOUNDARY_VIOLATION,
							LongStream.of(-1, 0, Long.MIN_VALUE).boxed())
					.expect(VerificationResult.UPPER_BOUNDARY_VIOLATION,
							LongStream.of(11, 12, Long.MAX_VALUE).boxed())
					.createTests(LongVerifier::new);
		}
	}


	//TODO other types (long, float, double)

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
