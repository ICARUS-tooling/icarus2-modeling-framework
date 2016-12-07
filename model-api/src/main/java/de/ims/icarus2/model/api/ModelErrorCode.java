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
package de.ims.icarus2.model.api;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.concurrent.locks.Lock;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.GenerationControl;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.edit.UndoableCorpusEdit;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.view.CorpusModel;
import de.ims.icarus2.model.api.view.CorpusView.PageControl;
import de.ims.icarus2.util.id.DuplicateIdentifierException;

/**
 * @author Markus Gärtner
 *
 */
public enum ModelErrorCode implements ErrorCode {

	//**************************************************
	//       3xx  EDIT ERRORS
	//**************************************************

	/**
	 * A general edit related error.
	 */
	EDIT_ERROR(300),

	/**
	 * Client code attempted to access a method that relies on synchronization via
	 * the global {@link Corpus#getLock() lock} of a corpus.
	 * <p>
	 * Such methods usually make use of the {@link Lock#tryLock()} method to check
	 * if the lock is already held by another thread.
	 */
	EDIT_UNSYNCHRONIZED_ACCESS(301),

	/**
	 * Client code attempted to perform an edit operation (like an undo or redo)
	 * that relies on synchronization with the {@link GenerationControl generation}
	 * of a corpus.
	 * Usually an edit has a well defined {@link UndoableCorpusEdit#getOldGenerationStage() begin}
	 * and {@link UndoableCorpusEdit#getNewGenerationStage() end} stage that determines
	 * exactly when it is valid to execute this {@link UndoableCorpusEdit edit}.
	 * If at the beginning or during execution of an {@link UndoableCorpusEdit#undo()} or
	 * {@link UndoableCorpusEdit#redo()} operation the current {@link GenerationControl#getStage() stage}
	 * does not match the edit's expectation, then it will throw this kind of error code.
	 */
	EDIT_GENERATION_OUT_OF_SYNC(302),


	//**************************************************
	//       4xx  DRIVER ERRORS
	//**************************************************

	/**
	 * A general error regarding a driver implementation
	 */
	DRIVER_ERROR(400),

	/**
	 * An unexpected I/O exception occurred during access to some indexing system associated with a
	 * {@link Driver}.
	 */
	DRIVER_INDEX_IO(401),

	/**
	 * Client code attempted to write to an index file in a manner other than using existing index
	 * values or appending to the greatest current index value. This restriction is imposed by the default
	 * implementations for the {@link Mapping} interface provided by file based {@link Driver}s. Note that
	 * the {@code Mapping} interface does not define write mechanics itself, since for example database
	 * backed implementations might directly link to the database's own indexing system and therefore
	 * not support client originated write operations on the index!
	 */
	DRIVER_INDEX_WRITE_VIOLATION(402),

	/**
	 * A driver implementation failed to create a proper checksum for an index or content file.
	 */
	DRIVER_CHECKSUM_FAIL(403),

	/**
	 * A driver failed to connect to its surrounding corpus instance. Usually this indicates a user
	 * originated cancellation of the connection attempt. Additionally this error can be used to signal
	 * that a driver was expected to have already been connected to a corpus, but was found unconnected.
	 */
	DRIVER_CONNECTION(404),

	/**
	 * Signals that a driver needs to be ready (i.e. all its modules must be ready) in order to serve a
	 * given request.
	 */
	DRIVER_READY(405),

	/**
	 * The metadata a driver stored for a resource got corrupted and is now displaying erroneous information.
	 */
	DRIVER_METADATA_CORRUPTED(406),

	/**
	 * The metadata a driver stored for a resource is missing required information.
	 */
	DRIVER_METADATA_MISSING(407),

	/**
	 * The content of a corpus resource is invalid.
	 */
	DRIVER_INVALID_CONTENT(410),

	//**************************************************
	//       5xx  CORPUS VIEW ERRORS
	//**************************************************

	/**
	 * A general error regarding a view implementation
	 */
	VIEW_ERROR(500),

	/**
	 * Closing a corpus view failed due to some owner not being able to release its lock when asked.
	 */
	VIEW_UNCLOSABLE(501),

	/**
	 * Creating a new corpus view in an access mode that would grant write access failed due to some other
	 * corpus view instance already working on the corpus in question.
	 * Note that there can only be one corpus view instance for a particular corpus with write access, but
	 * an unlimited number of reading views!
	 */
	VIEW_ALREADY_OPENED(502),

	/**
	 * An attempt to fetch the model for a corpus view failed because the data for the current
	 * page has not yet been loaded.
	 */
	VIEW_EMPTY(503),

	/**
	 * An attempt was made to call a method on a {@code CorpusView}'s {@link PageControl} that
	 * failed due to the control being locked.
	 */
	VIEW_LOCKED(504),

	/**
	 * An already closed view or one of its sub-components has been asked to perform an operation that
	 * requires the view to still be active.
	 */
	VIEW_CLOSED(505),

	//**************************************************
	//       8xx  MODEL ERRORS (inc. EDIT MODEL errors)
	//**************************************************

	/**
	 * A general error regarding a model implementation
	 */
	MODEL_ERROR(800),

	/**
	 * Execution of an atomic change failed due to missing prerequisites. This can happen if
	 * client code does not honor the corpus edit contract and makes direct changes to members
	 * of a corpus outside of {@link CorpusModel}.
	 * <p>
	 * If for example an item is removed from its host container via
	 * {@link CorpusModel#removeItem(Container, int) removeItem} the generated atomic change
	 * will remember the environment of its creation (e.g. size of host container, etc...) and then
	 * check them again once it gets executed or undone. So when after the item was removed another
	 * client code would then make direct changes to the container (e.g. by calling {@link Container#removeAllItems() removeAllItems}
	 * in order to clear it) the attempt to undo the first removal would fail, because the atomic change's
	 * environment has changed.
	 */
	MODEL_CORRUPTED_EDIT(801),

	/**
	 * Attempting to write to a read-only model failed
	 */
	MODEL_READ_ONLY(802),

	/**
	 * Attempting to read from a write-only model failed
	 */
	MODEL_WRITE_ONLY(803),

	/**
	 * A given index is negative or exceeds the limit specified by the context it is supposed
	 * to be used in (e.g. the size for a certain container when attempting to
	 * add a new item to it).
	 */
	MODEL_INDEX_OUT_OF_BOUNDS(804),

	/**
	 * The {@link Item} passed to a model method violates the scope constraint for that method.
	 * For example it is not possible to add an item to a container when the item is not a valid
	 * member of at least one of the container's designated <i>base-containers</i>.
	 */
	MODEL_ILLEGAL_MEMBER(805),

	/**
	 * Linking one or more {@link Item}s or derivations failed due to one of them presenting
	 * an illegal target. For example a {@link Container} might restrict his elements to be
	 * of a certain type or their original host container to have a special quality.
	 * <p>
	 * Note that those restrictions are implementation specific details a driver should be aware
	 * of and nothing directly expressed in a manifest!
	 */
	MODEL_ILLEGAL_LINKING(806),

	/**
	 * Two or more items are involved in an operation that relies on the {@link Item#getBeginOffset() begin}
	 * or {@link Item#getEndOffset() end} offsets of those items and the operation is only possible when
	 * all items refer to the exact same layer as their respective foundation!
	 */
	MODEL_INCOMPATIBLE_FOUNDATIONS(807),

	/**
	 * A general error to signal a mismatch between the type (class, enum specification, etc...) of a
	 * member of the model and an actual type argument.
	 */
	MODEL_TYPE_MISMATCH(808),

	/**
	 * An operation expected the presence of some model member which is missing.
	 */
	MODEL_MISSING_MEMBER(809),

	/**
	 * A member of the model (item, edge, structure, etc...) was found to be in an inconsistent state
	 * and cannot fulfill his role properly. This can happen when drivers fail to finish initialization of
	 * elements or if client code does not honor the edit contract of the model by making direct calls
	 * to modifying methods outside a given {@link CorpusModel} instance.
	 */
	MODEL_CORRUPTED_STATE(810),

	/**
	 * An operation failed due to invalid parameters.
	 */
	MODEL_INVALID_REQUEST(811),

	/**
	 * A {@link Position} object violated constraints imposed on its vector fields
	 */
	MODEL_POSITION_OUT_OF_BOUNDS(812),

	/**
	 * An attempt was made to set an invalid {@link Position} object as a fragment's begin or end boundary
	 */
	MODEL_INVALID_POSITION(813),

	/**
	 * A method consuming an {@link IndexSet} required it to be {@link IndexSet#isSorted() sorted} but found
	 * it lacking. Note that in general it is kind of a good practice to ensure most index sets that get passed
	 * around to framework code are in sorted state.
	 * <p>
	 * This error applies to both individual index sets or arrays of such!
	 */
	MODEL_UNSORTED_INDEX_SET(814),

	/**
	 * An operation cannot be performed because the target {@link Item} has no
	 * valid host container assigned to it.
	 */
	MODEL_HEADLESS(820),

	//FIXME add errors for missing content etc...
	;

	private final int code;

	ModelErrorCode(int errorCode) {
		this.code = errorCode;
	}

	@Override
	public int code() {
		return code;
	}

	private static final Int2ObjectMap<ModelErrorCode> codeLookup = new Int2ObjectOpenHashMap<>();

	public static ModelErrorCode forCode(int code) {
		if(codeLookup.isEmpty()) {
			synchronized (codeLookup) {
				if (codeLookup.isEmpty()) {
					for(ModelErrorCode error : values()) {
						if(codeLookup.containsKey(error.code))
							throw new DuplicateIdentifierException("Duplicate error code: "+error); //$NON-NLS-1$

						codeLookup.put(error.code, error);
					}
				}

			}
		}

		ModelErrorCode error = codeLookup.get(code);

		if(error==null)
			throw new IllegalArgumentException("Unknown error code: "+code); //$NON-NLS-1$

		return error;
	}

}
