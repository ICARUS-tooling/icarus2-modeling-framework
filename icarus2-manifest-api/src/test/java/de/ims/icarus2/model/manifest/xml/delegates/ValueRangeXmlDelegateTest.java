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
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;
import de.ims.icarus2.test.annotations.OverrideTest;

/**
 * @author Markus Gärtner
 *
 */
class ValueRangeXmlDelegateTest implements ManifestXmlDelegateTest<ValueRange, ValueRangeXmlDelegate> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends ValueRangeXmlDelegate> getTestTargetClass() {
		return ValueRangeXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.VALUE_RANGE;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#configurations()
	 */
	@Override
	public List<Config> configurations() {
		return ValueRange.SUPPORTED_VALUE_TYPES
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
	@OverrideTest
	@Test
	public void testMandatoryConstructors() throws Exception {
		createNoArgs();

		ValueRange valueRange = mock(ValueRange.class);
		assertEquals(valueRange, new ValueRangeXmlDelegate(valueRange).getInstance());

		for(ValueType valueType : ValueRange.SUPPORTED_VALUE_TYPES) {
			assertEquals(valueType, new ValueRangeXmlDelegate(valueType).getInstance().getValueType());
		}
	}

	@Test
	void testResetValueType() {
		ValueRangeXmlDelegate delegate = create();
		for(ValueType valueType : ValueRange.SUPPORTED_VALUE_TYPES) {
			assertEquals(valueType, delegate.reset(valueType).getInstance().getValueType());
		}
	}
}
