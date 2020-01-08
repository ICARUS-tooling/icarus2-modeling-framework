/*
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
package de.ims.icarus2.model.standard.io;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.io.FileManagerTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.OverrideTest;

/**
 * @author Markus Gärtner
 *
 */
class DefaultFileManagerTest implements FileManagerTest<DefaultFileManager> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends DefaultFileManager> getTestTargetClass() {
		return DefaultFileManager.class;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public DefaultFileManager createTestInstance(TestSettings settings) {
		return new DefaultFileManager(Paths.get("."));
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	@OverrideTest
	@Test
	public void testMandatoryConstructors() throws Exception {
		create();
	}
}
