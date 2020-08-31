/**
 *
 */
package de.ims.icarus2.query.api.engine.matcher;

import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;

import de.ims.icarus2.query.api.engine.matcher.mark.Interval;
import de.ims.icarus2.util.strings.StringPrimitives;

/**
 * @author Markus Gärtner
 *
 */
public class IntervalConverter extends SimpleArgumentConverter {

	/**
	 * @see org.junit.jupiter.params.converter.SimpleArgumentConverter#convert(java.lang.Object, java.lang.Class)
	 */
	@Override
	protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
		if(targetType!=Interval.class)
			throw new ArgumentConversionException("Unsupported target type: "+targetType);
		if(source.getClass()!=String.class)
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

}
