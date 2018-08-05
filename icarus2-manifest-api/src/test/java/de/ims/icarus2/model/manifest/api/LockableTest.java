/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.util.function.ObjBoolConsumer;

/**
 * @author Markus Gärtner
 *
 */
public interface LockableTest<L extends Lockable> {

	/**
	 * Expects a {@link ManifestException} of type {@link ManifestErrorCode#MANIFEST_LOCKED}
	 * when running the given {@link Executable}.
	 *
	 * @param executable
	 */
	public static void assertLocked(Executable executable) {
		ManifestException exception = assertThrows(ManifestException.class, executable);
		assertEquals(ManifestErrorCode.MANIFEST_LOCKED, exception.getErrorCode());
	}

	L createUnlocked();

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Lockable#lock()}.
	 */
	@Test
	default void testLock() {
		L lockable = createUnlocked();

		lockable.lock();

		assertTrue(lockable.isLocked());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Lockable#isLocked()}.
	 */
	@Test
	default void testIsLocked() {
		L lockable = createUnlocked();

		assertFalse(lockable.isLocked());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Lockable#checkNotLocked()}.
	 */
	@Test
	default void testCheckNotLocked() {
		L lockable = createUnlocked();

		lockable.checkNotLocked();

		lockable.lock();

		assertLocked(() -> lockable.checkNotLocked());
	}

	default <K extends Object> void assertLockableSetter(BiConsumer<L, K> setter, K value,
			boolean checkNPE, @SuppressWarnings("unchecked") K...illegalValues) {
		assertLockableSetter(createUnlocked(), setter, value, checkNPE, illegalValues);
	}

	@SuppressWarnings("unchecked")
	public static <L extends Lockable, K extends Object> void assertLockableSetter(L lockable, BiConsumer<L, K> setter, K value,
			boolean checkNPE, K...illegalValues) {
		assertLockableSetter(lockable, setter, (K[]) new Object[] {value}, checkNPE, illegalValues);
	}

	default <K extends Object> void assertLockableSetter(BiConsumer<L, K> setter, K[] values,
			boolean checkNPE, @SuppressWarnings("unchecked") K...illegalValues) {
		assertLockableSetter(createUnlocked(), setter, values, checkNPE, illegalValues);
	}

	public static <L extends Lockable, K extends Object> void assertLockableSetter(L lockable, BiConsumer<L, K> setter, K[] values,
			boolean checkNPE, @SuppressWarnings("unchecked") K...illegalValues) {
		ManifestTestUtils.assertSetter(lockable, setter, values, checkNPE, illegalValues);

		lockable.lock();

		LockableTest.assertLocked(() -> setter.accept(lockable, values[0]));
	}

	default void assertLockableSetter(ObjBoolConsumer<L> setter) {
		assertLockableSetter(createUnlocked(), setter);
	}

	public static <L extends Lockable> void assertLockableSetter(L lockable, ObjBoolConsumer<L> setter) {
		ManifestTestUtils.assertSetter(lockable, setter);

		lockable.lock();

		LockableTest.assertLocked(() -> setter.accept(lockable, true));
	}

	default <K extends Object> void assertLockableAccumulativeAdd(BiConsumer<L, K> adder,
			K[] illegalValues, boolean checkNPE, boolean checkDuplicate, @SuppressWarnings("unchecked") K...values) {
		assertLockableAccumulativeAdd(createUnlocked(), adder, illegalValues, checkNPE, checkDuplicate, values);
	}

	public static <L extends Lockable, K extends Object> void assertLockableAccumulativeAdd(
			L lockable, BiConsumer<L, K> adder,
			K[] illegalValues, boolean checkNPE, boolean checkDuplicate, @SuppressWarnings("unchecked") K...values) {
		ManifestTestUtils.assertAccumulativeAdd(lockable, adder, illegalValues, checkNPE, checkDuplicate, values);

		lockable.lock();

		LockableTest.assertLocked(() -> adder.accept(lockable, values[values.length-1]));
	}

	default <K extends Object, C extends Collection<K>> void assertLockableAccumulativeRemove(
			BiConsumer<L, K> adder, BiConsumer<L, K> remover,
			Function<L, C> getter, boolean checkNPE, boolean checkInvalidRemove,
					@SuppressWarnings("unchecked") K...values) {
		assertLockableAccumulativeRemove(createUnlocked(), adder, remover, getter, checkNPE, checkInvalidRemove, values);
	}

	public static <L extends Lockable, K extends Object, C extends Collection<K>> void assertLockableAccumulativeRemove(
			L lockable, BiConsumer<L, K> adder, BiConsumer<L, K> remover,
			Function<L, C> getter, boolean checkNPE, boolean checkInvalidRemove,
					@SuppressWarnings("unchecked") K...values) {
		ManifestTestUtils.assertAccumulativeRemove(lockable, adder, remover, getter, checkNPE, checkInvalidRemove, values);

		lockable.lock();

		LockableTest.assertLocked(() -> remover.accept(lockable, values[0]));
	}
}
