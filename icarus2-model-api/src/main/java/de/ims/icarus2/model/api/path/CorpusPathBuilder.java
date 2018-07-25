/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.api.path;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;

/**
 * @author Markus Gärtner
 *
 */
public class CorpusPathBuilder {

	private final PagedCorpusView view;

	public CorpusPathBuilder(PagedCorpusView view) {
		requireNonNull(view);

		this.view = view;
	}


	public void reset() {

	}


	public void appendLayer(Layer layer) {

	}

	public void appendItem(Item item) {

	}

	public void appendEdge(Edge edge) {

	}

	public void appendItemIndex(long index) {

	}

	public void appendEdgeIndex(long index) {

	}



	public CorpusPath createPath() {
		//TODO implement actual builder logic!
		return null;
	}
}
