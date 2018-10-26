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
package de.ims.icarus2.model.api.highlight;

import de.ims.icarus2.model.api.layer.HighlightLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 *
 * @author Markus Gärtner
 * @see Highlight
 *
 */
public interface HighlightCursor {

	Container getTarget();

	/**
	 * Returns the {@code HighlightLayer} this cursor originated from
	 */
	HighlightLayer getHighlightLayer();

	/**
	 * Returns the total number of {@link #getHighlights(int) highlight sequences}
	 * available in this cursor concurrently.
	 *
	 * @return
	 */
	int getConcurrentHighlightCount();

	/**
	 * Returns one of the highlight sequences of this cursor identified by the given
	 * {@code index}. Legal values for {@code index} range from {@code 0} up to
	 * {@link #getConcurrentHighlightCount()}-{@code 1}.
	 *
	 * @param index
	 * @return
	 */
	DataSequence<Highlight> getHighlights(int index);
}
