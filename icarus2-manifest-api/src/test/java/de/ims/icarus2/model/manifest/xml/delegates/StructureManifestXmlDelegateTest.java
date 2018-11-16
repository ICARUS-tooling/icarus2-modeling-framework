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
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;
import de.ims.icarus2.test.annotations.OverrideTest;

/**
 * @author Markus Gärtner
 *
 */
class StructureManifestXmlDelegateTest implements ManifestXmlDelegateTest<StructureManifest,
		StructureManifestXmlDelegate> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends StructureManifestXmlDelegate> getTestTargetClass() {
		return StructureManifestXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.STRUCTURE_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#configurations()
	 */
	@Override
	public List<Config> configurations() {
		return Stream.of(ContainerType.values())
				.flatMap(ct -> Stream.of(StructureType.values())
						.map(st -> ManifestGenerator.config()
								.preprocessor(ManifestType.STRUCTURE_MANIFEST, m -> {
									StructureManifest sm = (StructureManifest) m;
									sm.setContainerType(ct);
									sm.setStructureType(st);
								}).label(ct+" & "+st)))
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

		StructureManifest manifest = mockManifest();
		assertEquals(manifest, new StructureManifestXmlDelegate(manifest).getInstance());

		StructureLayerManifest layerManifest = mockTypedManifest(ManifestType.STRUCTURE_LAYER_MANIFEST);
		assertOptionalEquals(layerManifest, new StructureManifestXmlDelegate(layerManifest).getInstance().getLayerManifest());
	}

	@Test
	void testResetStructureLayerManifest() {
		StructureManifestXmlDelegate delegate = create();
		StructureLayerManifest layerManifest = mockTypedManifest(ManifestType.STRUCTURE_LAYER_MANIFEST);
		assertOptionalEquals(layerManifest, delegate.reset(layerManifest).getInstance().getLayerManifest());
	}
}
