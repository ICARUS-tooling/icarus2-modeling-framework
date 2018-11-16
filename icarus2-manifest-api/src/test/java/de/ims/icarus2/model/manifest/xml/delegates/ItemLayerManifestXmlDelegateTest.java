/**
 *
 */
package de.ims.icarus2.model.manifest.xml.delegates;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;
import de.ims.icarus2.test.annotations.OverrideTest;

/**
 * @author Markus Gärtner
 *
 */
class ItemLayerManifestXmlDelegateTest
		implements ManifestXmlDelegateTest<ItemLayerManifest, ItemLayerManifestXmlDelegate> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends ItemLayerManifestXmlDelegate> getTestTargetClass() {
		return ItemLayerManifestXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.ITEM_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	@OverrideTest
	@Test
	public void testMandatoryConstructors() throws Exception {
		createNoArgs();

		ItemLayerManifest layerManifest = mockManifest();
		assertEquals(layerManifest, new ItemLayerManifestXmlDelegate(layerManifest).getInstance());

		LayerGroupManifest groupManifest = mockTypedManifest(ManifestType.LAYER_GROUP_MANIFEST, true);
		assertOptionalEquals(groupManifest, new ItemLayerManifestXmlDelegate(groupManifest).getInstance().getGroupManifest());
	}

	@Test
	void testResetLayerGroup() {
		ItemLayerManifestXmlDelegate delegate = create();
		LayerGroupManifest groupManifest = mockTypedManifest(ManifestType.LAYER_GROUP_MANIFEST, true);
		assertOptionalEquals(groupManifest, delegate.reset(groupManifest).getInstance().getGroupManifest());
	}
}
