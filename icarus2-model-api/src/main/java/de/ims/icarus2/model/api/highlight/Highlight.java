/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.highlight;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * Lightweight meta annotation used for highlighting a (potentially empty)
 * collection of {@link Item items}. Each {@link Highlight} object is linked
 * to a non-empty list of shared {@link HighlightInfo} instances that contain
 * the actual metadata describing the source and nature of the highlight.
 * Note that those metadata entries apply to all the items the respective
 * {@code Highlight} instance {@link DataSet#contains(Item) affects}!
 * <p>
 * This interface is defined in a way that allows it to be implemented as
 * a composite object that holds sharable instances of both {@link DataSet}
 * implementations (for items and highlight info), thereby minimizing the
 * memory footprint to simple references.
 * <p>
 * Implementations of this interface should be immutable!
 *
 * @author Markus Gärtner
 *
 */
public interface Highlight {

	DataSet<Item> getAffectedItems();

	DataSet<HighlightInfo> getHighlightInfos();
}
