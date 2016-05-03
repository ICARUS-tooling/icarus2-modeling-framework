/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.api.highlight;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.util.DataSet;

/**
 * Lightweight meta annotation used for highlighting a (potentially empty)
 * collection of {@link Item items}. Each {@link Highlight} object is linked
 * to a non-empty list of shared {@link HighlightInfo} instances that contain
 * the actual metadata describing the source and nature of the highlight.
 * Note that those metadata entries apply to all the items the respective
 * highlight instance {@link DataSet#contains(Item) affects}!
 * <p>
 * This interface is defined in a way that allows it to be implemented as
 * a composite object that holds sharable instances of both {@link DataSet}
 * implementations (for items and highlight info), thereby minimizing the
 * memory footprint to simple references.
 * <p>
 * Implementations of this interface should be immutable!
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Highlight {

	DataSet<Item> getAffectedItems();

	DataSet<HighlightInfo> getHighlightInfos();
}
