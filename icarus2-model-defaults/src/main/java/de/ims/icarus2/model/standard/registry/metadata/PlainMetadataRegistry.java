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
package de.ims.icarus2.model.standard.registry.metadata;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.io.IOUtil;
import de.ims.icarus2.util.io.resource.IOResource;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(MetadataRegistry.class)
public class PlainMetadataRegistry implements MetadataRegistry {

	/**
	 * The default file suffix used by the modeling framework for metadata registry storage
	 * in ini-files.
	 */
	public static final String DEFAULT_FILE_ENDING = ".mdini";

	private static final Logger log = LoggerFactory
			.getLogger(PlainMetadataRegistry.class);

	private final TreeMap<String, String> entries = new TreeMap<>();

	private int useCount = 0;

	private int changedEntryCount = 0;

	private final IOResource resource;
	private final Charset encoding;

	public PlainMetadataRegistry(IOResource resource) {
		this(resource, StandardCharsets.UTF_8);
	}

	public PlainMetadataRegistry(IOResource resource, Charset encoding) {
		this.resource = requireNonNull(resource);
		this.encoding = requireNonNull(encoding);
	}

	public IOResource getResource() {
		return resource;
	}

	@Override
	public synchronized void close() {
		synchronize();
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#open()
	 */
	@Override
	public synchronized void open() {
		if(resource.getAccessMode().isRead()) {
			try {
				load();
			} catch (IOException e) {
				log.error("Failed to load value storage", e);
				//FIXME propagate error?
			}
		}
	}

	public synchronized void synchronize() {
		// Only save to disc when actual new entries exist
		if(checkStorage()) {
			try {
				saveNow();
			} catch (IOException e) {
				log.error("Failed to synchronize value storage", e);
			}
		}
	}

	/**
	 * Writes the content of this registry to the file specified
	 * in the constructor using UTF-8 character encoding.
	 */
	public void saveNow() throws IOException {

		int oldChangedEntryCount = changedEntryCount;
		/*
		 * Kinda expensive strategy, since we duplicate
		 * the entire registry content
		 */
		Properties tmp = new Properties();
		tmp.putAll(entries);

		resource.prepare();
		try (Writer writer = Channels.newWriter(resource.getWriteChannel(),
				encoding.newEncoder(), IOUtil.DEFAULT_BUFFER_SIZE)) {
			tmp.store(writer, null);
		} finally {
			changedEntryCount -= oldChangedEntryCount;
		}
	}

	/**
	 * Loads the value storage if necessary and returns {@code true} if at least one
	 * mapping was loaded.
	 */
	private synchronized boolean checkStorage() {
		// Attempt automatic reload only when underlying resource allows read access
		if(entries.isEmpty() && resource.getAccessMode().isRead()) {
			try {
				load();
			} catch (IOException e) {
				log.error("Failed to load value storage: {}", getResource(), e);
			}

			return !entries.isEmpty();
		}

		return changedEntryCount>0;
	}

	/**
	 *
	 */
	@SuppressWarnings("unchecked")
	public void load() throws IOException {
		Properties prop = new Properties();

		resource.prepare();
		try(Reader reader = Channels.newReader(resource.getReadChannel(),
				encoding.newDecoder(), IOUtil.DEFAULT_BUFFER_SIZE)) {
			prop.load(reader);
		}

		@SuppressWarnings("rawtypes")
		Map tmp = prop;
		entries.clear();
		entries.putAll(tmp);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#getValue(java.lang.String)
	 */
	@Override
	public synchronized String getValue(String key) {
		return entries.get(key);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#setValue(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized void setValue(String key, String value) {
		beginUpdate();
		try {
			if(value==null) {
				entries.remove(key);
			} else {
				entries.put(key, value);
			}

			changedEntryCount++;
		} finally {
			endUpdate();
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#beginUpdate()
	 */
	@Override
	public synchronized void beginUpdate() {
		checkStorage();

		useCount++;
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#endUpdate()
	 */
	@Override
	public synchronized void endUpdate() {
		useCount--;

		if(useCount==0) {
			synchronize();
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#delete()
	 */
	@Override
	public synchronized void delete() {
		useCount = 0;
		entries.clear();

		try {
			resource.delete();
		} catch (IOException e) {
			throw new ModelException(GlobalErrorCode.IO_ERROR, "Failed to delete registry resource");
		}
	}

	@Override
	public synchronized void forEachEntry(BiConsumer<? super String, ? super String> action) {
		checkStorage();

		entries.forEach(action);
	}

	/**
	 *
	 *
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#forEachEntry(java.lang.String, java.util.function.BiConsumer)
	 */
	@Override
	public synchronized void forEachEntry(String prefix,
			BiConsumer<? super String, ? super String> action) {

		String lowerBound = prefix;
		String upperBound = prefix+Character.MAX_VALUE;

		entries.subMap(lowerBound, true, upperBound, true).forEach(action);
	}
}
