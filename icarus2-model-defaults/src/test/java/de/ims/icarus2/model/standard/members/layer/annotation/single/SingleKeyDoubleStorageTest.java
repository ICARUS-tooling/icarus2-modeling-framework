/**
 *
 */
package de.ims.icarus2.model.standard.members.layer.annotation.single;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
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
import de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyDoubleStorage;

/**
 * @author Markus Gärtner
 *
 */
class SingleKeyDoubleStorageTest implements ManagedAnnotationStorageTest<SingleKeyDoubleStorage> {

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.single.SingleKeyDoubleStorage#SingleKeyDoubleStorage()}.
		 */
		@Test
		void testSingleKeyDoubleStorage() {
			assertNotNull(new SingleKeyDoubleStorage());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.single.SingleKeyDoubleStorage#SingleKeyDoubleStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {UNSET_INT, 1, 10, 100, 10_000})
		void testSingleKeyDoubleStorageInt(int capacity) {
			assertNotNull(new SingleKeyDoubleStorage(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyDoubleStorage#SingleKeyDoubleStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testSingleKeyDoubleStorageIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyDoubleStorage(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyDoubleStorage#SingleKeyDoubleStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {UNSET_INT, 1, 10, 100, 10_000})
		void testSingleKeyDoubleStorageBooleanInt(int capacity) {
			assertNotNull(new SingleKeyDoubleStorage(true, capacity));
			assertNotNull(new SingleKeyDoubleStorage(false, capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyDoubleStorage#SingleKeyDoubleStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testSingleKeyDoubleStorageBooleanIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyDoubleStorage(true, capacity));
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyDoubleStorage(false, capacity));
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
		return Double.valueOf(random().nextDouble());
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#valueType(java.lang.String)
	 */
	@Override
	public ValueType valueType(String key) {
		return ValueType.DOUBLE;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#createForLayer(de.ims.icarus2.model.api.layer.AnnotationLayer)
	 */
	@Override
	public SingleKeyDoubleStorage createForLayer(AnnotationLayer layer) {
		return new SingleKeyDoubleStorage();
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends SingleKeyDoubleStorage> getTestTargetClass() {
		return SingleKeyDoubleStorage.class;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#noEntryValue(java.lang.String)
	 */
	@Override
	public Object noEntryValue(String key) {
		return Double.valueOf(SingleKeyDoubleStorage.DEFAULT_NO_ENTRY_VALUE);
	}
}
