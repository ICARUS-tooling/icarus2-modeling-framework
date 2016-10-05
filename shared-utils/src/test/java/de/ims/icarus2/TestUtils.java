/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 */
package de.ims.icarus2;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author Markus Gärtner
 *
 */
public class TestUtils {

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
		assertNotNull("Expected", expected); //$NON-NLS-1$
		assertNotNull("Actual", actual); //$NON-NLS-1$

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
