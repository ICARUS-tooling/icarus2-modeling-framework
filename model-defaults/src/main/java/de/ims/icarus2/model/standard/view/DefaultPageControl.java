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
package de.ims.icarus2.model.standard.view;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.CorpusView;
import de.ims.icarus2.model.api.corpus.CorpusView.PageControl;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.events.PageListener;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.ItemLayerManager;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.AbstractPart;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultPageControl extends AbstractPart<CorpusView> implements PageControl {

	protected final PageIndexBuffer pageBuffer;
	protected final ItemLayerManager itemLayerManager;

	protected final Cache<Integer, IndexSet> indexSetCache;

	protected final Page currentPage = new Page();
	protected final PageLock pageLock = new PageLock();

	protected final ReentrantLock lock = new ReentrantLock();

	protected final List<PageListener> pageListeners = new CopyOnWriteArrayList<>();

	protected DefaultPageControl(PageControlBuilder builder) {
		checkNotNull(builder);

		PageIndexBuffer pageIndexBuffer = builder.getPageIndexBuffer();
		if(pageIndexBuffer==null) {
			pageIndexBuffer = new PageIndexBuffer(builder.getIndices(), builder.getPageSize());
		}

		pageBuffer = pageIndexBuffer;

		itemLayerManager = builder.getItemLayerManager();

		Cache<Integer, IndexSet> indexSetCache = builder.getIndexSetCache();
		if(indexSetCache==null) {
			int indexSetCacheSize = builder.getIndexCacheSize();
			if(indexSetCacheSize<=0) {
				indexSetCache = null;
			} else {
				indexSetCache = CacheBuilder.newBuilder()
						.maximumSize(indexSetCacheSize)
						.weakValues()
						.build(new CacheLoader<Integer, IndexSet>(){

							@Override
							public IndexSet load(Integer key) throws Exception {
								return DefaultPageControl.this.pageBuffer.createPage(key);
							}
						});
			}
		}

		this.indexSetCache = indexSetCache;
	}

	protected IndexSet loadIndices(int pageIndex) {
		IndexSet indices = null;

		if(indexSetCache!=null) {
			// If our cache is set we have to distinguish between loading cache and
			// a static one provided by client code.
			// In case of a loading cache we have some additional error handling to do.
			if(indexSetCache instanceof LoadingCache) {
				try {
					indices = ((LoadingCache<Integer, IndexSet>)indexSetCache).get(pageIndex);
				} catch (ExecutionException e) {
					throw ModelException.unwrap(e);
				}
			} else {
				indices = indexSetCache.getIfPresent(pageIndex);
			}
		}

		if(indices==null) {
			indices = pageBuffer.createPage(pageIndex);

			//TODO should we check for a non loading cache and insert the new IndexSet?
		}

		return indices;
	}

	protected void checkActiveView() {
		checkAdded();
		if(!getOwner().isActive())
			throw new ModelException(ModelErrorCode.VIEW_CLOSED, "View already closed");
	}

	protected void checkNotLocked() {
		if(pageLock.isLocked())
			throw new ModelException(ModelErrorCode.VIEW_LOCKED, "View's page control is currently locked");
	}

	protected void checkActivePage() {
		if(!currentPage.isLoaded())
			throw new ModelException(ModelErrorCode.VIEW_EMPTY, "View's current page not loaded");
	}

	protected void checkPageIndex(int pageIndex) {
		if(pageIndex<0 || pageIndex>=pageBuffer.getPageCount())
			throw new ModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					Messages.indexOutOfBoundsMessage("Page index out of bounds",
							0, pageBuffer.getPageCount(), pageIndex));
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.CorpusView.PageControl#getView()
	 */
	@Override
	public CorpusView getView() {
		checkAdded();
		return getOwner();
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.CorpusView.PageControl#getPageSize()
	 */
	@Override
	public int getPageSize() {
		checkActiveView();
		return pageBuffer.getPageSize();
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.CorpusView.PageControl#getPageCount()
	 */
	@Override
	public int getPageCount() {
		checkActiveView();
		return pageBuffer.getPageCount();
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.CorpusView.PageControl#getIndices()
	 */
	@Override
	public IndexSet getIndices() {
		checkActiveView();
		checkActivePage();
		return currentPage.getIndices();
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.CorpusView.PageControl#getPageIndex()
	 */
	@Override
	public int getPageIndex() {
		checkActiveView();
		checkActivePage();
		return currentPage.getPageIndex();
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.CorpusView.PageControl#loadPage(int)
	 */
	@Override
	public boolean loadPage(int index) throws InterruptedException {
		checkActiveView();
		checkNotLocked();
		checkPageIndex(index);

		lock.lock();
		try {

			final int pageIndex = index;

			if(currentPage.isLoaded() && currentPage.isPageIndex(pageIndex)) {
				return false;
			}

			if(currentPage.canClose() && !currentPage.isPageIndex(pageIndex)) {
				closePage0();
			}

			IndexSet indices = loadIndices(pageIndex);

			if(indices==null)
				throw new ModelException(GlobalErrorCode.MISSING_DATA, "No valid IndexSet available for page: "+pageIndex);

			loadPage0(pageIndex, indices);

		} finally {
			lock.unlock();
		}

		return true;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.CorpusView.PageControl#closePage()
	 */
	@Override
	public boolean closePage() throws InterruptedException {
		checkAdded();

		// Only check for locks if the surrounding view is still active
		if(!getView().isActive()) {
			checkNotLocked();
		}

		if(!currentPage.hasPage()) {
			return false;
		}

		checkActivePage();

		lock.lock();
		try {
			closePage0();
		} finally {
			lock.unlock();
		}

		return true;
	}

	protected ItemLayer getPrimaryLayer() {
		return getView().getScope().getPrimaryLayer();
	}

	/**
	 * Must be called under lock!
	 *
	 * @throws InterruptedException
	 */
	protected void closePage0() throws InterruptedException {

		final int pageIndex = currentPage.getPageIndex();
		final IndexSet indices = currentPage.getIndices();

		firePageClosing(pageIndex);

		currentPage.setPageState(PageState.CLOSING);
		try {
			itemLayerManager.release(IndexUtils.wrap(indices), getPrimaryLayer());
		} catch (ModelException e) {
			firePageFailed(pageIndex, e);
			throw new ModelException(GlobalErrorCode.DELEGATION_FAILED, "Failed to close page: "+pageIndex, e);
		} finally {
			currentPage.setIndices(null);
			currentPage.setPageState(PageState.BLANK);
		}

		firePageClosed(pageIndex);
	}

	/**
	 * Must be called under lock!
	 *
	 * @param pageIndex
	 * @param indices
	 * @throws InterruptedException
	 */
	protected void loadPage0(final int pageIndex, final IndexSet indices) throws InterruptedException {

		final int size = indices.size();

		firePageLoading(pageIndex, size);

		currentPage.setPageState(PageState.LOADING);
		try {
			itemLayerManager.load(IndexUtils.wrap(indices), getPrimaryLayer());

			currentPage.setPageIndex(pageIndex);
			currentPage.setIndices(indices);
			currentPage.setPageState(PageState.LOADED);
		} catch (ModelException e) {
			firePageFailed(pageIndex, e);
			currentPage.setPageState(PageState.BLANK);
			throw new ModelException(GlobalErrorCode.DELEGATION_FAILED, "Failed to load page: "+pageIndex, e);
		}

		firePageLoaded(pageIndex, size);
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.CorpusView.PageControl#isPageLoaded()
	 */
	@Override
	public boolean isPageLoaded() {
		checkActiveView();
		return currentPage.isLoaded();
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.CorpusView.PageControl#addPageListener(de.ims.icarus2.model.api.events.PageListener)
	 */
	@Override
	public void addPageListener(PageListener listener) {
		removePageListener(listener);
		pageListeners.add(listener);
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.CorpusView.PageControl#removePageListener(de.ims.icarus2.model.api.events.PageListener)
	 */
	@Override
	public void removePageListener(PageListener listener) {
		pageListeners.remove(listener);
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.CorpusView.PageControl#isLocked()
	 */
	@Override
	public boolean isLocked() {
		return pageLock.isLocked();
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.CorpusView.PageControl#lock(java.lang.Object)
	 */
	@Override
	public void lock(Object key) {
		pageLock.lock(key);
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.CorpusView.PageControl#unlock(java.lang.Object)
	 */
	@Override
	public void unlock(Object key) {
		pageLock.unlock(key);
	}


	protected void firePageClosing(int page) {
		for(PageListener listener : pageListeners) {
			listener.pageClosing(this, page);
		}
	}

	protected void firePageClosed(int page) {
		for(PageListener listener : pageListeners) {
			listener.pageClosed(this, page);
		}
	}

	protected void firePageLoading(int page, int size) {
		for(PageListener listener : pageListeners) {
			listener.pageLoading(this, page, size);
		}
	}

	protected void firePageLoaded(int page, int size) {
		for(PageListener listener : pageListeners) {
			listener.pageLoaded(this, page, size);
		}
	}

	protected void firePageFailed(int page, ModelException ex) {
		for(PageListener listener : pageListeners) {
			listener.pageFailed(this, page, ex);
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	protected static class PageLock {
		private Reference<?> keyRef;

		private Object getKey() {
			Object key = null;

			if(keyRef!=null) {
				key = keyRef.get();

				if(key==null) {
					keyRef = null;
					throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
							"Pending key reference present - previous lock not released");
				}
			}

			return key;
		}

		public synchronized boolean isLocked() {
			return getKey()!=null;
		}

		public synchronized void lock(Object key) {
			checkNotNull(key);

			Object currentKey = getKey();

			if(currentKey==key) {
				return;
			} else if(currentKey==null) {
				keyRef = new WeakReference<Object>(key);
			} else
				throw new ModelException(ModelErrorCode.VIEW_LOCKED, "Page already locked by key: "+currentKey);
		}

		public synchronized void unlock(Object key) {
			checkNotNull(key);

			Object currentKey = getKey();

			if(currentKey==key) {
				keyRef = null;
			} else
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Page currently not locked by key: "+key);
		}

		public synchronized void clearLock() {
			keyRef = null;
		}
	}

	protected static enum PageState {

		/**
		 * Page does not contain any data
		 */
		BLANK,

		/**
		 * Page is in the process of being populated
		 * <p>
		 * When successful the state will be {@link #LOADED}, otherwise {@link #.BLANK}
		 */
		LOADING,

		/**
		 * Page is fully operational and populated
		 */
		LOADED,

		/**
		 * Page is in the process of being emptied
		 * <p>
		 * When closed successfully the new state will be {@link #BLANK}, otherwise remain
		 * unchanged (which depending on the driver implementations can lead to inconsistent states!).
		 */
		CLOSING,
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	protected static class Page {
		private int pageIndex = NO_PAGE_INDEX;
		private IndexSet indices = null;
		private PageState pageState = PageState.BLANK;

		public synchronized IndexSet getIndices() {
			if(indices==null)
				throw new ModelException(ModelErrorCode.VIEW_EMPTY,
						"Missing indices data for page: "+pageIndex);

			return indices;
		}

		public synchronized int getPageIndex() {
			return pageIndex;
		}

		public synchronized boolean isPageIndex(int newPageIndex) {
			return pageIndex==newPageIndex;
		}

		public synchronized boolean setPageIndex(int newPageIndex) {
			boolean requiresReload = newPageIndex!=pageIndex;

			if(requiresReload) {
				pageIndex = newPageIndex;
				indices = null;
			}

			return requiresReload;
		}

		public synchronized boolean canLoad() {
			return pageState == PageState.BLANK;
		}

		public synchronized boolean canClose() {
			return pageState == PageState.LOADED;
		}

		public synchronized void setIndices(IndexSet indices) {
			this.indices = indices;
		}

		public synchronized void setPageState(PageState pageState) {
			this.pageState = pageState;
		}

		public synchronized boolean isLoading() {
			return pageState == PageState.LOADING;
		}

		public synchronized boolean isLoaded() {
			return pageState == PageState.LOADED;
		}

		public synchronized boolean isClosing() {
			return pageState == PageState.CLOSING;
		}

		public synchronized boolean isBlank() {
			return pageState == PageState.BLANK;
		}

		public synchronized boolean hasPage() {
			return pageIndex!=NO_PAGE_INDEX;
		}
	}

	public static class PageControlBuilder extends AbstractBuilder<PageControlBuilder, PageControl> {
		private PageIndexBuffer pageIndexBuffer;
		private IndexSet[] indices;
		private ItemLayerManager itemLayerManager;
		private Cache<Integer, IndexSet> indexSetCache;
		private int indexCacheSize;
		private int pageSize;

		public PageControlBuilder pageIndexBuffer(PageIndexBuffer pageIndexBuffer) {
			checkNotNull(pageIndexBuffer);
			checkState(this.pageIndexBuffer==null);

			this.pageIndexBuffer = pageIndexBuffer;

			return thisAsCast();
		}

		public PageIndexBuffer getPageIndexBuffer() {
			return pageIndexBuffer;
		}

		public PageControlBuilder indices(IndexSet[] indices) {
			checkNotNull(indices);
			checkState(this.indices==null);

			this.indices = indices;

			return thisAsCast();
		}

		public IndexSet[] getIndices() {
			return indices;
		}

		public PageControlBuilder itemLayerManager(ItemLayerManager itemLayerManager) {
			checkNotNull(itemLayerManager);
			checkState(this.itemLayerManager==null);

			this.itemLayerManager = itemLayerManager;

			return thisAsCast();
		}

		public ItemLayerManager getItemLayerManager() {
			return itemLayerManager;
		}

		public PageControlBuilder indexSetCache(Cache<Integer, IndexSet> indexSetCache) {
			checkNotNull(indexSetCache);
			checkState(this.indexSetCache==null);

			this.indexSetCache = indexSetCache;

			return thisAsCast();
		}

		public Cache<Integer, IndexSet> getIndexSetCache() {
			return indexSetCache;
		}

		public PageControlBuilder indexCacheSize(int indexCacheSize) {
			checkArgument(indexCacheSize>0);
			checkState(this.indexCacheSize==0);

			this.indexCacheSize = indexCacheSize;

			return thisAsCast();
		}

		public int getIndexCacheSize() {
			return indexCacheSize;
		}

		public PageControlBuilder pageSize(int pageSize) {
			checkArgument(pageSize>0);
			checkState(this.pageSize==0);

			this.pageSize = pageSize;

			return thisAsCast();
		}

		public int getPageSize() {
			return pageSize;
		}

		@Override
		protected void validate() {
			checkState("Missing item layer manager", itemLayerManager!=null);

			checkState("Must either provide a finished page index buffer or both a valid set of indices and a positive page size",
					pageIndexBuffer!=null || (indices!=null && pageSize>0));
		}

		@Override
		public DefaultPageControl create() {
			return new DefaultPageControl(this);
		}
	}
}
