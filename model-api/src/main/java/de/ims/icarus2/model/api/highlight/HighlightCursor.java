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

 * $Revision: 380 $
 * $Date: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/highlight/HighlightCursor.java $
 *
 * $LastChangedDate: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $LastChangedRevision: 380 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.highlight;

import de.ims.icarus2.model.api.layer.HighlightLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.util.collections.DataSequence;

/**
 *
 * @author Markus Gärtner
 * @version $Id: HighlightCursor.java 380 2015-04-02 01:28:48Z mcgaerty $
 * @see Highlight
 *
 */
public interface HighlightCursor {

	Container getTarget();

	/**
	 * Returns the {@code HighlightLayer} this cursor originated from
	 */
	HighlightLayer getHighlightLayer();

	/**
	 * Returns the total number of {@link #getHighlights(int) highlight sequences}
	 * available in this cursor concurrently.
	 *
	 * @return
	 */
	int getConcurrentHighlightCount();

	/**
	 * Returns one of the highlight sequences of this cursor identified by the given
	 * {@code index}. Legal values for {@code index} range from {@code 0} up to
	 * {@link #getConcurrentHighlightCount()}-{@code 1}.
	 *
	 * @param index
	 * @return
	 */
	DataSequence<Highlight> getHighlights(int index);
}
