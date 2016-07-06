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
 *
 */
package de.ims.icarus2.util;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;

/**
 * @author Markus Gärtner
 *
 */
public class Conditions {

	/**
	 * Utility method to ensure that certain fields get set at most once during
	 * the lifetime of an object. In case the given {@code present} object is not
	 * {@code null} and is not the same (as in reference equality) as the {@code given}
	 * argument, this method throws a {@code IcarusException} of type
	 * {@link GlobalErrorCode#ILLEGAL_STATE illegal state}.
	 *
	 * @param msg
	 * @param present
	 * @param given
	 */
	public static void checkNotSet(String msg, Object present, Object given) {
		if(present!=null && present!=given)
			throw new IcarusException(GlobalErrorCode.ILLEGAL_STATE,
					String.format("%s: %s already present - cant set %s", msg, present, given));
	}

	public static void checkState(boolean condition) {
		if(!condition)
			throw new IllegalStateException();
	}

	public static void checkState(String msg, boolean condition) {
		if(!condition)
			throw new IllegalStateException(msg);
	}

	public static void checkArgument(boolean condition) {
		if(!condition)
			throw new IllegalArgumentException();
	}

	public static void checkArgument(String msg, boolean condition) {
		if(!condition)
			throw new IllegalArgumentException(msg);
	}

	public static void checkNotNull(Object obj) {
		if(obj==null)
			throw new NullPointerException();
	}

	public static void checkNotNull(String msg, Object obj) {
		if(obj==null)
			throw new NullPointerException(msg);
	}
}
