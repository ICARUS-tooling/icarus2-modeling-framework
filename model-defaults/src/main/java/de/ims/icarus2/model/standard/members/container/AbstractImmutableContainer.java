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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/container/AbstractImmutableContainer.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.container;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 * @version $Id: AbstractImmutableContainer.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public abstract class AbstractImmutableContainer implements Container {

	private <T extends Object> T signalUnsupportedOperation() {
		throw new ModelException(getCorpus(), GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Container is immutable");
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.Item#setIndex(long)
	 */
	@Override
	public void setIndex(long newIndex) {
		signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.CONTAINER;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#createEditVerifier()
	 */
	@Override
	public ContainerEditVerifier createEditVerifier() {
		return new ImmutableContainerEditVerifier(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#addItem(long, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public void addItem(long index, Item item) {
		signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#addItems(long, de.ims.icarus2.util.collections.seq.DataSequence)
	 */
	@Override
	public void addItems(long index, DataSequence<? extends Item> items) {
		signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#removeItem(long)
	 */
	@Override
	public Item removeItem(long index) {
		return signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#removeItems(long, long)
	 */
	@Override
	public DataSequence<? extends Item> removeItems(long index0, long index1) {
		return signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#moveItem(long, long)
	 */
	@Override
	public void moveItem(long index0, long index1) {
		signalUnsupportedOperation();
	}

}
