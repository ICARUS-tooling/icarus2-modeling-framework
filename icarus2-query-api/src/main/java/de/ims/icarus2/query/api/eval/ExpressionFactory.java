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

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.eval.BinaryOperations.AlgebraicOp;
import de.ims.icarus2.query.api.eval.BinaryOperations.ComparableComparator;
import de.ims.icarus2.query.api.eval.BinaryOperations.NumericalComparator;
import de.ims.icarus2.query.api.eval.Expression.BooleanExpression;
import de.ims.icarus2.query.api.eval.Expression.NumericalExpression;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.AdditiveOpContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.AnnotationAccessContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ArrayAccessContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.BitwiseOpContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.BooleanLiteralContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.CastExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ComparisonOpContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ConjunctionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.DisjunctionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.EqualityCheckContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.FloatingPointLiteralContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ForEachContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.IntegerLiteralContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.MethodInvocationContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.MultiplicativeOpContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.NullLiteralContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.PathAccessContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.PrimaryContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.PrimaryExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ReferenceContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.SetPredicateContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.StringOpContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.TernaryOpContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.UnaryOpContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.WrappingExpressionContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class ExpressionFactory {

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
		handlers.put(ArrayAccessContext.class, ctx -> processArrayAccess((ArrayAccessContext) ctx));
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

	/** Insurance against changes in the IQL grammar that we missed */
	private <T> T failForUnhandledAlternative(ParserRuleContext ctx) {
		throw new QueryException(QueryErrorCode.AST_ERROR,
				"Unknown alterative: "+ctx.getClass().getCanonicalName(), asFragment(ctx));
	}

	private NumericalExpression ensureNumerical(Expression<?> source) {
		if(!TypeInfo.isNumerical(source.getResultType()))
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a numerical type: "+source.getResultType());
		//TODO maybe validate via instanceof ?
		return (NumericalExpression)source;
	}

	private BooleanExpression ensureBoolean(Expression<?> source) {
		if(!TypeInfo.isBoolean(source.getResultType()))
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a boolean type: "+source.getResultType());
		//TODO take switches and general boolean type conversion into account!!
		//TODO maybe validate via instanceof ?
		return (BooleanExpression)source;
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

	private BooleanExpression processBooleanLiteral(BooleanLiteralContext ctx) {
		return Literals.of(ctx.TRUE()!=null);
	}

	private NumericalExpression processIntegerLiteral(IntegerLiteralContext ctx) {
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

	private NumericalExpression processFloatingPointLiteral(FloatingPointLiteralContext ctx) {
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

		} else if(ctx.qualifiedIdentifier()!=null) {

		}

		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processPathAccess(PathAccessContext ctx) {
		//TODO implement
		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processMethodInvocation(MethodInvocationContext ctx) {
		//TODO implement
		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processArrayAccess(ArrayAccessContext ctx) {
		//TODO implement
		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processAnnotationAccess(AnnotationAccessContext ctx) {
		//TODO implement
		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processCastExpression(CastExpressionContext ctx) {
		//TODO implement
		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processWrappingExpression(WrappingExpressionContext ctx) {
		return processExpression0(ctx.expression());
	}

	Expression<?> processSetPredicate(SetPredicateContext ctx) {
		//TODO implement
		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processUnaryOp(UnaryOpContext ctx) {
		//TODO implement
		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processMultiplicativeOp(MultiplicativeOpContext ctx) {
		NumericalExpression left = ensureNumerical(processExpression0(ctx.left));
		NumericalExpression right = ensureNumerical(processExpression0(ctx.right));

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
		NumericalExpression left = ensureNumerical(processExpression0(ctx.left));
		NumericalExpression right = ensureNumerical(processExpression0(ctx.right));

		AlgebraicOp op = null;
		if(ctx.PLUS()!=null) {
			op = AlgebraicOp.ADD;
		} else if(ctx.MINUS()!=null) {
			op = AlgebraicOp.SUB;
		} else {
			failForUnhandledAlternative(ctx);
		}

		return BinaryOperations.numericalOp(op, left, right);
	}

	Expression<?> processBitwiseOp(BitwiseOpContext ctx) {
		NumericalExpression left = ensureNumerical(processExpression0(ctx.left));
		NumericalExpression right = ensureNumerical(processExpression0(ctx.right));

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
			//TODO try to infer type compatibility between the two comparables
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Expression<Comparable> leftComp = (Expression<Comparable>)left;
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Expression<Comparable> rightComp = (Expression<Comparable>)right;
			return processComparableComparison(ctx, leftComp, rightComp);
		}
		//TODO provide error information
		return failForUnhandledAlternative(ctx);
	}

	private BooleanExpression processNumericalComparison(ComparisonOpContext ctx,
			NumericalExpression left, NumericalExpression right) {
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
	private BooleanExpression processComparableComparison(ComparisonOpContext ctx,
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
		//TODO implement
		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processEqualityCheck(EqualityCheckContext ctx) {
		//TODO implement
		return failForUnhandledAlternative(ctx);
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
