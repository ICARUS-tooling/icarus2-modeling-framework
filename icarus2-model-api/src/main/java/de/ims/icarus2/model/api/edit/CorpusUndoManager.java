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

import static java.util.Objects.requireNonNull;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.events.CorpusAdapter;
import de.ims.icarus2.model.api.events.CorpusEvent;

/**
 * @author Markus Gärtner
 *
 */
public class CorpusUndoManager extends UndoManager implements CorpusUndoListener {

	private static final long serialVersionUID = -1207749889681029406L;

	private final Corpus corpus;

	private long savedGeneration = 0L;

	public CorpusUndoManager(Corpus corpus) {
		requireNonNull(corpus);

		this.corpus = corpus;

		corpus.addCorpusListener(new CorpusAdapter(){

			/**
			 * @see de.ims.icarus2.model.api.events.CorpusAdapter#corpusSaved(de.ims.icarus2.model.api.events.CorpusEvent)
			 */
			@Override
			public void corpusSaved(CorpusEvent e) {
				markSaved();
			}

		});

		corpus.getEditManager().addCorpusUndoListener(this);
	}

	/**
	 * @return the corpus
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	protected void markSaved() {
		UndoableCorpusEdit edit = editToBeUndone();
		savedGeneration = edit==null ? 0L : edit.getId();
	}

	public boolean isSavedState() {
		UndoableCorpusEdit edit = editToBeUndone();
		long id = edit==null ? 0L : edit.getId();

		return savedGeneration==id;
	}

	/**
	 * @see javax.swing.undo.UndoManager#editToBeUndone()
	 */
	@Override
	protected UndoableCorpusEdit editToBeUndone() {
		return (UndoableCorpusEdit) super.editToBeUndone();
	}

	/**
	 * @see javax.swing.undo.UndoManager#editToBeRedone()
	 */
	@Override
	protected UndoableCorpusEdit editToBeRedone() {
		return (UndoableCorpusEdit) super.editToBeRedone();
	}

	/**
	 * @see javax.swing.undo.UndoManager#addEdit(javax.swing.undo.UndoableEdit)
	 */
	@Override
	public synchronized boolean addEdit(UndoableEdit anEdit) {
		if(!(anEdit instanceof UndoableCorpusEdit))
			throw new IllegalArgumentException("Can only handle corpus edits"); //$NON-NLS-1$

		return super.addEdit(anEdit);
	}

	/**
	 * @see javax.swing.undo.UndoManager#discardAllEdits()
	 */
	@Override
	public synchronized void discardAllEdits() {
		super.discardAllEdits();
		savedGeneration = 0L;
	}

	/**
	 * @see de.ims.icarus2.model.api.edit.CorpusUndoListener#undoableEditHappened(de.ims.icarus2.model.api.edit.UndoableCorpusEdit)
	 */
	@Override
	public void undoableEditHappened(UndoableCorpusEdit edit) {
		addEdit(edit);
	}

	/**
	 * @see javax.swing.undo.UndoManager#undoableEditHappened(javax.swing.event.UndoableEditEvent)
	 */
	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
		throw new UnsupportedOperationException(
				"Use method in "+CorpusUndoListener.class.getSimpleName()+" interface"); //$NON-NLS-1$
	}
}