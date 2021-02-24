/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.driver.mapping;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import de.ims.icarus2.apiguard.OptionalMethod;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexSetBuilder;
import de.ims.icarus2.model.api.io.SynchronizedAccessor;

/**
 * Models the read access to an {@link Mapping} implementation. Note that all
 * methods in this interface that take arrays of {@link IndexSet} instances as
 * arguments, expect those arrays to be sorted according to the order defined by
 * {@link IndexSet#INDEX_SET_SORTER}!
 * <p>
 * For performance reasons it is advised to use batch methods instead of sequences of
 * single lookups whenever possible. This is motivated by the fact that client code
 * using this interface cannot know how the back-end logic is implemented. While
 * simple array based implementations like {@link MappingImplSpanOneToMany} do not
 * present great performance differences between single and batch lookups, there
 * can be database connected implementations which have a certain overhead for the
 * creation and execution of the respective query to the database system. For such
 * implementations it is vital that batch lookups are done via the correct methods
 * so that they can reduce the overhead down to a minimum!
 *
 * @author Markus Gärtner
 *
 */
public interface MappingReader extends SynchronizedAccessor<Mapping> {

	// Single index lookups

	/**
	 * Looks up the mapping for the specified {@code sourceIndex} and sends the result to the
	 * given {@code collector}. Returns {@code true} iff there was a valid mapping for the
	 * {@code sourceIndex} and the {@code collector} has been provided that value.
	 *
	 * @param sourceIndex
	 * @param collector
	 * @param settings TODO
	 * @return
	 * @throws ModelException
	 * @throws InterruptedException
	 */
	boolean lookup(long sourceIndex, IndexCollector collector,
			@Nullable RequestSettings settings) throws InterruptedException;

	/**
	 * Returns the (estimated or maximum) number of indices that are mapped to the given {@code sourceIndex}.
	 * This is equal to the {@link IndexSet#size() size} of an {@link IndexSet} that would
	 * be returned for a call to {@link #lookup(long, RequestSettings)} when provided
	 * with the same {@code sourceIndex} argument but allows for a much more efficient
	 * implementation since it doesn't need to actually read and return all the index values.
	 * <p>
	 * If the implementation is unable to efficiently determine to the number of mapped indices
	 * without actually loading them it can return {@code -1} to signal an "unknown" size.
	 * <p>
	 * For implementations that map to a constant sized amount of target indices, this method
	 * is allowed to return that constant number, even if the given {@code sourceIndex} doesn't
	 * actually map to any target indices.
	 *
	 * @param sourceIndex
	 * @param settings
	 * @return the number of {@link #lookup(long, RequestSettings) target indices} mapped to the given {@code sourceIndex}
	 * 		or {@code -1} if that number cannot be determined efficiently.
	 * @throws InterruptedException
	 */
	@OptionalMethod
	long getIndicesCount(long sourceIndex, @Nullable RequestSettings settings) throws InterruptedException;

	IndexSet[] lookup(long sourceIndex, @Nullable RequestSettings settings) throws InterruptedException;

	long getBeginIndex(long sourceIndex, @Nullable RequestSettings settings) throws InterruptedException;
	long getEndIndex(long sourceIndex, @Nullable RequestSettings settings) throws InterruptedException;

	// Bulk index lookups

	default IndexSet[] lookup(IndexSet[] sourceIndices, @Nullable RequestSettings settings) throws InterruptedException {
		requireNonNull(sourceIndices);

		int chunkSizeLimit = Math.max(1000, IndexUtils.maxSize(sourceIndices));

		IndexSetBuilder builder = new IndexCollectorFactory()
				.chunkSizeLimit(chunkSizeLimit)
				.outputSorted(RequestSettings.fallback(settings).isHintSet(RequestHint.OUTPUT_ORDER_SORTED))
				.create();

		lookup(sourceIndices, builder, settings);

		return builder.build();
	}

	boolean lookup(IndexSet[] sourceIndices, IndexCollector collector, @Nullable RequestSettings settings) throws InterruptedException;

	long getBeginIndex(IndexSet[] sourceIndices, @Nullable RequestSettings settings) throws InterruptedException;
	long getEndIndex(IndexSet[] sourceIndices, @Nullable RequestSettings settings) throws InterruptedException;

	// Utility method for efficient reverse lookups

	/**
	 * Find the source index that maps to the specified {@code targetIndex}, restricting the
	 * search to the closed interval {@code fromSource} to {@code toSource}. This method is
	 * intended for use of reverse indices that are able to efficiently pin down the possible
	 * range of source indices for a given target index and then delegate the remaining work
	 * of the lookup to an existing index inverse to their own mapping direction.
	 *
	 * @param fromSource
	 * @param toSource
	 * @param targetIndex
	 * @param settings TODO
	 * @return
	 * @throws ModelException
	 * @throws InterruptedException
	 */
	@OptionalMethod
	long find(long fromSource, long toSource, long targetIndex,
			@Nullable RequestSettings settings) throws InterruptedException;

	/**
	 * Performs a reverse lookup for a collection of target indices. Note that the {@code targetIndices}
	 * array is required to be sorted according to {@link IndexSet#INDEX_SET_SORTER}!
	 *
	 * @see #find(long, long, long, RequestSettings)
	 *
	 * @param fromSource
	 * @param toSource
	 * @param targetIndices
	 * @param settings TODO
	 * @return
	 * @throws ModelException
	 * @throws InterruptedException
	 */
	@OptionalMethod
	default IndexSet[] find(long fromSource, long toSource, IndexSet[] targetIndices,
			@Nullable RequestSettings settings) throws InterruptedException {
		requireNonNull(targetIndices);

		IndexSetBuilder builder = new IndexCollectorFactory()
				.chunkSizeLimit(IndexUtils.maxSize(targetIndices))
				.outputSorted(RequestSettings.fallback(settings).isHintSet(RequestHint.OUTPUT_ORDER_SORTED))
				.create();

		find(fromSource, toSource, targetIndices, builder, settings);

		return builder.build();
	}

	@OptionalMethod
	boolean find(long fromSource, long toSource, IndexSet[] targetIndices, IndexCollector collector,
			@Nullable RequestSettings settings) throws InterruptedException;
}
