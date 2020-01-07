/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.edit;

import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.GenerationControl.Stage;
import de.ims.icarus2.model.api.edit.change.AtomicChange;
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

	private transient int updateLevel = 0;
	private boolean endingUpdate = false;
	private UndoableCorpusEdit currentEdit;

	private final Corpus corpus;

	private final List<CorpusUndoListener> undoListeners = new CopyOnWriteArrayList<>();

	public CorpusEditManager(Corpus corpus) {
		requireNonNull(corpus);

		this.corpus = corpus;

		currentEdit = createUndoableEdit(null);
	}

	public synchronized void beginUpdate() {
		++updateLevel;
		fireEvent(new EventObject(CorpusEditEvents.BEGIN_UPDATE, "edit", currentEdit, "level",
				_int(updateLevel)));
	}

	public synchronized boolean hasActiveUpdate() {
		return updateLevel>0;
	}

	public synchronized void beginUpdate(String nameKey) {
		requireNonNull(nameKey);
		if(hasActiveUpdate())
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Cannot start named edit '"+nameKey+"' while another edit is already in progress.");

		currentEdit = createUndoableEdit(nameKey);

		// Delegate default handling to regular method
		beginUpdate();
	}

	public synchronized void endUpdate() {
		updateLevel--;
		if(updateLevel<0) {
			updateLevel = 0;
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Attempted to end update, but no matching beginUpdate() call was made previously");
		}

		if (!endingUpdate) {
			boolean end = updateLevel==0;
			fireEvent(new EventObject(CorpusEditEvents.END_UPDATE, "edit", currentEdit, "level", _int(updateLevel)));

			try {
				if (end && !currentEdit.isEmpty()) {

					//TODO move inner block into a customizable hook function

					// Notify listeners about imminent undoable edit
					fireEvent(new EventObject(CorpusEditEvents.BEFORE_UNDO, "edit", currentEdit));

					// Copy and then reset current edit
					UndoableCorpusEdit publishedEdit = currentEdit;
					currentEdit = createUndoableEdit(null);

					// Finalize generation information of edit
					//FIXME should we ensure some proper synchronization on the generation control here?
					if(publishedEdit.getNewGenerationStage()==null) {
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
				endingUpdate = false;
			}
		}
	}

	protected synchronized void dispatchEdit(UndoableCorpusEdit edit) {

		// Only need to fire the event. A corpus' generation control will listen to the appropriate event time on its own

		fireEvent(new EventObject(CorpusEditEvents.CHANGE, "edit", edit));
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
	public synchronized UndoableCorpusEdit createUndoableEdit(@Nullable final String nameKey) {
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
		requireNonNull(listener);
		if(!undoListeners.contains(listener)) 	{
			undoListeners.add(listener);
		}
	}

	public void removeCorpusUndoListener(CorpusUndoListener listener) {
		requireNonNull(listener);
		undoListeners.remove(listener);
	}

	protected void fireUndoableCorpusEdit(UndoableCorpusEdit edit) {
		requireNonNull(edit);

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
	public synchronized void execute(AtomicChange change) {
		requireNonNull(change);

		if(!getCorpus().getManifest().isEditable())
			throw new ModelException(getCorpus(), GlobalErrorCode.UNSUPPORTED_OPERATION, "Corpus does not support modifications"); //$NON-NLS-1$

		Lock lock = getCorpus().getLock();

		lock.lock();
		try {
			UndoableCorpusEdit edit = currentEdit;
			Stage stage = getCorpus().getGenerationControl().getStage();

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
			if(edit.isEmpty() && edit.getOldGenerationStage()==null) {
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
