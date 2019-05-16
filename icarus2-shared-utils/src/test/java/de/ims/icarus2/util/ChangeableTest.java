/**
 *
 */
package de.ims.icarus2.util;

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.swing.event.ChangeListener;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.Testable;

/**
 * @author Markus Gärtner
 *
 */
public interface ChangeableTest<C extends Changeable> extends Testable<C> {

	/**
	 * Performs whatever modification is required to trigger
	 * a change event on the specified {@code instance}.
	 */
	void invokeChange(C instance);

	default void testChangeNotification() {
		C instance = create();

		ChangeListener listener = mock(ChangeListener.class);

		instance.addChangeListener(listener);
		invokeChange(instance);

		verify(listener, times(1)).stateChanged(any());
	}

	default void testChangeNotificationAfterRemove() {
		C instance = create();

		ChangeListener listener = mock(ChangeListener.class);

		instance.addChangeListener(listener);
		instance.removeChangeListener(listener);
		invokeChange(instance);

		verify(listener, never()).stateChanged(any());
	}

	default void testChangeWithoutListeners() {
		C instance = create();
		invokeChange(instance);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.Changeable#addChangeListener(javax.swing.event.ChangeListener)}.
	 */
	@Test
	default void testAddChangeListener() {
		C instance = create();

		ChangeListener listener = mock(ChangeListener.class);

		instance.addChangeListener(listener);

		// Method should be idempotent
		instance.addChangeListener(listener);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.Changeable#addChangeListener(javax.swing.event.ChangeListener)}.
	 */
	@Test
	default void testAddChangeListener_null() {
		assertNPE(() -> create().addChangeListener(null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.Changeable#removeChangeListener(javax.swing.event.ChangeListener)}.
	 */
	@Test
	default void testRemoveChangeListener() {
		C instance = create();

		ChangeListener listener = mock(ChangeListener.class);

		instance.removeChangeListener(listener);

		// Method should be idempotent
		instance.removeChangeListener(listener);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.Changeable#removeChangeListener(javax.swing.event.ChangeListener)}.
	 */
	@Test
	default void testRemoveChangeListener_null() {
		assertNPE(() -> create().removeChangeListener(null));
	}

}
