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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.util.collections;

import static de.ims.icarus2.util.Conditions.checkNotNull;
import gnu.trove.map.hash.THashMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LazyMap<K extends Object, V extends Object> implements BiConsumer<K, V> {

	public static <K extends Object, V extends Object> LazyMap<K, V> lazyHashMap(final int capacity) {
		return new LazyMap<>(()-> new HashMap<>(capacity));
	}

	public static <K extends Object, V extends Object> LazyMap<K, V> lazyHashMap() {
		return new LazyMap<>(()-> new HashMap<>());
	}

	public static <K extends Object, V extends Object> LazyMap<K, V> lazyTHashMap(final int capacity) {
		return new LazyMap<>(()-> new THashMap<>(capacity));
	}

	public static <K extends Object, V extends Object> LazyMap<K, V> lazyTHashMap() {
		return new LazyMap<>(()-> new THashMap<>());
	}

	public static <K extends Object, V extends Object> LazyMap<K, V> lazyNavigableMap() {
		return new LazyMap<>(()-> new TreeMap<>());
	}


	private final Supplier<Map<K, V>> supplier;

	private Map<K, V> buffer;

	public LazyMap(Supplier<Map<K, V>> supplier) {
		checkNotNull(supplier);

		this.supplier = supplier;
	}

	public void add(K key, V value) {
		if(key==null || value==null) {
			return;
		}

		if(buffer==null) {
			buffer = supplier.get();
		}

		buffer.put(key, value);
	}

	public void addAll(Map<? extends K, ? extends V> items) {
		if(items==null || items.isEmpty()) {
			return;
		}

		if(buffer==null) {
			buffer = supplier.get();
		}

		buffer.putAll(items);
	}

	/**
	 * @see java.util.function.BiConsumer#accept(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void accept(K key, V value) {
		add(key, value);
	}

	public <C extends Map<K, V>> C get() {
		@SuppressWarnings("unchecked")
		C result = (C) buffer;

		return result;
	}

	@SuppressWarnings("unchecked")
	public <M extends Map<K, V>> M getAsMap() {
		M result = (M) buffer;

		if(result==null) {
			result = (M) Collections.emptyMap();
		}

		return result;
	}

}
