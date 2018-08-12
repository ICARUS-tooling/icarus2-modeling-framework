/**
 *
 */
package de.ims.icarus2.model.manifest;

import java.util.function.BiConsumer;

import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.GenericTest;

/**
 * @author Markus Gärtner
 *
 */
public interface ManifestFrameworkTest<T extends Object> extends GenericTest<T> {

	public static final BiConsumer<Executable, String> TYPE_CAST_CHECK = ManifestTestUtils::assertIllegalValue;


	public static final BiConsumer<Executable, String> INVALID_INPUT_CHECK = (executable, msg) ->
		ManifestTestUtils.assertManifestException(GlobalErrorCode.INVALID_INPUT, executable, msg);

	public static final BiConsumer<Executable, String> DUPLICATE_ID_CHECK = (executable, msg) ->
		ManifestTestUtils.assertManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID, executable, msg);

	public static final BiConsumer<Executable, String> UNKNOWN_ID_CHECK = (executable, msg) ->
		ManifestTestUtils.assertManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID, executable, msg);

}
