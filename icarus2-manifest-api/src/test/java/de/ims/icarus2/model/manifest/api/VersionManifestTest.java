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
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.test.TestUtils.DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.settings;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.asserter.Equals;

/**
 * @author Markus Gärtner
 *
 */
public interface VersionManifestTest extends LockableTest<VersionManifest>, TypedManifestTest<VersionManifest> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.VERSION;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.VersionManifest#getFormatId()}.
	 */
	@Test
	default void testGetFormatId() {
		assertGetter(createUnlocked(),
				"format1", "format2",
				DEFAULT(VersionManifest.DEFAULT_VERSION_FORMAT_ID),
				VersionManifest::getFormatId,
				VersionManifest::setFormatId);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.VersionManifest#getVersionString()}.
	 */
	@Test
	default void testGetVersionString() {
		assertGetter(createUnlocked(),
				"version1", "version2", NO_DEFAULT(),
				VersionManifest::getVersionString,
				VersionManifest::setVersionString);
	}

	@Provider
	default VersionManifest createVersionManifest(String formatId, String versionString) {
		VersionManifest manifest = createUnlocked();
		manifest.setFormatId(formatId);
		manifest.setVersionString(versionString);
		manifest.lock();
		return manifest;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.VersionManifest#equals(java.lang.Object)}.
	 */
	@Test
	default void testEquals() {
		String format1 = "format1";
		String format2 = "format2";
		String version1 = "version1";
		String version2 = "version2";

		Equals.create(createVersionManifest(format1, version1))
			.addEqual(createVersionManifest(format1, version1))
			.addUnequal(new Object())
			.addUnequal(createVersionManifest(format2, version1),
					createVersionManifest(format1, version2),
					createVersionManifest(format2, version2))
			.test();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.VersionManifest#setFormatId(java.lang.String)}.
	 */
	@Test
	default void testSetFormatId() {
		assertLockableSetter(settings(),
				VersionManifest::setFormatId,
				"format", NPE_CHECK,
				ManifestTestUtils.INVALID_INPUT_CHECK, "");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.VersionManifest#setVersionString(java.lang.String)}.
	 */
	@Test
	default void testSetVersionString() {
		assertLockableSetter(settings(),
				VersionManifest::setVersionString,
				"version", NPE_CHECK,
				ManifestTestUtils.INVALID_INPUT_CHECK, "");
	}

}
