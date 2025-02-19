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
package de.ims.icarus2.model.manifest.api;

import java.util.function.Function;

/**
 * @author Markus Gärtner
 *
 */
class AnnotationFlagTest implements FlagTest<AnnotationFlag>, StringResourceTest<AnnotationFlag> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.FlagTest#createFlags()
	 */
	@Override
	public AnnotationFlag[] createFlags() {
		return AnnotationFlag.values();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FlagTest#createParser()
	 */
	@Override
	public Function<String, AnnotationFlag> createParser() {
		return AnnotationFlag::parseAnnotationFlag;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.StringResourceTest#createStringResources()
	 */
	@Override
	public AnnotationFlag[] createStringResources() {
		return AnnotationFlag.values();
	}
}
