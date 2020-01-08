/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.io;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 * @param <S> the type of the source resource to be synchronized on
 * @param <A> the accessor type under test
 */
public interface SynchronizedAccessorTest<S, A extends SynchronizedAccessor<S>>
		extends ApiGuardedTest<A> {

	@Provider
	S createSource();

	@Provider
	A createAccessor(S source);

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default A createTestInstance(TestSettings settings) {
		return settings.process(createAccessor(createSource()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.io.SynchronizedAccessor#close()}.
	 */
	@Test
	default void testClose() {
		create().close();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.io.SynchronizedAccessor#getSource()}.
	 */
	@Test
	default void testGetSource() {
		S source = createSource();
		A accessor = createAccessor(source);
		assertThat(accessor.getSource()).isNotNull().isSameAs(source);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.io.SynchronizedAccessor#begin()}.
	 * Test method for {@link de.ims.icarus2.model.api.io.SynchronizedAccessor#end()}.
	 */
	@Test
	default void testBeginEndCycle() {
		A accessor = create();
		accessor.begin();
		try {
			// no-op
		} finally {
			accessor.end();
		}
	}
}
