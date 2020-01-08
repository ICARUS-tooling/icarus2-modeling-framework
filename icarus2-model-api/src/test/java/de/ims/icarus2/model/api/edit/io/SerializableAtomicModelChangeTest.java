/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.edit.io;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdges;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItems;
import static de.ims.icarus2.model.api.ModelTestUtils.mockPosition;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.getTestValues;
import static de.ims.icarus2.test.TestUtils.abort;
import static de.ims.icarus2.test.TestUtils.assertCollectionNotEmpty;
import static de.ims.icarus2.test.util.Pair.nullablePair;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.util.collections.ArrayUtils.swap;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.lang.Primitives._boolean;
import static de.ims.icarus2.util.lang.Primitives._double;
import static de.ims.icarus2.util.lang.Primitives._float;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.stubbing.Answer;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.edit.change.AtomicValueChange;
import de.ims.icarus2.model.api.edit.io.SerializableAtomicModelChange.BooleanValueChange;
import de.ims.icarus2.model.api.edit.io.SerializableAtomicModelChange.DoubleValueChange;
import de.ims.icarus2.model.api.edit.io.SerializableAtomicModelChange.EdgeChange;
import de.ims.icarus2.model.api.edit.io.SerializableAtomicModelChange.EdgeMoveChange;
import de.ims.icarus2.model.api.edit.io.SerializableAtomicModelChange.EdgeSequenceChange;
import de.ims.icarus2.model.api.edit.io.SerializableAtomicModelChange.FloatValueChange;
import de.ims.icarus2.model.api.edit.io.SerializableAtomicModelChange.IntegerValueChange;
import de.ims.icarus2.model.api.edit.io.SerializableAtomicModelChange.ItemChange;
import de.ims.icarus2.model.api.edit.io.SerializableAtomicModelChange.ItemMoveChange;
import de.ims.icarus2.model.api.edit.io.SerializableAtomicModelChange.ItemSequenceChange;
import de.ims.icarus2.model.api.edit.io.SerializableAtomicModelChange.LongValueChange;
import de.ims.icarus2.model.api.edit.io.SerializableAtomicModelChange.PositionChange;
import de.ims.icarus2.model.api.edit.io.SerializableAtomicModelChange.TerminalChange;
import de.ims.icarus2.model.api.edit.io.SerializableAtomicModelChange.ValueChange;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.random.Randomized;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.Mutable.MutableObject;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.collections.seq.DataSequence;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
class SerializableAtomicModelChangeTest {

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <C> type of the change to be tested
	 * @param <B> buffer data holding the state of whatever the change affects
	 */
	interface ModelChangeTest<C extends SerializableAtomicChange, B> extends ApiGuardedTest<C> {

		/** Create a bunch of usable data instances */
		Stream<Pair<String, B>> createData();
		/** Create a copy of current source state so we can verify it later */
		B cloneData(B source);
		/** Make sure given data matches the expected state */
		boolean dataEquals(B expected, B actual);

		/** Create a collection of individually testable changes for source */
		Stream<Pair<String, Function<B, C>>> createIndividualChanges(B origin);

		/** Create corrupted modifications to the given data to test against failure */
		Stream<Pair<String, BiConsumer<B, C>>> corruptData();

		/** Create legal mutations of the data that will still allow the change to apply */
		Stream<Pair<String, BiConsumer<B, C>>> mutateData();

		/** For some source data create testable change sequences */
		List<Pair<String, C>> createBulkChanges(B source);

		@Test
		default void testGetChangeType() {
			assertNotNull(create().getType());
		}

		/** For each data run a single change in isolation */
		@TestFactory
		default Stream<DynamicNode> testIsolated() {
			return createData()
					.map(pDat -> dynamicContainer("data: "+pDat.first, createIndividualChanges(pDat.second)
					.map(pCha -> dynamicTest("change: "+pCha.first, () -> {
						B original = pDat.second;
						B data = cloneData(original);
						assertNotSame(original, data);

						C change = pCha.second.apply(data);

						change.execute();
						assertFalse(dataEquals(original, data));

						assertTrue(change.canReverse());

						change.execute();
						assertTrue(dataEquals(original, data));
					}))));
		}

		/** For each data run a sequence of changes */
		@TestFactory
		default Stream<DynamicNode> testBulk() {
			return createData().map(p -> dynamicTest("data: "+p.first, () -> {
				B source = p.second;
				List<Pair<String,C>> changes = createBulkChanges(source);
				assertCollectionNotEmpty(changes);
				int count = changes.size();

				/** Lists the states before and after each change */
				List<B> states = new ArrayList<>(count+1);
				states.add(cloneData(source));

				// Apply changes once
				for (int i = 0; i < count; i++) {
					C change = changes.get(i).second;
					B before = states.get(i);
					B after = source;
					assertNotSame(before, after);
					change.execute();
					states.add(cloneData(after));
					assertFalse(dataEquals(before, after),
							i+" Nothing changed: "+changes.get(i).first);
				}

				Collections.reverse(changes);
				Collections.reverse(states);


				// Now reverse changes
				for(int i=0; i<count; i++) {
					C change = changes.get(i).second;
					assertTrue(change.canReverse(),
							i+" Irreversible change: "+changes.get(i).first);
					assertTrue(dataEquals(states.get(i), source));
					B before = source;
					B after = states.get(i+1);
					assertNotSame(before, after);
					change.execute();
					assertTrue(dataEquals(before, after),
							"Unexpected change: "+changes.get(i).first);
				}
			}));
		}

		/** For each data create a change and serialize it*/
		@TestFactory
		default Stream<DynamicNode> testSerializationIsolated() {
			return createData()
					.map(pDat -> dynamicContainer("data: "+pDat.first, createIndividualChanges(pDat.second)
					.map(pCha -> dynamicTest("change: "+pCha.first, () -> {
						B original = pDat.second;
						B data = cloneData(original);
						assertNotSame(original, data);

						C change = pCha.second.apply(data);

						// Serialize the change
						ChangeBuffer buffer = new ChangeBuffer();
						buffer.writeChange(change);

						// Assert that _something_ was written
						assertFalse(buffer.isEmpty());
					}))));
		}

		/** For each data create a change serialize it and then deserialize and compare */
		@TestFactory
		default Stream<DynamicNode> testDeserializationIsolated() {
			return createData()
					.map(pDat -> dynamicContainer("data: "+pDat.first, createIndividualChanges(pDat.second)
					.map(pCha -> dynamicTest("change: "+pCha.first, () -> {
						B original = pDat.second;
						B data = cloneData(original);
						assertNotSame(original, data);

						C change = pCha.second.apply(data);

						// Serialize the change
						ChangeBuffer buffer = new ChangeBuffer();
						buffer.writeChange(change);

						// Assert deserialization result
						assertEquals(change, buffer.readChange());
					}))));
		}

		/** For each data run a sequence of changes */
		@SuppressWarnings("unchecked")
		@TestFactory
		default Stream<DynamicNode> testBulkSerializationCycle() {
			return createData().map(p -> dynamicTest("data: "+p.first, () -> {
				B source = p.second;
				List<Pair<String,C>> changes = createBulkChanges(source);
				assertCollectionNotEmpty(changes);
				int count = changes.size();

				// Lists the states before and after each change
				List<B> states = new ArrayList<>(count+1);
				states.add(cloneData(source));

				ChangeBuffer buffer = new ChangeBuffer();

				// Apply changes once
				for (int i = 0; i < count; i++) {
					C change = changes.get(i).second;
					B before = states.get(i);
					B after = source;
					assertNotSame(before, after);
					change.execute();
					states.add(cloneData(after));
					assertFalse(dataEquals(before, after),
							i+" Nothing changed: "+changes.get(i).first);

					buffer.writeChange(change);
				}


				List<C> changes2 = new ArrayList<>(count);
				for (int i = 0; i < count; i++) {
					C change = (C) buffer.readChange();
					assertEquals(changes.get(i).second, change);
					changes2.add(change);
				}

				Collections.reverse(states);
				Collections.reverse(changes);
				Collections.reverse(changes2);

				// Now reverse changes
				for(int i=0; i<count; i++) {
					C change = changes2.get(i);
					assertTrue(dataEquals(states.get(i), source));
					B before = source;
					B after = states.get(i+1);
					assertNotSame(before, after);
					change.execute();
					assertTrue(dataEquals(before, after));
				}
			}));
		}

		/** For each data, create corrupted instances and then try to run a single change */
		@TestFactory
		default Stream<DynamicNode> testCorruptedIsolated() {
			return createData()
					.map(pDat -> dynamicContainer("data: "+pDat.first, createIndividualChanges(pDat.second)
					.map(pCha -> dynamicContainer("change: "+pCha.first, corruptData()
					.map(pCor -> dynamicTest("corruption: "+pCor.first, () -> {
						// Make a copy of original data, as we're gonna change it
						B data = cloneData(pDat.second);

						C change = pCha.second.apply(data);

						B data2 = cloneData(data);
						// Apply corruption
						pCor.second.accept(data, change);
						assertFalse(dataEquals(data, data2));

						// Try to invoke change on the corrupted data
						assertModelException(ModelErrorCode.MODEL_CORRUPTED_EDIT,
								change::execute);
					}))))));
		}

		/** For each data, create corrupted instances and then try to reverse a single change */
		@TestFactory
		default Stream<DynamicNode> testCorruptedReverseIsolated() {
			return createData()
					.map(pDat -> dynamicContainer("data: "+pDat.first, createIndividualChanges(pDat.second)
					.map(pCha -> dynamicContainer("change: "+pCha.first, corruptData()
					.map(pCor -> dynamicTest("corruption: "+pCor.first, () -> {
						// Make a copy of original data, as we're gonna change it
						B data = cloneData(pDat.second);

						C change = pCha.second.apply(data);
						// Invoke change on clean data
						change.execute();

						B data2 = cloneData(data);
						// Apply corruption
						pCor.second.accept(data, change);
						assertFalse(dataEquals(data, data2));

						// Try to reverse change on the corrupted data
						assertModelException(ModelErrorCode.MODEL_CORRUPTED_EDIT,
								change::execute);
					}))))));
		}

		/** For each data, create mutated instances and then verify successful changes */
		@TestFactory
		default Stream<DynamicNode> testMutatedIsolated() {
			return createData()
					.map(pDat -> dynamicContainer("data: "+pDat.first, createIndividualChanges(pDat.second)
					.map(pCha -> dynamicContainer("change: "+pCha.first, mutateData()
					.map(pMut -> dynamicTest("mutation: "+pMut.first, () -> {
						// Make a copy of original data, as we're gonna change it
						B data = cloneData(pDat.second);

						C change = pCha.second.apply(data);

						B data2 = cloneData(data);
						// Apply mutation
						pMut.second.accept(data, change);
						assertFalse(dataEquals(data, data2));

						// Check that the mutation won't cause the change to fail
						change.execute();
					}))))));
		}

		/** For each data, create mutated instances and then verify successful reversals */
		@TestFactory
		default Stream<DynamicNode> testMutatedReversedIsolated() {
			return createData()
					.map(pDat -> dynamicContainer("data: "+pDat.first, createIndividualChanges(pDat.second)
					.map(pCha -> dynamicContainer("change: "+pCha.first, mutateData()
					.map(pMut -> dynamicTest("mutation: "+pMut.first, () -> {
						// Make a copy of original data, as we're gonna change it
						B data = cloneData(pDat.second);

						C change = pCha.second.apply(data);
						// Invoke change on clean data
						change.execute();

						B data2 = cloneData(data);
						// Apply mutation
						pMut.second.accept(data, change);
						assertFalse(dataEquals(data, data2));

						// Check that the mutation won't cause the reversion to fail
						change.execute();
					}))))));
		}
	}

	// Use this to "disable" all the methods we haven't stubbed
	private static final Answer<?> UNSUPPORTED = invoc -> {
		throw new UnsupportedOperationException();
	};


	@ExtendWith(Randomized.class)
	abstract class TestBase {
		RandomGenerator rand;

		String randomKey() {
			return rand.randomString(10);
		}

		String randomKey2() {
			return rand.randomString(12);
		}

		int randomSize() {
			return rand.random(10, 30);
		}
	}

	abstract class ContainerContent extends TestBase {

		public Stream<Pair<String, Container>> createData() {
			return Stream.of(
				pair("empty", mockContainer(0)),
				pair("singleton", mockContainer(1)),
				pair("dual", mockContainer(2)),
				pair("random", mockContainer(randomSize()))
			);
		}

		public Container cloneData(Container source) {
			return mockContainer(items(source));
		}

		public boolean dataEquals(Container expected, Container actual) {
			if(expected.getItemCount()!=actual.getItemCount()) {
				return false;
			}
			for (int i = 0; i < expected.getItemCount(); i++) {
				if(expected.getItemAt(i)!=actual.getItemAt(i)) {
					return false;
				}
			}
			return true;
		}
	}

	private static Item[] items(Container source) {
		Item[] items = new Item[strictToInt(source.getItemCount())];
		for (int i = 0; i < items.length; i++) {
			items[i] = source.getItemAt(i);
		}
		return items;
	}

	private static Container mockContainer(int size) {

		Container container = mock(Container.class, UNSUPPORTED);
		stubContainerMethods(container, mockItems(size));

		return container;
	}
	private static Container mockContainer(Item...items) {

		Container container = mock(Container.class, UNSUPPORTED);
		stubContainerMethods(container, items);

		return container;
	}

	private static void stubContainerMethods(Container mock, Item[] items) {
		List<Item> buffer = new ArrayList<>();
		Collections.addAll(buffer, items);

		// Size
		doAnswer(invoc -> _long(buffer.size())).when(mock).getItemCount();
		// Add single
		doAnswer(invoc -> {
			buffer.add(((Number)invoc.getArgument(0)).intValue(), invoc.getArgument(1));
			return null;
		}).when(mock).addItem(anyLong(), any());
		// Remove single
		doAnswer(invoc -> buffer.remove(((Number)invoc.getArgument(0)).intValue())
		).when(mock).removeItem(anyLong());
		// Get individual items
		doAnswer(invoc -> buffer.get(((Number)invoc.getArgument(0)).intValue())
		).when(mock).getItemAt(anyLong());
		// Swap items
		doAnswer(invoc -> {
			int index0 = ((Number)invoc.getArgument(0)).intValue();
			int index1 = ((Number)invoc.getArgument(1)).intValue();
			Item item0 = buffer.get(index0);
			Item item1 = buffer.get(index1);
			assertSame(item0, buffer.set(index0, item1));
			assertSame(item1, buffer.set(index1, item0));
			return null;
		}).when(mock).swapItems(anyLong(), anyLong());
		// Add bulk
		doAnswer(invoc -> {
			int index = ((Number)invoc.getArgument(0)).intValue();
			DataSequence<? extends Item> seq = invoc.getArgument(1);
			buffer.addAll(index, seq.getEntries());
			return null;
		}).when(mock).addItems(anyLong(), any());
		// Remove bulk
		doAnswer(invoc -> {
			int index0 = ((Number)invoc.getArgument(0)).intValue();
			int index1 = ((Number)invoc.getArgument(1)).intValue();
			List<Item> tmp = buffer.subList(index0, index1+1);
			DataSequence<Item> seq = mockSequence(tmp.toArray(new Item[0]));
			tmp.clear();
			return seq;
		}).when(mock).removeItems(anyLong(), anyLong());

		//TODO
	}

	abstract class StructureContent extends TestBase {

		public Stream<Pair<String, Structure>> createData() {
			return Stream.of(
				pair("empty", mockStructure(0)),
				pair("singleton", mockStructure(1)),
				pair("dual", mockStructure(2)),
				pair("random", mockStructure(randomSize()))
			);
		}

		public Structure cloneData(Structure source) {
			return mockStructure(edges(source));
		}

		public boolean dataEquals(Structure expected, Structure actual) {
			if(expected.getEdgeCount()!=actual.getEdgeCount()) {
				return false;
			}
			for (int i = 0; i < expected.getEdgeCount(); i++) {
				if(expected.getEdgeAt(i)!=actual.getEdgeAt(i)) {
					return false;
				}
			}
			return true;
		}
	}

	private static Edge[] edges(Structure source) {
		Edge[] edges = new Edge[strictToInt(source.getEdgeCount())];
		for (int i = 0; i < edges.length; i++) {
			edges[i] = source.getEdgeAt(i);
		}
		return edges;
	}

	private static Structure mockStructure(int size) {

		Structure container = mock(Structure.class, UNSUPPORTED);
		stubStructureMethods(container, mockEdges(size));

		return container;
	}

	private static Structure mockStructure(Edge...edges) {

		Structure structure = mock(Structure.class, UNSUPPORTED);
		stubStructureMethods(structure, edges);

		return structure;
	}

	private static void stubStructureMethods(Structure mock, Edge...edges) {
		List<Edge> buffer = new ArrayList<>();
		Collections.addAll(buffer, edges);

		// Size
		doAnswer(invoc -> _long(buffer.size())).when(mock).getEdgeCount();
		// Add single
		doAnswer(invoc -> {
			buffer.add(((Number)invoc.getArgument(0)).intValue(), invoc.getArgument(1));
			return null;
		}).when(mock).addEdge(anyLong(), any());
		// Remove single
		doAnswer(invoc -> buffer.remove(((Number)invoc.getArgument(0)).intValue())
		).when(mock).removeEdge(anyLong());
		// Get individual items
		doAnswer(invoc -> buffer.get(((Number)invoc.getArgument(0)).intValue())
		).when(mock).getEdgeAt(anyLong());
		// Swap items
		doAnswer(invoc -> {
			int index0 = ((Number)invoc.getArgument(0)).intValue();
			int index1 = ((Number)invoc.getArgument(1)).intValue();
			Edge edge0 = buffer.get(index0);
			Edge edge1 = buffer.get(index1);
			assertSame(edge0, buffer.set(index0, edge1));
			assertSame(edge1, buffer.set(index1, edge0));
			return null;
		}).when(mock).swapEdges(anyLong(), anyLong());
		// Add bulk
		doAnswer(invoc -> {
			int index = ((Number)invoc.getArgument(0)).intValue();
			DataSequence<? extends Edge> seq = invoc.getArgument(1);
			buffer.addAll(index, seq.getEntries());
			return null;
		}).when(mock).addEdges(anyLong(), any());
		// Remove bulk
		doAnswer(invoc -> {
			int index0 = ((Number)invoc.getArgument(0)).intValue();
			int index1 = ((Number)invoc.getArgument(1)).intValue();
			List<Edge> tmp = buffer.subList(index0, index1+1);
			DataSequence<Edge> seq = mockSequence(tmp.toArray(new Edge[0]));
			tmp.clear();
			return seq;
		}).when(mock).removeEdges(anyLong(), anyLong());
		// Terminal
		doAnswer(invoc -> {
			Edge edge = invoc.getArgument(0);
			edge.setTerminal(invoc.getArgument(1), ((Boolean)invoc.getArgument(2)).booleanValue());
			return null;
		}).when(mock).setTerminal(any(), any(), anyBoolean());

		//TODO
	}

	@Nested
	class ItemChangeTest extends ContainerContent implements ModelChangeTest<ItemChange, Container> {

		@Override
		public Class<?> getTestTargetClass() {
			return ItemChange.class;
		}

		@Override
		public ItemChange createTestInstance(TestSettings settings) {
			int size = randomSize();
			Container container = mockContainer(size);
			return settings.process(new ItemChange(container, mockItem(), size, 0, true));
		}

		@Override
		public Stream<Pair<String, Function<Container, ItemChange>>> createIndividualChanges(Container origin) {
			return Stream.of(
				pair("add", container -> {
					long index = container.getItemCount();
					if(index>0) index = rand.random(0, index);
					return new ItemChange(container, mockItem(),
							container.getItemCount(), index, true);
				}),
				pair("remove", container -> {
					long size = container.getItemCount();
					if(size==0L) abort("Container already empty");
					long index = rand.random(0, size);
					return new ItemChange(container, container.getItemAt(index),
							container.getItemCount(), index, false);
				})
			);
		}

		@Override
		public List<Pair<String, ItemChange>> createBulkChanges(Container source) {
			List<Pair<String, ItemChange>> changes = new ArrayList<>();
			List<Item> dummy = list(items(source));
			for (int i = 0; i < 10; i++) {
				if(dummy.isEmpty() || rand.nextBoolean()) {
					// Add
					int index = rand.random(0, dummy.size()+1);
					Item item = mockItem();
					changes.add(pair("add "+index, new ItemChange(
							source, item, dummy.size(), index, true)));
					dummy.add(index, item);
				} else {
					// Remove
					int index = rand.random(0, dummy.size());
					Item item = dummy.remove(index);
					changes.add(pair("remove "+index, new ItemChange(
							source, item, dummy.size()+1, index, false)));
				}
			}
			return changes;
		}

		@Override
		public Stream<Pair<String, BiConsumer<Container, ItemChange>>> corruptData() {
			return Stream.of(
					pair("item added", (container, change) -> {
						long index = container.getItemCount();
						if(index>0) index = rand.random(0, index);
						container.addItem(index, mockItem());
					}),
					pair("item removed", (container, change) -> {
						if(container.getItemCount()<1) abort("Container already empty");
						container.removeItem(rand.random(0, container.getItemCount()));
					}),
					pair("emptied", (container, change) -> {
						if(container.getItemCount()<1) abort("Container already empty");
						container.removeItems(0, container.getItemCount()-1);
					}),
					pair("item moved", (container, change) -> {
						if(change.isAdd()) abort();
						if(container.getItemCount()<2) abort("Need 2 items for swapping");
						long index0 = change.getIndex();
						long index1;
						do {
							index1 = rand.random(0, container.getItemCount());
						} while(index1==index0);
						container.swapItems(index0, index1);
					})
			);
		}

		@Override
		public Stream<Pair<String, BiConsumer<Container, ItemChange>>> mutateData() {
			return Stream.of(
					pair("items moved", (container, change) -> {
						if(container.getItemCount()<3) abort("Need 2 other items");
						long size = container.getItemCount();
						long index = change.getIndex();
						long index0;
						long index1;
						do {
							index0 = rand.random(0, size);
							index1 = rand.random(0, size);
						} while(index1==index0 || index0==index || index1==index);
						container.swapItems(index0, index1);
					})
			);
		}
	}

	@Nested
	class ItemMoveTest extends ContainerContent implements ModelChangeTest<ItemMoveChange, Container> {

		@Override
		public Class<?> getTestTargetClass() {
			return ItemMoveChange.class;
		}

		@Override
		public ItemMoveChange createTestInstance(TestSettings settings) {
			Container container = mockContainer(randomSize());
			return settings.process(createIndividualChanges(container)
					.findFirst()
					.get()
					.second.apply(container));
		}

		@Override
		public Stream<Pair<String, Function<Container, ItemMoveChange>>> createIndividualChanges(Container origin) {
			return Stream.of(
				pair("move", container -> {
					int size = strictToInt(container.getItemCount());
					if(size<2) abort("Need 2 items for swapping");
					int index0 = rand.random(0, size);
					int index1;
					do {
						index1 = rand.random(0, size);
					} while(index1==index0);
					Item item0 = container.getItemAt(index0);
					Item item1 = container.getItemAt(index1);
					return new ItemMoveChange(container, size, index0, index1, item0, item1);
				})
			);
		}

		@Override
		public List<Pair<String, ItemMoveChange>> createBulkChanges(Container source) {
			int size = strictToInt(source.getItemCount());
			if(size<2) abort("Need 2 items for swapping");

			List<Pair<String, ItemMoveChange>> changes = new ArrayList<>();
			Item[] dummy = items(source);
			for (int i = 0; i < size; i++) {
				int index0 = rand.random(0, size);
				int index1;
				do {
					index1 = rand.random(0, size);
				} while(index1==index0);
				Item item0 = dummy[index0];
				Item item1 = dummy[index1];
				changes.add(pair(i+" move "+index0+"->"+index1, new ItemMoveChange(
						source, size, index0, index1, item0, item1)));
				swap(dummy, index0, index1);
			}
			return changes;
		}

		@Override
		public Stream<Pair<String, BiConsumer<Container, ItemMoveChange>>> corruptData() {
			return Stream.of(
					pair("item added", (container, change) -> {
						long index = container.getItemCount();
						if(index>0) index = rand.random(0, index);
						container.addItem(index, mockItem());
					}),
					pair("item removed", (container, change) -> {
						if(container.getItemCount()<1) abort("Container already empty");
						container.removeItem(rand.random(0, container.getItemCount()));
					}),
					pair("emptied", (container, change) -> {
						if(container.getItemCount()<1) abort("Container already empty");
						container.removeItems(0, container.getItemCount()-1);
					}),
					pair("item0 moved", (container, change) -> {
						if(container.getItemCount()<2) abort("Need 2 items for swapping");
						long index0 = change.getSourceIndex();
						long index1;
						do {
							index1 = rand.random(0, container.getItemCount());
						} while(index1==index0);
						container.swapItems(index0, index1);
					}),
					pair("item1 moved", (container, change) -> {
						if(container.getItemCount()<2) abort("Need 2 items for swapping");
						long index0 = change.getTargetIndex();
						long index1;
						do {
							index1 = rand.random(0, container.getItemCount());
						} while(index1==index0);
						container.swapItems(index0, index1);
					})
			);
		}

		@Override
		public Stream<Pair<String, BiConsumer<Container, ItemMoveChange>>> mutateData() {
			return Stream.of(
					pair("items moved", (container, change) -> {
						if(container.getItemCount()<4) abort("Need 2 disjoint pairs");
						long size = container.getItemCount();
						long indexS = change.getSourceIndex();
						long indexT = change.getTargetIndex();
						long index0;
						long index1;
						do {
							index0 = rand.random(0, size);
							index1 = rand.random(0, size);
						} while(index1==index0 || index0==indexS || index1==indexS
								|| index0==indexT || index1==indexT);
						container.swapItems(index0, index1);
					})
			);
		}
	}

	@Nested
	class ItemSequenceTest extends ContainerContent implements ModelChangeTest<ItemSequenceChange, Container> {

		@Override
		public Class<?> getTestTargetClass() {
			return ItemSequenceChange.class;
		}

		@Override
		public ItemSequenceChange createTestInstance(TestSettings settings) {
			int size = randomSize();
			return settings.process(new ItemSequenceChange(mockContainer(size),
					size, 0, mockSequence(mockItem())));
		}

		@Override
		public Stream<Pair<String, Function<Container, ItemSequenceChange>>> createIndividualChanges(Container origin) {
			return Stream.of(
				pair("add single", container -> {
					long index = container.getItemCount();
					if(index>0) index = rand.random(0, index);
					return new ItemSequenceChange(container, container.getItemCount(),
							index, mockSequence(mockItem()));
				}),
				pair("add bulk", container -> {
					long index = container.getItemCount();
					if(index>0) index = rand.random(0, index);
					return new ItemSequenceChange(container, container.getItemCount(),
							index, mockSequence(mockItems(randomSize())));
				}),
				pair("remove single", container -> {
					long size = container.getItemCount();
					if(size<1) abort("Container already empty");
					long index = rand.random(0, size);
					return new ItemSequenceChange(container, container.getItemCount(),
							index, index);
				}),
				pair("remove bulk", container -> {
					long size = container.getItemCount();
					if(size<2) abort("Need 2 items");
					long index0 = rand.random(0, size-1);
					long index1 = rand.random(index0+1, size);
					return new ItemSequenceChange(container, container.getItemCount(),
							index0, index1);
				})
			);
		}

		@Override
		public List<Pair<String, ItemSequenceChange>> createBulkChanges(Container source) {
			List<Pair<String, ItemSequenceChange>> changes = new ArrayList<>();
			List<Item> dummy = list(items(source));
			for (int i = 0; i < 10; i++) {
				if(dummy.isEmpty() || rand.nextBoolean()) {
					// Add
					int index = rand.random(0, dummy.size()+1);
					DataSequence<Item> items = mockSequence(mockItems(randomSize()));
					changes.add(pair("add "+index, new ItemSequenceChange(
							source, dummy.size(), index, items)));
					dummy.addAll(index, items.getEntries());
				} else {
					// Remove
					int index0 = rand.random(0, dummy.size());
					int index1 = rand.random(index0, dummy.size());
					List<Item> subList = dummy.subList(index0, index1+1);
					changes.add(pair("remove ["+index0+"-"+index1+"]", new ItemSequenceChange(
							source, dummy.size(), index0, index1)));
					subList.clear();
				}
			}
			return changes;
		}

		@Override
		public Stream<Pair<String, BiConsumer<Container, ItemSequenceChange>>> corruptData() {
			return Stream.of(
					pair("item added", (container, change) -> {
						long index = container.getItemCount();
						if(index>0) index = rand.random(0, index);
						container.addItem(index, mockItem());
					}),
					pair("item removed", (container, change) -> {
						if(container.getItemCount()<1) abort("Container already empty");
						container.removeItem(rand.random(0, container.getItemCount()));
					}),
					pair("emptied", (container, change) -> {
						if(container.getItemCount()<1) abort("Container already empty");
						container.removeItems(0, container.getItemCount()-1);
					})
					// Sequence changes do not verify individual item integrity!
			);
		}

		@Override
		public Stream<Pair<String, BiConsumer<Container, ItemSequenceChange>>> mutateData() {
			return Stream.of(
					// Move random items
					pair("items moved (random)", (container, change) -> {
						if(container.getItemCount()<4) abort("Need 2 disjoint pairs");
						long size = container.getItemCount();
						long index0;
						long index1;
						do {
							index0 = rand.random(0, size);
							index1 = rand.random(0, size);
						} while(index1==index0);
						container.swapItems(index0, index1);
					}),
					// Move random items
					pair("items moved (overlap)", (container, change) -> {
						if(change.isAdd()) abort("Cannot remove overlapping sequence before adding");
						if(container.getItemCount()<4) abort("Need 2 disjoint pairs");
						long size = container.getItemCount();
						long index0 = (change.getBeginIndex()+change.getEndIndex())/2;
						long index1;
						do {
							index1 = rand.random(0, size);
						} while(index1==index0);
						container.swapItems(index0, index1);
					})
			);
		}
	}

	@Nested
	class EdgeChangeTest extends StructureContent implements ModelChangeTest<EdgeChange, Structure> {

		@Override
		public Class<?> getTestTargetClass() {
			return EdgeChange.class;
		}

		@Override
		public EdgeChange createTestInstance(TestSettings settings) {
			Structure structure = mockStructure(randomSize());
			return settings.process(createIndividualChanges(structure)
					.findFirst()
					.get()
					.second.apply(structure));
		}

		@Override
		public Stream<Pair<String, Function<Structure, EdgeChange>>> createIndividualChanges(Structure origin) {
			return Stream.of(
				pair("add", structure -> {
					long index = structure.getEdgeCount();
					if(index>0) index = rand.random(0, index);
					return new EdgeChange(structure, mockEdge(),
							structure.getEdgeCount(), index, true);
				}),
				pair("remove", structure -> {
					long size = structure.getEdgeCount();
					if(size<1) abort("Structure already empty");
					long index = rand.random(0, size);
					return new EdgeChange(structure, structure.getEdgeAt(index),
							structure.getEdgeCount(), index, false);
				})
			);
		}

		@Override
		public List<Pair<String, EdgeChange>> createBulkChanges(Structure source) {
			List<Pair<String, EdgeChange>> changes = new ArrayList<>();
			List<Edge> dummy = list(edges(source));
			for (int i = 0; i < 10; i++) {
				if(dummy.isEmpty() || rand.nextBoolean()) {
					// Add
					int index = rand.random(0, dummy.size()+1);
					Edge edge = mockEdge();
					changes.add(pair("add "+index, new EdgeChange(
							source, edge, dummy.size(), index, true)));
					dummy.add(index, edge);
				} else {
					// Remove
					int index = rand.random(0, dummy.size());
					Edge edge = dummy.remove(index);
					changes.add(pair("remove "+index, new EdgeChange(
							source, edge, dummy.size()+1, index, false)));
				}
			}
			return changes;
		}

		@Override
		public Stream<Pair<String, BiConsumer<Structure, EdgeChange>>> corruptData() {
			return Stream.of(
					pair("edge added", (container, change) -> {
						long index = container.getEdgeCount();
						if(index>0) index = rand.random(0, index);
						container.addEdge(index, mockEdge());
					}),
					pair("edge removed", (structure, change) -> {
						if(structure.getEdgeCount()<1) abort("Structure already empty");
						structure.removeEdge(rand.random(0, structure.getEdgeCount()));
					}),
					pair("emptied", (structure, change) -> {
						if(structure.getEdgeCount()<1) abort("Structure already empty");
						structure.removeEdges(0, structure.getEdgeCount()-1);
					}),
					pair("edge moved", (structure, change) -> {
						if(change.isAdd()) abort();
						if(structure.getEdgeCount()<2) abort("Need 2 edges for swapping");
						long index0 = change.getIndex();
						long index1;
						do {
							index1 = rand.random(0, structure.getEdgeCount());
						} while(index1==index0);
						structure.swapEdges(index0, index1);
					})
			);
		}

		@Override
		public Stream<Pair<String, BiConsumer<Structure, EdgeChange>>> mutateData() {
			return Stream.of(
					pair("edges moved", (structure, change) -> {
						if(structure.getEdgeCount()<3) abort("Need 2 other edges");
						long size = structure.getEdgeCount();
						long index = change.getIndex();
						long index0;
						long index1;
						do {
							index0 = rand.random(0, size);
							index1 = rand.random(0, size);
						} while(index1==index0 || index0==index || index1==index);
						structure.swapEdges(index0, index1);
					})
			);
		}
	}

	@Nested
	class EdgeMoveTest extends StructureContent implements ModelChangeTest<EdgeMoveChange, Structure> {

		@Override
		public Class<?> getTestTargetClass() {
			return EdgeMoveChange.class;
		}

		@Override
		public EdgeMoveChange createTestInstance(TestSettings settings) {
			Structure structure = mockStructure(randomSize());
			return settings.process(createIndividualChanges(structure)
					.findFirst()
					.get()
					.second.apply(structure));
		}

		@Override
		public Stream<Pair<String, Function<Structure, EdgeMoveChange>>> createIndividualChanges(Structure origin) {
			return Stream.of(
				pair("move", structure -> {
					int size = strictToInt(structure.getEdgeCount());
					if(size<2) abort("Need 2 edges for swapping");
					int index0 =  rand.random(0, size);
					int index1;
					do {
						index1 = rand.random(0, size);
					} while(index1==index0);
					Edge edge0 = structure.getEdgeAt(index0);
					Edge edge1 = structure.getEdgeAt(index1);
					return new EdgeMoveChange(structure, size, index0, index1, edge0, edge1);
				})
			);
		}

		@Override
		public List<Pair<String, EdgeMoveChange>> createBulkChanges(Structure source) {
			int size = strictToInt(source.getEdgeCount());
			if(size<2) abort("Need 2 edges for swapping");

			List<Pair<String, EdgeMoveChange>> changes = new ArrayList<>();
			Edge[] dummy = edges(source);
			for (int i = 0; i < size; i++) {
				int index0 = rand.random(0, size);
				int index1;
				do {
					index1 = rand.random(0, size);
				} while(index1==index0);
				Edge edge0 = dummy[index0];
				Edge edge1 = dummy[index1];
				changes.add(pair(i+" move "+index0+"->"+index1, new EdgeMoveChange(
						source, size, index0, index1, edge0, edge1)));
				swap(dummy, index0, index1);
			}
			return changes;
		}

		@Override
		public Stream<Pair<String, BiConsumer<Structure, EdgeMoveChange>>> corruptData() {
			return Stream.of(
					pair("edge added", (structure, change) -> {
						long index = structure.getEdgeCount();
						if(index>0) index = rand.random(0, index);
						structure.addEdge(index, mockEdge());
					}),
					pair("edge removed", (structure, change) -> {
						if(structure.getEdgeCount()<1) abort("Structure already empty");
						structure.removeEdge(rand.random(0, structure.getEdgeCount()));
					}),
					pair("emptied", (structure, change) -> {
						if(structure.getEdgeCount()<1) abort("Structure already empty");
						structure.removeEdges(0, structure.getEdgeCount()-1);
					}),
					pair("edge0 moved", (structure, change) -> {
						if(structure.getEdgeCount()<2) abort("Need 2 edges for swapping");
						long index0 = change.getSourceIndex();
						long index1;
						do {
							index1 = rand.random(0, structure.getEdgeCount());
						} while(index1==index0);
						structure.swapEdges(index0, index1);
					}),
					pair("edge1 moved", (structure, change) -> {
						if(structure.getEdgeCount()<2) abort("Need 2 edges for swapping");
						long index0 = change.getTargetIndex();
						long index1;
						do {
							index1 = rand.random(0, structure.getEdgeCount());
						} while(index1==index0);
						structure.swapEdges(index0, index1);
					})
			);
		}

		@Override
		public Stream<Pair<String, BiConsumer<Structure, EdgeMoveChange>>> mutateData() {
			return Stream.of(
					pair("edges moved", (structure, change) -> {
						if(structure.getEdgeCount()<4) abort("Need 2 disjoint pairs");
						long size = structure.getEdgeCount();
						long indexS = change.getSourceIndex();
						long indexT = change.getTargetIndex();
						long index0;
						long index1;
						do {
							index0 = rand.random(0, size);
							index1 = rand.random(0, size);
						} while(index1==index0 || index0==indexS || index1==indexS
								|| index0==indexT || index1==indexT);
						structure.swapEdges(index0, index1);
					})
			);
		}
	}

	@Nested
	class EdgeSequenceTest extends StructureContent implements ModelChangeTest<EdgeSequenceChange, Structure> {

		@Override
		public Class<?> getTestTargetClass() {
			return EdgeSequenceChange.class;
		}

		@Override
		public EdgeSequenceChange createTestInstance(TestSettings settings) {
			int size = randomSize();
			return settings.process(new EdgeSequenceChange(mockStructure(size),
					size, 0, mockSequence(mockEdge())));
		}

		@Override
		public Stream<Pair<String, Function<Structure, EdgeSequenceChange>>> createIndividualChanges(Structure origin) {
			return Stream.of(
				pair("add single", structure -> {
					long index = structure.getEdgeCount();
					if(index>0) index = rand.random(0, index);
					return new EdgeSequenceChange(structure, structure.getEdgeCount(),
							index, mockSequence(mockEdge()));
				}),
				pair("add bulk", structure -> {
					long index = structure.getEdgeCount();
					if(index>0) index = rand.random(0, index);
					return new EdgeSequenceChange(structure, structure.getEdgeCount(),
							index, mockSequence(mockEdges(randomSize())));
				}),
				pair("remove single", structure -> {
					long size = structure.getEdgeCount();
					if(size<1) abort("Structure already empty");
					long index = rand.random(0, size);
					return new EdgeSequenceChange(structure, structure.getEdgeCount(),
							index, index);
				}),
				pair("remove bulk", structure -> {
					long size = structure.getEdgeCount();
					if(size<2) abort("Structure already empty");
					long index0 = rand.random(0, size-1);
					long index1 = rand.random(index0+1, size);
					return new EdgeSequenceChange(structure, structure.getEdgeCount(),
							index0, index1);
				})
			);
		}

		@Override
		public List<Pair<String, EdgeSequenceChange>> createBulkChanges(Structure source) {
			List<Pair<String, EdgeSequenceChange>> changes = new ArrayList<>();
			List<Edge> dummy = list(edges(source));
			for (int i = 0; i < 10; i++) {
				if(dummy.isEmpty() || rand.nextBoolean()) {
					// Add
					int index = rand.random(0, dummy.size()+1);
					DataSequence<Edge> edges = mockSequence(mockEdges(randomSize()));
					changes.add(pair("add "+index, new EdgeSequenceChange(
							source, dummy.size(), index, edges)));
					dummy.addAll(index, edges.getEntries());
				} else {
					// Remove
					int index0 = rand.random(0, dummy.size());
					int index1 = rand.random(index0, dummy.size());
					List<Edge> subList = dummy.subList(index0, index1+1);
					changes.add(pair("remove ["+index0+"-"+index1+"]", new EdgeSequenceChange(
							source, dummy.size(), index0, index1)));
					subList.clear();
				}
			}
			return changes;
		}

		@Override
		public Stream<Pair<String, BiConsumer<Structure, EdgeSequenceChange>>> corruptData() {
			return Stream.of(
					pair("edge added", (structure, change) -> {
						long index = structure.getEdgeCount();
						if(index>0) index = rand.random(0, index);
						structure.addEdge(index, mockEdge());
					}),
					pair("edge removed", (structure, change) -> {
						if(structure.getEdgeCount()<1) abort("Cannot remove from empty structure");
						structure.removeEdge(rand.random(0, structure.getEdgeCount()));
					}),
					pair("emptied", (structure, change) -> {
						if(structure.getEdgeCount()<1) abort("Cannot empty an empty structure");
						structure.removeEdges(0, structure.getEdgeCount()-1);
					})
					// Sequence changes do not verify individual edge integrity!
			);
		}

		@Override
		public Stream<Pair<String, BiConsumer<Structure, EdgeSequenceChange>>> mutateData() {
			return Stream.of(
					// Move random items
					pair("edges moved (random)", (structure, change) -> {
						if(structure.getEdgeCount()<4) abort("Needs 2 items outside span");
						long size = structure.getEdgeCount();
						long index0;
						long index1;
						do {
							index0 = rand.random(0, size);
							index1 = rand.random(0, size);
						} while(index1==index0);
						structure.swapEdges(index0, index1);
					}),
					// Move random items
					pair("edges moved (overlap)", (structure, change) -> {
						if(change.isAdd()) abort("Cannot remove overlapping sequence before adding");
						if(structure.getEdgeCount()<3) abort("Needs at least 1 item outside span");
						long size = structure.getEdgeCount();
						long index0 = (change.getBeginIndex()+change.getEndIndex())/2;
						long index1;
						do {
							index1 = rand.random(0, size);
						} while(index1==index0);
						structure.swapEdges(index0, index1);
					})
			);
		}
	}

	@Nested
	class TerminalChangeTest implements ModelChangeTest<TerminalChange, Structure> {

		private Edge mockEdge(Structure host, Item source, Item target) {
			MutableObject<Item> s = new MutableObject<Item>(source);
			MutableObject<Item> t = new MutableObject<Item>(target);

			Edge edge = mock(Edge.class, UNSUPPORTED);

			// Host
			doAnswer(invoc -> host).when(edge).getStructure();
			// Source
			doAnswer(invoc -> s.get()).when(edge).getSource();
			// Target
			doAnswer(invoc -> t.get()).when(edge).getTarget();
			// Set terminal
			doAnswer(invoc -> {
				Item item = invoc.getArgument(0);
				boolean isSource = ((Boolean)invoc.getArgument(1)).booleanValue();
				MutableObject<Item> terminal = isSource ? s : t;
				terminal.set(item);
				return null;
			}).when(edge).setTerminal(any(), anyBoolean());
			// Set source
			doAnswer(invoc -> {
				s.set(invoc.getArgument(0));
				return null;
			}).when(edge).setSource(any());
			// Set target
			doAnswer(invoc -> {
				t.set(invoc.getArgument(0));
				return null;
			}).when(edge).setTarget(any());

			return edge;
		}

		@Override
		public Class<?> getTestTargetClass() {
			return TerminalChange.class;
		}

		@Override
		public Structure cloneData(Structure source) {
			Structure s = mock(Structure.class, UNSUPPORTED);
			Edge e = source.getEdgeAt(0);
			Edge mock = mockEdge(s, e.getSource(), e.getTarget());
			stubStructureMethods(s, mock);
			return s;
		}

		@Override
		public boolean dataEquals(Structure expected, Structure actual) {
			if(expected.getEdgeCount()!=actual.getEdgeCount()) {
				return false;
			}
			Edge e0 = expected.getEdgeAt(0);
			Edge e1 = actual.getEdgeAt(0);
			if(e0.getSource()!=e1.getSource()) {
				return false;
			}
			if(e0.getTarget()!=e1.getTarget()) {
				return false;
			}
			return true;
		}

		private Structure createData(Item source, Item target) {
			Structure structure = mock(Structure.class, UNSUPPORTED);
			Edge edge = mockEdge(structure, source, target);
			stubStructureMethods(structure, edge);

			return structure;
		}

		@Override
		public TerminalChange createTestInstance(TestSettings settings) {
			Structure structure = createData(mockItem(), mockItem());
			return settings.process(createIndividualChanges(structure)
					.findFirst()
					.get()
					.second.apply(structure));
		}

		@Override
		public Stream<Pair<String, Structure>> createData() {
			return Stream.of(
					pair("empty", createData(null, null)),
					pair("full", createData(mockItem(), mockItem())),
					pair("source set", createData(mockItem(), null)),
					pair("target set", createData(null, mockItem()))
			);
		}

		@Override
		public Stream<Pair<String, Function<Structure, TerminalChange>>> createIndividualChanges(Structure origin) {
			return Stream.of(
					pair("source null", structure -> {
						Edge edge = structure.getEdgeAt(0);
						if(edge.getSource()==null) abort("Source already null");
						return new TerminalChange(structure, edge, true, null, edge.getSource());
					}),
					pair("source valid", structure -> new TerminalChange(structure,
							structure.getEdgeAt(0), true, mockItem(), structure.getEdgeAt(0).getSource())),
					pair("target null", structure -> {
						Edge edge = structure.getEdgeAt(0);
						if(edge.getTarget()==null) abort("Target already null");
						return new TerminalChange(structure, edge, false, null, edge.getTarget());
					}),
					pair("target valid", structure -> new TerminalChange(structure,
							structure.getEdgeAt(0), false, mockItem(), structure.getEdgeAt(0).getTarget()))
			);
		}

		@Override
		public List<Pair<String, TerminalChange>> createBulkChanges(Structure source) {
			List<Pair<String, TerminalChange>> changes = new ArrayList<>();
			Edge edge = source.getEdgeAt(0);
			Item s = edge.getSource();
			Item t = edge.getTarget();

			if(s!=null) {
				changes.add(pair("source null", new TerminalChange(source, edge, true, null, s)));
				s = null;
			}
			Item item = mockItem();
			changes.add(pair("source fill", new TerminalChange(source, edge, true, item, s)));
			s = item;

			if(t!=null) {
				changes.add(pair("target null", new TerminalChange(source, edge, false, null, t)));
				t = null;
			}
			item = mockItem();
			changes.add(pair("target fill", new TerminalChange(source, edge, false, item, t)));
			t = item;

			return changes;
		}

		@Override
		public Stream<Pair<String, BiConsumer<Structure, TerminalChange>>> corruptData() {
			return Stream.of(
					pair("source changed", (structure, change) -> {
						if(!change.isSource()) abort("Change unaffected by source");
						Edge edge = structure.getEdgeAt(0);
						edge.setSource(mockItem());
					}),
					pair("target changed", (structure, change) -> {
						if(change.isSource()) abort("Change unaffected by target");
						Edge edge = structure.getEdgeAt(0);
						edge.setTarget(mockItem());
					})
			);
		}

		@Override
		public Stream<Pair<String, BiConsumer<Structure, TerminalChange>>> mutateData() {
			return Stream.of(
					pair("source changed", (structure, change) -> {
						if(change.isSource()) abort("Change affected by source");
						Edge edge = structure.getEdgeAt(0);
						edge.setSource(mockItem());
					}),
					pair("target changed", (structure, change) -> {
						if(!change.isSource()) abort("Change affected by target");
						Edge edge = structure.getEdgeAt(0);
						edge.setTarget(mockItem());
					})
			);
		}
	}

	@Nested
	class PositionChangeTest implements ModelChangeTest<PositionChange, Fragment> {

		private Fragment mockFragment(Position begin, Position end) {
			Fragment fragment = mock(Fragment.class, UNSUPPORTED);

			MutableObject<Position> b = new MutableObject<>(begin);
			MutableObject<Position> e = new MutableObject<>(end);

			doAnswer(invoc -> b.get()).when(fragment).getFragmentBegin();
			doAnswer(invoc -> e.get()).when(fragment).getFragmentEnd();
			doAnswer(invoc -> {
				b.set(invoc.getArgument(0));
				return null;
			}).when(fragment).setFragmentBegin(any());
			doAnswer(invoc -> {
				e.set(invoc.getArgument(0));
				return null;
			}).when(fragment).setFragmentEnd(any());

			// To ensure the verification framework won't kick in
			doAnswer(invoc -> null).when(fragment).getLayer();

			return fragment;
		}

		@Override
		public Class<?> getTestTargetClass() {
			return PositionChange.class;
		}

		@Override
		public Stream<Pair<String, Fragment>> createData() {
			return Stream.of(
					pair("empty", mockFragment(null, null)),
					pair("filled", mockFragment(mockPosition(0, 1), mockPosition(0, 3))),
					pair("begin", mockFragment(mockPosition(0), null)),
					pair("end", mockFragment(null, mockPosition(1, 2, 3)))
			);
		}

		@Override
		public Fragment cloneData(Fragment source) {
			return mockFragment(source.getFragmentBegin(), source.getFragmentEnd());
		}

		@Override
		public boolean dataEquals(Fragment expected, Fragment actual) {
			return expected.getFragmentBegin()==actual.getFragmentBegin()
					&& expected.getFragmentEnd()==actual.getFragmentEnd();
		}

		@Override
		public PositionChange createTestInstance(TestSettings settings) {
			Fragment fragment = mockFragment(mockPosition(0, 2), mockPosition(0, 3));
			return settings.process(createIndividualChanges(fragment)
					.findFirst()
					.get()
					.second.apply(fragment));
		}

		@Override
		public Stream<Pair<String, Function<Fragment, PositionChange>>> createIndividualChanges(Fragment origin) {
			return Stream.of(
					pair("begin null", fragment -> {
						if(fragment.getFragmentBegin()==null) abort("begin already null");
						return new PositionChange(fragment, true, null);
					}),
					pair("begin valid", fragment -> new PositionChange(fragment, true, mockPosition(1,2))),
					pair("end null", fragment -> {
						if(fragment.getFragmentEnd()==null) abort("End already null");
						return new PositionChange(fragment, false, null);
					}),
					pair("end valid", fragment -> new PositionChange(fragment, false, mockPosition(3, 4)))
			);
		}

		@Override
		public List<Pair<String, PositionChange>> createBulkChanges(Fragment source) {
			List<Pair<String, PositionChange>> changes = new ArrayList<>();
			Position b = source.getFragmentBegin();
			Position e = source.getFragmentEnd();

			if(b!=null) {
				changes.add(pair("begin null", new PositionChange(source, true, null)));
				b = null;
			}
			Position pos = mockPosition(1, 2, 4);
			changes.add(pair("begin fill", new PositionChange(source, true, pos)));
			b = pos;

			if(e!=null) {
				changes.add(pair("end null", new PositionChange(source, false, null)));
				e = null;
			}
			pos = mockPosition(5, 4, 3);
			changes.add(pair("end fill", new PositionChange(source, false, pos)));
			e = pos;

			return changes;
		}

		@Override
		public Stream<Pair<String, BiConsumer<Fragment, PositionChange>>> corruptData() {
			return Stream.empty(); // THis change doesn't do real verifications
		}

		@Override
		public Stream<Pair<String, BiConsumer<Fragment, PositionChange>>> mutateData() {
			return Stream.of(
					pair("source changed", (fragment, change) -> {
						fragment.setFragmentBegin(mockPosition(1, 2));
					}),
					pair("target changed", (fragment, change) -> {
						fragment.setFragmentEnd(mockPosition(4, 5));
					})
			);
		}
	}

	abstract class AnnotationContent<T, C extends AtomicValueChange<T> & SerializableAtomicChange>
			extends TestBase implements ModelChangeTest<C, AnnotationLayer> {

		/** Return all supported value types, at least 1! */
		abstract Stream<ValueType> getValueTypes();

		/** Create a series of test values, with at least 1 entry! */
		abstract Stream<Pair<String,Object>> createValues(ValueType type);

		abstract Object noEntryValue(ValueType type);

		AnnotationLayer mockLayer(AnnotationStorage storage) {
			if(storage==null) {
				storage = mock(AnnotationStorage.class, UNSUPPORTED);
			}

			final AnnotationStorage s = storage;

			Corpus corpus = mock(Corpus.class);
			AnnotationLayer layer = mock(AnnotationLayer.class, UNSUPPORTED);
			doAnswer(invoc -> s).when(layer).getAnnotationStorage();
			doAnswer(invoc -> corpus).when(layer).getCorpus();

			return layer;
		}

		@Override
		public Stream<Pair<String, AnnotationLayer>> createData() {
			return getValueTypes().map(type -> pair(type.getName(), mockLayer(
					new AnnotationStorageDummy(type, noEntryValue(type)))));
		}

		@Override
		public AnnotationLayer cloneData(AnnotationLayer source) {
			AnnotationStorageDummy storage0 = (AnnotationStorageDummy) source.getAnnotationStorage();
			AnnotationStorageDummy storage = new AnnotationStorageDummy(
					storage0.getValueType(), storage0.getNoEntryValue());
			storage.copyFrom(storage0);
			return mockLayer(storage);
		}

		@Override
		public boolean dataEquals(AnnotationLayer expected, AnnotationLayer actual) {
			return expected.getAnnotationStorage().equals(actual.getAnnotationStorage());
		}

		abstract C createChange(AnnotationLayer layer, ValueType type,
				Item item, String key, Object newValue, Object oldValue);

		@Override
		public C createTestInstance(TestSettings settings) {
			ValueType valueType = getValueTypes().findFirst().get();
			Object noEntryValue = noEntryValue(valueType);
			AnnotationLayer layer = mockLayer(new AnnotationStorageDummy(valueType, noEntryValue));
			return settings.process(createIndividualChanges(layer)
					.findFirst()
					.get()
					.second.apply(layer));
		}

		@Override
		public Stream<Pair<String, Function<AnnotationLayer, C>>> createIndividualChanges(AnnotationLayer origin) {
			return createValues(((AnnotationStorageDummy)origin.getAnnotationStorage()).getValueType())
					.map(pVal -> pair(pVal.first, layer -> {
						AnnotationStorageDummy storage = (AnnotationStorageDummy) layer.getAnnotationStorage();
						ValueType type = storage.getValueType();
						Object value = pVal.second;
						if(Objects.equals(storage.getNoEntryValue(), value)) abort("noEntryValue/null");
						Item item = mockItem();
						String key = randomKey();
						return createChange(layer, type, item, key, value,
								storage.getValue(item, key));
					}));
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<Pair<String, C>> createBulkChanges(AnnotationLayer source) {
			AnnotationStorageDummy storage = (AnnotationStorageDummy) source.getAnnotationStorage();
			return rand.mix(Stream.of(mockItems(randomSize()))
					// Make keys
					.flatMap(item -> IntStream.rangeClosed(1, 6)
							.mapToObj(idx -> pair(item, "key_"+idx)))
					// Make queue of changes for every item+key combo
					.map(p -> {
						Item item = p.first;
						String key = p.second;
						List<Pair<String,Object>> values = createValues(storage.getValueType())
								.collect(Collectors.toList());
						Queue<Pair<String, C>> changes = new ArrayDeque<>();

						for (int i = 0; i < values.size(); i++) {
							Pair<String,Object> value = values.get(i);
							Pair<String,Object> oldValue = i>0 ? values.get(i-1)
									: nullablePair("noEntryValue", storage.getNoEntryValue());

							// Skip "empty" changes
							if(Objects.equals(value.second, oldValue.second)) {
								continue;
							}
							changes.add(pair(String.format("%s-%s-%d: %s->%s", item, key, _int(i),
										oldValue.first, value.first),
									createChange(source, storage.getValueType(),
											item, key, value.second, oldValue.second)));
						}

						return changes;
					}).toArray(Queue[]::new));
		}

		@Override
		public Stream<Pair<String, BiConsumer<AnnotationLayer, C>>> corruptData() {
			return Stream.of(
					pair("remove item", (layer, change) -> {
						AnnotationStorageDummy storage = (AnnotationStorageDummy) layer.getAnnotationStorage();
						if(storage.isEmpty()) abort("Storage already empty");
						storage.remove(change.getItem());
					}),
					pair("change value", (layer, change) -> {
						AnnotationStorageDummy storage = (AnnotationStorageDummy) layer.getAnnotationStorage();
						Object value = change.getPreviousValue();
						Object replacement = createValues(storage.getValueType())
								.map(p -> p.second)
								.filter(v -> !v.equals(value))
								.findFirst()
								.get();
						storage.setValue(change.getItem(), change.getKey(), replacement);
					})
			);
		}

		@Override
		public Stream<Pair<String, BiConsumer<AnnotationLayer, C>>> mutateData() {
			return Stream.of(
					pair("add extra key+value", (layer, change) -> {
						AnnotationStorageDummy storage = (AnnotationStorageDummy) layer.getAnnotationStorage();
						Object value = createValues(storage.getValueType())
								.map(p -> p.second)
								.filter(v -> !Objects.equals(v, storage.getNoEntryValue()))
								.findAny()
								.get();
						storage.setValue(change.getItem(), randomKey2(), value);
					}),
					pair("add extra key - same value", (layer, change) -> {
						AnnotationStorageDummy storage = (AnnotationStorageDummy) layer.getAnnotationStorage();
						Object value = storage.getValue(change.getItem(), change.getKey());
						if(Objects.equals(value, storage.getNoEntryValue())) abort("Already mapped to noEntryValue");
						storage.setValue(change.getItem(), randomKey2(), value);
					}),
					pair("add extra item - same key+value", (layer, change) -> {
						AnnotationStorageDummy storage = (AnnotationStorageDummy) layer.getAnnotationStorage();
						Object value = storage.getValue(change.getItem(), change.getKey());
						if(Objects.equals(value, storage.getNoEntryValue())) abort("Already mapped to noEntryValue");
						storage.setValue(mockItem(), change.getKey(), value);
					})
			);
		}
	}

	@Nested
	class ValueChangeTest extends AnnotationContent<Object, ValueChange> {

		@Override
		public Class<?> getTestTargetClass() {
			return ValueChange.class;
		}

		@Override
		Stream<ValueType> getValueTypes() {
			return Stream.of(ValueType.CUSTOM, ValueType.ENUM, ValueType.STRING);
		}

		@Override
		Stream<Pair<String, Object>> createValues(ValueType type) {
			MutableInteger idx = new MutableInteger();
			return Stream.of(getTestValues(type)).map(
					val -> pair(type.getName()+idx.getAndIncrement(), val));
		}

		@Override
		ValueChange createChange(AnnotationLayer layer, ValueType type, Item item,
				String key, Object newValue, Object oldValue) {
			return new ValueChange(layer, type, item, key, newValue, oldValue);
		}

		@Override
		Object noEntryValue(ValueType type) {
			return null;
		}
	}

	@Nested
	class IntegerValueChangeTest extends AnnotationContent<Integer, IntegerValueChange> {

		private int noEntryValue;

		@BeforeEach
		void setUp() {
			noEntryValue = rand.nextInt();
		}

		@Override
		public Class<?> getTestTargetClass() {
			return IntegerValueChange.class;
		}

		@Override
		Stream<ValueType> getValueTypes() {
			return Stream.of(ValueType.INTEGER);
		}

		@Override
		Object noEntryValue(ValueType type) {
			return _int(noEntryValue);
		}

		@Override
		Stream<Pair<String, Object>> createValues(ValueType type) {
			assertSame(ValueType.INTEGER, type);
			return rand.ints()
					.limit(10)
					.mapToObj(v -> pair(String.valueOf(v), _int(v)));
		}

		@Override
		IntegerValueChange createChange(AnnotationLayer layer, ValueType type,
				Item item, String key, Object newValue, Object oldValue) {
			return new IntegerValueChange(layer, item, key,
					((Number)newValue).intValue(), ((Number)oldValue).intValue());
		}
	}

	@Nested
	class LongValueChangeTest extends AnnotationContent<Long, LongValueChange> {

		private long noEntryValue;

		@BeforeEach
		void setUp() {
			noEntryValue = rand.nextLong();
		}

		@Override
		public Class<?> getTestTargetClass() {
			return LongValueChange.class;
		}

		@Override
		Stream<ValueType> getValueTypes() {
			return Stream.of(ValueType.LONG);
		}

		@Override
		Object noEntryValue(ValueType type) {
			return _long(noEntryValue);
		}

		@Override
		Stream<Pair<String, Object>> createValues(ValueType type) {
			assertSame(ValueType.LONG, type);
			return rand.longs()
					.limit(10)
					.mapToObj(v -> pair(String.valueOf(v), _long(v)));
		}

		@Override
		LongValueChange createChange(AnnotationLayer layer, ValueType type,
				Item item, String key, Object newValue, Object oldValue) {
			return new LongValueChange(layer, item, key,
					((Number)newValue).longValue(), ((Number)oldValue).longValue());
		}
	}

	@Nested
	class FloatValueChangeTest extends AnnotationContent<Float, FloatValueChange> {

		private float noEntryValue;

		@BeforeEach
		void setUp() {
			noEntryValue = rand.nextFloat();
		}

		@Override
		public Class<?> getTestTargetClass() {
			return FloatValueChange.class;
		}

		@Override
		Stream<ValueType> getValueTypes() {
			return Stream.of(ValueType.FLOAT);
		}

		@Override
		Object noEntryValue(ValueType type) {
			return _float(noEntryValue);
		}

		@Override
		Stream<Pair<String, Object>> createValues(ValueType type) {
			assertSame(ValueType.FLOAT, type);
			return DoubleStream.generate(rand::nextFloat)
					.limit(10)
					.mapToObj(v -> pair(String.valueOf(v), _float((float)v)));
		}

		@Override
		FloatValueChange createChange(AnnotationLayer layer, ValueType type,
				Item item, String key, Object newValue, Object oldValue) {
			return new FloatValueChange(layer, item, key,
					((Number)newValue).floatValue(), ((Number)oldValue).floatValue());
		}
	}

	@Nested
	class DoubleValueChangeTest extends AnnotationContent<Double, DoubleValueChange> {

		private double noEntryValue;

		@BeforeEach
		void setUp() {
			noEntryValue = rand.nextDouble();
		}

		@Override
		public Class<?> getTestTargetClass() {
			return DoubleValueChange.class;
		}

		@Override
		Stream<ValueType> getValueTypes() {
			return Stream.of(ValueType.DOUBLE);
		}

		@Override
		Object noEntryValue(ValueType type) {
			return _double(noEntryValue);
		}

		@Override
		Stream<Pair<String, Object>> createValues(ValueType type) {
			assertSame(ValueType.DOUBLE, type);
			return rand.doubles()
					.limit(10)
					.mapToObj(v -> pair(String.valueOf(v), _double(v)));
		}

		@Override
		DoubleValueChange createChange(AnnotationLayer layer, ValueType type,
				Item item, String key, Object newValue, Object oldValue) {
			return new DoubleValueChange(layer, item, key,
					((Number)newValue).doubleValue(), ((Number)oldValue).doubleValue());
		}
	}

	@Nested
	class BooleanValueChangeTest extends AnnotationContent<Boolean, BooleanValueChange> {

		@Override
		public Class<?> getTestTargetClass() {
			return BooleanValueChange.class;
		}

		@Override
		Stream<ValueType> getValueTypes() {
			return Stream.of(ValueType.BOOLEAN);
		}

		@Override
		Object noEntryValue(ValueType type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Stream<Pair<String, AnnotationLayer>> createData() {
			return createValues(ValueType.BOOLEAN)
					.map(p -> pair("noEntryValue="+p.first, mockLayer(
							new AnnotationStorageDummy(ValueType.BOOLEAN, p.second))));
		}

		@Override
		public BooleanValueChange createTestInstance(TestSettings settings) {
			return settings.process(new BooleanValueChange(
					mockLayer(new AnnotationStorageDummy(ValueType.BOOLEAN, Boolean.FALSE)),
					mockItem(), "test", true, false));
		}

		@Override
		Stream<Pair<String, Object>> createValues(ValueType type) {
			assertSame(ValueType.BOOLEAN, type);
			return Stream.of(Boolean.TRUE, Boolean.FALSE)
					.map(v -> pair(String.valueOf(v), v));
		}

		@Override
		BooleanValueChange createChange(AnnotationLayer layer, ValueType type,
				Item item, String key, Object newValue, Object oldValue) {
			return new BooleanValueChange(layer, item, key,
					((Boolean)newValue).booleanValue(), ((Boolean)oldValue).booleanValue());
		}
	}

	static class AnnotationStorageDummy implements AnnotationStorage {

		static class Key {
			final Item item;
			final String key;

			Key(Item item, String key) {
				this.item = requireNonNull(item);
				this.key = requireNonNull(key);
			}

			@Override
			public int hashCode() { return Objects.hash(item, key); }

			@Override
			public boolean equals(Object obj) {
				if(obj==this) {
					return true;
				} else if(obj instanceof Key) {
					Key other = (Key) obj;
					return item==other.item && key.equals(other.key);
				}
				return false;
			}
		}

		private final ValueType valueType;

		private final Map<Key, Object> data = new Object2ObjectOpenHashMap<>();

		private final Object noEntryValue;

		AnnotationStorageDummy(ValueType valueType, Object noEntryValue) {
			this.valueType = requireNonNull(valueType);
			this.noEntryValue = noEntryValue;
		}

		void copyFrom(AnnotationStorageDummy source) {
			data.putAll(source.data);
		}

		@Override
		public boolean equals(Object obj) {
			if(obj==this) {
				return true;
			} else if(obj instanceof AnnotationStorageDummy) {
				AnnotationStorageDummy other = (AnnotationStorageDummy) obj;
				return valueType.equals(other.valueType) && data.equals(other.data);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return data.hashCode();
		}

		public ValueType getValueType() { return valueType; }

		public Object getNoEntryValue() { return noEntryValue; }

		public Set<String> getKeys() {
			return data.keySet().stream().map(k -> k.key).collect(Collectors.toSet());
		}

		public Set<Item> getItems() {
			return data.keySet().stream().map(k -> k.item).collect(Collectors.toSet());
		}

		public Set<Key> keys() {
			return data.keySet();
		}

		public void remove(Item item) {
			for(Iterator<Key> it = data.keySet().iterator(); it.hasNext();) {
				if(it.next().item==item) {
					it.remove();
				}
			}
		}

		public boolean isEmpty() {
			return data.isEmpty();
		}

		@Override
		public boolean collectKeys(Item item, Consumer<String> action) {
			throw new UnsupportedOperationException();
		}

		private Key key(Item item, String key) {
			return new Key(item, key);
		}

		@Override
		public Object getValue(Item item, String key) {
			return data.getOrDefault(key(item, key), noEntryValue);
		}

		@Override
		public int getInteger(Item item, String key) {
			return ((Number)getValue(item, key)).intValue();
		}

		@Override
		public float getFloat(Item item, String key) {
			return ((Number)getValue(item, key)).floatValue();
		}

		@Override
		public double getDouble(Item item, String key) {
			return ((Number)getValue(item, key)).doubleValue();
		}

		@Override
		public long getLong(Item item, String key) {
			return ((Number)getValue(item, key)).longValue();
		}

		@Override
		public boolean getBoolean(Item item, String key) {
			return ((Boolean)getValue(item, key)).booleanValue();
		}

		@Override
		public void removeAllValues(Supplier<? extends Item> source) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setValue(Item item, String key, Object value) {
			if(value==null || value.equals(noEntryValue)) {
				data.remove(key(item, key));
			} else {
				valueType.checkValue(value);
				data.put(key(item, key), value);
			}
		}

		@Override
		public void setInteger(Item item, String key, int value) {
			setValue(item, key, _int(value));
		}

		@Override
		public void setLong(Item item, String key, long value) {
			setValue(item, key, _long(value));
		}

		@Override
		public void setFloat(Item item, String key, float value) {
			setValue(item, key, _float(value));
		}

		@Override
		public void setDouble(Item item, String key, double value) {
			setValue(item, key, _double(value));
		}

		@Override
		public void setBoolean(Item item, String key, boolean value) {
			setValue(item, key, _boolean(value));
		}

		@Override
		public boolean hasAnnotations() { throw new UnsupportedOperationException(); }

		@Override
		public boolean hasAnnotations(Item item) { throw new UnsupportedOperationException(); }

	}
}
