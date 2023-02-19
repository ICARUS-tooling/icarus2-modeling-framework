/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.lang.Primitives.strictToInt;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;

/**
 * A match encapsulates the information from a successful query evaluation on a single lane.
 * Note that matches are designed very lightweight and only carry numerical index and mapping
 * data that requires external resources to properly interpret.
 *
 * @author Markus Gärtner
 *
 */
public interface Match extends MatchSource {

	/**
	 * Provides a default method for creating a simple string representation of
	 * a normal {@link Match}.
	 * <p>
	 * {lane index: m_node->m_index, }
	 *
	 * @param match
	 * @return
	 */
	public static String toString(Match match) {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		sb.append(match.getLane());
		sb.append(match.getIndex());
		sb.append(": ");
		final int size = match.getMapCount();
		for (int i = 0; i < size; i++) {
			if(i>0) {
				sb.append(", ");
			}
			sb.append(match.getNode(i))
				.append("->")
				.append(match.getIndex(i));
		}
		sb.append('}');
		return sb.toString();
	}

	public static boolean matchesEqual(Match m1, Match m2) {
		if(m1.getType()!=m2.getType())
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Cannot compare matches of different type");
		if(m1.getIndex()!=m2.getIndex())
			return false;
		if(m1.getMapCount()!=m2.getMapCount())
			return false;
		for (int i = m1.getMapCount()-1; i >= 0; i--) {
			if(m1.getIndex(i)!=m2.getIndex(i))
				return false;
			if(m1.getNode(i)!=m2.getNode(i))
				return false;
		}
		return true;
	}

	public static int compareMatches(Match m1, Match m2) {
		if(m1.getType()!=m2.getType())
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Cannot compare matches of different type");
		if(m1.getIndex()!=m2.getIndex())
			return strictToInt(m1.getIndex()-m2.getIndex());
		if(m1.getMapCount()!=m2.getMapCount())
			return m1.getMapCount()-m2.getMapCount();
		for (int i = m1.getMapCount()-1; i >= 0; i--) {
			if(m1.getIndex(i)!=m2.getIndex(i))
				return m1.getIndex(i)-m2.getIndex(i);
			if(m1.getNode(i)!=m2.getNode(i))
				return m1.getNode(i)-m2.getNode(i);
		}
		return 0;
	}

	/** Returns the index of the associated lane.
	 * @see MappingContext#getLane(int) */
	default int getLane() {
		return 0;
	}
	/** Get the global index of the container this match refers to. */
	long getIndex();
	/** Returns the number of mappings inside this match */
	int getMapCount();
	/** Fetch the node id for the mapping at given index */
	int getNode(int index);
	/** Fetch the positional index for the mapping at given index */
	int getIndex(int index);

	/** The default implementation returns this {@link Match} object. */
	@Override
	default Match toMatch() { return this; }

	/** The default implementation returns {@link MatchType#SINGLE}. */
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

		@Override
		default int getLane() {
			return getCurrentLane();
		}

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
