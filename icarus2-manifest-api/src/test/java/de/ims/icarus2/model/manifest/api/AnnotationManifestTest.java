/**
 *
 */
package de.ims.icarus2.model.manifest.api;

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
public interface AnnotationManifestTest<M extends AnnotationManifest> extends MemberManifestTest<M> {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getLayerManifest()}.
	 */
	@Test
	default void testGetLayerManifest() {
		assertNull(createUnlocked().getLayerManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getKey()}.
	 */
	@Test
	default void testGetKey() {
		assertDerivativeGetter("key1", "key2", null, AnnotationManifest::getKey, AnnotationManifest::setKey);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalKey()}.
	 */
	@Test
	default void testIsLocalKey() {
		assertDerivativeIsLocal("key1", "key2", AnnotationManifest::isLocalKey, AnnotationManifest::setKey);
	}


	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#forEachAlias(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachAlias() {
		assertDerivativeForEach("alias1", "alias2", m -> m::forEachAlias, AnnotationManifest::addAlias);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalAlias(java.lang.String)}.
	 */
	@Test
	default void testIsLocalAlias() {
		assertDerivativeAccumulativeIsLocal("alias1", "alias2",
				AnnotationManifest::isLocalAlias, AnnotationManifest::addAlias);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#forEachLocalAlias(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalAlias() {
		assertDerivativeForEachLocal("alias1", "alias2", m -> m::forEachLocalAlias, AnnotationManifest::addAlias);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getAliases()}.
	 */
	@Test
	default void testGetAliases() {
		assertDerivativeAccumulativeGetter("alias1", "alias2",
				AnnotationManifest::getAliases, AnnotationManifest::addAlias);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getLocalAliases()}.
	 */
	@Test
	default void testGetLocalAliases() {
		assertDerivativeAccumulativeLocalGetter("alias1", "alias2",
				AnnotationManifest::getLocalAliases, AnnotationManifest::addAlias);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isAllowUnknownValues()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testIsAllowUnknownValues() {
		assertDerivativeFlagGetter(AnnotationManifest.DEFAULT_ALLOW_UNKNOWN_VALUES,
				AnnotationManifest::isAllowUnknownValues, AnnotationManifest::setAllowUnknownValues);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getValueRange()}.
	 */
	@Test
	default void testGetValueRange() {
		assertDerivativeGetter(ValueRange.class, null, AnnotationManifest::getValueRange, AnnotationManifest::setValueRange);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalValueRange()}.
	 */
	@Test
	default void testIsLocalValueRange() {
		assertDerivativeIsLocal(ValueRange.class, AnnotationManifest::isLocalValueRange, AnnotationManifest::setValueRange);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getValueSet()}.
	 */
	@Test
	default void testGetValueSet() {
		assertDerivativeGetter(ValueSet.class, null, AnnotationManifest::getValueSet, AnnotationManifest::setValueSet);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalValueSet()}.
	 */
	@Test
	default void testIsLocalValueSet() {
		assertDerivativeIsLocal(ValueSet.class, AnnotationManifest::isLocalValueSet, AnnotationManifest::setValueSet);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getValueType()}.
	 */
	@Test
	default void testGetValueType() {
		ValueType dummyType = mock(ValueType.class);

		for(ValueType valueType : ManifestTestUtils.getAvailableTestTypes()) {
			assertDerivativeGetter(valueType, dummyType, ValueType.STRING,
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
			assertDerivativeIsLocal(valueType, dummyType,
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

			assertDerivativeGetter(value1, value2, null,
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

			assertDerivativeIsLocal(value1, value2,
					AnnotationManifest::isLocalNoEntryValue, AnnotationManifest::setNoEntryValue);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#getContentType()}.
	 */
	@Test
	default void testGetContentType() {
		assertDerivativeGetter(ContentType.class, null,
				AnnotationManifest::getContentType, AnnotationManifest::setContentType);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalContentType()}.
	 */
	@Test
	default void testIsLocalContentType() {
		assertDerivativeIsLocal(ContentType.class,
				AnnotationManifest::isLocalContentType, AnnotationManifest::setContentType);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setKey(java.lang.String)}.
	 */
	@Test
	default void testSetKey() {
		assertSetter(AnnotationManifest::setKey, "key", true);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#addAlias(java.lang.String)}.
	 */
	@Test
	default void testAddAlias() {
		assertAccumulativeAdd(AnnotationManifest::addAlias, null, true, true, "alias1", "alias2", "alias3");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#removeAlias(java.lang.String)}.
	 */
	@Test
	default void testRemoveAlias() {
		assertAccumulativeRemove(AnnotationManifest::addAlias, AnnotationManifest::removeAlias,
				AnnotationManifest::getAliases, true, true, "alias1", "alias2", "alias3");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setValueRange(de.ims.icarus2.model.manifest.api.ValueRange)}.
	 */
	@Test
	default void testSetValueRange() {
		assertSetter(AnnotationManifest::setValueRange, mock(ValueRange.class), false);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setValueSet(de.ims.icarus2.model.manifest.api.ValueSet)}.
	 */
	@Test
	default void testSetValueSet() {
		assertSetter(AnnotationManifest::setValueSet, mock(ValueSet.class), false);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setValueType(de.ims.icarus2.model.manifest.types.ValueType)}.
	 */
	@Test
	default void testSetValueType() {
		assertSetter(AnnotationManifest::setValueType, mock(ValueType.class), true);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setContentType(de.ims.icarus2.util.data.ContentType)}.
	 */
	@Test
	default void testSetContentType() {
		assertSetter(AnnotationManifest::setContentType, mock(ContentType.class), false);
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
			assertSetter(AnnotationManifest::setNoEntryValue, value, false/*, illegalValue*/);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationManifest#setAllowUnknownValues(boolean)}.
	 */
	@Test
	default void testSetAllowUnknownValues() {
		assertSetter(AnnotationManifest::setAllowUnknownValues);
	}

}
