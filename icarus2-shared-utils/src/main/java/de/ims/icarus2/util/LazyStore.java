/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.util;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.function.Function;

import de.ims.icarus2.util.strings.StringResource;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * A simple utility class for enum implementations of the
 * {@link StringResource} interface.
 *
 * @author Markus Gärtner
 *
 */
public class LazyStore<F extends Object, K extends Object> {

	public static <S extends StringResource> LazyStore<S, String> forStringResource(Class<S> clazz) {
		return new LazyStore<>(clazz, StringResource::getStringValue);
	}

	private Map<K, F> lookup;

	private final Class<F> clazz;

	private final Function<F, K> keyGen;

	/**
	 * @param clazz enum class from which to obtain instances
	 * @param keyGen function to generate lookup keys. a {@code null} key indicates that an
	 * instance should be ignored.
	 */
	public LazyStore(Class<F> clazz, Function<F, K> keyGen) {
		this.clazz = requireNonNull(clazz);
		this.keyGen = requireNonNull(keyGen);
		checkArgument(clazz.isEnum());
	}

	public synchronized F lookup(K key) {
		requireNonNull(key);

		if(lookup==null) {
			lookup = new Object2ObjectOpenHashMap<>();

			F[] values = clazz.getEnumConstants();
			for(F value : values) {
				K generatedKey = keyGen.apply(value);
				if(generatedKey!=null) {
					lookup.put(generatedKey, value);
				}
			}
		}

		F flag = lookup.get(key);
		if(flag==null)
			throw new IllegalArgumentException("Unknown key: "+key);
		return flag;
	}
}
