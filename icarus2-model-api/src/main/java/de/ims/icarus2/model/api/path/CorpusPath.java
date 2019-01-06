/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;

/**
 *
 *
 * @author Markus Gärtner
 *
 */
public interface CorpusPath {

	PagedCorpusView getSource();

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
		public PagedCorpusView getSource() {
			return null;
		}
	};
}
