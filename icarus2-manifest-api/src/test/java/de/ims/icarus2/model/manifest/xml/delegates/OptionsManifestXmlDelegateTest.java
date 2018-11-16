/**
 *
 */
package de.ims.icarus2.model.manifest.xml.delegates;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.ims.icarus2.model.manifest.ManifestGenerator;
import de.ims.icarus2.model.manifest.ManifestGenerator.Config;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.OptionsManifest;
import de.ims.icarus2.model.manifest.api.OptionsManifest.Option;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;

/**
 * @author Markus Gärtner
 *
 */
class OptionsManifestXmlDelegateTest implements ManifestXmlDelegateTest<OptionsManifest, OptionsManifestXmlDelegate> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends OptionsManifestXmlDelegate> getTestTargetClass() {
		return OptionsManifestXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.OPTIONS_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#configurations()
	 */
	@Override
	public List<Config> configurations() {
		return Stream.of(Boolean.FALSE, Boolean.TRUE)
				.map(b -> ManifestGenerator.config().label("multivalue="+b)
						.preprocessor(ManifestType.OPTION, m -> ((Option)m).setMultiValue(true)))
				.collect(Collectors.toList());
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	public void testMandatoryConstructors() throws Exception {
		createNoArgs();

		OptionsManifest manifest = mockManifest();
		assertEquals(manifest, new OptionsManifestXmlDelegate(manifest).getInstance());
	}
}
