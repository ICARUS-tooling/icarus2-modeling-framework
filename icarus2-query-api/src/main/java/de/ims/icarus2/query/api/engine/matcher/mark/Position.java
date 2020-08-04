/**
 *
 */
package de.ims.icarus2.query.api.engine.matcher.mark;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * Helper to abstract away from the various possible combinations of
 * absolute, relative and inverse index values possible for marker
 * boundaries in IQL.
 *
 * @author Markus Gärtner
 *
 */
interface Position {

	public static Position of(Number num) {
		requireNonNull(num);
		Class<?> cls = num.getClass();

		// Only handle explicit floating point types special
		if(cls==Float.class || cls==Double.class) {
			double value = num.doubleValue();
			if(value<0.0) {
				value += 1.0;
			}
			return new Relative(value);
		}

		int value = num.intValue();
		if(value < 0) {
			return new Inverse(value);
		}

		return new Fixed(value);
	}

	/** Implements absolute and fixed indices. */
	final class Fixed implements Position {

		private final int value;

		Fixed(int value) {
			checkArgument("Value must not be negative", value>=0);
			this.value = value;
		}

		@Override
		public int translate(int size) { return value; }
	}

	/** Implements absolute reverse indices. */
	final class Inverse implements Position {

		private final int value;

		Inverse(int value) {
			checkArgument("Value must be negative", value<0);
			this.value = value;
		}

		@Override
		public int translate(int size) { return size+value; }
	}

	/** Models relative indices and does not support reverse indices. Use value+1.0 instead. */
	final class Relative implements Position {

		private final double value;

		Relative(double value) {
			checkArgument("Value must be between 0 and 1 (both inclusive)",
					value>=0.0 && value<=1.0);
			this.value = value;
		}

		@Override
		public int translate(int size) { return (int)Math.floor(size * value); }
	}

	/** Translate the internally stored value into an actual index within the size bounds. */
	int translate(int size);
}