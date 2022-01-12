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

import static de.ims.icarus2.util.Conditions.checkState;
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
import java.util.stream.IntStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.query.api.engine.QueryJob.JobController;
import de.ims.icarus2.query.api.engine.QueryOutput.BufferedMatchOutput;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Role;
import de.ims.icarus2.query.api.engine.result.Match;
import de.ims.icarus2.query.api.exp.EvaluationContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.LaneContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.RootContext;
import de.ims.icarus2.query.api.exp.env.SharedUtilityEnvironments;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlPayload;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryType;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
class SingleStreamJobTest {

	private static class SingleLaneTest {

		private String query;
//		private final List<Container> sentences = new ObjectArrayList<>();
		private QueryInput input;
		private QueryOutput output;
		private BiConsumer<Thread, Throwable> exceptionHandler;
		private final List<MatchAsserter> matches = new ObjectArrayList<>();

		private Consumer<QueryOutput> outputAsserter;

		private Boolean sortMatches;
		private Boolean promote;
		private Integer batchSize;
		private Integer workerLimit;
		private Integer timeout;

		SingleLaneTest query(String query) {
			checkState("query already set", this.query==null);
			this.query = requireNonNull(query);
			return this;
		}

//		SingleLaneTest sentences(List<Container> sentences) {
//			this.sentences.addAll(requireNonNull(sentences));
//			return this;
//		}
//
//		SingleLaneTest sentences(Container...sentences) {
//			CollectionUtils.feedItems(this.sentences, requireNonNull(sentences));
//			return this;
//		}

		SingleLaneTest input(QueryInput input) {
			checkState("input already set", this.input==null);
			this.input = requireNonNull(input);
			return this;
		}

		SingleLaneTest output(QueryOutput output) {
			checkState("output already set", this.output==null);
			this.output = requireNonNull(output);
			return this;
		}

		SingleLaneTest exceptionHandler(BiConsumer<Thread, Throwable> exceptionHandler) {
			checkState("exception handler already set", this.exceptionHandler==null);
			this.exceptionHandler = requireNonNull(exceptionHandler);
			return this;
		}

		SingleLaneTest matches(MatchAsserter...matches) {
			CollectionUtils.feedItems(this.matches, requireNonNull(matches));
			return this;
		}

		SingleLaneTest matches(int count, IntFunction<MatchAsserter> asserterGen) {
			IntStream.range(0, count).mapToObj(asserterGen).forEach(matches::add);
			return this;
		}

		SingleLaneTest sortMatches(boolean sortMatches) {
			checkState("'sortMatches' flag already set", this.sortMatches==null);
			this.sortMatches = Boolean.valueOf(sortMatches);
			return this;
		}

		SingleLaneTest promote(boolean promote) {
			checkState("'promote' flag already set", this.promote==null);
			this.promote = Boolean.valueOf(promote);
			return this;
		}

		SingleLaneTest batchSize(int batchSize) {
			checkState("'batchSize' value already set", this.batchSize==null);
			this.batchSize = Integer.valueOf(batchSize);
			return this;
		}

		SingleLaneTest workerLimit(int workerLimit) {
			checkState("'workerLimit' value already set", this.workerLimit==null);
			this.workerLimit = Integer.valueOf(workerLimit);
			return this;
		}

		SingleLaneTest timeout(int timeout) {
			checkState("'timeout' value already set", this.timeout==null);
			this.timeout = Integer.valueOf(timeout);
			return this;
		}

		private boolean isSet(Boolean b) { return b!=null && b.booleanValue(); }

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

			RootContext rootContext = EvaluationContext.rootBuilder(QueryTestUtils.dummyCorpus())
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

		private QueryJob createJob(StructurePattern pattern,
				BiConsumer<Thread, Throwable> exceptionCollector) {
			return SingleStreamJob.builder()
					.addPattern(pattern)
					.input(input)
					.output(output)
					.query(mock(IqlQuery.class)) // not needed for internal workings anyway
					.batchSize(batchSize.intValue())
					.exceptionHandler(exceptionCollector)
					.build();
		}

		void assertProcess()  throws Exception {
			checkState("missing query", query!=null);
			checkState("missing input", input!=null);
			checkState("missing output", output!=null);
//			checkState("missing sentences", !sentences.isEmpty());
			checkState("missing batchSize", batchSize!=null);
			checkState("missing workerLimit", workerLimit!=null);
			checkState("missing timeout", timeout!=null);

			StructurePattern pattern = createPattern();

			List<Pair<Thread, Throwable>> exceptions = new ObjectArrayList<>();
			BiConsumer<Thread, Throwable> exceptionCollector = (thread, ex) -> {
				synchronized (exceptions) {
					exceptions.add(Pair.pair(thread, ex));
				}
			};
			if(exceptionHandler!=null) {
				exceptionCollector = exceptionCollector.andThen(exceptionHandler);
			}

			QueryJob job = createJob(pattern, exceptionCollector);

			JobController controller = job.execute(workerLimit.intValue());
			assertThat(controller.getTotal()).as("Expecting %d created worker(s)", workerLimit).isEqualTo(workerLimit.intValue());
			assertThat(controller.getActive()).as("Expecting no active worker yet").isEqualTo(0);

			controller.start();

			assertThat(controller.awaitFinish(timeout.intValue(), TimeUnit.SECONDS)).isTrue();

			if(!exceptions.isEmpty()) {
				exceptions.forEach(p -> {
					System.err.printf("Error in thread: '%s'%n", p.first.getName());
					p.second.printStackTrace(System.err);
				});

				throw new AssertionError("Search encountered errors: "+exceptions.toString());
			}

			if(outputAsserter!=null) {
				assertThat(matches).as("Superfluous match listing").isEmpty();
				outputAsserter.accept(output);
			} else if(output instanceof BufferedMatchOutput) {
				BufferedMatchOutput bo = (BufferedMatchOutput) output;
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
		}
	}

	private static SingleLaneTest singleTest() { return new SingleLaneTest(); }

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

	private static MatchAsserter match() { return new MatchAsserter(); }

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
				.input(QueryUtils.fixedInput(QueryTestUtils.sentences(sentences)))
				.output(QueryUtils.bufferedOutput(0))
				.matches(sentences.length, i -> match().index(i).mapping(0, i))
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
				.input(QueryUtils.fixedInput(QueryTestUtils.sentences(sentences)))
				.output(QueryUtils.bufferedOutput(0))
				.sortMatches(true)
				.matches(sentences.length, i -> match().index(i).mapping(0, i))
				.assertProcess();
		}
	}

}
