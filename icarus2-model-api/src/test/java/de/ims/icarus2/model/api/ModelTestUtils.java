/**
 *
 */
package de.ims.icarus2.model.api;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.test.TestUtils.assertMock;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.collections.seq.DataSequence;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

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
		when(item.equals(any())).then(
				invocation -> equals(item, invocation.getArgument(0)));
		return item;
	}

	public static Item mockItem() {
		return mock(Item.class);
	}

	public static Edge mockEdge() {
		return mock(Edge.class);
	}

	public static Edge mockEdge(Item source, Item target) {
		requireNonNull(source);
		requireNonNull(target);

		Edge edge = mock(Edge.class);
		when(edge.getSource()).thenReturn(source);
		when(edge.getTarget()).thenReturn(target);

		return edge;
	}

	@SuppressWarnings("boxing")
	public static Container mockContainer(long itemCount) {
		checkArgument(itemCount>=0);

		Container container = mock(Container.class);
		when(container.getItemCount()).thenReturn(itemCount);

		stubItems(container);

		return container;
	}

	private static void checkItemIndex(Container container, long index) {
		if(index<0 || index>=container.getItemCount())
			throw new IndexOutOfBoundsException();
	}

	private static void stubItems(Container container) {

		final Long2ObjectMap<Item> items = new Long2ObjectOpenHashMap<>();

		when(container.getItemAt(anyLong())).then(invocation -> {
			@SuppressWarnings("boxing")
			long index = invocation.getArgument(0);
			checkItemIndex(container, index);

			return items.computeIfAbsent(index, k -> stubId(mockItem(), k));
		});
	}

	@SuppressWarnings("boxing")
	public static Structure mockStructure(long itemCount, long edgeCount) {
		checkArgument(itemCount>=0);
		checkArgument(edgeCount>=0);

		Structure structure = mock(Structure.class);
		when(structure.getItemCount()).thenReturn(itemCount);
		when(structure.getEdgeCount()).thenReturn(edgeCount);

		stubItems(structure);

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

	public static final Item ITEM = mock(Item.class);
	public static final DataSequence<Item> ITEM_SEQUENCE = mockSequence(1, ITEM);
	public static final Edge EDGE = mock(Edge.class);
	public static final DataSequence<Edge> EDGE_SEQUENCE = mockSequence(1, EDGE);
}
