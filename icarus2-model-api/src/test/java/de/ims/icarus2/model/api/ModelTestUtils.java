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
/**
 *
 */
package de.ims.icarus2.model.api;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.test.TestUtils.assertMock;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.collections.seq.DataSequence;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class ModelTestUtils {

	private static boolean equals(Item item, Object obj) {
		return obj instanceof Item && item.getId()==((Item)obj).getId();
	}

	@SuppressWarnings("boxing")
	public static <I extends Item> I stubId(I item, long id) {
		assertMock(item);
		when(item.getId()).thenReturn(id);
//		when(item.equals(any())).then(
//				invocation -> equals(item, invocation.getArgument(0)));
		return item;
	}

	public static Item mockItem() {
		return mock(Item.class);
	}

	public static Edge mockEdge() {
		return stubHost(mock(Edge.class), STRUCTURE);
	}

	public static Edge mockEdge(Structure structure) {
		return stubHost(mock(Edge.class), structure);
	}

	private static Edge stubHost(Edge edge, Structure structure) {
		when(edge.getStructure()).thenReturn(structure);
		return edge;
	}

	public static Edge mockEdge(Item source, Item target) {
		requireNonNull(source);
		requireNonNull(target);

		Edge edge = mockEdge();
		when(edge.getSource()).thenReturn(source);
		when(edge.getTarget()).thenReturn(target);

		return edge;
	}

	public static Edge mockEdge(Structure structure, Item source, Item target) {
		return stubHost(mockEdge(source, target), structure);
	}

	@SuppressWarnings("boxing")
	public static Container mockContainer(long itemCount) {
		checkArgument(itemCount>=0);

		Container container = mock(Container.class);
		when(container.getItemCount()).thenReturn(itemCount);

		if(itemCount>0) {
			stubItems(container);
		}

		return container;
	}

	private static void checkItemIndex(Container container, long index) {
		if(index<0 || index>=container.getItemCount())
			throw new IndexOutOfBoundsException();
	}

	@SuppressWarnings("boxing")
	public static void stubItems(Container container) {

		final Long2ObjectMap<Item> items = new Long2ObjectOpenHashMap<>();
		final Object2LongMap<Item> indices = new Object2LongOpenHashMap<>();
		indices.defaultReturnValue(-1);

		when(container.getItemAt(anyLong())).then(invocation -> {
			long index = invocation.getArgument(0);
			checkItemIndex(container, index);

			return items.computeIfAbsent(index, k -> {
				Item item = stubId(mockItem(), k);
				indices.put(item, k);
				return item;
			});
		});

		when(container.indexOfItem(any())).then(
				invocation -> indices.getLong(invocation.getArgument(0)));
	}

	@SuppressWarnings("boxing")
	public static Structure mockStructure(long itemCount, long edgeCount) {
		checkArgument(itemCount>=0);
		checkArgument(edgeCount>=0);

		Structure structure = mock(Structure.class);
		when(structure.getItemCount()).thenReturn(itemCount);
		when(structure.getEdgeCount()).thenReturn(edgeCount);

		if(itemCount>0) {
			stubItems(structure);
		}

		return structure;
	}

	private static void checkEdgeIndex(Structure structure, long index) {
		if(index<0 || index>=structure.getEdgeCount())
			throw new IndexOutOfBoundsException();
	}

	public static Structure mockStructure(long itemCount, Pair<Long, Long>...edges) {

		Structure structure = mockStructure(itemCount, edges.length);

		//TODO stub the edges

		return structure;
	}

	public static final Item ITEM = mockItem();
	public static final DataSequence<Item> ITEM_SEQUENCE = mockSequence(1, ITEM);
	public static final Edge EDGE = mockEdge();
	public static final DataSequence<Edge> EDGE_SEQUENCE = mockSequence(1, EDGE);
	public static final Container CONTAINER = mockContainer(0);
	public static final Structure STRUCTURE = mockStructure(0, 0);

	public static void assertIllegalMember(Executable executable, String msg) {
		assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER, executable, msg);
	}

	public static void assertIllegalMember(Executable executable) {
		assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER, executable, null);
	}

	public static void assertModelException(ErrorCode errorCode, Executable executable, String msg) {
		ModelException exception = assertThrows(ModelException.class, executable, msg);
		assertEquals(errorCode, exception.getErrorCode(), msg);
	}
}
