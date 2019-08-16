/**
 *
 */
package de.ims.icarus2.model.standard.members.layers.annotation.single;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.TestUtils.random;
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
import de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorageTest;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
class SingleKeyLongStorageTest implements ManagedAnnotationStorageTest<SingleKeyLongStorage> {

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyLongStorage#SingleKeyLongStorage()}.
		 */
		@Test
		void testSingleKeyLongStorage() {
			assertNotNull(new SingleKeyLongStorage());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyLongStorage#SingleKeyLongStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {UNSET_INT, 1, 10, 100, 10_000})
		void testSingleKeyLongStorageInt(int capacity) {
			assertNotNull(new SingleKeyLongStorage(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyLongStorage#SingleKeyLongStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testSingleKeyLongStorageIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyLongStorage(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyLongStorage#SingleKeyLongStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {UNSET_INT, 1, 10, 100, 10_000})
		void testSingleKeyLongStorageBooleanInt(int capacity) {
			assertNotNull(new SingleKeyLongStorage(true, capacity));
			assertNotNull(new SingleKeyLongStorage(false, capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyLongStorage#SingleKeyLongStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testSingleKeyLongStorageBooleanIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyLongStorage(true, capacity));
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyLongStorage(false, capacity));
		}

	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#typesForSetters(String)
	 */
	@Override
	public Set<ValueType> typesForSetters(String key) {
		return set(ValueType.INTEGER, ValueType.LONG);
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
		return Long.valueOf(random().nextLong());
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#valueType(java.lang.String)
	 */
	@Override
	public ValueType valueType(String key) {
		return ValueType.LONG;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#createForLayer(de.ims.icarus2.model.api.layer.AnnotationLayer)
	 */
	@Override
	public SingleKeyLongStorage createForLayer(AnnotationLayer layer) {
		return new SingleKeyLongStorage();
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends SingleKeyLongStorage> getTestTargetClass() {
		return SingleKeyLongStorage.class;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#noEntryValue(java.lang.String)
	 */
	@Override
	public Object noEntryValue(String key) {
		return Long.valueOf(SingleKeyLongStorage.DEFAULT_NO_ENTRY_VALUE);
	}
}
