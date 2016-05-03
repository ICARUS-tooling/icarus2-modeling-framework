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

 * $Revision: 445 $
 * $Date: 2016-01-11 17:33:05 +0100 (Mo, 11 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/layer/HighlightLayer.java $
 *
 * $LastChangedDate: 2016-01-11 17:33:05 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 445 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.layer;

import de.ims.icarus2.model.api.highlight.HighlightCursor;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.manifest.api.HighlightLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestOwner;

/**
 * @author Markus Gärtner
 * @version $Id: HighlightLayer.java 445 2016-01-11 16:33:05Z mcgaerty $
 *
 */
public interface HighlightLayer extends Layer, ManifestOwner<HighlightLayerManifest> {

	ItemLayer getPrimaryLayer();

	/**
	 * Returns a {@code HighlightCursor} that can be used to navigate over
	 * top-level highlights on the underlying {@link #getPrimaryLayer() primary layer}. If there are no
	 * top-level highlights available, this method should return {@code null}.
	 *
	 * @return
	 */
	HighlightCursor getHighlightCursor();

	/**
	 * Returns a {@code HighlightCursor} that can be used to navigate over
	 * highlights of the referenced layers top-level members (top-level members
	 * are the markables in that layers root container). If the container in
	 * question is not highlighted at all, this method returns {@code null}.
	 * Note that this method is intended for fetching highlights on nested containers
	 * and therefore will only be available if the <i>base-layer</i> is indeed
	 * built as a hierarchy of containers. If provided with the <i>base-layers</i>
	 * root container this method is essentially equal to calling {@link #getHighlightCursor()}.
	 *
	 * @param container The {@code Item} to fetch highlight information about
	 *
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 * @throws IllegalArgumentException if the {@code Item} is not a member of this
	 * layers <i>base-layer</i> as defined by {@link #getBaseLayer()} or if it is not a
	 * {@code Container}
	 */
	HighlightCursor getHighlightCursor(Container container);
}
