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

import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.Report;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.Report.Severity;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.iql.IqlBinding;
import de.ims.icarus2.query.api.iql.IqlConstraint;
import de.ims.icarus2.query.api.iql.IqlConstraint.BooleanOperation;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlPredicate;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlTerm;
import de.ims.icarus2.query.api.iql.IqlElement;
import de.ims.icarus2.query.api.iql.IqlElement.EdgeType;
import de.ims.icarus2.query.api.iql.IqlElement.IqlEdge;
import de.ims.icarus2.query.api.iql.IqlElement.IqlGrouping;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement;
import de.ims.icarus2.query.api.iql.IqlElement.IqlSet;
import de.ims.icarus2.query.api.iql.IqlElement.IqlTreeNode;
import de.ims.icarus2.query.api.iql.IqlExpression;
import de.ims.icarus2.query.api.iql.IqlGroup;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlLane.LaneType;
import de.ims.icarus2.query.api.iql.IqlPayload;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryType;
import de.ims.icarus2.query.api.iql.IqlQuantifier;
import de.ims.icarus2.query.api.iql.IqlQuantifier.QuantifierType;
import de.ims.icarus2.query.api.iql.IqlReference;
import de.ims.icarus2.query.api.iql.IqlResult;
import de.ims.icarus2.query.api.iql.IqlSorting;
import de.ims.icarus2.query.api.iql.IqlSorting.Order;
import de.ims.icarus2.query.api.iql.NodeArrangement;
import de.ims.icarus2.test.util.Pair;

/**
 * @author Markus Gärtner
 *
 */
class QueryProcessorTest {

	private void assertExpression(IqlExpression expression, String content) {
		assertThat(expression.getContent()).isEqualTo(content);
	}

	private void assertExpression(Optional<IqlExpression> expression, String content) {
		assertThat(expression).isNotEmpty();
		assertExpression(expression.get(), content);
	}

	private String qt(String s) {
		return '\''+s+'\'';
	}

	@Nested
	class ForPayloadErrors {

		private Report<ReportItem> expectReport(String rawPayload) {
			QueryProcessingException exception = assertThrows(QueryProcessingException.class,
					() -> new QueryProcessor(false).processPayload(rawPayload));
			assertThat(exception.getErrorCode()).isSameAs(QueryErrorCode.REPORT);
			return exception.getReport();
		}

		@SafeVarargs
		private final void assertReportHasErrors(Report<ReportItem> report,
				Pair<QueryErrorCode, List<Predicate<? super ReportItem>>>...entries) {
			assertThat(report.countErrors()).isEqualTo(entries.length);
			assertReportEntries(report, Severity.ERROR, list(entries));
		}

		@SafeVarargs
		private final void assertReportHasWarnings(Report<ReportItem> report,
				Pair<QueryErrorCode, List<Predicate<? super ReportItem>>>...entries) {
			assertThat(report.countWarnings()).isEqualTo(entries.length);
			assertReportEntries(report, Severity.WARNING, list(entries));
		}

		private void assertReportEntries(Report<ReportItem> report, Severity severity,
				List<Pair<QueryErrorCode, List<Predicate<? super ReportItem>>>> expected) {

			List<ReportItem> items = report.getItems()
					.stream()
					.filter(item -> item.getSeverity()==severity)
					.collect(Collectors.toList());

			for(Pair<QueryErrorCode, List<Predicate<? super ReportItem>>> matcher : expected) {
				ReportItem found = null;
				search : for (int i = 0; i < items.size(); i++) {
					ReportItem item = items.get(i);
					if(item.getCode()==matcher.first) {
						for(Predicate<? super ReportItem> pred : matcher.second) {
							if(!pred.test(item)) {
								// Exit early
								continue search;
							}
						}
						found = item;
						items.remove(i);
						break search;
					}
				}

				if(found==null) {
					fail("Missing entry with error code "+matcher.first);
				}
			}
		}

		private Predicate<? super ReportItem> msgEnds(String suffix) {
			return item -> item.getMessage().endsWith(suffix);
		}

		private Predicate<? super ReportItem> msgContains(String content) {
			return item -> item.getMessage().contains(content);
		}

		@ParameterizedTest
		@CsvSource(delimiter=';', value={
			"FIND ((x>0));((x>0))",
			"FIND [][((x>0))][];((x>0))",
			"FIND [][][] HAVING ((x>0));((x>0))",
		})
		void testSuperfluousExpressionNesting(String rawPayload, String offendingPart) {
			assertReportHasWarnings(expectReport(rawPayload),
					pair(QueryErrorCode.SUPERFLUOUS_DECLARATION, list(msgEnds(qt(offendingPart)))));
		}

		@ParameterizedTest
		@CsvSource(delimiter=';', value={
			"FIND 0-[];0-",
			"FIND []0-[][];0-",
			"FIND [0-[]];0-",
			"FIND []---0-[];0-",
		})
		void testInvalidAtMostQuantifier(String rawPayload, String offendingPart) {
			assertReportHasErrors(expectReport(rawPayload),
					pair(QueryErrorCode.INVALID_LITERAL, list(msgEnds(qt(offendingPart)))));
		}

		@ParameterizedTest
		@ValueSource(strings = {
				"FIND ORDERED [![]]",
		})
		void testInsufficientNodesForAlignment(String rawPayload) {
			assertReportHasWarnings(expectReport(rawPayload),
					pair(QueryErrorCode.INCORRECT_USE, list(msgContains("existentially"))));
		}

		@ParameterizedTest
		@CsvSource(delimiter=';', value={
				"FIND []-->[],[[]];GRAPH;[[]]",
				"FIND []-->[],[],4+[],[[]],[];GRAPH;[[]]",
				"FIND []-->[],3-[x<5],[[]],1..2[]--[rel==\"dom\"]->[];GRAPH;[[]]",
		})
		void testUnexpectedChildren(String rawPayload, String type, String offendingPart) {
			assertReportHasErrors(expectReport(rawPayload),
					pair(QueryErrorCode.UNSUPPORTED_FEATURE,
							list(msgContains(type),
									msgContains("mix tree and graph features"),
									msgEnds(qt(offendingPart)))));
		}

		@ParameterizedTest
		@CsvSource(delimiter=';', value={
				"FIND [[]], []-->[];TREE;[]-->[]",
				"FIND [],[[]],4+[],[]-->[],[];TREE;[]-->[]",
				"FIND [[]![x()]],[]-->[],3-[$t: pos!=\"NN\" 4+[]];TREE;[]-->[]",
		})
		void testUnexpectedEdge(String rawPayload, String type, String offendingPart) {
			assertReportHasErrors(expectReport(rawPayload),
					pair(QueryErrorCode.UNSUPPORTED_FEATURE,
							list(msgContains(type),
									msgContains("mix tree and graph features"),
									msgEnds(qt(offendingPart)))));
		}

		@ParameterizedTest
		@CsvSource(delimiter=';', value={
				"FIND 4..3[];4..3",
				"FIND 2..2[];2..2",
				"FIND <4..3>[];4..3",
				"FIND [[] 4..3[]];4..3",
				"FIND [], 4..3[]-->[], 5-[];4..3",
		})
		void testInvalidQuantifierRange(String rawPayload, String offendingPart) {
			assertReportHasErrors(expectReport(rawPayload),
					pair(QueryErrorCode.INVALID_LITERAL, list(msgEnds(qt(offendingPart)))));
		}

		@ParameterizedTest
		@CsvSource(delimiter=';', value={
				"FIND 2..3[]-->4-[];2..3[]-->4-[]",
				"FIND [],![]-->4-[],3+[];![]-->4-[]",
		})
		void testCompetingQuantifiers(String rawPayload, String offendingPart) {
			assertReportHasErrors(expectReport(rawPayload),
					pair(QueryErrorCode.UNSUPPORTED_FEATURE, list(msgEnds(qt(offendingPart)))));
		}

		@ParameterizedTest
		@CsvSource(delimiter=';', value={
				"FIND []< -[]--[];< -",
				"FIND []--[]- >[];- >",
		})
		void testNonContinuousEdgePart(String rawPayload, String offendingPart) {
			assertReportHasErrors(expectReport(rawPayload),
					pair(QueryErrorCode.NON_CONTINUOUS_TOKEN, list(msgEnds(qt(offendingPart)))));
		}

		@ParameterizedTest
		@CsvSource(delimiter=';', value={
				"FIND ![![]];![]",
				"FIND ![3+[] ![x<4]];![x<4]",
		})
		void testDoubleNegation(String rawPayload, String offendingPart) {
			assertReportHasErrors(expectReport(rawPayload),
					pair(QueryErrorCode.INCORRECT_USE,
							list(msgContains("Double negation"), msgContains(qt(offendingPart)))));
		}

		/*
		 * Errors to check:
		 * -
		 *
		 * Done:
		 * INCORRECT_USE -> double negation on nested nodes
		 * NON_CONTINUOUS_TOKEN -> part of complex edge definition is not continuous
		 * UNSUPPORTED_FEATURE -> quantifier on both ends of edge
		 * INVALID_LITERAL -> range quantifier with lower>=upper
		 * UNSUPPORTED_FEATURE -> edges in tree or sequence
		 * INVALID_LITERAL -> AT_MOST quantifier with 0
		 * SUPERFLUOUS_DECLARATION -> redundant nesting of node statements with {}
		 * INCORRECT_USE -> universally quantified root node
		 * INCORRECT_USE -> ALIGNED flag with less than 2 existentially quantified nodes
		 * UNSUPPORTED_FEATURE -> children in graph or sequence
		 */
	}

	@Nested
	class ForGroupings {

		private void assertGroup(IqlGroup group,
				String expression, @Nullable String filter,
				String label, @Nullable String defaultValue) {
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
				assertExpression(group.getFilterOn(), filter);
			}
			if(defaultValue!=null) {
				assertExpression(group.getDefaultValue(), defaultValue);
			}
		}

		@Test
		void testProcessGroupingBare() {
			String rawGrouping = "GROUP BY $token.value LABEL \"tok\"";
			List<IqlGroup> groupings = new QueryProcessor(false).processGrouping(rawGrouping);
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
			List<IqlGroup> groupings = new QueryProcessor(false).processGrouping(rawGrouping);
			assertThat(groupings)
				.isNotNull()
				.hasSize(1);
			assertGroup(groupings.get(0), "$token.value", null, "\"tok\"", defaultValue);
		}

		@Test
		void testProcessGroupingWithFilter() {
			String rawGrouping = "GROUP BY $token.value FILTER ON (x*2) > 123 LABEL \"tok\"";
			List<IqlGroup> groupings = new QueryProcessor(false).processGrouping(rawGrouping);
			assertThat(groupings)
				.isNotNull()
				.hasSize(1);
			assertGroup(groupings.get(0), "$token.value", "(x*2) > 123", "\"tok\"", null);
		}

		@Test
		void testProcessFullGrouping() {
			String rawGrouping = "GROUP BY $token.value FILTER ON (x*2) > 123 LABEL \"tok\" DEFAULT 456";
			List<IqlGroup> groupings = new QueryProcessor(false).processGrouping(rawGrouping);
			assertThat(groupings)
				.isNotNull()
				.hasSize(1);
			assertGroup(groupings.get(0), "$token.value", "(x*2) > 123", "\"tok\"", "456");
		}

		@Test
		void testStackedGrouping() {
			String rawGrouping = "GROUP BY $token.value LABEL \"tok\", BY @var1 LABEL \"anno\"";
			List<IqlGroup> groupings = new QueryProcessor(false).processGrouping(rawGrouping);
			assertThat(groupings)
				.isNotNull()
				.hasSize(2);
			assertGroup(groupings.get(0), "$token.value", null, "\"tok\"", null);
			assertGroup(groupings.get(1), "@var1", null, "\"anno\"", null);
		}

	}

	@Nested
	class ForResult {

		private void assertSorting(IqlSorting sorting, String expression, Order order) {
			assertThat(sorting.getExpression().getContent()).isEqualTo(expression);
			assertThat(sorting.getOrder()).isEqualTo(order);
		}

		@Test
		void testProcessResultLimit() {
			String rawResult = "LIMIT 123";
			IqlResult result = new IqlResult();
			new QueryProcessor(false).processResult(rawResult, result, true);
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
			new QueryProcessor(false).processResult(rawResult, result, true);
			assertThat(result.getLimit())
				.isPresent()
				.hasValue(50);
			assertThat(result.isPercent())
				.isTrue();
			assertThat(result.getSortings())
				.isEmpty();
		}

		@ParameterizedTest
		@EnumSource(Order.class)
		void testProcessResultSorting(Order order) {
			String rawResult = "ORDER BY $token.value "+order.getLabel();
			IqlResult result = new IqlResult();
			new QueryProcessor(false).processResult(rawResult, result, true);
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
			new QueryProcessor(false).processResult(rawResult, result, true);
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

	@Nested
	class ForPayload {

		@ParameterizedTest
		@ValueSource(strings = {"all", "ALL"})
		void testAll(String rawPayload) {
			IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
			assertThat(payload).extracting(IqlPayload::getQueryType).isEqualTo(QueryType.ALL);
			assertThat(payload.getBindings()).isEmpty();
			assertThat(payload.getConstraint()).isEmpty();
			assertThat(payload.getLanes()).isEmpty();
		}


		@SafeVarargs
		private final void assertBindings(IqlPayload payload, Consumer<IqlBinding>...asserters) {
			List<IqlBinding> bindings = payload.getBindings();
			assertThat(bindings).hasSize(asserters.length);
			for (int i = 0; i < asserters.length; i++) {
				asserters[i].accept(bindings.get(i));
			}
		}

		private void assertConstraint(IqlPayload payload, Consumer<IqlConstraint> asserter) {
			if(asserter!=null) {
				assertThat(payload.getConstraint()).isNotEmpty().get().satisfies(asserter);
			} else {
				assertThat(payload.getConstraint()).isEmpty();
			}
		}

		private Consumer<IqlBinding> bind(String target, boolean distinct, String...labels) {
			return binding -> {
				assertThat(labels.length>0);
				assertThat(binding.getTarget()).isEqualTo(target);
				assertThat(binding.isDistinct()).isEqualTo(distinct);
				List<IqlReference> refs = binding.getMembers();
				assertThat(refs).hasSize(labels.length);
				for (int i = 0; i < labels.length; i++) {
					assertThat(refs.get(i).getName()).isEqualTo(labels[i]);
				}
			};
		}

		@SafeVarargs
		private final void assertLanes(IqlPayload payload, Consumer<IqlLane>...asserters) {
			List<IqlLane> lanes = payload.getLanes();
			assertThat(lanes).hasSize(asserters.length);
			for (int i = 0; i < asserters.length; i++) {
				asserters[i].accept(lanes.get(i));
			}
		}

		private void assertProperElement(IqlProperElement element, String label,
				Consumer<IqlConstraint> constraint) {
			if(label!=null) {
				assertThat(element.getLabel()).contains(label);
			} else {
				assertThat(element.getLabel()).isEmpty();
			}

			if(constraint!=null) {
				assertThat(element.getConstraint()).isNotEmpty().get().satisfies(constraint);
			} else {
				assertThat(element.getConstraint()).isEmpty();
			}
		}

		private final Consumer<IqlLane> lane(LaneType type, Consumer<IqlElement> asserter) {
			return lane -> {
				assertThat(lane.getLaneType()).isSameAs(type);
				asserter.accept(lane.getElements());
			};
		}

		@SafeVarargs
		private final Consumer<IqlElement> sequence(NodeArrangement arrangement,
				Consumer<IqlElement>...asserters) {
			return element -> {
				assertThat(element).isInstanceOf(IqlSet.class);
				IqlSet set = (IqlSet)element;
				assertThat(set.getArrangement()).isSameAs(arrangement);
				List<IqlElement> items = set.getElements();
				assertThat(items).hasSize(asserters.length);
				for (int i = 0; i < asserters.length; i++) {
					asserters[i].accept(items.get(i));
				}
			};
		}

		@SafeVarargs
		private final Consumer<IqlElement> grouping(Consumer<IqlQuantifier> qAsserter,
				Consumer<IqlElement>...asserters) {
			return element -> {
				assertThat(element).isInstanceOf(IqlGrouping.class);
				IqlGrouping grouping = (IqlGrouping)element;
				if(qAsserter!=null) {
					List<IqlQuantifier> quantifiers = grouping.getQuantifiers();
					assertThat(quantifiers).hasSize(1);
					qAsserter.accept(quantifiers.get(0));
				}
				List<IqlElement> items = grouping.getElements();
				assertThat(items).hasSize(asserters.length);
				for (int i = 0; i < asserters.length; i++) {
					asserters[i].accept(items.get(i));
				}
			};
		}

		private Consumer<IqlElement> node() {
			return node(null, null);
		}

		@SafeVarargs
		private final Consumer<IqlElement> node(String label, Consumer<IqlConstraint> constraint,
				Consumer<IqlQuantifier>...qAsserters) {
			return element -> {
				assertThat(element).isInstanceOf(IqlNode.class);
				IqlNode node = (IqlNode) element;
				assertProperElement(node, label, constraint);
				List<IqlQuantifier> quantifiers = node.getQuantifiers();
				assertThat(quantifiers).hasSize(qAsserters.length);
				for (int i = 0; i < qAsserters.length; i++) {
					qAsserters[i].accept(quantifiers.get(i));
				}
			};
		}

		private final Consumer<IqlElement> tree(String label, Consumer<IqlConstraint> constraint,
				Consumer<IqlElement> childAsserter) {
			return element -> {
				assertThat(element).isInstanceOf(IqlTreeNode.class);
				IqlTreeNode tree = (IqlTreeNode) element;
				assertProperElement(tree, label, constraint);
				Optional<IqlElement> children = tree.getChildren();
				assertThat(children.isPresent()).isTrue();
				childAsserter.accept(children.get());
			};
		}

		private Consumer<IqlElement> edge(EdgeType edgeType, String label, Consumer<IqlConstraint> constraint,
				Consumer<IqlElement> sourceAssert, Consumer<IqlElement> targetAssert) {
			return element -> {
				assertThat(element).isInstanceOf(IqlEdge.class);
				IqlEdge edge = (IqlEdge) element;
				assertProperElement(edge, label, constraint);
				assertThat(edge.getEdgeType()).isEqualTo(edgeType);
				sourceAssert.accept(edge.getSource());
				targetAssert.accept(edge.getTarget());
			};
		}

		private Consumer<IqlConstraint> pred(String content) {
			return constraint -> {
				assertThat(constraint).isInstanceOf(IqlPredicate.class);
				assertExpression(((IqlPredicate)constraint).getExpression(), content);
			};
		}

		@SafeVarargs
		private final Consumer<IqlConstraint> term(BooleanOperation op,
				Consumer<IqlConstraint>...asserters) {
			return constraint -> {
				assertThat(constraint).isInstanceOf(IqlTerm.class);
				IqlTerm term = (IqlTerm) constraint;
				assertThat(term.getOperation()).isSameAs(op);
				List<IqlConstraint> items = term.getItems();
				assertThat(items).hasSize(asserters.length);
				for (int i = 0; i < asserters.length; i++) {
					asserters[i].accept(items.get(i));
				}
			};
		}

		private Consumer<IqlQuantifier> quant(QuantifierType qType, int val) {
			return quantifier -> {
				assertThat(quantifier.getQuantifierType()).isSameAs(qType);
				assertThat(quantifier.getValue()).hasValue(val);
			};
		}

		private Consumer<IqlQuantifier> quant(int lower, int upper) {
			return quantifier -> {
				assertThat(quantifier.getQuantifierType()).isSameAs(QuantifierType.RANGE);
				assertThat(quantifier.getValue()).isEmpty();
				assertThat(quantifier.getLowerBound()).hasValue(lower);
				assertThat(quantifier.getUpperBound()).hasValue(upper);
			};
		}

		private Consumer<IqlQuantifier> quantAll() {
			return quantifier -> {
				assertThat(quantifier.getQuantifierType()).isSameAs(QuantifierType.ALL);
				assertThat(quantifier.getValue()).isEmpty();
				assertThat(quantifier.getLowerBound()).isEmpty();
				assertThat(quantifier.getUpperBound()).isEmpty();
			};
		}

		private Consumer<IqlQuantifier> quantNone() {
			return quantifier -> {
				assertThat(quantifier.getQuantifierType()).isSameAs(QuantifierType.EXACT);
				assertThat(quantifier.getValue()).hasValue(0);
				assertThat(quantifier.getLowerBound()).isEmpty();
				assertThat(quantifier.getUpperBound()).isEmpty();
			};
		}

		@Nested
		class WhenPlain {

			private void assertPlain(IqlPayload payload) {
				assertThat(payload).extracting(IqlPayload::getQueryType).isEqualTo(QueryType.PLAIN);
				assertThat(payload.getLanes()).isEmpty();
			}

			@Nested
			class Unbound {

				@Test
				void testPredicate() {
					String rawPayload = "FIND $token1.val>0";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertPlain(payload);
					assertBindings(payload);
					assertConstraint(payload, pred("$token1.val>0"));
				}

				@Test
				void testConjunctiveChain() {
					String rawPayload = "FIND $token1.val>0 && func() !=true && ($edge.dist()<5+4)";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertPlain(payload);
					assertBindings(payload);
					assertConstraint(payload, term(BooleanOperation.CONJUNCTION,
							pred("$token1.val>0"),
							pred("func() !=true"),
							pred("$edge.dist()<5+4")));
				}

				@Test
				void testDisjunctiveChain() {
					String rawPayload = "FIND $token1.val>0 || func() !=true || ($edge.dist()<5+4)";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertPlain(payload);
					assertBindings(payload);
					assertConstraint(payload, term(BooleanOperation.DISJUNCTION,
							pred("$token1.val>0"),
							pred("func() !=true"),
							pred("$edge.dist()<5+4")));
				}

				@Test
				void testMixedExpression() {
					String rawPayload = "FIND $token1.val>0 || func() !=true && ($edge.dist()<5+4)";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertPlain(payload);
					assertBindings(payload);
					assertConstraint(payload, term(BooleanOperation.DISJUNCTION,
							pred("$token1.val>0"),
							term(BooleanOperation.CONJUNCTION,
								pred("func() !=true"),
								pred("$edge.dist()<5+4"))));
				}

			}

			@Nested
			class Bound {

				@Test
				void testBoundPredicate() {
					String rawPayload = "WITH $token FROM layer1 FIND $token.val>0";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertPlain(payload);
					assertBindings(payload, bind("layer1", false, "token"));
					assertConstraint(payload, pred("$token.val>0"));
				}

				@Test
				void testMultiBoundPredicate() {
					String rawPayload = "WITH $token1,$token2 FROM layer1 FIND $token1.val+$token2.val>0";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertPlain(payload);
					assertBindings(payload, bind("layer1", false, "token1", "token2"));
					assertConstraint(payload, pred("$token1.val+$token2.val>0"));
				}

				@Test
				void testDistinctMultiBoundPredicate() {
					String rawPayload = "WITH DISTINCT $token1,$token2 FROM layer1 FIND $token1.val+$token2.val>0";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertPlain(payload);
					assertBindings(payload, bind("layer1", true, "token1", "token2"));
					assertConstraint(payload, pred("$token1.val+$token2.val>0"));
				}

				@Test
				void testMultiLayerBoundPredicate() {
					String rawPayload = "WITH $token1 FROM layer1 AND $token2 FROM layer2 FIND $token1.val+$token2.val>0";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertPlain(payload);
					assertBindings(payload,
							bind("layer1", false, "token1"),
							bind("layer2", false, "token2"));
					assertConstraint(payload, pred("$token1.val+$token2.val>0"));
				}

			}
		}

		@Nested
		class WhenSequence {

			private void assertSequence(IqlPayload payload) {
				assertThat(payload).extracting(IqlPayload::getQueryType).isEqualTo(QueryType.SINGLE_LANE);
				List<IqlLane> lanes = payload.getLanes();
				assertThat(lanes).hasSize(1);
				IqlLane lane = lanes.get(0);
				assertThat(lane.getLaneType()).isSameAs(LaneType.SEQUENCE);
			}

			@Nested
			class Unbound {

				@Test
				void testEmptyUnnamedNode() {
					String rawPayload = "FIND []";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.SEQUENCE, node(null, null)));
				}

				@Test
				void testEmptyNamedNode() {
					String rawPayload = "FIND [$node1:]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.SEQUENCE, node("node1", null)));
				}

				@Test
				void testFilledUnnamedNode() {
					String rawPayload = "FIND [pos!=\"NNP\"]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.SEQUENCE, node(null, pred("pos!=\"NNP\""))));
				}

				@Test
				void testFilledNamedNode() {
					String rawPayload = "FIND [$node1: pos!=\"NNP\"]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.SEQUENCE, node("node1", pred("pos!=\"NNP\""))));
				}

			}

			@Nested
			class Quantified {

				@ParameterizedTest
				@ValueSource(strings = {"all", "ALL", "*"})
				void testUniversallyQuantifiedRoot(String quantifier) {
					String rawPayload = "FIND "+quantifier+"[]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.SEQUENCE,
							node(null, null, quantAll())));
				}

				@ParameterizedTest
				@ValueSource(ints = {0, 1, 1000, Integer.MAX_VALUE})
				void testEmptyExactQuantifiedNode(int quantifier) {
					String rawPayload = "FIND "+quantifier+"[]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.SEQUENCE,
							node(null, null, quant(QuantifierType.EXACT, quantifier))));
				}

				@ParameterizedTest
				@ValueSource(ints = {0, 1, 1000, Integer.MAX_VALUE})
				void testEmptyAtLeastQuantifiedNode(int quantifier) {
					String rawPayload = "FIND "+quantifier+"+[]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.SEQUENCE,
							node(null, null, quant(QuantifierType.AT_LEAST, quantifier))));
				}

				@ParameterizedTest
				@ValueSource(ints = {1, 1000, Integer.MAX_VALUE})
				void testEmptyAtMostQuantifiedNode(int quantifier) {
					String rawPayload = "FIND "+quantifier+"-[]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.SEQUENCE,
							node(null, null, quant(QuantifierType.AT_MOST, quantifier))));
				}

				@ParameterizedTest
				@ValueSource(strings = {"not", "NOT", "!"})
				void testEmptyNegatedNode(String quantifier) {
					String rawPayload = "FIND "+quantifier+"[]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.SEQUENCE, node(null, null, quant(QuantifierType.EXACT, 0))));
				}

				@ParameterizedTest
				@CsvSource({
					"0, 1",
					"1, 100",
					"0, "+Integer.MAX_VALUE
				})
				void testEmptyRangeQuantifiedNode(int lower, int upper) {
					String rawPayload = "FIND "+lower+".."+upper+"[]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.SEQUENCE, node(null, null, quant(lower, upper))));
				}

				@Test
				void testEmptyMultiQuantifiedNode() {
					String rawPayload = "FIND 2|10|5..7|1000+[]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.SEQUENCE,
							node(null, null,
								quant(QuantifierType.EXACT, 2),
								quant(QuantifierType.EXACT, 10),
								quant(5, 7),
								quant(QuantifierType.AT_LEAST, 1000))));
				}

				@Test
				void testFilledQuantifiedNode() {
					String rawPayload = "FIND 2|10|5..7|1000+[$node1: pos!=\"NNP\"]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.SEQUENCE,
							node("node1", pred("pos!=\"NNP\""),
								quant(QuantifierType.EXACT, 2),
								quant(QuantifierType.EXACT, 10),
								quant(5, 7),
								quant(QuantifierType.AT_LEAST, 1000))));
				}

				@Test
				void testEmptyUnnamedNodeSequence() {
					String rawPayload = "FIND [][][]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.SEQUENCE, sequence(NodeArrangement.UNSPECIFIED,
							node(null, null), node(null, null), node(null, null))));
				}

				@Test
				void testEmptyUnnamedQuantifiedNodeSequence() {
					String rawPayload = "FIND 4-[] 2..10[] <3|5+>[] ![]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.SEQUENCE, sequence(NodeArrangement.UNSPECIFIED,
							node(null, null, quant(QuantifierType.AT_MOST, 4)),
							node(null, null, quant(2, 10)),
							node(null, null, quant(QuantifierType.EXACT, 3), quant(QuantifierType.AT_LEAST, 5)),
							node(null, null, quant(QuantifierType.EXACT, 0)))));
				}

			}

			@Nested
			class Bound {

				@Test
				void testEmptyUnnamedNode() {
					String rawPayload = "WITH $token FROM layer1 FIND []";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload, bind("layer1", false, "token"));
					assertLanes(payload, lane(LaneType.SEQUENCE, node(null, null)));
				}

				@Test
				void testEmptyNamedNode() {
					String rawPayload = "WITH $token FROM layer1 FIND [$token:]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload, bind("layer1", false, "token"));
					assertLanes(payload, lane(LaneType.SEQUENCE, node("token", null)));
				}

				@Test
				void testEmptyNamedNodes() {
					String rawPayload = "WITH $token1,$token2 FROM layer1 FIND [$token1:][$token2:]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload, bind("layer1", false, "token1", "token2"));
					assertLanes(payload, lane(LaneType.SEQUENCE, sequence(NodeArrangement.UNSPECIFIED,
							node("token1", null), node("token2", null))));
				}
 			}

			@Nested
			class GlobalConstraints {

				@Test
				void testFilledNodes() {
					String rawPayload = "WITH DISTINCT $token1,$token2 FROM layer1 "
							+ "AND $p FROM phrase "
							+ "FIND [$token1: pos!=\"NNP\"] 4+[] [$token2: length()>12] "
							+ "HAVING $p.contains($token1) && !$p.contains($token2)";
//					System.out.println(rawPayload);
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertSequence(payload);
					assertBindings(payload,
							bind("layer1", true, "token1", "token2"),
							bind("phrase", false, "p"));
					assertLanes(payload, lane(LaneType.SEQUENCE, sequence(NodeArrangement.UNSPECIFIED,
							node("token1", pred("pos!=\"NNP\"")),
							node(null, null, quant(QuantifierType.AT_LEAST, 4)),
							node("token2", pred("length()>12")))));
					assertConstraint(payload, term(BooleanOperation.CONJUNCTION,
							pred("$p.contains($token1)"),
							pred("!$p.contains($token2)")));
				}

			}
		}

		@Nested
		class WhenTree {

			private void assertTree(IqlPayload payload) {
				assertThat(payload).extracting(IqlPayload::getQueryType).isEqualTo(QueryType.SINGLE_LANE);
				List<IqlLane> lanes = payload.getLanes();
				assertThat(lanes).hasSize(1);
				IqlLane lane = lanes.get(0);
				assertThat(lane.getLaneType()).isSameAs(LaneType.TREE);
			}

			@Nested
			class NestedNodes {

				@Test
				void testEmptyNestedNode() {
					String rawPayload = "FIND [[]]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertTree(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.TREE, tree(null, null, node())));
				}

				@Test
				void testEmptyNestedSiblings() {
					String rawPayload = "FIND [[][]]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertTree(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.TREE,
							tree(null, null, sequence(NodeArrangement.UNSPECIFIED,
									node(), node()))));
				}

				@Test
				void testEmptyNestedChain() {
					String rawPayload = "FIND [[[]]]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertTree(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.TREE,
							tree(null, null, tree(null, null, node()))));
				}
			}

			@Nested
			class QuantifiedNodes {

				@ParameterizedTest
				@ValueSource(strings = {"all", "ALL", "*"})
				void testEmptyAllQuantifiedNode(String quantifier) {
					String rawPayload = "FIND ["+quantifier+"[]]";
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertTree(payload);
					assertBindings(payload);
					assertLanes(payload, lane(LaneType.TREE,
							tree(null, null, node(null, null, quantAll()))));
				}
			}

			@Nested
			class GlobalConstraints {

				@Test
				void testFilledNodes() {
					String rawPayload = "WITH DISTINCT $token1,$token2 FROM layer1 "
							+ "AND $p FROM phrase "
							+ "FIND ORDERED [5-[][$token1: pos!=\"NNP\"]] 4+[] "
							+ "	  [$token2: length()>12 ![pos==\"DET\"] 3+[pos==\"MOD\"]] "
							+ "HAVING $p.contains($token1) && !$p.contains($token2)";
//					System.out.println(rawPayload);
					IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
					assertTree(payload);
					assertBindings(payload,
							bind("layer1", true, "token1", "token2"),
							bind("phrase", false, "p"));
					assertLanes(payload, lane(LaneType.TREE, sequence(NodeArrangement.ORDERED,
							tree(null, null, sequence(NodeArrangement.UNSPECIFIED,
									node(null, null, quant(QuantifierType.AT_MOST, 5)),
									node("token1", pred("pos!=\"NNP\"")))),
							node(null, null, quant(QuantifierType.AT_LEAST, 4)),
							tree("token2", pred("length()>12"), sequence(NodeArrangement.UNSPECIFIED,
									node(null, pred("pos==\"DET\""), quantNone()),
									node(null, pred("pos==\"MOD\""), quant(QuantifierType.AT_LEAST, 3)))))));
					assertConstraint(payload, term(BooleanOperation.CONJUNCTION,
							pred("$p.contains($token1)"),
							pred("!$p.contains($token2)")));
				}

				//TODO
			}
		}

		@Nested
		class WhenGraph {
			//TODO
		}

	}

}
