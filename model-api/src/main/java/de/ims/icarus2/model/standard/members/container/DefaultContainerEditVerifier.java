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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/container/DefaultContainerEditVerifier.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.container;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.util.DataSequence;

/**
 * @author Markus Gärtner
 * @version $Id: DefaultContainerEditVerifier.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public class DefaultContainerEditVerifier implements ContainerEditVerifier {

	private Container source;

	public DefaultContainerEditVerifier(Container source) {
		checkNotNull(source);

		this.source = source;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#getSource()
	 */
	@Override
	public Container getSource() {
		return source;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#close()
	 */
	@Override
	public void close() {
		source = null;
	}

	protected boolean isValidAddItemIndex(long index) {
		return index>=0L && index<=source.getItemCount();
	}

	protected boolean isValidRemoveItemIndex(long index) {
		return index>=0L && index<source.getItemCount();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#canAddItem(long, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean canAddItem(long index, Item item) {
		return item!=null && isValidAddItemIndex(index);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#canAddItems(long, de.ims.icarus2.model.util.DataSequence)
	 */
	@Override
	public boolean canAddItems(long index, DataSequence<? extends Item> items) {
		return items!=null && isValidAddItemIndex(index);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#canRemoveItem(long)
	 */
	@Override
	public boolean canRemoveItem(long index) {
		return isValidRemoveItemIndex(index);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#canRemoveItems(long, long)
	 */
	@Override
	public boolean canRemoveItems(long index0, long index1) {
		return isValidRemoveItemIndex(index0) && isValidRemoveItemIndex(index1) && index0<=index1;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#canMoveItem(long, long)
	 */
	@Override
	public boolean canMoveItem(long index0, long index1) {
		return isValidRemoveItemIndex(index0) && isValidRemoveItemIndex(index1);
	}
}
