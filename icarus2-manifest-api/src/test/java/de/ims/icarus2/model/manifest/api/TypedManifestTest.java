/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.TypedManifest;

/**
 * @author Markus Gärtner
 *
 */
public interface TypedManifestTest {

	ManifestType getExpectedType();

	TypedManifest createUnlocked();

	@Test
	default void testGetManifestType() {
		TypedManifest manifest = createUnlocked();
		if(manifest!=null) {
			assertEquals(getExpectedType(), manifest.getManifestType());
		}
	}
}
