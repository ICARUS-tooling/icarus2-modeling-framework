/**
 *
 */
package de.ims.icarus2.model.standard.members.layers.annotation.fixed;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
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
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
class FixedKeysFloatStorageTest implements MultiKeyAnnotationStorageTest<FixedKeysFloatStorage>,
		ManagedAnnotationStorageTest<FixedKeysFloatStorage> {

	/** Maps keys to noEntryValues */
	private Object2FloatMap<String> setup = new Object2FloatOpenHashMap<>();
	private List<String> keys = new ArrayList<>();

	@BeforeEach
	void setUp() {
		for (int i = 0; i < 20; i++) {
			String key = "key_" + i;
			setup.put(key, random().nextFloat());
			keys.add(key);
		}
	}

	@AfterEach
	void tearDown() {
		setup.clear();
		keys.clear();
	}

	@Override
	public Set<ValueType> typesForSetters(String key) {
		return set(ValueType.INTEGER, ValueType.LONG, ValueType.FLOAT, ValueType.DOUBLE);
	}

	@Override
	public Set<ValueType> typesForGetters(String key) {
		return set(ValueType.INTEGER, ValueType.LONG, ValueType.FLOAT, ValueType.DOUBLE);
	}

	@Override
	public Object testValue(String key) {
		float noEntryValue = setup.getFloat(key);
		float value;
		do {
			value = random().nextInt();
		} while (value == noEntryValue);
		return Float.valueOf(value);
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
	 * @see de.ims.icarus2.model.api.layer.AnnotationStorageTest#noEntryValue(java.lang.String)
	 */
	@Override
	public Object noEntryValue(String key) {
		return Float.valueOf(setup.getFloat(key));
	}

	@Override
	public ValueType valueType(String key) {
		return ValueType.FLOAT;
	}

	@Override
	public FixedKeysFloatStorage createForLayer(AnnotationLayer layer) {
		return new FixedKeysFloatStorage();
	}

	@Override
	public Class<? extends FixedKeysFloatStorage> getTestTargetClass() {
		return FixedKeysFloatStorage.class;
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
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysFloatStorage#FixedKeysFloatStorage()}.
		 */
		@Test
		void testFixedKeysFloatStorage() {
			assertNotNull(new FixedKeysFloatStorage());
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysFloatStorage#FixedKeysFloatStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { UNSET_INT, 1, 10, 100, 10_000 })
		void testFixedKeysFloatStorageInt(int capacity) {
			assertNotNull(new FixedKeysFloatStorage(capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysFloatStorage#FixedKeysFloatStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { 0, -2 })
		void testFixedKeysFloatStorageIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT, () -> new FixedKeysFloatStorage(capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysFloatStorage#FixedKeysFloatStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { UNSET_INT, 1, 10, 100, 10_000 })
		void testFixedKeysFloatStorageBooleanInt(int capacity) {
			assertNotNull(new FixedKeysFloatStorage(true, capacity));
			assertNotNull(new FixedKeysFloatStorage(false, capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysFloatStorage#FixedKeysFloatStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { 0, -2 })
		void testFixedKeysFloatStorageBooleanIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT, () -> new FixedKeysFloatStorage(true, capacity));
			assertModelException(GlobalErrorCode.INVALID_INPUT, () -> new FixedKeysFloatStorage(false, capacity));
		}

	}

}
