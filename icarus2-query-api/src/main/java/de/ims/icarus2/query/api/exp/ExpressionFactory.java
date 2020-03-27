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
package de.ims.icarus2.query.api.exp;

import static de.ims.icarus2.query.api.exp.EvaluationUtils.NO_ARGS;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castBoolean;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castComparable;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castFloatingPoint;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castInteger;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castIntegerList;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castItem;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castList;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.castText;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.collectTypes;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.decideType;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.unescape;
import static de.ims.icarus2.query.api.exp.EvaluationUtils.unquote;
import static de.ims.icarus2.query.api.iql.AntlrUtils.asFragment;
import static de.ims.icarus2.query.api.iql.AntlrUtils.cleanNumberLiteral;
import static de.ims.icarus2.query.api.iql.AntlrUtils.isContinuous;
import static de.ims.icarus2.query.api.iql.AntlrUtils.textOf;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.QuerySwitch;
import de.ims.icarus2.query.api.exp.BinaryOperations.AlgebraicOp;
import de.ims.icarus2.query.api.exp.BinaryOperations.ComparableComparator;
import de.ims.icarus2.query.api.exp.BinaryOperations.EqualityPred;
import de.ims.icarus2.query.api.exp.BinaryOperations.NumericalComparator;
import de.ims.icarus2.query.api.exp.BinaryOperations.StringMode;
import de.ims.icarus2.query.api.exp.BinaryOperations.StringOp;
import de.ims.icarus2.query.api.exp.EvaluationContext.AnnotationInfo;
import de.ims.icarus2.query.api.exp.Expression.IntegerListExpression;
import de.ims.icarus2.query.api.exp.Expression.ListExpression;
import de.ims.icarus2.query.api.exp.Expressions.PathProxy;
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
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ListStatementContext;
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
import de.ims.icarus2.util.Counter;
import de.ims.icarus2.util.MutablePrimitives.Primitive;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.lang.ClassUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class ExpressionFactory {

	/** Context and settings to be used for evaluating the query. */
	private final EvaluationContext context;

	/**
	 * Tracks certain aspects of the expression context.
	 * Note that tracking is only performed once the associated
	 * element has been successfully processed.
	 */
	private final Stats stats = new Stats();

	private static final Map<Class<? extends ParserRuleContext>, BiFunction<ExpressionFactory,ParserRuleContext, Expression<?>>>
		handlers = new Object2ObjectOpenHashMap<>();

	static {
		handlers.put(PrimaryExpressionContext.class, (f,ctx) -> f.processPrimary((PrimaryExpressionContext)ctx));
		handlers.put(PathAccessContext.class, (f,ctx) -> f.processPathAccess((PathAccessContext) ctx));
		handlers.put(MethodInvocationContext.class, (f,ctx) -> f.processMethodInvocation((MethodInvocationContext) ctx));
		handlers.put(ListAccessContext.class, (f,ctx) -> f.processListAccess((ListAccessContext) ctx));
		handlers.put(AnnotationAccessContext.class, (f,ctx) -> f.processAnnotationAccess((AnnotationAccessContext) ctx));
		handlers.put(CastExpressionContext.class, (f,ctx) -> f.processCastExpression((CastExpressionContext) ctx));
		handlers.put(WrappingExpressionContext.class, (f,ctx) -> f.processWrappingExpression((WrappingExpressionContext) ctx));
		handlers.put(SetPredicateContext.class, (f,ctx) -> f.processSetPredicate((SetPredicateContext) ctx));
		handlers.put(UnaryOpContext.class, (f,ctx) -> f.processUnaryOp((UnaryOpContext) ctx));
		handlers.put(MultiplicativeOpContext.class, (f,ctx) -> f.processMultiplicativeOp((MultiplicativeOpContext) ctx));
		handlers.put(AdditiveOpContext.class, (f,ctx) -> f.processAdditiveOp((AdditiveOpContext) ctx));
		handlers.put(BitwiseOpContext.class, (f,ctx) -> f.processBitwiseOp((BitwiseOpContext) ctx));
		handlers.put(ComparisonOpContext.class, (f,ctx) -> f.processComparisonOp((ComparisonOpContext) ctx));
		handlers.put(StringOpContext.class, (f,ctx) -> f.processStringOp((StringOpContext) ctx));
		handlers.put(EqualityCheckContext.class, (f,ctx) -> f.processEqualityCheck((EqualityCheckContext) ctx));
		handlers.put(ConjunctionContext.class, (f,ctx) -> f.processConjunction((ConjunctionContext) ctx));
		handlers.put(DisjunctionContext.class, (f,ctx) -> f.processDisjunction((DisjunctionContext) ctx));
		handlers.put(TernaryOpContext.class, (f,ctx) -> f.processTernaryOp((TernaryOpContext) ctx));
		handlers.put(ForEachContext.class, (f,ctx) -> f.processForEach((ForEachContext) ctx));
	}
	//TODO handle assignmentOp "exp AS (member | var)", a special expression that returns true if the assignment produces a value other than 0 or null

	public ExpressionFactory(EvaluationContext context) { this.context = requireNonNull(context); }

	public EvaluationContext getContext() { return context; }

	public Stats getStats() { return stats; }

	private boolean isAllowUnicode() {
		return !context.isSwitchSet(QuerySwitch.STRING_UNICODE_OFF);
	}

	private boolean isAllowAutoCast() {
		return !context.isSwitchSet(QuerySwitch.AUTOCAST_OFF);
	}

	private boolean isAllowEarlyExit() {
		return context.isSwitchSet(QuerySwitch.PREDICATES_OPTIMIZE_OFF);
	}

	private StringMode getStringMode() {
		StringMode stringMode = StringMode.DEFAULT;
		if(context.isSwitchSet(QuerySwitch.STRING_CASE_OFF)) {
			stringMode = StringMode.IGNORE_CASE;
		}
		return stringMode;
	}

	private static BiFunction<ExpressionFactory,ParserRuleContext, Expression<?>> handlerFor(ParserRuleContext ctx) {
		return Optional.ofNullable(handlers.get(ctx.getClass()))
				.orElseThrow(() -> new QueryException(QueryErrorCode.AST_ERROR,
						"Unknown query fragment: "+ctx.getClass().getCanonicalName(), asFragment(ctx)));
	}

	public Expression<?> processExpression(ExpressionContext ctx) {
		Expression<?> expression = processAndResolveExpression0(ctx);
		//TODO call optimize() already?
		return expression;
	}

	private Expression<?> processExpression0(ExpressionContext ctx, boolean resolveProxy) {
		Expression<?> expression = handlerFor(ctx).apply(this, ctx);

		if(expression==null)
			throw new QueryException(GlobalErrorCode.INTERNAL_ERROR,
					"Failed to parse into proper expression: "+textOf(ctx), asFragment(ctx));

		if(expression.isProxy() && resolveProxy) {
			expression = resolveProxy(expression);
		}

		return expression;
	}

	private Expression<?> processAndResolveExpression0(ExpressionContext ctx) {
		return processExpression0(ctx, true);
	}

	private Expression<?> resolveProxy(Expression<?> proxy) {
		if(proxy instanceof PathProxy) {
			// All we can do here is delegate to the context to resolve the path
			PathProxy<?> path = (PathProxy<?>) proxy;
			Expression<?> source = path.getSource().orElse(null);
			String name = path.getName();

			// Simple identifiers inside element context can resolve to annotation keys
			Optional<Assignable<? extends Item>> item = context.getElementStore();
			if(source==null && item.isPresent()) {
				QualifiedIdentifier identifier = QualifiedIdentifier.parseIdentifier(name);
				Optional<AnnotationInfo> annotation = context.findAnnotation(identifier);
				if(annotation.isPresent()) {
					Expression<?> exp = AnnotationAccess.of(item.get(), annotation.get());
					stats.annotations.increment(name);
					return exp;
				}
				// Otherwise it _must_ be a proper path in the object graph
			}

			//TODO can we infer expected type efficiently for better filtering?
			Expression<?> result = context.resolve(source, name, TypeFilter.ALL).orElseThrow(
					() -> new QueryException(QueryErrorCode.UNKNOWN_IDENTIFIER,
							"Failed to resolve reference: "+name, asFragment(path.getContext())));

			// Only track root references of a path
			if(source==null) {
				stats.references.increment(name);
			}

			return result;
		}

		throw new QueryException(GlobalErrorCode.INTERNAL_ERROR,
				"Unknown proxy class: "+proxy.getClass());
	}

	Expression<?>[] processExpressionList(ExpressionListContext ctx) {
		return ctx.expression().stream()
				.map(this::processAndResolveExpression0)
				.toArray(Expression[]::new);
	}

	/** Insurance against future changes in the IQL grammar that we missed */
	private static <T> T failForUnhandledAlternative(ParserRuleContext ctx) {
		throw new QueryException(QueryErrorCode.AST_ERROR,
				"Unknown alterative: "+ctx.getClass().getCanonicalName(), asFragment(ctx));
	}

	private Expression<?> ensureNumerical(Expression<?> source) {
		if(!source.isNumerical())
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a numerical type: "+source.getResultType());
		// No explicit autocast possible here
		return source;
	}

	private Expression<Primitive<Long>> ensureInteger(Expression<?> source) {
		if(!source.isInteger()) {
			if(isAllowAutoCast()) {
				return Conversions.toInteger(source);
			}

			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not an integer expression: "+source.getResultType());
		}
		return castInteger(source);
	}

	private Expression<Primitive<Double>> ensureFloatingPoint(Expression<?> source) {
		if(!source.isFloatingPoint()) {
			if(isAllowAutoCast()) {
				return Conversions.toFloatingPoint(source);
			}

			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a floating point expression: "+source.getResultType());
		}
		return castFloatingPoint(source);
	}

	private Expression<Primitive<Boolean>> ensureBoolean(Expression<?> source) {
		if(!source.isBoolean()) {
			if(isAllowAutoCast()) {
				return Conversions.toBoolean(source);
			}

			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a boolean type: "+source.getResultType());
		}
		return castBoolean(source);
	}

	//TODO add argument to enable conversion
	private Expression<CharSequence> ensureText(Expression<?> source) {
		if(!source.isText()) {
			if(isAllowAutoCast()) {
				return Conversions.toText(source);
			}

			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a text type: "+source.getResultType());
		}
		//TODO take string conversion into account!!
		return castText(source);
	}

	private Expression<?> ensureType(TypeInfo type, Expression<?> source) {
		if(TypeInfo.isInteger(type)) {
			return ensureInteger(source);
		} else if(TypeInfo.isFloatingPoint(type)) {
			return ensureFloatingPoint(source);
		} else if(TypeInfo.isBoolean(type)) {
			return ensureBoolean(source);
		} else if(TypeInfo.isText(type)) {
			return ensureText(source);
		}

		return source;
	}

	private Expression<? extends Item> ensureItem(Expression<?> source) {
		if(!source.isMember())
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a mmeber type: "+source.getResultType());
		return castItem(source);
	}

	private Expression<Primitive<Long>>[] ensureInteger(Expression<?>[] source) {
		return Stream.of(source)
				.map(this::ensureInteger)
				.toArray(Expression[]::new);
	}

	@SuppressWarnings("unused")
	@Deprecated
	private Expression<?>[] ensureExpandableInteger(Expression<?>[] source) {
		return Stream.of(source)
				.map(exp -> {
					if(exp.isList()) {
						return ensureIntegerList(exp);
					}

					return ensureInteger(exp);
				})
				.toArray(Expression[]::new);
	}

	private Expression<Primitive<Double>>[] ensureFloatingPoint(Expression<?>[] source) {
		return Stream.of(source)
				.map(this::ensureFloatingPoint)
				.toArray(Expression[]::new);
	}

	private Expression<Primitive<Boolean>>[] ensureBoolean(Expression<?>[] source) {
		return Stream.of(source)
				.map(this::ensureBoolean)
				.toArray(Expression[]::new);
	}

	private Expression<CharSequence>[] ensureText(Expression<?>[] source) {
		return Stream.of(source)
				.map(this::ensureText)
				.toArray(Expression[]::new);
	}

	private Expression<?>[] ensureType(TypeInfo type, Expression<?>[] source) {
		if(TypeInfo.isInteger(type)) {
			return ensureInteger(source);
		} else if(TypeInfo.isFloatingPoint(type)) {
			return ensureFloatingPoint(source);
		} else if(TypeInfo.isBoolean(type)) {
			return ensureBoolean(source);
		} else if(TypeInfo.isText(type)) {
			return ensureText(source);
		}

		return source;
	}

	private ListExpression<?, ?> ensureList(Expression<?> source) {
		if(!source.isList()) {
			//TODO check if result type of source is expandable

			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not a list type: "+source.getResultType());
		}
		return castList(source);
	}

	private IntegerListExpression<?> ensureIntegerList(Expression<?> source) {
		ListExpression<?, ?> list = ensureList(source);
		if(!TypeInfo.isInteger(list.getElementType()))
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					"Not an integer list type: "+source.getResultType());
		return castIntegerList(list);
	}

	private static Expression<Primitive<Boolean>> maybeNegate(Expression<Primitive<Boolean>> source, boolean negate) {
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
		} else if(pctx.listStatement()!=null) {
			return processList(pctx.listStatement());
		} else if(pctx.reference()!=null) {
			return processReference(pctx.reference());
		}

		return failForUnhandledAlternative(pctx);
	}

	private Expression<CharSequence> processStringLiteral(TerminalNode node) {
		String content = textOf(node);
		content = unquote(content);
		content = unescape(content);
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
		Expression<?> result = null;
		if(ctx.variableName()!=null) {
			// Grab identifier and let context resolve actual variable (might create one)
			String name = processIdentifier(ctx.variableName().Identifier());
			result = context.getVariable(name);
			stats.variables.increment(name);
		} else if(ctx.member()!=null) {
			// Grab identifier and let context resolve it to member expression
			String name = processIdentifier(ctx.member().Identifier());
			result = context.getMember(name).orElseThrow(
					() -> new QueryException(QueryErrorCode.UNKNOWN_IDENTIFIER,
							"No member available for name: "+name, asFragment(ctx)));
			stats.members.increment(name);
		} else if(ctx.Identifier()!=null) {
			//TODO check first if it is a special marker (e.g. 'rightmost') and then send to EvaluationContext (after checking for negated context)
			// Wrap into source-less path proxy for delayed resolution
			result = Expressions.pathProxy(processIdentifier(ctx.Identifier()), ctx);
		} else if(ctx.qualifiedIdentifier()!=null) {
			// Fetch key and current item context and create annotation access
			String key = processIdentifier(ctx.qualifiedIdentifier());
			Expression<? extends Item> item = context.getElementStore().orElseThrow(
					() -> new QueryException(QueryErrorCode.INCORRECT_USE,
							"No surrounding item context available", asFragment(ctx)));
			result = AnnotationAccess.of(item, context, Literals.of(key));
			stats.annotations.increment(key);
		} else
			failForUnhandledAlternative(ctx);

		return result;
	}

	private ListExpression<?,?> processList(ListStatementContext ctx) {
		Expression<?>[] elements = ctx.expressionList()!=null ?
				processExpressionList(ctx.expressionList()) : NO_ARGS;

		TypeInfo elementType = null;

		if(ctx.type()!=null) {
			// Honor explicit type definition
			elementType = processType(ctx.type());
		} else if(elements.length>0) {
			// Otherwise try best-effort option to derive type info
			Set<TypeInfo> types = collectTypes(elements);

			if(types.size()>1) {
				// Follow a strict type hierarchy
				elementType = decideType(types).orElse(TypeInfo.GENERIC);
			} else {
				elementType = CollectionUtils.first(types);
			}
		} else
			throw new QueryException(QueryErrorCode.INCORRECT_USE,
					"Empty array is missing explicit type declaration", asFragment(ctx));

		if(elements.length==0) {
			return ArrayLiterals.emptyArray(elementType);
		}

		// Force conversion if needed
		elements = ensureType(elementType, elements);

		return ListAccess.wrap(elements);
	}

	private void checkIdentifier(String id) {
		if(id.length()>255)
			throw new QueryException(QueryErrorCode.INCORRECT_USE,
					"Identifier exceeds limit of 255 characters: "+id);
	}

	private String processIdentifier(TerminalNode token) {
		String id = textOf(token);
		checkIdentifier(id);
		return id;
	}

	private String processIdentifier(QualifiedIdentifierContext ctx) {
		if(!isContinuous(ctx))
			throw new QueryException(QueryErrorCode.NON_CONTINUOUS_TOKEN,
					"Qualified identifier parts are not continuous (must not contain whitespaces): "+textOf(ctx),
					asFragment(ctx));
		checkIdentifier(textOf(ctx.hostId));
		checkIdentifier(textOf(ctx.elementId));
		return textOf(ctx);
	}

	Expression<?> processPathAccess(PathAccessContext ctx) {
		Expression<?> source = processAndResolveExpression0(ctx.source);

		// We only wrap into proxy here for delayed resolution
		return Expressions.pathProxy(source, processIdentifier(ctx.Identifier()), ctx);
	}

	Expression<?> processMethodInvocation(MethodInvocationContext ctx) {
		Expression<?>[] arguments = ctx.arguments==null ? NO_ARGS : processExpressionList(ctx.arguments);
		Expression<?> source = processExpression0(ctx.source, false);

		String name;

		if(source instanceof PathProxy) {
			// Unwrap proxy
			PathProxy<?> path = (PathProxy<?>) source;
			name = path.getName();
			source = path.getSource().orElse(null);
		} else
			throw new QueryException(GlobalErrorCode.INTERNAL_ERROR,
					"Unknown proxy class: "+source.getClass(), asFragment(ctx));

		// Delegate to context
		//TODO can we infer result type efficiently for better filtering?
		return context.resolve(source, name, TypeFilter.ALL, arguments).orElseThrow(
				() -> new QueryException(QueryErrorCode.UNKNOWN_IDENTIFIER,
						"Failed to resolve method: "+name, asFragment(ctx)));
	}

	Expression<?> processListAccess(ListAccessContext ctx) {
		ListExpression<?, ?> source = ensureList(processAndResolveExpression0(ctx.source));
		Expression<?>[] indices = processExpressionList(ctx.indices);

		if(indices.length<1)
			// TODO actually not possible due to IQL rule
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
		Expression<?> source = processAndResolveExpression0(ctx.source);
		Expression<?>[] arguments = processExpressionList(ctx.keys);

		if(arguments.length<1)
			throw new QueryException(QueryErrorCode.INCORRECT_USE,
					"Annotation access needs at least 1 key argument: "+textOf(ctx), asFragment(ctx));

		// AnnotationAccess handles the single-argument specialization
		return AnnotationAccess.of(ensureItem(source), context, ensureText(arguments));
	}

	private static TypeInfo processType(TypeContext ctx) {
		if(ctx.BOOLEAN()!=null) {
			return TypeInfo.BOOLEAN;
		} else if(ctx.STRING()!=null) {
			return TypeInfo.TEXT;
		} else if(ctx.INT()!=null) {
			return TypeInfo.INTEGER;
		} else if(ctx.FLOAT()!=null) {
			return TypeInfo.FLOATING_POINT;
		}

		return failForUnhandledAlternative(ctx);
	}

	Expression<?> processCastExpression(CastExpressionContext ctx) {
		Expression<?> source = processAndResolveExpression0(ctx.expression());

		TypeInfo type = processType(ctx.type());

		return Conversions.to(type, source);
	}

	Expression<?> processWrappingExpression(WrappingExpressionContext ctx) {
		return processAndResolveExpression0(unwrap(ctx));
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

		Expression<?> target = processAndResolveExpression0(ctx.source);
		Expression<?>[] elements = processExpressionList(ctx.listStatement().expressionList());

		Expression<Primitive<Boolean>> setPred;
		boolean negated = ctx.not()!=null;
		boolean all = ctx.all()!=null;

		if(all) {
			if(!target.isList())
				throw new QueryException(QueryErrorCode.INCORRECT_USE,
						"Cannot use the 'ALL IN' set predicate with a non-list "
						+ "target expression: "+textOf(ctx), asFragment(ctx));

			if(negated) {
				setPred = SetPredicates.allNotIn((ListExpression<?, ?>) target, elements);
			} else {
				setPred = SetPredicates.allIn((ListExpression<?, ?>) target, elements);
			}
		} else {
			setPred = SetPredicates.in(target, elements);
		}

		// Outer negation only needed when we're not in the 'all not in' mode
		return maybeNegate(setPred, negated && !all);
	}

	Expression<?> processUnaryOp(UnaryOpContext ctx) {
		Expression<?> source = processAndResolveExpression0(ctx.expression());

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
		Expression<?> left = ensureNumerical(processAndResolveExpression0(ctx.left));
		Expression<?> right = ensureNumerical(processAndResolveExpression0(ctx.right));

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
		Expression<?> left = processAndResolveExpression0(ctx.left);
		Expression<?> right = processAndResolveExpression0(ctx.right);

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
				.map(this::processAndResolveExpression0)
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
		Expression<?> left = ensureInteger(processAndResolveExpression0(ctx.left));
		Expression<?> right = ensureInteger(processAndResolveExpression0(ctx.right));

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

		Expression<?> left = processAndResolveExpression0(ctx.left);
		Expression<?> right = processAndResolveExpression0(ctx.right);

		if(TypeInfo.isNumerical(left.getResultType()) && TypeInfo.isNumerical(right.getResultType())) {
			return processNumericalComparison(ctx, ensureNumerical(left), ensureNumerical(right));
		} else if(left.isText() || right.isText()) {
			return processTextComparison(ctx, ensureText(left), ensureText(right));
		} else if(TypeInfo.isComparable(left.getResultType()) && TypeInfo.isComparable(right.getResultType())) {
			//TODO try to infer/ensure type compatibility between the two Comparable instances
			@SuppressWarnings({ "rawtypes" })
			Expression<Comparable> leftComp = castComparable(left);
			@SuppressWarnings({ "rawtypes" })
			Expression<Comparable> rightComp = castComparable(right);
			return processComparableComparison(ctx, leftComp, rightComp);
		}

		throw new QueryException(QueryErrorCode.INCORRECT_USE,
				"Operands of a comparison must either be numerical, textual or their result types must"
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

	private Expression<Primitive<Boolean>> processTextComparison(ComparisonOpContext ctx,
			Expression<?> left, Expression<?> right) {
		StringOp comp = null;
		if(ctx.LT()!=null) {
			comp = StringOp.LESS;
		} else if(ctx.LT_EQ()!=null) {
			comp = StringOp.LESS_OR_EQUAL;
		} else if(ctx.GT()!=null) {
			comp = StringOp.GREATER;
		} else if(ctx.GT_EQ()!=null) {
			comp = StringOp.GREATER_OR_EQUAL;
		} else {
			failForUnhandledAlternative(ctx);
		}

		return isAllowUnicode() ? BinaryOperations.unicodeOp(comp, getStringMode(), left, right)
				: BinaryOperations.asciiOp(comp, getStringMode(), left, right);
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
		Expression<CharSequence> target = ensureText(processAndResolveExpression0(ctx.left));
		Expression<CharSequence> query = ensureText(processAndResolveExpression0(ctx.right));

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
		Expression<?> left = processAndResolveExpression0(ctx.left);
		Expression<?> right = processAndResolveExpression0(ctx.right);

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
		List<Expression<?>> elements = new ArrayList<>();

		// Try to collapse any conjunctive sequence into a single term
		ExpressionContext tail = ctx;
		while(tail instanceof ConjunctionContext) {
			ConjunctionContext cctx = (ConjunctionContext) tail;
			elements.add(processAndResolveExpression0(cctx.left));
			tail = unwrap(cctx.right);
		}
		// Now add the final dangling expression
		elements.add(processAndResolveExpression0(tail));

		return LogicalOperators.conjunction(elements.toArray(NO_ARGS), isAllowEarlyExit());
	}

	Expression<?> processDisjunction(DisjunctionContext ctx) {
		List<Expression<?>> elements = new ArrayList<>();

		// Try to collapse any disjunctive sequence into a single term
		ExpressionContext tail = ctx;
		while(tail instanceof DisjunctionContext) {
			DisjunctionContext cctx = (DisjunctionContext) tail;
			elements.add(processAndResolveExpression0(cctx.left));
			tail = unwrap(cctx.right);
		}
		// Now add the final dangling expression
		elements.add(processAndResolveExpression0(tail));

		return LogicalOperators.disjunction(elements.toArray(NO_ARGS), isAllowEarlyExit());
	}

	Expression<?> processTernaryOp(TernaryOpContext ctx) {
		Expression<?> condition = ensureBoolean(processAndResolveExpression0(ctx.condition));
		Expression<?> option1 = processAndResolveExpression0(ctx.optionTrue);
		Expression<?> option2 = processAndResolveExpression0(ctx.optionFalse);

		TypeInfo type1 = option1.getResultType();
		TypeInfo type2 = option2.getResultType();
		TypeInfo type = null;

		if(type1.equals(type2)) {
			type = type1;
		}

		/*
		 *  If any of the expressions has an auto-castable type, try conversion.
		 *  This will also take care of primitive types.
		 */
		Optional<TypeInfo> coreType = decideType(set(type1, type2));
		if(coreType.isPresent()) {
			type = coreType.get();
			// Apply auto casts
			option1 = ensureType(type, option1);
			option2 = ensureType(type, option2);
		}

		// Easy mode failed, so we have to run a real search for types
		if(type==null) {
			type = findCommonType(type1, type2);
		}

		return TernaryOperation.of(type, condition, option1, option2);
	}

	private static TypeInfo findCommonType(TypeInfo type1, TypeInfo type2) {
		Set<Class<?>> typeCandidates = ClassUtils.commonSuperclasses(
				type1.getType(), type2.getType());
		if(typeCandidates.isEmpty())
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH, String.format(
					"No common super class or interface available for %s and %s", type1, type2));

		// Try to find the first actual class the types have in common
		Optional<Class<?>> properClass = typeCandidates.stream()
				.filter(c -> !c.isInterface())
				.findFirst();
		if(properClass.isPresent()) {
			return TypeInfo.of(properClass.get());
		}

		// No proper class, so we only have interfaces floating around
		Class<?>[] interfaces = typeCandidates.toArray(new Class[typeCandidates.size()]);
		Class<?> proxyClass = Proxy.getProxyClass(TernaryOperation.class.getClassLoader(), interfaces);
		return TypeInfo.of(proxyClass);
	}

	Expression<?> processForEach(ForEachContext ctx) {
		//TODO implement
		return failForUnhandledAlternative(ctx);
	}

	public static class Stats {
		private final Counter<String> members = new Counter<>();
		private final Counter<String> variables = new Counter<>();
		private final Counter<String> annotations = new Counter<>();
		private final Counter<String> references = new Counter<>();

		//TODO add public methods for reading the content in a save manner
	}
}
