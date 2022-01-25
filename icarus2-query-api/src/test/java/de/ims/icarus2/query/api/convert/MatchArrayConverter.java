/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.convert;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;

import de.ims.icarus2.query.api.annotation.MatchFormat;
import de.ims.icarus2.query.api.engine.result.Match;
import de.ims.icarus2.test.util.convert.ArrayConverters.ArrayConverterBase;

/**
 * @author Markus Gärtner
 *
 */
public class MatchArrayConverter extends ArrayConverterBase {

	/**
	 * @see de.ims.icarus2.test.util.convert.ArrayConverters.ArrayConverterBase#convert(java.lang.String[], org.junit.jupiter.api.extension.ParameterContext, java.lang.Class)
	 */
	@Override
	protected Object convert(String[] items, ParameterContext context, Class<?> componentType)
			throws ArgumentConversionException {
		final MatchFormat format = context.findAnnotation(MatchFormat.class).orElse(MatchConverter.defaultFormat());

		Match[] result = new Match[items.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = MatchConverter.parseMatch(items[i], format);
		}
		return result;
	}

//	/**
//	 * @see de.ims.icarus2.test.util.convert.ArrayConverters.MatrixConverterBase#convertElement(java.lang.String)
//	 */
//	@Override
//	protected Object convertElement(String raw, ParameterContext context) throws ArgumentConversionException {
//		final MatchFormat format = context.findAnnotation(MatchFormat.class).orElse(MatchConverter.defaultFormat());
//		return MatchConverter.parseMatch(raw, format);
//	}

}
