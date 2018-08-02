/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.test.annotations.PostponedTest;

/**
 * @author Markus Gärtner
 *
 */
public interface ManifestFactoryTest {

	ManifestFactory createFactory();

	@Test
	default void testGetSupportedTypes() {
		ManifestFactory factory = createFactory();
		Set<ManifestType> supportedTypes = factory.getSupportedTypes();

		assertNotNull(supportedTypes);
		assertFalse(supportedTypes.isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestFactory#create(de.ims.icarus2.model.manifest.api.ManifestType)}.
	 */
	@Test
	default void testCreateManifestType() {
		ManifestFactory factory = createFactory();

		for(ManifestType type : factory.getSupportedTypes()) {

			// Go only for environment free manifest types here
			if(type.requiresEnvironment()) {
				continue;
			}

			ManifestFragment manifest = factory.create(type);

			assertNotNull(manifest);
			assertEquals(type, manifest.getManifestType());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestFactory#create(de.ims.icarus2.model.manifest.api.ManifestType, java.lang.Object)}.
	 */
	@Test
	default void testCreateManifestTypeObject() {
		ManifestFactory factory = createFactory();

		for(ManifestType type : factory.getSupportedTypes()) {

			// Go only for environment dependent manifest types here
			if(!type.requiresEnvironment()) {
				continue;
			}

			for(ManifestType hostType : type.getRequiredEnvironment()) {
				TypedManifest host = ManifestTestUtils.mockTypedManifest(hostType);
				ManifestFragment manifest = factory.create(type, host);

				assertNotNull(manifest);
				assertEquals(type, manifest.getManifestType());
			}
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestFactory#create(de.ims.icarus2.model.manifest.api.ManifestType, java.lang.Object, de.ims.icarus2.util.Options)}.
	 */
	@Test
	@PostponedTest("testing with options requires implementation specific logic")
	default void testCreateManifestTypeObjectOptions() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestFactory#getManifestLocation()}.
	 */
	@Test
	default void testGetManifestLocation() {
		assertNotNull(createFactory().getManifestLocation());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestFactory#getRegistry()}.
	 */
	@Test
	default void testGetRegistry() {
		assertNotNull(createFactory().getRegistry());
	}

}
