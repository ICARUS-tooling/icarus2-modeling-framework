/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.driver.indices;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.annotations.Provider;

/**
 * Provides default tests to check the handling of invalid
 * input for the {@link IndexCollector} interface.
 *
 * @author Markus Gärtner
 *
 */
public interface IndexCollectorTest<C extends IndexCollector> {

	@Provider
	C create();

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexCollector#add(long, long)}.
	 */
	@Test
	default void testAddLongLong() {
		IndexCollector collector = create();
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> collector.add(0, -1)),
				() -> assertThrows(IllegalArgumentException.class, () -> collector.add(-1, 0)),
				() -> assertThrows(IllegalArgumentException.class, () -> collector.add(-2, -1))
		);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexCollector#add(de.ims.icarus2.model.api.driver.indices.IndexSet)}.
	 */
	@Test
	default void testAddIndexSet() {
		assertNPE(() -> create().add((IndexSet)null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexCollector#add(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
	 */
	@Test
	default void testAddIndexSetArray() {
		assertNPE(() -> create().add((IndexSet[])null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexCollector#add(java.util.List)}.
	 */
	@Test
	default void testAddListOfQextendsIndexSet() {
		assertNPE(() -> create().add((List<? extends IndexSet>)null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexCollector#accept(de.ims.icarus2.model.api.driver.indices.IndexSet)}.
	 */
	@Test
	default void testAcceptIndexSet() {
		assertNPE(() -> create().accept((IndexSet)null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexCollector#add(java.util.PrimitiveIterator.OfLong)}.
	 */
	@Test
	default void testAddOfLong() {
		assertNPE(() -> create().add((OfLong)null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexCollector#add(java.util.PrimitiveIterator.OfInt)}.
	 */
	@Test
	default void testAddOfInt() {
		assertNPE(() -> create().add((OfInt)null));
	}

	@Test
	default void testNegativeInput() {
		assertModelException(GlobalErrorCode.INVALID_INPUT,
				() -> create().add(-1L));
	}
}
