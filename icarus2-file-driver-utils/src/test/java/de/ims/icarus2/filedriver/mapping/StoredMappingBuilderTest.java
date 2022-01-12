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
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.util.lang.Primitives._int;

import de.ims.icarus2.filedriver.mapping.AbstractStoredMapping.AbstractStoredMappingBuilder;
import de.ims.icarus2.test.guard.ApiGuard;

/**
 * @author Markus Gärtner
 *
 */
public interface StoredMappingBuilderTest<M extends AbstractStoredMapping<?>, B extends AbstractStoredMappingBuilder<B, M>>
		extends MappingBuilderTest<M, B>{

	/**
	 * @see de.ims.icarus2.test.ApiGuardedTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<B> apiGuard) {
		MappingBuilderTest.super.configureApiGuard(apiGuard);
		apiGuard.defaultReturnValue("cacheSize", _int(AbstractStoredMapping.DEFAULT_CACHE_SIZE));
	}
}
