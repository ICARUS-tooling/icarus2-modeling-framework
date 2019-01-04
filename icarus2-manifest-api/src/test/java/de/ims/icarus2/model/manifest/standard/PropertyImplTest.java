/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.LockableTest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.MemberManifest.Property;
import de.ims.icarus2.model.manifest.api.OptionsManifest.Option;
import de.ims.icarus2.model.manifest.api.PropertyTest;
import de.ims.icarus2.model.manifest.standard.AbstractMemberManifest.PropertyImpl;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class PropertyImplTest implements PropertyTest {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends Property> getTestTargetClass() {
		return PropertyImpl.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.PropertyTest#createTestInstance(de.ims.icarus2.test.TestSettings, java.lang.String, de.ims.icarus2.model.manifest.types.ValueType)
	 */
	@Override
	public Property createTestInstance(TestSettings settings, String name, ValueType valueType) {
		return settings.process(new PropertyImpl(name, valueType));
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public Property createTestInstance(TestSettings settings) {
		return createTestInstance(settings, "propertyX", ValueType.DEFAULT_VALUE_TYPE);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.AbstractMemberManifest.PropertyImpl#setOption(de.ims.icarus2.model.manifest.api.OptionsManifest.Option)}.
	 */
	@Test
	void testSetOption() {
		Option option = mockTypedManifest(ManifestType.OPTION);
		when(option.getValueType()).thenReturn(ValueType.DEFAULT_VALUE_TYPE);

		LockableTest.assertLockableSetter(settings(),
				new PropertyImpl("noName", ValueType.STRING),
				PropertyImpl::setOption,
				option,
				NO_NPE_CHECK, NO_CHECK);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.PropertyTest#testGetOption()
	 */
	@TestFactory
	Stream<DynamicTest> testGetOptionByValueType() {
		return ValueType.valueTypes().stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
					PropertyImpl property = (PropertyImpl) createTestInstance(settings(), "property1", valueType);

					Option option = mockTypedManifest(ManifestType.OPTION);
					when(option.getValueType()).thenReturn(property.getValueType());

					property.setOption(option);

					assertOptionalEquals(option, property.getOption());
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.AbstractMemberManifest.PropertyImpl#setName(java.lang.String)}.
	 */
	@Test
	void testSetName() {
		LockableTest.assertLockableSetterBatch(settings(),
				new PropertyImpl("noName", ValueType.STRING),
				PropertyImpl::setName,
				ManifestTestUtils.getLegalIdValues(),
				NPE_CHECK, INVALID_ID_CHECK, ManifestTestUtils.getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.AbstractMemberManifest.PropertyImpl#setValueType(de.ims.icarus2.model.manifest.types.ValueType)}.
	 */
	@TestFactory
	Stream<DynamicTest> testSetValueType() {
		return ValueType.valueTypes().stream()
				.map(valueType -> DynamicTest.dynamicTest(valueType.getName(), () -> {
					LockableTest.assertLockableSetter(settings(),
							new PropertyImpl("noName", ValueType.STRING),
							PropertyImpl::setValueType,
							valueType, NPE_CHECK, NO_CHECK);
				}));
	}
}
