/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/driver/mapping/MappingReader.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.driver.mapping;

import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory;
import de.ims.icarus2.model.api.driver.indices.standard.IndexCollectorFactory.IndexSetBuilder;
import de.ims.icarus2.model.api.io.SynchronizedAccessor;
import de.ims.icarus2.util.annotations.OptionalMethod;

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
 * @version $Id: MappingReader.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public interface MappingReader extends SynchronizedAccessor<Mapping>, ModelConstants {

//	/**
//	 * Returns the number of mapping entries currently accessible by this reader.
//	 * If the reader has no information about stored data of in case it does not
//	 * rely on stored data at all, this method should return {@code -1}.
//	 *
//	 * @return
//	 */
//	long getEntryCount();

	// Single index lookups

	/**
	 * Looks up the mapping for the specified {@code sourceIndex} and sends the result to the
	 * given {@code collector}. Returns {@code true} iff there was a valid mapping for the
	 * {@code sourceIndex} and the {@code collector} was provided that value.
	 *
	 * @param sourceIndex
	 * @param collector
	 * @param settings TODO
	 * @return
	 * @throws ModelException
	 * @throws InterruptedException
	 */
	boolean lookup(long sourceIndex, IndexCollector collector, RequestSettings settings) throws InterruptedException;

	IndexSet[] lookup(long sourceIndex, RequestSettings settings) throws InterruptedException;

	long getBeginIndex(long sourceIndex, RequestSettings settings) throws InterruptedException;
	long getEndIndex(long sourceIndex, RequestSettings settings) throws InterruptedException;

	// Bulk index lookups

	default IndexSet[] lookup(IndexSet[] sourceIndices, RequestSettings settings) throws InterruptedException {

		IndexSetBuilder builder = new IndexCollectorFactory().chunkSizeLimit(IndexUtils.maxSize(sourceIndices)).create();

		lookup(sourceIndices, builder, settings);

		return builder.build();
	}

	boolean lookup(IndexSet[] sourceIndices, IndexCollector collector, RequestSettings settings) throws InterruptedException;

	long getBeginIndex(IndexSet[] sourceIndices, RequestSettings settings) throws InterruptedException;
	long getEndIndex(IndexSet[] sourceIndices, RequestSettings settings) throws InterruptedException;

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
	long find(long fromSource, long toSource, long targetIndex, RequestSettings settings) throws InterruptedException;

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
	default IndexSet[] find(long fromSource, long toSource, IndexSet[] targetIndices, RequestSettings settings) throws InterruptedException {

		IndexSetBuilder builder = new IndexCollectorFactory().chunkSizeLimit(IndexUtils.maxSize(targetIndices)).create();

		find(fromSource, toSource, targetIndices, builder, settings);

		return builder.build();
	}

	@OptionalMethod
	boolean find(long fromSource, long toSource, IndexSet[] targetIndices, IndexCollector collector, RequestSettings settings) throws InterruptedException;
}
