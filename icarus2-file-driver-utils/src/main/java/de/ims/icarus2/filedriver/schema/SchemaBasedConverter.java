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
package de.ims.icarus2.filedriver.schema;

import de.ims.icarus2.filedriver.Converter;

/**
 * @author Markus Gärtner
 *
 */
public interface SchemaBasedConverter extends Converter {

	/**
	 * Returns the schema this converter is based on. Usually this schema will not
	 * change over the lifetime of a converter, so this method should return a constant
	 * value.
	 *
	 * @return
	 */
	Schema getSchema();
}
