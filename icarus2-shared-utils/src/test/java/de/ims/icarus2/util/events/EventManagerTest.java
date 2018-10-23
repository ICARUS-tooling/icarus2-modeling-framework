/**
 *
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
