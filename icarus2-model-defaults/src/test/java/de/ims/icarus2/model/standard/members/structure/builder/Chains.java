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
import de.ims.icarus2.model.standard.members.structure.builder.StaticChainEdgeStorageTest.Config;
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

	private static void fillChain(Config config, PrimitiveIterator.OfInt nodes,
			int offset, int size, int rootIndex) {
		assertTrue(size>0);
		assertNull(config.edges[offset]);

		// SPecial treatment of "head" of the chain
		int previous = nodes.nextInt();
		config.edges[offset] = config.edge(null, config.nodes[previous]);
		config.rootEdges[rootIndex] = config.edges[offset];
		config.incoming[previous] = config.edges[offset];
		config.depths[previous] = 1;
		config.heights[previous] = config.descendants[previous] = size-1;

		// Now randomize the next size-1 elements
		for (int i = 1; i < size; i++) {
			assertNull(config.edges[offset+i]);

			int next = nodes.nextInt();
			Edge edge = config.edge(config.nodes[previous], config.nodes[next]);
			config.edges[offset+i] = edge;
			config.outgoing[previous] = edge;
			config.incoming[next] = edge;

			config.heights[next] = config.descendants[next] = size-i-1;
			config.depths[next] = i+1;

			previous = next;
		}
	}

	@SuppressWarnings("boxing")
	static Config singleChain(int size, double fraction) {
		checkArgument(fraction<=1.0);

		Config config = Config.basic(size);
		config.label = String.format("single chain - %.0f%% full", fraction*100);

		int part = (int) (size * fraction);
		config.defaultStructure();
		config.edges = new Edge[part];
		config.rootEdges = new Edge[1];

		fillChain(config, randomIndices(size, part), 0, part, 0);

		return config;
	}

	@SuppressWarnings("boxing")
	static Config multiChain(int size, double fraction) {
		checkArgument(fraction<=1.0);

		Config config = Config.basic(size);
		config.label = String.format("multi chain - %.0f%% full", fraction*100);

		int part = (int) (size * fraction);
		int chainCount = random(2, 6);

		config.defaultStructure();
		config.multiRoot = true;
		config.edges = new Edge[part];
		config.rootEdges = new Edge[chainCount];

		PrimitiveIterator.OfInt nodes = randomIndices(size, part);

		int remaining = part;
		for (int i = 0; i < chainCount; i++) {
			int chainSize = i==chainCount-1 ? remaining : random(1, remaining-chainCount+i+1);
			fillChain(config, nodes, part-remaining, chainSize, i);
			remaining -= chainSize;
		}
		assertEquals(0, remaining);

		return config;
	}
}
