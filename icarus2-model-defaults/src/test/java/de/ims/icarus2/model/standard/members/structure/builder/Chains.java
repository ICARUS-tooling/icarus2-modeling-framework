/**
 *
 */
package de.ims.icarus2.model.standard.members.structure.builder;

import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorageTest.ChainConfig;
import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public class Chains {


	static int randomSize() {
		return random(50, 100);
	}

	static PrimitiveIterator.OfInt randomIndices(int spectrum, int size) {
		int[] source = new int[spectrum];
		for (int i = 0; i < source.length; i++) {
			source[i] = i;
		}

		for (int i = 0; i < source.length; i++) {
			int x = TestUtils.random(0, spectrum);
			int tmp = source[i];
			source[i] = source[x];
			source[x] = tmp;
		}

		return IntStream.of(source).limit(size).iterator();
	}

	private static void fillChain(ChainConfig chainConfig, PrimitiveIterator.OfInt nodes,
			int offset, int size, int rootIndex) {
		assertTrue(size>0);
		assertNull(chainConfig.edges[offset]);

		// SPecial treatment of "head" of the chain
		int previous = nodes.nextInt();
		chainConfig.edges[offset] = chainConfig.edge(null, chainConfig.nodes[previous]);
		chainConfig.rootEdges[rootIndex] = chainConfig.edges[offset];
		chainConfig.incoming[previous] = chainConfig.edges[offset];
		chainConfig.depths[previous] = 1;
		chainConfig.heights[previous] = chainConfig.descendants[previous] = size-1;

		// Now randomize the next size-1 elements
		for (int i = 1; i < size; i++) {
			assertNull(chainConfig.edges[offset+i]);

			int next = nodes.nextInt();
			Edge edge = chainConfig.edge(chainConfig.nodes[previous], chainConfig.nodes[next]);
			chainConfig.edges[offset+i] = edge;
			chainConfig.outgoing[previous] = edge;
			chainConfig.incoming[next] = edge;

			chainConfig.heights[next] = chainConfig.descendants[next] = size-i-1;
			chainConfig.depths[next] = i+1;

			previous = next;
		}
	}

	@SuppressWarnings("boxing")
	static ChainConfig singleChain(int size, double fraction) {
		checkArgument(fraction<=1.0);

		ChainConfig chainConfig = ChainConfig.basic(size);
		chainConfig.label = String.format("single chain - %.0f%% full", fraction*100);

		int part = (int) (size * fraction);
		chainConfig.defaultStructure();
		chainConfig.edges = new Edge[part];
		chainConfig.rootEdges = new Edge[1];

		fillChain(chainConfig, randomIndices(size, part), 0, part, 0);

		return chainConfig;
	}

	@SuppressWarnings("boxing")
	static ChainConfig multiChain(int size, double fraction) {
		checkArgument(fraction<=1.0);

		ChainConfig chainConfig = ChainConfig.basic(size);
		chainConfig.label = String.format("multi chain - %.0f%% full", fraction*100);

		int part = (int) (size * fraction);
		int chainCount = random(2, 6);

		chainConfig.defaultStructure();
		chainConfig.multiRoot = true;
		chainConfig.edges = new Edge[part];
		chainConfig.rootEdges = new Edge[chainCount];

		PrimitiveIterator.OfInt nodes = randomIndices(size, part);

		int remaining = part;
		for (int i = 0; i < chainCount; i++) {
			int chainSize = i==chainCount-1 ? remaining : random(1, remaining-chainCount+i+1);
			fillChain(chainConfig, nodes, part-remaining, chainSize, i);
			remaining -= chainSize;
		}
		assertEquals(0, remaining);

		return chainConfig;
	}
}
