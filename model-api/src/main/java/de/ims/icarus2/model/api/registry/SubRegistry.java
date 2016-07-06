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
package de.ims.icarus2.model.api.registry;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;

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
		checkNotNull(source);
		checkNotNull(prefix);
		checkArgument("Prefix must not be empty", !prefix.trim().isEmpty());

		/*
		 *  Performance optimization:
		 *
		 *  In case the given source registry already is an instance of this class,
		 *  simply combine the key prefixes and reference only at the original registry.
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
		source.forEachEntry((key, value) -> {
			if(key.startsWith(prefix)) {
				keys.add(key);
			}
		});

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

		checkNotNull(prefix);

		final int cutoffPoint = this.prefix.length();
		prefix = this.prefix+'.'+normalizePrefix(prefix);

		source.forEachEntry(prefix, (key, value) -> {
			key = key.substring(cutoffPoint);
			action.accept(key, value);
		});
	}
}
