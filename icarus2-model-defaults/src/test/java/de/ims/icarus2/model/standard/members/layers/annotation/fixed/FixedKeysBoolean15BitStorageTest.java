/**
 *
 */
package de.ims.icarus2.model.standard.members.layers.annotation.fixed;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
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
import de.ims.icarus2.model.api.layer.ManagedAnnotationStorageTest;
import de.ims.icarus2.model.api.layer.MultiKeyAnnotationStorageTest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.collections.LookupList;

/**
 * @author Markus Gärtner
 *
 */
class FixedKeysBoolean15BitStorageTest implements MultiKeyAnnotationStorageTest<FixedKeysBoolean15BitStorage>,
		ManagedAnnotationStorageTest<FixedKeysBoolean15BitStorage> {

	private static final LookupList<String> keys = new LookupList<>();
	static {
		IntStream.range(0, FixedKeysBoolean15BitStorage.MAX_KEY_COUNT)
				.forEach(i -> keys.add("test_" + i));

	}

	private boolean[] noEntryValues;

	@BeforeEach
	void setUp() {
		noEntryValues = new boolean[keys.size()];
		for (int i = 0; i < keys.size(); i++) {
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
		return Boolean.valueOf(!noEntryValues[keys.indexOf(key)]);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.ManagedAnnotationStorageTest#supportsAutoRemoval()
	 */
	@Override
	public boolean supportsAutoRemoval() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.MultiKeyAnnotationStorageTest#supportsAutoRemoveAnnotations()
	 */
	@Override
	public boolean supportsAutoRemoveAnnotations() {
		return false;
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
		return Boolean.valueOf(noEntryValues[keys.indexOf(key)]);
	}

	@Override
	public ValueType valueType(String key) {
		return ValueType.BOOLEAN;
	}

	@Override
	public FixedKeysBoolean15BitStorage createForLayer(AnnotationLayer layer) {
		return new FixedKeysBoolean15BitStorage();
	}

	@Override
	public Class<? extends FixedKeysBoolean15BitStorage> getTestTargetClass() {
		return FixedKeysBoolean15BitStorage.class;
	}

	@Override
	public List<String> keys() {
		List<String> list = new ArrayList<>();
		keys.forEach(list::add);
		return list;
	}

	@Override
	public String key() {
		return keys.get(0);
	}

	@Nested
	class Constructors {

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBoolean15BitStorage#FixedKeysBoolean15BitStorage()}.
		 */
		@Test
		void testFixedKeysBoolean15BitStorage() {
			assertNotNull(new FixedKeysBoolean15BitStorage());
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBoolean15BitStorage#FixedKeysBoolean15BitStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { UNSET_INT, 1, 10, 100, 10_000 })
		void testFixedKeysBoolean15BitStorageInt(int capacity) {
			assertNotNull(new FixedKeysBoolean15BitStorage(capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBoolean15BitStorage#FixedKeysBoolean15BitStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { 0, -2 })
		void testFixedKeysBoolean15BitStorageIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT, () -> new FixedKeysBoolean15BitStorage(capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBoolean15BitStorage#FixedKeysBoolean15BitStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { UNSET_INT, 1, 10, 100, 10_000 })
		void testFixedKeysBoolean15BitStorageBooleanInt(int capacity) {
			assertNotNull(new FixedKeysBoolean15BitStorage(true, capacity));
			assertNotNull(new FixedKeysBoolean15BitStorage(false, capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBoolean15BitStorage#FixedKeysBoolean15BitStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { 0, -2 })
		void testFixedKeysBoolean15BitStorageBooleanIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT, () -> new FixedKeysBoolean15BitStorage(true, capacity));
			assertModelException(GlobalErrorCode.INVALID_INPUT, () -> new FixedKeysBoolean15BitStorage(false, capacity));
		}

	}

}
