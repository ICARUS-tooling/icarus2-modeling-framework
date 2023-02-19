/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.query.api.iql.AntlrUtils.asFeatureException;
import static de.ims.icarus2.query.api.iql.AntlrUtils.asSyntaxException;
import static de.ims.icarus2.query.api.iql.AntlrUtils.createParser;
import static de.ims.icarus2.query.api.iql.AntlrUtils.isContinuous;
import static de.ims.icarus2.query.api.iql.AntlrUtils.textOf;
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.collections.ArrayUtils.asSet;
import static de.ims.icarus2.util.strings.StringUtil.isNullOrEmpty;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;

import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.ReportBuilder;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.iql.AntlrUtils;
import de.ims.icarus2.query.api.iql.IqlBinding;
import de.ims.icarus2.query.api.iql.IqlConstraint;
import de.ims.icarus2.query.api.iql.IqlConstraint.BooleanOperation;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlPredicate;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlTerm;
import de.ims.icarus2.query.api.iql.IqlElement;
import de.ims.icarus2.query.api.iql.IqlElement.EdgeType;
import de.ims.icarus2.query.api.iql.IqlElement.IqlEdge;
import de.ims.icarus2.query.api.iql.IqlElement.IqlElementDisjunction;
import de.ims.icarus2.query.api.iql.IqlElement.IqlGrouping;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement;
import de.ims.icarus2.query.api.iql.IqlElement.IqlSequence;
import de.ims.icarus2.query.api.iql.IqlElement.IqlTreeNode;
import de.ims.icarus2.query.api.iql.IqlExpression;
import de.ims.icarus2.query.api.iql.IqlGroup;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlLane.LaneType;
import de.ims.icarus2.query.api.iql.IqlMarker;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerCall;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerExpression;
import de.ims.icarus2.query.api.iql.IqlMarker.MarkerExpressionType;
import de.ims.icarus2.query.api.iql.IqlObjectIdGenerator;
import de.ims.icarus2.query.api.iql.IqlPayload;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryType;
import de.ims.icarus2.query.api.iql.IqlQuantifier;
import de.ims.icarus2.query.api.iql.IqlQuantifier.Quantifiable;
import de.ims.icarus2.query.api.iql.IqlQuantifier.QuantifierModifier;
import de.ims.icarus2.query.api.iql.IqlQuantifier.QuantifierType;
import de.ims.icarus2.query.api.iql.IqlQueryElement;
import de.ims.icarus2.query.api.iql.IqlReference;
import de.ims.icarus2.query.api.iql.IqlReference.ReferenceType;
import de.ims.icarus2.query.api.iql.IqlResult;
import de.ims.icarus2.query.api.iql.IqlSorting;
import de.ims.icarus2.query.api.iql.IqlSorting.Order;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.query.api.iql.IqlType;
import de.ims.icarus2.query.api.iql.IqlUnique;
import de.ims.icarus2.query.api.iql.NodeArrangement;
import de.ims.icarus2.query.api.iql.antlr.IQLParser;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.BindingContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.BindingsListContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ConjunctionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ConstraintContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.DisjunctionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.DummyNodeContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.EdgeContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ElementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ElementDisjunctionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ElementGroupingContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ElementSequenceContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.EmptyEdgeContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.FilledEdgeContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.GraphFragmentContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.GroupExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.GroupStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.HitsLimitContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.LaneStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.LeftEdgePartContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.MarkerCallContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.MarkerConjunctionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.MarkerDisjunctionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.MarkerWrappingContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.MatchFlagContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.MemberContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.MemberLabelContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.NodeArrangementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.NodeContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.NodeStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.OrderExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.PayloadStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.PositionArgumentContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.PositionMarkerContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ProperNodeContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.QuantifierContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ResultStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.RightEdgePartContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.SelectionStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.SimpleQuantifierContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.SingleNodeContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.StructuralConstraintContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.VariableContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.WrappingExpressionContext;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.id.StaticIdentity;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

/**
 * Helper for processing the actual content of a query.
 *
 * @author Markus Gärtner
 *
 */
@NotThreadSafe
public class QueryProcessor {

	private static final Identity REPORT_IDENTITY = new StaticIdentity(
			QueryProcessor.class.getSimpleName(), "Query Processor");

	private final IqlObjectIdGenerator idGenerator = new IqlObjectIdGenerator();

	private final ReportBuilder<ReportItem> reportBuilder = ReportBuilder.builder(REPORT_IDENTITY);

	private final boolean ignoreWarnings;
	private final boolean keepRedundantGrouping;
	private final boolean defaultOrderedSequence;

	public QueryProcessor(Set<Option> options) {
		requireNonNull(options);
		ignoreWarnings = options.contains(Option.IGNORE_WARNINGS);
		keepRedundantGrouping = options.contains(Option.KEEP_REDUNDANT_GROUPING);
		defaultOrderedSequence = options.contains(Option.DEFAULT_ORDERED_SEQUENCE);
	}

	public QueryProcessor(Option...options) {
		this(asSet(options));
	}

	/**
	 * Options to customize the query parsing process.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public enum Option {
		/** Warnings will not cause the query parsing to fail */
		IGNORE_WARNINGS,
		/**
		 * {@link IqlGrouping} instances without quantifiers will not be unwrapped.
		 * This option mainly exists to control the structure of the state machine
		 * during testing.
		 */
		KEEP_REDUNDANT_GROUPING,
		/**
		 * {@link IqlSequence} instances without any order-related {@link NodeArrangement}
		 * will have their arrangement set to {@link NodeArrangement#ORDERED}.
		 */
		@VisibleForTesting
		DEFAULT_ORDERED_SEQUENCE,
		;
	}

	/**
	 * Parses the embedded textual representations for query payload,
	 * grouping and query instructions, if available.
	 *
	 * @param stream the raw query
	 * @throws QueryException if any of the sections to be processed
	 * has already been processed previously or if there was an error
	 * when parsing them.
	 */
	public void parseStream(IqlStream stream) {
		requireNonNull(stream);
		checkState("Query processed", !stream.isProcessed());

		String rawPayload = stream.getRawPayload().orElse(null);
		if(!isNullOrEmpty(rawPayload)) {
			stream.setPayload(processPayload(rawPayload));
		}

		String rawGrouping = stream.getRawGrouping().orElse(null);
		if(!isNullOrEmpty(rawGrouping)) {
			processGrouping(rawGrouping).forEach(stream::addGrouping);
		}
		String rawResult = stream.getRawResult().orElse(null);
		if(!isNullOrEmpty(rawResult)) {
			processResult(rawResult, stream.getResult(), stream.isPrimary());
		}

		stream.markProcessed();
	}

	public List<IqlGroup> processGrouping(String rawGrouping) {
		checkNotEmpty(rawGrouping);

		IQLParser parser = createParser(rawGrouping, "rawGrouping");
		try {
			return new GroupProcessor().processGroupStatement(parser.groupStatement());
		} catch(RecognitionException e) {
			throw asSyntaxException(e, "Failed to parse 'rawGrouping'");
		}
	}

	public void processResult(String rawResult, IqlResult result, boolean primary) {
		checkNotEmpty(rawResult);

		IQLParser parser = createParser(rawResult, "rawResult");
		try {
			new ResultProcessor().processResult(parser.resultStatement(), result, primary);
		} catch(RecognitionException e) {
			throw asSyntaxException(e, "Failed to parse 'rawResult'");
		}
	}

	public IqlPayload processPayload(String rawPayload) {
		checkNotEmpty(rawPayload);

		IQLParser parser = createParser(rawPayload, "rawPayload");
		try {
			PayloadStatementContext ctx = parser.payloadStatement();

			if(ctx.ALL()!=null) {
				IqlPayload payload = new IqlPayload();
				// If we are asked to return ALL we don't need to bother with bindings or constraints
				payload.setQueryType(QueryType.ALL);
				return payload;
			}

			return new PayloadProcessor().processPayloadStatement(ctx);

		} catch(RecognitionException e) {
			throw asSyntaxException(e, "Failed to parse 'rawPayload'");
		}
	}

	/** Insurance against changes in the IQL grammar that we missed */
	private <E extends IqlQueryElement> E failForUnhandledAlternative(ParserRuleContext ctx) {
		throw asFeatureException(ctx, "Unhandled alternative of type "+ctx.getClass());
	}

	private void genId(IqlUnique target) {
		target.setId(idGenerator.generateId(target));
	}

	private IqlExpression processExpression(ExpressionContext ctx) {
		IqlExpression expression = new IqlExpression();
		expression.setContent(textOf(ctx));
		return expression;
	}

	private IqlReference processVariableName(VariableContext ctx) {
		IqlReference variable = new IqlReference();

		genId(variable);
		variable.setReferenceType(ReferenceType.VARIABLE);
		variable.setName(textOf(ctx.Identifier()));

		return variable;
	}

	private class GroupProcessor {

		private List<IqlGroup> processGroupStatement(GroupStatementContext gctx) {
			List<IqlGroup> groups = new ArrayList<>();
			for (GroupExpressionContext ectx : gctx.groupExpression()) {
				groups.add(processGroupExpression(ectx));
			}
			return groups;
		}

		private IqlGroup processGroupExpression(GroupExpressionContext ctx) {
			IqlGroup group = new IqlGroup();
			genId(group);

			group.setGroupBy(processExpression(ctx.selector));
			group.setLabel(textOf(ctx.label));

			Optional.ofNullable(ctx.filter)
				.map(QueryProcessor.this::processExpression)
				.ifPresent(group::setFilterOn);
			Optional.ofNullable(ctx.defaultValue)
				.map(QueryProcessor.this::processExpression)
				.ifPresent(group::setDefaultValue);

			group.checkIntegrity();

			return group;
		}

	}

	private class ResultProcessor {


		private void processResult(ResultStatementContext ctx, IqlResult result, boolean primary) {
			// Optional limit (pure int)
			Optional.ofNullable(ctx.limit)
				.map(AntlrUtils::textOf)
				.map(AntlrUtils::cleanNumberLiteral)
				.map(Long::parseLong)
				.ifPresent(result::setLimit);
			// Optional 'first' flag
			result.setFirst(ctx.FIRST()!=null);
			// Optional order instructions
			if(ctx.orderExpressionList()!=null) {
				List<IqlSorting> sortings = new ArrayList<>();
				for(OrderExpressionContext octx : ctx.orderExpressionList().orderExpression()) {
					sortings.add(processOrderExpression(octx));
				}
				sortings.forEach(result::addSorting);
			}

			if(!primary && (result.isFirst()
					|| result.getLimit().isPresent()
					|| !result.getSortings().isEmpty())) {
				reportBuilder.addWarning(QueryErrorCode.INCORRECT_USE,
						"Non-primary result statement contains sorting/limit declarations: {}",
						textOf(ctx));

			}
			//TODO do we need to run an integrity check on 'result' here?
		}

		private  IqlSorting processOrderExpression(OrderExpressionContext ctx) {
			IqlSorting sorting = new IqlSorting();
			sorting.setOrder(ctx.ASC()!=null ? Order.ASCENDING : Order.DESCENDING);
			sorting.setExpression(processExpression(ctx.expression()));
			return sorting;
		}
	}

	private static class ExpansionInfo {
		static final int UNDEFINED = -1;
		static final int UNLIMITED = Integer.MAX_VALUE;

		int minSize = UNDEFINED;
		int expansionLimit = UNDEFINED;

		void add(int minSize, int expansionLimit) {
			if(this.minSize==UNDEFINED) {
				this.minSize = minSize;
			} else if(minSize!=UNDEFINED) {
				this.minSize += minSize;
			}

			if(this.expansionLimit==UNDEFINED) {
				this.expansionLimit = expansionLimit;
			} else if(this.expansionLimit!=UNLIMITED) {
				if(expansionLimit==UNLIMITED) {
					this.expansionLimit = UNLIMITED;
				} else if(expansionLimit!=UNDEFINED) {
					this.expansionLimit += expansionLimit;
				}
			}
		}

		void add(ExpansionInfo other) {
			add(other.minSize, other.expansionLimit);
		}

		private void alternative(int minSize, int expansionLimit) {
			if(this.minSize==UNDEFINED) {
				this.minSize = minSize;
			} else if(minSize!=UNDEFINED) {
				this.minSize = Math.min(this.minSize, minSize);
			}

			if(this.expansionLimit==UNDEFINED) {
				this.expansionLimit = expansionLimit;
			} else if(this.expansionLimit!=UNLIMITED) {
				if(expansionLimit==UNLIMITED) {
					this.expansionLimit = UNLIMITED;
				} else if(expansionLimit!=UNDEFINED) {
					this.expansionLimit = Math.max(this.expansionLimit, expansionLimit);
				}
			}
		}

		private void alternative(OptionalInt minSize, OptionalInt expansionLimit) {
			alternative(minSize.orElse(UNDEFINED), expansionLimit.orElse(UNDEFINED));
		}

		void alternative(ExpansionInfo other) {
			alternative(other.minSize, other.expansionLimit);
		}

		private int scaleOverflowSensitive(int value, int factor) {
			if(value==UNDEFINED || value==UNLIMITED) {
				return value;
			}
			value *= factor;
			if(value<0) {
				value = Integer.MAX_VALUE;
			}
			return value;
		}

		private void scale(int minSizeMultiplier, int expansionLimitMultiplier) {
			minSize = scaleOverflowSensitive(minSize, minSizeMultiplier);
			expansionLimit = scaleOverflowSensitive(expansionLimit, expansionLimitMultiplier);
		}

		void quantify(Quantifiable element) {
			int minSizeMultiplier = 1;
			int expansionLimitMultiplier = 1;
			if(element.hasQuantifiers()) {
				final ExpansionInfo tmp = new ExpansionInfo();

				for(IqlQuantifier quantifier : element.getQuantifiers()) {
					if(quantifier.isUniversallyQuantified()) {
						tmp.alternative(1, UNLIMITED);
					} else {
						switch (quantifier.getQuantifierType()) {
						case RANGE:
							tmp.alternative(quantifier.getLowerBound(), quantifier.getUpperBound());
							break;
						case EXACT:
							tmp.alternative(quantifier.getValue(), quantifier.getValue());
							break;
						case AT_LEAST:
							tmp.alternative(quantifier.getValue(), OptionalInt.of(UNLIMITED));
							break;
						case AT_MOST:
							tmp.alternative(OptionalInt.of(1), quantifier.getValue());
							break;

						default:
							break;
						}
					}
				}

				minSizeMultiplier = tmp.minSize;
				expansionLimitMultiplier = tmp.expansionLimit;
			}

			if(element instanceof IqlElement && ((IqlElement)element).getType()==IqlType.EDGE) {
				minSizeMultiplier = scaleOverflowSensitive(minSizeMultiplier, 2);
				expansionLimitMultiplier = scaleOverflowSensitive(expansionLimitMultiplier, 2);
			}

			scale(minSizeMultiplier, expansionLimitMultiplier);
		}
	}

	private class PayloadProcessor {

		private boolean treeFeaturesUsed = false;
		private boolean graphFeaturesUsed = false;

		private Map<IqlElement, ExpansionInfo> expansionInfos = new Reference2ObjectOpenHashMap<>();

		private void reportTreeFeaturesUsed(ParserRuleContext ctx) {
			if(graphFeaturesUsed) {
				reportBuilder.addError(QueryErrorCode.UNSUPPORTED_FEATURE,
						"Cannot mix tree and graph features - query type already determined to be GRAPH: '{1}'", textOf(ctx));
			} else {
				treeFeaturesUsed = true;
			}
		}

		private void reportGraphFeaturesUsed(ParserRuleContext ctx) {
			if(treeFeaturesUsed) {
				reportBuilder.addError(QueryErrorCode.UNSUPPORTED_FEATURE,
						"Cannot mix tree and graph features - query type already determined to be TREE: '{1}'", textOf(ctx));
			} else {
				graphFeaturesUsed = true;
			}
		}

		//TODO
		private ExpansionInfo getExpansionInfo(IqlElement element) {
			ExpansionInfo info = expansionInfos.get(element);
			if(info == null) {
				info = new ExpansionInfo();

				switch (element.getType()) {
				case NODE:
					info.add(1, 1);
					info.quantify((IqlNode)element);
					break;

				case EDGE:
					info.add(2, 2);
					info.quantify((IqlEdge)element);
					break;

				case TREE_NODE:
					info.add(1, 1);
					((IqlTreeNode)element).getChildren()
						.map(this::getExpansionInfo)
						.ifPresent(info::add);
					info.quantify((IqlTreeNode)element);
					break;

				case GROUPING:
					info.add(getExpansionInfo(((IqlGrouping)element).getElement()));
					info.quantify((IqlGrouping)element);
					break;

				case SEQUENCE:
					((IqlSequence)element).getElements()
						.stream()
						.map(this::getExpansionInfo)
						.forEach(info::add);
					break;

				case DISJUNCTION:
					((IqlElementDisjunction)element).getAlternatives()
						.stream()
						.map(this::getExpansionInfo)
						.forEach(info::alternative);
					break;

				default:
					break;
				}

				expansionInfos.put(element, info);
			}
			return info;
		}

		private int pureDigits(Token token) {
			return Integer.parseInt(textOf(token));
		}

		/** Unwraps arbitrarily nested wrapping expression to the deepest nested one */
		private ExpressionContext unwrap(ExpressionContext ctx) {
			ExpressionContext original = ctx;
			int depth = 0;
			while(ctx instanceof WrappingExpressionContext) {
				ctx = ((WrappingExpressionContext)ctx).expression();
				depth++;
			}
			if(depth>1) {
				reportBuilder.addWarning(QueryErrorCode.SUPERFLUOUS_DECLARATION,
						"Superfluous wrapping of expression '{1}'", textOf(original));
			}
			return ctx;
		}

//		/**
//		 * Unwraps arbitrarily nested wrapping of node statements as long as they
//		 * do not provide explicit quantification. In other words, only unwrap
//		 * superfluous wrappings!
//		 */
//		private NodeStatementContext unwrap(NodeStatementContext ctx) {
//			NodeStatementContext original = ctx;
//			int depth = 0;
//			while(ctx instanceof ElementGroupingContext) {
//				ElementGroupingContext ectx = (ElementGroupingContext) ctx;
//				// Stop as soon as we see any explicit quantification
//				if(ectx.quantifier()!=null) {
//					break;
//				}
//				ctx = ectx.nodeStatement();
//				depth++;
//			}
//			if(depth>1) {
//				reportBuilder.addWarning(QueryErrorCode.SUPERFLUOUS_DECLARATION,
//						"Superfluous wrapping of node statement '{1}'", textOf(original));
//			}
//			return ctx;
//		}

		private IqlPayload processPayloadStatement(PayloadStatementContext ctx) {
			IqlPayload payload = new IqlPayload();
			genId(payload);

			// Handle bindings
			BindingsListContext blctx = ctx.bindingsList();
			if(blctx!=null) {
				for(BindingContext bctx : blctx.binding()) {
					payload.addBinding(processBinding(bctx));
				}
			}

			// Handle filter
			if(ctx.constraint()!=null) {
				payload.setConstraint(processConstraint(ctx.constraint()));
			}

			// Handle actual selection statement variants
			SelectionStatementContext sctx = ctx.selectionStatement();
			// Plain constraints or global constraints section of structure statement
			if(sctx.constraint()!=null) {
				payload.setConstraint(processConstraint(sctx.constraint()));
			}

			if(sctx.laneStatementsList()!=null) {
				int count = 0;
				for(LaneStatementContext lsctx : sctx.laneStatementsList().laneStatement()) {
					payload.addLane(processLaneStatement(lsctx));
					count++;
				}
				payload.setQueryType(count > 1 ?
						QueryType.MULTI_LANE : QueryType.SINGLE_LANE);

				if(count==1) {
					//TODO technically this is wrong, as LANE declarations can be used to control the structure/container context
					reportBuilder.addWarning(QueryErrorCode.SUPERFLUOUS_DECLARATION,
							"Can omit 'LANE' declaration if using only a single lane.");
				}
			} else if(sctx.structuralConstraint()!=null) {
				// Structure statement [sequence,tree,graph]
				IqlLane lane = new IqlLane();
				genId(lane);
				lane.setName(IqlLane.PROXY_NAME);

				processLaneContent(lane, sctx.structuralConstraint());

				payload.addLane(lane);
				payload.setQueryType(QueryType.SINGLE_LANE);
			} else {
				// Simple plain statement
				payload.setQueryType(QueryType.PLAIN);

				if(ctx.constraint()!=null) {
					reportBuilder.addWarning(QueryErrorCode.SUPERFLUOUS_DECLARATION,
							"Do not mix FILTER BY expression with plain query");
				}
			}

			payload.checkIntegrity();

			if(reportBuilder.getErrorCount()>0)
				throw new QueryProcessingException("Failed to process payload - encountered errors", reportBuilder.build());
			if(reportBuilder.getWarningCount()>0 && !ignoreWarnings)
				throw new QueryProcessingException("Failed to process payload - encountered warnings", reportBuilder.build());

			return payload;
		}

		private IqlLane processLaneStatement(LaneStatementContext ctx) {
			IqlLane lane = new IqlLane();
			genId(lane);

			lane.setName(textOf(ctx.name));

			Optional.ofNullable(ctx.member())
				.map(this::extractMemberName)
				.ifPresent(lane::setAlias);

			processLaneContent(lane, ctx.structuralConstraint());

			return lane;
		}

		private NodeArrangement processNodeArrangement(NodeArrangementContext ctx) {
			if(ctx.ORDERED()!=null) {
				return NodeArrangement.ORDERED;
			} else if(ctx.ADJACENT()!=null) {
				return NodeArrangement.ADJACENT;
			} else if(ctx.UNORDERED()!=null) {
				return NodeArrangement.UNORDERED;
			}

			return failForUnhandledAlternative(ctx);
		}

		private void processLaneContent(IqlLane lane, StructuralConstraintContext ctx) {

			try {
				// Handle actual structural constraints
				lane.setElement(processStructuralConstraint(ctx, new TreeInfo()));

				// Handle modifiers
				HitsLimitContext hlctx = ctx.hitsLimit();
				if(hlctx!=null) {
					if(hlctx.FIRST()!=null) {
						lane.setLimit(1L);
					} else {
						int limit = pureDigits(hlctx.PureDigits().getSymbol());
						if(limit==1) {
							reportBuilder.addWarning(QueryErrorCode.SUPERFLUOUS_DECLARATION,
									"The <n HITS> limit declration should not be used to restrict "
									+ "the result limit to 1. Simply use <FIRST> for that");
						}
						lane.setLimit(limit);
					}
				}

				// Handle match flags
				for (MatchFlagContext mfctx : ctx.matchFlag()) {
					lane.setFlag(IqlLane.MatchFlag.parse(mfctx.getText()));
				}

				LaneType laneType;
				if(treeFeaturesUsed) {
					laneType = LaneType.TREE;
				} else if(graphFeaturesUsed) {
					laneType = LaneType.GRAPH;
				} else {
					laneType = LaneType.SEQUENCE;
				}

				lane.setLaneType(laneType);
			} finally {
				treeFeaturesUsed = false;
				graphFeaturesUsed = false;
			}
		}

		/** Counts explicitly existentially quantified elements in the given list. */
		private int countExistentialElements(List<IqlElement> elements) {
			int count = 0;
			for (IqlElement element : elements) {
				count += getExpansionInfo(element).minSize;
			}
			return count;
		}

		/** Checks whether collection of nodes can be expanded to at least the given number of instances. */
		private boolean canExpandToAtLeast(int required, List<IqlElement> elements) {
			int accumulatedExpansion = 0;
			for (IqlElement element : elements) {
				accumulatedExpansion += getExpansionInfo(element).expansionLimit;
				if(accumulatedExpansion>=required) {
					return true;
				}
			}
			return false;
		}

		private IqlBinding processBinding(BindingContext ctx) {
			IqlBinding binding = new IqlBinding();

			binding.setTarget(textOf(ctx.Identifier()));
			binding.setDistinct(ctx.DISTINCT()!=null);
			binding.setEdges(ctx.EDGES()!=null);
			for(MemberContext mctx : ctx.member()) {
				binding.addMember(processMember(mctx));
			}

			return binding;
		}

		private IqlReference processMember(MemberContext ctx) {
			IqlReference member = new IqlReference();

			genId(member);
			member.setReferenceType(ReferenceType.MEMBER);
			member.setName(textOf(ctx.Identifier()));

			return member;
		}

		private String extractMemberName(MemberContext ctx) {
			return textOf(ctx.Identifier());
		}

		private IqlConstraint processConstraint(ConstraintContext ctx) {
			return processBooleanExpression(ctx.expression());
		}

		private IqlConstraint processBooleanExpression(ExpressionContext ctx) {
			ctx = unwrap(ctx);

			if(ctx instanceof ConjunctionContext) {
				return processConjunction((ConjunctionContext) ctx);
			} else if(ctx instanceof DisjunctionContext) {
				return processDisjunction((DisjunctionContext) ctx);
			} else {
				return processPredicate(ctx);
			}
		}

		private IqlPredicate processPredicate(ExpressionContext ctx) {
			IqlPredicate predicate = new IqlPredicate();
			genId(predicate);
			predicate.setExpression(processExpression(ctx));
			return predicate;
		}

		private IqlTerm processConjunction(ConjunctionContext ctx) {
			IqlTerm term = new IqlTerm();
			genId(term);
			term.setOperation(BooleanOperation.CONJUNCTION);

			// Try to collapse any conjunctive sequence into a single term
			ExpressionContext tail = ctx;
			while(tail instanceof ConjunctionContext) {
				ConjunctionContext cctx = (ConjunctionContext) tail;
				term.addItem(processBooleanExpression(cctx.left));
				tail = unwrap(cctx.right);
			}
			// Now add the final dangling expression
			term.addItem(processBooleanExpression(tail));
			return term;
		}

		private IqlTerm processDisjunction(DisjunctionContext ctx) {
			IqlTerm term = new IqlTerm();
			genId(term);
			term.setOperation(BooleanOperation.DISJUNCTION);

			// Try to collapse any disjunctive sequence into a single term
			ExpressionContext tail = ctx;
			while(tail instanceof DisjunctionContext) {
				DisjunctionContext cctx = (DisjunctionContext) tail;
				term.addItem(processBooleanExpression(cctx.left));
				tail = unwrap(cctx.right);
			}
			// Now add the final dangling expression
			term.addItem(processBooleanExpression(tail));
			return term;
		}

		private IqlElement processStructuralConstraint(StructuralConstraintContext ctx, TreeInfo tree) {
			// If we only have a single node, don't wrap it needlessly
			List<NodeStatementContext> nodes = ctx.nodeStatement();
			if(nodes.size()==1) {
				return processNodeStatement(nodes.get(0), tree);
			}

			// More than one node -> wrap into a proper sequence
			IqlSequence structure = new IqlSequence();
			genId(structure);
			if(defaultOrderedSequence) {
				structure.addArrangement(NodeArrangement.ORDERED);
			}

			//TODO rethink the tree entry call here, as it might throw of arrangement assignment in downstream parse calls
			tree.enter(structure, false);
			for(NodeStatementContext nctx : ctx.nodeStatement()) {
				structure.addElement(processNodeStatement(nctx, tree));
			}
			tree.exit();

			return structure;
		}

		/** Process given node statement and honor limitations of specified query type */
		private IqlElement processNodeStatement(NodeStatementContext ctx, TreeInfo tree) {

			if(ctx instanceof ElementGroupingContext) {
				return processElementGrouping((ElementGroupingContext) ctx, tree);
			} else if(ctx instanceof ElementSequenceContext) {
				return processElementSequence((ElementSequenceContext) ctx, tree);
			} else if (ctx instanceof SingleNodeContext) {
				return processNode(((SingleNodeContext)ctx).node(), true, tree);
			} else if (ctx instanceof GraphFragmentContext) {
				return processGraphFragment((GraphFragmentContext) ctx, tree);
			} else if(ctx instanceof ElementDisjunctionContext) {
				return processElementDisjunction((ElementDisjunctionContext) ctx, tree);
			}

			return failForUnhandledAlternative(ctx);
		}

		private IqlElement processElementGrouping(ElementGroupingContext ctx,
				TreeInfo tree) {
			IqlGrouping grouping = new IqlGrouping();
			genId(grouping);

			boolean negated = false;

			if(ctx.quantifier()!=null) {
				List<IqlQuantifier> quantifiers = processQuantifier(ctx.quantifier());
				quantifiers.forEach(grouping::addQuantifier);

				negated = grouping.isExistentiallyNegated();

				if(tree.isNegated() && negated) {
					reportBuilder.addError(QueryErrorCode.INCORRECT_USE,
							"Double negation of nested node grouping '{1}' - try to express query positively instead", textOf(ctx));
				}
			}

			tree.enter(grouping, negated);
			List<NodeStatementContext> elements = ctx.nodeStatement();
			if(elements.size()==1) {
				grouping.setElement(processNodeStatement(elements.get(0), tree));
			} else {
				grouping.setElement(processElements(elements, null, tree));
			}

			tree.exit();

			return (grouping.hasQuantifiers() || keepRedundantGrouping) ? grouping : grouping.getElement();
		}

		private IqlSequence processElements(List<NodeStatementContext> elements,
				List<NodeArrangementContext> arrangements, TreeInfo tree) {
			IqlSequence sequence = new IqlSequence();
			genId(sequence);

			if(arrangements!=null) {
				for(NodeArrangementContext nactx : arrangements) {
					sequence.addArrangement(processNodeArrangement(nactx));
				}
			}

//			// Make sure top-level structures get assigned ORDERED
//			if((arrangements==null || arrangements.isEmpty()) && !tree.hasParent()) {
//				sequence.addArrangement(NodeArrangement.ORDERED);
//			} else
			// Honor default settings for node ordering
			if(defaultOrderedSequence
					&& !sequence.hasArrangement(NodeArrangement.ORDERED)
					&& !sequence.hasArrangement(NodeArrangement.UNORDERED)) {
				sequence.addArrangement(NodeArrangement.ORDERED);
			}

			tree.enter(sequence, false);
			for(NodeStatementContext nctx : elements) {
				sequence.addElement(processNodeStatement(nctx, tree));
			}
			tree.exit();

			//TODO needs a more sophisticated detection: multiple nodes can be in fact the same on (e.g. in graph)
			if(sequence.hasArrangement(NodeArrangement.ADJACENT)
					&& countExistentialElements(sequence.getElements())<2
					&& !canExpandToAtLeast(2, sequence.getElements())) {
				reportBuilder.addWarning(QueryErrorCode.INCORRECT_USE,
						"For adjacency feature to be effective the query needs at least"
						+ " two distinct nodes that are existentially quantified, or quantification"
						+ " that can expand to two or more node instances.");
			}

			return sequence;
		}

		private IqlSequence processElementSequence(ElementSequenceContext ctx, TreeInfo tree) {
			return processElements(ctx.nodeStatement(), ctx.nodeArrangement(), tree);
		}

		private IqlSequence processGraphFragment(GraphFragmentContext ctx, TreeInfo tree) {
			IqlSequence elements = new IqlSequence();
			genId(elements);

			tree.enter(elements, false);
			for(ElementContext ectx : ctx.element()) {
				elements.addElement(processElement(ectx, tree));
			}
			tree.exit();

			return elements;
		}

		private void processProperElement0(IqlProperElement element,
				@Nullable MemberLabelContext mlctx, @Nullable ConstraintContext cctx) {
			genId(element);
			if(mlctx!=null) {
				element.setLabel(textOf(mlctx.member().Identifier()));
			}
			if(cctx!=null) {
				element.setConstraint(processConstraint(cctx));
			}
		}

		private IqlNode processNode(NodeContext ctx, boolean allowDummy, TreeInfo tree) {
			if(ctx instanceof ProperNodeContext) {
				return processProperNode((ProperNodeContext) ctx, tree);
			} else if(ctx instanceof DummyNodeContext) {
				if(!allowDummy) {
					reportBuilder.addError(QueryErrorCode.UNSUPPORTED_FEATURE,
							"Usage of dummy node syntax not supported at this positions: '{1}'", textOf(ctx));
				}
				return processDummyNode((DummyNodeContext) ctx, tree);
			}

			return failForUnhandledAlternative(ctx);
		}

		private IqlNode processDummyNode(DummyNodeContext ctx, TreeInfo tree) {
			IqlNode node = new IqlNode();
			genId(node);
			IqlQuantifier quantifier = new IqlQuantifier();
			quantifier.setQuantifierModifier(QuantifierModifier.RELUCTANT);
			node.addQuantifier(quantifier);

			if(ctx.STAR()!=null) {
				quantifier.setValue(0);
				quantifier.setQuantifierType(QuantifierType.AT_LEAST);
			} else if(ctx.QMARK()!=null) {
				quantifier.setQuantifierType(QuantifierType.RANGE);
				quantifier.setLowerBound(0);
				quantifier.setUpperBound(1);
			} else if(ctx.PLUS()!=null) {
				quantifier.setValue(1);
				quantifier.setQuantifierType(QuantifierType.AT_LEAST);
			} else {
				failForUnhandledAlternative(ctx);
			}

			return node;
		}

		private IqlNode processProperNode(ProperNodeContext ctx, TreeInfo tree) {
			IqlNode node;
			// Decide on type of node
			if(ctx.structuralConstraint()!=null) {
				reportTreeFeaturesUsed(ctx);
				node = new IqlTreeNode();
			} else {
				node = new IqlNode();
			}

			// Handle label and regular constraints
			processProperElement0(node, ctx.memberLabel(), ctx.constraint());

			// Handle markers
			if(ctx.positionMarker()!=null) {
				node.setMarker(processMarker(ctx.positionMarker()));
			}

			// Handle quantification
			boolean negated = false;
			if(ctx.quantifier()!=null) {
				List<IqlQuantifier> quantifiers = processQuantifier(ctx.quantifier());
				quantifiers.forEach(node::addQuantifier);

				negated = node.isExistentiallyNegated();

				if(tree.isNegated() && node.isExistentiallyNegated()) {
					reportBuilder.addError(QueryErrorCode.INCORRECT_USE,
							"Double negation of nested node '{1}' - try to express query positively instead", textOf(ctx));
				}
			}

			// Finally process and add actual children
			if(ctx.structuralConstraint()!=null) {
				tree.enter(node, negated);
				((IqlTreeNode) node).setChildren(processStructuralConstraint(ctx.structuralConstraint(), tree));
				tree.exit();
			}

			return node;
		}

		private List<IqlQuantifier> processQuantifier(QuantifierContext ctx) {
			// List of disjunctive quantifier statements
			List<IqlQuantifier> quantifiers = ctx.simpleQuantifier()
					.stream()
					.map(this::processSimpleQuantifier)
					// Ignore redundant quantifiers directly
					.collect(Collectors.toList());

			Set<IqlQuantifier> compacted = new ObjectOpenCustomHashSet<>(quantifiers, IqlQuantifier.EQUALITY);
			if(compacted.size()<quantifiers.size()) {
				quantifiers.removeAll(compacted);
				String redundantQuantifiers = String.join(", ", quantifiers.stream()
						.map(IqlQuantifier::toString)
						.toArray(String[]::new));
				reportBuilder.addWarning(QueryErrorCode.SUPERFLUOUS_DECLARATION,
						"Redundant quantifiers: [{1}]", redundantQuantifiers);

				quantifiers.clear();
				quantifiers.addAll(compacted);
			}

			return quantifiers;
		}

		/** Unwraps arbitrarily nested marker wrapping to the deepest nested one */
		private PositionMarkerContext unwrap(PositionMarkerContext ctx) {
			PositionMarkerContext original = ctx;

			// Unwrap arbitrarily deep
			int depth = 0;
			while(ctx instanceof MarkerWrappingContext) {
				ctx = ((MarkerWrappingContext)ctx).positionMarker();
				depth++;
			}

			if(depth>1) {
				reportBuilder.addWarning(QueryErrorCode.SUPERFLUOUS_DECLARATION,
						"Superfluous wrapping of marker '{1}'", textOf(original));
			}
			return ctx;
		}

		private IqlMarker processMarker(PositionMarkerContext ctx) {
			ctx = unwrap(ctx);

			if(ctx instanceof MarkerCallContext) {
				return processMarkerCall((MarkerCallContext) ctx);
			} else if(ctx instanceof MarkerConjunctionContext) {
				return processMarkerConjunction((MarkerConjunctionContext) ctx);
			} else if(ctx instanceof MarkerDisjunctionContext) {
				return processMarkerDisjunction((MarkerDisjunctionContext) ctx);
			}

			return failForUnhandledAlternative(ctx);
		}

		private IqlMarkerCall processMarkerCall(MarkerCallContext ctx) {
			IqlMarkerCall call = new IqlMarkerCall();
			call.setName(textOf(ctx.name));
			List<PositionArgumentContext> args = ctx.positionArgument();
			if(!args.isEmpty()) {
				call.setArguments(args.stream()
						.map(this::processPositionArgument)
						.toArray(Number[]::new));
			}
			return call;
		}

		private Number processPositionArgument(PositionArgumentContext ctx) {
			if(ctx.signedIntegerLiteral()!=null) {
				return Integer.valueOf(textOf(ctx.signedIntegerLiteral()));
			} else if(ctx.signedFloatingPointLiteral()!=null) {
				return Double.valueOf(textOf(ctx.signedFloatingPointLiteral()));
			}

			return failForUnhandledAlternative(ctx);
		}

		private IqlMarkerExpression processMarkerConjunction(MarkerConjunctionContext ctx) {
			IqlMarkerExpression exp = new IqlMarkerExpression();
			exp.setExpressionType(MarkerExpressionType.CONJUNCTION);

			// Try to collapse any conjunctive sequence into a single term
			PositionMarkerContext tail = ctx;
			while(tail instanceof MarkerConjunctionContext) {
				MarkerConjunctionContext cctx = (MarkerConjunctionContext) tail;
				exp.addItem(processMarker(cctx.left));
				tail = unwrap(cctx.right);
			}
			// Now add the final dangling expression
			exp.addItem(processMarker(tail));
			return exp;
		}

		private IqlMarkerExpression processMarkerDisjunction(MarkerDisjunctionContext ctx) {
			IqlMarkerExpression exp = new IqlMarkerExpression();
			exp.setExpressionType(MarkerExpressionType.DISJUNCTION);

			// Try to collapse any conjunctive sequence into a single term
			PositionMarkerContext tail = ctx;
			while(tail instanceof MarkerDisjunctionContext) {
				MarkerDisjunctionContext dctx = (MarkerDisjunctionContext) tail;
				exp.addItem(processMarker(dctx.left));
				tail = unwrap(dctx.right);
			}
			// Now add the final dangling expression
			exp.addItem(processMarker(tail));
			return exp;
		}

		private IqlQuantifier processSimpleQuantifier(SimpleQuantifierContext ctx) {
			IqlQuantifier quantifier = new IqlQuantifier();

			// Basic quantifier type
			if(ctx.all()!=null) {
				// Universally quantified
				quantifier.setQuantifierType(QuantifierType.ALL);
			} else if(ctx.not()!=null) {
				// Existentially negated
				quantifier.setQuantifierType(QuantifierType.EXACT);
				quantifier.setValue(0);
			} else if(ctx.PLUS()!=null) {
				// At-Least quantifier
				quantifier.setQuantifierType(QuantifierType.AT_LEAST);
				quantifier.setValue(pureDigits(ctx.value));
			} else if(ctx.MINUS()!=null) {
				// At-Most quantifier
				quantifier.setQuantifierType(QuantifierType.AT_MOST);
				int value = pureDigits(ctx.value);
				if(value==0) {
					reportBuilder.addError(QueryErrorCode.INVALID_LITERAL,
							"Cannot use 'at-most' quantifier with a target value of 0 - use 'exact' instead: '{1}'", textOf(ctx));
				}
				quantifier.setValue(value);
			} else if(ctx.DOUBLE_DOT()!=null) {
				// Bounded-Range quantifier
				quantifier.setQuantifierType(QuantifierType.RANGE);
				int lower = pureDigits(ctx.lowerBound);
				int upper = pureDigits(ctx.upperBound);
				if(lower>=upper) {
					reportBuilder.addError(QueryErrorCode.INVALID_LITERAL,
							"Lower end of range quantifier must not be equal or greater than upper end: '{1}'", textOf(ctx));
				}
				quantifier.setLowerBound(lower);
				quantifier.setUpperBound(upper);
			} else {
				// Exact quantifier
				quantifier.setQuantifierType(QuantifierType.EXACT);
				quantifier.setValue(pureDigits(ctx.value));
			}

			// Quantifier modifier (GREEDY is the default)
			if(ctx.QMARK()!=null) {
				quantifier.setQuantifierModifier(QuantifierModifier.RELUCTANT);
			} else if(ctx.EXMARK()!=null) {
				quantifier.setQuantifierModifier(QuantifierModifier.POSSESSIVE);
			}

			// Discontinuous flag
			if(ctx.CARET()!=null) {
				if(quantifier.isExistentiallyNegated() || quantifier.isUniversallyQuantified()) {
					reportBuilder.addError(QueryErrorCode.INCORRECT_USE,
							"Cannot apply 'discontinuous' flag for negation or universal quantifiers: '{1}'", textOf(ctx));
				}
				quantifier.setDiscontinuous(true);
			}

			return quantifier;
		}

		private IqlElement processElement(ElementContext ctx, TreeInfo tree) {
			if(ctx.edge()!=null) {
				reportGraphFeaturesUsed(ctx);

				IqlNode source = processNode(ctx.source, false, tree);
				IqlNode target = processNode(ctx.target, false, tree);

				if(source.hasQuantifiers() && target.hasQuantifiers()) {
					reportBuilder.addError(QueryErrorCode.UNSUPPORTED_FEATURE,
							"Competing quantifier definition - can only define quantifiers on either source or target: '{1}'", textOf(ctx));
				}

				IqlEdge edge = processEdge(ctx.edge(), source, target);
				return edge;
			}

			return processNode(ctx.content, true, tree);
		}

		private IqlEdge processEdge(EdgeContext ctx, IqlNode source, IqlNode target) {
			if(ctx instanceof EmptyEdgeContext) {
				return processEmptyEdge((EmptyEdgeContext) ctx, source, target);
			} else if(ctx instanceof FilledEdgeContext) {
				return processFilledEdge((FilledEdgeContext) ctx, source, target);
			}
			return failForUnhandledAlternative(ctx);
		}

		private IqlEdge processEmptyEdge(EmptyEdgeContext ctx, IqlNode source, IqlNode target) {
			IqlEdge edge = new IqlEdge();

			genId(edge);
			if(ctx.EDGE_LEFT()!=null) {
				edge.setSource(target);
				edge.setTarget(source);
			} else {
				edge.setSource(source);
				edge.setTarget(target);
			}

			if(ctx.EDGE_UNDIRECTED()!=null) {
				edge.setEdgeType(EdgeType.UNDIRECTED);
			} else if(ctx.EDGE_BIDIRECTIONAL()!=null) {
				edge.setEdgeType(EdgeType.BIDIRECTIONAL);
			} else {
				edge.setEdgeType(EdgeType.UNIDIRECTIONAL);
			}

			return edge;
		}

		private IqlEdge processFilledEdge(FilledEdgeContext ctx, IqlNode source, IqlNode target) {
			IqlEdge edge = new IqlEdge();

			processProperElement0(edge, ctx.memberLabel(), ctx.constraint());

			LeftEdgePartContext left = ctx.leftEdgePart();
			RightEdgePartContext right = ctx.rightEdgePart();

			if(!isContinuous(left)) {
				reportBuilder.addError(QueryErrorCode.NON_CONTINUOUS_TOKEN,
						"Left part of multipart edge definition is not continuous (must not contain whitespaces): '{1}'", textOf(left));
			}
			if(!isContinuous(right)) {
				reportBuilder.addError(QueryErrorCode.NON_CONTINUOUS_TOKEN,
						"Right part of multipart edge definition is not continuous (must not contain whitespaces): '{1}'", textOf(right));
			}

			if(left.directedEdgeLeft()!=null && right.undirectedEdge()!=null) {
				edge.setSource(target);
				edge.setTarget(source);
			} else {
				edge.setSource(source);
				edge.setTarget(target);
			}

			if(left.undirectedEdge()!=null && right.undirectedEdge()!=null) {
				edge.setEdgeType(EdgeType.UNDIRECTED);
			} else if(left.directedEdgeLeft()!=null && right.directedEdgeRight()!=null) {
				edge.setEdgeType(EdgeType.BIDIRECTIONAL);
			} else {
				edge.setEdgeType(EdgeType.UNIDIRECTIONAL);
			}

			return edge;
		}

		private IqlElementDisjunction processElementDisjunction(ElementDisjunctionContext ctx,
				TreeInfo tree) {
			IqlElementDisjunction dis = new IqlElementDisjunction();
			genId(dis);

			tree.enter(dis, false);
			// Try to collapse any nested alternatives into a single list
			NodeStatementContext tail = ctx;
			while(tail instanceof ElementDisjunctionContext) {
				ElementDisjunctionContext nactx = (ElementDisjunctionContext) tail;
				dis.addAlternative(processNodeStatement(nactx.left, tree));
				tail = nactx.right;
			}
			// Now add the final dangling expression
			dis.addAlternative(processNodeStatement(tail, tree));
			tree.exit();

			return dis;
		}
	}

	private static class TreeInfo {
		private final ObjectArrayList<IqlElement> trace = new ObjectArrayList<>();
		private final BooleanArrayList negated = new BooleanArrayList();

		void enter(IqlElement node, boolean negated) {
			trace.push(requireNonNull(node));
			this.negated.push(negated);
		}

		boolean hasParent() { return !trace.isEmpty() && trace.top() instanceof IqlTreeNode; }
		boolean isNegated() { return negated.contains(true); }

		IqlTreeNode parent() { return (IqlTreeNode) trace.top(); }

		IqlElement exit() { negated.popBoolean(); return trace.pop(); }
	}
}
