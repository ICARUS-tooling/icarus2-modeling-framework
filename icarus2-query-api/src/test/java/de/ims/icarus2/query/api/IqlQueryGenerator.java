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
import de.ims.icarus2.query.api.iql.IqlElement;
import de.ims.icarus2.query.api.iql.IqlElement.EdgeType;
import de.ims.icarus2.query.api.iql.IqlElement.IqlEdge;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlElement.IqlTreeNode;
import de.ims.icarus2.query.api.iql.IqlExpression;
import de.ims.icarus2.query.api.iql.IqlGroup;
import de.ims.icarus2.query.api.iql.IqlImport;
import de.ims.icarus2.query.api.iql.IqlLayer;
import de.ims.icarus2.query.api.iql.IqlObjectIdgenerator;
import de.ims.icarus2.query.api.iql.IqlPayload;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryType;
import de.ims.icarus2.query.api.iql.IqlProperty;
import de.ims.icarus2.query.api.iql.IqlQuantifier;
import de.ims.icarus2.query.api.iql.IqlQuantifier.QuantifierType;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlQueryElement;
import de.ims.icarus2.query.api.iql.IqlReference;
import de.ims.icarus2.query.api.iql.IqlReference.IqlMember;
import de.ims.icarus2.query.api.iql.IqlReference.IqlVariable;
import de.ims.icarus2.query.api.iql.IqlReference.MemberType;
import de.ims.icarus2.query.api.iql.IqlResult;
import de.ims.icarus2.query.api.iql.IqlResult.ResultType;
import de.ims.icarus2.query.api.iql.IqlResultInstruction;
import de.ims.icarus2.query.api.iql.IqlScope;
import de.ims.icarus2.query.api.iql.IqlSorting;
import de.ims.icarus2.query.api.iql.IqlSorting.Order;
import de.ims.icarus2.query.api.iql.IqlType;
import de.ims.icarus2.query.api.iql.IqlUnique;
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
		case EXPRESSION: prepareExpression((IqlExpression) element, build, config); break;
		case GROUP: prepareGroup((IqlGroup) element, build, config); break;
		case IMPORT: prepareImport((IqlImport) element, build, config); break;
		case LAYER: prepareLayer((IqlLayer) element, build, config); break;
		case MEMBER: prepareMember((IqlMember) element, build, config); break;
		case NODE: prepareNode((IqlNode) element, build, config); break;
		case PAYLOAD: preparePayload((IqlPayload) element, build, config); break;
		case PREDICATE: preparePredicate((IqlPredicate) element, build, config); break;
		case PROPERTY: prepareProperty((IqlProperty) element, build, config); break;
		case QUANTIFIER: prepareQuantifier((IqlQuantifier) element, build, config); break;
		case QUERY: prepareQuery((IqlQuery) element, build, config); break;
		case RESULT: prepareResult((IqlResult) element, build, config); break;
		case RESULT_INSTRUCTION: prepareResultInstruction((IqlResultInstruction) element, build, config); break;
		case SCOPE: prepareScope((IqlScope) element, build, config); break;
		case SORTING: prepareSorting((IqlSorting) element, build, config); break;
		case TERM: prepareTerm((IqlTerm) element, build, config); break;
		case TREE_NODE: prepareTreeNode((IqlTreeNode) element, build, config); break;
		case VARIABLE: prepareVariable((IqlVariable) element, build, config); break;

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

	private void prepareAliasedReference0(IqlAliasedReference reference, IncrementalBuild<?> build, Config config) {
		prepareUnique0(reference, build, config);

		// mandatory data
		reference.setName(index("name"));

		build.addFieldChange(reference::setName, "name", index("name"));
		build.addFieldChange(reference::setAlias, "alias", index("alias"));
	}

	private void prepareBinding(IqlBinding binding, IncrementalBuild<?> build, Config config) {
		prepareQueryElement0(binding, build, config);

		// mandatory data
		binding.setTarget(index("target"));
		binding.addMember(generateFull(IqlType.MEMBER, config));

		build.addFieldChange(binding::setDistinct, "distint", Boolean.TRUE);
		build.addFieldChange(binding::setDistinct, "distint", Boolean.FALSE);
		build.addFieldChange(binding::setTarget, "target", index("target"));

		for (int i = 0; i < config.getCount(IqlType.MEMBER, DEFAULT_COUNT); i++) {
			build.addNestedChange("member", IqlType.MEMBER, config, binding, binding::addMember);
		}
	}

	private void prepareConstraint0(IqlConstraint constraint, IncrementalBuild<?> build, Config config) {
		prepareUnique0(constraint, build, config);

		build.addFieldChange(constraint::setSolved, "solved", Boolean.TRUE);
		build.addFieldChange(constraint::setSolvedAs, "solvedAs", Boolean.TRUE);
	}

	private void preparePredicate(IqlConstraint.IqlPredicate predicate, IncrementalBuild<?> build, Config config) {
		prepareConstraint0(predicate, build, config);

		// mandatory data
		predicate.setExpression(generateFull(IqlType.EXPRESSION, config));

		build.addNestedChange("expression", IqlType.EXPRESSION, config, predicate, predicate::setExpression);
	}

	private void prepareTerm(IqlConstraint.IqlTerm term, IncrementalBuild<?> build, Config config) {
		prepareConstraint0(term, build, config);

		// mandatory data
		term.setLeft(generateFull(IqlType.PREDICATE, config));
		term.setRight(generateFull(IqlType.PREDICATE, config));
		term.setOperation(rng.random(BooleanOperation.class));

		build.addNestedChange("left", IqlType.PREDICATE, config, term, term::setLeft);
		build.addNestedChange("right", IqlType.PREDICATE, config, term, term::setRight);
		for(BooleanOperation operation : BooleanOperation.values()) {
			build.addEnumFieldChange(term::setOperation, "operation", operation);
		}
	}

	private void prepareCorpus(IqlCorpus corpus, IncrementalBuild<?> build, Config config) {
		prepareAliasedReference0(corpus, build, config);
	}

	private void prepareData(IqlData data, IncrementalBuild<?> build, Config config) {
		prepareUnique0(data, build, config);

		// mandatory data
		data.setVariable(index("variable"));
		data.setContent(index("content"));

		build.addFieldChange(data::setVariable, "variable", index("variable"));
		build.addFieldChange(data::setContent, "content", index("content"));
		build.addFieldChange(data::setChecksum, "checksum", index("checksum"));
	}

	private void prepareElement0(IqlElement element, IncrementalBuild<?> build, Config config) {
		prepareUnique0(element, build, config);

		build.addFieldChange(element::setLabel, "label", index("label"));
		build.addNestedChange("constraint", IqlType.PREDICATE, config, element, element::setConstraint);
	}

	private void prepareNode(IqlNode node, IncrementalBuild<?> build, Config config) {
		prepareElement0(node, build, config);

		for (int i = 0; i < config.getCount(IqlType.QUANTIFIER, DEFAULT_COUNT); i++) {
			build.addNestedChange("quantifier", IqlType.QUANTIFIER, config, node, node::addQuantifier);
		}
	}

	private void prepareTreeNode(IqlTreeNode treeNode, IncrementalBuild<?> build, Config config) {
		prepareNode(treeNode, build, config);

		if(config.tryNested(IqlType.TREE_NODE)) {
			for (int i = 0; i < config.getCount(IqlType.TREE_NODE, DEFAULT_COUNT); i++) {
				build.addNestedChange("children", IqlType.TREE_NODE, config, treeNode, treeNode::addChild);
			}
			config.endNested(IqlType.TREE_NODE);
		}
	}

	private void prepareEdge(IqlEdge edge, IncrementalBuild<?> build, Config config) {
		prepareElement0(edge, build, config);

		// mandatory data
		edge.setEdgeType(rng.random(EdgeType.class));
		edge.setSource(generateFull(IqlType.NODE, config));
		edge.setTarget(generateFull(IqlType.NODE, config));

		for(EdgeType edgeType : EdgeType.values()) {
			build.addEnumFieldChange(edge::setEdgeType, "edgeType", edgeType);
		}
		build.addNestedChange("source", IqlType.NODE, config, edge, edge::setSource);
		build.addNestedChange("target", IqlType.NODE, config, edge, edge::setTarget);
	}

	@SuppressWarnings("boxing")
	private void prepareExpression(IqlExpression expression, IncrementalBuild<?> build, Config config) {
		prepareQueryElement0(expression, build, config);

		// mandatory data
		expression.setContent(String.format("someTargetValue<(%s-%s(%d))", index("value"), index("func"), index()));

		build.addFieldChange(expression::setReturnType, "returnType", Dummy.class);
		build.addFieldChange(expression::setContent, "content",
				String.format("%s(123, %s)~\"val%d\"", index("func"), index("value"), index()));
	}

	@SuppressWarnings("boxing")
	private void prepareGroup(IqlGroup group, IncrementalBuild<?> build, Config config) {
		prepareUnique0(group, build, config);

		// mandatory data
		group.setGroupBy(generateFull(IqlType.EXPRESSION, config));
		group.setLabel(index("label"));

		build.addNestedChange("groupBy", IqlType.EXPRESSION, config, group, group::setGroupBy);
		build.addNestedChange("filterOn", IqlType.EXPRESSION, config, group, group::setFilterOn);
		build.addFieldChange(group::setLabel, "label", index("label"));
		// Include all typical value types!
		build.addFieldChange(group::setDefaultValue, "defaultValue", index("value"));
		build.addFieldChange(group::setDefaultValue, "defaultValue", true);
		build.addFieldChange(group::setDefaultValue, "defaultValue", false);
		build.addFieldChange(group::setDefaultValue, "defaultValue", Integer.MAX_VALUE);
		build.addFieldChange(group::setDefaultValue, "defaultValue", Long.MAX_VALUE);
		build.addFieldChange(group::setDefaultValue, "defaultValue", Double.MAX_VALUE);
	}

	private void prepareImport(IqlImport imp, IncrementalBuild<?> build, Config config) {
		prepareAliasedReference0(imp, build, config);

		build.addFieldChange(imp::setOptional, "optional", Boolean.TRUE);
	}

	private void prepareLayer(IqlLayer layer, IncrementalBuild<?> build, Config config) {
		prepareAliasedReference0(layer, build, config);

		build.addFieldChange(layer::setPrimary, "primary", Boolean.TRUE);
		build.addFieldChange(layer::setAllMembers, "allMembers", Boolean.TRUE);
	}

	private void preparePayload(IqlPayload payload, IncrementalBuild<?> build, Config config) {
		prepareQueryElement0(payload, build, config);

		// mandatory data
		payload.setQueryType(rng.random(QueryType.class));

		for(QueryType queryType : QueryType.values()) {
			build.addEnumFieldChange(payload::setQueryType, "queryType", queryType);
		}
		build.addNestedChange("constraint", IqlType.PREDICATE, config, payload, payload::setConstraint);
		for (int i = 0; i < config.getCount(IqlType.NODE, DEFAULT_COUNT); i++) {
			build.addNestedChange("elements", IqlType.NODE, config, payload, payload::addElement);
		}
	}

	@SuppressWarnings("boxing")
	private void prepareProperty(IqlProperty property, IncrementalBuild<?> build, Config config) {
		prepareQueryElement0(property, build, config);

		// mandatory data
		property.setKey(index("key"));

		build.addFieldChange(property::setKey, "key", index("key"));
		// Include all typical value types!
		build.addFieldChange(property::setValue, "value", index("value"));
		build.addFieldChange(property::setValue, "value", true);
		build.addFieldChange(property::setValue, "value", false);
		build.addFieldChange(property::setValue, "value", Integer.MAX_VALUE);
		build.addFieldChange(property::setValue, "value", Long.MAX_VALUE);
		build.addFieldChange(property::setValue, "value", Double.MAX_VALUE);
	}

	@SuppressWarnings("boxing")
	private void prepareQuantifier(IqlQuantifier quantifier, IncrementalBuild<?> build, Config config) {
		prepareQueryElement0(quantifier, build, config);

		// mandatory data
		quantifier.setQuantifierType(rng.random(QuantifierType.EXACT, QuantifierType.AT_LEAST, QuantifierType.AT_MOST));
		quantifier.setValue(index());

		for(QuantifierType quantifierType : QuantifierType.values()) {
			build.addEnumFieldChange(quantifier::setQuantifierType, "quantifierType", quantifierType);
		}

		build.addFieldChange(quantifier::setLowerBound, "lowerBound", index());
		build.addFieldChange(quantifier::setUpperBound, "upperBound", index());
		build.addFieldChange(quantifier::setValue, "value", index());
	}

	private void prepareQuery(IqlQuery query, IncrementalBuild<?> build, Config config) {
		prepareUnique0(query, build, config);

		// mandatory data
		query.setRawPayload(index("some-raw-payload"));
		query.addCorpus(generateFull(IqlType.CORPUS, config));
		query.setProcessedResult(generateFull(IqlType.RESULT, config));

		build.addFieldChange(query::setDialect, "dialect", index("dialect"));
		build.addFieldChange(query::setRawPayload, "rawPayload", index("payload-data"));
		build.addFieldChange(query::setRawGrouping, "rawGrouping", index("grouping-data"));
		build.addFieldChange(query::setRawResult, "rawResult", index("result-data"));
		build.addNestedChange("processedPayload", IqlType.PAYLOAD, config, query, query::setProcessedPayload);
		build.addNestedChange("processedResult", IqlType.RESULT, config, query, query::setProcessedResult);
		for (int i = 0; i < config.getCount(IqlType.IMPORT, DEFAULT_COUNT); i++) {
			build.addNestedChange("imports", IqlType.IMPORT, config, query, query::addImport);
		}
		for (int i = 0; i < config.getCount(IqlType.PROPERTY, DEFAULT_COUNT); i++) {
			build.addNestedChange("setup", IqlType.PROPERTY, config, query, query::addSetup);
		}
		for (int i = 0; i < config.getCount(IqlType.CORPUS, DEFAULT_COUNT); i++) {
			build.addNestedChange("corpora", IqlType.CORPUS, config, query, query::addCorpus);
		}
		for (int i = 0; i < config.getCount(IqlType.LAYER, DEFAULT_COUNT); i++) {
			build.addNestedChange("layers", IqlType.LAYER, config, query, query::addLayer);
		}
		for (int i = 0; i < config.getCount(IqlType.SCOPE, DEFAULT_COUNT); i++) {
			build.addNestedChange("scope", IqlType.SCOPE, config, query, query::addScope);
		}
		for (int i = 0; i < config.getCount(IqlType.GROUP, DEFAULT_COUNT); i++) {
			build.addNestedChange("grouping", IqlType.GROUP, config, query, query::addGrouping);
		}
		for (int i = 0; i < config.getCount(IqlType.DATA, DEFAULT_COUNT); i++) {
			build.addNestedChange("embeddedData", IqlType.DATA, config, query, query::addEmbeddedData);
		}
	}

	private void prepareReference0(IqlReference reference, IncrementalBuild<?> build, Config config) {
		prepareUnique0(reference, build, config);

		// mandatory data
		reference.setName(index("name"));

		build.addFieldChange(reference::setName, "name", index("name"));
	}

	private void prepareMember(IqlMember member, IncrementalBuild<?> build, Config config) {
		prepareReference0(member, build, config);

		// mandatory data
		member.setMemberType(rng.random(MemberType.class));

		if(build.isRoot(member)) {
			for(MemberType memberType : MemberType.values()) {
				build.addEnumFieldChange(member::setMemberType, "memberType", memberType);
			}
		} else {
			build.addEnumFieldChange(member::setMemberType, "memberType", rng.random(MemberType.class));
		}
	}

	private void prepareVariable(IqlVariable variable, IncrementalBuild<?> build, Config config) {
		prepareReference0(variable, build, config);
	}

	@SuppressWarnings("boxing")
	private void prepareResult(IqlResult result, IncrementalBuild<?> build, Config config) {
		prepareQueryElement0(result, build, config);

		// mandatory data
		result.addResultType(rng.random(ResultType.class));

		build.addFieldChange(result::setLimit, "limit", Long.MAX_VALUE);
		for(ResultType resultType : ResultType.values()) {
			build.addEnumFieldChange(result::addResultType, "resultTypes", resultType);
		}
		for (int i = 0; i < config.getCount(IqlType.RESULT_INSTRUCTION, DEFAULT_COUNT); i++) {
			build.addNestedChange("resultInstructions", IqlType.RESULT_INSTRUCTION, config, result, result::addResultInstruction);
		}
		for (int i = 0; i < config.getCount(IqlType.SORTING, DEFAULT_COUNT); i++) {
			build.addNestedChange("sortings", IqlType.SORTING, config, result, result::addSorting);
		}
	}

	private void prepareResultInstruction(IqlResultInstruction instruction, IncrementalBuild<?> build, Config config) {
		prepareUnique0(instruction, build, config);

		//TODO
	}

	private void prepareScope(IqlScope scope, IncrementalBuild<?> build, Config config) {
		prepareAliasedReference0(scope, build, config);

		// mandatory data
		scope.addLayer(generateFull(IqlType.LAYER, config));

		build.addFieldChange(scope::setPrimary, "primary", Boolean.TRUE);
		for (int i = 0; i < config.getCount(IqlType.LAYER, DEFAULT_COUNT); i++) {
			build.addNestedChange("layer", IqlType.LAYER, config, scope, scope::addLayer);
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
		build.addNestedChange("expression", IqlType.EXPRESSION, config, sorting, sorting::setExpression);
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

		IqlObjectIdgenerator idGenerator = new IqlObjectIdgenerator();

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
