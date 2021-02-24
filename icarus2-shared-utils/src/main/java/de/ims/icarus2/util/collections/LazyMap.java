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
package de.ims.icarus2.util.collections;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class LazyMap<K extends Object, V extends Object> implements BiConsumer<K, V> {

	public static <K extends Object, V extends Object> LazyMap<K, V> lazyNativeHashMap(final int capacity) {
		return new LazyMap<>(()-> new HashMap<>(capacity));
	}

	public static <K extends Object, V extends Object> LazyMap<K, V> lazyNativeHashMap() {
		return new LazyMap<>(HashMap::new);
	}

	public static <K extends Object, V extends Object> LazyMap<K, V> lazyHashMap(final int capacity) {
		return new LazyMap<>(()-> new Object2ObjectOpenHashMap<>(capacity));
	}

	public static <K extends Object, V extends Object> LazyMap<K, V> lazyHashMap() {
		return new LazyMap<>(Object2ObjectOpenHashMap::new);
	}

	public static <K extends Object, V extends Object> LazyMap<K, V> lazyNavigableMap() {
		return new LazyMap<>(TreeMap::new);
	}


	private final Supplier<Map<K, V>> supplier;

	private Map<K, V> buffer;

	public LazyMap(Supplier<Map<K, V>> supplier) {
		requireNonNull(supplier);

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

	public void addReverse(V value, K key) {
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
