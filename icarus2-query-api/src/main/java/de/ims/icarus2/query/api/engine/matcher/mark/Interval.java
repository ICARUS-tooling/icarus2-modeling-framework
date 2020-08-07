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

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;

/**
 * Models a simple bounded interval in {@code int} space.
 * <p>
 * Note that this utility class does not define any setter methods
 * itself, as it is purely used in the context of positional
 * {@link Marker markers} to describe legal index ranges for matching.
 *
 * @author Markus Gärtner
 *
 */
public class Interval {

	public static Interval of(int value) {
		Interval iv = new Interval();
		iv.from = iv.to = value;
		return iv;
	}

	public static Interval of(int from, int to) {
		Interval iv = new Interval();
		iv.from = from;
		iv.to = to;
		return iv;
	}

	/** The first legal index */
	public int from = UNSET_INT;

	/** The last legal index */
	public int to = UNSET_INT;

	public boolean contains(int index) { return index>=from && index<=to; }

	public boolean isValid() { return from!=UNSET_INT && to!=UNSET_INT; }

	public void reset() { from = UNSET_INT; to = UNSET_INT; }

	public int size() { return to-from+1; }

	public boolean isEmpty() { return to<from; }

	/**
	 * Reduces this interval's boundaries to encompass the index intersection
	 * between this and the {@code other} interval.
	 *
	 * @return {@code true} iff this interval is not empty after intersecting
	 */
	public boolean intersect(Interval other) {
		from = Math.max(from, other.from);
		to = Math.min(to, other.to);
		return from<=to;
	}

	@Override
	public String toString() {
		return "["+from+","+to+"]";
	}

	@Override
	public int hashCode() {
		return from * to;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==this) {
			return true;
		} else if(obj instanceof Interval) {
			Interval other = (Interval)obj;
			return from==other.from && to==other.to;
		}
		return false;
	}
}
