/**
 *
 */
package de.ims.icarus2.test;

/**
 * @author Markus Gärtner
 *
 */
public interface TargetedTest<T extends Object> extends Testable<T> {

	Class<? extends T> getTestTargetClass();

}
