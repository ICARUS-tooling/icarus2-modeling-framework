/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import de.ims.icarus2.model.manifest.api.FactoryTest;

/**
 * @author Markus Gärtner
 *
 */
class DefaultSchemaConverterFactoryTest implements FactoryTest<DefaultSchemaConverterFactory, Converter> {

	@Override
	public Class<?> getTestTargetClass() {
		return DefaultSchemaConverterFactory.class;
	}

	//TODO create actual tests for some edge cases
}
