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
package de.ims.icarus2.filedriver.mapping;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.verify;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.filedriver.io.BufferedIOResource;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.model.api.driver.mapping.WritableMappingTest;
import de.ims.icarus2.util.io.resource.IOResource;

/**
 * @author Markus Gärtner
 *
 */
public interface StoredMappingTest<M extends AbstractStoredMapping<?>, C extends StoredMappingTest.AbstractConfig<M>>
		extends WritableMappingTest<M, C> {

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.mapping.AbstractStoredMapping#delete()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testDelete() {
		return configurations().map(config -> dynamicTest(config.label, () -> {
			try(M mapping = config.create()) {
				BufferedIOResource resource = mapping.getBufferedResource();
				mapping.delete();
				verify(resource).delete();
			} finally {
				config.close();
			}
		}));
	}

	public abstract static class AbstractConfig<M extends AbstractStoredMapping<?>> extends Config<M> {
		public Supplier<BlockCache> blockCacheGen;
		public int cacheSize;
		public Supplier<IOResource> resourceGen;
	}
}
