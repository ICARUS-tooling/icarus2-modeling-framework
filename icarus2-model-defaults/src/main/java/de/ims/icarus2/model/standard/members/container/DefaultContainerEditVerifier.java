/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
