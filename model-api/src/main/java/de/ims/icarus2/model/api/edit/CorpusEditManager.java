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
package de.ims.icarus2.model.api.edit;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.util.events.EventObject;
import de.ims.icarus2.util.events.EventSource;
import de.ims.icarus2.util.events.WeakEventSource;

/**
 *
 *
 * @author Markus Gärtner
 *
 */
public class CorpusEditManager extends WeakEventSource {

	private static final long serialVersionUID = -8320136116919999917L;

	private AtomicInteger updateLevel = new AtomicInteger(0);
	private AtomicBoolean endingUpdate = new AtomicBoolean(false);
	private UndoableCorpusEdit currentEdit;

	private final Corpus corpus;

	private final List<CorpusUndoListener> undoListeners = new CopyOnWriteArrayList<>();

	public CorpusEditManager(Corpus corpus) {
		checkNotNull(corpus);

		this.corpus = corpus;

		currentEdit = createUndoableEdit(null);
	}

	public void beginUpdate() {
		fireEvent(new EventObject(CorpusEditEvents.BEGIN_UPDATE, "edit", currentEdit, "level", updateLevel.incrementAndGet()));
	}

	public boolean hasActiveUpdate() {
		return updateLevel.get()>0;
	}

	public void beginUpdate(String nameKey) {
		checkNotNull(nameKey);
		if(hasActiveUpdate())
			throw new IllegalStateException("Cannot start named edit '"+nameKey+"' while another edit is already in progress."); //$NON-NLS-1$ //$NON-NLS-2$

		currentEdit = createUndoableEdit(nameKey);

		// Delegate default handling to regular method
		beginUpdate();
	}

	public void endUpdate() {
		int level = updateLevel.getAndDecrement();

		if (!endingUpdate.get()) {
			boolean end = endingUpdate.compareAndSet(false, level==1);
			fireEvent(new EventObject(CorpusEditEvents.END_UPDATE, "edit", currentEdit, "level", level));

			try {
				if (end && !currentEdit.isEmpty()) {
					// Notify listeners about imminent undoable edit
					fireEvent(new EventObject(CorpusEditEvents.BEFORE_UNDO, "edit", currentEdit));

					// Copy and then reset current edit
					UndoableCorpusEdit publishedEdit = currentEdit;
					currentEdit = createUndoableEdit(null);

					// Finalize generation information of edit
					//FIXME should we ensure some proper synchronization on the generation control here?
					if(publishedEdit.getNewGenerationStage()==UndoableCorpusEdit.UNSET_GENERATION_STAGE) {
						publishedEdit.setNewGenerationStage(getCorpus().getGenerationControl().getStage());
					}

					// Allow edit to manage notification (per default this will fire a "change" event)
					publishedEdit.dispatch();

					// Notify listeners about executed undoable edit
					fireEvent(new EventObject(CorpusEditEvents.UNDO, "edit", publishedEdit));

					// Finally dispatch the edit so undo managers and other specialized
					// listeners can handle or accumulate it
					fireUndoableCorpusEdit(publishedEdit);
				}
			} finally {
				endingUpdate.set(false);
			}
		}
	}

	protected void dispatchEdit(UndoableCorpusEdit edit) {

		//TODO the following commented out part should be removed?

//		GenerationControl generationControl = getCorpus().getGenerationControl();
//		Lock lock = getCorpus().getLock();
//
//		lock.lock();
//		try {
//			long oldStage = edit.getOldGenerationStage();
//			long newStage;
//
//			// If it's the first time the given edit gets dispatched, only advance the generation stage and save it
//			if(oldStage==-1L) {
//				oldStage = generationControl.getStage();
//				newStage = generationControl.advance();
//			} else {
//				/* We're "reverting" from the post state of the given edit to its pre state,
//				 * therefore the stages get switched.
//				 *
//				 */
//				oldStage = edit.getNewGenerationStage();
//				newStage = edit.getOldGenerationStage();
//
//				// Expected "old" stage is the new stage after the edit has originally been performed
//				generationControl.step(oldStage, newStage);
//			}
//
//			edit.setOldGenerationStage(oldStage);
//			edit.setNewGenerationStage(newStage);
//		} finally {
//			lock.unlock();
//		}

		// END-TODO

		fireEvent(new EventObject(CorpusEditEvents.CHANGE, "edit", edit)); //$NON-NLS-1$
	}

	/**
	 * Allows subclasses to customize what implementation to use.
	 * <p>
	 * The default behavior is to create a new {@link UndoableCorpusEdit}
	 * with its {@link UndoableCorpusEdit#dispatch()} method overridden so
	 * that it forwards to an internal dispatcher method of this manager.
	 * <p>
	 * Note that this method is made public instead of protected so that
	 * serialization frameworks can exploit it for obtaining "correct" instances
	 * to use together with the active edit manager implementation.
	 *
	 * @return
	 */
	public UndoableCorpusEdit createUndoableEdit(final String nameKey) {
		return new UndoableCorpusEdit(getCorpus(), nameKey) {

			private static final long serialVersionUID = -471363052764925086L;

			/**
			 * Use the surrounding {@link EventSource} to publish the CHANGE event.
			 *
			 * @see de.ims.icarus2.model.api.edit.UndoableCorpusEdit#dispatch()
			 */
			@Override
			public void dispatch() {
				dispatchEdit(this);
			}

		};
	}

	/**
	 * @return the corpus
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	public void addCorpusUndoListener(CorpusUndoListener listener) {
		if(!undoListeners.contains(listener)) 	{
			undoListeners.add(listener);
		}
	}

	public void removeCorpusUndoListener(CorpusUndoListener listener) {
		undoListeners.remove(listener);
	}

	protected void fireUndoableCorpusEdit(UndoableCorpusEdit edit) {
		if(edit==null)
			throw new NullPointerException("Invalid edit"); //$NON-NLS-1$

		if(undoListeners.isEmpty()) {
			return;
		}

		for(CorpusUndoListener listener : undoListeners) {
			listener.undoableEditHappened(edit);
		}
	}

	/**
	 * Executes the given atomic change and adds it to the edit currently in
	 * progress. This is considered as a micro-transaction and therefore
	 * a full cycle of update level related methods is called. Note that if
	 * the change fails in its {@link AtomicChange#execute()} method by
	 * throwing an exception, the update level will remain unaffected and no
	 * update related listener notification will be performed.
	 * <p>
	 * Note that a change will only trigger a {@value CorpusEditEvents#EXECUTE}
	 * event the first time it is executed by the model. Subsequent executions
	 * (when undoing or redoing a change) will {@code not} result in events being
	 * fired for every atomic change, only for top level changes as a whole!
	 * Note further that each incoming change is <b>always</b> broadcasted via
	 * above mentioned event type in raw form, i.e. <b>before</b> any attempt of
	 * actually executing it will be made. This is so that consumers of atomic
	 * changes such as serialization frameworks have access to the raw representation
	 * and can obtain the metadata required for proper serialization from the
	 * unchanged pre-change state of the corpus resource.
	 *
	 * @param change
	 * @throws ModelException if the corpus is not editable
	 */
	public void execute(AtomicChange change) {

		if(!getCorpus().getManifest().isEditable())
			throw new ModelException(getCorpus(), GlobalErrorCode.UNSUPPORTED_OPERATION, "Corpus does not support modifications"); //$NON-NLS-1$

		Lock lock = getCorpus().getLock();

		lock.lock();
		try {
			UndoableCorpusEdit edit = currentEdit;
			long stage = getCorpus().getGenerationControl().getStage();

			// Fire the "raw" change before executing it
			fireEvent(new EventObject(CorpusEditEvents.EXECUTE, "change", change)); //$NON-NLS-1$

			// Execute change before update level is modified
			change.execute();

			/*
			 * If this is the first occasion we executed a change with a fresh new edit object
			 * it's time to initialize the 'oldGenerationStage' field.
			 * Note that we need that extra check since the setter methods for stages in the edit
			 * class only ever allow a single invocation each!
			 */
			if(edit.isEmpty() && edit.getOldGenerationStage()==UndoableCorpusEdit.UNSET_GENERATION_STAGE) {
				edit.setOldGenerationStage(stage);
			}

			beginUpdate();
			edit.add(change);
			endUpdate();
		} finally {
			lock.unlock();
		}
	}
}
