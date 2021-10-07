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
/**
 *
 */
package de.ims.icarus2.query.api.engine.matcher.mark;

import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.ims.icarus2.query.api.engine.matcher.mark.MarkerTransform.MarkerSetup;
import de.ims.icarus2.query.api.engine.matcher.mark.MarkerTransform.Term;
import de.ims.icarus2.query.api.iql.IqlMarker;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerCall;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerExpression;
import de.ims.icarus2.query.api.iql.IqlMarker.MarkerExpressionType;
import de.ims.icarus2.query.api.iql.IqlType;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
class MarkerTransformTest {

	private static IqlMarkerCall makeCall(String name) {
		return makeCall(name, 0);
	}

	private static IqlMarkerCall makeCall(String name, int args) {
		IqlMarkerCall call = new IqlMarkerCall();
		call.setName(name);
		if(args>0) {
			call.setArguments(IntStream.range(0, args)
					.mapToObj(Integer::valueOf)
					.toArray(Number[]::new));
		}
		return call;
	}

	private static final String[] SEQ_NAMES = Stream.concat(
			Stream.of(HorizontalMarker.Type.values()).map(HorizontalMarker.Type::getSequenceLabel),
			Stream.of(HorizontalMarker.Type.values()).map(HorizontalMarker.Type::getTreeHierarchyLabel)
			).toArray(String[]::new);

	private static final String[] LVL_NAMES = Stream.of(LevelMarker.values())
			.map(LevelMarker::getLabel)
			.toArray(String[]::new);

	private static final String[] GEN_NAMES = Stream.of(GenerationMarker.values())
			.map(GenerationMarker::getLabel)
			.toArray(String[]::new);

	@Nested
	class WithInstance {

		/**
		 * Test the {@link MarkerTransform#scan(de.ims.icarus2.query.api.iql.IqlMarker)} method.
		 */
		@Nested
		class ForScan {

			MarkerTransform transform;

			@BeforeEach
			void setUp() { transform = new MarkerTransform(); }

			@AfterEach
			void tearDown() { transform = null; }

			/**
			 * Expect 'pure' flags for any raw names
			 */
			@Nested
			class ForRawNames {

				@ParameterizedTest
				@EnumSource(HorizontalMarker.Type.class)
				public void testSequenceMarkers(HorizontalMarker.Type type) throws Exception {
					IqlMarker marker = makeCall(type.getSequenceLabel(), type.getArgCount());
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Unrecognized pure call")
						.isTrue();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform._flags(type.getSequenceLabel()));
				}

				@ParameterizedTest
				@EnumSource(HorizontalMarker.Type.class)
				public void testChildMarkers(HorizontalMarker.Type type) throws Exception {
					IqlMarker marker = makeCall(type.getTreeHierarchyLabel(), type.getArgCount());
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Unrecognized pure call")
						.isTrue();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform._flags(type.getTreeHierarchyLabel()));
				}

				@ParameterizedTest
				@EnumSource(LevelMarker.class)
				public void testLevelMarkers(LevelMarker type) throws Exception {
					IqlMarker marker = makeCall(type.getLabel());
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Unrecognized pure call")
						.isTrue();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform._flags(type.getLabel()));
				}

				@ParameterizedTest
				@EnumSource(GenerationMarker.class)
				public void testGenerationMarkers(GenerationMarker type) throws Exception {
					IqlMarker marker = makeCall(type.getLabel(), type.getArgCount());
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Unrecognized pure call")
						.isTrue();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform._flags(type.getLabel()));
				}
			}

			@Nested
			class ForPureCombinations {

				@RandomizedTest
				@Test
				public void testConjunctiveSequenceMarkers(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.and(list(
							makeCall(rng.random(SEQ_NAMES)),
							makeCall(rng.random(SEQ_NAMES)),
							makeCall(rng.random(SEQ_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Unrecognized pure mix")
						.isTrue();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.SEQ);
				}

				@RandomizedTest
				@Test
				public void testDisjunctiveSequenceMarkers(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.or(list(
							makeCall(rng.random(SEQ_NAMES)),
							makeCall(rng.random(SEQ_NAMES)),
							makeCall(rng.random(SEQ_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Unrecognized pure mix")
						.isTrue();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.SEQ);
				}

				@RandomizedTest
				@Test
				public void testConjunctiveLevelMarkers(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.and(list(
							makeCall(rng.random(LVL_NAMES)),
							makeCall(rng.random(LVL_NAMES)),
							makeCall(rng.random(LVL_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Unrecognized pure mix")
						.isTrue();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.LVL);
				}

				@RandomizedTest
				@Test
				public void testDisjunctiveLevelMarkers(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.or(list(
							makeCall(rng.random(LVL_NAMES)),
							makeCall(rng.random(LVL_NAMES)),
							makeCall(rng.random(LVL_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Unrecognized pure mix")
						.isTrue();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.LVL);
				}

				@RandomizedTest
				@Test
				public void testConjunctiveGenerationMarkers(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.and(list(
							makeCall(rng.random(GEN_NAMES)),
							makeCall(rng.random(GEN_NAMES)),
							makeCall(rng.random(GEN_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Unrecognized pure mix")
						.isTrue();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.GEN);
				}

				@RandomizedTest
				@Test
				public void testDisjunctiveGenerationMarkers(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.or(list(
							makeCall(rng.random(GEN_NAMES)),
							makeCall(rng.random(GEN_NAMES)),
							makeCall(rng.random(GEN_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Unrecognized pure mix")
						.isTrue();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.GEN);
				}
			}

			@Nested
			class ForMixedCombinations {

				@RandomizedTest
				@Test
				public void testConjunctiveSeqLvlMix(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.and(list(
							makeCall(rng.random(SEQ_NAMES)),
							makeCall(rng.random(LVL_NAMES)),
							makeCall(rng.random(SEQ_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Faulty pure mix")
						.isFalse();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.SEQ | MarkerTransform.LVL);
				}

				@RandomizedTest
				@Test
				public void testConjunctiveSeqGenMix(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.and(list(
							makeCall(rng.random(SEQ_NAMES)),
							makeCall(rng.random(GEN_NAMES)),
							makeCall(rng.random(SEQ_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Faulty pure mix")
						.isFalse();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.SEQ | MarkerTransform.GEN);
				}

				@RandomizedTest
				@Test
				public void testConjunctiveLvlSeqMix(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.and(list(
							makeCall(rng.random(LVL_NAMES)),
							makeCall(rng.random(SEQ_NAMES)),
							makeCall(rng.random(LVL_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Faulty pure mix")
						.isFalse();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.LVL | MarkerTransform.SEQ);
				}

				@RandomizedTest
				@Test
				public void testConjunctiveLvlGenMix(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.and(list(
							makeCall(rng.random(LVL_NAMES)),
							makeCall(rng.random(GEN_NAMES)),
							makeCall(rng.random(LVL_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Faulty pure mix")
						.isFalse();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.LVL | MarkerTransform.GEN);
				}

				@RandomizedTest
				@Test
				public void testConjunctiveGenSeqMix(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.and(list(
							makeCall(rng.random(GEN_NAMES)),
							makeCall(rng.random(SEQ_NAMES)),
							makeCall(rng.random(GEN_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Faulty pure mix")
						.isFalse();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.GEN | MarkerTransform.SEQ);
				}

				@RandomizedTest
				@Test
				public void testConjunctiveGenLvlMix(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.and(list(
							makeCall(rng.random(GEN_NAMES)),
							makeCall(rng.random(LVL_NAMES)),
							makeCall(rng.random(GEN_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Faulty pure mix")
						.isFalse();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.GEN | MarkerTransform.LVL);
				}

				@RandomizedTest
				@Test
				public void testDisjunctiveSeqLvlMix(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.or(list(
							makeCall(rng.random(SEQ_NAMES)),
							makeCall(rng.random(LVL_NAMES)),
							makeCall(rng.random(SEQ_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Faulty pure mix")
						.isFalse();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.SEQ | MarkerTransform.LVL);
				}

				@RandomizedTest
				@Test
				public void testDisjunctiveSeqGenMix(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.or(list(
							makeCall(rng.random(SEQ_NAMES)),
							makeCall(rng.random(GEN_NAMES)),
							makeCall(rng.random(SEQ_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Faulty pure mix")
						.isFalse();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.SEQ | MarkerTransform.GEN);
				}

				@RandomizedTest
				@Test
				public void testDisjunctiveLvlSeqMix(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.or(list(
							makeCall(rng.random(LVL_NAMES)),
							makeCall(rng.random(SEQ_NAMES)),
							makeCall(rng.random(LVL_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Faulty pure mix")
						.isFalse();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.LVL | MarkerTransform.SEQ);
				}

				@RandomizedTest
				@Test
				public void testDisjunctiveLvlGenMix(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.or(list(
							makeCall(rng.random(LVL_NAMES)),
							makeCall(rng.random(GEN_NAMES)),
							makeCall(rng.random(LVL_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Faulty pure mix")
						.isFalse();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.LVL | MarkerTransform.GEN);
				}

				@RandomizedTest
				@Test
				public void testDisjunctiveGenSeqMix(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.or(list(
							makeCall(rng.random(GEN_NAMES)),
							makeCall(rng.random(SEQ_NAMES)),
							makeCall(rng.random(GEN_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Faulty pure mix")
						.isFalse();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.GEN | MarkerTransform.SEQ);
				}

				@RandomizedTest
				@Test
				public void testDisjunctiveGenLvlMix(RandomGenerator rng) throws Exception {
					IqlMarker marker = IqlMarkerExpression.or(list(
							makeCall(rng.random(GEN_NAMES)),
							makeCall(rng.random(LVL_NAMES)),
							makeCall(rng.random(GEN_NAMES))
					));
					int flags = transform.scan(marker);
					assertThat(MarkerTransform._pure(flags))
						.as("Faulty pure mix")
						.isFalse();
					assertThat(flags)
						.as("Inconsistent name check")
						.isEqualTo(MarkerTransform.GEN | MarkerTransform.LVL);
				}

			}
		}
	}

	@Nested
	class ForToTerm {

		@TestFactory
		public Stream<DynamicTest> testRawSequenceCall() throws Exception {
			return Stream.of(SEQ_NAMES).map(name -> dynamicTest(name, () -> {
				IqlMarkerCall call = makeCall(name);
				MarkerTransform transform = new MarkerTransform();
				transform.scan(call);

				Term term = transform.toTerm(call);
				assertThat(term.isType(MarkerTransform.RAW)).isTrue();
				assertThat(term.isPure()).isTrue();
				assertThat(term.isSet(MarkerTransform.SEQ)).isTrue();
			}));
		}

		@TestFactory
		public Stream<DynamicTest> testRawLevelCall() throws Exception {
			return Stream.of(LVL_NAMES).map(name -> dynamicTest(name, () -> {
				IqlMarkerCall call = makeCall(name);
				MarkerTransform transform = new MarkerTransform();
				transform.scan(call);

				Term term = transform.toTerm(call);
				assertThat(term.isType(MarkerTransform.RAW)).isTrue();
				assertThat(term.isPure()).isTrue();
				assertThat(term.isSet(MarkerTransform.LVL)).isTrue();
			}));
		}

		@TestFactory
		public Stream<DynamicTest> testRawGenerationCall() throws Exception {
			return Stream.of(GEN_NAMES).map(name -> dynamicTest(name, () -> {
				IqlMarkerCall call = makeCall(name);
				MarkerTransform transform = new MarkerTransform();
				transform.scan(call);

				Term term = transform.toTerm(call);
				assertThat(term.isType(MarkerTransform.RAW)).isTrue();
				assertThat(term.isPure()).isTrue();
				assertThat(term.isSet(MarkerTransform.GEN)).isTrue();
			}));
		}

		//TODO test conversion of actual marker expressions
	}

	static Consumer<IqlMarker> is(IqlMarker marker) {
		return m ->  assertThat(m).isSameAs(marker);
	}

	static Consumer<IqlMarker> call(String name) {
		return m -> {
			assertThat(m.getType()).isSameAs(IqlType.MARKER_CALL);
			assertThat(((IqlMarkerCall)m).getName()).isEqualTo(name);
		};
	}

	@SafeVarargs
	static Consumer<IqlMarker> and(Consumer<? super IqlMarker>...elementAsserters) {
		return m -> {
			assertThat(m.getType()).isSameAs(IqlType.MARKER_EXPRESSION);
			IqlMarkerExpression exp = (IqlMarkerExpression)m;
			assertThat(exp.getExpressionType()).isSameAs(MarkerExpressionType.CONJUNCTION);
			List<IqlMarker> elements = exp.getItems();
			assertThat(elements).hasSameSizeAs(elementAsserters);
			for (int i = 0; i < elementAsserters.length; i++) {
				elementAsserters[i].accept(elements.get(i));
			}
		};
	}

	@SafeVarargs
	static Consumer<IqlMarker> or(Consumer<? super IqlMarker>...elementAsserters) {
		return m -> {
			assertThat(m.getType()).isSameAs(IqlType.MARKER_EXPRESSION);
			IqlMarkerExpression exp = (IqlMarkerExpression)m;
			assertThat(exp.getExpressionType()).isSameAs(MarkerExpressionType.DISJUNCTION);
			List<IqlMarker> elements = exp.getItems();
			assertThat(elements).hasSameSizeAs(elementAsserters);
			for (int i = 0; i < elementAsserters.length; i++) {
				elementAsserters[i].accept(elements.get(i));
			}
		};
	}

	/** Tests for actual marker transformation */
	@Nested
	class ForApply {
		//TODO test mixed marker expressions that require serious transformation

		@Test
		@DisplayName("(a | b) & c")
		public void test2v1() throws Exception {
			IqlMarker a = makeCall(SEQ_NAMES[0]);
			IqlMarker b = makeCall(GEN_NAMES[0]);
			IqlMarker c = makeCall(SEQ_NAMES[0]);

			IqlMarker marker = IqlMarkerExpression.and(
					IqlMarkerExpression.or(a, b),
					c
			);

			MarkerTransform transform = new MarkerTransform();

			MarkerSetup[] setups = transform.apply(marker);
			assertThat(setups).hasSize(2);
			and(is(a), is(c)).accept(setups[0].horizontalMarker);
			is(b).accept(setups[1].generationMarker);
			is(c).accept(setups[1].horizontalMarker);
		}

		@Test
		@DisplayName("(a | b) & (c | d)")
		public void test2v2() throws Exception {
			IqlMarker a = makeCall(SEQ_NAMES[0]);
			IqlMarker b = makeCall(GEN_NAMES[0]);
			IqlMarker c = makeCall(SEQ_NAMES[1]);
			IqlMarker d = makeCall(LVL_NAMES[0]);

			IqlMarker marker = IqlMarkerExpression.and(
					IqlMarkerExpression.or(a, b),
					IqlMarkerExpression.or(c, d)
			);

			MarkerTransform transform = new MarkerTransform();

			// (a&c) | (b&c) | (a&d) | (b&d)

			MarkerSetup[] setups = transform.apply(marker);
			assertThat(setups).hasSize(4);
			// a & c
			and(is(a), is(c)).accept(setups[0].horizontalMarker);
			// b & c
			is(b).accept(setups[1].generationMarker);
			is(c).accept(setups[1].horizontalMarker);
			// a & d
			is(a).accept(setups[2].horizontalMarker);
			is(d).accept(setups[2].levelMarker);
			// b & d
			is(b).accept(setups[3].generationMarker);
			is(d).accept(setups[3].levelMarker);
		}
	}
}
