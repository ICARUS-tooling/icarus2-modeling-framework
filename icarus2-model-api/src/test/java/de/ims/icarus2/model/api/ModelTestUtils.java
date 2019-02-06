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
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.RUNS_EXHAUSTIVE;
import static de.ims.icarus2.test.TestUtils.assertMock;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.function.BiFunction;
import java.util.stream.LongStream;

import org.junit.jupiter.api.function.Executable;
import org.mockito.internal.stubbing.answers.CallsRealMethods;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.model.api.members.MemberType;
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

	public static LongStream randomIndices() {
		return random().longs(RUNS, UNSET_LONG, Long.MAX_VALUE);
	}

	public static LongStream exhaustiveRandomIndices() {
		return random().longs(RUNS_EXHAUSTIVE, UNSET_LONG, Long.MAX_VALUE);
	}

	private static boolean equals(Item item, Object obj) {
		return obj instanceof Item && item.getId()==((Item)obj).getId();
	}

	@SuppressWarnings("boxing")
	public static <I extends Item> I stubId(I item, long id) {
		assertMock(item);
		String s = item.getMemberType()+"_"+id;
		when(item.getId()).thenReturn(id);
		when(item.toString()).thenReturn(s);
//		when(item.equals(any())).then(
//				invocation -> equals(item, invocation.getArgument(0)));
		return item;
	}

	public static Item mockItem() {
		return stubType(mock(Item.class), MemberType.ITEM);
	}

	public static Item mockItem(Container host) {
		return stubHost(mockItem(), host);
	}

	private static Item stubHost(Item item, Container container) {
		when(item.getContainer()).thenReturn(container);
		return item;
	}

	private static <I extends Item> I stubType(I item, MemberType type) {
		assertMock(item);
		when(item.getMemberType()).thenReturn(type);
		return item;
	}

	public static Edge mockEdge() {
		return stubHost(stubType(mock(Edge.class), MemberType.EDGE), STRUCTURE);
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
	public static <C extends Container> C stubItemCount(C container,
			long itemCount) {
		checkArgument(itemCount>=0);
		assertMock(container);

		when(container.getItemCount()).thenReturn(itemCount);

		return container;
	}

	public static Container mockContainer() {
		return mock(Container.class);
	}

	public static Container mockContainer(long itemCount) {
		Container container = mock(Container.class);

		stubItemCount(container, itemCount);

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

//		when(container.indexOfItem(any())).thenReturn(-1L);
//		when(container.getItemAt(anyLong())).thenThrow(IndexOutOfBoundsException.class);
//
//		for(int i=0; i<container.getItemCount(); i++) {
//			Item item = stubId(mockItem(container), i);
//			when(container.indexOfItem(item)).thenReturn((long)i);
//			when(container.getItemAt(i)).thenReturn(item);
//		}

		final Long2ObjectMap<Item> items = new Long2ObjectOpenHashMap<>();
		final Object2LongMap<Item> indices = new Object2LongOpenHashMap<>();
		indices.defaultReturnValue(-1L);

		when(container.getItemAt(anyLong())).then(invocation -> {
			long index = invocation.getArgument(0);
			checkItemIndex(container, index);

			return items.computeIfAbsent(index, k -> {
				Item item = stubId(mockItem(), k);
				indices.put(item, k);
				return item;
			});
		});

		when(container.indexOfItem(any())).then(invocation -> {
			Item item = invocation.getArgument(0);

			long result = indices.getLong(item);

			if(result==-1L) {
				for(int i=0; i<(int)container.getItemCount(); i++) {
					if(container.getItemAt(i)==item) {
						return (long)i;
					}
				}
			}

			return result;
		});
	}

	public static Structure mockStructure() {
		Structure structure = mock(Structure.class, withSettings().defaultAnswer(new CallsRealMethods()));

		Item root = mockItem(structure);
		when(structure.getVirtualRoot()).thenReturn(root);

		return structure;
	}

	@SuppressWarnings("boxing")
	public static <S extends Structure> S stubEdgeCount(S structure,
			long edgeCount) {
		checkArgument(edgeCount>=0);
		assertMock(structure);

		when(structure.getEdgeCount()).thenReturn(edgeCount);

		return structure;
	}

	public static Structure mockStructure(long itemCount, long edgeCount) {
		Structure structure = mockStructure();
		stubItemCount(structure, itemCount);
		stubEdgeCount(structure, edgeCount);

		if(itemCount>0) {
			stubItems(structure);
		}

		return structure;
	}

	private static void checkEdgeIndex(Structure structure, long index) {
		if(index<0 || index>=structure.getEdgeCount())
			throw new IndexOutOfBoundsException();
	}

	public static final int ROOT = -1;

	private static Item itemAt(Structure structure, long index) {
		return (int)index==ROOT ? structure.getVirtualRoot() : structure.getItemAt(index);
	}

	@SuppressWarnings("boxing")
	public static <S extends Structure, N extends Number> S stubEdges(S structure,
			@SuppressWarnings("unchecked") Pair<N, N>...edges) {

		when(structure.indexOfEdge(any())).thenReturn(Long.valueOf(-1));

		for(int i=0; i<edges.length; i++) {
			Pair<N,N> entry = edges[i];
			Edge edge = mockEdge(structure,
					itemAt(structure, entry.first.longValue()),
					itemAt(structure, entry.second.longValue()));

			when(structure.getEdgeAt(i)).thenReturn(edge);
			when(structure.indexOfEdge(edge)).thenReturn(Long.valueOf(i));
		}

		return structure;
	}

	public static <N extends Number> Structure mockStructure(long itemCount,
			@SuppressWarnings("unchecked") Pair<N, N>...edges) {

		Structure structure = mockStructure(itemCount, edges.length);

		stubEdges(structure, edges);

		return structure;
	}

	public static Structure mockStructure(
			long itemCount, long edgeCount,
			BiFunction<Structure, Long, Edge> edgeCreator) {

		Structure structure = mockStructure(itemCount, edgeCount);

		stubEdges(structure, edgeCreator);

		return structure;
	}

	//TODO doc
	@SuppressWarnings("boxing")
	public static final BiFunction<Structure, Long, Edge> DEFAULT_MAKE_EDGE =
			(structure, index) -> {
				long idx = index.longValue();
				Item target = structure.getItemAt(idx);
				Item source = idx==ROOT ? structure.getVirtualRoot()
						: structure.getItemAt(idx);

				return stubId(mockEdge(structure, source, target), index);
			};

	@SuppressWarnings("boxing")
	public static <S extends Structure> S stubEdges(S structure,
			BiFunction<Structure, Long, Edge> edgeCreator) {

		final Long2ObjectMap<Edge> edges = new Long2ObjectOpenHashMap<>();
		final Object2LongMap<Edge> indices = new Object2LongOpenHashMap<>();
		indices.defaultReturnValue(-1L);

		when(structure.getEdgeAt(anyLong())).then(invocation -> {
			long index = invocation.getArgument(0);
			checkEdgeIndex(structure, index);

			return edges.computeIfAbsent(index, k -> {
				Edge edge = edgeCreator.apply(structure, index);
				indices.put(edge, k);
				return edge;
			});
		});

		when(structure.indexOfEdge(any())).then(invocation -> {
			return indices.getLong(invocation.getArgument(0));
		});

		return structure;
	}

	public static <S extends Structure> S stubDefaultLazyEdges(S structure) {
		return stubEdges(structure, DEFAULT_MAKE_EDGE);
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

	public static void assertModelException(ErrorCode errorCode, Executable executable) {
		ModelException exception = assertThrows(ModelException.class, executable);
		assertEquals(errorCode, exception.getErrorCode());
	}
}
