/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import de.ims.icarus2.model.manifest.api.AnnotationLayerManifestTest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class AnnotationLayerManifestImplTest implements AnnotationLayerManifestTest<AnnotationLayerManifestImpl> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifestTest#createHosted(de.ims.icarus2.test.TestSettings, de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry, de.ims.icarus2.model.manifest.api.TypedManifest)
	 */
	@Override
	public AnnotationLayerManifestImpl createHosted(TestSettings settings, ManifestLocation manifestLocation,
			ManifestRegistry registry, TypedManifest host) {
		return new AnnotationLayerManifestImpl(manifestLocation, registry, (LayerGroupManifest) host);
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends AnnotationLayerManifestImpl> getTestTargetClass() {
		return AnnotationLayerManifestImpl.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	public ManifestType getExpectedType() {
		return ManifestType.ANNOTATION_LAYER_MANIFEST;
	}

}
