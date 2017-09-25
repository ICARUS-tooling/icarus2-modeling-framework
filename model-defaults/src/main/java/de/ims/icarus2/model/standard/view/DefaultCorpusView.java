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
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.event.ChangeListener;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.api.view.CorpusModel;
import de.ims.icarus2.model.api.view.CorpusOwner;
import de.ims.icarus2.model.api.view.CorpusView;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.AbstractPart;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.classes.Lazy;
import de.ims.icarus2.util.events.ChangeSource;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultCorpusView extends AbstractPart<Corpus> implements CorpusView {

	public static final int DEFAULT_PAGE_CACHE_SIZE = 10;

	// Environment
	protected final Corpus corpus;
	protected final Scope scope;
	protected final AccessMode accessMode;
	protected final ChangeSource changeSource;

	// Lifecycle states
	protected final Set<CorpusOwner> owners;
	protected volatile boolean closed = false;
	protected volatile boolean closing = false;

	// View content
	protected final IndexSet[] indices;
	protected final long size;
	protected final int pageSize;
	protected final ItemLayerManager itemLayerManager;

	// Lazy members
	protected final Lazy<PageControl> pageControl;
	protected final Lazy<CorpusModel> model;

	protected DefaultCorpusView(CorpusViewBuilder builder) {
		requireNonNull(builder);

		scope = builder.getScope();
		accessMode = builder.getAccessMode();
		corpus = builder.getScope().getCorpus();
		indices = builder.getIndices();
		pageSize = builder.getPageSize();
		itemLayerManager = builder.getItemLayerManager();

		size = IndexUtils.count(indices);

		owners = new ReferenceOpenHashSet<>();
		changeSource = new ChangeSource(this);

		pageControl = Lazy.create(this::createPageControl);

		model = Lazy.create(this::createModel);
	}

	protected PageIndexBuffer createPageIndexBuffer(IndexSet[] indices, int pageSize) {
		return new PageIndexBuffer(indices, pageSize);
	}

	public PageControl createPageControl() {
		PageControl pageControl = new DefaultPageControl.Builder()
			.indices(indices)
			.itemLayerManager(itemLayerManager)
			.pageSize(pageSize)
			.build();

		pageControl.addNotify(this);

		return pageControl;
	}

	protected CorpusModel createModel() {
		CorpusModel model = new DefaultCorpusModel.Builder()
			.accessMode(accessMode)
			.itemLayerManager(itemLayerManager)
			.build();

		model.addNotify(this);

		return model;
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		changeSource.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		changeSource.removeChangeListener(listener);
	}

	/**
	 * @see de.ims.icarus2.model.api.view.CorpusView#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @see de.ims.icarus2.model.api.view.CorpusView#getScope()
	 */
	@Override
	public Scope getScope() {
		return scope;
	}

	/**
	 *  Only to be called under 'owners' lock
	 */
	protected final void checkOpen() {
		if(closed)
			throw new ModelException(getCorpus(), ModelErrorCode.VIEW_CLOSED, "View already closed"); //$NON-NLS-1$
		if(closing)
			throw new ModelException(getCorpus(), ModelErrorCode.VIEW_CLOSED, "View is closing down"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus2.model.api.view.CorpusView#closable()
	 */
	@Override
	public boolean closable() {
		synchronized (owners) {
			checkOpen();

			return owners.isEmpty();
		}
	}

	/**
	 * @throws InterruptedException
	 * @see de.ims.icarus2.model.api.view.CorpusView#close()
	 */
	@Override
	public void close() throws InterruptedException {
		synchronized (owners) {
			checkOpen();

			//TODO change policy so that we ask ALL owners before throwing on exception in case any of them failed to release the view!
			for(Iterator<CorpusOwner> it = owners.iterator(); it.hasNext();) {
				CorpusOwner owner = it.next();

				if(owner.release()) {
					it.remove();
				} else
					throw new ModelException(getCorpus(), ModelErrorCode.VIEW_UNCLOSABLE,
							"Unable to close view - could not release ownership of "+owner.getName()); //$NON-NLS-1$
			}

			closing = true;
		}

		// At this point no owners may prevent the view from closing.
		// Therefore simply free the content of the current page and remove both model and page control
		try {

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
		} finally {
			closed = true;
			changeSource.fireStateChanged();
		}
	}

	@Override
	public AccessMode getAccessMode() {
		return accessMode;
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
	public boolean isActive() {
		synchronized (owners) {
			return !closed && !closing;
		}
	}

	@Override
	public PageControl getPageControl() {
		return pageControl.value();
	}

	@Override
	public CorpusModel getModel() {
		return model.value();
	}

	/**
	 * Creates a fresh unmodifiable copy of the current collection of
	 * owners that hold partial ownership of this view.
	 *
	 * @see de.ims.icarus2.model.api.view.CorpusView#getOwners()
	 */
	@Override
	public Set<CorpusOwner> getOwners() {
		synchronized (owners) {
			return Collections.unmodifiableSet(new HashSet<>(owners));
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.view.CorpusView#acquire(de.ims.icarus2.model.api.view.CorpusOwner)
	 */
	@Override
	public void acquire(CorpusOwner owner) throws ModelException {
		if (owner == null)
			throw new NullPointerException("Invalid owner"); //$NON-NLS-1$

		synchronized (owners) {
			checkOpen();

//			if(owners.contains(owner)) {
//				return;
//			}
//
			owners.add(owner);
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.view.CorpusView#release(de.ims.icarus2.model.api.view.CorpusOwner)
	 */
	@Override
	public void release(CorpusOwner owner) throws ModelException {
		if (owner == null)
			throw new NullPointerException("Invalid owner"); //$NON-NLS-1$

		synchronized (owners) {
			checkOpen();

			if(!owners.remove(owner))
				throw new ModelException(getCorpus(), GlobalErrorCode.INVALID_INPUT,
						"Owner does not hold ownership of this view: "+owner.getName()); //$NON-NLS-1$

			//TODO do additional processing or automatic closing?
		}
	}

	public static class CorpusViewBuilder extends AbstractBuilder<CorpusViewBuilder, CorpusView> {

		private Scope scope;
		private AccessMode accessMode;
		private ItemLayerManager itemLayerManager;
		private IndexSet[] indices;
		private int pageSize;

		public CorpusViewBuilder scope(Scope scope) {
			requireNonNull(scope);
			checkState(this.scope==null);

			this.scope = scope;

			return thisAsCast();
		}

		public Scope getScope() {
			return scope;
		}

		public CorpusViewBuilder accessMode(AccessMode accessMode) {
			requireNonNull(accessMode);
			checkState(this.accessMode==null);

			this.accessMode = accessMode;

			return thisAsCast();
		}

		public AccessMode getAccessMode() {
			return accessMode;
		}

		public CorpusViewBuilder indices(IndexSet[] indices) {
			requireNonNull(indices);
			checkState(this.indices==null);

			this.indices = indices;

			return thisAsCast();
		}

		public IndexSet[] getIndices() {
			return indices;
		}

		public CorpusViewBuilder itemLayerManager(ItemLayerManager itemLayerManager) {
			requireNonNull(itemLayerManager);
			checkState(this.itemLayerManager==null);

			this.itemLayerManager = itemLayerManager;

			return thisAsCast();
		}

		public ItemLayerManager getItemLayerManager() {
			return itemLayerManager;
		}

		public CorpusViewBuilder pageSize(int pageSize) {
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
			checkState("Missing scope", scope!=null);
			checkState("Missing access mode", accessMode!=null);
			checkState("Missing item layer manager", itemLayerManager!=null);
			checkState("Missing indices", indices!=null);
			checkState("Missing page size", pageSize>0);
		}

		@Override
		protected DefaultCorpusView create() {
			return new DefaultCorpusView(this);
		}
	}
}
