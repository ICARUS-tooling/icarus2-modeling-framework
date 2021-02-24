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
package de.ims.icarus2.model.standard.registry.metadata;

import java.util.TreeMap;
import java.util.function.BiConsumer;

import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * Implements a virtual in-memory metadata registry whose content does not get synchronized to a
 * persistent storage and therefore will be lost once the registry is closed.
 *
 * @author Markus Gärtner
 *
 */
@TestableImplementation(MetadataRegistry.class)
public class VirtualMetadataRegistry implements MetadataRegistry {

	private final TreeMap<String, String> entries = new TreeMap<>();


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
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#beginUpdate()
	 */
	@Override
	public void beginUpdate() {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#endUpdate()
	 */
	@Override
	public void endUpdate() {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#delete()
	 */
	@Override
	public synchronized void delete() {
		entries.clear();
	}

	@Override
	public synchronized void forEachEntry(BiConsumer<? super String, ? super String> action) {
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
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#close()
	 */
	@Override
	public void close() {
		delete();
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#open()
	 */
	@Override
	public void open() {
		// no-op
	}
}
