/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static de.ims.icarus2.util.lang.Primitives._boolean;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
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
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.NodeInfo.Field;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.NodeInfo.Type;
import de.ims.icarus2.query.api.engine.matcher.mark.HorizontalMarker;
import de.ims.icarus2.query.api.engine.matcher.mark.Interval;
import de.ims.icarus2.query.api.engine.matcher.mark.Marker.RangeMarker;
import de.ims.icarus2.query.api.exp.Assignable;
import de.ims.icarus2.query.api.exp.EvaluationContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.ElementContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.LaneContext;
import de.ims.icarus2.query.api.exp.EvaluationUtils;
import de.ims.icarus2.query.api.exp.Expression;
import de.ims.icarus2.query.api.exp.ExpressionFactory;
import de.ims.icarus2.query.api.exp.Literals;
import de.ims.icarus2.query.api.exp.LogicalOperators;
import de.ims.icarus2.query.api.iql.AbstractIqlQueryElement;
import de.ims.icarus2.query.api.iql.IqlConstraint;
import de.ims.icarus2.query.api.iql.IqlConstraint.BooleanOperation;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlPredicate;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlTerm;
import de.ims.icarus2.query.api.iql.IqlElement;
import de.ims.icarus2.query.api.iql.IqlElement.IqlElementDisjunction;
import de.ims.icarus2.query.api.iql.IqlElement.IqlGrouping;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlElement.IqlSequence;
import de.ims.icarus2.query.api.iql.IqlElement.IqlTreeNode;
import de.ims.icarus2.query.api.iql.IqlExpression;
import de.ims.icarus2.query.api.iql.IqlMarker;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerCall;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerExpression;
import de.ims.icarus2.query.api.iql.IqlMarker.MarkerExpressionType;
import de.ims.icarus2.query.api.iql.IqlPayload.MatchFlag;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryModifier;
import de.ims.icarus2.query.api.iql.IqlQuantifier;
import de.ims.icarus2.query.api.iql.IqlQuantifier.QuantifierModifier;
import de.ims.icarus2.query.api.iql.IqlQueryElement;
import de.ims.icarus2.query.api.iql.IqlType;
import de.ims.icarus2.query.api.iql.IqlUtils;
import de.ims.icarus2.query.api.iql.NodeArrangement;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.collections.ArrayUtils;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.strings.ToStringBuilder;
import de.ims.icarus2.util.tree.IntTree;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

/**
 * Implements the state machine to match a node sequence defined in the
 * IQL protocol.
 * <p>
 * This implementation splits the actual state machine and the state buffer
 * for matching into different classes/objects: An instance of this class
 * only holds the actual state machine with all its nodes and transition logic.
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
	static Node accept = new Node(-2) {
		@Override
		public String toString() { return "Accept-Dummy"; }

		@Override
		void setNext(Node next) { throw new UnsupportedOperationException("Generic accept node is immutable"); }

		@Override
		boolean isFinisher() { return true; }

		@Override
		public NodeInfo info() {return null; }
	};

	private final AtomicInteger matcherIdGen = new AtomicInteger(0);

	/** The IQL source of structural constraints for this matcher. */
	private final IqlQueryElement source;
	/** Defines the general direction for matching. ANY is equivalent to FIRST here. */
	private final QueryModifier modifier;
	/** Additional flags controlling search aspects. */
	private final Set<MatchFlag> flags;
	/** The root context for evaluations in this pattern */
	private final LaneContext context;
	/** Blueprint for instantiating a new {@link SequenceMatcher} */
	private final StateMachineSetup setup;

	private SequencePattern(Builder builder) {
		modifier = builder.getModifier();
		flags = builder.geFlags();
		source = builder.getRoot();
		context = builder.geContext();

		SequenceQueryProcessor proc = new SequenceQueryProcessor(builder);
		setup = proc.createStateMachine();

		setup.initialSize = builder.getInitialBufferSize();

		//TODO
	}

	public IqlQueryElement getSource() { return source; }

	/**
	 * Crates a new matcher that uses this state machine and that can be safely used from
	 * within a single thread.
	 * <p>
	 * Important Note:<br>
	 * This method <b>MUST</b> be called from the thread that is intended to
	 * be used for the actual matching, as the underlying {@link EvaluationContext}
	 * instances use {@link ThreadLocal} to fetch expressions and assignables.
	 */
	//TODO add arguments to delegate result buffer, dispatch output and attach monitoring
	public SequenceMatcher matcher() {
		final int id = matcherIdGen.getAndIncrement();

		return new SequenceMatcher(setup, id);
	}

	public NodeInfo[] info() {
		return Stream.of(setup.getNodes())
				.map(Node::info)
				.filter(IcarusUtils.NOT_NULL())
				.toArray(NodeInfo[]::new);
	}

	@VisibleForTesting
	NonResettingMatcher matcherForTesting() {
		int id = matcherIdGen.getAndIncrement();
		return new NonResettingMatcher(setup, id);
	}

	@VisibleForTesting
	static final int INITIAL_SIZE = 1<<10;

	/**
	 * Encapsulates all the information needed to instantiate a matcher for
	 * the sequence matching state machine.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static class StateMachineSetup {

		/** Hint for the starting size of all buffer structures */
		int initialSize = INITIAL_SIZE;

		/** Stores all the raw node definitions from the query extracted from {@link #source} */
		IqlNode[] rawNodes = {};
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
		/** Total number of border savepoints */
		int borderCount = 0;
		/** Total number of gates that prevent duplicate position matching */
		int gateCount = 0;
		/** Total number of tree node anchors */
		int anchorCount = 0;
		/** Total number of closure buffers */
		int closureCount = 0;
		/** Total number of nodes that affect the skip flag */
		int skipControlCount;
		/** Size info of all the permutations */
		int[] permutations = {};
		/** Original referential intervals */
		Interval[] intervals = {};
		/** Keeps track of all the tracked nodes. Used for monitoring */
		Node[] trackedNodes = {};
		/** All the nodes in this state machine */
		Node[] nodes = {};
		/** Lists all the markers operating on the root frame */
		RangeMarker[] globalMarkers = {};
		/** Lists all the markers operating on nested frames */
		RangeMarker[] nestedMarkers = {};
		/** Blueprints for creating new {@link NodeMatcher} instances per thread */
		@SuppressWarnings("unchecked")
		Supplier<Matcher<Item>>[] matchers = new Supplier[0];
		/** Blueprints for creating member storages per thread */
		@SuppressWarnings("unchecked")
		Supplier<Assignable<? extends Item>>[] members = new Supplier[0];


		// Access methods for the matcher/state
		Node getRoot() { return root; }
		Node[] getNodes() { return nodes; }
		RangeMarker[] getGlobalMarkers() { return globalMarkers; }
		RangeMarker[] getNestedMarkers() { return nestedMarkers; }
		IqlNode[] getRawNodes() { return rawNodes; }
		int[] getHits() { return new int[rawNodes.length]; }
		int[] getBorders() { return new int[borderCount]; }
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
		Cache[] makeCaches(int initalSize) {
			return IntStream.range(0, cacheCount)
				.mapToObj(i -> new Cache(initalSize))
				.toArray(Cache[]::new);
		}
		Interval[] makeIntervals() {
			return  Stream.of(intervals)
					.map(Interval::clone)
					.toArray(Interval[]::new);
		}
		int[][] makeBuffer(int initalSize) {
			return IntStream.range(0, bufferCount)
				.mapToObj(i -> new int[initalSize])
				.toArray(int[][]::new);
		}
		Gate[] makeGates(int initialSize) {
			return IntStream.range(0, gateCount)
				.mapToObj(i -> new Gate(initialSize))
				.toArray(Gate[]::new);
		}
		Anchor[] makeAnchors() {
			return IntStream.range(0, anchorCount)
					.mapToObj(i -> new Anchor())
					.toArray(Anchor[]::new);
		}
		ClosureContext[] makeClosures(int initialSize) {
			return IntStream.range(0, closureCount)
					.mapToObj(i -> new ClosureContext(initialSize))
					.toArray(ClosureContext[]::new);
		}
		PermutationContext[] makePermutations() {
			return IntStream.range(0, permutations.length)
				.mapToObj(i -> new PermutationContext(permutations[i]))
				.toArray(PermutationContext[]::new);
		}
	}

	static final class IqlListProxy extends AbstractIqlQueryElement {

		private final List<? extends IqlQueryElement> items;

		public IqlListProxy(List<? extends IqlQueryElement> items) {
			this.items = ObjectLists.unmodifiable(new ObjectArrayList<>(items));
		}

		public List<? extends IqlQueryElement> getItems() { return items; }

		@Override
		public IqlType getType() { return IqlType.DUMMY; }

		@Override
		public void checkIntegrity() {
			super.checkIntegrity();
			checkCollection(items);
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

		int cacheCount;
		int bufferCount;
		int borderCount;
		int gateCount;
		int skipControlCount;
		int anchorCount;
		IntList permutators = new IntArrayList();
		Supplier<Matcher<Container>> filter;
		Supplier<Expression<?>> global;
		ElementContext context;
		ExpressionFactory expressionFactory;

		boolean findOnly = false;

		int id;

		int level = 0;

		final List<Node> nodes = new ArrayList<>();
		final LaneContext rootContext;
		final QueryModifier modifier;
		final List<IqlNode> rawNodes = new ArrayList<>();
		final List<Interval> intervals = new ArrayList<>();
		final List<NodeDef> matchers = new ArrayList<>();
		final List<MemberDef> members = new ArrayList<>();
		final List<Node> trackedNodes = new ArrayList<>();
		final List<RangeMarker> globalMarkers = new ArrayList<>();
		final List<RangeMarker> nestedMarkers = new ArrayList<>();
		final long limit;
		final IqlElement rootElement;
		final IqlConstraint filterConstraint;
		final IqlConstraint globalConstraint;
		final Set<MatchFlag> flags;

		final boolean monitor;
		final Function<IqlNode, IqlNode> nodeTransform;
		final boolean cacheAll;

		private static enum Flag {
			/** Signals that the segment is accompanied by a disjunctive marker
			 * or a marker that produces more than a single interval,
			 * requiring scans to be moved outside the prefix section. */
			COMPLEX_MARKER,

			@Deprecated
			SIMPLE_MARKER,
			;
		}

		/**
		 * Utility class for building the state machine.
		 * Essentially wraps around a sequence of {@link Node} instances
		 * and keeps track of the number of virtual and non-virtual nodes
		 * as well as holding special flags to inform processing of
		 * higher elements in the query.
		 *
		 * @author Markus Gärtner
		 *
		 */
		private static class Segment {

			/** Begin of the state machine section for this frame. {@link SequencePattern#accept} by default. */
			private Node start = accept;
			/** End  of the state machine section for this frame. {@link SequencePattern#accept} by default. */
			private Node end = accept;
			/** Number of non-virtual nodes that directly match elements in the target structure. */
			private int nodes = 0;
			/** Total number of _any_ kind of nodes in this segment */
			private int size = 0;
			/** Optional flags to control state machine construction */
			private Set<Flag> flags;

			void checkNotEmpty() {
				checkState("Frame empty", !isEmpty());
			}

			Node start() { checkNotEmpty(); return start; }
			Node end() { checkNotEmpty(); return end; }
			int nodes() { return nodes; }
			boolean isEmpty() { return start==accept; }
			boolean isSingleton() { return nodes==1; }

			int size() { return size; }

			boolean isFlagSet(Flag flag) { return flags!=null && flags.contains(flag); }
			void setFlag(Flag flag) {
				if(flags==null) {
					flags = EnumSet.of(flag);
				} else {
					flags.add(flag);
				}
			}

			private void mergeFlags(Segment other) {
				if(other.flags!=null) {
					if(flags==null) {
						flags = EnumSet.noneOf(Flag.class);
					}
					flags.addAll(other.flags);
				}
			}

			private void maybeIncNodes(Node n) {
				n = unwrap(n);
				if(n instanceof Single || n instanceof Empty) {
					nodes++;
				}
			}

			void incNodes(int n) { nodes += n; }

			/** Add given node as head of inner sequence. */
			<N extends Node> N push(N node) {
				requireNonNull(node);
				maybeIncNodes(node);
				size += length(node);
				if(end==accept) {
					start = node;
					end = last(node);
				} else {
					last(node).setNext(start);
					start = node;
				}
				return node;
			}

			/** Add given node as tail of inner sequence. */
			<N extends Node> N append(N node) {
				requireNonNull(node);
				maybeIncNodes(node);
				size += length(node);
				if(end==accept) {
					start = node;
					end = last(node);
				} else {
					end.setNext(node);
					end = last(node);
				}
				return node;
			}

			void replace(Segment other) {
				if(other==this) {
					return;
				}
				start = other.start;
				end = other.end;
				nodes = other.nodes;
				flags = null;
				mergeFlags(other);
				size = other.size;
			}

			/**
			 * Add other segment as head to this one (if this is empty, replace all content instead).
			 */
			void push(Segment other) {
				requireNonNull(other);
				if(isEmpty()) {
					replace(other);
				} else {
					other.end().setNext(start);
					start = other.start();
					nodes += other.nodes();
					size += other.size();
					mergeFlags(other);
				}
			}

			/**
			 * Add other segment as tail to this one (if this is empty, replace all content instead).
			 */
			void append(Segment other) {
				requireNonNull(other);
				if(isEmpty()) {
					replace(other);
				} else {
					end.setNext(other.start());
					end = other.end();
					nodes += other.nodes();
					size += other.size();
					mergeFlags(other);
				}
			}

			Frame toFrame() {
				Frame frame = new Frame();
				frame.replace(this);
				return frame;
			}
		}

		/**
		 * Advanced utility class for constructing the state machine.
		 * Adds affix support to the basic {@link Segment} class.
		 * Affixes
		 *
		 * @author Markus Gärtner
		 *
		 */
		private static class Frame extends Segment {
			/** Designates the context as being an atom for inclusion in an outer frame. */
//			private final boolean atom;
			/** Suffix structure to be hoisted into surrounding frame. */
			private Segment prefix = null;
			/** Prefix structure to be hoisted into surrounding frame. */
			private Segment suffix = null;

			Segment suffix() { if(suffix==null) suffix = new Segment(); return suffix; }
			Segment prefix() { if(prefix==null) prefix = new Segment(); return prefix; }
			boolean hasSuffix() { return suffix!=null; }
			boolean hasPrefix() { return prefix!=null; }
			boolean hasAffix() { return hasPrefix() || hasSuffix(); }

			/** Prepends the prefix if present and appends the suffix if present. */
			Frame collapse() {
				checkNotEmpty();
				collapsePrefix();
				collapseSuffix();
				return this;
			}

			Frame collapsePrefix() {
				if(prefix!=null && !prefix.isEmpty()) {
					push(prefix);
				}
				prefix = null;
				return this;
			}

			Frame collapseSuffix() {
				if(suffix!=null && !suffix.isEmpty()) {
					append(suffix);
				}
				suffix = null;
				return this;
			}

			private void replace(Frame other) {
				if(other==this) {
					return;
				}
				super.replace(other);
				prefix = other.prefix;
				suffix = other.suffix;
			}

			Segment clearPrefix() { Segment s = prefix; prefix = null; return s; }
			Segment clearSuffix() { Segment s = suffix; suffix = null; return s; }

			/**
			 * Add other frame as head to this one (if this is empty, replace all content instead):
			 * <p>
			 * Append other's prefix if present.
			 * Push other's suffix if present and this is empty.
			 * Push other's content.
			 */
			@Deprecated
			void push(Frame other) {
				requireNonNull(other);
				if(isEmpty()) {
					replace(other);
					return;
				}

				if(other.hasPrefix()) {
					checkState("Prefix already set - no clear merge rule available", !hasPrefix());
					prefix().push(other.prefix());
				}
				if(other.hasSuffix()) {
					push(other.suffix());
				}

				super.push(other);
			}

			/**
			 * Add other frame as tail to this one (if this is empty, replace all content instead):
			 * <p>
			 * Collapse suffix if present.
			 * Collapse other's prefix.
			 * Push other's content.
			 * Replace suffix with other's suffix if present.
			 */
			void append(Frame other) {
				requireNonNull(other);
				if(isEmpty()) {
					replace(other);
					return;
				}

				collapseSuffix();
				other.collapsePrefix();
				super.append(other);
				suffix = other.suffix();
			}
		}

		SequenceQueryProcessor(Builder builder) {
			rootContext = builder.geContext();
			modifier = builder.getModifier();
			flags = builder.geFlags();
			limit = builder.getLimit();
			rootElement = builder.getRoot();
			filterConstraint = builder.getFilterConstraint();
			globalConstraint = builder.getGlobalConstraint();

			monitor = builder.isAllowMonitor();
			nodeTransform = builder.getNodeTransform();
			cacheAll = builder.isCacheAll();
		}

		/** Check whether given flag is set in current query context. */
		private boolean isFlagSet(MatchFlag flag) { return flags.contains(flag); }

		/** Process element as independent frame */
		private Frame process(IqlElement source, @Nullable Node scan) {
			switch (source.getType()) {
			case GROUPING: return grouping((IqlGrouping) source, scan);
			case SEQUENCE: return sequence((IqlSequence) source, scan);
			case NODE: return node((IqlNode) source, scan, false);
			case TREE_NODE: return node((IqlNode) source, scan, true);
			// Only disjunction inherits the 'adjacency' property from surrounding context
			case DISJUNCTION: return disjunction((IqlElementDisjunction) source, scan);

			default:
				// We do not support IqlEdge here!!
				throw EvaluationUtils.forUnsupportedQueryFragment("element", source.getType());
			}
		}

		// RAW ELEMENT PROCESSING

		//FIXME currently we might distort the legal intervals when shifting them to the left

		/** Process single node */
		private Frame node(IqlNode source, @Nullable Node scan, boolean isTree) {

			if(nodeTransform!=null) {
				source = requireNonNull(nodeTransform.apply(source), "Node transformation fail");
			}

			final List<IqlQuantifier> quantifiers = source.getQuantifiers();
			final IqlMarker marker = source.getMarker().orElse(null);
			final IqlConstraint constraint = source.getConstraint().orElse(null);
			final String label = source.getLabel().orElse(null);
			final int anchorId = isTree && ((IqlTreeNode)source).getChildren().isPresent() ? anchor() : UNSET_INT;

			final Frame frame = new Frame();

			Segment atom;

			// Process actual node content
			if(constraint==null) {
				// Dummy nodes don't get added to the "proper nodes" list
				atom = segment(empty(source, label, anchorId));
			} else {
				// Full fledged node with local constraints and potentially a member label

				rawNodes.add(source);
				// Prepare context and expression processing
				context = rootContext.derive()
						.element(source)
						.build();
				expressionFactory = new ExpressionFactory(context);
				atom = segment(single(source, label, constraint, anchorId));
				// Reset context
				expressionFactory = null;
				context = null;
			}

			// Handle quantifiers
			if(!quantifiers.isEmpty()) {
				atom = quantify(atom, quantifiers);
			}

			frame.push(atom);

			// Ignore outer scan if our atom is actually capable of scanning
			if(scan!=null && unwrap(frame.start()).isScanCapable()) {
				scan = null;
			}

			/*
			 * We don't handle markers immediately at the node position they
			 * appear, but on the first enclosing  explorative node, which
			 * currently are only the Exhaust, Find and PermSlot nodes.
			 * This way the state machine can properly and early reduce the
			 * search space for exploration. This also allows us to treat
			 * markers on multiple (nested) nodes in an ADJACENT sequence as erroneous.
			 */
			if(marker!=null) {
				int border = border();
				IntList nestedMarkerIndices = new IntArrayList();
				Segment filter = marker(marker, scan, IcarusUtils.DO_NOTHING(), nestedMarkerIndices::add);
				// Saves the previousIndex window
				frame.prefix().push(borderBegin(border, nestedMarkerIndices.toIntArray()));
				// Creates the marker window
				frame.prefix().append(filter);
				// Restores previousIndex window
				frame.suffix().append(borderEnd(border));
				// Consume scan
				scan = null;
			}

			if(anchorId!=UNSET_INT) {
				IqlTreeNode treeNode = (IqlTreeNode) source;
				IqlElement children = treeNode.getChildren().get();

				// Exhaustive inner search for child node construct
				boolean cached = needsCacheForChild(children);
				Node innerScan = exhaust(true, cached ? cache() : UNSET_INT);
				level++;
				Frame child = process(treeNode.getChildren().get(), innerScan);
				level--;
				// Collapse entire atom as we cannot hoist anything out of the tree node
				child.collapse();

				// Connect atom to tail
				TreeConn conn = treeCon(treeNode, anchorId);
				child.append(conn);

				// Wrap atom into tree node and add after the "content" node section
				Node tree = tree(treeNode, anchorId, child.start(), conn);
				frame.append(tree);
			}

			// If external scan survived atom and marker overrides, apply it finally
			if(scan!=null) {
				frame.push(scan);
			}

			return frame;
		}

		/** Process (quantified) node group */
		private Frame grouping(IqlGrouping source, @Nullable Node scan) {
			final List<IqlQuantifier> quantifiers = source.getQuantifiers();

			// If we have quantifiers we cannot move the scan inside the group sequence
			final boolean hoistScan = !quantifiers.isEmpty();
			final boolean oldFindOnly = setFindOnly(!allowExhaustive(quantifiers));
			//FIXME we need to either adjust the specification or branch here for collections of mixed continuous and discontinuous quantifiers!

			// Make sure to process the group content as a detached atom
			final Frame group = process(source.getElement(), hoistScan ? null : scan);

			// Apply quantification
			group.replace(quantify(group, quantifiers));

			// Finally add external scan if provided
			if(scan!=null && hoistScan && !unwrap(group.start()).isScanCapable()) {
				group.push(scan);
			}

			resetFindOnly(oldFindOnly);

			return group;
		}

		/** Process (ordered or adjacent) node sequence */
		private Frame sequence(IqlSequence source, @Nullable Node scan) {
			final List<IqlElement> elements = source.getElements();
			final boolean adjacent = source.hasArrangement(NodeArrangement.ADJACENT);
			//TODO throw error on illegal arrangement combinations!
			if(source.hasArrangement(NodeArrangement.ORDERED)) {
				return orderedGroup(elements, adjacent, scan);
			}

			return unorderedGroup(elements, adjacent, scan);

//			throw EvaluationUtils.forUnsupportedValue("arrangement", source.getArrangements());
		}

		/** Process alternatives for branching */
		private Frame disjunction(IqlElementDisjunction source, @Nullable Node scan) {
			final List<IqlElement> elements = source.getAlternatives();
			Segment branch = branch(new IqlListProxy(elements), elements.size(),
					i -> process(elements.get(i), null));
			if(scan!=null) {
				branch.push(scan);
			}

			return branch.toFrame();
		}

		// INTERNAL HELPERS

		private boolean allowExhaustive(List<IqlQuantifier> quantifiers) {
			if(quantifiers.isEmpty()) {
				return true;
			}

			loop : for (IqlQuantifier quantifier : quantifiers) {
				switch (quantifier.getQuantifierType()) {
				case ALL:
					continue loop;

				case AT_LEAST: return false;

				case AT_MOST:
				case EXACT:
					if(quantifier.getValue().getAsInt()!=1) {
						return false;
					}
					break;

				case RANGE:
					if(quantifier.getLowerBound().getAsInt()!=1
							|| quantifier.getUpperBound().getAsInt()!=1) {
						return false;
					}
					break;

				default:
					break;
				}
			}

			return true;
		}

		/** Check if child element has quantification, disjunction, nesting or multiple actual nodes */
		private boolean needsCacheForChild(IqlElement child) {
			switch (child.getType()) {
			case NODE:
				return false;

			case DISJUNCTION:
			case TREE_NODE:
				return true;

			case GROUPING: {
				IqlGrouping grouping = (IqlGrouping) child;
				return grouping.hasQuantifiers() || needsCacheForChild(grouping.getElement());
			}

			case SEQUENCE: {
				IqlSequence sequence = (IqlSequence) child;
				List<IqlElement> elements = sequence.getElements();
				return elements.size() > 1 || (!elements.isEmpty() && needsCacheForChild(elements.get(0)));
			}

			default:
				return false;
			}
		}

		private int id() { return id++; }

		private boolean setFindOnly(boolean findOnly) {
			boolean res = this.findOnly;
			this.findOnly |= findOnly;
			return res;
		}

		private void resetFindOnly(boolean findOnly) {
			this.findOnly = findOnly;
		}

		private void discard(Node node) {
			if(!nodes.remove(node))
				throw new QueryException(GlobalErrorCode.INTERNAL_ERROR,
						"Unable to discard node from store: "+node);
			node.detach();
		}

		private <N extends Node> N store(N node) {
			if(!node.isProxy()) {
				nodes.add(node);
			}
			if(node.isSkipControl()) {
				skipControlCount++;
			}
			return node;
		}

		private Node storeTrackable(Node node) {
			store(node);
			if(monitor) {
				trackedNodes.add(node);
				node = new Track(node);
			}
			return node;
		}

		private Frame frame(Node node) {
			 Frame frame = new Frame();
			 frame.push(node);
			 return frame;
		}

		private Segment segment(Node node) {
			Segment segment = new Segment();
			segment.push(node);
			return segment;
		}

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

		private Assignable<? extends Container> containerStore() {
			return rootContext.getContainerStore().orElseThrow(
					() -> EvaluationUtils.forInternalError("No container store available"));
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

		/** Free scan exploration. Depending on context it'll be exhaustive or 'findOnly'. */
		private Node explore(boolean forward, boolean cached) {

			cached |= cacheAll;

			/*
			 * No matter how complex the potentially pending marker construct is,
			 * the attached scan will always work with the same tail (at least
			 * content-wise). Therefore we can use a shared scan node
			 * for all the branches and save graph size.
			 */
			Node scan;
			if(findOnly) {
				if(!forward)
					throw new QueryException(QueryErrorCode.INCORRECT_USE,
							"Cannot do backwards scan inside 'find-only' environment");
				scan = find();
			} else {
				scan = exhaust(forward, cached ? cache() : UNSET_INT);
			}

			return scan;
		}

		/** Create graph for the marker construct. */
		private Segment marker(IqlMarker marker, Node scan, Consumer<? super Node> nodeAction,
				IntConsumer nestedMarkerAction) {
			Segment seg;
			switch (marker.getType()) {
			case MARKER_CALL: {
				IqlMarkerCall call = (IqlMarkerCall) marker;
				seg = markerCall(call, nodeAction, nestedMarkerAction);
				if(scan!=null) {
					seg.append(scan);
				}
			} break;

			case MARKER_EXPRESSION: {
				IqlMarkerExpression expression = (IqlMarkerExpression) marker;
				List<IqlMarker> items = expression.getItems();
				if(expression.getExpressionType()==MarkerExpressionType.CONJUNCTION) {
					seg = intersection(items, scan, nodeAction, nestedMarkerAction);
				} else {
					seg = union(items, scan, nodeAction, nestedMarkerAction);
				}
			} break;

			default:
				throw EvaluationUtils.forUnsupportedQueryFragment("marker", marker.getType());
			}

			return seg;
		}

		private Segment markerCall(IqlMarkerCall call, Consumer<? super Node> nodeAction,
				IntConsumer nestedMarkerAction) {
			final String name = call.getName();

			if(HorizontalMarker.isValidName(name)) {
				// 'isSewuence' is to be read here as 'clipIndex' flag for the Clip nodes
				final boolean isSequence = HorizontalMarker.isSequenceName(name);

				if(level==0 && !isSequence)
					throw EvaluationUtils.forIncorrectUse("Tree hierarchy marker not allowed outside of nested nodes: %s", name);

				final RangeMarker marker = HorizontalMarker.of(name, markerArgs(call));

				if(marker.isDynamic()) {
					// Register markers only if we have to dynamically adjust their intervals
					if(level>0 && !isSequence) {
						nestedMarkerAction.accept(nestedMarkers.size());
						nestedMarkers.add(marker);
					} else {
						globalMarkers.add(marker);
					}
					final int intervalIndex = interval(marker);
					marker.setIndex(intervalIndex);
					final int count = marker.intervalCount();
					if(count>1) {
						return branch(call, count, i -> {
							final Node clip = dynamicClip(call, isSequence, intervalIndex+i);
							nodeAction.accept(clip);
							return frame(clip);
						});
					}

					final Node clip = dynamicClip(call, isSequence, intervalIndex);
					nodeAction.accept(clip);
					return segment(clip);
				}

				assert marker.intervalCount()==1 : "static marker cannot have multiple intervals";

				final Interval interval = Interval.blank();
				marker.setIndex(0);
				marker.adjust(new Interval[] {interval}, 1);
				final Node fixed = fixedClip(call, isSequence, interval.from, interval.to);
				nodeAction.accept(fixed);
				return segment(fixed);
			}

			throw EvaluationUtils.forUnsupportedValue("marker-name", name);
		}

		/** Create single fixed clip */
		private Node fixedClip(IqlMarkerCall source, boolean clipIndex, int from, int to) {
			return storeTrackable(new FixedClip(id(), source, clipIndex, from, to));
		}

		/** Create single dynamic clip */
		private Node dynamicClip(IqlMarkerCall source, boolean clipIndex, int intervalIndex) {
			return storeTrackable(new DynamicClip(id(), source, clipIndex, intervalIndex));
		}

		/**
		 * Sorts segments based on them having the {@link Flag#COMPLEX_MARKER} flag
		 * set or not. Segments without the flag will be first. In case of a draw
		 * the order is determined by segment length.
		 */
		@SuppressWarnings("unused")
		private static final Comparator<Segment> SEGMENT_COMPLEXITY_ORDER = (s1, s2) -> {
			if(s1.isFlagSet(Flag.COMPLEX_MARKER)) {
				if(s2.isFlagSet(Flag.COMPLEX_MARKER)) {
					// Both complex marker disjunctions -> order by total size
					return s1.size()-s2.size();
				}
				// Complex segments go after simpler ones
				return 1;
			}
			// Per default, we only go by the total size of segments
			return s1.size()-s2.size();
		};

		/** Combine sequence of intersecting markers */
		private Segment intersection(List<IqlMarker> markers, Node scan, Consumer<? super Node> nodeAction,
				IntConsumer nestedMarkerAction) {
			assert markers.size()>1 : "Need 2+ markers for intersection";
			Segment seg = new Segment();
			markers.stream()
					.map(m -> marker(m, null, nodeAction, nestedMarkerAction))
					// For optimization reasons we 'should' sort markers, but that would violate the specification
					//.sorted(SEGMENT_COMPLEXITY_ORDER)
					.forEach(seg::append);
			if(scan!=null) {
				seg.append(scan);
			}
			return seg;
		}

		/** Create branches for disjunctive markers */
		private Segment union(List<IqlMarker> markers, Node scan, Consumer<? super Node> nodeAction,
				IntConsumer nestedMarkerAction) {
			assert markers.size()>1 : "Need 2+ markers for union";

			MutableBoolean hasDynamicInterval = new MutableBoolean(false);
			List<FixedClip> fixedIntervals = new ArrayList<>();

			Consumer<? super Node> action2 = nodeAction.andThen(node -> {
				if(node instanceof FixedClip) {
					fixedIntervals.add((FixedClip) node);
				} else {
					hasDynamicInterval.setBoolean(true);
				}
			});

			final Segment seg = branch(new IqlListProxy(markers), markers.size(),
					i -> marker(markers.get(i), null, action2, nestedMarkerAction).toFrame());
			seg.setFlag(Flag.COMPLEX_MARKER);

			//TODO use the info from hasDynamicInterval and fixedIntervals to decide on whether we need gates here!
			final int gateId = gate();
			if(scan!=null) {
				seg.append(scan);
			}
			seg.push(filter(true, gateId));
			seg.append(filter(false, gateId));
			return seg;
		}

		private int cache() { return cacheCount++; }
		private int buffer() { return bufferCount++; }
		private int border() { return borderCount++; }
		private int gate() { return gateCount++; }
		private int anchor() { return anchorCount++; }

		private int permutator(int size) {
			int index = permutators.size();
			permutators.add(size);
			return index;
		}

		private static Number[] markerArgs(IqlMarkerCall call) {
			return IntStream.range(0, call.getArgumentCount())
					.mapToObj(call::getArgument)
					.map(Number.class::cast)
					.toArray(Number[]::new);
		}

		/** Push intervals for given marker on the stack and return index of first interval */
		private int interval(RangeMarker marker) {
			final int intervalIndex = intervals.size();
			final int count = marker.intervalCount();
			for (int i = 0; i < count; i++) {
				intervals.add(Interval.blank());
			}
			return intervalIndex;
		}

		private Node empty(IqlQueryElement source, @Nullable String label, int anchorId) {
			return storeTrackable(new Empty(id(), source, member(label), anchorId));
		}

		private Begin begin() { return store(new Begin(id())); }

		/** Make a utility node that either saves or restores a border point  */
		private Border borderBegin(int borderId, int[] nestedMarkerIndices) {
			return store(new Border(id(), borderId, nestedMarkerIndices));
		}

		/** Make a utility node that either saves or restores a border point  */
		private Border borderEnd(int borderId) { return store(new Border(id(), borderId)); }

		private Filter filter(boolean reset, int gateId) { return store(new Filter(id(), reset, gateId)); }

		private Node finish(long limit, boolean stopAfterMatch) {
			return storeTrackable(new Finish(id(), limit, stopAfterMatch));
		}

		private Node permutate(IqlQueryElement source, int permId, boolean adjacent, Node[] atoms) {
			return storeTrackable(new PermInit(id(), source, permId, !adjacent, atoms));
		}

		private Node permutationElement(int permId, int slot) {
			return storeTrackable(new PermSlot(id(), permId, slot));
		}

		private Node tree(IqlTreeNode source, int anchorId, Node child, TreeConn conn) {
			return storeTrackable(new Tree(id(), source, anchorId, child, conn));
		}

		private TreeConn treeCon(IqlTreeNode source, int anchorId) {
			return store(new TreeConn(id(), source, anchorId));
		}

		private Reset reset() { return store(new Reset(id())); }

		private Node single(IqlNode source, @Nullable String label, IqlConstraint constraint, int anchorId) {
			return storeTrackable(new Single(id(), source, matcher(constraint), cache(), member(label), anchorId));
		}

		private Node find() {
			return storeTrackable(new Find(id()));
		}

		private Node exhaust(boolean forward, int cacheId) {
			return storeTrackable(new Exhaust(id(), cacheId, forward));
		}

		private Node negate(IqlQuantifier source, Node atom) {
			return storeTrackable(new Negation(id(), source, cache(), atom));
		}

		private Node all(IqlQuantifier source, Node atom) {
			return storeTrackable(new All(id(), source, atom));
		}

		private Segment branch(IqlQueryElement source, int count, IntFunction<Frame> atomGen) {
			List<Node> atoms = new ArrayList<>();
			BranchConn conn = store(new BranchConn(id()));
			// For consistency we collect branches in reverse order
			for (int i = 0; i < count; i++) {
				// Might be a complex sub-structure
				Frame atom = atomGen.apply(i);
				Node start = unwrap(atom.start());
				Node end = atom.end();

				/* Try to unfold nested branches that have no complex decorations
				 * We need to make sure we only unfold branch singular structures that
				 * do not have an additional tail set to them.
				 */
				if(start instanceof Branch && !atom.hasAffix()
						&& end instanceof BranchConn
						&& ((Branch)start).conn==end) {
					Branch branch = (Branch) start;
					Node[] nestedAtoms = branch.getAtoms();
					Node nestedConn = branch.conn;
					for(Node n : nestedAtoms) {
						atoms.add(n);
						lastBefore(n, nestedConn).setNext(conn);
					}
					discard(branch);
					discard(nestedConn);
					continue;
				}

				// Link atom to connection node
				atom.suffix().append(conn);
				// Ensure entire atom is one sequence
				atom.collapse();
				atoms.add(atom.start());
			}
			return segment(storeTrackable(new Branch(id(), source, conn,
					atoms.toArray(new Node[atoms.size()]))));
		}

		private Node repetition(IqlQuantifier source, Node atom, int cmin, int cmax,
				int mode, boolean discontinuous) {
			return storeTrackable(new Repetition(id(), source, atom, cmin, cmax, mode,
					buffer(), buffer(), buffer(), discontinuous ? id() : -1));
		}

		private boolean needsCacheForScan(Node node) {
			if(node instanceof Single) {
				// If we have a complex structure AFTER the node, ensure caching!
				return !node.next.isFinisher();
			}
			// Lone single nodes never need external caching
			return false;
		}

		private Frame unorderedGroup(List<IqlElement> elements, boolean adjacent, @Nullable Node scan) {
			final int size = elements.size();
			if(size==1) {
				return process(elements.get(0), scan);
			}

			Frame group = new Frame();

			final int permId = permutator(size);
			final Node[] atoms = new Node[size];

			// We fill atoms and proxies arrays later
			group.push(permutate(new IqlListProxy(elements), permId, adjacent, atoms));

			for (int slot = 0; slot < size; slot++) {

				// Process each atom as distinct element
				Frame step = process(elements.get(slot), null);
				// Prepend "outer" proxy to allow for situational scanning
				step.push(permutationElement(permId, slot));
				// Collapse no so we can get clipping before the inner scans
				step.collapse();
				// "Inner" proxy to ensure the atom gets properly linked to subsequent slots
				// needs to be added after collapsing so we don't cut off border reset nodes
				step.append(store(new PermConn(id(), permId, slot)));

				atoms[slot] = step.start();
				group.incNodes(step.nodes());
			}

			if(scan!=null) {
				group.push(scan);
			}

			return group;
		}

		/**
		 * Sequential scanning of ordered elements.
		 * Note that any node but the first in the original
		 * sequence may receive a scan attached to it depending on the 'adjacent' flag.
		 * The first node in the sequence might have its prefix hoisted.
		 */
		private Frame orderedGroup(List<IqlElement> elements, boolean adjacent, @Nullable Node scan) {
			if(elements.size()==1) {
				return process(elements.get(0), scan);
			}

			Frame group = new Frame();
			int last = elements.size()-1;
			for(int i=0; i<=last; i++) {
				Frame step = process(elements.get(i), i==0 ? scan : null);
				// Special handling only for subsequent steps
				if(i>0 || adjacent) {
					if(!adjacent) {
						Node head = unwrap(step.start());
						// Any node but the first can receive an automatic scan attached to it
						if(!head.isScanCapable() && !head.isFixed()) {
							// Cashing will be used either for complex inner structure or intermediate nodes
							step.push(explore(true, needsCacheForScan(head) || i<last));
						}
					}
					// Ensure we don't mix up hoistable content and proactively collapse
					step.collapse();
				}
				// Accumulate steps into single sequence
				group.append(step);
			}
			return group;
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
		private Segment quantify(Segment atom, List<IqlQuantifier> quantifiers) {
			if(quantifiers.isEmpty()) {
				// No quantification -> nothing to do
				return atom;
			} else if(quantifiers.size()==1) {
				// Singular quantifier -> simple wrapping
				final Node head = atom.start();
				final Node quant = quantify(head, quantifiers.get(0));
				if(head==quant) {
					return atom;
				}
				return segment(quant);
			} else {
				// Combine all quantifiers into a branch structure
				return branch(new IqlListProxy(quantifiers), quantifiers.size(),
						i -> frame(quantify(atom.start(), quantifiers.get(i))));
			}
		}

		/** Wraps a quantification around atoms */
		private Node quantify(Node atom, IqlQuantifier quantifier) {
			Node node;
			if(quantifier.isExistentiallyNegated()) {
				node = negate(quantifier, atom);
			} else if(quantifier.isUniversallyQuantified()) {
				node = all(quantifier, atom);
			} else {
				int min = 1;
				int max = Integer.MAX_VALUE;
				int mode = GREEDY;
				switch (quantifier.getQuantifierType()) {
				case ALL: { // *
					throw EvaluationUtils.forInternalError("Universal quantification not handled here");
				}
				case EXACT: { // n
					min = max = quantifier.getValue().getAsInt();
					mode = POSSESSIVE;
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
					mode = mode(quantifier);
				} break;

				default:
					throw EvaluationUtils.forUnsupportedQueryFragment("quantifier", quantifier.getQuantifierType());
				}

				if(min==1 && max==1) {
					node = atom;
				} else {
					node = repetition(quantifier, atom, min, max, mode, quantifier.isDiscontinuous());
				}
			}
			return node;
		}

		StateMachineSetup createStateMachine() {
			// Ensure we have a proper structural constraint here
			rootElement.checkIntegrity();

			//TODO run a verification of marker+quantifier combinations

			if(filterConstraint != null) {
				expressionFactory = new ExpressionFactory(rootContext);
				filter = new FilterDef(containerStore(), constraint(globalConstraint), rootContext);
				expressionFactory = null;
			}

			final boolean disjoint = isFlagSet(MatchFlag.DISJOINT);

			resetFindOnly(false);
			//FIXME need to rework the permutation node as we stack scans here otherwise!!!
			Node rootScan = explore(modifier!=QueryModifier.LAST, false);

			// For now we don't honor the 'consumed' flag on IqlElement instances
			final Frame frame = process(rootElement, rootScan);

			// Collapse all actual content before we add special nodes
			frame.collapse();

			// Global constraints get evaluated after all normal content but before dispatch phase
			if(globalConstraint != null) {
				expressionFactory = new ExpressionFactory(rootContext);
				global = new ExpressionDef(constraint(globalConstraint), rootContext);
				expressionFactory = null;
				frame.append(new GlobalConstraint(id(), globalConstraint));
			}

			// If we need to reset search space after each match, add special Reset node
			if(disjoint) {
				frame.push(reset());
			}

			// Add size-based filter
			frame.prefix().push(begin());

			// Add final dispatch bridge
			frame.suffix().append(finish(limit, disjoint));

			// Now collapse everything again
			frame.collapse();

			final Node root = frame.start();

			// Force optimization
			root.study(new TreeInfo());

			// Fill state machine
			StateMachineSetup sm = new StateMachineSetup();
			sm.nodes = nodes.toArray(new Node[0]);
			sm.trackedNodes = trackedNodes.stream().toArray(Node[]::new);
			sm.filterConstraint = filter;
			sm.globalConstraint = global;
			sm.rawNodes = rawNodes.toArray(new IqlNode[0]);
			sm.limit = limit;
			sm.root = root;
			sm.cacheCount = cacheCount;
			sm.borderCount = borderCount;
			sm.bufferCount = bufferCount;
			sm.gateCount = gateCount;
			sm.anchorCount = anchorCount;
			sm.skipControlCount = skipControlCount;
			sm.permutations = permutators.toIntArray();
			sm.intervals = intervals.toArray(new Interval[0]);
			sm.globalMarkers = globalMarkers.toArray(new RangeMarker[0]);
			sm.nestedMarkers = nestedMarkers.toArray(new RangeMarker[0]);
			sm.matchers = matchers.toArray(new Supplier[0]);

			return sm;
		}
	}

	/**
	 * Models the matching context for a node collection in the tree.
	 * For plain sequence matching this is equal to the total list
	 * of nodes in the container. For nested nodes this then changes
	 * to the (ordered) lists of child nodes in the tree.
	 * <p>
	 * Each frame is essentially a window to a sorted list of index values
	 * which also can non-continuous:
	 * <pre>
	 * +------------------------------------------------------------+
	 * |       from  from+1  from+2  ...  to-2   to-1   to          |
	 * +------------------------------------------------------------+
	 * | i_0   i_1   i_2     i_3          i_n-3  i_n-2  i_n-1  i_n  |
	 * +------------------------------------------------------------+
	 * </pre>
	 */
	static class TreeFrame {
		/** Index of the node that this frame represents or {@code -1} for the virtual root node */
		int index = -1;
		/** Sorted index values */
		int[] indices;
		/** Total number of index values available */
		int length;
		/** Length of path to root */
		int depth;
		/** Length of path to deepest nested leaf */
		int height;
		/** Index of parent node/frame */
		int parent;
		/** Accumulated number of descendants in the subtree rooted at this frame */
		int descendants;
		/** Begin index of span covered by node */
		int begin;
		/** End index of span covered by node */
		int end;
		/**
		 * Index of the most recent node matched in this frame.
		 * Used to enforce adjacent node matching. If free positioning
		 * is allowed, scan nodes will reset this value to {@code IcarusUtils#UNSET_INT};
		 */
		int previousIndex = UNSET_INT;
		/**
		 * Position of the most recent node matched inside the current frame.
		 * This is used to
		 */
		@Deprecated
		int previousPos = UNSET_INT;

		/** Region of allowed positional values */
		Interval window = Interval.blank();

		/** Flag to signal that frame data is up to date */
		boolean valid; //TODO actually use this for lazy tree construction

		TreeFrame(int initialSize) {
			indices = new int[initialSize];
		}

		/** Ensure that {@code newSize} index values fit into the indices buffer */
		void resize(int newSize) {
			indices = Arrays.copyOf(indices, newSize);
		}

		public void reset() {
			window.reset(0, length-1);
			previousIndex = UNSET_INT;
		}

		// tree methods

		//TODO do we need those methods or is direct access preferred?
//		/** Length (number of items) of current sequence */
//		final int length() { return length; }
//		/** Nesting depth of current node */
//		final int depth() { return depth; }
//		/** Height (distance to deepest leaf) of current node */
//		final int height() { return height; }
//		/** Index of parent node */
//		final int parent() { return parent; }

		/** Fetches global index for (child) node at given index */
		final int childAt(int index) { return indices[index]; }

		// positioning methods

		/** Starting position of window */
		final int from() { return window.from; }
		/** End position of window */
		final int to() { return window.to; }

		/** Set starting position of window */
		final void from(int value) { window.from = value; }
		/** Set end position of window */
		final void to(int value) { window.to = value; }
		/** Reset entire window bounds to supplied values */
		final void resetWindow(int from, int to) { window.reset(from, to); }

		/** Smallest index in frame */
		final int lowest() { return indices[0]; }
		/** Highest index in frame */
		final int highest() { return indices[length-1]; }

		/**
		 * Tests if the specified {@code index} is contained in the currently
		 * available index interval. The default implementation uses a binary
		 * search within the closed interval {@code from .. to} for the given
		 * {@code index} and returns {@code true} if the value could be found.
		 */
		//TODO benchmark if we can save substantial time by using an IntSet buffer for frames after a certain size
		boolean containsIndex(int index) {
			if(index < lowest() || index > highest()) {
				return false;
			}
			return Arrays.binarySearch(indices, window.from, window.to+1, index) > -1;
		}

		/** Check whether the current window contains the supplied positional value */
		boolean containsPos(int pos) { return window.contains(pos); }

		/**
		 * Applies a span-based index filter to this frame, limiting the
		 * current window to only those positional values that map to indices
		 * within the specified  filter interval.
		 *
		 * @param window interval of legal index values
		 * @return {@code true} if after filtering the window is not empty
		 */
		boolean retainIndices(Interval filter) {
			int from = window.from;
			int to = window.to;

			// Filter fully outside window
			if(filter.to<indices[from] || filter.from>indices[to]) {
				window.reset();
				return false;
			}

			// binary search returns -insertion_point - 1

			to = Arrays.binarySearch(indices, from, to+1, filter.to);
			if(to < 0) {
				to = -to - 1; // we need the last element smaller than filter.to value
			}

			from = Arrays.binarySearch(indices, from, to+1, filter.from);
			if(from < 0) {
				from = -from - 1;
				// we need the first element greater than filter.from value
				if(indices[from] < filter.from) {
					from++;
				}
			}

			if(from==to && !filter.contains(indices[from])) {
				window.reset();
			} else {
				window.reset(from, to);
			}

			return !window.isEmpty();
		}

		/**
		 * Applies a span-based index filter to this frame, limiting the
		 * current window to only those positional values that are also
		 * contained in the given filter interval.
		 *
		 * @param filter window interval of legal index values
		 * @return {@code true} if after filtering the window is not empty
		 */
		boolean retainPos(Interval filter) {
			return window.intersect(filter);
		}
	}

	static final class RootFrame extends TreeFrame {

		RootFrame(int initialSize) {
			super(initialSize);
			ArrayUtils.fillAscending(indices);

			window.reset(0, 0);
			depth = height = descendants = 0;
			parent = UNSET_INT;
			valid = true;
			begin = end = UNSET_INT;
		}

		/**
		 * For the top level traversal we can directly use the span-based
		 * interval method for membership checks of individual index values.
		 * @see de.ims.icarus2.query.api.engine.matcher.SequencePattern.TreeFrame#containsIndex(int)
		 */
		@Override
		boolean containsIndex(int index) {
			return window.contains(index);
		}

		/**
		 * Due to identity mapping between positional and index values we
		 * can simply intersect the raw window with the filter interval.
		 *
		 * @see Interval#intersect(Interval)
		 * @see de.ims.icarus2.query.api.engine.matcher.SequencePattern.TreeFrame#retainIndices(de.ims.icarus2.query.api.engine.matcher.mark.Interval)
		 */
		@Override
		boolean retainIndices(Interval filter) {
			return window.intersect(filter);
		}

		/** Keeps identity mapping in resized indices array. */
		@Override
		void resize(int newSize) {
			int oldSize = indices.length;
			indices = Arrays.copyOf(indices, newSize);
			for (int i = oldSize; i < newSize; i++) {
				indices[i] = i;
			}
		}

		/**
		 * Reset this root frame for the specified length, adjusting
		 * {@link #length} and {@link #from} fields;
		 */
		void reset(int length) {
			this.length = length;
			window.reset(0, length-1);
			previousIndex = UNSET_INT;
		}

		@Override
		public void reset() {
			length = 0;
			window.reset(0, 0);
			previousIndex = UNSET_INT;
		}
	}

	static final int MODE_SKIP = 0;
	static final int MODE_ADJACENT = 0;

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
	static class State implements IntTree {
		/** Raw target container or structure */
		Container target;
		/** Total number of items in container */
		int size = 0;

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
		/** All the raw markers that produce global restrictions on node positions */
		final RangeMarker[] globalMarkers;
		/** All the raw markers that produce nested restrictions on node positions */
		final RangeMarker[] nestedMarkers;
		/** All the gate caches for keeping track of duplicate matcher positions */
		final Gate[] gates;
		/** In-place permutation generators and contexts to be used for unordered groups */
		final PermutationContext[] permutations;
		/** Keeps track of the last hit index for every raw node */
		final int[] hits;
		/** The available int[] buffers used by various node implementations */
		final int[][] buffers;
		/** Stores the right boundary around marker interval operations */
		final int[] borders;

		final Matcher<Container> filterConstraint;
		final Expression<?> globalConstraint;

		Monitor monitor;

		/** Total number of reported full matches so far */
		long reported = 0;

		/** Number of mappings stored so far and also the next insertion index */
		int entry = 0;

		/** The frame representing the overall list of items in the container */
		final RootFrame rootFrame;

		/** Tentatively marked tree node spots. Stores the positional index for node. */
		final Anchor[] anchors;

		/** Utility buffers for closure operations over node dominance */
		final ClosureContext[] closures;

		/** Set by the Finish node if a result limit exists and we already found enough matches. */
		boolean finished;
		/**
		 * Set by the Finish node if result limit is exceeded or the search is using
		 * DISJOINT mode. Nodes that iteratively explore the search space should use this
		 * flag as indicator. When using DISJOINT mode the Reset node will clear this flag
		 * after each match and adjust the search space to exclude already exhausted areas.
		 */
		boolean stop;

		/**
		 * Indicator for skip-capable nodes whether or not they are actually allowed to
		 * skip parts of the search space. Default value is {@code true} to support
		 * efficient searching. Certain nodes will set this to {@code false} for portions
		 * of the automaton. If any node changes this value, it must make sure to reset it
		 * to the previously set value!
		 */
		final ModeTrace[] modes;

		/**
		 * End index of the last (sub)match, used by repetitions and similar nodes to keep track.
		 * Initially {@code 0}, turned to {@code -1} for failed matches and to
		 * the next index to be visited when a match occurred.
		 */
		int last = 0;

		/**
		 * Currently active frame in the tree matching.
		 * Initially set to the {@link State} instance itself to represent the
		 * entirety of the item sequence.
		 */
		TreeFrame frame;

		Consumer<State> resultHandler;

		// Growing Buffers

		/** Items in target container, copied for faster access */
		Item[] elements;
		/** View on the target tree as frames */
		TreeFrame[] tree;
		/** Indices of root nodes in the tree. {@code -1} indicates end of list */
		int[] roots;
		/** Path from a root node to current node */
		int[] trace;
		/** Keys for the node mapping */
		int[] m_node;
		/** Values for the node mapping, i.e. the associated indices */
		int[] m_pos;
		/** Marks individual nodes as excluded from further matching */
		boolean[] locked;

		State(StateMachineSetup setup) {
			final int initialSize = setup.initialSize == UNSET_INT ? INITIAL_SIZE : setup.initialSize;
			elements = new Item[initialSize];
			tree = new TreeFrame[initialSize];
			roots = new int[initialSize];
			trace = new int[initialSize];
			m_node = new int[initialSize];
			m_pos = new int[initialSize];
			locked = new boolean[initialSize];

			globalMarkers = setup.getGlobalMarkers();
			nestedMarkers = setup.getNestedMarkers();

			nodes = setup.getRawNodes();
			hits = setup.getHits();
			borders = setup.getBorders();

			modes = new ModeTrace[2];

			modes[MODE_SKIP] = new ModeTrace(setup.skipControlCount, true);

			filterConstraint = setup.makeFilterConstraint();
			globalConstraint = setup.makeGlobalConstraint();

			matchers = setup.makeMatchers();
			members = setup.makeMembers();
			caches = setup.makeCaches(initialSize);
			intervals = setup.makeIntervals();
			buffers = setup.makeBuffer(initialSize);
			gates = setup.makeGates(initialSize);
			anchors = setup.makeAnchors();
			closures = setup.makeClosures(initialSize);
			permutations = setup.makePermutations();

			rootFrame = new RootFrame(initialSize);
			frame = rootFrame;

			Arrays.fill(roots, UNSET_INT);
			for (int i = 0; i < tree.length; i++) {
				tree[i] = new TreeFrame(initialSize);
			}
		}

		/** Fetch current scope id, i.e. a marker for resetting.  */
		final int scope() {
			return entry;
		}

		/**
		 * Reset scope to an old marker, i.e. discard all mappings stored since then.
		 */
		final void resetScope(int scope) {
			while(entry>scope) {
				entry--;
				locked[m_pos[entry]] = false;
			}
		}

		/** Resolve raw node for 'nodeId' and map to 'index' in result buffer. */
		final void map(int nodeId, int index) {
			assert !locked[index] : "index "+index+" already locked";
			locked[index] = true;

			m_node[entry] = nodeId;
			m_pos[entry] = index;
			entry++;
		}

		final void setMode(int mode, boolean value) { modes[mode].set(value); }
		final boolean isSet(int mode) { return modes[mode].value; }
		final void resetMode(int mode) { modes[mode].back(); }

		public void reset() {
			// Cleanup duty -> we must erase all references to target and its elements
			target = null;
			Arrays.fill(elements, 0, size, null);
			Arrays.fill(locked, 0, size, false);
			Arrays.fill(hits, UNSET_INT);
			for (int i = 0; i < caches.length; i++) {
				caches[i].reset(size);
			}
			for (int i = 0; i < tree.length; i++) {
				tree[i].reset();
			}
			for (int i = 0; i < modes.length; i++) {
				modes[i].reset();
			}
			entry = 0;
			last = 0;
			size = 0;
			rootFrame.reset();
			Arrays.fill(roots, UNSET_INT);
			frame = rootFrame;
		}

		final void dispatchMatch() {
			if(resultHandler!=null) {
				resultHandler.accept(this);
			}
		}

		final void monitor(Monitor monitor) {
			checkState("Monitor already set", this.monitor==null);
			this.monitor = monitor;
		}

		final void resultHandler(Consumer<State> resultHandler) {
			checkState("Result handler already set", this.resultHandler==null);
			this.resultHandler = resultHandler;
		}

		// tree interface

		@Override
		public final int size() { return size; }

		@Override
		public final int size(int nodeId) { return tree[nodeId].length; }

		@Override
		public final int height(int nodeId) { return tree[nodeId].height; }

		@Override
		public final int depth(int nodeId) { return tree[nodeId].depth; }

		@Override
		public final int childAt(int nodeId, int index) { return tree[nodeId].indices[index]; }

	}

	/**
	 * Public entry point for sequence matching and holder of the
	 * state during a matching operation.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class SequenceMatcher extends State implements Matcher<Container> {

		/** The only thread allowed to call {@link #matches(long, Container)} on this instance */
		final Thread thread;

		final int id;

		/** The node of the state machine to start matching with. */
		final Node root;

		SequenceMatcher(StateMachineSetup stateMachineSetup, int id) {
			super(stateMachineSetup);

			this.id = id;
			thread = Thread.currentThread();
			this.root = stateMachineSetup.getRoot();
		}

		@Override
		public int id() { return id; }

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
			int requiredBufferSize = size + 1;
			// If new size exceeds buffer, grow all storages
			if(requiredBufferSize>=elements.length) {
				growBuffers(requiredBufferSize);
			}
			// Now copy container content into our buffer for faster access during matching
			this.target = target;
			for (int i = 0; i < size; i++) {
				elements[i] = target.getItemAt(i);
			}
			rootFrame.reset(size);

			// Update dynamic marker intervals
			for (int i = 0; i < globalMarkers.length; i++) {
				/* The 'adjust' method allows for early exit in case no valid
				 * intervals have been produced. But we can't make use of that
				 * here, as markers can be used in complex disjunctive query
				 * constructs and we have no way of knowing here which marker
				 * can be used as quick-check for an early abort.
				 */
				//TODO maybe add flag array to mark intervals that can be used as early exit check?
				globalMarkers[i].adjust(intervals, size);
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

		private void growBuffers(int minCapacity) {
			final int oldSize = elements.length;
			final int newSize = CollectionUtils.growSize(oldSize, minCapacity);
			elements = new Item[newSize];
			m_node = new int[newSize];
			m_pos = new int[newSize];
			locked = new boolean[newSize];
			trace = new int[newSize];
			roots = new int[newSize];
			for (int i = 0; i < buffers.length; i++) {
				buffers[i] = new int[newSize];
			}
			for (int i = 0; i < caches.length; i++) {
				caches[i].resize(newSize);
			}
			for (int i = 0; i < gates.length; i++) {
				gates[i].resize(newSize);
			}
			for (int i = 0; i < tree.length; i++) {
				tree[i].resize(newSize);
			}
			for (int i = 0; i < closures.length; i++) {
				closures[i].resize(newSize);
			}

			Arrays.fill(roots, UNSET_INT);
			rootFrame.resize(newSize);
			for (int i = oldSize; i < tree.length; i++) {
				tree[i].resize(newSize);
			}
		}
	}

	/**
	 * A special helper class for testing.
	 * <p>
	 * This implementation overrides the default {@link SequenceMatcher#reset()} method
	 * to do nothing. This is so that test code can actually do proper assertions on the
	 * internal matcher state <b>after</b> a matching attempt (be it a fail or success).
	 * In addition a dedicated {@link NonResettingMatcher#fullReset()} method is provided
	 * that does the job of the former {@link SequenceMatcher#reset()} method and the
	 * added {@link NonResettingMatcher#softReset()} to only reset state information
	 * not used by testing code. To not pollute the public API, this class and
	 * all the dedicated methods are kept package-private.
	 *
	 * @author Markus Gärtner
	 *
	 */
	@VisibleForTesting
	static class NonResettingMatcher extends SequenceMatcher {

		NonResettingMatcher(StateMachineSetup stateMachineSetup, int id) {
			super(stateMachineSetup, id);
		}

		/**
		 * Does nothing, so that we can properly assert the state machine's internal
		 * state after matching.
		 */
		@Override
		public void reset() { /* no-op */ }

		/**
		 * Replacement for the original {@link SequenceMatcher#reset()} method so
		 * that test code can decide to reset matcher state if needed.
		 */
		@VisibleForTesting
		void fullReset() { super.reset(); }

		/** Only resets external references and the temporary result buffer. */
		@VisibleForTesting
		void softReset() {
			Arrays.fill(elements, 0, size, null);
			Arrays.fill(hits, UNSET_INT);
			entry = 0;
			last = 0;
			rootFrame.reset();
		}
	}

	public static class Builder extends AbstractBuilder<Builder, SequencePattern> {

		private IqlElement root;
		private Integer id;
		private Integer initialBufferSize;
		private QueryModifier modifier = QueryModifier.ANY;
		private Long limit;
		private IqlConstraint filterConstraint;
		private IqlConstraint globalConstraint;
		private LaneContext context;
		private final Set<MatchFlag> flags = EnumSet.noneOf(MatchFlag.class);
		private Function<IqlNode, IqlNode> nodeTransform;
		private Boolean allowMonitor;
		private Boolean cacheAll;

		//TODO add field for adjusting initial buffer sizes
		//TODO add fields for configuring the result buffer

		private Builder() { /* no-op */ }

		@VisibleForTesting
		boolean isCacheAll() { return cacheAll==null ? false : cacheAll.booleanValue(); }

		@VisibleForTesting
		Builder cacheAll(boolean cacheAll) {
			checkState("'cacheAll' flag already set", this.cacheAll==null);
			this.cacheAll = Boolean.valueOf(cacheAll);
			return this;
		}

		@VisibleForTesting
		Function<IqlNode, IqlNode> getNodeTransform() { return nodeTransform; }

		@VisibleForTesting
		Builder nodeTransform(Function<IqlNode, IqlNode> nodeTransform) {
			requireNonNull(nodeTransform);
			checkState("node transformation already set", this.nodeTransform==null);
			this.nodeTransform = nodeTransform;
			return this;
		}

		public boolean isAllowMonitor() { return allowMonitor==null ? false : allowMonitor.booleanValue(); }

		public Builder allowMonitor(boolean allowMonitor) {
			checkState("'allowMonitor' flag already set", this.allowMonitor==null);
			this.allowMonitor = Boolean.valueOf(allowMonitor);
			return this;
		}

		public int getId() { return id==null ? UNSET_INT : id.intValue(); }

		public Builder id(int id) {
			checkArgument("ID must be positive", id>=0);
			checkState("ID already set", this.id==null);
			this.id = Integer.valueOf(id);
			return this;
		}

		public int getInitialBufferSize() { return initialBufferSize==null ? UNSET_INT : initialBufferSize.intValue(); }

		public Builder initialBufferSize(int initialBufferSize) {
			checkArgument("Initial buffer size must be positive", initialBufferSize>=0);
			checkState("Initial buffer size already set", this.initialBufferSize==null);
			this.initialBufferSize = Integer.valueOf(initialBufferSize);
			return this;
		}

		public QueryModifier getModifier() { return modifier; }

		public Builder modifier(QueryModifier modifier) {
			requireNonNull(modifier);
			checkState("modifier already set", this.modifier==QueryModifier.ANY);
			this.modifier = modifier;
			return this;
		}

		public long getLimit() { return limit==null ? UNSET_LONG : limit.longValue(); }

		public Builder limit(long limit) {
			checkArgument("limit must be positive", limit>=0);
			checkState("limit already set", this.limit==null);
			this.limit = Long.valueOf(limit);
			return this;
		}

		public IqlElement getRoot() { return root; }

		public Builder root(IqlElement root) {
			requireNonNull(root);
			checkState("root already set", this.root==null);
			this.root = root;
			return this;
		}

		public IqlConstraint getFilterConstraint() { return filterConstraint; }

		public Builder filterConstraint(IqlConstraint filterConstraint) {
			requireNonNull(filterConstraint);
			checkState("filter constrint already set", this.filterConstraint==null);
			this.filterConstraint = filterConstraint;
			return this;
		}

		public IqlConstraint getGlobalConstraint() { return globalConstraint; }

		public Builder globalConstraint(IqlConstraint globalConstraint) {
			requireNonNull(globalConstraint);
			checkState("global constrint already set", this.globalConstraint==null);
			this.globalConstraint = globalConstraint;
			return this;
		}

		public LaneContext geContext() { return context; }

		public Builder context(LaneContext context) {
			requireNonNull(context);
			checkState("context already set", this.context==null);
			this.context = context;
			return this;
		}

		public Set<MatchFlag> geFlags() { return EnumSet.copyOf(flags); }

		public Builder flag(MatchFlag flag) {
			requireNonNull(flag);
			checkState("flag already set", !flags.contains(flag));
			flags.add(flag);
			return this;
		}

		public Builder flags(Collection<MatchFlag> flags) {
			requireNonNull(flags);
			checkArgument("set of flags must not be empty", !flags.isEmpty());
			this.flags.addAll(flags);
			return this;
		}

		@Override
		protected void validate() {
			super.validate();

			checkState("No root element defined", root!=null);
			checkState("Id not defined", id!=null);
			checkState("Context not defined", context!=null);
			checkState("Context is not a lane context", context.isLane());
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
	 * Utility interface to track actions during an active evaluation process of the
	 * state machine. Currently this interface relies heavily on exposure of internal
	 * state information of {@link State} to provide the information needed to properly
	 * monitor the state machine. In the future we wanna switch that to wrapper interfaces
	 * that provide a similar level of access without really exposing internal fields/classes.
	 *
	 * @author Markus Gärtner
	 *
	 */
	interface Monitor {
		//TODO add callbacks for result dispatch and other events

		/** Called when a proper node is entered */
		default void enterNode(Node node, State state, int pos) {
			// no-op;
		}

		/** Called when a proper node is exited */
		default void exitNode(Node node, State state, int pos, boolean result) {
			// no-op;
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
			this.id = id;
			this.element = requireNonNull(element);
			this.constraints = new ExpressionDef(constraint, context);
			this.context = requireNonNull(context);
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
	@Deprecated
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

	interface AdaptableSize {
		void resize(int newSize);
	}

	static final class Cache implements AdaptableSize {
		/**
		 * Paired booleans for each entry, leaving capacity for 512 entries by default.
		 * First value of each entry indicates whether it is actually set, second one
		 * stores the cached value.
		 */
		boolean[] data;

		Cache(int initialSize) {
			data = new boolean[initialSize<<1];
		}

		@Override
		public
		void resize(int newSize) {
			data = Arrays.copyOf(data, newSize<<1);
		}

		void reset(int size) {
			Arrays.fill(data, 0, size<<1, false);
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

	static final class ModeTrace {
		private final boolean[] trace;
		private final boolean defaultValue;
		private int index;
		boolean value;

		ModeTrace(int size, boolean defaultValue) {
			trace = new boolean[size];
			this.defaultValue = defaultValue;
			value = defaultValue;
		}

		final void set(boolean value) {
			trace[index++] = this.value;
			this.value = value;
		}

		final void back() {
			this.value = trace[--index];
		}

		final void reset() {
			value = defaultValue;
			index = 0;
		}
	}

	static final class Gate implements AdaptableSize {
		/**
		 * For each slot indicates if it has been visited already.
		 */
		boolean[] data;
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;

		Gate(int initialSize) {
			data = new boolean[initialSize];
		}

		@Override
		public void resize(int newSize) {
			data = Arrays.copyOf(data, newSize);
			resetBounds();
		}

		private void resetBounds() {
			min = Integer.MAX_VALUE;
			max = Integer.MIN_VALUE;
		}

		/** Marks the provided {@code pos} and returns {@code true} iff it was still available. */
		boolean visit(int pos) {
			if(data[pos]) {
				return false;
			}

			data[pos] = true;
			if(pos<min) min = pos;
			if(pos>max) max= pos;

			return true;
		}

		/** Check if given {@code pos} is still unvisited. */
		boolean canVisit(int pos) {
			return !data[pos];
		}

		void clear() {
			if(max>=0) {
				Arrays.fill(data, min, max+1, false);
			}
			resetBounds();
		}
	}

	/**
	 * Utility class for handling permutations in the state machine during matching.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static final class PermutationContext {
		/** Source of the permutation */
		final Permutator source;
		/**
		 * Permutated list of atom nodes, following {@link Permutator#current() current} configuration.
		 * (indexed by atom index)
		 */
		final Node[] current;
		/** Indicator what slot atom i is located in the current permutation. (indexed by atom index) */
		final int[] slots;
		/** Right border for traversal of each atom. (indexed by atom index) */
		final int[] fences;
		/** Smallest slot index that produced a direct fail when matching the respective atom */
		int skip;
		/** Links to next perm-slot node for the tail section of atom nodes. (indexed by atom index) */
		final Node[] next;
		/** Flag to signal that a {@link PermConn} has been activated. Used for skipping. (indexed by atom index) */
		final boolean[] used;
		/** Flag to indicate that the {@link PermSlot} node for a given index is allowed to use scanning. (indexed by atom index) */
		boolean scan;

		PermutationContext(int size) {
			source = Permutator.forSize(size);
			current = new Node[size];
			slots = new int[size];
			next = new Node[size];
			fences = new int[size];
			used = new boolean[size];
		}

		void reset() {
			Arrays.fill(current, null);
			Arrays.fill(next, null);
			source.reset();
		}
	}

	static final class ClosureStep {
		int frameId;
		int pos;

		void reset(int frameId, int pos) {
			this.frameId = frameId;
			this.pos = pos;
		}
	}

	/** Models a trace for the {@link TreeClosure} node. */
	static final class ClosureContext implements AdaptableSize {
		/** Current position in the trace */
		int depth;
		/** */
		ClosureStep[] steps;

		ClosureContext(int initialSize) {
			depth = UNSET_INT;
			steps = new ClosureStep[initialSize];
		}

		@Override
		public void resize(int newSize) {
			int oldSize = steps.length;
			steps = Arrays.copyOf(steps, newSize);
			for (int i = oldSize; i < newSize; i++) {
				steps[i] = new ClosureStep();
			}
		}

		void reset() {
			depth = 0;
		}

		ClosureStep current() { return steps[depth]; }

		/** Go up one level */
		void ascend() { depth--; }
		/** Go down one level and store frameId and 0-pos as step data */
		void descend(int frameId) { depth++; current().reset(frameId, 0); }
	}

	static final class Anchor {
		/** Frame to step into */
		TreeFrame frame;
		/** Surrounding frame in which the node was anchored. */
		TreeFrame parent;
		/** Return position for the {@link TreeConn} node. */
		int pos = UNSET_INT;
		/** The global index the node has been anchored to. */
		int index = UNSET_INT;

		void reset() {
			frame = parent = null;
			pos = index = UNSET_INT;
		}
	}

	/**
	 * Utility class to track and accumulate information about (a portion of) the
	 * state machine.
	 * <p>
	 * Properties of this class are organized into upstream and downstream information,
	 * depending on how they are used:
	 * <p>
	 * <i>downstream</i> information is accumulated in a forward way and provided to
	 * child nodes by their "parents". In other words, nodes read this information
	 * before modifying the {@link TreeInfo} instance passed to them or forwarding it
	 * to subsequent parts of the state machine. Downstream properties are often set
	 * to a new value by specific nodes and then reset to their previousIndex values after
	 * those nodes have studied their subsection of the state machine.
	 * <p>
	 * <i>upstream</i> information is accumulated in a post-order fashion, i.e. backwards
	 * and after nested parts of the state machine have been studied. This is effectively
	 * information obtained from child nodes and passed on to parent nodes.
	 *
	 *
	 *
	 * @author Markus Gärtner
	 *
	 */
	static class TreeInfo implements Cloneable {
		/** Minimum number of elements to be matched by a subtree. (upstream property) */
		int minSize = 0;
		/** Maximum number of elements to be matched by a subtree. (upstream property) */
		int maxSize = 0;
		/** Flag to indicate whether {@link #maxSize} is actually valid. (upstream property) */
		boolean maxValid = true;
		/** Indicates that the state machine corresponding to a sub node is fully deterministic. (upstream property) */
		boolean deterministic = true;
		/** Indicates that parts of the input can be skipped. (upstream property) */
		boolean skip = false;
		/** Indicates that a part of the state machine should stop after a successful match. (downstream property) */
		boolean stopOnSuccess = false;
		/**
		 * Size of the current segment between matching 'save' and 'restore' border nodes.
		 * If no markers are used, this is equal to {@link #minSize}. (downstream property)
		 */
		int segmentSize = 0;
		/**
		 * Accumulated number of nodes in a subtree. Calculated post-order when studying
		 * a search graph. (upstream property)
		 */
		int descendants = 0;
		/** Nesting depth of tree nodes */
		int depth = 0;
		//TODO properly propagate 'depth' for accumulating nodes (Branch, PermInit and Repetition)

		/** Used to track fixed positions or areas. */
		int from, to;

		void reset() {
			minSize = 0;
			maxSize = 0;
			segmentSize = 0;
            maxValid = true;
            deterministic = true;
            skip = false;
            stopOnSuccess = false;
            from = UNSET_INT;
            to = UNSET_INT;
            depth = 0;
            descendants = 0;
		}

		@Override
		public TreeInfo clone() {
			try {
				return (TreeInfo) super.clone();
			} catch (CloneNotSupportedException e) { throw new InternalError(e); }
		}
	}

	/**
	 * Utility class to carry information about a single node in the state machine.
	 * This class mainly exists to expose internal details of the state machine
	 * without making the actual nodes visible to the outside.
	 * <p>
	 * Instances of this class can be freely shared as they are effectively immutable
	 * and not attached to the state machine anymore.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class NodeInfo implements Serializable {

		private static final long serialVersionUID = 4868471479629357556L;

		/** Keys for properties in {@link NodeInfo}. */
		public enum Field {
			MEMBER(Integer.class),
			NODE(Integer.class),
			CACHE(Integer.class),
			SCOPE_BUFFER(Integer.class),
			POSITION_BUFFER(Integer.class),
			PREVIOUS_BUFFER(Integer.class),
			MIN_SIZE(Integer.class),
			MIN_REPETITION(Integer.class),
			MAX_REPETITION(Integer.class),
			/** Number of total matches allowed. */
			LIMIT(Long.class),
			/** Stop search after successful match. */
			STOP(Boolean.class),
			/** Left boundary of a fixed clips. */
			CLIP_FROM(Integer.class),
			/** Right boundary of a fixed clip. */
			CLIP_TO(Integer.class),
			/** Id of a dynamic clip. */
			CLIP(Integer.class),
			/** Id of a border save/restore point. */
			BORDER(Integer.class),
			/** Id of a filter save/restore point. */
			GATE(Integer.class),
			/** Id of a {@link ClosureContext}. */
			CLOSURE(Integer.class),
			/** Id of a permutation context that holds the state and utility information for permutating node lists. */
			PERMUTATION_CONTEXT(Integer.class),
			/** Original index of an atom within the permutation - used for linking. */
			PERMUTATION_INDEX(Integer.class),
			/** Indicates whether or not a permutation element is meant to scan the search space for a match. */
			SCAN(Boolean.class),
			/** Indicates that a node is a reset or restore point for a shared operation. */
			RESET(Boolean.class),
			/** Indicates that a explorative node is allowed to produce zero-width assertions. */
			OPTIONAL(Boolean.class),
			/** Direction indicator for an exhaustive scan. */
			FORWARD(Boolean.class),
			/** Indicator for clips to enable fast skipping of excluded search space for exhaustive exploration. */
			SKIP(Boolean.class),
			/** The actual level of greediness for repetitive nodes. */
			GREEDINESS(QuantifierModifier.class),
			/** Id of an anchor point to store a tree node assignment */
			ANCHOR(Integer.class),
			/** Minimum height of a tree node */
			HEIGHT(Integer.class),
			/** Minimum depth of a tree node */
			DEPTH(Integer.class),
			/** Minimum number of imemdiate children of a tree node */
			CHILDREN(Integer.class),
			/** Minimum number of accumulated descendants of a tree node */
			DESCENDANTS(Integer.class),
			;

			private final Class<?> valueClass;
			private Field(Class<?> valueClass) { this.valueClass = requireNonNull(valueClass); }

			public Class<?> getValueClass() { return valueClass; }
		}

		/** Basic types of nodes used in the state machine. */
		public enum Type {
			/** Special node to provide pre-match filtering. */
			BEGIN,
			/** Empty search node without internal constraints. */
			EMPTY,
			/** Regular search node with internal constraints. */
			SINGLE,
			/** Scan that searches the remaining space exhaustively. */
			SCAN_EXHAUSTIVE,
			/** Scan that stops after first match. */
			SCAN_FIRST,
			/** Branch head to initiate a node disjunction. */
			BRANCH,
			/** Branch tail to wrap up a node disjunction. */
			BRANCH_CONN,
			/** Entry node for a permutation */
			PERMUTATE,
			/** Intermediate proxy node inside a permutation that wraps around the actual atom node. */
			PERMUTATION_ELEMENT,
			/** Permutation tail to link individual atoms to the rest of the state machine. */
			PERMUTATION_CONN,
			/** Final node that manages dispatch of search result. */
			FINISH,
			/** Special node that handles global constraints. */
			GLOBAL_CONSTRAINT,
			/** Fixed marker that adjusts search space. */
			CLIP_FIXED,
			/** Dynamic marker that adjusts search space. */
			CLIP_DYNAMIC,
			/** Left or right boundary of a search space adjustment. */
			BORDER,
			/** Left or right part of duplicate detection for branched markers. */
			FILTER,
			/** Exhaustive negation of an inner atom. */
			NEGATION,
			/** Controlled (open) repetition of an inner atom. */
			REPETITION,
			/** Exhaustive exploration based on the universal quantifier. */
			ALL,
			/** Special node to enforce disjoint results. */
			RESET,
			/** Vertical navigation node to enter a child tree. */
			STEP_INTO,
			/** Vertical reset node to go back to a definite (outer) frame in the tree. */
			STEP_OUT,
			/** Utility node to track the actual invocation of other nodes. */
			CALLBACK,
			/** Exploration node to search an entire subtree. */
			CLOSURE,
			;
		}

		private final int id;
		private final Type type;
		private final int next;
		private final int logicalNext;
		private final String classLabel;
//		private int conn = -1;
		private IntList atoms;
		private Map<Field, Object> properties;

		private static int idOf(Node node) {
			return node==null ? -1 : unwrap(node).id;
		}

		NodeInfo(Node node, Type type) {
			requireNonNull(node);
			checkArgument("Cannot create info for proxy node", !node.isProxy());
			this.type = requireNonNull(type);
			id = node.id;
			checkArgument("Non-proxy node used invalid id: "+id, id!=-1);
			next = idOf(node.next);
			logicalNext = idOf(node.getLogicalNext());
			classLabel = node.getClass().getSimpleName();
		}

		// PUBLIC access methods

		/** The type of the underlying node. */
		public Type getType() { return type; }

		/** Unique id of the underlying node. */
		public int getId() { return id; }

		/** Id of next node in the state machine or {@code -1} if not connected to a next node. */
		public int getNext() { return next; }

		/** Id of the logical next node in the state machine or {@code -1} if no such node exists. */
		public int getLogicalNext() { return logicalNext; }

		/** Label based on the internal class name of the underlying node. */
		public String getClassLabel() { return classLabel; }

//		/** Id of the special internal connection proxy if the underlying node describes a branch. */
//		public int getConn() { return conn; }

		/** List of ids for all the atoms linked to the underlying node. */
		public IntList getAtoms() { return atoms==null ? IntLists.EMPTY_LIST : atoms; }

		/** Returns the number of atoms registered. */
		public int getAtomCount() { return atoms==null ? 0 : atoms.size(); }

		/** Map view on the properties of the underlying node. */
		public Map<Field, Object> getProperties() { return properties==null ? Collections.emptyMap() : properties; }

		@Nullable
		public Object getProperty(Field field) {
			return properties==null ? null : properties.get(field);
		}

		// INTERNAL modification methods

//		NodeInfo conn(Node node) {
//			requireNonNull(node);
//			checkState("Conn already set", this.conn==-1);
//			int id = idOf(node);
//			checkArgument("Invalid conn id: "+id, id!=-1);
//			this.conn = id;
//			return this;
//		}

		NodeInfo atoms(boolean allowNull, Node...nodes) {
			if(atoms==null) {
				atoms = new IntArrayList();
			}

			for(Node node : nodes) {
				if(!allowNull) {
					requireNonNull(node);
				}
				int id = idOf(node);
				if(!allowNull) {
					checkArgument("Invalid atom id: "+id, id!=-1);
				}
				checkArgument("Duplicate atom id: "+id, !atoms.contains(id));
				atoms.add(id);
			}

			return this;
		}

		NodeInfo property(Field field, int value) { return property(field, _int(value)); }
		NodeInfo property(Field field, long value) { return property(field, _long(value)); }
		NodeInfo property(Field field, boolean value) { return property(field, _boolean(value)); }

		NodeInfo property(Field field, Object value) {
			requireNonNull(field);
			requireNonNull(value);
			if(properties==null) {
				properties = new EnumMap<>(Field.class);
			}
			checkArgument("Property already set: "+field, !properties.containsKey(field));
			properties.put(field, value);
			return this;
		}

		@Override
		public String toString() {
			ToStringBuilder builder = ToStringBuilder.create(this);

			builder.add(type.toString());
			builder.add("id", id);
			builder.add("next", next);
			builder.add("logicalNext", logicalNext);
			builder.add("classLabel", classLabel);
			builder.add("atoms", atoms==null ? "<none>" : atoms.toString());

			if(properties!=null) {
				for(Map.Entry<Field, Object> entry : properties.entrySet()) {
					builder.add(entry.getKey().toString(), String.valueOf(entry.getValue()));
				}
			}

			return builder.build();
		}
	}

	/** Traverse the node's sequence via {@link Node#next} till its own actual tail. */
	static Node last(Node n) {
		return lastBefore(n, accept);
	}

	/** Traverse the node's sequence via {@link Node#next} till reaching the designated fence. */
	static Node lastBefore(Node n, Node fence) {
		assert n!=accept : "cannot start with generic accept node";
		while(n.next!=null && n.next!=fence) {
			n = n.next;
		}
		return n;
	}

	/** Traverse and return the size of the node's sequence via {@link Node#next}
	 * till its own actual tail. */
	static int length(Node n) {
		assert n!=accept : "cannot start with generic accept node";
		int size = 1;
		while(n.next!=null && n.next!=accept) {
			n = n.next;
			size++;
		}
		return size;
	}

	/** Unwraps proxy nodes */
	static Node unwrap(Node node) {
		while(node.isProxy()) {
			node = node.next;
		}
		return node;
	}

	/** Implements a generic accept node that keeps track of the last matched index. */
	static abstract class Node {
		Node next = accept;
		final int id;

		Node(int id) { this.id = id; }

		/** Only modifier method. Allows subclasses to customize how connections should be attached.  */
		void setNext(Node next) { this.next = requireNonNull(next); }

		/**
		 *
		 * <p>
		 * The default implementation just accepts the check and marks
		 * the position as {@link State#last}.
		 */
		boolean match(State state, int pos) {
			state.last = pos;
			return true;
		}

		/** Analyze the underlying node graph and return {@code true} iff the matching will be deterministic. */
		boolean study(TreeInfo info) {
			if(next!=null) {
				return next.study(info);
			}

			return info.deterministic;
		}

		@Override
		public String toString() { return ToStringBuilder.create(this).add("id", id).build(); }

		/** Returns {@code true} iff this node can scan the search space itself. */
		boolean isScanCapable() { return false; }

		/** Returns {@code true} iff this node is part of the finishing block of the state machine. */
		boolean isFinisher() { return false; }

		/** Returns {@code true} iff this node is connective bridge for atoms */
		boolean isConnective() { return false; }

		boolean isProxy() { return false; }

		boolean isFixed() { return isProxy() ? getNext().isFixed() : false; }

		boolean isSkipControl() { return false; }

		Node getNext() { return next; }

		void detach() { setNext(accept); }

		/** Utility method to fetch internally linked nodes */
		@Nullable Node[] getAtoms() { return null; }

		/** Returns the next node after this one or {@code null} if the next node is reached indirectly */
		@Nullable Node getLogicalNext() { return isFinisher() ? null : next; }

		/**
		 * Returns a sharable {@link NodeInfo} object that describes this node
		 * or {@code null} if this node is a proxy or the generic accept node.
		 */
		public abstract NodeInfo info();
	}

	static final int GREEDY = QuantifierModifier.GREEDY.id();
	static final int RELUCTANT = QuantifierModifier.RELUCTANT.id();
	static final int POSSESSIVE = QuantifierModifier.POSSESSIVE.id();

	static final class Track extends Node {

		Track(Node next) {
			super(-1);
			checkArgument("Cannot monitor generic accept node", next!=accept);
			setNext(next);
		}

		@Override
		public NodeInfo info() {return null; }

		@Override
		boolean match(State state, int pos) {
			final Monitor monitor = state.monitor;
			final Node node = next;

			// If we have an actual monitor, do proper tracking
			if(monitor!=null) {
				monitor.enterNode(node, state, pos);
				final boolean result = node.match(state, pos);
				monitor.exitNode(node, state, pos, result);
				return result;
			}

			// Otherwise just delegate to real node
			return node.match(state, pos);
		}

		@Override
		boolean isProxy() { return true; }

		@Override
		public String toString() {
			return next.toString();
		}
	}

	static abstract class ProperNode extends Node implements Comparable<ProperNode> {
		final IqlQueryElement source;
		ProperNode(int id, IqlQueryElement source) {
			super(id);
			this.source = requireNonNull(source);
		}
		@Override
		public int compareTo(ProperNode o) { return Integer.compare(id, o.id); }

		public IqlQueryElement getSource() { return source; }

		//TODO once the state machine is implemented, add monitoring methods
//		abstract void append(StringBuilder sb);
	}

	/** Helper for "empty" nodes that are only existentially quantified. */
	static final class Empty extends ProperNode {
		final int memberId;
		final int anchorId;

		Empty(int id, IqlQueryElement source, int memberId, int anchorId) {
			super(id, source);
			this.memberId = memberId;
			this.anchorId = anchorId;
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.EMPTY)
					.property(Field.MEMBER, memberId)
					.property(Field.ANCHOR, anchorId);
		}

		@Override
		boolean match(State state, int pos) {
			final TreeFrame frame = state.frame;
			// Ensure existence
			if(!frame.containsPos(pos)) {
				return false;
			}

			final int index = frame.childAt(pos);

			// Bail on locked index
			if(state.locked[index]) {
				return false;
			}

			// Ensure adjacent matching if desired
			if(frame.previousIndex!=UNSET_INT && frame.previousIndex!=index-1) {
				return false;
			}

			// We match every node, so no extra check needed to refresh 'previousIndex'
			frame.previousIndex = index;

			// Store member mapping so that other constraints can reference it
			if(memberId!=UNSET_INT) {
				state.members[memberId].assign(state.elements[pos]);
			}
			// Store tree anchor
			if(anchorId!=UNSET_INT) {
				Anchor anchor = state.anchors[anchorId];
				anchor.parent = frame;
				anchor.index = index;
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
			info.descendants++;
			info.minSize++;
			info.maxSize++;
			info.segmentSize++;
			return info.deterministic;
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("memberId", memberId)
					.add("anchorId", anchorId)
					.build();
		}
	}

	/** Intermediate helper to filter out target sequences that are too short */
	static final class Begin extends Node {
		int minSize;

		Begin(int id) { super(id); }

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.BEGIN)
					.property(Field.MIN_SIZE, minSize);
		}

		@Override
		boolean match(State state, int pos) {
			if(state.frame.length < minSize) {
				return false;
			}
			return next.match(state, pos);
		}

		@Override
		boolean study(TreeInfo info) {
			next.study(info);
			minSize = info.minSize;
			// A nested negation allows for effective zero-width query!
//			checkState("Minimum size of sequence must be greater than or equal 1", minSize>0);
			return info.deterministic;
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this).add("minSize", minSize).build();
		}
	}

	/**
	 * Implements a final accept node that verifies that all existentially
	 * quantified nodes have been matched and which records the entire match
	 * as a result.
	 */
	static final class Finish extends Node {
		final long limit;
		final boolean stopAfterMatch;

		Finish(int id, long limit, boolean stopAfterMatch) {
			super(id);
			this.limit = limit;
			this.stopAfterMatch = stopAfterMatch;
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.FINISH)
					.property(Field.LIMIT, limit)
					.property(Field.STOP, stopAfterMatch);
		}

		@Override
		boolean match(State state, int pos) {
			state.dispatchMatch();

			state.reported++;
			if(limit!=UNSET_LONG && state.reported>=limit) {
				state.finished = true;
			}
			state.stop = stopAfterMatch || state.finished;
			state.last = pos;

			return true;
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this).add("limit", limit).build();
		}

		@Override
		boolean isFinisher() { return true; }
	}

	/** Proxy for evaluating the global constraints. */
	static final class GlobalConstraint extends ProperNode {

		GlobalConstraint(int id, IqlConstraint source) {
			super(id, source);
		}

		@Override
		public NodeInfo info() { return new NodeInfo(this, Type.GLOBAL_CONSTRAINT); }

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

		@Override
		boolean isFinisher() { return true; }
	}

	static abstract class Clip extends ProperNode {
		final boolean clipIndex;

		boolean skip = false;

		Clip(int id, IqlMarkerCall source, boolean clipIndex) {
			super(id, source);
			this.clipIndex = clipIndex;
		}

		@Override
		boolean match(State state, int pos) {
			final TreeFrame frame = state.frame;
			final Interval interval = interval(state);
			// Update search interval and bail early if we fail
			if((clipIndex && !frame.retainIndices(interval))
					//TODO split index-based (global) markers and positional (local) markers, refresh the latter inside a Border node that starts a marker graph
					|| (!clipIndex && !frame.retainPos(interval))) {
				return false;
			}
			// Skip ahead if allowed
			if(pos<frame.from() && skip && state.isSet(MODE_SKIP)) {
				pos = frame.from();
			}
			// Early bail in case we're outside of allowed search space
			if(!frame.containsPos(pos)) {
				return false;
			}
			// Continue with actual search
			return next.match(state, pos);
		}

		abstract Interval interval(State state);

		void study0(TreeInfo info) {
			boolean skip0 = info.skip;
			info.skip = false;
			next.study(info);
			skip = info.skip;
			info.skip = skip0;

			// Cascade skip flag through intersecting markers
			if(next instanceof Clip) {
				skip = ((Clip)next).skip;
			}
		}
	}

	static final class FixedClip extends Clip {
		final Interval region;

		FixedClip(int id, IqlMarkerCall source, boolean clipIndex, int from, int to) {
			super(id, source, clipIndex);
			checkArgument("Invalid interval begin", from>=0);
			checkArgument("Invalid interval end", to>=0);
			checkArgument("Empty interval", from<=to);
			region = Interval.of(from, to);
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.CLIP_FIXED)
					.property(Field.CLIP_FROM, region.from)
					.property(Field.CLIP_TO, region.to)
					.property(Field.SKIP, skip);
		}

		@Override
		boolean isFixed() { return true; }

		@Override
		Interval interval(State state) { return region; }

		@Override
		boolean study(TreeInfo info) {
			study0(info);

			info.from = region.from;
			info.to = region.to;

			return info.deterministic;
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("from", region.from)
					.add("to", region.to)
					.add("skip", skip)
					.add("clipIndex", clipIndex)
					.build();
		}
	}

	/** Interval filter based on raw intervals in the matcher */
	static final class DynamicClip extends Clip {
		final int intervalIndex;
		DynamicClip(int id, IqlMarkerCall source, boolean clipIndex, int intervalIndex) {
			super(id, source, clipIndex);
			this.intervalIndex = intervalIndex;
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.CLIP_DYNAMIC)
					.property(Field.CLIP, intervalIndex)
					.property(Field.SKIP, skip);
		}

		@Override
		boolean isFixed() { return false; }

		@Override
		Interval interval(State state) { return state.intervals[intervalIndex]; }

		@Override
		boolean study(TreeInfo info) {
			study0(info);

			return info.deterministic;
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("intervalIndex", intervalIndex)
					.add("skip", skip)
					.add("clipIndex", clipIndex)
					.build();
		}
	}

	/** Save- or restore-point for the right interval boundary during marker constructs */
	static final class Border extends Node {
		final boolean save;
		final int borderId;
		final int[] nestedMarkers;

		/** Create a border-start node that saves the window and potentially refreshes nested markers */
		Border(int id, int borderId, int[] nestedMarkers) {
			super(id);
			this.save = true;
			// Ignore empty array
			this.nestedMarkers = nestedMarkers.length==0 ? null : nestedMarkers;
			this.borderId = borderId;
		}

		/** Create a border-end node that restores a previously saved window state */
		Border(int id, int borderId) {
			super(id);
			this.save = false;
			this.nestedMarkers = null;
			this.borderId = borderId;
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.BORDER)
					.property(Field.BORDER, borderId)
					.property(Field.RESET, !save);
		}

		@Override
		boolean isFixed() { return next.isFixed(); }

		@Override
		boolean match(State state, int pos) {
			if(save) {
				state.borders[borderId] = state.frame.to();

				// Refresh nested markers based on current frame
				if(nestedMarkers!=null) {
					final int size = state.frame.length;
					for(int markerIndex : nestedMarkers) {
						state.nestedMarkers[markerIndex].adjust(state.intervals, size);
					}
				}

			} else {
				state.frame.to(state.borders[borderId]);
			}

			return next.match(state, pos);
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("save", save)
					.add("borderId", borderId)
					.build();
		}

		/** Delegates to {@link Node#next} node, since we're only a proxy. */
		@Override
		boolean isFinisher() { return next.isFinisher(); }

		/**
		 * Adjust the {@link TreeInfo#segmentSize} so that it cuts off at
		 * segment boundaries dictated by border nodes.
		 */
		@Override
		boolean study(TreeInfo info) {
			int segL = info.segmentSize;
			info.segmentSize = 0;
			next.study(info);
			if(!save) {
				info.segmentSize = segL;
			}
			return info.deterministic;
		}
	}

	/** Either resets a {@link Gate} buffer or checks if a match position is still allowed. */
	static final class Filter extends Node {
		final boolean reset;
		final int gateId;
		/**
		 * @param reset
		 * @param gateId
		 */
		public Filter(int id, boolean reset, int gateId) {
			super(id);
			this.reset = reset;
			this.gateId = gateId;
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.FILTER)
					.property(Field.GATE, gateId)
					.property(Field.RESET, reset);
		}

		@Override
		boolean isFixed() { return next.isFixed(); }

		/** Tries  */
		@Override
		boolean match(State state, int pos) {
			final Gate gate = state.gates[gateId];
			if(reset) {
				// Make all slots available again
				gate.clear();
			} else if(!gate.visit(pos)) {
				return false;
			}

			return next.match(state, pos);
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("reset", reset)
					.add("gateId", gateId)
					.build();
		}

		/** Delegates to {@link Node#next} node, since we're only a proxy. */
		@Override
		boolean isFinisher() { return next.isFinisher(); }
	}

	/** Special scan that ensures no instance of atom appears before legal end of sequence/match. */
	static final class Negation extends ProperNode {
		int minSize = 0; // 0 means we have to check till end of sequence
		final Node atom;
		final int cacheId;

		public Negation(int id, IqlQuantifier source, int cacheId, Node atom) {
			super(id, source);
			this.cacheId = cacheId;
			this.atom = requireNonNull(atom);
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.NEGATION)
					.property(Field.MIN_SIZE, minSize)
					.property(Field.CACHE, cacheId)
					.atoms(false, atom);
		}

		@Override
		boolean isFixed() { return atom.isFixed(); }

		@Override
		boolean isScanCapable() { return true; }

		@Override
		Node[] getAtoms() { return new Node[] {atom}; }

		@Override
		boolean match(State state, int pos) {
			if(minSize==0) {
				return matchFull(state, pos);
			}
			return matchWithTail(state, pos);
		}

		private boolean matchFull(State state, int pos) {
			final TreeFrame frame = state.frame;
    		final int from = frame.from();
    		final int to = frame.to();
			final Cache cache = state.caches[cacheId];
			final int fence = frame.length-1;

			for (int i = pos; i <= fence; i++) {
				frame.previousIndex = UNSET_INT;
				int scope = state.scope();
				boolean stored = cache.isSet(i);
				boolean matched;
				if(stored) {
					matched = cache.getValue(i);
				} else {
					// Previously unseen index, so explore and cache result
					matched = !atom.match(state, i);
					cache.setValue(i, matched);
				}

				state.resetScope(scope);
				frame.resetWindow(from, to);
				if(!matched) {
					return false;
				}
			}

			return next.match(state, frame.length-1);
		}

		private boolean matchWithTail(State state, int pos) {
			final TreeFrame frame = state.frame;
    		final int from = frame.from();
    		final int to = frame.to();
			final Cache cache = state.caches[cacheId];
			final int fence = to - minSize + 1;
			final int previous = frame.previousIndex;

			boolean result = false;

			for (int i = pos; i <= fence && !state.stop; i++) {
				frame.previousIndex = UNSET_INT;
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
						state.resetScope(scope);
						continue;
					}
				} else {
					// Previously unseen index, so explore and cache result
					matched = !atom.match(state, pos) && next.match(state, i);
					cache.setValue(i, matched);
				}

				result |= matched;

				state.resetScope(scope);
				frame.resetWindow(from, to);
			}

			frame.previousIndex = previous;

			return result;
		}

		@Override
		boolean study(TreeInfo info) {
			int minSize0 = info.minSize;
			next.study(info);
			minSize = info.minSize-minSize0;

			info.deterministic = false;

			info = new TreeInfo();
			atom.study(info);
			checkState("Nested atom must not contain zero-width assertion!", info.minSize>0);

			return false;
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("id", id)
					.add("minSize", minSize)
					.add("cacheId", cacheId)
					.add("atom", atom)
					.build();
		}
	}

	/** Special scan that implements universal quantification. */
	static final class All extends ProperNode {
		final Node atom;
		int minSize;

		public All(int id, IqlQuantifier source, Node atom) {
			super(id, source);
			this.atom = requireNonNull(atom);
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.ALL)
					.atoms(false, atom);
		}

		@Override
		boolean isFixed() { return atom.isFixed(); }

		@Override
		boolean isScanCapable() { return true; }

		@Override
		Node[] getAtoms() { return new Node[] {atom}; }

		@Override
		boolean match(State state, int pos) {
			final TreeFrame frame = state.frame;
			final int last = frame.to()-minSize+1;
			final int previous = frame.previousIndex;
			// Visit all elements of initial search window!
			boolean result = true;
			while (pos <=last) {
				frame.previousIndex = UNSET_INT;
				// mismatch or zero-width assertion
				if(!atom.match(state, pos) || pos==state.last) {
					result = false;
					break;
				}
				pos = state.last;
			}
			frame.previousIndex = previous;
			if(!result) {
				return false;
			}
			return next.match(state, last+1);
		}

		@Override
		boolean study(TreeInfo info) {

			TreeInfo tmp = new TreeInfo();
			atom.study(tmp);
			minSize = Math.max(1, tmp.minSize);

			//TODO implement a flag in TreeInfo to pass down info to atom nodes that no result mapping is desired
			info.deterministic = false;
			info.skip = true;

			return next.study(info);
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("id", id)
					.add("atom", atom)
					.build();
		}
	}

	/** Utility node to track whether a portion of the SM actually got executed. */
	static class Callback extends Node {
		boolean called = false;

		Callback(int id) { super(id); }

		@Override
		public NodeInfo info() { return new NodeInfo(this, Type.CALLBACK); }

		/**
		 * Default implementation only sets the {@link #called} flag and then
		 * delegates to the {@link #getNext() next} node for actual matching.
		 * <p>
		 * Subclasses should make sure that when they override this method the
		 * flag gets properly set or they call {@code super.match(state, pos)}
		 * as part of the execution!
		 */
		@Override
		boolean match(State state, int pos) {
			called = true;
			return next.match(state, pos);
		}

		@Override
		public String toString() { return ToStringBuilder.create(this).toString(); }
	}

	/** Matches an inner constraint to a specific node, employing memoization. */
	static final class Single extends ProperNode {
		final int nodeId;
		final int cacheId;
		final int memberId;
		final int anchorId;

		Single(int id, IqlNode source, int nodeId, int cacheId, int memberId, int anchorId) {
			super(id, source);
			this.nodeId = nodeId;
			this.cacheId = cacheId;
			this.memberId = memberId;
			this.anchorId = anchorId;
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.SINGLE)
					.property(Field.NODE, nodeId)
					.property(Field.CACHE, cacheId)
					.property(Field.MEMBER, memberId)
					.property(Field.ANCHOR, anchorId);
		}

		@Override
		boolean match(State state, int pos) {
			final TreeFrame frame = state.frame;

			if(!frame.containsPos(pos)) {
				return false;
			}

			final int index = frame.childAt(pos);

			// Bail on locked index
			if(state.locked[index]) {
				return false;
			}

			// Ensure adjacent matching if desired
			if(frame.previousIndex!=UNSET_INT && frame.previousIndex!=index-1) {
				return false;
			}

			final Cache cache = state.caches[cacheId];

			boolean value;

			if(cache.isSet(index)) {
				value = cache.getValue(index);
			} else {
				// Unknown index -> compute local constraints once and cache result
				final Matcher<Item> m = state.matchers[nodeId];
				assert m!=null : "Null matcher at node-id "+nodeId;
				final Item item = state.elements[index];
				assert item!=null : "Null item at index "+index;
				value = m.matches(index, item);
				cache.setValue(index, value);
			}

			if(value) {
				// Keep track of preliminary match
				state.map(nodeId, index);

				// Store member mapping so that other constraints can reference it
				if(memberId!=UNSET_INT) {
					state.members[memberId].assign(state.elements[index]);
				}
				// Store tree anchor
				if(anchorId!=UNSET_INT) {
					Anchor anchor = state.anchors[anchorId];
					anchor.parent = frame;
					anchor.index = index;
				}

				frame.previousIndex = index;

				// Continue down the path
				value = next.match(state, pos+1);

				// Ensure we don't keep item references
				if(memberId!=UNSET_INT) {
					state.members[memberId].clear();
				}

				if(value) {
					// Store last successful match
					state.hits[nodeId] = index;
				}
			}

			return value;
		}

		@Override
		boolean study(TreeInfo info) {
			next.study(info);
			info.descendants++;
			info.minSize++;
			info.maxSize++;
			info.segmentSize++;
			return info.deterministic;
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("id", id)
					.add("nodeId", nodeId)
					.add("cacheId", cacheId)
					.add("memberId", memberId)
					.add("anchorId", anchorId)
					.build();
		}
	}

	/**
	 * Implements a forward-only one-shot exploration of the remaining
	 * search space. In contrast to {@link Exhaust} this type of scan will
	 * not reset the current mapping after a matching attempt, but rather
	 * finish the search after the first successful full match.
	 */
	static class Find extends Node {
		int minSize = 1;
		boolean optional;

		Find(int id) { super(id); }

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.SCAN_FIRST)
					.property(Field.MIN_SIZE, minSize)
					.property(Field.OPTIONAL, optional);
		}

		@Override
		boolean isScanCapable() { return true; }

		@Override
		void setNext(Node next) {
			assert !unwrap(next).isScanCapable() : "Cannot chain scan nodes";
			super.setNext(next);
		}

		@Override
		boolean match(State state, int pos) {
			final TreeFrame frame = state.frame;

			// Short-cut for zero-width assertion
			if(optional && pos==frame.to()+1) {
				return next.match(state, pos);
			}

    		final int from = frame.from();
    		final int to = frame.to();
			final int fence = to - minSize + 1;

			boolean result = false;

			for (int i = pos; i <= fence && !state.stop; i++) {
				frame.previousIndex = UNSET_INT;
				int scope = state.scope();
				result |= next.match(state, i);
				frame.resetWindow(from, to);

				// We are only interested in the first successful match
				if(result) {
					break;
				}

				// Only reset if we failed to find a match (space boundaries are reset above)
				state.resetScope(scope);
			}

			return result;
		}

		@Override
		boolean study(TreeInfo info) {
			int minSize0 = info.segmentSize;
			next.study(info);
			minSize = info.segmentSize-minSize0;
			optional = minSize==0;

			/* For scanning an optional inner atom behaves similar to a single
			 * node of size 1, as in either case we are going to scan till the last
			 * position in the current search space.
			 */
			minSize = Math.max(minSize, 1);

			info.deterministic = false;
			info.skip = true;

			return false;
		}
	}

	/**
	 * Implements the exhaustive exploration of remaining search space
	 * by iteratively scanning for matches of the current tail. In addition
	 * to the basic forward-only search of {@link Find} this implementation
	 * also offers the ability to scan backwards and also uses (optional) caching.
	 */
	static final class Exhaust extends Find {
		final int cacheId;
		final boolean forward;

		Exhaust(int id, int cacheId, boolean forward) {
			super(id);
			this.cacheId = cacheId;
			this.forward = forward;
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.SCAN_EXHAUSTIVE)
					.property(Field.MIN_SIZE, minSize)
					.property(Field.OPTIONAL, optional)
					.property(Field.CACHE, cacheId)
					.property(Field.FORWARD, forward);
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("forward", forward)
					.add("cacheId", cacheId)
					.add("minSize", minSize)
					.build();
		}

		@Override
		boolean match(State state, int pos) {

			// Short-cut for zero-width assertion
			if(optional && pos==state.frame.to()+1) {
				return next.match(state, pos);
			}

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
			final TreeFrame frame = state.frame;
    		final int from = frame.from();
    		final int to = frame.to();
			final int fence = to - minSize + 1;

			boolean result = false;

			for (int i = pos; i <= fence && !state.stop; i++) {
				frame.previousIndex = UNSET_INT;
				int scope = state.scope();
				result |= next.match(state, i);
				state.resetScope(scope);
				frame.resetWindow(from, to);
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
			final TreeFrame frame = state.frame;
    		final int from = frame.from();
    		final int to = frame.to();
			final Cache cache = state.caches[cacheId];
			final int fence = to - minSize + 1;

			boolean result = false;

			for (int i = pos; i <= fence && !state.stop; i++) {
				frame.previousIndex = UNSET_INT;
				final int scope = state.scope();
				final int index = frame.indices[i];
				final boolean stored = cache.isSet(index);
				boolean matched;
				if(stored) {
					matched = cache.getValue(index);
					if(matched) {
						// Continue matching, but we know it will succeed
						//TODO re-evaluate this in the context of complex SM graphs (subgraph could still fail?)
						next.match(state, i);
					} else {
						// We know this is a dead-end, so skip
						state.resetScope(scope);
						continue;
					}
				} else {
					// Previously unseen index, so explore and cache result
					matched = next.match(state, i);
					cache.setValue(index, matched);
				}

				result |= matched;

				state.resetScope(scope);
				frame.resetWindow(from, to);
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
			final TreeFrame frame = state.frame;
    		final int from = frame.from();
    		final int to = frame.to();
			boolean result = false;

			for (int i = to - minSize + 1; i >= pos && !state.stop; i--) {
				frame.previousIndex = UNSET_INT;
				int scope = state.scope();
				result |= next.match(state, i);
				state.resetScope(scope);
				frame.resetWindow(from, to);
			}

			return result;
		}
	}

	/** Head node for a permutation */
	static final class PermInit extends ProperNode {
		/** Raw list of elements to shuffle */
		final Node[] atoms;
		/** Reference to the {@link Permutator} instance */
		final int permId;
		/** Minimum length of individual nodes */
		final int[] minSizes;
		/** Flag to signal if we are allowed to scan at all (disabled for ADJACENT sequences) */
		final boolean scan;

		/** Minimum length of SM tail after this permutation*/
		int minSize;
		/** Only look for first successful hit. Used by repitition nodes. */
		boolean stopOnSuccess;

		static final int NO_SKIP = Integer.MAX_VALUE;

		PermInit(int id, IqlQueryElement source, int permId, boolean scan, Node[] atoms) {
			super(id, source);
			this.permId = permId;
			this.scan = scan;
			checkArgument("Need at least 2 elements", atoms.length>1);
			this.atoms = atoms;
			minSizes = new int[atoms.length];
		}

		/**
		 * @see de.ims.icarus2.query.api.engine.matcher.SequencePattern.Node#info()
		 */
		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.PERMUTATE)
					.atoms(false, atoms)
					.property(Field.PERMUTATION_CONTEXT, permId)
					.property(Field.STOP, stopOnSuccess)
					.property(Field.MIN_SIZE, minSize);
		}

		@Override
		boolean isSkipControl() { return true; }

		@Override
		boolean study(TreeInfo info) {

			stopOnSuccess = info.stopOnSuccess;

			TreeInfo tmp = new TreeInfo();

			for (int i = 0; i < atoms.length; i++) {
				// Pass on downstream properties
				tmp.stopOnSuccess = info.stopOnSuccess;

				Node atom = atoms[i];
				atom.study(tmp);

				minSizes[i] = tmp.minSize;
				info.minSize += tmp.minSize;
				info.maxSize += tmp.maxSize;
				info.maxValid &= tmp.maxValid;
				info.deterministic &= tmp.deterministic;
				info.segmentSize += tmp.segmentSize;

				tmp.reset();
			}

			int minSize0 = info.segmentSize;
			next.study(info);
			minSize = info.segmentSize-minSize0;

			return info.deterministic;
		}

		@Override
		boolean match(State state, int pos) {
			final PermutationContext ctx = state.permutations[permId];

			final TreeFrame frame = state.frame;
    		final int from = frame.from();
    		final int to = frame.to();
        	final int scope = state.scope();
        	final int previous = frame.previousIndex;
    		boolean result = false;

			while(!state.stop) {
				// Apply current permutation and calculate boundaries
				prepare(ctx, to);

				// Let atoms do their job
				state.setMode(MODE_SKIP, false);
				boolean matched = ctx.current[0].match(state, pos);
				state.resetMode(MODE_SKIP);

				result |= matched;

	        	// Only reset range here
				frame.resetWindow(from, to);

				/*
				 * We might have failed due to one of the permutated nodes.
				 * If so, check skip index and try to skip permutations. If
				 * skipping fails, we need to bail completely.
				 */
				if(!matched && ctx.skip!=NO_SKIP && !ctx.source.skip(ctx.skip)) {
					break;
				}

                if(stopOnSuccess && result) {
                	break;
                }

	        	// Only reset if we failed or are not meant to keep the first match
	        	state.resetScope(scope);
	        	frame.previousIndex = previous;

				// Bail as soon as permutations are exhausted
				if(!ctx.source.next()) {
					break;
				}
			}

			ctx.reset();

			return result;
		}

		private void prepare(PermutationContext ctx, int to) {

			final int[] config = ctx.source.current();
    		final int last = atoms.length-1;

			// Build forward links
			for (int i = 0; i <= last; i++) {
				int slot = config[i];
				ctx.slots[slot] = i;
				ctx.next[slot] = i==last ? next : atoms[config[i+1]];
				ctx.current[i] = atoms[config[i]];
			}

//			System.out.printf("perm %s -> %s%n", Arrays.toString(config), Arrays.toString(ctx.current));

			// Update lookups
			int fence = to - minSize - Math.max(1, minSizes[config[last]]) + 1;
			ctx.fences[config[last]] = fence;
			// Accumulate fences back to front
			for (int i = last-1; i >= 0; i--) {
				int slot = config[i];
				fence -= minSizes[slot];
				ctx.fences[slot] = fence;
			}

    		ctx.skip = NO_SKIP;
    		ctx.scan = scan;
		}

		@Override
		Node[] getAtoms() { return atoms.clone(); }

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("id", id)
					.add("minSize", minSize)
					.add("permId", permId)
					.add("stopOnSuccess", stopOnSuccess)
					.add("atoms", Arrays.toString(atoms))
					.add("minSizes", Arrays.toString(minSizes))
					.build();
		}
	}

	/** Multiplexer for an individual slot in the permutation */
	static final class PermSlot extends Node {
		/** Reference to the {@link Permutator} instance */
		final int permId;
		/** Position of this wrapper node in the permutation */
		final int atomIndex;
		/** Only look for first successful hit. Used by repitition nodes. */
		boolean stopOnSuccess;

		PermSlot(int id, int permId, int atomIndex) {
			super(id);
			this.permId = permId;
			this.atomIndex = atomIndex;
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.PERMUTATION_ELEMENT)
					.property(Field.PERMUTATION_CONTEXT, permId)
					.property(Field.PERMUTATION_INDEX, atomIndex);
		}

		@Override
		boolean isSkipControl() { return true; }

		@Override
		boolean isScanCapable() { return true; }

		@Override
		boolean study(TreeInfo info) {

			stopOnSuccess = info.stopOnSuccess;

			next.study(info);

            /*
             *  We generally support skipping, but have to determine at search time
             *  if we actually enable or disable it for individual nodes depending
             *  on the current permutation.
             */
            info.skip = true;

			return info.deterministic;
		}

		@Override
		boolean match(State state, int pos) {
			final PermutationContext ctx = state.permutations[permId];

			boolean result = false;

			/*
			 *  Allow skipping inside the permutation (PermInit node takes
			 *  care of disabling it for the first slot) depending on context.
			 */
			state.setMode(MODE_SKIP, ctx.scan);

			// We can either search iteratively for the next match
			if(ctx.scan && ctx.slots[atomIndex]>0) {
				final TreeFrame frame = state.frame;
	    		final int from = frame.from();
	    		final int to = frame.to();
	    		final int fence = ctx.fences[atomIndex];

				for (int i = pos; i <= fence && !state.stop; i++) {
					frame.previousIndex = UNSET_INT;
					int scope = state.scope();
					// Local match result
					boolean matched = matchAtom(state, i, ctx);

					result |= matched;

					frame.resetWindow(from, to);

	                if(stopOnSuccess && result) {
	                	break;
	                }

					state.resetScope(scope);
				}

			} else {
				// Or only try a single index, due to an outer scan
				result = matchAtom(state, pos, ctx);
			}

			state.resetMode(MODE_SKIP);

			return result;
		}

		private boolean matchAtom(State state, int pos, PermutationContext ctx) {
			ctx.used[atomIndex] = false;
			final boolean result = next.match(state, pos);
			final int slot = ctx.slots[atomIndex];
			if(!result && ctx.used[atomIndex] && slot < ctx.skip) {
				// Otherwise update the skip index
				ctx.skip = slot;
			}
			return result;
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("id", id)
					.add("permId", permId)
					.add("atomIndex", atomIndex)
					.build();
		}
	}

	/**
     * Guard node at the end of each permutation atom to block the {@link #study(TreeInfo)}
     * chain but forward the {@link #match(State, int)} call to 'next'.
     */
	static final class PermConn extends Node {
		/** Id of the permutation context instance */
		final int permId;
		/** Index of the atom this perm-conn acts as tail */
		final int atomIndex;

		PermConn(int id, int permId, int atomIndex) {
			super(id);

			this.permId = permId;
			this.atomIndex = atomIndex;
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.PERMUTATION_CONN)
					.property(Field.PERMUTATION_CONTEXT, permId)
					.property(Field.PERMUTATION_INDEX, atomIndex);
		}

        @Override
		boolean match(State state, int pos) {
			final PermutationContext ctx = state.permutations[permId];
			// Mark slot as used
			ctx.used[atomIndex] = true;
			// COntinue matching with next slot
			return ctx.next[atomIndex].match(state, pos);
        }

        @Override
		boolean study(TreeInfo info) {
            return info.deterministic;
        }

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("id", id)
					.add("permId", permId)
					.add("atomIndex", atomIndex)
					.build();
		}
	}

	/**
     * Guard node at the end of each branch to block the {@link #study(TreeInfo)}
     * chain but forward the {@link #match(State, int)} call to 'next'. This implementation
     * will only forward part of the {@link #study(TreeInfo)} call at most <b>once</b>
     * in order to gather information it needs to pass onto preceding nodes.
     */
    static final class BranchConn extends Node {
    	boolean skip = false;
    	boolean skipSet = false;
        BranchConn(int id) { super(id); }

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.BRANCH_CONN)
					.property(Field.SKIP, skip);
		}

        @Override
		boolean match(State state, int pos) {
            return next.match(state, pos);
        }

        @Override
		boolean study(TreeInfo info) {
        	// Check once if we need to pass the 'skip' flag up the hierarchy
        	if(!skipSet) {
        		TreeInfo sentinel = new TreeInfo();
        		next.study(sentinel);
        		skip = sentinel.skip;
        		skipSet = true;
        	}
        	info.skip = skip;
            return false;
        }

        @Override
        boolean isConnective() { return true; }

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("id", id)
					.add("skip", skip)
					.add("slot", skipSet)
					.build();
		}
    }

    /**
	 * Models multiple alternative paths. Can also be used to model
	 * {@code 0..1} (greedy) and {@code 0..1?} (reluctant) ranged
	 * quantifiers.
	 */
	static final class Branch extends ProperNode {
		final BranchConn conn;
		final Node[] atoms;
		boolean stopOnSuccess;

		Branch(int id, IqlQueryElement source, BranchConn conn, Node...atoms) {
			// 'source' can be either IqlMarkerExpression or IqlElementDisjunction
			super(id, source);
			checkArgument("Need at least 2 branch atoms", atoms.length>1);
			this.atoms = atoms;
			this.conn = conn;
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.BRANCH)
					.property(Field.STOP, stopOnSuccess)
					.atoms(true, atoms);
		}

		@Override
		boolean isFixed() {
			for(Node atom : atoms) {
				if(!atom.isFixed()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * We need to both re-route this node AND the branch-conn to the given
		 * 'next' connector. Otherwise nested branching would result in disconnected
		 * branch-conn nodes, making it impossible for inner branching constructs
		 * to ever produce matches.
		 */
		@Override
		void setNext(Node next) {
			super.setNext(next);
			conn.setNext(next);
		}

		@Override
		Node getLogicalNext() { return null; }

		@Override
		Node[] getAtoms() { return atoms.clone(); }

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("id", id)
					.add("atoms", Arrays.toString(atoms))
					.build();
		}

		@Override
		boolean match(State state, int pos) {
			final TreeFrame frame = state.frame;
    		final int from = frame.from();
    		final int to = frame.to();
        	final int scope = state.scope();
        	final int previous = frame.previousIndex;

    		boolean result = false;
	        for (int n = 0; n < atoms.length && !state.stop; n++) {
	        	final Node atom = atoms[n];
	        	if (atom==null) {
	            	// zero-width path
	                result |= conn.next.match(state, pos);
	            } else {
	                result |= atom.match(state, pos);
	            }
	        	// Only reset range here
	        	frame.resetWindow(from, to);

                if(stopOnSuccess && result) {
                	break;
                }

	        	// Only reset if we failed or are not meant to keep the first match
	        	state.resetScope(scope);
	        	frame.previousIndex = previous;
	        }
	        return result;
		}

		@Override
		boolean study(TreeInfo info) {

			stopOnSuccess = info.stopOnSuccess;

			TreeInfo tmp = info.clone();

			info.reset();
			int minL2 = Integer.MAX_VALUE; // we only operate in int space here anyway
			int maxL2 = -1;
			int segL = Integer.MAX_VALUE;
			int descL = Integer.MAX_VALUE;

			for (int n = 0; n < atoms.length; n++) {
				Node atom = atoms[n];
				if (atom == null) {
					continue;
				}
				// This will cause "conn" node to forward study call at most once!
				atom.study(info);
				minL2 = Math.min(minL2, info.minSize);
				maxL2 = Math.max(maxL2, info.maxSize);
				segL = Math.min(segL, info.segmentSize);
				descL = Math.min(descL, info.descendants);
				tmp.maxValid &= info.maxValid;
				info.reset();
			}

			tmp.minSize += minL2;
			tmp.maxSize += maxL2;
			tmp.segmentSize += segL;
			tmp.descendants += descL;

			conn.next.study(info);

			info.minSize += tmp.minSize;
			info.maxSize += tmp.maxSize;
			info.descendants += tmp.descendants;
			info.segmentSize += tmp.segmentSize;
			info.maxValid &= tmp.maxValid;
			info.deterministic = false;

			return false;
		}
	}

	static final class Repetition extends ProperNode {
    	final int cmin;
    	final int cmax;
    	final Node atom;
    	final Find findAtom;
    	final int type;
    	final int scopeBuf;
    	final int posBuf;
    	final int prevBuf;

		Repetition(int id, IqlQuantifier source, Node atom, int cmin, int cmax, int type,
				int scopeBuf, int posBuf, int prevBuf, int findId) {
			super(id, source);
			this.atom = atom;
			this.cmin = cmin;
			this.cmax = cmax;
			this.type = type;
			this.scopeBuf = scopeBuf;
			this.posBuf = posBuf;
			this.prevBuf = prevBuf;
			if(findId!=-1) {
				findAtom = new Find(findId);
				findAtom.setNext(atom);
			} else {
				findAtom = null;
			}
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.REPETITION)
					.property(Field.MAX_REPETITION, cmax)
					.property(Field.MIN_REPETITION, cmin)
					.property(Field.GREEDINESS, QuantifierModifier.forId(type))
					.property(Field.SCOPE_BUFFER, scopeBuf)
					.property(Field.POSITION_BUFFER, posBuf)
					.property(Field.PREVIOUS_BUFFER, prevBuf)
					.atoms(false, atom);
		}

		@Override
		boolean isFixed() { return atom.isFixed(); }

		@Override
		Node[] getAtoms() { return new Node[] {findAtom==null ? atom : findAtom}; }

		@Override
		boolean match(State state, int pos) {
        	// Save state for entire match call
        	int scope = state.scope();

        	boolean matched = true;

        	// Try to match minimum number of repetitions
            int count;
            for (count = 0; count < cmin; count++) {
                if (!matchAtom(state, pos, count)) {
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
            	state.resetScope(scope);
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
        	final TreeFrame frame = state.frame;
			int backLimit = count;
			// Stores scope handles for reset when backing off
			int[] b_scope = state.buffers[scopeBuf];
			// Stores matcher positions for backing off
			int[] b_pos = state.buffers[posBuf];
			// Stores value of frame.previous for backing off
			int[] b_prev = state.buffers[prevBuf];
			// We are greedy so match as many as we can
			while (count<cmax) {
				// Keep track of scope and position
				int scope = state.scope();
				b_pos[count] = pos;
				b_scope[count] = scope;
				b_prev[count] = frame.previousIndex;
				// Try advancing
				if(!matchAtom(state, pos, count)) {
					state.resetScope(scope);
					break;
				}
				 // Zero length match
				if (pos == state.last) {
					state.resetScope(scope);
					break;
				}
				// Move up index and number matched
				pos = state.last;
				count++;
			}

			// Options for 'atom' exhausted, now try matching our tail

			// Handle backing off if tail match fails
			for (;;) {
				if (next.match(state, pos)) {
					return true;
				}
				// Can't backtrack further
				if(count==backLimit) {
					break;
				}
				// Need to backtrack one more step
				count--;
				pos = b_pos[count];
				frame.previousIndex = b_prev[count];
				state.resetScope(b_scope[count]);
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
        	final TreeFrame frame = state.frame;
			for (;;) {
                // Try finishing match without consuming any more
				final int from = frame.from();
				final int to = frame.to();
				int scope = state.scope();
				final int previous = frame.previousIndex;
				if (next.match(state, pos)) {
					return true;
				}
				state.resetScope(scope);
				frame.resetWindow(from, to);
				frame.previousIndex = previous;
                // At the maximum, no match found
				if (count >= cmax) {
					return false;
				}
                // Okay, must try one more atom
				scope = state.scope();
				if (!matchAtom(state, pos, count)) {
					state.resetScope(scope);
					return false;
				}
                // If we haven't moved forward then must break out
				// zero-width atom match
				if (pos == state.last) {
					state.resetScope(scope);
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
				if (!matchAtom(state, pos, count)) {
					state.resetScope(scope);
					break;
				}
				// zero-width atom match
				if (pos == state.last) {
					state.resetScope(scope);
					break;
				}
                // Move up index and number matched
				pos = state.last;
				count++;
			}
			return next.match(state, pos);
        }

        private boolean matchAtom(State state, int pos, int count) {
        	if(count==0 || findAtom==null) {
        		return atom.match(state, pos);
        	}
        	return findAtom.match(state, pos);
        }

        @Override
		boolean study(TreeInfo info) {
            // Save original info
            int minL = info.minSize;
            int maxL = info.maxSize;
            int segL = info.segmentSize;
            int descL = info.descendants;
            boolean maxV = info.maxValid;
            boolean detm = info.deterministic;
            boolean stopOnSuccess = info.stopOnSuccess;
            info.reset();

            // Ensure we don't get any full explorative nodes as atoms
            info.stopOnSuccess = true;
            if(findAtom==null) {
            	atom.study(info);
            } else {
            	findAtom.study(info);
            }

            int temp = info.minSize * cmin + minL;
            if (temp < minL) {
                temp = Integer.MAX_VALUE; // we only operate in int space here anyway
            }
            info.minSize = temp;

            temp = info.segmentSize * cmin + segL;
            if (temp < segL) {
                temp = Integer.MAX_VALUE; // we only operate in int space here anyway
            }
            info.segmentSize = temp;

            temp = info.descendants * cmin + descL;
            if (temp < descL) {
                temp = Integer.MAX_VALUE; // we only operate in int space here anyway
            }
            info.descendants = temp;

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

            // Study remaining part
            info.stopOnSuccess = stopOnSuccess;
            next.study(info);

            // Nodes with static scanning act as barriers against skipping
            info.skip = false;

            return info.deterministic;
        }

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("id", id)
					.add("cmin", cmin)
					.add("cmax", cmax)
					.add("atom", atom)
					.add("type", type)
					.add("scopeBuf", scopeBuf)
					.add("posBuf", posBuf)
					.add("prevBuf", prevBuf)
					.build();
		}
    }

	/** Enforces disjoint match results */
	//TODO rethink this approach when we move to trees
	static final class Reset extends Node {

		Reset(int id) { super(id); }

		//TODO maybe set "stopOnSuccess" flag in study method?

		@Override
		public NodeInfo info() { return new NodeInfo(this, Type.RESET); }

		@Override
		boolean match(State state, int pos) {
			final TreeFrame frame = state.frame;
			final int from = frame.from();
			final int to = frame.to();

			boolean result = false;

			while(!state.finished && frame.containsPos(pos)) {
				// Bail as soon as a new search fails
				if(!next.match(state, pos)) {
					break;
				}
				result = true;
				frame.resetWindow(from, to);
				// Remove entire length of match from search space
				pos = state.last;
				// Reset stop signal so exploration can have another try
				state.stop = false;
			}

			return result;
		}

		@Override
		boolean study(TreeInfo info) {
			info.stopOnSuccess = true;
			return next.study(info);
		}
	}

	/**
	 * Steps down into the child nodes of previously anchored node.
	 * <p>
	 * This node replaces the current {@link State#frame}
	 * and starts a new matching process at the beginning of the new
	 * frame.
	 */
	static final class Tree extends ProperNode {
		/** Pointer to the anchor slot to fetch the node index that we step into */
		final int anchorId;
		/** Nested part of the automaton */
		final Node child;
		/** Connection of {@code child} to rest of state machine  */
		final TreeConn conn;


		int height = UNSET_INT;
		int depth = UNSET_INT;
		int descendants = UNSET_INT;
		int children = UNSET_INT;
		/** If set we can continue even if node doesn't have a subtree */
		boolean optional = false;

		Tree(int id, IqlQueryElement source, int anchorId, Node child, TreeConn conn) {
			super(id, source);
			this.anchorId = anchorId;
			this.child = requireNonNull(child);
			this.conn = requireNonNull(conn);
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.STEP_INTO)
					.property(Field.ANCHOR, anchorId)
					.atoms(false, child)
					.property(Field.HEIGHT, height)
					.property(Field.DEPTH, depth)
					.property(Field.CHILDREN, children)
					.property(Field.DESCENDANTS, descendants);
		}

		@Override
		void setNext(Node next) {
			super.setNext(next);
			conn.setNext(next);
		}

		@Override
		Node[] getAtoms() { return new Node[] {child}; }

		@Override
		boolean match(State state, int pos) {
			final TreeFrame oldFrame = state.frame;
			final Anchor anchor = state.anchors[anchorId];
			assert anchor.index != UNSET_INT : "Illegal index for frame: "+anchor.index;

			final TreeFrame newFrame = state.tree[anchor.index];
			assert newFrame.valid : "Invalid tree frame for index "+anchor.index;

			// Bail early if basic (upper) tree properties aren't met
			if(newFrame.length < children || newFrame.depth < depth) {
				return false;
			}

			// Skip costly vertical movement if there's no subtree and 'optional' flag is set
			if(optional && newFrame.length==0) {
				return conn.next.match(state, pos);
			}

			// Bail early if basic (lower) tree properties aren't met
			if(newFrame.height < height || newFrame.descendants < descendants) {
				return false;
			}

			// Step into new frame
			newFrame.reset();
			state.frame = newFrame;

			// Store our return position and frame for the TreeConn node
			anchor.pos = pos;
			anchor.frame = newFrame;

			// Start again at beginning of the (nested) frame
			boolean result = child.match(state, 0);

			// Finally reset frame and anchor data
			state.frame = oldFrame;
			anchor.reset();

			return result;
		}

		@Override
		boolean study(TreeInfo info) {
			depth = info.depth;

			// Study subtree in isolation
			TreeInfo tmp = new TreeInfo();
			tmp.depth = info.depth + 1;
			child.study(tmp);

			height = tmp.depth - depth;
			children = tmp.minSize;
			descendants = tmp.descendants;

			// Enable fast-path option for matching in case of missing subtree
			optional = children==0;

			conn.next.study(info);

			info.descendants += tmp.descendants;
			info.deterministic &= tmp.deterministic;
			info.depth = tmp.depth;

			return info.deterministic;
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("anchorId", anchorId)
					.add("child", child)
					.add("height", height)
					.add("depth", depth)
					.add("children", children)
					.add("descendants", descendants)
					.build();
		}
	}

	/**
	 * Steps out of a previously anchored child node into the old frame to continue the
	 * state machine and then resets back to the child frame.
	 */
	static final class TreeConn extends ProperNode {
		/** Pointer to the anchor slot to fetch the node index that we step into */
		final int anchorId;

		TreeConn(int id, IqlQueryElement source, int anchorId) {
			super(id, source);
			this.anchorId = anchorId;
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.STEP_OUT)
					.property(Field.ANCHOR, anchorId);
		}

		@Override
		boolean match(State state, int pos) {
			final Anchor anchor = state.anchors[anchorId];
			assert anchor.pos != UNSET_INT : "illegal return position for anchor id: "+anchorId;
			assert anchor.parent != null : "missing return frame for anchor id: "+anchorId;
			assert anchor.frame != null : "missing nested frame for anchor id: "+anchorId;

			// Apply previously savedanchor data
			state.frame = anchor.parent;
			pos = anchor.pos;

			boolean result = next.match(state, pos);

			// Reset back to child frame (needed for e.g. scans)
			state.frame = anchor.frame;

			return result;
		}

		/** Block any studying to reach the outer tree */
		@Override
		boolean study(TreeInfo info) {
			return info.deterministic;
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("anchorId", anchorId)
					.build();
		}

        @Override
        boolean isConnective() { return true; }

		@Override
		Node getLogicalNext() { return next.isConnective() ? next : null; }
	}

	/**
	 * Explorative node that will continue tail of the matcher graph for
	 * the currently active frame and <b>all</b> of its descendants.
	 */
	static final class TreeClosure extends ProperNode {
		/** Key to fetch the {@link ClosureContext} for this node */
		final int closureId;
		/** */
		final int cacheId;
		/** Tracker to determine whether the subtree actually got fully invoked */
		final Callback callback;

		boolean stopOnSuccess;

		TreeClosure(int id, IqlMarkerCall source, int closureId, int cacheId, Callback callback) {
			super(id, source);
			this.closureId = closureId;
			this.cacheId = cacheId;
			this.callback = requireNonNull(callback);
		}

		@Override
		public NodeInfo info() {
			return new NodeInfo(this, Type.CLOSURE)
					.property(Field.CLOSURE, closureId)
					.property(Field.CACHE, cacheId)
					.property(Field.STOP, stopOnSuccess);
		}

		@Override
		boolean match(State state, int pos) {
			//TODO determine what mode we need to use for traversal
			final TreeFrame root = state.frame;
			final ClosureContext ctx = state.closures[closureId];
			final Cache cache = state.caches[cacheId];

			/*
			 * We run a pre-order traversal on the subtree with
			 * local trace so that we don't blow up the Java stack.
			 */

			// 0-depth frame, match in current context
			boolean result = next.match(state, pos);

			// Prepare first child frame
			ctx.depth = 1;
			ctx.current().reset(root.index, pos);

			while(!state.stop && ctx.depth>0) {
				ClosureStep step = ctx.current();

				TreeFrame frame = state.tree[step.frameId];
				assert frame!=state.rootFrame : "Cannot descend _into_ root frame";

				// Try remaining nodes in current frame
				if(step.pos < frame.length) {
					// Assign current frame context
					state.frame = frame;

					// And start a search from current position
					final int index = frame.indices[step.pos];
		    		final int from = frame.from();
		    		final int to = frame.to();
		        	final int scope = state.scope();
		        	final int previous = frame.previousIndex;

		        	// Reset callback tracking
		        	callback.called = false;

					boolean stored = cache.isSet(index);
					boolean matched = false;
					if(stored) {
						matched = cache.getValue(index);
						if(matched) {
							/*
							 * This is a tricky situation:
							 * We know the node itself will match, but since we
							 * can be embedded into a more complex construct such
							 * as a permutation, the tail can still fail!
							 * Therefore we must not simply accept success, but
							 * actually use the result of the subtree matching.
							 */
							matched = next.match(state, step.pos);
						}
						// We know this is a dead-end, nothing further to do there
					} else {
						// Previously unseen index, so explore and cache result
						matched = next.match(state, step.pos);
						// We only store the "local" success of next node
						cache.setValue(index, callback.called);
					}

					result |= matched;

		        	// Only reset range here
		        	frame.resetWindow(from, to);

	                if(stopOnSuccess && result) {
	                	break;
	                }

		        	// Only reset if we failed or are not meant to keep the first match
		        	state.resetScope(scope);
		        	frame.previousIndex = previous;

					// Continue to next neighbor for future traversal
					step.pos++;
					// Now descend if possible
					if(frame.length > 0) {
						ctx.descend(frame.lowest());
					}
				}
				// Subtree exhausted, ascend and move on
				else {
					ctx.ascend();
					ctx.current().pos++;
				}
			}

			// Reset back to old frame
			state.frame = root;

			return result;
		}

		@Override
		boolean study(TreeInfo info) {
			stopOnSuccess = info.stopOnSuccess;
			info.deterministic = false;
			next.study(info);
			return info.deterministic;
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("closureId", closureId)
					.add("stopOnSuccess", stopOnSuccess)
					.build();
		}
	}
}
