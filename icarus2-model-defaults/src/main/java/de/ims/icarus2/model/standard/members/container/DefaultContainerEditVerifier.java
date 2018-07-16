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
package de.ims.icarus2.model.standard.members.container;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultContainerEditVerifier implements ContainerEditVerifier {

	private Container source;

	public DefaultContainerEditVerifier(Container source) {
		requireNonNull(source);

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
	 * @see de.ims.icarus2.model.api.members.container.ContainerEditVerifier#canAddItems(long, de.ims.icarus2.util.collections.seq.DataSequence)
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
