/**
 *
 */
package de.ims.icarus2.query.api.engine.matcher.mark;

import static de.ims.icarus2.util.collections.ArrayUtils.array;
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.assertj.core.api.Assertions.assertThat;
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
		// IsAt with relative index
		"IsAt,  0.1, 10, false, 0, 0",
		"IsAt,  0.9, 10, false, 8, 8",
		"IsAt,  0.5, 10, false, 4, 4",

		//TODO
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
}
