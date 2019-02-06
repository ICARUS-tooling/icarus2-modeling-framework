/**
 *
 */
package de.ims.icarus2.test.guard;

import static java.util.Objects.requireNonNull;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

/**
 * @author Markus Gärtner
 *
 */
class PropertyGuardian extends Guardian {

	private final Class<?> targetClass;

	public PropertyGuardian(Class<?> targetClass) {
		this.targetClass = requireNonNull(targetClass);
	}

	/**
	 * @see de.ims.icarus2.test.guard.Guardian#createTests(org.junit.jupiter.api.TestReporter)
	 */
	@Override
	DynamicNode createTests(TestReporter testReporter) {
		// TODO Auto-generated method stub
		return null;
	}
}
