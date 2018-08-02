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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface DocumentableTest extends LockableTest {

	/**
	 * Create an unlocked instance of {@link Documentable} for testing
	 * @return
	 */
	@Override
	Documentable createUnlocked();

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentable#getDocumentation()}.
	 */
	@Test
	default void testGetDocumentation() {
		Documentable documentable = createUnlocked();

		assertNull(documentable.getDocumentation());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentable#setDocumentation(de.ims.icarus2.model.manifest.api.Documentation)}.
	 */
	@Test
	default void testSetDocumentation() {
		Documentable documentable = createUnlocked();

		// Test with null
		TestUtils.assertNPE(() -> documentable.setDocumentation(null));

		// Test with real value
		Documentation documentation = mock(Documentation.class);
		documentable.setDocumentation(documentation);
		assertSame(documentation, documentable.getDocumentation());

		// Test locking
		assertFalse(documentable.isLocked());
		documentable.lock();
		LockableTest.assertLocked(() -> documentable.setDocumentation(null));
	}

}
