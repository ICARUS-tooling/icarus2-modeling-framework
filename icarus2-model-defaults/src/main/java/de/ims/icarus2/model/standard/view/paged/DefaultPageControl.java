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
package de.ims.icarus2.model.standard.view.paged;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives.unbox;
import static java.util.Objects.requireNonNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Api.ApiType;
import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.apiguard.Guarded.MethodType;
import de.ims.icarus2.apiguard.Mandatory;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.events.PageListener;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.AbstractPart;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Link;
import de.ims.icarus2.util.mem.ReferenceType;
import de.ims.icarus2.util.strings.NamedObject;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(PageControl.class)
@Assessable
public class DefaultPageControl extends AbstractPart<PagedCorpusView> implements PageControl {

	public static Builder builder() {
		return new Builder();
	}

	@Link
	protected final PageIndexBuffer pageBuffer;
	@Link
	protected final ItemLayerManager itemLayerManager;

	@de.ims.icarus2.util.mem.Reference(ReferenceType.DOWNLINK)
	protected final Cache<Integer, IndexSet> indexSetCache;

	@de.ims.icarus2.util.mem.Reference(ReferenceType.DOWNLINK)
	protected final Page currentPage = new Page();
	@de.ims.icarus2.util.mem.Reference(ReferenceType.DOWNLINK)
	protected final PageLock pageLock = new PageLock();

	@de.ims.icarus2.util.mem.Reference(ReferenceType.DOWNLINK)
	protected final ReentrantLock lock = new ReentrantLock();

	@de.ims.icarus2.util.mem.Reference(ReferenceType.DOWNLINK)
	protected final List<PageListener> pageListeners = new CopyOnWriteArrayList<>();

	protected DefaultPageControl(Builder builder) {
		requireNonNull(builder);

		PageIndexBuffer pageIndexBuffer = builder.getPageIndexBuffer();
		if(pageIndexBuffer==null) {
			pageIndexBuffer = new PageIndexBuffer(builder.getIndices(), builder.getPageSize());
		}

		pageBuffer = pageIndexBuffer;

		itemLayerManager = builder.getItemLayerManager();

		Cache<Integer, IndexSet> indexSetCache = builder.getIndexSetCache();
		if(indexSetCache==null) {
			int indexSetCacheSize = builder.getIndexCacheSize();
			if(indexSetCacheSize>0) {
				indexSetCache = CacheBuilder.newBuilder()
						.maximumSize(indexSetCacheSize)
						.weakValues()
						.build(new CacheLoader<Integer, IndexSet>(){

							@Override
							public IndexSet load(Integer key) throws Exception {
								return DefaultPageControl.this.pageBuffer.createPage(unbox(key));
							}
						});
			}
		}

		this.indexSetCache = indexSetCache;
	}

	protected IndexSet loadIndices(int pageIndex) {
		IndexSet indices = null;

		final boolean isLoadingCache = LoadingCache.class.isInstance(indexSetCache);

		if(indexSetCache!=null) {
			// If our cache is set we have to distinguish between loading cache and
			// a static one provided by client code.
			// In case of a loading cache we have some additional error handling to do.
			if(isLoadingCache) {
				try {
					indices = ((LoadingCache<Integer, IndexSet>)indexSetCache).get(_int(pageIndex));
				} catch (ExecutionException e) {
					throw ModelException.unwrap(e);
				}
			} else {
				indices = indexSetCache.getIfPresent(_int(pageIndex));
			}
		}

		if(indices==null) {
			// Manually crate the page
			indices = pageBuffer.createPage(pageIndex);

			// Insert page into cache if one is available and it's not a LoadingCache
			if(!isLoadingCache && indexSetCache!=null && indices!=null) {
				indexSetCache.put(_int(pageIndex), indices);
			}
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
					Messages.indexOutOfBounds("Page index out of bounds",
							0, pageBuffer.getPageCount()-1, pageIndex));
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#getView()
	 */
	@Override
	public PagedCorpusView getView() {
		checkAdded();
		return getOwner();
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#getPageSize()
	 */
	@Override
	public int getPageSize() {
		checkActiveView();
		return pageBuffer.getPageSize();
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#getPageCount()
	 */
	@Override
	public int getPageCount() {
		checkActiveView();
		return pageBuffer.getPageCount();
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#getIndices()
	 */
	@Override
	public IndexSet getIndices() {
		checkActiveView();
		checkActivePage();
		return currentPage.getIndices();
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#getPageIndex()
	 */
	@Override
	public int getPageIndex() {
		checkActiveView();
		return currentPage.getPageIndex();
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#loadPage(int)
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
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#closePage()
	 */
	@Override
	public boolean closePage() throws InterruptedException {
		checkAdded();

		// Only check for locks if the surrounding view is still active
		if(getView().isActive()) {
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
		} catch (IcarusApiException e) {
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
		} catch (IcarusApiException e) {
			firePageFailed(pageIndex, e);
			currentPage.setPageState(PageState.BLANK);
			throw new ModelException(GlobalErrorCode.DELEGATION_FAILED, "Failed to load page for index: "+pageIndex, e);
		}

		firePageLoaded(pageIndex, size);
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#isPageLoaded()
	 */
	@Override
	public boolean isPageLoaded() {
		checkActiveView();
		return currentPage.isLoaded();
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#addPageListener(de.ims.icarus2.model.api.events.PageListener)
	 */
	@Override
	public void addPageListener(PageListener listener) {
		requireNonNull(listener);

		removePageListener(listener);
		pageListeners.add(listener);
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#removePageListener(de.ims.icarus2.model.api.events.PageListener)
	 */
	@Override
	public void removePageListener(PageListener listener) {
		requireNonNull(listener);

		pageListeners.remove(listener);
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#isLocked()
	 */
	@Override
	public boolean isLocked() {
		return pageLock.isLocked();
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#lock(NamedObject)
	 */
	@Override
	public void lock(NamedObject key) {
		pageLock.lock(key);
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl#unlock(NamedObject)
	 */
	@Override
	public void unlock(NamedObject key) {
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

	protected void firePageFailed(int page, IcarusApiException ex) {
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
		private Reference<NamedObject> keyRef;

		private NamedObject getKeyUnsafe() {
			NamedObject key = null;

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
			return getKeyUnsafe()!=null;
		}

		public synchronized void lock(NamedObject key) {
			requireNonNull(key);

			NamedObject currentKey = getKeyUnsafe();

			if(currentKey==key) {
				return;
			} else if(currentKey==null) {
				keyRef = new WeakReference<>(key);
			} else
				throw new ModelException(ModelErrorCode.VIEW_LOCKED, "Page already locked by key: "+currentKey.getName());
		}

		public synchronized void unlock(NamedObject key) {
			requireNonNull(key);

			Object currentKey = getKeyUnsafe();

			if(currentKey==key) {
				keyRef = null;
			} else
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Page currently not locked by key: "+key.getName());
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

	@Api(type=ApiType.BUILDER)
	public static class Builder extends AbstractBuilder<Builder, DefaultPageControl> {
		private PageIndexBuffer pageIndexBuffer;
		private IndexSet[] indices;
		private ItemLayerManager itemLayerManager;
		private Cache<Integer, IndexSet> indexSetCache;
		private int indexCacheSize;
		private int pageSize;

		protected Builder() {
			// no-op
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder pageIndexBuffer(PageIndexBuffer pageIndexBuffer) {
			requireNonNull(pageIndexBuffer);
			checkState(this.pageIndexBuffer==null);

			this.pageIndexBuffer = pageIndexBuffer;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public PageIndexBuffer getPageIndexBuffer() {
			return pageIndexBuffer;
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder indices(IndexSet[] indices) {
			requireNonNull(indices);
			checkArgument(indices.length>0);
			checkState(this.indices==null);

			this.indices = indices;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public IndexSet[] getIndices() {
			return indices;
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder itemLayerManager(ItemLayerManager itemLayerManager) {
			requireNonNull(itemLayerManager);
			checkState(this.itemLayerManager==null);

			this.itemLayerManager = itemLayerManager;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public ItemLayerManager getItemLayerManager() {
			return itemLayerManager;
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder indexSetCache(Cache<Integer, IndexSet> indexSetCache) {
			requireNonNull(indexSetCache);
			checkState(this.indexSetCache==null);

			this.indexSetCache = indexSetCache;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public Cache<Integer, IndexSet> getIndexSetCache() {
			return indexSetCache;
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder indexCacheSize(int indexCacheSize) {
			checkArgument(indexCacheSize>0);
			checkState(this.indexCacheSize==0);

			this.indexCacheSize = indexCacheSize;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="0")
		public int getIndexCacheSize() {
			return indexCacheSize;
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder pageSize(int pageSize) {
			checkArgument(pageSize>0);
			checkState(this.pageSize==0);

			this.pageSize = pageSize;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER, defaultValue="0")
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
