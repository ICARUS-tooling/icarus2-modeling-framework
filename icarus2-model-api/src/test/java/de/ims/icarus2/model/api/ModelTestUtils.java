/**
 *
 */
package de.ims.icarus2.model.api;

import static de.ims.icarus2.test.TestUtils.assertMock;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.test.util.Pair;

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

		return container;
	}

	private void stubItems(Container container) {
		//TODO
	}

	@SuppressWarnings("boxing")
	public static Structure mockStructure(long itemCount, long edgeCount) {
		checkArgument(itemCount>=0);
		checkArgument(edgeCount>=0);

		Structure structure = mock(Structure.class);
		when(structure.getItemCount()).thenReturn(itemCount);
		when(structure.getEdgeCount()).thenReturn(edgeCount);

		return structure;
	}

	@SuppressWarnings("boxing")
	public static Structure mockStructure(long itemCount, Pair<Long, Long>...edges) {

		Structure structure = mockStructure(itemCount, edges.length);
	}
}
