/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
 * Implements a <i>forward-only</i> traversal mechanism for
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
	 * Returns the current item in this stream.
	 *
	 * @return
	 *
	 * @throws ModelException with {@link ModelErrorCode#STREAM_NO_ITEM}
	 * in case there is no active item available currently.
	 */
	Item currentItem();

	// Support part

	/**
	 *
	 */
	void flush();

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
	 *
	 * @return
	 *
	 * @throws ModelException with {@link ModelErrorCode#STREAM_MARK_NOT_SUPPORTED}
	 * if the stream implementation does not support marks.
	 */
	default boolean clearMark() {
		throw new ModelException(ModelErrorCode.STREAM_MARK_NOT_SUPPORTED,
				"Default implemenattion does not support mark");
	}

	/**
	 *
	 * @return
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
	 * Closes this stream and releases all associated resources.
	 *
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	void close();
}
