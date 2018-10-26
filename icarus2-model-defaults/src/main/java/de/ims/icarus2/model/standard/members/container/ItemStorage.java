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

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.util.Recyclable;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public interface ItemStorage extends Recyclable {

	/**
	 * Signals that the storage is going to be used in a live environment
	 * defined by the {@code context} {@link Container}. This method is
	 * designed so that a storage can perform proper initialization work
	 * and do sanity checks in the container it is hosted in.
	 *
	 * @param context
	 *
	 * @throws ModelException in case the given container is unfit as a
	 * context for this storage
	 */
	void addNotify(Container context) throws ModelException;

	void removeNotify(Container context) throws ModelException;

	ContainerType getContainerType();

	long getItemCount(Container context);

	Item getItemAt(Container context, long index);

	long indexOfItem(Container context, Item item);

	void addItem(Container context, long index, Item item);

	void addItems(Container context, long index, DataSequence<? extends Item> items);

	Item removeItem(Container context, long index);

	DataSequence<? extends Item> removeItems(Container context, long index0, long index1);

	void moveItem(Container context, long index0, long index1);

	long getBeginOffset(Container context);

	long getEndOffset(Container context);

	ContainerEditVerifier createEditVerifier(Container context);

	boolean isDirty(Container context);
}
