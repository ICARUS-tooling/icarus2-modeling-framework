/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.container;

import static de.ims.icarus2.SharedTestUtils.mockSet;
import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.manifest.api.ContainerFlag;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.PostponedTest;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
@PostponedTest
class SpanItemStorageTest implements ItemStorageTest<SpanItemStorage> {

	@Override
	public Class<? extends SpanItemStorage> getTestTargetClass() {
		return SpanItemStorage.class;
	}

	@Override
	public SpanItemStorage createTestInstance(TestSettings settings) {
		SpanItemStorage storage = new SpanItemStorage();
		storage.addNotify(createContainer());
		return settings.process(storage);
	}

	@Override
	public ContainerType getExpectedContainerType() {
		return ContainerType.SPAN;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorageTest#createContainer()
	 */
	@Override
	public Container createContainer() {
		return createContext(createTargetContainer());
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

	private static Container createContext(Container target) {
		DataSet<Container> baseContainers = mockSet(target);

		Container context = mockContainer();
		when(context.getBaseContainers()).thenReturn(baseContainers);

		return context;
	}

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#SpanItemStorage()}.
		 */
		@Test
		void testSpanItemStorage() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#SpanItemStorage(long, long)}.
		 */
		@Test
		void testSpanItemStorageLongLong() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#SpanItemStorage(de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testSpanItemStorageItemItem() {
			fail("Not yet implemented"); // TODO
		}

	}

	@Nested
	class WithInstance {
		private Container target, context;
		private SpanItemStorage storage;

		@BeforeEach
		void setUp() {
			target = createTargetContainer();
			context = createContext(target);
			storage = new SpanItemStorage();
			storage.addNotify(context);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#addNotify(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testAddNotify() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#removeNotify(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testRemoveNotify() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#getItemCount(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testGetItemCount() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)}.
		 */
		@Test
		void testGetItemAt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testIndexOfItem() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testAddItem() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)}.
		 */
		@Test
		void testAddItems() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#removeItem(de.ims.icarus2.model.api.members.container.Container, long)}.
		 */
		@Test
		void testRemoveItem() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
		 */
		@Test
		void testRemoveItems() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#swapItems(de.ims.icarus2.model.api.members.container.Container, long, long)}.
		 */
		@Test
		void testSwapItems() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testGetBeginOffset() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#getEndOffset(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testGetEndOffset() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#getBeginIndex()}.
		 */
		@Test
		void testGetBeginIndex() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#getEndIndex()}.
		 */
		@Test
		void testGetEndIndex() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#setBeginIndex(long)}.
		 */
		@Test
		void testSetBeginIndex() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#setEndIndex(long)}.
		 */
		@Test
		void testSetEndIndex() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#setSpanIndices(long, long)}.
		 */
		@Test
		void testSetSpanIndices() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#recycle()}.
		 */
		@Test
		void testRecycle() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#revive()}.
		 */
		@Test
		void testRevive() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#createEditVerifier(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testCreateEditVerifier() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#isDirty(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testIsDirty() {
			fail("Not yet implemented"); // TODO
		}

	}

	@Nested
	class Internals {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#target(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testTarget() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#beginIndex()}.
		 */
		@Test
		void testBeginIndex() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#endIndex()}.
		 */
		@Test
		void testEndIndex() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#toTargetIndex(long)}.
		 */
		@Test
		void testToTargetIndex() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#fromTargetIndex(long)}.
		 */
		@Test
		void testFromTargetIndex() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#checkTargetItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testCheckTargetItem() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.container.SpanItemStorage#isEmpty()}.
		 */
		@Test
		void testIsEmpty() {
			fail("Not yet implemented"); // TODO
		}

	}

}
