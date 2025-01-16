/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.cli;

import static de.ims.icarus2.util.lang.Primitives._char;
import static de.ims.icarus2.util.lang.Primitives._int;

import de.ims.icarus2.util.strings.StringPrimitives;
import picocli.CommandLine;
import picocli.CommandLine.TypeConversionException;

/**
 * @author Markus Gärtner
 *
 */
public class DurationConverter implements CommandLine.ITypeConverter<Integer> {

	/**
	 * @see picocli.CommandLine.ITypeConverter#convert(java.lang.String)
	 */
	@Override
	public Integer convert(String value) throws TypeConversionException {
		if(value==null || value.isEmpty())
			throw new TypeConversionException("The value is empty");
		int last = value.length()-1;
		int multiplier = 1;

		char unit = value.charAt(last);
		if(!Character.isDigit(unit)) {
			last --;
			switch (unit) {
			case 's':
			case 'S':
				multiplier = 1;
				break;

			case 'm':
			case 'M':
				multiplier = 60;
				break;

			case 'h':
			case 'H':
				multiplier = 3600;
				break;

			default:
				throw new TypeConversionException(String.format("Unsupported time unit symbol: expected 'H', 'M' or 'S' "
						+ "(for hours, minutes or seconds) - got '%s'", _char(unit)));
			}
		}

		int duration = StringPrimitives.parseInt(value, 10, 0, last);
		duration *= multiplier;

		return _int(duration);
	}

}
