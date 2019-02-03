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
/**
 *
 */
package de.ims.icarus2.test;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * @author Markus Gärtner
 *
 */
public class Randomizer<T extends Object> implements Supplier<T> {

	public static <T> Randomizer<T> from(List<? extends T> items) {
		return new Randomizer<>(items.size(), items::get);
	}

	public static <T> Randomizer<T> from(Collection<? extends T> items) {
		return from(new ArrayList<>(items));
	}

	@SafeVarargs
	public static <T> Randomizer<T> from(T...items) {
		return new Randomizer<>(items.length, idx -> items[idx]);
	}

	private final IntFunction<T> mapper;
	private final int size;
	private final Random rng = new Random(System.currentTimeMillis());

	public Randomizer(int size, IntFunction<T> mapper) {
		if(size<2)
			throw new IllegalArgumentException("No point in ranodmizing collection with less than 2 elements...");

		this.size = size;
		this.mapper = requireNonNull(mapper);
	}

	public T randomize() {
		int index = rng.nextInt(size);
		return mapper.apply(index);
	}

	/**
	 * @see java.util.function.Supplier#get()
	 */
	@Override
	public T get() {
		return randomize();
	}

	public static Randomizer<Byte> BYTE = new Randomizer<>(Byte.MAX_VALUE, v -> Byte.valueOf((byte) v));
	public static Randomizer<Integer> INTEGER = new Randomizer<>(Integer.MAX_VALUE, v -> Integer.valueOf(v));
}
