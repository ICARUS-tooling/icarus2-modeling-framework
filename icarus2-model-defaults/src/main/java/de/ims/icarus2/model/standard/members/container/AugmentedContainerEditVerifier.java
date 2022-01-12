/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

/**
 * An extended default edit verifier that overrides the index validation methods
 * of {@link UnrestrictedContainerEditVerifier} to check that supplied indices are
 * not located within the "wrapped" part of the item storage.
 *
 * @author Markus Gärtner
 *
 */
public class AugmentedContainerEditVerifier extends UnrestrictedContainerEditVerifier {

	private AugmentedItemStorage storage;

	/**
	 * @param source
	 */
	public AugmentedContainerEditVerifier(Container source, AugmentedItemStorage storage) {
		super(source);

		this.storage = requireNonNull(storage);
	}

	@Override
	protected boolean isValidAddItemIndex(long index) {
//		System.out.printf("add: idx=%d wrapped=%b size=%d%n",index,
//				storage.isWrappedIndex(getSource(), index),
//				storage.getItemCount(getSource()));
		return index>=0
				&& !storage.isWrappedIndex(getSource(), index)
				&& index<=storage.getItemCount(getSource());
	}

	@Override
	protected boolean isValidRemoveItemIndex(long index) {
//		System.out.printf("rem: idx=%d wrapped=%b size=%d%n",index,
//				storage.isWrappedIndex(getSource(), index),
//				storage.getItemCount(getSource()));
		return index>=0
				&& !storage.isWrappedIndex(getSource(), index)
				&& index<storage.getItemCount(getSource());
	}

	/**
	 * @return the storage
	 */
	public AugmentedItemStorage getStorage() {
		return storage;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.UnrestrictedContainerEditVerifier#close()
	 */
	@Override
	public void close() {
		storage = null;
		super.close();
	}
}