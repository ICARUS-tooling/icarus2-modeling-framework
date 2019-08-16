/**
 *
 */
package de.ims.icarus2.model.standard.members.layers.annotation.single;

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorageTest;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
class SingleKeyFloatStorageTest implements ManagedAnnotationStorageTest<SingleKeyFloatStorage> {

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyFloatStorage#SingleKeyFloatStorage()}.
		 */
		@Test
		void testSingleKeyFloatStorage() {
			assertNotNull(new SingleKeyFloatStorage());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyFloatStorage#SingleKeyFloatStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {UNSET_INT, 1, 10, 100, 10_000})
		void testSingleKeyFloatStorageInt(int capacity) {
			assertNotNull(new SingleKeyFloatStorage(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyFloatStorage#SingleKeyFloatStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testSingleKeyFloatStorageIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyFloatStorage(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyFloatStorage#SingleKeyFloatStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {UNSET_INT, 1, 10, 100, 10_000})
		void testSingleKeyFloatStorageBooleanInt(int capacity) {
			assertNotNull(new SingleKeyFloatStorage(true, capacity));
			assertNotNull(new SingleKeyFloatStorage(false, capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyFloatStorage#SingleKeyFloatStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testSingleKeyFloatStorageBooleanIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyFloatStorage(true, capacity));
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyFloatStorage(false, capacity));
		}

	}

	@Nested
	class Overflows {

		/**
		 * Test method for {@link SingleKeyFloatStorage#setDouble(de.ims.icarus2.model.api.members.item.Item, String, double)}.
		 */
		@ParameterizedTest
		@ValueSource(doubles = {Double.MAX_VALUE, -Double.MAX_VALUE,
				-Float.MAX_VALUE*1.1, Float.MAX_VALUE*1.1})
		void testFloatOverflow(double value) {
			assertIcarusException(GlobalErrorCode.VALUE_OVERFLOW,
					() -> create().setDouble(mockItem(), key(), value));
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#typesForSetters(String)
	 */
	@Override
	public Set<ValueType> typesForSetters(String key) {
		return NUMBER_TYPES;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#typesForGetters(String)
	 */
	@Override
	public Set<ValueType> typesForGetters(String key) {
		return NUMBER_TYPES;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#testValue(java.lang.String)
	 */
	@Override
	public Object testValue(String key) {
		return Float.valueOf(random().nextFloat());
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#valueType(java.lang.String)
	 */
	@Override
	public ValueType valueType(String key) {
		return ValueType.FLOAT;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#createForLayer(de.ims.icarus2.model.api.layer.AnnotationLayer)
	 */
	@Override
	public SingleKeyFloatStorage createForLayer(AnnotationLayer layer) {
		return new SingleKeyFloatStorage();
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends SingleKeyFloatStorage> getTestTargetClass() {
		return SingleKeyFloatStorage.class;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#noEntryValue(java.lang.String)
	 */
	@Override
	public Object noEntryValue(String key) {
		return Float.valueOf(SingleKeyFloatStorage.DEFAULT_NO_ENTRY_VALUE);
	}

}
