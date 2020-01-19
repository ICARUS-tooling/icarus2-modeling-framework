/**
 *
 */
package de.ims.icarus2.test;

import static de.ims.icarus2.test.TestTags.LEARNER;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;

/**
 * @author Markus Gärtner
 *
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Tag(LEARNER)
public @interface LearnerTest {
	// meta annotation
}
