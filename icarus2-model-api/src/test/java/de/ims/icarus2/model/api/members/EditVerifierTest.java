/**
 *
 */
package de.ims.icarus2.model.api.members;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface EditVerifierTest<E extends Object, V extends EditVerifier<E>> extends GenericTest<V> {

	@Provider
	V createForSource(E source);

	Class<E> getVerificationTargetClass();

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.EditVerifier#getSource()}.
	 */
	@Test
	default void testGetSource() {
		assertNotNull(create().getSource());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.EditVerifier#close()}.
	 */
	@Test
	default void testClose() {
		create().close();
	}

}
