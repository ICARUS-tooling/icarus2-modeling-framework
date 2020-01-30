/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.test.TestUtils.assertDeepEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.query.api.IqlQueryGenerator;
import de.ims.icarus2.query.api.IqlQueryGenerator.IncrementalBuild;
import de.ims.icarus2.query.api.Query;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlType;
import de.ims.icarus2.query.api.iql.IqlUtils;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
class QueryEngineTest {

	@Nested
	class Internal {

		@Test
		void builder() {
			assertThat(QueryEngine.builder()).isNotNull();
		}
	}


	@Nested
	class ReadQuery {

		/**
		 * Test method for {@link QueryEngine#readQuery(de.ims.icarus2.query.api.Query)}.
		 */
		@RandomizedTest
		@TestFactory
		@DisplayName("read incrementally built query")
		Stream<DynamicTest> testIncremental(RandomGenerator rng) {
			IqlQueryGenerator generator = new IqlQueryGenerator(rng);
			IncrementalBuild<IqlQuery> build = generator.build(IqlType.QUERY, IqlQueryGenerator.config());
			ObjectMapper mapper = IqlUtils.createMapper();

			return IntStream.rangeClosed(0, build.getChangeCount())
					.mapToObj(i -> dynamicTest(build.currentLabel(), () -> {
						if(i>0) {
							assertThat(build.applyNextChange()).isTrue();
						}
						IqlQuery original = build.getInstance();
						String json = mapper.writeValueAsString(original);

						QueryEngine engine = QueryEngine.builder()
								.mapper(mapper)
								.corpusManager(mock(CorpusManager.class))
								.build();
						IqlQuery query = engine.readQuery(new Query(json));
						assertDeepEqual(null, original, query, json);
					}));

		}

	}
}
