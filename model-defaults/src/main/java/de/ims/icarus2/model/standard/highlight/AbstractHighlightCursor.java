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
package de.ims.icarus2.model.standard.highlight;

import static de.ims.icarus2.util.Conditions.checkNotNull;
import de.ims.icarus2.model.api.highlight.HighlightCursor;
import de.ims.icarus2.model.api.layer.HighlightLayer;
import de.ims.icarus2.model.api.members.container.Container;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractHighlightCursor implements HighlightCursor {

	private final HighlightLayer layer;
	private final Container target;

	public AbstractHighlightCursor(HighlightLayer layer, Container target) {
		checkNotNull(layer);
		checkNotNull(target);

		this.layer = layer;
		this.target = target;
	}

	/**
	 * @see de.ims.icarus2.model.api.highlight.HighlightCursor#getHighlightLayer()
	 */
	@Override
	public HighlightLayer getHighlightLayer() {
		return layer;
	}

	/**
	 * @see de.ims.icarus2.model.api.highlight.HighlightCursor#getTarget()
	 */
	@Override
	public Container getTarget() {
		return target;
	}
}
