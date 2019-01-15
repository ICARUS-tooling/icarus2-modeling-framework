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
package de.ims.icarus2.model.standard.members.container;

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.displayString;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;

import de.ims.icarus2.model.api.ModelTestUtils;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.collections.seq.DataSequence;

@SuppressWarnings("boxing")
public class ContainerEditVerifierTestBuilder {

	private final ContainerEditVerifier verifier;

	/**
	 * Legal values for {@link ContainerEditVerifier#canAddItem(long, Item)}
	 */
	private final List<Pair<? extends Number, Item>> addSingleLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link ContainerEditVerifier#canAddItem(long, Item)}
	 */
	private final List<Pair<? extends Number, Item>> addSingleIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link ContainerEditVerifier#canAddItems(long, de.ims.icarus2.util.collections.seq.DataSequence)}
	 */
	private final List<Pair<? extends Number, DataSequence<Item>>> addBatchLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link ContainerEditVerifier#canAddItems(long, de.ims.icarus2.util.collections.seq.DataSequence)}
	 */
	private final List<Pair<? extends Number, DataSequence<Item>>> addBatchIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link ContainerEditVerifier#canRemoveItem(long)}
	 */
	private final List<Number> removeSingleLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link ContainerEditVerifier#canRemoveItem(long)}
	 */
	private final List<Number> removeSingleIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link ContainerEditVerifier#canRemoveItems(long, long)}
	 */
	private final List<Pair<? extends Number, ? extends Number>> removeBatchLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link ContainerEditVerifier#canRemoveItems(long, long)}
	 */
	private final List<Pair<? extends Number, ? extends Number>> removeBatchIllegal = new ArrayList<>();

	/**
	 * Legal values for {@link ContainerEditVerifier#canSwapItems(long, long)}
	 */
	private final List<Pair<? extends Number, ? extends Number>> swapSingleLegal = new ArrayList<>();
	/**
	 * Illegal values for {@link ContainerEditVerifier#canSwapItems(long, long)}
	 */
	private final List<Pair<? extends Number, ? extends Number>> swapSingleIllegal = new ArrayList<>();

	public ContainerEditVerifierTestBuilder(ContainerEditVerifier verifier) {
		this.verifier = requireNonNull(verifier);
	}

	/**
	 * @return the verifier
	 */
	public ContainerEditVerifier getVerifier() {
		return verifier;
	}

	public ContainerEditVerifierTestBuilder addSingleLegal(Item item, long...values) {
		for(long index : values) {
			addSingleLegal.add(new Pair<>(index, item));
		}
		return this;
	}

	public ContainerEditVerifierTestBuilder addSingleLegal(long...values) {
		return addSingleLegal(ModelTestUtils.ITEM, values);
	}

	public ContainerEditVerifierTestBuilder addSingleIllegal(Item item, long...values) {
		for(long index : values) {
			addSingleIllegal.add(new Pair<>(index, item));
		}
		return this;
	}

	public ContainerEditVerifierTestBuilder addSingleIllegal(long...values) {
		return addSingleIllegal(ModelTestUtils.ITEM, values);
	}

	public ContainerEditVerifierTestBuilder addBatchLegal(DataSequence<Item> items, long...values) {
		for(long index : values) {
			addBatchLegal.add(new Pair<>(index, items));
		}
		return this;
	}

	public ContainerEditVerifierTestBuilder addBatchLegal(long...values) {
		return addBatchLegal(ModelTestUtils.ITEM_SEQUENCE, values);
	}

	public ContainerEditVerifierTestBuilder addBatchIllegal(DataSequence<Item> items, long...values) {
		for(long index : values) {
			addBatchIllegal.add(new Pair<>(index, items));
		}
		return this;
	}

	public ContainerEditVerifierTestBuilder addBatchIllegal(long...values) {
		return addBatchIllegal(ModelTestUtils.ITEM_SEQUENCE, values);
	}

	public ContainerEditVerifierTestBuilder removeSingleLegal(long...values) {
		for(long index : values) {
			removeSingleLegal.add(index);
		}
		return this;
	}

	public ContainerEditVerifierTestBuilder removeSingleIllegal(long...values) {
		for(long index : values) {
			removeSingleIllegal.add(index);
		}
		return this;
	}

	public <N extends Number> ContainerEditVerifierTestBuilder removeBatchLegal(
			@SuppressWarnings("unchecked") Pair<N, N>...entries) {
		Collections.addAll(removeBatchLegal, entries);
		return this;
	}
	public ContainerEditVerifierTestBuilder removeBatchLegal(long from, long to) {
		removeBatchLegal.add(new Pair<>(from, to));
		return this;
	}

	public <N extends Number> ContainerEditVerifierTestBuilder removeBatchIllegal(
			@SuppressWarnings("unchecked") Pair<N, N>...entries) {
		Collections.addAll(removeBatchIllegal, entries);
		return this;
	}
	public ContainerEditVerifierTestBuilder removeBatchIllegal(long from, long to) {
		removeBatchIllegal.add(new Pair<>(from, to));
		return this;
	}

	public ContainerEditVerifierTestBuilder swapSingleLegal(long index0, long index1) {
		swapSingleLegal.add(new Pair<>(index0, index1));
		return this;
	}
	public <N extends Number> ContainerEditVerifierTestBuilder swapSingleLegal(
			@SuppressWarnings("unchecked") Pair<N, N>...entries) {
		Collections.addAll(swapSingleLegal, entries);
		return this;
	}

	public ContainerEditVerifierTestBuilder swapSingleIllegal(long index0, long index1) {
		swapSingleIllegal.add(new Pair<>(index0, index1));
		return this;
	}
	public <N extends Number> ContainerEditVerifierTestBuilder swapSingleIllegal(
			@SuppressWarnings("unchecked") Pair<N, N>...entries) {
		Collections.addAll(swapSingleIllegal, entries);
		return this;
	}

	public Stream<DynamicTest> createTests() {
		return createTestsForSpec(this).stream();
	}

	public static List<DynamicTest> createTestsForSpec(ContainerEditVerifierTestBuilder spec) {
		List<DynamicTest> tests = new ArrayList<>();

		// SINGLE ADD
		TestUtils.makeTests(spec.addSingleLegal,
				p -> displayString("add single legal: %s", p.first),
				p -> spec.verifier.canAddItem(p.first.longValue(), p.second), true, tests::add);
		TestUtils.makeTests(spec.addSingleIllegal,
				p -> displayString("add single illegal: %s", p.first),
				p -> spec.verifier.canAddItem(p.first.longValue(), p.second), false, tests::add);

		// BATCH ADD
		TestUtils.makeTests(spec.addBatchLegal,
				p -> displayString("add batch legal: %s [len=%s]", p.first, p.second.entryCount()),
				p -> spec.verifier.canAddItems(p.first.longValue(), p.second), true, tests::add);
		TestUtils.makeTests(spec.addBatchIllegal,
				p -> displayString("add batch illegal: %s [len=%s]", p.first, p.second.entryCount()),
				p -> spec.verifier.canAddItems(p.first.longValue(), p.second), false, tests::add);

		// SINGLE REMOVE
		TestUtils.makeTests(spec.removeSingleLegal,
				idx -> displayString("remove single legal: %s", idx),
				idx -> spec.verifier.canRemoveItem(idx.longValue()), true, tests::add);
		TestUtils.makeTests(spec.removeSingleIllegal,
				idx -> displayString("remove single illegal: %s", idx),
				idx -> spec.verifier.canRemoveItem(idx.longValue()), false, tests::add);

		// BATCH REMOVE
		TestUtils.makeTests(spec.removeBatchLegal,
				p -> displayString("remove batch legal: %s to %s", p.first, p.second),
				p -> spec.verifier.canRemoveItems(p.first.longValue(), p.second.longValue()), true, tests::add);
		TestUtils.makeTests(spec.removeBatchIllegal,
				p -> displayString("remove batch illegal: %s to %s", p.first, p.second),
				p -> spec.verifier.canRemoveItems(p.first.longValue(), p.second.longValue()), false, tests::add);

		// MOVE
		TestUtils.makeTests(spec.swapSingleLegal,
				p -> displayString("swap single legal: %s to %s", p.first, p.second),
				p -> spec.verifier.canSwapItems(p.first.longValue(), p.second.longValue()), true, tests::add);
		TestUtils.makeTests(spec.swapSingleIllegal,
				p -> displayString("swap single illegal: %s to %s", p.first, p.second),
				p -> spec.verifier.canSwapItems(p.first.longValue(), p.second.longValue()), false, tests::add);

		return tests;
	}

	public static Stream<DynamicTest> createNullArgumentsTests(ContainerEditVerifier verifier) {
		List<DynamicTest> tests = new ArrayList<>();

		tests.add(DynamicTest.dynamicTest("add single null", () -> assertNPE(
				() -> verifier.canAddItem(0, null))));

		tests.add(DynamicTest.dynamicTest("add batch null", () -> assertNPE(
				() -> verifier.canAddItems(0, null))));

		return tests.stream();
	}
}