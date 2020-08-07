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
package de.ims.icarus2.query.api.engine.matcher.mark;

import static de.ims.icarus2.util.collections.ArrayUtils.array;
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.function.Consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.query.api.engine.matcher.mark.Marker.RangeMarker;
import de.ims.icarus2.query.api.engine.matcher.mark.SequenceMarker.Name;
import de.ims.icarus2.test.util.convert.NumberConverter;

/**
 * @author Markus Gärtner
 *
 */
class SequenceMarkerTest {

	private static Consumer<SequenceMarker.MarkerBase> hasName(Name name) {
		return m -> assertThat(m.getRawName()).isSameAs(name);
	}

	@Nested
	class FactoryMethod {
		@Test
		void testNullName() {
			assertThatNullPointerException().isThrownBy(() -> SequenceMarker.of(null));
		}

		@Test
		void testNullArguments() {
			assertThatNullPointerException().isThrownBy(
					() -> SequenceMarker.of("IsAt", (Number[])null));
		}


		@ParameterizedTest
		@CsvSource({
			"IsAt, AT",
			"IsNotAt, NOT_AT",
			"IsAfter, AFTER",
			"IsBefore, BEFORE",
			"IsInside, INSIDE",
			"IsOutside, OUTSIDE",
			"IsFirst, FIRST",
			"IsLast, LAST",
		})
		void testConsistency(String name, Name rawName) {
			Number[] args = {_int(1), _int(2)};
			// Verify normal casing
			assertThat(SequenceMarker.of(name, args))
				.as("Default casing")
				.isInstanceOfSatisfying(SequenceMarker.MarkerBase.class, hasName(rawName));
			// Verify lower casing
			assertThat(SequenceMarker.of(name.toLowerCase(), args))
				.as("Lower casing")
				.isInstanceOfSatisfying(SequenceMarker.MarkerBase.class, hasName(rawName));
			// Verify upper casing
			assertThat(SequenceMarker.of(name.toUpperCase(), args))
				.as("Upper casing")
				.isInstanceOfSatisfying(SequenceMarker.MarkerBase.class, hasName(rawName));
		}
	}

	@Nested
	class UnsupportedRelativePositions {
		@Test
		void onIsAt() {
			assertThatIllegalArgumentException().isThrownBy(
					() -> SequenceMarker.of("IsAt", Double.valueOf(0.5)));
		}
		@Test
		void onIsNotAt() {
			assertThatIllegalArgumentException().isThrownBy(
					() -> SequenceMarker.of("IsNotAt", Double.valueOf(0.5)));
		}
	}

	@ParameterizedTest
	@ValueSource(ints={1, 10, 100, Integer.MAX_VALUE})
	void testFirst(int size) {
		RangeMarker marker = SequenceMarker.of("IsFirst");
		Interval interval = new Interval();

		assertThat(marker.adjust(array(interval), 0, size)).isTrue();
		assertThat(interval.from).isEqualTo(0);
		assertThat(interval.to).isEqualTo(0);
	}

	@ParameterizedTest
	@ValueSource(ints={1, 10, 100, Integer.MAX_VALUE})
	void testLast(int size) {
		RangeMarker marker = SequenceMarker.of("IsLast");
		Interval interval = new Interval();

		assertThat(marker.adjust(array(interval), 0, size)).isTrue();
		int expected = size-1;
		assertThat(interval.from).isEqualTo(expected);
		assertThat(interval.to).isEqualTo(expected);
	}

	@ParameterizedTest(name="{0}({1}) on {2} elements gives interval [{4},{5}], empty={3}")
	@CsvSource({
		// IsAt with fixed index
		"IsAt,  1, 10, false, 0, 0",
		"IsAt,  4, 10, false, 3, 3",
		"IsAt, 10, 10, false, 9, 9",
		// IsAt with reverse index
		"IsAt,  -1, 10, false, 9, 9",
		"IsAt,  -4, 10, false, 6, 6",
		"IsAt, -10, 10, false, 0, 0",

		// IsAfter with fixed index
		"IsAfter,  1, 10, false, 1, 9",
		"IsAfter,  5, 10, false, 5, 9",
		"IsAfter,  9, 10, false, 9, 9",
		"IsAfter,  10, 10, true, 10, 9",
		// IsAfter with reverse index
		"IsAfter,  -1, 10, true, 10, 9",
		"IsAfter,  -2, 10, false, 9, 9",
		"IsAfter,  -5, 10, false, 6, 9",
		"IsAfter,  -10, 10, false, 1, 9",
		// IsAfter with relative index
		"IsAfter,  0.1, 10, false, 1, 9",
		"IsAfter,  0.5, 10, false, 5, 9",
		"IsAfter,  0.9, 10, false, 9, 9",
		"IsAfter,  0.95, 10, false, 9, 9",
		// IsAfter with relative reverse index
		"IsAfter,  -0.05, 10, false, 9, 9",
		"IsAfter,  -0.1, 10, false, 9, 9",
		"IsAfter,  -0.5, 10, false, 5, 9",
		"IsAfter,  -0.9, 10, false, 1, 9",
		"IsAfter,  -0.95, 10, false, 1, 9",

		// IsBefore with fixed index
		"IsBefore,  1, 10, true, 0, -1",
		"IsBefore,  5, 10, false, 0, 3",
		"IsBefore,  9, 10, false, 0, 7",
		"IsBefore,  10, 10, false, 0, 8",
		// IsBefore with reverse index
		"IsBefore,  -1, 10, false, 0, 8",
		"IsBefore,  -2, 10, false, 0, 7",
		"IsBefore,  -5, 10, false, 0, 4",
		"IsBefore,  -10, 10, true, 0, -1",
		// IsBefore with relative index
		"IsBefore,  0.1, 10, true, 0, -1",
		"IsBefore,  0.5, 10, false, 0, 3",
		"IsBefore,  0.9, 10, false, 0, 7",
		"IsBefore,  0.95, 10, false, 0, 8",
		// IsBefore with relative reverse index
		"IsBefore,  -0.05, 10, false, 0, 8",
		"IsBefore,  -0.1, 10, false, 0, 7",
		"IsBefore,  -0.5, 10, false, 0, 3",
		"IsBefore,  -0.9, 10, true, 0, -1",
		"IsBefore,  -0.95, 10, true, 0, -1",
	})
	@DisplayName("1 arg -> 1 interval")
	void testSingleIntervalMarkersWith1Arg(String name,
			@ConvertWith(NumberConverter.class) Number arg,
			int size, boolean empty, int begin, int end) {
		RangeMarker marker = SequenceMarker.of(name, arg);
		Interval interval = new Interval();
		assertThat(marker.adjust(array(interval), 0, size)).isNotEqualTo(empty);
		assertThat(interval.from).isEqualTo(begin);
		assertThat(interval.to).isEqualTo(end);
	}

	@ParameterizedTest(name="{0}({1}) on {2} elements gives intervals [{4},{5}] and [{6},{7}], empty={3}")
	@CsvSource({
		// IsNotAt with fixed index
		"IsNotAt, 1, 10, false, 0, -1, 1, 9",
		"IsNotAt, 5, 10, false, 0, 3, 5, 9",
		"IsNotAt, 10, 10, false, 0, 8, 10, 9",
		// IsNotAt with reverse index
		"IsNotAt, -1, 10, false, 0, 8, 10, 9",
		"IsNotAt, -5, 10, false, 0, 4, 6, 9",
		"IsNotAt, -10, 10, false, 0, -1, 1, 9",
	})
	@DisplayName("1 arg -> 2 intervals")
	void testDualIntervalMarkersWith1Arg(String name,
			@ConvertWith(NumberConverter.class) Number arg,
			int size, boolean empty, int begin1, int end1, int begin2, int end2) {
		RangeMarker marker = SequenceMarker.of(name, arg);
		Interval interval1 = new Interval();
		Interval interval2 = new Interval();
		assertThat(marker.adjust(array(interval1, interval2), 0, size)).isNotEqualTo(empty);
		assertThat(interval1.from).isEqualTo(begin1);
		assertThat(interval1.to).isEqualTo(end1);
		assertThat(interval2.from).isEqualTo(begin2);
		assertThat(interval2.to).isEqualTo(end2);
	}

	@ParameterizedTest(name="{0}({1}{2}) on {3} elements gives intervals [{5},{6}], empty={4}")
	@CsvSource({
		// IsInside with fixed indices
		"IsInside, 1, 10, 10, false, 0, 9",
		"IsInside, 3, 7, 10, false, 2, 6",
		"IsInside, 5, 5, 10, false, 4, 4",
		"IsInside, 6, 5, 10, true, 5, 4",
		// IsInside with reverse indices
		"IsInside, -10, -1, 10, false, 0, 9",
		"IsInside, -7, -3, 10, false, 3, 7",
		"IsInside, -5, -5, 10, false, 5, 5",
		"IsInside, -6, -5, 10, false, 4, 5",
		"IsInside, -5, -6, 10, true, 5, 4",
		// IsInside with relative indices
		"IsInside, 0.05, 0.95, 10, false, 0, 8",
		"IsInside, 0.1, 0.9, 10, false, 0, 8",
		"IsInside, 0.5, 0.5, 10, false, 4, 4",
		"IsInside, 0.7, 0.3, 10, true, 6, 2",
		// IsInside with relative reverse indices
		"IsInside, -0.95, -0.05, 10, false, 0, 8",
		"IsInside, -0.9, -0.1, 10, false, 0, 8",
		"IsInside, -0.5, -0.5, 10, false, 4, 4",
		"IsInside, -0.3, -0.7, 10, true, 6, 2",
		// IsInside with mixed indices
		"IsInside, 1, 0.5, 10, false, 0, 4",
		"IsInside, 1, -0.5, 10, false, 0, 4",
		"IsInside, 0.5, 10, 10, false, 4, 9",
		"IsInside, -0.5, 10, 10, false, 4, 9",
		"IsInside, -7, 0.8, 10, false, 3, 7",
		"IsInside, -7, -0.2, 10, false, 3, 7",
		"IsInside, 0.4, -3, 10, false, 3, 7",
		"IsInside, -0.6, -3, 10, false, 3, 7",
	})
	@DisplayName("2 args -> 1 interval")
	void testSingleIntervalMarkersWith2Arg(String name,
			@ConvertWith(NumberConverter.class) Number arg1, @ConvertWith(NumberConverter.class) Number arg2,
			int size, boolean empty, int begin, int end) {
		RangeMarker marker = SequenceMarker.of(name, arg1, arg2);
		Interval interval1 = new Interval();
		Interval interval2 = new Interval();
		assertThat(marker.adjust(array(interval1, interval2), 0, size)).isNotEqualTo(empty);
		assertThat(interval1.from).isEqualTo(begin);
		assertThat(interval1.to).isEqualTo(end);
	}

	@ParameterizedTest(name="{0}({1}{2}) on {3} elements gives intervals [{5},{6}] and [{7},{8}], empty={4}")
	@CsvSource({
		// IsOutside with fixed index
		"IsOutside, 1, 10, 10, true, 0, -1, 10, 9",
		//TODO
	})
	@DisplayName("2 args -> 2 intervals")
	void testDualIntervalMarkersWith2Arg(String name,
			@ConvertWith(NumberConverter.class) Number arg1, @ConvertWith(NumberConverter.class) Number arg2,
			int size, boolean empty, int begin1, int end1, int begin2, int end2) {
		RangeMarker marker = SequenceMarker.of(name, arg1, arg2);
		Interval interval1 = new Interval();
		Interval interval2 = new Interval();
		assertThat(marker.adjust(array(interval1, interval2), 0, size)).isNotEqualTo(empty);
		assertThat(interval1.from).isEqualTo(begin1);
		assertThat(interval1.to).isEqualTo(end1);
		assertThat(interval2.from).isEqualTo(begin2);
		assertThat(interval2.to).isEqualTo(end2);
	}
}
