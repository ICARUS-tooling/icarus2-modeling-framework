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
package de.ims.icarus2.util;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import de.ims.icarus2.util.strings.StringResource;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * A simple utility class for enum implementations of the
 * {@link StringResource} interface.
 *
 * @author Markus Gärtner
 *
 * @param <F> type of the elements stored
 * @param <K> key type used for lookup
 */
public class LazyStore<F extends Object, K extends Object> {

	public static <S extends StringResource> LazyStore<S, String> forStringResource(Class<S> clazz) {
		return new LazyStore<>(clazz, StringResource::getStringValue);
	}

	private Map<K, F> lookup;

	private final Class<F> clazz;

	private final Function<F, K> keyGen;
	private final Function<K, K> keyMod;

	/**
	 * @param clazz enum class from which to obtain instances
	 * @param keyGen function to generate lookup keys. a {@code null} key indicates that an
	 * instance should be ignored.
	 */
	public LazyStore(Class<F> clazz, Function<F, K> keyGen) {
		this(clazz, keyGen, null);
	}

	/**
	 * @param clazz enum class from which to obtain instances
	 * @param keyGen function to generate lookup keys. a {@code null} key indicates that an
	 * instance should be ignored.
	 */
	public LazyStore(Class<F> clazz, Function<F, K> keyGen, @Nullable Function<K, K> keyMod) {
		this.clazz = requireNonNull(clazz);
		this.keyGen = requireNonNull(keyGen);
		this.keyMod = keyMod;
		checkArgument("Class must be an enum type", clazz.isEnum());
	}

	private K adjust(K key) {
		return keyMod==null ? key : keyMod.apply(key);
	}

	private void ensureLookup() {
		if(lookup==null) {
			lookup = new Object2ObjectOpenHashMap<>();

			F[] values = clazz.getEnumConstants();
			for(F value : values) {
				K generatedKey = keyGen.apply(value);
				if(generatedKey!=null) {
					lookup.put(adjust(generatedKey), value);
				}
			}
		}
	}

	public synchronized F lookup(K key) {
		requireNonNull(key);
		ensureLookup();

		F value = lookup.get(adjust(key));
		if(value==null)
			throw new IllegalArgumentException("Unknown key: "+key);
		return value;
	}

	public synchronized F lookup(K key, Function<K, ? extends RuntimeException> exGen) {
		requireNonNull(key);
		ensureLookup();

		F value = lookup.get(adjust(key));
		if(value==null)
			throw exGen.apply(key);
		return value;
	}

	public synchronized boolean hasKey(K key) {
		requireNonNull(key);
		ensureLookup();
		return lookup.containsKey(adjust(key));
	}
}
