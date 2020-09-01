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
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.concurrent.NotThreadSafe;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.query.api.engine.matcher.mark.Interval;
import de.ims.icarus2.query.api.engine.matcher.mark.Marker.RangeMarker;
import de.ims.icarus2.query.api.exp.Assignable;
import de.ims.icarus2.query.api.exp.EvaluationContext;
import de.ims.icarus2.query.api.exp.Expression;
import de.ims.icarus2.query.api.exp.ExpressionFactory;
import de.ims.icarus2.query.api.iql.IqlConstraint;
import de.ims.icarus2.query.api.iql.IqlElement;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryModifier;
import de.ims.icarus2.query.api.iql.IqlQuantifier.QuantifierModifier;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.collections.CollectionUtils;
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
 * @param <C> Type of the surrounding context (typically the sequence itself)
 * @param <E> Type of individual elements matched by the state machine
 *
 * @author Markus Gärtner
 *
 */
//TODO add link for ICQL specification!!
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
	private final EvaluationContext context;
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
		/** Total number of managed {@link Interval} instances */
		int intervalCount = 0;
		/** Total number of int[] buffers needed by nodes */
		int bufferCount = 0;
		/** Original referential intervals */
		IntervalRef[] intervalRefs = {};
		/** Keeps track of all the proper nodes. Used for monitoring */
		ProperNode[] properNodes = {};
		/** Lists all the markers used by original nodes */
		RangeMarker[] markers = {};
		/** Blueprints for creating new {@link NodeMatcher} instances per thread */
		Supplier<Matcher<Item>>[] nodeDefs;

		// Access methods for the matcher/state
		Node getRoot() { return root; }
		RangeMarker[] getMarkers() { return markers; }
		IqlNode[] getNodes() { return nodes; }
		int[] getHits() { return new int[nodes.length]; }
		Matcher<Container> makeFilterConstraint() {
			return filterConstraint==null ? null : filterConstraint.get();
		}
		Expression<?> makeGlobalConstraint() {
			return globalConstraint==null ? null : globalConstraint.get();
		}
		Matcher<Item>[] makeNodeMatchers() {
			return Stream.of(nodeDefs)
				.map(Supplier::get)
				.toArray(Matcher[]::new);
		}
		Cache[] makeCaches() {
			return IntStream.range(0, cacheCount)
				.mapToObj(i -> new Cache())
				.toArray(Cache[]::new);
		}
		Interval[] makeIntervals() {
			return IntStream.range(0, intervalCount)
				.mapToObj(i -> Interval.blank())
				.toArray(Interval[]::new);
		}
		IntervalRef[] makeIntervalRefs() {
			return  Stream.of(intervalRefs)
				.map(IntervalRef::clone)
				.toArray(IntervalRef[]::new);
		}
		int[][] makeBuffer() {
			return IntStream.range(0, bufferCount)
				.mapToObj(i -> new int[INITIAL_SIZE])
				.toArray(int[][]::new);
		}
	}

	/** Utility class for generating the state machine */
	static class SequenceQueryProcessor {
		Node root;
		Node tail = accept;
		int cacheCount;
		int intervalCount;
		int bufferCount;
		Supplier<Matcher<Container>> filter;
		Supplier<Expression<?>> global;

		final EvaluationContext rootContext;
		final QueryModifier modifier;
		final ExpressionFactory expressionFactory;
		final List<IqlNode> nodes = new ArrayList<>();
		final List<IntervalRef> intervalRefs = new ArrayList<>();
		final List<NodeDef> nodeDefs = new ArrayList<>();
		final Set<ProperNode> properNodes = new ReferenceOpenHashSet<>();
		final List<RangeMarker> markers = new ArrayList<>();
		final long limit;
		final IqlElement rootElement;
		final IqlConstraint filterConstraint;
		final IqlConstraint globalConstraint;

		SequenceQueryProcessor(Builder builder) {
			rootContext = builder.geContext();
			expressionFactory = builder.geExpressionFactory();
			modifier = builder.getModifier();
			limit = builder.getLimit();
			rootElement = builder.getRoot();
			filterConstraint = builder.getFilterConstraint();
			globalConstraint = builder.getGlobalConstraint();
		}

		StateMachineSetup createStateMachine() {
			StateMachineSetup sm = new StateMachineSetup();
			//TODO implement the converter

			// Fill state machine
			sm.properNodes = properNodes.stream().sorted().toArray(ProperNode[]::new);
			sm.filterConstraint = filter;
			sm.globalConstraint = global;
			sm.nodes = nodes.toArray(new IqlNode[0]);
			sm.limit = this.limit;
			sm.root = this.root;
			sm.properNodes = properNodes.stream().sorted().toArray(ProperNode[]::new);
			sm.cacheCount = cacheCount;
			sm.intervalCount = intervalCount;
			sm.bufferCount = bufferCount;
			sm.intervalRefs = intervalRefs.toArray(new IntervalRef[0]);
			sm.markers = markers.toArray(new RangeMarker[0]);
			sm.nodeDefs = nodeDefs.toArray(new Supplier[0]);

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
	static class State {
		/** Items in target container, copied for faster access */
		Item[] elements;

		/** Raw nodes from the query, order matches the items in 'matchers' */
		final IqlNode[] nodes;
		/** All the atomic nodes defined in the query */
		final Matcher<Item>[] matchers;
		/** Caches used by various nodes */
		final Cache[] caches;
		/** Raw position intervals used by nodes */
		final Interval[] intervals;
		/** Referential intervals used by nodes to delegate positional information*/
		final IntervalRef[] intervalRefs;
		/** All the raw markers that  */
		final RangeMarker[] markers;
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

		/** Last allowed index to match. Typically equal to {@code size - 1}. */
		int to = UNSET_INT;
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

		State(StateMachineSetup stateMachineSetup) {
			this.elements = new Item[INITIAL_SIZE];
			this.markers = stateMachineSetup.getMarkers();
			this.nodes = stateMachineSetup.getNodes();
			this.hits = stateMachineSetup.getHits();

			this.filterConstraint = stateMachineSetup.makeFilterConstraint();
			this.globalConstraint = stateMachineSetup.makeGlobalConstraint();

			this.matchers = stateMachineSetup.makeNodeMatchers();
			this.caches = stateMachineSetup.makeCaches();
			this.intervals = stateMachineSetup.makeIntervals();
			this.intervalRefs = stateMachineSetup.makeIntervalRefs();
			this.buffers = stateMachineSetup.makeBuffer();
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

		void reset() {
			// Cleanup duty -> we must erase all references to target and its elements
			Arrays.fill(elements, 0, size, null);
			Arrays.fill(hits, UNSET_INT);
			entry = 0;
			last = 0;
			to = 0;
		}

		void dispatchMatch() {
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
			int intervalIndex = 0;
			for (int i = 0; i < markers.length; i++) {
				RangeMarker marker = markers[i];
				marker.adjust(intervals, intervalIndex, size);
				intervalIndex += marker.intervalCount();
			}

			// Update interval refs
			for (int i = 0; i < intervalRefs.length; i++) {
				intervalRefs[i].update(this);
			}

			// Let the state machine do its work
			boolean matched = root.match(this, 0);
			/*
			 * Stable predicates at this point:
			 *  - All hits have already been reported.
			 *  - Global constraints have already been taken into account.
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
		private EvaluationContext context;
		private ExpressionFactory expressionFactory;

		//TODO add fields for configuring the result buffer

		private Builder() { /* no-op */ }

		//TODO add all the setter methods

		public int getId() { return id; }

		public QueryModifier getModifier() { return modifier; }

		public long getLimit() { return limit; }

		public IqlElement getRoot() { return root; }

		public IqlConstraint getFilterConstraint() { return filterConstraint; }

		public IqlConstraint getGlobalConstraint() { return globalConstraint; }

		public EvaluationContext geContext() { return context; }

		public ExpressionFactory geExpressionFactory() { return expressionFactory; }

		@Override
		protected void validate() {
			super.validate();

			checkState("No root element defined", root!=null);
			checkState("Id not defined", id>=0);
			checkState("Context not defined", context!=null);
			checkState("Context is not a root context", context.isRoot());
			checkState("Expression factory not defined", expressionFactory!=null);
		}

		@Override
		protected SequencePattern create() { return new SequencePattern(this); }
	}

	//TODO add mechanics to properly collect results from multiple buffers
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

	/** Encapsulates information to instantiate a new {@link Expression}. */
	static class ExpressionDef implements Supplier<Expression<?>> {
		final Expression<?> constraint;
		final EvaluationContext context;

		public ExpressionDef(Expression<?> constraint, EvaluationContext context) {
			this.constraint = requireNonNull(constraint);
			this.context = requireNonNull(context);
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

		public NodeDef(int id, Assignable<? extends Item> element, Expression<?> constraint,
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

		public FilterDef(Assignable<? extends Container> lane, Expression<?> constraint,
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
	static final class IntervalRef extends Interval implements Cloneable {
		/** Expansion amount of the interval, typically a negative value */
		private final int shift;
		/** Pointer to the original interval to be used for shifting */
		private final int intervalIndex;

		/** Basic constructor to link a ref to an existing interval */
		public IntervalRef(int intervalIndex, int shift) {
			this.intervalIndex = intervalIndex;
			this.shift = shift;
		}
		/** Extension constructor to bypass chains of interval refs */
		public IntervalRef(IntervalRef ref, int shift) {
			this.intervalIndex = ref.intervalIndex;
			this.shift = shift+ref.shift;
		}

		/** Looks up the original interval and updates own content from it by applying shift */
		void update(SequenceMatcher matcher) {
			Interval source = matcher.intervals[intervalIndex];
			from = source.from+shift;
			to = source.to+shift;
		}

		/** Since this implementation only holds pointer values we can do shallow copy */
		@Override
		public IntervalRef clone() {
			try {
				return (IntervalRef) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new InternalError("Class is cloneable", e);
			}
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

		boolean isSet(int index) {
			return data[index<<1];
		}

		boolean getValue(int index) {
			return data[(index<<1)+1];
		}

		void setValue(int index, boolean value) {
			index <<= 1;
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

	static abstract class ProperNode extends Node implements Comparable<ProperNode> {
		final int id;
		ProperNode(int id) { this.id = id; }
		@Override
		public int compareTo(ProperNode o) { return Integer.compare(id, o.id); }
		//TODO once the state machine is implemented, add monitoring methods
//		abstract void append(StringBuilder sb);
	}

	//TODO make a Begin node class that only verifies the total sequence length against minSize of tree info

	/**
	 * Implements a final accept node that verifies that all existentially
	 * quantified nodes have been matched and which records the entire match
	 * as a result.
	 */
	static final class Finish extends Node {
		final long limit;

		public Finish(long limit) { this.limit = limit; }

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
	static final class Region extends Node {
		final int intervalIndex;
		final int intervalCount;
		Region(int intervalIndex, int intervalCount) {
			this.intervalIndex = intervalIndex;
			this.intervalCount = intervalCount;
		}

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

//	/** Interval filter based on raw intervals in the matcher */
//	static final class RegionRef extends Node {
//		final int intervalRefIndex;
//		final int intervalRefCount;
//		RegionRef(int intervalRefIndex, int intervalRefCount) {
//			this.intervalRefIndex = intervalRefIndex;
//			this.intervalRefCount = intervalRefCount;
//		}
//
//		@Override
//		boolean match(SequenceMatcher matcher, int j) {
//			final IntervalRef[] intervals = matcher.intervalRefs;
//			final int fence = intervalRefIndex + intervalRefCount;
//			boolean allowed = false;
//			for (int i = intervalRefIndex; i < fence; i++) {
//				if(intervals[i].contains(j)) {
//					allowed = true;
//					break;
//				}
//			}
//
//			if(!allowed) {
//				return false;
//			}
//
//			return next.match(matcher, j);
//		}
//
//		@Override
//		boolean study(TreeInfo info) {
//			next.study(info);
//
//			int[] intervals = new int[intervalRefCount];
//			for (int i = 0; i < intervals.length; i++) {
//				intervals[i] = intervalRefIndex+i;
//			}
//			info.intervals = intervals;
//			info.intervalRefs = true;
//
//			return info.deterministic;
//		}
//	}

	//	/** Interval filter based on raw intervals in the matcher */
	//	static final class RegionRef extends Node {
	//		final int intervalRefIndex;
	//		final int intervalRefCount;
	//		RegionRef(int intervalRefIndex, int intervalRefCount) {
	//			this.intervalRefIndex = intervalRefIndex;
	//			this.intervalRefCount = intervalRefCount;
	//		}
	//
	//		@Override
	//		boolean match(SequenceMatcher matcher, int j) {
	//			final IntervalRef[] intervals = matcher.intervalRefs;
	//			final int fence = intervalRefIndex + intervalRefCount;
	//			boolean allowed = false;
	//			for (int i = intervalRefIndex; i < fence; i++) {
	//				if(intervals[i].contains(j)) {
	//					allowed = true;
	//					break;
	//				}
	//			}
	//
	//			if(!allowed) {
	//				return false;
	//			}
	//
	//			return next.match(matcher, j);
	//		}
	//
	//		@Override
	//		boolean study(TreeInfo info) {
	//			next.study(info);
	//
	//			int[] intervals = new int[intervalRefCount];
	//			for (int i = 0; i < intervals.length; i++) {
	//				intervals[i] = intervalRefIndex+i;
	//			}
	//			info.intervals = intervals;
	//			info.intervalRefs = true;
	//
	//			return info.deterministic;
	//		}
	//	}

	/** Matches an inner constraint to a specific node, employing memoization. */
	static final class Single extends ProperNode {
		final int nodeId;
		final int cacheId;

		Single(int id, int nodeId, int cacheId) {
			super(id);
			this.nodeId = nodeId;
			this.cacheId = cacheId;
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

				value = next.match(state, pos+1);

				if(value) {
					// Store last successful match
					state.hits[nodeId] = pos;
				}
			}

			return value;
		}

		@Override
		boolean study(TreeInfo info) {
			info.minSize++;
			info.maxSize++;
			info.offset++;
			return next.study(info);
		}
	}

	/**
	 * Implements the exhaustive exploration of remaining search space
	 * by iteratively scanning for matches of the current tail.
	 */
	static final class Scan extends ProperNode {
		int minSize = 1; // can't be less than 1 since at some point inside we need a proper node

		//TODO add pointer to a single interval
		final int cacheId;
		final boolean forward;

		Scan(int id, int cacheId, boolean forward) {
			super(id);
			this.cacheId = cacheId;
			this.forward = forward;
		}

		@Override
		boolean match(State state, int pos) {
			//TODO add support for interval restriction in sub-match methods
			if(cacheId!=UNSET_INT) {
				return matchForwardCached(state, pos);
			} else if(forward) {
				return matchForwardUncached(state, pos);
			}
			return matchBackwards(state, pos);
		}

		/**
		 * Implements the iterative scanning of all remaining possibilities
		 * for matches of the nested atom, honoring the default direction defined
		 * by the target sequence.
		 * This variant does not use memoization and is intended for top-level
		 * scanning when there is no benefit from caching as every node will
		 * only be visited once anyway.
		 */
		private boolean matchForwardUncached(State state, int pos) {
			final int fence = state.to - minSize + 1;

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
		private boolean matchForwardCached(State state, int pos) {
			Cache cache = state.caches[cacheId];

			int fence = state.to - minSize + 1;

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
		private boolean matchBackwards(State state, int pos) {
			boolean result = false;

			for (int i = state.to - minSize + 1; i >= pos && !state.finished; i--) {
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

			assert minSize>0 : "zero-width atom";

			info.deterministic = false;

			return false;
		}
	}

	/**
     * Guard node at the end of each branch to block the {@link #study(TreeInfo)}
     * chain but forward the {@link #match(State, int)} call to 'next'.
     */
    static final class BranchConn extends Node {
        BranchConn() {}
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
			//TODO ensure that all the atoms use conn as next?
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
            int minL2 = Integer.MAX_VALUE; //arbitrary large enough num
            int maxL2 = -1;

            for (int n = 0; n < atoms.length; n++) {
                if (atoms[n] != null)
                    atoms[n].study(info);
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
