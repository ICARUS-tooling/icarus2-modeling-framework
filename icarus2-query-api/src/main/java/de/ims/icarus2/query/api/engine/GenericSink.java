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
/**
 *
 */
package de.ims.icarus2.query.api.engine;

/**
 * @author Markus Gärtner
 *
 */
public interface GenericSink {

	/**
	 * Initialize the sink and prepare if for an as of yet unknown number of
	 * elements to be consumed. This initialization method is used when the
	 * engine cannot estimate the number of elements and when processing
	 * is performed live, i.e. the down stream implementation is not
	 * using any form of buffering.
	 * <p>
	 * The engine guarantees that this method will be called a maximum of one times
	 * for any {@link GenericSink} instance.
	 */
	void prepare();

	/**
	 * Initialize the sink to expect a given number of elements to be consumed
	 * (given as upper limit).
	 * This initialization method is used whenever the engine can reliably
	 * determine the total number of elements, e.g. from a set result size
	 * limit in the query.
	 * <p>
	 * The engine guarantees that this method will be called a maximum of one times
	 * for any {@link GenericSink} instance.
	 * @param size the maximum number of elements to be expected, never 0 or less
	 */
	void prepare(int size);

	/**
	 * Called when the process was terminated prematurely, either due to
	 * buffer problems or an (internal) error. The behavior of this method is
	 * largely unspecified and implementation-dependent. Generally an implementation
	 * should make an effort to gracefully discard already consumed elements and
	 * roll back any save operations made for those.
	 * <p>
	 * The engine guarantees that this method will be called a maximum of one times
	 * for any {@link GenericSink} instance.
	 */
	void discard() throws InterruptedException;

	/**
	 * Called by the engine after the processing terminated successfully.
	 * <p>
	 * The engine guarantees that this method will be called a maximum of one times
	 * for any {@link GenericSink} instance when the last valid element has been
	 * consumed.
	 */
	void finish() throws InterruptedException;
}
