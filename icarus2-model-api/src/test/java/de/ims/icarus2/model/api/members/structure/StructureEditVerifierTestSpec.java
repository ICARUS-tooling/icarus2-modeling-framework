/**
 *
 */
package de.ims.icarus2.model.api.members.structure;

import static de.ims.icarus2.test.TestUtils.displayString;
import static de.ims.icarus2.test.util.Pair.pair;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;

import de.ims.icarus2.model.api.ModelTestUtils;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.test.util.Triple;
import de.ims.icarus2.util.collections.seq.DataSequence;

@SuppressWarnings("boxing")
public class StructureEditVerifierTestSpec {

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
	final List<Triple<Edge, Long, Boolean>> setTerminalLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link StructureEditVerifier#canSetTerminal(Edge, Item, boolean)}
	 */
	final List<Triple<Edge, Long, Boolean>> setTerminalIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link StructureEditVerifier#canCreateEdge(Item, Item)}
	 * <p>
	 * Note that we store index values here for simplicity. The actual test code translates
	 * them into the respective {@code Item} instances located at those indices.
	 */
	final List<Pair<Long, Long>> createEdgeLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link StructureEditVerifier#canCreateEdge(Item, Item)}
	 * <p>
	 * Note that we store index values here for simplicity. The actual test code translates
	 * them into the respective {@code Item} instances located at those indices.
	 */
	final List<Pair<Long, Long>> createEdgeIllegal = new ArrayList<>();

	public StructureEditVerifierTestSpec(StructureEditVerifier verifier) {
		this.verifier = requireNonNull(verifier);
	}

	public StructureEditVerifierTestSpec addSingleLegal(Edge edge, long...values) {
		for(long index : values) {
			addSingleLegal.add(pair(index, edge));
		}
		return this;
	}

	public StructureEditVerifierTestSpec addSingleLegal(long...values) {
		return addSingleLegal(ModelTestUtils.EDGE, values);
	}

	public StructureEditVerifierTestSpec addSingleIllegal(Edge edge, long...values) {
		for(long index : values) {
			addSingleIllegal.add(pair(index, edge));
		}
		return this;
	}

	public StructureEditVerifierTestSpec addSingleIllegal(long...values) {
		return addSingleIllegal(ModelTestUtils.EDGE, values);
	}

	public StructureEditVerifierTestSpec addBatchLegal(DataSequence<Edge> edges, long...values) {
		for(long index : values) {
			addBatchLegal.add(pair(index, edges));
		}
		return this;
	}

	public StructureEditVerifierTestSpec addBatchLegal(long...values) {
		return addBatchLegal(ModelTestUtils.EDGE_SEQUENCE, values);
	}

	public StructureEditVerifierTestSpec addBatchIllegal(DataSequence<Edge> edges, long...values) {
		for(long index : values) {
			addBatchIllegal.add(pair(index, edges));
		}
		return this;
	}

	public StructureEditVerifierTestSpec addBatchIllegal(long...values) {
		return addBatchIllegal(ModelTestUtils.EDGE_SEQUENCE, values);
	}

	public StructureEditVerifierTestSpec removeSingleLegal(long...values) {
		for(long index : values) {
			removeSingleLegal.add(index);
		}
		return this;
	}

	public StructureEditVerifierTestSpec removeSingleIllegal(long...values) {
		for(long index : values) {
			removeSingleIllegal.add(index);
		}
		return this;
	}

	public StructureEditVerifierTestSpec removeBatchLegal(
			@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
		Collections.addAll(removeBatchLegal, entries);
		return this;
	}
	public StructureEditVerifierTestSpec removeBatchLegal(long from, long to) {
		removeBatchLegal.add(pair(from, to));
		return this;
	}

	public StructureEditVerifierTestSpec removeBatchIllegal(
			@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
		Collections.addAll(removeBatchIllegal, entries);
		return this;
	}
	public StructureEditVerifierTestSpec removeBatchIllegal(long from, long to) {
		removeBatchIllegal.add(pair(from, to));
		return this;
	}

	public StructureEditVerifierTestSpec swapSingleLegal(long index0, long index1) {
		swapSingleLegal.add(pair(index0, index1));
		return this;
	}
	public StructureEditVerifierTestSpec swapSingleLegal(
			@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
		Collections.addAll(swapSingleLegal, entries);
		return this;
	}

	public StructureEditVerifierTestSpec swapSingleIllegal(long index0, long index1) {
		swapSingleIllegal.add(pair(index0, index1));
		return this;
	}
	public StructureEditVerifierTestSpec swapSingleIllegal(
			@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
		Collections.addAll(swapSingleIllegal, entries);
		return this;
	}

	public StructureEditVerifierTestSpec setTerminalLegal(
			@SuppressWarnings("unchecked") Triple<Edge, Long, Boolean>...entries) {
		Collections.addAll(setTerminalLegal, entries);
		return this;
	}
	public StructureEditVerifierTestSpec setTerminalIllegal(
			@SuppressWarnings("unchecked") Triple<Edge, Long, Boolean>...entries) {
		Collections.addAll(setTerminalIllegal, entries);
		return this;
	}

	public StructureEditVerifierTestSpec createEdgeLegal(
			@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
		Collections.addAll(createEdgeLegal, entries);
		return this;
	}
	public StructureEditVerifierTestSpec createEdgeIllegal(
			@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
		Collections.addAll(createEdgeIllegal, entries);
		return this;
	}

	Edge edgeAt(long index) {
		return verifier.getSource().getEdgeAt(index);
	}

	Item itemAt(long index) {
		return verifier.getSource().getItemAt(index);
	}

	public Stream<DynamicTest> createTests() {
		return createTestsForSpec(this).stream();
	}

	public static List<DynamicTest> createTestsForSpec(StructureEditVerifierTestSpec spec) {
		List<DynamicTest> tests = new ArrayList<>();

		// SINGLE ADD
		TestUtils.makeTests(spec.addSingleLegal,
				p -> displayString("add single legal: %s", p.first),
				p -> spec.verifier.canAddItem(p.first, p.second), true, tests::add);
		TestUtils.makeTests(spec.addSingleIllegal,
				p -> displayString("add single illegal: %s", p.first),
				p -> spec.verifier.canAddItem(p.first, p.second), false, tests::add);

		// BATCH ADD
		TestUtils.makeTests(spec.addBatchLegal,
				p -> displayString("add batch legal: %s [len=%s]", p.first, p.second.entryCount()),
				p -> spec.verifier.canAddItems(p.first, p.second), true, tests::add);
		TestUtils.makeTests(spec.addBatchIllegal,
				p -> displayString("add batch illegal: %s [len=%s]", p.first, p.second.entryCount()),
				p -> spec.verifier.canAddItems(p.first, p.second), false, tests::add);

		// SINGLE REMOVE
		TestUtils.makeTests(spec.removeSingleLegal,
				idx -> displayString("remove single legal: %s", idx),
				idx -> spec.verifier.canRemoveItem(idx), true, tests::add);
		TestUtils.makeTests(spec.removeSingleIllegal,
				idx -> displayString("remove single illegal: %s", idx),
				idx -> spec.verifier.canRemoveItem(idx), false, tests::add);

		// BATCH REMOVE
		TestUtils.makeTests(spec.removeBatchLegal,
				p -> displayString("remove batch legal: %s to %s", p.first, p.second),
				p -> spec.verifier.canRemoveItems(p.first, p.second), true, tests::add);
		TestUtils.makeTests(spec.removeBatchIllegal,
				p -> displayString("remove batch illegal: %s to %s", p.first, p.second),
				p -> spec.verifier.canRemoveItems(p.first, p.second), false, tests::add);

		// MOVE
		TestUtils.makeTests(spec.swapSingleLegal,
				p -> displayString("swap single legal: %s to %s", p.first, p.second),
				p -> spec.verifier.canSwapItems(p.first, p.second), true, tests::add);
		TestUtils.makeTests(spec.swapSingleIllegal,
				p -> displayString("swap single illegal: %s to %s", p.first, p.second),
				p -> spec.verifier.canSwapItems(p.first, p.second), false, tests::add);

		// TERMINAL
		TestUtils.makeTests(spec.setTerminalLegal,
				p -> displayString("set terminal legal: item_%d as %s at %s",
						p.second, p.first, label(p.third)),
				p -> spec.verifier.canSetTerminal(p.first, spec.itemAt(p.second), p.third),
						true, tests::add);
		TestUtils.makeTests(spec.setTerminalIllegal,
				p -> displayString("set terminal illegal: item_%d as %s at %s",
						p.second, p.first, label(p.third)),
				p -> spec.verifier.canSetTerminal(p.first, spec.itemAt(p.second), p.third),
						false, tests::add);

		// CREATE
		TestUtils.makeTests(spec.createEdgeLegal,
				p -> displayString("create edge legal: item_%d to item_%d",
						p.first, p.second),
				p -> spec.verifier.canCreateEdge(spec.itemAt(p.first), spec.itemAt(p.second)),
						true, tests::add);
		TestUtils.makeTests(spec.createEdgeIllegal,
				p -> displayString("create edge illegal: item_%d to item_%d",
						p.first, p.second),
				p -> spec.verifier.canCreateEdge(spec.itemAt(p.first), spec.itemAt(p.second)),
						false, tests::add);

		return tests;
	}

	static String label(boolean isSource) {
		return isSource ? "source" : "target";
	}
}