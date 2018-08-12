/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import de.ims.icarus2.model.manifest.api.HighlightLayerManifestTest;
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
class HighlightLayerManifestImplTest implements HighlightLayerManifestTest<HighlightLayerManifestImpl> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifestTest#createHosted(TestSettings, de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry, de.ims.icarus2.model.manifest.api.TypedManifest)
	 */
	@Override
	public HighlightLayerManifestImpl createHosted(TestSettings settings, ManifestLocation manifestLocation,
			ManifestRegistry registry, TypedManifest host) {
		return new HighlightLayerManifestImpl(manifestLocation, registry, (LayerGroupManifest) host);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	public ManifestType getExpectedType() {
		return ManifestType.HIGHLIGHT_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends HighlightLayerManifestImpl> getTestTargetClass() {
		return HighlightLayerManifestImpl.class;
	}

}
