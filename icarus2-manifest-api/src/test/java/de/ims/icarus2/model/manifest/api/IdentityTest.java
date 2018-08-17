/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import javax.swing.Icon;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 *
 */
public interface IdentityTest<I extends Identity> extends GenericTest<I> {

	default I createEmpty() {
		return createTestInstance(settings());
	}

	@Provider
	I createFromIdentity(String id, String name, String description, Icon icon);

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#getId()}.
	 */
	@Test
	default void testGetId() {
		I empty = createEmpty();
		if(empty!=null) {
			assertNull(empty.getId());
		}

		Icon icon = mock(Icon.class);
		I fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			assertEquals("myId", fromIdentity.getId());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#getName()}.
	 */
	@Test
	default void testGetName() {
		I empty = createEmpty();
		if(empty!=null) {
			assertNull(empty.getName());
		}

		Icon icon = mock(Icon.class);
		I fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			assertEquals("name", fromIdentity.getName());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#getDescription()}.
	 */
	@Test
	default void testGetDescription() {
		I empty = createEmpty();
		if(empty!=null) {
			assertNull(empty.getDescription());
		}

		Icon icon = mock(Icon.class);
		I fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			assertEquals("description", fromIdentity.getDescription());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#getIcon()}.
	 */
	@Test
	default void testGetIcon() {
		I empty = createEmpty();
		if(empty!=null) {
			assertNull(empty.getIcon());
		}

		Icon icon = mock(Icon.class);
		I fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			assertSame(icon, fromIdentity.getIcon());
		}
	}

}
