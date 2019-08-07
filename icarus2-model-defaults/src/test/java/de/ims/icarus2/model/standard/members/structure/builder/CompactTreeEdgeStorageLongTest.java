/**
 *
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorage.CompactTreeEdgeStorageLong;
import de.ims.icarus2.test.annotations.TestLocalOnly;

/**
 * @author Markus Gärtner
 *
 */
class CompactTreeEdgeStorageLongTest implements StaticTreeEdgeStorageTest<CompactTreeEdgeStorageLong> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends CompactTreeEdgeStorageLong> getTestTargetClass() {
		return CompactTreeEdgeStorageLong.class;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticTreeEdgeStorageTest#createFromBuilder(de.ims.icarus2.model.standard.members.structure.builder.StructureBuilder)
	 */
	@Override
	public CompactTreeEdgeStorageLong createFromBuilder(StructureBuilder builder) {
		return CompactTreeEdgeStorageLong.fromBuilder(builder);
	}


	@Nested
	class EdgeCases {

		@Test
		@TestLocalOnly
		void testMaxChainSize() {
			int size = CompactTreeEdgeStorageLong.MAX_NODE_COUNT;
			ChainsAndTrees.TreeConfig treeConfig = ChainsAndTrees.singleTree(size, 1.0,
					CompactTreeEdgeStorageLong.MAX_HEIGHT, UNSET_INT);
			CompactTreeEdgeStorageLong tree = createFromBuilder(toBuilder(treeConfig));
			assertEquals(size, tree.getEdgeCount(treeConfig.structure));
		}

		@SuppressWarnings("boxing")
		@Test
		void testOverflowNodeCount() {
			StructureBuilder builder = mock(StructureBuilder.class);
			when(builder.getNodeCount()).thenReturn(CompactTreeEdgeStorageLong.MAX_NODE_COUNT+1);
			assertThrows(IllegalArgumentException.class,
					() -> createFromBuilder(builder));
		}

		@SuppressWarnings("boxing")
		@Test
		void testOverflowEdgeCount() {
			StructureBuilder builder = mock(StructureBuilder.class);
			when(builder.getNodeCount()).thenReturn(CompactTreeEdgeStorageLong.MAX_NODE_COUNT);
			when(builder.getEdgeCount()).thenReturn(CompactTreeEdgeStorageLong.MAX_EDGE_COUNT+1);
			assertThrows(IllegalArgumentException.class,
					() -> createFromBuilder(builder));
		}

		@SuppressWarnings("boxing")
		@Test
		void testOverflowHeight() {
			StructureBuilder builder = mock(StructureBuilder.class);
			when(builder.getNodeCount()).thenReturn(CompactTreeEdgeStorageLong.MAX_NODE_COUNT);
			when(builder.getEdgeCount()).thenReturn(CompactTreeEdgeStorageLong.MAX_EDGE_COUNT);
			EdgeBuffer edgeBuffer = mock(EdgeBuffer.class);
			when(edgeBuffer.getMaxHeight()).thenReturn(CompactTreeEdgeStorageLong.MAX_HEIGHT+1);
			when(edgeBuffer.getMaxDepth()).thenReturn(CompactTreeEdgeStorageLong.MAX_DEPTH);

			when(builder.edgeBuffer()).thenReturn(edgeBuffer);

			assertThrows(IllegalArgumentException.class,
					() -> createFromBuilder(builder));
		}

		@SuppressWarnings("boxing")
		@Test
		void testOverflowDepth() {
			StructureBuilder builder = mock(StructureBuilder.class);
			when(builder.getNodeCount()).thenReturn(CompactTreeEdgeStorageLong.MAX_NODE_COUNT);
			when(builder.getEdgeCount()).thenReturn(CompactTreeEdgeStorageLong.MAX_EDGE_COUNT);
			EdgeBuffer edgeBuffer = mock(EdgeBuffer.class);
			when(edgeBuffer.getMaxHeight()).thenReturn(CompactTreeEdgeStorageLong.MAX_HEIGHT);
			when(edgeBuffer.getMaxDepth()).thenReturn(CompactTreeEdgeStorageLong.MAX_DEPTH+1);

			when(builder.edgeBuffer()).thenReturn(edgeBuffer);

			assertThrows(IllegalArgumentException.class,
					() -> createFromBuilder(builder));
		}
	}
}
