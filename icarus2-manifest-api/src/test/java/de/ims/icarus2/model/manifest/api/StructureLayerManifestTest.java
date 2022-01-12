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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertManifestException;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubId;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubType;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.transform_id;
import static de.ims.icarus2.test.TestUtils.assertNotPresent;
import static de.ims.icarus2.test.TestUtils.assertPredicate;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.standard.AbstractItemLayerManifestBase;

/**
 * @author Markus Gärtner
 *
 */
public interface StructureLayerManifestTest extends ItemLayerManifestTestMixin<StructureLayerManifest> {

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
	default <C extends ContainerManifestBase<?>> void assertInvalidRootStructure(
			boolean expectEmpty, @SuppressWarnings("unchecked") C...containerManifests) {
		StructureLayerManifest manifest = createUnlocked();

		Hierarchy<ContainerManifestBase<?>> hierarchy = AbstractItemLayerManifestBase
				.getOrCreateLocalContainerhierarchy(manifest);
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

	default StructureLayerManifest createWithHierarchy(ContainerManifestBase<?>...containers) {
		StructureLayerManifest manifest = createUnlocked();
		Hierarchy<ContainerManifestBase<?>> hierarchy = AbstractItemLayerManifestBase
				.getOrCreateLocalContainerhierarchy(manifest);
		for(ContainerManifestBase<?> container : containers) {
			hierarchy.add(container);
		}
		return manifest;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureLayerManifest#getRootStructureManifest()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testGetRootStructureManifest() {
		ContainerManifest root = ItemLayerManifestTestMixin.mockContainerManifest("root");
		StructureManifest structure = mockStructureManifest("structrue1");
		ContainerManifest container1 = ItemLayerManifestTestMixin.mockContainerManifest("container1");
		ContainerManifest container2 = ItemLayerManifestTestMixin.mockContainerManifest("container2");

		assertInvalidRootStructure(true);
		assertInvalidRootStructure(false, root);

		assertInvalidRootStructure(false, root, container1);

		Predicate<StructureLayerManifest> rootCheck = m -> {
			return m.getRootStructureManifest().orElse(null)==structure;
		};

		BiFunction<StructureLayerManifest, ContainerManifestBase<?>, Boolean> staticModifier = (m, cont) -> {
			Hierarchy<ContainerManifestBase<?>> hierarchy = AbstractItemLayerManifestBase
					.getOrCreateLocalContainerhierarchy(m);
			hierarchy.add(cont);
			return hierarchy.getDepth()>0 && hierarchy.levelOf(structure)>UNSET_INT;
		};

		// Test with simply adding more containers after initial root
		assertPredicate(createWithHierarchy(structure), staticModifier, rootCheck, transform_id(),
				container1, container2);
		assertPredicate(createWithHierarchy(root, structure), staticModifier, rootCheck, transform_id(),
				container1, container2);

		BiFunction<StructureLayerManifest, ContainerManifestBase<?>, Boolean> mixedModifier = (m, cont) -> {
			Hierarchy<ContainerManifestBase<?>> hierarchy = AbstractItemLayerManifestBase
					.getOrCreateLocalContainerhierarchy(m);
			hierarchy.insert(cont, 0);
			return hierarchy.getDepth()>0 && hierarchy.levelOf(structure)>UNSET_INT;
		};

		// Test with shifting the root manifest
		assertPredicate(createWithHierarchy(structure), mixedModifier, rootCheck, transform_id(),
				root, container1, container2);
		assertPredicate(createWithHierarchy(root, structure), mixedModifier, rootCheck, transform_id(),
				container1, container2);
	}
}
