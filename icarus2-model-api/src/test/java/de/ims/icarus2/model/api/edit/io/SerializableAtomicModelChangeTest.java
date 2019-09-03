/**
 *
 */
package de.ims.icarus2.model.api.edit.io;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdges;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItems;
import static de.ims.icarus2.model.api.ModelTestUtils.mockPosition;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.getTestValues;
import static de.ims.icarus2.test.TestUtils.abort;
import static de.ims.icarus2.test.TestUtils.assertCollectionNotEmpty;
import static de.ims.icarus2.test.TestUtils.mix;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.randomString;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
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
import org.mockito.stubbing.Answer;

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
import de.ims.icarus2.test.guard.ApiGuard;
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

		@Override
		default void configureApiGuard(ApiGuard<C> apiGuard) {
			ApiGuardedTest.super.configureApiGuard(apiGuard);
			apiGuard.nullGuard(true);
		}

		/** Create a bunch of usable data instances */
		Stream<Pair<String, B>> createData();
		/** Create a copy of current source state so we can verify it later */
		B cloneData(B source);
		/** Make sure given data matches the expected state */
		boolean dataEquals(B expected, B actual);

		/** Create a single testable change for source */
		C createChange(B source);

		/** For some source data create testable change instances (defaults to createChange()) */
		default List<Pair<String, C>> createChanges(B source) {
			return Arrays.asList(pair("default", createChange(source)));
		}

		@Test
		default void testGetChangeType() {
			assertNotNull(create().getType());
		}

		/** For each data run a single change in isolation */
		@TestFactory
		default Stream<DynamicNode> testIsolated() {
			return createData().map(p -> dynamicTest(p.first, () -> {
				B source = p.second;
				B data = cloneData(source);
				assertNotSame(source, data);

				C change = createChange(data);

				change.execute();
				assertFalse(dataEquals(source, data));

				assertTrue(change.canReverse());

				change.execute();
				assertTrue(dataEquals(source, data));
			}));
		}

		/** For each data run a sequence of changes */
		@TestFactory
		default Stream<DynamicNode> testBulk() {
			return createData().map(p -> dynamicTest(p.first, () -> {
				B source = p.second;
				List<Pair<String,C>> changes = createChanges(source);
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

		//TODO add tests for: serialization, deserialization, full I/O cycle, corrupted data
	}

	private static int randomSize() {
		return random(10, 30);
	}

	// Use this to "disable" all the methods we haven't stubbed
	private static final Answer<?> UNSUPPORTED = invoc -> {
		throw new UnsupportedOperationException();
	};

	abstract class ContainerContent {

		public Stream<Pair<String, Container>> createData() {
			return Stream.of(
				pair("empty", mockContainer(0)),
				pair("singleton", mockContainer(0)),
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

	abstract class StructureContent {

		public Stream<Pair<String, Structure>> createData() {
			return Stream.of(
				pair("empty", mockStructure(0)),
				pair("singleton", mockStructure(0)),
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
			return settings.process(createChange(mockContainer(randomSize())));
		}

		@Override
		public ItemChange createChange(Container source) {
			return new ItemChange(source, mockItem(), source.getItemCount(), 0, true);
		}

		@Override
		public List<Pair<String, ItemChange>> createChanges(Container source) {
			List<Pair<String, ItemChange>> changes = new ArrayList<>();
			List<Item> dummy = list(items(source));
			for (int i = 0; i < 10; i++) {
				if(dummy.isEmpty() || random().nextBoolean()) {
					// Add
					int index = random(0, dummy.size()+1);
					Item item = mockItem();
					changes.add(pair("add "+index, new ItemChange(
							source, item, dummy.size(), index, true)));
					dummy.add(index, item);
				} else {
					// Remove
					int index = random(0, dummy.size());
					Item item = dummy.remove(index);
					changes.add(pair("remove "+index, new ItemChange(
							source, item, dummy.size()+1, index, false)));
				}
			}
			return changes;
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
			return settings.process(createChange(mockContainer(randomSize())));
		}

		@Override
		public ItemMoveChange createChange(Container source) {
			int size = strictToInt(source.getItemCount());
			if(size<2) {
				abort();
			}
			int index0 = random(0, size);
			int index1;
			do {
				index1 = random(0, size);
			} while(index1==index0);
			Item item0 = source.getItemAt(index0);
			Item item1 = source.getItemAt(index1);
			return new ItemMoveChange(source, size, index0, index1, item0, item1);
		}

		@Override
		public List<Pair<String, ItemMoveChange>> createChanges(Container source) {
			int size = strictToInt(source.getItemCount());
			if(size<2) {
				abort();
			}

			List<Pair<String, ItemMoveChange>> changes = new ArrayList<>();
			Item[] dummy = items(source);
			for (int i = 0; i < size; i++) {
				int index0 = random(0, size);
				int index1;
				do {
					index1 = random(0, size);
				} while(index1==index0);
				Item item0 = dummy[index0];
				Item item1 = dummy[index1];
				changes.add(pair(i+" move "+index0+"->"+index1, new ItemMoveChange(
						source, size, index0, index1, item0, item1)));
				swap(dummy, index0, index1);
			}
			return changes;
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
			return settings.process(createChange(mockContainer(randomSize())));
		}

		@Override
		public ItemSequenceChange createChange(Container source) {
			return new ItemSequenceChange(source, source.getItemCount(),
					0, mockSequence(mockItem()));
		}

		@Override
		public List<Pair<String, ItemSequenceChange>> createChanges(Container source) {
			List<Pair<String, ItemSequenceChange>> changes = new ArrayList<>();
			List<Item> dummy = list(items(source));
			for (int i = 0; i < 10; i++) {
				if(dummy.isEmpty() || random().nextBoolean()) {
					// Add
					int index = random(0, dummy.size()+1);
					DataSequence<Item> items = mockSequence(mockItems(randomSize()));
					changes.add(pair("add "+index, new ItemSequenceChange(
							source, dummy.size(), index, items)));
					dummy.addAll(index, items.getEntries());
				} else {
					// Remove
					int index0 = random(0, dummy.size());
					int index1 = random(index0, dummy.size());
					List<Item> subList = dummy.subList(index0, index1+1);
					changes.add(pair("remove ["+index0+"-"+index1+"]", new ItemSequenceChange(
							source, dummy.size(), index0, index1)));
					subList.clear();
				}
			}
			return changes;
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
			return settings.process(createChange(mockStructure(randomSize())));
		}

		@Override
		public EdgeChange createChange(Structure source) {
			return new EdgeChange(source, mockEdge(), source.getEdgeCount(), 0, true);
		}

		@Override
		public List<Pair<String, EdgeChange>> createChanges(Structure source) {
			List<Pair<String, EdgeChange>> changes = new ArrayList<>();
			List<Edge> dummy = list(edges(source));
			for (int i = 0; i < 10; i++) {
				if(dummy.isEmpty() || random().nextBoolean()) {
					// Add
					int index = random(0, dummy.size()+1);
					Edge edge = mockEdge();
					changes.add(pair("add "+index, new EdgeChange(
							source, edge, dummy.size(), index, true)));
					dummy.add(index, edge);
				} else {
					// Remove
					int index = random(0, dummy.size());
					Edge edge = dummy.remove(index);
					changes.add(pair("remove "+index, new EdgeChange(
							source, edge, dummy.size()+1, index, false)));
				}
			}
			return changes;
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
			return settings.process(createChange(mockStructure(randomSize())));
		}

		@Override
		public EdgeMoveChange createChange(Structure source) {
			int size = strictToInt(source.getEdgeCount());
			if(size<2) {
				abort();
			}
			int index0 = random(0, size);
			int index1;
			do {
				index1 = random(0, size);
			} while(index1==index0);
			Edge edge0 = source.getEdgeAt(index0);
			Edge edge1 = source.getEdgeAt(index1);
			return new EdgeMoveChange(source, size, index0, index1, edge0, edge1);
		}

		@Override
		public List<Pair<String, EdgeMoveChange>> createChanges(Structure source) {
			int size = strictToInt(source.getEdgeCount());
			if(size<2) {
				abort();
			}

			List<Pair<String, EdgeMoveChange>> changes = new ArrayList<>();
			Edge[] dummy = edges(source);
			for (int i = 0; i < size; i++) {
				int index0 = random(0, size);
				int index1;
				do {
					index1 = random(0, size);
				} while(index1==index0);
				Edge edge0 = dummy[index0];
				Edge edge1 = dummy[index1];
				changes.add(pair(i+" move "+index0+"->"+index1, new EdgeMoveChange(
						source, size, index0, index1, edge0, edge1)));
				swap(dummy, index0, index1);
			}
			return changes;
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
			return settings.process(createChange(mockStructure(randomSize())));
		}

		@Override
		public EdgeSequenceChange createChange(Structure source) {
			return new EdgeSequenceChange(source, source.getEdgeCount(),
					0, mockSequence(mockEdge()));
		}

		@Override
		public List<Pair<String, EdgeSequenceChange>> createChanges(Structure source) {
			List<Pair<String, EdgeSequenceChange>> changes = new ArrayList<>();
			List<Edge> dummy = list(edges(source));
			for (int i = 0; i < 10; i++) {
				if(dummy.isEmpty() || random().nextBoolean()) {
					// Add
					int index = random(0, dummy.size()+1);
					DataSequence<Edge> edges = mockSequence(mockEdges(randomSize()));
					changes.add(pair("add "+index, new EdgeSequenceChange(
							source, dummy.size(), index, edges)));
					dummy.addAll(index, edges.getEntries());
				} else {
					// Remove
					int index0 = random(0, dummy.size());
					int index1 = random(index0, dummy.size());
					List<Edge> subList = dummy.subList(index0, index1+1);
					changes.add(pair("remove ["+index0+"-"+index1+"]", new EdgeSequenceChange(
							source, dummy.size(), index0, index1)));
					subList.clear();
				}
			}
			return changes;
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

			return settings.process(createChange(createData(null, null)));
		}

		@Override
		public Stream<Pair<String, Structure>> createData() {
			return Stream.of(
					pair("empty", createData(null, null)),
					pair("full", createData(mockItem(), mockItem())),
					pair("source", createData(mockItem(), null)),
					pair("target", createData(null, mockItem()))
			);
		}

		@Override
		public TerminalChange createChange(Structure source) {
			Edge edge = source.getEdgeAt(0);
			return new TerminalChange(source, edge, true, mockItem(), edge.getSource());
		}

		@Override
		public List<Pair<String, TerminalChange>> createChanges(Structure source) {
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
		public PositionChange createTestInstance(TestSettings settings) {
			return settings.process(createChange(mockFragment(null, null)));
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
		public PositionChange createChange(Fragment source) {
			return new PositionChange(source, true, mockPosition(0, 1, 2));
		}

		@Override
		public List<Pair<String, PositionChange>> createChanges(Fragment source) {
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
	}

	abstract class AnnotationContent<C extends SerializableAtomicChange>
			implements ModelChangeTest<C, AnnotationLayer> {

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

			AnnotationLayer layer = mock(AnnotationLayer.class, UNSUPPORTED);
			doAnswer(invoc -> s).when(layer).getAnnotationStorage();

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
			return settings.process(createChange(mockLayer(
					new AnnotationStorageDummy(valueType, noEntryValue))));
		}

		@Override
		public C createChange(AnnotationLayer source) {
			AnnotationStorageDummy storage = (AnnotationStorageDummy) source.getAnnotationStorage();
			ValueType type = storage.getValueType();
			Object value = createValues(type).findAny().map(p -> p.second).get();
			Item item = mockItem();
			String key = randomString(10);

			return createChange(source, type, item, key, value,
					storage.getValue(item, key));
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<Pair<String, C>> createChanges(AnnotationLayer source) {
			AnnotationStorageDummy storage = (AnnotationStorageDummy) source.getAnnotationStorage();
			return mix(Stream.of(mockItems(randomSize()))
					.flatMap(item -> IntStream.rangeClosed(1, 6)
							.mapToObj(idx -> pair(item, "key_"+idx)))
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
							changes.add(pair(String.format("%s-%s-%d: %s->%s", item, key, _int(i),
										oldValue.first, value.first),
									createChange(source, storage.getValueType(),
											item, key, value.second, oldValue.second)));
						}

						return changes;
					}).toArray(Queue[]::new));
		}
	}

	@Nested
	class ValueChangeTest extends AnnotationContent<ValueChange> {

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
	class IntegerValueChangeTest extends AnnotationContent<IntegerValueChange> {

		private int noEntryValue;

		@BeforeEach
		void setUp() {
			noEntryValue = random().nextInt();
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
			return random().ints()
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
	class LongValueChangeTest extends AnnotationContent<LongValueChange> {

		private long noEntryValue;

		@BeforeEach
		void setUp() {
			noEntryValue = random().nextLong();
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
			return random().longs()
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
	class FloatValueChangeTest extends AnnotationContent<FloatValueChange> {

		private float noEntryValue;

		@BeforeEach
		void setUp() {
			noEntryValue = random().nextFloat();
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
			return DoubleStream.generate(random()::nextFloat)
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
	class DoubleValueChangeTest extends AnnotationContent<DoubleValueChange> {

		private double noEntryValue;

		@BeforeEach
		void setUp() {
			noEntryValue = random().nextDouble();
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
			return random().doubles()
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
	class BooleanValueChangeTest extends AnnotationContent<BooleanValueChange> {

		private boolean noEntryValue;

		@BeforeEach
		void setUp() {
			noEntryValue = random().nextBoolean();
		}

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
			return _boolean(noEntryValue);
		}

		@Override
		Stream<Pair<String, Object>> createValues(ValueType type) {
			assertSame(ValueType.BOOLEAN, type);
			return Stream.of(Boolean.valueOf(!noEntryValue))
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
