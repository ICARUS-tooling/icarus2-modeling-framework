/**
 *
 */
package de.ims.icarus2.model.manifest.xml.delegates;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestGenerator;
import de.ims.icarus2.model.manifest.ManifestGenerator.Config;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;
import de.ims.icarus2.test.annotations.OverrideTest;

/**
 * @author Markus Gärtner
 *
 */
class ContainerManifestXmlDelegateTest implements ManifestXmlDelegateTest<ContainerManifest, ContainerManifestXmlDelegate> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends ContainerManifestXmlDelegate> getTestTargetClass() {
		return ContainerManifestXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.CONTAINER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#configurations()
	 */
	@Override
	public List<Config> configurations() {
		return Stream.of(ContainerType.values())
				.map(ct -> ManifestGenerator.config()
							.preprocessor(ManifestType.CONTAINER_MANIFEST,
									m -> ((ContainerManifest)m).setContainerType(ct))
							.label(ct.toString()))
				.collect(Collectors.toList());
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	@OverrideTest
	@Test
	public void testMandatoryConstructors() throws Exception {
		createNoArgs();

		ContainerManifest manifest = mockManifest();
		assertEquals(manifest, new ContainerManifestXmlDelegate(manifest).getInstance());

		ItemLayerManifest layerManifest = mockTypedManifest(ManifestType.ITEM_LAYER_MANIFEST);
		assertOptionalEquals(layerManifest, new ContainerManifestXmlDelegate(layerManifest).getInstance().getLayerManifest());
	}

	@Test
	void testResetItemLayerManifest() {
		ContainerManifestXmlDelegate delegate = create();
		ItemLayerManifest layerManifest = mockTypedManifest(ManifestType.ITEM_LAYER_MANIFEST);
		assertOptionalEquals(layerManifest, delegate.reset(layerManifest).getInstance().getLayerManifest());
	}
}
