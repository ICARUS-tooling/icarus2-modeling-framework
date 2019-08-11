/**
 *
 */
package de.ims.icarus2.model.standard.members.layers.annotation.single;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ManagedAnnotationStorageTest;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
class SingleKeyBooleanStorageTest implements ManagedAnnotationStorageTest<SingleKeyBooleanStorage> {


	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyBooleanStorage#SingleKeyBooleanStorage()}.
		 */
		@Test
		void testSingleKeyBooleanStorage() {
			assertNotNull(new SingleKeyBooleanStorage());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyBooleanStorage#SingleKeyBooleanStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {UNSET_INT, 1, 10, 100, 10_000})
		void testSingleKeyBooleanStorageInt(int capacity) {
			assertNotNull(new SingleKeyBooleanStorage(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyBooleanStorage#SingleKeyBooleanStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testSingleKeyBooleanStorageIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyBooleanStorage(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyBooleanStorage#SingleKeyBooleanStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {UNSET_INT, 1, 10, 100, 10_000})
		void testSingleKeyBooleanStorageBooleanInt(int capacity) {
			assertNotNull(new SingleKeyBooleanStorage(true, capacity));
			assertNotNull(new SingleKeyBooleanStorage(false, capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyBooleanStorage#SingleKeyBooleanStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testSingleKeyBooleanStorageBooleanIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyBooleanStorage(true, capacity));
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyBooleanStorage(false, capacity));
		}

	}

	/**
	 * @see de.ims.icarus2.model.api.layer.ManagedAnnotationStorageTest#supportsItemManagement()
	 */
	@Override
	public boolean supportsItemManagement() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationStorageTest#noEntryValue(java.lang.String)
	 */
	@Override
	public Object noEntryValue(String key) {
		return Boolean.valueOf(SingleKeyBooleanStorage.DEFAULT_NO_ENTRY_VALUE);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationStorageTest#typesForSetters(String)
	 */
	@Override
	public Set<ValueType> typesForSetters(String key) {
		return set(ValueType.BOOLEAN);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationStorageTest#typesForGetters(String)
	 */
	@Override
	public Set<ValueType> typesForGetters(String key) {
		return set(ValueType.BOOLEAN);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationStorageTest#testValue(java.lang.String)
	 */
	@Override
	public Object testValue(String key) {
		return Boolean.TRUE;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationStorageTest#valueType(java.lang.String)
	 */
	@Override
	public ValueType valueType(String key) {
		return ValueType.BOOLEAN;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationStorageTest#createForLayer(de.ims.icarus2.model.api.layer.AnnotationLayer)
	 */
	@Override
	public SingleKeyBooleanStorage createForLayer(AnnotationLayer layer) {
		return new SingleKeyBooleanStorage();
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends SingleKeyBooleanStorage> getTestTargetClass() {
		return SingleKeyBooleanStorage.class;
	}
}
