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
import de.ims.icarus2.query.api.iql.antlr.IQLBaseVisitor;
import de.ims.icarus2.query.api.iql.antlr.IQLParser;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.BindingContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.BindingsListContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ConstraintContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ConstraintStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.GroupExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.GroupStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.MemberContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.OrderExpressionContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.ResultStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.SelectiveStatementContext;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.VariableNameContext;
import de.ims.icarus2.query.api.iql.antlr.IQLVisitor;

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
			return groupStatementVisitor.visitGroupStatement(parser.groupStatement());
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
					sortings.add(orderVisitor.visit(octx));
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
						payload.addBinding(bindingVisitor.visitBinding(bctx));
					}
				}

				// Handle actual selection statement variants
				SelectiveStatementContext sctx = ctx.selectiveStatement();
				if(sctx.constraint()!=null) {
					// Simple plain statement
					payload.setQueryType(QueryType.PLAIN);
					payload.setConstraint(constraintVisitor.visitConstraint(sctx.constraint()));
				} else {
					// Structure statement [plain,tree,graph]
				}
			}

			return payload;
		} catch(RecognitionException e) {
			throw asSyntaxException(e, "Failed to parse 'rawPayload'");
		}
	}

	private String id(IqlUnique target) {
		return idGenerator.generateId(target);
	}

	private final IQLVisitor<List<IqlGroup>> groupStatementVisitor = new IQLBaseVisitor<List<IqlGroup>>() {
		@Override
		public List<IqlGroup> visitGroupStatement(GroupStatementContext gctx) {
			List<IqlGroup> groups = new ArrayList<>();
			for (GroupExpressionContext ectx : gctx.groupExpression()) {
				groups.add(groupExpressionVisitor.visitGroupExpression(ectx));
			}
			return groups;
		}
	};

	private final IQLVisitor<IqlGroup> groupExpressionVisitor = new IQLBaseVisitor<IqlGroup>() {
		@Override
		public IqlGroup visitGroupExpression(GroupExpressionContext ctx) {
			IqlGroup group = new IqlGroup();

			group.setGroupBy(toExpression(ctx.selector));
			group.setLabel(textOf(ctx.label));

			Optional.ofNullable(ctx.filter)
				.map(QueryProcessor.this::toExpression)
				.ifPresent(group::setFilterOn);
			Optional.ofNullable(ctx.defaultValue)
				.map(QueryProcessor.this::toExpression)
				.ifPresent(group::setDefaultValue);

			return group;
		}
	};

	private IqlExpression toExpression(IQLParser.ExpressionContext ctx) {
		IqlExpression expression = new IqlExpression();
		expression.setContent(textOf(ctx));
		return expression;
	}

	private final IQLVisitor<IqlSorting> orderVisitor = new IQLBaseVisitor<IqlSorting>() {
		@Override
		public IqlSorting visitOrderExpression(OrderExpressionContext ctx) {
			IqlSorting sorting = new IqlSorting();
			sorting.setOrder(ctx.ASC()!=null ? Order.ASCENDING : Order.DESCENDING);
			sorting.setExpression(toExpression(ctx.expression()));
			return sorting;
		}
	};

	private final IQLVisitor<IqlBinding> bindingVisitor = new IQLBaseVisitor<IqlBinding>() {
		@Override
		public IqlBinding visitBinding(BindingContext ctx) {
			IqlBinding binding = new IqlBinding();

			binding.setTarget(textOf(ctx.qualifiedIdentifier()));
			binding.setDistinct(ctx.DISTINCT()!=null);
			for(MemberContext mctx : ctx.member()) {
				binding.addMember(referenceVisitor.visitMember(mctx));
			}

			return binding;
		}
	};

	private final IQLVisitor<IqlReference> referenceVisitor = new IQLBaseVisitor<IqlReference>() {
		@Override
		public IqlReference visitMember(MemberContext ctx) {
			IqlReference member = new IqlReference();

			member.setId(id(member));
			member.setReferenceType(ReferenceType.MEMBER);
			member.setName(textOf(ctx.Identifier()));

			return member;
		}

		@Override
		public IqlReference visitVariableName(VariableNameContext ctx) {
			IqlReference member = new IqlReference();

			member.setId(id(member));
			member.setReferenceType(ReferenceType.VARIABLE);
			member.setName(textOf(ctx.Identifier()));

			return member;
		}
	};

	private final IQLVisitor<IqlConstraint> constraintVisitor = new IQLBaseVisitor<IqlConstraint>() {
		@Override
		public IqlConstraint visitConstraint(ConstraintContext ctx) {
			IqlConstraint constraint = new IqlConstraint();

			constraint.setId(id(constraint));
			//TODO

			return constraint;
		}
	};
}
