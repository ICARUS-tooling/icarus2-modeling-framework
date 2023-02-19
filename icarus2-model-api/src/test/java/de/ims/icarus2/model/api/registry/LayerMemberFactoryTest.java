/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.registry;

import static de.ims.icarus2.SharedTestUtils.mockSet;
import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockPosition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.manifest.api.ContainerFlag;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
public interface LayerMemberFactoryTest<F extends LayerMemberFactory>
		extends ApiGuardedTest<F>{

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.LayerMemberFactory#newContainer(de.ims.icarus2.model.manifest.api.ContainerManifestBase, de.ims.icarus2.model.api.members.container.Container, long)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	@RandomizedTest
	default Stream<DynamicTest> testNewContainer(RandomGenerator rand) {
		return Stream.of(ContainerType.values())
			.map(containerType -> dynamicTest(containerType.name(), () -> {
				ContainerManifest manifest = mock(ContainerManifest.class);
				when(manifest.getContainerType()).thenReturn(containerType);

				Container host = mock(Container.class);
				when((ContainerManifest)host.getManifest()).thenReturn(manifest);

				// Make sure we have a single static base container (some implementations might need that)
				ContainerManifest baseManifest = mock(ContainerManifest.class);
				when(baseManifest.isContainerFlagSet(ContainerFlag.NON_STATIC)).thenReturn(Boolean.FALSE);
				Container baseContainer = mockContainer();
				when((ContainerManifest)baseContainer.getManifest()).thenReturn(baseManifest);
				DataSet<Container> baseContainers = mockSet(baseContainer);

				long id = rand.randomId();

				Container container = create().newContainer(manifest, host, baseContainers, null, id);
				assertNotNull(container);

				assertTrue(containerType.isCompatibleWith(container.getContainerType()));
				assertSame(manifest, container.getManifest());
				assertSame(host, container.getContainer());
				assertSame(baseContainers, container.getBaseContainers());
				assertNull(container.getBoundaryContainer());
				assertEquals(id, container.getId());
			}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.LayerMemberFactory#newStructure(de.ims.icarus2.model.manifest.api.StructureManifest, de.ims.icarus2.model.api.members.container.Container, long)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	@RandomizedTest
	default Stream<DynamicTest> testNewStructure(RandomGenerator rand) {
		return Stream.of(StructureType.values())
				.flatMap(structureType -> Stream.of(ContainerType.values())
						.map(containerType -> dynamicTest(containerType+"+"+structureType, () -> {
					StructureManifest manifest = mock(StructureManifest.class);
					when(manifest.getContainerType()).thenReturn(containerType);
					when(manifest.getStructureType()).thenReturn(structureType);

					Container host = mock(Container.class);
					when((StructureManifest)host.getManifest()).thenReturn(manifest);

					// Make sure we have a single static base container (some implementations might need that)
					ContainerManifest baseManifest = mock(ContainerManifest.class);
					when(baseManifest.isContainerFlagSet(ContainerFlag.NON_STATIC)).thenReturn(Boolean.FALSE);
					Container baseContainer = mockContainer();
					when((ContainerManifest)baseContainer.getManifest()).thenReturn(baseManifest);
					DataSet<Container> baseContainers = mockSet(baseContainer);

					long id = rand.randomId();

					Structure structure = create().newStructure(manifest, host, baseContainers, null, id);
					assertNotNull(structure);

					assertTrue(structureType.isCompatibleWith(structure.getStructureType()));
					assertSame(manifest, structure.getManifest());
					assertSame(host, structure.getContainer());
					assertSame(baseContainers, structure.getBaseContainers());
					assertNull(structure.getBoundaryContainer());
					assertEquals(id, structure.getId());
				})));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.LayerMemberFactory#newItem(de.ims.icarus2.model.api.members.container.Container, long)}.
	 */
	@Test
	@RandomizedTest
	default void testNewItem(RandomGenerator rand) {
		Container host = mock(Container.class);
		long id = rand.randomId();

		Item item = create().newItem(host, id);

		assertSame(host, item.getContainer());
		assertEquals(id, item.getId());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.LayerMemberFactory#newEdge(de.ims.icarus2.model.api.members.structure.Structure, long)}.
	 */
	@Test
	@RandomizedTest
	default void testNewEdgeStructureLong(RandomGenerator rand) {
		Structure host = mock(Structure.class);
		long id = rand.randomId();

		Edge edge = create().newEdge(host, id);

		assertSame(host, edge.getContainer());
		assertSame(host, edge.getStructure());
		assertEquals(id, edge.getId());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.LayerMemberFactory#newEdge(de.ims.icarus2.model.api.members.structure.Structure, long, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	@RandomizedTest
	default void testNewEdgeStructureLongItemItem(RandomGenerator rand) {
		Structure host = mock(Structure.class);
		Item source = mockItem();
		Item target = mockItem();
		long id = rand.randomId();

		Edge edge = create().newEdge(host, id, source, target);

		assertSame(host, edge.getContainer());
		assertSame(host, edge.getStructure());
		assertSame(source, edge.getSource());
		assertSame(target, edge.getTarget());
		assertEquals(id, edge.getId());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.LayerMemberFactory#newFragment(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	@RandomizedTest
	default void testNewFragmentContainerLongItem(RandomGenerator rand) {
		Container host = mock(Container.class);
		long id = rand.randomId();
		Item item = mockItem();

		Fragment fragment = create().newFragment(host, id, item);

		assertSame(host, fragment.getContainer());
		assertSame(item, fragment.getItem());
		assertEquals(id, fragment.getId());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.registry.LayerMemberFactory#newFragment(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.raster.Position, de.ims.icarus2.model.api.raster.Position)}.
	 */
	@Test
	@RandomizedTest
	default void testNewFragmentContainerLongItemPositionPosition(RandomGenerator rand) {
		Container host = mock(Container.class);
		long id = rand.randomId();
		Item item = mockItem();
		Position position1 = mockPosition();
		Position position2 = mockPosition();

		Fragment fragment = create().newFragment(host, id, item, position1, position2);

		assertSame(host, fragment.getContainer());
		assertSame(item, fragment.getItem());
		assertEquals(id, fragment.getId());
		assertSame(position1, fragment.getFragmentBegin());
		assertSame(position2, fragment.getFragmentEnd());
	}

}
