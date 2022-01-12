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
package de.ims.icarus2.model.standard;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.standard.members.container.DefaultContainer;
import de.ims.icarus2.model.standard.members.container.ItemStorage;
import de.ims.icarus2.model.standard.members.item.DefaultItem;
import de.ims.icarus2.model.standard.members.structure.DefaultEdge;
import de.ims.icarus2.model.standard.members.structure.DefaultStructure;
import de.ims.icarus2.model.standard.members.structure.EdgeStorage;
import de.ims.icarus2.test.util.Pair;

/**
 * @author Markus Gärtner
 *
 */
public class ModelDefaultsTestUtils {

	private static final Consumer<Exception> EXPECT_NPE =
			e -> assertEquals(NullPointerException.class, e.getClass());

	public static final <E extends Exception> Consumer<? super E> expectNPE() {
		return EXPECT_NPE;
	}

	public static final <E extends Exception> Consumer<? super E> expectErrorType(ErrorCode errorCode) {
		return e -> {
			assertTrue(IcarusRuntimeException.class.isInstance(e), () -> e.getClass().getName());
			assertEquals(errorCode, ((IcarusRuntimeException)e).getErrorCode());
		};
	}

	public static Item makeItem(Container container) {
		return new DefaultItem(container);
	}

	public static Item makeItem(Container container, long id) {
		return new DefaultItem(container, id);
	}

	public static Edge makeEdge(Structure structure) {
		return new DefaultEdge(structure);
	}

	public static Edge makeEdge(Structure structure, Item source, Item target) {
		return new DefaultEdge(structure, source, target);
	}

	public static <C extends Container> C fillItems(C container,
			Item...items) {
		requireNonNull(container);
		checkArgument(items.length>0);

		for(Item item : items) {
			container.addItem(item);
		}

		return container;
	}

	public static <C extends Container> C fillItems(C container,
			int itemCount) {
		requireNonNull(container);
		checkArgument(itemCount>0);

		int id = 0;
		while(itemCount-->0) {
			container.addItem(makeItem(container, id++));
		}

		return container;
	}

	private static Item itemAt(Structure structure, long index) {
		return index==-1 ? structure.getVirtualRoot() : structure.getItemAt(index);
	}

	@SafeVarargs
	public static <S extends Structure, N extends Number> S fillEdges(S structure,
			Pair<N, N>...entries) {
		requireNonNull(structure);
		checkArgument(entries.length>0);

		for(Pair<N, N> entry : entries) {
			Item source = itemAt(structure, entry.first.longValue());
			Item target = itemAt(structure, entry.second.longValue());
			structure.addEdge(makeEdge(structure, source, target));
		}

		return structure;
	}

	public static DefaultContainer makeContainer(ContainerManifest manifest) {
		requireNonNull(manifest);

		return new DefaultContainer() {
			@Override
			public ContainerManifest getManifest() {
				return manifest;
			}
		};
	}

	public static DefaultContainer makeContainer(ContainerManifest manifest,
			ItemStorage itemStorage) {
		DefaultContainer container = makeContainer(manifest);
		container.setItemStorage(itemStorage);
		return container;
	}

	public static DefaultStructure makeStructure(StructureManifest manifest) {
		requireNonNull(manifest);

		return new DefaultStructure() {
			@Override
			public StructureManifest getManifest() {
				return manifest;
			}
		};
	}

	public static DefaultStructure makeStructure(StructureManifest manifest,
			ItemStorage itemStorage, EdgeStorage edgeStorage) {
		DefaultStructure structure = makeStructure(manifest);
		structure.setItemStorage(itemStorage);
		structure.setEdgeStorage(edgeStorage);
		return structure;
	}
}
