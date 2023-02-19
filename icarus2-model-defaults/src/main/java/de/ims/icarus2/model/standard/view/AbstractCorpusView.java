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
package de.ims.icarus2.model.standard.view;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.apiguard.Guarded.MethodType;
import de.ims.icarus2.apiguard.Mandatory;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.CorpusOwner;
import de.ims.icarus2.model.api.corpus.OwnableCorpusPart;
import de.ims.icarus2.model.api.view.CorpusView;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.AbstractPart;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.events.ChangeSource;
import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Link;
import de.ims.icarus2.util.mem.Reference;
import de.ims.icarus2.util.mem.ReferenceType;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;

/**
 * @author Markus Gärtner
 *
 */
@Assessable
public abstract class AbstractCorpusView extends AbstractPart<Corpus> implements CorpusView, OwnableCorpusPart {

	private static final Logger log = LoggerFactory.getLogger(AbstractCorpusView.class);

	// Environment
	@Link(type=ReferenceType.UPLINK,cache=true)
	protected final Corpus corpus;
	@Link(type=ReferenceType.UPLINK,cache=true)
	protected final Scope scope;
	@Link(type=ReferenceType.UPLINK,cache=true)
	protected final AccessMode accessMode;
	@Reference(ReferenceType.DOWNLINK)
	protected final ChangeSource changeSource;

	// Lifecycle states
	protected final ReferenceSet<CorpusOwner> owners;
	protected volatile boolean closed = false;
	protected volatile boolean closing = false;

	protected AbstractCorpusView(Builder<?, ?> builder) {
		requireNonNull(builder);

		scope = builder.getScope();
		accessMode = builder.getAccessMode();
		corpus = builder.getScope().getCorpus();

		owners = new ReferenceOpenHashSet<>();
		changeSource = new ChangeSource(this);
	}

	/**
	 * Asks all the still present owners to release this view and
	 * throws {@link ModelException} of type {@link ModelErrorCode#VIEW_UNCLOSABLE}
	 * if any of them fails to do so.
	 *
	 * @throws ModelException of type {@link ModelErrorCode#VIEW_UNCLOSABLE} if at least
	 * one owner failed to {@link CorpusOwner#release()} this view
	 *
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		synchronized (owners) {
			// Always good to ensure the method is idempotent
			if(closed || closing) {
				return;
			}

			LazyCollection<String> blockingOwners = LazyCollection.lazyList();

			for(Iterator<CorpusOwner> it = owners.iterator(); it.hasNext();) {
				CorpusOwner owner = it.next();

				String name = owner.getName().get();

				try {
					if(owner.release()) {
						it.remove();
					} else {
						blockingOwners.add(name);
					}
				} catch (InterruptedException e) {
					log.error("Owner '{}' interrupted while releasing view", name, e);
					// treat it as regular fail
					blockingOwners.add(name);
				}
			}

			if(!blockingOwners.isEmpty())
				throw new ModelException(getCorpus(), ModelErrorCode.VIEW_UNCLOSABLE,
						"Unable to close view - could not release ownership of "+blockingOwners.toString()); //$NON-NLS-1$

			closing = true;
		}

		// At this point no owners may prevent the view from closing.
		// Delegate release of resources to subclass now.
		try {
			closeImpl();
		} catch (InterruptedException e) {
			throw new ModelException(GlobalErrorCode.INTERRUPTED,
					"Internal close method got itnerrupted", e);
		} finally {
			closed = true;
			closing = false;
			changeSource.fireStateChanged();
		}
	}

	/**
	 * Actual release method for all internally held resources.
	 *
	 * @throws InterruptedException
	 */
	protected abstract void closeImpl() throws InterruptedException;

	/**
	 * Creates a fresh unmodifiable copy of the current collection of
	 * owners that hold partial ownership of this view.
	 *
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView#getOwners()
	 */
	@Override
	public Set<CorpusOwner> getOwners() {
		synchronized (owners) {
			return Collections.unmodifiableSet(new HashSet<>(owners));
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView#acquire(de.ims.icarus2.model.api.corpus.CorpusOwner)
	 */
	@Override
	public void acquire(CorpusOwner owner) throws ModelException {
		requireNonNull(owner);

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
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView#release(de.ims.icarus2.model.api.corpus.CorpusOwner)
	 */
	@Override
	public void release(CorpusOwner owner) throws ModelException {
		requireNonNull(owner);

		synchronized (owners) {
			checkOpen();

			if(!owners.remove(owner))
				throw new ModelException(getCorpus(), GlobalErrorCode.INVALID_INPUT,
						"Owner does not hold ownership of this view: "+owner.getName()); //$NON-NLS-1$

			//TODO do additional processing or automatic closing?
		}
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
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView#getScope()
	 */
	@Override
	public Scope getScope() {
		return scope;
	}

	@Override
	public AccessMode getAccessMode() {
		return accessMode;
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
	 * @see de.ims.icarus2.model.api.view.paged.PagedCorpusView#closable()
	 */
	@Override
	public boolean closable() {
		synchronized (owners) {
			checkOpen();

			return owners.isEmpty();
		}
	}

	@Override
	public boolean isActive() {
		synchronized (owners) {
			return !closed && !closing;
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <B>
	 * @param <V>
	 */
	protected abstract static class Builder<B extends Builder<B, V>, V extends AbstractCorpusView> extends AbstractBuilder<B, V> {

		private Scope scope;
		private AccessMode accessMode;

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public B scope(Scope scope) {
			requireNonNull(scope);
			checkState(this.scope==null);

			this.scope = scope;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public Scope getScope() {
			return scope;
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public B accessMode(AccessMode accessMode) {
			requireNonNull(accessMode);
			checkState(this.accessMode==null);

			if(!isAccessModeSupported(accessMode))
				throw new ModelException(GlobalErrorCode.INVALID_INPUT,
						"Access mode not supported by this view: "+accessMode);

			this.accessMode = accessMode;

			return thisAsCast();
		}

		protected boolean isAccessModeSupported(AccessMode accessMode) {
			return true;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public AccessMode getAccessMode() {
			return accessMode;
		}

		@Override
		protected void validate() {
			checkState("Missing scope", scope!=null);
			checkState("Missing access mode", accessMode!=null);
		}
	}
}
