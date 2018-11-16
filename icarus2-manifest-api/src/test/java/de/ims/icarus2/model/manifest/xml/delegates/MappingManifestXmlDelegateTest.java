/**
 *
 */
package de.ims.icarus2.model.manifest.xml.delegates;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestGenerator.Config;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;

/**
 * @author Markus Gärtner
 *
 */
class MappingManifestXmlDelegateTest implements ManifestXmlDelegateTest<MappingManifest, MappingManifestXmlDelegate>{

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends MappingManifestXmlDelegate> getTestTargetClass() {
		return MappingManifestXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.MAPPING_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#configurations()
	 */
	@Override
	public List<Config> configurations() {
		// TODO Auto-generated method stub
		return ManifestXmlDelegateTest.super.configurations();
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	public void testMandatoryConstructors() throws Exception {
		createNoArgs();
	}

	@Test
	void testResetDriverManifest() {
		MappingManifestXmlDelegate delegate = create();
		DriverManifest driverManifest = mockTypedManifest(ManifestType.DRIVER_MANIFEST);
		assertOptionalEquals(driverManifest, delegate.reset(driverManifest).getInstance().getDriverManifest());
	}
}
