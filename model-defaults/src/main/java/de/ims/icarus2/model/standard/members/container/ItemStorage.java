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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/container/ItemStorage.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.container;

import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.util.Recyclable;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 * @version $Id: ItemStorage.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public interface ItemStorage extends ModelConstants, Recyclable {

	/**
	 * Signals that the storage is going to be used in a live environment
	 * defined by the {@code context} {@link Container}. This method is
	 * designed so that a storage can perform proper initialization work
	 * and do sanity checks in the container it is hosted in.
	 *
	 * @param context
	 *
	 * @throws ModelException in case the given container is unfit as a
	 * context for this storage
	 */
	void addNotify(Container context) throws ModelException;

	void removeNotify(Container context) throws ModelException;

	ContainerType getContainerType();

	long getItemCount(Container context);

	Item getItemAt(Container context, long index);

	long indexOfItem(Container context, Item item);

	void addItem(Container context, long index, Item item);

	void addItems(Container context, long index, DataSequence<? extends Item> items);

	Item removeItem(Container context, long index);

	DataSequence<? extends Item> removeItems(Container context, long index0, long index1);

	void moveItem(Container context, long index0, long index1);

	long getBeginOffset(Container context);

	long getEndOffset(Container context);

	ContainerEditVerifier createEditVerifier(Container context);

	boolean isDirty(Container context);
}
