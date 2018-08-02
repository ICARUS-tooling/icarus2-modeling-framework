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
package de.ims.icarus2.util.strings;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.annotations.PostponedTest;
import de.ims.icarus2.util.id.Identifiable;
import de.ims.icarus2.util.id.Identity;

class StringUtilTest {

	@Test
	void testGetCollator() {
		assertNotNull(StringUtil.getCollator());
	}

	@Test
	@PostponedTest
	void testCompareLocaleAware() {
		fail("Not yet implemented");
	}

	@Test
	void testFormatStringObjectArray() {
		assertEquals("xxyxx", StringUtil.format("xx{1}xx", "y"));
		assertEquals("xxyzxx", StringUtil.format("xx{1}{2}xx", "y", "z"));
		assertEquals("xxyxx", StringUtil.format("xx{}xx", "y"));
		assertEquals("xxyzxx", StringUtil.format("xx{}{}xx", "y", "z"));
		assertEquals("xxyyzxx", StringUtil.format("xx{}{1}{}xx", "y", "z"));
		assertEquals("xxyyxx", StringUtil.format("xx{1}{1}xx", "y"));
	}

	@Test
	@PostponedTest
	void testFormatLocaleStringObjectArray() {
		fail("Not yet implemented");
	}

	@Test
	void testIntern() {
		String s1 = "test123";

		char[] c = new char[s1.length()];
		s1.getChars(0, s1.length(), c, 0);
		String s2 = new String(c);

		assertNotSame(s1, s2);

		String sIntern = StringUtil.intern(s1);

		assertEquals(s1, sIntern);
		assertSame(sIntern, StringUtil.intern(s2));
	}

	@Test
	void testEqualsCharSequenceObject() {
		String s1 = "test123";
		String s2 = "test123";
		String s3 = "different";

		assertFalse(StringUtil.equals(s1, null));
		assertFalse(StringUtil.equals(s1, new Object()));
		assertFalse(StringUtil.equals(s1, s3));

		assertTrue(StringUtil.equals(s1, (Object)s1));
		assertTrue(StringUtil.equals(s1, (Object)s2));
	}

	@Test
	void testEqualsCharSequenceCharSequence() {
		String s1 = "test123";
		String s2 = "test123";
		String s3 = "different";

		assertFalse(StringUtil.equals(s1, null));
		assertFalse(StringUtil.equals(s1, new Object()));
		assertFalse(StringUtil.equals(s1, s3));

		assertTrue(StringUtil.equals(s1, (Object)s1));
		assertTrue(StringUtil.equals(s1, (Object)s2));
	}

	@Test
	void testNotNullString() {
		String s = "test123";

		assertSame(s, StringUtil.notNull(s));
		assertEquals("", StringUtil.notNull(null));
	}

	@Test
	void testNotNullStringString() {
		String s = "test123";
		String fallback = "different";

		assertSame(s, StringUtil.notNull(s, fallback));
		assertEquals(fallback, StringUtil.notNull(null, fallback));
		assertThrows(NullPointerException.class, () -> StringUtil.notNull(null, null));
	}

	@Test
	void testSplitLines() {
		String[] expected = {"x", "y", "z"};
		assertArrayEquals(expected, StringUtil.splitLines("x\ny\nz"));
		assertArrayEquals(expected, StringUtil.splitLines("x\r\ny\r\nz"));
		assertArrayEquals(expected, StringUtil.splitLines("x\ry\rz"));
	}

	@Test
	void testHashCharSequence() {
		String s = "test123";
		assertEquals(s.hashCode(), StringUtil.hash(s));
		assertEquals("".hashCode(), StringUtil.hash(""));
	}

	@Test
	void testHashCharArrayIntInt() {
		char[] c = {'t','e','s','t'};
		String s = new String(c);

		assertEquals(s.hashCode(), StringUtil.hash(c, 0, c.length));
	}

	@Test
	void testToStringCharSequence() {
		String s = "test123";

		assertEquals(s, StringUtil.toString(s));
		assertSame(s, StringUtil.toString(s));
	}

	@Test
	void testToStringCharSequenceIntInt() {
		String s = "test";

		assertEquals("es", StringUtil.toString(s, 1, 3));
		assertEquals(s, StringUtil.toString(s, 0, s.length()));
	}

	@Test
	void testStartsWithCharSequenceCharSequenceInt() {
		assertTrue(StringUtil.startsWith("xyz", "xyz", 0));
		assertTrue(StringUtil.startsWith("xyz", "yz", 1));
		assertTrue(StringUtil.startsWith("", "", 0));

		assertFalse(StringUtil.startsWith("xyz", "abc", 0));
		assertFalse(StringUtil.startsWith("xyz", "xy", 1));
		assertFalse(StringUtil.startsWith("xyz", "yza", 1));
	}

	@Test
	void testStartsWithCharSequenceCharSequence() {
		assertTrue(StringUtil.startsWith("xyz", "xyz"));
		assertTrue(StringUtil.startsWith("xyz", "xy"));
		assertTrue(StringUtil.startsWith("xyz", ""));
		assertTrue(StringUtil.startsWith("", ""));

		assertFalse(StringUtil.startsWith("xyz", "abc"));
		assertFalse(StringUtil.startsWith("xyz", "xyza"));
		assertFalse(StringUtil.startsWith("", "x"));
	}

	@Test
	void testEndsWithCharSequenceCharSequence() {
		assertTrue(StringUtil.endsWith("xyz", "z"));
		assertTrue(StringUtil.endsWith("xyz", "yz"));
		assertTrue(StringUtil.endsWith("xyz", "xyz"));
		assertTrue(StringUtil.endsWith("xyz", ""));
		assertTrue(StringUtil.endsWith("", ""));

		assertFalse(StringUtil.endsWith("xyz", "a"));
		assertFalse(StringUtil.endsWith("xyz", "xyza"));
		assertFalse(StringUtil.endsWith("", "x"));
	}

	@Test
	void testIndexOfCharSequenceChar() {
		assertEquals(-1, StringUtil.indexOf("", 'x'));
		assertEquals(0, StringUtil.indexOf("x", 'x'));
		assertEquals(0, StringUtil.indexOf("xx", 'x'));
		assertEquals(1, StringUtil.indexOf("yx", 'x'));
		assertEquals(-1, StringUtil.indexOf("yy", 'x'));
	}

	@Test
	void testIndexOfCharSequenceCharInt() {
		assertEquals(-1, StringUtil.indexOf("", 'x', 0));
		assertEquals(0, StringUtil.indexOf("x", 'x', 0));
		assertEquals(0, StringUtil.indexOf("xx", 'x', 0));
		assertEquals(1, StringUtil.indexOf("yx", 'x', 0));
		assertEquals(-1, StringUtil.indexOf("yy", 'x', 0));
		assertEquals(-1, StringUtil.indexOf("xy", 'x', 1));
		assertEquals(2, StringUtil.indexOf("xyx", 'x', 1));
		assertEquals(2, StringUtil.indexOf("xyx", 'x', 2));
	}

	@Test
	void testIndexOfCharSequenceCharIntInt() {
		assertEquals(-1, StringUtil.indexOf("", 'x', 0, 0));
		assertEquals(0, StringUtil.indexOf("x", 'x', 0, 0));
		assertEquals(1, StringUtil.indexOf("xx", 'x', 1, 1));
		assertEquals(-1, StringUtil.indexOf("xyx", 'x', 1, 1));
		assertEquals(2, StringUtil.indexOf("xyx", 'x', 1, 2));
		assertEquals(-1, StringUtil.indexOf("xyy", 'x', 1, 2));
		assertEquals(-1, StringUtil.indexOf("xxx", 'x', 2, 1));
	}

	@Test
	void testLastIndexOfCharSequenceChar() {
		assertEquals(0, StringUtil.lastIndexOf("x", 'x'));
		assertEquals(1, StringUtil.lastIndexOf("xx", 'x'));
		assertEquals(1, StringUtil.lastIndexOf("yx", 'x'));
		assertEquals(-1, StringUtil.lastIndexOf("yy", 'x'));
		assertEquals(-1, StringUtil.lastIndexOf("", 'x'));
	}

	@Test
	void testLastIndexOfCharSequenceCharInt() {
		assertEquals(-1, StringUtil.lastIndexOf("", 'x', 0));
		assertEquals(0, StringUtil.lastIndexOf("x", 'x', 1));
		assertEquals(0, StringUtil.lastIndexOf("x", 'x', 0));
		assertEquals(-1, StringUtil.lastIndexOf("yx", 'x', 0));
		assertEquals(1, StringUtil.lastIndexOf("xx", 'x', 1));
		assertEquals(0, StringUtil.lastIndexOf("xy", 'x', 1));
		assertEquals(-1, StringUtil.lastIndexOf("xx", 'y', 1));
	}

	@Test
	@Disabled("see testIndexOfCharSequenceIntIntCharSequenceIntIntInt()")
	void testIndexOfCharSequenceCharSequence() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled("see testIndexOfCharSequenceIntIntCharSequenceIntIntInt()")
	void testIndexOfCharSequenceCharSequenceInt() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled("see testLastIndexOfCharSequenceIntIntCharSequenceIntIntInt()")
	void testLastIndexOfCharSequenceCharSequence() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled("see testLastIndexOfCharSequenceIntIntCharSequenceIntIntInt()")
	void testLastIndexOfCharSequenceCharSequenceInt() {
		fail("Not yet implemented");
	}

	@Test
	void testIndexOfCharSequenceIntIntCharSequenceIntIntInt() {
		//TODO implement test
	}

	@Test
	void testLastIndexOfCharSequenceIntIntCharSequenceIntIntInt() {
		//TODO implement test
	}

	@SuppressWarnings("boxing")
	@Test
	void testGetName() {
		assertEquals(null, StringUtil.getName(null));
		assertEquals("", StringUtil.getName(""));
		assertEquals("1", StringUtil.getName(1));
		assertEquals("true", StringUtil.getName(true));

		String s = "test123";

		Identity identity = mock(Identity.class);
		when(identity.getName()).thenReturn(s);

		Identifiable identifiable = mock(Identifiable.class);
		when(identifiable.getIdentity()).thenReturn(identity);

		NamedObject namedObject = mock(NamedObject.class);
		when(namedObject.getName()).thenReturn(s);

		assertEquals(s, StringUtil.getName(identity));
		assertEquals(s, StringUtil.getName(identifiable));
		assertEquals(s, StringUtil.getName(namedObject));
	}

	@SuppressWarnings("boxing")
	@Test
	void testAsText() {
		assertEquals(null, StringUtil.asText(null));
		assertEquals("", StringUtil.asText(""));
		assertEquals("1", StringUtil.asText(1));
		assertEquals("true", StringUtil.asText(true));

		String s = "test123";

		TextItem textItem = mock(TextItem.class);
		when(textItem.getText()).thenReturn(s);

		assertEquals(s, StringUtil.asText(textItem));
	}

	@Test
	@PostponedTest
	void testToSwingTooltip() {
		fail("Not yet implemented");
	}

	@Test
	void testToUnwrappedSwingTooltip() {
		assertEquals(null, StringUtil.toUnwrappedSwingTooltip(null));
		assertEquals(null, StringUtil.toUnwrappedSwingTooltip(""));
		assertEquals("xy", StringUtil.toUnwrappedSwingTooltip("xy"));
		assertEquals("<html>x<br>y", StringUtil.toUnwrappedSwingTooltip("x\ny"));
		assertEquals("<html>x<br>y", StringUtil.toUnwrappedSwingTooltip("x\r\ny"));
		assertEquals("<html>x<br>y", StringUtil.toUnwrappedSwingTooltip("x\n\ry"));
		assertEquals("<html>x<br>y", StringUtil.toUnwrappedSwingTooltip("x\ry"));
		assertEquals("<html>x<br><br>y", StringUtil.toUnwrappedSwingTooltip("x\n\ny"));
		assertEquals("<html>x<br><br>y", StringUtil.toUnwrappedSwingTooltip("x\r\ry"));
	}

	@Test
	void testGetBaseName() {
		assertEquals("", StringUtil.getBaseName(""));
		assertEquals("x", StringUtil.getBaseName("x"));
		assertEquals("x", StringUtil.getBaseName("x(1)"));
		assertEquals("x", StringUtil.getBaseName("x (1)"));
		assertEquals("x", StringUtil.getBaseName("x  (1)"));
	}

	@Test
	void testGetCurrentCount() {
		assertEquals(-1, StringUtil.getCurrentCount(""));
		assertEquals(-1, StringUtil.getCurrentCount("x"));
		assertEquals(-1, StringUtil.getCurrentCount("()"));
		assertEquals(1, StringUtil.getCurrentCount("x(1)"));
		assertEquals(1, StringUtil.getCurrentCount("x (1)"));
		assertEquals(1, StringUtil.getCurrentCount("x  (1)"));
		assertEquals(22, StringUtil.getCurrentCount("x(22)"));
		assertEquals(23, StringUtil.getCurrentCount("x (23)"));
		assertEquals(24, StringUtil.getCurrentCount("x  (24)"));
	}

	@Test
	@PostponedTest
	void testGetUniqueNameStringSetOfString() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testGetUniqueNameStringSetOfStringBoolean() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testFitStringInt() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testFitStringIntString() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testFormatDecimalInt() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testFormatDecimalLong() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testFormatDecimalDouble() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testFormatDecimalFloat() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testFormatShortenedDecimalDouble() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testFormatShortenedDecimalInt() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testFormatDuration() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testTrim() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testTrimLeft() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testTrimRight() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testWrapStringComponentInt() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testWrapStringFontMetricsInt() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testSplitStringComponentInt() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testSplitStringFontMetricsInt() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testCapitalize() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testJoinStringArray() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testJoinStringArrayStringCharChar() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testJoinStringArrayString() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testCompareNumberAwareIgnoreCase() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testCompareNumberAware() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testEndsWithCharSequenceChar() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testPadRight() {
		fail("Not yet implemented");
	}

	@Test
	@PostponedTest
	void testPadLeft() {
		fail("Not yet implemented");
	}

	@Test
	void testHexString() {
		long[] samples = {
				Long.MIN_VALUE+1, -100, -1, 0, 1, 100, Long.MAX_VALUE
		};

		for(long sample : samples) {
			char[] buffer = new char[64];
			int charsWritten = StringUtil.writeHexString(sample, buffer, 0);
			assertTrue(charsWritten>0);
			long readValue = StringUtil.parseHexString(buffer, 0, charsWritten);
			assertEquals(sample, readValue, "Fail for value: "+sample);
		}
	}

}
