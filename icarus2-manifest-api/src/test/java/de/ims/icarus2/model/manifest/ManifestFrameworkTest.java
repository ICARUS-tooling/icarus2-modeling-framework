/**
 *
 */
package de.ims.icarus2.model.manifest;

import java.util.function.BiConsumer;

import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.test.GenericTest;

/**
 * @author Markus Gärtner
 *
 */
public interface ManifestFrameworkTest<T extends Object> extends GenericTest<T> {

	public static final BiConsumer<Executable, Object> TYPE_CAST_CHECK = ManifestTestUtils::assertIllegalValue;
}
