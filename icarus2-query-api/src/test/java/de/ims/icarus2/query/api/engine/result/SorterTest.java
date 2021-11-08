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
package de.ims.icarus2.query.api.engine.result;

import static de.ims.icarus2.util.lang.Primitives._int;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.query.api.engine.result.Sorter.AsciiSorter;
import de.ims.icarus2.query.api.engine.result.Sorter.FloatingPointSorter;
import de.ims.icarus2.query.api.engine.result.Sorter.IntegerSorter;
import de.ims.icarus2.query.api.engine.result.Sorter.UnicodeSorter;
import de.ims.icarus2.query.api.exp.BinaryOperations.StringMode;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.guard.ApiGuard;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.MutablePrimitives.MutableDouble;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.collections.Substitutor;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

/**
 * @author Markus Gärtner
 *
 */
class SorterTest {

	private static final Match DUMMY_MATCH = mock(Match.class);
	static {
		when(DUMMY_MATCH.toString()).thenReturn("DUMMY_MATCH");
	}

	private static ResultEntry entry(int payloadSize) {
		return new ResultEntry(DUMMY_MATCH, payloadSize);
	}

	private static ResultEntry entry(long...payload) {
		ResultEntry entry = new ResultEntry(DUMMY_MATCH, payload.length);
		System.arraycopy(payload, 0, entry.payload, 0, payload.length);
		return entry;
	}

	private static void assertEntry(ResultEntry entry, long...expectedPayload) {
		assertThat(entry.payload).containsExactly(expectedPayload);
	}

	private static Stream<DynamicTest> assertProcess(Sorter sorter, ResultEntry[] entries,
			boolean desc, RandomGenerator rng, int repetitions) {
		ResultEntry[] expected = entries.clone();
		if(desc) {
			ObjectArrays.reverse(expected);
		}

		return IntStream.range(0, repetitions).mapToObj(i ->
				dynamicTest(String.format("repetition %d/%d", _int(i+1), _int(repetitions)), () -> {

			rng.shuffle(entries);

//			System.out.println(Arrays.toString(entries));

			Arrays.sort(entries, sorter);

			assertThat(entries).containsExactly(expected);
		}));
	}

	interface TestBase<S extends Sorter> extends ApiGuardedTest<S> {

		@Override
		default void configureApiGuard(ApiGuard<S> apiGuard) {
			ApiGuardedTest.super.configureApiGuard(apiGuard);

			/*
			 *  We get both a compare(Object, Object) and compare(ResultEntry, ResultEntry)
			 *  method in the list for the null guard. Therefore we just over-generate
			 *  parameters here and spam ResultEntry instances.
			 */
			apiGuard.parameterResolver(Object.class, s -> new ResultEntry(mock(Match.class), 1));
			/*
			 * Some of the constructors have primitive parameters before the null-guarded
			 * object arguments. Therefore we need to make sure that  the
			 */
			apiGuard.parameterResolver(int.class, s -> _int(1));
		}

		S create(int offset, int sign, @Nullable Sorter next);

		long[] createSorted(int size, RandomGenerator rng);

		@Override
		default S createTestInstance(TestSettings settings) {
			return settings.process(create(0, Sorter.SIGN_ASC, null));
		}

		@Test
		default void testIllegalOffset() throws Exception {
			assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
					() -> create(-1, Sorter.SIGN_ASC, null)).withMessageContaining("Offset");
		}

		@ParameterizedTest
		@ValueSource(ints = {0, 2, -2})
		default void testIllegalSign(int sign) throws Exception {
			assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
					() -> create(0, sign, null)).withMessageContaining("Sign");
		}

		@TestFactory
		@RandomizedTest
		default Stream<DynamicNode> testOffsetsAsc(RandomGenerator rng) throws Exception {
			return IntStream.range(0, 10).mapToObj(offset -> {
				long[] values = createSorted(30, rng);
				ResultEntry[] entries = LongStream.of(values)
						.mapToObj(v -> {
							ResultEntry entry = entry(10);
							entry.payload[offset] = v;
							return entry;
						})
						.toArray(ResultEntry[]::new);

				S sorter = create(offset, Sorter.SIGN_ASC, null);

				return dynamicContainer("offset="+offset,
						assertProcess(sorter, entries, false, rng, TestUtils.RUNS));
			});
		}

		@TestFactory
		@RandomizedTest
		default Stream<DynamicNode> testOffsetsDesc(RandomGenerator rng) throws Exception {
			return IntStream.range(0, 10).mapToObj(offset -> {
				long[] values = createSorted(30, rng);
				ResultEntry[] entries = LongStream.of(values)
						.mapToObj(v -> {
							ResultEntry entry = entry(10);
							entry.payload[offset] = v;
							return entry;
						})
						.toArray(ResultEntry[]::new);
				ResultEntry[] expected = entries.clone();
				ObjectArrays.reverse(expected);

				S sorter = create(offset, Sorter.SIGN_DESC, null);

				return dynamicContainer("offset="+offset,
						assertProcess(sorter, entries, true, rng, TestUtils.RUNS));
			});
		}
	}

	@Nested
	class ForIntegerSorter implements TestBase<IntegerSorter> {

		@Override
		public Class<?> getTestTargetClass() { return IntegerSorter.class; }

		@Override
		public IntegerSorter create(int offset, int sign, Sorter next) {
			return new IntegerSorter(offset, sign, next);
		}

		@Override
		public long[] createSorted(int size, RandomGenerator rng) {
			MutableLong buffer = new MutableLong();
			return IntStream.range(0, size)
					.mapToLong(i -> buffer.getAndIncrement(rng.nextInt(20)))
					.toArray();
		}
	}

	@Nested
	class ForFloatingPointSorter implements TestBase<FloatingPointSorter> {

		@Override
		public Class<?> getTestTargetClass() { return FloatingPointSorter.class; }

		@Override
		public FloatingPointSorter create(int offset, int sign, Sorter next) {
			return new FloatingPointSorter(offset, sign, next);
		}

		@Override
		public long[] createSorted(int size, RandomGenerator rng) {
			MutableDouble buffer = new MutableDouble();
			return IntStream.range(0, size)
					.mapToLong(i -> Double.doubleToLongBits(buffer.getAndIncrement(rng.nextInt(20) * 0.1)))
					.toArray();
		}
	}

	@Nested
	class ForAsciiSorter implements TestBase<AsciiSorter> {

		private Substitutor<CharSequence> substitutor;

		@BeforeEach
		void setUp() {
			substitutor = new Substitutor<>();
		}

		@AfterEach
		void tearDown() {
			substitutor.close();
			substitutor = null;
		}

		@Override
		public Class<?> getTestTargetClass() { return FloatingPointSorter.class; }

		@Override
		public AsciiSorter create(int offset, int sign, Sorter next) {
			return new AsciiSorter(offset, sign, substitutor,
					StringMode.DEFAULT.getCharComparator(), next);
		}

		@Override
		public long[] createSorted(int size, RandomGenerator rng) {
			return IntStream.range(0, size)
					.mapToObj(i -> rng.randomString(5))
					.sorted()
					.mapToLong(s -> substitutor.applyAsInt(s))
					.toArray();
		}

		@ParameterizedTest
		@CsvSource({
			"X, x",
			"123, 123",
			"Water, water",
			"test, TeSt",
		})
		public void testCaseInsensitiveMatching(String a, String b) throws Exception {
			ResultEntry entryA = entry((long)substitutor.applyAsInt(a));
			ResultEntry entryB = entry((long)substitutor.applyAsInt(b));
			AsciiSorter sorter = new AsciiSorter(0, Sorter.SIGN_ASC, substitutor,
					StringMode.IGNORE_CASE.getCharComparator(), null);
			assertThat(entryA).usingComparator(sorter).isEqualTo(entryB);
		}
	}

	@Nested
	class ForUnicodeSorter implements TestBase<UnicodeSorter> {

		private Substitutor<CharSequence> substitutor;

		@BeforeEach
		void setUp() {
			substitutor = new Substitutor<>();
		}

		@AfterEach
		void tearDown() {
			substitutor.close();
			substitutor = null;
		}

		@Override
		public Class<?> getTestTargetClass() { return UnicodeSorter.class; }

		@Override
		public UnicodeSorter create(int offset, int sign, Sorter next) {
			return new UnicodeSorter(offset, sign, substitutor,
					StringMode.DEFAULT.getCodePointComparator(), next);
		}

		@Override
		public long[] createSorted(int size, RandomGenerator rng) {
			return IntStream.range(0, size)
					.mapToObj(i -> rng.randomString(5))
					.sorted()
					.mapToLong(s -> substitutor.applyAsInt(s))
					.toArray();
		}

		@ParameterizedTest
		@CsvSource({
			// ASCII values
			"X, x",
			"123, 123",
			"Water, water",
			"test, TeSt",
			/*
			 *  Unicode values (taken from https://www.utf8-chartable.de/unicode-utf8-table.pl)
			 *  using page with code points U+10280 to U+1067F to enforce multi-char codepoints
			 *  in the Java strings.
			 */
			"𐐁, 𐐩", // U+10401 -> U+10429
			"T𐐁sT, t𐐩ST",
		})
		public void testCaseInsensitiveMatching(String a, String b) throws Exception {
			ResultEntry entryA = entry((long)substitutor.applyAsInt(a));
			ResultEntry entryB = entry((long)substitutor.applyAsInt(b));
			UnicodeSorter sorter = new UnicodeSorter(0, Sorter.SIGN_ASC, substitutor,
					StringMode.IGNORE_CASE.getCodePointComparator(), null);
			assertThat(entryA).usingComparator(sorter).isEqualTo(entryB);
		}
	}

	@Nested
	class ForChainedSorters {

		@TestFactory
		@RandomizedTest
		public Stream<DynamicTest> testChainedInteger(RandomGenerator rng) throws Exception {
			long[] v1 = {0, 1, 1, 2, 3, 4, 4, 4};
			long[] v2 = {4, 4, 5, 0, 1, 2, 5, 9};
			ResultEntry[] entries = IntStream.range(0, v1.length)
					.mapToObj(i -> entry(v1[i], v2[i]))
					.toArray(ResultEntry[]::new);

			Sorter sorter = new IntegerSorter(0, Sorter.SIGN_ASC,
					new IntegerSorter(1, Sorter.SIGN_ASC, null));

			return assertProcess(sorter, entries, false, rng, TestUtils.RUNS);
		}

		@TestFactory
		@RandomizedTest
		public Stream<DynamicTest> testFloatingPointAndInteger(RandomGenerator rng) throws Exception {
			double[] v1 = {0.1, 1.3, 1.3, 2.7, 3.2, 4.5, 4.5, 4.5};
			long[] v2 = {4, 4, 5, 0, 1, 2, 5, 9};
			ResultEntry[] entries = IntStream.range(0, v1.length)
					.mapToObj(i -> entry(Double.doubleToLongBits(v1[i]), v2[i]))
					.toArray(ResultEntry[]::new);

			Sorter sorter = new FloatingPointSorter(0, Sorter.SIGN_ASC,
					new IntegerSorter(1, Sorter.SIGN_ASC, null));

			return assertProcess(sorter, entries, false, rng, TestUtils.RUNS);
		}

		@TestFactory
		@RandomizedTest
		public Stream<DynamicTest> testAsciiAndInteger(RandomGenerator rng) throws Exception {
			String[] v1 = {"a1", "b2", "b2", "b3", "c4", "d6", "d6", "d6"};
			long[] v2 = {4, 4, 5, 0, 1, 2, 5, 9};
			Substitutor<CharSequence> substitutor = new Substitutor<>();
			ResultEntry[] entries = IntStream.range(0, v1.length)
					.mapToObj(i -> entry(substitutor.applyAsInt(v1[i]), v2[i]))
					.toArray(ResultEntry[]::new);

			Sorter sorter = new AsciiSorter(0, Sorter.SIGN_ASC, substitutor,
					StringMode.DEFAULT.getCharComparator(),
					new IntegerSorter(1, Sorter.SIGN_ASC, null));

			return assertProcess(sorter, entries, false, rng, TestUtils.RUNS);
		}

		@TestFactory
		@RandomizedTest
		public Stream<DynamicTest> testUnicodeAndInteger(RandomGenerator rng) throws Exception {
			String[] v1 = {"a1", "b2", "b2", "b3", "c4", "d6", "d6", "d6"};
			long[] v2 = {4, 4, 5, 0, 1, 2, 5, 9};
			Substitutor<CharSequence> substitutor = new Substitutor<>();
			ResultEntry[] entries = IntStream.range(0, v1.length)
					.mapToObj(i -> entry(substitutor.applyAsInt(v1[i]), v2[i]))
					.toArray(ResultEntry[]::new);

			Sorter sorter = new UnicodeSorter(0, Sorter.SIGN_ASC, substitutor,
					StringMode.DEFAULT.getCodePointComparator(),
					new IntegerSorter(1, Sorter.SIGN_ASC, null));

			return assertProcess(sorter, entries, false, rng, TestUtils.RUNS);
		}
	}
}
