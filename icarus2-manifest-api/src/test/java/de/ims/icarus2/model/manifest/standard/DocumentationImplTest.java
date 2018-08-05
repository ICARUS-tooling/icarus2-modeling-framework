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
package de.ims.icarus2.model.manifest.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.Documentation;
import de.ims.icarus2.model.manifest.api.DocumentationTest;

/**
 * @author Markus Gärtner
 *
 */
class DocumentationImplTest implements DocumentationTest<Documentation> {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.DocumentationImpl#DocumentationImpl()}.
	 */
	@Test
	void testDocumentationImpl() {
		DocumentationImpl impl = new DocumentationImpl();
		assertNull(impl.getContent());
		assertTrue(impl.getResources().isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.DocumentationImpl#DocumentationImpl(java.lang.String)}.
	 */
	@Test
	void testDocumentationImplString() {
		DocumentationImpl impl = new DocumentationImpl("content");
		assertEquals("content", impl.getContent());
		assertTrue(impl.getResources().isEmpty());
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DocumentationTest#createUnlocked()
	 */
	@Override
	public Documentation createUnlocked() {
		return new DocumentationImpl();
	}

}
