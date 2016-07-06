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
package de.ims.icarus2.model.standard.highlight;

import static de.ims.icarus2.util.Conditions.checkNotNull;
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

		checkNotNull(items);
		checkNotNull(highlightInfos);

		this.items = items;
		this.highlightInfos = highlightInfos;
	}

	public SimpleHighlight(DataSet<Item> items) {

		checkNotNull(items);

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
