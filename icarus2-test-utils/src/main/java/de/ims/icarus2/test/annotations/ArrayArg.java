/**
 *
 */
package de.ims.icarus2.test.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.converter.ConvertWith;

import de.ims.icarus2.test.util.convert.ArrayConverters;
import de.ims.icarus2.test.util.convert.ComponentConverter;

/**
 * @author Markus Gärtner
 *
 */
@Documented
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@ConvertWith(ArrayConverters.GenericArrayConverter.class)
public @interface ArrayArg {

	/** The converter used for individual array elements. */
	Class<? extends ComponentConverter> componentConverter();
}
