/**
 *
 */
package de.ims.icarus2.model.manifest;

import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import de.ims.icarus2.test.ErrorCodeTest;

/**
 * @author Markus Gärtner
 *
 */
class ManifestErrorCodeTest implements ErrorCodeTest<ManifestErrorCode> {

	/**
	 * @see de.ims.icarus2.test.ErrorCodeTest#createErrorCodes()
	 */
	@Override
	public ManifestErrorCode[] createErrorCodes() {
		return ManifestErrorCode.values();
	}

	/**
	 * @see de.ims.icarus2.test.ErrorCodeTest#createCodeParser()
	 */
	@Override
	public IntFunction<ManifestErrorCode> createCodeParser() {
		return ManifestErrorCode::forCode;
	}

	/**
	 * @see de.ims.icarus2.test.ErrorCodeTest#createCodeGenerator()
	 */
	@Override
	public ToIntFunction<ManifestErrorCode> createCodeGenerator() {
		return ManifestErrorCode::code;
	}

}
