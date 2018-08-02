/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.test.DiffUtils.Trace;

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

	public static void assertNPE(Executable executable) {
		assertThrows(NullPointerException.class, executable);
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
		Set<K> actual = new HashSet<>();

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

}
