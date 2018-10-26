/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.driver.indices;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;

import java.sql.ResultSet;
import java.util.Comparator;
import java.util.PrimitiveIterator;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.util.IcarusUtils;


/**
 * Models an arbitrary collection of {@code long} index values. Values in the collection
 * can either appear in random order or be sorted!
 * The latter situation can be exploited to do easy checks for continuous collections
 * of indices:<br>
 * <i>Let i_0 be the first index in the set and i_n the last, with the set holding
 * n+1 indices, then the collection of indices is continuous, if and only if the
 * difference i_n-i_0 is exactly n</i>
 * <p>
 * Note that often consuming code requires an {@link IndexSet} instance to be sorted
 * in order to guarantee certain computational qualities. In that case the respective
 * method should document that requirement and ensure to check the index set (array)
 * arguments, throwing {@link ModelErrorCode#MODEL_UNSORTED_INDEX_SET} when being presented
 * with unsorted indices.
 *
 *
 * When {@code IndexSet} instances occur in an array and the method they are being
 * used for refers to this specification, they have to be sorted according to
 * {@link #INDEX_SET_SORTER}.
 *
 * @author Markus Gärtner
 *
 */
public interface IndexSet {

	//FIXME add a final UUID field that captures information about the source state (like generation id etc) so that client code can verify if a given index set is referencing outdated elements
	//FIXME maybe add mechanics to allow one-time forward-only implementations

	/**
	 * Assumes that {@link IndexSet} instances passed to the {@link Comparator#compare(Object, Object) compare}
	 * method are {@link IndexSet#isSorted() sorted}!
	 */
	public static final Comparator<IndexSet> INDEX_SET_SORTER =
			(o1, o2) -> {
				int result = Long.compare(o1.firstIndex(), o2.firstIndex());
				if(result==0) {
					result = Long.compare(o1.lastIndex(), o2.lastIndex());
				}
				return result;
			};


	public static final Comparator<IndexSet> INDEX_SET_SIZE_SORTER =
			(o1, o2) -> Integer.compare(o1.size(), o2.size());

	/**
	 * Special return value for the {@link #size()} method indicating that
	 * the implementation does not know about the total number of entries.
	 */
	public static final int UNKNOWN_SIZE = IcarusUtils.UNSET_INT;

	/**
	 * Returns the number of index values in this set.
	 * <p>
	 * If the implementation does not know about the number of entries
	 * then it is allowed to return {@link #UNKNOWN_SIZE}. Note however,
	 * that this greatly devalues the robustness of certain optimization
	 * facilities! Providing an index set of unknown size should only be
	 * done when it is planned to only access the entries in a stream
	 * like fashion.
	 */
	int size();

	long indexAt(int index);

	default long firstIndex() {
		return indexAt(0);
	}

	default long lastIndex() {
		return indexAt(size()-1);
	}

	IndexValueType getIndexValueType();

	// SORTING

	/**
	 * Returns {@code true} in case this index set is already sorted.
	 *
	 * @return
	 */
	boolean isSorted();

	/**
	 * Sorts the content of this index set and returns {@code true} if successful.
	 *
	 * @return
	 */
	boolean sort();

	// EXPORT

	/**
	 * Copies data from this {@code IndexSet} into a target {@code buffer} array.
	 *
	 * @see #export(int, int, byte[], int)
	 */
	default void export(byte[] buffer, int offset) {
		export(0, size(), buffer, offset);
	}

	/**
	 * Copies data from this {@code IndexSet} into a target {@code buffer} array.
	 * @param beginIndex position of first index to be copied, inclusive
	 * @param endIndex position of last index to be copied, exclusive
	 * @param buffer target array to copy indices into
	 * @param offset position of first insert into target buffer
	 */
	default void export(int beginIndex, int endIndex, byte[] buffer, int offset) {
		for(int i=beginIndex; i<endIndex; i++) {
			buffer[offset++] = (byte) indexAt(i);
		}
	}

	/**
	 * Copies data from this {@code IndexSet} into a target {@code buffer} array.
	 *
	 * @see #export(int, int, short[], int)
	 */
	default void export(short[] buffer, int offset) {
		export(0, size(), buffer, offset);
	}

	/**
	 * Copies data from this {@code IndexSet} into a target {@code buffer} array.
	 * @param beginIndex position of first index to be copied, inclusive
	 * @param endIndex position of last index to be copied, exclusive
	 * @param buffer target array to copy indices into
	 * @param offset position of first insert into target buffer
	 */
	default void export(int beginIndex, int endIndex, short[] buffer, int offset) {
		for(int i=beginIndex; i<endIndex; i++) {
			buffer[offset++] = (short) indexAt(i);
		}
	}

	/**
	 * Copies data from this {@code IndexSet} into a target {@code buffer} array.
	 *
	 * @see #export(int, int, int[], int)
	 */
	default void export(int[] buffer, int offset) {
		export(0, size(), buffer, offset);
	}

	/**
	 * Copies data from this {@code IndexSet} into a target {@code buffer} array.
	 * @param beginIndex position of first index to be copied, inclusive
	 * @param endIndex position of last index to be copied, exclusive
	 * @param buffer target array to copy indices into
	 * @param offset position of first insert into target buffer
	 */
	default void export(int beginIndex, int endIndex, int[] buffer, int offset) {
		for(int i=beginIndex; i<endIndex; i++) {
			buffer[offset++] = (int) indexAt(i);
		}
	}

	/**
	 * Copies data from this {@code IndexSet} into a target {@code buffer} array.
	 *
	 * @see #export(int, int, long[], int)
	 */
	default void export(long[] buffer, int offset) {
		export(0, size(), buffer, offset);
	}

	/**
	 * Copies data from this {@code IndexSet} into a target {@code buffer} array.
	 * @param beginIndex position of first index to be copied, inclusive
	 * @param endIndex position of last index to be copied, exclusive
	 * @param buffer target array to copy indices into
	 * @param offset position of first insert into target buffer
	 */
	default void export(int beginIndex, int endIndex, long[] buffer, int offset) {
		for(int i=beginIndex; i<endIndex; i++) {
			buffer[offset++] = indexAt(i);
		}
	}

	// TRASVERSAL

	default void forEachIndex(LongConsumer action) {
		forEachIndex(action, 0, size());
	}

	/**
	 *
	 * @param action
	 * @param beginIndex position of first index to apply the given action to, inclusive
	 * @param endIndex position of last index to apply the given action to, exclusive
	 */
	default void forEachIndex(LongConsumer action, int beginIndex, int endIndex) {
		for(int i=beginIndex; i<endIndex; i++) {
			action.accept(indexAt(i));
		}
	}

	default void forEachIndex(IntConsumer action) {
		forEachIndex(action, 0, size());
	}

	/**
	 *
	 * @param action
	 * @param beginIndex position of first index to apply the given action to, inclusive
	 * @param endIndex position of last index to apply the given action to, exclusive
	 *
	 * @throws ModelException in case this set contains values that exceed integer space
	 */
	default void forEachIndex(IntConsumer action, int beginIndex, int endIndex) {
		if(!IndexValueType.INTEGER.isValidSubstitute(getIndexValueType()))
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Cannot serve IntConsumer - index set contains values beyond integer space");

		for(int i=beginIndex; i<endIndex; i++) {
			action.accept((int) indexAt(i));
		}
	}

	default void forEachEntry(LongBinaryOperator action) {
		forEachEntry(action, 0, size());
	}

	default void forEachEntry(LongBinaryOperator action, int beginIndex, int endIndex) {

		for(int i=beginIndex; i<endIndex; i++) {
			action.applyAsLong(i, indexAt(i));
		}
	}

	default void forEachEntry(IntBinaryOperator action) {
		forEachEntry(action, 0, size());
	}

	default void forEachEntry(IntBinaryOperator action, int beginIndex, int endIndex) {
		if(!IndexValueType.INTEGER.isValidSubstitute(getIndexValueType()))
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Cannot serve IntConsumer - index set contains values beyond integer space");

		for(int i=beginIndex; i<endIndex; i++) {
			action.applyAsInt(i, (int)indexAt(i));
		}
	}

	// TRANSFORMATION

	/**
	 * Splits the current set of indices so that each new subset contains at most
	 * the given number of indices.
	 *
	 * @param chunkSize
	 * @return
	 *
	 * @see #subSet(int, int)
	 */
	default IndexSet[] split(int chunkSize) {
		checkArgument("Chunk size must be positive", chunkSize>0);

		int size = size();

		checkState("Cannot split index set of unknown size", size!=UNKNOWN_SIZE);

		if(chunkSize>=size) {
			return IndexUtils.wrap(this);
		}

		int chunks = (int)Math.ceil((double)size/chunkSize);

		if(chunks>IcarusUtils.MAX_INTEGER_INDEX)
			throw new ModelException(GlobalErrorCode.INDEX_OVERFLOW, "Cannot create array of size: "+chunks); //$NON-NLS-1$

		IndexSet[] result = new IndexSet[(int) chunks];

		int fromIndex = 0;
		int toIndex;
		for(int i=0; i<chunks; i++) {
			toIndex = Math.min(fromIndex+chunkSize, size-1);
			result[i] = subSet(fromIndex, toIndex);
			fromIndex = toIndex+1;
		}

		return result;
	}

	/**
	 *
	 * @param fromIndex first index within this index set the new one is meant to contain (inclusive)
	 * @param toIndex last index within this index set the new one is meant to contain (inclusive)
	 * @return
	 */
	IndexSet subSet(int fromIndex, int toIndex);

	/**
	 * Return an {@code IndexSet} object that describes the exact same set of index values
	 * as this one, but is disconnected from any implementation specific shared storage.
	 * If an implementation is not relying on shared storage or cannot be disconnected from it,
	 * this method should just return the current {@code IndexSet} itself.
	 */
	IndexSet externalize();

	// FEATURE FLAGS

	/**
	 * General hint that the implementation is able to sort its content.
	 * Note that this information is kind of redundant since the {@link #sort()} method
	 * already expresses this information, but more in a post-condition kind of way.
	 */
	public static final int FEATURE_CAN_SORT = (1 << 0);

	/**
	 * Indicates whether or not the implementation supports exporting parts of it via any
	 * of the following method:
	 * <ul>
	 * <li>{@link #split(int)}</li>
	 * <li>{@link #subSet(int, int)}</li>
	 * </ul>
	 * Implementations are free to
	 */
	public static final int FEATURE_CAN_EXPORT = (1 << 1);

	/**
	 * Signals that this implementation is not able to provide information about the number
	 * of entries it holds. This can be the case when the index set is a wrapper around some
	 * foreign storage like the {@link ResultSet result} of a SQL query.
	 */
	public static final int FEATURE_INDETERMINATE_SIZE = (1 << 2);

	/**
	 * Signals that client code cannot address entries in this set in a <i>random-access</i> pattern.
	 * The exact semantics of <i>forward-only</i> are implementation specific, but the general contract
	 * is that for any 2 consecutive calls to {@link #indexAt(int)} the {@code index} parameter passed
	 * to the second call must be greater than the one passed to the first call.
	 * Some implementations might require it to be exactly the next greater integer value, but that is
	 * generally not required and implementations should implement an internal "skip" mechanism that
	 * scrolls forward to the desired position, discarding any entries on the way.
	 * <p>
	 * Note that by declaring this feature active an implementation becomes effectively one-time usable only.
	 */
	public static final int FEATURE_CURSOR_FORWARD_ONLY = (1 << 3);

	/**
	 * Signals that the implementation is thread-safe and it can be shared across multiple threads.
	 * Per default implementations are <b>not</> required to implement synchronization measures to
	 * improve performance.
	 */
	public static final int FEATURE_THREAD_SAFE = (1 << 4);

	public static final int DEFAULT_FEATURES =
			FEATURE_CAN_SORT
			| FEATURE_CAN_EXPORT;

	default int getFeatures() {
		return DEFAULT_FEATURES;
	}

	default boolean hasFeatures(int features) {
		return (getFeatures() & features) == features;
	}

	default void checkHasFeatures(int features) {
		if(!hasFeatures(features)) {
			StringBuilder sb = new StringBuilder("Required features not available: ");

			int pos = 0;
			//TODO aggregate textual info of the missing features and use as error message!

			throw new ModelException(GlobalErrorCode.NOT_IMPLEMENTED, sb.toString());
		}
	}

	// ITERATOR SUPPORT

	/**
	 * Creates and returns a {@link PrimitiveIterator.OfLong} iterator that traverses the
	 * entire number of index values within this {@code IndexSet}.
	 *
	 * @return
	 */
	default OfLong iterator() {
		return IndexUtils.asIterator(this);
	}

	default OfLong iterator(int start) {
		return IndexUtils.asIterator(this, start);
	}

	default OfLong iterator(int start, int end) {
		return IndexUtils.asIterator(this, start, end);
	}

	// STREAM SUPPORT

	default IntStream intStream() {
		return IndexUtils.asIntStream(this);
	}

	default LongStream longStream() {
		return IndexUtils.asLongStream(this);
	}
}
