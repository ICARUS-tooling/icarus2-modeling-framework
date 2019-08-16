/**
 *
 */
package de.ims.icarus2.model.standard.members.layers.annotation.fixed;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorageTest;
import de.ims.icarus2.model.api.layer.annotation.MultiKeyAnnotationStorageTest;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
class FixedKeysBoolean7BitStorageTest implements MultiKeyAnnotationStorageTest<FixedKeysBoolean7BitStorage>,
		ManagedAnnotationStorageTest<FixedKeysBoolean7BitStorage> {

	private static final String[] keys = IntStream.range(0,
			FixedKeysBoolean7BitStorage.MAX_KEY_COUNT)
			.mapToObj(i -> "test_"+i)
			.toArray(String[]::new);

	private boolean[] noEntryValues;

	@BeforeEach
	void setUp() {
		noEntryValues = new boolean[keys.length];
		for (int i = 0; i < keys.length; i++) {
			noEntryValues[i] = random().nextBoolean();
		}
	}

	@AfterEach
	void tearDown() {
		noEntryValues = null;
	}

	@Override
	public Set<ValueType> typesForSetters(String key) {
		return singleton(ValueType.BOOLEAN);
	}

	@Override
	public Set<ValueType> typesForGetters(String key) {
		return singleton(ValueType.BOOLEAN);
	}

	@Override
	public Object testValue(String key) {
		return Boolean.valueOf(!noEntryValues[Arrays.binarySearch(keys, key)]);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorageTest#supportsAutoRemoval()
	 */
	@Override
	public boolean supportsAutoRemoval() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.MultiKeyAnnotationStorageTest#supportsAutoRemoveAnnotations()
	 */
	@Override
	public boolean supportsAutoRemoveAnnotations() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorageTest#supportsItemManagement()
	 */
	@Override
	public boolean supportsItemManagement() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#noEntryValue(java.lang.String)
	 */
	@Override
	public Object noEntryValue(String key) {
		return Boolean.valueOf(noEntryValues[Arrays.binarySearch(keys, key)]);
	}

	@Override
	public ValueType valueType(String key) {
		return ValueType.BOOLEAN;
	}

	@Override
	public FixedKeysBoolean7BitStorage createForLayer(AnnotationLayer layer) {
		return new FixedKeysBoolean7BitStorage();
	}

	@Override
	public Class<? extends FixedKeysBoolean7BitStorage> getTestTargetClass() {
		return FixedKeysBoolean7BitStorage.class;
	}

	@Override
	public List<String> keys() {
		return Arrays.asList(keys);
	}

	@Override
	public String key() {
		return keys[0];
	}

	@Nested
	class Constructors {

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBoolean7BitStorage#FixedKeysBoolean7BitStorage()}.
		 */
		@Test
		void testFixedKeysBoolean7BitStorage() {
			assertNotNull(new FixedKeysBoolean7BitStorage());
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBoolean7BitStorage#FixedKeysBoolean7BitStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { UNSET_INT, 1, 10, 100, 10_000 })
		void testFixedKeysBoolean7BitStorageInt(int capacity) {
			assertNotNull(new FixedKeysBoolean7BitStorage(capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBoolean7BitStorage#FixedKeysBoolean7BitStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { 0, -2 })
		void testFixedKeysBoolean7BitStorageIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT, () -> new FixedKeysBoolean7BitStorage(capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBoolean7BitStorage#FixedKeysBoolean7BitStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { UNSET_INT, 1, 10, 100, 10_000 })
		void testFixedKeysBoolean7BitStorageBooleanInt(int capacity) {
			assertNotNull(new FixedKeysBoolean7BitStorage(true, capacity));
			assertNotNull(new FixedKeysBoolean7BitStorage(false, capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBoolean7BitStorage#FixedKeysBoolean7BitStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { 0, -2 })
		void testFixedKeysBoolean7BitStorageBooleanIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT, () -> new FixedKeysBoolean7BitStorage(true, capacity));
			assertModelException(GlobalErrorCode.INVALID_INPUT, () -> new FixedKeysBoolean7BitStorage(false, capacity));
		}

	}

}
