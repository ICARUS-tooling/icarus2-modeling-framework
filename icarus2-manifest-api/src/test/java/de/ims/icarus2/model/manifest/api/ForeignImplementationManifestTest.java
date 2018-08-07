/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.test.GenericTest.NO_DEFAULT;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
public interface ForeignImplementationManifestTest<M extends ForeignImplementationManifest> extends MemberManifestTest<M> {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ForeignImplementationManifest#getImplementationManifest()}.
	 */
	@Test
	default void testGetImplementationManifest() {
		assertDerivativeGetter(mock(ImplementationManifest.class), mock(ImplementationManifest.class), NO_DEFAULT(),
				ForeignImplementationManifest::getImplementationManifest,
				ForeignImplementationManifest::setImplementationManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ForeignImplementationManifest#isLocalImplementation()}.
	 */
	@Test
	default void testIsLocalImplementation() {
		assertDerivativeIsLocal(mock(ImplementationManifest.class), mock(ImplementationManifest.class),
				ForeignImplementationManifest::isLocalImplementation,
				ForeignImplementationManifest::setImplementationManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ForeignImplementationManifest#setImplementationManifest(de.ims.icarus2.model.manifest.api.ImplementationManifest)}.
	 */
	@Test
	default void testSetImplementationManifest() {
		assertLockableSetter(ForeignImplementationManifest::setImplementationManifest,
				mock(ImplementationManifest.class), true, TYPE_CAST_CHECK);
	}

}
