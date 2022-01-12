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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.getIllegalIdValues;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.getLegalIdValues;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubId;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubType;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.transform_id;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.inject_consumeTargetLayerManifest;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.inject_createTargetLayerManifest;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.transform_targetLayerId;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.assertMock;
import static de.ims.icarus2.test.TestUtils.assertOptGetter;
import static de.ims.icarus2.test.TestUtils.assertPredicate;
import static de.ims.icarus2.test.TestUtils.assertPresent;
import static de.ims.icarus2.test.TestUtils.constant;
import static de.ims.icarus2.test.TestUtils.inject_genericInserter;
import static de.ims.icarus2.test.TestUtils.settings;
import static de.ims.icarus2.test.TestUtils.transform_genericOptValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestFeature;
import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.standard.AbstractItemLayerManifestBase;
import de.ims.icarus2.test.func.TriConsumer;

/**
 * @author Markus Gärtner
 *
 */
interface ItemLayerManifestTestMixin<M extends ItemLayerManifestBase<M>> extends LayerManifestTest<M> {

	public static ContainerManifest mockContainerManifest(String id) {
		ContainerManifest containerManifest = mockTypedManifest(ManifestType.CONTAINER_MANIFEST);
		return stubType(stubId(containerManifest, id),
				ManifestType.CONTEXT_MANIFEST);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.ITEM_LAYER_MANIFEST;
	}

	@SuppressWarnings("boxing")
	public static <M extends ItemLayerManifestBase<?>, V extends ContainerManifestBase<?>> TriConsumer<M, V, Integer>
			inject_insert() {
		return (m, cont, index) -> {
			AbstractItemLayerManifestBase.getOrCreateLocalContainerhierarchy(m)
				.insert(cont, index);
		};
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#getRootContainerManifest()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testGetRootContainerManifest() {
		ContainerManifest root = mockContainerManifest("root");
		ContainerManifest container1 = mockContainerManifest("container1");
		ContainerManifest container2 = mockContainerManifest("container2");

		// Test basic behavior when constantly changing the root manifest itself
		assertOptGetter(createUnlocked(),
				container1, container2, null,
				ItemLayerManifestBase::getRootContainerManifest,
				inject_genericInserter(inject_insert(), constant(0)));

		Predicate<M> rootCheck = m -> {
			Optional<ContainerManifestBase<?>> manifest = m.getRootContainerManifest();
			assertPresent(manifest);
			return manifest.get()==root;
		};

		BiFunction<M, ContainerManifest, Boolean> staticModifier = (m, cont) -> {
			AbstractItemLayerManifestBase.getOrCreateLocalContainerhierarchy(m).add(cont);
			return true;
		};

		// Test with simply adding more containers after initial root
		assertPredicate(createUnlocked(), staticModifier, rootCheck, transform_id(),
				root, container1, container2);

		BiFunction<M, ContainerManifestBase<?>, Boolean> mixedModifier = (m, cont) -> {
			AbstractItemLayerManifestBase.getOrCreateLocalContainerhierarchy(m).insert(cont, 0);
			return cont==root;
		};

		// Test with shifting the root manifest
		assertPredicate(createUnlocked(), mixedModifier, rootCheck, transform_id(),
				root, container1, container2);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#hasLocalContainerHierarchy()}.
	 */
	@Test
	default void testIsLocalContainerHierarchy() {
		assertDerivativeIsLocal(settings(),
				mock(Hierarchy.class), mock(Hierarchy.class),
				ItemLayerManifestBase::hasLocalContainerHierarchy,
				ItemLayerManifestBase::setContainerHierarchy);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#getBoundaryLayerManifest()}.
	 */
	@Test
	default void testGetBoundaryLayerManifest() {
		assertDerivativeOptGetter(settings(),
				"layer1",
				"layer2",
				NO_DEFAULT(),
				transform_genericOptValue(ItemLayerManifestBase::getBoundaryLayerManifest, transform_targetLayerId()),
				inject_createTargetLayerManifest(ItemLayerManifestBase::setAndGetBoundaryLayer));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifest#isLocalBoundaryLayerManifest()}.
	 */
	@Test
	default void testIsLocalBoundaryLayerManifest() {
		assertDerivativeIsLocal(settings(),
				"layer1",
				"layer2",
				ItemLayerManifestBase::isLocalBoundaryLayerManifest,
				inject_createTargetLayerManifest(ItemLayerManifestBase::setAndGetBoundaryLayer));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#getFoundationLayerManifest()}.
	 */
	@Test
	default void testGetFoundationLayerManifest() {
		assertDerivativeOptGetter(settings(),
				"layer1",
				"layer2",
				NO_DEFAULT(),
				transform_genericOptValue(ItemLayerManifestBase::getFoundationLayerManifest, transform_targetLayerId()),
				inject_createTargetLayerManifest(ItemLayerManifestBase::setAndGetFoundationLayer));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#isLocalFoundationLayerManifest()}.
	 */
	@Test
	default void testIsLocalFoundationLayerManifest() {
		assertDerivativeIsLocal(settings(),
				"layer1",
				"layer2",
				ItemLayerManifestBase::isLocalFoundationLayerManifest,
				inject_createTargetLayerManifest(ItemLayerManifestBase::setAndGetFoundationLayer));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#isPrimaryLayerManifest()}.
	 */
	@Test
	default void testIsPrimaryLayerManifest() {
		assertFalse(createUnlocked().isPrimaryLayerManifest());

		M manifest = createTestInstance(settings(ManifestTestFeature.EMBEDDED));
		LayerGroupManifest groupManifest = assertMock(manifest.getGroupManifest());
		when(groupManifest.getPrimaryLayerManifest()).thenReturn(Optional.of(manifest));

		assertTrue(manifest.isPrimaryLayerManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#setAndGetBoundaryLayer(java.lang.String)}.
	 */
	@Test
	default void testSetAndGetBoundaryLayer() {
		assertLockableSetterBatch(settings(),
				ItemLayerManifestBase::setAndGetBoundaryLayer,
				getLegalIdValues(), true,
				ManifestTestUtils.INVALID_ID_CHECK, getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#setBoundaryLayerId(String, java.util.function.Consumer)}.
	 */
	@Test
	default void testSetBoundaryLayerId() {
		assertLockableSetterBatch(settings(),
				inject_consumeTargetLayerManifest(ItemLayerManifestBase::setBoundaryLayerId),
				getLegalIdValues(), true,
				ManifestTestUtils.INVALID_ID_CHECK, getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#setAndGetFoundationLayer(java.lang.String)}.
	 */
	@Test
	default void testSetAndGetFoundationLayer() {
		assertLockableSetterBatch(settings(),
				ItemLayerManifestBase::setAndGetFoundationLayer,
				getLegalIdValues(), true,
				ManifestTestUtils.INVALID_ID_CHECK, getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ItemLayerManifestBase#setFoundationLayerId(String, java.util.function.Consumer)}.
	 */
	@Test
	default void testSetFoundationLayerId() {
		assertLockableSetterBatch(settings(),
				inject_consumeTargetLayerManifest(ItemLayerManifestBase::setFoundationLayerId),
				getLegalIdValues(), true,
				ManifestTestUtils.INVALID_ID_CHECK, getIllegalIdValues());
	}

}
