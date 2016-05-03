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

 * $Revision: 382 $
 * $Date: 2015-04-09 16:23:50 +0200 (Do, 09 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/members/item/Fragment.java $
 *
 * $LastChangedDate: 2015-04-09 16:23:50 +0200 (Do, 09 Apr 2015) $
 * $LastChangedRevision: 382 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.members.item;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.raster.Rasterizer;

/**
 * A {@code Fragment} allows for the definition of {@code Item} objects that
 * are not bound by the logical structure of an existing foundation layer.
 * A regular item references parts of other existing items, like pre-tokenized test
 * and/or split objects in a text that forms the actual corpus. With the use of
 * fragments it is possible to handle <i>raw</i> data (text, audio, video, etc...).
 * Note however, that using fragments is much more expensive than using
 * items, since a lookup structure has to be built for each item object that
 * contains fragments, in order to visualize or explore it.
 * <p>
 * As a precondition for the use of fragments, a <i>value</i> annotation-layer has to be
 * present. The annotations in that layer are then {@link Rasterizer rasterized} to create
 * a discrete address space for each raw item. In this discrete space it is then possible
 * to form well defined parts using {@link Position position pointers} as begin and end of
 * actual {@link Fragment} objects.
 *
 * @author Markus Gärtner
 * @version $Id: Fragment.java 382 2015-04-09 14:23:50Z mcgaerty $
 *
 *@see Item
 */
public interface Fragment extends Item {

	@Override
	FragmentLayer getLayer();

	/**
	 * Returns the item this fragment is a part of.
	 *
	 * @return
	 */
	Item getItem();

	/**
	 * Returns the position within the surrounding item of
	 * this fragment that denotes the actual begin of the fragment itself.
	 *
	 * @return
	 */
	Position getFragmentBegin();

	/**
	 * Returns the position within the surrounding item of
	 * this fragment that denotes the actual end of the fragment itself.
	 *
	 * @return
	 */
	Position getFragmentEnd();

	// Modification methods

	/**
	 * Changes the begin position of the fragment to the new {@code position}.
	 *
	 * @param position
	 * @throws ModelException if the {@code position} violates
	 * the bounds specified by the raster size of the hosting item
	 */
	void setFragmentBegin(Position position);

	/**
	 * Changes the end position of the fragment to the new {@code position}.
	 *
	 * @param position
	 * @throws ModelException if the {@code position} violates
	 * the bounds specified by the raster size of the hosting item
	 */
	void setFragmentEnd(Position position);
}
