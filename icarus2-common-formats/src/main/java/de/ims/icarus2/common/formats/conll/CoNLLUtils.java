/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.common.formats.conll;

import java.net.URL;

/**
 * @author Markus Gärtner
 *
 */
public class CoNLLUtils {

	public static final String CORPUS_NAME = "icarus.conll09";

	/** Returns the URL for the small example corpus shipped with ICARUS2 */
	public static URL getCorpusUrl() {
		return CoNLLUtils.class.getResource(CORPUS_NAME);
	}

	public static final String CONLL09_TEMPLATE = "common.format.conll09";
	public static final String CONLL09_SCHEMA_TEMPLATE = "common.format.conll09.schema";
}
