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
 */
package de.ims.icarus2.model.standard.corpus;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.GenerationControl;
import de.ims.icarus2.model.api.edit.CorpusEditEvents;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.util.events.EventListener;
import de.ims.icarus2.util.events.EventObject;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultGeneration implements GenerationControl, EventListener {

	public static final String KEY_STAGE = "generation.stage";
	public static final String KEY_MAX_STAGE = "generation.maxStage";

	private final Corpus corpus;

	private final AtomicLong stage = new AtomicLong(INITIAL_STAGE);

	private long maxStage = INITIAL_STAGE;

	/**
	 * @param corpus
	 */
	public DefaultGeneration(Corpus corpus) {
		checkNotNull(corpus);
		checkState("Corpus must be editable to support mutable generation control", corpus.getManifest().isEditable());

		this.corpus = corpus;

		loadStage();
	}

	protected void loadStage() {
		MetadataRegistry registry = getCorpus().getMetadataRegistry();

		stage.set(registry.getLongValue(KEY_STAGE, INITIAL_STAGE));
		maxStage = registry.getLongValue(KEY_MAX_STAGE, INITIAL_STAGE);
	}

	/**
	 * Persists the current stage in the corpus' {@link MetadataRegistry}.
	 * <p>
	 * Note that this method tries to acquire the global lock for the corpus
	 * to ensure consistency of the two saved values.
	 *
	 * @throws ModelException in case the corpus lock is held by a foreign thread
	 */
	protected void storeStage() {

		Lock lock = getCorpus().getLock();

		// Check for lock free or held by current thread
		if(lock.tryLock()) {
			try {
				MetadataRegistry registry = getCorpus().getMetadataRegistry();
				registry.beginUpdate();
				try {
					registry.setLongValue(KEY_STAGE, getStage());
					registry.setLongValue(KEY_MAX_STAGE, maxStage);
				} finally {
					registry.endUpdate();
				}
			} finally {
				lock.unlock();
			}
		} else
			throw new ModelException(ModelErrorCode.EDIT_UNSYNCHRONIZED_ACCESS,
				"Unable to persist generation stage - corpus lock held by foreign thread");
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.GenerationControl#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.GenerationControl#getStage()
	 */
	@Override
	public long getStage() {
		return stage.get();
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.GenerationControl#advance()
	 */
	@Override
	public long advance() {
		Lock lock = getCorpus().getLock();

		// Check for lock free or held by current thread
		if(lock.tryLock()) {
			try {
				long newStage = ++maxStage;

				stage.set(newStage);

				return newStage;
			} finally {
				lock.unlock();
			}
		} else
			throw new ModelException(ModelErrorCode.EDIT_UNSYNCHRONIZED_ACCESS,
					"Unable to increment generation stage - corpus lock held by foreign thread");
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.GenerationControl#step(long, long)
	 */
	@Override
	public boolean step(long expectedId, long newId) {
		checkArgument(expectedId>=INITIAL_STAGE);
		checkArgument(newId>=INITIAL_STAGE);

		Lock lock = getCorpus().getLock();

		// Check for lock free or held by current thread
		if(lock.tryLock()) {
			try {
				if(stage.compareAndSet(expectedId, newId)) {
					if(newId>maxStage) {
						maxStage = newId;
					}

					return true;
				} else {
					return false;
				}
			} finally {
				lock.unlock();
			}
		} else
			throw new ModelException(ModelErrorCode.EDIT_UNSYNCHRONIZED_ACCESS,
					"Unable to change generation stage - corpus lock held by foreign thread");
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.GenerationControl#close()
	 */
	@Override
	public void close() {
		storeStage();
	}

	/**
	 * @see de.ims.icarus2.util.events.EventListener#invoke(java.lang.Object, de.ims.icarus2.util.events.EventObject)
	 */
	@Override
	public void invoke(Object sender, EventObject event) {
		switch (event.getName()) {

		// If an atomic change occurs for the first time we simple do a generation step forward
		case CorpusEditEvents.EXECUTE:
			advance();
			break;

		default:
			break;
		}
	}
}
