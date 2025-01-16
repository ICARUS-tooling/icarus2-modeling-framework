/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.test.util;

import java.util.stream.IntStream;

import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
public class Unicodes {

	private static char strictToChar(int v) {
		if(v<Character.MIN_VALUE || v>Character.MAX_VALUE)
			throw new IllegalArgumentException("Outside of char space: "+v);
		return (char)v;
	}

	public static IntStream validBMPChars() {
		return IntStream.of(
				Character.MIN_VALUE, Character.MIN_VALUE+1,
				Character.MIN_SUPPLEMENTARY_CODE_POINT>>>1,
				Character.MIN_SUPPLEMENTARY_CODE_POINT-1
		);
	}

	public static IntStream randomSupplementaryCodepoints(RandomGenerator rng) {
		return rng.ints(Character.MIN_SUPPLEMENTARY_CODE_POINT, Character.MAX_CODE_POINT+1);
	}

	public static IntStream randomSupplementaryCodepoints(RandomGenerator rng, int size) {
		return randomSupplementaryCodepoints(rng).limit(size);
	}

	public static IntStream randomBMPCodepoints(RandomGenerator rng) {
		return rng.ints(Character.MIN_VALUE, Character.MIN_SURROGATE);
	}

	public static IntStream randomBMPCodepoints(RandomGenerator rng, int size) {
		return randomBMPCodepoints(rng).limit(size);
	}

	public static int randomSupplementaryCodepoint(RandomGenerator rng) {
		return rng.random(Character.MIN_SUPPLEMENTARY_CODE_POINT, Character.MAX_CODE_POINT+1);
	}

	public static char randomHighSurrogate(RandomGenerator rng) {
		return strictToChar(rng.random(Character.MIN_HIGH_SURROGATE, Character.MAX_HIGH_SURROGATE+1));
	}

	public static char randomLowSurrogate(RandomGenerator rng) {
		return strictToChar(rng.random(Character.MIN_LOW_SURROGATE, Character.MAX_LOW_SURROGATE+1));
	}

	public static char randomBMPCodepoint(RandomGenerator rng) {
		return strictToChar(rng.random(Character.MIN_VALUE, Character.MIN_SURROGATE+1));
	}
}
