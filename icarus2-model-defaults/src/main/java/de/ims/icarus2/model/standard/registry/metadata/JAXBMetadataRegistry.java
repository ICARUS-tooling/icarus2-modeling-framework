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
package de.ims.icarus2.model.standard.registry.metadata;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
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
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.io.resource.IOResource;
import de.ims.icarus2.util.xml.jaxb.JAXBGate;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(MetadataRegistry.class)
public class JAXBMetadataRegistry extends JAXBGate<JAXBMetadataRegistry.StorageBuffer>
		implements MetadataRegistry {

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

	public JAXBMetadataRegistry(IOResource resource) {
		this(resource, StandardCharsets.UTF_8);
	}

	public JAXBMetadataRegistry(IOResource resource, Charset encoding) {
		super(resource, encoding, StorageBuffer.class);
	}

	@Override
	public synchronized void close() {
		synchronize();
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
		if(checkStorage()) {
			try {
				saveBuffer(true);
			} catch (Exception e) {
				log.error("Failed to synchronize value storage to file", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @see de.ims.icarus2.util.xml.jaxb.JAXBGate#afterSave()
	 */
	@Override
	protected void afterSave() {
		changedEntryCount = 0; //TODO suboptimal, as in worst case we lose information about intermediate changes
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
				log.error("Failed to load value storage: "+getResource(), e); //$NON-NLS-1$
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

		String lowerBound = prefix;
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
