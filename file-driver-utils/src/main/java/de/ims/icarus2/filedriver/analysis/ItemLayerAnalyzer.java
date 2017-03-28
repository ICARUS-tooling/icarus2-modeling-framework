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
 */
package de.ims.icarus2.filedriver.analysis;

import java.util.function.ObjLongConsumer;

import de.ims.icarus2.model.api.members.item.Item;

/**
 * An analyzer that consumes {@link Item items} to collect its metadata.
 *
 * @author Markus Gärtner
 *
 */
public interface ItemLayerAnalyzer extends Analyzer, ObjLongConsumer<Item> {

	/**
	 * Analyzer usable as empty fallback. This implementation does nothing,
	 * all methods simply return.
	 */
	public static final ItemLayerAnalyzer EMPTY_ANALYZER = new ItemLayerAnalyzer() {

		@Override
		public void accept(Item t, long value) {
			// no-op
		}
	};
}
