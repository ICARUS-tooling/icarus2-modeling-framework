/*
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
package de.ims.icarus2.model.api.view.streamed;

/**
 * @author Markus Gärtner
 *
 */
public enum StreamOption {



	/**
	 * If set, then the stream implementation is capable of skipping
	 * portions of the underlying data.
	 * <p>
	 * This usually comes with a certain trade-off in terms of buffer
	 * efficiency, and client code should only decide to use this
	 * option if frequent skips over large numbers of items are to be
	 * expected.
	 */
	ALLOW_SKIP,

	/**
	 * If set, client code can put a mark on an item in the stream
	 * and then successively go back to that item later.
	 * <p>
	 * Note that implementations might impose a limit on the number
	 * of items between the mark and the current position in the
	 * stream up to which the mark can be kept alive.
	 */
	ALLOW_MARK,
	;
}
