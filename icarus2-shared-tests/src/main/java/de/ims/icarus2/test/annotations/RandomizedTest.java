/**
 *
 */
package de.ims.icarus2.test.annotations;

import static de.ims.icarus2.test.TestTags.RANDOMIZED;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ims.icarus2.test.TestTags;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.random.Randomized;

/**
 * Marks a test or entire test suite to be extended with the {@link Randomized} extension
 * and also adds the {@link TestTags#RANDOMIZED} tag.
 * <p>
 * Note that this annotation does <b>not</b> add any meta annotations
 * as to how to execute individual tests!
 * <p>
 * Tests or classes annotated this way offer a way of repeatable randomness, as the underlying
 * random generator is initialized for the respective scope with a constant value, either given
 * as a {@link Seed} annotation or deterministically by calculating a 64 bit hash from either
 * the class name of the test class or the fully qualified class + method name of the test method
 * being executed.
 *
 * @author Markus Gärtner
 *
 * @see RandomGenerator
 * @see Seed
 * @see Randomized
 *
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@Tag(RANDOMIZED)
@ExtendWith(Randomized.class)
public @interface RandomizedTest {
	// meta annotation
}
