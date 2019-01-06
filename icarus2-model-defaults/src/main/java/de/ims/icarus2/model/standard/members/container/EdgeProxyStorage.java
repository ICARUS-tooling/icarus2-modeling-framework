/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.model.standard.members.container;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public class EdgeProxyStorage implements ItemStorage {

	private final Structure structure;

	public EdgeProxyStorage(Structure structure) {
		if (structure == null)
			throw new NullPointerException("Invalid structure");

		//TODO maybe check for 'edges-static' flag in manifest?

		this.structure = structure;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return true;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#addNotify(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public void addNotify(Container context) throws ModelException {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#removeNotify(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public void removeNotify(Container context) throws ModelException {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.LIST;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getItemCount(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getItemCount(Container context) {
		return structure.getEdgeCount();
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getItemAt(de.ims.icarus2.model.api.members.container.Container, long)
	 */
	@Override
	public Edge getItemAt(Container context, long index) {
		return structure.getEdgeAt(index);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#indexOfItem(de.ims.icarus2.model.api.members.container.Container, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public long indexOfItem(Container context, Item item) {
		return item.getMemberType()==MemberType.EDGE ?
				structure.indexOfEdge((Edge) item) : IcarusUtils.UNSET_LONG;
	}

	/**
	 * Signal unsupported attempt to perform a modification on the underlying
	 * structure. Since this implementation is only a proxy wrapper around a
	 * structure all modifying methods are bound to fail.
	 */
	private <T extends Object> T signalUnsupportedOperation(Container context) {
		throw new ModelException(context.getCorpus(), GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Edge proxy storage cannot modify underlying structure");
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
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getBeginOffset(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getBeginOffset(Container context) {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#getEndOffset(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public long getEndOffset(Container context) {
		return IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorage#createEditVerifier(de.ims.icarus2.model.api.members.container.Container)
	 */
	@Override
	public ContainerEditVerifier createEditVerifier(Container context) {
		return new ImmutableContainerEditVerifier(context);
	}

	@Override
	public boolean isDirty(Container context) {
		return structure.isDirty();
	}

}
