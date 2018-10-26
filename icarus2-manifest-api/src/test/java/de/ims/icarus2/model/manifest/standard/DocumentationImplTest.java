/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.test.TestUtils.assertNotPresent;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.DocumentationTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class DocumentationImplTest implements DocumentationTest<DocumentationImpl> {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.DocumentationImpl#DocumentationImpl()}.
	 */
	@Test
	void testDocumentationImpl() {
		DocumentationImpl impl = new DocumentationImpl();
		assertNotPresent(impl.getContent());
		assertTrue(impl.getResources().isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.DocumentationImpl#DocumentationImpl(java.lang.String)}.
	 */
	@Test
	void testDocumentationImplString() {
		DocumentationImpl impl = new DocumentationImpl("content");
		assertOptionalEquals("content", impl.getContent());
		assertTrue(impl.getResources().isEmpty());
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public DocumentationImpl createTestInstance(TestSettings settings) {
		return settings.process(new DocumentationImpl());
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends DocumentationImpl> getTestTargetClass() {
		return DocumentationImpl.class;
	}

}
