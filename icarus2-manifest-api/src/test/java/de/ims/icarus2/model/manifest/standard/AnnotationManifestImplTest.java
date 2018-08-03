/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifestTest;
import de.ims.icarus2.model.manifest.api.Embedded;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestTest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.TypedManifest;

/**
 * @author Markus Gärtner
 *
 */
class AnnotationManifestImplTest implements AnnotationManifestTest {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.AnnotationManifestImpl#AnnotationManifestImpl(de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry, de.ims.icarus2.model.manifest.api.AnnotationLayerManifest)}.
	 */
	@Test
	void testAnnotationManifestImplManifestLocationManifestRegistryAnnotationLayerManifest() {
		AnnotationLayerManifest layerManifest = ManifestTestUtils.mockTypedManifest(ManifestType.ANNOTATION_LAYER_MANIFEST);
		ManifestLocation manifestLocation = ManifestTest.mockManifestLocation(false);
		ManifestRegistry registry = ManifestTest.mockManifestRegistry();

		AnnotationManifest manifest = new AnnotationManifestImpl(manifestLocation, registry, layerManifest);

		assertSame(layerManifest, manifest.getHost());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.AnnotationManifestImpl#AnnotationManifestImpl(de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry)}.
	 */
	@Test
	void testAnnotationManifestImplManifestLocationManifestRegistry() {
		createUnlocked();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.AnnotationManifestImpl#AnnotationManifestImpl(de.ims.icarus2.model.manifest.api.AnnotationLayerManifest)}.
	 */
	@Test
	void testAnnotationManifestImplAnnotationLayerManifest() {
		AnnotationLayerManifest layerManifest = ManifestTestUtils.mockTypedManifest(ManifestType.ANNOTATION_LAYER_MANIFEST);
		new AnnotationManifestImpl(layerManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestTest#createUnlocked(de.ims.icarus2.model.manifest.api.ManifestRegistry, de.ims.icarus2.model.manifest.api.ManifestLocation)
	 */
	@Override
	public AnnotationManifest createUnlocked(ManifestRegistry registry, ManifestLocation location) {
		return new AnnotationManifestImpl(location, registry);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	public ManifestType getExpectedType() {
		return ManifestType.ANNOTATION_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	public Set<ManifestType> getAllowedHostTypes() {
		return Collections.singleton(ManifestType.ANNOTATION_LAYER_MANIFEST);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#createEmbedded(de.ims.icarus2.model.manifest.api.TypedManifest)
	 */
	@Override
	public Embedded createEmbedded(TypedManifest host) {
		return new AnnotationManifestImpl((AnnotationLayerManifest) host);
	}

}
