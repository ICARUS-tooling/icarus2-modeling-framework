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
 *
 */
package de.ims.icarus2.model.standard.registry.metadata;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.registry.MetadataRegistry;

/**
 * @author Markus Gärtner
 *
 */
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

	private final Path file;

	private static final Map<Path, PlainMetadataRegistry> instances = new WeakHashMap<>();

	public static PlainMetadataRegistry getSharedRegistry(Path file) {
		checkNotNull(file);

		synchronized (instances) {
			PlainMetadataRegistry registry = instances.get(file);

			if(registry==null) {
				registry = new PlainMetadataRegistry(file);
				instances.put(file, registry);
			}

			return registry;
		}
	}

	private static void shutdown(PlainMetadataRegistry storage) {
		synchronized (instances) {
			try {
				// Ensure the data gets saved properly
				storage.synchronize();
			} finally {
				// Finally discard storage
				instances.remove(storage.getFile());
			}
		}
	}

	private PlainMetadataRegistry(Path file) {
		checkNotNull(file);

		this.file = file;
	}

	public Path getFile() {
		return file;
	}

	@Override
	public synchronized void close() {
		shutdown(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#open()
	 */
	@Override
	public synchronized void open() {
		try {
			load();
		} catch (IOException e) {
			log.error("Failed to load value storage from file", e); //$NON-NLS-1$
			//FIXME propagate error?
		}
	}

	public synchronized void synchronize() {
		// Only save to disc when actual new entries exist
		if(!checkStorage()) {
			try {
				saveNow();
			} catch (IOException e) {
				log.error("Failed to synchronize value storage to file", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 *
	 */
	public void saveNow() throws IOException {
		Properties tmp = new Properties();
		tmp.putAll(entries);

		Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
		try {
			tmp.store(writer, null);
		} finally {
			changedEntryCount = 0;
		}
	}

	/**
	 * Loads the value storage if necessary and returns {@code true} if at least one
	 * mapping was loaded.
	 */
	private synchronized boolean checkStorage() {
		if(entries.isEmpty()) {
			try {
				load();
			} catch (IOException e) {
				log.error("Failed to load value storage: {}", getFile(), e); //$NON-NLS-1$
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

		Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
		prop.load(reader);

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
		if(value==null) {
			entries.remove(key);
		} else {
			entries.put(key, value);
		}

		changedEntryCount++;
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
			Files.delete(file);
		} catch (IOException e) {
			throw new ModelException(GlobalErrorCode.IO_ERROR, "Failed to delete registry file");
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

		String lowerBound = prefix+Character.MIN_VALUE;
		String upperBound = prefix+Character.MAX_VALUE;

		entries.subMap(lowerBound, true, upperBound, true).forEach(action);
	}
}
