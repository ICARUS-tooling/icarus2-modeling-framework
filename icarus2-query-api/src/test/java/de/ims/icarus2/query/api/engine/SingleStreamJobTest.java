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
package de.ims.icarus2.query.api.engine;

import static de.ims.icarus2.util.lang.Primitives._int;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.query.api.engine.QueryJob.JobController;
import de.ims.icarus2.query.api.engine.QueryUtils.BufferedSingleLaneQueryOutput;
import de.ims.icarus2.query.api.engine.SingleStreamJob.SingleLaneJob;
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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
class SingleStreamJobTest {

	private static class SingleLaneTest {

		private String query;
		private final List<Container> sentences = new ObjectArrayList<>();
		//TODO

		void assertProcess() {

		}
	}


	private static StructurePattern pattern(String query, int id) {

		String payloadString = QueryTestUtils.expand(query);
		IqlPayload payload = new QueryProcessor().processPayload(payloadString);
		assertThat(payload).as("No payload").isNotNull();
		assertThat(payload.getQueryType()).isEqualTo(QueryType.SINGLE_LANE);
		assertThat(payload.getLanes()).as("Missing lane").isNotEmpty();
		IqlLane lane = payload.getLanes().get(0);

		Scope scope = QueryTestUtils.scope();

		RootContext rootContext = EvaluationContext.rootBuilder()
				.corpus(scope.getCorpus())
				.scope(scope)
				.addEnvironment(SharedUtilityEnvironments.all())
				.build();
		LaneContext context = rootContext.derive()
				.lane(lane)
				.build();

		return StructurePattern.builder()
				.id(id)
				.role(Role.SINGLETON)
				.context(context)
				.source(lane)
				.build();
	}

	@Nested
	class ForSingleLane {

		@Test
		public void testSingleWorker() throws Exception {
			String[] sentences = {
					"X----",
					"-X---",
					"--X--",
			};

			StructurePattern pattern = pattern("[$X]", 0);

			QueryInput input = QueryUtils.fixedInput(QueryTestUtils.sentences(sentences));
			BufferedSingleLaneQueryOutput output = QueryUtils.bufferedOutput(pattern.getId());
			ExceptionCollector exceptionHandler = new ExceptionCollector();

			SingleLaneJob job = (SingleLaneJob) SingleStreamJob.builder()
					.addPattern(pattern)
					.input(input)
					.output(output)
					.query(mock(IqlQuery.class)) // not needed for internal workings anyway
					.batchSize(2) //TODO can we verify that the worker had to load twice?
					.exceptionHandler(exceptionHandler)
					.build();

			JobController controller = job.execute(1);
			assertThat(controller.getTotal()).as("Expecting 1 created worker").isEqualTo(1);
			assertThat(controller.getActive()).as("Expecting no active worker yet").isEqualTo(0);

			controller.start();

			assertThat(controller.awaitFinish(10, TimeUnit.SECONDS)).isTrue();
			assertThat(exceptionHandler.hasEntries()).isFalse();

			List<Match> matches = output.getMatches();
			assertThat(matches).hasSize(3);
			for (int i = 0; i < matches.size(); i++) {
				Match match = matches.get(i);
				assertThat(match.getIndex()).isEqualTo(i);
				assertThat(match.getMapCount()).isEqualTo(1);
				assertThat(match.getNode(0)).isEqualTo(0);
				assertThat(match.getIndex(0)).isEqualTo(i);
			}
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

			StructurePattern pattern = pattern("[$X]", 0);

			QueryInput input = QueryUtils.fixedInput(QueryTestUtils.sentences(sentences));
			BufferedSingleLaneQueryOutput output = QueryUtils.bufferedOutput(pattern.getId());
			ExceptionCollector exceptionHandler = new ExceptionCollector();

			SingleLaneJob job = (SingleLaneJob) SingleStreamJob.builder()
					.addPattern(pattern)
					.input(input)
					.output(output)
					.query(mock(IqlQuery.class)) // not needed for internal workings anyway
					.batchSize(2)
					.exceptionHandler(exceptionHandler)
					.build();

			JobController controller = job.execute(workerLimit);
			assertThat(controller.getTotal()).as("Expecting %d created worker", _int(workerLimit)).isEqualTo(workerLimit);
			assertThat(controller.getActive()).as("Expecting no active worker yet").isEqualTo(0);

			controller.start();

			assertThat(controller.awaitFinish(10, TimeUnit.SECONDS)).isTrue();
			assertThat(exceptionHandler.hasEntries()).isFalse();

			List<Match> matches = new ObjectArrayList<>(output.getMatches());
			assertThat(matches).hasSize(sentences.length);

			// Need to sort results due to race between threads
			matches.sort((m1, m2) -> Long.compare(m1.getIndex(), m2.getIndex()));

			for (int i = 0; i < matches.size(); i++) {
				Match match = matches.get(i);
				assertThat(match.getIndex()).isEqualTo(i);
				assertThat(match.getMapCount()).isEqualTo(1);
				assertThat(match.getNode(0)).isEqualTo(0);
				assertThat(match.getIndex(0)).isEqualTo(i);
			}
		}
	}

}
