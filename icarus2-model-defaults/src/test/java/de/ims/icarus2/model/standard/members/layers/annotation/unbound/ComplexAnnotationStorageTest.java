/**
 *
 */
package de.ims.icarus2.model.standard.members.layers.annotation.unbound;

import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.randomString;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ManagedAnnotationStorageTest;
import de.ims.icarus2.model.api.layer.MultiKeyAnnotationStorageTest;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
class ComplexAnnotationStorageTest implements ManagedAnnotationStorageTest<ComplexAnnotationStorage>,
		MultiKeyAnnotationStorageTest<ComplexAnnotationStorage> {

	private Map<String, KeyConfig<?>> setup = new LinkedHashMap<>();
	private List<String> keys = new ArrayList<>();

	private static final ValueType[] _types = {
			ValueType.STRING,
			ValueType.INTEGER,
			ValueType.LONG,
			ValueType.DOUBLE,
			ValueType.FLOAT,
			ValueType.BOOLEAN,
			ValueType.CUSTOM,
	};

	@SuppressWarnings("rawtypes")
	private static final Supplier[] _gen = {
			() -> randomString(20),
			() -> Integer.valueOf(random().nextInt()),
			() -> Long.valueOf(random().nextLong()),
			() -> Double.valueOf(random().nextDouble()*Double.MAX_VALUE),
			() -> Float.valueOf(random().nextFloat()*Float.MAX_VALUE),
			() -> Boolean.valueOf(random().nextBoolean()),
			() -> new Object(),
	};

	private static String key(int index) {
		return "test_"+index;
	}

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() {
		for (int i = 0; i < 20; i++) {
			int idx = i<_types.length ? i : random(0, _types.length);
			String key = key(i);
			setup.put(key, new KeyConfig<>(_types[idx], _gen[idx]));
			keys.add(key);
		}
	}

	@AfterEach
	void tearDown() {
		keys.clear();
		setup.clear();
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationStorageTest#valueType(java.lang.String)
	 */
	@Override
	public ValueType valueType(String key) {
		return setup.get(key).valueType;
	}

	@Override
	public Object testValue(String key) {
		return setup.get(key).testValue();
	}

	@Override
	public Object noEntryValue(String key) {
		return setup.get(key).noEntryValue;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationStorageTest#keyForType(de.ims.icarus2.model.manifest.types.ValueType)
	 */
	@Override
	public String keyForType(ValueType type) {
		for (int i = 0; i < _types.length; i++) {
			if(_types[i]==type) {
				return key(i);
			}
		}
		throw new IllegalArgumentException("Unsupported type: "+type);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationStorageTest#key()
	 */
	@Override
	public String key() {
		return keys.get(0);
	}

	@Override
	public List<String> keys() {
		return keys;
	}

	@Override
	public Class<? extends ComplexAnnotationStorage> getTestTargetClass() {
		return ComplexAnnotationStorage.class;
	}

	@Override
	public Set<ValueType> typesForSetters(String key) {
		return setup.get(key).typesForSetters();
	}

	@Override
	public Set<ValueType> typesForGetters(String key) {
		return setup.get(key).typesForGetters();
	}

	@Override
	public ComplexAnnotationStorage createForLayer(AnnotationLayer layer) {
		return new ComplexAnnotationStorage();
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

	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationStorage#ComplexAnnotationStorage(java.util.function.Supplier)}.
		 */
		@Test
		void testComplexAnnotationStorageSupplierOfAnnotationBundle() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationStorage#ComplexAnnotationStorage(int, java.util.function.Supplier)}.
		 */
		@Test
		void testComplexAnnotationStorageIntSupplierOfAnnotationBundle() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationStorage#ComplexAnnotationStorage(boolean, int, java.util.function.Supplier)}.
		 */
		@Test
		void testComplexAnnotationStorageBooleanIntSupplierOfAnnotationBundle() {
			fail("Not yet implemented"); // TODO
		}

	}

	private static final class KeyConfig<T> {
		private final ValueType valueType;
		private final Supplier<T> generator;
		private final T noEntryValue;

		public KeyConfig(ValueType valueType, Supplier<T> generator) {
			this.valueType = requireNonNull(valueType);
			this.generator = requireNonNull(generator);

			this.noEntryValue = requireNonNull(generator.get());
		}

		T testValue() {
			T value;
			do {
				value = requireNonNull(generator.get());
			} while(Objects.equals(noEntryValue, value));
			return value;
		}

		Set<ValueType> typesForGetters() {
			if(valueType==ValueType.INTEGER || valueType==ValueType.LONG
					|| valueType==ValueType.FLOAT || valueType==ValueType.DOUBLE) {
				return set(ValueType.INTEGER, ValueType.LONG, ValueType.FLOAT, ValueType.DOUBLE);
			}

			return singleton(valueType);
		}

		Set<ValueType> typesForSetters() {
			if(valueType==ValueType.INTEGER || valueType==ValueType.LONG) {
				return set(ValueType.INTEGER, ValueType.LONG, ValueType.FLOAT, ValueType.DOUBLE);
//				return set(ValueType.INTEGER, ValueType.LONG);
			} else if(valueType==ValueType.FLOAT || valueType==ValueType.DOUBLE) {
				return set(ValueType.INTEGER, ValueType.LONG, ValueType.FLOAT, ValueType.DOUBLE);
			}

			if(valueType==ValueType.CUSTOM) {
				return set(ValueType.CUSTOM, ValueType.STRING);
			}

			return singleton(valueType);
		}
	}
}
