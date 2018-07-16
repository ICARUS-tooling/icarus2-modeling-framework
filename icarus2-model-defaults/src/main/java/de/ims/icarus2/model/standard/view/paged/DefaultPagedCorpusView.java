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
package de.ims.icarus2.model.standard.view.paged;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.api.view.paged.CorpusModel;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;
import de.ims.icarus2.model.standard.view.AbstractCorpusView;
import de.ims.icarus2.util.lang.Lazy;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultPagedCorpusView extends AbstractCorpusView implements PagedCorpusView {

	public static Builder newBuilder() {
		return new Builder();
	}

	public static final int DEFAULT_PAGE_CACHE_SIZE = 10;

	// View content
	protected final IndexSet[] indices;
	protected final long size;
	protected final int pageSize;
	protected final ItemLayerManager itemLayerManager;

	// Lazy members
	protected final Lazy<PageControl> pageControl;
	protected final Lazy<CorpusModel> model;

	protected DefaultPagedCorpusView(Builder builder) {
		super(builder);

		indices = builder.getIndices();
		pageSize = builder.getPageSize();
		itemLayerManager = builder.getItemLayerManager();

		size = IndexUtils.count(indices);

		pageControl = Lazy.create(this::createPageControl);

		model = Lazy.create(this::createModel);
	}

	protected PageIndexBuffer createPageIndexBuffer(IndexSet[] indices, int pageSize) {
		return new PageIndexBuffer(indices, pageSize);
	}

	public PageControl createPageControl() {
		PageControl pageControl = DefaultPageControl.newBuilder()
			.indices(indices)
			.itemLayerManager(itemLayerManager)
			.pageSize(pageSize)
			.build();

		pageControl.addNotify(this);

		return pageControl;
	}

	protected CorpusModel createModel() {
		CorpusModel model = DefaultCorpusModel.newBuilder()
			.accessMode(accessMode)
			.itemLayerManager(itemLayerManager)
			.build();

		model.addNotify(this);

		return model;
	}

	/**
	 * @see de.ims.icarus2.model.standard.view.AbstractCorpusView#closeImpl()
	 */
	@Override
	protected void closeImpl() throws InterruptedException {
		// Simply free the content of the current page and remove both model and page control

		if(pageControl.created()) {
			PageControl pageControl = getPageControl();
			//FIXME page control may be locked, resulting in exception at this point -> inconsistent 'closed' state
			pageControl.closePage();
			pageControl.removeNotify(this);
		}

		if(model.created()) {
			CorpusModel model = getModel();

			model.removeNotify(this);
		}
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public int getPageSize() {
		return pageSize;
	}

	@Override
	public PageControl getPageControl() {
		return pageControl.value();
	}

	@Override
	public CorpusModel getModel() {
		return model.value();
	}

	public static class Builder extends AbstractCorpusView.Builder<Builder, DefaultPagedCorpusView> {
		private ItemLayerManager itemLayerManager;
		private IndexSet[] indices;
		private int pageSize;

		protected Builder() {
			// no-op
		}

		public Builder indices(IndexSet[] indices) {
			requireNonNull(indices);
			checkState(this.indices==null);

			this.indices = indices;

			return thisAsCast();
		}

		public IndexSet[] getIndices() {
			return indices;
		}

		public Builder itemLayerManager(ItemLayerManager itemLayerManager) {
			requireNonNull(itemLayerManager);
			checkState(this.itemLayerManager==null);

			this.itemLayerManager = itemLayerManager;

			return thisAsCast();
		}

		public ItemLayerManager getItemLayerManager() {
			return itemLayerManager;
		}

		public Builder pageSize(int pageSize) {
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
			super.validate();

			checkState("Missing item layer manager", itemLayerManager!=null);
			checkState("Missing indices", indices!=null);
			checkState("Missing page size", pageSize>0);
		}

		@Override
		protected DefaultPagedCorpusView create() {
			return new DefaultPagedCorpusView(this);
		}
	}
}
