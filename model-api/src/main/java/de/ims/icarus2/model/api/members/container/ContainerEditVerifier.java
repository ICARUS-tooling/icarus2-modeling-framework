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

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/members/container/ContainerEditVerifier.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.members.container;

import de.ims.icarus2.model.api.members.EditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * Verification mechanism to check preconditions for certain actions
 * on a {@link Container} object.
 * Note that implementations of this interface do not have to be thread-safe,
 * meaning that the results obtained by their precondition checks can only
 * guarantee success of the respective modification method when either done
 * in a strictly single-thread access environment or if the client code can
 * ensure a similar pattern.
 *
 * @author Markus Gärtner
 * @version $Id: ContainerEditVerifier.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public interface ContainerEditVerifier extends EditVerifier<Container> {

	/**
	 * Precondition check for the {@link Container#addItem(long, Item)} method.
	 *
	 * @param index
	 * @param item
	 * @return
	 */
	boolean canAddItem(long index, Item item);

	/**
	 * Precondition check for the {@link Container#addItems(long, DataSequence)} method.
	 *
	 * @param index
	 * @param items
	 * @return
	 */
	boolean canAddItems(long index, DataSequence<? extends Item> items);

	/**
	 * Precondition check for the {@link Container#removeItem(long)} method.
	 *
	 * @param index
	 * @return
	 */
	boolean canRemoveItem(long index);

	/**
	 * Precondition check for the {@link Container#removeItems(long, long)} method.
	 *
	 * @param index0
	 * @param index1
	 * @return
	 */
	boolean canRemoveItems(long index0, long index1);

	/**
	 * Precondition check for the {@link Container#moveItem(long, long)} method.
	 *
	 * @param index0
	 * @param index1
	 * @return
	 */
	boolean canMoveItem(long index0, long index1);
}
