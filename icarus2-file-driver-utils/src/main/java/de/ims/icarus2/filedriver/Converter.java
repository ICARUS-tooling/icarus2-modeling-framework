/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.Report;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndex;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexStorage;
import de.ims.icarus2.model.api.driver.ChunkState;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.mods.DriverModule;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.standard.driver.ChunkConsumer;


/**
 * Models the analysis and conversion of a binary corpus resource into actual
 * model instances and vice versa.
 *
 * @author Markus Gärtner
 *
 */
public interface Converter extends DriverModule {

	public enum ReadMode {
		/** Input data only needs to be analyzed once, no actual in-memory storage required */
		SCAN,
		/** An individual chunk of data from within a single file is to be loaded */
		CHUNK,
		/** Reading of an entire file is desired */
		FILE,
		;
	}

	/**
	 * Provides a list of properties the converter might be queried for
	 * during its lifecycle.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public enum ConverterProperty {

		/**
		 * Allows the converter to decide whether or not to actually support
		 * indexing of chunk information. Since for layer-centric formats it
		 * is impractical to try and address individual chunks on the byte level,
		 * it's up to the converter that models those specific formats to
		 * reject the usage of chunk indices.
		 * <p>
		 * This is a {@code boolean} property and its default value is {@code false}.
		 */
		CHUNK_INDEX_SUPPORTED(Boolean.class, Boolean.FALSE),

		/**
		 * Hints for the construction of {@link ChunkIndex} instances as to what
		 * {@link IndexValueType} to use. If a converter does not support chunking
		 * or wishes to construct those indices, i.e. override the
		 * {@link Converter#createChunkIndices()} method, it should return an empty
		 * {@link Map} for this property. Otherwise the map should contain an entry
		 * for each {@link LayerGroupManifest}, using the {@link LayerGroupManifest#getId() group's id}
		 * as key and mapping to a positive {@link Integer} value that denotes the
		 * estimated (not necessarily minimal) size of individual top level members
		 * of that group in bytes.
		 */
		EXTIMATED_CHUNK_SIZES(Map.class, Collections.emptyMap()),

		;

		private final Object defaultValue;
		private final Class<?> type;

		private ConverterProperty(Class<?> type, Object defaultValue) {
			this.type = type;
			this.defaultValue = defaultValue;
		}

		public Object getDefaultValue() {
			return defaultValue;
		}

		public boolean isValidType(Object value) {
			return value==null || type.isInstance(value);
		}
	}

	default Object getPropertyValue(ConverterProperty property) {
		return property.getDefaultValue();
	}

	@Override
	FileDriver getDriver();

	/**
	 * Preprocessing step that reads the specified corpus file and populates chunking and metadata.
	 * <p>
	 * In addition it should perform content verification and return a report containing information
	 * about violations.
	 */
	Report<ReportItem> scanFile(int fileIndex) throws IOException, InterruptedException, IcarusApiException;

	/**
	 * Loads the entire content of the specified file and converts its content into proper model
	 * representations.
	 *
	 * @param fileIndex
	 * @param action
	 * @return the total number of elements in the respective primary layer that have been loaded from the file
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws IcarusApiException
	 */
	LoadResult loadFile(int fileIndex, ChunkConsumer action) throws IOException, InterruptedException, IcarusApiException;

	/**
	 * Returns a cursor that allows {@code random-access} style reading of the specified layer.
	 * <p>
	 * This is an optional method and only required when the converter actually supports chunking and
	 * the driver it is linked to has a {@code non-null} chunk index storage. In case any of those
	 * two conditions is not met the method should return {@code null}.
	 *
	 * @param fileIndex
	 * @param layer
	 * @return
	 * @throws IOException
	 */
	Cursor<?> getCursor(int fileIndex, ItemLayer layer) throws IOException, IcarusApiException;

	/**
	 * Allows a converter implementation to decide upon what chunk indices to use.
	 * <p>
	 * The default implementation returns {@code null} which shifts the responsibility
	 * to the surrounding {@link FileDriver} instance.
	 *
	 * @return
	 */
	default ChunkIndexStorage createChunkIndices() {
		return null;
	}

	/**
	 * Models a {@code random-access} read mechanism to load individual parts of a corpus resource.
	 * A cursor, once obtained from the hosting converter, can be used to load contents from an
	 * underlying file resource at arbitrary locations.
	 * <p>
	 * Note that a cursor is not thread-safe! If client code wishes to distribute loading of file
	 * resources across multiple threads, it should instantiate one cursor for every thread.
	 *
	 * @author Markus Gärtner
	 *
	 */
	interface Cursor<C extends Converter> extends AutoCloseable {

		C getConverter();

		//TODO

		Item load(long id) throws IOException, InterruptedException, IcarusApiException;

		/**
		 * Short-hand method to load a large number of items from this cursor.
		 * The provided {@link ChunkConsumer} will be notified whenever an item has
		 * been loaded.
		 *
		 * @param indices
		 * @param consumer
		 * @return the total number of {@link ChunkState#VALID valid} items that have been loaded
		 * @throws IOException
		 * @throws InterruptedException
		 * @throws IcarusApiException
		 */
		default LoadResult loadAll(IndexSet[] indices, ChunkConsumer consumer)
				throws IOException, InterruptedException, IcarusApiException {
			// Reset result
			getResult();

			for(IndexSet set : indices) {
				for(int i=0; i<set.size(); i++) {
					long index = set.indexAt(i);
					Item item = load(index);
					consumer.accept(index, item, ChunkState.forItem(item));
				}
			}

			return getResult();
		}

		/**
		 * Returns a {@link LoadResult} object that encapsulates all data chunks
		 * that have been loaded since this cursor has been created or this method
		 * was called the last time, whichever happened more recent.
		 *
		 * @return
		 */
		LoadResult getResult();

		/**
		 * Releases all underlying resources and makes this cursor unusable for further
		 * {@link #load(long) load} operations.
		 *
		 * @see java.lang.AutoCloseable#close()
		 */
		@Override
		public void close() throws IOException;
	}

	/**
	 * Models a transactional wrapper for loaded data chunks.
	 *
	 * @author Markus Gärtner
	 *
	 */
	interface LoadResult extends AutoCloseable {

		/**
		 * Commits the loaded (valid) data chunks to the respective back-end caches
		 * and returns the total number of chunks that have been published this way.
		 *
		 * @throws IllegalStateException if this result has already been {@link #publish() published}
		 * or {@link #discard() discarded} before
		 */
		long publish();

		/**
		 * Discards all the loaded data chunks and returns the total number
		 * of chunks that have been discarded this way.
		 *
		 * @throws IllegalStateException if this result has already been {@link #publish() published}
		 * or {@link #discard() discarded} before
		 */
		long discard();

		/**
		 * Returns the total number of data chunks that have been loaded.
		 *
		 * @return
		 */
		long loadedChunkCount();

		/**
		 * Returns the number of data chunks that have been loaded and
		 * were assigned the specified {@link ChunkState state}.
		 *
		 * @param state
		 * @return
		 */
		long chunkCount(ChunkState state);

		@Override
		default void close() {
			// no-op
		}
	}
}
