/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.test.reflect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Nested;
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
			//TODO
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
		public void voidMethod1(String s) { /* no-op */ }
		public void voidMethod2(String s) { /* no-op */ }
		public String stringMethod1() { return "string"; }
		public String stringMethod1(String s) { return s; }
	}

	static class Dummy2 extends Dummy1 {
		@Override
		public void voidMethod2(String s) { /* no-op */ }
		@Override
		public String stringMethod1(String s) { return s; }
	}
}
