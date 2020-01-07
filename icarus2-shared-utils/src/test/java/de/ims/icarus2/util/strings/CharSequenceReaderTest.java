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
package de.ims.icarus2.util.strings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
class CharSequenceReaderTest {

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CharSequenceReader#read()}.
	 * @throws IOException
	 */
	@Test
	void testRead() throws IOException {
		try(CharSequenceReader reader = new CharSequenceReader("test123")) {
			assertEquals('t', (char)reader.read());
			assertEquals('e', (char)reader.read());
			assertEquals('s', (char)reader.read());
			assertEquals('t', (char)reader.read());
			assertEquals('1', (char)reader.read());
			assertEquals('2', (char)reader.read());
			assertEquals('3', (char)reader.read());

			assertEquals(-1, reader.read());
		}

	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CharSequenceReader#read(char[], int, int)}.
	 * @throws IOException
	 */
	@Test
	void testReadCharArrayIntInt() throws IOException {
		String s = "test123";
		try(CharSequenceReader reader = new CharSequenceReader(s)) {
			char[] buffer = new char[10];

			int charsRead = reader.read(buffer, 0, s.length());
			assertEquals(s.length(), charsRead);
			assertEquals(-1, reader.read());

			String read = new String(buffer, 0, charsRead);

			assertEquals(s, read);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CharSequenceReader#skip(long)}.
	 * @throws IOException
	 */
	@Test
	void testSkip() throws IOException {
		String s = "0123456789";
		try(CharSequenceReader reader = new CharSequenceReader(s)) {
			assertEquals('0', (char)reader.read());

			assertEquals(0L, reader.skip(0));

			assertEquals('1', (char)reader.read());

			assertEquals(1L, reader.skip(1));

			assertEquals('3', (char)reader.read());

			assertEquals(6L, reader.skip(10));

			assertEquals(-1, reader.read());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CharSequenceReader#ready()}.
	 */
	@Test
	void testReady() {
		try(CharSequenceReader reader = new CharSequenceReader("")) {
			assertTrue(reader.ready());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CharSequenceReader#markSupported()}.
	 */
	@Test
	void testMarkSupported() {
		try(CharSequenceReader reader = new CharSequenceReader("")) {
			assertTrue(reader.markSupported());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CharSequenceReader#mark(int)}.
	 */
	@Test
	@Disabled("included in testReset()")
	void testMark() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CharSequenceReader#reset()}.
	 * @throws IOException
	 */
	@Test
	void testReset() throws IOException {
		String s = "0123456789";
		try(CharSequenceReader reader = new CharSequenceReader(s)) {
			assertEquals('0', (char)reader.read());

			reader.mark(-1);

			assertEquals('1', (char)reader.read());
			assertEquals('2', (char)reader.read());
			assertEquals('3', (char)reader.read());
			assertEquals('4', (char)reader.read());
			assertEquals('5', (char)reader.read());

			reader.reset();

			assertEquals('1', (char)reader.read());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CharSequenceReader#close()}.
	 * @throws IOException
	 */
	@Test
	void testClose() throws IOException {
		// close() and clear() are essentially equal in the target class
		testClear();
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CharSequenceReader#setSource(java.lang.CharSequence)}.
	 * @throws IOException
	 */
	@Test
	void testSetSource() throws IOException {
		try(CharSequenceReader reader = new CharSequenceReader()) {
			assertThrows(NullPointerException.class, () -> reader.setSource(null));

			String s = "test123";
			reader.setSource(s);
			assertSame(s, reader.getSource());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.strings.CharSequenceReader#clear()}.
	 * @throws IOException
	 */
	@Test
	void testClear() throws IOException {
		String s = "test123";
		try(CharSequenceReader reader = new CharSequenceReader(s)) {
			assertEquals(s, reader.getSource());

			reader.clear();

			assertNotNull(reader.getSource());
			assertTrue(reader.getSource().length()==0);

			assertEquals(-1, reader.read());
		}
	}

}
