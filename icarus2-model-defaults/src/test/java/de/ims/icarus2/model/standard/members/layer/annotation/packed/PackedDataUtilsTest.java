/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.layer.annotation.packed;

import static de.ims.icarus2.test.TestUtils.RUNS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;

import de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataUtils.Substitutor;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
class PackedDataUtilsTest {

	@Nested
	class ForSubstitutor {

		@RepeatedTest(RUNS)
		@RandomizedTest
		void testConsistency(RandomGenerator rng) {
			try(Substitutor<Object> sub = new Substitutor<>()) {
				Object[] items = rng.randomContent();
				Int2ObjectMap<Object> int2obj = new Int2ObjectOpenHashMap<>();
				Object2IntMap<Object> obj2int = new Object2IntOpenHashMap<>();

				// Fill sub with data
				for(Object item : items) {
					int replacement = sub.applyAsInt(item);
					assertTrue(replacement>=0, "Invalid replacement");
					assertFalse(int2obj.containsKey(replacement), "Duplicate replacement");
					int2obj.put(replacement, item);
					obj2int.put(item, replacement);
				}

				// Verify consistency across repeated substitutions
				for(Object item : items) {
					int replacement = sub.applyAsInt(item);
					assertEquals(obj2int.getInt(item), replacement, "Inconsistent substitution for item: "+item);
				}


				// Verify consistency through re-substitution
				for(int replacement : int2obj.keySet().toIntArray()) {
					Object item = sub.apply(replacement);
					assertEquals(int2obj.get(replacement), item, "Inconsistent resubstitution for replacement: "+replacement);
				}
			}
		}
	}
}
