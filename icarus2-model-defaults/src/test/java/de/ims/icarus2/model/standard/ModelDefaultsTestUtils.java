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
/**
 *
 */
package de.ims.icarus2.model.standard;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.standard.members.item.DefaultItem;
import de.ims.icarus2.model.standard.members.structure.DefaultEdge;
import de.ims.icarus2.test.util.Pair;

/**
 * @author Markus Gärtner
 *
 */
public class ModelDefaultsTestUtils {

	public static Item item(Container container) {
		return new DefaultItem(container);
	}

	public static Edge edge(Structure structure, Item source, Item target) {
		return new DefaultEdge(structure, source, target);
	}

	public static <C extends Container> C prepareContainer(C container,
			Item...items) {
		requireNonNull(container);
		checkArgument(items.length>0);

		for(Item item : items) {
			container.addItem(item);
		}

		return container;
	}

	public static <C extends Container> C prepareContainer(C container,
			int itemCount) {
		requireNonNull(container);
		checkArgument(itemCount>0);

		while(itemCount-->0) {
			container.addItem(item(container));
		}

		return container;
	}

	@SafeVarargs
	public static <S extends Structure> S prepareStructure(S structure,
			Pair<Long, Long>...entries) {
		requireNonNull(structure);
		checkArgument(entries.length>0);

		for(Pair<Long, Long> entry : entries) {
			@SuppressWarnings("boxing")
			Item source = structure.getItemAt(entry.first);
			@SuppressWarnings("boxing")
			Item target = structure.getItemAt(entry.second);
			structure.addEdge(edge(structure, source, target));
		}

		return structure;
	}
}
