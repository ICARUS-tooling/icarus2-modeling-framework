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
package de.ims.icarus2.model.manifest.api;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.ImplementationManifest.Factory;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @param <F> type of the factory implementation under test
 * @param <T> type of the result of {@link Factory#create(Class, ImplementationManifest, ImplementationLoader)}
 *
 * @author Markus Gärtner
 *
 */
public interface FactoryTest<F extends Factory, T> extends ApiGuardedTest<F>, GenericTest<F> {

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default F createTestInstance(TestSettings settings) {
		return createNoArgs();
	}


	@Override
	public default void testMandatoryConstructors() throws Exception {
		// Factory MUST have working no-args constructor!
		getTestTargetClass().newInstance();
	}

	@Test
	default void testIncompatibleResultClass() {
		assertThatExceptionOfType(ClassCastException.class)
			.isThrownBy(() -> create().create(Dummy.class,
					mock(ImplementationManifest.class),
					mock(ImplementationLoader.class)));
	}

	class Dummy {
		// just a dummy class for testing
	}
}
