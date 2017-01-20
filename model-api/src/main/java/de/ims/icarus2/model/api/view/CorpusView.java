/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 */
package de.ims.icarus2.model.api.view;

import java.util.Set;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.events.CorpusListener;
import de.ims.icarus2.model.api.events.PageListener;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.Changeable;
import de.ims.icarus2.util.Part;

/**
 * @author Markus Gärtner
 *
 */
public interface CorpusView extends Part<Corpus>, Changeable {

	/**
	 * Returns the {@code Corpus} that is backing this corpus view.
	 * @return
	 */
	Corpus getCorpus();

	/**
	 * Returns the {@code scope} that was used to limit the contexts
	 * and layers involved in this corpus view or {@code null} if no vertical
	 * filtering was performed.
	 */
	Scope getScope();

	/**
	 * Returns the {@link CorpusAccessMode mode} this view was created for.
	 * Note that depending on the access mode a quite large number of methods
	 * in the associated {@link #getModel() model} have a different default
	 * behavior. In <i>read-only</i> mode for example all <i>modifying</i>
	 * methods will throw an exception.
	 *
	 * @return
	 */
	CorpusAccessMode getAccessMode();

	/**
	 * Returns the number of element in this corpus view, i.e. the number of items
	 * contained in the <i>primary-layer</i> of this corpus view's {@code Scope}.
	 * This number is equal to the number of index values passed to the view on creation time.
	 *
	 * @return
	 */
	long getSize();

	/**
	 * Returns the maximum number of top-level {@link Item members} of this view's
	 * {@link Scope#getPrimaryLayer() primary-layer} that are available via a single
	 * page.
	 *
	 * @return
	 */
	int getPageSize();

	// Destruction support

	/**
	 * Attempts to acquire shared ownership of this corpus view by the given {@code owner}.
	 * If the given owner already holds shared ownership of this corpus view, the method
	 * simply returns.
	 *
	 * @param owner
	 * @throws NullPointerException if the {@code owner} argument is {@code null}.
	 * @throws ModelException if {@link #close()} has already been called on this
	 * 			corpus view and it's in the process of releasing its data.
	 */
	void acquire(CorpusOwner owner);

	/**
	 * Removes the given {@code owner}'s shared ownership on this corpus view. If no
	 * more owners are registered to this corpus view, a subsequent call to {@link #closable()}
	 * will return {@code true}.
	 *
	 * @param owner
	 * @throws NullPointerException if the {@code owner} argument is {@code null}.
	 * @throws ModelException if {@link #close()} has already been called on this
	 * 			corpus view and it's in the process of releasing its data.
	 * @throws IllegalArgumentException if the given owner does not hold shared ownership
	 * 			of this corpus view.
	 */
	void release(CorpusOwner owner);

	/**
	 * Returns an immutable set view of all the owners currently registered with this corpus view.
	 */
	Set<CorpusOwner> getOwners();

	/**
	 * Checks whether or not the corpus view is currently closable, i.e.
	 * there are no more registered owners expressing interest in this view.
	 * A return value of {@code true} means
	 * TODO
	 *
	 * @return
	 */
	boolean closable();

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
	 * @throws InterruptedException
	 * @throws IllegalStateException in case there are still owners that could not be made to
	 * 			release their partial ownership of this corpus view
	 */
	void close() throws InterruptedException;

	/**
	 * Returns {@code true} if and only if this view is neither closed nor in the process of closing.
	 * @return
	 */
	boolean isActive();

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
	 * Manages the state of the internal paging system. While in principal a {@code CorpusView}
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
	 * of a still locked page control any client code that attempts to {@link #lock(Object) lock} a
	 * control should always make sure to also register with the surrounding corpus to receive
	 * {@link CorpusListener#corpusViewDestroyed(de.ims.icarus2.model.api.events.CorpusEvent)} events,
	 * which are sent <b>after</b> a view got closed, and release all data still associated with that view.
	 * <p>
	 * Paging models the <i>horizontal</i> part of filtering for scalable
	 * access to corpus resources provided by the framework.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface PageControl extends Part<CorpusView> {

		public static final int NO_PAGE_INDEX = -1;

		/**
		 *
		 * @return the {@code CorpusView} this control is managing the paging for
		 */
		CorpusView getView();

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
		void lock(Object key) throws ModelException;
		void unlock(Object key) throws ModelException;
	}

	public enum ViewMode {
		STREAM,
		RANDOM_ACCESS,
		;
	}
}
