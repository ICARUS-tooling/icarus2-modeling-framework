/**
 *
 */
package de.ims.icarus2.test.reflect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Markus Gärtner
 *
 */
class ClassCacheTest {

	@Nested
	class HashStrategyTest {

		@Nested
		class HashCodeMethod {

			private int hash(Method m) {
				return ClassCache.STRATEGY.hashCode(m);
			}

			private boolean equals(Method m1, Method m2) {
				return ClassCache.STRATEGY.equals(m1, m2);
			}

			@ParameterizedTest
			@MethodSource("de.ims.icarus2.test.reflect.ClassCacheTest#dummy1Methods")
			void testIdenticalHashOnSelf(Method method) {
				assertEquals(hash(method), hash(method));
			}
		}

		@Nested
		class EqualsMethod {

		}
		@Test
		public void testtestName() throws Exception {

		}
	}

	static Method[] dummy1Methods() {
		return Dummy1.class.getMethods();
	}

	static Method[] dummy2Methods() {
		return Dummy2.class.getMethods();
	}


	// DUMMY CLASSES FOR TESTING

	static class Dummy1 {
		public void voidMethod1(String s) { /* no-op */ };
		public void voidMethod2(String s) { /* no-op */ };
		public String stringMethod1() { return "string"; };
		public String stringMethod1(String s) { return s; };
	}

	static class Dummy2 extends Dummy1 {
		@Override
		public void voidMethod2(String s) { /* no-op */ };
		@Override
		public String stringMethod1(String s) { return s; };
	}
}
