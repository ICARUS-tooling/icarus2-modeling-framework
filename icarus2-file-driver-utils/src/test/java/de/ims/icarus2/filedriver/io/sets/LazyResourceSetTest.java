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
package de.ims.icarus2.filedriver.io.sets;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.util.Pair.pair;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.io.PathResolver;
import de.ims.icarus2.model.api.io.ResourcePath;
import de.ims.icarus2.model.manifest.api.LocationType;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.io.resource.FileResourceProvider;

/**
 * @author Markus Gärtner
 *
 */
class LazyResourceSetTest implements ResourceSetTest<LazyResourceSet> {

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.sets.LazyResourceSet#getPathResolver()}.
	 */
	@Test
	void testGetPathResolver() {
		PathResolver resolver = mockResolver();
		LazyResourceSet set = new LazyResourceSet(new FileResourceProvider(), resolver);
		assertThat(set.getPathResolver()).isSameAs(resolver);
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<?> getTestTargetClass() {
		return LazyResourceSet.class;
	}

	@SuppressWarnings("boxing")
	private PathResolver mockResolver(ResourcePath...paths) {
		PathResolver resolver = mock(PathResolver.class);
		when(resolver.getPathCount()).thenReturn(paths.length);
		doAnswer(invoc -> paths[(Integer)invoc.getArgument(0)]).when(resolver).getPath(anyInt());
		return resolver;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public LazyResourceSet createTestInstance(TestSettings settings) {
		return settings.process(new LazyResourceSet(new FileResourceProvider(), mockResolver()));
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.sets.ResourceSetTest#createFilled()
	 */
	@Override
	public LazyResourceSet createFilled() {
		return new LazyResourceSet(new FileResourceProvider(),
				mockResolver(new ResourcePath("test", LocationType.LOCAL)));
	}

	@TestFactory
	Stream<DynamicNode> testSupportedPathTypes() {
		return Stream.<Pair<LocationType, String>>of(
				pair(LocationType.LOCAL, "test/file"),
				pair(LocationType.REMOTE, TestUtils.TEST_URL.toExternalForm()))
				.map(p -> dynamicTest(p.first.name(), () -> {
					LazyResourceSet set = new LazyResourceSet(new FileResourceProvider(), mockResolver(
							new ResourcePath(p.second, p.first)));
					assertThat(set.getResourceCount()).isEqualTo(1);
					assertThat(set.getResourceAt(0)).isNotNull();
		}));
	}

	@TestFactory
	Stream<DynamicNode> testUnsupportedPathTypes() {
		return Stream.of(LocationType.values())
				.filter(type -> type!=LocationType.LOCAL && type!=LocationType.REMOTE)
				.map(type -> dynamicTest(type.name(), () -> {
					LazyResourceSet set = new LazyResourceSet(new FileResourceProvider(), mockResolver(
							new ResourcePath("test", type)));

					assertModelException(GlobalErrorCode.DELEGATION_FAILED, () -> set.getResourceAt(0));
				}));
	}
}
