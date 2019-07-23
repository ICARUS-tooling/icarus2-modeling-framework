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
package de.ims.icarus2.model.api.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.func.QuadConsumer;
import de.ims.icarus2.test.func.TriConsumer;
import de.ims.icarus2.test.func.TriFunction;

/**
 * @author Markus Gärtner
 *
 */
public interface MetadataRegistryTest<R extends MetadataRegistry> extends GenericTest<R> {

	/**
	 * Creates a new registry instance to read from the same back-end storage
	 * as the provided {@code original}. The purpose of this method is to
	 * simulate the scenario of writing metadata to storage and then (at a later
	 * time) read it again (with a new registry instance).
	 * If the registry implementation under test does not support persistent storage
	 * (e.g. by being designed as an in-memory storage), this method can return the
	 * {@code original} argument as-is instead.
	 */
	@Provider
	R createReadingCopy(R original);

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.MetadataRegistry#close()}.
	 */
	@Test
	default void testClose() {
		create().close();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.MetadataRegistry#open()}.
	 */
	@Test
	default void testOpen() {
		create().open();
	}

	/**
	 *
	 * @param setter method signature: (registry, key, value)
	 * @param getter method signature: (registry, key, noEntryValue) = value
	 * @param changer method signature: (registry, key, value, noEntryValue)
	 * @param noEntryValue sentinel value to indicate "null" for primitive types
	 * @param values
	 */
	@SuppressWarnings("boxing")
	default <T> void testFullReadWriteCycle(TriConsumer<R, String, T> setter,
			TriFunction<R, String, T, T> getter, QuadConsumer<R, String, T, T> changer,
			T noEntryValue, @SuppressWarnings("unchecked") T...values) {
		assertTrue(values.length>0, "Requires at least 1 value");

		// Write data into registry
		R original = create();
		original.beginUpdate();
		try {
			for (int i = 0; i < values.length; i++) {
				T value = values[i];
				String key = "key_"+i;

				if(changer!=null && i%2==0) {
					changer.accept(original, key, value, noEntryValue);
				} else {
					setter.accept(original, key, value);
				}
			}
		} finally {
			original.endUpdate();
		}

		// Read data from registry
		R copy = createReadingCopy(original);
		copy.beginUpdate();
		try {
			for (int i = 0; i < values.length; i++) {
				T expected = values[i];
				String key = "key_"+i;
				T actual = getter.apply(copy, key, noEntryValue);

				assertEquals(expected, actual, String.format(
						"Mismatch for value '%s' at index %d",
						TestUtils.displayString(expected), i));
			}
		} finally {
			copy.endUpdate();
		}
	}

	@Test
	default void testStringCycle() {
		testFullReadWriteCycle(
				MetadataRegistry::setValue,
				(r,k,_n) -> r.getValue(k), // noEntryValue is only needed for primitive types!
				null, null,
				"value1", TestUtils.EMOJI, "xxxxxxxxxxxxxxxxxxxxxxxx", TestUtils.LOREM_IPSUM_ISO);
	}

	@SuppressWarnings("boxing")
	@Test
	default void testIntegerCycle() {
		testFullReadWriteCycle(
				MetadataRegistry::setIntValue,
				MetadataRegistry::getIntValue,
				MetadataRegistry::changeIntValue, 0,
				1, Integer.MAX_VALUE, 0, Integer.MIN_VALUE/2);
	}

	@SuppressWarnings("boxing")
	@Test
	default void testLongCycle() {
		testFullReadWriteCycle(
				MetadataRegistry::setLongValue,
				MetadataRegistry::getLongValue,
				MetadataRegistry::changeLongValue, 0L,
				1L, Long.MAX_VALUE, 0L, Long.MIN_VALUE/2);
	}

	@SuppressWarnings("boxing")
	@Test
	default void testByteCycle() {
		testFullReadWriteCycle(
				MetadataRegistry::setByteValue,
				MetadataRegistry::getByteValue,
				MetadataRegistry::changeByteValue, (byte)0,
				(byte)1, Byte.MAX_VALUE, (byte)0, Byte.MIN_VALUE);
	}

	@SuppressWarnings("boxing")
	@Test
	default void testShortCycle() {
		testFullReadWriteCycle(
				MetadataRegistry::setShortValue,
				MetadataRegistry::getShortValue,
				MetadataRegistry::changeShortValue, (short)0,
				(short)1, Short.MAX_VALUE, (short)0, Short.MIN_VALUE);
	}

	@SuppressWarnings("boxing")
	@Test
	default void testFloatCycle() {
		testFullReadWriteCycle(
				MetadataRegistry::setFloatValue,
				MetadataRegistry::getFloatValue,
				MetadataRegistry::changeFloatValue, (float)0,
				(float)1, Float.MAX_VALUE, (float)0, -Float.MAX_VALUE);
	}

	@SuppressWarnings("boxing")
	@Test
	default void testDoubleCycle() {
		testFullReadWriteCycle(
				MetadataRegistry::setDoubleValue,
				MetadataRegistry::getDoubleValue,
				MetadataRegistry::changeDoubleValue, (double)0,
				(double)1, Double.MAX_VALUE, (double)0, -Double.MAX_VALUE);
	}

	@Test
	default void testBooleanCycle() {
		testFullReadWriteCycle(
				MetadataRegistry::setBooleanValue,
				MetadataRegistry::getBooleanValue,
				MetadataRegistry::changeBooleanValue, Boolean.FALSE,
				Boolean.TRUE, Boolean.FALSE, Boolean.FALSE);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.MetadataRegistry#beginUpdate()}.
	 */
	@Test
	default void testBeginEndUpdate() {
		try(R registry = create()) {
			registry.beginUpdate();
			registry.endUpdate();
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.MetadataRegistry#delete()}.
	 */
	@Test
	default void testDelete() {
		try(R registry = create()) {
			registry.setValue("key", "value");

			registry.delete();

			assertNull(registry.getValue("key"));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.MetadataRegistry#forEachEntry(java.util.function.BiConsumer)}.
	 */
	@Test
	default void testForEachEntryBiConsumerOfQsuperStringQsuperString() {
		R registry = create();

		registry.beginUpdate();
		try {
			registry.setValue("a.b.c", "val1");
			registry.setValue("a.b", "val2");
			registry.setValue("a.b.c.d", "val3");
			registry.setValue("x.y.z", "val4");
		} finally {
			registry.endUpdate();
		}

		BiConsumer<String, String> action = mock(BiConsumer.class);
		registry.forEachEntry(action);

		verify(action).accept("a.b.c", "val1");
		verify(action).accept("a.b", "val2");
		verify(action).accept("a.b.c.d", "val3");
		verify(action).accept("x.y.z", "val4");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.MetadataRegistry#forEachEntry(java.lang.String, java.util.function.BiConsumer)}.
	 */
	@Test
	default void testForEachEntryStringBiConsumerOfQsuperStringQsuperString() {
		R registry = create();

		registry.beginUpdate();
		try {
			registry.setValue("a.b.c", "val1");
			registry.setValue("a.b", "val2");
			registry.setValue("a.b.c.d", "val3");
			registry.setValue("x.y.z", "val4");
		} finally {
			registry.endUpdate();
		}

		BiConsumer<String, String> action1 = mock(BiConsumer.class);
		registry.forEachEntry("a.b", action1);

		verify(action1).accept(eq("a.b.c"), eq("val1"));
		verify(action1).accept(eq("a.b"), eq("val2"));
		verify(action1).accept(eq("a.b.c.d"), eq("val3"));

		BiConsumer<String, String> action2 = mock(BiConsumer.class);
		registry.forEachEntry("a.b.c", action2);

		verify(action2).accept(eq("a.b.c"), eq("val1"));
		verify(action2).accept(eq("a.b.c.d"), eq("val3"));

		BiConsumer<String, String> action3 = mock(BiConsumer.class);
		registry.forEachEntry("x", action3);

		verify(action3).accept(eq("x.y.z"), eq("val4"));
	}

}
