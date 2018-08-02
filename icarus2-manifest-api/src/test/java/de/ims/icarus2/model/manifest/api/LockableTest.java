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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.model.manifest.ManifestErrorCode;

/**
 * @author Markus Gärtner
 *
 */
public interface LockableTest {

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

	Lockable createUnlocked();

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Lockable#lock()}.
	 */
	@Test
	default void testLock() {
		Lockable lockable = createUnlocked();

		lockable.lock();

		assertTrue(lockable.isLocked());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Lockable#isLocked()}.
	 */
	@Test
	default void testIsLocked() {
		Lockable lockable = createUnlocked();

		assertFalse(lockable.isLocked());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Lockable#checkNotLocked()}.
	 */
	@Test
	default void testCheckNotLocked() {
		Lockable lockable = createUnlocked();

		lockable.checkNotLocked();

		lockable.lock();

		assertLocked(() -> lockable.checkNotLocked());
	}

}
