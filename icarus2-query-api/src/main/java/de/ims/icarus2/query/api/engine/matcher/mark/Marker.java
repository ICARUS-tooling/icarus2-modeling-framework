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

/**
 * @author Markus Gärtner
 *
 */
public interface Marker {

	/** Returns the type of this marker. */
	MarkerType getType();

	/**
	 * Returns the raw name of this marker.
	 * Note that names are allowed to use case for better readability,
	 * but name-equality of markers is done ignoring case.
	 */
	String getName();

	/**
	 * Returns {@code true} iff this marker's associated set of legal indices
	 * is independent of the target sequence.
	 * <p>
	 * For instance, the {@code isInside(x, y)} marker will <b>always</b>
	 * produce the fixed interval {@code [x,y]} as legal range of indices.
	 * However, its counterpart {@code isOutside(x, y)} will produce two
	 * intervals, {@code [1, x-1]} and {@code [y+1, n]}, where the latter
	 * depends on the size {@code n} of the target sequence and so needs
	 * to be generated dynamically.
	 */
	boolean isDynamic();

	//TODO need a method to update the interval for matching

	public interface RangeMarker extends Marker {

		/** The number of intervals this marker manages */
		int intervalCount();

		/**
		 * Adjusts a selection of intervals managed by this marker and
		 * returns {@code true} iff at least a single legal index has
		 * been produced.
		 *
		 * @return {@code true} iff at least one of intervals managed by this
		 * marker is not {@code Interval#isEmpty() empty}.
		 * @param intervals buffer of intervals to adjust. This marker is
		 * only responsible for the sub-array {@code [index .. index+intervalCount()]}.
		 * @param index the starting index within the {@code intervals} array
		 * that holds the first interval managed by this marker.
		 * @param size the total number of elements available in the target sequence,
		 * guaranteed to be greater than zero.
		 */
		boolean adjust(Interval[] intervals, int index, int size);
	}
}
