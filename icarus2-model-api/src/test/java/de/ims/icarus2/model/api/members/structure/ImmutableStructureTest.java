/**
 *
 */
package de.ims.icarus2.model.api.members.structure;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.assertUnsupportedOperation;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.members.container.ImmutableContainerTest;

/**
 * @author Markus Gärtner
 *
 */
public interface ImmutableStructureTest<S extends Structure> extends StructureTest<S>,
		ImmutableContainerTest<S> {

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.structure.Structure#addEdge(de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@Test
	default void testAddEdgeEdge() {
		assertUnsupportedOperation(() ->create().addEdge(mockEdge()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.structure.Structure#addEdge(long, de.ims.icarus2.model.api.members.item.Edge)}.
	 */
	@Test
	default void testAddEdgeLongEdge() {
		assertUnsupportedOperation(() -> create().addEdge(0L, mockEdge()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.structure.Structure#addEdges(de.ims.icarus2.util.collections.seq.DataSequence)}.
	 */
	@Test
	default void testAddEdgesDataSequenceOfQextendsEdge() {
		assertUnsupportedOperation(() -> create().addEdges(mockSequence(mockEdge())));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.structure.Structure#addEdges(long, de.ims.icarus2.util.collections.seq.DataSequence)}.
	 */
	@Test
	default void testAddEdgesLongDataSequenceOfQextendsEdge() {
		assertUnsupportedOperation(() -> create().addEdges(0L, mockSequence(mockEdge())));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.structure.Structure#removeEdge(long)}.
	 */
	@Test
	default void testRemoveEdge() {
		assertUnsupportedOperation(() -> create().removeEdge(0L));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.structure.Structure#removeEdges(long, long)}.
	 */
	@Test
	default void testRemoveEdges() {
		assertUnsupportedOperation(() -> create().removeEdges(0L, 1L));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.structure.Structure#swapEdges(long, long)}.
	 */
	@Test
	default void testSwapEdges() {
		assertUnsupportedOperation(() -> create().swapEdges(0L, 1L));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.structure.Structure#setTerminal(de.ims.icarus2.model.api.members.item.Edge, de.ims.icarus2.model.api.members.item.Item, boolean)}.
	 */
	@Test
	default void testSetTerminal() {
		assertUnsupportedOperation(() -> create().setTerminal(mockEdge(), mockItem(), true));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.members.structure.Structure#newEdge(de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testNewEdge() {
		assertUnsupportedOperation(() -> create().newEdge(mockItem(), mockItem()));
	}

}
