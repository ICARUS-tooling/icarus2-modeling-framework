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

import static de.ims.icarus2.query.api.iql.AntlrUtils.asFeatureException;
import static de.ims.icarus2.query.api.iql.AntlrUtils.asSyntaxException;
import static de.ims.icarus2.query.api.iql.AntlrUtils.createParser;
import static de.ims.icarus2.query.api.iql.AntlrUtils.isContinuous;
import static de.ims.icarus2.query.api.iql.AntlrUtils.textOf;
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.strings.StringUtil.isNullOrEmpty;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement;
import de.ims.icarus2.query.api.iql.IqlElement.IqlTreeNode;
import de.ims.icarus2.query.api.iql.IqlExpression;
import de.ims.icarus2.query.api.iql.IqlGroup;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlLane.LaneType;
import de.ims.icarus2.query.api.iql.IqlLane.NodeArrangement;
import de.ims.icarus2.query.api.iql.IqlObjectIdGenerator;
import de.ims.icarus2.query.api.iql.IqlPayload;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryType;
import de.ims.icarus2.query.api.iql.IqlQuantifier;
import de.ims.icarus2.query.api.iql.IqlQuantifier.QuantifierType;
import de.ims.icarus2.query.api.iql.IqlQueryElement;
import de.ims.icarus2.query.api.iql.IqlReference;
import de.ims.icarus2.query.api.iql.IqlReference.ReferenceType;
import de.ims.icarus2.query.api.iql.IqlResult;
import de.ims.icarus2.query.api.iql.IqlSorting;
import de.ims.icarus2.query.api.iql.IqlSorting.Order;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.query.api.iql.IqlUnique;
import de.ims.icarus2.query.api.iql.antlr.IQLParser;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.BindingContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.BindingsListContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ConjunctionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ConstraintContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.DisjunctionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.EdgeContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ElementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ElementSequenceContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.EmptyEdgeContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.FilledEdgeContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.GroupExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.GroupStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.LaneStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.LeftEdgePartContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.MemberContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.MemberLabelContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.NodeAlternativesContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.NodeArrangementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.NodeContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.NodeGroupingContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.NodeSequenceContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.NodeStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.OrderExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.PayloadStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.QuantifierContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ResultStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.RightEdgePartContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.SelectiveStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.UnsignedSimpleQuantifierContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.VariableNameContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.WrappingExpressionContext;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.id.StaticIdentity;

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

	QueryProcessor(boolean ignoreWarnings) {
		this.ignoreWarnings = ignoreWarnings;
	}

	/**
	 * Parses the the embedded textual representations
	 * for query payload, grouping and query instructions,
	 * the latter two if defined.
	 *
	 * @param stream the raw query
	 * @throws QueryException if any of the sections to be processed
	 * has already been processed previously or if there was an error
	 * when parsing them.
	 */
	public void parseStream(IqlStream stream) {
		requireNonNull(stream);
		checkState("Query processed", !stream.isProcessed());

		String rawGrouping = stream.getRawGrouping().orElse(null);
		if(!isNullOrEmpty(rawGrouping)) {
			processGrouping(rawGrouping).forEach(stream::addGrouping);
		}
		String rawResult = stream.getRawResult().orElse(null);
		if(!isNullOrEmpty(rawResult)) {
			processResult(rawResult, stream.getResult(), stream.isPrimary());
		}

		stream.setPayload(processPayload(stream.getRawPayload()));

		stream.markProcessed();
	}

	@VisibleForTesting
	List<IqlGroup> processGrouping(String rawGrouping) {
		checkNotEmpty(rawGrouping);

		IQLParser parser = createParser(rawGrouping, "rawGrouping");
		try {
			return new GroupProcessor().processGroupStatement(parser.groupStatement());
		} catch(RecognitionException e) {
			throw asSyntaxException(e, "Failed to parse 'rawGrouping'");
		}
	}

	@VisibleForTesting
	void processResult(String rawResult, IqlResult result, boolean primary) {
		checkNotEmpty(rawResult);

		IQLParser parser = createParser(rawResult, "rawResult");
		try {
			new ResultProcessor().processResult(parser.resultStatement(), result, primary);
		} catch(RecognitionException e) {
			throw asSyntaxException(e, "Failed to parse 'rawResult'");
		}
	}

	@VisibleForTesting
	IqlPayload processPayload(String rawPayload) {
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

	private IqlReference processVariableName(VariableNameContext ctx) {
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
			// Optional percentage flag
			result.setPercent(ctx.PERCENT()!=null);
			// Optional order instructions
			if(ctx.orderExpressionList()!=null) {
				List<IqlSorting> sortings = new ArrayList<>();
				for(OrderExpressionContext octx : ctx.orderExpressionList().orderExpression()) {
					sortings.add(processOrderExpression(octx));
				}
				sortings.forEach(result::addSorting);
			}

			if(!primary && (result.isPercent() || result.getLimit().isPresent()
					|| !result.getSortings().isEmpty())) {
				reportBuilder.addWarning(QueryErrorCode.INCORRECT_USE,
						"Non-primary result statement contains sorting/percent/limit declarations: {}",
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

	private class PayloadProcessor {

		private boolean treeFeaturesUsed = false;
		private boolean graphFeaturesUsed = false;

		/** Unwraps arbitrarily nested wrapping expression to the deepest nested one */
		private ExpressionContext unwrap(ExpressionContext ctx) {
			ExpressionContext original = ctx;
			int depths = 0;
			while(ctx instanceof WrappingExpressionContext) {
				ctx = ((WrappingExpressionContext)ctx).expression();
				depths++;
			}
			if(depths>1) {
				reportBuilder.addWarning(QueryErrorCode.SUPERFLUOUS_DECLARATION,
						"Superfluous wrapping of expression '{1}'", textOf(original));
			}
			return ctx;
		}

		/** Unwraps arbitrarily nested wrapping of node statements */
		private NodeStatementContext unwrap(NodeStatementContext ctx) {
			NodeStatementContext original = ctx;
			int depths = 0;
			while(ctx instanceof NodeGroupingContext) {
				ctx = ((NodeGroupingContext)ctx).nodeStatement();
			}
			if(depths>1) {
				reportBuilder.addWarning(QueryErrorCode.SUPERFLUOUS_DECLARATION,
						"Superfluous wrapping of node statement '{1}'", textOf(original));
			}
			return ctx;
		}

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

			// Handle actual selection statement variants
			SelectiveStatementContext sctx = ctx.selectiveStatement();
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
			} else if(sctx.nodeStatement()!=null) {
				// Structure statement [sequence,tree,graph]
				IqlLane lane = new IqlLane();
				genId(lane);
				lane.setName(IqlLane.PROXY_NAME);

				processLaneContent(lane, sctx.nodeArrangement(), sctx.nodeStatement());

				payload.addLane(lane);
				payload.setQueryType(QueryType.SINGLE_LANE);
			} else {
				// Simple plain statement
				payload.setQueryType(QueryType.PLAIN);
			}

			payload.checkIntegrity();

			if(reportBuilder.getErrorCount()>0)
				throw new QueryProcessingException("Failed to process payload - encountered errors", reportBuilder.build());
			if(reportBuilder.getWarningCount()>0 && !ignoreWarnings)
				throw new QueryProcessingException("Failed to process payload - encountered errors", reportBuilder.build());

			return payload;
		}

		private IqlLane processLaneStatement(LaneStatementContext ctx) {
			IqlLane lane = new IqlLane();
			genId(lane);

			lane.setName(extractMemberName(ctx.member()));

			processLaneContent(lane, ctx.nodeArrangement(), ctx.nodeStatement());

			return lane;
		}

		private NodeArrangement processNodeArrangement(NodeArrangementContext ctx) {
			if(ctx==null) {
				return NodeArrangement.UNSPECIFIED;
			}

			if(ctx.ORDERED()!=null) {
				return NodeArrangement.ORDERED;
			} else if(ctx.ADJACENT()!=null) {
				return NodeArrangement.ADJACENT;
			}

			return failForUnhandledAlternative(ctx);
		}

		private void processLaneContent(IqlLane lane, NodeArrangementContext nctx,
				NodeStatementContext ctx) {

			lane.setNodeArrangement(processNodeArrangement(nctx));

			try {
				// Start the (recursive) node parsing with a 'null' parent
				processNodeStatement(ctx, null).forEach(lane::addElement);

				LaneType laneType = LaneType.SEQUENCE;
				if(treeFeaturesUsed) {
					laneType = LaneType.TREE;
				} else if(graphFeaturesUsed) {
					laneType = LaneType.GRAPH;
				}

				lane.setLaneType(laneType);
			} finally {
				treeFeaturesUsed = false;
				graphFeaturesUsed = false;
			}

			//TODO needs a more sophisticated detection: multiple nodes can be in fact the same on (e.g. in graph)
			if(lane.getNodeArrangement()!=NodeArrangement.UNSPECIFIED
					&& countExistentialElements(lane.getElements())<2) {
				reportBuilder.addWarning(QueryErrorCode.INCORRECT_USE,
						"For node arrangement feature to be effective the query needs at least"
						+ " two distinct nodes that are existentially quantified.");
			}
		}

		private int countExistentialElements(List<IqlElement> elements) {
			int count = 0;
			for (IqlElement element : elements) {
				boolean existential = false;
				switch (element.getType()) {
				case EDGE:
					existential = ((IqlEdge)element).isExistentiallyQuantified();
					break;

				case NODE:
				case TREE_NODE:
					existential = ((IqlNode)element).isExistentiallyQuantified();
					break;

				default:
					break;
				}

				if(existential) {
					count++;
				}
			}
			return count;
		}

		private IqlBinding processBinding(BindingContext ctx) {
			IqlBinding binding = new IqlBinding();

			binding.setTarget(textOf(ctx.qualifiedIdentifier()));
			binding.setDistinct(ctx.DISTINCT()!=null);
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

		/** Process given node statement and honor limitations of specified query type */
		private List<? extends IqlElement> processNodeStatement(NodeStatementContext ctx, IqlTreeNode parent) {
			ctx = unwrap(ctx);

			if(ctx instanceof NodeSequenceContext) {
				return processNodeSequence((NodeSequenceContext) ctx, parent);
			} else if (ctx instanceof ElementSequenceContext) {
				return processElementSequence((ElementSequenceContext) ctx, parent);
			} else if (ctx instanceof NodeAlternativesContext) {
				return list(processNodeAlternatives((NodeAlternativesContext) ctx, parent));
			}

			return failForUnhandledAlternative(ctx);
		}

		private List<IqlNode> processNodeSequence(NodeSequenceContext ctx,
				@Nullable IqlTreeNode parent) {
			List<IqlNode> elements = new ArrayList<>();
			for(NodeContext nctx : ctx.node()) {
				elements.add(processNode(nctx, parent));
			}
			return elements;
		}

		private List<IqlElement> processElementSequence(ElementSequenceContext ctx,
				@Nullable IqlTreeNode parent) {
			List<IqlElement> elements = new ArrayList<>();
			for(ElementContext ectx : ctx.element()) {
				elements.add(processElement(ectx, parent));
			}
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

		private IqlNode processNode(NodeContext ctx, @Nullable IqlTreeNode parent) {
			IqlNode node;
			// Decide on type of node
			if(ctx.nodeStatement()!=null) {
				if(graphFeaturesUsed) {
					reportBuilder.addError(QueryErrorCode.UNSUPPORTED_FEATURE,
							"Cannot mix tree and graph features - query type already determined to be GRAPH: '{1}'", textOf(ctx));
				} else {
					treeFeaturesUsed = true;
				}
				node = new IqlTreeNode();
			} else {
				node = new IqlNode();
			}

			processProperElement0(node, ctx.memberLabel(), ctx.constraint());

			if(ctx.quantifier()!=null) {
				List<IqlQuantifier> quantifiers = processQuantifier(ctx.quantifier());
				quantifiers.forEach(node::addQuantifier);

				if(parent!=null && parent.isNegated() && node.isNegated()) {
					reportBuilder.addError(QueryErrorCode.INCORRECT_USE,
							"Double negation of nested nodes '{1}' - try to express query positively instead", textOf(ctx));
				} else if(parent==null && node.isUniversallyQuantified()) {
					//TODO need to revisit this limitation in the IQL specification!! (we might allow universal quantification here)
					/* Info for todo: nodes are typically bound to items that are embedded into a
					 * surrounding grouping (e.g. sentences) that provides a natural limitation for
					 * the scope of universal quantification. So a universally node would read
					 * 'find container (sentence) where _ALL_ items match this one'. As a result we
					 * would move the sanity check one level up and ensure that there cannot be any
					 * concurrent nodes (i.e. the universally quantified node must be the sole top
					 * level node, either globally or within its node alternative group).
					 */
					reportBuilder.addError(QueryErrorCode.INCORRECT_USE,
							"Cannot universally qualify top-level nodes: {1}", textOf(ctx));
				}
			}

			// Finally process actual children
			if(ctx.nodeStatement()!=null) {
				IqlTreeNode tNode = (IqlTreeNode) node;
				processNodeStatement(ctx.nodeStatement(), tNode).forEach(tNode::addChild);
			}

			return node;
		}

		private List<IqlQuantifier> processQuantifier(QuantifierContext ctx) {
			List<IqlQuantifier> quantifiers = new ArrayList<>();

			if(ctx.all()!=null) {
				// Universally quantified
				IqlQuantifier quantifier = new IqlQuantifier();
				quantifier.setQuantifierType(QuantifierType.ALL);
				quantifiers.add(quantifier);
			} else if(ctx.not()!=null) {
				// Negated
				IqlQuantifier quantifier = new IqlQuantifier();
				quantifier.setQuantifierType(QuantifierType.EXACT);
				quantifier.setValue(0);
				quantifiers.add(quantifier);
			} else {
				// List of disjunctive quantifier statements
				for(UnsignedSimpleQuantifierContext uctx : ctx.unsignedSimpleQuantifier()) {
					quantifiers.add(processUnsignedSimpleQuantifier(uctx));
				}
			}

			return quantifiers;
		}

		private int pureDigits(Token token) {
			return Integer.parseInt(textOf(token));
		}

		private IqlQuantifier processUnsignedSimpleQuantifier(UnsignedSimpleQuantifierContext ctx) {
			IqlQuantifier quantifier = new IqlQuantifier();

			if(ctx.PLUS()!=null) {
				quantifier.setQuantifierType(QuantifierType.AT_LEAST);
				quantifier.setValue(pureDigits(ctx.value));
			} else if(ctx.MINUS()!=null) {
				quantifier.setQuantifierType(QuantifierType.AT_MOST);
				int value = pureDigits(ctx.value);
				if(value==0) {
					reportBuilder.addError(QueryErrorCode.INVALID_LITERAL,
							"Cannot use 'at-most' quantifier with a target value of 0 - use 'exact' instead: '{1}'", textOf(ctx));
				}
				quantifier.setValue(value);
			} else if(ctx.DOUBLE_DOT()!=null) {
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
				quantifier.setQuantifierType(QuantifierType.EXACT);
				quantifier.setValue(pureDigits(ctx.value));
			}
			return quantifier;
		}

		private IqlElement processElement(ElementContext ctx, @Nullable IqlTreeNode parent) {
			if(ctx.edge()!=null) {
				if(treeFeaturesUsed) {
					reportBuilder.addError(QueryErrorCode.UNSUPPORTED_FEATURE,
							"Cannot mix tree and graph features - query type already determined to be TREE: '{1}'", textOf(ctx));
				} else {
					graphFeaturesUsed = true;
				}

				IqlNode source = processNode(ctx.source, parent);
				IqlNode target = processNode(ctx.target, parent);

				if(source.hasQuantifiers() && target.hasQuantifiers()) {
					reportBuilder.addError(QueryErrorCode.UNSUPPORTED_FEATURE,
							"Competing quantifier definition - can only define quantifiers on either source or target: '{1}'", textOf(ctx));
				}

				IqlEdge edge = processEdge(ctx.edge(), source, target);
				return edge;
			}

			return processNode(ctx.content, parent);
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

			genId(edge);
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

		private IqlElementDisjunction processNodeAlternatives(NodeAlternativesContext ctx,
				@Nullable IqlTreeNode parent) {
			IqlElementDisjunction dis = new IqlElementDisjunction();
			genId(dis);

			// Try to collapse any nested alternatives into a single list
			NodeStatementContext tail = ctx;
			while(tail instanceof NodeAlternativesContext) {
				NodeAlternativesContext nactx = (NodeAlternativesContext) tail;
				dis.addAlternative(processNodeStatement(nactx.left, parent));
				tail = unwrap(nactx.right);
			}
			// Now add the final dangling expression (note that we have to forward the original parent!)
			dis.addAlternative(processNodeStatement(tail, parent));

			return dis;
		}
	}
}
