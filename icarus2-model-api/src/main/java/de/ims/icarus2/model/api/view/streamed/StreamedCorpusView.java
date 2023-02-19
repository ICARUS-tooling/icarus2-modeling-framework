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
package de.ims.icarus2.model.api.view.streamed;

import java.util.Set;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.OwnableCorpusPart;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.view.CorpusView;

/**
 * Implements a <i>forward-only</i> traversal mechanism for a corpus
 * or a section of a corpus.
 *
 * @author Markus Gärtner
 *
 */
public interface StreamedCorpusView extends CorpusView, OwnableCorpusPart {

	// Configuration part

	/**
	 * Returns all the options that are set for this stream
	 * instance. Note that the returned set can be slightly
	 * different to the collection of options used to create
	 * this stream. Implementations can choose not to support
	 * optional features depending on their capabilities.
	 * <p>
	 * If client code heavily relies on a specified option
	 * being supported, then it should test its presence after
	 * the stream has been created!
	 *
	 * @return
	 */
	Set<StreamOption> getOptions();

	/**
	 * Returns {@code true} iff the specified option is set
	 * for this stream instance.
	 *
	 * @param option
	 * @return
	 */
	boolean hasOption(StreamOption option);

	// Navigation part

	/**
	 * Tries to advance the stream to the next item.
	 * If there are no more items available in the
	 * stream, this method will return {@code false}.
	 *
	 * @return {@code true} iff there was another item
	 * available in the stream and the method succeeded
	 * in advancing to it.
	 */
	boolean advance();

	/**
	 * Returns {@code true} iff the stream currently has a valid active
	 * item that can subsequently be fetched by {@link #currentItem()}.
	 *
	 * @return
	 */
	boolean hasItem();

	/**
	 * Returns the current item in this stream.
	 *
	 * @return
	 *
	 * @throws ModelException with {@link ModelErrorCode#STREAM_NO_ITEM}
	 * in case there is no active item available currently.
	 * @throws ClassCastException if the current item cannot be cast to
	 * the desired type.
	 */
	<I extends Item> I currentItem();

	// Support part

	/**
	 * Release any buffered data, discard the mark, but keep the stream alive
	 * otherwise.
	 */
	void flush();

	/**
	 * Skips the next {@code n} items in this stream.
	 * Returns {@code true} iff there are any more items available in this
	 * stream after the skip operation has completed.
	 * <p>
	 * This method might discard the mark as a side effect.
	 *
	 * @param n
	 * @return
	 */
	default boolean skip(long n) {
		throw new ModelException(ModelErrorCode.STREAM_SKIP_NOT_SUPPORTED,
				"Default implementation does not support skipping items");
	}

	/**
	 * Tries to mark the currently active item so that a later call to
	 * {@link #reset()} moves the stream back to the current position.
	 *
	 * @return {@code true} iff setting/changing the mark to the current position
	 * was successful.
	 *
	 * @throws ModelException with {@link ModelErrorCode#STREAM_NO_ITEM} if
	 * no active item is available or {@link ModelErrorCode#STREAM_MARK_NOT_SUPPORTED}
	 * if the stream implementation does not support marks.
	 */
	default boolean mark() {
		throw new ModelException(ModelErrorCode.STREAM_MARK_NOT_SUPPORTED,
				"Default implemenattion does not support mark");
	}

	/**
	 * Reset any internal state related to the mark. Does nothing if no
	 * mark has been set.
	 *
	 * @return {@code true} iff a mark has been set and was cleared
	 *
	 * @throws ModelException with {@link ModelErrorCode#STREAM_MARK_NOT_SUPPORTED}
	 * if the stream implementation does not support marks.
	 */
	default boolean clearMark() {
		throw new ModelException(ModelErrorCode.STREAM_MARK_NOT_SUPPORTED,
				"Default implemenattion does not support mark");
	}

	/**
	 * Tells whether or not the mark is set.
	 *
	 * @return {@code true} iff a mark has been set to a valid item previously.
	 *
	 * @throws ModelException with {@link ModelErrorCode#STREAM_MARK_NOT_SUPPORTED}
	 * if the stream implementation does not support marks.
	 */
	default boolean hasMark() {
		throw new ModelException(ModelErrorCode.STREAM_MARK_NOT_SUPPORTED,
				"Default implemenattion does not support mark");
	}

	/**
	 * Tries to move the stream back to where the {@link #mark() mark} was
	 * put previously. If the operation was successful, this will also
	 * clear the current mark.
	 *
	 * @return {@code true} iff the stream has been successfully moved back
	 * to the previous position of the mark.
	 *
	 * @throws ModelException with {@link ModelErrorCode#STREAM_MARK_NOT_SET} if
	 * no mark has been set previously or {@link ModelErrorCode#STREAM_MARK_NOT_SUPPORTED}
	 * if the stream implementation does not support marks.
	 */
	default boolean reset() {
		throw new ModelException(ModelErrorCode.STREAM_MARK_NOT_SUPPORTED,
				"Default implemenattion does not support mark");
	}

	/**
	 * Tests if {@link #advance() advancing} the current position would
	 * invalidate the mark.
	 *
	 * @return
	 * @throws ModelException with {@link ModelErrorCode#STREAM_MARK_NOT_SET} if
	 * no mark has been set previously or {@link ModelErrorCode#STREAM_MARK_NOT_SUPPORTED}
	 * if the stream implementation does not support marks.
	 */
	default boolean wouldInvalidateMark() {
		throw new ModelException(ModelErrorCode.STREAM_MARK_NOT_SUPPORTED,
				"Default implemenattion does not support mark");
	}

	/**
	 * Closes this stream and releases all associated resources.
	 *
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	void close();
}
