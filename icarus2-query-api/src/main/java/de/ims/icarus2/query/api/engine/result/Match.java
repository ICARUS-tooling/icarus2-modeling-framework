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
package de.ims.icarus2.query.api.engine.result;

/**
 * @author Markus Gärtner
 *
 */
public interface Match extends MatchSource {

	/** Get the global index of the container this match refers to. */
	long getIndex();
	/** Returns the number of mappings inside this match */
	int getMapCount();
	/** Fetch the node id for the mapping at given index */
	int getNode(int index);
	/** Fetch the positional index for the mapping at given index */
	int getIndex(int index);

	@Override
	default Match toMatch() { return this; }

	default MatchType getType() { return MatchType.SINGLE; }

	enum MatchType {
		SINGLE,
		MULTI,
		;
	}

	/**
	 * Implements an aggregated match that represents multiple matches from
	 * different lanes.
	 * To navigate the multiple matches contained a cursor-style mechanic is
	 * provided via {@link #moveToLane(int)}.
	 * The basic {@link Match} methods (e.g {@link Match#getIndex()},
	 * {@link Match#drainTo(MatchSink)}, etc..) always refer to the match that
	 * the cursor currently points to with {@link #getCurrentLane()}.
	 * Initially the cursor will always positioned at the lane that has the
	 * index of {@code 0}.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface MultiMatch extends Match {

		/** Returns the index of the current lane */
		int getCurrentLane();

		/** Returns the total number of lanes present in this match.
		 * Note that this number is <b>always</b> equal to the number of
		 * lanes involved in the original matching process, even if for
		 * individual lanes the multi-match does not contain any mappings! */
		int getLaneCount();

		/** Moves the read cursor to the match for the specified lane. */
		void moveToLane(int index);

		/** Moves the read cursor back to the first lane. */
		default void reset() { moveToLane(0); }

		@Override
		default MatchType getType() { return MatchType.MULTI; }
	}
}
