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
import de.ims.icarus2.model.api.layer.AnnotationStorageTest;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
class SingleKeyObjectStorageTest implements AnnotationStorageTest<SingleKeyObjectStorage> {

	@Override
	public Class<? extends SingleKeyObjectStorage> getTestTargetClass() {
		return SingleKeyObjectStorage.class;
	}

	@Override
	public Object testValue(String key) {
		return new Object();
	}

	@Override
	public Set<ValueType> typesForSetters() {
		return set(ValueType.UNKNOWN);
	}

	@Override
	public Set<ValueType> typesForGetters() {
		return set(ValueType.UNKNOWN);
	}

	@Override
	public SingleKeyObjectStorage createForLayer(AnnotationLayer layer) {
		SingleKeyObjectStorage storage = new SingleKeyObjectStorage();
		storage.addNotify(layer);
		return storage;
	}

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyObjectStorage#SingleKeyObjectStorage()}.
		 */
		@Test
		void testSingleKeyObjectStorage() {
			assertNotNull(new SingleKeyObjectStorage());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyObjectStorage#SingleKeyObjectStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {UNSET_INT, 1, 10, 100, 10_000})
		void testSingleKeyObjectStorageInt(int capacity) {
			assertNotNull(new SingleKeyObjectStorage(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyObjectStorage#SingleKeyObjectStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testSingleKeyObjectStorageIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyObjectStorage(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyObjectStorage#SingleKeyObjectStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {UNSET_INT, 1, 10, 100, 10_000})
		void testSingleKeyObjectStorageBooleanInt(int capacity) {
			assertNotNull(new SingleKeyObjectStorage(true, capacity));
			assertNotNull(new SingleKeyObjectStorage(false, capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyObjectStorage#SingleKeyObjectStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testSingleKeyObjectStorageBooleanIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyObjectStorage(true, capacity));
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyObjectStorage(false, capacity));
		}

	}

}
