/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.test.util.convert;

import static java.util.Objects.requireNonNull;

import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;

/**
 * @author Markus Gärtner
 *
 */
public class NumberConverter extends SimpleArgumentConverter {

	private static String shrink(String s) {
		return s.substring(0, s.length()-1);
	}

	/**
	 * @see org.junit.jupiter.params.converter.SimpleArgumentConverter#convert(java.lang.Object, java.lang.Class)
	 */
	@Override
	protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
		requireNonNull(source);
		if(source instanceof Number) {
			return source;
		}
		if(!String.class.isInstance(source))
			throw new ArgumentConversionException("Not a valid source type: "+source.getClass());

		String s = (String) source;
		char last = s.charAt(s.length()-1);

		// Honor explicitly marked types
		if(last=='F') {
			return Float.valueOf(shrink(s));
		} else if(last=='D') {
			return Double.valueOf(shrink(s));
		} else if(last=='L') {
			return Long.valueOf(shrink(s));
		} else if(last=='S') {
			return Short.valueOf(shrink(s));
		} else if(last=='B') {
			return Byte.valueOf(shrink(s));
		}

		// No markers, so try integer first and then default to floating type
		try {
			return Integer.valueOf(s);
		} catch(NumberFormatException e) { /* ignore */ }

		return Double.valueOf(s);
	}

}
