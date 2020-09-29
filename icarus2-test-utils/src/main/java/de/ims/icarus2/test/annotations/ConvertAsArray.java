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

import de.ims.icarus2.test.util.convert.ArrayConverter;
import de.ims.icarus2.test.util.convert.ComponentConverter;

/**
 * @author Markus Gärtner
 *
 */
@Documented
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@ConvertWith(ArrayConverter.class)
public @interface ConvertAsArray {

	String delimiter() default ";";

	String open() default "{";

	String close() default "}";

	String empty() default "-";

	Class<? extends ComponentConverter> componentConverter();
}
