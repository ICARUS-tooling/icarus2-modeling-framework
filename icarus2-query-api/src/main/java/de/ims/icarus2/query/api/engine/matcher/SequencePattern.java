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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
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
import de.ims.icarus2.query.api.iql.IqlElement.IqlSet;
import de.ims.icarus2.query.api.iql.IqlExpression;
import de.ims.icarus2.query.api.iql.IqlMarker;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerCall;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerExpression;
import de.ims.icarus2.query.api.iql.IqlMarker.MarkerExpressionType;
import de.ims.icarus2.query.api.iql.IqlPayload.MatchFlag;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryModifier;
import de.ims.icarus2.query.api.iql.IqlQuantifier;
import de.ims.icarus2.query.api.iql.IqlQuantifier.QuantifierModifier;
import de.ims.icarus2.query.api.iql.IqlUtils;
import de.ims.icarus2.query.api.iql.NodeArrangement;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.strings.ToStringBuilder;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
	static Node accept = new Node() {
		@Override
		public String toString() { return "Accept-Dummy"; }

		@Override
		void setNext(Node next) { throw new UnsupportedOperationException("Generic accept node is immutable"); }

		@Override
		boolean isFinisher() { return true; }
	};

	static final Supplier<Node> none = () -> accept;

	private final AtomicInteger matcherIdGen = new AtomicInteger(0);

	/** The IQL source of structural constraints for this matcher. */
	private final IqlElement source;
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
	//TODO add arguments to delegate result buffer, dispatch output and attach monitoring
	public SequenceMatcher matcher() {
		int id = matcherIdGen.getAndIncrement();

		return new SequenceMatcher(setup, id);
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
		/** Total number of border savepoints */
		int borderCount = 0;
		/** Total number of gates that prevent duplicate position matching */
		int gateCount = 0;
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
		Gate[] makeGates() {
			return IntStream.range(0, gateCount)
				.mapToObj(i -> new Gate())
				.toArray(Gate[]::new);
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
		Supplier<Matcher<Container>> filter;
		Supplier<Expression<?>> global;
		ElementContext context;
		ExpressionFactory expressionFactory;
//		IqlMarker pendingMarker;
//		int pendingBorder = UNSET_INT;
//		/** Keeps track of stacked single nodes between scans */
//		int offset = 0;
		boolean findOnly = false;

		int id;

		final LaneContext rootContext;
		final QueryModifier modifier;
		final List<IqlNode> nodes = new ArrayList<>();
		final List<Interval> intervals = new ArrayList<>();
		final List<NodeDef> matchers = new ArrayList<>();
		final List<MemberDef> members = new ArrayList<>();
		final Set<ProperNode> properNodes = new ReferenceOpenHashSet<>();
		final List<Node> sm = new ArrayList<>();
		final List<RangeMarker> markers = new ArrayList<>();
		final IntList markerPos = new IntArrayList();
		final long limit;
		final IqlElement rootElement;
		final IqlConstraint filterConstraint;
		final IqlConstraint globalConstraint;
		final Set<MatchFlag> flags;

		final Monitor monitor;
		final Function<IqlNode, IqlNode> nodeTransform;
		final boolean cacheAll;

		final Stack<Frame> stack = new ObjectArrayList<>();

		/** Traverse the node's sequence via {@link Node#next} till its own actual tail. */
		private static Node last(Node n) {
			assert n!=accept : "cannot start with generic accept node";
			while(n.next!=null && n.next!=accept) {
				n = n.next;
			}
			return n;
		}

		/** Traverse and return the size of the node's sequence via {@link Node#next}
		 * till its own actual tail. */
		private static int length(Node n) {
			assert n!=accept : "cannot start with generic accept node";
			int size = 1;
			while(n.next!=null && n.next!=accept) {
				n = n.next;
				size++;
			}
			return size;
		}

		private static enum Flag {
			/** Signals that the segment is accompanied by a disjunctive marker,
			 * requiring scans to be moved outside the prefix section. */
			COMPLEX_MARKER,

			@Deprecated
			SIMPLE_MARKER,
			;
		}

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

			private void checkNotDefault(Node n) {
				checkArgument("Generic accept node not supported here", n!=requireNonNull(accept));
			}

			private void maybeIncNodes(Node n) {
				if(n instanceof Single || n instanceof Empty) {
					nodes++;
				}
			}

			/** Replaces the inner sequence with the given one */
			<N extends Node> N replace(N node) {
				requireNonNull(node);
				start = node;
				end = last(node);
				start = node;
				maybeIncNodes(node);
				size = length(node);
				return node;
			}

			/** Add given node as head of inner sequence. */
			<N extends Node> N push(N node) {
				requireNonNull(node);
				if(end==accept) {
					start = node;
					end = last(node);
				} else {
					last(node).setNext(start);
					start = node;
				}
				maybeIncNodes(node);
				size += length(node);
				return node;
			}

			/** Add given node as tail of inner sequence. */
			<N extends Node> N append(N node) {
				requireNonNull(node);
				if(end==accept) {
					start = node;
					end = last(node);
				} else {
					end.setNext(node);
					end = last(node);
				}
				maybeIncNodes(node);
				size += length(node);
				return node;
			}

			void replace(Segment other) {
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
					size += other.size;
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
					size += other.size;
					mergeFlags(other);
				}
			}

			Frame toFrame() {
				Frame frame = new Frame();
				frame.replace(this);
				return frame;
			}
		}

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

			/** Prepends the prefix if present and appends the suffix if present. */
			void collapse() {
				checkNotEmpty();
				if(prefix!=null) {
					push(prefix);
					prefix = null;
				}
				if(suffix!=null) {
					append(suffix);
					suffix = null;
				}
			}

			private void replace(Frame other) {
				super.replace(other);
				prefix = other.prefix;
				suffix = other.suffix;
			}

			/**
			 * Add other frame as head to this one (if this is empty, replace all content instead):
			 * <p>
			 * Append other's prefix if present.
			 * Push other's suffix if present and this is empty.
			 * Push other's content.
			 */
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
		}

		SequenceQueryProcessor(Builder builder) {
			rootContext = builder.geContext();
			modifier = builder.getModifier();
			flags = builder.geFlags();
			limit = builder.getLimit();
			rootElement = builder.getRoot();
			filterConstraint = builder.getFilterConstraint();
			globalConstraint = builder.getGlobalConstraint();

			monitor = builder.getMonitor();
			nodeTransform = builder.getNodeTransform();
			cacheAll = builder.isCacheAll();
		}

//		private Frame frame() { return stack.top(); }
//		private Frame startFrame(boolean atom) {
//			Frame frame = new Frame(atom);
//			stack.push(frame);
//			return frame;
//		}
//		private void endFrame(Frame frame) {
//			checkState("Frame stack corrupted", frame==stack.top());
//		}

		private boolean flagSet(MatchFlag flag) { return flags.contains(flag); }

		/** Process element as independent frame */
		private Frame process(IqlElement source) {
			switch (source.getType()) {
			case GROUPING: return grouping((IqlGrouping) source);
			case SET: return sequence((IqlSet) source);
			case NODE: return node((IqlNode) source);
			// Only disjunction inherits the 'adjacency' property from surrounding context
			case DISJUNCTION: return disjunction((IqlElementDisjunction) source);

			default:
				// We do not support IqlTreeNode and IqlEdge here!!
				throw EvaluationUtils.forUnsupportedQueryFragment("element", source.getType());
			}
		}

		// RAW ELEMENT PROCESSING

		//FIXME currently we might distort the legal intervals when shifting them to the left

		/** Process single node */
		private Frame node(IqlNode source) {

			if(nodeTransform!=null) {
				source = requireNonNull(nodeTransform.apply(source), "Node transformation fail");
			}

			final List<IqlQuantifier> quantifiers = source.getQuantifiers();
			final IqlMarker marker = source.getMarker().orElse(null);
			final IqlConstraint constraint = source.getConstraint().orElse(null);
			final String label = source.getLabel().orElse(null);

			final Frame frame = new Frame();

			/*
			 * We don't handle markers immediately at the node position they
			 * appear, but on the first enclosing  explorative node, which
			 * currently are only the Exhaust or Find nodes. This way the state machine
			 * can properly and early reduce the search space for exploration.
			 * This also allows us to treat markers on multiple (nested) nodes
			 * in an ADJACENT sequence as erroneous.
			 */
			if(marker!=null) {
//				if(pendingMarker!=null)
//					throw new QueryException(QueryErrorCode.INCORRECT_USE,
//							"Unresolved pending marker, this usually happens when using the "
//							+ "'ADJACENT' modifier with multiple marker-bearing nodes. Affected node:\n"
//							+ serialize(source));

				int border = border();
				// Creates the marker window
				frame.prefix().push(marker(marker));
				// Saves the previous window
				frame.prefix().push(border(true, border)); //TODO implement a way of adding conditional parts of the frame
				// Restores previous window
				frame.suffix().append(border(false, border));
			}

			Segment atom;

			// Process actual node content
			if(constraint==null) {
				// Dummy nodes don't get added to the "proper nodes" list
				atom = segment(empty(label));
			} else {
				// Full fledged node with local constraints and potentially a member label

				nodes.add(source);
				// Prepare context and expression processing
				context = rootContext.derive()
						.element(source)
						.build();
				expressionFactory = new ExpressionFactory(context);
				atom = segment(single(label, constraint));
				// Reset context
				expressionFactory = null;
				context = null;
			}

			// Handle quantifiers
			if(!quantifiers.isEmpty()) {
				atom = quantify(atom, quantifiers);
			}

			frame.push(atom);

			return frame;
		}

		/** Process (quantified) node group */
		private Frame grouping(IqlGrouping source) {
			final List<IqlQuantifier> quantifiers = source.getQuantifiers();

			boolean oldFindOnly = findOnly;
			//FIXME we need to either adjust the specification or branch here for collections of mixed continuous and discontinuous quantifiers!
			if(!findOnly && !allowExhaustive(quantifiers)) {
				findOnly = true;
			}

			// Make sure to process the group content as a detached atom
			final Frame frame = looseGroup(source.getElements());

			findOnly = oldFindOnly;

			// Ensure we don't have any border nodes dangling around
			if(!frame.isSingleton()) {
				frame.collapse();
			}

			// Finally apply quantification
			frame.replace(quantify(frame, quantifiers));

			return frame;
		}

		/** Process (ordered or adjacent) node sequence and link to active sequence */
		private Frame sequence(IqlSet source) {
			final List<IqlElement> elements = source.getElements();
			if(source.getArrangement()==NodeArrangement.ADJACENT) {
				return adjacentGroup(elements);
			}
			return looseGroup(elements);
		}

		/** Process alternatives and link to active sequence */
		private Frame disjunction(IqlElementDisjunction source) {
			final List<IqlElement> elements = source.getAlternatives();
			return branch(elements.size(), false, i -> process(elements.get(i))).toFrame();
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

		private int id() { return id++; }

		private <N extends Node> N store(N node) {
			//TODO wrap into monitoring node if needed
			sm.add(node);
			if(node instanceof ProperNode) {
				properNodes.add((ProperNode) node);
			}
			return node;
		}

		private Node storeReplaceable(Node node) {
			store(node);
			if(node instanceof ProperNode && monitor!=null) {
				node = new Track(node, monitor);
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
				//TODO re-evaluate if we can actually use caching when marker is present
				scan = exhaust(forward, cached ? cache() : UNSET_INT);
			}

			return scan;
		}

		/** Create graph for the marker construct and attach to tail. */
		private Segment marker(IqlMarker marker) {
			switch (marker.getType()) {
			case MARKER_CALL: {
				IqlMarkerCall call = (IqlMarkerCall) marker;
				return range(marker(call));
			}

			case MARKER_EXPRESSION: {
				IqlMarkerExpression expression = (IqlMarkerExpression) marker;
				List<IqlMarker> items = expression.getItems();
				if(expression.getExpressionType()==MarkerExpressionType.CONJUNCTION) {
					return intersection(items);
				}
				return union(items);
			}

			default:
				throw EvaluationUtils.forUnsupportedQueryFragment("marker", marker.getType());
			}

		}

		private Segment range(RangeMarker marker) {
			if(marker.isDynamic()) {
				final int intervalIndex = interval(marker);
				final int count = marker.intervalCount();
				if(count>1) {
					return branch(count, false, i -> frame(clip(intervalIndex+i)));
				}

				return segment(clip(intervalIndex));
			}

			assert marker.intervalCount()==1 : "static marker cannot have multiple intervals";

			final Interval interval = Interval.blank();
			marker.adjust(new Interval[] {interval}, 0, 1);
			return segment(fixed(interval.from, interval.to));
		}

		/** Create single fixed clip */
		private Fixed fixed(int from, int to) {
			return store(new Fixed(from, to));
		}

		/** Create single dynamic clip */
		private DynamicClip clip(int intervalIndex) {
//			if(shift!=0) {
//				return new ShiftedClip(intervalIndex);
//			}
			return store(new DynamicClip(intervalIndex));
		}

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
		private Segment intersection(List<IqlMarker> markers) {
			assert markers.size()>1 : "Need 2+ markers for intersection";
			Segment seg = new Segment();
			markers.stream()
					.map(this::marker)
					.sorted(SEGMENT_COMPLEXITY_ORDER)
					.forEach(seg::append);
			return seg;
		}

		/** Create branches for disjunctive markers */
		private Segment union(List<IqlMarker> markers) {
			assert markers.size()>1 : "Need 2+ markers for union";
			final Segment seg = branch(markers.size(), false, i -> marker(markers.get(i)).toFrame());
			seg.setFlag(Flag.COMPLEX_MARKER);
			final int gateId = gate();
			seg.push(filter(true, gateId));
			seg.append(filter(false, gateId));
			return seg;
		}

		private int cache() { return cacheCount++; }

		private int buffer() { return bufferCount++; }

		private int border() { return borderCount++; }

		private int gate() { return gateCount++; }

		private RangeMarker marker(IqlMarkerCall call) {
			Number[] arguments = IntStream.range(0, call.getArgumentCount())
					.mapToObj(call::getArgument)
					.map(Number.class::cast)
					.toArray(Number[]::new);
			return SequenceMarker.of(call.getName(), arguments);
		}

		/** Push intervals for given marker on the stack and return index of first interval */
		private int interval(RangeMarker marker) {
			final int markerIndex = markers.size();
			markers.add(marker);
			final int intervalIndex = intervals.size();
			markerPos.add(markerIndex);
			final int count = marker.intervalCount();
			for (int i = 0; i < count; i++) {
				intervals.add(Interval.blank());
			}
//			if(shift>0) {
//				return shiftLeft(intervalIndex, count, shift);
//			}
			return intervalIndex;
		}

		/** Create referential intervals for a group of source intervals, using a specific shift. */
		@Deprecated
		private int shiftLeft(int sourceIndex, int count, int shift) {
			final int intervalIndex = intervals.size();
			for (int i = 0; i < count; i++) {
				intervalRef(sourceIndex+i, shift);
			}
			return intervalIndex;
		}

		@Deprecated
		private int intervalRef(int targetIndex, int shift) {
			final int intervalIndex = intervals.size();
			final Interval source = intervals.get(targetIndex);
			if(source instanceof IntervalRef) {
				intervals.add(new IntervalRef((IntervalRef)source, shift));
			} else {
				intervals.add(new IntervalRef(targetIndex, shift));
			}
			return intervalIndex;
		}

		private Node empty(@Nullable String label) {
			return storeReplaceable(new Empty(member(label)));
		}

		private Begin begin() { return store(new Begin()); }

		/** Make a utility node that either saves or restores a border point  */
		private Border border(boolean save, int borderId) { return store(new Border(save, borderId)); }

		private Filter filter(boolean reset, int gateId) { return store(new Filter(reset, gateId)); }

		private Finish finish(long limit, boolean stopAfterMatch) {
			return store(new Finish(limit, stopAfterMatch, monitor));
		}

		private Reset reset() { return store(new Reset()); }

		private Node single(@Nullable String label, IqlConstraint constraint) {
			return storeReplaceable(new Single(id(), matcher(constraint), cache(), member(label)));
		}

		private Node find() {
			return storeReplaceable(new Find(id()));
		}

		private Node exhaust(boolean forward, int cacheId) {
			return storeReplaceable(new Exhaust(id(), cacheId, forward));
		}

		private Node negate(Node atom) {
			return storeReplaceable(new Negation(id(), cache(), atom));
		}

		private Node all(Node atom) {
			return storeReplaceable(new All(id(), atom));
		}

		private Segment branch(int count, boolean stopOnSuccess, IntFunction<Frame> atomGen) {
			List<Node> atoms = new ArrayList<>();
			BranchConn conn = store(new BranchConn());
			// For consistency we collect branches in reverse order
			for (int i = count-1; i >=0; i--) {
				// Might be a complex sub-structure
				Frame atom = atomGen.apply(i);
				// Link atom to connection node
				atom.suffix().append(conn);
				// Ensure entire atom is one sequence
				atom.collapse();
				atoms.add(atom.start());
			}
			// Need to revert order so that priority is preserved
			Collections.reverse(atoms);
			return segment(storeReplaceable(new Branch(id(), stopOnSuccess, conn,
					atoms.toArray(new Node[atoms.size()]))));
		}

		private Node repetition(Node atom, int cmin, int cmax, int mode, boolean discontinuous) {
			return storeReplaceable(new Repetition(id(), atom, cmin, cmax, mode,
					buffer(), buffer(), discontinuous ? id() : UNSET_INT));
		}

		private boolean needsCacheForScan(Node node) {
			if(node instanceof Single) {
				// If we have a complex structure AFTER the node, ensure caching!
				return !node.next.isFinisher();
			}
			// Lone single nodes never need external caching
			return false;
		}

		/**
		 * Sequential scanning of ordered elements.
		 * Note that any node but the first in the original
		 * sequence may receive a scan attached to it.
		 * The first node in the sequence might have its prefix hoisted.
		 */
		private Frame looseGroup(List<IqlElement> elements) {
			if(elements.size()==1) {
				return process(elements.get(0));
			}

			Frame frame = new Frame();
			// Stack nodes back to front
			int last = elements.size()-1;
			for(int i=last; i>=0; i--) {
				Frame step = process(elements.get(i));
				// Special handling only for subsequent steps
				if(i>0) {
					Node head = step.start();
					// Any node but the first can receive a scan attached to it
					if(!head.isScanCapable()) {
						// Cashing will be used either for complex inner structure or intermediate nodes
						step.push(explore(true, needsCacheForScan(head) || i<last));
					}
					// Ensure we don't mix up hoistable content and proactively collapse
					step.collapse();
				}
				// Accumulate steps into single sequence
				frame.push(step);
			}
			return frame;
		}

		/** Sequential check without scanning */
		private Frame adjacentGroup(List<IqlElement> elements) {
			Frame frame = new Frame();
			// Stack nodes back to front
			for(int i=elements.size()-1; i>=0; i--) {
				Frame step = process(elements.get(i));
				if(i>0) {
					step.collapse();
				}
				frame.push(step);
			}
			return frame;
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
				return segment(quantify(atom.start(), quantifiers.get(0)));
			} else {
				// Combine all quantifiers into a branch structure
				return branch(quantifiers.size(), false, i -> frame(quantify(atom.start(), quantifiers.get(i))));
			}
		}

		/** Wraps a quantification around atoms */
		private Node quantify(Node atom, IqlQuantifier quantifier) {
			Node node;
			if(quantifier.isExistentiallyNegated()) {
				node = negate(atom);
			} else if(quantifier.isUniversallyQuantified()) {
				node = all(atom);
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
					node = repetition(atom, min, max, mode, quantifier.isDiscontinuous());
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

			final boolean disjoint = flagSet(MatchFlag.DISJOINT);

			//TODO for now we don't honor the 'consumed' flag on IqlElement instances
			final Frame frame = process(rootElement);

			// Prepare top-level scan if needed
			if(!frame.start().isScanCapable()) {

				/*
				 *  If we have complex disjunctive markers, we need to take the
				 *  expensive route and move them inside the scan. Otherwise we'd
				 *  create a situation where the marker nodes "hide" legal search
				 *  space from the scan and we are missing out on positive matches.
				 */
				if(frame.hasPrefix() && frame.prefix().isFlagSet(Flag.COMPLEX_MARKER)) {
					frame.collapse();
				}

				frame.push(explore(modifier!=QueryModifier.LAST, false));
			}

			// Collapse all actual content before we add special nodes
			frame.collapse();

			// Global constraints get evaluated after all normal content but before dispatch phase
			if(globalConstraint != null) {
				expressionFactory = new ExpressionFactory(rootContext);
				global = new ExpressionDef(constraint(globalConstraint), rootContext);
				expressionFactory = null;
				frame.append(new GlobalConstraint(id()));
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
			sm.properNodes = properNodes.stream().sorted().toArray(ProperNode[]::new);
			sm.filterConstraint = filter;
			sm.globalConstraint = global;
			sm.nodes = nodes.toArray(new IqlNode[0]);
			sm.limit = limit;
			sm.root = root;
			sm.properNodes = properNodes.stream().sorted().toArray(ProperNode[]::new);
			sm.cacheCount = cacheCount;
			sm.borderCount = borderCount;
			sm.bufferCount = bufferCount;
			sm.gateCount = gateCount;
			sm.markerPos = markerPos.toIntArray();
			sm.intervals = intervals.toArray(new Interval[0]);
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
		/** All the gate caches for keeping trck of duplicate matcher positions */
		final Gate[] gates;
		/** Positions into 'intervals' to signal what intervals to update for what marker */
		final int[] markerPos;
		/** Positions of referential intervals */
		final int[] intervalRefs;
		/** Keeps track of the last hit index for every raw node */
		final int[] hits;
		/** The available int[] buffers used by various node implementations */
		final int[][] buffers;
		/** Stores the right boundary around marker interval operations */
		final int[] borders;

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
		 * Set by the Finish node if result limit is exceeded or the search is using
		 * DISJOINT mode. Nodes that iteratively explore the search space should use this
		 * flag as indicator. When using DISJOINT mode the Reset node will clear this flag
		 * after each match and adjust the search space to exclude already exhausted areas.
		 */
		boolean stop;

		/**
		 * End index of the last (sub)match, used by repetitions and similar nodes to keep track.
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
			borders = setup.getBorders();

			filterConstraint = setup.makeFilterConstraint();
			globalConstraint = setup.makeGlobalConstraint();

			matchers = setup.makeMatchers();
			members = setup.makeMembers();
			caches = setup.makeCaches();
			intervals = setup.makeIntervals();
			buffers = setup.makeBuffer();
			gates = setup.makeGates();

			intervalRefs = refs(intervals);

			from = 0;
			to = 0;
		}

		private static int[] refs(Interval[] intervals) {
			IntList indices = new IntArrayList();
			for (int i = 0; i < intervals.length; i++) {
				if(intervals[i] instanceof IntervalRef) {
					indices.add(i);
				}
			}
			return indices.toIntArray();
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
			for (int i = 0; i < caches.length; i++) {
				caches[i].reset(size);
			}
			entry = 0;
			last = 0;
			from = 0;
			to = 0;
			size = 0;
		}

		void dispatchMatch() {
			if(resultHandler!=null) {
				resultHandler.accept(this);
			}
		}
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

		//TODO add

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
			// If new size exceeds buffer, grow all storages
			if(size>=elements.length) {
				int newSize = CollectionUtils.growSize(elements.length, size);
				elements = new Item[newSize];
				m_node = new int[newSize];
				m_pos = new int[newSize];
				for (int i = 0; i < buffers.length; i++) {
					buffers[i] = new int[newSize];
				}
				for (int i = 0; i < caches.length; i++) {
					caches[i].reset(newSize);
				}
				for (int i = 0; i < gates.length; i++) {
					gates[i].reset(newSize);
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
				assert false; // fail for any ref
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
			from = 0;
			to = 0;
			size = 0;
		}
	}

	public static class Builder extends AbstractBuilder<Builder, SequencePattern> {

		private IqlElement root;
		private Integer id;
		private QueryModifier modifier = QueryModifier.ANY;
		private Long limit;
		private IqlConstraint filterConstraint;
		private IqlConstraint globalConstraint;
		private LaneContext context;
		private final Set<MatchFlag> flags = EnumSet.noneOf(MatchFlag.class);
		private Function<IqlNode, IqlNode> nodeTransform;
		private Monitor monitor;
		private Boolean cacheAll;

		//TODO add fields for configuring the result buffer

		private Builder() { /* no-op */ }

		//TODO add all the setter methods

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

		public Monitor getMonitor() { return monitor; }

		public Builder monitor(Monitor monitor) {
			requireNonNull(monitor);
			checkState("monitor already set", this.monitor==null);
			this.monitor = monitor;
			return this;
		}

		public int getId() { return id==null ? UNSET_INT : id.intValue(); }

		public Builder id(int id) {
			checkArgument("ID must be positive", id>=0);
			checkState("ID already set", this.id==null);
			this.id = Integer.valueOf(id);
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
	public interface Monitor {
		//TODO add callbacks for result dispatch and other events

		/** Called when a proper node is entered */
		default void enterNode(Node node, State state, int pos) {
			// no-op;
		}

		/** Called when a successful match is about to be dispatched */
		//TODO change to an actual wrapper interface that allows deferred access to the internals of State without full exposure
		default void dispatchResult(State state) {
			// no-op
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

	static class Gate {
		/**
		 * For each slot indicates if it has been visited already.
		 */
		boolean[] data = new boolean[INITIAL_SIZE];
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;

		Gate() { resetBounds(); }

		void reset(int size) {
			if(size>=data.length) {
				data = new boolean[CollectionUtils.growSize(data.length, size)];
			} else {
				Arrays.fill(data, 0, size, false);
			}
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

	static class TreeInfo implements Cloneable {
		/** Minimum number of elements to be matched by a subtree. */
		int minSize = 0;
		/** Maximum number of elements to be matched by a subtree. */
		int maxSize = 0;
		/** Flag to indicate whether {@link #maxSize} is actually valid. */
		boolean maxValid = true;
		/** Indicates that the state machine corresponding to a sub node is fully deterministic. */
		boolean deterministic = true;
		/** Indicates that parts of the input can be skipped. */
		boolean skip = false;
		/**
		 * Size of the current segment between matching 'save' and 'restore' border nodes.
		 * If no markers are used, this is equal to {@link #minSize}.
		 */
		int segmentSize = 0;

		/** Used to track fixed positions or areas. */
		int from, to;

		void reset() {
			minSize = 0;
			maxSize = 0;
			segmentSize = 0;
            maxValid = true;
            deterministic = true;
            skip = false;
            from = UNSET_INT;
            to = UNSET_INT;
		}

		@Override
		public TreeInfo clone() {
			try {
				return (TreeInfo) super.clone();
			} catch (CloneNotSupportedException e) { throw new InternalError(e); }
		}
	}


	/** Implements a generic accept node that keeps track of the last matched index. */
	static class Node {
		Node next = accept;

		/** Only modifier method. Allows subclasses to customize how conenctions should be attached.  */
		void setNext(Node next) { this.next = requireNonNull(next); }

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

		@Override
		public String toString() {
			return ToStringBuilder.create(this).build();
		}

		/** Returns {@code true} iff this node can scan the search space itself. */
		boolean isScanCapable() { return false; }

		/** Returns {@code true} iff this node is part of the finishing block of the state machine. */
		boolean isFinisher() { return false; }
	}

	static final int GREEDY = QuantifierModifier.GREEDY.ordinal();
	static final int RELUCTANT = QuantifierModifier.RELUCTANT.ordinal();
	static final int POSSESSIVE = QuantifierModifier.POSSESSIVE.ordinal();

	static final class Track extends Node {
		final Monitor monitor;

		Track(Node next, Monitor monitor) {
			checkArgument("Cannot monitor generic accept node", next!=accept);
			this.monitor = monitor;
			setNext(next);
		}

		@Override
		boolean match(State state, int pos) {
			monitor.enterNode(next, state, pos);
			return next.match(state, pos);
		}

		@Override
		public String toString() {
			return next.toString();
		}
	}

	/** Helper for "empty" nodes that are only existentially quantified. */
	static final class Empty extends Node {
		final int memberId;

		Empty(int memberId) {
			this.memberId = memberId;
		}

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
			info.segmentSize++;
			return info.deterministic;
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this).add("memberId", memberId).build();
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

		final Monitor monitor;

		@VisibleForTesting
		Finish(long limit, boolean stopAfterMatch) {
			this(limit, stopAfterMatch, null);
		}

		Finish(long limit, boolean stopAfterMatch, Monitor monitor) {
			this.limit = limit;
			this.stopAfterMatch = stopAfterMatch;
			this.monitor = monitor;
		}

		@Override
		boolean match(State state, int pos) {
			if(monitor!=null) {
				monitor.dispatchResult(state);
			}

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

		@Override
		boolean isFinisher() { return true; }
	}

	static abstract class Clip extends Node {
		boolean skip = false;

		abstract boolean intersect(State state);

		@Override
		boolean match(State state, int pos) {
			// Update search interval
			if(!intersect(state)) {
				return false;
			}
			// Skip ahead if allowed
			if(skip && pos<state.from) {
				pos = state.from;
			}
			// Early bail in case we're outside of allowed search space
			if(!state.contains(pos)) {
				return false;
			}
			// Continue with actual search
			return next.match(state, pos);
		}

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

	static final class Fixed extends Clip {
		final Interval region;

		Fixed(int from, int to) {
			checkArgument("Invalid interval begin", from>=0);
			checkArgument("Invalid interval end", to>=0);
			checkArgument("Empty interval", from<=to);
			region = Interval.of(from, to);
		}

		@Override
		boolean intersect(State state) {
			return state.intersect(region);
		}

		@Override
		boolean study(TreeInfo info) {
			study0(info);

			//TODO verify that we don't have impossible interval requirements.
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
					.build();
		}
	}

	/** Interval filter based on raw intervals in the matcher */
	static final class DynamicClip extends Clip {
		final int intervalIndex;
		DynamicClip(int intervalIndex) {
			this.intervalIndex = intervalIndex;
		}

		@Override
		boolean intersect(State state) {
			return state.intersect(state.intervals[intervalIndex]);
		}

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
					.build();
		}
	}

	/** Save- or restore-point for the right interval boundary during marker constructs */
	static final class Border extends Node {
		final boolean save;
		final int borderId;

		Border(boolean save, int borderId) {
			this.save = save;
			this.borderId = borderId;
		}

		@Override
		boolean match(State state, int pos) {
			if(save) {
				state.borders[borderId] = state.to;
			} else {
				state.to = state.borders[borderId];
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

		/** Delegates to {@link Node#next} node, since er're only a proxy. */
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
		public Filter(boolean reset, int gateId) {
			this.reset = reset;
			this.gateId = gateId;
		}

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

		/** Delegates to {@link Node#next} node, since er're only a proxy. */
		@Override
		boolean isFinisher() { return next.isFinisher(); }
	}

	/** Special scan that ensures no instance of atom appears before legal end of sequence/match. */
	static final class Negation extends ProperNode {
		int minSize = 0; // 0 means we have to check till end of sequence
		int atomMinSize = 1; // cannot negate zero-width assertions
		final Node atom;
		final int cacheId;

		public Negation(int id, int cacheId, Node atom) {
			super(id);
			this.cacheId = cacheId;
			this.atom = requireNonNull(atom);
		}

		@Override
		boolean isScanCapable() { return true; }

		@Override
		boolean match(State state, int pos) {
			if(minSize==0) {
				return matchFull(state, pos);
			}
			return matchWithTail(state, pos);
		}

		private boolean matchFull(State state, int pos) {
    		final int from = state.from;
    		final int to = state.to;
			final Cache cache = state.caches[cacheId];
			final int fence = state.size-1;

			for (int i = pos; i <= fence; i++) {
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

				state.reset(scope);
				state.reset(from, to);
				if(!matched) {
					return false;
				}
			}

			return next.match(state, state.size-1);
		}

		private boolean matchWithTail(State state, int pos) {
    		final int from = state.from;
    		final int to = state.to;
			final Cache cache = state.caches[cacheId];
			final int fence = state.to - minSize + 1;

			boolean result = false;

			for (int i = pos; i <= fence && !state.stop; i++) {
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
					matched = !atom.match(state, pos) && next.match(state, i);
					cache.setValue(i, matched);
				}

				result |= matched;

				state.reset(scope);
				state.reset(from, to);
			}

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
			atomMinSize = info.minSize;
			checkState("Nested atom must not contain zero-width assertion!", atomMinSize>0);

			return false;
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("id", id)
					.add("minSize", minSize)
					.add("atomMinSize", atomMinSize)
					.add("cacheId", cacheId)
					.add("atom", atom)
					.build();
		}
	}

	/** Special scan that implements universal quantification. */
	static final class All extends ProperNode {
		final Node atom;

		public All(int id, Node atom) {
			super(id);
			this.atom = requireNonNull(atom);
		}

		@Override
		boolean isScanCapable() { return true; }

		@Override
		boolean match(State state, int pos) {
//			assert pos==0 : "Universal quantification must cover the entire search space";
			//TODO ensure that we actually started at pos==0 ?
			final int last = state.to;
			for (int i = pos; i <= last; i++) {
				if(!atom.match(state, i)) {
					return false;
				}
			}
			return next.match(state, last);
		}

		@Override
		boolean study(TreeInfo info) {
			//TODO implement a flag in TreeInfo to pass down info to atom nodes that no result mapping is desired
			info.deterministic = false;
			info.skip = true;
			atom.study(info);

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
			if(!state.contains(pos)) {
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
					.build();
		}
	}

	/**
	 * Implements a forward-only one-shot exploration of the remaining
	 * search space. In contrast to {@link Exhaust} this type of scan will
	 * not reset the current mapping after a matching attempt, but rather
	 * finish the search after the first successful full match.
	 */
	static class Find extends ProperNode {
		int minSize = 1;
		boolean optional;

		Find(int id) { super(id); }

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("id", id)
					.build();
		}

		@Override
		boolean isScanCapable() { return true; }

		@Override
		boolean match(State state, int pos) {

			// Short-cut for zero-width assertion
			if(optional && pos==state.to+1) {
				return next.match(state, pos);
			}

    		final int from = state.from;
    		final int to = state.to;
			final int fence = state.to - minSize + 1;

			boolean result = false;

			for (int i = pos; i <= fence && !state.stop; i++) {
				int scope = state.scope();
				result |= next.match(state, i);
				state.reset(from, to);

				// We are only interested in the first successful match
				if(result) {
					break;
				}

				// Only reset if we failed to find a match (space boundaries are reset above)
				state.reset(scope);
			}

			return result;
		}

		/** @see Exhaust#study(TreeInfo) */
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
	 * by iteratively scanning for matches of the current tail.
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
		public String toString() {
			return ToStringBuilder.create(this)
					.add("id", id)
					.add("forward", forward)
					.add("cacheId", cacheId)
					.add("minSize", minSize)
					.build();
		}

		@Override
		boolean match(State state, int pos) {

			// Short-cut for zero-width assertion
			if(optional && pos==state.to+1) {
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
    		final int from = state.from;
    		final int to = state.to;
			final int fence = to - minSize + 1;

			boolean result = false;

			for (int i = pos; i <= fence && !state.stop; i++) {
				int scope = state.scope();
				result |= next.match(state, i);
				state.reset(scope);
				state.reset(from, to);
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
    		final int from = state.from;
    		final int to = state.to;
			final Cache cache = state.caches[cacheId];
			final int fence = to - minSize + 1;

			boolean result = false;

			for (int i = pos; i <= fence && !state.stop; i++) {
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
				state.reset(from, to);
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
    		final int from = state.from;
    		final int to = state.to;
			boolean result = false;

			for (int i = to - minSize + 1; i >= pos && !state.stop; i--) {
				int scope = state.scope();
				result |= next.match(state, i);
				state.reset(scope);
				state.reset(from, to);
			}

			return result;
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
        BranchConn() { /* no-op */ }
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
            return info.deterministic;
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
		final boolean stopOnSuccess;

		Branch(int id, boolean stopOnSuccess, BranchConn conn, Node...atoms) {
			super(id);
			checkArgument("Need at least 2 branch atoms", atoms.length>1);
			this.stopOnSuccess = stopOnSuccess;
			this.atoms = atoms;
			this.conn = conn;
		}

		/**
		 * We need to both re-route this node AND the branch-conn to the give
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
		public String toString() {
			return ToStringBuilder.create(this)
					.add("id", id)
					.add("atoms", Arrays.toString(atoms))
					.build();
		}

		//TODO make and override 'setNext' method?

		@Override
		boolean match(State state, int pos) {
    		final int from = state.from;
    		final int to = state.to;
        	final int scope = state.scope();
    		boolean result = false;
	        for (int n = 0; n < atoms.length && !state.stop; n++) {
	        	final Node atom = atoms[n];
	        	if (atom==null) {
	            	// zero-width path
	                result |= conn.next.match(state, pos);
	            } else {
	                result |= atom.match(state, pos);
	            }
	        	// Need to reset fully to allow clean restart for each branch
	        	state.reset(scope);
                state.reset(from, to);

                if(stopOnSuccess && result) {
                	break;
                }
	        }
	        return result;
		}

		@Override
		boolean study(TreeInfo info) {
			TreeInfo tmp = info.clone();

			info.reset();
			int minL2 = Integer.MAX_VALUE; // we only operate in int space here anyway
			int maxL2 = -1;
			int segL = Integer.MAX_VALUE;

			for (int n = 0; n < atoms.length; n++) {
				Node atom = atoms[n];
				if (atom == null) {
					continue;
				}
				atom.study(info);
				minL2 = Math.min(minL2, info.minSize);
				maxL2 = Math.max(maxL2, info.maxSize);
				segL = Math.min(segL, info.segmentSize);
				tmp.maxValid &= info.maxValid;
				info.reset();
			}

			tmp.minSize += minL2;
			tmp.maxSize += maxL2;
			tmp.segmentSize += segL;

			conn.next.study(info);

			info.minSize += tmp.minSize;
			info.maxSize += tmp.maxSize;
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

		Repetition(int id, Node atom, int cmin, int cmax, int type,
				int scopeBuf, int posBuf, int findAtomId) {
			super(id);
			this.atom = atom;
			this.cmin = cmin;
			this.cmax = cmax;
			this.type = type;
			this.scopeBuf = scopeBuf;
			this.posBuf = posBuf;
			if(findAtomId==UNSET_INT) {
				findAtom = null;
			} else {
				findAtom = new Find(findAtomId);
				findAtom.setNext(atom);
			}
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
					.build();
		}

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
				if(!matchAtom(state, pos, count)) {
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
				// Can't backtrack further
				if(count==0) {
					break;
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
				if (!matchAtom(state, pos, count)) {
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
				if (!matchAtom(state, pos, count)) {
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
            boolean maxV = info.maxValid;
            boolean detm = info.deterministic;
            info.reset();

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
            if (temp < minL) {
                temp = Integer.MAX_VALUE; // we only operate in int space here anyway
            }
            info.segmentSize = temp;

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

	/** Enforces disjoint match results */
	//TODO rethink this approach when we move to trees
	static final class Reset extends Node {
		@Override
		boolean match(State state, int pos) {
			final int from = state.from;
			final int to = state.to;

			boolean result = false;

			while(!state.finished && state.contains(pos)) {
				// Bail as soon as a new search fails
				if(!next.match(state, pos)) {
					break;
				}
				result = true;
				state.reset(from, to);
				// Remove entire length of match from search space
				pos = state.last;
				// Reset stop signal so exploration can have another try
				state.stop = false;
			}

			return result;
		}
	}
}
