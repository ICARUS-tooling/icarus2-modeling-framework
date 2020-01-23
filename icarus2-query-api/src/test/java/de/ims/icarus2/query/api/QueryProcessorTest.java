/**
 *
 */
package de.ims.icarus2.query.api;

import static de.ims.icarus2.test.TestUtils.assertDeepEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ims.icarus2.query.api.IqlQueryGenerator.IncrementalBuild;
import de.ims.icarus2.query.api.iql.IqlExpression;
import de.ims.icarus2.query.api.iql.IqlGroup;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlResult;
import de.ims.icarus2.query.api.iql.IqlSorting;
import de.ims.icarus2.query.api.iql.IqlSorting.Order;
import de.ims.icarus2.query.api.iql.IqlType;
import de.ims.icarus2.query.api.iql.IqlUtils;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
class QueryProcessorTest {

	/**
	 * Test method for {@link de.ims.icarus2.query.api.QueryProcessor#readQuery(de.ims.icarus2.query.api.Query)}.
	 */
	@RandomizedTest
	@TestFactory
	@DisplayName("read incrementally built query")
	Stream<DynamicTest> testReadQuery(RandomGenerator rng) {
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

					QueryProcessor processor = new QueryProcessor();
					IqlQuery query = processor.readQuery(new Query(json));
					assertDeepEqual(null, original, query, json);
				}));

	}

	private static void assertGroup(IqlGroup group,
			String expression, @Nullable String filter,
			String label, @Nullable Object defaultValue) {
		assertThat(group)
			.extracting(IqlGroup::getGroupBy)
			.isNotNull();
		assertThat(group.getGroupBy())
			.extracting(IqlExpression::getContent)
			.isNotNull()
			.isEqualTo(expression);
		assertThat(group)
			.extracting(IqlGroup::getLabel)
			.isEqualTo(label);
		if(filter!=null) {
			assertThat(group.getFilterOn()).isNotEmpty();
			assertThat(group.getFilterOn().get().getContent()).isEqualTo(filter);
		}
		if(defaultValue!=null) {
			assertThat(group.getDefaultValue()).isNotEmpty();
			assertThat(group.getDefaultValue().get().getContent()).isEqualTo(defaultValue);
		}
	}

	@Test
	void testProcessGroupingBare() {
		String rawGrouping = "GROUP BY $token.value LABEL \"tok\"";
		List<IqlGroup> groupings = new QueryProcessor().processGrouping(rawGrouping);
		assertThat(groupings)
			.isNotNull()
			.hasSize(1);
		assertGroup(groupings.get(0), "$token.value", null, "\"tok\"", null);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"123",
			"-123.456",
			"false",
			"true",
			"someVar",
			"@var",
			"$member.func()",
			"\"string\"",
	})
	void testProcessGroupingWithDefaults(String defaultValue) {
		String rawGrouping = "GROUP BY $token.value LABEL \"tok\" DEFAULT "+defaultValue;
		List<IqlGroup> groupings = new QueryProcessor().processGrouping(rawGrouping);
		assertThat(groupings)
			.isNotNull()
			.hasSize(1);
		assertGroup(groupings.get(0), "$token.value", null, "\"tok\"", defaultValue);
	}

	@Test
	void testProcessGroupingWithFilter() {
		String rawGrouping = "GROUP BY $token.value FILTER ON (x*2) > 123 LABEL \"tok\"";
		List<IqlGroup> groupings = new QueryProcessor().processGrouping(rawGrouping);
		assertThat(groupings)
			.isNotNull()
			.hasSize(1);
		assertGroup(groupings.get(0), "$token.value", "(x*2) > 123", "\"tok\"", null);
	}

	@Test
	void testProcessFullGrouping() {
		String rawGrouping = "GROUP BY $token.value FILTER ON (x*2) > 123 LABEL \"tok\" DEFAULT 456";
		List<IqlGroup> groupings = new QueryProcessor().processGrouping(rawGrouping);
		assertThat(groupings)
			.isNotNull()
			.hasSize(1);
		assertGroup(groupings.get(0), "$token.value", "(x*2) > 123", "\"tok\"", "456");
	}

	@Test
	void testStackedGrouping() {
		String rawGrouping = "GROUP BY $token.value LABEL \"tok\", BY @var1 LABEL \"anno\"";
		List<IqlGroup> groupings = new QueryProcessor().processGrouping(rawGrouping);
		assertThat(groupings)
			.isNotNull()
			.hasSize(2);
		assertGroup(groupings.get(0), "$token.value", null, "\"tok\"", null);
		assertGroup(groupings.get(1), "@var1", null, "\"anno\"", null);
	}

	@Test
	void testProcessResultLimit() {
		String rawResult = "LIMIT 123";
		IqlResult result = new IqlResult();
		new QueryProcessor().processResult(rawResult, result);
		assertThat(result.getLimit())
			.isPresent()
			.hasValue(123);
		assertThat(result.isPercent())
			.isFalse();
		assertThat(result.getSortings())
			.isEmpty();
	}

	@Test
	void testProcessResultLimitPercent() {
		String rawResult = "LIMIT 50%";
		IqlResult result = new IqlResult();
		new QueryProcessor().processResult(rawResult, result);
		assertThat(result.getLimit())
			.isPresent()
			.hasValue(50);
		assertThat(result.isPercent())
			.isTrue();
		assertThat(result.getSortings())
			.isEmpty();
	}

	private static void assertSorting(IqlSorting sorting, String expression, Order order) {
		assertThat(sorting.getExpression().getContent()).isEqualTo(expression);
		assertThat(sorting.getOrder()).isEqualTo(order);
	}

	@ParameterizedTest
	@EnumSource(Order.class)
	void testProcessResultSorting(Order order) {
		String rawResult = "ORDER BY $token.value "+order.getLabel();
		IqlResult result = new IqlResult();
		new QueryProcessor().processResult(rawResult, result);
		assertThat(result.getLimit())
			.isEmpty();
		assertThat(result.isPercent())
			.isFalse();
		assertThat(result.getSortings())
			.hasSize(1);
		assertSorting(result.getSortings().get(0), "$token.value", order);
	}

	@Test
	void testProcessStackedResult() {
		String rawResult = "LIMIT 66% ORDER BY exp1 ASC, exp2 DESC, exp3 ASC";
		IqlResult result = new IqlResult();
		new QueryProcessor().processResult(rawResult, result);
		assertThat(result.getLimit())
			.isPresent()
			.hasValue(66);
		assertThat(result.isPercent())
			.isTrue();
		List<IqlSorting> sortings = result.getSortings();
		assertThat(sortings)
			.hasSize(3);
		assertSorting(sortings.get(0), "exp1", Order.ASCENDING);
		assertSorting(sortings.get(1), "exp2", Order.DESCENDING);
		assertSorting(sortings.get(2), "exp3", Order.ASCENDING);
	}

}
