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
package de.ims.icarus2.test.guard;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
class PropertyGuardianTest {

	@Nested
	class PropertyFilterTest {

		private void assertFilteredMethods(Class<?> clazz, boolean strict, String...expected) {
			String[] actual = Stream.of(clazz.getMethods())
				.filter(PropertyGuardian.createPropertyMethodFilter(strict))
				.map(Method::getName)
				.toArray(String[]::new);

			Arrays.sort(actual);
			Arrays.sort(expected);

			assertArrayEquals(expected, actual);
		}

		@Test
		void testFindPropertyMethods() {
			assertFilteredMethods(Dummy1.class, false, "setter1", "getter1");
		}

		@Test
		void testFindPropertyMethodsStrict() {
			assertFilteredMethods(Dummy2.class, true, "setVal1", "getVal1");
		}
	}

	// DUMMY CLASSES

	public static class Dummy1 {
		public void setter1(String s) { /* no-op */ }
		public void setter2(String s, int x) { /* no-op */ }

		public String getter1() { return "result1"; }
		public String getter2(String s) { return s; }
	}

	public static class Dummy2 {
		public void setVal1(String s) { /* no-op */ }
		public void setter2(String s, int x) { /* no-op */ }

		public String getVal1() { return "result1"; }
		public String getter2(String s) { return s; }
	}
}
