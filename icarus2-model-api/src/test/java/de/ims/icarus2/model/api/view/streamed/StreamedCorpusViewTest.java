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
package de.ims.icarus2.model.api.view.streamed;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.OwnableCorpusPartTest;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.api.view.CorpusViewTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface StreamedCorpusViewTest<V extends StreamedCorpusView>
		extends OwnableCorpusPartTest<V>, CorpusViewTest<V> {

	/**
	 *
	 * @param corpus the environment as created by {@link #createEnvironment()}
	 * @param accessMode selected access mode
	 * @param size either {@link IcarusUtils#UNSET_LONG} to indicate that the
	 * size is to be chosen freely or a specific size value for the stream.
	 * @return
	 */
	@Provider
	V createView(Corpus corpus, AccessMode accessMode, long size, int capacity);

	/**
	 * @see de.ims.icarus2.model.api.view.CorpusViewTest#createForAccessMode(de.ims.icarus2.util.AccessMode)
	 */
	@Provider
	@Override
	default V createForAccessMode(AccessMode accessMode) {
		return createView(createEnvironment(), accessMode, UNSET_LONG, UNSET_INT);
	}

	@Provider
	@Override
	default V createForSize(long size) {
		return createView(createEnvironment(), AccessMode.READ, size, UNSET_INT);
	}

	@Provider
	default V createForCapacity(int capacity) {
		return createView(createEnvironment(), AccessMode.READ, UNSET_LONG, capacity);
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Provider
	@Override
	default V createTestInstance(TestSettings settings) {
		return settings.process(createView(createEnvironment(), AccessMode.READ, UNSET_LONG, UNSET_INT));
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#createNoArgs()
	 */
	@Provider
	@Override
	default V createNoArgs() {
		throw new UnsupportedOperationException("Views are supposed to be created via builders");
	}

	@Override
	default void testMandatoryConstructors() throws Exception {
		throw new UnsupportedOperationException("No constructor testing - only builders");
	}

	Set<StreamOption> getSupportedOptions();

	default Stream<DynamicTest> testGetOptions() {
		return getSupportedOptions().stream()
				.map(option -> dynamicTest(option.name(),
						() -> {
							try(V view = create()){
								assertTrue(create().hasOption(option));
							}}));
	}

	// EMPTY STREAM

	@DisplayName("Empty Stream")
	@TestFactory
	default List<DynamicNode> emptyStreamTests() {
		List<DynamicNode> tests = new ArrayList<>();

		Collections.addAll(tests,
				dynamicTest("hasItem", () -> {
					try(V view = create()){
						assertFalse(create().hasItem());
					}}),
				dynamicTest("currentItem", () -> {
					try(V view = create()){
						assertModelException(ModelErrorCode.STREAM_NO_ITEM,
						() -> create().currentItem());
					}}),
				dynamicTest("advance", () -> {
					try(V view = create()){
						assertFalse(create().advance());
					}}),
				dynamicTest("close", () -> {
					try(V view = create()){
						create().close();
					}})
		);

		Set<StreamOption> options = getSupportedOptions();

		if(options.contains(StreamOption.ALLOW_MARK)) {
			Collections.addAll(tests,
				dynamicTest("mark", () -> {
					try(V view = create()){
						assertModelException(ModelErrorCode.STREAM_NO_ITEM,
						() -> create().mark());
					}}),
				dynamicTest("reset", () -> {
					try(V view = create()){
						assertModelException(ModelErrorCode.STREAM_MARK_NOT_SET,
						() -> create().reset());
					}}),
				dynamicTest("hasMark", () -> {
					try(V view = create()){
						assertFalse(create().hasMark());
					}})
			);
		}

		return tests;
	}

	// FILLED STREAM

	Stream<Item> getRawItemStream();
	ItemLayerManager getItemLayerManager();

	/**
	 * Test method for {@link StreamedCorpusView#hasItem()}.
	 */
	@Test
	default void testHasItemInitial() {
		try(V view = create()) {
			assertFalse(view.hasItem());
		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#currentItem()}.
	 */
	@Test
	default void testCurrentItemInitial() {
		try(V view = create()) {
			assertModelException(ModelErrorCode.STREAM_NO_ITEM, () -> view.currentItem());
		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#currentItem()}.
	 */
	@Test
	default void testCurrentItemFull() {
		try(V view = create()) {
			Item[] raw = getRawItemStream().toArray(Item[]::new);

			for(int i=0; i<raw.length; i++) {
				Item expected = raw[i];
				assertTrue(view.advance(), "Missing item at position "+i);
				Item actual = view.currentItem();

				assertSame(expected, actual,
						String.format("Wrong item at position %d of %d",
								_int(i+1), _int(raw.length)));
			}
		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#advance()}.
	 */
	@Test
	default void testAdvanceInitial() {
		try(V view = createForSize(10)) {
			assertTrue(view.advance());
		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#advance()}.
	 */
	@Test
	default void testAdvanceFull() throws Exception {
		try(V view = create()) {
			Item[] raw = getRawItemStream().toArray(Item[]::new);
			for(int i=0; i<raw.length; i++) {
				assertTrue(view.advance(), String.format(
						"Failed to advance for item %d of %d", _int(i+1), _int(raw.length)));
			}

			// Check that EoS gets reported properly
			assertFalse(view.advance());
			// Check that the advance() method stays idempotent after EoS is reached
			assertFalse(view.advance());
			assertFalse(view.advance());
		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#close()}.
	 */
	@Test
	default void testClose() {
		try(V view = create()) {
			// This only verifies idempotence of the close() method
			view.close();
		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#mark()}.
	 */
	@Test
	default void testMarkInitial() {
		try(V view = create()) {
			assertModelException(ModelErrorCode.STREAM_NO_ITEM, () -> view.mark());
		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#mark()}.
	 */
	@Test
	default void testMarkFull() {
		try(V view = create()) {
			while(view.advance()) {
				view.mark();
			}

		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#hasMark()}.
	 */
	@Test
	default void testHasMarkInitial() {
		try(V view = create()) {
			assertFalse(view.hasMark());
		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#hasMark()}.
	 */
	@Test
	default void testHasMarkFull() {
		try(V view = create()) {
			while(view.advance()) {
				view.mark();
				assertTrue(view.hasMark());
			}
		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#clearMark()}.
	 */
	@Test
	default void testClearMarkFull() {
		try(V view = create()) {
			while(view.advance()) {
				view.mark();
				assertTrue(view.hasMark());
				view.clearMark();
				assertFalse(view.hasMark());
			}
		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#reset()}.
	 */
	@Test
	default void testResetInitial() {
		try(V view = create()) {
			assertModelException(ModelErrorCode.STREAM_MARK_NOT_SET, () -> view.reset());
		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#reset()}.
	 */
	@RepeatedTest(value=RUNS)
	@RandomizedTest
	default void testResetFull(RandomGenerator rand) {
		int size = rand.random(500, 1000);
		try(V view = createForSize(size)) {

			// Go to random index
			int markedIndex = rand.random(50, size-2);
			int index = 0;
			while(index++<markedIndex) {
				view.advance();
			}
			assertTrue(view.hasItem());

			// Mark position
			view.mark();
			Item item = view.currentItem();
			assertNotNull(item);
			assertTrue(view.hasMark());

			// Move random distance away
			int distance = rand.random(2, size-markedIndex);
			while(distance-->0 && !view.wouldInvalidateMark() && view.advance()) {
				// Just moving the cursor away as much as possible
			}

			// Reset back to mark
			view.reset();
			assertFalse(view.hasMark());
			assertTrue(view.hasItem());
			assertSame(item, view.currentItem());
		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#wouldInvalidateMark()}.
	 */
	@Test
	default void testWouldInvalidateMarkInitial() {
		try(V view = create()) {
			assertModelException(ModelErrorCode.STREAM_MARK_NOT_SET,
					() -> view.wouldInvalidateMark());
		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#wouldInvalidateMark()}.
	 */
	@RepeatedTest(value=RUNS)
	@RandomizedTest
	default void testWouldInvalidateMarkInitialFull(RandomGenerator rand) {
		int size = rand.random(500, 1000);
		int capacity = rand.random(20, size/2);

		try(V view = createView(createEnvironment(), AccessMode.READ_WRITE, size, capacity)) {
			int markedIndex = rand.random(0, capacity);
			for (int i = 0; i < capacity; i++) {
				view.advance();
				if(i==markedIndex) {
					view.mark();
				}
			}
			assertTrue(view.hasMark());
			assertTrue(view.wouldInvalidateMark());

			view.advance();
			assertFalse(view.hasMark());
			assertModelException(ModelErrorCode.STREAM_MARK_NOT_SET,
					() -> view.wouldInvalidateMark());
		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#flush()}.
	 */
	@RepeatedTest(value=RUNS)
	@RandomizedTest
	default void testFlush(RandomGenerator rand) {
		int size = rand.random(500, 1000);
		int capacity = rand.random(20, size/2);

		try(V view = createView(createEnvironment(), AccessMode.READ_WRITE, size, capacity)) {
			// Create random mark
			int markedIndex = rand.random(0, capacity);
			for (int i = 0; i < capacity; i++) {
				view.advance();
				if(i==markedIndex) {
					view.mark();
				}
			}
			assertTrue(view.hasMark());

			// Move random distance away
			int distance = rand.random(2, size-markedIndex);
			while(distance-->0 && !view.wouldInvalidateMark() && view.advance()) {
				// Just moving the cursor away as much as possible
			}
			assertTrue(view.hasMark());

			Item item = view.currentItem();
			view.flush();

			assertTrue(view.hasItem());
			assertFalse(view.hasMark());
			assertSame(item, view.currentItem());
		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#skip(long)}.
	 */
	@RepeatedTest(value=RUNS)
	@RandomizedTest
	default void testSkipWithoutLossOfMark(RandomGenerator rand) {
		int size = rand.random(500, 1000);
		int capacity = rand.random(20, size/2);

		try(V view = createView(createEnvironment(), AccessMode.READ_WRITE, size, capacity)) {
			// Move to random spot
			int initialSteps = rand.random(capacity/4, capacity/2);
			int markedIndex = rand.random(0, capacity/4);
			for (int i = 0; i < initialSteps; i++) {
				view.advance();
				if(i==markedIndex) {
					view.mark();
				}
			}
			assertTrue(view.hasMark());

			// Now skip random number of items within current chunk
			int distance = rand.random(1, capacity-initialSteps);
			view.skip(distance);

			assertTrue(view.hasMark());
			assertTrue(view.hasItem());
		}
	}

	/**
	 * Test method for {@link StreamedCorpusView#skip(long)}.
	 */
	@RepeatedTest(value=RUNS)
	@RandomizedTest
	default void testSkipWithLossOfMark(RandomGenerator rand) {
		int size = rand.random(500, 1000);
		int capacity = rand.random(20, size/2);

		try(V view = createView(createEnvironment(), AccessMode.READ_WRITE, size, capacity)) {
			// Move to random spot
			int initialSteps = rand.random(capacity/4, capacity/2);
			int markedIndex = rand.random(0, capacity/4);
			for (int i = 0; i < initialSteps; i++) {
				view.advance();
				if(i==markedIndex) {
					view.mark();
				}
			}
			assertTrue(view.hasMark());

			// Now skip random number of items within current chunk
			int distance = rand.random(capacity, size-initialSteps);
			view.skip(distance);

			assertFalse(view.hasMark());
			assertTrue(view.hasItem());
		}
	}
}
