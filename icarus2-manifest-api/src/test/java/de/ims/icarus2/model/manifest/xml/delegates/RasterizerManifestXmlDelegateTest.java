/**
 *
 */
package de.ims.icarus2.model.manifest.xml.delegates;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.RasterizerManifest;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;
import de.ims.icarus2.test.annotations.OverrideTest;

/**
 * @author Markus Gärtner
 *
 */
class RasterizerManifestXmlDelegateTest
		implements ManifestXmlDelegateTest<RasterizerManifest, RasterizerManifestXmlDelegate> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends RasterizerManifestXmlDelegate> getTestTargetClass() {
		return RasterizerManifestXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.RASTERIZER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	@OverrideTest
	@Test
	public void testMandatoryConstructors() throws Exception {
		createNoArgs();

		RasterizerManifest manifest = mockManifest();
		assertEquals(manifest, new RasterizerManifestXmlDelegate(manifest).getInstance());

		FragmentLayerManifest layerManifest = mockTypedManifest(ManifestType.FRAGMENT_LAYER_MANIFEST);
		assertOptionalEquals(layerManifest, new RasterizerManifestXmlDelegate(layerManifest).getInstance().getLayerManifest());
	}

	@Test
	void testResetStructureLayerManifest() {
		RasterizerManifestXmlDelegate delegate = create();
		FragmentLayerManifest layerManifest = mockTypedManifest(ManifestType.FRAGMENT_LAYER_MANIFEST);
		assertOptionalEquals(layerManifest, delegate.reset(layerManifest).getInstance().getLayerManifest());
	}
}
