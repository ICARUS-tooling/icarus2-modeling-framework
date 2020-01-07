/**
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
package de.ims.icarus2.model.manifest.standard;

import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.settings;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.CategoryTest;
import de.ims.icarus2.model.manifest.api.LockableTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class DefaultCategoryTest implements CategoryTest<DefaultCategory>, LockableTest<DefaultCategory> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.IdentityTest#createFromIdentity(java.lang.String, java.lang.String, java.lang.String, javax.swing.Icon)
	 */
	@Override
	public DefaultCategory createFromIdentity(String id, String name, String description) {
		DefaultCategory category = new DefaultCategory();
		category.setId(id);
		category.setName(name);
		category.setDescription(description);
		return category;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends DefaultCategory> getTestTargetClass() {
		return DefaultCategory.class;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public DefaultCategory createTestInstance(TestSettings settings) {
		return settings.process(new DefaultCategory());
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.CategoryTest#createWithNamespace(java.lang.String)
	 */
	@Override
	public DefaultCategory createWithNamespace(String namespace) {
		DefaultCategory category = new DefaultCategory();
		category.setNamespace(namespace);
		return category;
	}

	@Test
	public void testSetNamespace() {
		assertLockableSetter(settings(), DefaultCategory::setNamespace,
				"namespace", true, NO_CHECK);
	}
}
