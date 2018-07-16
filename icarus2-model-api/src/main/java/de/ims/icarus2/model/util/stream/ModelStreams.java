/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
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
