/*
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
package de.ims.icarus2.model.standard.view.streamed;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.standard.driver.virtual.VirtualItemLayerManager;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
class ItemStreamBufferTest implements ApiGuardedTest<ItemStreamBuffer> {

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public ItemStreamBuffer createTestInstance(TestSettings settings) {
		ItemLayer layer = mock(ItemLayer.class, CALLS_REAL_METHODS);
		ItemLayerManager itemLayerManager = new VirtualItemLayerManager();
		int capacity = 1000;

		return settings.process(new ItemStreamBuffer(itemLayerManager, layer, capacity));
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends ItemStreamBuffer> getTestTargetClass() {
		return ItemStreamBuffer.class;
	}

	@Nested
	class WithSetup {
		private VirtualItemLayerManager itemLayerManager;
		private ItemStreamBuffer buffer;

		private final int capacity = 100;

		@SuppressWarnings("boxing")
		@BeforeEach
		void setUp() {
			ItemLayerManifest manifest = mock(ItemLayerManifest.class);
			when(manifest.getUID()).thenReturn(1);
			ItemLayer layer = mock(ItemLayer.class);
			when((ItemLayerManifest)layer.getManifest()).thenReturn(manifest);

			itemLayerManager = new VirtualItemLayerManager(
					VirtualItemLayerManager.IGNORE_MISSING_ITEMS);
			itemLayerManager.addLayer(layer);

			itemLayerManager = spy(itemLayerManager);

			buffer = new ItemStreamBuffer(itemLayerManager, layer, capacity);
		}

		@AfterEach
		void tearDown() {
			buffer.close();
			itemLayerManager.clear();

			buffer = null;
			itemLayerManager = null;
		}

		@Nested
		class WhenEmpty {

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#hasItem()}.
			 */
			@Test
			void testHasItemInitial() {
				assertFalse(buffer.hasItem());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#currentItem()}.
			 */
			@Test
			void testCurrentItemInitial() {
				assertNull(buffer.currentItem());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#advance()}.
			 */
			@Test
			void testAdvanceInitial() {
				assertFalse(buffer.advance());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#close()}.
			 */
			@Test
			void testClose() {
				// This only verifies idempotence of the close() method
				buffer.close();
			}
		}

		@Nested
		class WhenFilled {

			private void fillManager(int itemCount) {
				while(itemCount-->0) {
					itemLayerManager.addItem(buffer.getLayer(), mockItem());
				}
			}

			private final int segments = 3;

			private final int size = capacity*segments;

			@BeforeEach
			void setUp() {
				fillManager(size);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#hasItem()}.
			 */
			@Test
			void testHasItemInitial() {
				assertFalse(buffer.hasItem());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#currentItem()}.
			 */
			@Test
			void testCurrentItemInitial() {
				assertNull(buffer.currentItem());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#currentItem()}.
			 */
			@Test
			void testCurrentItemFull() {
				for(int i=0; i<size; i++) {
					buffer.advance();
					assertSame(itemLayerManager.getItem(buffer.getLayer(), i), buffer.currentItem(),
							String.format("Failed to advance for item %d of %d", _int(i+1), _int(size)));
				}
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#advance()}.
			 */
			@Test
			void testAdvanceInitial() {
				assertTrue(buffer.advance());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#advance()}.
			 */
			@Test
			void testAdvanceFull() throws Exception {
				for(int i=0; i<size; i++) {
					assertTrue(buffer.advance(), String.format(
							"Failed to advance for item %d of %d", _int(i+1), _int(size)));
				}

				// Check that EoS gets reported properly
				assertFalse(buffer.advance());
				// Check that the advance() method stays idempotent after EoS is reached
				assertFalse(buffer.advance());
				assertFalse(buffer.advance());

				// Check that we actually had to load a new chunk 'segment' times
				verify(itemLayerManager, times(segments)).load(any(), eq(buffer.getLayer()), any());
				// Check that we actually allowed a matching number of chunks to be released
				verify(itemLayerManager, times(segments)).release(any(), eq(buffer.getLayer()));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#close()}.
			 */
			@Test
			void testClose() {
				// This only verifies idempotence of the close() method
				buffer.close();
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#mark()}.
			 */
			@Test
			void testMarkInitial() {
				assertThrows(IllegalStateException.class, () -> buffer.mark());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#mark()}.
			 */
			@Test
			void testMarkFull() {
				for(int i=0; i<size; i++) {
					buffer.advance();
					buffer.mark();
				}
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#hasMark()}.
			 */
			@Test
			void testHasMarkInitial() {
				assertFalse(buffer.hasMark());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#hasMark()}.
			 */
			@Test
			void testHasMarkFull() {
				for(int i=0; i<size; i++) {
					buffer.advance();
					buffer.mark();
					assertTrue(buffer.hasMark());
				}
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#clearMark()}.
			 */
			@Test
			void testClearMarkFull() {
				for(int i=0; i<size; i++) {
					buffer.advance();
					buffer.mark();
					assertTrue(buffer.hasMark());
					buffer.clearMark();
					assertFalse(buffer.hasMark());
				}
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#reset()}.
			 */
			@Test
			void testResetInitial() {
				assertThrows(IllegalStateException.class, () -> buffer.reset());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#reset()}.
			 */
			@RepeatedTest(RUNS)
			@RandomizedTest
			void testResetFull(RandomGenerator rng) {

				// Go to random index
				int markedIndex = rng.random(0, size/2);
				int index = 0;
				while(index++<=markedIndex) {
					buffer.advance();
				}

				// Mark position
				buffer.mark();
				Item item = buffer.currentItem();
				assertNotNull(item);
				assertTrue(buffer.hasMark());

				// Move random distance away
				int distance = rng.random(2, size-markedIndex);
				while(distance-->0 && !buffer.wouldInvalidateMark() && buffer.advance()) {
					// Just moving the cursor away as much as possible
				}

				// Reset back to mark
				buffer.reset();
				assertFalse(buffer.hasMark());
				assertTrue(buffer.hasItem());
				assertSame(item, buffer.currentItem());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#wouldInvalidateMark()}.
			 */
			@Test
			void testWouldInvalidateMarkInitial() {
				assertFalse(buffer.wouldInvalidateMark());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#wouldInvalidateMark()}.
			 */
			@RepeatedTest(RUNS)
			@RandomizedTest
			void testWouldInvalidateMarkInitialFull(RandomGenerator rng) {
				int markedIndex = rng.random(0, capacity);
				for (int i = 0; i < capacity; i++) {
					assertFalse(buffer.wouldInvalidateMark());
					buffer.advance();
					if(i==markedIndex) {
						buffer.mark();
					}
				}
				assertTrue(buffer.hasMark());
				assertTrue(buffer.wouldInvalidateMark());

				buffer.advance();
				assertFalse(buffer.hasMark());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#flush()}.
			 */
			@RepeatedTest(RUNS)
			@RandomizedTest
			void testFlush(RandomGenerator rng) {
				// Create random mark
				int markedIndex = rng.random(0, capacity);
				for (int i = 0; i < capacity; i++) {
					buffer.advance();
					if(i==markedIndex) {
						buffer.mark();
					}
				}
				assertTrue(buffer.hasMark());

				// Move random distance away
				int distance = rng.random(2, size-markedIndex);
				while(distance-->0 && !buffer.wouldInvalidateMark() && buffer.advance()) {
					// Just moving the cursor away as much as possible
				}
				assertTrue(buffer.hasMark());

				Item item = buffer.currentItem();
				buffer.flush();

				assertTrue(buffer.hasItem());
				assertFalse(buffer.hasMark());
				assertSame(item, buffer.currentItem());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#skip(long)}.
			 */
			@RepeatedTest(RUNS)
			@RandomizedTest
			void testSkipWithoutLossOfMark(RandomGenerator rng) {
				// Move to random spot
				int initialSteps = rng.random(capacity/4, capacity/2);
				int markedIndex = rng.random(0, capacity/4);
				for (int i = 0; i < initialSteps; i++) {
					buffer.advance();
					if(i==markedIndex) {
						buffer.mark();
					}
				}
				assertTrue(buffer.hasMark());

				// Now skip random number of items within current chunk
				int distance = rng.random(1, capacity-initialSteps);
				buffer.skip(distance);

				assertTrue(buffer.hasMark());
				assertTrue(buffer.hasItem());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.view.streamed.ItemStreamBuffer#skip(long)}.
			 */
			@RepeatedTest(RUNS)
			@RandomizedTest
			void testSkipWithLossOfMark(RandomGenerator rng) {
				// Move to random spot
				int initialSteps = rng.random(capacity/4, capacity/2);
				int markedIndex = rng.random(0, capacity/4);
				for (int i = 0; i < initialSteps; i++) {
					buffer.advance();
					if(i==markedIndex) {
						buffer.mark();
					}
				}
				assertTrue(buffer.hasMark());

				// Now skip random number of items within current chunk
				int distance = rng.random(capacity, size-initialSteps);
				buffer.skip(distance);

				assertFalse(buffer.hasMark());
				assertTrue(buffer.hasItem());
			}
		}
	}

}
