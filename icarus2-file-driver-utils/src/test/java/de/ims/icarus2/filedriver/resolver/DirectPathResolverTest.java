/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.resolver;

import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.io.ResourcePath;
import de.ims.icarus2.model.manifest.api.LocationType;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class DirectPathResolverTest implements ApiGuardedTest<DirectPathResolver> {

	//TODO add tests for the forManifest() method

	@Override
	public Class<?> getTestTargetClass() {
		return DirectPathResolver.class;
	}

	@Override
	public DirectPathResolver createTestInstance(TestSettings settings) {
		return settings.process(new DirectPathResolver("test"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.resolver.DirectPathResolver#DirectPathResolver(java.util.List)}.
	 */
	@Test
	void testDirectPathResolverListOfString() {
		List<String> paths = list("path1", "path2", "path3");
		DirectPathResolver resolver = new DirectPathResolver(paths);
		assertThat(resolver.getPathCount()).isEqualTo(paths.size());
		for (int i = 0; i < paths.size(); i++) {
			ResourcePath path = resolver.getPath(i);
			assertThat(path).isNotNull();
			assertThat(path.getType()).isSameAs(LocationType.LOCAL);
			assertThat(path.getPath()).isEqualTo(paths.get(i));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.resolver.DirectPathResolver#DirectPathResolver(java.lang.String[])}.
	 */
	@Test
	void testDirectPathResolverStringArray() {
		String[] paths = {"path1", "path2", "path3"};
		DirectPathResolver resolver = new DirectPathResolver(paths);
		assertThat(resolver.getPathCount()).isEqualTo(paths.length);
		for (int i = 0; i < paths.length; i++) {
			ResourcePath path = resolver.getPath(i);
			assertThat(path).isNotNull();
			assertThat(path.getType()).isSameAs(LocationType.LOCAL);
			assertThat(path.getPath()).isEqualTo(paths[i]);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.resolver.DirectPathResolver#close()}.
	 */
	@Test
	void testClose() {
		create().close();
	}

}
