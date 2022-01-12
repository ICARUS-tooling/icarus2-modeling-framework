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
package de.ims.icarus2.model.api.view.paged;

import javax.swing.event.ChangeEvent;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.OwnableCorpusPart;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.events.CorpusListener;
import de.ims.icarus2.model.api.events.PageListener;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.view.CorpusView;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.Changeable;
import de.ims.icarus2.util.Part;
import de.ims.icarus2.util.strings.NamedObject;

/**
 * A filtered view on a corpus resource.
 *
 * Filtering is supported along two axis via different mechanics:
 * <p>
 * <b>Vertically</b> by the {@link Scope} that is used to construct a view instance.
 * This affects the {@link Layer layers} available for interaction through a view
 * and also dictates the affiliated {@link Driver drivers} which need to be used
 * for transforming between the physical representations and members of this framework. <br>
 * <b>Horizontally</b> via the {@link IndexSet indices} used to form the underlying
 * {@link PageControl pages} that can be traversed. <br>
 * Together those two filter axis provide a powerful tool to inspect exactly those
 * areas of a corpus that are of interest for a certain task.
 * <p>
 * A corpus view instance has one state observable through the {@link Changeable}
 * interface, namely that of being {@link #isActive() active}. The respective
 * {@link ChangeEvent} will fire exactly once, when the view gets {@link #close() closed}.
 * <p>
 * Note that a {@code PagedCorpusView} does not provide any methods to directly interact with
 * the underlying corpus data. Instead it provides a configuration-specific implementation
 * of {@link CorpusModel} which mirrors all the <i>raw</i> read and write methods of various
 * framework members used for representing segmentation, (hierarchical) structure or content
 * of layers in a corpus.
 *
 * @author Markus Gärtner
 *
 */
public interface PagedCorpusView extends CorpusView, OwnableCorpusPart {

	/**
	 * Returns the {@link AccessMode mode} this view was created for.
	 * Note that depending on the access mode a quite large number of methods
	 * in the associated {@link #getModel() model} have a different default
	 * behavior. In <i>read-only</i> mode for example all <i>modifying</i>
	 * methods will throw an exception.
	 *
	 * @return
	 */
	@Override
	AccessMode getAccessMode();

	/**
	 * {@inheritDoc}
	 * <p>
	 * This number is equal to the number of index values passed to the view on creation time.
	 *
	 * @return
	 */
	@Override
	long getSize();

	/**
	 * Returns the maximum number of top-level {@link Item members} of this view's
	 * {@link Scope#getPrimaryLayer() primary-layer} that are available via a single
	 * page.
	 *
	 * @return
	 */
	int getPageSize();

	/**
	 * Checks whether this corpus view is allowed to be closed and if so, releases
	 * all currently held data. Note that if there are still {@code CorpusOwner}s holding
	 * on to this corpus view, they will be asked to release their ownership. If after this
	 * initial release phase there is still at least one ownership pending, the call will
	 * fail with an {@code ModelException}.
	 * Otherwise the corpus view will release its data and disconnect any links to the hosting
	 * corpus.
	 *
	 * @throws ModelException
	 * @throws IllegalStateException in case there are still owners that could not be made to
	 * 			release their partial ownership of this corpus view
	 */
	@Override
	void close();

	// Page support

	PageControl getPageControl();

	/**
	 * Fetches the shared corpus model view on the data represented by this corpus view.
	 * Note that for this method to succeed the data for the current page has to
	 * be properly loaded! Attempting to fetch a model view for a corpus view whose
	 * current page is empty, will result in a {@code ModelException} being thrown.
	 *
	 * @return the corpus model view on the data represented by this corpus view.
	 * @throws ModelException if no data has been loaded so far
	 */
	CorpusModel getModel();

	/**
	 * Manages the state of the internal paging system. While in principal a {@code PagedCorpusView}
	 * instance is allowed to represent an arbitrary large number of items, it is not practical
	 * to hold more than a limited number of them in memory. Therefore the
	 * total number of items in a view gets split into reasonably sized pages. At most one page
	 * can be accessed via the shared {@link CorpusModel} of this view.The {@code PageControl}
	 * interface defines methods for external modules to (un)load pages and to register listeners
	 * to react to page changes.
	 * <p>
	 * In addition it is possible to lock the current page with a {@code key} object so that it is
	 * not possible for other code to call methods that would cause page changes. Note, however, that
	 * this restriction only applies to external attempts to call those methods! When a corpus view
	 * is closing down for whatever reasons, it will close the page control and thereby also the
	 * active page, regardless of its lock state. To prevent data corruption or other negative side effects
	 * of a still locked page control any client code that attempts to {@link #lock(de.ims.icarus2.util.strings.NamedObject) lock} a
	 * control should always make sure to also register with the surrounding corpus to receive
	 * {@link CorpusListener#corpusPartDestroyed(de.ims.icarus2.model.api.events.CorpusEvent)} events,
	 * which are sent <b>after</b> a view got closed, and release all data still associated with that view.
	 * <p>
	 * Paging models the <i>horizontal</i> part of filtering for scalable
	 * access to corpus resources provided by the framework.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface PageControl extends Part<PagedCorpusView> {

		public static final int NO_PAGE_INDEX = -1;

		/**
		 *
		 * @return the {@code PagedCorpusView} this control is managing the paging for
		 */
		PagedCorpusView getView();

		/**
		 * Returns the maximum number of elements per page, or {@code -1} if this
		 * corpus view only contains {@code 1} page and that page's size is therefore determined by
		 * the size of the this corpus view's <i>primary layer</i>.
		 * @return
		 *
		 * @throws ModelException in case the page has not yet been loaded or the surrounding view
		 * is not active.
		 */
		int getPageSize();

		/**
		 * Returns the set of index values for the current page. Index values refer to the primary layer
		 * of the surrounding view's scope.
		 * @return
		 *
		 * @throws ModelException in case the page has not yet been loaded or the surrounding view
		 * is not active.
		 */
		IndexSet getIndices();

		/**
		 * Returns the number of available pages for this corpus view.
		 *
		 * @return
		 */
		int getPageCount();

		/**
		 * Returns the index of the current page, initially {@code -1}.
		 * @return
		 *
		 * @throws ModelException in case the surrounding view is not active.
		 */
		int getPageIndex();

		/**
		 * Synchronously attempts to load the specified page of data
		 * into this sub-corpus. If the requested page is already loaded and active, this method simply returns.
		 * <p>
		 * Fires the following events to registered {@code PageListener}s:
		 * <ul>
		 * <li>{@link PageListener#pageClosed(PageControl, int) pageClosed} in case another than the requested
		 * page is already loaded and needs to be closed</li>
		 * <li>{@link PageListener#pageLoading(PageControl, int, int) pageLoading} as soon as a potentially present
		 * page has ben closed and loading of the requested page has started</li>
		 * <li>{@link PageListener#pageLoaded(PageControl, int, int) pageLoaded} when loading succeeded</li>
		 * <li>{@link PageListener#pageFailed(PageControl, int, ModelException) pageFailed} when the requested
		 * page could not be loaded or closing a presently loaded page failed
		 * (note that this callback also includes the encountered error!)</li>
		 * </ul>
		 *
		 * @param index the index of the page to load
		 * @return {@code true} iff loading the requested page succeeded without errors
		 * and the content of this corpus view changed as a result.
		 * @throws ModelException if there was an IO error or other problem encountered
		 * 			while loading data (like memory shortage, ...)
	     * @throws IndexOutOfBoundsException if this corpus view supports pagin and
	     * 			 the index is out of range (<tt>index &lt; 0 || index &gt;= getPageCount()</tt>)
		 */
		boolean loadPage(int index) throws InterruptedException;

		/**
		 * Shorthand method for loading the first page.
		 *
		 * @return
		 * @throws InterruptedException
		 */
		default boolean load() throws InterruptedException {
			return loadPage(0);
		}

		/**
		 * Closes the current page or does nothing if no page has been loaded.
		 *
		 * Fires {@link PageListener#pageClosed(PageControl, int)} as soon as the page has been closed.
		 *
		 * @return {@code true} iff a page has previously been loaded and was successfully closed as a
		 * result of this call
		 *
		 * @throws ModelException in case the page has not yet been loaded
		 */
		boolean closePage() throws InterruptedException;

		/**
		 * Checks whether or not the data for the current page has been loaded.
		 * @return
		 */
		boolean isPageLoaded();

		void addPageListener(PageListener listener);
		void removePageListener(PageListener listener);

		boolean isLocked();
		void lock(NamedObject key) throws ModelException;
		void unlock(NamedObject key) throws ModelException;
	}
}
