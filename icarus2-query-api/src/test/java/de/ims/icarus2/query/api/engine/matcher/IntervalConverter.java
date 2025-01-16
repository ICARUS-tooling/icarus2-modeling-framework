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
package de.ims.icarus2.query.api.engine.matcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;

import de.ims.icarus2.query.api.engine.matcher.mark.Interval;
import de.ims.icarus2.test.annotations.ArrayArg;
import de.ims.icarus2.test.util.convert.ComponentConverter;
import de.ims.icarus2.util.strings.StringPrimitives;

/**
 * @author Markus Gärtner
 *
 */
public class IntervalConverter extends SimpleArgumentConverter implements ComponentConverter {

	/**
	 * @see org.junit.jupiter.params.converter.SimpleArgumentConverter#convert(java.lang.Object, java.lang.Class)
	 */
	@Override
	protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
		if(targetType!=Interval.class)
			throw new ArgumentConversionException("Unsupported target type: "+targetType);
		if(!String.class.isInstance(source))
			throw new ArgumentConversionException("Unsupported source type: "+source.getClass());

		String s = (String)source;

		if(s.equals("-")) {
			return Interval.blank();
		}

		int sep = s.indexOf('-');
		if(sep==-1) {
			return Interval.of(Integer.parseInt(s));
		}

		return Interval.of(
				StringPrimitives.parseInt(s, 0, sep-1),
				StringPrimitives.parseInt(s, sep+1, -1));
	}

	@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
	@Retention(RetentionPolicy.RUNTIME)
	@ConvertWith(IntervalConverter.class)
	public @interface IntervalArg {
		// marker interface
	}

	@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
	@Retention(RetentionPolicy.RUNTIME)
	@ArrayArg(componentConverter=IntervalConverter.class)
	public @interface IntervalArrayArg {
		// marker interface
	}

	/**
	 * @see de.ims.icarus2.test.util.convert.ComponentConverter#convert(java.lang.Object, org.junit.jupiter.api.extension.ParameterContext, java.lang.Class)
	 */
	@Override
	public Object convert(Object source, ParameterContext context, Class<?> componentType)
			throws ArgumentConversionException {
		return convert(source, componentType);
	}
}
