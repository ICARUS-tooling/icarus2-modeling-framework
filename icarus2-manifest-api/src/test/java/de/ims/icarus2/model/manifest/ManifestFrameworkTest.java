/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

	public static final BiConsumer<Executable, String> INVALID_ID_CHECK = (executable, msg) ->
		ManifestTestUtils.assertManifestException(ManifestErrorCode.MANIFEST_INVALID_ID, executable, msg);

	public static final BiConsumer<Executable, String> INVALID_INPUT_CHECK = (executable, msg) ->
		ManifestTestUtils.assertManifestException(GlobalErrorCode.INVALID_INPUT, executable, msg);

	public static final BiConsumer<Executable, String> DUPLICATE_ID_CHECK = (executable, msg) ->
		ManifestTestUtils.assertManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID, executable, msg);

	public static final BiConsumer<Executable, String> UNKNOWN_ID_CHECK = (executable, msg) ->
		ManifestTestUtils.assertManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID, executable, msg);

}
