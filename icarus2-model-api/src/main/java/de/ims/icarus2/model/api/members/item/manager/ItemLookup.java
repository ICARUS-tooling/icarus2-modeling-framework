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
package de.ims.icarus2.model.api.members.item.manager;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;

import de.ims.icarus2.model.api.members.item.Item;

/**
 * Specifies an abstract read-only storage for {@code Item} instances.
 * Access to those items is modeled in an array-like style together with
 * a lookup method to fetch the index within the storage a given item
 * is located at.
 * <p>
 * Note that all index values returned or taken as input parameters
 * are of type {@link long}!
 *
 * @author Markus Gärtner
 *
 */
public interface ItemLookup {

	long getItemCount();

	default boolean isEmpty() {
		return getItemCount()==0L;
	}

	Item getItemAt(long index);

	/**
	 * Return the index of given {@code item} or {@code -1}
	 * if the item is nor contained in this lookup.
	 * @param item
	 * @return
	 */
	long indexOfItem(Item item);

	default void forEachItem(ObjLongConsumer<? super Item> action) {
		requireNonNull(action);
		long size = getItemCount();
		for(long i = 0; i<size; i++) {
			action.accept(getItemAt(i), i);
		}
	}

	default void forEachItem(Consumer<? super Item> action) {
		requireNonNull(action);
		long size = getItemCount();
		for(long i = 0; i<size; i++) {
			action.accept(getItemAt(i));
		}
	}
}
