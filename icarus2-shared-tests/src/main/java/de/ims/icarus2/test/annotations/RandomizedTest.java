/**
 *
 */
package de.ims.icarus2.test.annotations;

import static de.ims.icarus2.test.TestTags.RANDOMIZED;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ims.icarus2.test.random.Randomized;

/**
 * @author Markus Gärtner
 *
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
@Tag(RANDOMIZED)
@ExtendWith(Randomized.class)
public @interface RandomizedTest {
	// meta annotation
}
