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
package de.ims.icarus2.util.events;

import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.GenericTest;

/**
 * @author Markus Gärtner
 *
 */
public interface EventManagerTest<E extends EventManager> extends GenericTest<E> {

	/**
	 * Test method for {@link de.ims.icarus2.util.events.EventManager#addListener(de.ims.icarus2.util.events.SimpleEventListener)}.
	 */
	@Test
	default void testAddListenerSimpleEventListener() {
		E instance = createTestInstance(settings());

		instance.addListener(mock(SimpleEventListener.class));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.events.EventManager#addListener(de.ims.icarus2.util.events.SimpleEventListener, java.lang.String)}.
	 */
	@Test
	default void testAddListenerSimpleEventListenerString() {
		E instance = createTestInstance(settings());

		instance.addListener(mock(SimpleEventListener.class), null);
		instance.addListener(mock(SimpleEventListener.class), "myEvent");

		assertThrows(IllegalArgumentException.class,
				() -> instance.addListener(mock(SimpleEventListener.class), ""));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.events.EventManager#removeListener(de.ims.icarus2.util.events.SimpleEventListener)}.
	 */
	@Test
	default void testRemoveListenerSimpleEventListener() {
		E instance = createTestInstance(settings());

		instance.removeListener(mock(SimpleEventListener.class));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.events.EventManager#removeListener(de.ims.icarus2.util.events.SimpleEventListener, java.lang.String)}.
	 */
	@Test
	default void testRemoveListenerSimpleEventListenerString() {
		E instance = createTestInstance(settings());

		instance.removeListener(mock(SimpleEventListener.class), null);
		instance.removeListener(mock(SimpleEventListener.class), "myEvent");

		assertThrows(IllegalArgumentException.class,
				() -> instance.removeListener(mock(SimpleEventListener.class), ""));
	}

}
