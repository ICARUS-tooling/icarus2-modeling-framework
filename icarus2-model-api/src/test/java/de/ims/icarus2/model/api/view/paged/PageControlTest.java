/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.model.api.ModelTestUtils.assertIndicesEqualsExact;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.meAsserter;
import static de.ims.icarus2.model.api.ModelTestUtils.range;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.wrap;
import static de.ims.icarus2.test.TestUtils.npeAsserter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.events.PageListener;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.PartTest;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.strings.NamedObject;

/**
 * @author Markus Gärtner
 *
 */
public interface PageControlTest<C extends PageControl> extends PartTest<PagedCorpusView, C> {

	final static int DEFAULT_PAGE_SIZE = 10;

	final static IndexSet[] DEFAULT_INDICES = wrap(range(0, 9));

	@Provider
	C createFilled(PagedCorpusView view, int pageSize, IndexSet...indices);

	@Provider
	PagedCorpusView createView(boolean active);

	@Provider
	default C createSinglePageView() {
		return createFilled(createEnvironment(), DEFAULT_PAGE_SIZE, DEFAULT_INDICES);
	}

	@Provider
	default C createDualPageView() {
		return createFilled(createEnvironment(), DEFAULT_PAGE_SIZE/2, DEFAULT_INDICES);
	}

	@Provider
	@Override
	default PagedCorpusView createEnvironment() {
		return createView(true);
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Provider
	@Override
	default C createTestInstance(TestSettings settings) {
		return createSinglePageView();
	}

	/**
	 * @see de.ims.icarus2.util.PartTest#createUnadded()
	 */
	@Provider
	@Override
	default C createUnadded() {
		return fail("must be overridden by actual test implementation!!!");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#getView()}.
	 */
	@Test
	default void testGetView() {
		try(PagedCorpusView view = createEnvironment()) {
			C control = createFilled(view, DEFAULT_PAGE_SIZE, DEFAULT_INDICES);
			assertSame(view, control.getView());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#getPageSize()}.
	 */
	@Test
	@RandomizedTest
	default void testGetPageSize(RandomGenerator rand) {
		int pageSize = rand.random(2, 100);
		C control = createFilled(createEnvironment(), pageSize, DEFAULT_INDICES);
		assertEquals(pageSize, control.getPageSize());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#getIndices()}.
	 */
	@DisplayName("Indices")
	@TestFactory
	default Stream<DynamicTest> testGetIndices() {
		return Stream.of(
				dynamicTest("inactive view [ME]", meAsserter(ModelErrorCode.VIEW_CLOSED,
						() -> createFilled(createView(false),
								DEFAULT_PAGE_SIZE, DEFAULT_INDICES).getIndices())),

				dynamicTest("no page [ME]", meAsserter(ModelErrorCode.VIEW_EMPTY,
						() -> create().getIndices())),

				dynamicTest("closed page [ME]", () -> {
							C control = create();
							control.load();
							control.closePage();
							assertModelException(ModelErrorCode.VIEW_EMPTY,
									() -> control.getIndices());
						}),

				dynamicTest("loaded [1 page]",
						() -> {
							C control = createSinglePageView();
							assertTrue(control.load());
							assertIndicesEqualsExact(range(0, 9), control.getIndices());
						}),
				dynamicTest("loaded [1/2 page]",
						() -> {
							C control = createDualPageView();
							assertTrue(control.load());
							assertIndicesEqualsExact(range(0, 4), control.getIndices());
						}),
				dynamicTest("loaded [2/2 page]",
						() -> {
							C control = createDualPageView();
							assertTrue(control.loadPage(1));
							assertIndicesEqualsExact(range(5, 9), control.getIndices());
						})
		);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#getPageCount()}.
	 */
	@DisplayName("PageCount")
	@TestFactory
	default Stream<DynamicTest> testGetPageCount() {
		return Stream.of(
				dynamicTest("size==page_size",
						() -> assertEquals(1, createFilled(createEnvironment(), 10, range(0, 9)).getPageCount())),
				dynamicTest("size<page_size",
						() -> assertEquals(1, createFilled(createEnvironment(), 20, range(0, 9)).getPageCount())),
				dynamicTest("size>page_size",
						() -> assertEquals(2, createFilled(createEnvironment(), 10, range(0, 10)).getPageCount())),
				dynamicTest("size==page_size*2",
						() -> assertEquals(2, createFilled(createEnvironment(), 5, range(0, 9)).getPageCount())),
				dynamicTest("size>page_size*3",
						() -> assertEquals(4, createFilled(createEnvironment(), 5,
								range(0, 6), range(7, 10), range(11,16)).getPageCount()))
		);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#getPageIndex()}.
	 */
	@DisplayName("PageIndex")
	@TestFactory
	default Stream<DynamicTest> testGetPageIndex() {
		return Stream.of(
				dynamicTest("initial",
						() -> assertEquals(PageControl.NO_PAGE_INDEX, create().getPageIndex())),

				dynamicTest("loaded [1 page]",
						() -> {
							C control = create();
							assertTrue(control.load());
							assertEquals(0, control.getPageIndex());
						}),
				dynamicTest("loaded [1/2 page]",
						() -> {
							C control = createDualPageView();
							assertTrue(control.load());
							assertEquals(0, control.getPageIndex());
						}),
				dynamicTest("loaded [2/2 page]",
						() -> {
							C control = createDualPageView();
							assertTrue(control.loadPage(1));
							assertEquals(1, control.getPageIndex());
						}),

				dynamicTest("inactive view [ME]", meAsserter(ModelErrorCode.VIEW_CLOSED,
						() -> createFilled(createView(false),
								DEFAULT_PAGE_SIZE, DEFAULT_INDICES).getPageIndex()))
		);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#loadPage(int)}.
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#load()}.
	 */
	@DisplayName("PageLoading")
	@TestFactory
	default Stream<DynamicTest> testLoadPage() {
		return Stream.of(
				dynamicTest("load [single page]",
						() -> assertTrue(create().load())),

				dynamicTest("loadPage [single page]",
						() -> assertTrue(create().loadPage(0))),

				dynamicTest("inactive view [ME]",
						meAsserter(ModelErrorCode.VIEW_CLOSED, () -> createFilled(
								createView(false), DEFAULT_PAGE_SIZE, DEFAULT_INDICES).loadPage(0))),

				dynamicTest("invalid page index [negative]",
						meAsserter(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS, () -> create().loadPage(-1))),
				dynamicTest("invalid page index [exceeds page count]",
						meAsserter(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS, () -> create().loadPage(2))),

				dynamicTest("locked page [ME]",
						meAsserter(ModelErrorCode.VIEW_LOCKED, () -> {
							C control = create();
							control.lock(mock(NamedObject.class));
							control.loadPage(0);
						})),

				dynamicTest("loadPage [multi pages]",
						() -> {
							C control = createDualPageView();
							assertTrue(control.loadPage(0));
							assertTrue(control.loadPage(1));
						})
		);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#load()}.
	 */
	@Test
	@Disabled("covered by testLoadPage()")
	default void testLoad() {
		fail("covered by testLoadPage()");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#closePage()}.
	 */
	@DisplayName("PageClosing")
	@TestFactory
	default Stream<DynamicTest> testClosePage() {
		return Stream.of(
				dynamicTest("no view [ME]", () -> {
						C control = createUnadded();
						PartTest.assertAddRemoveError(() -> control.closePage());
				}),

				dynamicTest("locked page [ME]",
						meAsserter(ModelErrorCode.VIEW_LOCKED, () -> {
							C control = create();
							control.lock(mock(NamedObject.class));
							control.closePage();
						})),

				dynamicTest("page closed [ME]",
						meAsserter(ModelErrorCode.VIEW_EMPTY, () -> {
							C control = create();
							control.load();
							control.closePage();
							// now we have a set page, but no content
							control.closePage();
						})),

				dynamicTest("locked page with inactive view", () -> {
							C control = createFilled(
									createView(false), DEFAULT_PAGE_SIZE, DEFAULT_INDICES);
							control.lock(mock(NamedObject.class));
							control.closePage();
						}),

				dynamicTest("initial [no page]", () -> assertFalse(create().closePage())),

				dynamicTest("full cycle [1 page]", () -> {
							C control = create();
							control.load();
							control.closePage();
						}),
				dynamicTest("full cycle [2 pages]", () -> {
							C control = createDualPageView();
							// 1st page
							control.loadPage(0);
							control.closePage();
							// 2nd page
							control.loadPage(1);
							control.closePage();
						})
		);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#isPageLoaded()}.
	 */
	@DisplayName("PageLoadingState")
	@TestFactory
	default Stream<DynamicTest> testIsPageLoaded() {
		return Stream.of(
				dynamicTest("initial", () -> assertFalse(create().isPageLoaded())),

				dynamicTest("loaded [1 page]", () -> {
							C control = create();
							control.load();
							assertTrue(control.isPageLoaded());
						}),

				dynamicTest("loaded [2 pages]", () -> {
							C control = createDualPageView();
							control.loadPage(0);
							assertTrue(control.isPageLoaded());
							control.loadPage(1);
							assertTrue(control.isPageLoaded());
						}),

				dynamicTest("inactive view [ME]", meAsserter(ModelErrorCode.VIEW_CLOSED,
						() -> createFilled(createView(false),
								DEFAULT_PAGE_SIZE, DEFAULT_INDICES).isPageLoaded()))
		);
	}

	@DisplayName("PageListener")
	@TestFactory
	default List<DynamicTest> pageListenerTests() {
		return LazyCollection.<DynamicTest>lazyList().addAll(
				dynamicTest("addPageListener",
						() -> create().addPageListener(mock(PageListener.class))),
				dynamicTest("addPageListener [NPE]",
						npeAsserter(() -> create().addPageListener(null))),

				dynamicTest("removePageListener [unknown]",
						() -> create().removePageListener(mock(PageListener.class))),
				dynamicTest("removePageListener [NPE]",
						npeAsserter(() -> create().removePageListener(null))),
				dynamicTest("removePageListener",
						() -> {
							C control = create();
							PageListener listener = mock(PageListener.class);
							control.addPageListener(listener);
							control.removePageListener(listener);
						}),

				dynamicTest("event: loading+loaded [1 page]",
						() -> {
							C control = create();
							PageListener listener = mock(PageListener.class);
							control.addPageListener(listener);

							control.loadPage(0);

							verify(listener, times(1)).pageLoading(eq(control), eq(0), eq(DEFAULT_PAGE_SIZE));
							verify(listener, times(1)).pageLoaded(eq(control), eq(0), eq(DEFAULT_PAGE_SIZE));
						}),

				dynamicTest("event: full event cycle [2 pages]",
						() -> {
							int pageSize = DEFAULT_PAGE_SIZE/2;
							C control = createFilled(createEnvironment(), pageSize, DEFAULT_INDICES);
							PageListener listener = mock(PageListener.class);
							control.addPageListener(listener);

							control.loadPage(0);
							control.loadPage(1);

							verify(listener).pageLoading(eq(control), eq(0), eq(pageSize));
							verify(listener).pageLoaded(eq(control), eq(0), eq(pageSize));
							verify(listener).pageClosing(eq(control), eq(0));
							verify(listener).pageClosed(eq(control), eq(0));
							verify(listener).pageLoading(eq(control), eq(1), eq(pageSize));
							verify(listener).pageLoaded(eq(control), eq(1), eq(pageSize));
						}),

				dynamicTest("event: full event cycle [1 page]",
						() -> {
							C control = create();
							PageListener listener = mock(PageListener.class);
							control.addPageListener(listener);

							control.loadPage(0);
							control.closePage();

							verify(listener).pageLoading(eq(control), eq(0), eq(DEFAULT_PAGE_SIZE));
							verify(listener).pageLoaded(eq(control), eq(0), eq(DEFAULT_PAGE_SIZE));
							verify(listener).pageClosing(eq(control), eq(0));
							verify(listener).pageClosed(eq(control), eq(0));
						}),

				dynamicTest("event: closing+closed [no load]",
						() -> {
							C control = create();
							PageListener listener = mock(PageListener.class);
							control.addPageListener(listener);

							control.closePage();

							verify(listener, never()).pageClosing(eq(control), anyInt());
						})

				//TODO how to cause an error in loading/closing procedure to verify PageListener.pageFailed()?
		).getAsList();
	}

	@DisplayName("Locking")
	@TestFactory
	default List<DynamicTest> lockTests() {
		return LazyCollection.<DynamicTest>lazyList().addAll(
						dynamicTest("lock", () -> create().lock(mock(NamedObject.class))),
						dynamicTest("lock [null]", npeAsserter(() -> create().lock(null))),
						dynamicTest("lock foreign", () -> {
							C control = create();
							NamedObject key1 = mock(NamedObject.class);
							NamedObject key2 = mock(NamedObject.class);
							control.lock(key1);
							assertModelException(ModelErrorCode.VIEW_LOCKED,
									() -> control.lock(key2));
						}),
						dynamicTest("lock repeatedly", () -> {
							C control = create();
							NamedObject key = mock(NamedObject.class);
							control.lock(key);
							// supposed to be idempotent
							control.lock(key);
							control.lock(key);
						}),
						dynamicTest("unlock", () -> {
							C control = create();
							NamedObject key = mock(NamedObject.class);
							control.lock(key);
							control.unlock(key);
						}),
						dynamicTest("unlock foreign", () -> {
							C control = create();
							NamedObject key1 = mock(NamedObject.class);
							NamedObject key2 = mock(NamedObject.class);
							control.lock(key1);
							assertModelException(GlobalErrorCode.INVALID_INPUT,
									() -> control.unlock(key2));
						}),
						dynamicTest("unlock [null]", npeAsserter(() -> create().unlock(null))),
						dynamicTest("isLocked initial", () -> assertFalse(create().isLocked())),
						dynamicTest("isLocked after lock", () -> {
							C control = create();
							control.lock(mock(NamedObject.class));
							assertTrue(control.isLocked());
						}),
						dynamicTest("isLocked after unlock", () -> {
							C control = create();
							NamedObject key = mock(NamedObject.class);
							control.lock(key);
							control.unlock(key);
							assertFalse(control.isLocked());
						}))
				.getAsList();
	}
}
