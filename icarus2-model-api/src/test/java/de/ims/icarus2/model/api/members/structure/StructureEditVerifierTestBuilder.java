/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.model.api.members.structure;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.assertIllegalMember;
import static de.ims.icarus2.model.api.ModelTestUtils.mockEdge;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.displayString;
import static de.ims.icarus2.test.util.Pair.pair;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.test.util.Triple;
import de.ims.icarus2.util.collections.seq.DataSequence;

@SuppressWarnings("boxing")
public class StructureEditVerifierTestBuilder {

	public static final long ROOT = -1L;

	final StructureEditVerifier verifier;

	/**
	 * Legal values for {@link StructureEditVerifier#canAddEdge(long, Edge)}
	 */
	final List<Pair<Long, Edge>> addSingleLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link StructureEditVerifier#canAddEdge(long, Edge)}
	 */
	final List<Pair<Long, Edge>> addSingleIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link StructureEditVerifier#canAddEdges(long, DataSequence)}
	 */
	final List<Pair<Long, DataSequence<Edge>>> addBatchLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link StructureEditVerifier#canAddEdges(long, DataSequence)}
	 */
	final List<Pair<Long, DataSequence<Edge>>> addBatchIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link StructureEditVerifier#canRemoveEdge(long)}
	 */
	final List<Long> removeSingleLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link StructureEditVerifier#canRemoveEdge(long)}
	 */
	final List<Long> removeSingleIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link StructureEditVerifier#canRemoveEdges(long, long)}
	 */
	final List<Pair<Long, Long>> removeBatchLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link StructureEditVerifier#canRemoveEdges(long, long)}
	 */
	final List<Pair<Long, Long>> removeBatchIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link StructureEditVerifier#canSwapEdges(long, long)}
	 */
	final List<Pair<Long, Long>> swapSingleLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link StructureEditVerifier#canSwapEdges(long, long)}
	 */
	final List<Pair<Long, Long>> swapSingleIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link StructureEditVerifier#canSetTerminal(Edge, Item, boolean)}
	 */
	final List<Triple<Edge, Item, Boolean>> setTerminalLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link StructureEditVerifier#canSetTerminal(Edge, Item, boolean)}
	 */
	final List<Triple<Edge, Item, Boolean>> setTerminalIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link StructureEditVerifier#canCreateEdge(Item, Item)}
	 * <p>
	 * Note that we store index values here for simplicity. The actual test code translates
	 * them into the respective {@code Item} instances located at those indices.
	 */
	final List<Pair<Item, Item>> createEdgeLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link StructureEditVerifier#canCreateEdge(Item, Item)}
	 * <p>
	 * Note that we store index values here for simplicity. The actual test code translates
	 * them into the respective {@code Item} instances located at those indices.
	 */
	final List<Pair<Item, Item>> createEdgeIllegal = new ArrayList<>();

	/**
	 * Additional calls that are expected to fail with an exception which
	 * is to be evaluated by a custom consumer.
	 */
	final List<Triple<String, Executable, Consumer<? super Exception>>> fail = new ArrayList<>();

	//TODO add list of Executable calls with expected error types

	public StructureEditVerifierTestBuilder(StructureEditVerifier verifier) {
		this.verifier = requireNonNull(verifier);
	}

	private Edge edge() {
		return mockEdge(verifier.getSource());
	}

	public StructureEditVerifierTestBuilder addSingleLegal(Edge edge, long...values) {
		for(long index : values) {
			addSingleLegal.add(pair(index, edge));
		}
		return this;
	}

	public StructureEditVerifierTestBuilder addSingleLegal(long...values) {
		return addSingleLegal(edge(), values);
	}

	public StructureEditVerifierTestBuilder addSingleIllegal(Edge edge, long...values) {
		for(long index : values) {
			addSingleIllegal.add(pair(index, edge));
		}
		return this;
	}

	public StructureEditVerifierTestBuilder addSingleIllegal(long...values) {
		return addSingleIllegal(edge(), values);
	}

	public StructureEditVerifierTestBuilder addBatchLegal(DataSequence<Edge> edges, long...values) {
		for(long index : values) {
			addBatchLegal.add(pair(index, edges));
		}
		return this;
	}

	public StructureEditVerifierTestBuilder addBatchLegal(long...values) {
		return addBatchLegal(mockSequence(3, edge()), values);
	}

	public StructureEditVerifierTestBuilder addBatchIllegal(DataSequence<Edge> edges, long...values) {
		for(long index : values) {
			addBatchIllegal.add(pair(index, edges));
		}
		return this;
	}

	public StructureEditVerifierTestBuilder addBatchIllegal(long...values) {
		return addBatchIllegal(mockSequence(3, edge()), values);
	}

	public StructureEditVerifierTestBuilder removeSingleLegal(long...values) {
		for(long index : values) {
			removeSingleLegal.add(index);
		}
		return this;
	}

	public StructureEditVerifierTestBuilder removeSingleIllegal(long...values) {
		for(long index : values) {
			removeSingleIllegal.add(index);
		}
		return this;
	}

	public StructureEditVerifierTestBuilder removeBatchLegal(
			@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
		Collections.addAll(removeBatchLegal, entries);
		return this;
	}
	public StructureEditVerifierTestBuilder removeBatchLegal(long from, long to) {
		removeBatchLegal.add(pair(from, to));
		return this;
	}

	public StructureEditVerifierTestBuilder removeBatchIllegal(
			@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
		Collections.addAll(removeBatchIllegal, entries);
		return this;
	}
	public StructureEditVerifierTestBuilder removeBatchIllegal(long from, long to) {
		removeBatchIllegal.add(pair(from, to));
		return this;
	}

	public StructureEditVerifierTestBuilder swapSingleLegal(long index0, long index1) {
		swapSingleLegal.add(pair(index0, index1));
		return this;
	}
	public StructureEditVerifierTestBuilder swapSingleLegal(
			@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
		Collections.addAll(swapSingleLegal, entries);
		return this;
	}

	public StructureEditVerifierTestBuilder swapSingleIllegal(long index0, long index1) {
		swapSingleIllegal.add(pair(index0, index1));
		return this;
	}
	public StructureEditVerifierTestBuilder swapSingleIllegal(
			@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
		Collections.addAll(swapSingleIllegal, entries);
		return this;
	}


	public StructureEditVerifierTestBuilder setTerminalLegal(
			@SuppressWarnings("unchecked") Triple<Edge, Item, Boolean>...entries) {
		Collections.addAll(setTerminalLegal, entries);
		return this;
	}
	public StructureEditVerifierTestBuilder setTerminalIllegal(
			@SuppressWarnings("unchecked") Triple<Edge, Item, Boolean>...entries) {
		Collections.addAll(setTerminalIllegal, entries);
		return this;
	}

	public StructureEditVerifierTestBuilder setTerminalLegalIndirect(
			@SuppressWarnings("unchecked") Triple<Edge, Long, Boolean>...entries) {
		Stream.of(entries)
				.map(t -> Triple.of(t.first, itemAt(t.second), t.third))
				.forEach(setTerminalLegal::add);
		return this;
	}
	public StructureEditVerifierTestBuilder setTerminalIllegalIndirect(
			@SuppressWarnings("unchecked") Triple<Edge, Long, Boolean>...entries) {
		Stream.of(entries)
			.map(t -> Triple.of(t.first, itemAt(t.second), t.third))
			.forEach(setTerminalIllegal::add);
		return this;
	}

	public StructureEditVerifierTestBuilder createEdgeLegal(
			@SuppressWarnings("unchecked") Pair<Item, Item>...entries) {
		Collections.addAll(createEdgeLegal, entries);
		return this;
	}
	public StructureEditVerifierTestBuilder createEdgeIllegal(
			@SuppressWarnings("unchecked") Pair<Item, Item>...entries) {
		Collections.addAll(createEdgeIllegal, entries);
		return this;
	}


	public StructureEditVerifierTestBuilder createEdgeLegalIndirect(
			@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
		Stream.of(entries)
			.map(p -> Pair.pair(itemAt(p.first), itemAt(p.second)))
			.forEach(createEdgeLegal::add);
		return this;
	}
	public StructureEditVerifierTestBuilder createEdgeIllegalIndirect(
			@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
		Stream.of(entries)
			.map(p -> Pair.pair(itemAt(p.first), itemAt(p.second)))
			.forEach(createEdgeIllegal::add);
		return this;
	}

	public StructureEditVerifierTestBuilder fail(String label,
			Executable task, Consumer<? super Exception> exceptionHandler) {
		fail.add(Triple.of(label, task, exceptionHandler));
		return this;
	}
	public StructureEditVerifierTestBuilder fail(
			@SuppressWarnings("unchecked") Triple<String,Executable,Consumer<? super Exception>>...entries) {
		Collections.addAll(fail, entries);
		return this;
	}

	Edge edgeAt(long index) {
		return verifier.getSource().getEdgeAt(index);
	}

	Item itemAt(long index) {
		Structure structure = verifier.getSource();
		return index==ROOT ? structure.getVirtualRoot() : structure.getItemAt(index);
	}

	public Stream<DynamicTest> createTests() {
		return createTestsForSpec(this).stream();
	}

	public static List<DynamicTest> createTestsForSpec(StructureEditVerifierTestBuilder spec) {
		List<DynamicTest> tests = new ArrayList<>();

		// SINGLE ADD
		TestUtils.makeTests(spec.addSingleLegal,
				p -> displayString("add single legal: %s", p.first),
				p -> spec.verifier.canAddEdge(p.first, p.second), true, tests::add);
		TestUtils.makeTests(spec.addSingleIllegal,
				p -> displayString("add single illegal: %s", p.first),
				p -> spec.verifier.canAddEdge(p.first, p.second), false, tests::add);

		// BATCH ADD
		TestUtils.makeTests(spec.addBatchLegal,
				p -> displayString("add batch legal: %s [len=%s]", p.first, p.second.entryCount()),
				p -> spec.verifier.canAddEdges(p.first, p.second), true, tests::add);
		TestUtils.makeTests(spec.addBatchIllegal,
				p -> displayString("add batch illegal: %s [len=%s]", p.first, p.second.entryCount()),
				p -> spec.verifier.canAddEdges(p.first, p.second), false, tests::add);

		// SINGLE REMOVE
		TestUtils.makeTests(spec.removeSingleLegal,
				idx -> displayString("remove single legal: %s", idx),
				idx -> spec.verifier.canRemoveEdge(idx), true, tests::add);
		TestUtils.makeTests(spec.removeSingleIllegal,
				idx -> displayString("remove single illegal: %s", idx),
				idx -> spec.verifier.canRemoveEdge(idx), false, tests::add);

		// BATCH REMOVE
		TestUtils.makeTests(spec.removeBatchLegal,
				p -> displayString("remove batch legal: %s to %s", p.first, p.second),
				p -> spec.verifier.canRemoveEdges(p.first, p.second), true, tests::add);
		TestUtils.makeTests(spec.removeBatchIllegal,
				p -> displayString("remove batch illegal: %s to %s", p.first, p.second),
				p -> spec.verifier.canRemoveEdges(p.first, p.second), false, tests::add);

		// MOVE
		TestUtils.makeTests(spec.swapSingleLegal,
				p -> displayString("swap single legal: %s to %s", p.first, p.second),
				p -> spec.verifier.canSwapEdges(p.first, p.second), true, tests::add);
		TestUtils.makeTests(spec.swapSingleIllegal,
				p -> displayString("swap single illegal: %s to %s", p.first, p.second),
				p -> spec.verifier.canSwapEdges(p.first, p.second), false, tests::add);

		// TERMINAL
		TestUtils.makeTests(spec.setTerminalLegal,
				t -> displayString("set terminal legal: item_%s as %s at %s",
						t.second, label(t.third), t.first),
				t -> spec.verifier.canSetTerminal(t.first, t.second, t.third),
						true, tests::add);
		TestUtils.makeTests(spec.setTerminalIllegal,
				t -> displayString("set terminal illegal: item_%s as %s at %s",
						t.second, label(t.third), t.first),
				t -> spec.verifier.canSetTerminal(t.first, t.second, t.third),
						false, tests::add);

		// CREATE
		TestUtils.makeTests(spec.createEdgeLegal,
				t -> displayString("create edge legal: item_%s to item_%s",
						t.first, t.second),
				t -> spec.verifier.canCreateEdge(t.first, t.second),
						true, tests::add);
		TestUtils.makeTests(spec.createEdgeIllegal,
				t -> displayString("create edge illegal: item_%s to item_%s",
						t.first, t.second),
				t -> spec.verifier.canCreateEdge(t.first, t.second),
						false, tests::add);

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

	public static Stream<DynamicTest> createNullArgumentsTests(StructureEditVerifier verifier) {
		List<DynamicTest> tests = new ArrayList<>();

		tests.add(DynamicTest.dynamicTest("add single null", () -> assertNPE(
				() -> verifier.canAddEdge(0, null))));

		tests.add(DynamicTest.dynamicTest("add batch null", () -> assertNPE(
				() -> verifier.canAddEdges(0, null))));

		tests.add(DynamicTest.dynamicTest("set terminal null edge", () -> assertNPE(
				() -> verifier.canSetTerminal(null, mockItem(), true))));
		tests.add(DynamicTest.dynamicTest("set terminal null edge", () -> assertNPE(
				() -> verifier.canSetTerminal(mockEdge(), null, true))));

		tests.add(DynamicTest.dynamicTest("create edge null source", () -> assertNPE(
				() -> verifier.canCreateEdge(null, mockItem()))));
		tests.add(DynamicTest.dynamicTest("create edge null target", () -> assertNPE(
				() -> verifier.canCreateEdge(mockItem(), null))));

		return tests.stream();
	}

	/**
	 * Creates tests for calling verifier methods with illegal members.
	 * <p>
	 * Note that the supplied {@code verifier} <b>must</b> have an underlying
	 * structure with <b>at least</b> {@code 1} edge whose terminals are also contained!
	 *
	 * @param verifier
	 * @return
	 */
	public static Stream<DynamicTest> createIllegalMemberTests(StructureEditVerifier verifier) {
		List<DynamicTest> tests = new ArrayList<>();

		assertTrue(verifier.getSource().getEdgeCount()>0, "must have at least 1 edge for testing");

		Edge edge = verifier.getSource().getEdgeAt(0);

		tests.add(DynamicTest.dynamicTest("add single redundant", () -> assertIllegalMember(
				() -> verifier.canAddEdge(0, edge))));

		tests.add(DynamicTest.dynamicTest("add batch redundant", () -> assertIllegalMember(
				() -> verifier.canAddEdges(0, mockSequence(mockEdge(), mockEdge(), edge)))));

		tests.add(DynamicTest.dynamicTest("set terminal foreign edge", () -> assertIllegalMember(
				() -> verifier.canSetTerminal(mockEdge(), edge.getTarget(), true))));
		tests.add(DynamicTest.dynamicTest("set terminal foreign item", () -> assertIllegalMember(
				() -> verifier.canSetTerminal(edge, mockItem(), true))));

		tests.add(DynamicTest.dynamicTest("create edge foreign source", () -> assertIllegalMember(
				() -> verifier.canCreateEdge(mockItem(), edge.getTarget()))));
		tests.add(DynamicTest.dynamicTest("create edge foreign target", () -> assertIllegalMember(
				() -> verifier.canCreateEdge(edge.getTarget(), mockItem()))));

		return tests.stream();
	}
}