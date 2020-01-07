/**
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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubId;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertNotPresent;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static de.ims.icarus2.test.TestUtils.settings;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface ModuleManifestTest extends EmbeddedTest<ModuleManifest>,
		ForeignImplementationManifestTest<ModuleManifest> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	@Provider
	default ModuleManifest createTestInstance(TestSettings settings) {
		return ForeignImplementationManifestTest.super.createTestInstance(settings);
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
		return ManifestType.MODULE_MANIFEST;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest#getDriverManifest()}.
	 */
	@Test
	default void testGetDriverManifest() {
		assertHostGetter(ModuleManifest::getDriverManifest);
	}


	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest#getModuleSpec()}.
	 */
	@Test
	default void testGetModuleSpec() {

		// Test unlinked module
		assertNotPresent(create().getModuleSpec());

		// Test linked module

		String specId = "spec1";
		ModuleSpec spec = stubId(mockTypedManifest(ManifestType.MODULE_SPEC), specId);
		DriverManifest driverManifest = mockTypedManifest(ManifestType.DRIVER_MANIFEST);
		when(driverManifest.getModuleSpec(any())).thenReturn(Optional.empty());
		when(driverManifest.getModuleSpec(eq(specId))).thenReturn(Optional.of(spec));

		ModuleManifest instance = createEmbedded(settings(), driverManifest);
		assertNotPresent(instance.getModuleSpec());
		instance.setModuleSpecId(specId);
		assertOptionalEquals(spec, instance.getModuleSpec());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest#setModuleSpecId(java.lang.String)}.
	 */
	@Test
	default void testSetModuleSpecId() {
		assertLockableSetterBatch(settings(),
				ModuleManifest::setModuleSpecId,
				ManifestTestUtils.getLegalIdValues(),
				NPE_CHECK, ManifestTestUtils.INVALID_ID_CHECK, ManifestTestUtils.getIllegalIdValues());
	}

}
