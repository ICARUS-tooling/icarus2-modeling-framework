/**
 *
 */
package de.ims.icarus2.query.api.engine;

import static de.ims.icarus2.test.TestUtils.assertDeepEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ims.icarus2.query.api.IqlQueryGenerator;
import de.ims.icarus2.query.api.Query;
import de.ims.icarus2.query.api.IqlQueryGenerator.IncrementalBuild;
import de.ims.icarus2.query.api.engine.QueryEngine;
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

						QueryEngine engine = QueryEngine.builder().build();
						IqlQuery query = engine.readQuery(new Query(json));
						assertDeepEqual(null, original, query, json);
					}));

		}

	}
}
