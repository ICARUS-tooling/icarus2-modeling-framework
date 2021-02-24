/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.structure;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
class NodeTests {

	static final int MIN = 3;
	static final int MAX = 10;

	static int randomCount(RandomGenerator rng) {
		return rng.random(MIN, MAX);
	}

	static int fillRandom(NodeInfo info, boolean incoming, RandomGenerator rng) {
		int count = randomCount(rng);
		fill(info, count, incoming);
		return count;
	}

	static void fill(NodeInfo info, int count, boolean incoming) {
		while (count-- > 0) {
			info.addEdge(mockEdge(), incoming);
		}
	}

	static Edge[] randomEdges(RandomGenerator rng) {
		int count = randomCount(rng);
		return edges(count);
	}

	static Edge[] edges(int count) {
		Edge[] edges = new Edge[count];
		for (int i = 0; i < edges.length; i++) {
			edges[i] = mockEdge();
		}
		return edges;
	}

	static void fill(NodeInfo info, Edge[] edges, boolean incoming) {
		for (Edge edge : edges) {
			info.addEdge(edge, incoming);
		}
	}

	static void assertEdgeAt(NodeInfo info, Edge[] edges, boolean incoming) {
		for (int i = 0; i < edges.length; i++) {
			assertEquals(edges[i], info.edgeAt(i, incoming));
		}

		for(int index : new int[]{-1, edges.length, edges.length+1}) {
			assertModelException(ModelErrorCode.MODEL_INDEX_OUT_OF_BOUNDS,
					() -> info.edgeAt(index, incoming));
		}
	}


	static void fillAndAssert(NodeInfo info, Edge[] edges, boolean incoming) {
		fill(info, edges, incoming);
		assertEdgeAt(info, edges, incoming);
	}

	static void fillAndAssert(NodeInfo info, int count, boolean incoming, RandomGenerator rng) {
		Edge[] edges = count==-1 ? randomEdges(rng) : edges(count);
		fill(info, edges, incoming);
		assertEdgeAt(info, edges, incoming);
	}

	static void fillAndAssert(NodeInfo info, int countIn, int countOut, RandomGenerator rng) {
		Edge[] edgesIn = countIn==-1 ? randomEdges(rng) : edges(countIn);
		Edge[] edgesOut = countOut==-1 ? randomEdges(rng) : edges(countOut);
		fill(info, edgesIn, true);
		fill(info, edgesOut, false);
		assertEdgeAt(info, edgesIn, true);
		assertEdgeAt(info, edgesOut, false);
	}

	static void assertEdgeAt(NodeInfo info, List<Edge> edges, boolean incoming) {
		for (int i = 0; i < edges.size(); i++) {
			assertEquals(edges.get(i), info.edgeAt(i, incoming));
		}
	}

	static void removeAndAssert(NodeInfo info, Edge[] edges, boolean incoming, RandomGenerator rng) {
		List<Edge> buffer = list(edges);

		while(!buffer.isEmpty()) {
			int index = rng.random(0, buffer.size());
			Edge edge = buffer.remove(index);
			info.removeEdge(edge, incoming);

			assertEdgeAt(info, buffer, incoming);

			// Make sure repeated attempts to remove fail
			assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					() -> info.removeEdge(edge, incoming));
		}
	}

	static void fillRemoveAndAssert(NodeInfo info, int count, boolean incoming, RandomGenerator rng) {
		Edge[] edges = count==-1 ? randomEdges(rng) : edges(count);
		fill(info, edges, incoming);
		removeAndAssert(info, edges, incoming, rng);
	}

	static void fillRemoveAndAssert(NodeInfo info, int countIn, int countOut, RandomGenerator rng) {
		Edge[] edgesIn = countIn==-1 ? randomEdges(rng) : edges(countIn);
		Edge[] edgesOut = countOut==-1 ? randomEdges(rng) : edges(countOut);
		fill(info, edgesIn, true);
		fill(info, edgesOut, false);

		removeAndAssert(info, edgesIn, true, rng);
		removeAndAssert(info, edgesOut, false, rng);

		assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
				() -> info.removeEdge(mockEdge(), true));
		assertModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
				() -> info.removeEdge(mockEdge(), false));
	}
}
