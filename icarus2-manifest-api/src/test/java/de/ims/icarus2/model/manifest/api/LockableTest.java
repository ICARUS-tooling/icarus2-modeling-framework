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

import static de.ims.icarus2.test.TestUtils.settings;
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
import de.ims.icarus2.model.manifest.ManifestFrameworkTest;
import de.ims.icarus2.model.manifest.ManifestTestFeature;
import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.util.function.ObjBoolConsumer;

/**
 * @author Markus Gärtner
 *
 */
public interface LockableTest<L extends Lockable> extends ManifestFrameworkTest<L> {

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

	@Provider
	default L createUnlocked(TestSettings settings) {
		return createTestInstance(settings.clone().withFeatures(ManifestTestFeature.UNLOCKED));
	}

	@Provider
	default L createUnlocked() {
		return createUnlocked(settings());
	}

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
			boolean checkNPE, BiConsumer<Executable, String> legalityCheck, @SuppressWarnings("unchecked") K...illegalValues) {
		assertLockableSetter(createUnlocked(), setter, value, checkNPE, legalityCheck, illegalValues);
	}

	@SuppressWarnings("unchecked")
	public static <L extends Lockable, K extends Object> void assertLockableSetter(L lockable, BiConsumer<L, K> setter,
			K value, boolean checkNPE, BiConsumer<Executable, String> legalityCheck, K...illegalValues) {
		assertLockableSetter(lockable, setter, (K[]) new Object[] {value}, checkNPE, legalityCheck, illegalValues);
	}

	default <K extends Object> void assertLockableSetter(BiConsumer<L, K> setter, K[] values,
			boolean checkNPE, BiConsumer<Executable, String> legalityCheck, @SuppressWarnings("unchecked") K...illegalValues) {
		assertLockableSetter(createUnlocked(), setter, values, checkNPE, legalityCheck, illegalValues);
	}

	public static <L extends Lockable, K extends Object> void assertLockableSetter(L lockable, BiConsumer<L, K> setter, K[] values,
			boolean checkNPE, BiConsumer<Executable, String> legalityCheck, @SuppressWarnings("unchecked") K...illegalValues) {
		ManifestTestUtils.assertSetter(lockable, setter, values, checkNPE, legalityCheck, illegalValues);

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
			K[] illegalValues, BiConsumer<Executable, String> legalityCheck, boolean checkNPE, BiConsumer<Executable, String> duplicateCheck, @SuppressWarnings("unchecked") K...values) {
		assertLockableAccumulativeAdd(createUnlocked(), adder, illegalValues, legalityCheck, checkNPE, duplicateCheck, values);
	}

	public static <L extends Lockable, K extends Object> void assertLockableAccumulativeAdd(
			L lockable, BiConsumer<L, K> adder,
			K[] illegalValues, BiConsumer<Executable, String> legalityCheck, boolean checkNPE, BiConsumer<Executable, String> duplicateCheck, @SuppressWarnings("unchecked") K...values) {
		ManifestTestUtils.assertAccumulativeAdd(lockable, adder, illegalValues, legalityCheck, checkNPE, duplicateCheck, values);

		lockable.lock();

		LockableTest.assertLocked(() -> adder.accept(lockable, values[values.length-1]));
	}

	default <K extends Object, C extends Collection<K>> void assertLockableAccumulativeRemove(
			BiConsumer<L, K> adder, BiConsumer<L, K> remover,
			Function<L, C> getter, boolean checkNPE,
			BiConsumer<Executable, String> invalidRemoveCheck, @SuppressWarnings("unchecked") K...values) {
		assertLockableAccumulativeRemove(createUnlocked(), adder, remover, getter, checkNPE, invalidRemoveCheck, values);
	}

	public static <L extends Lockable, K extends Object, C extends Collection<K>> void assertLockableAccumulativeRemove(
			L lockable, BiConsumer<L, K> adder, BiConsumer<L, K> remover,
			Function<L, C> getter, boolean checkNPE,
			BiConsumer<Executable, String> invalidRemoveCheck, @SuppressWarnings("unchecked") K...values) {
		ManifestTestUtils.assertAccumulativeRemove(lockable, adder, remover, getter, checkNPE, invalidRemoveCheck, values);

		lockable.lock();

		LockableTest.assertLocked(() -> remover.accept(lockable, values[0]));
	}
}
