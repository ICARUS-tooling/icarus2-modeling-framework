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
package de.ims.icarus2.test;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * @author Markus Gärtner
 *
 */
public class Randomizer<T extends Object> {

	private final List<T> buffer = new ArrayList<>();
	private final Random rng = new Random(System.currentTimeMillis());

	public Randomizer(Collection<? extends T> items) {
		requireNonNull(items);
		if(items.size()<2)
			throw new IllegalArgumentException("No point in ranodmizing collection with less than 2 elements...");
		buffer.addAll(items);
	}

	public T randomize() {
		int index = rng.nextInt(buffer.size());
		return buffer.get(index);
	}
}
