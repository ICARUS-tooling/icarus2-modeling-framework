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
	 * Precondition check for the {@link Container#swapItems(long, long)} method.
	 *
	 * @param index0
	 * @param index1
	 * @return
	 */
	boolean canSwapItems(long index0, long index1);
}
