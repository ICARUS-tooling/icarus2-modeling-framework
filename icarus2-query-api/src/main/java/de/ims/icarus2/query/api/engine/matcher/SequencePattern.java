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
package de.ims.icarus2.query.api.engine.matcher;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.engine.matcher.mark.Interval;
import de.ims.icarus2.query.api.engine.matcher.mark.Marker.RangeMarker;
import de.ims.icarus2.query.api.engine.matcher.mark.SequenceMarker;
import de.ims.icarus2.query.api.exp.Assignable;
import de.ims.icarus2.query.api.exp.EvaluationContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.ElementContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.LaneContext;
import de.ims.icarus2.query.api.exp.EvaluationUtils;
import de.ims.icarus2.query.api.exp.Expression;
import de.ims.icarus2.query.api.exp.ExpressionFactory;
import de.ims.icarus2.query.api.exp.Literals;
import de.ims.icarus2.query.api.exp.LogicalOperators;
import de.ims.icarus2.query.api.iql.IqlConstraint;
import de.ims.icarus2.query.api.iql.IqlConstraint.BooleanOperation;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlPredicate;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlTerm;
import de.ims.icarus2.query.api.iql.IqlElement;
import de.ims.icarus2.query.api.iql.IqlElement.IqlElementDisjunction;
import de.ims.icarus2.query.api.iql.IqlElement.IqlGrouping;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlElement.IqlSequence;
import de.ims.icarus2.query.api.iql.IqlExpression;
import de.ims.icarus2.query.api.iql.IqlMarker;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerCall;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryModifier;
import de.ims.icarus2.query.api.iql.IqlQuantifier;
import de.ims.icarus2.query.api.iql.IqlQuantifier.QuantifierModifier;
import de.ims.icarus2.query.api.iql.IqlUtils;
import de.ims.icarus2.query.api.iql.NodeArrangement;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * Implements the state machine to match a node sequence defined in the
 * IQL protocol.
 * <p>
 * This implementation splits the actual state machine and the state buffer
 * for matching into different classes/objects: An instance of this class
 * only holds the actual state machine with all its node and transition logic.
 * The {@link #matcher()} method must be used to create a new matcher object
 * that can be used within a single thread to match {@link Container} instances.
 * <p>
 * For a comprehensive documentation of the IQL features visit the official
 * <a href="https://github.com/ICARUS-tooling/icarus2-modeling-framework/blob/dev/icarus2-query-api/doc/iql_specification.pdf">
 * documentation</a>.
 * A explanation of the complete ICQP state machine is available
 * <a href="https://github.com/ICARUS-tooling/icarus2-modeling-framework/blob/dev/icarus2-query-api/doc/icqp_specification.pdf">
 * here</a>.
 * <p>
 * This implementation is heavily influenced by the {@link Pattern} class of the java regex
 * framework and as such many helper classes, methods or variables are named similarly,
 * but usually offer slightly different or extended functionality. The concept of separating
 * the state machine for matching and the actual matcher state remains the same though.
 * <p>
 * A typical usage scenario looks like the following:
 * <pre><code>
 * SequencePattern.Builder builder = SequencePattern.builder();
 * ...                                                           // configure builder
 * SequencePattern pattern = builder.build();                    // obtain state machine
 *
 * SequenceMatcher matcher = pattern.matcher();                  // instantiate a new matcher
 *
 * </code></pre>
 *
 * @author Markus Gärtner
 *
 */
@NotThreadSafe
public class SequencePattern {

	public static Builder builder() {
		return new Builder();
	}


	/** Dummy node usable as tail if query is simple */
	static Node accept = new Node();

	private final AtomicInteger matcherIdGen = new AtomicInteger(0);

	/** The IQL source of structural constraints for this matcher. */
	private final IqlElement source;
	/** Defines the general direction for matching. ANY is equivalent to FIRST here. */
	private final QueryModifier modifier;
	/** The root context for evaluations in this pattern */
	private final LaneContext context;
	/** Blueprint for instantiating a new {@link SequenceMatcher} */
	private final StateMachineSetup setup;

	private SequencePattern(Builder builder) {
		modifier = builder.getModifier();
		source = builder.getRoot();
		context = builder.geContext();

		SequenceQueryProcessor proc = new SequenceQueryProcessor(builder);
		setup = proc.createStateMachine();

		//TODO
	}

	/**
	 * Crates a new matcher that uses this state machine and that can be safely used from
	 * within a single thread.
	 * <p>
	 * Important Note:<br>
	 * This method <b>MUST</b> be called from the thread that is intended to
	 * be used for the actual matching, as the underlying {@link EvaluationContext}
	 * instances use {@link ThreadLocal} to fetch expressions and assignables.
	 */
	//TODO add arguments to delegate result buffer dispatch output and monitoring
	public Matcher<Container> matcher() {
		int id = matcherIdGen.getAndIncrement();

		return new SequenceMatcher(setup, id);
	}

	private static final int INITIAL_SIZE = 1<<10;

	/**
	 * Encapsulates all the information needed to instantiate a matcher for
	 * the sequence matching state machine.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static class StateMachineSetup {

		/** Stores all the raw node definitions from the query extracted from {@link #source} */
		IqlNode[] nodes = {};
		/** Constraint from the 'FILTER BY' section */
		Supplier<Matcher<Container>> filterConstraint;
		/** Constraint from the 'HAVING' section */
		Supplier<Expression<?>> globalConstraint;
		/** Maximum number of reported to report per container. Either -1 or a positive value. */
		long limit = UNSET_LONG;
		/** Entry point to the object graph of the state machine */
		Node root;
		/** Total number of caches needed for this pattern */
		int cacheCount = 0;
		/** Total number of int[] buffers needed by nodes */
		int bufferCount = 0;
		/** Original referential intervals */
		Interval[] intervals = {};
		/** Keeps track of all the proper nodes. Used for monitoring */
		ProperNode[] properNodes = {};
		/** Lists all the markers used by original nodes */
		RangeMarker[] markers = {};
		/** Positions into 'intervals' to signal what intervals to update for what marker */
		int[] markerPos = {};
		/** Blueprints for creating new {@link NodeMatcher} instances per thread */
		@SuppressWarnings("unchecked")
		Supplier<Matcher<Item>>[] matchers = new Supplier[0];
		/** Blueprints for creating member storages per thread */
		@SuppressWarnings("unchecked")
		Supplier<Assignable<? extends Item>>[] members = new Supplier[0];


		// Access methods for the matcher/state
		Node getRoot() { return root; }
		RangeMarker[] getMarkers() { return markers; }
		int[] getMarkerPos() { return markerPos; }
		IqlNode[] getNodes() { return nodes; }
		int[] getHits() { return new int[nodes.length]; }
		Matcher<Container> makeFilterConstraint() {
			return filterConstraint==null ? null : filterConstraint.get();
		}
		Expression<?> makeGlobalConstraint() {
			return globalConstraint==null ? null : globalConstraint.get();
		}
		Matcher<Item>[] makeMatchers() {
			return Stream.of(matchers)
				.map(Supplier::get)
				.toArray(Matcher[]::new);
		}
		Assignable<? extends Item>[] makeMembers() {
			return Stream.of(members)
				.map(Supplier::get)
				.toArray(Assignable[]::new);
		}
		Cache[] makeCaches() {
			return IntStream.range(0, cacheCount)
				.mapToObj(i -> new Cache())
				.toArray(Cache[]::new);
		}
		Interval[] makeIntervals() {
			return  Stream.of(intervals)
					.map(Interval::clone)
					.toArray(Interval[]::new);
		}
		int[][] makeBuffer() {
			return IntStream.range(0, bufferCount)
				.mapToObj(i -> new int[INITIAL_SIZE])
				.toArray(int[][]::new);
		}
	}

	/** Utility class for generating the state machine */
	static class SequenceQueryProcessor {
		private static final ObjectMapper mapper = IqlUtils.createMapper();
		private static String serialize(IqlElement element) {
			try {
				return mapper.writeValueAsString(element);
			} catch (JsonProcessingException e) {
				throw new QueryException(GlobalErrorCode.INTERNAL_ERROR,
						"Failed to serialize element", e);
			}
		}

		Node head;
		Node tail = accept;
		int cacheCount;
		int bufferCount;
		Supplier<Matcher<Container>> filter;
		Supplier<Expression<?>> global;
		ElementContext context;
		ExpressionFactory expressionFactory;
		IqlMarker pendingMarker;

		int id;

		final LaneContext rootContext;
		final QueryModifier modifier;
		final List<IqlNode> nodes = new ArrayList<>();
		final List<Interval> intervals = new ArrayList<>();
		final List<NodeDef> matchers = new ArrayList<>();
		final List<MemberDef> members = new ArrayList<>();
		final Set<ProperNode> properNodes = new ReferenceOpenHashSet<>();
		final List<RangeMarker> markers = new ArrayList<>();
		final IntList markerPos = new IntArrayList();
		final long limit;
		final IqlElement rootElement;
		final IqlConstraint filterConstraint;
		final IqlConstraint globalConstraint;

		SequenceQueryProcessor(Builder builder) {
			rootContext = builder.geContext();
			modifier = builder.getModifier();
			limit = builder.getLimit();
			rootElement = builder.getRoot();
			filterConstraint = builder.getFilterConstraint();
			globalConstraint = builder.getGlobalConstraint();
		}

		/** Process element and link to active sequence */
		private Node process(IqlElement source) {
			Node node;

			switch (source.getType()) {
			case GROUPING: node = grouping((IqlGrouping) source); break;
			case SEQUENCE: node = sequence((IqlSequence) source); break;
			case NODE: node = node((IqlNode) source); break;
			case DISJUNCTION: node = disjunction((IqlElementDisjunction) source); break;

			default:
				// We do not support IqlTreeNode and IqlEdge here!!
				throw EvaluationUtils.forUnsupportedQueryFragment("element", source.getType());
			}

			pushTail(node);

			//TODO post-process node?

			return node;
		}

		// RAW ELEMENT PROCESSING

		/** Process single node and link to active sequence */
		private Node node(IqlNode source) {

			final List<IqlQuantifier> quantifiers = source.getQuantifiers();
			final IqlMarker marker = source.getMarker().orElse(null);
			final IqlConstraint constraint = source.getConstraint().orElse(null);
			final String label = source.getLabel().orElse(null);

			if(marker!=null && pendingMarker!=null) {
				if(pendingMarker!=null)
					throw new QueryException(QueryErrorCode.INCORRECT_USE,
							"Unresolved pending marker, this usually happens when using the "
							+ "'ADJACENT' modifier with multiple marker-bearing nodes. Affected node:\n"
							+ serialize(source));

				pendingMarker = marker;
			}

			// Prepare context
			context = rootContext.derive()
					.element(source)
					.build();

			// Process actual node content
			Node atom;
			if(constraint==null) {
				atom = empty(label);
			} else {
				expressionFactory = new ExpressionFactory(context);
				atom = single(label, constraint);
				expressionFactory = null;
			}

			// Handle quantifiers
			Node node = quantify(atom, quantifiers);

			// Reset context
			context = null;

			return node;
		}

		/** Process (quantified) node group and link to active sequence */
		private Node grouping(IqlGrouping source) {
			final List<IqlQuantifier> quantifiers = source.getQuantifiers();

			final Node oldTail = replaceTail(accept);
			final Node head = looseGroup(source.getElements());
			replaceTail(oldTail);

			return quantify(head, quantifiers);
		}

		/** Process (ordered or adjacent) node sequence and link to active sequence */
		private Node sequence(IqlSequence source) {
			final List<IqlElement> elements = source.getElements();
			if(source.getArrangement()==NodeArrangement.ADJACENT) {
				return adjacentGroup(elements);
			}
			return looseGroup(elements);
		}

		/** Process alternatives and link to active sequence */
		private Node disjunction(IqlElementDisjunction source) {
			final List<IqlElement> elements = source.getAlternatives();
			final List<Node> atoms = new ArrayList<>(elements.size());
			final Node conn = branchStart();
			for(IqlElement element : elements) {
				atoms.add(process(element));
				branchRestart(conn);
			}
			return branchEnd(conn, atoms.toArray(new Node[atoms.size()]));
		}

		// INTERNAL HELPERS

		/** Fetch current tail */
		private Node tail() { return tail; }

		/** Link node to tail, set tail to new node and return it */
		private <N extends Node> N pushTail(N node) {
			node.next = tail;
			tail = node;
			return node;
		}

		/** Replace tail by node and return old tail */
		private Node replaceTail(Node node) {
			final Node oldTail = tail;
			tail = node;
			return oldTail;
		}

		private int id() { return id++; }

		private int member(@Nullable String label) {
			if(label==null) {
				return UNSET_INT;
			}
			final MemberDef memberDef = new MemberDef(label, context);
			final int memberId = members.size();
			members.add(memberDef);
			return memberId;
		}

		private Assignable<? extends Item> elementStore() {
			return context.getElementStore().orElseThrow(
					() -> EvaluationUtils.forInternalError("No element store available"));
		}

		/**
		 * Transforms a {@link IqlConstraint constraint} object into a boolean
		 * expression that is linked to the specified {@link EvaluationContext}.
		 * <p>
		 * If the {@code constraint} is already marked as {@link IqlConstraint#isSolved() solved},
		 * this method will only return a constant expression bearing the corresponding
		 * {@link IqlConstraint#isSolvedAs() value}.
		 */
		private Expression<?> constraint(IqlConstraint source) {
			if(source.isSolved()) {
				return Literals.of(source.isSolvedAs());
			}

			switch (source.getType()) {
			case TERM: {
				final IqlTerm term = (IqlTerm) source;
				return term(term.getOperation()==BooleanOperation.DISJUNCTION, term.getItems());
			}
			case PREDICATE: {
				final IqlPredicate predicate = (IqlPredicate) source;
				return predicate(predicate.getExpression());
			}
			default:
				throw EvaluationUtils.forUnsupportedQueryFragment("constraint", source.getType());
			}
		}

		private Expression<?> term(boolean disjunction, List<IqlConstraint> expressions) {
			final Expression<?>[] elements = expressions.stream()
					.map(this::constraint)
					.toArray(Expression[]::new);
			// Don't care about optimization here, the NodeDef wrapper will take care of this
			return disjunction ? LogicalOperators.disjunction(elements, true)
					: LogicalOperators.conjunction(elements, true);
		}

		private Expression<?> predicate(IqlExpression expression) {
			return expressionFactory.process(expression.getContent());
		}

		private int matcher(IqlConstraint constraint) {
			assert context!=null: "missing evaluation context";
			assert expressionFactory!=null: "missing expression factory";

			final NodeDef nodeDef = new NodeDef(id(), elementStore(), constraint(constraint), context);
			final int nodeId = matchers.size();
			matchers.add(nodeDef);
			return nodeId;
		}

		/** Attaches a scan to current tail that honors (multiple) intervals */
		private Node explore(boolean forward, boolean cached) {
			/*
			 * No matter how complex the potentially pending marker construct is,
			 * the attached scan will always work with the same tail (at least
			 * content-wise). Therefore we can use a shared cache for all the
			 * branches and save evaluation time.
			 */
			final int cacheId = cached ? cache() : UNSET_INT;

			if(pendingMarker!=null) {
				//TODO analyze marker and build branch/clip/intersection nodes
			}

			//TODO in case of multiple branches for intervals, consider using a shared cache for scans?
			return pushTail(scan(forward, cacheId, UNSET_INT));
		}

		private int cache() { return cacheCount++; }

		private int buffer() { return bufferCount++; }

		private RangeMarker marker(IqlMarkerCall call) {
			Number[] arguments = IntStream.range(0, call.getArgumentCount())
					.mapToObj(call::getArgument)
					.map(Number.class::cast)
					.toArray(Number[]::new);
			return SequenceMarker.of(call.getName(), arguments);
		}

		/** Push intervals for given marker on the stack and return index of first interval */
		private int interval(RangeMarker marker) {
			final int intervalIndex = intervals.size();
			for (int i = 0; i < marker.intervalCount(); i++) {
				markerPos.add(intervals.size());
				intervals.add(Interval.blank());
			}
			return intervalIndex;
		}

		private int interval() {
			final int intervalIndex = intervals.size();
			intervals.add(Interval.blank());
			return intervalIndex;
		}

		private int intervalRef(int targetIndex, int shift) {
			final int intervalIndex = intervals.size();
			intervals.add(new IntervalRef(targetIndex, shift));
			return intervalIndex;
		}

		private Empty empty(@Nullable String label) {
			return new Empty(member(label));
		}

		private Begin begin() { return new Begin(); }

		private Finish finish(long limit) { return new Finish(limit); }

		private Single single(@Nullable String label, IqlConstraint constraint) {
			return new Single(id(), matcher(constraint), cache(), member(label));
		}

		private Scan scan(boolean forward, int cacheId, int intervalIndex) {
			return new Scan(id(), cacheId, intervalIndex, forward);
		}

		private Negation negate(Node atom) {
			return new Negation(id(), atom);
		}

		/** Start a branch by placing a fresh {@link BranchConn} as tail */
		private BranchConn branchStart() { return pushTail(new BranchConn()); }

		/** Replace tail by given {@link BranchConn} instance */
		private void branchRestart(Node conn) { replaceTail(conn); }

		/** Wrap up branch by replacing tail with new {@link Branch} instance */
		private Branch branchEnd(Node conn, Node...atoms) {
			final Branch branch = new Branch(id(), conn, atoms);
			replaceTail(branch);
			return branch;
		}

		private Repetition repetition(Node atom, int cmin, int cmax, int mode) {
			return new Repetition(id(), atom, cmin, cmax, mode, buffer(), buffer());
		}

		/** Sequential scanning of ordered elements */
		private Node looseGroup(List<IqlElement> elements) {
			Node head = null;
			for(int i=elements.size()-1; i>=0; i--) {
				head = process(elements.get(i));
				head = explore(true, true);
			}
			return head;
		}

		/** Sequential check without scanning */
		private Node adjacentGroup(List<IqlElement> elements) {
			Node head = null;
			for(int i=elements.size()-1; i>=0; i--) {
				head = process(elements.get(i));
			}
			return head;
		}

		private int mode(IqlQuantifier quantifier) {
			switch (quantifier.getQuantifierModifier()) {
			case GREEDY: return GREEDY;
			case POSSESSIVE: return POSSESSIVE;
			case RELUCTANT: return RELUCTANT;
			default: throw EvaluationUtils.forUnsupportedQueryFragment("quantifier mode",
					quantifier.getQuantifierModifier());
			}
		}

		/** Creates nodes to handle quantification and attaches to sequence */
		private Node quantify(Node atom, List<IqlQuantifier> quantifiers) {
			if(quantifiers.isEmpty()) {
				// No quantification -> use atom as is
				return pushTail(atom);
			} else if(quantifiers.size()==1) {
				// Singular quantifier -> simple wrapping
				return quantify(atom, quantifiers.get(0));
			} else {
				// Combine all quantifiers into a branch structure
				final Node conn = branchStart();
				final List<Node> atoms = new ArrayList<>(quantifiers.size());
				for(IqlQuantifier quantifier : quantifiers) {
					atoms.add(quantify(atom, quantifier));
					branchRestart(conn);
				}
				return branchEnd(conn, atoms.toArray(new Node[atoms.size()]));
			}
		}

		/** Wraps a quantification node around atom and attaches to sequence */
		private Node quantify(Node atom, IqlQuantifier quantifier) {
			Node node;
			if(quantifier.isExistentiallyNegated()) {
				node = negate(atom);
			} else {
				int min = 1;
				int max = Integer.MAX_VALUE;
				int mode = GREEDY;
				switch (quantifier.getQuantifierType()) {
				case ALL: { // *
					mode = POSSESSIVE;
				} break;
				case EXACT: { // n
					min = max = quantifier.getValue().getAsInt();
					mode = GREEDY;
				} break;
				case AT_LEAST: { // n+
					min = quantifier.getValue().getAsInt();
					mode = mode(quantifier);
				} break;
				case AT_MOST: { // 1..n
					max = quantifier.getValue().getAsInt();
					mode = mode(quantifier);
				} break;
				case RANGE: { // n..m
					min = quantifier.getLowerBound().getAsInt();
					max = quantifier.getUpperBound().getAsInt();
				} break;

				default:
					throw EvaluationUtils.forUnsupportedQueryFragment("quantifier", quantifier.getQuantifierType());
				}

				if(min==max && min==1) {
					// Don't bother with repetition for single count
					node = atom;
				} else {
					node = repetition(atom, min, max, mode);
				}
			}
			return pushTail(node);
		}

		StateMachineSetup createStateMachine() {
			// Ensure we have a proper structural constraint here
			rootElement.checkIntegrity();

			//TODO run a verification of marker+quantifier combinations

			// Start at the end of the state machine
			replaceTail(finish(limit));

			//TODO for now we don't honor the 'consumed' flag on IqlElement instances
			Node root = process(rootElement);

			// Prepare top-level scan
			replaceTail(root);
			if(modifier==QueryModifier.LAST) {
				root = explore(false, false); // backwards scan
			} else {
				root = explore(true, false); // default forward scan
			}

			// Add size-based filter
			root = pushTail(begin());

			// Fill state machine
			StateMachineSetup sm = new StateMachineSetup();
			sm.properNodes = properNodes.stream().sorted().toArray(ProperNode[]::new);
			sm.filterConstraint = filter;
			sm.globalConstraint = global;
			sm.nodes = nodes.toArray(new IqlNode[0]);
			sm.limit = limit;
			sm.root = root;
			sm.properNodes = properNodes.stream().sorted().toArray(ProperNode[]::new);
			sm.cacheCount = cacheCount;
			sm.markerPos = markerPos.toIntArray();
			sm.markers = markers.toArray(new RangeMarker[0]);
			sm.matchers = matchers.toArray(new Supplier[0]);

			return sm;
		}
	}

	/**
	 * Contains all the state information for a {@link SequenceMatcher}
	 * operating on a single thread.
	 * <p>
	 * This class mainly exists as an intermediary access point for
	 * testing the functionality of {@link Node} implementations and
	 * other aspects of the state machine for sequence matching.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static class State extends Interval {
		/** Items in target container, copied for faster access */
		Item[] elements;

		/** Raw nodes from the query, order matches the items in 'matchers' */
		final IqlNode[] nodes;
		/** All the atomic nodes defined in the query */
		final Matcher<Item>[] matchers;
		/** Storage end points for mapping member labels to matched instances */
		final Assignable<? extends Item>[] members;
		/** Caches used by various nodes */
		final Cache[] caches;
		/** Raw position intervals and referential intervals used by nodes */
		final Interval[] intervals;
		/** All the raw markers that produce restrictions on node positions */
		final RangeMarker[] markers;
		/** Positions into 'intervals' to signal what intervals to update for what marker */
		final int[] markerPos;
		/** Positions of referential intervals */
		final int[] intervalRefs;
		/** Keeps track of the last hit index for every raw node */
		final int[] hits;
		/** The available int[] buffers used by various node implementations */
		final int[][] buffers;

		final Matcher<Container> filterConstraint;
		final Expression<?> globalConstraint;

		/** Total number of reported full matches so far */
		long reported = 0;


		/** Number of mappings stored so far and also the next insertion index */
		int entry = 0;

		/** Keys for the node mapping */
		int[] m_node = new int[INITIAL_SIZE];
		/** Values for the node mapping, i.e. the associated indices */
		int[] m_pos = new int[INITIAL_SIZE];

		/** Total number of elements in the target sequence. */
		int size = UNSET_INT;
		/** Set by the Finish node if a result limit exists and we already found enough matches. */
		boolean finished;

		/**
		 * End index of the last (sub)match, used by repetitions to keep track.
		 * Initially {@code 0}, turned to {@code -1} for failed matches and to
		 * the next index to be visited when a match occurred.
		 */
		int last = 0;

		Consumer<State> resultHandler;

		State(StateMachineSetup setup) {
			elements = new Item[INITIAL_SIZE];
			markers = setup.getMarkers();
			markerPos = setup.getMarkerPos();
			nodes = setup.getNodes();
			hits = setup.getHits();

			filterConstraint = setup.makeFilterConstraint();
			globalConstraint = setup.makeGlobalConstraint();

			matchers = setup.makeMatchers();
			members = setup.makeMembers();
			caches = setup.makeCaches();
			intervals = setup.makeIntervals();
			buffers = setup.makeBuffer();

			intervalRefs = complement(markerPos, intervals.length);

			from = 0;
			to = 0;
		}

		private static int[] complement(int[] markerPos, int size) {
			if(size==0) {
				return new int[0];
			}

			IntSet markers = new IntOpenHashSet();
			for (int pos : markerPos) {
				markers.add(pos);
			}

			IntList comp = new IntArrayList();
			for (int i = 0; i < size; i++) {
				if(!markers.contains(i)) {
					comp.add(i);
				}
			}

			return comp.toIntArray();
		}

		int scope() {
			return entry;
		}

		void reset(int scope) {
			entry = scope;
		}

		/** Resolve raw node for 'nodeId' and map to 'index' in result buffer. */
		void map(int nodeId, int index) {
			m_node[entry] = nodeId;
			m_pos[entry] = index;
			entry++;
		}

		@Override
		public void reset() {
			// Cleanup duty -> we must erase all references to target and its elements
			Arrays.fill(elements, 0, size, null);
			Arrays.fill(hits, UNSET_INT);
			entry = 0;
			last = 0;
			to = 0;
		}

		void dispatchMatch() {
			if(resultHandler!=null) {
				resultHandler.accept(this);
			}
			//TODO create immutable and serializable object from current state and send it to subscriber
			//TODO increment reported counter upon dispatching
		}
	}

	/**
	 * Public entry point for sequence matching and holder of the
	 * state during a matching operation.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static class SequenceMatcher extends State implements Matcher<Container> {

		/** The only thread allowed to call {@link #matches(long, Container)} on this instance */
		final Thread thread;

		final int id;

		/** The node of the state machine to start matching with. */
		final Node root;

//		/** The source of this matcher */
//		private final SequencePattern pattern;

		//TODO add

		SequenceMatcher(StateMachineSetup stateMachineSetup, int id) {
			super(stateMachineSetup);

			this.id = id;
			thread = Thread.currentThread();
			this.root = stateMachineSetup.getRoot();
		}

		@Override
		public int id() { return 0; }

		/**
		 * @see de.ims.icarus2.query.api.engine.matcher.Matcher#matches(long, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public boolean matches(long index, Container target) {
			// Sanity check to make sure we operate on the correct thread
			checkState("Illegal access from foreign thread", thread==Thread.currentThread());

			// Apply pre-filtering if available to reduce matcher overhead
			if(filterConstraint!=null && !filterConstraint.matches(index, target)) {
				return false;
			}

			int size = strictToInt(target.getItemCount());
			// If new size exceeds buffer, grow all storages
			if(size>=elements.length) {
				int newSize = CollectionUtils.growSize(elements.length, size);
				elements = new Item[newSize];
				m_node = new int[newSize];
				m_pos = new int[newSize];
				for (int i = 0; i < buffers.length; i++) {
					buffers[i] = new int[newSize];
				}
			}
			// Now copy container content into our buffer for faster access during matching
			for (int i = 0; i < size; i++) {
				elements[i] = target.getItemAt(i);
			}
			this.size = size;
			to = size-1;

			// Update raw intervals
			for (int i = 0; i < markers.length; i++) {
				RangeMarker marker = markers[i];
				marker.adjust(intervals, markerPos[i], size);
			}

			// Update interval refs
			for (int i = 0; i < intervalRefs.length; i++) {
				IntervalRef ref = (IntervalRef) intervals[intervalRefs[i]];
				ref.update(this);
			}

			// Let the state machine do its work
			boolean matched = root.match(this, 0);
			/*
			 * Stable predicates at this point:
			 *  - All hits reported.
			 *  - Global constraints evaluated.
			 */
			reset();

			return matched;
		}
	}

	public static class Builder extends AbstractBuilder<Builder, SequencePattern> {

		private IqlElement root;
		private int id = UNSET_INT;
		private QueryModifier modifier = QueryModifier.ANY;
		private long limit = UNSET_LONG;
		private IqlConstraint filterConstraint;
		private IqlConstraint globalConstraint;
		private LaneContext context;

		//TODO add fields for configuring the result buffer

		private Builder() { /* no-op */ }

		//TODO add all the setter methods

		public int getId() { return id; }

		public QueryModifier getModifier() { return modifier; }

		public long getLimit() { return limit; }

		public IqlElement getRoot() { return root; }

		public IqlConstraint getFilterConstraint() { return filterConstraint; }

		public IqlConstraint getGlobalConstraint() { return globalConstraint; }

		public LaneContext geContext() { return context; }

		@Override
		protected void validate() {
			super.validate();

			checkState("No root element defined", root!=null);
			checkState("Id not defined", id>=0);
			checkState("Context not defined", context!=null);
			checkState("Context is not a root context", context.isRoot());
		}

		@Override
		protected SequencePattern create() { return new SequencePattern(this); }
	}

	//TODO add mechanics to properly collect results from multiple buffers
	@Deprecated
	static class ResultBuffer {
		/** Index of the UoI that is being searched */
		private long index = UNSET_LONG;

		/** Number of mappings stored so far and also the next insertion index */
		private int size = 0;

		private static final int INITIAL_SIZE = 1<<10;

		/** Position in the target sequence for mappings */
		private int[] indices = new int[INITIAL_SIZE];
		/** Associated node that is matched to a certain index */
		private IqlNode[] nodes = new IqlNode[INITIAL_SIZE];

		void reset(long index, int size) {
			this.index = index;
			this.size = 0;
			if(size>=indices.length) {
				int newSize = CollectionUtils.growSize(size);
				indices = Arrays.copyOf(indices, newSize);
				nodes = Arrays.copyOf(nodes, newSize);
			}
		}

		//TODO method for transformation into serializable result object

		int scope() {
			return size;
		}

		void reset(int scope) {
			size = scope;
		}

		void map(IqlNode node, int index) {
			nodes[size] = node;
			indices[size] = index;
			size++;
		}

		void dispatch() {
			//TODO create immutable and serializable object from current state and send it to subscriber
			//TODO increment reported counter upon dispatching
		}
	}

	/**
	 * Encapsulates information to instantiate a new {@link Expression}.
	 * THe constraint {@link Expression} supplied will be
	 * {@link EvaluationContext#optimize(Expression) optimized} autoamtically.
	 */
	static class ExpressionDef implements Supplier<Expression<?>> {
		final Expression<?> constraint;
		final EvaluationContext context;

		ExpressionDef(Expression<?> constraint, EvaluationContext context) {
			requireNonNull(constraint);
			this.context = requireNonNull(context);
			this.constraint = context.optimize(constraint);
		}

		@Override
		public Expression<?> get() {
			return context.duplicate(constraint);
		}
	}

	/**
	 * Buffer for all the information needed to create a {@link NodeMatcher}
	 * for a new {@link SequenceMatcher} instance.
	 */
	static class NodeDef implements Supplier<Matcher<Item>> {
		final Assignable<? extends Item> element;
		final Supplier<Expression<?>> constraints;
		final EvaluationContext context;
		final int id;

		NodeDef(int id, Assignable<? extends Item> element, Expression<?> constraint,
				EvaluationContext context) {
			this.context = requireNonNull(context);
			this.constraints = new ExpressionDef(constraint, context);
			this.id = id;
			this.element = requireNonNull(element);
		}

		@Override
		public Matcher<Item> get() {
			Assignable<? extends Item> element = context.duplicate(this.element);
			Expression<?> constraint = constraints.get();
			return new NodeMatcher(id, element, constraint);
		}
	}

	/**
	 * Buffer for all the information needed to create a {@link ContainerMatcher}
	 * for a new {@link SequenceMatcher} instance.
	 */
	static class FilterDef implements Supplier<Matcher<Container>> {
		final Assignable<? extends Container> element;
		final Supplier<Expression<?>> constraints;
		final EvaluationContext context;

		FilterDef(Assignable<? extends Container> lane, Expression<?> constraint,
				EvaluationContext context) {
			this.context = requireNonNull(context);
			this.constraints = new ExpressionDef(constraint, context);
			this.element = requireNonNull(lane);
		}

		@Override
		public Matcher<Container> get() {
			Assignable<? extends Container> lane = context.duplicate(this.element);
			Expression<?> constraint = constraints.get();
			return new ContainerMatcher(lane, constraint);
		}
	}

	static class MemberDef implements Supplier<Assignable<? extends Item>> {
		final String name;
		final EvaluationContext context;

		MemberDef(String name, EvaluationContext context) {
			this.name = checkNotEmpty(name);
			this.context = requireNonNull(context);
		}

		@Override
		public Assignable<? extends Item> get() {
			return context.getMember(name).orElseThrow(
					() -> EvaluationUtils.forUnknownIdentifier(name, "assignable item"));
		}
	}

	/**
	 * Models an interval that effectively represents the shifting of some referenced
	 * <i>original</i> interval by a set amount.
	 * The use case is keeping track of the legal index intervals for nodes that did
	 * not define their own markers but that are tied in some way to at least one node
	 * that did come with markers.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static final class IntervalRef extends Interval {
		/** Expansion amount of the interval, typically a negative value */
		private final int shift;
		/** Pointer to the original interval to be used for shifting */
		private final int intervalIndex;

		/** Basic constructor to link a ref to an existing interval */
		IntervalRef(int intervalIndex, int shift) {
			this.intervalIndex = intervalIndex;
			this.shift = shift;
		}
		/** Extension constructor to bypass chains of interval refs */
		public IntervalRef(IntervalRef ref, int shift) {
			this.intervalIndex = ref.intervalIndex;
			this.shift = shift+ref.shift;
		}

		/** Looks up the original interval and updates own content from it by applying shift */
		void update(State state) {
			Interval source = state.intervals[intervalIndex];
			from = source.from+shift;
			to = source.to+shift;
		}
	}

	static class Cache {
		/**
		 * Paired booleans for each entry, leaving capacity for 512 entries by default.
		 * First value of each entry indicates whether it is actually set, second one
		 * stores the cached value.
		 */
		boolean[] data = new boolean[INITIAL_SIZE];

		void reset(int size) {
			int capacity = size<<1;
			if(capacity>=data.length) {
				data = new boolean[CollectionUtils.growSize(data.length, capacity)];
			} else {
				Arrays.fill(data, 0, capacity, false);
			}
		}

		@VisibleForTesting
		int size() {
			return data.length>>1;
		}

		boolean isSet(int index) {
			return data[index<<1];
		}

		boolean getValue(int index) {
			return data[(index<<1)+1];
		}

		void setValue(int index, boolean value) {
			index <<= 1;
			if(data[index])
				throw new IllegalStateException("Slot already set: "+index);
			data[index] = true;
			data[index+1] = value;
		}
	}

	static class TreeInfo {
		/** Minimum number of elements to be matched by a subtree. */
		int minSize = 0;
		/** Maximum number of elements to be matched by a subtree. */
		int maxSize = 0;
		/** Flag to indicate whether {@link #maxSize} is actually valid. */
		boolean maxValid = true;
		/** Indicates that the state machine corresponding to a sub node is fully deterministic. */
		boolean deterministic = true;

		/** Contains pointers to raw intervals or interval refs. */
		int[] intervals;
		/** Hint on whether the 'intervals' ids are for actual raw intervals or interval refs. */
		boolean intervalRefs;
		/** Used to track the shift of intervals for preceding nodes */
		int offset = 0;

		void reset() {
			minSize = 0;
			maxSize = 0;
            maxValid = true;
            deterministic = true;
            intervals = null;
            intervalRefs = false;
            offset = 0;
		}
	}


	/** Implements a generic accept node that keeps track of the last matched index. */
	static class Node {
		Node next = accept;

		boolean match(State state, int pos) {
			state.last = pos;
			return true;
		}

		boolean study(TreeInfo info) {
			if(next!=null) {
				return next.study(info);
			}

			return info.deterministic;
		}

		boolean isProxy() {
			return false;
		}
	}

	private static boolean isDangling(Node n) {
		return n.next==accept || n.next==null;
	}

	private static <N extends Node> N detach(N node) {
		node.next = null;
		return node;
	}

	static final int GREEDY = QuantifierModifier.GREEDY.ordinal();
	static final int RELUCTANT = QuantifierModifier.RELUCTANT.ordinal();
	static final int POSSESSIVE = QuantifierModifier.POSSESSIVE.ordinal();

	/** Helper for "empty" nodes that are only existentially quantified. */
	static final class Empty extends Node {
		final int memberId;

		Empty(int memberId) { this.memberId = memberId; }

		@Override
		boolean match(State state, int pos) {
			// Ensure existence
			if(pos>state.to) {
				return false;
			}

			// Store member mapping so that other constraints can reference it
			if(memberId!=UNSET_INT) {
				state.members[memberId].assign(state.elements[pos]);
			}
			// Immediately forward to next node
			boolean result = next.match(state, pos+1);
			// Ensure we don't keep item references
			if(memberId!=UNSET_INT) {
				state.members[memberId].clear();
			}
			return result;
		}

		@Override
		boolean study(TreeInfo info) {
			next.study(info);
			info.minSize++;
			info.maxSize++;
			info.offset++;
			return info.deterministic;
		}
	}

	static abstract class ProperNode extends Node implements Comparable<ProperNode> {
		final int id;
		ProperNode(int id) { this.id = id; }
		@Override
		public int compareTo(ProperNode o) { return Integer.compare(id, o.id); }
		//TODO once the state machine is implemented, add monitoring methods
//		abstract void append(StringBuilder sb);
	}

	/** Intermediate helper to filter out target sequences that are too short */
	static final class Begin extends Node {
		int minSize;

		Begin() { /* no-op */ }

		@Override
		boolean match(State state, int pos) {
			if(state.size < minSize) {
				return false;
			}
			return next.match(state, pos);
		}

		@Override
		boolean study(TreeInfo info) {
			next.study(info);
			minSize = info.minSize;
			checkState("Minimum size of sequence must be greater than or equal 1", minSize>0);
			return info.deterministic;
		}
	}

	/**
	 * Implements a final accept node that verifies that all existentially
	 * quantified nodes have been matched and which records the entire match
	 * as a result.
	 */
	static final class Finish extends Node {
		final long limit;

		Finish(long limit) { this.limit = limit; }

		@Override
		boolean match(State state, int pos) {
			state.dispatchMatch();

			state.reported++;
			if(limit!=UNSET_LONG && state.reported>=limit) {
				state.finished = true;
			}

			return true;
		}
	}

	/** Proxy for evaluating the global constraints. */
	static final class GlobalConstraint extends ProperNode {

		GlobalConstraint(int id) {
			super(id);
		}

		@Override
		boolean match(State state, int pos) {
			//TODO do we need to anchor all member labels here?
			if(!state.globalConstraint.computeAsBoolean()) {
				return false;
			}
			return next.match(state, pos);
		}

		@Override
		boolean study(TreeInfo info) {
			info.deterministic = false;
			//TODO check the expression info from the global constraints
			return next.study(info);
		}
	}

	/** Interval filter based on raw intervals in the matcher */
	@Deprecated
	static final class Clip extends Node {
		final int intervalIndex;
		final int intervalCount;
		Clip(int intervalIndex, int intervalCount) {
			this.intervalIndex = intervalIndex;
			this.intervalCount = intervalCount;
		}

		@Override
		boolean isProxy() { return super.isProxy(); }

		@Override
		boolean match(State state, int pos) {
			final Interval[] intervals = state.intervals;
			final int fence = intervalIndex + intervalCount;

			boolean allowed = false;
			for (int i = intervalIndex; i < fence; i++) {
				if(intervals[i].contains(pos)) {
					allowed = true;
					break;
				}
			}

			if(!allowed) {
				return false;
			}

			return next.match(state, pos);
		}

		@Override
		boolean study(TreeInfo info) {
			next.study(info);

			int[] intervals = new int[intervalCount];
			for (int i = 0; i < intervals.length; i++) {
				intervals[i] = intervalIndex+i;
			}
			info.intervals = intervals;
			info.intervalRefs = false;

			return info.deterministic;
		}
	}

	static final class Negation extends ProperNode {
		final Node atom;

		public Negation(int id, Node atom) {
			super(id);
			this.atom = requireNonNull(atom);
		}

		@Override
		boolean match(State state, int pos) {
			if(atom.match(state, pos)) {
				return false;
			}

			return next.match(state, pos);
		}

		@Override
		boolean study(TreeInfo info) {
			next.study(info);
			info.deterministic = false;
			return false;
		}
	}

	/** Matches an inner constraint to a specific node, employing memoization. */
	static final class Single extends ProperNode {
		final int nodeId;
		final int cacheId;
		final int memberId;

		Single(int id, int nodeId, int cacheId, int memberId) {
			super(id);
			this.nodeId = nodeId;
			this.cacheId = cacheId;
			this.memberId = memberId;
		}

		@Override
		boolean match(State state, int pos) {
			if(pos>state.to) {
				return false;
			}

			final Cache cache = state.caches[cacheId];

			boolean value;

			if(cache.isSet(pos)) {
				value = cache.getValue(pos);
			} else {
				// Unknown index -> compute local constraints once and cache result
				final Matcher<Item> m = state.matchers[nodeId];
				value = m.matches(pos, state.elements[pos]);
				cache.setValue(pos, value);
			}

			if(value) {
				// Keep track of preliminary match
				state.map(nodeId, pos);

				// Store member mapping so that other constraints can reference it
				if(memberId!=UNSET_INT) {
					state.members[memberId].assign(state.elements[pos]);
				}

				// Continue down the path
				value = next.match(state, pos+1);

				// Ensure we don't keep item references
				if(memberId!=UNSET_INT) {
					state.members[memberId].clear();
				}

				if(value) {
					// Store last successful match
					state.hits[nodeId] = pos;
				}
			}

			return value;
		}

		@Override
		boolean study(TreeInfo info) {
			next.study(info);
			info.minSize++;
			info.maxSize++;
			info.offset++;
			return info.deterministic;
		}
	}

	/** Helper node to produce an intersection of intervals for a subsequent scan */
	static final class Intersection extends ProperNode {
		final int targetIndex; // marks the storage for the intersection
		final int sourceIndex;
		final int sourceCount; // must be 2 or more

		Intersection(int id, int targetIndex, int sourceIndex, int sourceCount) {
			super(id);
			this.targetIndex = targetIndex;
			this.sourceIndex = sourceIndex;
			this.sourceCount = sourceCount;
		}

		@Override
		boolean match(State state, int pos) {
			Interval[] intervals = state.intervals;

			Interval region = intervals[targetIndex];
			region.reset(intervals[sourceIndex]);

			for (int i = 1; i < sourceCount; i++) {
				region.intersect(intervals[sourceIndex+i]);
			}

			// We do no actual matching here, but merely prepare the stage for next node
			return next.match(state, pos);
		}
	}

	/**
	 * Implements the exhaustive exploration of remaining search space
	 * by iteratively scanning for matches of the current tail.
	 * This implementation honors marker intervals.
	 */
	static final class Scan extends ProperNode {
		int minSize = 1; // can't be less than 1 since at some point inside we need a proper node

		final int cacheId;
		final boolean forward;
		final int intervalIndex;

		Scan(int id, int cacheId, int intervalIndex, boolean forward) {
			super(id);
			this.cacheId = cacheId;
			this.intervalIndex = intervalIndex;
			this.forward = forward;
		}

		@Override
		boolean match(State state, int pos) {
			Interval region = intervalIndex==UNSET_INT ? state : state.intervals[intervalIndex];

			// Bail early if we can't accommodate 'pos'
			if(!region.contains(pos)) {
				return false;
			}

			if(cacheId!=UNSET_INT) {
				return matchForwardCached(state, pos, region.to);
			} else if(forward) {
				return matchForwardUncached(state, pos, region.to);
			}
			return matchBackwards(state, pos, region.to);
		}

		/**
		 * Implements the iterative scanning of all remaining possibilities
		 * for matches of the nested atom, honoring the default direction defined
		 * by the target sequence.
		 * This variant does not use memoization and is intended for top-level
		 * scanning when there is no benefit from caching as every node will
		 * only be visited once anyway.
		 */
		private boolean matchForwardUncached(State state, int pos, int to) {
			final int fence = to - minSize + 1;

			boolean result = false;

			for (int i = pos; i <= fence && !state.finished; i++) {
				int scope = state.scope();
				result |= next.match(state, i);
				state.reset(scope);
			}

			return result;
		}

		/**
		 * Implements the iterative scanning of all remaining possibilities
		 * for matches of the nested atom.
		 * We employ memoization here to prevent repeatedly checking false
		 * leads. If a node is marked as successful hit, we still need to
		 * continue down all further nodes in the state machine, but we can
		 * effectively rule out unsuccessful paths.
		 */
		private boolean matchForwardCached(State state, int pos, int to) {
			Cache cache = state.caches[cacheId];

			int fence = to - minSize + 1;

			boolean result = false;

			for (int i = pos; i <= fence && !state.finished; i++) {
				int scope = state.scope();
				boolean stored = cache.isSet(i);
				boolean matched;
				if(stored) {
					matched = cache.getValue(i);
					if(matched) {
						// Continue matching, but we know it will succeed
						next.match(state, i);
					} else {
						// We know this is a dead-end, so skip
						state.reset(scope);
						continue;
					}
				} else {
					// Previously unseen index, so explore and cache result
					matched = next.match(state, i);
					cache.setValue(i, matched);
				}

				result |= matched;

				state.reset(scope);
			}

			return result;
		}

		/**
		 * Implements the iterative scanning of all remaining possibilities
		 * for matches of the nested atom, using the reverse direction as
		 * defined by the target sequence.
		 * This variant does not use memoization and is intended for top-level
		 * scanning when there is no benefit from caching as every node will
		 * only be visited once anyway.
		 * The main purpose for this separate implementation is to handle the
		 * LAST query modifier.
		 */
		private boolean matchBackwards(State state, int pos, int to) {
			boolean result = false;

			for (int i = to - minSize + 1; i >= pos && !state.finished; i--) {
				int scope = state.scope();
				result |= next.match(state, i);
				state.reset(scope);
			}

			return result;
		}

		@Override
		boolean study(TreeInfo info) {
			next.study(info);
			minSize = info.minSize;

			checkState("Minimum size of nested atom must be greater than or equal 1", minSize>0);

			info.deterministic = false;
			info.offset = 0; // Scan effectively consumes a marker interval

			return false;
		}
	}

	/**
     * Guard node at the end of each branch to block the {@link #study(TreeInfo)}
     * chain but forward the {@link #match(State, int)} call to 'next'.
     */
    static final class BranchConn extends Node {
        BranchConn() { /* no-op */ }
        @Override
		boolean match(State state, int pos) {
            return next.match(state, pos);
        }
        @Override
		boolean study(TreeInfo info) {
            return info.deterministic;
        }
    }

    /**
     * Models multiple alternative paths. Can also be used to model
     * {@code 0..1} (greedy) and {@code 0..1?} (reluctant) ranged
     * quantifiers.
     */
    static final class Branch extends ProperNode {
    	final Node conn;
    	final Node[] atoms;

		Branch(int id, Node conn, Node...atoms) {
			super(id);
			checkArgument("Need at least 2 branch atoms", atoms.length>1);
			this.atoms = atoms;
			this.conn = conn;
		}

    	@Override
    	boolean match(State state, int pos) {
            for (int n = 0; n < atoms.length; n++) {
                if (atoms[n] == null) {
                	// zero-width path
                    if (conn.next.match(state, pos))
                        return true;
                } else if (atoms[n].match(state, pos)) {
                    return true;
                }
            }
            return false;
    	}

    	@Override
		boolean study(TreeInfo info) {
			int minL = info.minSize;
			int maxL = info.maxSize;
			boolean maxV = info.maxValid;

			info.reset();
			int minL2 = Integer.MAX_VALUE; // arbitrary large enough num
			int maxL2 = -1;

			for (int n = 0; n < atoms.length; n++) {
				if (atoms[n] != null) {
					atoms[n].study(info);
				}
				minL2 = Math.min(minL2, info.minSize);
				maxL2 = Math.max(maxL2, info.maxSize);
				maxV = (maxV & info.maxValid);
				info.reset();
			}

			minL += minL2;
			maxL += maxL2;

			conn.next.study(info);

			info.minSize += minL;
			info.maxSize += maxL;
			info.maxValid &= maxV;
			info.deterministic = false;

			return false;
		}
    }

    static final class Repetition extends ProperNode {
    	final int cmin;
    	final int cmax;
    	final Node atom;
    	final int type;
    	final int scopeBuf;
    	final int posBuf;

		Repetition(int id, Node atom, int cmin, int cmax, int type,
				int scopeBuf, int posBuf) {
			super(id);
			this.atom = atom;
			this.cmin = cmin;
			this.cmax = cmax;
			this.type = type;
			this.scopeBuf = scopeBuf;
			this.posBuf = posBuf;
		}

        @Override
		boolean match(State state, int pos) {
        	// Save state for entire match call
        	int scope = state.scope();

        	boolean matched = true;

        	// Try to match minimum number of repetitions
            int count;
            for (count = 0; count < cmin; count++) {
                if (!atom.match(state, pos)) {
    				matched = false;
    				break;
                }
                // Successful atom match -> move forward
                pos = state.last;
            }

            if(matched) {
	            if (type == GREEDY)
	            	matched = matchGreedy(state, pos, count);
	            else if (type == RELUCTANT)
	            	matched = matchReluctant(state, pos, count);
	            else
	            	matched = matchPossessive(state, pos, count);
            }

            // Roll back all our mappings if we failed
            if(!matched) {
            	state.reset(scope);
            }

            return matched;
        }

        /**
         * Greedy match. Collect as many atom matches as possible and
         * backtrack if the tail fails.
         *
         * @param count the index to start matching at
         * @param count the number of atoms that have matched already
         */
        boolean matchGreedy(State state, int pos, int count) {
			int backLimit = count;
			// Stores scope handles for reset when backing off
			int[] b_scope = state.buffers[scopeBuf];
			// Stores matcher positions for backing off
			int[] b_pos = state.buffers[posBuf];
			// We are greedy so match as many as we can
			while (count<cmax) {
				// Keep track of scope and position
				int scope = state.scope();
				b_pos[count] = pos;
				b_scope[count] = scope;
				// Try advancing
				if(!atom.match(state, pos)) {
					state.reset(scope);
					break;
				}
				 // Zero length match
				if (pos == state.last) {
					state.reset(scope);
					break;
				}
				// Move up index and number matched
				pos = state.last;
				count++;
			}

			// Options for 'atom' exhausted, now try matching our tail

			// Handle backing off if match fails
			while (count >= backLimit) {
				if (next.match(state, pos)) {
					return true;
				}
				// Need to backtrack one more step
				count--;
				pos = b_pos[count];
				state.reset(b_scope[count]);
			}
			// Could not find a match for next, so fail
			return false;
        }

        /**
         * Reluctant match. Minimum has been satisfied, so let's try not
         * to match any more atoms.
         * @param pos the index to start matching at
         * @param count the number of atoms that have matched already
         */
        boolean matchReluctant(State state, int pos, int count) {
			for (;;) {
                // Try finishing match without consuming any more
				int scope = state.scope();
				if (next.match(state, pos)) {
					return true;
				}
				state.reset(scope);
                // At the maximum, no match found
				if (count >= cmax) {
					return false;
				}
                // Okay, must try one more atom
				scope = state.scope();
				if (!atom.match(state, pos)) {
					state.reset(scope);
					return false;
				}
                // If we haven't moved forward then must break out
				// zero-width atom match
				if (pos == state.last) {
					state.reset(scope);
					return false;
				}
                // Move up index and number matched
				pos = state.last;
				count++;
            }
        }

        /**
         * Possessive match. Collect all atom matches and disregard tail.
         * @param pos the index to start matching at
         * @param count the number of atoms that have matched already
         */
        boolean matchPossessive(State state, int pos, int count) {
			for (; count < cmax;) {
				// Try as many elements as possible
				int scope = state.scope();
				if (!atom.match(state, pos)) {
					state.reset(scope);
					break;
				}
				// zero-width atom match
				if (pos == state.last) {
					state.reset(scope);
					break;
				}
                // Move up index and number matched
				pos = state.last;
				count++;
			}
			return next.match(state, pos);
        }
        @Override
		boolean study(TreeInfo info) {
            // Save original info
            int minL = info.minSize;
            int maxL = info.maxSize;
            boolean maxV = info.maxValid;
            boolean detm = info.deterministic;
            info.reset();

            atom.study(info);

            int temp = info.minSize * cmin + minL;
            if (temp < minL) {
                temp = 0xFFFFFFF; // arbitrary large number
            }
            info.minSize = temp;

            if (maxV & info.maxValid) {
                temp = info.maxSize * cmax + maxL;
                info.maxSize = temp;
                if (temp < maxL) {
                    info.maxValid = false;
                }
            } else {
                info.maxValid = false;
            }

            if (info.deterministic && cmin == cmax)
                info.deterministic = detm;
            else
                info.deterministic = false;
            return next.study(info);
        }
    }
}
