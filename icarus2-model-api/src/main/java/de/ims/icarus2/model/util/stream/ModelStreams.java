/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.util.stream;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.StructureType;

/**
 * @author Markus Gärtner
 *
 */
public class ModelStreams {

	public static Stream<Item> newElementStream(Container container) {
		return StreamSupport.stream(new ContainerSpliterator(container), false);

	}

	public static Stream<Item> newNodeStream(Structure structure) {
		// For plain sets (aka empty structures) we can delegate to the basic elements stream
		if(structure.getStructureType()==StructureType.SET) {
			return newElementStream(structure);
		}
		return StreamSupport.stream(new StructureNodeSpliterator(structure), false);

	}

	public static Stream<Edge> newEdgeStream(Structure structure) {
		return StreamSupport.stream(new StructureEdgeSpliterator(structure), false);

	}
}
