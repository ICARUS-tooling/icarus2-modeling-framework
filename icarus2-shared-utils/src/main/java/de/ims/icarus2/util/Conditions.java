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
}
