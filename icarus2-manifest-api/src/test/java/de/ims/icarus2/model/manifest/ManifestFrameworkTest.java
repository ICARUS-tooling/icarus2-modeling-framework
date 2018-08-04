/**
 *
 */
package de.ims.icarus2.model.manifest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.util.function.ObjBoolConsumer;

/**
 * @author Markus Gärtner
 *
 */
public interface ManifestFrameworkTest<T extends Object> extends GenericTest<T> {

	public static <T extends Object, K extends Object> void assertSetter(T instance, BiConsumer<T, K> setter, K value,
			boolean checkNPE, @SuppressWarnings("unchecked") K...illegalValues) {
		if(checkNPE) {
			TestUtils.assertNPE(() -> setter.accept(instance, null));
		} else {
			setter.accept(instance, null);
		}

		setter.accept(instance, value);

		for(K illegalValue : illegalValues) {
			ManifestTestUtils.assertIllegalValue(() -> setter.accept(instance, illegalValue));
		}
	}

	public static <T extends Object> void assertSetter(T instance, ObjBoolConsumer<T> setter) {
		setter.accept(instance, true);
		setter.accept(instance, false);
	}

	public static <T extends Object, K extends Object> void assertAccumulativeAdd(
			T instance, BiConsumer<T, K> adder,
			K[] illegalValues, boolean checkNPE, boolean checkDuplicate, @SuppressWarnings("unchecked") K...values) {
		int valCount = values.length;
		assertTrue(valCount>2, "Insufficient test values - need more than 2");

		if(checkNPE) {
			TestUtils.assertNPE(() -> adder.accept(instance, null));
		}

		// Test with first (n-1)th values and keep last one for testing under lock
		for(int i=0; i<valCount-1; i++) {
			adder.accept(instance, values[i]);
		}

		if(checkDuplicate) {
			ManifestTestUtils.assertManifestException(GlobalErrorCode.INVALID_INPUT, () -> adder.accept(instance, values[0]));
		}

		if(illegalValues!=null) {
			for(K illegalValue : illegalValues) {
				ManifestTestUtils.assertIllegalValue(() -> adder.accept(instance, illegalValue));
			}
		}
	}

	public static <T extends Object, K extends Object, C extends Collection<K>> void assertAccumulativeRemove(
			T instance, BiConsumer<T, K> adder, BiConsumer<T, K> remover,
			Function<T, C> getter, boolean checkNPE, boolean checkInvalidRemove,
					@SuppressWarnings("unchecked") K...values) {
		int valCount = values.length;
		assertTrue(valCount>2, "Insufficient test values - need more than 2");

		for(K value : values) {
			adder.accept(instance, value);
		}

		TestUtils.assertNPE(() -> remover.accept(instance, null));

		TestUtils.assertCollectionEquals(getter.apply(instance), values);

		remover.accept(instance, values[0]);

		TestUtils.assertCollectionEquals(getter.apply(instance), Arrays.copyOfRange(values, 1, valCount));

		if(checkInvalidRemove) {
			ManifestTestUtils.assertManifestException(GlobalErrorCode.INVALID_INPUT,
					() -> remover.accept(instance, values[0]));
		}

		for(int i=1; i<valCount; i++) {
			remover.accept(instance, values[i]);
		}

		assertTrue(getter.apply(instance).isEmpty());


		adder.accept(instance, values[0]);
	}

	public static <T extends Object, K extends Object> void assertGetter(
			T instance, K value1, K value2, K defaultValue, Function<T,K> getter, BiConsumer<T, K> setter) {
		if(defaultValue==null) {
			assertNull(getter.apply(instance));
		} else {
			assertEquals(defaultValue, getter.apply(instance));
		}

		setter.accept(instance, value1);
		assertEquals(value1, getter.apply(instance));
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object, K extends Object> void assertAccumulativeGetter(
			T instance, K value1, K value2, Function<T,? extends Collection<K>> getter, BiConsumer<T, K> adder) {
		assertTrue(getter.apply(instance).isEmpty());

		adder.accept(instance, value1);
		TestUtils.assertCollectionEquals(getter.apply(instance), value1);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object, K extends Object> void assertAccumulativeLocalGetter(
			T instance, K value1, K value2, Function<T,? extends Collection<K>> getter, BiConsumer<T, K> adder) {
		assertTrue(getter.apply(instance).isEmpty());

		adder.accept(instance, value1);
		TestUtils.assertCollectionEquals(getter.apply(instance), value1);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object, K extends Object, A extends Consumer<? super K>> void assertForEach(
			T instance, K value1, K value2, Function<T,Consumer<A>> forEachGen, BiConsumer<T, K> adder) {

		TestUtils.assertForEachNPE(forEachGen.apply(instance));

		TestUtils.assertForEachEmpty(forEachGen.apply(instance));

		adder.accept(instance, value1);
		TestUtils.assertForEachUnsorted(forEachGen.apply(instance), value1);

		adder.accept(instance, value2);
		TestUtils.assertForEachUnsorted(forEachGen.apply(instance), value1, value2);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object, K extends Object, A extends Consumer<? super K>> void assertForEachLocal(
			T instance, K value1, K value2, Function<T,Consumer<A>> forEachLocalGen, BiConsumer<T, K> adder) {

		TestUtils.assertForEachNPE(forEachLocalGen.apply(instance));

		TestUtils.assertForEachEmpty(forEachLocalGen.apply(instance));

		adder.accept(instance, value1);
		TestUtils.assertForEachUnsorted(forEachLocalGen.apply(instance), value1);

		adder.accept(instance, value2);
		TestUtils.assertForEachUnsorted(forEachLocalGen.apply(instance), value1, value2);
	}

	/**
	 * @see #assertGetter(Class, Object, Object, Object, Function, BiConsumer)
	 *
	 * @param argType
	 * @param value1
	 * @param value2
	 * @param defaultValue
	 * @param getter
	 * @param setter
	 */
	public static <T extends Object, K extends Object> void assertIsLocal(
			T instance, K value1, K value2, Predicate<T> isLocalCheck, BiConsumer<T, K> setter) {
		assertFalse(isLocalCheck.test(instance));

		setter.accept(instance, value1);
		assertTrue(isLocalCheck.test(instance));
	}

	public static <T extends Object, K extends Object> void assertAccumulativeIsLocal(
			T instance, K value1, K value2, BiPredicate<T, K> isLocalCheck, BiConsumer<T, K> adder) {

		TestUtils.assertNPE(() -> isLocalCheck.test(instance, null));

		assertFalse(isLocalCheck.test(instance, value1));

		adder.accept(instance, value1);
		assertTrue(isLocalCheck.test(instance, value1));

		adder.accept(instance, value2);
		assertTrue(isLocalCheck.test(instance, value2));
	}

	@SuppressWarnings("boxing")
	public static <T extends Object> void assertFlagGetter(
			T instance, Boolean defaultValue, Predicate<T> getter, ObjBoolConsumer<T> setter) {
		if(defaultValue!=null) {
			assertEquals(defaultValue.booleanValue(), getter.test(instance));
		}

		setter.accept(instance, true);
		assertTrue(getter.test(instance));
		setter.accept(instance, false);
		assertFalse(getter.test(instance));
	}
}
