/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.version.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.util.version.common.MajorMinorReleaseVersionFormat.MajorMinorReleaseVersion;

/**
 * @author Markus Gärtner
 *
 */
public class MajorMinorReleaseVersionFormatTest {

	private MajorMinorReleaseVersionFormat format;

	@BeforeEach
	public void prepare() {
		format = new MajorMinorReleaseVersionFormat();
	}

	@Test
	public void testLegitValues() throws Exception {

		test("1.1.1", 1, 1, 1, null);
		test("0.1", 0, 1, 0, null);
		test("0.0.1", 0, 0, 1, null);
		test("0.0.1 beta", 0, 0, 1, "beta");
		test("1", 1, 0, 0, null);
		test("1.0", 1, 0, 0, null);
		test("1.0.0", 1, 0, 0, null);
		test("1.0.0 alpha", 1, 0, 0, "alpha");
		test("1.1.1 test", 1, 1, 1, "test");
	}

	private void test(String versionString, int major, int minor, int release, String info) {
		MajorMinorReleaseVersion version = format.parseVersion(versionString);

		assertNotNull(version);

		assertEquals(major, version.getMajor(), "major field:");
		assertEquals(minor, version.getMinor(), "minor field:");
		assertEquals(release, version.getRelease(), "release field:");
		assertEquals(info, version.getInfo(), "info field:");
		assertEquals(versionString, version.getVersionString(), "version string field:");
	}

	@Test
	public void testEmpty() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> {
			format.parseVersion("");
		});
	}

	@Test
	public void testNull() throws Exception {
		assertThrows(NullPointerException.class, () -> {
			format.parseVersion(null);
		});
	}
}
