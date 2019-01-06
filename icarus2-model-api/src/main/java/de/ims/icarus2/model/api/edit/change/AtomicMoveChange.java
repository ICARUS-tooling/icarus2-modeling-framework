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
package de.ims.icarus2.model.api.edit.change;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;

/**
 *
 * @author Markus Gärtner
 *
 * @param <E> type of element that was moved
 * @param <C> type of container the element has been moved within
 */
public interface AtomicMoveChange<E extends Item, C extends Container> extends AtomicChange {

	E getSourceElement();

	E getTargetElement();

	C getContainer();

	long getSourceIndex();

	long getTargetIndex();
}
