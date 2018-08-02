/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.ManifestFactory;
import de.ims.icarus2.model.manifest.api.ManifestFactoryTest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;

/**
 * @author Markus Gärtner
 *
 */
class DefaultManifestFactoryTest implements ManifestFactoryTest {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.DefaultManifestFactory#DefaultManifestFactory(de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry)}.
	 */
	@Test
	void testDefaultManifestFactory() {
		ManifestLocation manifestLocation = mock(ManifestLocation.class);
		ManifestRegistry registry = mock(ManifestRegistry.class);

		assertThrows(NullPointerException.class, () -> new DefaultManifestFactory(null, registry));
		assertThrows(NullPointerException.class, () -> new DefaultManifestFactory(manifestLocation, null));

		ManifestFactory factory = new DefaultManifestFactory(manifestLocation, registry);

		assertSame(manifestLocation, factory.getManifestLocation());
		assertSame(registry, factory.getRegistry());
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestFactoryTest#createFactory()
	 */
	@Override
	public ManifestFactory createFactory() {
		ManifestLocation manifestLocation = mock(ManifestLocation.class);
		ManifestRegistry registry = mock(ManifestRegistry.class);

		return new DefaultManifestFactory(manifestLocation, registry);
	}

}
