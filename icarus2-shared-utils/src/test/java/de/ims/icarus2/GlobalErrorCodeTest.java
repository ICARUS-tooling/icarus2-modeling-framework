/**
 *
 */
package de.ims.icarus2;

import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import de.ims.icarus2.test.ErrorCodeTest;

/**
 * @author Markus Gärtner
 *
 */
class GlobalErrorCodeTest implements ErrorCodeTest<GlobalErrorCode> {

	/**
	 * @see de.ims.icarus2.test.ErrorCodeTest#createErrorCodes()
	 */
	@Override
	public GlobalErrorCode[] createErrorCodes() {
		return GlobalErrorCode.values();
	}

	/**
	 * @see de.ims.icarus2.test.ErrorCodeTest#createCodeParser()
	 */
	@Override
	public IntFunction<GlobalErrorCode> createCodeParser() {
		return GlobalErrorCode::forCode;
	}

	/**
	 * @see de.ims.icarus2.test.ErrorCodeTest#createCodeGenerator()
	 */
	@Override
	public ToIntFunction<GlobalErrorCode> createCodeGenerator() {
		return GlobalErrorCode::code;
	}

}
