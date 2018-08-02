/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
public interface ManifestFragmentTest extends TypedManifestTest, LockableTest {

	@Override
	ManifestFragment createUnlocked();

	@Test
	default void testGetId() {
		ManifestFragment manifest = createUnlocked();
		if(manifest!=null) {
			assertNotNull(manifest.getId());
		}
	}

	@Test
	default void testGetUniqueId() {
		ManifestFragment manifest = createUnlocked();
		if(manifest!=null) {
			assertNotNull(manifest.getUniqueId());
		}
	}
}
