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
package de.ims.icarus2.query.api.eval;

import static de.ims.icarus2.query.api.iql.AntlrUtils.asFragment;
import static de.ims.icarus2.query.api.iql.AntlrUtils.cleanNumberLiteral;
import static de.ims.icarus2.query.api.iql.AntlrUtils.textOf;
import static de.ims.icarus2.util.lang.Primitives._char;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.QuerySwitch;
import de.ims.icarus2.query.api.eval.BinaryOperations.AlgebraicOp;
import de.ims.icarus2.query.api.eval.BinaryOperations.ComparableComparator;
import de.ims.icarus2.query.api.eval.BinaryOperations.EqualityPred;
import de.ims.icarus2.query.api.eval.BinaryOperations.NumericalComparator;
import de.ims.icarus2.query.api.eval.BinaryOperations.StringMode;
import de.ims.icarus2.query.api.eval.BinaryOperations.StringOp;
import de.ims.icarus2.query.api.eval.Expression.IntegerListExpression;
import de.ims.icarus2.query.api.eval.Expression.ListExpression;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.AdditiveOpContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.AnnotationAccessContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.BitwiseOpContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.BooleanLiteralContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.CastExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ComparisonOpContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ConjunctionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.DisjunctionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.EqualityCheckContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ExpressionListContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.FloatingPointLiteralContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ForEachContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.IntegerLiteralContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ListAccessContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.MethodInvocationContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.MultiplicativeOpContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.NullLiteralContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.PathAccessContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.PrimaryContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.PrimaryExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.QualifiedIdentifierContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ReferenceContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.SetPredicateContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.StringOpContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.TernaryOpContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.TypeContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.UnaryOpContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.WrappingExpressionContext;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class ExpressionFactory {

	/** Context and settings to be used for evaluating the query. */
	private final EvaluationContext context;

	private final Map<Class<? extends ParserRuleContext>, Function<ParserRuleContext, Expression<?>>>
		handlers = new Object2ObjectOpenHashMap<>();

	public ExpressionFactory(EvaluationContext context) {
		this.context = requireNonNull(context);

		setupHandlers();
	}

	private void setupHandlers() {
		handlers.put(PrimaryExpressionContext.class, ctx -> processPrimary((PrimaryExpressionContext)ctx));
		handlers.put(PathAccessContext.class, ctx -> processPathAccess((PathAccessContext) ctx));
		handlers.put(MethodInvocationContext.class, ctx -> processMethodInvocation((MethodInvocationContext) ctx));
		handlers.put(ListAccessContext.class, ctx -> processListAccess((ListAccessContext) ctx));
		handlers.put(AnnotationAccessContext.class, ctx -> processAnnotationAccess((AnnotationAccessContext) ctx));
		handlers.put(CastExpressionContext.class, ctx -> processCastExpression((CastExpressionContext) ctx));
		handlers.put(WrappingExpressionContext.class, ctx -> processWrappingExpression((WrappingExpressionContext) ctx));
		handlers.put(SetPredicateContext.class, ctx -> processSetPredicate((SetPredicateContext) ctx));
		handlers.put(UnaryOpContext.class, ctx -> processUnaryOp((UnaryOpContext) ctx));
		handlers.put(MultiplicativeOpContext.class, ctx -> processMultiplicativeOp((MultiplicativeOpContext) ctx));
		handlers.put(AdditiveOpContext.class, ctx -> processAdditiveOp((AdditiveOpContext) ctx));
		handlers.put(BitwiseOpContext.class, ctx -> processBitwiseOp((BitwiseOpContext) ctx));
		handlers.put(ComparisonOpContext.class, ctx -> processComparisonOp((ComparisonOpContext) ctx));
		handlers.put(StringOpContext.class, ctx -> processStringOp((StringOpContext) ctx));
		handlers.put(EqualityCheckContext.class, ctx -> processEqualityCheck((EqualityCheckContext) ctx));
		handlers.put(ConjunctionContext.class, ctx -> processConjunction((ConjunctionContext) ctx));
		handlers.put(DisjunctionContext.class, ctx -> processDisjunction((DisjunctionContext) ctx));
		handlers.put(TernaryOpContext.class, ctx -> processTernaryOp((TernaryOpContext) ctx));
		handlers.put(ForEachContext.class, ctx -> processForEach((ForEachContext) ctx));
	}

	private boolean isAllowUnicode() {
		return !context.isSwitchSet(QuerySwitch.STRING_UNICODE_OFF);
	}

	private StringMode getStringMode() {
		StringMode stringMode = StringMode.DEFAULT;
		if(context.isSwitchSet(QuerySwitch.STRING_CASE_OFF)) {
			stringMode = StringMode.IGNORE_CASE;
		}
		return stringMode;
	}

	private Function<ParserRuleContext, Expression<?>> handlerFor(ParserRuleContext ctx) {
		return Optional.ofNullable(handlers.get(ctx.getClass()))
				.orElseThrow(() -> new QueryException(QueryErrorCode.AST_ERROR,
						"Unknown query fragment: "+ctx.getClass().getCanonicalName(), asFragment(ctx)));
	}

	public Expression<?> processExpression(ExpressionContext ctx) {
		Expression<?> expression = processExpression0(ctx);
		//TODO call optimize() already?
		return expression;
	}

	private Expression<?> processExpression0(ExpressionContext ctx) {
		return handlerFor(ctx).apply(ctx);
	}

	Expression<?>[] processExpressionList(ExpressionListContext ctx) {
		return ctx.expression().stream()
				.map(this::processExpression0)
				.toArray(Expression[]::new);
	}

	/** Insurance against changes in the IQL grammar that we missed */
	private <T> T failForUnhandledAlternative(ParserRuleContext ctx) {
		throw new QueryException(QueryErrorCode.AST_ERROR,
				"Unknown alterative: "+ctx.getClass().getCanonicalName(), asFragment(ctx));
	}

	private Expression<?> ensureNumerical(Expression<?> source) {
		if(!source.isNumerical())
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a numerical type: "+source.getResultType());
		//TODO maybe validate via instanceof ?
		return source;
	}

	private Expression<?> ensureInteger(Expression<?> source) {
		if(source.isInteger())
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not an integer expression: "+source.getResultType());
		return source;
	}

	@SuppressWarnings("unchecked")
	private Expression<Primitive<Boolean>> ensureBoolean(Expression<?> source) {
		if(!source.isBoolean())
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a boolean type: "+source.getResultType());
		//TODO take switches and general boolean type conversion into account!!
		//TODO maybe validate via instanceof ?
		return (Expression<Primitive<Boolean>>)source;
	}

	//TODO add argument to enable conversion
	@SuppressWarnings("unchecked")
	private Expression<CharSequence> ensureText(Expression<?> source) {
		if(!source.isText())
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a text type: "+source.getResultType());
		//TODO take string conversion into account!!
		//TODO maybe validate via instanceof ?
		return (Expression<CharSequence>)source;
	}

	private Expression<?>[] ensureInteger(Expression<?>[] source) {
		return Stream.of(source)
				.map(this::ensureInteger)
				.toArray(Expression[]::new);
	}

	private Expression<CharSequence>[] ensureText(Expression<?>[] source) {
		return Stream.of(source)
				.map(this::ensureText)
				.toArray(Expression[]::new);
	}

	private ListExpression<?, ?> ensureList(Expression<?> source) {
		if(!source.isList())
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a list type: "+source.getResultType());
		return (ListExpression<?, ?>)source;
	}

	private IntegerListExpression<?> ensureIntegerList(Expression<?> source) {
		ListExpression<?, ?> list = ensureList(source);
		if(!TypeInfo.isInteger(list.getElementType()))
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not an integer list type: "+source.getResultType());
		return (IntegerListExpression<?>)list;
	}

	private Expression<Primitive<Boolean>> maybeNegate(Expression<Primitive<Boolean>> source, boolean negate) {
		return negate ? UnaryOperations.not(source) : source;
	}

	Expression<?> processPrimary(PrimaryExpressionContext ctx) {
		PrimaryContext pctx = ctx.primary();

		if(pctx.nullLiteral()!=null) {
			return processNullLiteral(pctx.nullLiteral());
		} if(pctx.booleanLiteral()!=null) {
			return processBooleanLiteral(pctx.booleanLiteral());
		} else if(pctx.floatingPointLiteral()!=null) {
			return processFloatingPointLiteral(pctx.floatingPointLiteral());
		} else if(pctx.integerLiteral()!=null) {
			return processIntegerLiteral(pctx.integerLiteral());
		} else if(pctx.StringLiteral()!=null) {
			return processStringLiteral(pctx.StringLiteral());
		} else if(pctx.reference()!=null) {
			return processReference(pctx.reference());
		}

		return failForUnhandledAlternative(pctx);
	}

	private Expression<CharSequence> processStringLiteral(TerminalNode node) {
		String content = textOf(node);
		// If needed, unescape the content first
		if(content.indexOf('\\')!=-1) {
			StringBuilder sb = new StringBuilder(content.length());
			boolean escaped = false;
			for (int i = 0; i < content.length(); i++) {
				char c = content.charAt(i);
				if(c=='\\') {
					escaped = true;
				} else if(escaped) {
					char r;
					switch (c) {
					case '\\': r = '\\'; break;
					case 'r' : r = '\r'; break;
					case 'n':  r = '\n'; break;
					case 't':  r = '\t'; break;

					default:
						throw new QueryException(QueryErrorCode.INVALID_LITERAL,String.format(
								"Unexpected escape sequence in '%s' at index %d: %s",
								content, _int(i), _char(c)));
					}
					sb.append(r);
				} else {
					sb.append(c);
				}
			}
			content = sb.toString();
		}
		return Literals.of(content);
	}

	private Expression<?> processNullLiteral(NullLiteralContext ctx) {
		return Literals.ofNull();
	}

	private Expression<Primitive<Boolean>> processBooleanLiteral(BooleanLiteralContext ctx) {
		return Literals.of(ctx.TRUE()!=null);
	}

	private Expression<?> processIntegerLiteral(IntegerLiteralContext ctx) {
		String content = textOf(ctx);
		content = cleanNumberLiteral(content);

		try {
			long value = Long.parseLong(content);
			return Literals.of(value);
		} catch(NumberFormatException e) {
			throw new QueryException(QueryErrorCode.INVALID_LITERAL,
					"Invalid integer literal: "+textOf(ctx), asFragment(ctx), e);
		}
	}

	private Expression<?> processFloatingPointLiteral(FloatingPointLiteralContext ctx) {
		String content = textOf(ctx);
		content = cleanNumberLiteral(content);

		try {
			double value = Double.parseDouble(content);
			return Literals.of(value);
		} catch(NumberFormatException e) {
			throw new QueryException(QueryErrorCode.INVALID_LITERAL,
					"Invalid foating point literal: "+textOf(ctx), asFragment(ctx), e);
		}
	}

	private Expression<?> processReference(ReferenceContext ctx) {
		if(ctx.variableName()!=null) {

		} else if(ctx.member()!=null) {

		} else if(ctx.Identifier()!=null) {

		} else if(ctx.qualifiedIdentifier()!=null) {

		}

		return failForUnhandledAlternative(ctx);
	}

	private QualifiedIdentifier processQualifiedIdentifier(QualifiedIdentifierContext ctx) {
		return QualifiedIdentifier.of(textOf(ctx), textOf(ctx.hostId), textOf(ctx.elementId));
	}

	Expression<?> processPathAccess(PathAccessContext ctx) {
		Expression<?> source = processExpression0(ctx.source);
		//TODO implement
		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processMethodInvocation(MethodInvocationContext ctx) {
		Expression<?>[] arguments = processExpressionList(ctx.arguments);
		Expression<?> source = processExpression0(ctx.source);
		//TODO implement
		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processListAccess(ListAccessContext ctx) {
		ListExpression<?, ?> source = ensureList(processExpression0(ctx.source));
		Expression<?>[] indices = processExpressionList(ctx.indices);

		if(indices.length<1)
			throw new QueryException(QueryErrorCode.INCORRECT_USE,
					"List access needs at least 1 index argument: "+textOf(ctx), asFragment(ctx));

		// Special handling for access expressions with only 1 argument
		if(indices.length==1) {
			// Single argument can still be a list
			if(indices[0].isList()) {
				IntegerListExpression<?> index = ensureIntegerList(indices[0]);
				return ListAccess.filter(source, index);
			}
			// Single non-list argument -> pick element (ListAccess will choose best implementation for type)
			Expression<?> index = ensureNumerical(indices[0]);
			return ListAccess.atIndex(source, index);
		}

		// Generic case: list of index functions -> let ListAccess pick the best implementation
		Expression<?>[] index = ensureInteger(indices);
		return ListAccess.filter(source, ListAccess.wrapIndices(index));
	}

	Expression<?> processAnnotationAccess(AnnotationAccessContext ctx) {
		Expression<?>[] keys = processExpressionList(ctx.keys);

		if(keys.length<1)
			throw new QueryException(QueryErrorCode.INCORRECT_USE,
					"Annotation access needs at least 1 key argument: "+textOf(ctx), asFragment(ctx));

		if(keys.length==1) {
			if(keys[0].isList()) {
				//TODO
			}
			//TODO
		}

		ensureText(keys);
		//TODO implement
		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processCastExpression(CastExpressionContext ctx) {
		Expression<?> source = processExpression0(ctx.expression());

		TypeContext tctx = ctx.type();

		if(tctx.BOOLEAN()!=null) {
			return Conversions.toBoolean(source);
		} else if(tctx.STRING()!=null) {
			return Conversions.toText(source);
		} else if(tctx.INT()!=null) {
			return Conversions.toInteger(source);
		} else if(tctx.FLOAT()!=null) {
			return Conversions.toFloatingPoint(source);
		}

		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processWrappingExpression(WrappingExpressionContext ctx) {
		return processExpression0(unwrap(ctx));
	}

	/** Unwraps until a non wrapper context is encountered */
	private ExpressionContext unwrap(ExpressionContext ctx) {
		ExpressionContext ectx = ctx;
		while(ectx instanceof WrappingExpressionContext) {
			ectx = ((WrappingExpressionContext)ectx).expression();
		}
		return ectx;
	}

	Expression<?> processSetPredicate(SetPredicateContext ctx) {

		Expression<?> target = processExpression0(ctx.source);
		Expression<?>[] elements = processExpressionList(ctx.set);

		Expression<Primitive<Boolean>> setPred;

		if(ctx.all()!=null) {
			if(!target.isList())
				throw new QueryException(QueryErrorCode.INCORRECT_USE,
						"Cannot use the 'ALL IN' set predicate with a non-list "
						+ "target expression: "+textOf(ctx), asFragment(ctx));
			setPred = SetPredicates.allIn((ListExpression<?, ?>) target, elements);
		} else {
			setPred = SetPredicates.in(target, elements);
		}

		return maybeNegate(setPred, ctx.not()!=null);
	}

	Expression<?> processUnaryOp(UnaryOpContext ctx) {
		Expression<?> source = processExpression0(ctx.expression());

		if(ctx.EXMARK()!=null || ctx.NOT()!=null) {
			return UnaryOperations.not(ensureBoolean(source));
		} else if(ctx.MINUS()!=null) {
			return UnaryOperations.minus(ensureNumerical(source));
		} else if(ctx.TILDE()!=null) {
			return UnaryOperations.bitwiseNot(ensureNumerical(source));
		}

		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processMultiplicativeOp(MultiplicativeOpContext ctx) {
		Expression<?> left = ensureNumerical(processExpression0(ctx.left));
		Expression<?> right = ensureNumerical(processExpression0(ctx.right));

		AlgebraicOp op = null;
		if(ctx.STAR()!=null) {
			op = AlgebraicOp.MULT;
		} else if(ctx.SLASH()!=null) {
			op = AlgebraicOp.DIV;
		} else if(ctx.PERCENT()!=null) {
			op = AlgebraicOp.MOD;
		} else {
			failForUnhandledAlternative(ctx);
		}

		return BinaryOperations.numericalOp(op, left, right);
	}

	Expression<?> processAdditiveOp(AdditiveOpContext ctx) {
		Expression<?> left = processExpression0(ctx.left);
		Expression<?> right = processExpression0(ctx.right);

		AlgebraicOp op = null;
		if(ctx.PLUS()!=null) {
			op = AlgebraicOp.ADD;
		} else if(ctx.MINUS()!=null) {
			op = AlgebraicOp.SUB;
		} else {
			failForUnhandledAlternative(ctx);
		}

		if(left.isText() || right.isText()) {
			return processStringConcatenation(ctx);
		}

		return BinaryOperations.numericalOp(op,
				ensureNumerical(left), ensureNumerical(right));
	}

	private Expression<CharSequence> processStringConcatenation(AdditiveOpContext ctx) {
		/*
		 * Additive op in IQL is left associative, so we gonna add elements to
		 * the buffer starting from the right while descending down the parse tree.
		 */
		List<ExpressionContext> items = new ArrayList<>();

		ExpressionContext ectx = ctx;
		while(ectx instanceof AdditiveOpContext) {
			AdditiveOpContext actx = (AdditiveOpContext) ectx;
			items.add(actx.right);
			ectx = actx.left;
		}
		// Now ectx is the last left-directed child
		items.add(ectx);

		// Reverse list before processing
		Collections.reverse(items);

		Expression<CharSequence>[] elements = items.stream()
				// Basic processing
				.map(this::processExpression0)
				// Make sure we only concatenate strings
				.map(this::ensureText)
				// Expand nested concatenations for optimization
				.flatMap(exp -> {
					if(exp instanceof StringConcatenation) {
						return Stream.of(((StringConcatenation)exp).getElements());
					}
					return Stream.of(exp);
				})
				.toArray(Expression[]::new);

		return StringConcatenation.concat(elements);
	}

	Expression<?> processBitwiseOp(BitwiseOpContext ctx) {
		Expression<?> left = ensureInteger(processExpression0(ctx.left));
		Expression<?> right = ensureInteger(processExpression0(ctx.right));

		AlgebraicOp op = null;
		if(ctx.AMP()!=null) {
			op = AlgebraicOp.BIT_AND;
		} else if(ctx.PIPE()!=null) {
			op = AlgebraicOp.BIT_OR;
		} else if(ctx.CARET()!=null) {
			op = AlgebraicOp.BIT_XOR;
		} else if(ctx.SHIFT_LEFT()!=null) {
			op = AlgebraicOp.LSHIFT;
		} else if(ctx.SHIFT_RIGHT()!=null) {
			op = AlgebraicOp.RSHIFT;
		} else {
			failForUnhandledAlternative(ctx);
		}

		return BinaryOperations.numericalOp(op, left, right);
	}

	Expression<?> processComparisonOp(ComparisonOpContext ctx) {

		Expression<?> left = processExpression0(ctx.left);
		Expression<?> right = processExpression0(ctx.right);

		if(TypeInfo.isNumerical(left.getResultType()) && TypeInfo.isNumerical(right.getResultType())) {
			return processNumericalComparison(ctx, ensureNumerical(left), ensureNumerical(right));
		} else if(TypeInfo.isComparable(left.getResultType()) && TypeInfo.isComparable(right.getResultType())) {
			//TODO try to infer/ensure type compatibility between the two comparables
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Expression<Comparable> leftComp = (Expression<Comparable>)left;
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Expression<Comparable> rightComp = (Expression<Comparable>)right;
			return processComparableComparison(ctx, leftComp, rightComp);
		}

		throw new QueryException(QueryErrorCode.INCORRECT_USE,
				"Operands of a comparison must either be numerical or their result types must"
				+ " both implement java.lang.Comparable: "+textOf(ctx), asFragment(ctx));
	}

	private Expression<Primitive<Boolean>> processNumericalComparison(ComparisonOpContext ctx,
			Expression<?> left, Expression<?> right) {
		NumericalComparator comp = null;
		if(ctx.LT()!=null) {
			comp = NumericalComparator.LESS;
		} else if(ctx.LT_EQ()!=null) {
			comp = NumericalComparator.LESS_OR_EQUAL;
		} else if(ctx.GT()!=null) {
			comp = NumericalComparator.GREATER;
		} else if(ctx.GT_EQ()!=null) {
			comp = NumericalComparator.GREATER_OR_EQUAL;
		} else {
			failForUnhandledAlternative(ctx);
		}

		return BinaryOperations.numericalPred(comp, left, right);
	}

	@SuppressWarnings("rawtypes")
	private Expression<Primitive<Boolean>> processComparableComparison(ComparisonOpContext ctx,
			Expression<Comparable> left, Expression<Comparable> right) {
		ComparableComparator comp = null;
		if(ctx.LT()!=null) {
			comp = ComparableComparator.LESS;
		} else if(ctx.LT_EQ()!=null) {
			comp = ComparableComparator.LESS_OR_EQUAL;
		} else if(ctx.GT()!=null) {
			comp = ComparableComparator.GREATER;
		} else if(ctx.GT_EQ()!=null) {
			comp = ComparableComparator.GREATER_OR_EQUAL;
		} else {
			failForUnhandledAlternative(ctx);
		}

		return BinaryOperations.comparablePred(comp, left, right);
	}

	Expression<?> processStringOp(StringOpContext ctx) {
		Expression<CharSequence> target = ensureText(processExpression0(ctx.left));
		Expression<CharSequence> query = ensureText(processExpression0(ctx.right));

		StringOp op = null;
		boolean negate = false;

		if(ctx.MATCHES()!=null || ctx.NOT_MATCHES()!=null) {
			// Regex rules are more strict
			if(!query.isConstant())
				throw new QueryException(QueryErrorCode.INCORRECT_USE,
						"Query part of regex operation must be a constant", asFragment(ctx));
			op = StringOp.MATCHES;
			negate = ctx.NOT_MATCHES()!=null;
		} else if(ctx.CONTAINS()!=null || ctx.NOT_CONTAINS()!=null) {
			op = StringOp.CONTAINS;
			negate = ctx.NOT_CONTAINS()!=null;
		} else {
			return failForUnhandledAlternative(ctx);
		}

		Expression<Primitive<Boolean>> expression = isAllowUnicode() ?
				BinaryOperations.unicodeOp(op, getStringMode(), target, query)
				: BinaryOperations.asciiOp(op, getStringMode(), target, query);

		return maybeNegate(expression, negate);
	}

	Expression<?> processEqualityCheck(EqualityCheckContext ctx) {
		Expression<?> left = processExpression0(ctx.left);
		Expression<?> right = processExpression0(ctx.right);

		Expression<Primitive<Boolean>> expression;

		if(left.isNumerical() && right.isNumerical()) {
			// Strictly numerical check
			NumericalComparator comparator;
			if(ctx.EQ()!=null) {
				comparator = NumericalComparator.EQUALS;
			} else if(ctx.NOT_EQ()!=null) {
				comparator = NumericalComparator.NOT_EQUALS;
			} else
				return failForUnhandledAlternative(ctx);

			expression = BinaryOperations.numericalPred(comparator,
					ensureNumerical(left), ensureNumerical(right));

		} else if(left.isText() || right.isText()) {
			// Expanded string equality check

			StringOp op = StringOp.EQUALS;
			boolean negate = false;
			if(ctx.EQ()!=null) {
				// no-op
			} else if(ctx.NOT_EQ()!=null) {
				negate = true;
			} else
				return failForUnhandledAlternative(ctx);

			Expression<CharSequence> target = ensureText(left);
			Expression<CharSequence> query = ensureText(right);

			expression = isAllowUnicode() ?
					BinaryOperations.unicodeOp(op, getStringMode(), target, query)
					: BinaryOperations.asciiOp(op, getStringMode(), target, query);

			expression = maybeNegate(expression, negate);

		} else {
			// Basic (costly) object equality check

			EqualityPred pred;
			if(ctx.EQ()!=null) {
				pred = EqualityPred.EQUALS;
			} else if(ctx.NOT_EQ()!=null) {
				pred = EqualityPred.NOT_EQUALS;
			} else
				return failForUnhandledAlternative(ctx);

			expression = BinaryOperations.equalityPred(pred, left, right);
		}

		return expression;
	}

	Expression<?> processConjunction(ConjunctionContext ctx) {
		//TODO implement
		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processDisjunction(DisjunctionContext ctx) {
		//TODO implement
		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processTernaryOp(TernaryOpContext ctx) {
		//TODO implement
		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processForEach(ForEachContext ctx) {
		//TODO implement
		return failForUnhandledAlternative(ctx);
	}
}
