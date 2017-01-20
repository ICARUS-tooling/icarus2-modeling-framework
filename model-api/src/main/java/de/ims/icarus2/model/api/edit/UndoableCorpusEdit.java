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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.GenerationControl;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 *
 */
public class UndoableCorpusEdit extends AbstractUndoableEdit {

	private static final long serialVersionUID = 3374451587305862185L;

	private static final AtomicLong idGenerator = new AtomicLong();

	/**
	 * Unique identifier for edits that are created in the same session under the same VM.
	 */
	private final long id;

	/**
	 * Holds the corpus of the edit.
	 */
	private final Corpus corpus;

	/**
	 * Holds the list of atomic changes that make up this undoable edit.
	 */
	private final List<AtomicChange> changes = new ArrayList<>();

	/**
	 * Specifies this undoable edit is significant. Default is true.
	 */
	private final boolean significant;

	private boolean reversible = true;

	/**
	 * Optional human readable textual representation of the edit oepration.
	 */
	private final String name;

	// Consistency check fields

	static final long UNSET_GENERATION_STAGE = -1L;

	/**
	 * Generation stage of the associated corpus <b>before</b> the edit took place.
	 */
	private long oldGenerationStage = UNSET_GENERATION_STAGE;

	/**
	 * Generation stage of the associated corpus <b>after</b> the edit took place.
	 */
	private long newGenerationStage = UNSET_GENERATION_STAGE;

	/**
	 * Constructs a new undoable edit for the given corpus.
	 */
	public UndoableCorpusEdit(Corpus corpus) {
		this(corpus, true, null);
	}

	/**
	 * Constructs a new undoable edit for the given corpus and sets its generation stage fields.
	 * This constructor is mainly intended for use with edit serialization frameworks.
	 */
	public UndoableCorpusEdit(Corpus corpus, long oldGenerationStage, long newGenerationStage) {
		this(corpus, true, null);

		setOldGenerationStage(oldGenerationStage);
		setNewGenerationStage(newGenerationStage);
	}

	/**
	 * Constructs a new named undoable edit for the given corpus.
	 */
	public UndoableCorpusEdit(Corpus corpus, String name) {
		this(corpus, true, name);
	}

	/**
	 * Constructs a new undoable edit for the given corpus.
	 */
	public UndoableCorpusEdit(Corpus corpus, boolean significant, String name) {
		requireNonNull(corpus);

		this.corpus = corpus;
		this.significant = significant;
		this.name = name;

		id = idGenerator.incrementAndGet();
	}

	/**
	 * Empty method that allows subclasses to customize the actual dispatch
	 * behavior.
	 * <p>
	 * This includes passing the undoable edit to listeners and/or doing
	 * preparation work before actually dispatching it.
	 */
	public void dispatch() {
		// no-op
	}

	/**
	 * @return the corpus
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @return the changes
	 */
	public List<AtomicChange> getChanges() {
		return CollectionUtils.getListProxy(changes);
	}

	/**
	 * @return the significant
	 */
	@Override
	public boolean isSignificant() {
		return significant;
	}

	public boolean isFinal() {
		return reversible;
	}

	/**
	 * Returns true if this edit contains no changes.
	 */
	public boolean isEmpty() {
		return changes.isEmpty();
	}

	/**
	 * Adds the specified change to this edit.
	 * <p>
	 * This will also set the internal value returned by {@link #getOldGenerationStage()}
	 * in the case it hasn't already been set and there haven't been any atomic changes
	 * added to this edit so far. So if an edit gets constructed from serialized data
	 * the generation stage fields should be set before adding any changes.
	 */
	public void add(AtomicChange change) {

		changes.add(change);
	}

	private boolean isGenerationInSync() {
		return newGenerationStage!=UNSET_GENERATION_STAGE // ensures that the edit is complete
				&& newGenerationStage==getCorpus().getGenerationControl().getStage(); // ensures that this is the most up2date edit
	}

	/**
	 * Verifies that this edit is the most up-to-date edit of the
	 * associated corpus by checking the generation stage and
	 * delegates to the super method.
	 *
	 * @see javax.swing.undo.AbstractUndoableEdit#canUndo()
	 */
	@Override
	public boolean canUndo() {
		return super.canUndo() && isGenerationInSync();
	}

	/**
	 * Verifies that this edit is the most up-to-date edit of the
	 * associated corpus by checking the generation stage and
	 * delegates to the super method.
	 *
	 * @see javax.swing.undo.AbstractUndoableEdit#canRedo()
	 */
	@Override
	public boolean canRedo() {
		return super.canRedo() && isGenerationInSync();
	}

	/**
	 * This method only verifies that the current generation stage matches the expected
	 * {@link #getNewGenerationStage() value} stored.
	 */
	private void checkGenerationPreChange() {
		GenerationControl generationControl = getCorpus().getGenerationControl();
		long currentStage = generationControl.getStage();
		long expectedStage = getNewGenerationStage();

		if(currentStage!=newGenerationStage)
			throw new ModelException(getCorpus(), ModelErrorCode.EDIT_GENERATION_OUT_OF_SYNC,
					Messages.mismatchMessage("Cannot execute operation, corpus generation stage out of sync", expectedStage, currentStage));
	}

	/**
	 * This method attempts to {@link GenerationControl#step(long, long) update} the
	 * generation counter with the values stored as {@link #getOldGenerationStage() begin}
	 * and {@link #getNewGenerationStage() end} for this edit and throws an exception in
	 * case this fails.
	 */
	private void checkAndRefreshGenerationPostChange() {
		GenerationControl generationControl = getCorpus().getGenerationControl();
		long oldStage = getOldGenerationStage();
		long newStage = getNewGenerationStage();

		// Final consistency check. If this succeeds we're golden
		if(!generationControl.step(newStage, oldStage))
			throw new ModelException(getCorpus(), ModelErrorCode.EDIT_GENERATION_OUT_OF_SYNC,
					"Corrupted edit, generation stage deviates from expected value");

		// If everything went fine swap our saved stages
		//TODO maybe move this mechanic into one 2-arg method depending on where else it's being used
		this.newGenerationStage = oldStage;
		this.oldGenerationStage = newStage;
	}

	/**
	 * Executes all the atomic changes in reverse order of their
	 * registration and dispatches a notification afterwards.
	 */
	@Override
	public void undo() throws CannotUndoException {
		// Allow default check for available undo
		super.undo();

		int count = changes.size();

		Lock lock = getCorpus().getLock();

		lock.lock();
		int index = count-1;
		try {
			// fail-fast in case of inconsistencies
			checkGenerationPreChange();

			for (; index >= 0; index--) {
				changes.get(index).execute();
			}

			// Verify again before dispatching and then commit the stage change -> rollback if that fails
			checkAndRefreshGenerationPostChange();

			dispatch();
		} catch(Exception e) {
			// If rollback fails we'll shadow the original exception with an InternalError
			rollbackUndo(index);
			throw e;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Attempts to roll back all the changes at index positions larger than {@code failedIndex}
	 * @param index
	 */
	private void rollbackUndo(int failedIndex) {
		try {
			int count = changes.size();
			for(int i=failedIndex+1; i<count; i++) {
				changes.get(i).execute();
			}
		} catch(Exception e) {
			throw new InternalError("Failed to rollback undo operation", e);
		}
	}

	/**
	 * Executes all the atomic changes in the order of their
	 * registration and dispatches a notification afterwards.
	 */
	@Override
	public void redo() throws CannotRedoException {
		// Allow default check for available redo
		super.redo();

		int count = changes.size();

		Lock lock = getCorpus().getLock();

		lock.lock();

		// Index up to which changes have been attempted to execute
		int index = 0;
		try {
			// fail-fast in case of inconsistencies
			checkGenerationPreChange();

			for (; index < count; index++) {
				changes.get(index).execute();
			}

			// Verify again before dispatching and then commit the stage change -> rollback if that fails
			checkAndRefreshGenerationPostChange();

			dispatch();
		} catch(Exception e) {
			// If rollback fails we'll shadow the original exception with an InternalError
			rollbackRedo(index);
			throw e;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Attempts to roll back all the changes at index positions larger than {@code failedIndex}
	 * @param index
	 */
	private void rollbackRedo(int failedIndex) {
		try {
			for(int i=failedIndex-1; i>=0; i++) {
				changes.get(i).execute();
			}
		} catch(Exception e) {
			throw new InternalError("Failed to rollback redo operation", e);
		}
	}

	/**
	 * @see javax.swing.undo.AbstractUndoableEdit#getPresentationName()
	 */
	@Override
	public String getPresentationName() {
		return name!=null ? name : super.getPresentationName();
	}

	/**
	 * @see javax.swing.undo.AbstractUndoableEdit#getUndoPresentationName()
	 */
	@Override
	public String getUndoPresentationName() {
		String name = getPresentationName();
		String prefix = "undo"; //$NON-NLS-1$

		return "".equals(name) ? prefix : prefix+"."+name; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see javax.swing.undo.AbstractUndoableEdit#getRedoPresentationName()
	 */
	@Override
	public String getRedoPresentationName() {
		String name = getPresentationName();
		String prefix = "redo"; //$NON-NLS-1$

		return "".equals(name) ? prefix : prefix+"."+name; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Utility method to retrieve the {@link Context} affected
	 * by a certain {@link AtomicChange change}. This method first
	 * calls {@link AtomicChange#getAffectedMember()} and if the
	 * result is not {@link null} it will traverse the corpus
	 * element hierarchy to grab the surrounding context.
	 *
	 *
	 * @param change
	 * @return
	 */
	public static Context getContextForChange(AtomicChange change){
		requireNonNull(change);

		CorpusMember member = change.getAffectedMember();
		if(ModelUtils.isNonLayer(member)) {
			return ((Item)member).getLayer().getContext();
		} else if(ModelUtils.isLayer(member)) {
			return ((Layer)member).getContext();
		} else {
			return null;
		}
	}

	/**
	 * Utility method to retrieve the {@link Layer} affected
	 * by a certain {@link AtomicChange change}. This method first
	 * calls {@link AtomicChange#getAffectedMember()} and if the
	 * result is not {@link null} it will traverse the corpus
	 * element hierarchy to grab the surrounding layer.
	 *
	 *
	 * @param change
	 * @return
	 */
	public static Layer getLayerForChange(AtomicChange change){
		requireNonNull(change);

		CorpusMember member = change.getAffectedMember();
		if(ModelUtils.isNonLayer(member)) {
			return ((Item)member).getLayer();
		} else if(ModelUtils.isLayer(member)) {
			return (Layer)member;
		} else {
			return null;
		}
	}

	/**
	 * Checks whether this edit affects the given {@code Context}.
	 * This is done by traversing all the atomic changes stored in
	 * this edit and comparing their context (determined via calling
	 * {@link AtomicChange#getAffectedMember()} and resolving that
	 * member's context) to the given one.
	 */
	public boolean isAffected(Context context) {
		requireNonNull(context);

		for(AtomicChange change : changes) {
			Context affected = getContextForChange(change);
			if(affected!=null && affected.equals(context)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Collects all the {@code Context} objects affected by this edit and
	 * returns them as a {@code Set}. If no contexts could be resolved using
	 * the {@link AtomicChange#getAffectedMember()} method of stored changes
	 * than the empty set will be returned.
	 * <p>
	 * Note that the order of appearance in the returned set does <b>not</b>
	 * reflect the order of changes as stored in this edit!
	 * The returned set is not guaranteed to be modifiable, so client code should
	 * treat it as read-only.
	 */
	public Set<Context> getAffectedContexts() {
		LazyCollection<Context> result = LazyCollection.lazySet();

		for(AtomicChange change : changes) {
			Context affected = getContextForChange(change);
			if(affected!=null) {
				result.add(affected);
			}
		}

		return result.getAsSet();
	}

	/**
	 * Collects all the {@code Layer} objects affected by this edit and
	 * returns them as a {@code Set}. If no contexts could be resolved using
	 * the {@link AtomicChange#getAffectedMember()} method of stored changes
	 * than the empty set will be returned.
	 * <p>
	 * Note that the order of appearance in the returned set does <b>not</b>
	 * reflect the order of changes as stored in this edit!
	 * The returned set is not guaranteed to be modifiable, so client code should
	 * treat it as read-only.
	 */
	public Set<Layer> getAffectedLayers() {
		LazyCollection<Layer> result = LazyCollection.lazySet();

		for(AtomicChange change : changes) {
			Layer affected = getLayerForChange(change);
			if(affected!=null) {
				result.add(affected);
			}
		}

		return result.getAsSet();
	}

	/**
	 * @return the oldGenerationStage
	 */
	public long getOldGenerationStage() {
		return oldGenerationStage;
	}

	/**
	 * @param oldGenerationStage the oldGenerationStage to set
	 */
	void setOldGenerationStage(long oldGenerationStage) {
		checkArgument(oldGenerationStage!=UNSET_GENERATION_STAGE);
		checkState(this.oldGenerationStage==UNSET_GENERATION_STAGE);

		this.oldGenerationStage = oldGenerationStage;
	}

	/**
	 * @return the newGenerationStage
	 */
	public long getNewGenerationStage() {
		return newGenerationStage;
	}

	/**
	 * @param newGenerationStage the newGenerationStage to set
	 */
	void setNewGenerationStage(long newGenerationStage) {
		checkArgument(newGenerationStage!=UNSET_GENERATION_STAGE);
		checkState(this.newGenerationStage==UNSET_GENERATION_STAGE);

		this.newGenerationStage = newGenerationStage;
	}
}
