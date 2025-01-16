/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.standard.ModelDefaultsTestUtils.expectErrorType;
import static de.ims.icarus2.model.standard.ModelDefaultsTestUtils.expectNPE;
import static de.ims.icarus2.model.standard.ModelDefaultsTestUtils.makeEdge;
import static de.ims.icarus2.model.standard.ModelDefaultsTestUtils.makeItem;
import static de.ims.icarus2.test.TestUtils.FALSE;
import static de.ims.icarus2.test.TestUtils.TRUE;
import static de.ims.icarus2.test.TestUtils.displayString;
import static de.ims.icarus2.test.util.Pair.pair;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelTestUtils;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.annotations.TestBuilder;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.test.util.Triple;
import de.ims.icarus2.util.collections.seq.ArraySequence;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.seq.ListSequence;

@SuppressWarnings("boxing")
@TestBuilder(StructureEditVerifier.class)
public class StructureEditVerifierTestBuilder {

	public static final int ROOT = ModelTestUtils.ROOT;

	private final StructureEditVerifier verifier;

	/**
	 * Legal values for {@link StructureEditVerifier#canAddEdge(long, Edge)}
	 */
	private final List<Edge> addSingleLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link StructureEditVerifier#canAddEdge(long, Edge)}
	 */
	private final List<Edge> addSingleIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link StructureEditVerifier#canAddEdges(long, DataSequence)}
	 */
	private final List<DataSequence<Edge>> addBatchLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link StructureEditVerifier#canAddEdges(long, DataSequence)}
	 */
	private final List<DataSequence<Edge>> addBatchIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link StructureEditVerifier#canRemoveEdge(long)}
	 */
	private final List<Number> removeSingleLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link StructureEditVerifier#canRemoveEdge(long)}
	 */
	private final List<Number> removeSingleIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link StructureEditVerifier#canRemoveEdges(long, long)}
	 */
	private final List<Pair<? extends Number, ? extends Number>> removeBatchLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link StructureEditVerifier#canRemoveEdges(long, long)}
	 */
	private final List<Pair<? extends Number, ? extends Number>> removeBatchIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link StructureEditVerifier#canSwapEdges(long, long)}
	 */
	private final List<Pair<? extends Number, ? extends Number>> swapSingleLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link StructureEditVerifier#canSwapEdges(long, long)}
	 */
	private final List<Pair<? extends Number, ? extends Number>> swapSingleIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link StructureEditVerifier#canSetTerminal(Edge, Item, boolean)}
	 */
	private final List<Triple<Edge, Item, Boolean>> setTerminalLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link StructureEditVerifier#canSetTerminal(Edge, Item, boolean)}
	 */
	private final List<Triple<Edge, Item, Boolean>> setTerminalIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link StructureEditVerifier#canCreateEdge(Item, Item)}
	 * <p>
	 * Note that we store index values here for simplicity. The actual test code translates
	 * them into the respective {@code Item} instances located at those indices.
	 */
	private final List<Pair<Item, Item>> createEdgeLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link StructureEditVerifier#canCreateEdge(Item, Item)}
	 * <p>
	 * Note that we store index values here for simplicity. The actual test code translates
	 * them into the respective {@code Item} instances located at those indices.
	 */
	private final List<Pair<Item, Item>> createEdgeIllegal = new ArrayList<>();

	/**
	 * Additional calls that are expected to fail with an exception which
	 * is to be evaluated by a custom consumer.
	 */
	private final List<Triple<String, Executable, Consumer<? super Exception>>> fail = new ArrayList<>();

	//TODO add list of Executable calls with expected error types

	public StructureEditVerifierTestBuilder(StructureEditVerifier verifier) {
		this.verifier = requireNonNull(verifier);
	}

	/**
	 * @return the verifier
	 */
	public StructureEditVerifier getVerifier() {
		return verifier;
	}

	private Edge edge(long source, long target) {
		return makeEdge(verifier.getSource(), itemAt(source), itemAt(target));
	}

	/**
	 * Declare legal collection of edges for {@link StructureEditVerifier#canAddEdge(Edge)}
	 * @param edges
	 * @return
	 */
	public StructureEditVerifierTestBuilder addSingleLegal(Edge...edges) {
		Collections.addAll(addSingleLegal, edges);
		return this;
	}

	/**
	 * Declare legal collection of edges for {@link StructureEditVerifier#canAddEdge(Edge)}.
	 * Each pair is translated into a directed edge with terminals {@link Pair#first} as
	 * {@link Edge#getSource()} and {@link Pair#second} as {@link Edge#getTarget()}. Index
	 * values of {@code -1} will be translated to the underlying structure's
	 * {@link Structure#getVirtualRoot() root node}.
	 *
	 * @param edges
	 * @return
	 */
	public <N extends Number> StructureEditVerifierTestBuilder addSingleLegal(
			@SuppressWarnings("unchecked") Pair<N, N>...entries) {
		for(Pair<N, N> entry : entries) {
			addSingleLegal.add(edge(entry.first.longValue(), entry.second.longValue()));
		}
		return this;
	}

	/**
	 * Declare illegal collection of edges for {@link StructureEditVerifier#canAddEdge(Edge)}
	 * @param edges
	 * @return
	 */
	public StructureEditVerifierTestBuilder addSingleIllegal(Edge...edges) {
		Collections.addAll(addSingleIllegal, edges);
		return this;
	}

	/**
	 * Declare illegal collection of edges for {@link StructureEditVerifier#canAddEdge(Edge)}.
	 * Each pair is translated into a directed edge with terminals {@link Pair#first} as
	 * {@link Edge#getSource()} and {@link Pair#second} as {@link Edge#getTarget()}. Index
	 * values of {@code -1} will be translated to the underlying structure's
	 * {@link Structure#getVirtualRoot() root node}.
	 *
	 * @param edges
	 * @return
	 */
	public <N extends Number> StructureEditVerifierTestBuilder addSingleIllegal(
			@SuppressWarnings("unchecked") Pair<N, N>...entries) {
		for(Pair<N, N> entry : entries) {
			addSingleIllegal.add(edge(entry.first.longValue(), entry.second.longValue()));
		}
		return this;
	}

	/**
	 * Declare legal sequences of edges for {@link StructureEditVerifier#canAddEdges(DataSequence)}
	 * @param edges
	 * @return
	 */
	public StructureEditVerifierTestBuilder addBatchLegal(
			@SuppressWarnings("unchecked") DataSequence<Edge>...edges) {
		Collections.addAll(addBatchLegal, edges);
		return this;
	}

	/**
	 * Declare a legal sequence of edges for {@link StructureEditVerifier#canAddEdges(DataSequence)}.
	 * Each pair is translated into a directed edge with terminals {@link Pair#first} as
	 * {@link Edge#getSource()} and {@link Pair#second} as {@link Edge#getTarget()}. Index
	 * values of {@code -1} will be translated to the underlying structure's
	 * {@link Structure#getVirtualRoot() root node}. The entire collection of resulting edges is
	 * then wrapped in a {@link DataSequence}.
	 *
	 * @param edges
	 * @return
	 */
	public <N extends Number> StructureEditVerifierTestBuilder addBatchLegalIndirect(
			@SuppressWarnings("unchecked") Pair<N,N>...entries) {
		List<Edge> edges = new ArrayList<>();
		for(Pair<N, N> entry : entries) {
			edges.add(edge(entry.first.longValue(), entry.second.longValue()));
		}
		addBatchLegal.add(new ListSequence<>(edges));
		return this;
	}

	/**
	 * Declare illegal sequences of edges for {@link StructureEditVerifier#canAddEdges(DataSequence)}
	 * @param edges
	 * @return
	 */
	public StructureEditVerifierTestBuilder addBatchIllegal(
			@SuppressWarnings("unchecked") DataSequence<Edge>...edges) {
		Collections.addAll(addBatchIllegal, edges);
		return this;
	}

	/**
	 * Declare an illegal sequence of edges for {@link StructureEditVerifier#canAddEdges(DataSequence)}.
	 * Each pair is translated into a directed edge with terminals {@link Pair#first} as
	 * {@link Edge#getSource()} and {@link Pair#second} as {@link Edge#getTarget()}. Index
	 * values of {@code -1} will be translated to the underlying structure's
	 * {@link Structure#getVirtualRoot() root node}.The entire collection of resulting edges is then
	 * wrapped in a {@link DataSequence}.
	 *
	 * @param edges
	 * @return
	 */
	public <N extends Number> StructureEditVerifierTestBuilder addBatchIllegalIndirect(
			@SuppressWarnings("unchecked") Pair<N, N>...entries) {
		List<Edge> edges = new ArrayList<>();
		for(Pair<N, N> entry : entries) {
			edges.add(edge(entry.first.longValue(), entry.second.longValue()));
		}
		addBatchIllegal.add(new ListSequence<>(edges));
		return this;
	}

	/**
	 * Declare legal indices for {@link StructureEditVerifier#canRemoveEdge(long)}
	 * @param edges
	 * @return
	 */
	public StructureEditVerifierTestBuilder removeSingleLegal(long...values) {
		for(long index : values) {
			removeSingleLegal.add(index);
		}
		return this;
	}

	/**
	 * Declare illegal indices for {@link StructureEditVerifier#canRemoveEdge(long)}
	 * @param edges
	 * @return
	 */
	public StructureEditVerifierTestBuilder removeSingleIllegal(long...values) {
		for(long index : values) {
			removeSingleIllegal.add(index);
		}
		return this;
	}

	/**
	 * Declare legal index ranges for {@link StructureEditVerifier#canRemoveEdges(long, long)}
	 * @param edges
	 * @return
	 */
	public <N extends Number> StructureEditVerifierTestBuilder removeBatchLegal(
			@SuppressWarnings("unchecked") Pair<N, N>...entries) {
		Collections.addAll(removeBatchLegal, entries);
		return this;
	}
	/**
	 * Declare a single legal index ranges for {@link StructureEditVerifier#canRemoveEdges(long, long)}
	 * @param edges
	 * @return
	 */
	public StructureEditVerifierTestBuilder removeBatchLegal(long from, long to) {
		removeBatchLegal.add(pair(from, to));
		return this;
	}

	/**
	 * Declare illegal index ranges for {@link StructureEditVerifier#canRemoveEdges(long, long)}
	 * @param edges
	 * @return
	 */
	public <N extends Number> StructureEditVerifierTestBuilder removeBatchIllegal(
			@SuppressWarnings("unchecked") Pair<N, N>...entries) {
		Collections.addAll(removeBatchIllegal, entries);
		return this;
	}
	/**
	 * Declare a single illegal index ranges for {@link StructureEditVerifier#canRemoveEdges(long, long)}
	 * @param edges
	 * @return
	 */
	public StructureEditVerifierTestBuilder removeBatchIllegal(long from, long to) {
		removeBatchIllegal.add(pair(from, to));
		return this;
	}

	/**
	 * Declare a single legal index pair for {@link StructureEditVerifier#canSwapEdges(long, long)}
	 * @param edges
	 * @return
	 */
	public StructureEditVerifierTestBuilder swapSingleLegal(long index0, long index1) {
		swapSingleLegal.add(pair(index0, index1));
		return this;
	}
	/**
	 * Declare legal index pairs for {@link StructureEditVerifier#canSwapEdges(long, long)}
	 * @param edges
	 * @return
	 */
	public <N extends Number> StructureEditVerifierTestBuilder swapSingleLegal(
			@SuppressWarnings("unchecked") Pair<N, N>...entries) {
		Collections.addAll(swapSingleLegal, entries);
		return this;
	}

	/**
	 * Declare a single illegal index pair for {@link StructureEditVerifier#canSwapEdges(long, long)}
	 * @param edges
	 * @return
	 */
	public StructureEditVerifierTestBuilder swapSingleIllegal(long index0, long index1) {
		swapSingleIllegal.add(pair(index0, index1));
		return this;
	}
	/**
	 * Declare illegal index pairs for {@link StructureEditVerifier#canSwapEdges(long, long)}
	 * @param edges
	 * @return
	 */
	public <N extends Number> StructureEditVerifierTestBuilder swapSingleIllegal(
			@SuppressWarnings("unchecked") Pair<N, N>...entries) {
		Collections.addAll(swapSingleIllegal, entries);
		return this;
	}

	/**
	 * Declare legal arguments for {@link StructureEditVerifier#canSetTerminal(Edge, Item, boolean)}
	 * @param edges
	 * @return
	 */
	public StructureEditVerifierTestBuilder setTerminalLegal(
			@SuppressWarnings("unchecked") Triple<Edge, Item, Boolean>...entries) {
		Collections.addAll(setTerminalLegal, entries);
		return this;
	}
	/**
	 * Declare illegal arguments for {@link StructureEditVerifier#canSetTerminal(Edge, Item, boolean)}
	 * @param edges
	 * @return
	 */
	public StructureEditVerifierTestBuilder setTerminalIllegal(
			@SuppressWarnings("unchecked") Triple<Edge, Item, Boolean>...entries) {
		Collections.addAll(setTerminalIllegal, entries);
		return this;
	}

	/**
	 * Declare multiple sets of legal arguments for {@link StructureEditVerifier#canSetTerminal(Edge, Item, boolean)}.
	 * In each triple the {@link Triple#first first} element is translated into the edge at that position
	 * in the underlying structure and the {@link Triple#second} element into an item.  Index
	 * values for the item of {@code -1} will be translated to the underlying structure's
	 * {@link Structure#getVirtualRoot() root node}.
	 *
	 * @param edges
	 * @return
	 */
	public <N extends Number> StructureEditVerifierTestBuilder setTerminalLegalIndirect(
			@SuppressWarnings("unchecked") Triple<N, N, Boolean>...entries) {
		Stream.of(entries)
				.map(t ->
				Triple.triple(edgeAt(t.first.longValue()), itemAt(t.second.longValue()), t.third))
				.forEach(setTerminalLegal::add);
		return this;
	}
	/**
	 * Declare multiple sets of illegal arguments for {@link StructureEditVerifier#canSetTerminal(Edge, Item, boolean)}.
	 * In each triple the {@link Triple#first first} element is translated into the edge at that position
	 * in the underlying structure and the {@link Triple#second} element into an item.  Index
	 * values for the item of {@code -1} will be translated to the underlying structure's
	 * {@link Structure#getVirtualRoot() root node}.
	 *
	 * @param edges
	 * @return
	 */
	public <N extends Number> StructureEditVerifierTestBuilder setTerminalIllegalIndirect(
			@SuppressWarnings("unchecked") Triple<N, N, Boolean>...entries) {
		Stream.of(entries)
			.map(t -> Triple.triple(edgeAt(t.first.longValue()), itemAt(t.second.longValue()), t.third))
			.forEach(setTerminalIllegal::add);
		return this;
	}

	/**
	 * Declare legal item pairs for {@link StructureEditVerifier#canCreateEdge(Item, Item)}.
	 *
	 * @param entries
	 * @return
	 */
	public StructureEditVerifierTestBuilder createEdgeLegal(
			@SuppressWarnings("unchecked") Pair<Item, Item>...entries) {
		Collections.addAll(createEdgeLegal, entries);
		return this;
	}
	/**
	 * Declare illegal item pairs for {@link StructureEditVerifier#canCreateEdge(Item, Item)}.
	 *
	 * @param entries
	 * @return
	 */
	public StructureEditVerifierTestBuilder createEdgeIllegal(
			@SuppressWarnings("unchecked") Pair<Item, Item>...entries) {
		Collections.addAll(createEdgeIllegal, entries);
		return this;
	}

	/**
	 * Declare legal item pairs for {@link StructureEditVerifier#canCreateEdge(Item, Item)}
	 * via item indices. Index values of {@code -1} will be translated to the underlying
	 * structure's {@link Structure#getVirtualRoot() root node}.
	 *
	 * @param entries
	 * @return
	 */
	public <N extends Number> StructureEditVerifierTestBuilder createEdgeLegalIndirect(
			@SuppressWarnings("unchecked") Pair<N, N>...entries) {
		Stream.of(entries)
			.map(p -> Pair.pair(itemAt(p.first.longValue()), itemAt(p.second.longValue())))
			.forEach(createEdgeLegal::add);
		return this;
	}
	/**
	 * Declare illegal item pairs for {@link StructureEditVerifier#canCreateEdge(Item, Item)}
	 * via item indices. Index values of {@code -1} will be translated to the underlying
	 * structure's {@link Structure#getVirtualRoot() root node}.
	 *
	 * @param entries
	 * @return
	 */
	public <N extends Number> StructureEditVerifierTestBuilder createEdgeIllegalIndirect(
			@SuppressWarnings("unchecked") Pair<N, N>...entries) {
		Stream.of(entries)
			.map(p -> Pair.pair(itemAt(p.first.longValue()), itemAt(p.second.longValue())))
			.forEach(createEdgeIllegal::add);
		return this;
	}

	/**
	 * Declare tasks that are expected to fail and assert the thrown exception with a
	 * custom {@code exceptionHandler}.
	 *
	 * @param label
	 * @param task
	 * @param exceptionHandler
	 * @return
	 */
	public StructureEditVerifierTestBuilder fail(String label,
			Executable task, Consumer<? super Exception> exceptionHandler) {
		fail.add(Triple.triple(label, task, exceptionHandler));
		return this;
	}
	/**
	 * Declare multiple sets of arguments for {@link #fail(String, Executable, Consumer)}.
	 *
	 * @param entries
	 * @return
	 */
	public StructureEditVerifierTestBuilder fail(
			@SuppressWarnings("unchecked") Triple<String,Executable,Consumer<? super Exception>>...entries) {
		Collections.addAll(fail, entries);
		return this;
	}

	private static final Consumer<? super Exception> EXPECT_ILLEGAL_MEMBER =
			expectErrorType(ModelErrorCode.MODEL_ILLEGAL_MEMBER);


	/**
	 * Creates tests for calling verifier methods with illegal members.
	 * <p>
	 * Note that the supplied {@code verifier} <b>must</b> have an underlying
	 * structure with <b>at least</b> {@code 1} edge whose terminals are also contained!
	 *
	 * @param verifier
	 * @return
	 */
	public StructureEditVerifierTestBuilder failForIllegalMembers() {

		assertTrue(verifier.getSource().getEdgeCount()>0, "must have at least 1 edge for testing");

		Edge edge = verifier.getSource().getEdgeAt(0);

		assertNotNull(edge);
		assertNotNull(edge.getSource());
		assertNotNull(edge.getTarget());

		Edge foreignEdge = mockEdge(mockItem(), mockItem());

		Item foreignItem = mockItem();

		fail("add single redundant", () -> verifier.canAddEdge(edge), EXPECT_ILLEGAL_MEMBER);

		fail("add batch redundant", () -> verifier.canAddEdges(new ArraySequence<>(edge, edge)),
				EXPECT_ILLEGAL_MEMBER);
		fail("add batch foreign", () -> verifier.canAddEdges(new ArraySequence<>(foreignEdge, edge)),
				EXPECT_ILLEGAL_MEMBER);

		fail("set terminal foreign edge", () -> verifier.canSetTerminal(
				foreignEdge, edge.getTarget(), true), EXPECT_ILLEGAL_MEMBER);
		fail("set terminal foreign item", () -> verifier.canSetTerminal(
				edge, foreignItem, true), EXPECT_ILLEGAL_MEMBER);

		fail("create edge foreign source", () -> verifier.canCreateEdge(
				foreignItem, edge.getTarget()), EXPECT_ILLEGAL_MEMBER);
		fail("create edge foreign target", () -> verifier.canCreateEdge(
				edge.getTarget(), foreignItem), EXPECT_ILLEGAL_MEMBER);

		return this;
	}

	public StructureEditVerifierTestBuilder failForNullMembers() {

		fail("add single null", () -> verifier.canAddEdge(null), expectNPE());

		fail("add batch null", () -> verifier.canAddEdges(null), expectNPE());

		fail("set terminal null edge", () -> verifier.canSetTerminal(null,
				makeItem(verifier.getSource()), true), expectNPE());
		fail("set terminal null edge", () -> verifier.canSetTerminal(
				makeEdge(verifier.getSource()), null, true), expectNPE());

		fail("create edge null source", () -> verifier.canCreateEdge(null,
				makeItem(verifier.getSource())), expectNPE());
		fail("create edge null target", (() -> verifier.canCreateEdge(
				makeItem(verifier.getSource()), null)), expectNPE());


		return this;
	}

	Edge edgeAt(long index) {
		return requireNonNull(verifier.getSource().getEdgeAt(index), "missing edge at "+index);
	}

	Item itemAt(long index) {
		Structure structure = verifier.getSource();
		return requireNonNull(index==ROOT ? structure.getVirtualRoot() : structure.getItemAt(index),
				"missing item at "+index);
	}

	/**
	 * Create the default tests according to previous specifications
	 * in the builder. All parameters given to {@code xxxLegal} methods
	 * will be used to create tests that expect the matching {@code canXXX}
	 * method in {@link StructureEditVerifier} to report {@code true} and
	 * {@code false} for the {@code xxxIllegal} parameters.
	 *
	 * @return
	 */
	public Stream<DynamicTest> createTests() {
		return createTestsForSpec(this).stream();
	}

	public static List<DynamicTest> createTestsForSpec(StructureEditVerifierTestBuilder spec) {
		List<DynamicTest> tests = new ArrayList<>();

		// SINGLE ADD
		TestUtils.makeTests(spec.addSingleLegal,
				e -> displayString("add single legal: %s", e),
				e -> spec.verifier.canAddEdge(e), TRUE, tests::add);
		TestUtils.makeTests(spec.addSingleIllegal,
				e -> displayString("add single illegal: %s", e),
				e -> spec.verifier.canAddEdge(e), FALSE, tests::add);

		// BATCH ADD
		TestUtils.makeTests(spec.addBatchLegal,
				s -> displayString("add batch legal: [len=%s]", s.entryCount()),
				s -> spec.verifier.canAddEdges(s), TRUE, tests::add);
		TestUtils.makeTests(spec.addBatchIllegal,
				s -> displayString("add batch illegal: [len=%s]", s.entryCount()),
				s -> spec.verifier.canAddEdges(s), FALSE, tests::add);

		// SINGLE REMOVE
		TestUtils.makeTests(spec.removeSingleLegal,
				idx -> displayString("remove single legal: %s", idx),
				idx -> spec.verifier.canRemoveEdge(idx.longValue()), TRUE, tests::add);
		TestUtils.makeTests(spec.removeSingleIllegal,
				idx -> displayString("remove single illegal: %s", idx),
				idx -> spec.verifier.canRemoveEdge(idx.longValue()), FALSE, tests::add);

		// BATCH REMOVE
		TestUtils.makeTests(spec.removeBatchLegal,
				p -> displayString("remove batch legal: %s to %s", p.first, p.second),
				p -> spec.verifier.canRemoveEdges(p.first.longValue(), p.second.longValue()), TRUE, tests::add);
		TestUtils.makeTests(spec.removeBatchIllegal,
				p -> displayString("remove batch illegal: %s to %s", p.first, p.second),
				p -> spec.verifier.canRemoveEdges(p.first.longValue(), p.second.longValue()), FALSE, tests::add);

		// MOVE
		TestUtils.makeTests(spec.swapSingleLegal,
				p -> displayString("swap single legal: %s to %s", p.first, p.second),
				p -> spec.verifier.canSwapEdges(p.first.longValue(), p.second.longValue()), TRUE, tests::add);
		TestUtils.makeTests(spec.swapSingleIllegal,
				p -> displayString("swap single illegal: %s to %s", p.first, p.second),
				p -> spec.verifier.canSwapEdges(p.first.longValue(), p.second.longValue()), FALSE, tests::add);

		// TERMINAL
		TestUtils.makeTests(spec.setTerminalLegal,
				t -> displayString("set terminal legal: item_%s as %s at %s",
						t.second, label(t.third), t.first),
				t -> spec.verifier.canSetTerminal(t.first, t.second, t.third),
				TRUE, tests::add);
		TestUtils.makeTests(spec.setTerminalIllegal,
				t -> displayString("set terminal illegal: item_%s as %s at %s",
						t.second, label(t.third), t.first),
				t -> spec.verifier.canSetTerminal(t.first, t.second, t.third),
				FALSE, tests::add);

		// CREATE
		TestUtils.makeTests(spec.createEdgeLegal,
				t -> displayString("create edge legal: item_%s to item_%s",
						t.first, t.second),
				t -> spec.verifier.canCreateEdge(t.first, t.second),
						TRUE, tests::add);
		TestUtils.makeTests(spec.createEdgeIllegal,
				t -> displayString("create edge illegal: item_%s to item_%s",
						t.first, t.second),
				t -> spec.verifier.canCreateEdge(t.first, t.second),
				FALSE, tests::add);

		// FAIL
		spec.fail.stream()
			.map(t -> DynamicTest.dynamicTest(t.first, () -> {
				Exception ex = assertThrows(Exception.class, t.second);
				t.third.accept(ex);
			}))
			.forEach(tests::add);

		return tests;
	}

	static String label(boolean isSource) {
		return isSource ? "source" : "target";
	}
}