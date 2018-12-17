/**
 *
 */
package de.ims.icarus2.model.manifest.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.types.ValueType.MatrixType;
import de.ims.icarus2.model.manifest.types.ValueType.VectorType;

/**
 * Test suite for {@link ValueType}
 *
 * @author Markus Gärtner
 *
 */
class ValueTypeTest {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#parseValueType(java.lang.String)}.
	 */
	@TestFactory
	Stream<DynamicTest> testParseValueType() {
		return ValueType.valueTypes()
				.stream()
				.flatMap(valueType -> Stream.of(
						DynamicTest.dynamicTest(valueType.getName(), () -> {
							assertEquals(valueType, ValueType.parseValueType(valueType.getStringValue()));
						}),
						DynamicTest.dynamicTest(valueType.getName()+" as vector", () -> {
							VectorType vectorType1 = VectorType.withUndefinedSize(valueType);
							assertEquals(vectorType1, ValueType.parseValueType(vectorType1.getStringValue()));

							VectorType vectorType2 = VectorType.withSize(valueType, 5);
							assertEquals(vectorType2, ValueType.parseValueType(vectorType2.getStringValue()));
						}),
						DynamicTest.dynamicTest(valueType.getName()+" as matrix", () -> {
							MatrixType matrixType = MatrixType.withSize(valueType, 4, 7);
							assertEquals(matrixType, ValueType.parseValueType(matrixType.getStringValue()));
						})));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#toChars(java.lang.Object)}.
	 */
	@Test
	void testToChars() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#parse(java.lang.CharSequence, java.lang.ClassLoader)}.
	 */
	@Test
	void testParse() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#persist(java.lang.Object)}.
	 */
	@Test
	void testPersist() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#parseAndPersist(java.lang.CharSequence, java.lang.ClassLoader)}.
	 */
	@Test
	void testParseAndPersist() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#getStringValue()}.
	 */
	@Test
	void testGetStringValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#getName()}.
	 */
	@Test
	void testGetName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#getBaseClass()}.
	 */
	@Test
	void testGetBaseClass() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#isValidValue(java.lang.Object)}.
	 */
	@Test
	void testIsValidValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#isValidType(java.lang.Class)}.
	 */
	@Test
	void testIsValidType() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#valueTypes()}.
	 */
	@Test
	void testValueTypes() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#basicValueTypes()}.
	 */
	@Test
	void testBasicValueTypes() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#simpleValueTypes()}.
	 */
	@Test
	void testSimpleValueTypes() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#checkValue(java.lang.Object)}.
	 */
	@Test
	void testCheckValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#checkValues(java.util.Collection)}.
	 */
	@Test
	void testCheckValuesCollectionOfQ() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.types.ValueType#checkValues(java.lang.Object[])}.
	 */
	@Test
	void testCheckValuesObjectArray() {
		fail("Not yet implemented"); // TODO
	}

}
