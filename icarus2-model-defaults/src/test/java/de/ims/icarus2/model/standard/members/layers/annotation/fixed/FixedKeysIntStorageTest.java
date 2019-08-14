/**
 *
 */
package de.ims.icarus2.model.standard.members.layers.annotation.fixed;

import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ManagedAnnotationStorageTest;
import de.ims.icarus2.model.api.layer.MultiKeyAnnotationStorageTest;
import de.ims.icarus2.model.manifest.types.ValueType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
class FixedKeysIntStorageTest implements MultiKeyAnnotationStorageTest<FixedKeysIntStorage>,
	ManagedAnnotationStorageTest<FixedKeysIntStorage> {

	/** Maps keys to noEntryValues */
	private Object2IntMap<String> setup = new Object2IntOpenHashMap<>();
	private List<String> keys = new ArrayList<>();

	@BeforeEach
	void setUp() {
		for (int i = 0; i < 20; i++) {
			String key = "key_"+i;
			setup.put(key, random().nextInt());
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
		return set(ValueType.INTEGER, ValueType.LONG);
	}

	@Override
	public Set<ValueType> typesForGetters(String key) {
		return set(ValueType.INTEGER, ValueType.LONG, ValueType.FLOAT, ValueType.DOUBLE);
	}

	@Override
	public Object testValue(String key) {
		int noEntryValue = setup.getInt(key);
		int value;
		do {
			value = random().nextInt();
		} while(value==noEntryValue);
		return Integer.valueOf(value);
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
		return Integer.valueOf(setup.getInt(key));
	}

	@Override
	public ValueType valueType(String key) {
		return ValueType.INTEGER;
	}

	@Override
	public FixedKeysIntStorage createForLayer(AnnotationLayer layer) {
		return new FixedKeysIntStorage();
	}

	@Override
	public Class<? extends FixedKeysIntStorage> getTestTargetClass() {
		return FixedKeysIntStorage.class;
	}

	@Override
	public List<String> keys() {
		return keys;
	}

	@Override
	public String key() {
		return keys.get(0);
	}

	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysIntStorage#FixedKeysIntStorage()}.
		 */
		@Test
		void testFixedKeysIntStorage() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysIntStorage#FixedKeysIntStorage(int)}.
		 */
		@Test
		void testFixedKeysIntStorageInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysIntStorage#FixedKeysIntStorage(boolean, int)}.
		 */
		@Test
		void testFixedKeysIntStorageBooleanInt() {
			fail("Not yet implemented"); // TODO
		}

	}

}
