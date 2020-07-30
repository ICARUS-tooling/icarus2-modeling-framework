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
package de.ims.icarus2.query.api.engine.matcher.seq;

import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.query.api.engine.matcher.AbstractMatcher;
import de.ims.icarus2.query.api.engine.matcher.NodeMatcher;
import de.ims.icarus2.query.api.iql.IqlElement;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryModifier;
import de.ims.icarus2.util.AbstractBuilder;

/**
 * Implements the automaton to match a node sequence defined in the
 * IQL protocol.
 *
 * @author Markus Gärtner
 *
 */
public class SequenceMatcher extends AbstractMatcher<Container> {

	/** Dummy node usable as tail if query is simple */
	static Node accept = new Node();

	/** Defines the general direction for matching. ANY is equivalent to FIRST here. */
	private final QueryModifier modifier;

	/** Maximum number of hits to report per container. */
	private final long limit;

	/** The IQL source of structural constraints for this matcher. */
	private final IqlElement source;

	/** Last allowed index to match */
	private int to;

	/** All the atomic nodes defined in the query */
	private NodeMatcher[] nodes;

	/** Entry point to the object graph of matcher nodes */
	private Node root;
	/** Final state of the matcher graph */
	private Node tail = accept;

	/** ResultBuffer cache for keeping track of matched nodes and multiplicities */
	private final ResultBuffer result;

	private SequenceMatcher(Builder builder) {
		super(builder.getId());

		modifier = builder.getModifier();
		limit = builder.getLimit();
		source = builder.getRoot();

		root = node(source);

		//TODO

		result = new ResultBuffer();
	}

	//TODO make extra node converter class to transform IqlElement tree into Node graph and keep track of things
	private Node node(IqlElement source) {

	}

	public static class Builder extends AbstractBuilder<Builder, SequenceMatcher> {

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
		protected SequenceMatcher create() { return new SequenceMatcher(this); }

	}

	/**
	 * @see de.ims.icarus2.query.api.engine.matcher.Matcher#matches(long, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean matches(long index, Container target) {
		// TODO Auto-generated method stub
		return false;
	}

	static class ResultBuffer {
		long index;

	}


	/** Implements a generic accept node */
	static class Node {
		Node next = accept;

		boolean match(SequenceMatcher matcher, int i, int j) {
			return true;
		}
	}

	/**
	 * Implements a final accept node that verifies that all existentially
	 * quantified nodes have been matched and that records the entire match
	 * as a result.
	 */
	static class Finish extends Node {
		@Override
		boolean match(SequenceMatcher matcher, int i, int j) {
			return true;
		}
	}

	/** Implements horizontal restriction of matching at the next available index */
	static class FixedFirst extends Node {

		@Override
		boolean match(SequenceMatcher matcher, int i, int j) {
			return next.match(matcher, i, j);
		}
	}
}
