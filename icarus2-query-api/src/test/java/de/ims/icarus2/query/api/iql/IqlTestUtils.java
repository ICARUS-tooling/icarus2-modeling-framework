/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.iql;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import de.ims.icarus2.query.api.iql.IqlConstraint.BooleanOperation;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlPredicate;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlTerm;
import de.ims.icarus2.query.api.iql.IqlElement.EdgeType;
import de.ims.icarus2.query.api.iql.IqlElement.IqlEdge;
import de.ims.icarus2.query.api.iql.IqlElement.IqlElementDisjunction;
import de.ims.icarus2.query.api.iql.IqlElement.IqlGrouping;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement;
import de.ims.icarus2.query.api.iql.IqlElement.IqlSequence;
import de.ims.icarus2.query.api.iql.IqlElement.IqlTreeNode;
import de.ims.icarus2.query.api.iql.IqlLane.LaneType;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerCall;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerExpression;
import de.ims.icarus2.query.api.iql.IqlMarker.MarkerExpressionType;
import de.ims.icarus2.query.api.iql.IqlQuantifier.QuantifierModifier;
import de.ims.icarus2.query.api.iql.IqlQuantifier.QuantifierType;

/**
 * @author Markus Gärtner
 *
 */
public class IqlTestUtils {

	public static final String NO_LABEL = null;
	public static final IqlMarker NO_MARKER = null;
	public static final IqlConstraint NO_CONSTRAINT = null;
	public static final String ID = "_id_";

	private static void setId(IqlUnique unique) {
		unique.setId(ID);
	}

	public static IqlNode node(@Nullable String label, @Nullable IqlMarker marker,
			@Nullable IqlConstraint constraint) {
		IqlNode node = new IqlNode();
		setId(node);
		Optional.ofNullable(label).ifPresent(node::setLabel);
		Optional.ofNullable(marker).ifPresent(node::setMarker);
		Optional.ofNullable(constraint).ifPresent(node::setConstraint);
		return node;
	}

	public static <Q extends IqlQuantifier.Quantifiable> Q quantify(
			Q target, IqlQuantifier...quantifiers) {
		Stream.of(quantifiers).forEach(target::addQuantifier);
		return target;
	}

	public static IqlElementDisjunction disjunction(IqlElement...elements) {
		IqlElementDisjunction disjunction = new IqlElementDisjunction();
		setId(disjunction);
		Stream.of(elements).forEach(disjunction::addAlternative);
		return disjunction;
	}

	public static IqlGrouping grouping(IqlElement element) {
		IqlGrouping grouping = new IqlGrouping();
		setId(grouping);
		grouping.setElement(element);
		return grouping;
	}

	private static IqlSequence sequence(IqlElement...elements) {
		IqlSequence sequence = new IqlSequence();
		setId(sequence);
		Stream.of(elements).forEach(sequence::addElement);
		return sequence;
	}

	public static IqlSequence unordered(boolean adjacent, IqlElement...elements) {
		IqlSequence sequence = sequence(elements);
		sequence.addArrangement(NodeArrangement.UNORDERED);
		if(adjacent) sequence.addArrangement(NodeArrangement.ADJACENT);
		return sequence;
	}

	public static IqlSequence ordered(boolean adjacent, IqlElement...elements) {
		IqlSequence sequence = sequence(elements);
		sequence.addArrangement(NodeArrangement.ORDERED);
		if(adjacent) sequence.addArrangement(NodeArrangement.ADJACENT);
		return sequence;
	}

	public static IqlConstraint constraint(String content) {
		IqlExpression expression = new IqlExpression();
		expression.setContent(content);
		IqlPredicate predicate = new IqlPredicate();
		setId(predicate);
		predicate.setExpression(expression);
		return predicate;
	}

	public static String eq_exp(char c) { return "$.toString()==\""+c+"\""; }
	public static String ic_exp(char c) {
		char c_l = Character.toLowerCase(c);
		char c_u = Character.toUpperCase(c);
		return or(eq_exp(c_l), eq_exp(c_u));
	}

	public static String and(String...items) { return "(" + String.join(" && ", items) + ")"; }

	public static String or(String...items) { return "(" + String.join(" || ", items) + ")"; }

	public static IqlMarker mark(String name, Number...args) {
		IqlMarkerCall call = new IqlMarkerCall();
		call.setName(name);
		if(args.length>0) {
			call.setArguments(args);
		}
		return call;
	}

	public static IqlMarker and(IqlMarker...markers) {
		IqlMarkerExpression exp = new IqlMarkerExpression();
		exp.setExpressionType(MarkerExpressionType.CONJUNCTION);
		Stream.of(markers).forEach(exp::addItem);
		return exp;
	}

	public static IqlMarker or(IqlMarker...markers) {
		IqlMarkerExpression exp = new IqlMarkerExpression();
		exp.setExpressionType(MarkerExpressionType.DISJUNCTION);
		Stream.of(markers).forEach(exp::addItem);
		return exp;
	}

	public static IqlQuantifier all() { return IqlQuantifier.all(); }

	public static IqlQuantifier negated() { return IqlQuantifier.none(); }

	private static IqlQuantifier of(QuantifierType type, int value,
			QuantifierModifier mode, boolean discontinuous) {
		IqlQuantifier quantifier = new IqlQuantifier();
		quantifier.setQuantifierType(type);
		quantifier.setValue(value);
		quantifier.setQuantifierModifier(mode);
		quantifier.setDiscontinuous(discontinuous);
		return quantifier;
	}

	private static IqlQuantifier of(int min, int max,
			QuantifierModifier mode, boolean discontinuous) {
		IqlQuantifier quantifier = new IqlQuantifier();
		quantifier.setQuantifierType(QuantifierType.RANGE);
		quantifier.setLowerBound(min);
		quantifier.setUpperBound(max);
		quantifier.setQuantifierModifier(mode);
		quantifier.setDiscontinuous(discontinuous);
		return quantifier;
	}

	public static IqlQuantifier exact(int value, boolean discontinuous) {
		return of(QuantifierType.EXACT, value, QuantifierModifier.GREEDY, discontinuous);
	}

	public static IqlQuantifier atMostGreedy(int max, boolean discontinuous) {
		return of(QuantifierType.AT_MOST, max, QuantifierModifier.GREEDY, discontinuous);
	}

	public static IqlQuantifier atMostReluctant(int max, boolean discontinuous) {
		return of(QuantifierType.AT_MOST, max, QuantifierModifier.RELUCTANT, discontinuous);
	}

	public static IqlQuantifier atMostPossessive(int max, boolean discontinuous) {
		return of(QuantifierType.AT_MOST, max, QuantifierModifier.POSSESSIVE, discontinuous);
	}

	public static IqlQuantifier atLeastGreedy(int min, boolean discontinuous) {
		return of(QuantifierType.AT_LEAST, min, QuantifierModifier.GREEDY, discontinuous);
	}

	public static IqlQuantifier atLeastReluctant(int min, boolean discontinuous) {
		return of(QuantifierType.AT_LEAST, min, QuantifierModifier.RELUCTANT, discontinuous);
	}

	public static IqlQuantifier atLeastPossessive(int min, boolean discontinuous) {
		return of(QuantifierType.AT_LEAST, min, QuantifierModifier.POSSESSIVE, discontinuous);
	}

	public static IqlQuantifier rangeGreedy(int min, int max, boolean discontinuous) {
		return of(min, max, QuantifierModifier.GREEDY, discontinuous);
	}

	public static IqlQuantifier rangeReluctant(int min, int max, boolean discontinuous) {
		return of(min, max, QuantifierModifier.RELUCTANT, discontinuous);
	}

	public static IqlQuantifier rangePossessive(int min, int max, boolean discontinuous) {
		return of(min, max, QuantifierModifier.POSSESSIVE, discontinuous);
	}

	// ASSERTIONS

	public static void assertExpression(IqlExpression expression, String content) {
		assertThat(expression.getContent()).isEqualTo(content);
	}

	public static void assertExpression(Optional<IqlExpression> expression, String content) {
		assertThat(expression).isNotEmpty();
		assertExpression(expression.get(), content);
	}

	public static String quote(String s) {
		return '\''+s+'\'';
	}

	@SafeVarargs
	public	static final void assertBindings(IqlPayload payload, Consumer<IqlBinding>...asserters) {
		List<IqlBinding> bindings = payload.getBindings();
		assertThat(bindings).hasSize(asserters.length);
		for (int i = 0; i < asserters.length; i++) {
			asserters[i].accept(bindings.get(i));
		}
	}

	public static void assertConstraint(IqlPayload payload, Consumer<IqlConstraint> asserter) {
		if(asserter!=null) {
			assertThat(payload.getConstraint()).isNotEmpty().get().satisfies(asserter);
		} else {
			assertThat(payload.getConstraint()).isEmpty();
		}
	}

	public static Consumer<IqlBinding> bindAssert(String target, boolean distinct, String...labels) {
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
	public	static final void assertLanes(IqlPayload payload, Consumer<IqlLane>...asserters) {
		List<IqlLane> lanes = payload.getLanes();
		assertThat(lanes).hasSize(asserters.length);
		for (int i = 0; i < asserters.length; i++) {
			asserters[i].accept(lanes.get(i));
		}
	}

	public static void assertProperElement(IqlProperElement element, String label,
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

	public static final Consumer<IqlLane> laneAssert(LaneType type, Consumer<IqlElement> asserter) {
		return lane -> {
			assertThat(lane.getLaneType()).isSameAs(type);
			asserter.accept(lane.getElement());
		};
	}

	@SafeVarargs
	public static final Consumer<IqlElement> sequenceAssert(Consumer<IqlSequence> arrAsserter,
			Consumer<IqlElement>...asserters) {
		return element -> {
			assertThat(element).isInstanceOf(IqlSequence.class);
			IqlSequence sequence = (IqlSequence)element;
			arrAsserter.accept(sequence);
			List<IqlElement> items = sequence.getElements();
			assertThat(items).hasSize(asserters.length);
			for (int i = 0; i < asserters.length; i++) {
				asserters[i].accept(items.get(i));
			}
		};
	}

	public static final Consumer<IqlSequence> arrangementsAssert(NodeArrangement...arrangements) {
		return sequence -> {
			assertThat(sequence.getArrangements()).containsExactlyInAnyOrder(arrangements);
		};
	}

	public static final Consumer<IqlSequence> noArrangementsAssert() {
		return sequence -> assertThat(sequence.getArrangements()).isEmpty();
	}

	public static final Consumer<IqlElement> groupingAssert(Consumer<IqlQuantifier> qAsserter,
			Consumer<IqlElement> eAsserter) {
		return element -> {
			assertThat(element).isInstanceOf(IqlGrouping.class);
			IqlGrouping grouping = (IqlGrouping)element;
			if(qAsserter!=null) {
				List<IqlQuantifier> quantifiers = grouping.getQuantifiers();
				assertThat(quantifiers).hasSize(1);
				qAsserter.accept(quantifiers.get(0));
			}
			eAsserter.accept(grouping.getElement());
		};
	}

	public static Consumer<IqlElement> nodeAssert() {
		return nodeAssert(null, null);
	}

	@SafeVarargs
	public static final Consumer<IqlElement> nodeAssert(String label, Consumer<IqlConstraint> constraint,
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

	public static final Consumer<IqlElement> treeAssert
	(String label, Consumer<IqlConstraint> constraint,
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

	public static Consumer<IqlElement> edgeAssert(EdgeType edgeType, String label, Consumer<IqlConstraint> constraint,
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

	public static Consumer<IqlConstraint> predAssert(String content) {
		return constraint -> {
			assertThat(constraint).isInstanceOf(IqlPredicate.class);
			assertExpression(((IqlPredicate)constraint).getExpression(), content);
		};
	}

	@SafeVarargs
	public static final Consumer<IqlConstraint> termAssert(BooleanOperation op,
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

	public static Consumer<IqlQuantifier> quantAssert(QuantifierType qType, int val) {
		return quantifier -> {
			assertThat(quantifier.getQuantifierType()).isSameAs(qType);
			assertThat(quantifier.getValue()).hasValue(val);
		};
	}

	public static Consumer<IqlQuantifier> quantAssert(int lower, int upper) {
		return quantifier -> {
			assertThat(quantifier.getQuantifierType()).isSameAs(QuantifierType.RANGE);
			assertThat(quantifier.getValue()).isEmpty();
			assertThat(quantifier.getLowerBound()).hasValue(lower);
			assertThat(quantifier.getUpperBound()).hasValue(upper);
		};
	}

	public static Consumer<IqlQuantifier> quantAllAssert() {
		return quantifier -> {
			assertThat(quantifier.getQuantifierType()).isSameAs(QuantifierType.ALL);
			assertThat(quantifier.getValue()).isEmpty();
			assertThat(quantifier.getLowerBound()).isEmpty();
			assertThat(quantifier.getUpperBound()).isEmpty();
		};
	}

	public static Consumer<IqlQuantifier> quantNoneAssert() {
		return quantifier -> {
			assertThat(quantifier.getQuantifierType()).isSameAs(QuantifierType.EXACT);
			assertThat(quantifier.getValue()).hasValue(0);
			assertThat(quantifier.getLowerBound()).isEmpty();
			assertThat(quantifier.getUpperBound()).isEmpty();
		};
	}

}
