/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Api.ApiType;
import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.apiguard.Guarded.MethodType;
import de.ims.icarus2.apiguard.Mandatory;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.api.view.paged.CorpusModel;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;
import de.ims.icarus2.model.standard.view.AbstractCorpusView;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.lang.Lazy;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(PagedCorpusView.class)
public class DefaultPagedCorpusView extends AbstractCorpusView implements PagedCorpusView {

	public static Builder builder() {
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

	protected PageControl createPageControl() {
		PageControl pageControl = DefaultPageControl.builder()
			.indices(indices)
			.itemLayerManager(itemLayerManager)
			.pageSize(pageSize)
			.build();

		pageControl.addNotify(this);

		return pageControl;
	}

	protected CorpusModel createModel() {
		CorpusModel model = DefaultCorpusModel.builder()
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

	@Api(type=ApiType.BUILDER)
	public static class Builder extends AbstractCorpusView.Builder<Builder, DefaultPagedCorpusView> {
		private ItemLayerManager itemLayerManager;
		private IndexSet[] indices;
		private int pageSize;

		protected Builder() {
			// no-op
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder indices(IndexSet[] indices) {
			requireNonNull(indices);
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
		@Mandatory
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
