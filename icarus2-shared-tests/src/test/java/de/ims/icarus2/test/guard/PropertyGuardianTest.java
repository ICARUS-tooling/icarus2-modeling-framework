/**
 *
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

		private void assertFilteredMethods(String...expected) {
			String[] actual = Stream.of(Dummy1.class.getMethods())
				.filter(PropertyGuardian.PROPERTY_FILTER)
				.map(Method::getName)
				.toArray(String[]::new);

			Arrays.sort(actual);
			Arrays.sort(expected);

			assertArrayEquals(expected, actual);
		}

		@Test
		void testFindPropertyMethods() {
			assertFilteredMethods("setter1", "getter1");
		}

//		@Test
//		void tmp() {
//			Stream.of(Dummy1.class.getMethods())
//					.forEach(m -> System.out.println(m.getName()+": "+m.getReturnType()));
//		}
	}

	// DUMMY CLASSES

	public static class Dummy1 {
		public void setter1(String s) { /* no-op */ };
		public void setter2(String s, int x) { /* no-op */ };

		public String getter1() { return "result1"; };
		public String getter2(String s) { return s; };
	}
}
