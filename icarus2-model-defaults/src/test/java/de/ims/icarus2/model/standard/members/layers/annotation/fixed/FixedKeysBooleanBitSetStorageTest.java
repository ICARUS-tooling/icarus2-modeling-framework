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

/**
 * @author Markus Gärtner
 *
 */
class FixedKeysBooleanBitSetStorageTest implements MultiKeyAnnotationStorageTest<FixedKeysBooleanBitSetStorage>,
		ManagedAnnotationStorageTest<FixedKeysBooleanBitSetStorage> {

	private List<String> keys = new ArrayList<>();
	private boolean[] noEntryValues;

	@BeforeEach
	void setUp() {
		int size = random(5, 20);
		noEntryValues = new boolean[size];
		for (int i = 0; i < size; i++) {
			noEntryValues[i] = random().nextBoolean();
			keys.add("test_"+i);
		}
	}

	@AfterEach
	void tearDown() {
		keys.clear();
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
	public FixedKeysBooleanBitSetStorage createForLayer(AnnotationLayer layer) {
		return new FixedKeysBooleanBitSetStorage();
	}

	@Override
	public Class<? extends FixedKeysBooleanBitSetStorage> getTestTargetClass() {
		return FixedKeysBooleanBitSetStorage.class;
	}

	@Override
	public List<String> keys() {
		return keys;
	}

	@Override
	public String key() {
		return keys.get(0);
	}

	@Nested
	class Constructors {

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBooleanBitSetStorage#FixedKeysBooleanBitSetStorage()}.
		 */
		@Test
		void testFixedKeysBooleanBitSetStorage() {
			assertNotNull(new FixedKeysBooleanBitSetStorage());
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBooleanBitSetStorage#FixedKeysBooleanBitSetStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { UNSET_INT, 1, 10, 100, 10_000 })
		void testFixedKeysBooleanBitSetStorageInt(int capacity) {
			assertNotNull(new FixedKeysBooleanBitSetStorage(capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBooleanBitSetStorage#FixedKeysBooleanBitSetStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { 0, -2 })
		void testFixedKeysBooleanBitSetStorageIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT, () -> new FixedKeysBooleanBitSetStorage(capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBooleanBitSetStorage#FixedKeysBooleanBitSetStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { UNSET_INT, 1, 10, 100, 10_000 })
		void testFixedKeysBooleanBitSetStorageBooleanInt(int capacity) {
			assertNotNull(new FixedKeysBooleanBitSetStorage(true, capacity));
			assertNotNull(new FixedKeysBooleanBitSetStorage(false, capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBooleanBitSetStorage#FixedKeysBooleanBitSetStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { 0, -2 })
		void testFixedKeysBooleanBitSetStorageBooleanIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT, () -> new FixedKeysBooleanBitSetStorage(true, capacity));
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new FixedKeysBooleanBitSetStorage(false, capacity));
		}

	}

}
