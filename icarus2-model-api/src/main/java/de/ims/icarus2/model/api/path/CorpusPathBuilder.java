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