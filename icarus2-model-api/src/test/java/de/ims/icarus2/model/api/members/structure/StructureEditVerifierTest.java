/**
 *
 */
package de.ims.icarus2.model.api.members.structure;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.mockStructure;
import static de.ims.icarus2.test.TestUtils.displayString;
import static de.ims.icarus2.test.TestUtils.settings;
import static de.ims.icarus2.test.util.Pair.pair;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.test.util.Triple;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public interface StructureEditVerifierTest<V extends StructureEditVerifier> {

	Stream<DynamicTest> testEmptyStructure();

	Stream<DynamicTest> testSmallStructure();

	Stream<DynamicTest> testLargeStructure();

	@Provider
	V createStructureEditVerifier(TestSettings settings, Structure structure);

	@Test
	default void testStructureEditVerifierLifecycle() {
		Structure structure = mockStructure(0, 0);
		@SuppressWarnings("resource")
		V verifier = createStructureEditVerifier(settings(), structure);

		assertEquals(structure, verifier.getSource());

		verifier.close();

		assertNull(verifier.getSource());
	}

	public static final Edge EDGE = mock(Edge.class);

	public static final DataSequence<Edge> EDGE_SEQUENCE = mockSequence(1, EDGE);

	@SuppressWarnings("boxing")
	public static List<DynamicTest> createTests(StructureEditVerifierTestSpec spec) {
		List<DynamicTest> tests = new ArrayList<>();

		// SINGLE ADD
		makeTests(spec.addSingleLegal,
				p -> displayString("add single legal: %s", p.first),
				p -> spec.verifier.canAddItem(p.first, p.second), true, tests::add);
		makeTests(spec.addSingleIllegal,
				p -> displayString("add single illegal: %s", p.first),
				p -> spec.verifier.canAddItem(p.first, p.second), false, tests::add);

		// BATCH ADD
		makeTests(spec.addBatchLegal,
				p -> displayString("add batch legal: %s [len=%s]", p.first, p.second.entryCount()),
				p -> spec.verifier.canAddItems(p.first, p.second), true, tests::add);
		makeTests(spec.addBatchIllegal,
				p -> displayString("add batch illegal: %s [len=%s]", p.first, p.second.entryCount()),
				p -> spec.verifier.canAddItems(p.first, p.second), false, tests::add);

		// SINGLE REMOVE
		makeTests(spec.removeSingleLegal,
				idx -> displayString("remove single legal: %s", idx),
				idx -> spec.verifier.canRemoveItem(idx), true, tests::add);
		makeTests(spec.removeSingleIllegal,
				idx -> displayString("remove single illegal: %s", idx),
				idx -> spec.verifier.canRemoveItem(idx), false, tests::add);

		// BATCH REMOVE
		makeTests(spec.removeBatchLegal,
				p -> displayString("remove batch legal: %s to %s", p.first, p.second),
				p -> spec.verifier.canRemoveItems(p.first, p.second), true, tests::add);
		makeTests(spec.removeBatchIllegal,
				p -> displayString("remove batch illegal: %s to %s", p.first, p.second),
				p -> spec.verifier.canRemoveItems(p.first, p.second), false, tests::add);

		// MOVE
		makeTests(spec.moveSingleLegal,
				p -> displayString("move single legal: %s to %s", p.first, p.second),
				p -> spec.verifier.canMoveItem(p.first, p.second), true, tests::add);
		makeTests(spec.moveSingleIllegal,
				p -> displayString("move single illegal: %s to %s", p.first, p.second),
				p -> spec.verifier.canMoveItem(p.first, p.second), false, tests::add);

		// TERMINAL
		makeTests(spec.changeTerminalLegal,
				p -> displayString("set terminal legal: %s to %s", p.first, p.second),
				p -> spec.verifier.canMoveItem(p.first, p.second), true, tests::add);
		makeTests(spec.moveSingleIllegal,
				p -> displayString("move single illegal: %s to %s", p.first, p.second),
				p -> spec.verifier.canMoveItem(p.first, p.second), false, tests::add);
		//TODO

		// CREATE
		//TODO

		return tests;
	}

	public static <E extends Object> void makeTests(List<E> args,
			Function<E, String> labelGen,
			Predicate<? super E> check, boolean expectTrue,
			Consumer<? super DynamicTest> collector) {
		for(E arg : args) {
			collector.accept(DynamicTest.dynamicTest(labelGen.apply(arg), () -> {
				if(expectTrue) {
					assertTrue(check.test(arg));
				} else {
					assertFalse(check.test(arg));
				}
			}));
		}
	}

	@SuppressWarnings("boxing")
	public static class StructureEditVerifierTestSpec {

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
		 * Legal values for {@link StructureEditVerifier#canMoveEdge(long, long)}
		 */
		final List<Pair<Long, Long>> moveSingleLegal = new ArrayList<>();
		/**
		 * Illegal values for {@link StructureEditVerifier#canMoveEdge(long, long)}
		 */
		final List<Pair<Long, Long>> moveSingleIllegal = new ArrayList<>();

		/**
		 * Legal values for {@link StructureEditVerifier#canSetTerminal(Edge, Item, boolean)}
		 */
		final List<Triple<Edge, Long, Boolean>> changeTerminalLegal = new ArrayList<>();
		/**
		 * Illegal values for {@link StructureEditVerifier#canSetTerminal(Edge, Item, boolean)}
		 */
		final List<Triple<Edge, Long, Boolean>> changeTerminalIllegal = new ArrayList<>();

		/**
		 * Illegal values for {@link StructureEditVerifier#canCreateEdge(Item, Item)}
		 * <p>
		 * Note that we store index values here for simplicity. The actual test code translates
		 * them into the respective {@code Item} instances located at those indices.
		 */
		final List<Pair<Long, Long>> createEdgeLegal = new ArrayList<>();

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
			return addSingleLegal(EDGE, values);
		}

		public StructureEditVerifierTestSpec addSingleIllegal(Edge edge, long...values) {
			for(long index : values) {
				addSingleIllegal.add(pair(index, edge));
			}
			return this;
		}

		public StructureEditVerifierTestSpec addSingleIllegal(long...values) {
			return addSingleIllegal(EDGE, values);
		}

		public StructureEditVerifierTestSpec addBatchLegal(DataSequence<Edge> edges, long...values) {
			for(long index : values) {
				addBatchLegal.add(pair(index, edges));
			}
			return this;
		}

		public StructureEditVerifierTestSpec addBatchLegal(long...values) {
			return addBatchLegal(EDGE_SEQUENCE, values);
		}

		public StructureEditVerifierTestSpec addBatchIllegal(DataSequence<Edge> edges, long...values) {
			for(long index : values) {
				addBatchIllegal.add(pair(index, edges));
			}
			return this;
		}

		public StructureEditVerifierTestSpec addBatchIllegal(long...values) {
			return addBatchIllegal(EDGE_SEQUENCE, values);
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

		public StructureEditVerifierTestSpec moveSingleLegal(long index0, long index1) {
			moveSingleLegal.add(pair(index0, index1));
			return this;
		}
		public StructureEditVerifierTestSpec moveSingleLegal(
				@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
			Collections.addAll(moveSingleLegal, entries);
			return this;
		}

		public StructureEditVerifierTestSpec moveSingleIllegal(long index0, long index1) {
			moveSingleIllegal.add(pair(index0, index1));
			return this;
		}
		public StructureEditVerifierTestSpec moveSingleIllegal(
				@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
			Collections.addAll(moveSingleIllegal, entries);
			return this;
		}

		public Stream<DynamicTest> test() {
			return createTests(this).stream();
		}
	}
}
