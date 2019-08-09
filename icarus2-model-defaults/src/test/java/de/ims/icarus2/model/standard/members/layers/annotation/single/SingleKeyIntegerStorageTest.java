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
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.standard.members.layers.annotation.ManagedAnnotationStorageTest;

/**
 * @author Markus Gärtner
 *
 */
class SingleKeyIntegerStorageTest implements ManagedAnnotationStorageTest<SingleKeyIntegerStorage> {

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyIntegerStorage#SingleKeyIntegerStorage()}.
		 */
		@Test
		void testSingleKeyIntegerStorage() {
			assertNotNull(new SingleKeyIntegerStorage());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyIntegerStorage#SingleKeyIntegerStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {UNSET_INT, 1, 10, 100, 10_000})
		void testSingleKeyIntegerStorageInt(int capacity) {
			assertNotNull(new SingleKeyIntegerStorage(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyIntegerStorage#SingleKeyIntegerStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testSingleKeyIntegerStorageIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyIntegerStorage(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyIntegerStorage#SingleKeyIntegerStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {UNSET_INT, 1, 10, 100, 10_000})
		void testSingleKeyIntegerStorageBooleanInt(int capacity) {
			assertNotNull(new SingleKeyIntegerStorage(true, capacity));
			assertNotNull(new SingleKeyIntegerStorage(false, capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyIntegerStorage#SingleKeyIntegerStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testSingleKeyIntegerStorageBooleanIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyIntegerStorage(true, capacity));
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyIntegerStorage(false, capacity));
		}

	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationStorageTest#typesForSetters()
	 */
	@Override
	public Set<ValueType> typesForSetters() {
		return set(ValueType.INTEGER, ValueType.LONG);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationStorageTest#typesForGetters()
	 */
	@Override
	public Set<ValueType> typesForGetters() {
		return NUMBER_TYPES;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationStorageTest#testValue(java.lang.String)
	 */
	@Override
	public Object testValue(String key) {
		return Integer.valueOf(random().nextInt());
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationStorageTest#createForLayer(de.ims.icarus2.model.api.layer.AnnotationLayer)
	 */
	@Override
	public SingleKeyIntegerStorage createForLayer(AnnotationLayer layer) {
		return new SingleKeyIntegerStorage();
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends SingleKeyIntegerStorage> getTestTargetClass() {
		return SingleKeyIntegerStorage.class;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationStorageTest#noEntryValue(java.lang.String)
	 */
	@Override
	public Object noEntryValue(String key) {
		return Integer.valueOf(SingleKeyIntegerStorage.DEFAULT_NO_ENTRY_VALUE);
	}
}
