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
package de.ims.icarus2.filedriver.io.sets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.util.io.resource.IOResource;

/**
 * @author Markus Gärtner
 *
 */
class SingletonResourceSetTest implements ResourceSetTest<SingletonResourceSet> {

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.sets.SingletonResourceSet#SingletonResourceSet(de.ims.icarus2.util.io.resource.IOResource)}.
	 */
	@Test
	void testSingletonResourceSet() {
		IOResource resource = mock(IOResource.class);
		SingletonResourceSet set = new SingletonResourceSet(resource);
		assertThat(set.getResourceCount()).isEqualTo(1);
		assertThat(set.getResourceAt(0)).isSameAs(resource);
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<?> getTestTargetClass() {
		return SingletonResourceSet.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public SingletonResourceSet createTestInstance(TestSettings settings) {
		return settings.process(createFilled());
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.sets.ResourceSetTest#createFilled()
	 */
	@Override
	public SingletonResourceSet createFilled() {
		return new SingletonResourceSet(mock(IOResource.class));
	}

}
