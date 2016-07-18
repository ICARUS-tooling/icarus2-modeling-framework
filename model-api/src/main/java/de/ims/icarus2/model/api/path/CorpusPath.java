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

import de.ims.icarus2.model.api.corpus.CorpusView;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;

/**
 *
 *
 * @author Markus Gärtner
 *
 */
public interface CorpusPath {

	CorpusView getSource();

	int getElementCount();

	PathElementType getType(int index);

	Layer getLayer(int index);

	Item getItem(int index);

	Edge getEdge(int index);

	long getIndex(int index);

	/**
	 * Models the different kinds of elements that can occur in a {@link CorpusPath}.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public enum PathElementType {

		/**
		 * Top level frame
		 */
		LAYER,

		/**
		 * Content of previous path element, which had to be a container/structure.
		 * <p>
		 * Since containers are allowed to contain duplicated, depending on the manifest
		 * settings, we don't point to the elements in a path, but rather list the index
		 * in the container or structure!
		 */
		ITEM_INDEX,

		/**
		 * Edge hosted within previous path element, which had to be a structure.
		 */
		EDGE_INDEX,

		/**
		 * Item unrelated to previous path element.
		 */
		ITEM,

		/**
		 * Edge unrelated to previous path element.
		 */
		EDGE,
		;

		private static final PathElementType[] _values = values();

		public static PathElementType forOrdinal(int index) {
			return _values[index];
		}
	}

	public static final CorpusPath EMPTY_PATH = new CorpusPath() {

		@Override
		public PathElementType getType(int index) {
			throw new IndexOutOfBoundsException();
		}

		@Override
		public Layer getLayer(int index) {
			throw new IndexOutOfBoundsException();
		}

		@Override
		public Item getItem(int index) {
			throw new IndexOutOfBoundsException();
		}

		@Override
		public long getIndex(int index) {
			throw new IndexOutOfBoundsException();
		}

		@Override
		public Edge getEdge(int index) {
			throw new IndexOutOfBoundsException();
		}

		@Override
		public int getElementCount() {
			return 0;
		}

		@Override
		public CorpusView getSource() {
			return null;
		}
	};
}
