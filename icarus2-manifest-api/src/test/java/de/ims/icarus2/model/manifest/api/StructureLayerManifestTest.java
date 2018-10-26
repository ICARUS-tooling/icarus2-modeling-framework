/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertManifestException;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubId;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubType;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.transform_id;
import static de.ims.icarus2.model.manifest.api.ItemLayerManifestTest.mockContainerManifest;
import static de.ims.icarus2.test.TestUtils.assertNotPresent;
import static de.ims.icarus2.test.TestUtils.assertPredicate;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.standard.ItemLayerManifestImpl;

/**
 * @author Markus Gärtner
 *
 */
public interface StructureLayerManifestTest<M extends StructureLayerManifest> extends ItemLayerManifestTest<M> {

	public static StructureManifest mockStructureManifest(String id) {
		StructureManifest structureManifest = mockTypedManifest(ManifestType.STRUCTURE_MANIFEST);

		return stubType(stubId(structureManifest, id), ManifestType.STRUCTURE_MANIFEST);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.STRUCTURE_LAYER_MANIFEST;
	}

	/**
	 * Adds all of the supplied {@link ContainerManifest} instances to the {@link StructureLayerManifest}
	 * and then expects the {@link StructureLayerManifest#getRootStructureManifest()} method to fail or
	 * return {@code null} depending on the {@code expectNull} parameter.
	 *
	 * @param settings
	 * @param manifest
	 * @param containerManifests
	 */
	default <C extends ContainerManifest> void assertInvalidRootStructure(
			boolean expectEmpty, @SuppressWarnings("unchecked") C...containerManifests) {
		M manifest = createUnlocked();

		Hierarchy<ContainerManifest> hierarchy = ItemLayerManifestImpl.getOrCreateLocalContainerhierarchy(manifest);
		assertTrue(hierarchy.isEmpty());

		for(C containerManifest : containerManifests) {
			hierarchy.add(containerManifest);
		}

		if(expectEmpty) {
			assertNotPresent(manifest.getRootStructureManifest());
		} else {
			assertManifestException(ManifestErrorCode.MANIFEST_MISSING_MEMBER,
					() -> manifest.getRootStructureManifest(), null);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureLayerManifest#getRootStructureManifest()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testGetRootStructureManifest() {
		ContainerManifest root = mockContainerManifest("root");
		StructureManifest structure = mockStructureManifest("structrue1");
		ContainerManifest container1 = mockContainerManifest("container1");
		ContainerManifest container2 = mockContainerManifest("container2");

		assertInvalidRootStructure(true);
		assertInvalidRootStructure(true, root);
		assertInvalidRootStructure(true, structure);

		assertInvalidRootStructure(false, root, container1);
		assertInvalidRootStructure(false, structure, container1);

		Predicate<M> rootCheck = m -> {
			return m.getRootStructureManifest().orElse(null)==structure;
		};

		BiFunction<M, ContainerManifest, Boolean> staticModifier = (m, cont) -> {
			Hierarchy<ContainerManifest> hierarchy = ItemLayerManifestImpl.getOrCreateLocalContainerhierarchy(m);
			hierarchy.add(cont);
			return hierarchy.getDepth()>1 && hierarchy.levelOf(structure)>0;
		};

		// Test with simply adding more containers after initial root
		assertPredicate(createUnlocked(), staticModifier, rootCheck, transform_id(),
				root, structure, container1, container2);

		BiFunction<M, ContainerManifest, Boolean> mixedModifier = (m, cont) -> {
			Hierarchy<ContainerManifest> hierarchy = ItemLayerManifestImpl.getOrCreateLocalContainerhierarchy(m);
			hierarchy.insert(cont, 0);
			return hierarchy.getDepth()>1 && hierarchy.levelOf(structure)>0;
		};

		// Test with shifting the root manifest
		assertPredicate(createUnlocked(), mixedModifier, rootCheck, transform_id(),
				structure, root, container1, container2);
	}
}
