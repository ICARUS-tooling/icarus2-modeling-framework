/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.query.api.engine.matcher.mark.Position.Fixed;
import de.ims.icarus2.query.api.engine.matcher.mark.Position.Inverse;
import de.ims.icarus2.query.api.engine.matcher.mark.Position.Relative;

/**
 * @author Markus Gärtner
 *
 */
class PositionTest {

	@Nested
	class ForFactoryMethod {

		@Test
		void testInvalidFixed() {
			assertThatIllegalArgumentException().isThrownBy(
					() -> Position.of(Integer.valueOf(0)));

		}

		@ParameterizedTest
		@ValueSource(ints = {1, 2, 10_000})
		void testFixed(int value) {
			Position pos = Position.of(Integer.valueOf(value));
			assertThat(pos).isInstanceOf(Fixed.class);
		}

		@ParameterizedTest
		@ValueSource(ints = {-1, -10_000})
		void testInverse(int value) {
			Position pos = Position.of(Integer.valueOf(value));
			assertThat(pos).isInstanceOf(Inverse.class);
		}

		@ParameterizedTest
		@ValueSource(doubles = {-0.9, 0.1, 0.9, 0.5, -0.5})
		void testRelative(double value) {
			Position pos = Position.of(Double.valueOf(value));
			assertThat(pos).isInstanceOf(Relative.class);
		}

		@ParameterizedTest
		@ValueSource(floats = {-0.9f, 0.1f, 0.9f, 0.5f, -0.5f})
		void testRelativeFloat(float value) {
			Position pos = Position.of(Float.valueOf(value));
			assertThat(pos).isInstanceOf(Relative.class);
		}

		@ParameterizedTest
		@ValueSource(doubles = {0.0, 1.0, -1.0, -1.01, 1.01, 100.5, -100.6})
		void testInvalidRelative(double value) {
			assertThatIllegalArgumentException().isThrownBy(
					() -> Position.of(Double.valueOf(value)));
		}
	}

	@Nested
	class AsPosition {

		protected int transform(Position pos, int size) { return pos.asPosition(size); }

		@ParameterizedTest
		@ValueSource(ints = {1, 2, 10, 10_000})
		void testFixed(int value) {
			Position pos = Position.of(Integer.valueOf(value));
			int expected = value-1;
			// Minimum assertion
			assertThat(transform(pos, 1)).isEqualTo(expected);
			// Make sure large size has no impact
			assertThat(transform(pos, Integer.MAX_VALUE)).isEqualTo(expected);
		}

		@ParameterizedTest
		@ValueSource(ints = {-1, -10, -10_000})
		void testReverse(int value) {
			Position pos = Position.of(Integer.valueOf(value));

			int offset = Math.abs(value);
			// Minimum assertion
			int size1 = offset;
			assertThat(transform(pos, size1)).isEqualTo(size1-offset);
			// Make sure large size has no impact
			int size2 = 10_000_000;
			assertThat(transform(pos, size2)).isEqualTo(size2-offset);
		}

		@ParameterizedTest(name="size= {1}, val= {0}, expected= {2}")
		@CsvSource({
			"10, 0.5, 4",
			"10, 0.1, 0",
			"10, 0.05, 0",

			"100, 0.5, 49",
			"100, 0.1, 9",
			"100, 0.11, 10",
			"100, 0.001, 0",
			"100, 0.99, 98",

			"10, -0.5, 4",
			"10, -0.1, 8",
			"10, -0.05, 9",
			"10, -0.09, 8",
			"10, -0.15, 8",
			"10, -0.16, 7",

			"100, -0.5, 49",
			"100, -0.1, 89",
			"100, -0.11, 88",
			"100, -0.99, 0",
			"100, -0.001, 99",
		})
		void testRelative(int size, double value, int translated) {
			Position pos = Position.of(Double.valueOf(value));

			assertThat(transform(pos, size)).isEqualTo(translated);
		}
	}

	@Nested
	class AsLowerBoundInclusive extends AsPosition {

		@Override
		protected int transform(Position pos, int size) { return pos.asLowerBound(size, true); }

		@Override
		@ParameterizedTest(name="size= {1}, val= {0}, expected= {2}")
		@CsvSource({
			"10, 0.5,  4",
			"10, 0.04, 0",
			"10, 0.05, 0",
			"10, 0.09, 0",
			"10, 0.1,  0",
			"10, 0.11, 1",
			"10, 0.14, 1",
			"10, 0.15, 1",
			"10, 0.19, 1",
			"10, 0.2,  1",
			"10, 0.24, 2",

			"100, 0.5, 49",
			"100, 0.1, 9",
			"100, 0.11, 10",
			"100, 0.001, 0",
			"100, 0.99, 98",
			"100, 0.999, 99",

			"10, -0.5, 4",
			"10, -0.1, 8",
			"10, -0.05, 9",
			"10, -0.09, 9",
			"10, -0.15, 8",
			"10, -0.16, 8",

			"100, -0.5, 49",
			"100, -0.1, 89",
			"100, -0.11, 88",
			"100, -0.99, 1",
			"100, -0.001, 99",
		})
		void testRelative(int size, double value, int translated) {
			Position pos = Position.of(Double.valueOf(value));

			assertThat(transform(pos, size)).isEqualTo(translated);
		}
	}

	@Nested
	class AsUpperBoundInclusive extends AsPosition {

		@Override
		protected int transform(Position pos, int size) { return pos.asUpperBound(size, true); }

		@Override
		@ParameterizedTest(name="size= {1}, val= {0}, expected= {2}")
		@CsvSource({
			"10, 0.5,  4",
			"10, 0.04, -1",
			"10, 0.05, -1",
			"10, 0.09, -1",
			"10, 0.1,  0",
			"10, 0.11, 0",
			"10, 0.14, 0",
			"10, 0.15, 0",
			"10, 0.19, 0",
			"10, 0.2,  1",
			"10, 0.24, 1",

			"100, 0.5, 49",
			"100, 0.1, 9",
			"100, 0.11, 10",
			"100, 0.001, -1",
			"100, 0.99, 98",
			"100, 0.999, 98",

			"10, -0.5, 4",
			"10, -0.1, 8",
			"10, -0.05, 8",
			"10, -0.09, 8",
			"10, -0.15, 7",
			"10, -0.16, 7",

			"100, -0.5, 49",
			"100, -0.1, 89",
			"100, -0.11, 88",
			"100, -0.99, 0",
			"100, -0.001, 98",
		})
		void testRelative(int size, double value, int translated) {
			Position pos = Position.of(Double.valueOf(value));

			assertThat(transform(pos, size)).isEqualTo(translated);
		}
	}

	@Nested
	class AsLowerBoundExclusive extends AsPosition {

		@Override
		protected int transform(Position pos, int size) { return pos.asLowerBound(size, false); }

		@Override
		@ParameterizedTest
		@ValueSource(ints = {1, 2, 10, 10_000})
		void testFixed(int value) {
			Position pos = Position.of(Integer.valueOf(value));
			int expected = value;
			// Minimum assertion
			assertThat(transform(pos, 1)).isEqualTo(expected);
			// Make sure large size has no impact
			assertThat(transform(pos, Integer.MAX_VALUE)).isEqualTo(expected);
		}

		@Override
		@ParameterizedTest
		@ValueSource(ints = {-1, -10, -10_000})
		void testReverse(int value) {
			Position pos = Position.of(Integer.valueOf(value));

			int offset = Math.abs(value);
			// Minimum assertion
			int size1 = offset;
			assertThat(transform(pos, size1)).isEqualTo(size1-offset+1);
			// Make sure large size has no impact
			int size2 = 10_000_000;
			assertThat(transform(pos, size2)).isEqualTo(size2-offset+1);
		}

		@Override
		@ParameterizedTest(name="size= {1}, val= {0}, expected= {2}")
		@CsvSource({
			"10, 0.5,  5",
			"10, 0.04, 1",
			"10, 0.05, 1",
			"10, 0.09, 1",
			"10, 0.1,  1",
			"10, 0.11, 1",
			"10, 0.14, 1",
			"10, 0.15, 1",
			"10, 0.19, 1",
			"10, 0.2,  2",
			"10, 0.24, 2",

			"100, 0.5, 50",
			"100, 0.1, 10",
			"100, 0.11, 11",
			"100, 0.001, 1",
			"100, 0.99, 99",
			"100, 0.999, 99",

			"10, -0.5, 5",
			"10, -0.1, 9",
			"10, -0.05, 9",
			"10, -0.09, 9",
			"10, -0.15, 8",
			"10, -0.16, 8",

			"100, -0.5, 50",
			"100, -0.1, 90",
			"100, -0.11, 89",
			"100, -0.99, 1",
			"100, -0.001, 99",
		})
		void testRelative(int size, double value, int translated) {
			Position pos = Position.of(Double.valueOf(value));

			assertThat(transform(pos, size)).isEqualTo(translated);
		}
	}

	@Nested
	class AsUpperBoundExclusive extends AsPosition {

		@Override
		protected int transform(Position pos, int size) { return pos.asUpperBound(size, false); }

		@Override
		@ParameterizedTest
		@ValueSource(ints = {1, 2, 10, 10_000})
		void testFixed(int value) {
			Position pos = Position.of(Integer.valueOf(value));
			int expected = value-2;
			// Minimum assertion
			assertThat(transform(pos, 1)).isEqualTo(expected);
			// Make sure large size has no impact
			assertThat(transform(pos, Integer.MAX_VALUE)).isEqualTo(expected);
		}

		@Override
		@ParameterizedTest
		@ValueSource(ints = {-1, -10, -10_000})
		void testReverse(int value) {
			Position pos = Position.of(Integer.valueOf(value));

			int offset = Math.abs(value);
			// Minimum assertion
			int size1 = offset;
			assertThat(transform(pos, size1)).isEqualTo(size1-offset-1);
			// Make sure large size has no impact
			int size2 = 10_000_000;
			assertThat(transform(pos, size2)).isEqualTo(size2-offset-1);
		}

		@Override
		@ParameterizedTest(name="size= {1}, val= {0}, expected= {2}")
		@CsvSource({
			"10, 0.5,  3",
			"10, 0.04, -1",
			"10, 0.05, -1",
			"10, 0.09, -1",
			"10, 0.1,  -1",
			"10, 0.11, 0",
			"10, 0.14, 0",
			"10, 0.15, 0",
			"10, 0.19, 0",
			"10, 0.2,  0",
			"10, 0.24, 1",

			"100, 0.5, 48",
			"100, 0.1, 8",
			"100, 0.11, 9",
			"100, 0.001, -1",
			"100, 0.99, 97",
			"100, 0.999, 98",

			"10, -0.5, 3",
			"10, -0.1, 7",
			"10, -0.05, 8",
			"10, -0.09, 8",
			"10, -0.15, 7",
			"10, -0.16, 7",

			"100, -0.5, 48",
			"100, -0.1, 88",
			"100, -0.11, 87",
			"100, -0.99, 0",
			"100, -0.001, 98",
		})
		void testRelative(int size, double value, int translated) {
			Position pos = Position.of(Double.valueOf(value));

			assertThat(transform(pos, size)).isEqualTo(translated);
		}
	}
}
