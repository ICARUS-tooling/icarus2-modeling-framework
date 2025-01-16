/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.layer;

import de.ims.icarus2.model.api.highlight.HighlightCursor;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.manifest.api.HighlightLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestOwner;

/**
 * @author Markus Gärtner
 *
 */
public interface HighlightLayer extends Layer, ManifestOwner<HighlightLayerManifest> {

	ItemLayer getPrimaryLayer();

	/**
	 * Returns a {@code HighlightCursor} that can be used to navigate over
	 * top-level highlights on the underlying {@link #getPrimaryLayer() primary layer}. If there are no
	 * top-level highlights available, this method should return {@code null}.
	 *
	 * @return
	 */
	HighlightCursor getHighlightCursor();

	/**
	 * Returns a {@code HighlightCursor} that can be used to navigate over
	 * highlights of the referenced layers top-level members (top-level members
	 * are the markables in that layers root container). If the container in
	 * question is not highlighted at all, this method returns {@code null}.
	 * Note that this method is intended for fetching highlights on nested containers
	 * and therefore will only be available if the <i>base-layer</i> is indeed
	 * built as a hierarchy of containers. If provided with the <i>base-layers</i>
	 * root container this method is essentially equal to calling {@link #getHighlightCursor()}.
	 *
	 * @param container The {@code Item} to fetch highlight information about
	 *
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 * @throws IllegalArgumentException if the {@code Item} is not a member of this
	 * layers <i>base-layer</i> as defined by {@link #getBaseLayer()} or if it is not a
	 * {@code Container}
	 */
	HighlightCursor getHighlightCursor(Container container);
}
