/**
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
package de.ims.icarus2.model.standard.highlight;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.highlight.HighlightCursor;
import de.ims.icarus2.model.api.layer.HighlightLayer;
import de.ims.icarus2.model.api.members.container.Container;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractHighlightCursor implements HighlightCursor {

	private final HighlightLayer layer;
	private final Container target;

	public AbstractHighlightCursor(HighlightLayer layer, Container target) {
		requireNonNull(layer);
		requireNonNull(target);

		this.layer = layer;
		this.target = target;
	}

	/**
	 * @see de.ims.icarus2.model.api.highlight.HighlightCursor#getHighlightLayer()
	 */
	@Override
	public HighlightLayer getHighlightLayer() {
		return layer;
	}

	/**
	 * @see de.ims.icarus2.model.api.highlight.HighlightCursor#getTarget()
	 */
	@Override
	public Container getTarget() {
		return target;
	}
}
