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
import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractImmutableContainer implements Container {

	private <T extends Object> T signalUnsupportedOperation() {
		throw new ModelException(getCorpus(), GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Container is immutable");
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
	@Unguarded(Unguarded.UNSUPPORTED)
	public void addItem(long index, Item item) {
		signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#addItems(long, de.ims.icarus2.util.collections.seq.DataSequence)
	 */
	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public void addItems(long index, DataSequence<? extends Item> items) {
		signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#removeItem(long)
	 */
	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public Item removeItem(long index) {
		return signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#removeItems(long, long)
	 */
	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public DataSequence<? extends Item> removeItems(long index0, long index1) {
		return signalUnsupportedOperation();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.container.Container#swapItems(long, long)
	 */
	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public void swapItems(long index0, long index1) {
		signalUnsupportedOperation();
	}

}
