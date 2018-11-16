/**
 *
 */
package de.ims.icarus2.model.manifest.xml.delegates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.stream.Collectors;

import de.ims.icarus2.model.manifest.ManifestGenerator;
import de.ims.icarus2.model.manifest.ManifestGenerator.Config;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.ValueManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;

/**
 * @author Markus Gärtner
 *
 */
class ValueManifestXmlDelegateTest implements ManifestXmlDelegateTest<ValueManifest, ValueManifestXmlDelegate> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends ValueManifestXmlDelegate> getTestTargetClass() {
		return ValueManifestXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.VALUE_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#configurations()
	 */
	@Override
	public List<Config> configurations() {
		return ValueManifest.SUPPORTED_VALUE_TYPES
			.stream()
			.filter(ValueType::isSimpleType)
			.map(type -> ManifestGenerator.config()
					.valueType(type)
					.label(type.getName()))
			.collect(Collectors.toList());
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	public void testMandatoryConstructors() throws Exception {
		createNoArgs();

		ValueManifest manifest = mock(ValueManifest.class);
		assertEquals(manifest, new ValueManifestXmlDelegate(manifest).getInstance());

		for(ValueType valueType : ValueType.valueTypes()) {
			assertEquals(valueType, new ValueManifestXmlDelegate(valueType).getInstance().getValueType());
		}
	}

}
