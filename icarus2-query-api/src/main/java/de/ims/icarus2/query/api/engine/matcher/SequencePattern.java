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
 *
 * </code></pre>
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

	/** The IQL source of structural constraints for this matcher. */
	private final IqlElement source;
	/** Stores all the raw node definitions from the query extracted from {@link #source} */
	private final IqlNode[] nodes;
	/** Defines the general direction for matching. ANY is equivalent to FIRST here. */
	private final QueryModifier modifier;
	/** Maximum number of reported to report per container. Either -1 or a positive value. */
	private final long limit;
	/** THe root context for evaluations in this pattern */
	private final EvaluationContext context;
	/** Constraint from the 'FILTER BY' section */
	private final FilterDef filterConstraint;
	/** COnstraint from the 'HAVING' section */
	private final ExpressionDef globalConstraint;


	/** Dummy node usable as tail if query is simple */
	static Node accept = new Node();

	/** Entry point to the object graph of the state machine */
	private final Node root;
	/** Total number of caches needed for this pattern */
	private final int cacheCount;
	/** Total number of managed {@link Interval} instances */
	private final int intervalCount;
	/** Total number of int[] buffers needed by nodes */
	private final int bufferCount;
	/** Original referential intervals */
	private final IntervalRef[] intervalRefs;
	/** Keeps track of all the proper nodes. Used for monitoring */
	private final ProperNode[] properNodes;
	/** Lists all the markers used by original nodes */
	private final RangeMarker[] markers;
	/** Blueprints for creating new {@link NodeMatcher} instances per thread */
	private final List<NodeDef> nodeDefs;

	private final AtomicInteger matcherIdGen = new AtomicInteger(0);

	private SequencePattern(Builder builder) {
		modifier = builder.getModifier();
		limit = builder.getLimit();
		source = builder.getRoot();
		context = builder.geContext();

		SequenceProcessor proc = new SequenceProcessor(context, builder.geExpressionFactory());
		proc.process(source, builder.getFilterConstraint(), builder.getGlobalConstraint());

		root = proc.root;
		filterConstraint = proc.filter;
		globalConstraint = proc.global;
		nodes = proc.nodes.toArray(new IqlNode[0]);
		properNodes = proc.properNodes.stream().sorted().toArray(ProperNode[]::new);
		cacheCount = proc.cacheCount;
		intervalCount = proc.intervalCount;
		bufferCount = proc.bufferCount;
		intervalRefs = proc.intervalRefs.toArray(new IntervalRef[0]);
		markers = proc.markers.toArray(new RangeMarker[0]);
		nodeDefs = proc.nodeDefs;

		//TODO
	}

	/** Utility class for generating the state machine */
	static class SequenceProcessor {
		Node root;
		Node tail = accept;
		int cacheCount;
		int intervalCount;
		int bufferCount;
		FilterDef filter;
		ExpressionDef global;

		final EvaluationContext rootContext;
		final ExpressionFactory expressionFactory;
		final List<IqlNode> nodes = new ArrayList<>();
		final List<IntervalRef> intervalRefs = new ArrayList<>();
		final List<NodeDef> nodeDefs = new ArrayList<>();
		final Set<ProperNode> properNodes = new ReferenceOpenHashSet<>();
		final List<RangeMarker> markers = new ArrayList<>();

		SequenceProcessor(EvaluationContext rootContext, ExpressionFactory expressionFactory) {
			this.rootContext = requireNonNull(rootContext);
			this.expressionFactory = requireNonNull(expressionFactory);
		}

		void process(IqlElement root, IqlConstraint filterConstraint,
				IqlConstraint globalConstraint) {

		}
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

		return new SequenceMatcher(this, id);
	}

	private static Expression<?> instantiate(ExpressionDef def) {
		return def==null ? null : def.constraint();
	}

	private static ContainerMatcher instantiate(FilterDef def) {
		return def==null ? null : def.matcher();
	}

	/**
	 * Public entry point for sequence matching and holder of the
	 * state during a matching operation.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static class SequenceMatcher extends AbstractMatcher<Container> {

		private static final int INITIAL_SIZE = 1<<10;

		//TODO have pattern record the thread we used to create this matcher and verify when matching?

		/** Last allowed index to match. Typically equal to {@code size - 1}. */
		private int to = UNSET_INT;
		/** Total number of elements in the target sequence. */
		private int size = UNSET_INT;
		/** Set by the Finish node if a result limit exists and we already found enough matches. */
		private boolean finished;

		/** Target sequence to be searched */
		private Container sequence;
		/** Items in target container, copied for faster access */
		private Item[] items = new Item[INITIAL_SIZE];
		/** Index of the container being matched */
		private long index = UNSET_LONG;

		/** Total number of reported reported so far */
		private long reported = 0;


		/** Number of mappings stored so far and also the next insertion index */
		private int entry = 0;

		/** Keys for the node mapping */
		private int[] m_node = new int[INITIAL_SIZE];
		/** Values for the node mapping, i.e. the associated indices */
		private int[] m_pos = new int[INITIAL_SIZE];

		/**
		 * End index of the last (sub)match, used by repetitions to keep track.
		 * Initially {@code 0}, turned to {@code -1} for failed matches and to
		 * the next index to be visited when a match occurred.
		 */
		private int last = 0;

		/** The source of this matcher */
		private final SequencePattern pattern;
		/** Raw nodes from the query */
		private final IqlNode[] nodes;
		/** All the atomic nodes defined in the query */
		private final NodeMatcher[] matchers;
		/** Caches used by various nodes */
		private final Cache[] caches;
		/** Raw position intervals used by nodes */
		private final Interval[] intervals;
		/** Referential intervals used by nodes to delegate positional information*/
		private final IntervalRef[] intervalRefs;
		/** All the raw markers that  */
		private final RangeMarker[] markers;
		/** Keeps track of the last hit index for every raw node */
		private final int[] hits;
		/** The available int[] buffers used by various node implementations */
		private final int[][] buffers;

		private final ContainerMatcher filterConstraint;
		private final Expression<?> globalConstraint;

		//TODO add

		private SequenceMatcher(SequencePattern pattern, int id) {
			super(id);

			this.pattern = requireNonNull(pattern);

			this.markers = pattern.markers;
			this.nodes = pattern.nodes;
			this.hits = new int[nodes.length];

			this.filterConstraint = instantiate(pattern.filterConstraint);
			this.globalConstraint = instantiate(pattern.globalConstraint);

			this.matchers = pattern.nodeDefs.stream()
					.map(NodeDef::matcher)
					.toArray(NodeMatcher[]::new);
			this.caches = IntStream.range(0, pattern.cacheCount)
					.mapToObj(i -> new Cache())
					.toArray(Cache[]::new);
			this.intervals = IntStream.range(0, pattern.intervalCount)
					.mapToObj(i -> new Interval())
					.toArray(Interval[]::new);
			this.intervalRefs = Stream.of(pattern.intervalRefs)
					.map(IntervalRef::clone)
					.toArray(IntervalRef[]::new);
			this.buffers = IntStream.range(0, pattern.bufferCount)
					.mapToObj(i -> new int[INITIAL_SIZE])
					.toArray(int[][]::new);
		}

		/**
		 * @see de.ims.icarus2.query.api.engine.matcher.Matcher#matches(long, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public boolean matches(long index, Container target) {

			// Apply pre-filtering if available to reduce matcher overhead
			if(filterConstraint!=null && !filterConstraint.matches(index, target)) {
				return false;
			}

			this.index = index;
			this.sequence = target;

			int size = strictToInt(target.getItemCount());
			// If new size exceeds buffer, grow all storages
			if(size>=items.length) {
				int newSize = CollectionUtils.growSize(items.length, size);
				items = new Item[newSize];
				m_node = new int[newSize];
				m_pos = new int[newSize];
				for (int i = 0; i < buffers.length; i++) {
					buffers[i] = new int[newSize];
				}
			}
			// Now copy container content into our buffer
			for (int i = 0; i < size; i++) {
				items[i] = target.getItemAt(i);
			}
			this.size = size;

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
			boolean matched = pattern.root.match(this, 0);
			// From here on down all hits have already been reported

			// Global constraints have already been taken into account at this point

			// Cleanup duty -> we must erase all references to target and its elements
			Arrays.fill(items, 0, size, null);
			sequence = null;
			for (int i = 0; i < matchers.length; i++) {
				matchers[i].reset();
			}
			Arrays.fill(hits, UNSET_INT);
			//TODO should we cleanup the node map as well?
			entry = 0;

			return matched;
		}

		int scope() {
			return size;
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

		void dispatch() {
			//TODO create immutable and serializable object from current state and send it to subscriber
			//TODO increment reported counter upon dispatching
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

		private Builder() { /* no-op */ };

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

	static class ExpressionDef {
		final Expression<?> constraint;
		final EvaluationContext context;

		public ExpressionDef(Expression<?> constraint, EvaluationContext context) {
			this.constraint = requireNonNull(constraint);
			this.context = requireNonNull(context);
		}

		Expression<?> constraint() {
			return context.duplicate(constraint);
		}
	}

	/**
	 * Buffer for all the information needed to create a {@link NodeMatcher}
	 * for a new {@link SequenceMatcher} instance.
	 */
	static class NodeDef extends ExpressionDef {
		final Assignable<? extends Item> element;
		final int id;

		public NodeDef(int id, Assignable<? extends Item> element, Expression<?> constraint,
				EvaluationContext context) {
			super(constraint, context);
			this.id = id;
			this.element = requireNonNull(element);
		}

		//TODO relies on the internal threadlocal instances of the context (rly a good idea?)
		NodeMatcher matcher() {
			Assignable<? extends Item> element = context.duplicate(this.element);
			Expression<?> constraint = constraint();
			return new NodeMatcher(id, element, constraint);
		}
	}

	/**
	 * Buffer for all the information needed to create a {@link ContainerMatcher}
	 * for a new {@link SequenceMatcher} instance.
	 */
	static class FilterDef extends ExpressionDef {
		final Assignable<? extends Container> element;

		public FilterDef(Assignable<? extends Container> lane, Expression<?> constraint,
				EvaluationContext context) {
			super(constraint, context);
			this.element = requireNonNull(lane);
		}

		//TODO relies on the internal threadlocal instances of the context (rly a good idea?)
		ContainerMatcher matcher() {
			Assignable<? extends Container> lane = context.duplicate(this.element);
			Expression<?> constraint = constraint();
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
		 * Paired booleans for each entry, leaving capacity for 256 entries by default.
		 * First value of each entry indicates whether it is actually set, second one
		 * stores the cached value.
		 */
		boolean[] data = new boolean[512];

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

		boolean match(SequenceMatcher matcher, int pos) {
			matcher.last = pos;
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

	/**
	 * Implements a final accept node that verifies that all existentially
	 * quantified nodes have been matched and which records the entire match
	 * as a result.
	 */
	static final class Finish extends Node {
		@Override
		boolean match(SequenceMatcher matcher, int pos) {
			final SequencePattern pattern = matcher.pattern;

			matcher.dispatch();

			matcher.reported++;
			if(pattern.limit!=UNSET_LONG && matcher.reported>=pattern.limit) {
				matcher.finished = true;
			}

			return true;
		}
	}

	static final class GlobalConstraint extends ProperNode {

		GlobalConstraint(int id) {
			super(id);
		}

		@Override
		boolean match(SequenceMatcher matcher, int pos) {
			//TODO do we need to anchor all member labels here?
			if(!matcher.globalConstraint.computeAsBoolean()) {
				return false;
			}
			return next.match(matcher, pos);
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
		boolean match(SequenceMatcher matcher, int pos) {
			final Interval[] intervals = matcher.intervals;
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

			return next.match(matcher, pos);
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
		boolean match(SequenceMatcher matcher, int pos) {
			final Cache cache = matcher.caches[cacheId];

			boolean value;

			if(cache.isSet(pos)) {
				value = cache.getValue(pos);
			} else {
				// Unknown index -> compute atom once and cache result
				final NodeMatcher m = matcher.matchers[nodeId];
				value = m.matches(pos, matcher.items[pos]);
				cache.setValue(pos, value);
			}

			if(value) {
				// Keep track of preliminary match
				matcher.map(nodeId, pos);

				value = next.match(matcher, pos+1);

				if(value) {
					// Store last successful match
					matcher.hits[nodeId] = pos;
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
		int minSize = 0;

		final int cacheId;
		final boolean forward;

		Scan(int id, int cacheId, boolean forward) {
			super(id);
			this.cacheId = cacheId;
			this.forward = forward;
		}

		@Override
		boolean match(SequenceMatcher matcher, int pos) {
			if(cacheId!=UNSET_INT) {
				return matchForwardCached(matcher, pos);
			} else if(forward) {
				return matchForwardUncached(matcher, pos);
			}
			return matchBackwards(matcher, pos);
		}

		/**
		 * Implements the iterative scanning of all remaining possibilities
		 * for matches of the nested atom, honoring the default direction defined
		 * by the target sequence.
		 * This variant does not use memoization and is intended for top-level
		 * scanning when there is no benefit from caching as every node will
		 * only be visited once anyway.
		 */
		private boolean matchForwardUncached(SequenceMatcher matcher, int pos) {
			final int fence = matcher.to - minSize;

			boolean result = false;

			for (int i = pos; i <= fence && !matcher.finished; i++) {
				int scope = matcher.scope();
				result |= next.match(matcher, i);
				matcher.reset(scope);
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
		private boolean matchForwardCached(SequenceMatcher matcher, int pos) {
			Cache cache = matcher.caches[cacheId];

			int fence = matcher.to - minSize;

			boolean result = false;

			for (int i = pos; i <= fence && !matcher.finished; i++) {
				int scope = matcher.scope();
				boolean stored = cache.isSet(i);
				boolean matched;
				if(stored) {
					matched = cache.getValue(i);
					if(matched) {
						// Continue matching, but we know it will succeed
						next.match(matcher, i);
					} else {
						// We know this is a dead-end, so skip
						matcher.reset(scope);
						continue;
					}
				} else {
					// Previously unseen index, so explore and cache result
					matched = next.match(matcher, i);
					cache.setValue(i, matched);
				}

				result |= matched;

				matcher.reset(scope);
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
		 */
		private boolean matchBackwards(SequenceMatcher matcher, int pos) {
			boolean result = false;

			for (int i = matcher.to - minSize; i >= pos && !matcher.finished; i++) {
				int scope = matcher.scope();
				result |= next.match(matcher, i);
				matcher.reset(scope);
			}

			return result;
		}

		@Override
		boolean study(TreeInfo info) {
			//TODO store 'info' state and merge atom and next info
			next.study(info);
			minSize = info.minSize;
			assert minSize>0 : "zero-width atom";

			info.deterministic = false;

			return false;
		}
	}

	/**
     * Guard node at the end of each branch to block the {@link #study(TreeInfo)}
     * chain but forward the {@link #match(SequenceMatcher, int)} call to 'next'.
     */
    static final class BranchConn extends Node {
        BranchConn() {}
        @Override
		boolean match(SequenceMatcher matcher, int pos) {
            return next.match(matcher, pos);
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
    	boolean match(SequenceMatcher matcher, int pos) {
            for (int n = 0; n < atoms.length; n++) {
                if (atoms[n] == null) {
                	// zero-width path
                    if (conn.next.match(matcher, pos))
                        return true;
                } else if (atoms[n].match(matcher, pos)) {
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
		boolean match(SequenceMatcher matcher, int pos) {
        	// Save state for entire match call
        	int scope = matcher.scope();

        	boolean matched = true;

        	// Try to match minimum number of repetitions
            int count;
            for (count = 0; count < cmin; count++) {
                if (!atom.match(matcher, pos)) {
    				matched = false;
    				break;
                }
                // Successful atom match -> move forward
                pos = matcher.last;
            }

            if(matched) {
	            if (type == GREEDY)
	            	matched = matchGreedy(matcher, pos, count);
	            else if (type == RELUCTANT)
	            	matched = matchReluctant(matcher, pos, count);
	            else
	            	matched = matchPossessive(matcher, pos, count);
            }

            // Rollback all our mappings
            if(!matched) {
            	matcher.reset(scope);
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
        boolean matchGreedy(SequenceMatcher matcher, int pos, int count) {
			int backLimit = count;
			// Stores scope handles for reset when backing off
			int[] b_scope = matcher.buffers[scopeBuf];
			// Stores matcher positions for backing off
			int[] b_pos = matcher.buffers[posBuf];
			// We are greedy so match as many as we can
			while (count<cmax) {
				// Keep track of scope and position
				int scope = matcher.scope();
				b_pos[count] = pos;
				b_scope[count] = scope;
				// Try advancing
				if(!atom.match(matcher, pos)) {
					matcher.reset(scope);
					break;
				}
				 // Zero length match
				if (pos == matcher.last) {
					matcher.reset(scope);
					break;
				}
				// Move up index and number matched
				pos = matcher.last;
				count++;
			}

			// Options for 'atom' exhausted, now try matching our tail

			// Handle backing off if match fails
			while (count >= backLimit) {
				if (next.match(matcher, pos)) {
					return true;
				}
				// Need to backtrack one more step
				count--;
				pos = b_pos[count];
				matcher.reset(b_scope[count]);
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
        boolean matchReluctant(SequenceMatcher matcher, int pos, int count) {
			for (;;) {
                // Try finishing match without consuming any more
				int scope = matcher.scope();
				if (next.match(matcher, pos)) {
					return true;
				}
				matcher.reset(scope);
                // At the maximum, no match found
				if (count >= cmax) {
					return false;
				}
                // Okay, must try one more atom
				scope = matcher.scope();
				if (!atom.match(matcher, pos)) {
					matcher.reset(scope);
					return false;
				}
                // If we haven't moved forward then must break out
				// zero-width atom match
				if (pos == matcher.last) {
					matcher.reset(scope);
					return false;
				}
                // Move up index and number matched
				pos = matcher.last;
				count++;
            }
        }

        /**
         * Possessive match. Collect all atom matches and disregard tail.
         * @param pos the index to start matching at
         * @param count the number of atoms that have matched already
         */
        boolean matchPossessive(SequenceMatcher matcher, int pos, int count) {
			for (; count < cmax; count++) {
				// Try as many elements as possible
				int scope = matcher.scope();
				if (!atom.match(matcher, pos)) {
					matcher.reset(scope);
					break;
				}
				// zero-width atom match
				if (pos == matcher.last) {
					matcher.reset(scope);
					break;
				}
                // Move up index and number matched
				pos = matcher.last;
				count++;
			}
			return next.match(matcher, pos);
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
