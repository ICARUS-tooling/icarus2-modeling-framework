/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.test.GenericTest.NO_DEFAULT;
import static de.ims.icarus2.test.GenericTest.NO_ILLEGAL;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.data.ContentType;

/**
 * @author Markus Gärtner
 *
 */
public interface AnnotationManifestTest<M extends AnnotationManifest> extends EmbeddedMemberManifestTest<M> {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getLayerManifest()}.
	 */
	@Test
	default void testGetLayerManifest() {
		assertNotNull(createUnlocked().getLayerManifest());
		assertNull(createTemplate(settings()).getLayerManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getKey()}.
	 */
	@Test
	default void testGetKey() {
		assertDerivativeGetter(settings(), "key1", "key2", NO_DEFAULT(), AnnotationManifest::getKey, AnnotationManifest::setKey);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalKey()}.
	 */
	@Test
	default void testIsLocalKey() {
		assertDerivativeIsLocal(settings(), "key1", "key2", AnnotationManifest::isLocalKey, AnnotationManifest::setKey);
	}


	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#forEachAlias(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachAlias() {
		assertDerivativeForEach(settings(), "alias1", "alias2", m -> m::forEachAlias, AnnotationManifest::addAlias);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalAlias(java.lang.String)}.
	 */
	@Test
	default void testIsLocalAlias() {
		assertDerivativeAccumulativeIsLocal(settings(), "alias1", "alias2",
				AnnotationManifest::isLocalAlias, AnnotationManifest::addAlias);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#forEachLocalAlias(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalAlias() {
		assertDerivativeForEachLocal(settings(), "alias1", "alias2", m -> m::forEachLocalAlias, AnnotationManifest::addAlias);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getAliases()}.
	 */
	@Test
	default void testGetAliases() {
		assertDerivativeAccumulativeGetter(settings(), "alias1", "alias2",
				AnnotationManifest::getAliases, AnnotationManifest::addAlias);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getLocalAliases()}.
	 */
	@Test
	default void testGetLocalAliases() {
		assertDerivativeAccumulativeLocalGetter(settings(), "alias1", "alias2",
				AnnotationManifest::getLocalAliases, AnnotationManifest::addAlias);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isAllowUnknownValues()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testIsAllowUnknownValues() {
		assertDerivativeFlagGetter(settings(), AnnotationManifest.DEFAULT_ALLOW_UNKNOWN_VALUES,
				AnnotationManifest::isAllowUnknownValues, AnnotationManifest::setAllowUnknownValues);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getValueRange()}.
	 */
	@Test
	default void testGetValueRange() {
		assertDerivativeGetter(settings(), mock(ValueRange.class), mock(ValueRange.class), null,
				AnnotationManifest::getValueRange, AnnotationManifest::setValueRange);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalValueRange()}.
	 */
	@Test
	default void testIsLocalValueRange() {
		assertDerivativeIsLocal(settings(), mock(ValueRange.class), mock(ValueRange.class),
				AnnotationManifest::isLocalValueRange, AnnotationManifest::setValueRange);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getValueSet()}.
	 */
	@Test
	default void testGetValueSet() {
		assertDerivativeGetter(settings(), mock(ValueSet.class), mock(ValueSet.class), NO_DEFAULT(),
				AnnotationManifest::getValueSet, AnnotationManifest::setValueSet);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalValueSet()}.
	 */
	@Test
	default void testIsLocalValueSet() {
		assertDerivativeIsLocal(settings(), mock(ValueSet.class), mock(ValueSet.class),
				AnnotationManifest::isLocalValueSet, AnnotationManifest::setValueSet);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getValueType()}.
	 */
	@Test
	default void testGetValueType() {
		ValueType dummyType = mock(ValueType.class);

		for(ValueType valueType : ManifestTestUtils.getAvailableTestTypes()) {
			assertDerivativeGetter(settings(), valueType, dummyType, ValueType.STRING,
					AnnotationManifest::getValueType, AnnotationManifest::setValueType);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalValueType()}.
	 */
	@Test
	default void testIsLocalValueType() {
		ValueType dummyType = mock(ValueType.class);

		for(ValueType valueType : ManifestTestUtils.getAvailableTestTypes()) {
			assertDerivativeIsLocal(settings(), valueType, dummyType,
					AnnotationManifest::isLocalValueType, AnnotationManifest::setValueType);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getNoEntryValue()}.
	 */
	@Test
	default void testGetNoEntryValue() {

		for(ValueType valueType : ManifestTestUtils.getAvailableTestTypes()) {
			Object[] values = ManifestTestUtils.getTestValues(valueType);
			assertTrue(values.length>1, "Insufficient test values for type: "+valueType);
			Object value1 = values[0];
			Object value2 = values[1];

			assertDerivativeGetter(settings(), value1, value2, NO_DEFAULT(),
					AnnotationManifest::getNoEntryValue, AnnotationManifest::setNoEntryValue);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalNoEntryValue()}.
	 */
	@Test
	default void testIsLocalNoEntryValue() {

		for(ValueType valueType : ManifestTestUtils.getAvailableTestTypes()) {
			Object[] values = ManifestTestUtils.getTestValues(valueType);
			assertTrue(values.length>1, "Insufficient test valeus for type: "+valueType);
			Object value1 = values[0];
			Object value2 = values[1];

			assertDerivativeIsLocal(settings(), value1, value2,
					AnnotationManifest::isLocalNoEntryValue, AnnotationManifest::setNoEntryValue);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getContentType()}.
	 */
	@Test
	default void testGetContentType() {
		assertDerivativeGetter(settings(), mock(ContentType.class), mock(ContentType.class), NO_DEFAULT(),
				AnnotationManifest::getContentType, AnnotationManifest::setContentType);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalContentType()}.
	 */
	@Test
	default void testIsLocalContentType() {
		assertDerivativeIsLocal(settings(), mock(ContentType.class), mock(ContentType.class),
				AnnotationManifest::isLocalContentType, AnnotationManifest::setContentType);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setKey(java.lang.String)}.
	 */
	@Test
	default void testSetKey() {
		assertLockableSetter(settings(),AnnotationManifest::setKey, "key", true, TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#addAlias(java.lang.String)}.
	 */
	@Test
	default void testAddAlias() {
		assertLockableAccumulativeAdd(settings(),
				AnnotationManifest::addAlias, NO_ILLEGAL(),
				TYPE_CAST_CHECK, true, INVALID_INPUT_CHECK,
				"alias1", "alias2", "alias3");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#removeAlias(java.lang.String)}.
	 */
	@Test
	default void testRemoveAlias() {
		assertLockableAccumulativeRemove(settings(),
				AnnotationManifest::addAlias, AnnotationManifest::removeAlias,
				AnnotationManifest::getAliases,
				true, INVALID_INPUT_CHECK, "alias1", "alias2", "alias3");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setValueRange(de.ims.icarus2.model.manifest.api.ValueRange)}.
	 */
	@Test
	default void testSetValueRange() {
		assertLockableSetter(settings(),AnnotationManifest::setValueRange, mock(ValueRange.class), false, TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setValueSet(de.ims.icarus2.model.manifest.api.ValueSet)}.
	 */
	@Test
	default void testSetValueSet() {
		assertLockableSetter(settings(),AnnotationManifest::setValueSet, mock(ValueSet.class), false, TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setValueType(de.ims.icarus2.model.manifest.types.ValueType)}.
	 */
	@Test
	default void testSetValueType() {
		assertLockableSetter(settings(),AnnotationManifest::setValueType, mock(ValueType.class), true, TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setContentType(de.ims.icarus2.util.data.ContentType)}.
	 */
	@Test
	default void testSetContentType() {
		assertLockableSetter(settings(),AnnotationManifest::setContentType, mock(ContentType.class), false, TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setNoEntryValue(java.lang.Object)}.
	 */
	@Test
	default void testSetNoEntryValue() {
		for(ValueType valueType : ManifestTestUtils.getAvailableTestTypes()) {
			Object value = ManifestTestUtils.getTestValue(valueType);
			//TODO verify if we need value check for noEntryValue field
//			Object illegalValue = ManifestTestUtils.getIllegalValue(valueType);
			assertLockableSetter(settings(),AnnotationManifest::setNoEntryValue, value, false, TYPE_CAST_CHECK/*, illegalValue*/);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setAllowUnknownValues(boolean)}.
	 */
	@Test
	default void testSetAllowUnknownValues() {
		assertLockableSetter(settings(),AnnotationManifest::setAllowUnknownValues);
	}

}
