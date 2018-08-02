/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface EmbeddedTest {

	/**
	 * Return all the allowed host types or an empty set in case there
	 * is no limitation on host types.
	 * @return
	 */
	Set<ManifestType> getAllowedHostTypes();

	/**
	 * Create the {@link Embedded} instance under test with the specified host.
	 *
	 * @return
	 */
	Embedded createEmbedded(TypedManifest host);

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Embedded#getHost()}.
	 */
	@Test
	default void testGetHost() {
		Set<ManifestType> allowedHostTypes = getAllowedHostTypes();

		for(ManifestType hostType : allowedHostTypes) {
			TypedManifest host = ManifestTestUtils.mockTypedManifest(hostType);
			Embedded embedded = createEmbedded(host);
			assertNotNull(embedded.getHost());
			assertSame(host, embedded.getHost());
		}
	}

}
