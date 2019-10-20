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
package de.ims.icarus2.model.standard.members.container;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.test.TestUtils.DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertFlagGetter;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertSetter;
import static de.ims.icarus2.test.TestUtils.defaultNullCheck;
import static de.ims.icarus2.test.TestUtils.filledArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.driver.id.IdManager;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.container.ContainerTest;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerFlag;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.guard.ApiGuard;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
class DefaultContainerTest implements ContainerTest<Container> {

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	public void configureApiGuard(ApiGuard<Container> apiGuard) {
		ContainerTest.super.configureApiGuard(apiGuard);

		apiGuard.parameterResolver(ContainerManifestBase.class,
				instance -> mock(ContainerManifest.class));
		apiGuard.constructorOverride(this::create);
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends Container> getTestTargetClass() {
		return DefaultContainer.class;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.MemberTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public Container createTestInstance(TestSettings settings) {
		DefaultContainer container = new DefaultContainer();

		ContainerManifest manifest = mock(ContainerManifest.class);
		when(manifest.getContainerType()).thenReturn(ContainerType.LIST);

		ItemStorage itemStorage = mock(ItemStorage.class, defaultNullCheck());
		when(itemStorage.getContainerType()).thenReturn(ContainerType.LIST);

		container.setManifest(manifest);
		container.setItemStorage(itemStorage);

		return settings.process(container);
	}

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#DefaultContainer()}.
		 */
		@Test
		void testDefaultContainer() {
			assertNotNull(new DefaultContainer());
		}
	}

	@Nested
	class WithBareInstance {
		private DefaultContainer instance;


		@BeforeEach
		void setUp() {
			instance = new DefaultContainer();
		}


		@AfterEach
		void tearDown() {
			instance = null;
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#getMemberType()}.
		 */
		@Test
		void testGetMemberType() {
			assertEquals(MemberType.CONTAINER, instance.getMemberType());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#getBaseContainers()}.
		 */
		@Test
		void testGetBaseContainers() {
			assertNotNull(instance.getBaseContainers());
			assertTrue(instance.getBaseContainers().isEmpty());

			assertGetter(instance,
					mock(DataSet.class), mock(DataSet.class),
					DEFAULT(DataSet.emptySet()),
					DefaultContainer::getBaseContainers,
					DefaultContainer::setBaseContainers);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#setBaseContainers(de.ims.icarus2.util.collections.set.DataSet)}.
		 */
		@Test
		void testSetBaseContainers() {
			assertSetter(instance, DefaultContainer::setBaseContainers,
					mock(DataSet.class), NPE_CHECK, NO_CHECK);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#isItemsComplete()}.
		 */
		@Test
		void testIsItemsComplete() {
			assertFlagGetter(instance, Boolean.FALSE,
					DefaultContainer::isItemsComplete,
					DefaultContainer::setItemsComplete);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#setItemsComplete(boolean)}.
		 */
		@Test
		void testSetItemsComplete() {
			assertSetter(instance, DefaultContainer::setItemsComplete);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#getBoundaryContainer()}.
		 */
		@Test
		void testGetBoundaryContainer() {
			assertGetter(instance, mockContainer(), mockContainer(),
					NO_DEFAULT(),
					DefaultContainer::getBoundaryContainer,
					DefaultContainer::setBoundaryContainer);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#setBoundaryContainer(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testSetBoundaryContainer() {
			assertSetter(instance, DefaultContainer::setBoundaryContainer,
					mockContainer(), NO_NPE_CHECK, NO_CHECK);
		}

		@Nested
		class ExpectingIllegalState {

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#itemStorage()}.
			 */
			@Test
			void testItemStorage() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.itemStorage());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#getItemCount()}.
			 */
			@Test
			void testGetItemCount() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.getItemCount());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#getItemAt(long)}.
			 */
			@Test
			void testGetItemAt() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.getItemAt(1L));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#indexOfItem(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testIndexOfItem() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.indexOfItem(mockItem()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#removeItem(long)}.
			 */
			@Test
			void testRemoveItem() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.removeItem(1L));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#removeItems(long, long)}.
			 */
			@Test
			void testRemoveItems() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.removeItems(1L, 2L));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#addItem(long, de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testAddItem() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.addItem(1L, mockItem()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#addItems(long, de.ims.icarus2.util.collections.seq.DataSequence)}.
			 */
			@Test
			void testAddItems() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.addItems(1L, mockSequence()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#swapItems(long, long)}.
			 */
			@Test
			void testSwapItems() {
				assertModelException(GlobalErrorCode.ILLEGAL_STATE,
						() -> instance.swapItems(1L, 2L));
			}
		}
	}

	/** Base class for test instances that require setup environment */
	@Nested
	class WithManifestEnvironment {
		ContainerManifest manifest;
		Container host;
		ItemStorage itemStorage;

		DefaultContainer instance;

		@BeforeEach
		void setUp() {
			manifest = mock(ContainerManifest.class);
			when(manifest.getContainerType()).thenReturn(ContainerType.LIST);

			itemStorage = mock(ItemStorage.class);
			when(itemStorage.getContainerType()).thenReturn(ContainerType.LIST);

			host = mockContainer();
			when((ContainerManifest)host.getManifest()).thenReturn(manifest);
		}

		@AfterEach
		void tearDown() {
			manifest = null;
			host = null;
			itemStorage = null;
			instance = null;
		}
	}

	@Nested
	class WithCustomSetup extends WithManifestEnvironment {
		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#recycle()}.
		 */
		@Test
		void testRecycle() {
			instance = new DefaultContainer();
			instance.setManifest(manifest);
			instance.setContainer(host);

			DataSet<Container> baseContainers = mock(DataSet.class);

			instance.setBaseContainers(baseContainers);
			instance.setBoundaryContainer(mockContainer());
			instance.setItemStorage(itemStorage);

			instance.recycle();

			assertNull(instance.getBoundaryContainer());
			assertNull(instance.getItemStorage());
			assertNotSame(baseContainers, instance.getBaseContainers());
			assertTrue(instance.getBaseContainers().isEmpty());

			verify(itemStorage).removeNotify(eq(instance));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#getItemStorage()}.
		 */
		@Test
		void testGetItemStorage() {
			instance = new DefaultContainer();
			instance.setManifest(manifest);
			instance.setContainer(host);

			ItemStorage storage1 = mock(ItemStorage.class);
			when(storage1.getContainerType()).thenReturn(ContainerType.LIST);
			ItemStorage storage2 = mock(ItemStorage.class);
			when(storage2.getContainerType()).thenReturn(ContainerType.LIST);

			assertGetter(instance, storage1, storage2,
					NO_DEFAULT(),
					DefaultContainer::getItemStorage,
					DefaultContainer::setItemStorage);

			verify(storage1).addNotify(eq(instance));
			verify(storage1).removeNotify(eq(instance));
			verify(storage2).addNotify(eq(instance));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#checkItemStorage(de.ims.icarus2.model.standard.members.container.ItemStorage)}.
		 */
		@TestFactory
		Stream<DynamicTest> testCheckItemStorageIncompatible() {
			return Stream.of(ContainerType.values())
					.flatMap(typeFromManifest -> Stream.of(typeFromManifest.getIncompatibleTypes())
							.map(typeFromStorage -> dynamicTest(
									String.format("manifest=%s, storage=%s",
											typeFromManifest, typeFromStorage), () -> {

								instance = new DefaultContainer();
								instance.setManifest(manifest);
								instance.setContainer(host);

								when(manifest.getContainerType()).thenReturn(typeFromManifest);
								when(itemStorage.getContainerType()).thenReturn(typeFromStorage);

								assertModelException(ModelErrorCode.MODEL_TYPE_MISMATCH,
										() -> instance.checkItemStorage(itemStorage),
										"Incompatible types");
							})));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#checkItemStorage(de.ims.icarus2.model.standard.members.container.ItemStorage)}.
		 */
		@TestFactory
		Stream<DynamicTest> testCheckItemStorage() {
			return Stream.of(ContainerType.values())
					.flatMap(typeFromManifest -> Stream.of(typeFromManifest.getCompatibleTypes())
							.map(typeFromStorage -> dynamicTest(
									String.format("manifest=%s, storage=%s",
											typeFromManifest, typeFromStorage), () -> {

								instance = new DefaultContainer();
								instance.setManifest(manifest);
								instance.setContainer(host);

								when(manifest.getContainerType()).thenReturn(typeFromManifest);
								when(itemStorage.getContainerType()).thenReturn(typeFromStorage);

								instance.checkItemStorage(itemStorage);
							})));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#setItemStorage(de.ims.icarus2.model.standard.members.container.ItemStorage)}.
		 */
		@Test
		void testSetItemStorage() {
			instance = new DefaultContainer();
			instance.setManifest(manifest);
			instance.setContainer(host);

			assertSetter(instance, DefaultContainer::setItemStorage, itemStorage, NO_NPE_CHECK, NO_CHECK);

			verify(itemStorage).addNotify(eq(instance));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#revive()}.
		 */
		@SuppressWarnings("boxing")
		@Test
		@RandomizedTest
		void testRevive(RandomGenerator rng) {
			IdManager idManager = mock(IdManager.class);
			ItemLayer layer = mock(ItemLayer.class);

			when(host.getLayer()).thenReturn(layer);
			when(layer.getIdManager()).thenReturn(idManager);

			long id = Math.max(1L, rng.nextLong());
			long index = Math.max(1L, rng.nextLong());

			instance = new DefaultContainer();
			instance.setManifest(manifest);
			instance.setContainer(host);
			instance.setId(id);

			when(idManager.indexOfId(anyLong())).thenReturn(IcarusUtils.UNSET_LONG);

			assertFalse(instance.revive());

			when(idManager.indexOfId(eq(id))).thenReturn(index);

			assertFalse(instance.revive());

			instance.setItemStorage(itemStorage);

			assertTrue(instance.revive());
		}
	}

	@Nested
	class WithPresetStorage extends WithManifestEnvironment {

		@Override
		@BeforeEach
		void setUp() {
			super.setUp();
			instance = new DefaultContainer();
			instance.setManifest(manifest);
			instance.setContainer(host);
			instance.setItemStorage(itemStorage);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#isDirty()}.
		 */
		@SuppressWarnings("boxing")
		@Test
		void testIsDirty() {
			when(itemStorage.isDirty(any())).thenReturn(false);

			assertFalse(instance.isDirty());

			instance.setDirty(true);
			assertTrue(instance.isDirty());

			instance.setDirty(false);
			when(itemStorage.isDirty(any())).thenReturn(true);
			assertTrue(instance.isDirty());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#itemStorage()}.
		 */
		@Test
		void testItemStorage() {
			assertSame(itemStorage, instance.itemStorage());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#createEditVerifier()}.
		 */
		@SuppressWarnings({ "boxing" })
		@Test
		void testCreateEditVerifier() {
			when(manifest.isContainerFlagSet(any())).thenReturn(false);

			ContainerEditVerifier verifierForStatic = instance.createEditVerifier();
			assertNotNull(verifierForStatic);
			assertFalse(verifierForStatic.isAllowEdits());


			ContainerEditVerifier verifier = mock(ContainerEditVerifier.class);
			when(itemStorage.createEditVerifier(eq(instance))).thenReturn(verifier);
			when(manifest.isContainerFlagSet(ContainerFlag.NON_STATIC)).thenReturn(true);
			ContainerEditVerifier verifierForEditable = instance.createEditVerifier();
			assertSame(verifier, verifierForEditable);
			verify(itemStorage).createEditVerifier(eq(instance));
		}

		@Nested
		class ExpectStorageDelegation {

			private final int RUNS = 10;

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#getBeginOffset()}.
			 */
			@SuppressWarnings("boxing")
			@Test
			void testGetBeginOffset() {
				when(itemStorage.getBeginOffset(eq(instance))).thenReturn(IcarusUtils.UNSET_LONG, 0L, 1L, Long.MAX_VALUE);

				assertEquals(IcarusUtils.UNSET_LONG, instance.getBeginOffset());
				assertEquals(0L, instance.getBeginOffset());
				assertEquals(1L, instance.getBeginOffset());
				assertEquals(Long.MAX_VALUE, instance.getBeginOffset());

				verify(itemStorage, times(4)).getBeginOffset(eq(instance));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#getEndOffset()}.
			 */
			@SuppressWarnings("boxing")
			@Test
			void testGetEndOffset() {
				when(itemStorage.getEndOffset(eq(instance))).thenReturn(IcarusUtils.UNSET_LONG, 0L, 1L, Long.MAX_VALUE);

				assertEquals(IcarusUtils.UNSET_LONG, instance.getEndOffset());
				assertEquals(0L, instance.getEndOffset());
				assertEquals(1L, instance.getEndOffset());
				assertEquals(Long.MAX_VALUE, instance.getEndOffset());

				verify(itemStorage, times(4)).getEndOffset(eq(instance));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#getManifest()}.
			 */
			@Test
			void testGetManifest() {
				assertSame(manifest, instance.getManifest());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#getContainerType()}.
			 */
			@TestFactory
			Stream<DynamicTest> testGetContainerType() {
				return Stream.of(ContainerType.values())
						.map(containerType -> dynamicTest(containerType.name(), () -> {
							when(itemStorage.getContainerType()).thenReturn(containerType);
							assertEquals(containerType, instance.getContainerType());
						}));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#clearItemStorage()}.
			 */
			@Test
			void testClearItemStorage() {
				instance.clearItemStorage();

				assertNull(instance.getItemStorage());

				verify(itemStorage).removeNotify(eq(instance));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#getItemCount()}.
			 */
			@SuppressWarnings("boxing")
			@Test
			void testGetItemCount() {
				when(itemStorage.getItemCount(eq(instance))).thenReturn(0L, 1L, Long.MAX_VALUE);

				assertEquals(0L, instance.getItemCount());
				assertEquals(1L, instance.getItemCount());
				assertEquals(Long.MAX_VALUE, instance.getItemCount());

				verify(itemStorage, times(3)).getItemCount(eq(instance));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#getItemAt(long)}.
			 */
			@Test
			@RandomizedTest
			void testGetItemAt(RandomGenerator rng) {
				Item[] items = filledArray(RUNS, Item.class);
				long[] indices = rng.longs(RUNS, 0, Long.MAX_VALUE).toArray();

				IntStream.range(0, RUNS).forEach(
						idx -> when(itemStorage.getItemAt(eq(instance), eq(indices[idx])))
							.thenReturn(items[idx]));

				IntStream.range(0, RUNS).forEach(
						idx -> assertEquals(items[idx], instance.getItemAt(indices[idx])));


				IntStream.range(0, RUNS).forEach(
						idx -> verify(itemStorage).getItemAt(eq(instance), eq(indices[idx])));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#indexOfItem(de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@SuppressWarnings("boxing")
			@Test
			@RandomizedTest
			void testIndexOfItem(RandomGenerator rng) {
				Item[] items = filledArray(RUNS, Item.class);
				long[] indices = rng.longs(RUNS, 0, Long.MAX_VALUE).toArray();

				IntStream.range(0, RUNS).forEach(
						idx -> when(itemStorage.indexOfItem(eq(instance), eq(items[idx])))
							.thenReturn(indices[idx]));

				IntStream.range(0, RUNS).forEach(
						idx -> assertEquals(indices[idx], instance.indexOfItem(items[idx])));

				IntStream.range(0, RUNS).forEach(
						idx -> verify(itemStorage).indexOfItem(eq(instance), eq(items[idx])));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#removeItem(long)}.
			 */
			@Test
			@RandomizedTest
			void testRemoveItem(RandomGenerator rng) {
				long[] indices = rng.longs(20, 0, Long.MAX_VALUE).toArray();

				LongStream.of(indices).forEach(instance::removeItem);

				LongStream.of(indices).forEach(index -> verify(itemStorage).removeItem(eq(instance), eq(index)));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#removeItems(long, long)}.
			 */
			@SuppressWarnings({ "unchecked", "boxing" })
			@Test
			@RandomizedTest
			void testRemoveItems(RandomGenerator rng) {
				@SuppressWarnings("rawtypes")
				Pair[] indices = {
					rng.randomLongPair(0, Long.MAX_VALUE),
					rng.randomLongPair(0, Long.MAX_VALUE),
					rng.randomLongPair(0, Long.MAX_VALUE),
				};
				@SuppressWarnings("rawtypes")
				DataSequence[] items = filledArray(indices.length, DataSequence.class);
				IntStream.range(0, indices.length).forEach(
						idx -> {
							Pair<Long, Long> p = indices[idx];
							when(itemStorage.removeItems(eq(instance), eq(p.first), eq(p.second)))
								.thenReturn(items[idx]);
							});
				IntStream.range(0, indices.length).forEach(
						idx -> {
							Pair<Long, Long> p = indices[idx];
							assertEquals(items[idx], instance.removeItems(p.first, p.second));
							});

				IntStream.range(0, indices.length).forEach(
						idx -> {
							Pair<Long, Long> p = indices[idx];
							verify(itemStorage).removeItems(eq(instance), eq(p.first), eq(p.second));
						});
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#addItem(long, de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			@RandomizedTest
			void testAddItem(RandomGenerator rng) {
				Item[] items = filledArray(RUNS, Item.class);
				long[] indices = rng.longs(RUNS, 0, Long.MAX_VALUE).toArray();

				IntStream.range(0, RUNS).forEach(
						idx -> instance.addItem(indices[idx], items[idx]));

				IntStream.range(0, RUNS).forEach(
						idx -> verify(itemStorage).addItem(eq(instance), eq(indices[idx]), eq(items[idx])));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#addItems(long, de.ims.icarus2.util.collections.seq.DataSequence)}.
			 */
			@SuppressWarnings("unchecked")
			@Test
			@RandomizedTest
			void testAddItems(RandomGenerator rng) {
				@SuppressWarnings("rawtypes")
				DataSequence[] items = filledArray(RUNS, DataSequence.class);
				long[] indices = rng.longs(RUNS, 0, Long.MAX_VALUE).toArray();

				IntStream.range(0, RUNS).forEach(
						idx -> instance.addItems(indices[idx], items[idx]));

				IntStream.range(0, RUNS).forEach(
						idx -> verify(itemStorage).addItems(eq(instance), eq(indices[idx]), eq(items[idx])));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.DefaultContainer#swapItems(long, long)}.
			 */
			@SuppressWarnings("boxing")
			@Test
			@RandomizedTest
			void testSwapItems(RandomGenerator rng) {
				@SuppressWarnings("rawtypes")
				Pair[] indices = {
					rng.randomLongPair(0, Long.MAX_VALUE),
					rng.randomLongPair(0, Long.MAX_VALUE),
					rng.randomLongPair(0, Long.MAX_VALUE),
					rng.randomLongPair(0, Long.MAX_VALUE),
				};
				IntStream.range(0, indices.length).forEach(
						idx -> {
							@SuppressWarnings("unchecked")
							Pair<Long, Long> p = indices[idx];
							instance.swapItems(p.first, p.second);
							});

				IntStream.range(0, indices.length).forEach(
						idx -> {
							@SuppressWarnings("unchecked")
							Pair<Long, Long> p = indices[idx];
							verify(itemStorage).swapItems(eq(instance), eq(p.first), eq(p.second));
						});
			}
		}
	}

}
