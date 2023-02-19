/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * @author Markus Gärtner
 *
 */
class MemberFlagsTest {

	private List<DynamicTest> test(IntPredicate method, int positive) {
		return test(method, positive, without(positive));
	}

	private List<DynamicTest> test(IntPredicate method, int positive, int...negatives) {

		List<DynamicTest> tests = new ArrayList<>();

		tests.add(dynamicTest("positive: "+positive,
				() -> assertTrue(method.test(positive))));

		IntStream.of(negatives)
			.mapToObj(negative -> dynamicTest("negative: "+negative,
					() -> assertFalse(method.test(negative))))
			.forEach(tests::add);

		return tests;
	}

	private static final int[] flags = {
			MemberFlags.ITEM_ALIVE,
			MemberFlags.ITEM_DIRTY,
			MemberFlags.ITEM_LOCKED,
			MemberFlags.ITEMS_COMPLETE,
			MemberFlags.EDGES_COMPLETE,
			MemberFlags.STRUCTURE_AUGMENTED,
	};

	private int[] without(int flag) {
		return without(flag, flags);
	}

	private int[] without(int flag, int[] source) {
		int[] result = IntStream.of(source)
				.filter(i -> i!=flag)
				.toArray();
		assertEquals(source.length-1, result.length);
		return result;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.MemberFlags#isItemAlive(int)}.
	 */
	@TestFactory
	List<DynamicTest> testIsItemAlive() {
		return test(MemberFlags::isItemAlive, MemberFlags.ITEM_ALIVE);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.MemberFlags#isItemLocked(int)}.
	 */
	@TestFactory
	List<DynamicTest> testIsItemLocked() {
		return test(MemberFlags::isItemLocked, MemberFlags.ITEM_LOCKED);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.MemberFlags#isItemDirty(int)}.
	 */
	@TestFactory
	List<DynamicTest> testIsItemDirty() {
		return test(MemberFlags::isItemDirty, MemberFlags.ITEM_DIRTY);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.MemberFlags#isItemUsable(int)}.
	 */
	@TestFactory
	List<DynamicTest> testIsItemUsable() {
		return test(MemberFlags::isItemUsable, MemberFlags.ITEM_DEFAULT_FLAGS);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.MemberFlags#isItemsComplete(int)}.
	 */
	@TestFactory
	List<DynamicTest> testIsItemsComplete() {
		return test(MemberFlags::isItemsComplete, MemberFlags.ITEMS_COMPLETE);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.MemberFlags#isEdgesComplete(int)}.
	 */
	@TestFactory
	List<DynamicTest> testIsEdgesComplete() {
		return test(MemberFlags::isEdgesComplete, MemberFlags.EDGES_COMPLETE);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.MemberFlags#isStructureAugmented(int)}.
	 */
	@TestFactory
	List<DynamicTest> testIsStructureAugmented() {
		return test(MemberFlags::isStructureAugmented, MemberFlags.STRUCTURE_AUGMENTED);
	}

}
