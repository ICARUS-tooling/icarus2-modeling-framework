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
package de.ims.icarus2.model.api.registry;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author Markus Gärtner
 *
 */
public class SubRegistry implements MetadataRegistry {

	private final String prefix;
	private final MetadataRegistry source;

	public SubRegistry(MetadataRegistry source, String prefix) {
		requireNonNull(source);
		requireNonNull(prefix);
		checkArgument("Prefix must not be empty", !prefix.trim().isEmpty());

		/*
		 *  Performance optimization:
		 *
		 *  In case the given source registry already is an instance of this class,
		 *  simply combine the key prefixes and reference only the original registry.
		 *  This prevents costly chaining of SubRegistry instances and ensures that we
		 *  only ever get a direct pointer to the first foreign implementation in the
		 *  registry hierarchy.
		 */
		if(source instanceof SubRegistry) {
			SubRegistry sub = (SubRegistry) source;
			prefix = sub.prefix+'.'+normalizePrefix(prefix);
			source = sub.source;
		}

		this.prefix = prefix;
		this.source = source;
	}

	private static String normalizePrefix(String prefix) {
		return prefix.charAt(0)=='.' ? prefix.substring(1) : prefix;
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#getValue(java.lang.String)
	 */
	@Override
	public String getValue(String key) {
		key = prefix+'.'+key;

		return source.getValue(key);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#setValue(java.lang.String, java.lang.String)
	 */
	@Override
	public void setValue(String key, String value) {
		key = prefix+'.'+key;

		source.setValue(key, value);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#beginUpdate()
	 */
	@Override
	public void beginUpdate() {
		source.beginUpdate();
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#endUpdate()
	 */
	@Override
	public void endUpdate() {
		source.endUpdate();
	}

	/**
	 * Erases from the backing registry all entries starting with the
	 * specified {@code prefix}.
	 * This is done by first traversing all entries in the source registry and
	 * collecting those with the matching prefix. Afterwards all those collected
	 * keys are used to sequentially erase entries from the backing registry.
	 *
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#delete()
	 */
	@Override
	public void delete() {
		final List<String> keys = new ArrayList<>();

		// Collect keys
		source.forEachEntry(prefix, (key, value) -> keys.add(key));

		// Now delete entries
		source.beginUpdate();
		try {
			for(String key : keys) {
				source.setValue(key, null);
			}
		} finally {
			source.endUpdate();
		}
	}

	/**
	 * Since this implementation doesn't store data by itself, this method
	 * does nothing.
	 *
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#close()
	 */
	@Override
	public void close() {
		// no-op
	}

	/**
	 * Since this implementation doesn't store data by itself, this method
	 * does nothing.
	 *
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#open()
	 */
	@Override
	public void open() {
		// no-op
	}

	@Override
	public void forEachEntry(BiConsumer<? super String, ? super String> action) {
		final int cutoffPoint = prefix.length();
		String prefix = this.prefix+'.';
		source.forEachEntry(prefix, (key, value) -> {
			key = key.substring(cutoffPoint);
			action.accept(key, value);
		});
	}

	/**
	 * Combines the given {@code prefix} with the internal prefix of this registry to
	 * {@code local_prefix.prefix} and then delegates to the wrapped registry via
	 * {@link MetadataRegistry#forEachEntry(String, BiConsumer)}.
	 * Note that the keys forwarded to the given {@code action} will have their prefix
	 * matching this registry's prefix removed.
	 *
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#forEachEntry(java.lang.String, java.util.function.BiConsumer)
	 */
	@Override
	public void forEachEntry(String prefix,
			BiConsumer<? super String, ? super String> action) {

		requireNonNull(prefix);

		final int cutoffPoint = this.prefix.length();
		prefix = this.prefix+'.'+normalizePrefix(prefix);

		source.forEachEntry(prefix, (key, value) -> {
			key = key.substring(cutoffPoint);
			action.accept(key, value);
		});
	}
}
