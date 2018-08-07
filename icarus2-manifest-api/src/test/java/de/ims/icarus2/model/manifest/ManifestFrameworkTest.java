/**
 *
 */
package de.ims.icarus2.model.manifest;

import java.util.function.Consumer;

import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.test.GenericTest;

/**
 * @author Markus Gärtner
 *
 */
public interface ManifestFrameworkTest<T extends Object> extends GenericTest<T> {

	public static final Consumer<Executable> TYPE_CAST_CHECK = ManifestTestUtils::assertIllegalValue;
}
