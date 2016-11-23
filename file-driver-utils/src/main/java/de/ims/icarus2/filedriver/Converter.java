/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
 */
package de.ims.icarus2.filedriver;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import de.ims.icarus2.Report;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndex;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexStorage;
import de.ims.icarus2.model.api.driver.ChunkState;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
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
public interface Converter extends AutoCloseable {

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
		CHUNK_INDEX_SUPPORTED(Boolean.class, false),

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

	/**
	 * Called by the {@link FileDriver} that uses this converter.
	 * Meant to perform setup work and initialize internal resources.
	 *
	 * @param driver
	 */
	void init(FileDriver driver);

	FileDriver getFileDriver();

	/**
	 * Preprocessing step that reads the specified corpus file and populates chunking and metadata.
	 * <p>
	 * In addition it should perform content verification and return a report containing information
	 * about violations.
	 */
	Report<ReportItem> scanFile(int fileIndex) throws IOException, InterruptedException;

	/**
	 * Loads the entire content of the specified file and converts its content into proper model
	 * representations.
	 *
	 * @param fileIndex
	 * @param action
	 * @return the total number of elements in the respective primary layer that have been loaded from the file
	 * @throws IOException
	 * @throws InterruptedException
	 */
	long loadFile(int fileIndex, ChunkConsumer action) throws IOException, InterruptedException;

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
	Cursor getCursor(int fileIndex, ItemLayer layer) throws IOException;

	/**
	 * Releases any internal resources and most importantly disconnects from the
	 * {@link FileDriver driver} supplied by the previous call to {@link #init(FileDriver)}.
	 *
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close();

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
	 *
	 * @author Markus Gärtner
	 *
	 */
	interface Cursor extends AutoCloseable {

		//TODO

		Item load(long id) throws IOException, InterruptedException;

		default long loadAll(IndexSet[] indices, ChunkConsumer consumer) throws IOException, InterruptedException {
			long result = 0L;

			for(IndexSet set : indices) {
				for(int i=0; i<set.size(); i++) {
					long index = set.indexAt(i);
					Item item = load(index);
					ChunkState state = ChunkState.forItem(item);

					if(state==ChunkState.VALID) {
						result++;
					}

					consumer.accept(index, item, state);
				}
			}

			return result;
		}

		/**
		 * @see java.lang.AutoCloseable#close()
		 */
		@Override
		public void close() throws IOException;
	}
}
