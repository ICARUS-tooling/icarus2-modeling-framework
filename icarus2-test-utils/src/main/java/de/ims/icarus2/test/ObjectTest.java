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
package de.ims.icarus2.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
public interface ObjectTest {

	/**
	 * Create a series of test objects that are supposed to be
	 * different.
	 *
	 * @return
	 */
	Object[] createDifferent();

	Object[] createEqual();

	/**
	 * Hook for subclasses to cleanup/release the given instance.
	 *
	 * @param string
	 */
	default void cleanup(Object obj) {
		// no-op
	}

	/**
	 * Run basic {@link Object#equals(Object) equality} checks
	 */
	@Test
	default void testEquals() {
		Object[] objects = createDifferent();

		assertNotNull(objects);

		if(objects.length==0) {
			return;
		}

		Object dummy = new Object() {
			// hide inheritance
		};

		// Individual equality checks
		Object last = null;
		int lastIndex = -1;
		for(int i=0; i<objects.length; i++) {
			Object object = objects[i];
			if(object==null) {
				continue;
			}

			assertTrue(object.equals(object), "Reflexive equality violated for "+i);
			assertFalse(object.equals(null), "Null inequality violated for "+i);
			assertFalse(object.equals(dummy), "Foreign inequality violated for "+i);

			if(last!=null) {
				assertFalse(last.equals(object), "Known inequality violated between "+lastIndex+" and "+i);
			}
			last = object;
			lastIndex = i;
		}

		for(Object object : objects) {
			if(object!=null) {
				cleanup(object);
			}
		}
	}

	@Test
	default void testHashCodeDifferent() {
		Object[] objects = createDifferent();

		assertNotNull(objects);

		if(objects.length==0) {
			return;
		}

		Object last = null;
		int lastIndex = -1;
		for(int i=0; i<objects.length; i++) {
			Object object = objects[i];
			if(object==null) {
				continue;
			}

			if(last!=null) {
				assertNotEquals(last.hashCode(), object.hashCode(),
						"hash inequality violated between "+lastIndex+" and "+i);
			}

			last = object;
			lastIndex = i;
		}

		for(Object object : objects) {
			if(object!=null) {
				cleanup(object);
			}
		}
	}

	@Test
	default void testHashCodeEqual() {
		Object[] objects = createEqual();

		assertNotNull(objects);

		if(objects.length==0) {
			return;
		}

		Object last = null;
		int lastIndex = -1;
		for(int i=0; i<objects.length; i++) {
			Object object = objects[i];
			if(object==null) {
				continue;
			}

			if(last!=null) {
				assertEquals(last.hashCode(), object.hashCode(), "hash inequality violated between "+lastIndex+" and "+i);
			}

			last = object;
			lastIndex = i;
		}

		for(Object object : objects) {
			if(object!=null) {
				cleanup(object);
			}
		}
	}
}
