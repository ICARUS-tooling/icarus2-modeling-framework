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
package de.ims.icarus2.model.api.driver.indices;

import static de.ims.icarus2.model.api.driver.indices.IndexUtils.checkIndex;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.checkRangeExlusive;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.lang.Primitives.strictToByte;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static de.ims.icarus2.util.lang.Primitives.strictToShort;
import static java.util.Objects.requireNonNull;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.PrimitiveIterator;
import java.util.PrimitiveIterator.OfLong;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.function.IntBiConsumer;
import de.ims.icarus2.util.function.IntLongConsumer;
import de.ims.icarus2.util.function.LongBiPredicate;


/**
 * Models an arbitrary collection of non-negative{@code long} index values.
 * Values in the collection can either appear in random order or be sorted!
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

	/**
	 * Returns whether or not this index set contains any values.
	 * Note that for implementations that report the size to be
	 * {@link Feature#INDETERMINATE_SIZE} the return value of this
	 * method is only relevant if {@code true}.
	 * @return
	 */
	default boolean isEmpty() {
		return size()==0;
	}

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
	 * @return the number of values copied
	 *
	 * @see #export(int, int, byte[], int)
	 */
	default int export(byte[] buffer, int offset) {
		int size = size();
		export(0, size, buffer, offset);
		return size;
	}

	/**
	 * Copies data from this {@code IndexSet} into a target {@code buffer} array.
	 * @param beginIndex position of first index to be copied, inclusive
	 * @param endIndex position of last index to be copied, exclusive
	 * @param buffer target array to copy indices into
	 * @param offset position of first insert into target buffer
	 */
	default void export(int beginIndex, int endIndex, byte[] buffer, int offset) {
		requireNonNull(buffer);
		checkRangeExlusive(this, beginIndex, endIndex);
		for(int i=beginIndex; i<endIndex; i++) {
			buffer[offset++] = strictToByte(indexAt(i));
		}
	}

	/**
	 * Copies data from this {@code IndexSet} into a target {@code buffer} array.
	 * @return the number of values copied
	 *
	 * @see #export(int, int, short[], int)
	 */
	default int export(short[] buffer, int offset) {
		int size = size();
		export(0, size, buffer, offset);
		return size;
	}

	/**
	 * Copies data from this {@code IndexSet} into a target {@code buffer} array.
	 * @param beginIndex position of first index to be copied, inclusive
	 * @param endIndex position of last index to be copied, exclusive
	 * @param buffer target array to copy indices into
	 * @param offset position of first insert into target buffer
	 */
	default void export(int beginIndex, int endIndex, short[] buffer, int offset) {
		requireNonNull(buffer);
		checkRangeExlusive(this, beginIndex, endIndex);
		for(int i=beginIndex; i<endIndex; i++) {
			buffer[offset++] = strictToShort(indexAt(i));
		}
	}

	/**
	 * Copies data from this {@code IndexSet} into a target {@code buffer} array.
	 * @return the number of values copied
	 *
	 * @see #export(int, int, int[], int)
	 */
	default int export(int[] buffer, int offset) {
		int size = size();
		export(0, size, buffer, offset);
		return size;
	}

	/**
	 * Copies data from this {@code IndexSet} into a target {@code buffer} array.
	 * @param beginIndex position of first index to be copied, inclusive
	 * @param endIndex position of last index to be copied, exclusive
	 * @param buffer target array to copy indices into
	 * @param offset position of first insert into target buffer
	 */
	default void export(int beginIndex, int endIndex, int[] buffer, int offset) {
		requireNonNull(buffer);
		checkRangeExlusive(this, beginIndex, endIndex);
		for(int i=beginIndex; i<endIndex; i++) {
			buffer[offset++] = strictToInt(indexAt(i));
		}
	}

	/**
	 * Copies data from this {@code IndexSet} into a target {@code buffer} array.
	 * @return the number of values copied
	 *
	 * @see #export(int, int, long[], int)
	 */
	default int export(long[] buffer, int offset) {
		int size = size();
		export(0, size, buffer, offset);
		return size;
	}

	/**
	 * Copies data from this {@code IndexSet} into a target {@code buffer} array.
	 * @param beginIndex position of first index to be copied, inclusive
	 * @param endIndex position of last index to be copied, exclusive
	 * @param buffer target array to copy indices into
	 * @param offset position of first insert into target buffer
	 */
	default void export(int beginIndex, int endIndex, long[] buffer, int offset) {
		requireNonNull(buffer);
		checkRangeExlusive(this, beginIndex, endIndex);
		for(int i=beginIndex; i<endIndex; i++) {
			buffer[offset++] = indexAt(i);
		}
	}

	// TRAVERSAL

	/**
	 * Applies {@code action} to each index value in this set.
	 * @param action
	 */
	default void forEachIndex(LongConsumer action) {
		requireNonNull(action);
		if(!isEmpty()) {
			forEachIndex(action, 0, size());
		}
	}

	/**
	 * Applies {@code action} to each index value in the specified region of this set.
	 *
	 * @param action
	 * @param beginIndex position of first index to apply the given action to, inclusive
	 * @param endIndex position of last index to apply the given action to, exclusive
	 */
	default void forEachIndex(LongConsumer action, int beginIndex, int endIndex) {
		requireNonNull(action);
		checkRangeExlusive(this, beginIndex, endIndex);
		for(int i=beginIndex; i<endIndex; i++) {
			action.accept(indexAt(i));
		}
	}

	/**
	 * Equivalent of {@link #forEachIndex(LongConsumer)} for sets that contain
	 * only int-compatible values.
	 *
	 * @param action
	 *
	 * @see #forEachIndex(IntConsumer, int, int)
	 * @see #forEachIndex(LongConsumer)
	 */
	default void forEachIndex(IntConsumer action) {
		requireNonNull(action);
		if(!isEmpty()) {
			forEachIndex(action, 0, size());
		}
	}

	/**
	 * Equivalent of {@link #forEachIndex(LongConsumer, int, int)} for sets that contain
	 * only int-compatible values.
	 *
	 * @param action
	 * @param beginIndex position of first index to apply the given action to, inclusive
	 * @param endIndex position of last index to apply the given action to, exclusive
	 *
	 * @throws ModelException in case this set potentially contains values that exceed integer space
	 */
	default void forEachIndex(IntConsumer action, int beginIndex, int endIndex) {
		requireNonNull(action);
		checkRangeExlusive(this, beginIndex, endIndex);
		for(int i=beginIndex; i<endIndex; i++) {
			action.accept(strictToInt(indexAt(i)));
		}
	}

	/**
	 * Calls the specified {@code action} for each pair {@code <index,value_at_index>}
	 * in this set.
	 * @param action
	 */
	default void forEachEntry(IntLongConsumer action) {
		requireNonNull(action);
		if(!isEmpty()) {
			forEachEntry(action, 0, size());
		}
	}

	/**
	 * Calls the specified {@code action} for each pair {@code <index,value_at_index>}
	 * in the specified region of this set.
	 * @param action
	 */
	default void forEachEntry(IntLongConsumer action, int beginIndex, int endIndex) {
		requireNonNull(action);
		checkRangeExlusive(this, beginIndex, endIndex);
		for(int i=beginIndex; i<endIndex; i++) {
			action.accept(i, indexAt(i));
		}
	}

	default void forEachEntry(IntBiConsumer action) {
		requireNonNull(action);
		if(!isEmpty()) {
			forEachEntry(action, 0, size());
		}
	}

	/**
	 *
	 * @param action
	 * @param beginIndex
	 * @param endIndex
	 * @throws ModelException of type {@link GlobalErrorCode#ILLEGAL_STATE} if the content of
	 * this index set is not appropriate for an {@link IntBiConsumer}.
	 */
	default void forEachEntry(IntBiConsumer action, int beginIndex, int endIndex) {
		requireNonNull(action);
		checkRangeExlusive(this, beginIndex, endIndex);
		for(int i=beginIndex; i<endIndex; i++) {
			action.accept(i, strictToInt(indexAt(i)));
		}
	}

	// CHECKING

	default boolean checkIndices(LongPredicate check) {
		requireNonNull(check);
		return !isEmpty() && checkIndices(check, 0, size());
	}

	/**
	 *
	 * @param check
	 * @param beginIndex position of first index to apply the given action to, inclusive
	 * @param endIndex position of last index to apply the given action to, exclusive
	 * @return {@code true} iff the given {@code check} holds for all index values in the
	 * specified region of this set.
	 */
	default boolean checkIndices(LongPredicate check, int beginIndex, int endIndex) {
		requireNonNull(check);
		checkRangeExlusive(this, beginIndex, endIndex);
		for(int i=beginIndex; i<endIndex; i++) {
			if(!check.test(indexAt(i))) {
				return false;
			}
		}

		return true;
	}

	default boolean checkIndices(IntPredicate check) {
		requireNonNull(check);
		return !isEmpty() && checkIndices(check, 0, size());
	}

	/**
	 *
	 * @param check
	 * @param beginIndex position of first index to apply the given action to, inclusive
	 * @param endIndex position of last index to apply the given action to, exclusive
	 * @return {@code true} iff the given {@code check} holds for all index values in the
	 * specified region of this set.
	 * @throws ModelException of type {@link GlobalErrorCode#ILLEGAL_STATE} if the content of
	 * this index set is not appropriate for an {@link IntPredicate}.
	 */
	default boolean checkIndices(IntPredicate check, int beginIndex, int endIndex) {
		requireNonNull(check);
		checkRangeExlusive(this, beginIndex, endIndex);
		for(int i=beginIndex; i<endIndex; i++) {
			if(!check.test(strictToInt(indexAt(i)))) {
				return false;
			}
		}

		return true;
	}

	default boolean checkConsecutiveIndices(LongBiPredicate check) {
		requireNonNull(check);
		return !isEmpty() && checkConsecutiveIndices(check, 0, size());
	}

	/**
	 *
	 * @param check
	 * @param beginIndex position of first index to apply the given action to, inclusive
	 * @param endIndex position of last index to apply the given action to, exclusive
	 * @return {@code true} iff the given {@code check} holds for all consecutive index
	 * values in the specified region of this set.
	 */
	default boolean checkConsecutiveIndices(LongBiPredicate check, int beginIndex, int endIndex) {
		requireNonNull(check);
		checkRangeExlusive(this, beginIndex, endIndex);
		if(beginIndex==endIndex-1) {
			return false;
		}

		// We try to call indexAt(i) at most once for every index i
		long previous = indexAt(beginIndex);
		for(int i=beginIndex+1; i<endIndex; i++) {
			long index = indexAt(i);
			if(!check.test(previous, index)) {
				return false;
			}
			previous = index;
		}

		return true;
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
			throw new ModelException(GlobalErrorCode.VALUE_OVERFLOW,
					"Cannot create array of size: "+chunks);

		IndexSet[] result = new IndexSet[chunks];

		int fromIndex = 0;
		int toIndex;
		for(int i=0; i<chunks; i++) {
			toIndex = Math.min(fromIndex+chunkSize-1, size-1);
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
	 * <p
	 * It is vital that the {@link IndexSet} returned by this method and the one this method
	 * has been invoked on, do <b>not</b> share any state afterwards!
	 */
	IndexSet externalize();

	// FEATURE FLAGS

	public enum Feature {

		/**
		 * General hint that the implementation is able to sort its content.
		 * Note that this information is kind of redundant since the {@link #sort()} method
		 * already expresses this information, but more in a post-condition kind of way.
		 * <p>
		 * Also note that it is perfectly legal for an implementation to not support
		 * sorting on demand but be already sorted!
		 */
		SORTABLE,

		/**
		 * Indicates whether or not the implementation supports exporting parts of it via any
		 * of the following method:
		 * <ul>
		 * <li>{@link #split(int)}</li>
		 * <li>{@link #subSet(int, int)}</li>
		 * </ul>
		 * Implementations are free to
		 */
		EXPORTABLE,

		/**
		 * Signals that this implementation is not able to provide information about the number
		 * of entries it holds. This can be the case when the index set is a wrapper around some
		 * foreign storage like the {@link ResultSet result} of a SQL query.
		 */
		INDETERMINATE_SIZE,

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
		CURSOR_FORWARD_ONLY,

		/**
		 * Signals that the implementation is thread-safe and it can be shared across multiple threads.
		 * Per default implementations are <b>not</> required to implement synchronization measures to
		 * improve performance.
		 */
		THREAD_SAFE,
		;
	}

	public static final Set<Feature> DEFAULT_FEATURES =
			Collections.unmodifiableSet(EnumSet.of(
					Feature.EXPORTABLE, Feature.SORTABLE));

	default Set<Feature> getFeatures() {
		return DEFAULT_FEATURES;
	}

	default boolean hasFeatures(Feature...features) {
		return getFeatures().containsAll(Arrays.asList(features));
	}

	default boolean hasFeature(Feature feature) {
		return getFeatures().contains(requireNonNull(feature));
	}

	default void checkHasFeatures(Feature...features) {
		LazyCollection<Feature> missingFeatures = LazyCollection.lazyList();
		Set<Feature> presentFeatures = getFeatures();

		for(Feature feature : features) {
			if(!presentFeatures.contains(feature)) {
				missingFeatures.add(feature);
			}
		}

		if(!missingFeatures.isEmpty()) {
			StringBuilder sb = new StringBuilder("Required features not available: ");

			sb.append(missingFeatures.toString());

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

	/**
	 *
	 * @param start first index for values returned from iterator
	 * @return
	 */
	default OfLong iterator(int start) {
		checkIndex(this, start);
		return IndexUtils.asIterator(this, start);
	}

	/**
	 *
	 * @param start begin of the section to iterate over, inclusive
	 * @param end end of the section to iterate over, exclusive
	 * @return
	 */
	default OfLong iterator(int start, int end) {
		checkRangeExlusive(this, start, end);
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
