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
package de.ims.icarus2.query.api.iql;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import de.ims.icarus2.query.api.iql.IqlConstraint.IqlPredicate;
import de.ims.icarus2.query.api.iql.IqlElement.IqlGrouping;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlElement.IqlSet;
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

	public static <Q extends IqlQuantifier.Quantifiable> Q quantify(Q target, IqlQuantifier...quantifiers) {
		Stream.of(quantifiers).forEach(target::addQuantifier);
		return target;
	}

	public static IqlGrouping grouping(IqlElement...elements) {
		IqlGrouping grouping = new IqlGrouping();
		setId(grouping);
		Stream.of(elements).forEach(grouping::addElement);
		return grouping;
	}

	public static IqlSet sequence(IqlElement...elements) {
		IqlSet sequence = new IqlSet();
		setId(sequence);
		Stream.of(elements).forEach(sequence::addElement);
		return sequence;
	}

	public static IqlSet adjacent(IqlElement...elements) {
		IqlSet sequence = sequence(elements);
		sequence.setArrangement(NodeArrangement.ADJACENT);
		return sequence;
	}

	public static IqlSet ordered(IqlElement...elements) {
		IqlSet sequence = sequence(elements);
		sequence.setArrangement(NodeArrangement.ORDERED);
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

	private static IqlQuantifier of(QuantifierType type, int value, QuantifierModifier mode) {
		IqlQuantifier quantifier = new IqlQuantifier();
		quantifier.setQuantifierType(type);
		quantifier.setValue(value);
		quantifier.setQuantifierModifier(mode);
		return quantifier;
	}

	private static IqlQuantifier of(int min, int max, QuantifierModifier mode) {
		IqlQuantifier quantifier = new IqlQuantifier();
		quantifier.setQuantifierType(QuantifierType.RANGE);
		quantifier.setLowerBound(min);
		quantifier.setUpperBound(max);
		quantifier.setQuantifierModifier(mode);
		return quantifier;
	}

	public static IqlQuantifier exact(int value) {
		return of(QuantifierType.EXACT, value, QuantifierModifier.GREEDY);
	}

	public static IqlQuantifier atMostGreedy(int max) {
		return of(QuantifierType.AT_MOST, max, QuantifierModifier.GREEDY);
	}

	public static IqlQuantifier atMostReluctant(int max) {
		return of(QuantifierType.AT_MOST, max, QuantifierModifier.RELUCTANT);
	}

	public static IqlQuantifier atMostPossessive(int max) {
		return of(QuantifierType.AT_MOST, max, QuantifierModifier.POSSESSIVE);
	}

	public static IqlQuantifier atLeastGreedy(int min) {
		return of(QuantifierType.AT_LEAST, min, QuantifierModifier.GREEDY);
	}

	public static IqlQuantifier atLeastReluctant(int min) {
		return of(QuantifierType.AT_LEAST, min, QuantifierModifier.RELUCTANT);
	}

	public static IqlQuantifier atLeastPossessive(int min) {
		return of(QuantifierType.AT_LEAST, min, QuantifierModifier.POSSESSIVE);
	}

	public static IqlQuantifier rangeGreedy(int min, int max) {
		return of(min, max, QuantifierModifier.GREEDY);
	}

	public static IqlQuantifier rangeReluctant(int min, int max) {
		return of(min, max, QuantifierModifier.RELUCTANT);
	}

	public static IqlQuantifier rangePossessive(int min, int max) {
		return of(min, max, QuantifierModifier.POSSESSIVE);
	}

}
