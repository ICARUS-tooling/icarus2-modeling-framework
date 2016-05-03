/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 448 $
 * $Date: 2016-01-19 17:30:06 +0100 (Di, 19 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/edit/CorpusEditManager.java $
 *
 * $LastChangedDate: 2016-01-19 17:30:06 +0100 (Di, 19 Jan 2016) $
 * $LastChangedRevision: 448 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.edit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus2.util.events.EventObject;
import de.ims.icarus2.util.events.WeakEventSource;

/**
 *
 *
 * @author Markus Gärtner
 * @version $Id: CorpusEditManager.java 448 2016-01-19 16:30:06Z mcgaerty $
 *
 */
public class CorpusEditManager extends WeakEventSource {

	private static final long serialVersionUID = -8320136116919999917L;

	private int updateLevel = 0;
	private boolean endingUpdate = false;
	private UndoableCorpusEdit currentEdit;

	private final Corpus corpus;

	private final List<CorpusUndoListener> undoListeners = new CopyOnWriteArrayList<>();

	public CorpusEditManager(Corpus corpus) {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$

		this.corpus = corpus;

		currentEdit = createUndoableEdit(null);
	}

	public void beginUpdate() {
		updateLevel++;
		fireEvent(new EventObject(CorpusEditEvents.BEGIN_UPDATE));
	}

	public boolean hasActiveUpdate() {
		return updateLevel>0;
	}

	public void beginUpdate(String nameKey) {
		if(nameKey==null)
			throw new NullPointerException("Invalid edit name"); //$NON-NLS-1$
		if(hasActiveUpdate())
			throw new IllegalStateException("Cannot start named edit '"+nameKey+"' while another edit is already in progress."); //$NON-NLS-1$ //$NON-NLS-2$

		currentEdit = createUndoableEdit(nameKey);

		// Delegate default handling to regular method
		beginUpdate();
	}

	public void endUpdate() {
		updateLevel--;

		if (!endingUpdate) {
			endingUpdate = updateLevel == 0;
			fireEvent(new EventObject(CorpusEditEvents.END_UPDATE,
					"edit", currentEdit)); //$NON-NLS-1$

			try {
				if (endingUpdate && !currentEdit.isEmpty()) {
					// Notify listeners about imminent undoable edit
					fireEvent(new EventObject(CorpusEditEvents.BEFORE_UNDO, "edit", //$NON-NLS-1$
							currentEdit));

					// Copy and then reset current edit
					UndoableCorpusEdit publishedEdit = currentEdit;
					currentEdit = createUndoableEdit(null);

					// Allow edit to manage notification (per default this will fire a "change" event)
					publishedEdit.dispatch();

					// Notify listeners about executed undoable edit
					fireEvent(new EventObject(CorpusEditEvents.UNDO, "edit", publishedEdit)); //$NON-NLS-1$

					// Finally dispatch the edit so undo managers and other specialized
					// listeners can handle or accumulate it
					fireUndoableCorpusEdit(publishedEdit);
				}
			} finally {
				endingUpdate = false;
			}
		}
	}

	/**
	 * @return
	 */
	protected UndoableCorpusEdit createUndoableEdit(final String nameKey) {
		return new UndoableCorpusEdit(getCorpus(), nameKey)
		{

			private static final long serialVersionUID = -471363052764925086L;

			/**
			 * @see de.ims.icarus2.model.api.edit.UndoableCorpusEdit#dispatch()
			 */
			@Override
			public void dispatch() {
				fireEvent(new EventObject(CorpusEditEvents.CHANGE, "edit", this)); //$NON-NLS-1$
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
	 * listener notification will be performed.
	 * <p>
	 * Note that a change will only trigger a {@value CorpusEditEvents#EXECUTE}
	 * event the first time it is executed by the model. Subsequent executions
	 * (when undoing or redoing a change) will {@code not} result in events being
	 * fired for every atomic change, only for top level changes as a whole!
	 *
	 * @param change
	 * @throws UnsupportedOperationException if the corpus is not editable
	 */
	public void execute(UndoableCorpusEdit.AtomicChange change) {

		if(!getCorpus().getManifest().isEditable())
			throw new UnsupportedOperationException("Corpus does not support modifications"); //$NON-NLS-1$

		Lock lock = getCorpus().getLock();

		lock.lock();
		try {
			// Execute change before update level is modified
			change.execute();

			beginUpdate();
			currentEdit.add(change);
			fireEvent(new EventObject(CorpusEditEvents.EXECUTE, "change", change)); //$NON-NLS-1$
			endUpdate();
		} finally {
			lock.unlock();
		}
	}
}
