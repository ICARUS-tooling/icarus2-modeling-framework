/**
 *
 */
package de.ims.icarus2.model.api;

import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import de.ims.icarus2.test.ErrorCodeTest;

/**
 * @author Markus Gärtner
 *
 */
class ModelErrorCodeTest implements ErrorCodeTest<ModelErrorCode> {

	/**
	 * @see de.ims.icarus2.test.ErrorCodeTest#createErrorCodes()
	 */
	@Override
	public ModelErrorCode[] createErrorCodes() {
		return ModelErrorCode.values();
	}

	/**
	 * @see de.ims.icarus2.test.ErrorCodeTest#createCodeParser()
	 */
	@Override
	public IntFunction<ModelErrorCode> createCodeParser() {
		return ModelErrorCode::forCode;
	}

	/**
	 * @see de.ims.icarus2.test.ErrorCodeTest#createCodeGenerator()
	 */
	@Override
	public ToIntFunction<ModelErrorCode> createCodeGenerator() {
		return ModelErrorCode::code;
	}

}
