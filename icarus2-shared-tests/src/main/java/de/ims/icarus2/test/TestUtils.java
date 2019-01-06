/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.test;

import static de.ims.icarus2.test.util.Pair.pair;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import de.ims.icarus2.test.DiffUtils.Trace;
import de.ims.icarus2.test.func.TriConsumer;
import de.ims.icarus2.test.util.IdentitySet;
import de.ims.icarus2.test.util.Pair;

/**
 * Collection of useful testing methods.
 *
 * TODO: move shared testing code into a dedicated subproject (as main/java source) for proper dependency declarations
 *
 * @author Markus Gärtner
 *
 */
public class TestUtils {

	public static final String LOREM_IPSUM_ASCII =
			"0f73n 51m1l4r c0mm4ndz @R j00, 4r3 Wh0 4cc355 k0n"
    		+ "t@kt da. 0u7 (0py 3x4|\\/|3|\\|3d 1F, |7 No+ p@g3"
    		+ " 51m1l4r. 4|| p4g3, r3zUltz 45, y3r +o p4g3 tHUm8"
    		+ "41|_. 1T M155In9 4bund4n7 4r3, 1nf0 kvv3r33, 4v41"
    		+ "|4b|3 aLL @R. 0n d3n +H@T 7|24n5|473d, h@x 0f Wh3"
    		+ "n f34tUr3.";

	public static final String LOREM_IPSUM_ISO =
			"Lorem ipsum dolor sit amet, ex sit hinc choro err"
					  + "oribus, pericula intellegat mei ea. Has ea idqu"
					  + "e quaestio aliquando, sumo illum oratio te sit,"
					  + " te quot consequat elaboraret eam. Est cu natum"
					  + " accusamus patrioque. Eu ius quis ludus indoctu"
					  + "m.";

	public static final String LOREM_IPSUM_CHINESE =
			"王直妹晃惑生際験刺楽可海杯焼。国力梗印力者準結費用転豊歌傷下"
		    		+ "情密門食何。先工号録成司胃般都転国写。影高滞文勝参育仕新男過"
		    		+ "政天大談人元交。打持南本内客凶鳥文時愛崎師援注広。注記展覧走"
		    		+ "所女共勝店写提東育格摩致迎木。題管経辺思必時気軍提田帰国皇球"
		    		+ "北暮理。草権労球国球地国変億慶査造備快。触帝希及生生男国無始"
		    		+ "策中。";

	public static final String EMOJI = "👍"; // thumbs-up emoji

	private static final PrintStream out = null; //TODO need a facility to change this dynamically

	public static void print(String s) {
		if(out!=null)
			out.print(s);
	}

	/**
	 * Clone of the field in de.ims.icarus2.util.IcarusUtils
	 */
	public static final int MAX_INTEGER_INDEX = Integer.MAX_VALUE-8;

	@SuppressWarnings({ "boxing", "rawtypes" })
	private static final Pair[] displayLabels = {
			pair(Long.MIN_VALUE, "Long.MIN_VALUE"),
			pair(Integer.MIN_VALUE, "Integer.MIN_VALUE"),
			pair(Short.MIN_VALUE, "Short.MIN_VALUE"),
			pair(Byte.MIN_VALUE, "Byte.MIN_VALUE"),
			pair(Byte.MAX_VALUE, "Byte.MAX_VALUE"),
			pair(Short.MAX_VALUE, "Short.MAX_VALUE"),
			pair(Integer.MAX_VALUE/2, "Integer.MAX_VALUE/2"),
			pair(MAX_INTEGER_INDEX, "MAX_INDEX"),
			pair(Integer.MAX_VALUE, "Integer.MAX_VALUE"),
			pair(Long.MAX_VALUE/2, "Long.MAX_VALUE/2"),
			pair(Long.MAX_VALUE, "Long.MAX_VALUE"),
	};

	private static final int displayRange = 10;

	/**
	 * Utility method that produces a more human-readable string for a given number.
	 * It will use fixed numeric points such as {@link Integer#MAX_VALUE} to produce
	 * String for numbers around those points such as {@code "Integer.MAX_VALUE-4"}.
	 *
	 * @param value
	 * @return
	 */
	public static String displayString(long value) {
		for(@SuppressWarnings("rawtypes") Pair entry : displayLabels) {
			long center = ((Number)entry.first).longValue();
			String label = (String)entry.second;

			if(value==center) {
				return label;
			} else if(value < center && value >= center-displayRange) {
				return label+"-"+(center-value);
			} else if(value > center && value <= center+displayRange) {
				return label+"+"+(value-center);
			}
		}

		return String.valueOf(value);
	}

	public static String displayString(String pattern, Object...args) {
		for(int i=0; i<args.length; i++) {
			Object arg = args[i];
			if(Number.class.isInstance(arg)) { //TODO potential issue: floating-point numbers might get distorted?
				args[i] = displayString(((Number)arg).longValue());
			} else {
				args[i] = String.valueOf(arg);
			}
		}

		return String.format(pattern, args);
	}

	public static void println() {
		if(out!=null)
			out.println();
	}

	public static void println(String s) {
		if(out!=null)
			out.println(s);
	}

	public static Random random() {
		return new Random(1L);
	}

	public static <T extends Object> Supplier<T> randomizer(Collection<? extends T> source) {
		requireNonNull(source);
		if(source.size()==1) {
			final T singleton = source.iterator().next();
			return () -> singleton;
		} else {
			Randomizer<T> randomizer = Randomizer.from(source);
			return randomizer::randomize;
		}
	}

	public static <T extends Object> T random(@SuppressWarnings("unchecked") T...source) {
		requireNonNull(source);
		return source[random().nextInt(source.length)];
	}

	public static <T extends Object> T random(List<? extends T> source) {
		requireNonNull(source);
		return source.get(random().nextInt(source.size()));
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object> T random(Collection<? extends T> source) {
		requireNonNull(source);
		return (T) random(source.toArray());
	}

	public static TestSettings settings() {
		return new TestSettings();
	}

	public static TestSettings settings(TestFeature...features) {
		return settings().features(features);
	}

	public static boolean isMock(Object obj) {
		return obj!=null && Mockito.mockingDetails(obj).isMock();
	}

	public static <T extends Object> T assertMock(T mock) {
		assertNotNull(mock, "Mock is null");
		assertTrue(isMock(mock), "Given object is not a mock: "+mock);
		return mock;
	}

	public static <T extends Object> T assertMock(Optional<T> mock) {
		return assertMock(mock.orElse(null));
	}

	public static void assertNPE(Executable executable) {
		assertThrows(NullPointerException.class, executable);
	}

	public static <T extends Object> void assertPairwiseNotEquals(
			@SuppressWarnings("unchecked") T...items) {
		assertNotNull(items);
		assertTrue(items.length>1);

		for(int i=0; i<items.length-1; i++) {
			for(int j=i+1;j<items.length; j++) {
				assertNotEquals(items[i], items[j]);
			}
		}
	}

	public static <T extends Object> void assertPairwiseNotEquals(
			BiPredicate<? super T, ? super T> equals, @SuppressWarnings("unchecked") T...items) {
		assertNotNull(equals);
		assertNotNull(items);
		assertTrue(items.length>1);

		for(int i=0; i<items.length-1; i++) {
			for(int j=i+1;j<items.length; j++) {
				assertFalse(equals.test(items[i], items[j]));
			}
		}
	}

	public static <V extends Object> void assertPresent(Optional<V> value) {
		assertNotNull(value);
		assertTrue(value.isPresent());
	}

	public static <V> void assertPresent(Optional<V> value, String msg) {
		assertNotNull(value, msg);
		assertTrue(value.isPresent(), msg);
	}

	public static void assertNotPresent(Optional<?> value) {
		assertNotNull(value);
		assertFalse(value.isPresent());
	}

	public static <V extends Object> void assertNotPresent(Optional<V> value, String msg) {
		assertNotNull(value, msg);
		assertFalse(value.isPresent(), msg);
	}

	public static <V extends Object> void assertOptionalEquals(V expected, Optional<?> actual) {
		assertNotNull(actual);
		assertEquals(expected, actual.orElse(null));
	}

	public static <V extends Object> void assertOptionalEquals(V expected, Optional<V> actual, String msg) {
		assertNotNull(actual, msg);
		assertEquals(expected, actual.orElse(null), msg);
	}

	public static <T extends Object, V extends Object> Function<T, V> unwrapGetter(Function<T, Optional<V>> getter) {
		return obj -> getter.apply(obj)
				.orElse(null);
	}

	public static <T extends Object, K extends Object, V extends Object>
			BiFunction<T, K, V> unwrapLookup(BiFunction<T, K, Optional<V>> lookup) {
		return (obj, key) -> lookup.apply(obj, key)
				.orElse(null);
	}

	/**
	 * Assert that a method implementing the {@code for-each loop} provides
	 * a set of previously registered objects in a specified order.
	 *
	 * @param loop
	 * @param expected
	 */
	@SuppressWarnings("unchecked")
	public static <K extends Object, A extends Consumer<? super K>> void assertForEachSorted(Consumer<A> loop, K...expected) {
		List<K> actual = new ArrayList<>();

		Consumer<K> action = actual::add;

		loop.accept((A)action);

		assertEquals(expected.length, actual.size());

		for(int i=0; i<expected.length; i++) {
			assertEquals(expected[i], actual.get(i), "Mismatch at index "+i);
		}
	}

	/**
	 * Assert that a method implementing the {@code for-each loop} provides
	 * a set of previously registered objects in no specific order.
	 *
	 * @param loop
	 * @param expected
	 */
	@SuppressWarnings("unchecked")
	public static <K extends Object, A extends Consumer<? super K>> void assertForEachUnsorted(Consumer<A> loop, K...expected) {
		IdentitySet<K> actual = new IdentitySet<>();

		Consumer<K> action = actual::add;

		loop.accept((A)action);

		assertEquals(expected.length, actual.size());

		for(int i=0; i<expected.length; i++) {
			assertTrue(actual.contains(expected[i]));
		}
	}

	/**
	 * Assert that a method implementing the {@code for-each loop} provides
	 * a total of {@code 0} objects.
	 *
	 * @param loop
	 * @param expected
	 */
	@SuppressWarnings("unchecked")
	public static <K extends Object, A extends Consumer<? super K>> void assertForEachEmpty(Consumer<A> loop) {
		Set<K> actual = new HashSet<>();

		Consumer<K> action = actual::add;

		loop.accept((A)action);

		assertTrue(actual.isEmpty());
	}

	public static <K extends Object, A extends Consumer<? super K>> void assertForEachNPE(Consumer<A> loop) {
		assertNPE(() -> loop.accept(null));
	}

	@SuppressWarnings("unchecked")
	public static <K extends Object, A extends Predicate<? super K>> void assertForEachUntilSentinel(
			Consumer<A> loop, K sentinel, int expectedCalls) {
		AtomicInteger count = new AtomicInteger(-1);
		AtomicBoolean sentinelFound = new AtomicBoolean(false);
		Predicate<? super K> pred = v -> {
			count.incrementAndGet();
			boolean found = sentinel.equals(v);
			sentinelFound.compareAndSet(false, found);
			return found;
		};

		loop.accept((A)pred);

		if(sentinelFound.get()) {
			assertEquals(expectedCalls, count.intValue());
		} else {
			assertEquals(expectedCalls, -1);
		}
	}

	@SuppressWarnings("unchecked")
	public static <K extends Object, A extends Predicate<? super K>> void assertForEachUntilPred(
			Consumer<A> loop, A pred, int expectedCalls) {
		LongAdder count = new LongAdder();
		Predicate<? super K> sum = v -> {
			count.increment();
			return pred.test(v);
		};

		loop.accept((A)sum);

		assertEquals(expectedCalls, count.intValue());
	}

	public static <E extends Object> void assertCollectionEquals(
			Collection<? extends E> expected, Collection<? extends E> actual) {
		assertEquals(expected.size(), actual.size());

		for(E element : expected) {
			assertTrue(actual.contains(element), "Missing element: "+element);
		}
	}

	@SuppressWarnings("unchecked")
	public static <E extends Object> void assertCollectionEquals(Collection<? extends E> actual, E...expected) {
		assertEquals(expected.length, actual.size(), "collection size mismatch");

		for(E element : expected) {
			assertTrue(actual.contains(element), "Missing element: "+element);
		}
	}

	public static <E extends Object> void assertCollectionEmpty(Collection<? extends E> col) {
		assertNotNull(col);
		assertTrue(col.isEmpty());
	}

	public static <E extends Object> void assertCollectionNotEmpty(Collection<? extends E> col) {
		assertNotNull(col);
		assertFalse(col.isEmpty());
	}

	public static <E extends Object> void assertListEquals(
			List<? extends E> expected, List<? extends E> actual) {
		assertEquals(expected.size(), actual.size());

		for(int i=0; i<expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i),
					"Mismatch at index "+i+": expected "+expected.get(i)+" - got "+actual.get(i));
		}
	}

	public static <E extends Object> void assertListEquals(
			List<? extends E> actual, @SuppressWarnings("unchecked") E...expected) {
		assertEquals(expected.length, actual.size());

		for(int i=0; i<expected.length; i++) {
			assertEquals(expected[i], actual.get(i),
					"Mismatch at index "+i+": expected "+expected[i]+" - got "+actual.get(i));
		}
	}

	public static <E extends Object> void assertArrayEmpty(E[] array) {
		assertNotNull(array);
		assertTrue(array.length==0);
	}

	public static <E extends Object> void assertArrayNotEmpty(E[] array) {
		assertNotNull(array);
		assertTrue(array.length>0);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> E other(E value) {
		Enum<E>[] values = value.getDeclaringClass().getEnumConstants();
		assertTrue(values.length>0);
		int pos = Arrays.binarySearch(values, value);
		assertTrue(pos>=0);

		pos++;
		if(pos>=values.length) {
			pos = 0;
		}

		return (E) values[pos];
	}

	private static <E extends Object> E[] filterUniques(E[] data) {
		if(data.length<2) {
			return data;
		}

		Set<E> buffer = new HashSet<>();
		Collections.addAll(buffer, data);

		if(buffer.size()<data.length) {
			data = Arrays.copyOf(data, buffer.size());
			buffer.toArray(data);
		}

		return data;
	}

    private static boolean isEquals(Object expected, Object actual) {
        return expected.equals(actual);
    }

	public static void assertHashContract(Object expected, Object actual) {
		assertHashContract(null, expected, actual);
	}

	private static final Object dummy = new Dummy();

	public static void assertObjectContract(Object obj) {
		assertFalse(obj.equals(null));
		assertFalse(obj.equals(dummy));
		assertNotNull(obj.toString());
	}

	public static void assertHashContract(String message, Object expected, Object actual) {
		assertNotNull(expected, "Expected"); //$NON-NLS-1$
		assertNotNull(actual, "Actual"); //$NON-NLS-1$

		int expectedHash = expected.hashCode();
		int actualHash = actual.hashCode();

		boolean isEquals = isEquals(expected, actual);
		boolean isHashEquals = expectedHash==actualHash;

		if(isEquals && !isHashEquals) {
			failForEquality(message, expectedHash, actualHash);
		} else if(!isEquals && isHashEquals) {
			failForInequality(message, expectedHash);
		}
	}

	private static void failForEquality(String message, int expectedHash, int actualHash) {
        String formatted = ""; //$NON-NLS-1$
        if (message != null) {
            formatted = message + " "; //$NON-NLS-1$
        }

        fail(formatted+"expected hash "+expectedHash+" for two equal objects, but got: "+actualHash); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static void failForInequality(String message, int hash) {
        String formatted = ""; //$NON-NLS-1$
        if (message != null) {
            formatted = message + " "; //$NON-NLS-1$
        }

        fail(formatted+"expected hash different to "+hash+" for two inequal objects"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void assertDeepEqual(String msg, Object expected, Object actual, String serializedForm) throws IllegalAccessException {

		// Collect all the differences
		Trace trace = DiffUtils.deepDiff(expected, actual);

		if(trace.hasMessages()) {
			failForTrace(msg, trace, expected, serializedForm);
		}
	}

	public static void assertDeepNotEqual(String msg, Object expected, Object actual) throws IllegalAccessException {

		// Collect all the differences
		Trace trace = DiffUtils.deepDiff(expected, actual);

		if(!trace.hasMessages()) {
			failForEqual(msg, expected, actual);
		}
	}

	private static String getId(Object obj) {
		String id = null;

		try {
			Method method = obj.getClass().getMethod("getName");
			id = (String) method.invoke(obj);
		} catch(NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// ignore
		}

		if(id==null) {
			id = "<unnamed>";
		}

		return obj.getClass()+"@["+id+"]";
	}


	private static void failForEqual(String msg, Object original, Object created) {
		String message = "Expected result of deserialization to be different from original"; //$NON-NLS-1$
		if(msg!=null) {
			message = msg+": "+message; //$NON-NLS-1$
		}
		fail(message);
	}

	private static void failForTrace(String msg, Trace trace, Object obj, String xml) {
		String message = getId(obj);

		message += " result of deserialization is different from original: \n"; //$NON-NLS-1$
		message += trace.getMessages();
		message += " {serialized form: "+xml.replaceAll("\\s{2,}", "")+"}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		if(msg!=null) {
			message = msg+": "+message; //$NON-NLS-1$
		}

		fail(message);
	}

	private static class Dummy {
		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Dummy@"+hashCode(); //$NON-NLS-1$
		}
	}

	/**
	 * Helper method usable as legality checker for test routines.
	 * All the {@code assertXXX} methods in this class which take
	 * callbacks for legality checks will use this constant to
	 * decide whether a given non-null checker should actually be
	 * used.
	 * <p>
	 * Note that this checker will {@link Assertions#fail(String) fail}
	 * a test when actually called!
	 */
	public static final BiConsumer<Executable, String> NO_CHECK = (e, msg) -> fail("Not meant to have legality check called");

	public static final BiConsumer<Executable, String> ILLEGAL_ARGUMENT_CHECK = (e, msg) -> {
		assertThrows(IllegalArgumentException.class, e, msg);
	};

	public static final BiConsumer<Executable, String> ILLEGAL_STATE_CHECK = (e, msg) -> {
		assertThrows(IllegalStateException.class, e, msg);
	};

	public static final BiConsumer<Executable, String> INDEX_OUT_OF_BOUNDS_CHECK = (e, msg) -> {
		assertThrows(IndexOutOfBoundsException.class, e, msg);
	};

	/**
	 * Constant to indicate that the test routine should perform
	 * a setter check using a {@code null} value and expect a
	 * {@link NullPointerException} to be thrown.
	 */
	public static final boolean NPE_CHECK = true;

	/**
	 * Inverse of {@link #NPE_CHECK}
	 */
	public static final boolean NO_NPE_CHECK = false;

	private static final Supplier<?> NO_DEFAULT = () -> null;

	@SuppressWarnings("unchecked")
	public static <K extends Object> Supplier<K> NO_DEFAULT() {
		return (Supplier<K>) NO_DEFAULT;
	}

	private static final Supplier<?> IGNORE_DEFAULT = () -> fail("Not supposed to request a default value!");

	@SuppressWarnings("unchecked")
	public static <K extends Object> Supplier<K> IGNORE_DEFAULT() {
		return (Supplier<K>) IGNORE_DEFAULT;
	}

	public static <K extends Object> Supplier<K> DEFAULT(K value) {
		return () -> value;
	}

	public static <K extends Object> K[] NO_ILLEGAL() {
		return (K[]) null;
	}

	public static <K extends Object> Function<K, K> IDENTITY() {
		return k -> k;
	}

	private static Object null_obj = null;

	@SuppressWarnings("unchecked")
	public static <T extends Object> T NO_VALUE() {
		return (T) null_obj;
	}

	public static String forValue(String msg, Object value) {
		return msg + " - " + String.valueOf(value);
	}

	public static <T extends Object> Consumer<T> DO_NOTHING() {
		return obj -> {
			// no-op
		};
	}

	//-------------------------------------------
	// SETTER ASSERTIONS
	//-------------------------------------------

	public static <T extends Object, K extends Object> void assertSetter(T instance, BiConsumer<T, K> setter, K value,
			boolean checkNPE, BiConsumer<Executable, String> legalityCheck, @SuppressWarnings("unchecked") K...illegalValues) {
		if(checkNPE) {
			assertNPE(() -> setter.accept(instance, null));
		} else {
			setter.accept(instance, null);
		}

		setter.accept(instance, value);

		for(K illegalValue : illegalValues) {
			legalityCheck.accept(() -> setter.accept(instance, illegalValue),
					forValue("Testing illegal value", illegalValue));
		}
	}

	/**
	 * Asserts a getter method in a way very similar to {@link #assertGetter(Object, Object, Object, Supplier, Function, BiConsumer)},
	 * but with the restriction that after one call to the {@code setter} method an exception is
	 * expected, asserted by the given {@code restrictionCheck} handler.
	 *
	 * @param instance
	 * @param setter
	 * @param value
	 * @param checkNPE
	 * @param legalityCheck
	 * @param illegalValues
	 *
	 * @see #assertGetter(Object, Object, Object, Supplier, Function, BiConsumer)
	 * @see #ILLEGAL_STATE_CHECK
	 */
	public static <T extends Object, K extends Object> void assertRestrictedSetter(
			T instance, BiConsumer<T, K> setter,
			K value1, K value2,
			boolean checkNPE,
			BiConsumer<Executable, String> restrictionCheck) {
		if(checkNPE) {
			assertNPE(() -> setter.accept(instance, null));
		} else {
			setter.accept(instance, null);
		}

		setter.accept(instance, value1);

		restrictionCheck.accept(() -> setter.accept(instance, value2),
					forValue("Testing restricted setter call", value2));
	}

	public static <T extends Object, K extends Object> void assertSetterBatch(T instance, BiConsumer<T, K> setter, K[] values,
			boolean checkNPE, BiConsumer<Executable, String> legalityCheck, @SuppressWarnings("unchecked") K...illegalValues) {
		if(checkNPE) {
			assertNPE(() -> setter.accept(instance, null));
		} else {
			setter.accept(instance, null);
		}

		for(K value : values) {
			setter.accept(instance, value);
		}

		for(K illegalValue : illegalValues) {
			legalityCheck.accept(() -> setter.accept(instance, illegalValue),
					forValue("Testing illegal value", illegalValue));
		}
	}

	public static <T extends Object> void assertSetter(T instance, BiConsumer<T, Boolean> setter) {
		setter.accept(instance, Boolean.TRUE);
		setter.accept(instance, Boolean.FALSE);
	}

	public static <T extends Object, K extends Object> void assertAccumulativeAdd(
			T instance, BiConsumer<T, K> adder,
			K[] illegalValues, BiConsumer<Executable, String> legalityCheck, boolean checkNPE,
			BiConsumer<Executable, String> duplicateCheck, @SuppressWarnings("unchecked") K...values) {

		values = filterUniques(values);

		if(checkNPE) {
			assertNPE(() -> adder.accept(instance, null));
		}

		for(int i=0; i<values.length; i++) {
			adder.accept(instance, values[i]);
		}

		K first = values[0];

		if(duplicateCheck!=NO_CHECK) {
			duplicateCheck.accept(() -> adder.accept(instance, first),
					forValue("Testing duplicate value", first));
		}

		if(legalityCheck!=NO_CHECK && illegalValues!=null) {
			for(K illegalValue : illegalValues) {
				legalityCheck.accept(() -> adder.accept(instance, illegalValue),
						forValue("Testing illegal value", illegalValue));
			}
		}
	}

	public static <T extends Object, K extends Object, C extends Collection<K>> void assertAccumulativeRemove(
			T instance, BiConsumer<T, K> adder, BiConsumer<T, K> remover,
			Function<T, C> getter, boolean checkNPE,
			BiConsumer<Executable, String> invalidRemoveCheck, @SuppressWarnings("unchecked") K...values) {

		values = filterUniques(values);

		for(K value : values) {
			adder.accept(instance, value);
		}

		if(checkNPE) {
			assertNPE(() -> remover.accept(instance, null));
		}

		assertCollectionEquals(getter.apply(instance), values);

		remover.accept(instance, values[0]);

		assertCollectionEquals(getter.apply(instance), Arrays.copyOfRange(values, 1, values.length));

		K first = values[0];

		if(invalidRemoveCheck!=NO_CHECK) {
			invalidRemoveCheck.accept(() -> remover.accept(instance, first),
					forValue("Testing invalid remove value", first));
		}

		for(int i=1; i<values.length; i++) {
			remover.accept(instance, values[i]);
		}

		assertTrue(getter.apply(instance).isEmpty());


		adder.accept(instance, values[0]);
	}

	//-------------------------------------------
	// GETTER ASSERTIONS
	//-------------------------------------------

	public static <T extends Object, K extends Object> void assertGetter(
			T instance, K value1, K value2, Supplier<? extends K> defaultValue,
			Function<T,K> getter, BiConsumer<T, K> setter) {
		if(defaultValue==null || defaultValue==NO_DEFAULT()) {
			assertNull(getter.apply(instance));
		} else if(defaultValue!=IGNORE_DEFAULT()) {
			assertEquals(defaultValue.get(), getter.apply(instance));
		}

		setter.accept(instance, value1);
		assertEquals(value1, getter.apply(instance));

		if(value2!=null) {
			setter.accept(instance, value2);
			assertEquals(value2, getter.apply(instance));
		}
	}

	public static <T extends Object, K extends Object> void assertRestrictedGetter(
			T instance, K value1,
			BiConsumer<Executable, String> restrictionCheck,
			Function<T,K> getter, BiConsumer<T, K> setter) {

		restrictionCheck.accept(() -> getter.apply(instance),
					"Testing restricted getter call on empty instance");

		setter.accept(instance, value1);
		assertEquals(value1, getter.apply(instance));
	}

	public static <T extends Object, K extends Object> void assertOptGetter(
			T instance, K value1, K value2, Supplier<? extends K> defaultValue,
			Function<T,Optional<K>> getter, BiConsumer<T, K> setter) {
		if(defaultValue==null || defaultValue==NO_DEFAULT()) {
			assertNotPresent(getter.apply(instance));
		} else if(defaultValue!=IGNORE_DEFAULT()) {
			assertOptionalEquals(defaultValue.get(), getter.apply(instance));
		}

		setter.accept(instance, value1);
		assertOptionalEquals(value1, getter.apply(instance));

		if(value2!=null) {
			setter.accept(instance, value2);
			assertOptionalEquals(value2, getter.apply(instance));
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object, K extends Object> void assertAccumulativeGetter(
			T instance, K value1, K value2, Function<T,? extends Collection<? extends K>> getter, BiConsumer<T, K> adder) {
		assertTrue(getter.apply(instance).isEmpty());

		adder.accept(instance, value1);
		assertCollectionEquals(getter.apply(instance), value1);

		adder.accept(instance, value2);
		assertCollectionEquals(getter.apply(instance), value1, value2);
	}

	public static <T extends Object, K extends Object> void assertAccumulativeArrayGetter(
			T instance, K value1, K value2, Function<T, K[]> getter, BiConsumer<T, K> adder) {
		assertArrayEmpty(getter.apply(instance));

		adder.accept(instance, value1);
		assertArrayEquals(new Object[] {value1}, getter.apply(instance));

		adder.accept(instance, value2);
		assertArrayEquals(new Object[] {value1, value2}, getter.apply(instance));
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object, K extends Object> void assertAccumulativeFlagGetter(
			T instance, BiPredicate<T,K> getter, BiConsumer<T, K> adder, BiConsumer<T, K> remover, K...values) {

		values = filterUniques(values);

		for(K value : values) {
			assertFalse(getter.test(instance, value));

			adder.accept(instance, value);
			assertTrue(getter.test(instance, value));
		}

		for(K value : values) {
			assertTrue(getter.test(instance, value));

			remover.accept(instance, value);
			assertFalse(getter.test(instance, value));
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object, K extends Object> void assertAccumulativeLocalGetter(
			T instance, K value1, K value2, Function<T,? extends Collection<K>> getter, BiConsumer<T, K> adder) {
		assertTrue(getter.apply(instance).isEmpty());

		adder.accept(instance, value1);
		assertCollectionEquals(getter.apply(instance), value1);
	}

	public static <T extends Object, K extends Object, I extends Object> void assertAccumulativeLookup(
			T instance, K value1, K value2, BiFunction<T, I, K> lookup,
			boolean checkNPE, BiConsumer<Executable, String> invalidLookupCheck,
			BiConsumer<T, K> adder, Function<K, I> keyGen, @SuppressWarnings("unchecked") I...invalidLookups) {

		if(checkNPE) {
			assertNPE(() -> lookup.apply(instance, null));
		} else {
			lookup.apply(instance, null);
		}

		if(invalidLookupCheck!=NO_CHECK && invalidLookups.length>0) {
			for(I invalidLookup : invalidLookups) {
				invalidLookupCheck.accept(() -> lookup.apply(instance, invalidLookup),
						forValue("Testing invalid lookup value", invalidLookup));
			}
		}

		adder.accept(instance, value1);
		assertEquals(value1, lookup.apply(instance, keyGen.apply(value1)));

		adder.accept(instance, value2);
		assertEquals(value2, lookup.apply(instance, keyGen.apply(value2)));
	}

	/**
	 *
	 * @param instance
	 * @param value1
	 * @param value2
	 * @param lookup
	 * @param checkNPE
	 * @param adder
	 * @param keyGen
	 * @param invalidLookups
	 *
	 * @param <T> type of object under test
	 * @param <K> type of values added to object
	 * @param <I> type of intermediary key representatio nfor lookup
	 */
	public static <T extends Object, K extends Object, I extends Object> void assertAccumulativeOptLookup(
			T instance, K value1, K value2, BiFunction<T, I, Optional<K>> lookup,
			boolean checkNPE,
			BiConsumer<T, K> adder, Function<K, I> keyGen, @SuppressWarnings("unchecked") I...invalidLookups) {

		if(checkNPE) {
			assertNPE(() -> lookup.apply(instance, null));
		} else {
			assertNotPresent(lookup.apply(instance, null));
		}

		if(invalidLookups.length>0) {
			for(I invalidLookup : invalidLookups) {
				assertNotPresent(lookup.apply(instance, invalidLookup),
						forValue("Testing invalid lookup value", invalidLookup));
			}
		}

		adder.accept(instance, value1);
		assertOptionalEquals(value1, lookup.apply(instance, keyGen.apply(value1)));

		adder.accept(instance, value2);
		assertOptionalEquals(value2, lookup.apply(instance, keyGen.apply(value2)));
	}

	public static <T extends Object, K extends Object, I extends Object> void assertAccumulativeLookupContains(
			T instance, K value1, K value2, BiPredicate<T, I> check,
			boolean checkNPE, BiConsumer<T, K> adder, Function<K, I> keyGen) {

		if(checkNPE) {
			assertNPE(() -> check.test(instance, null));
		} else {
			check.test(instance, null);
		}

		adder.accept(instance, value1);
		assertTrue(check.test(instance, keyGen.apply(value1)));
		assertFalse(check.test(instance, keyGen.apply(value2)));

		adder.accept(instance, value2);
		assertTrue(check.test(instance, keyGen.apply(value2)));
	}

	@SafeVarargs
	public static <T extends Object, K extends Object> void assertAccumulativeCount(
			T instance,
			BiConsumer<T, K> adder, BiConsumer<T, K> remover,
			Function<T, Integer> counter, K...values) {

		values = filterUniques(values);

		assertTrue(values.length>1, "Needs at least 2 test values for add/remove");

		assertEquals(0, counter.apply(instance).intValue());

		int size = 0;

		// Incremental add
		for(K value : values) {
			adder.accept(instance, value);
			size++;
			assertEquals(size, counter.apply(instance).intValue());
		}

		// Incremental remove
		for(K value : values) {
			remover.accept(instance, value);
			size--;
			assertEquals(size, counter.apply(instance).intValue());
		}
	}

	public static <T extends Object, K extends Object> void assertAccumulativeFilter(
			T instance,
			BiConsumer<T, K> adder, BiFunction<T, Predicate<? super K>,? extends Collection<K>> getter,
			Predicate<? super K> filter,
			@SuppressWarnings("unchecked") K...values) {

		values = filterUniques(values);

		assertTrue(values.length>1, "Needs at least 2 test values for add/remove");

		List<K> expectedValues = new ArrayList<>();

		// Incremental add
		for(K value : values) {
			adder.accept(instance, value);
			if(filter.test(value)) {
				expectedValues.add(value);
			}
		}

		Collection<K> filtered = getter.apply(instance, filter);

		assertCollectionEquals(expectedValues, filtered);
	}

	public static <T extends Object, K extends Object> Consumer<Consumer<? super K>> wrapForEach(
			T instance, BiConsumer<T,Consumer<? super K>> forEach) {
		return action -> forEach.accept(instance, action);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object, K extends Object> void assertForEach(
			T instance, K value1, K value2, BiConsumer<T,Consumer<? super K>> forEach, BiConsumer<T, K> adder) {

		Consumer<Consumer<? super K>> loop = wrapForEach(instance, forEach);

		assertForEachNPE(loop);

		assertForEachEmpty(loop);

		adder.accept(instance, value1);
		assertForEachUnsorted(loop, value1);

		adder.accept(instance, value2);
		assertForEachUnsorted(loop, value1, value2);
	}

	public static <T extends Object, K extends Object> Consumer<Predicate<? super K>> wrapForEachUntil(
			T instance, BiConsumer<T,Predicate<? super K>> forEachUntil) {
		return action -> forEachUntil.accept(instance, action);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object, K extends Object> void assertForEachUntil(
			T instance, BiConsumer<T, Predicate<? super K>> forEachUntil,
			BiConsumer<T, K> adder, K... values) {

		values = filterUniques(values);

		assertNPE(() -> forEachUntil.accept(instance, null));

		// Verify that none of our values can be found
		for(int i=0; i<values.length; i++) {
			assertForEachUntilSentinel(wrapForEachUntil(instance, forEachUntil), values[i], -1);
		}

		// Fill target
		for(K value : values) {
			adder.accept(instance, value);
		}

		// Now do the real lookups
		for(int i=0; i<values.length; i++) {
			assertForEachUntilSentinel(wrapForEachUntil(instance, forEachUntil), values[i], i);
		}

		//TODO do we want to also cover the case of a foreign value not being found?
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object, K extends Object> void assertForEachLocal(
			T instance, K value1, K value2, BiConsumer<T,Consumer<? super K>> forEachLocal, BiConsumer<T, K> adder) {

		Consumer<Consumer<? super K>> loop = wrapForEach(instance, forEachLocal);

		assertForEachNPE(loop);

		assertForEachEmpty(loop);

		adder.accept(instance, value1);
		assertForEachUnsorted(loop, value1);

		adder.accept(instance, value2);
		assertForEachUnsorted(loop, value1, value2);
	}

	/**
	 * @see #assertGetter(Class, Object, Object, Object, Function, BiConsumer)
	 *
	 */
	public static <T extends Object, K extends Object> void assertIsLocal(
			T instance, K value1, K value2, Predicate<T> isLocalCheck, BiConsumer<T, K> setter) {
		assertFalse(isLocalCheck.test(instance));

		setter.accept(instance, value1);
		assertTrue(isLocalCheck.test(instance));
	}

	public static <T extends Object, K extends Object> void assertAccumulativeIsLocal(
			T instance, K value1, K value2, BiPredicate<T, K> isLocalCheck, BiConsumer<T, K> adder) {

		assertNPE(() -> isLocalCheck.test(instance, null));

		assertFalse(isLocalCheck.test(instance, value1));

		adder.accept(instance, value1);
		assertTrue(isLocalCheck.test(instance, value1));

		adder.accept(instance, value2);
		assertTrue(isLocalCheck.test(instance, value2));
	}

	public static <T extends Object, K extends Object> void assertAccumulativeHasLocal(
			T instance, K value1, K value2, Predicate<T> isLocalCheck, BiConsumer<T, K> adder) {

		assertFalse(isLocalCheck.test(instance));

		adder.accept(instance, value1);
		assertTrue(isLocalCheck.test(instance));

		adder.accept(instance, value2);
		assertTrue(isLocalCheck.test(instance));
	}

	public static <T extends Object> void assertFlagGetter(
			T instance, Boolean defaultValue, Predicate<T> getter, BiConsumer<T, Boolean> setter) {
		if(defaultValue!=null) {
			assertEquals(defaultValue, Boolean.valueOf(getter.test(instance)));
		}

		setter.accept(instance, Boolean.TRUE);
		assertTrue(getter.test(instance));
		setter.accept(instance, Boolean.FALSE);
		assertFalse(getter.test(instance));
	}

	public static <T extends Object, K extends Object> void assertListSize(
			T instance, BiConsumer<T, K> adder, BiConsumer<T, K> remover,
			Function<T, Integer> counter, @SuppressWarnings("unchecked") K...values) {
		assertAccumulativeCount(instance, adder, remover, counter, values);
	}

	public static <T extends Object, K extends Object> void assertListIndexOf(
			T instance, BiConsumer<T, K> adder, BiConsumer<T, K> remover,
			BiFunction<T, K, Integer> indexOf, @SuppressWarnings("unchecked") K...values) {

		values = filterUniques(values);

		assertTrue(values.length>1, "Needs at least 2 test values for add/remove");

		assertEquals(-1, indexOf.apply(instance, values[0]).intValue());

		List<K> buffer = new ArrayList<>(values.length);

		// Incremental add
		for(K value : values) {
			adder.accept(instance, value);
			assertEquals(buffer.size(), indexOf.apply(instance, value).intValue());
			buffer.add(value);
		}

		Random r = random();

		while(!buffer.isEmpty()) {
			int index = r.nextInt(buffer.size());
			K value = buffer.remove(index);
			assertEquals(index, indexOf.apply(instance, value).intValue());
			remover.accept(instance, value);
			assertEquals(-1, indexOf.apply(instance, value).intValue());
		}
	}

	@SuppressWarnings("boxing")
	public static <T extends Object, K extends Object> void assertListAtIndex(
			T instance, BiConsumer<T, K> adder, BiConsumer<T, K> remover,
			BiFunction<T, Integer, K> atIndex, @SuppressWarnings("unchecked") K...values) {

		values = filterUniques(values);

		assertTrue(values.length>1, "Needs at least 2 test values for add/remove");

		assertThrows(IndexOutOfBoundsException.class, () -> atIndex.apply(instance, 0));
		assertThrows(IndexOutOfBoundsException.class, () -> atIndex.apply(instance, -1));

		List<K> buffer = new ArrayList<>(values.length);

		// Incremental add
		for(K value : values) {
			adder.accept(instance, value);
			assertEquals(value, atIndex.apply(instance, buffer.size()));
			buffer.add(value);
		}

		Random r = random();

		while(!buffer.isEmpty()) {
			int index = r.nextInt(buffer.size());
			K value = buffer.remove(index);
			assertEquals(value, atIndex.apply(instance, index));
			remover.accept(instance, value);
		}
	}

	@SuppressWarnings("boxing")
	public static <T extends Object, K extends Object> void assertListInsertAt(
			T instance, TriConsumer<T, K, Integer> inserter,
			BiFunction<T, Integer, K> atIndex, @SuppressWarnings("unchecked") K...values) {

		values = filterUniques(values);

		assertTrue(values.length>1, "Needs at least 2 test values for insert/remove");

		K first = values[0];

		assertThrows(IndexOutOfBoundsException.class, () -> inserter.accept(instance, first, -1));
		assertNPE(() -> inserter.accept(instance, null, 0));

		List<K> buffer = new ArrayList<>(values.length);

		// Incremental add
		for(K value : values) {
			int index = buffer.isEmpty() ? 0 : Math.min(0, buffer.size()-1);
			inserter.accept(instance, value, index);
			buffer.add(index, value);

			assertListEquals(instance, buffer, atIndex);
		}
	}

	@SuppressWarnings("boxing")
	public static <T extends Object, K extends Object> void assertListRemoveAt(
			T instance, BiConsumer<T, K> adder,
			BiConsumer<T, Integer> remover,
			BiFunction<T, Integer, K> atIndex, @SuppressWarnings("unchecked") K...values) {

		values = filterUniques(values);

		assertTrue(values.length>1, "Needs at least 2 test values for insert/remove");

		assertThrows(IndexOutOfBoundsException.class, () -> remover.accept(instance, -1));
		assertThrows(IndexOutOfBoundsException.class, () -> remover.accept(instance, 0));

		List<K> buffer = new ArrayList<>(values.length);

		for(K value : values) {
			buffer.add(value);
			adder.accept(instance, value);
		}

		// Incremental add
		while(!buffer.isEmpty()) {
			int index = buffer.size()>1 ? buffer.size()-1 : 0;
			remover.accept(instance, index);
			buffer.remove(index);

			assertListEquals(instance, buffer, atIndex);
		}
	}

	@SuppressWarnings("boxing")
	private static <T extends Object, K extends Object> void assertListEquals(
			T instance, List<K> list, BiFunction<T, Integer, K> atIndex) {
		for(int i=0; i<list.size(); i++) {
			assertEquals(list.get(i), atIndex.apply(instance, i),
					"Mismatch at index "+i);
		}
	}

	@SuppressWarnings("boxing")
	public static <T extends Object, K extends Object> void assertPredicate(
			T instance, BiFunction<T, K, Boolean> modifier,
			Predicate<T> predicate, Function<? super K, String> msgGen, @SuppressWarnings("unchecked") K... values) {

		for(K value : values) {
			boolean expected = modifier.apply(instance, value).booleanValue();
			assertEquals(expected, predicate.test(instance),
					() -> {return "predicate failed for value "+msgGen.apply(value);});
		}
	}

	//-------------------------
	//  INJECTIONS
	//-------------------------

	public static <M extends Object, K extends Object, T extends Object> BiConsumer<M, K> inject_genericSetter(
			BiConsumer<M, T> setter, Function<K, T> transform) {
		return (m, val) -> {
			setter.accept(m, transform.apply(val));
		};
	}

	public static <M extends Object, K extends Object> BiConsumer<M, K> inject_genericInserter(
			TriConsumer<M, K, Integer> inserter, Supplier<Integer> indices) {
		return (m, val) -> {
			inserter.accept(m, val, indices.get());
		};
	}

	//-------------------------
	//  TRANSFORMATIONS
	//-------------------------

	/**
	 * Creates a wrapper around a generic getter method that returns a collection
	 * and transforms the result based on the specified {@code transform} function.
	 *
	 * @return
	 */
	public static <M extends Object, T extends Object, K extends Object> Function<M, List<K>> transform_genericCollectionGetter(
			Function<M, ? extends Collection<T>> getter, Function<T, K> transform) {
		return m -> {
			List<K> result = new ArrayList<>();
			for(T item : getter.apply(m)) {
				result.add(transform.apply(item));
			}
			return result;
		};
	}

	public static <M extends Object, T extends Object, K extends Object> Function<M, K> transform_genericValue(
			Function<M, T> getter, Function<T, K> transform) {
		return m -> {
			return transform.apply(getter.apply(m));
		};
	}

	public static <M extends Object, T extends Object, K extends Object> Function<M, Optional<K>> transform_genericOptValue(
			Function<M, Optional<T>> getter, Function<T, K> transform) {
		return m -> {
			return getter.apply(m).map(transform);
		};
	}

	public static <T extends Object, K extends Object>
			BiConsumer<T, Collection<K>> wrap_batchConsumer(BiConsumer<T, K> action) {
		return (m, items) -> {
			for(K item : items) {
				action.accept(m, item);
			}
		};
	}


	//-------------------------
	//  SUPPLIERS
	//-------------------------

	public static <V extends Object> Supplier<V> constant(V value) {
		return () -> value;
	}

	public static <E extends Object> void makeTests(List<E> args,
			Function<E, String> labelGen,
			Predicate<? super E> check, boolean expectTrue,
			Consumer<? super DynamicTest> collector) {
		for(E arg : args) {
			collector.accept(DynamicTest.dynamicTest(labelGen.apply(arg), () -> {
				if(expectTrue) {
					assertTrue(check.test(arg));
				} else {
					assertFalse(check.test(arg));
				}
			}));
		}
	}

	public static int[] intRange(int from, int to) {
		int[] array = new int[to-from+1];
		for(int idx = 0; idx<array.length; idx++) {
			array[idx] = from+idx;
		}
		return array;
	}

	public static long[] longRange(long from, long to) {
		long diff = to-from+1;
		assertTrue(diff<MAX_INTEGER_INDEX);
		long[] array = new long[(int)diff];
		for(int idx = 0; idx<array.length; idx++) {
			array[idx] = from+idx;
		}
		return array;
	}
}
