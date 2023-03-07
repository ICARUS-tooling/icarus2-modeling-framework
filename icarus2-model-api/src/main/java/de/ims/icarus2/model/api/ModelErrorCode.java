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
package de.ims.icarus2.model.api;

import java.util.concurrent.locks.Lock;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.ErrorCodeScope;
import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.GenerationControl;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mods.DriverModule;
import de.ims.icarus2.model.api.edit.UndoableCorpusEdit;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.view.paged.CorpusModel;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl;
import de.ims.icarus2.model.api.view.streamed.StreamedCorpusView;

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
	 * The (physical) content of a corpus resource is missing. This can include incorrect
	 * path definitions to a file or unresolvable remote connections.
	 */
	DRIVER_MISSING_CONTENT(409),

	/**
	 * The content of a corpus resource is invalid.
	 */
	DRIVER_INVALID_CONTENT(410),

	/**
	 * Client code attempted to call one of the write methods of the {@link Driver} interface on
	 * a corpus resource that has been declared as not editable.
	 */
	DRIVER_NOT_EDITABLE(411),

	/**
	 * Models a general exception when it comes to accessing corpus resources. Usually this means
	 * a corpus file path cannot be resolved or some other form of I/O error occurred.
	 */
	DRIVER_RESOURCE(412),

	/**
	 * Loading (i.e. instantiating) one or more {@link DriverModule}(s) failed and the driver
	 * is unable to properly connect to the corpus.
	 */
	DRIVER_MODULE_LOADING(413),

	/**
	 * {@link DriverModule#prepare(de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest, de.ims.icarus2.model.api.driver.mods.ModuleMonitor) Preparing}
	 * one or more {@link DriverModule}(s) failed and the driver is unable to properly connect to
	 * the corpus.
	 */
	DRIVER_MODULE_PREPARATION(414),

	/**
	 * A general error occurred when interacting with a member of the index or mapping
	 * framework, such as a {@link IndexSet} or {@link Mapping}.
	 */
	DRIVER_INDEX_ERROR(420),

	/**
	 * An unexpected I/O exception occurred during access to some indexing system associated with a
	 * {@link Driver}.
	 */
	DRIVER_INDEX_IO(421),

	/**
	 * Client code attempted to write to an index file in a manner other than using existing index
	 * values or appending to the greatest current index value. This restriction is imposed by the default
	 * implementations for the {@link Mapping} interface provided by file based {@link Driver}s. Note that
	 * the {@code Mapping} interface does not define write mechanics itself, since for example database
	 * backed implementations might directly link to the database's own indexing system and therefore
	 * not support client originated write operations on the index!
	 */
	DRIVER_INDEX_WRITE_VIOLATION(422),

	/**
	 * An attempt to {@link IndexSet#sort() sort} an {@link IndexSet} failed.
	 */
	DRIVER_INDEX_SORT(423),

	//**************************************************
	//       5xx  STREAM ERRORS
	//**************************************************

	/**
	 * A general error occurred when streaming the content
	 * of a corpus.
	 */
	STREAM_ERROR(500),

	/**
	 * Trying to access the current element in the stream failed.
	 * This is also used for other "read" methods that require
	 * access to the current item or associated information.
	 * <p>
	 * Typically this kind of error happens when a stream hasn't
	 * been properly {@link StreamedCorpusView#advance() advanced} or if
	 * it has been {@link StreamedCorpusView#flush() flushed} directly
	 * before calling a method that tries to access the current
	 * item.
	 */
	STREAM_NO_ITEM(501),

	STREAM_MARK_NOT_SUPPORTED(510),
	STREAM_MARK_NOT_SET(511),

	STREAM_SKIP_NOT_SUPPORTED(520),


	//**************************************************
	//       6xx  CORPUS VIEW ERRORS
	//**************************************************

	/**
	 * A general error regarding a view implementation
	 */
	VIEW_ERROR(600),

	/**
	 * Closing a corpus view failed due to some owner not being able to release its lock when asked.
	 */
	VIEW_UNCLOSABLE(601),

	/**
	 * Creating a new corpus view in an access mode that would grant write access failed due to some other
	 * corpus view instance already working on the corpus in question.
	 * Note that there can only be one corpus view instance for a particular corpus with write access, but
	 * an unlimited number of reading views!
	 */
	VIEW_ALREADY_OPENED(602),

	/**
	 * An attempt to fetch the model for a corpus view failed because the data for the current
	 * page has not yet been loaded.
	 */
	VIEW_EMPTY(603),

	/**
	 * An attempt was made to call a method on a {@code PagedCorpusView}'s {@link PageControl} that
	 * failed due to the control being locked.
	 */
	VIEW_LOCKED(604),

	/**
	 * An already closed view or one of its sub-components has been asked to perform an operation that
	 * requires the view to still be active.
	 */
	VIEW_CLOSED(605),

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
	 * A method consuming {@code long} index values required them to be supplied in sorted order,
	 * but at least one of them violated that contract.
	 */
	MODEL_UNSORTED_INPUT(815),

	/**
	 * Storing some sort of corpus member or annotation value in the storage backend used
	 * for the corpus failed. This
	 */
	MODEL_STORAGE_ERROR(816),

	/**
	 * An operation cannot be performed because the target {@link Item} has no
	 * valid host container assigned to it.
	 */
	MODEL_HEADLESS(820),

	//FIXME add errors for missing content etc...
	;

	private static volatile ErrorCodeScope SCOPE;

	public static ErrorCodeScope getScope() {
		ErrorCodeScope scope = SCOPE;
		if(scope==null) {
			synchronized (ModelErrorCode.class) {
				if((scope = SCOPE) == null) {
					scope = SCOPE = ErrorCodeScope.newScope(3000, ModelErrorCode.class.getSimpleName());
				}
			}
		}
		return scope;
	}

	private final int code;

	ModelErrorCode(int errorCode) {
		this.code = errorCode;

		ErrorCode.register(this);
	}

	@Override
	public int code() {
		return code+getScope().getCode();
	}

	/**
	 * @see de.ims.icarus2.ErrorCode#scope()
	 */
	@Override
	public ErrorCodeScope scope() {
		return getScope();
	}

	/**
	 * Resolves the given error code to the matching enum constant.
	 * {@code Code} can be given both as an internal id or global code.
	 *
	 * @param code
	 * @return
	 */
	public static ModelErrorCode forCode(int code) {
		getScope().checkCode(code);

		ErrorCode error = ErrorCode.forCode(code);

		if(error==null)
			throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT, "Unknown error code: "+code);
		if(!ModelErrorCode.class.isInstance(error))
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Corrupted mapping for error code: "+code);

		return ModelErrorCode.class.cast(error);
	}

}
