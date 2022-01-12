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
package de.ims.icarus2.query.api.engine.matcher.mark;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
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

	/**
	 * Turns the given numerical value into an actual {@link Position} instance
	 * with the following rules:
	 * <ul>
	 * <li>if {@code num} is a floating point value, create a relative position</li>
	 * <li>if {@code num} is a negative integer value, create a reverse position</li>
	 * <li>if {@code num} is a positive integer value, decrement it by {@code 1} to
	 * translate from the 1-based IQL system, and then create a fixed position</li>
	 * </ul>
	 *
	 * @param num the raw numerical value
	 * @return
	 */
	public static Position of(Number num) {
		requireNonNull(num);
		Class<?> cls = num.getClass();

		// Only handle explicit floating point types in a special way
		if(cls==Float.class || cls==Double.class) {
			return new Relative(num.doubleValue());
		}

		int value = strictToInt(num.longValue());
		if(value < 0) {
			// Inverse values keep their raw form, as they nicely fit into 0-based space
			return new Inverse(value);
		}

		// Translate into our 0-based system
		value--;
		return new Fixed(value);
	}

	boolean isRelative();
	boolean isReverse();

	/**
	 * Translate the internally stored value into an actual index within the size bounds.
	 * @param size size of the index space, never smaller than {@code 1}
	 */
	int asPosition(int size);

	default int asLowerBound(int size, boolean inclusive) {
		int index = asPosition(size);
		if(!inclusive) {
			index++;
		}
		return index;
	}

	default int asUpperBound(int size, boolean inclusive) {
		int index = asPosition(size);
		if(!inclusive) {
			index--;
		}
		return index;
	}

	/** Implements absolute and fixed indices. */
	final class Fixed implements Position {

		private final int value;

		Fixed(int value) {
			checkArgument("Value must not be negative", value>=0);
			this.value = value;
		}

		@Override
		public int asPosition(int size) { return value; }

		@Override
		public boolean isRelative() { return false; }

		@Override
		public boolean isReverse() { return false; }
	}

	/** Implements absolute reverse indices. */
	final class Inverse implements Position {

		private final int value;

		Inverse(int value) {
			checkArgument("Value must be negative", value<0);
			this.value = value;
		}

		@Override
		public int asPosition(int size) { return size+value; }

		@Override
		public boolean isRelative() { return false; }

		@Override
		public boolean isReverse() { return true; }
	}

	/** Models relative indices and does not support reverse indices. */
	final class Relative implements Position {

		private final double value;
		private final boolean reverse;

		Relative(double value) {
			boolean reverse = false;
			if(value<0.0) {
				checkArgument("Value must be between -1 and 0 (both exclusive)",
						value>-1.0 && value<0.0);
				value += 1.0;
				reverse = true;
			} else {
				checkArgument("Value must be between 0 and 1 (both exclusive)",
						value>0.0 && value<1.0);
			}
			this.value = value;
			this.reverse = reverse;
		}

		@Override
		public boolean isRelative() { return true; }

		@Override
		public boolean isReverse() { return reverse; }

		/** Rounded integer -1, cannot go below 0 */
		@Override
		public int asPosition(int size) { return Math.max(0, strictToInt(Math.round(size * value)) - 1); }

		/** Rounded up integer -1 */
		@Override
		public int asLowerBound(int size, boolean inclusive) {
			double raw = (size * value) - 1;
			int index = (int)Math.ceil(raw);
			// Only shift for exclusive bounds when the raw value wasn't already a proper integer
			if(!inclusive && (raw==index || raw<0.0)) {
				index++;
			}
			return index;
		}

		/** Rounded down integer -1, can create invalid intervals */
		@Override
		public int asUpperBound(int size, boolean inclusive) {
			double raw = (size * value) - 1;
			int index = (int)Math.floor(raw);
			// Only shift for exclusive bounds when the raw value wasn't already a proper integer
			if(!inclusive && raw==index) {
				index--;
			}
			return index;
		}
	}
}