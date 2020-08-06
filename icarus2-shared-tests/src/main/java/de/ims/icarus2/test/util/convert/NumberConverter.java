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
