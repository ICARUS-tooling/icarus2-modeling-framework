/**
 *
 */
package de.ims.icarus2.model.manifest.xml.delegates;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;
import de.ims.icarus2.test.annotations.OverrideTest;

/**
 * @author Markus Gärtner
 *
 */
class AnnotationLayerManifestXmlDelegateTest
		implements ManifestXmlDelegateTest<AnnotationLayerManifest, AnnotationLayerManifestXmlDelegate> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends AnnotationLayerManifestXmlDelegate> getTestTargetClass() {
		return AnnotationLayerManifestXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.ANNOTATION_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	@OverrideTest
	@Test
	public void testMandatoryConstructors() throws Exception {
		createNoArgs();

		AnnotationLayerManifest manifest = mockManifest();
		assertEquals(manifest, new AnnotationLayerManifestXmlDelegate(manifest).getInstance());

		LayerGroupManifest groupManifest = mockTypedManifest(ManifestType.LAYER_GROUP_MANIFEST, true);
		assertOptionalEquals(groupManifest, new AnnotationLayerManifestXmlDelegate(groupManifest)
				.getInstance().getGroupManifest());
	}

	@Test
	void testResetAnnotationLayerManifest() {
		AnnotationLayerManifestXmlDelegate delegate = create();
		LayerGroupManifest groupManifest = mockTypedManifest(ManifestType.LAYER_GROUP_MANIFEST, true);
		assertOptionalEquals(groupManifest, delegate.reset(groupManifest)
				.getInstance().getGroupManifest());
	}
}
