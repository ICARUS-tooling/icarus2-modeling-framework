/**
 *
 */
package de.ims.icarus2.model.manifest.xml.delegates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestGenerator;
import de.ims.icarus2.model.manifest.ManifestGenerator.Config;
import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.ValueSet;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;

/**
 * @author Markus Gärtner
 *
 */
class ValueSetXmlDelegateTest implements ManifestXmlDelegateTest<ValueSet, ValueSetXmlDelegate> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends ValueSetXmlDelegate> getTestTargetClass() {
		return ValueSetXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.VALUE_SET;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#configurations()
	 */
	@Override
	public List<Config> configurations() {
		return ManifestTestUtils.getAvailableTestTypes()
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

		ValueSet valueSet = mock(ValueSet.class);
		assertEquals(valueSet, new ValueSetXmlDelegate(valueSet).getInstance());

		for(ValueType valueType : ValueType.valueTypes()) {
			assertEquals(valueType, new ValueSetXmlDelegate(valueType).getInstance().getValueType());
		}
	}

	/**
	 * Test method for {@link ValueSetXmlDelegate#reset(ValueType)}
	 */
	@Test
	void testResetValueType() {
		ValueSetXmlDelegate delegate = create();
		for(ValueType valueType : ValueType.valueTypes()) {
			assertEquals(valueType, delegate.reset(valueType).getInstance().getValueType());
		}
	}
}
