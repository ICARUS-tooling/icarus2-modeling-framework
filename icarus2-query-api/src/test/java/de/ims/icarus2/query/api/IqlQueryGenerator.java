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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.ims.icarus2.model.manifest.ManifestGenerator;
import de.ims.icarus2.query.api.iql.IqlAliasedReference;
import de.ims.icarus2.query.api.iql.IqlBinding;
import de.ims.icarus2.query.api.iql.IqlConstraint;
import de.ims.icarus2.query.api.iql.IqlConstraint.BooleanOperation;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlPredicate;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlTerm;
import de.ims.icarus2.query.api.iql.IqlCorpus;
import de.ims.icarus2.query.api.iql.IqlData;
import de.ims.icarus2.query.api.iql.IqlData.ChecksumType;
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
import de.ims.icarus2.query.api.iql.IqlImport;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlLane.LaneType;
import de.ims.icarus2.query.api.iql.IqlLayer;
import de.ims.icarus2.query.api.iql.IqlNamedReference;
import de.ims.icarus2.query.api.iql.IqlObjectIdGenerator;
import de.ims.icarus2.query.api.iql.IqlPayload;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryModifier;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryType;
import de.ims.icarus2.query.api.iql.IqlProperties;
import de.ims.icarus2.query.api.iql.IqlProperty;
import de.ims.icarus2.query.api.iql.IqlQuantifier;
import de.ims.icarus2.query.api.iql.IqlQuantifier.QuantifierType;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlQueryElement;
import de.ims.icarus2.query.api.iql.IqlReference;
import de.ims.icarus2.query.api.iql.IqlReference.ReferenceType;
import de.ims.icarus2.query.api.iql.IqlResult;
import de.ims.icarus2.query.api.iql.IqlResult.ResultType;
import de.ims.icarus2.query.api.iql.IqlResultInstruction;
import de.ims.icarus2.query.api.iql.IqlScope;
import de.ims.icarus2.query.api.iql.IqlSorting;
import de.ims.icarus2.query.api.iql.IqlSorting.Order;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.query.api.iql.IqlType;
import de.ims.icarus2.query.api.iql.IqlUnique;
import de.ims.icarus2.query.api.iql.NodeArrangement;
import de.ims.icarus2.test.Dummy;
import de.ims.icarus2.test.random.RandomGenerator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class IqlQueryGenerator {

	public static <E extends IqlQueryElement> IncrementalBuild<E> generateOnce(
			IqlQueryGenerator generator, IqlType type, Config config) {

		return generator.build(type, config);
	}

	/**
	 * Creates an instance for given {@link IqlType} with the bare minimum of fields
	 * set to pass the integrity check. THe implementation is kinda inefficient as we
	 * create a full incremental build and then throw away all the dynamic changes,
	 *  but whatever...
	 *
	 * @param type
	 * @param rng
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <E extends IqlQueryElement> E createBare(IqlType type, RandomGenerator rng) {
		return (E) generateOnce(new IqlQueryGenerator(rng), type, config()).getInstance();
	}

	private static final int DEFAULT_COUNT = 3;

	private final RandomGenerator rng;

	private final AtomicInteger counter = new AtomicInteger(0);

	public IqlQueryGenerator(RandomGenerator rng) {
		this.rng = requireNonNull(rng);
	}

	private int index() {
		return counter.getAndIncrement();
	}

	private String index(String base) {
		return base+index();
	}

	public <M extends IqlQueryElement> IncrementalBuild<M> build(IqlType type, Config config) {
		requireNonNull(type);
		requireNonNull(config);
		return generate0(type, config);
	}

	@SuppressWarnings("unchecked")
	private <E extends IqlQueryElement> E instantiate(IqlType type, Config config) {
		try {
			return (E) type.getType().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return fail("Failed to instantiate "+type.getId(), e);
		}
	}

	private <E extends IqlQueryElement> E generateFull(IqlType type, Config config) {
		E instance = instantiate(type, config);
		IncrementalBuild<E> build = prepare0(instance, config);
		return build.applyAllAndGet();
	}

	private <E extends IqlQueryElement> IncrementalBuild<E> generate0(IqlType type, Config config) {
		E instance = instantiate(type, config);
		return prepare0(instance, config);
	}

	private <E extends IqlQueryElement> IncrementalBuild<E> prepare0(E element, Config config) {
		IqlType type = element.getType();
		IncrementalBuild<E> build = new IncrementalBuild<E>(element);

		config.preprocess(element);

		switch (type) {
		case BINDING: prepareBinding((IqlBinding) element, build, config); break;
		case CORPUS: prepareCorpus((IqlCorpus) element, build, config); break;
		case DATA: prepareData((IqlData) element, build, config); break;
		case EDGE: prepareEdge((IqlEdge) element, build, config); break;
		case DISJUNCTION: prepareElementDisjunction((IqlElementDisjunction) element, build, config); break;
		case GROUPING: prepareElementGrouping((IqlGrouping) element, build, config); break;
		case EXPRESSION: prepareExpression((IqlExpression) element, build, config); break;
		case GROUP: prepareGroup((IqlGroup) element, build, config); break;
		case IMPORT: prepareImport((IqlImport) element, build, config); break;
		case LANE: prepareLane((IqlLane) element, build, config); break;
		case LAYER: prepareLayer((IqlLayer) element, build, config); break;
		case NODE: prepareNode((IqlNode) element, build, config); break;
		case SEQUENCE: prepareElementSet((IqlSequence) element, build, config); break;
		case PAYLOAD: preparePayload((IqlPayload) element, build, config); break;
		case PREDICATE: preparePredicate((IqlPredicate) element, build, config); break;
		case PROPERTY: prepareProperty((IqlProperty) element, build, config); break;
		case QUANTIFIER: prepareQuantifier((IqlQuantifier) element, build, config); break;
		case QUERY: prepareQuery((IqlQuery) element, build, config); break;
		case REFERENCE: prepareReference((IqlReference) element, build, config); break;
		case RESULT: prepareResult((IqlResult) element, build, config); break;
		case RESULT_INSTRUCTION: prepareResultInstruction((IqlResultInstruction) element, build, config); break;
		case SCOPE: prepareScope((IqlScope) element, build, config); break;
		case SORTING: prepareSorting((IqlSorting) element, build, config); break;
		case STREAM: prepareStream((IqlStream) element, build, config); break;
		case TERM: prepareTerm((IqlTerm) element, build, config); break;
		case TREE_NODE: prepareTreeNode((IqlTreeNode) element, build, config); break;

		default:
			fail("Type not handled: "+type);
		}

		config.postprocess(element);

		return build;
	}

	private void prepareQueryElement0(IqlQueryElement element, IncrementalBuild<?> build, Config config) {
		// no-op
	}

	private void prepareUnique0(IqlUnique unique, IncrementalBuild<?> build, Config config) {
		prepareQueryElement0(unique, build, config);

		// mandatory data
		unique.setId(config.idGenerator.generateId(unique));
	}

	private void prepareNamedReference0(IqlNamedReference reference, IncrementalBuild<?> build, Config config) {
		prepareUnique0(reference, build, config);

		// mandatory data
		reference.setName(index("name"));

		build.addFieldChange(reference::setName, IqlProperties.NAME, index("name"));
	}

	private void prepareAliasedReference0(IqlAliasedReference reference, IncrementalBuild<?> build, Config config) {
		prepareNamedReference0(reference, build, config);

		build.addFieldChange(reference::setAlias, IqlProperties.ALIAS, index("alias"));
	}

	private void prepareBinding(IqlBinding binding, IncrementalBuild<?> build, Config config) {
		prepareQueryElement0(binding, build, config);

		// mandatory data
		binding.setTarget(index("target"));
		binding.addMember(generateFull(IqlType.REFERENCE, config));

		build.addFieldChange(binding::setDistinct, IqlProperties.DISTINCT, Boolean.TRUE);
		build.addFieldChange(binding::setDistinct, IqlProperties.DISTINCT, Boolean.FALSE);
		build.addFieldChange(binding::setTarget, IqlProperties.TARGET, index("target"));
		build.addFieldChange(binding::setEdges, IqlProperties.EDGES, Boolean.TRUE);
		build.addFieldChange(binding::setEdges, IqlProperties.EDGES, Boolean.FALSE);

		for (int i = 0; i < config.getCount(IqlType.REFERENCE, DEFAULT_COUNT); i++) {
			build.addNestedChange(IqlProperties.MEMBERS, IqlType.REFERENCE, config, binding, binding::addMember);
		}
	}

	private void prepareConstraint0(IqlConstraint constraint, IncrementalBuild<?> build, Config config) {
		prepareUnique0(constraint, build, config);

		build.addFieldChange(constraint::setSolved, IqlProperties.SOLVED, Boolean.TRUE);
		build.addFieldChange(constraint::setSolvedAs, IqlProperties.SOLVED_AS, Boolean.TRUE);
	}

	private void preparePredicate(IqlConstraint.IqlPredicate predicate, IncrementalBuild<?> build, Config config) {
		prepareConstraint0(predicate, build, config);

		// mandatory data
		predicate.setExpression(generateFull(IqlType.EXPRESSION, config));

		build.addNestedChange(IqlProperties.EXPRESSION, IqlType.EXPRESSION, config, predicate, predicate::setExpression);
	}

	private void prepareTerm(IqlConstraint.IqlTerm term, IncrementalBuild<?> build, Config config) {
		prepareConstraint0(term, build, config);

		// mandatory data
		term.addItem(generateFull(IqlType.PREDICATE, config));
		term.setOperation(rng.random(BooleanOperation.class));

		for (int i = 0; i < config.getCount(IqlType.PREDICATE, DEFAULT_COUNT); i++) {
			build.addNestedChange(IqlProperties.ITEMS, IqlType.PREDICATE, config, term, term::addItem);
		}
		for(BooleanOperation operation : BooleanOperation.values()) {
			build.addEnumFieldChange(term::setOperation, IqlProperties.OPERATION, operation);
		}
	}

	private void prepareCorpus(IqlCorpus corpus, IncrementalBuild<?> build, Config config) {
		prepareNamedReference0(corpus, build, config);

		build.addFieldChange(corpus::setPid, IqlProperties.PID, index("pid"));
	}

	private void prepareData(IqlData data, IncrementalBuild<?> build, Config config) {
		prepareNamedReference0(data, build, config);

		// mandatory data
		data.setContent(index("content"));
		data.setCodec(index("codec"));

		build.addFieldChange(data::setContent, IqlProperties.CONTENT, index("content"));
		build.addFieldChange(data::setCodec, IqlProperties.CODEC, index("codec"));
		build.addFieldChange(data::setChecksum, IqlProperties.CHECKSUM, index("checksum"));
		for(ChecksumType checksumType : ChecksumType.values()) {
			build.addEnumFieldChange(data::setChecksumType, IqlProperties.CHECKSUM_TYPE, checksumType);
		}
	}

	private void prepareElement0(IqlElement element, IncrementalBuild<?> build, Config config) {
		prepareUnique0(element, build, config);

		build.addFieldChange(element::setConsumed, IqlProperties.CONSUMED, Boolean.TRUE);
	}

	private void prepareProperElement0(IqlProperElement element, IncrementalBuild<?> build, Config config) {
		prepareElement0(element, build, config);

		build.addFieldChange(element::setLabel, "label", index("label"));
		build.addNestedChange(IqlProperties.CONSTRAINT, IqlType.PREDICATE, config, element, element::setConstraint);
	}

	private void prepareNode(IqlNode node, IncrementalBuild<?> build, Config config) {
		prepareProperElement0(node, build, config);

		for (int i = 0; i < config.getCount(IqlType.QUANTIFIER, DEFAULT_COUNT); i++) {
			build.addNestedChange(IqlProperties.QUANTIFIERS, IqlType.QUANTIFIER, config, node, node::addQuantifier);
		}
	}

	private void prepareElementSet(IqlSequence nodeSet, IncrementalBuild<?> build, Config config) {
		prepareElement0(nodeSet, build, config);

		for(NodeArrangement nodeArrangement : NodeArrangement.values()) {
			build.addEnumFieldChange(nodeSet::setArrangement, IqlProperties.ARRANGEMENT, nodeArrangement);
		}

		if(config.tryNested(IqlType.TREE_NODE)) {
			for (int i = 0; i < config.getCount(IqlType.TREE_NODE, DEFAULT_COUNT); i++) {
				build.addNestedChange(IqlProperties.NODES, IqlType.TREE_NODE, config, nodeSet, nodeSet::addElement);
			}
			config.endNested(IqlType.TREE_NODE);
		}
	}

	private void prepareTreeNode(IqlTreeNode treeNode, IncrementalBuild<?> build, Config config) {
		prepareNode(treeNode, build, config);

		if(config.tryNested(IqlType.TREE_NODE)) {
			build.addNestedChange(IqlProperties.CHILDREN, IqlType.TREE_NODE, config, treeNode, treeNode::setChildren);
			config.endNested(IqlType.TREE_NODE);
		}
	}

	private void prepareEdge(IqlEdge edge, IncrementalBuild<?> build, Config config) {
		prepareProperElement0(edge, build, config);

		// mandatory data
		edge.setEdgeType(rng.random(EdgeType.class));
		edge.setSource(generateFull(IqlType.NODE, config));
		edge.setTarget(generateFull(IqlType.NODE, config));

		for(EdgeType edgeType : EdgeType.values()) {
			build.addEnumFieldChange(edge::setEdgeType, IqlProperties.EDGE_TYPE, edgeType);
		}
		build.addNestedChange(IqlProperties.SOURCE, IqlType.NODE, config, edge, edge::setSource);
		build.addNestedChange(IqlProperties.TARGET, IqlType.NODE, config, edge, edge::setTarget);
	}

	private void prepareElementDisjunction(IqlElementDisjunction dis, IncrementalBuild<?> build, Config config) {
		prepareElement0(dis, build, config);

		for (int i = 0; i < 3; i++) {
			dis.addAlternative((IqlSequence)generateFull(IqlType.SEQUENCE, config));
		}
	}

	private void prepareElementGrouping(IqlGrouping wrapper, IncrementalBuild<?> build, Config config) {
		prepareElement0(wrapper, build, config);

		if(config.tryNested(IqlType.GROUPING)) {
			// Must have at least 1 node
			wrapper.addElement(generateFull(IqlType.NODE, config));

			for (int i = 0; i < config.getCount(IqlType.NODE, DEFAULT_COUNT-1); i++) {
				build.addNestedChange(IqlProperties.NODES, IqlType.NODE, config, wrapper, wrapper::addElement);
			}
			config.endNested(IqlType.GROUPING);
		}

		for (int i = 0; i < config.getCount(IqlType.QUANTIFIER, DEFAULT_COUNT); i++) {
			build.addNestedChange(IqlProperties.QUANTIFIERS, IqlType.QUANTIFIER, config, wrapper, wrapper::addQuantifier);
		}
	}

	@SuppressWarnings("boxing")
	private void prepareExpression(IqlExpression expression, IncrementalBuild<?> build, Config config) {
		prepareQueryElement0(expression, build, config);

		// mandatory data
		expression.setContent(String.format("someTargetValue<(%s-%s(%d))", index("value"), index("func"), index()));

		build.addFieldChange(expression::setReturnType, IqlProperties.RETURN_TYPE, Dummy.class);
		build.addFieldChange(expression::setContent, IqlProperties.CONTENT,
				String.format("%s(123, %s)~\"val%d\"", index("func"), index("value"), index()));
	}

	private IqlExpression expression(Object val, IncrementalBuild<?> build, Config config) {
		IqlExpression exp = new IqlExpression();
		prepareQueryElement0(exp, build, config);
		exp.setContent(String.valueOf(val));
		return exp;
	}

	@SuppressWarnings("boxing")
	private void prepareGroup(IqlGroup group, IncrementalBuild<?> build, Config config) {
		prepareUnique0(group, build, config);

		// mandatory data
		group.setGroupBy(generateFull(IqlType.EXPRESSION, config));
		group.setLabel(index("label"));

		build.addNestedChange(IqlProperties.GROUP_BY, IqlType.EXPRESSION, config, group, group::setGroupBy);
		build.addNestedChange(IqlProperties.FILTER_ON, IqlType.EXPRESSION, config, group, group::setFilterOn);
		build.addFieldChange(group::setLabel, IqlProperties.LABEL, index("label"));
		// Include all typical value types!
		build.addFieldChange(group::setDefaultValue, IqlProperties.DEFAULT_VALUE, expression(index("value"), build, config));
		build.addFieldChange(group::setDefaultValue, IqlProperties.DEFAULT_VALUE, expression(true, build, config));
		build.addFieldChange(group::setDefaultValue, IqlProperties.DEFAULT_VALUE, expression(false, build, config));
		build.addFieldChange(group::setDefaultValue, IqlProperties.DEFAULT_VALUE, expression(Integer.MAX_VALUE, build, config));
		build.addFieldChange(group::setDefaultValue, IqlProperties.DEFAULT_VALUE, expression(Long.MAX_VALUE, build, config));
		build.addFieldChange(group::setDefaultValue, IqlProperties.DEFAULT_VALUE, expression(Double.MAX_VALUE, build, config));
	}

	private void prepareImport(IqlImport imp, IncrementalBuild<?> build, Config config) {
		prepareNamedReference0(imp, build, config);

		build.addFieldChange(imp::setOptional, IqlProperties.OPTIONAL, Boolean.TRUE);
	}

	private void prepareLane(IqlLane lane, IncrementalBuild<?> build, Config config) {
		prepareAliasedReference0(lane, build, config);

		// mandatory data
		lane.setLaneType(LaneType.SEQUENCE);
		lane.setElements(generateFull(IqlType.SEQUENCE, config));

		for(LaneType laneType : LaneType.values()) {
			build.addEnumFieldChange(lane::setLaneType, IqlProperties.LANE_TYPE, laneType);
		}
	}

	private void prepareLayer(IqlLayer layer, IncrementalBuild<?> build, Config config) {
		prepareAliasedReference0(layer, build, config);

		build.addFieldChange(layer::setPrimary, IqlProperties.PRIMARY, Boolean.TRUE);
		build.addFieldChange(layer::setAllMembers, IqlProperties.ALL_MEMBERS, Boolean.TRUE);
	}

	private void preparePayload(IqlPayload payload, IncrementalBuild<?> build, Config config) {
		prepareUnique0(payload, build, config);

		// mandatory data
		payload.setQueryType(QueryType.ALL);

		build.addChange(IqlProperties.QUERY_TYPE+"/"+IqlProperties.CONSTRAINT, () -> {
			payload.setQueryType(QueryType.PLAIN);
			payload.setConstraint(generateFull(IqlType.PREDICATE, config));
		});
		payload.setFilter(generateFull(IqlType.PREDICATE, config));
		build.addFieldChange(payload::setName, IqlProperties.NAME, index("name"));
		for(QueryType queryType : QueryType.values()) {
			build.addEnumFieldChange(payload::setQueryType, IqlProperties.QUERY_TYPE, queryType);
		}
		build.addNestedChange(IqlProperties.CONSTRAINT, IqlType.PREDICATE, config, payload, payload::setConstraint);
		for (int i = 0; i < config.getCount(IqlType.LANE, DEFAULT_COUNT); i++) {
			build.addNestedChange(IqlProperties.LANES, IqlType.LANE, config, payload, payload::addLane);
		}
		for(QueryModifier queryModifier : QueryModifier.values()) {
			build.addEnumFieldChange(payload::setQueryModifier, IqlProperties.QUERY_MODIFIER, queryModifier);
		}
		for(int limit : new int[] {1, 5, 10}) {
			build.addFieldChange(payload::setLimit, IqlProperties.LIMIT, _int(limit));
		}
	}

	@SuppressWarnings("boxing")
	private void prepareProperty(IqlProperty property, IncrementalBuild<?> build, Config config) {
		prepareQueryElement0(property, build, config);

		// mandatory data
		property.setKey(index("key"));

		build.addFieldChange(property::setKey, IqlProperties.KEY, index("key"));
		// Include all typical value types!
		build.addFieldChange(property::setValue, IqlProperties.VALUE, index("value"));
		build.addFieldChange(property::setValue, IqlProperties.VALUE, true);
		build.addFieldChange(property::setValue, IqlProperties.VALUE, false);
		build.addFieldChange(property::setValue, IqlProperties.VALUE, Integer.MAX_VALUE);
		build.addFieldChange(property::setValue, IqlProperties.VALUE, Long.MAX_VALUE);
		build.addFieldChange(property::setValue, IqlProperties.VALUE, Double.MAX_VALUE);
	}

	@SuppressWarnings("boxing")
	private void prepareQuantifier(IqlQuantifier quantifier, IncrementalBuild<?> build, Config config) {
		prepareQueryElement0(quantifier, build, config);

		// mandatory data
		quantifier.setQuantifierType(rng.random(QuantifierType.EXACT, QuantifierType.AT_LEAST, QuantifierType.AT_MOST));
		quantifier.setValue(index());

		for(QuantifierType quantifierType : QuantifierType.values()) {
			build.addEnumFieldChange(quantifier::setQuantifierType, IqlProperties.QUANTIFIER_TYPE, quantifierType);
		}

		build.addFieldChange(quantifier::setLowerBound, IqlProperties.LOWER_BOUND, index());
		build.addFieldChange(quantifier::setUpperBound, IqlProperties.UPPER_BOUND, index());
		build.addFieldChange(quantifier::setValue, IqlProperties.VALUE, index());
	}

	private void prepareStream(IqlStream stream, IncrementalBuild<?> build, Config config) {
		prepareUnique0(stream, build, config);

		// mandatory data
		stream.setRawPayload(index("some-raw-payload"));
		stream.setCorpus(generateFull(IqlType.CORPUS, config));
		stream.setResult(generateFull(IqlType.RESULT, config));

		build.addFieldChange(stream::setRawGrouping, IqlProperties.RAW_GROUPING, index("grouping-data"));
		build.addFieldChange(stream::setRawResult, IqlProperties.RAW_RESULT, index("result-data"));
		build.addNestedChange(IqlProperties.RESULT, IqlType.RESULT, config, stream, stream::setResult);
		build.addNestedChange(IqlProperties.CORPUS, IqlType.CORPUS, config, stream, stream::setCorpus);
		build.addNestedChange(IqlProperties.SCOPE, IqlType.SCOPE, config, stream, stream::setScope);
		build.addFieldChange(stream::setRawPayload, IqlProperties.RAW_PAYLOAD, index("payload-data"));
		build.addNestedChange(IqlProperties.PAYLOAD, IqlType.PAYLOAD, config, stream, stream::setPayload);
		build.addFieldChange(stream::setPrimary, IqlProperties.PRIMARY, Boolean.TRUE);
		for (int i = 0; i < config.getCount(IqlType.LAYER, DEFAULT_COUNT); i++) {
			build.addNestedChange(IqlProperties.LAYERS, IqlType.LAYER, config, stream, stream::addLayer);
		}
		for (int i = 0; i < config.getCount(IqlType.GROUP, DEFAULT_COUNT); i++) {
			build.addNestedChange(IqlProperties.GROUPING, IqlType.GROUP, config, stream, stream::addGrouping);
		}
	}

	private void prepareQuery(IqlQuery query, IncrementalBuild<?> build, Config config) {
		prepareUnique0(query, build, config);

		// mandatory data
		query.addStream(generateFull(IqlType.STREAM, config));

		build.addFieldChange(query::setDialect, IqlProperties.DIALECT, index("dialect"));
		for (int i = 0; i < config.getCount(IqlType.IMPORT, DEFAULT_COUNT); i++) {
			build.addNestedChange(IqlProperties.IMPORTS, IqlType.IMPORT, config, query, query::addImport);
		}
		for (int i = 0; i < config.getCount(IqlType.PROPERTY, DEFAULT_COUNT); i++) {
			build.addNestedChange(IqlProperties.SETUP, IqlType.PROPERTY, config, query, query::addSetup);
		}
		for (int i = 0; i < config.getCount(IqlType.DATA, DEFAULT_COUNT); i++) {
			build.addNestedChange(IqlProperties.EMBEDDED_DATA, IqlType.DATA, config, query, query::addEmbeddedData);
		}
	}

	private void prepareReference(IqlReference reference, IncrementalBuild<?> build, Config config) {
		prepareNamedReference0(reference, build, config);

		// mandatory data
		reference.setReferenceType(rng.random(ReferenceType.class));

		for(ReferenceType referenceType : ReferenceType.values()) {
			build.addEnumFieldChange(reference::setReferenceType, IqlProperties.REFERENCE_TYPE, referenceType);
		}
	}

	@SuppressWarnings("boxing")
	private void prepareResult(IqlResult result, IncrementalBuild<?> build, Config config) {
		prepareQueryElement0(result, build, config);

		// mandatory data
		result.addResultType(rng.random(ResultType.class));

		build.addFieldChange(result::setLimit, IqlProperties.LIMIT, Long.MAX_VALUE);
		for(ResultType resultType : ResultType.values()) {
			build.addEnumFieldChange(result::addResultType, IqlProperties.RESULT_TYPES, resultType);
		}
		for (int i = 0; i < config.getCount(IqlType.RESULT_INSTRUCTION, DEFAULT_COUNT); i++) {
			build.addNestedChange(IqlProperties.RESULT_INSTRUCTIONS, IqlType.RESULT_INSTRUCTION, config, result, result::addResultInstruction);
		}
		for (int i = 0; i < config.getCount(IqlType.SORTING, DEFAULT_COUNT); i++) {
			build.addNestedChange(IqlProperties.SORTINGS, IqlType.SORTING, config, result, result::addSorting);
		}
	}

	private void prepareResultInstruction(IqlResultInstruction instruction, IncrementalBuild<?> build, Config config) {
		prepareUnique0(instruction, build, config);

		//TODO
	}

	private void prepareScope(IqlScope scope, IncrementalBuild<?> build, Config config) {
		prepareUnique0(scope, build, config);

		// mandatory data
		scope.addLayer(generateFull(IqlType.LAYER, config));

		for (int i = 0; i < config.getCount(IqlType.LAYER, DEFAULT_COUNT); i++) {
			build.addNestedChange(IqlProperties.LAYERS, IqlType.LAYER, config, scope, scope::addLayer);
		}
	}

	private void prepareSorting(IqlSorting sorting, IncrementalBuild<?> build, Config config) {
		prepareQueryElement0(sorting, build, config);

		// mandatory data
		sorting.setExpression(generateFull(IqlType.EXPRESSION, config));
		sorting.setOrder(rng.random(Order.class));

		for(Order order : Order.values()) {
			build.addEnumFieldChange(sorting::setOrder, "order", order);
		}
		build.addNestedChange(IqlProperties.EXPRESSION, IqlType.EXPRESSION, config, sorting, sorting::setExpression);
	}

	private static String with(String field, Object obj) {
		requireNonNull(obj);
		String s = (obj instanceof String) ? obj.toString() : obj.getClass().getName();

		return "with "+field+" "+s;
	}

	private static <T extends Enum<T>> String name(String field, T value) {
		return "with "+field+" "+value.getClass().getName()+"@"+value.name();
	}

	/**
	 * Wraps an entire (sub-)build into a single change by using its result as
	 * the input for the specified {@code setter} method.
	 */
	private static <E extends IqlQueryElement> Change wrap(IncrementalBuild<E> build, Consumer<E> setter) {
		return () -> {
			build.applyAllChanges();
			setter.accept(build.getInstance());
		};
	}

	public static Config config() {
		return new Config();
	}

	private static class Step {
		final String label;
		final Change change;
		/**
		 * @param label
		 * @param change
		 */
		public Step(String label, Change change) {
			this.label = requireNonNull(label);
			this.change = requireNonNull(change);
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	@FunctionalInterface
	interface Change {

		void apply();
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <E> manifest type of the internal instance
	 */
	public final class IncrementalBuild<E extends IqlQueryElement> {

		private final E instance;

		private final Queue<Step> steps = new LinkedList<>();

		private Step lastStep = null;

		private int changeCount = 0;

		IncrementalBuild(E instance) {
			this.instance = requireNonNull(instance);
		}

		public E getInstance() {
			return instance;
		}

		boolean isRoot(IqlQueryElement element) {
			return instance==element;
		}

		public boolean applyNextChange() {
			lastStep = steps.poll();
			if(lastStep!=null) {
				lastStep.change.apply();
			}
			return lastStep!=null;
		}

		public String currentLabel() {
			return lastStep==null ? "-" : lastStep.label;
		}

		public boolean applyAllChanges() {
			boolean hasMoreChanges = !steps.isEmpty();
			while(!steps.isEmpty()) {
				steps.remove().change.apply();
			}
			return hasMoreChanges;
		}

		E applyAllAndGet() {
			applyAllChanges();
			return getInstance();
		}

		void addChange(String label, Change change) {
			steps.add(new Step(label, change));
			changeCount++;
		}

		<T extends Object> void addValueChange(String label, Consumer<? super T> setter, T value) {
			requireNonNull(setter);
			requireNonNull(value);

			addChange(label, () -> setter.accept(value));
		}

		<T extends Object> void addFieldChange(Consumer<? super T> setter, String field, T value) {
			requireNonNull(setter);
			requireNonNull(value);

			addChange(with(field, value), () -> setter.accept(value));
		}

		<T extends Object> void addLazyFieldChange(Consumer<? super T> setter, String field, Supplier<? extends T> source) {
			requireNonNull(setter);
			requireNonNull(source);

			addChange(with(field, "<delayed>"), () -> setter.accept(source.get()));
		}

		<T extends Enum<T>> void addEnumFieldChange(Consumer<? super T> setter, String field, T value) {
			requireNonNull(setter);
			requireNonNull(value);

			addChange(name(field, value), () -> setter.accept(value));
		}

		/**
		 * {@link ManifestGenerator#generate0(IqlType, IqlQueryElement, Config) Generates} a new
		 * manifest, {@link ManifestGenerator#wrap(IncrementalBuild, Consumer) wrapping} its build into
		 * a single {@link Change} by applying the given {@code setter}. Finally adds the new change as
		 * as step using a label based on the given {@link IqlType type}.
		 * @param type
		 * @param host
		 * @param config
		 * @param setter
		 */
		<E_sub extends IqlQueryElement> void addNestedChange(String field, IqlType type, Config config,
				IqlQueryElement host, Consumer<E_sub> setter) {
			addChange(name(field, type), wrap(generate0(type, config), setter));
		}

		<M_sub extends IqlQueryElement> void addChange(String field, IqlType type,
				Config config, Consumer<M_sub> setter) {
			addChange(name(field, type), wrap(generate0(type, config), setter));
		}

		public int getChangeCount() {
			return changeCount;
		}
	}

	public static final class Config {

		// Configuration
		int defaultCount = -1;
		Object2IntMap<IqlType> specificCounts = new Object2IntOpenHashMap<>();
		String label = "<unnamed>";

		IqlObjectIdGenerator idGenerator = new IqlObjectIdGenerator();

		Map<IqlType, List<Consumer<? super IqlQueryElement>>> preprocessors = new Object2ObjectOpenHashMap<>();
		Map<IqlType, List<Consumer<? super IqlQueryElement>>> postprocessors = new Object2ObjectOpenHashMap<>();

		Set<IqlType> nested = EnumSet.noneOf(IqlType.class);

		// States
		int index = -1;

		Config() {
			specificCounts.defaultReturnValue(-1);
		}

		public Config defaultElementCount(int value) {
			checkArgument(value>=-1);
			defaultCount = value;
			return this;
		}

		public Config elementCount(IqlType type, int value) {
			requireNonNull(type);
			checkArgument(value>=-1);
			specificCounts.put(type, value);
			return this;
		}

		public Config ignoreElement(IqlType type) {
			requireNonNull(type);
			specificCounts.put(type, 0);
			return this;
		}

		int getCount(IqlType type, int fallback) {
			int count = specificCounts.getInt(type);
			if(count==-1) {
				count = defaultCount;
			}
			if(count==-1) {
				count = fallback;
			}
			return count;
		}

		boolean tryNested(IqlType type) {
			return nested.add(type);
		}

		void endNested(IqlType type) {
			assertThat(nested).contains(type);
			nested.remove(type);
		}

		public Config label(String label) {
			this.label = requireNonNull(label);
			return this;
		}

		public String getLabel() {
			return label;
		}

		private void addProcessor(Map<IqlType, List<Consumer<? super IqlQueryElement>>> map,
				IqlType type, Consumer<? super IqlQueryElement> processor) {
			requireNonNull(type);
			requireNonNull(processor);

			map.computeIfAbsent(type, t -> new ArrayList<>()).add(processor);
		}

		private void process(Map<IqlType, List<Consumer<? super IqlQueryElement>>> map,
				IqlQueryElement element) {
			map.getOrDefault(element.getType(), Collections.emptyList())
				.forEach(p -> p.accept(element));
		}

		public Config preprocessor(IqlType type, Consumer<? super IqlQueryElement> processor) {
			addProcessor(preprocessors, type, processor);
			return this;
		}

		void preprocess(IqlQueryElement element) {
			process(preprocessors, element);
		}

		public Config postprocessor(IqlType type, Consumer<? super IqlQueryElement> processor) {
			addProcessor(postprocessors, type, processor);
			return this;
		}

		void postprocess(IqlQueryElement element) {
			process(postprocessors, element);
		}

		private void clear(Map<IqlType, List<Consumer<? super IqlQueryElement>>> map,
				IqlType type) {
			map.getOrDefault(type, Collections.emptyList()).clear();
			map.remove(type);
		}

		void clearPreprocessors(IqlType type) {
			clear(preprocessors, type);
		}

		void clearPostprocessors(IqlType type) {
			clear(postprocessors, type);
		}
	}
}
