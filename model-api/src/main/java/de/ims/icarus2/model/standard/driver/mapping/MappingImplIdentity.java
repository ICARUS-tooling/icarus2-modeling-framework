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

 * $Revision: 429 $
 * $Date: 2015-10-07 17:08:17 +0200 (Mi, 07 Okt 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/mapping/MappingImplIdentity.java $
 *
 * $LastChangedDate: 2015-10-07 17:08:17 +0200 (Mi, 07 Okt 2015) $
 * $LastChangedRevision: 429 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.driver.mapping;

import static de.ims.icarus2.model.api.driver.indices.IndexUtils.firstIndex;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.lastIndex;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.model.standard.driver.indices.SingletonIndexSet;

/**
 * Implements a total index of type {@code one-to-one} which maps
 * indices to their own value between two layers. This index stores
 * <b>no</b> internal state and therefore the synchronization and
 * close methods on its reader instances have no effect!
 *
 * @author Markus Gärtner
 * @version $Id: MappingImplIdentity.java 429 2015-10-07 15:08:17Z mcgaerty $
 *
 */
public class MappingImplIdentity extends AbstractMapping {

	/**
	 * @see de.ims.icarus2.model.api.driver.mapping.Mapping#newReader()
	 */
	@Override
	public MappingReader newReader() {
		return this.new Reader();
	}

	public class Reader implements MappingReader {

		/**
		 * @see de.ims.icarus2.model.io.SynchronizedAccessor#getSource()
		 */
		@Override
		public Mapping getSource() {
			return MappingImplIdentity.this;
		}

		/**
		 * @see de.ims.icarus2.model.io.SynchronizedAccessor#begin()
		 */
		@Override
		public void begin() {
			// no-op
		}

		/**
		 * @see de.ims.icarus2.model.io.SynchronizedAccessor#end()
		 */
		@Override
		public void end() {
			// no-op
		}

		/**
		 * @see de.ims.icarus2.model.io.SynchronizedAccessor#close()
		 */
		@Override
		public void close() throws ModelException {
			// no-op
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(long sourceIndex, IndexCollector collector, RequestSettings settings)
				throws InterruptedException {
			collector.add(sourceIndex);
			return true;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(long, RequestSettings)
		 */
		@Override
		public IndexSet[] lookup(long sourceIndex, RequestSettings settings) throws ModelException,
				InterruptedException {
			return new IndexSet[]{new SingletonIndexSet(sourceIndex)};
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(long, RequestSettings)
		 */
		@Override
		public long getBeginIndex(long sourceIndex, RequestSettings settings) throws ModelException,
				InterruptedException {
			return sourceIndex;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(long, RequestSettings)
		 */
		@Override
		public long getEndIndex(long sourceIndex, RequestSettings settings) throws ModelException,
				InterruptedException {
			return sourceIndex;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public IndexSet[] lookup(IndexSet[] sourceIndices, RequestSettings settings)
				throws InterruptedException {
			return sourceIndices;
		}

		/**
		 * @return
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#lookup(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean lookup(IndexSet[] sourceIndices, IndexCollector collector, RequestSettings settings)
				throws InterruptedException {
			collector.add(sourceIndices);
			return true;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getBeginIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public long getBeginIndex(IndexSet[] sourceIndices, RequestSettings settings)
				throws InterruptedException {
			return firstIndex(sourceIndices);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#getEndIndex(de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public long getEndIndex(IndexSet[] sourceIndices, RequestSettings settings)
				throws InterruptedException {
			return lastIndex(sourceIndices);
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, long, RequestSettings)
		 */
		@Override
		public long find(long fromSource, long toSource, long targetIndex, RequestSettings settings)
				throws InterruptedException {
			return targetIndex;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, de.ims.icarus2.model.api.driver.indices.IndexSet[], RequestSettings)
		 */
		@Override
		public IndexSet[] find(long fromSource, long toSource,
				IndexSet[] targetIndices, RequestSettings settings) throws ModelException,
				InterruptedException {
			return targetIndices;
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingReader#find(long, long, de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexCollector, RequestSettings)
		 */
		@Override
		public boolean find(long fromSource, long toSource,
				IndexSet[] targetIndices, IndexCollector collector, RequestSettings settings)
				throws InterruptedException {
			collector.add(targetIndices);
			return true;
		}

	}
}
