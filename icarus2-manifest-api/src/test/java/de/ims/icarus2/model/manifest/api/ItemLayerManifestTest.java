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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.getIllegalIdValues;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.getLegalIdValues;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubId;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.inject_createTargetLayerManifest;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.transform_targetLayerId;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertMock;
import static de.ims.icarus2.test.TestUtils.assertPredicate;
import static de.ims.icarus2.test.TestUtils.constant;
import static de.ims.icarus2.test.TestUtils.inject_genericInserter;
import static de.ims.icarus2.test.TestUtils.settings;
import static de.ims.icarus2.test.TestUtils.transform_genericValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestFeature;
import de.ims.icarus2.test.func.TriConsumer;

/**
 * @author Markus Gärtner
 *
 */
public interface ItemLayerManifestTest<M extends ItemLayerManifest> extends LayerManifestTest<M> {

	public static ContainerManifest mockContainerManifest(String id) {
		ContainerManifest containerManifest = mockTypedManifest(ManifestType.CONTAINER_MANIFEST);
		return (ContainerManifest) stubId((ManifestFragment)containerManifest, id);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.ITEM_LAYER_MANIFEST;
	}

	@SuppressWarnings("boxing")
	public static <M extends ItemLayerManifest, V extends ContainerManifest> TriConsumer<M, V, Integer>
			inject_insert() {
		return (m, cont, index) -> {
			ItemLayerManifest.getOrCreateLocalContainerhierarchy(m)
				.insert(cont, index);
		};
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifest#getRootContainerManifest()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testGetRootContainerManifest() {
		ContainerManifest root = mockContainerManifest("root");
		ContainerManifest container1 = mockContainerManifest("container1");
		ContainerManifest container2 = mockContainerManifest("container2");

		// Test basic behavior when constantly changing the root manifest itself
		assertGetter(createUnlocked(),
				container1, container2, null,
				ItemLayerManifest::getRootContainerManifest,
				inject_genericInserter(inject_insert(), constant(0)));

		Predicate<M> rootCheck = m -> {
			ContainerManifest manifest = m.getRootContainerManifest();
			assertNotNull(manifest);
			return manifest==root;
		};

		BiFunction<M, ContainerManifest, Boolean> staticModifier = (m, cont) -> {
			ItemLayerManifest.getOrCreateLocalContainerhierarchy(m).add(cont);
			return true;
		};

		// Test with simply adding more containers after initial root
		assertPredicate(createUnlocked(), staticModifier, rootCheck,
				root, container1, container2);

		BiFunction<M, ContainerManifest, Boolean> mixedModifier = (m, cont) -> {
			ItemLayerManifest.getOrCreateLocalContainerhierarchy(m).insert(cont, 0);
			return cont==root;
		};

		// Test with shifting the root manifest
		assertPredicate(createUnlocked(), mixedModifier, rootCheck,
				root, container1, container2);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifest#hasLocalContainerHierarchy()}.
	 */
	@Test
	default void testIsLocalContainerHierarchy() {
		assertDerivativeIsLocal(settings(),
				mock(Hierarchy.class), mock(Hierarchy.class),
				ItemLayerManifest::hasLocalContainerHierarchy,
				ItemLayerManifest::setContainerHierarchy);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifest#getBoundaryLayerManifest()}.
	 */
	@Test
	default void testGetBoundaryLayerManifest() {
		assertDerivativeGetter(settings(),
				"layer1",
				"layer2",
				NO_DEFAULT(),
				transform_genericValue(ItemLayerManifest::getBoundaryLayerManifest, transform_targetLayerId()),
				inject_createTargetLayerManifest(ItemLayerManifest::setBoundaryLayerId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifest#isLocalBoundaryLayerManifest()}.
	 */
	@Test
	default void testIsLocalBoundaryLayerManifest() {
		assertDerivativeIsLocal(settings(),
				"layer1",
				"layer2",
				ItemLayerManifest::isLocalBoundaryLayerManifest,
				inject_createTargetLayerManifest(ItemLayerManifest::setBoundaryLayerId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifest#getFoundationLayerManifest()}.
	 */
	@Test
	default void testGetFoundationLayerManifest() {
		assertDerivativeGetter(settings(),
				"layer1",
				"layer2",
				NO_DEFAULT(),
				transform_genericValue(ItemLayerManifest::getFoundationLayerManifest, transform_targetLayerId()),
				inject_createTargetLayerManifest(ItemLayerManifest::setFoundationLayerId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifest#isLocalFoundationLayerManifest()}.
	 */
	@Test
	default void testIsLocalFoundationLayerManifest() {
		assertDerivativeIsLocal(settings(),
				"layer1",
				"layer2",
				ItemLayerManifest::isLocalFoundationLayerManifest,
				inject_createTargetLayerManifest(ItemLayerManifest::setFoundationLayerId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifest#isPrimaryLayerManifest()}.
	 */
	@Test
	default void testIsPrimaryLayerManifest() {
		assertFalse(createUnlocked().isPrimaryLayerManifest());

		M manifest = createTestInstance(settings(ManifestTestFeature.EMBEDDED));
		LayerGroupManifest groupManifest = assertMock(manifest.getGroupManifest());
		when(groupManifest.getPrimaryLayerManifest()).thenReturn(manifest);

		assertTrue(manifest.isPrimaryLayerManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifest#setBoundaryLayerId(java.lang.String)}.
	 */
	@Test
	default void testSetBoundaryLayerId() {
		assertLockableSetterBatch(settings(),
				ItemLayerManifest::setBoundaryLayerId,
				getLegalIdValues(), true,
				INVALID_ID_CHECK, getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifest#setFoundationLayerId(java.lang.String)}.
	 */
	@Test
	default void testSetFoundationLayerId() {
		assertLockableSetterBatch(settings(),
				ItemLayerManifest::setFoundationLayerId,
				getLegalIdValues(), true,
				INVALID_ID_CHECK, getIllegalIdValues());
	}

}
