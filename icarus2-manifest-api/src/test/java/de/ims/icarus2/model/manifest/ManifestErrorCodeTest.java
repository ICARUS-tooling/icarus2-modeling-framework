/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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