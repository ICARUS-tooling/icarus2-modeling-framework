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
package de.ims.icarus2.query.api;

import static de.ims.icarus2.query.api.iql.AntlrUtils.asSyntaxException;
import static de.ims.icarus2.query.api.iql.AntlrUtils.createParser;
import static de.ims.icarus2.query.api.iql.AntlrUtils.textOf;
import static de.ims.icarus2.query.api.iql.IqlUtils.createMapper;
import static de.ims.icarus2.query.api.iql.IqlUtils.fragment;
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.strings.StringUtil.isNullOrEmpty;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.RecognitionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.query.api.iql.AntlrUtils;
import de.ims.icarus2.query.api.iql.IqlBinding;
import de.ims.icarus2.query.api.iql.IqlConstraint;
import de.ims.icarus2.query.api.iql.IqlConstraint.BooleanOperation;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlPredicate;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlTerm;
import de.ims.icarus2.query.api.iql.IqlElement;
import de.ims.icarus2.query.api.iql.IqlExpression;
import de.ims.icarus2.query.api.iql.IqlGroup;
import de.ims.icarus2.query.api.iql.IqlObjectIdGenerator;
import de.ims.icarus2.query.api.iql.IqlPayload;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryType;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlReference;
import de.ims.icarus2.query.api.iql.IqlReference.ReferenceType;
import de.ims.icarus2.query.api.iql.IqlResult;
import de.ims.icarus2.query.api.iql.IqlSorting;
import de.ims.icarus2.query.api.iql.IqlSorting.Order;
import de.ims.icarus2.query.api.iql.IqlUnique;
import de.ims.icarus2.query.api.iql.antlr.IQLParser;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.BindingContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.BindingsListContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ConjunctionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ConstraintContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ConstraintStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.DisjunctionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.GraphStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.GroupExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.GroupStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.MemberContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.NodeGroupingContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.NodeStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.OrderExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ResultStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.SelectiveStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.SequenceStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.StructureStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.TreeStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.VariableNameContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.WrappingExpressionContext;

/**
 * @author Markus Gärtner
 *
 */
public class QueryProcessor {

	private final ObjectMapper mapper = createMapper();
	private final IqlObjectIdGenerator idGenerator = new IqlObjectIdGenerator();

	/**
	 * Parses a raw JSON query into an {@link IqlQuery} instance,
	 * but does <b>not</b> parse the embedded textual representations
	 * for query payload, grouping and query instructions.
	 *
	 * @param rawQuery
	 * @return
	 * @throws QueryException
	 */
	public IqlQuery readQuery(Query rawQuery) throws QueryException {
		requireNonNull(rawQuery);

		try {
			return mapper.readValue(rawQuery.getText(), IqlQuery.class);
		} catch (JsonProcessingException e) {
			throw new QueryException(QueryErrorCode.JSON_ERROR,
					"Failed to read query",
					fragment(rawQuery.getText(), e.getLocation()), e);
		}
	}

	/**
	 * Parses the the embedded textual representations
	 * for query payload, grouping and query instructions,
	 * the latter two if defined.
	 *
	 * @param query the raw query
	 * @throws QueryException if any of the sections to be processed
	 * has already been processed previously or if there was an error
	 * when parsing them.
	 */
	public void parseQuery(IqlQuery query) {
		requireNonNull(query);
		checkState("Query processed", !query.isProcessed());

		String rawGrouping = query.getRawGrouping().orElse(null);
		if(!isNullOrEmpty(rawGrouping)) {
			processGrouping(rawGrouping).forEach(query::addGrouping);
		}
		String rawResult = query.getRawResult().orElse(null);
		if(!isNullOrEmpty(rawResult)) {
			processResult(rawResult, query.getResult());
		}
		//TODO handle payload

		query.markProcessed();
	}

	@VisibleForTesting
	List<IqlGroup> processGrouping(String rawGrouping) {
		checkNotEmpty(rawGrouping);

		IQLParser parser = createParser(rawGrouping, "rawGrouping");
		try {
			return processGroupStatement(parser.groupStatement());
		} catch(RecognitionException e) {
			throw asSyntaxException(e, "Failed to parse 'rawGrouping'");
		}
	}

	@VisibleForTesting
	void processResult(String rawResult, IqlResult result) {
		checkNotEmpty(rawResult);

		IQLParser parser = createParser(rawResult, "rawResult");
		try {
			ResultStatementContext ctx = parser.resultStatement();
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
		} catch(RecognitionException e) {
			throw asSyntaxException(e, "Failed to parse 'rawResult'");
		}
	}

	@VisibleForTesting
	IqlPayload processPayload(String rawPayload) {
		checkNotEmpty(rawPayload);

		IQLParser parser = createParser(rawPayload, "rawPayload");
		try {
			ConstraintStatementContext ctx = parser.constraintStatement();
			IqlPayload payload = new IqlPayload();

			if(ctx.ALL()!=null) {
				// If we are asked to return ALL we don't need to bother with bindings or constraints
				payload.setQueryType(QueryType.ALL);
			} else {
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

				if(sctx.structureStatement()!=null) {
					// Structure statement [sequence,tree,graph]
					StructureStatementContext ssctx = sctx.structureStatement();
					NodeStatementContext nsctx = null;
					QueryType queryType = null;
					if(ssctx instanceof SequenceStatementContext) {
						nsctx = ((SequenceStatementContext)ssctx).nodeStatement();
						queryType = QueryType.SEQUENCE;
					} else if(ssctx instanceof TreeStatementContext) {
						TreeStatementContext tsctx = (TreeStatementContext) ssctx;
						payload.setAligned(tsctx.ALIGNED()!=null);
						queryType = QueryType.TREE;
						nsctx = tsctx.nodeStatement();
					} else if(ssctx instanceof GraphStatementContext) {
						GraphStatementContext gsctx = (GraphStatementContext) ssctx;
						payload.setAligned(gsctx.ALIGNED()!=null);
						queryType = QueryType.GRAPH;
						nsctx = gsctx.nodeStatement();
					} // needs sanity check against yet unhandled alternatives

					processNodeStatement(nsctx, queryType).forEach(payload::addElement);
				} else {
					// Simple plain statement
					payload.setQueryType(QueryType.PLAIN);
				}
			}

			return payload;
		} catch(RecognitionException e) {
			throw asSyntaxException(e, "Failed to parse 'rawPayload'");
		}
	}

	private void genId(IqlUnique target) {
		target.setId(idGenerator.generateId(target));
	}

	private List<IqlGroup> processGroupStatement(GroupStatementContext gctx) {
		List<IqlGroup> groups = new ArrayList<>();
		for (GroupExpressionContext ectx : gctx.groupExpression()) {
			groups.add(processGroupExpression(ectx));
		}
		return groups;
	}

	private IqlGroup processGroupExpression(GroupExpressionContext ctx) {
		IqlGroup group = new IqlGroup();

		group.setGroupBy(processExpression(ctx.selector));
		group.setLabel(textOf(ctx.label));

		Optional.ofNullable(ctx.filter)
			.map(QueryProcessor.this::processExpression)
			.ifPresent(group::setFilterOn);
		Optional.ofNullable(ctx.defaultValue)
			.map(QueryProcessor.this::processExpression)
			.ifPresent(group::setDefaultValue);

		return group;
	}

	private IqlExpression processExpression(ExpressionContext ctx) {
		IqlExpression expression = new IqlExpression();
		expression.setContent(textOf(ctx));
		return expression;
	}

	private  IqlSorting processOrderExpression(OrderExpressionContext ctx) {
		IqlSorting sorting = new IqlSorting();
		sorting.setOrder(ctx.ASC()!=null ? Order.ASCENDING : Order.DESCENDING);
		sorting.setExpression(processExpression(ctx.expression()));
		return sorting;
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

	private IqlReference processVariableName(VariableNameContext ctx) {
		IqlReference variable = new IqlReference();

		genId(variable);
		variable.setReferenceType(ReferenceType.VARIABLE);
		variable.setName(textOf(ctx.Identifier()));

		return variable;
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

	/** Unwraps arbitrarily nested wrapping expression to the deepest nested one */
	private ExpressionContext unwrap(ExpressionContext ctx) {
		while(ctx instanceof WrappingExpressionContext) {
			ctx = ((WrappingExpressionContext)ctx).expression();
		}
		return ctx;
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

	/** Unwraps arbitrarily nested wrapping of node statements */
	private NodeStatementContext unwrap(NodeStatementContext ctx) {
		while(ctx instanceof NodeGroupingContext) {
			ctx = ((NodeGroupingContext)ctx).nodeStatement();
		}
		return ctx;
	}

	/** Process given node statement and honor limitations of specified query type */
	private List<IqlElement> processNodeStatement(NodeStatementContext ctx, QueryType queryType) {
		ctx = unwrap(ctx);
		//TODO
		return null;
	}

}
