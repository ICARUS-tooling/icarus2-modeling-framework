/**
 *
 */
package de.ims.icarus2.util.version.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.ims.icarus2.util.version.common.MajorMinorReleaseVersionFormat.MajorMinorReleaseVersion;

/**
 * @author Markus
 *
 */
public class MajorMinorReleaseVersionFormatTest {

	@Rule
	public ExpectedException thrown= ExpectedException.none();

	private MajorMinorReleaseVersionFormat format;

	@Before
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

		assertEquals("major field:", major, version.getMajor());
		assertEquals("minor field:", minor, version.getMinor());
		assertEquals("release field:", release, version.getRelease());
		assertEquals("info field:", info, version.getInfo());
		assertEquals("version string field:", versionString, version.getVersionString());
	}

	@Test
	public void testEmpty() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		format.parseVersion("");
	}

	@Test
	public void testNull() throws Exception {
		thrown.expect(NullPointerException.class);
		format.parseVersion(null);
	}
}
