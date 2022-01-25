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

import static de.ims.icarus2.util.lang.Primitives._int;

import java.lang.reflect.AnnotatedElement;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.platform.commons.util.AnnotationUtils;

import de.ims.icarus2.query.api.annotation.MatchFormat;
import de.ims.icarus2.query.api.engine.result.DefaultMatch;
import de.ims.icarus2.query.api.engine.result.Match;
import de.ims.icarus2.test.util.convert.ComponentConverter;

/**
 * @author Markus Gärtner
 *
 */
public class MatchConverter implements ComponentConverter, ArgumentConverter {

	@MatchFormat
	private static final Object _annoCatch = new Object();

	private static final MatchFormat defaultFormat;
	static {
		AnnotatedElement element;
		try {
			element = MatchConverter.class.getDeclaredField("_annoCatch");
		} catch (NoSuchFieldException | SecurityException e) {
			throw new InternalError(e);
		}
		defaultFormat = AnnotationUtils.findAnnotation(element, MatchFormat.class)
				.orElseThrow(() -> new InternalError("Missing our default annotation field"));
	}

	public static MatchFormat defaultFormat() { return defaultFormat; }

	/**
	 * @see org.junit.jupiter.params.converter.ArgumentConverter#convert(java.lang.Object, org.junit.jupiter.api.extension.ParameterContext)
	 */
	@Override
	public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
		if(!String.class.isInstance(source))
			throw new ArgumentConversionException("Can only convert strings: "+source);
		return parseMatch((String) source, context);
	}

	/**
	 * @see de.ims.icarus2.test.util.convert.ComponentConverter#convert(java.lang.Object, org.junit.jupiter.api.extension.ParameterContext, java.lang.Class)
	 */
	@Override
	public Object convert(Object source, ParameterContext context, Class<?> componentType)
			throws ArgumentConversionException {
		if(!Match.class.isAssignableFrom(componentType))
			throw new ArgumentConversionException("Can only convert to Match class: "+componentType);

		return convert(source, context);
	}

	public static Match parseMatch(String s, ParameterContext context) {
		final MatchFormat format = context.findAnnotation(MatchFormat.class).orElse(defaultFormat());
		return parseMatch(s, format);
	}

	public static Match parseMatch(String s, MatchFormat format) {

		int left = 0;
		if(!format.open().isEmpty()) {
			if(!s.startsWith(format.open()))
				throw new ArgumentConversionException(String.format("Expected %s to start with %s",
						s, format.open()));
			left += format.open().length();
		}

		int right = s.length();
		if(!format.close().isEmpty()) {
			if(!s.endsWith(format.close()))
				throw new ArgumentConversionException(String.format("Expected %s to end with %s",
						s, format.close()));
			right -= format.close().length();
		}

		s = s.substring(left, right);

		int header = s.indexOf(format.header());
		if(header==-1)
			throw new ArgumentConversionException(String.format("Missing header (separated by %s) in %s",
					format.header(), s));

		int index = Integer.parseInt(s.substring(0, header));

		String[] mappings = s.substring(header+1).split(format.delimiter());

		if(mappings.length==0) {
			return DefaultMatch.empty(index);
		}

		int[] m_node = new int[mappings.length];
		int[] m_index = new int[mappings.length];

		for (int i = 0; i < mappings.length; i++) {
			String m = mappings[i];
			int sep = m.indexOf(format.assignment());
			if(sep==-1)
				throw new ArgumentConversionException(String.format("Missing assignment (indicated by %s) in mapping %s at index %d",
						format.assignment(), m, _int(i)));

			m_node[i] = Integer.parseInt(m.substring(0, sep));
			m_index[i] = Integer.parseInt(m.substring(sep+format.assignment().length()));
		}

		return DefaultMatch.of(index, m_node, m_index);
	}
}
