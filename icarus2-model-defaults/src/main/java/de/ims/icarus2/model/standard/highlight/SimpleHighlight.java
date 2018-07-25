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
package de.ims.icarus2.model.standard.highlight;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.highlight.Highlight;
import de.ims.icarus2.model.api.highlight.HighlightInfo;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
public class SimpleHighlight implements Highlight {

	private final DataSet<Item> items;
	private final DataSet<HighlightInfo> highlightInfos;

	public SimpleHighlight(DataSet<Item> items,
			DataSet<HighlightInfo> highlightInfos) {

		requireNonNull(items);
		requireNonNull(highlightInfos);

		this.items = items;
		this.highlightInfos = highlightInfos;
	}

	public SimpleHighlight(DataSet<Item> items) {

		requireNonNull(items);

		this.items = items;
		this.highlightInfos = DataSet.emptySet();
	}

	/**
	 * @see de.ims.icarus2.model.api.highlight.Highlight#getAffectedItems()
	 */
	@Override
	public DataSet<Item> getAffectedItems() {
		return items;
	}

	/**
	 * @see de.ims.icarus2.model.api.highlight.Highlight#getHighlightInfos()
	 */
	@Override
	public DataSet<HighlightInfo> getHighlightInfos() {
		return highlightInfos;
	}

}
