/**
 *
 */
package de.ims.icarus2.model.standard.members.layer.annotation.packed;

import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.randomContent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;

import de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataUtils.Substitutor;
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
		void testConsistency() {
			try(Substitutor<Object> sub = new Substitutor<>()) {
				Object[] items = randomContent();
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
