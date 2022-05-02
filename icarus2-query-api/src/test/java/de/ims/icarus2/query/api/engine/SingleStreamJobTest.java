/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine;

import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.query.api.engine.QueryJob.JobController;
import de.ims.icarus2.query.api.engine.QueryUtils.BufferedQueryOutput;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Role;
import de.ims.icarus2.query.api.engine.result.Match;
import de.ims.icarus2.query.api.engine.result.Match.MatchType;
import de.ims.icarus2.query.api.engine.result.Match.MultiMatch;
import de.ims.icarus2.query.api.exp.EvaluationContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.LaneContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.RootContext;
import de.ims.icarus2.query.api.exp.EvaluationUtils;
import de.ims.icarus2.query.api.exp.env.SharedUtilityEnvironments;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlPayload;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryType;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
class SingleStreamJobTest {

	protected abstract static class SingleStreamTest<T extends SingleStreamTest<T>> {

			protected String query;
			protected CorpusData corpus;
			protected QueryInput input;
			protected QueryOutput output;
			protected BiConsumer<Thread, Throwable> exceptionHandler;

			protected Consumer<QueryOutput> outputAsserter;

			protected Boolean sortMatches;
			protected Boolean promote;
			protected Integer batchSize;
			protected Integer workerLimit;
			protected Integer timeout;

			@SuppressWarnings("unchecked")
			T thisAsCast() { return (T) this; }

			T query(String query) {
				checkState("query already set", this.query==null);
				this.query = requireNonNull(query);
				return thisAsCast();
			}

			T corpus(CorpusData corpus) {
				checkState("corpus already set", this.corpus==null);
				this.corpus = requireNonNull(corpus);
				return thisAsCast();
			}

			T input(QueryInput input) {
				checkState("input already set", this.input==null);
				this.input = requireNonNull(input);
				return thisAsCast();
			}

			T output(QueryOutput output) {
				checkState("output already set", this.output==null);
				this.output = requireNonNull(output);
				return thisAsCast();
			}

			T sortMatches(boolean sortMatches) {
				checkState("'sortMatches' flag already set", this.sortMatches==null);
				this.sortMatches = Boolean.valueOf(sortMatches);
				return thisAsCast();
			}

			T promote(boolean promote) {
				checkState("'promote' flag already set", this.promote==null);
				this.promote = Boolean.valueOf(promote);
				return thisAsCast();
			}

			T batchSize(int batchSize) {
				checkState("'batchSize' value already set", this.batchSize==null);
				this.batchSize = Integer.valueOf(batchSize);
				return thisAsCast();
			}

			T workerLimit(int workerLimit) {
				checkState("'workerLimit' value already set", this.workerLimit==null);
				this.workerLimit = Integer.valueOf(workerLimit);
				return thisAsCast();
			}

			T timeout(int timeout) {
				checkState("'timeout' value already set", this.timeout==null);
				this.timeout = Integer.valueOf(timeout);
				return thisAsCast();
			}

			T config(Consumer<T> config) {
				config.accept(thisAsCast());
				return thisAsCast();
			}

			protected boolean isSet(Boolean b) { return b!=null && b.booleanValue(); }

			protected abstract QueryJob createJob();

			protected void assertOutput(QueryOutput output) {
				throw new UnsupportedOperationException("no dedicated output assertion provided by subclass");
			}

			void assertProcess()  throws Exception {
				checkState("missing query", query!=null);
				checkState("missing corpus", corpus!=null);
				checkState("missing input", input!=null);
				checkState("missing output", output!=null);
				checkState("missing batchSize", batchSize!=null);
				checkState("missing workerLimit", workerLimit!=null);
				checkState("missing timeout", timeout!=null);

				QueryJob job = createJob();

				JobController controller = job.execute(workerLimit.intValue());
				assertThat(controller.getTotal()).as("Expecting %d created worker(s)", workerLimit).isEqualTo(workerLimit.intValue());
				assertThat(controller.getActive()).as("Expecting no active worker yet").isEqualTo(0);

				controller.start();

				assertThat(controller.awaitFinish(timeout.intValue(), TimeUnit.SECONDS))
					.as("process timed out").isTrue();

				List<Throwable> exceptions = controller.getExceptions();
				if(!exceptions.isEmpty()) {
					exceptions.forEach(p -> p.printStackTrace(System.err));

					throw new AssertionError("Search encountered errors: "+exceptions.toString());
				}

				if(outputAsserter!=null) {
					outputAsserter.accept(output);
				} else  {
					assertOutput(output);
				}
			}
		}

	private static class SingleLaneTest extends SingleStreamTest<SingleLaneTest> {

		protected final List<MatchAsserter> matches = new ObjectArrayList<>();

		SingleLaneTest matches(MatchAsserter...matches) {
			CollectionUtils.feedItems(this.matches, requireNonNull(matches));
			return thisAsCast();
		}

		SingleLaneTest matches(int count, IntFunction<MatchAsserter> asserterGen) {
			IntStream.range(0, count).mapToObj(asserterGen).forEach(matches::add);
			return thisAsCast();
		}

		@Override
		protected void assertOutput(QueryOutput output) {
			assertThat(output).isInstanceOf(BufferedQueryOutput.class);
			BufferedQueryOutput bo = (BufferedQueryOutput) output;
			List<Match> ml = bo.getMatches();
			assertThat(ml).hasSameSizeAs(matches);

			if(isSet(sortMatches)) {
				ml = new ObjectArrayList<>(ml);
				ml.sort((m1, m2) -> Long.compare(m1.getIndex(), m2.getIndex()));
			}

			for (int i = 0; i < ml.size(); i++) {
				matches.get(i).assertMatch(ml.get(i), i);
			}
		}

		private StructurePattern createPattern() {

			String payloadString = query;
			if(isSet(promote)) {
				payloadString = QueryTestUtils.expand(payloadString);
			}
			IqlPayload payload = new QueryProcessor().processPayload(payloadString);
			assertThat(payload).as("No payload").isNotNull();
			assertThat(payload.getQueryType()).isEqualTo(QueryType.SINGLE_LANE);
			assertThat(payload.getLanes()).as("Missing lane").isNotEmpty();
			IqlLane lane = payload.getLanes().get(0);

			MutableInteger id = new MutableInteger(0);
			EvaluationUtils.visitNodes(lane.getElement(), node -> {
				assertThat(node.getMappingId()).isEqualTo(UNSET_INT);
				node.setMappingId(id.getAndIncrement());
			});

			RootContext rootContext = EvaluationContext.rootBuilder(corpus)
					.addEnvironment(SharedUtilityEnvironments.all())
					.build();
			LaneContext context = rootContext.derive()
					.lane(lane)
					.build();

			return StructurePattern.builder()
					.id(0)
					.role(Role.SINGLETON)
					.context(context)
					.source(lane)
					.build();
		}

		private QueryJob createJob(StructurePattern pattern) {
			return SingleStreamJob.builder()
					.addPattern(pattern)
					.input(input)
					.output(output)
					.query(mock(IqlQuery.class)) // not needed for internal workings anyway
					.batchSize(batchSize.intValue())
					.build();
		}

		@Override
		protected QueryJob createJob() {
			return createJob(createPattern());
		}
	}

	/** Prepare a new {@link SingleLaneTest} with a mocked {@link CorpusData} already set. */
	private static SingleLaneTest singleTest() {
		return new SingleLaneTest()
				.corpus(QueryTestUtils.dummyCorpus());
	}

	private static class MultiLaneTest extends SingleStreamTest<MultiLaneTest> {

		protected final List<MatchAsserter[]> matches = new ObjectArrayList<>();

		MultiLaneTest matches(MatchAsserter...matches) {
			this.matches.add(requireNonNull(matches));
			return thisAsCast();
		}

		MultiLaneTest matches(MatchAsserter[][] matches) {
			for (MatchAsserter[] matchAsserters : matches) {
				this.matches.add(matchAsserters);
			}
			return thisAsCast();
		}

		MultiLaneTest matches(int count, IntFunction<MatchAsserter[]> asserterGen) {
			IntStream.range(0, count).mapToObj(asserterGen).forEach(matches::add);
			return thisAsCast();
		}
		@Override
		protected void assertOutput(QueryOutput output) {
			assertThat(output).isInstanceOf(BufferedQueryOutput.class);
			BufferedQueryOutput bo = (BufferedQueryOutput) output;
			List<Match> ml = bo.getMatches();
			assertThat(ml).as("result count mismatch").hasSameSizeAs(matches);

			if(isSet(sortMatches)) {
				//TODO verify if we should actually enable sorting for multi-match results
//				ml = new ObjectArrayList<>(ml);
//				ml.sort((m1, m2) -> Long.compare(m1.getIndex(), m2.getIndex()));
			}

			for (int i = 0; i < ml.size(); i++) {
				assertThat(ml.get(i).getType()).as("type mismatch at match %d", _int(i)).isSameAs(MatchType.MULTI);
				MatchAsserter[] asserters = matches.get(i);
				MultiMatch m = (MultiMatch) ml.get(i);
				assertThat(m.getLaneCount()).as("lane count mismatch in result %d",_int(i)).isEqualTo(asserters.length);
				for (int j = 0; j < asserters.length; j++) {
					m.moveToLane(j);
					asserters[j].assertMatch(m, i);
				}
			}
		}

		private List<StructurePattern> createPatterns() {

			String payloadString = query;
			if(isSet(promote)) {
				payloadString = QueryTestUtils.expand(payloadString);
			}
			IqlPayload payload = new QueryProcessor().processPayload(payloadString);
			assertThat(payload).as("No payload").isNotNull();
			assertThat(payload.getQueryType()).isEqualTo(QueryType.MULTI_LANE);
			List<IqlLane> lanes = payload.getLanes();
			assertThat(lanes).as("Missing lanes").isNotEmpty();

			RootContext rootContext = EvaluationContext.rootBuilder(corpus)
					.addEnvironment(SharedUtilityEnvironments.all())
					.build();

			return IntStream.range(0, lanes.size()).mapToObj(index -> {
				IqlLane lane = lanes.get(index);
				MutableInteger id = new MutableInteger(0);
				EvaluationUtils.visitNodes(lane.getElement(), node -> {
					assertThat(node.getMappingId()).isEqualTo(UNSET_INT);
					node.setMappingId(id.getAndIncrement());
				});
				LaneContext context = rootContext.derive()
						.lane(lane)
						.build();
				return StructurePattern.builder()
						.id(index)
						.role(Role.of(index==0, index==lanes.size()-1))
						.context(context)
						.source(lane)
						.build();
			}).collect(Collectors.toList());
		}

		private QueryJob createJob(List<StructurePattern> patterns) {
			return SingleStreamJob.builder()
					.addPatterns(patterns)
					.input(input)
					.output(output)
					.query(mock(IqlQuery.class)) // not needed for internal workings anyway
					.batchSize(batchSize.intValue())
					.build();
		}

		@Override
		protected QueryJob createJob() {
			return createJob(createPatterns());
		}
	}

	private static MultiLaneTest multiTest() { return new MultiLaneTest(); }

	private static class MatchAsserter {
		private long index = UNSET_LONG;
		private final List<Pair<Integer, Integer>> mapping = new ObjectArrayList<>();

		MatchAsserter index(long index) {
			this.index = index;
			return this;
		}

		MatchAsserter mapping(int mappingId, int index) {
			mapping.add(Pair.pair(mappingId, index));
			return this;
		}

		void assertMatch(Match match, int matchIndex) {
			assertThat(match.getIndex()).as("index mismatch in match %d", _int(matchIndex)).isEqualTo(index);
			assertThat(match.getMapCount()).as("size mismatch in match %d", _int(matchIndex)).isEqualTo(mapping.size());
			for (int i = 0; i < mapping.size(); i++) {
				Pair<Integer, Integer> m = mapping.get(i);
				assertThat(match.getNode(i)).as("Mapping id mismatch at index %d in match %d", _int(i), _int(matchIndex)).isEqualTo(m.first);
				assertThat(match.getIndex(i)).as("Mapped index mismatch at index %d in match %d", _int(i), _int(matchIndex)).isEqualTo(m.second);
			}
		}
	}

	private static MatchAsserter match(long index) { return new MatchAsserter().index(index); }

	@Nested
	class ForSingleLane {

		@Test
		public void testSingleWorker() throws Exception {
			String[] sentences = {
					"X----",
					"-X---",
					"--X--",
			};

			singleTest()
				.query("[$X]")
				.promote(true)
				.batchSize(2)
				.workerLimit(1)
				.timeout(5)
				.input(QueryUtils.fixedInput(_containers(sentences)))
				.output(QueryUtils.bufferedOutput(0))
				.matches(sentences.length, i -> match(i).mapping(0, i))
				.assertProcess();
		}

		@ParameterizedTest
		@ValueSource(ints = {2, 3, 10})
		public void testMultipleWorkers(int workerLimit) throws Exception {
			String[] sentences = {
					"X----",
					"-X---",
					"--X--",
					"---X--",
					"----X--",
					"-----X--",
					"------X--",
					"-------X--",
					"--------X--",
					"---------X--",
			};

			singleTest()
				.query("[$X]")
				.promote(true)
				.batchSize(2)
				.workerLimit(workerLimit)
				.timeout(10)
				.input(QueryUtils.fixedInput(_containers(sentences)))
				.output(QueryUtils.bufferedOutput(0))
				.sortMatches(true)
				.matches(sentences.length, i -> match(i).mapping(0, i))
				.assertProcess();
		}
	}

	private static Container[] _containers(String...sentences) {
		return QueryTestUtils.sentences(sentences);
	}

	/** Fixed lane mapper implementation erases all data on {@link LaneMapper#close()},
	 * so we need to instantiate a fresh mapper for every test run! */
	private static Supplier<LaneMapper> _mapdIdent(int size) {
		return () -> LaneMapper.fixedBuilder()
				.mapIndividual(0, size-1, i -> i)
				.build();
	}

	private static final class Data5 {

		/**X matches at indices 0, 1, 2 */
		private final static String[] SINGLE_X_012 = {"X----", "-X---", "--X--"};
		/**X matches at indices 1 */
		private final static String[] SINGLE_X_1 = {"A----", "-X---", "--A--"};
		/**X matches at indices 0, 2 */
		private final static String[] SINGLE_X_02 = {"X----", "-A---", "--X--"};
		/**Y matches at indices 4, 3, 2 */
		private final static String[] SINGLE_Y_432 = {"----Y", "---Y-", "--Y--"};
		/**Y matches at indices 3 */
		private final static String[] SINGLE_Y_3 = {"----B", "---Y-", "--B--"};
		/**Y matches at indices 4, 2 */
		private final static String[] SINGLE_Y_42 = {"----Y", "---B-", "--Y--"};

		/** Maps indices 0 to 4 via identity */
		private final static Supplier<LaneMapper> MAPPER_3_IDENT = _mapdIdent(3);
		/** Maps indices [0..4] to [4..0] */
		private final static Supplier<LaneMapper> MAPPER_3_CROSS = () -> {
			return LaneMapper.fixedBuilder()
					.mapIndividual(0, 2, i -> 2-i)
					.build();
		};

	}

	private static final class Data6 {

		/**X matches at indices [0,1] [2,3] [4,5] */
		private final static String[] DOUBLE_XXX_DISTINCT = {"XX----", "--XX--", "----XX"};

		/**X matches at indices 1, 2, and 5 */
		private final static String[] SINGLE_Y_125 = {"-Y----", "--Y---", "-----Y"};

		/** Maps indices [0,1] to 0 and 2 to 2 */
		private final static Supplier<LaneMapper> MAPPER_3_NON_OVERLAP = () -> {
			return LaneMapper.fixedBuilder()
					.map(0, 0)
					.map(1, 0)
					.map(2, 2)
					.build();
		};

	}

	@Nested
	class ForMultipleLanes {

		/** Provides expected matchings as pairs of pairs, with each inner pair holding the
		 * sentence index as first value and token index as second. */
		@SuppressWarnings("boxing")
		private Consumer<MultiLaneTest> matches(@SuppressWarnings("unchecked") Pair<Integer, Integer>...matches) {
			assertThat(matches.length % 2).as("match count must be multiple of 2: %d", _int(matches.length)).isEqualTo(0);
			return test -> {
			test.matches(matches.length/2, index -> {
					Pair<Integer, Integer> m1 = matches[index*2];
					Pair<Integer, Integer> m2 = matches[index*2 + 1];
					return new MatchAsserter[] {
						match(m1.first).mapping(0, m1.second),
						match(m2.first).mapping(0, m2.second),
					};
				});
			};
		}

		@Nested
		@TestInstance(Lifecycle.PER_CLASS)
		class For1to1Mapping {

			/** label, query, sent1, sent2, mapper, matches */
			@SuppressWarnings("unchecked")
			Stream<Arguments> args() {

				final List<Arguments> args = new ObjectArrayList<>();

				args.add(Arguments.of(
						"no hits on 2nd lane",
						"LANE sent1 [$X] AND LANE sent2 [$Y]",
						Data5.SINGLE_X_012,
						Data5.SINGLE_X_012,
						Data5.MAPPER_3_IDENT,
						TestUtils.DO_NOTHING()
				));

				args.add(Arguments.of(
						"no hits on 1st lane",
						"LANE sent1 [$X] AND LANE sent2 [$Y]",
						Data5.SINGLE_Y_432,
						Data5.SINGLE_Y_432,
						Data5.MAPPER_3_IDENT,
						TestUtils.DO_NOTHING()
				));

				args.add(Arguments.of(
						"3 hits, identity mapping",
						"LANE sent1 [$X] AND LANE sent2 [$Y]",
						Data5.SINGLE_X_012,
						Data5.SINGLE_Y_432,
						Data5.MAPPER_3_IDENT,
						matches(pair(0, 0), pair(0, 4),
								pair(1, 1), pair(1, 3),
								pair(2, 2), pair(2, 2))
				));

				args.add(Arguments.of(
						"2 distinct hits, filter on 2nd lane",
						"LANE sent1 [$X] AND LANE sent2 [$Y]",
						Data5.SINGLE_X_012,
						Data5.SINGLE_Y_42,
						Data5.MAPPER_3_IDENT,
						matches(pair(0, 0), pair(0, 4),
								pair(2, 2), pair(2, 2))
				));

				args.add(Arguments.of(
						"single hit, filter on 2nd lane",
						"LANE sent1 [$X] AND LANE sent2 [$Y]",
						Data5.SINGLE_X_012,
						Data5.SINGLE_Y_3,
						Data5.MAPPER_3_IDENT,
						matches(pair(1, 1), pair(1, 3))
				));

				args.add(Arguments.of(
						"3 hits, cross mapping",
						"LANE sent1 [$X] AND LANE sent2 [$Y]",
						Data5.SINGLE_X_012,
						Data5.SINGLE_Y_432,
						Data5.MAPPER_3_CROSS,
						matches(pair(0, 0), pair(2, 2),
								pair(1, 1), pair(1, 3),
								pair(2, 2), pair(0, 4))
				));

				args.add(Arguments.of(
						"2 distinct hits, filter on 1st lane",
						"LANE sent1 [$X] AND LANE sent2 [$Y]",
						Data5.SINGLE_X_02,
						Data5.SINGLE_Y_432,
						Data5.MAPPER_3_IDENT,
						matches(pair(0, 0), pair(0, 4),
								pair(2, 2), pair(2, 2))
				));

				args.add(Arguments.of(
						"single hit, filter on 1st lane",
						"LANE sent1 [$X] AND LANE sent2 [$Y]",
						Data5.SINGLE_X_1,
						Data5.SINGLE_Y_432,
						Data5.MAPPER_3_IDENT,
						matches(pair(1, 1), pair(1, 3))
				));

				return args.stream();
			}

			/** @see #singleWorkerArgs() */
			@ParameterizedTest(name = "{index}: {0}")
			@MethodSource("args")
			public void test(String label, String query, String[] sent1, String[] sent2,
					Supplier<LaneMapper> mapper,
					Consumer<MultiLaneTest> config) throws Exception {

				Container[] sentences1 = _containers(sent1);
				Container[] sentences2 = _containers(sent2);

				CorpusData corpus = CorpusData.Virtual.builder()
						.layer("sent1").elements(sentences1).sources("token").commit()
						.layer("sent2").elements(sentences2).sources("token").commit()
						.mapper("sent1", "sent2", mapper.get())
						.build();

				multiTest()
					.query(query)
					.corpus(corpus)
					.promote(true)
					.batchSize(2)
					.workerLimit(1)
					.timeout(5)
					.input(QueryUtils.fixedInput(sentences1))
					.output(QueryUtils.bufferedOutput(0))
					.config(config)
					.assertProcess();
			}

		}

		@Nested
		@TestInstance(Lifecycle.PER_CLASS)
		class ForNto1Mapping {

			/** label, query, sent1, sent2, mapper, matches */
			@SuppressWarnings("unchecked")
			Stream<Arguments> args() {

				final List<Arguments> args = new ObjectArrayList<>();

				args.add(Arguments.of(
						"no hits on 2nd lane",
						"LANE sent1 [$X] AND LANE sent2 [$Y]",
						Data6.DOUBLE_XXX_DISTINCT,
						Data6.DOUBLE_XXX_DISTINCT,
						Data6.MAPPER_3_NON_OVERLAP,
						TestUtils.DO_NOTHING()
				));

				args.add(Arguments.of(
						"no hits on 1st lane",
						"LANE sent1 [$X] AND LANE sent2 [$Y]",
						Data6.SINGLE_Y_125,
						Data6.SINGLE_Y_125,
						Data6.MAPPER_3_NON_OVERLAP,
						TestUtils.DO_NOTHING()
				));

				args.add(Arguments.of(
						"6 hits",
						"LANE sent1 [$X] AND LANE sent2 [$Y]",
						Data6.DOUBLE_XXX_DISTINCT,
						Data6.SINGLE_Y_125,
						Data6.MAPPER_3_NON_OVERLAP,
						matches(pair(0, 0), pair(0, 1),
								pair(0, 1), pair(0, 1),
								pair(1, 2), pair(0, 1),
								pair(1, 3), pair(0, 1),
								pair(2, 4), pair(2, 5),
								pair(2, 5), pair(2, 5))
				));

				//TODO reverse map and fails?

				return args.stream();
			}

			/** @see #singleWorkerArgs() */
			@ParameterizedTest(name = "{index}: {0}")
			@MethodSource("args")
			public void test(String label, String query, String[] sent1, String[] sent2,
					Supplier<LaneMapper> mapper,
					Consumer<MultiLaneTest> config) throws Exception {

				Container[] sentences1 = _containers(sent1);
				Container[] sentences2 = _containers(sent2);

				CorpusData corpus = CorpusData.Virtual.builder()
						.layer("sent1").elements(sentences1).sources("token").commit()
						.layer("sent2").elements(sentences2).sources("token").commit()
						.mapper("sent1", "sent2", mapper.get())
						.build();

				multiTest()
					.query(query)
					.corpus(corpus)
					.promote(true)
					.batchSize(2)
					.workerLimit(1)
					.timeout(5)
					.input(QueryUtils.fixedInput(sentences1))
					.output(QueryUtils.bufferedOutput(0))
					.config(config)
					.assertProcess();
			}

		}
	}
}
