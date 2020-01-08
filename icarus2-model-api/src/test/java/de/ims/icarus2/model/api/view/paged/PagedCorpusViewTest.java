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
package de.ims.icarus2.model.api.view.paged;

import static de.ims.icarus2.model.api.ModelTestUtils.range;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.OwnableCorpusPartTest;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.view.CorpusViewTest;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl;
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
public interface PagedCorpusViewTest<V extends PagedCorpusView>
		extends CorpusViewTest<V>, OwnableCorpusPartTest<V> {

	static final int DEFAULT_PAGE_SIZE = 10;
	static final IndexSet[] DEFAULT_INDICES = new IndexSet[] {
			range(0, 3), range(4, 8), range(9,9)
	};

	/**
	 *
	 * @param corpus the environment as created by {@link #createEnvironment()}
	 * @param accessMode selected access mode
	 * @param size either {@link IcarusUtils#UNSET_LONG} to indicate that the
	 * size is to be chosen freely or a specific size value for the stream.
	 * @return
	 */
	@Provider
	V createView(Corpus corpus, AccessMode accessMode, int pageSize, IndexSet...indices);

	/**
	 * @see de.ims.icarus2.model.api.view.CorpusViewTest#createForAccessMode(de.ims.icarus2.util.AccessMode)
	 */
	@Provider
	@Override
	default V createForAccessMode(AccessMode accessMode) {
		return createView(createEnvironment(), accessMode, DEFAULT_PAGE_SIZE, DEFAULT_INDICES);
	}

	@Provider
	@Override
	default V createForSize(long size) {
		IndexSet indices = null;
		if(size==UNSET_LONG) {
			indices = IndexUtils.EMPTY_SET;
		} else {
			indices = range(0, size-1);
		}
		return createView(createEnvironment(), AccessMode.READ, DEFAULT_PAGE_SIZE,indices);
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Provider
	@Override
	default V createTestInstance(TestSettings settings) {
		return settings.process(createView(createEnvironment(), AccessMode.READ,
				DEFAULT_PAGE_SIZE, DEFAULT_INDICES));
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

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.PagedCorpusView#close()}.
	 */
	@Test
	default void testClose() {
		try(V view = create()) {
			// This only verifies idempotence of the close() method
			view.close();
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.PagedCorpusView#getPageSize()}.
	 */
	@RepeatedTest(value=RUNS)
	@RandomizedTest
	default void testGetPageSize(RandomGenerator rand) {
		int pageSize = rand.random(1, 1000);
		try(V view = createView(createEnvironment(), AccessMode.READ, pageSize, DEFAULT_INDICES)) {
			assertEquals(pageSize, view.getPageSize());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.PagedCorpusView#getPageControl()}.
	 */
	@Test
	default void testGetPageControl() {
		try(V view = create()) {
			PageControl control = view.getPageControl();
			assertNotNull(control);
			assertSame(view, control.getView());

			assertEquals(view.getPageSize(), control.getPageSize());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.PagedCorpusView#getModel()}.
	 */
	@Test
	default void testGetModel() {
		try(V view = create()) {
			CorpusModel model = view.getModel();
			assertNotNull(model);
			assertSame(view, model.getView());

			//TODO maybe verify consistency of certain properties between view and model?
		}
	}

}
