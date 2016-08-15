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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.util.xml.jaxb.JAXBGate;

/**
 * @author Markus Gärtner
 *
 */
public class JAXBMetadataRegistry extends JAXBGate<JAXBMetadataRegistry.StorageBuffer> implements MetadataRegistry {

	/**
	 * The default file suffix used by the modeling framework for metadata registry storage
	 * in xml-files.
	 */
	public static final String DEFAULT_FILE_ENDING = ".mdxml";

	private static final Logger log = LoggerFactory
			.getLogger(JAXBMetadataRegistry.class);

	private final TreeMap<String, String> entries = new TreeMap<>();

	private int changedEntryCount = 0;

	private int useCount = 0;

	private static final Map<Path, JAXBMetadataRegistry> instances = new WeakHashMap<>();

	public static JAXBMetadataRegistry getSharedRegistry(Path file) {
		if (file == null)
			throw new NullPointerException("Invalid file"); //$NON-NLS-1$

		synchronized (instances) {
			JAXBMetadataRegistry storage = instances.get(file);

			if(storage==null) {
				storage = new JAXBMetadataRegistry(file);
				instances.put(file, storage);
			}

			return storage;
		}
	}

	private static void destroy(JAXBMetadataRegistry storage) {
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

	private JAXBMetadataRegistry(Path file) {
		super(file, StorageBuffer.class);
	}

	@Override
	public synchronized void close() {
		destroy(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#open()
	 */
	@Override
	public void open() {
		try {
			loadBuffer();
		} catch (Exception e) {
			log.error("Failed to load value storage from file", e); //$NON-NLS-1$
			//FIXME propagate error?
		}
	}

	/**
	 * @see de.ims.icarus2.util.xml.jaxb.JAXBGate#readBuffer(java.lang.Object)
	 */
	@Override
	protected synchronized void readBuffer(StorageBuffer buffer)
			throws Exception {
		entries.clear();

		for(StorageEntry entry : buffer.items) {
			String key = entry.getKey();
			String value = entry.getValue();

			entries.put(key, value);
		}
	}

	/**
	 * @see de.ims.icarus2.util.xml.jaxb.JAXBGate#createBuffer()
	 */
	@Override
	protected synchronized StorageBuffer createBuffer()
			throws Exception {
		if(entries.isEmpty()) {
			return null;
		}

		List<StorageEntry> entryList = new ArrayList<>();

		for(Entry<String, String> entry : entries.entrySet()) {
			if(entry.getValue()==null) {
				continue;
			}
			entryList.add(new StorageEntry(entry.getKey(), entry.getValue()));
		}

		return new StorageBuffer(entryList);
	}

	public synchronized void synchronize() {
		// Only save to disc when actual new entries exist
		if(!checkStorage()) {
			try {
				saveBufferNow();
			} catch (Exception e) {
				log.error("Failed to synchronize value storage to file", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @see de.ims.icarus2.util.xml.jaxb.JAXBGate#save(java.nio.file.Path, boolean)
	 */
	@Override
	public void save(Path file, boolean saveNow) throws Exception {
		try {
			super.save(file, saveNow);
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
				loadBuffer();
			} catch (Exception e) {
				log.error("Failed to load value storage: "+getFile(), e); //$NON-NLS-1$
			}

			return !entries.isEmpty();
		}

		return changedEntryCount>0;
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
	 * This implementation clears all entries and then attempts to delete the backing physical file.
	 *
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#delete()
	 */
	@Override
	public synchronized void delete() {
		useCount = 0;
		entries.clear();

		try {
			super.delete();
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

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	@XmlRootElement(name="entries")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class StorageBuffer {
		@XmlElement(name="entry")
		private List<StorageEntry> items;

		private StorageBuffer() {
			// no-op
		}

		private StorageBuffer(List<StorageEntry> items) {
			this.items = items;
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	@XmlRootElement(name="entry")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class StorageEntry {
		@XmlElement(name="key", required=true)
		private String key;
		@XmlElement(name="value", required=true)
		private String value;

		private StorageEntry() {
			// no-op
		}

		private StorageEntry(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}
	}
}
