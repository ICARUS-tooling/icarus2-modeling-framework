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

import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import javax.annotation.concurrent.NotThreadSafe;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.query.api.exp.Assignable;
import de.ims.icarus2.query.api.exp.EvaluationContext;
import de.ims.icarus2.query.api.exp.Expression;
import de.ims.icarus2.query.api.iql.IqlElement;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryModifier;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.collections.CollectionUtils;

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
 * <a href="">here</a>.
 * <p>
 * This implementation is heavily influenced by the {@link Pattern} class of the java regex
 * framework and as such many helper classes, methods or variables are named similarly,
 * but usually offer slightly different or extended functionality.
 *
 * @author Markus Gärtner
 *
 */
//TODO add link for ICQL specification!!
@NotThreadSafe
public class SequencePattern {

	/** The IQL source of structural constraints for this matcher. */
	private final IqlElement source;

	/** Defines the general direction for matching. ANY is equivalent to FIRST here. */
	private final QueryModifier modifier;

	/** Maximum number of hits to report per container. Either -1 or a positive value. */
	private final long limit;

	/** Dummy node usable as tail if query is simple */
	static Node accept = new Node();

	/** Entry point to the object graph of the state machine */
	private final Node root;
	/** Final state of the matcher graph */
	private final Node tail;
	/** Total number of caches needed for this pattern */
	private final int cacheCount;
	//TODO keep a collection of raw expressions and their contexts as blueprint for populating the matchers

	private final List<NodeDef> nodes;

	private final AtomicInteger matcherIdGen = new AtomicInteger(0);

	private SequencePattern(Builder builder) {
		modifier = builder.getModifier();
		limit = builder.getLimit();
		source = builder.getRoot();

		SequenceProcessor proc = new SequenceProcessor();
		proc.process(builder.getRoot());

		root = proc.root;
		tail = proc.tail;
		cacheCount = proc.cacheCount;
		nodes = proc.nodes;

		//TODO
	}

	/** Utility class for generating the state machine */
	static class SequenceProcessor {
		Node root;
		Node tail = accept;
		int cacheCount;
		final List<NodeDef> nodes = new ArrayList<>();

		void process(IqlElement root) {

		}
	}

	/**
	 * Crates a new matcher that uses this state machine and that can be safely used from
	 * within a single thread.
	 */
	public Matcher<Container> matcher() {
		int id = matcherIdGen.getAndIncrement();
		ResultBuffer result = new ResultBuffer();
		NodeMatcher[] nodes = this.nodes.stream()
				.map(NodeDef::createMatcher)
				.toArray(NodeMatcher[]::new);
		Cache[] caches = IntStream.range(0, cacheCount)
				.mapToObj(i -> new Cache())
				.toArray(Cache[]::new);

		return new SequenceMatcher(this, nodes, result, caches, id);
	}

	/**
	 * Public entry point for sequence matching and holder of the
	 * state during a matching operation.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static class SequenceMatcher extends AbstractMatcher<Container> {

		/** Last allowed index to match. Typically equal to {@code size - 1}. */
		private int to = UNSET_INT;
		/** Total number of elements in the target sequence. */
		private int size = UNSET_INT;
		/** Set by the Finish node if a result limit exists and we already found enough matches. */
		private boolean finished;

		private Container sequence;
		private Item[] items;
		private long index;

		/** Total number of hits reported so far */
		private long hits = 0;

		/** The source of this matcher */
		private final SequencePattern pattern;
		/** All the atomic nodes defined in the query */
		private final NodeMatcher[] nodes;
		/** ResultBuffer cache for keeping track of matched nodes and multiplicities */
		private final ResultBuffer result;
		/** Caches */
		private final Cache[] caches;

		private SequenceMatcher(SequencePattern pattern, NodeMatcher[] nodes,
				ResultBuffer result, Cache[] caches, int id) {
			super(id);

			this.pattern = requireNonNull(pattern);
			this.nodes = checkNotEmpty(nodes);
			this.result = requireNonNull(result);
			this.caches = requireNonNull(caches);
		}

		/**
		 * @see de.ims.icarus2.query.api.engine.matcher.Matcher#matches(long, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public boolean matches(long index, Container target) {
			this.index = index;
			this.sequence = target;
			//TODO swap items from container into our array.
			//TODO reset buffers with target and then call root.match(this, 0)

			// Let the state machine do its work
			return pattern.root.match(this, 0);
		}

	}

	public static class Builder extends AbstractBuilder<Builder, SequencePattern> {

		private IqlElement root;
		private int id = UNSET_INT;
		private QueryModifier modifier = QueryModifier.ANY;
		private long limit = UNSET_LONG;

		//TODO

		public int getId() { return id; }

		public QueryModifier getModifier() { return modifier; }

		public long getLimit() { return limit; }

		public IqlElement getRoot() { return root; }

		@Override
		protected void validate() {
			super.validate();

			checkState("No root element defined", root!=null);
			checkState("Id not defined", id>=0);
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
			//TODO increment hits counter upon dispatching
		}
	}

	/**
	 * Buffer for all the information needed to create a {@link NodeMatcher}
	 * for a new {@link SequenceMatcher} instance.
	 */
	static class NodeDef {
		final Assignable<? extends Item> element;
		final Expression<?> constraint;
		final EvaluationContext context;
		final int id;

		public NodeDef(int id, Assignable<? extends Item> element, Expression<?> constraint,
				EvaluationContext context) {
			this.id = id;
			this.element = requireNonNull(element);
			this.constraint = requireNonNull(constraint);
			this.context = requireNonNull(context);
		}

		NodeMatcher createMatcher() {
			Assignable<? extends Item> element = context.duplicate(this.element);
			Expression<?> constraint = context.duplicate(this.constraint);
			return new NodeMatcher(id, element, constraint);
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

	static abstract class Region {
		int from, to;

		boolean contains(int index) {
			return index>=from && index<=to;
		}

		abstract void refresh(SequenceMatcher matcher);
	}

	static class At extends Region {
		private final int pos;
		At(int pos) {
			this.pos = pos;
		}

		@Override
		void refresh(SequenceMatcher matcher) {
			// TODO Auto-generated method stub

		}
	}

	static class TreeInfo {
		/** Minimum number of elements to be matched by a subtree. */
		int minSize = 0;
		/** Maximum number of elements to be matched by a subtree. */
		int maxSize = 0;
		/** Flag to indicate whether {@link #maxSize} is actually valid. */
		boolean maxValid = false;
		/** Indicates that the state machine corresponding to a sub node is fully deterministic. */
		boolean deterministic = true;


	}


	/** Implements a generic accept node */
	static class Node {
		Node next = accept;

		boolean match(SequenceMatcher matcher, int j) {
			return true;
		}

		boolean study(TreeInfo info) {
			if(next!=null) {
				return next.study(info);
			}

			return info.deterministic;
		}
	}

	/**
	 * Implements a final accept node that verifies that all existentially
	 * quantified nodes have been matched and which records the entire match
	 * as a result.
	 */
	static class Finish extends Node {
		@Override
		boolean match(SequenceMatcher matcher, int j) {
			final ResultBuffer buffer = matcher.result;
			final SequencePattern pattern = matcher.pattern;

			buffer.dispatch();

			matcher.hits++;
			if(pattern.limit!=UNSET_LONG && matcher.hits>=pattern.limit) {
				matcher.finished = true;
			}


			return true;
		}
	}

	/** Common super class for nodes that use caching */
	static abstract class Cached extends Node {
		final int cacheId;

		Cached(int cacheId) {
			this.cacheId = cacheId;
		}
	}

	/** Matches an inner constraint to a specific node without any caching */
	static class Single extends Node {

		final int nodeId;

		Single(int nodeId) {
			this.nodeId = nodeId;
		}

		@Override
		boolean match(SequenceMatcher matcher, int j) {
			final NodeMatcher node = matcher.nodes[nodeId];
			boolean value = node.matches(j, matcher.items[j]);
			if(value) {
				value = next.match(matcher, j+1);
			}
			return value;
		}

		@Override
		boolean study(TreeInfo info) {
			info.minSize++;
			return next.study(info);
		}
	}

	/** Matches an inner constraint to a specific node and uses caching */
	static class SingleC extends Cached {

		final int nodeId;

		SingleC(int nodeId, int cacheId) {
			super(cacheId);
			this.nodeId = nodeId;
		}

		@Override
		boolean match(SequenceMatcher matcher, int j) {
			final Cache cache = matcher.caches[cacheId];
			if(cache.isSet(j)) {
				return cache.getValue(j);
			}

			// Unknown index ->
			final NodeMatcher node = matcher.nodes[nodeId];
			boolean value = node.matches(j, matcher.items[j]);
			cache.setValue(j, value);
			if(value) {
				value = next.match(matcher, j);
			}
			return value;
		}

		@Override
		boolean study(TreeInfo info) {
			info.minSize++;
			return next.study(info);
		}
	}

	/**
	 * Implements the iterative scanning of all remaining possibilities
	 * for matches of the nested atom, honoring the default direction defined
	 * by the target sequence.
	 * This variant does not use memoization and is intended for top-level
	 * scanning when there is no benefit from caching as every node will
	 * only be visited once anyway.
	 */
	static class ScanForward extends Node {
		int minSize = 0;
		Node atom;

		@Override
		boolean match(SequenceMatcher matcher, int j) {
			final ResultBuffer buffer = matcher.result;
			final int fence = matcher.to - minSize;

			boolean result = false;

			for (int i = j; i <= fence && !matcher.finished; i++) {
				int scope = buffer.scope();
				result |= atom.match(matcher, i);
				buffer.reset(scope);
			}

			return result;
		}

		@Override
		boolean study(TreeInfo info) {
			//TODO store 'info' state and merge atom and next info
			info.deterministic = false;
			return atom.study(info);
		}
	}

	/**
	 * Implements the iterative scanning of all remaining possibilities
	 * for matches of the nested atom, using the reverse direction as
	 * defined by the target sequence.
	 * This variant does not use memoization and is intended for top-level
	 * scanning when there is no benefit from caching as every node will
	 * only be visited once anyway.
	 */
	static class ScanBackward extends Node {
		int minSize = 0;
		Node atom;

		@Override
		boolean match(SequenceMatcher matcher, int j) {
			final ResultBuffer buffer = matcher.result;

			boolean result = false;

			for (int i = matcher.to - minSize; i >= j && !matcher.finished; i++) {
				int scope = buffer.scope();
				result |= atom.match(matcher, i);
				buffer.reset(scope);
			}

			return result;
		}
	}

	/**
	 * Implements the iterative scanning of all remaining possibilities
	 * for matches of the nested atom.
	 * We employ memoization here to prevent repeatedly checking false
	 * leads. If a node is marked as successful hit, we still need to
	 * continue down all further nodes in the state machine, but we can
	 * effectively rule out unsuccessful paths.
	 */
	static class ScanForwardC extends Cached {
		int minSize = 0;
		Node atom;

		ScanForwardC(int cacheId) {
			super(cacheId);
		}

		@Override
		boolean match(SequenceMatcher matcher, int j) {
			Cache cache = matcher.caches[cacheId];
			ResultBuffer buffer = matcher.result;

			int fence = matcher.to - minSize;

			boolean result = false;

			for (int i = j; i <= fence && !matcher.finished; i++) {
				int scope = buffer.scope();
				boolean stored = cache.isSet(i);
				boolean matched;
				if(stored) {
					matched = cache.getValue(i);
					if(matched) {
						// Continue matching, but we know it will succeed
						atom.match(matcher, i);
					} else {
						// We know this is a dead-end, so skip
						continue;
					}
				} else {
					// Previously unseen index, so explore and cache result
					matched = atom.match(matcher, i);
					cache.setValue(i, matched);
				}

				result |= matched;

				buffer.reset(scope);
			}

			return result;
		}
	}
}
