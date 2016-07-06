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

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractImmutableItemStorage implements ItemStorage {

	private <T extends Object> T signalUnsupportedOperation(Container context) {
		throw new ModelException(context.getCorpus(), GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Item storage is immutable");
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#addItem(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public void addItem(Container context, long index, Item item) {
		signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#addItems(de.ims.icarus2.model.api.members.container.Container, long, de.ims.icarus2.util.collections.seq.DataSequence)
	 */
	@Override
	public void addItems(Container context, long index,
			DataSequence<? extends Item> items) {
		signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeItem(de.ims.icarus2.model.api.members.container.Container, long)
	 */
	@Override
	public Item removeItem(Container context, long index) {
		return signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeItems(de.ims.icarus2.model.api.members.container.Container, long, long)
	 */
	@Override
	public DataSequence<? extends Item> removeItems(Container context,
			long index0, long index1) {
		return signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#moveItem(de.ims.icarus2.model.api.members.container.Container, long, long)
	 */
	@Override
	public void moveItem(Container context, long index0, long index1) {
		signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#createEditVerifier(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public ContainerEditVerifier createEditVerifier(Container context) {
		return new ImmutableContainerEditVerifier(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#isDirty(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public boolean isDirty(Container context) {
		return false;
	}

}
