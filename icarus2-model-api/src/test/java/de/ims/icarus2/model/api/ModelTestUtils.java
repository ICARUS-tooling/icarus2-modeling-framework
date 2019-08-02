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
import static de.ims.icarus2.test.TestUtils.randomBytes;
import static de.ims.icarus2.test.TestUtils.randomInts;
import static de.ims.icarus2.test.TestUtils.randomLongs;
import static de.ims.icarus2.test.TestUtils.randomShorts;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.PrimitiveIterator.OfLong;
import java.util.function.BiFunction;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;

import org.junit.jupiter.api.function.Executable;
import org.mockito.internal.stubbing.answers.CallsRealMethods;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.ArrayIndexSet;
import de.ims.icarus2.model.api.driver.indices.standard.VirtualIndexSet;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.raster.Metric;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.raster.RasterAxis;
import de.ims.icarus2.model.api.raster.Rasterizer;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.collections.seq.DataSequence;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class ModelTestUtils {

	/**
	 * Produces a stream of {@value TestUtils#RUNS} random {@code long} values.
	 * @return
	 */
	public static LongStream randomIndices() {
		return random().longs(RUNS, 0L, Long.MAX_VALUE);
	}

	public static LongStream exhaustiveRandomIndices() {
		return random().longs(RUNS_EXHAUSTIVE, 0L, Long.MAX_VALUE);
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

	@SuppressWarnings("boxing")
	public static <I extends Item> I stubIndex(I item, long index) {
		assertMock(item);
		when(item.getIndex()).thenReturn(index);
		return item;
	}

	@SuppressWarnings("boxing")
	public static <I extends Item> I stubFlags(I item, boolean alive, boolean dirty, boolean locked) {
		when(item.isAlive()).thenReturn(alive);
		when(item.isDirty()).thenReturn(dirty);
		when(item.isLocked()).thenReturn(locked);
		return item;
	}

	@SuppressWarnings("boxing")
	public static Item stubOffsets(Item item, long beginOffset, long endOffset) {
		when(item.getBeginOffset()).thenReturn(beginOffset);
		when(item.getEndOffset()).thenReturn(endOffset);
		return item;
	}

	public static Item mockItem() {
		return stubType(mock(Item.class), MemberType.ITEM);
	}

	public static Item mockUsableItem() {
		return stubFlags(stubType(mock(Item.class), MemberType.ITEM), true, true, true);
	}

	public static Item mockItem(Container host) {
		return stubHost(mockItem(), host);
	}

	public static Item[] mockItems(int count) {
		Item[] items = new Item[count];
		while (--count >= 0) {
			items[count] = mockItem();
		}
		return items;
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

	public static Edge mockUsableEdge() {
		return stubFlags(mockEdge(), true, true, true);
	}

	public static Edge mockEdge(Structure structure) {
		return stubHost(mock(Edge.class), structure);
	}

	public static Edge stubHost(Edge edge, Structure structure) {
		when(edge.getStructure()).thenReturn(structure);
		return edge;
	}

	public static Edge mockEdge(Item source, Item target) {
		if(source==null && target==null)
			throw new AssertionError("One of source or target has to be non-null");

		Edge edge = mockEdge();
		when(edge.getSource()).thenReturn(source);
		when(edge.getTarget()).thenReturn(target);

		return edge;
	}

	public static Edge mockEdge(Structure structure, Item source, Item target) {
		return stubHost(mockEdge(source, target), structure);
	}

	public static Position mockPosition(long...values) {
		Position position = mock(Position.class);
		stubValues(position, values);
		return position;
	}

	public static Fragment mockFragment() {
		return mock(Fragment.class);
	}

	public static Fragment mockUsableFragment() {
		return stubFlags(mockFragment(), true, true, true);
	}

	@SuppressWarnings("boxing")
	public static Rasterizer mockRasterizer(int axisCount) {
		Rasterizer rasterizer = mock(Rasterizer.class);

		when(rasterizer.getAxisCount()).thenReturn(axisCount);

		for (int i = 0; i < axisCount; i++) {
			RasterAxis axis = mock(RasterAxis.class);
			when(rasterizer.getRasterAxisAt(eq(i))).thenReturn(axis);
		}

		Metric<Position> metric = mock(Metric.class);
		when(rasterizer.getMetric()).thenReturn(metric);

		return rasterizer;
	}

	@SuppressWarnings("boxing")
	public static Rasterizer stubOrder(Rasterizer rasterizer, Position pos1, Position pos2) {
		Metric<Position> metric = assertMock(rasterizer.getMetric());

		when(metric.compare(pos1, pos2)).thenReturn(-1);

		return rasterizer;
	}

	@SuppressWarnings("boxing")
	public static Position stubValues(Position position, long...values) {
		when(position.getDimensionality()).thenReturn(values.length);
		for (int i = 0; i < values.length; i++) {
			when(position.getValue(eq(i))).thenReturn(values[i]);
		}
		return position;
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

	public static Container mockUsableContainer() {
		return stubFlags(mock(Container.class), true, true, true);
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

	public static Structure mockUsableStructure() {
		return stubFlags(mockStructure(), true, true, true);
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

	public static void assertUnsupportedOperation(Executable executable) {
		assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION, executable);
	}

	public static IndexSet sortedIndices(int size, long start) {
		return new VirtualIndexSet(start, size, IndexValueType.forValue(start+size),
				(offset, i) -> offset+i, true);
	}

	public static Object randomArray(IndexValueType indexValueType, int size) {
		switch (indexValueType) {
		case BYTE: return randomBytes(size, (byte)0, (byte)indexValueType.maxValue());
		case SHORT: return randomShorts(size, (short)0, (short)indexValueType.maxValue());
		case INTEGER: return randomInts(size, 0, (int)indexValueType.maxValue());
		case LONG: return randomLongs(size, 0L, indexValueType.maxValue());

		default:
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Unknown index value type: "+indexValueType);
		}
	}

	public static IndexSet randomIndices(IndexValueType indexValueType, int size) {
		return new ArrayIndexSet(indexValueType, randomArray(indexValueType, size));
	}

	public static void assertIndicesEqualsExact(IndexSet expected, IndexSet actual) {
		assertEquals(expected.size(), actual.size(), "Size mismatch");

		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.indexAt(i), actual.indexAt(i), "Mismatch at index "+i);
		}
	}

	private static final IndexValueType defaultType = IndexValueType.LONG;

	private static IndexSet basicSet() {
		return mock(IndexSet.class, CALLS_REAL_METHODS);
	}

	@SuppressWarnings("boxing")
	public static IndexSet set(IndexValueType valueType, long...indices) {
		IndexSet set = basicSet();

		when(set.size()).thenReturn(indices.length);
		when(set.getIndexValueType()).thenReturn(valueType);
		when(set.indexAt(anyInt())).thenAnswer(
				inv -> Long.valueOf(indices[((Integer)inv.getArgument(0)).intValue()]));

		return set;
	}

	public static IndexSet set(long...indices) {
		return set(defaultType, indices);
	}

	@SuppressWarnings("boxing")
	public static IndexSet sorted(IndexValueType valueType, long...indices) {
		IndexSet set = set(valueType, indices);
		when(set.isSorted()).thenReturn(Boolean.TRUE);

		return set;
	}

	public static IndexSet sorted(long...indices) {
		return sorted(defaultType, indices);
	}

	@SuppressWarnings("boxing")
	public static IndexSet range(IndexValueType valueType, long from, long to) {
		assumeTrue(from>=0L);
		assumeTrue(to>=from);
		IndexSet set = basicSet();

		when(set.size()).thenReturn(Math.toIntExact(to-from+1));
		when(set.isSorted()).thenReturn(Boolean.TRUE);
		when(set.getIndexValueType()).thenReturn(valueType);
		when(set.indexAt(anyInt())).thenAnswer(inv -> {
			int index = ((Integer)inv.getArgument(0)).intValue();
			long result = from+index;
			if(result>to)
				throw new IndexOutOfBoundsException();
			return Long.valueOf(result);
		});

		return set;
	}

	public static IndexSet range(long from, long to) {
		return range(defaultType, from, to);
	}

	@SuppressWarnings("boxing")
	public static IndexSet[] mockIndices(int...sizes) {
		IndexSet[] sets = new IndexSet[sizes.length];
		for (int i = 0; i < sets.length; i++) {
			sets[i] = basicSet();
			when(sets[i].size()).thenReturn(sizes[i]);
		}
		return sets;
	}

	public static LongSet asSet(IndexSet source) {
		LongSet set = new LongOpenHashSet(source.size());
		source.forEachIndex((LongConsumer)set::add);
		return set;
	}

	public static void assertIndicesEquals(IndexSet expected, IndexSet actual) {
		assertEquals(expected.size(), actual.size(), "Size mismatch");

		LongSet setExp = asSet(expected);
		LongSet setAct = asSet(actual);

		setExp.removeAll(setAct);
		assertTrue(setExp.isEmpty(), "Total leftover indices: "+setExp.size());
	}

	public static void assertIndicesEquals(IndexSet[] expected, IndexSet[] actual) {
		assertEquals(IndexUtils.count(expected),
				IndexUtils.count(actual), "Size mismatch");

		OfLong itExp = IndexUtils.asIterator(expected);
		OfLong itAct = IndexUtils.asIterator(actual);

		int idx = 0;
		while(itExp.hasNext() && itAct.hasNext()) {
			assertEquals(itExp.nextLong(), itAct.nextLong(), "Mismatch at index "+idx);
			idx++;
		}

		assertFalse(itExp.hasNext());
		assertFalse(itAct.hasNext());
	}

	public static <T extends ModelException> Executable meAsserter(
			ErrorCode code, Executable executable) {
		return () -> assertModelException(code, executable);
	}
}
