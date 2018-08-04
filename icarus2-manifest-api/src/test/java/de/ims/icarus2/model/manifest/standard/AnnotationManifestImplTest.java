/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import java.util.Collections;
import java.util.Set;

import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifestTest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.TypedManifest;

/**
 * @author Markus Gärtner
 *
 */
class AnnotationManifestImplTest implements AnnotationManifestTest {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends AnnotationManifest> getTestTargetClass() {
		return AnnotationManifestImpl.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifestTest#createHosted(de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry, de.ims.icarus2.model.manifest.api.TypedManifest)
	 */
	@Override
	public AnnotationManifest createHosted(ManifestLocation manifestLocation, ManifestRegistry registry,
			TypedManifest host) {
		return new AnnotationManifestImpl(manifestLocation, registry, (AnnotationLayerManifest) host);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestTest#createUnlocked(de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry)
	 */
	@Override
	public AnnotationManifest createUnlocked(ManifestLocation location, ManifestRegistry registry) {
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

}
