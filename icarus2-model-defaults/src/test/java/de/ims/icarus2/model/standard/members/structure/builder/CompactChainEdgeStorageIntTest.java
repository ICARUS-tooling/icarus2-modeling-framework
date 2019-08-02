/**
 *
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockStructure;
import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorage.CompactChainEdgeStorageInt;

/**
 * @author Markus Gärtner
 *
 */
class CompactChainEdgeStorageIntTest implements StaticChainEdgeStorageTest<CompactChainEdgeStorageInt>{

	@Override
	public Class<? extends CompactChainEdgeStorageInt> getTestTargetClass() {
		return CompactChainEdgeStorageInt.class;
	}

	@Override
	public Config createDefaultTestConfiguration(int size) {
		return fullSingleChain(size);
	}

	private static Edge makeEdge(Item source, Item target) {
		requireNonNull(target);

		Edge edge = mockEdge(source, target);
		if(source==null) {
			doReturn("root->"+target).when(edge).toString();
		} else {
			doReturn(source+"->"+target).when(edge).toString();
		}
		return edge;
	}

	@SuppressWarnings("boxing")
	private Config fullSingleChain(int size) {
		Config config = Config.basic(size);
		config.label = "single chain - full";

		config.structure = mockStructure();

		config.edges = new Edge[size];
		config.edges[0] = makeEdge(null, config.nodes[0]);
		config.rootEdges = new Edge[] {config.edges[0]};
		config.depths[0] = 1;
		config.heights[0] = config.descendants[0] = size-1;

		for (int i = 1; i < size; i++) {
			Edge edge = makeEdge(config.nodes[i-1], config.nodes[i]);
			config.edges[i] = edge;
			config.outgoing[i-1] = edge;

			config.heights[i] = config.descendants[i] = size-i-1;
			config.depths[i] = i+1;

			when(config.structure.indexOfItem(config.nodes[i])).thenReturn(Long.valueOf(i));
		}

		System.arraycopy(config.edges, 0, config.incoming, 0, size);

		return config;
	}

	private Config fullMultiChain(int size) {

	}

	private Config partialMultiChain(int size) {

	}

	private Config partialSingleChain(int size) {

	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorageTest#createTestConfigurations()
	 */
	@Override
	public Stream<Config> createTestConfigurations() {
		return Stream.of(
				fullSingleChain(randomSize())//,
//				partialSingleChain(randomSize()),
//				fullMultiChain(randomSize()),
//				partialMultiChain(randomSize())
				);
	}

	@Override
	public CompactChainEdgeStorageInt createFromBuilder(StructureBuilder builder) {

		builder.prepareEdgeBuffer();

		return CompactChainEdgeStorageInt.fromBuilder(builder);
	}

}
