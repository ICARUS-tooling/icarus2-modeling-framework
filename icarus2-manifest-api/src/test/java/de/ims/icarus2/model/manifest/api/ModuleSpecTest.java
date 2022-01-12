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
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.test.TestUtils.DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertOptGetter;
import static de.ims.icarus2.test.TestUtils.other;
import static de.ims.icarus2.test.TestUtils.settings;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;

import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec;
import de.ims.icarus2.util.Multiplicity;

/**
 * @author Markus Gärtner
 *
 */
public interface ModuleSpecTest extends ManifestFragmentTest<ModuleSpec>,
		ModifiableIdentityTest<ModuleSpec>, DocumentableTest<ModuleSpec>,
		CategorizableTest<ModuleSpec>, EmbeddedTest<ModuleSpec> {


	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestFragmentTest#testGetId()
	 */
	@Override
	default void testGetId() {
		ModifiableIdentityTest.super.testGetId();
		ManifestFragmentTest.super.testGetId();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	default Set<ManifestType> getAllowedHostTypes() {
		return singleton(ManifestType.DRIVER_MANIFEST);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.MODULE_SPEC;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec#getDriverManifest()}.
	 */
	@Test
	default void testGetDriverManifest() {
		assertHostGetter(ModuleSpec::getDriverManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec#isCustomizable()}.
	 */
	@Test
	default void testIsCustomizable() {
		assertGetter(create(),
				Boolean.TRUE, Boolean.FALSE,
				DEFAULT(ModuleSpec.DEFAULT_IS_CUSTOMIZABLE),
				ModuleSpec::isCustomizable,
				ModuleSpec::setCustomizable);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec#getMultiplicity()}.
	 */
	@Test
	default void testGetMultiplicity() {
		for(Multiplicity multiplicity : Multiplicity.values()) {
			assertGetter(create(),
					multiplicity, other(multiplicity),
					DEFAULT(ModuleSpec.DEFAULT_MULTIPLICITY),
					ModuleSpec::getMultiplicity,
					ModuleSpec::setMultiplicity);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec#getExtensionPointUid()}.
	 */
	@Test
	default void testGetExtensionPointUid() {
		assertOptGetter(create(),
				"uid1", "uid2",
				NO_DEFAULT(),
				ModuleSpec::getExtensionPointUid,
				ModuleSpec::setExtensionPointUid);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec#setMultiplicity(de.ims.icarus2.util.Multiplicity)}.
	 */
	@Test
	default void testSetMultiplicity() {
		assertLockableSetterBatch(settings(),
				ModuleSpec::setMultiplicity,
				Multiplicity.values(),
				NPE_CHECK, NO_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec#setCustomizable(boolean)}.
	 */
	@Test
	default void testSetCustomizable() {
		assertLockableSetter(settings(), ModuleSpec::setCustomizable);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec#setExtensionPointUid(java.lang.String)}.
	 */
	@Test
	default void testSetExtensionPointUid() {
		assertLockableSetter(settings(),
				ModuleSpec::setExtensionPointUid,
				"uid1",
				NO_NPE_CHECK, NO_CHECK);
	}

}
