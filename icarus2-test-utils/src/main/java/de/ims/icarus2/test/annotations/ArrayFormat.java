/**
 *
 */
package de.ims.icarus2.test.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Markus Gärtner
 *
 */
@Documented
//@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ArrayFormat {

	String delimiter() default ";";

	String open() default "{";

	String close() default "}";

	String empty() default "-";
}
