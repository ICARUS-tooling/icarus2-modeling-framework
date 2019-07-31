/**
 *
 */
package de.ims.icarus2.model.standard.members.container;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.test.TestUtils.IGNORE_DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertSetter;
import static de.ims.icarus2.test.TestUtils.randomId;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerFlag;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class WrappingItemStorageTest implements ItemStorageTest<WrappingItemStorage> {

	@Override
	public Class<? extends WrappingItemStorage> getTestTargetClass() {
		return WrappingItemStorage.class;
	}

	@Override
	public WrappingItemStorage createTestInstance(TestSettings settings) {
		return settings.process(new WrappingItemStorage(createTargetContainer()));
	}

	@Override
	public ContainerType getExpectedContainerType() {
		return null;
	}

	@SuppressWarnings("boxing")
	private static Container createTargetContainer() {
		ContainerManifest manifest = mock(ContainerManifest.class);
		when(manifest.isContainerFlagSet(ContainerFlag.NON_STATIC)).thenReturn(Boolean.FALSE);
		Container container = mockContainer();
		when((ContainerManifest)container.getManifest()).thenReturn(manifest);
		when(container.getContainerType()).thenReturn(ContainerType.LIST);

		when(container.indexOfItem(eq(null))).thenThrow(new NullPointerException());

		return container;
	}

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#WrappingItemStorage(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testWrappingItemStorage() {
			Container target = createTargetContainer();
			WrappingItemStorage storage = new WrappingItemStorage(target);
			assertSame(target, storage.getSourceContainer());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#WrappingItemStorage(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@SuppressWarnings("boxing")
		@Test
		void testWrappingItemStorage_NonStaticTarget() {
			ContainerManifest manifest = mock(ContainerManifest.class);
			when(manifest.isContainerFlagSet(ContainerFlag.NON_STATIC)).thenReturn(Boolean.TRUE);
			Container container = mockContainer();
			when((ContainerManifest)container.getManifest()).thenReturn(manifest);

			assertModelException(ModelErrorCode.MODEL_ILLEGAL_LINKING,
					() -> new WrappingItemStorage(container));
		}

	}

	@Nested
	class WithInstance {
		private WrappingItemStorage storage;

		@BeforeEach
		void setUp() {
			storage = create();
		}

		@AfterEach
		void tearDown() {
			storage = null;
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#recycle()}.
		 */
		@Test
		void testRecycle() {
			storage.recycle();
			assertNull(storage.getSourceContainer());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#revive()}.
		 */
		@Test
		void testRevive() {
			assertTrue(storage.revive());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#revive()}.
		 */
		@Test
		void testReviveAfterRecycle() {
			storage.recycle();
			assertFalse(storage.revive());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#revive()}.
		 */
		@Test
		void testReviveWithReset() {
			storage.recycle();
			storage.setSourceContainer(createTargetContainer());
			assertTrue(storage.revive());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#getSourceContainer()}.
		 */
		@Test
		void testGetSourceContainer() {
			assertGetter(create(),
					createTargetContainer(),
					createTargetContainer(),
					IGNORE_DEFAULT(),
					WrappingItemStorage::getSourceContainer,
					WrappingItemStorage::setSourceContainer);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#setSourceContainer(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testSetSourceContainer() {
			assertSetter(create(),
					WrappingItemStorage::setSourceContainer,
					createTargetContainer(),
					NPE_CHECK,
					NO_CHECK);
		}
	}

	@Nested
	class WithSource {
		private WrappingItemStorage storage;
		private Container source;

		@BeforeEach
		void setUp() {
			source = createTargetContainer();
			storage = new WrappingItemStorage(source);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#getSourceContainer()}.
		 */
		@Test
		void testGetSourceContainer() {
			assertSame(source, storage.getSourceContainer());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#getContainerType()}.
		 */
		@Test
		void testGetContainerType() {
			storage.getContainerType();
			verify(source).getContainerType();
		}

		@Nested
		class WithContext {
			private Container context;

			@BeforeEach
			void setUp() {
				context = mockContainer();
				storage.addNotify(context);
			}

			@AfterEach
			void tearDown() {
				context = null;
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#getItemCount(de.ims.icarus2.model.api.members.container.Container)}.
			 */
			@Test
			void testGetItemCount() {
				storage.getItemCount(context);
				verify(source).getItemCount();
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)}.
			 */
			@Test
			void testGetItemAt() {
				long index = randomId();
				storage.getItemAt(context, index);
				verify(source).getItemAt(index);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testIndexOfItem() {
				Item item = mockItem();
				storage.indexOfItem(context, item);
				verify(source).indexOfItem(item);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)}.
			 */
			@Test
			void testAddItem() {
				assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
						() -> storage.addItem(context, randomId(), mockItem()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)}.
			 */
			@Test
			void testAddItems() {
				assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
						() -> storage.addItems(context, randomId(), mockSequence()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#removeItem(de.ims.icarus2.model.api.members.container.Container, long)}.
			 */
			@Test
			void testRemoveItem() {
				assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
						() -> storage.removeItem(context, randomId()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
			 */
			@Test
			void testRemoveItems() {
				assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
						() -> storage.removeItems(context, randomId(), randomId()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#swapItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
			 */
			@Test
			void testSwapItems() {
				assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
						() -> storage.swapItems(context, randomId(), randomId()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)}.
			 */
			@Test
			void testGetBeginOffset() {
				storage.getBeginOffset(context);
				verify(source).getBeginOffset();
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#getEndOffset(de.ims.icarus2.model.api.members.container.Container)}.
			 */
			@Test
			void testGetEndOffset() {
				storage.getEndOffset(context);
				verify(source).getEndOffset();
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#createEditVerifier(de.ims.icarus2.model.api.members.container.Container)}.
			 */
			@Test
			void testCreateEditVerifier() {
				assertNotNull(storage.createEditVerifier(context));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.container.WrappingItemStorage#isDirty(de.ims.icarus2.model.api.members.container.Container)}.
			 */
			@Test
			void testIsDirty() {
				storage.isDirty(context);
				verify(source).isDirty();
			}
		}
	}

}
