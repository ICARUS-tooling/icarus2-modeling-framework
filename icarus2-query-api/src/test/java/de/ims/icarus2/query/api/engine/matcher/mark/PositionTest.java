/**
 *
 */
package de.ims.icarus2.query.api.engine.matcher.mark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Nested;
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

		@ParameterizedTest
		@ValueSource(ints = {0, 1, 10_000})
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
		@ValueSource(doubles = {-1.0, 0.0, 1.0, 0.5, -0.5})
		void testRelative(double value) {
			Position pos = Position.of(Double.valueOf(value));
			assertThat(pos).isInstanceOf(Relative.class);
		}

		@ParameterizedTest
		@ValueSource(floats = {-1.0f, 0.0f, 1.0f, 0.5f, -0.5f})
		void testRelativeFloat(float value) {
			Position pos = Position.of(Float.valueOf(value));
			assertThat(pos).isInstanceOf(Relative.class);
		}

		@ParameterizedTest
		@ValueSource(doubles = {-1.01, 1.01, 100.5, -100.6})
		void testInvalidRelative(double value) {
			assertThatIllegalArgumentException().isThrownBy(
					() -> Position.of(Double.valueOf(value)));
		}
	}

	@Nested
	class ForTranslation {

		@ParameterizedTest
		@ValueSource(ints = {0, 1, 10, 10_000})
		void testFixed(int value) {
			Position pos = Position.of(Integer.valueOf(value));

			// Minimum assertion
			assertThat(pos.translate(1)).isEqualTo(value);
			// Make sure large size has no impact
			assertThat(pos.translate(Integer.MAX_VALUE)).isEqualTo(value);
		}

		@ParameterizedTest
		@ValueSource(ints = {-1, -10, -10_000})
		void testReverse(int value) {
			Position pos = Position.of(Integer.valueOf(value));

			int offset = Math.abs(value);
			// Minimum assertion
			int size1 = offset;
			assertThat(pos.translate(size1)).isEqualTo(size1-offset);
			// Make sure large size has no impact
			int size2 = 10_000_000;
			assertThat(pos.translate(size2)).isEqualTo(size2-offset);
		}

		@ParameterizedTest(name="size= {1}, val= {0}, expected= {2}")
		@CsvSource({
			"10, 0.5, 5",
			"10, 0.1, 1",
			"10, 0.05, 0",

			"100, 0.5, 50",
			"100, 0.1, 10",
			"100, 0.11, 11",
			"100, 0.001, 0",
			"100, 0.99, 99",

			"10, -0.5, 5",
			"10, -0.1, 9",
			"10, -0.15, 8",

			"100, -0.5, 50",
			"100, -0.1, 90",
			"100, -0.11, 89",
			"100, -0.99, 1",
			"100, -0.001, 99",
		})
		void testRelative(int size, double value, int translated) {
			Position pos = Position.of(Double.valueOf(value));

			assertThat(pos.translate(size)).isEqualTo(translated);
		}
	}
}
