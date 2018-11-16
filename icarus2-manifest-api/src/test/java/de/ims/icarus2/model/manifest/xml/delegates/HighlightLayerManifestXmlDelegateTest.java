/**
 *
 */
package de.ims.icarus2.model.manifest.xml.delegates;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.HighlightLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;
import de.ims.icarus2.test.annotations.OverrideTest;

/**
 * @author Markus Gärtner
 *
 */
class HighlightLayerManifestXmlDelegateTest
		implements ManifestXmlDelegateTest<HighlightLayerManifest, HighlightLayerManifestXmlDelegate> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends HighlightLayerManifestXmlDelegate> getTestTargetClass() {
		return HighlightLayerManifestXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.HIGHLIGHT_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	@OverrideTest
	@Test
	public void testMandatoryConstructors() throws Exception {
		createNoArgs();

		HighlightLayerManifest manifest = mockManifest();
		assertEquals(manifest, new HighlightLayerManifestXmlDelegate(manifest).getInstance());

		LayerGroupManifest groupManifest = mockTypedManifest(ManifestType.LAYER_GROUP_MANIFEST, true);
		assertOptionalEquals(groupManifest, new HighlightLayerManifestXmlDelegate(groupManifest)
				.getInstance().getGroupManifest());
	}

	@Test
	void testResetAnnotationLayerManifest() {
		HighlightLayerManifestXmlDelegate delegate = create();
		LayerGroupManifest groupManifest = mockTypedManifest(ManifestType.LAYER_GROUP_MANIFEST, true);
		assertOptionalEquals(groupManifest, delegate.reset(groupManifest)
				.getInstance().getGroupManifest());
	}
}
