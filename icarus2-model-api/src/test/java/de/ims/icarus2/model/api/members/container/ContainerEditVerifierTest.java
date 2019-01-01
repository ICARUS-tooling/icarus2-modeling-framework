/**
 *
 */
package de.ims.icarus2.model.api.members.container;

import static de.ims.icarus2.SharedTestUtils.mockSequence;
import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.test.TestUtils.displayString;
import static de.ims.icarus2.test.TestUtils.settings;
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

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public interface ContainerEditVerifierTest<V extends ContainerEditVerifier> {

	Stream<DynamicTest> testEmptyContainer();

	Stream<DynamicTest> testSmallContainer();

	Stream<DynamicTest> testLargeContainer();

	@Provider
	V createContainerEditVerifier(TestSettings settings, Container container);

	@Test
	default void testContainerEditVerifierLifecycle() {
		Container container = mockContainer(0);
		V verifier = createContainerEditVerifier(settings(), container);

		assertEquals(container, verifier.getSource());

		verifier.close();

		assertNull(verifier.getSource());
	}

	public static final Item ITEM = mock(Item.class);

	public static final DataSequence<Item> ITEM_SEQUENCE = mockSequence(1, ITEM);

	@SuppressWarnings("boxing")
	public static List<DynamicTest> createTests(ContainerEditVerifierTestSpec spec) {
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
	public static class ContainerEditVerifierTestSpec {

		final ContainerEditVerifier verifier;

		/**
		 * Legal values for {@link ContainerEditVerifier#canAddItem(long, Item)}
		 */
		final List<Pair<Long, Item>> addSingleLegal = new ArrayList<>();
		/**
		 * Illegal values for {@link ContainerEditVerifier#canAddItem(long, Item)}
		 */
		final List<Pair<Long, Item>> addSingleIllegal = new ArrayList<>();

		/**
		 * Legal values for {@link ContainerEditVerifier#canAddItems(long, de.ims.icarus2.util.collections.seq.DataSequence)}
		 */
		final List<Pair<Long, DataSequence<Item>>> addBatchLegal = new ArrayList<>();
		/**
		 * Illegal values for {@link ContainerEditVerifier#canAddItems(long, de.ims.icarus2.util.collections.seq.DataSequence)}
		 */
		final List<Pair<Long, DataSequence<Item>>> addBatchIllegal = new ArrayList<>();

		/**
		 * Legal values for {@link ContainerEditVerifier#canRemoveItem(long)}
		 */
		final List<Long> removeSingleLegal = new ArrayList<>();
		/**
		 * Illegal values for {@link ContainerEditVerifier#canRemoveItem(long)}
		 */
		final List<Long> removeSingleIllegal = new ArrayList<>();

		/**
		 * Legal values for {@link ContainerEditVerifier#canRemoveItems(long, long)}
		 */
		final List<Pair<Long, Long>> removeBatchLegal = new ArrayList<>();
		/**
		 * Illegal values for {@link ContainerEditVerifier#canRemoveItems(long, long)}
		 */
		final List<Pair<Long, Long>> removeBatchIllegal = new ArrayList<>();

		/**
		 * Legal values for {@link ContainerEditVerifier#canMoveItem(long, long)}
		 */
		final List<Pair<Long, Long>> moveSingleLegal = new ArrayList<>();
		/**
		 * Illegal values for {@link ContainerEditVerifier#canMoveItem(long, long)}
		 */
		final List<Pair<Long, Long>> moveSingleIllegal = new ArrayList<>();

		public ContainerEditVerifierTestSpec(ContainerEditVerifier verifier) {
			this.verifier = requireNonNull(verifier);
		}

		public ContainerEditVerifierTestSpec addSingleLegal(Item item, long...values) {
			for(long index : values) {
				addSingleLegal.add(new Pair<>(index, item));
			}
			return this;
		}

		public ContainerEditVerifierTestSpec addSingleLegal(long...values) {
			return addSingleLegal(ITEM, values);
		}

		public ContainerEditVerifierTestSpec addSingleIllegal(Item item, long...values) {
			for(long index : values) {
				addSingleIllegal.add(new Pair<>(index, item));
			}
			return this;
		}

		public ContainerEditVerifierTestSpec addSingleIllegal(long...values) {
			return addSingleIllegal(ITEM, values);
		}

		public ContainerEditVerifierTestSpec addBatchLegal(DataSequence<Item> items, long...values) {
			for(long index : values) {
				addBatchLegal.add(new Pair<>(index, items));
			}
			return this;
		}

		public ContainerEditVerifierTestSpec addBatchLegal(long...values) {
			return addBatchLegal(ITEM_SEQUENCE, values);
		}

		public ContainerEditVerifierTestSpec addBatchIllegal(DataSequence<Item> items, long...values) {
			for(long index : values) {
				addBatchIllegal.add(new Pair<>(index, items));
			}
			return this;
		}

		public ContainerEditVerifierTestSpec addBatchIllegal(long...values) {
			return addBatchIllegal(ITEM_SEQUENCE, values);
		}

		public ContainerEditVerifierTestSpec removeSingleLegal(long...values) {
			for(long index : values) {
				removeSingleLegal.add(index);
			}
			return this;
		}

		public ContainerEditVerifierTestSpec removeSingleIllegal(long...values) {
			for(long index : values) {
				removeSingleIllegal.add(index);
			}
			return this;
		}

		public ContainerEditVerifierTestSpec removeBatchLegal(
				@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
			Collections.addAll(removeBatchLegal, entries);
			return this;
		}
		public ContainerEditVerifierTestSpec removeBatchLegal(long from, long to) {
			removeBatchLegal.add(new Pair<>(from, to));
			return this;
		}

		public ContainerEditVerifierTestSpec removeBatchIllegal(
				@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
			Collections.addAll(removeBatchIllegal, entries);
			return this;
		}
		public ContainerEditVerifierTestSpec removeBatchIllegal(long from, long to) {
			removeBatchIllegal.add(new Pair<>(from, to));
			return this;
		}

		public ContainerEditVerifierTestSpec moveSingleLegal(long index0, long index1) {
			moveSingleLegal.add(new Pair<>(index0, index1));
			return this;
		}
		public ContainerEditVerifierTestSpec moveSingleLegal(
				@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
			Collections.addAll(moveSingleLegal, entries);
			return this;
		}

		public ContainerEditVerifierTestSpec moveSingleIllegal(long index0, long index1) {
			moveSingleIllegal.add(new Pair<>(index0, index1));
			return this;
		}
		public ContainerEditVerifierTestSpec moveSingleIllegal(
				@SuppressWarnings("unchecked") Pair<Long, Long>...entries) {
			Collections.addAll(moveSingleIllegal, entries);
			return this;
		}

		public Stream<DynamicTest> test() {
			return createTests(this).stream();
		}
	}
}
