/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine.matcher;

import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.query.api.engine.matcher.StructurePattern.last;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.ANCHOR_0;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.BUFFER_0;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.BUFFER_1;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.BUFFER_2;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.CACHE_0;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.CACHE_1;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.CACHE_2;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.CLOSURE_0;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.CONTINUOUS;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.DISCONTINUOUS;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.EQUALS_A;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.EQUALS_B;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.EQUALS_C;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.EQUALS_NOT_X;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.EQUALS_X;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.EQUALS_X_IC;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.EQUALS_Y;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.MAP_0;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.MAP_1;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.NODE_0;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.NODE_1;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.NODE_2;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.NO_ANCHOR;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.NO_LIMIT;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.NO_MEMBER;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.NO_STOP;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.PING_0;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.REGION_0;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.branch;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.item;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.matchers;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.seq;
import static de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.Utils.tree;
import static de.ims.icarus2.query.api.exp.ExpressionTestUtils.assertQueryException;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.NO_LABEL;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.NO_MARKER;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.all;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.atLeastGreedy;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.atLeastPossessive;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.atLeastReluctant;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.atMostGreedy;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.atMostPossessive;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.atMostReluctant;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.constraint;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.disjunction;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.eq_exp;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.exact;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.grouping;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.ic_exp;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.mark;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.negated;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.ordered;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.quantify;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.rangeGreedy;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.rangePossessive;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.rangeReluctant;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.unordered;
import static de.ims.icarus2.test.TestUtils.filledArray;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static de.ims.icarus2.util.lang.Primitives._boolean;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.StructureFlag;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.engine.QueryProcessor;
import de.ims.icarus2.query.api.engine.QueryProcessor.Option;
import de.ims.icarus2.query.api.engine.QueryTestUtils;
import de.ims.icarus2.query.api.engine.TreeStructure;
import de.ims.icarus2.query.api.engine.matcher.IntervalConverter.IntervalArg;
import de.ims.icarus2.query.api.engine.matcher.IntervalConverter.IntervalArrayArg;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Branch;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.BranchConn;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Cache;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.DynamicClip;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Exhaust;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Finish;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.LevelFilter;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Monitor;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Node;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.NodeInfo;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.NonResettingMatcher;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Ping;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Repetition;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Role;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.RootFrame;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Single;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.State;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.StateMachineSetup;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.StructureQueryProcessor;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Tree;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.TreeClosure;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.TreeConn;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.TreeFrame;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.TreeInfo;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.TreeManager;
import de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.RepetitionUtils.ClosedBase;
import de.ims.icarus2.query.api.engine.matcher.StructurePatternTest.RepetitionUtils.OpenBase;
import de.ims.icarus2.query.api.engine.matcher.mark.Interval;
import de.ims.icarus2.query.api.exp.EvaluationContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.LaneContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.RootContext;
import de.ims.icarus2.query.api.exp.EvaluationUtils;
import de.ims.icarus2.query.api.exp.env.SharedUtilityEnvironments;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlPredicate;
import de.ims.icarus2.query.api.iql.IqlElement;
import de.ims.icarus2.query.api.iql.IqlElement.IqlEdge;
import de.ims.icarus2.query.api.iql.IqlElement.IqlElementDisjunction;
import de.ims.icarus2.query.api.iql.IqlElement.IqlGrouping;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlElement.IqlSequence;
import de.ims.icarus2.query.api.iql.IqlElement.IqlTreeNode;
import de.ims.icarus2.query.api.iql.IqlExpression;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlLane.LaneType;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerCall;
import de.ims.icarus2.query.api.iql.IqlObjectIdGenerator;
import de.ims.icarus2.query.api.iql.IqlPayload;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryType;
import de.ims.icarus2.query.api.iql.IqlQuantifier;
import de.ims.icarus2.query.api.iql.IqlQueryElement;
import de.ims.icarus2.query.api.iql.IqlTestUtils;
import de.ims.icarus2.query.api.iql.NodeArrangement;
import de.ims.icarus2.test.annotations.IntArrayArg;
import de.ims.icarus2.test.annotations.IntMatrixArg;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * This test class is one big unification of test suites for various aspects of the
 * {@link StructurePattern} state machine for IQL. Strictly speaking we should divide
 * this instantiation of anti-patterns into a small selection of test suites (in also
 * separate files) for distinct aspects of the state machine. But currently it is just
 * simpler to keep it in one place as both the state machine processor, evaluation
 * and test suite are still in a rather volatile state.
 *
 * @author Markus Gärtner
 *
 */
public class StructurePatternTest {

	private static final Class<?>[] ELEMENT_TYPES = {
		IqlNode.class,
		IqlTreeNode.class,
		IqlGrouping.class,
		IqlSequence.class,
		IqlElementDisjunction.class,
		IqlEdge.class,
	};

	private static Matcher<Item> matcher(int id, CharPredicate pred) {
		return new Matcher<Item>() {

			@Override
			public boolean matches(long index, Item target) {
				String s = target.toString();
				assertThat(s).hasSize(1);
				return pred.test(s.charAt(0));
			}

			@Override
			public int id() { return id; }
		};
	}

	private static final IqlObjectIdGenerator idGen = new IqlObjectIdGenerator();

	/**
	 * Upgrades a {@link IqlNode} from an "empty" one to a node with a "true" constraint.
	 * This utility methods exists to force the recognition of nodes as mappable entries
	 * in the query so we can test the occurrence of dummy nodes.
	 */
	static final  Function<IqlNode, IqlNode> PROMOTE_NODE =  node -> {
		if(!node.getConstraint().isPresent()) {
			IqlExpression exp = new IqlExpression();
			exp.setContent("true");
			IqlPredicate pred = new IqlPredicate();
			idGen.assignId(pred);
			pred.setExpression(exp);
			node.setConstraint(pred);
		}
		return node;
	};

	static Consumer<? super StructurePattern.Builder> LIMIT(int limit) {
		return b -> b.limit(limit);
	}

	static final Consumer<? super StructurePattern.Builder> DISJOINT = b -> b.flag(IqlLane.MatchFlag.DISJOINT);
	static final Consumer<? super StructurePattern.Builder> CACHE_ALL = b -> b.cacheAll(true);
	static final Consumer<? super StructurePattern.Builder> LIMIT_SINGLE = b -> b.limit(1);

	static final Consumer<? super ResultConfig> UNORDERED = r -> r.ignoreOrder(true);

	private interface CharPredicate {
		boolean test(char c);
	}

	/** Print string representation of node and some state info to {@code System.out}. */
	private static final Monitor MONITOR = new Monitor() {
		@Override
		public void enterNode(Node node, State state, int pos) {
			System.out.printf("enter [pos:%d - matches:%d] %s%n", _int(pos), _long(state.reported), node);
		}
		@Override
		public void exitNode(Node node, State state, int pos, boolean result) {
			System.out.printf("exit [pos:%d - matches:%d] %s -> %b%n", _int(pos), _long(state.reported), node, _boolean(result));
			if(node.isFinisher()) {
				dispatchResult(state);
			}
		}

		private void dispatchResult(State state) {
			if(state.entry==0) {
				System.out.println("[empty result]");
			} else {
				System.out.printf("[%d entries: ", _int(state.entry));
				for (int i = 0; i < state.entry; i++) {
					if(i>0) {
						System.out.print(", ");
					}
					System.out.printf("%d->%d",_int(state.m_node[i]), _int(state.m_index[i]));
				}
				System.out.println("]");
			}
		}
	};

	static class Utils {

		static final char X = 'X';
		static final char Y = 'Y';
		static final char Z = 'Z';
		static final char x = 'x';
		static final char A = 'A';
		static final char B = 'B';
		static final char C = 'C';

		static final CharPredicate EQUALS_X = eq(X);
		static final CharPredicate EQUALS_NOT_X = neq(X);
		static final CharPredicate EQUALS_X_IC = ic(x);
		static final CharPredicate EQUALS_Y = eq(Y);

		static final CharPredicate EQUALS_A = eq(A);
		static final CharPredicate EQUALS_B = eq(B);
		static final CharPredicate EQUALS_C = eq(C);

		static final int NODE_0 = 0;
		static final int NODE_1 = 1;
		static final int NODE_2 = 2;
		static final int MAP_0 = 0;
		static final int MAP_1 = 1;
		static final int MAP_2 = 2;
		static final int CACHE_0 = 0;
		static final int CACHE_1 = 1;
		static final int CACHE_2 = 2;
		static final int CACHE_3 = 3;
		static final int BUFFER_0 = 0;
		static final int BUFFER_1 = 1;
		static final int BUFFER_2 = 2;
		static final int REGION_0 = 0;
		static final int REGION_1 = 1;
		static final int ANCHOR_0 = 0;
		static final int ANCHOR_1 = 1;
		static final int ANCHOR_2 = 2;
		static final int ANCHOR_3 = 3;
		static final int CLOSURE_0 = 0;
		static final int CLOSURE_1 = 1;
		static final int CLOSURE_2 = 2;
		static final int PING_0 = 0;
		static final int PING_1 = 1;
		static final int PING_2 = 2;

		static final int NO_FIND = UNSET_INT;

		static final int NO_CACHE = UNSET_INT;
		static final int NO_LIMIT = UNSET_INT;
		static final int NO_MEMBER = UNSET_INT;
		static final int NO_ANCHOR = UNSET_INT;

		static final boolean CONTINUOUS = false;
		static final boolean DISCONTINUOUS = true;

		static final boolean NO_STOP = false;

		/** Match given character exactly */
		static StructurePatternTest.CharPredicate eq(char sentinel) {
			return c -> c==sentinel;
		}
		/** Match anything but given character */
		static StructurePatternTest.CharPredicate neq(char sentinel) {
			return c -> c!=sentinel;
		}
		/** Match given character while ignoring case */
		static StructurePatternTest.CharPredicate ic(char sentinel) {
			return c -> c==Character.toLowerCase(sentinel) || c==Character.toUpperCase(sentinel);
		}
		/** Match no character -> always return false */
		static StructurePatternTest.CharPredicate none() {
			return c -> false;
		}
		static Supplier<Matcher<Item>> sup(Matcher<Item> m) { return () -> m; }
		@SafeVarargs
		static Supplier<Matcher<Item>>[] matchers(Matcher<Item>...matchers) {
			return Stream.of(matchers).map(Utils::sup).toArray(Supplier[]::new);
		}
		static Item item(int index, char c) {
			Item item = mockItem();
			when(_long(item.getIndex())).thenReturn(_long(index));
			when(item.toString()).thenReturn(String.valueOf(c));
			return item;
		}
		static Node seq(Node...nodes) {
			for (int i = 1; i < nodes.length; i++) {
				nodes[i-1].setNext(nodes[i]);
			}
			// Enforce optimization
			nodes[0].study(new TreeInfo());
			return nodes[0];
		}
		static Branch branch(int id, Node...atoms) {
			BranchConn conn = new BranchConn(-1);
			for (Node atom : atoms) {
				if(atom!=null) {
					atom.setNext(conn);
				}
			}
			return new Branch(id, mock(IqlQueryElement.class), conn, atoms);
		}
		/** Attaches a {@link Tree} to {@code node} and returns matching {@link TreeConn} */
		static Node tree(Single node, Node atom, IntSupplier idGen) {
			TreeConn conn = new TreeConn(idGen.getAsInt(), node.source, node.anchorId);
			Node scan = new Exhaust(idGen.getAsInt(), true);
			scan.setNext(atom);
			Tree tree = new Tree(idGen.getAsInt(), node.source, node.anchorId, scan, conn);
			node.setNext(tree);
			StructurePattern.last(atom).setNext(conn);
			atom = node;
			return conn;
		}
	}

	/** Matches an inner constraint, but neither caches nor maps the result. */
	static final class Proxy extends Node {
		final int nodeId;

		Proxy(int nodeId) {
			super(-1);
			this.nodeId = nodeId;
		}

		@Override
		boolean match(State state, int pos) {
			if(pos>state.frame.to()) {
				return false;
			}

			final Matcher<Item> m = state.matchers[nodeId];
			boolean value = m.matches(pos, state.elements[pos]);

			if(value) {
				value = next.match(state, pos+1);
			}

			return value;
		}

		@Override
		boolean study(TreeInfo info) {
			info.minSize++;
			info.maxSize++;
			info.segmentSize++;
			return next.study(info);
		}

		@Override
		public NodeInfo info() { return null; }

		@Override
		boolean isProxy() { return true; }
	}

	static IqlNode[] mockMappedNodes(int size) {
		return IntStream.range(0, size)
				.mapToObj(i -> mock(IqlNode.class))
				.toArray(IqlNode[]::new);
	}

	/** Expected query structure (only applies to inner {@link IqlElement} instances!) */
	@FunctionalInterface
	static interface QueryConfig {

		@Deprecated
		static final IqlElement EMPTY = mock(IqlElement.class);

		void assertQuery(IqlElement element);

		static QueryConfig of(Class<? extends IqlElement> type) {
			return e -> assertThat(e).isExactlyInstanceOf(type);
		}

		static QueryConfig grouping(QueryConfig nested) {
			return element -> {
				assertThat(element).isInstanceOf(IqlGrouping.class);
				nested.assertQuery(((IqlGrouping)element).getElement());
			};
		}

		static QueryConfig tree(QueryConfig children) {
			return element -> {
				assertThat(element).isInstanceOf(IqlTreeNode.class);
				Optional<IqlElement> elements = ((IqlTreeNode)element).getChildren();
				assertThat(elements).as("no children present").isNotEmpty();
				children.assertQuery(elements.get());
			};
		}

		static QueryConfig sequence(QueryConfig...nested) {
			return element -> {
				assertThat(element).isInstanceOf(IqlSequence.class);
				List<IqlElement> elements = ((IqlSequence)element).getElements();
				assertThat(elements).as("Incorrect number of nested elements").hasSameSizeAs(nested);
				for (int i = 0; i < nested.length; i++) {
					nested[i].assertQuery(elements.get(i));
				}
			};
		}

		static QueryConfig disjunction(QueryConfig...branches) {
			return element -> {
				assertThat(element).isInstanceOf(IqlElementDisjunction.class);
				List<IqlElement> alternatives = ((IqlElementDisjunction)element).getAlternatives();
				assertThat(alternatives).as("Incorrect number of alternative elements").hasSameSizeAs(branches);
				for (int i = 0; i < branches.length; i++) {
					branches[i].assertQuery(alternatives.get(i));
				}
			};
		}

		@SuppressWarnings("unchecked")
		static QueryConfig fromQuery(String s) {
			Stack<Object> stack = new ObjectArrayList<>();
			StringBuilder text = new StringBuilder();

			stack.push(new ObjectArrayList<QueryConfig>());

			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);

				if(Character.isWhitespace(c)) {
					continue;
				}

				switch (c) {
				case '[': {
					if(stack.top()==IqlNode.class) {
						stack.push(new ObjectArrayList<QueryConfig>());
					}
					stack.push(IqlNode.class);
				} break;
				case ']': {
					QueryConfig node;
					if(stack.top() instanceof List) {
						List<QueryConfig> nested = (List<QueryConfig>) stack.pop();
						if(nested.size()>1) {
							node = QueryConfig.tree(QueryConfig.sequence(nested.toArray(new QueryConfig[0])));
						} else {
							node = QueryConfig.tree(nested.get(0));
						}
					} else {
						node = QueryConfig.of(IqlNode.class);
					}
					assertThat(stack.pop()).isSameAs(IqlNode.class);
					((List<QueryConfig>)stack.top()).add(node);
					text.setLength(0);
				} break;
				case '{': {
					if(stack.top()==IqlNode.class) {
						stack.push(new ObjectArrayList<QueryConfig>());
					}
					stack.push(IqlGrouping.class);
					stack.push(new ObjectArrayList<QueryConfig>());
				} break;
				case '}': {
					QueryConfig node;
					List<QueryConfig> nested = (List<QueryConfig>) stack.pop();
					assertThat(stack.pop()).isSameAs(IqlGrouping.class);
					if(nested.size()>1) {
						node = QueryConfig.grouping(QueryConfig.sequence(nested.toArray(new QueryConfig[0])));
					} else {
						node = QueryConfig.grouping(nested.get(0));
					}
					((List<QueryConfig>)stack.top()).add(node);
					text.setLength(0);
				} break;

				default:
					text.append(c); break;
				}
			}

			List<QueryConfig> rootNodes = (List<QueryConfig>) stack.pop();
			if(rootNodes.size()>1) {
				return QueryConfig.sequence(rootNodes.toArray(new QueryConfig[0]));
			}
			return rootNodes.get(0);
		}
	}

	@Nested
	class ForQueryConfig {

		private QueryConfig mockConfig(IqlElement valid) {
			QueryConfig config = mock(QueryConfig.class);
			doAnswer(invoc -> {
				assertThat((IqlElement)invoc.getArgument(0)).isSameAs(valid);
				return null;
			}).when(config).assertQuery(any(IqlElement.class));
			return config;
		}

		@SuppressWarnings("unchecked")
		@TestFactory
		public Stream<DynamicTest> testOf() throws Exception {
			return Stream.of(ELEMENT_TYPES)
					.map(type -> dynamicTest(type.getSimpleName(), () -> {
						QueryConfig config = QueryConfig.of((Class<IqlElement>)type);

						IqlElement valid = (IqlElement) type.newInstance();
						config.assertQuery(valid);

						assertThatExceptionOfType(AssertionError.class).as("invalid type").isThrownBy(() -> {
							IqlElement invalid = mock(IqlElement.class);
							config.assertQuery(invalid);
						});
			}));
		}

		private IqlTreeNode makeTree(IqlElement nested) {
			IqlTreeNode tree = new IqlTreeNode();
			tree.setChildren(nested);
			return tree;
		}

		@Test
		public void testTree() throws Exception {
			IqlElement child = mock(IqlElement.class);
			IqlTreeNode valid = makeTree(child);
			QueryConfig nested = mockConfig(child);

			QueryConfig config = QueryConfig.tree(nested);
			config.assertQuery(valid);

			assertThatExceptionOfType(AssertionError.class).as("invalid type").isThrownBy(() -> {
				IqlTreeNode invalid = makeTree(mock(IqlElement.class));
				config.assertQuery(invalid);
			});
		}

		private IqlGrouping makeGrouping(IqlElement nested) {
			IqlGrouping grouping = new IqlGrouping();
			grouping.setElement(nested);
			return grouping;
		}

		@Test
		public void testGrouping() throws Exception {
			IqlElement child = mock(IqlElement.class);
			IqlGrouping valid = makeGrouping(child);
			QueryConfig nested = mockConfig(child);

			QueryConfig config = QueryConfig.grouping(nested);
			config.assertQuery(valid);

			assertThatExceptionOfType(AssertionError.class).as("invalid type").isThrownBy(() -> {
				IqlGrouping invalid = makeGrouping(mock(IqlElement.class));
				config.assertQuery(invalid);
			});
		}

		/** Instantiate sequence and fill with given elements */
		private IqlSequence makeSequence(IqlElement...nested) {
			IqlSequence sequence = new IqlSequence();
			Stream.of(nested).forEach(sequence::addElement);
			return sequence;
		}

		/** Instantiate sequence and fill with specified number of mocked elements */
		private IqlSequence makeSequence(int size) {
			IqlSequence sequence = new IqlSequence();
			IntStream.range(0, size)
				.mapToObj(i -> mock(IqlElement.class))
				.forEach(sequence::addElement);
			return sequence;
		}

		@Nested
		class ForSequence {



			@ParameterizedTest
			@CsvSource({
				"1, 2",
				"2, 3",
				"10, 5",
				"5, 10",
			})
			public void testSizeMismatch(int expected, int given) {
				IqlSequence target = makeSequence(given);
				QueryConfig config = QueryConfig.sequence(filledArray(expected, QueryConfig.class));

				assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {
					config.assertQuery(target);
				});
			}

			@ParameterizedTest
			@CsvSource({
				"2, 0",
				"2, 1",
				"3, 0",
				"3, 1",
				"3, 2",
				"20, 4",
				"20, 13",
			})
			public void testElementMismatch(int size, int diffIndex) {
				IqlElement[] expected = filledArray(size, IqlElement.class);
				IqlElement[] given = expected.clone();
				given[diffIndex] = mock(IqlElement.class);

				QueryConfig config = QueryConfig.sequence(Stream.of(expected)
						.map(element -> mockConfig(element))
						.toArray(QueryConfig[]::new));

				IqlSequence target = makeSequence(given);

				assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {
					config.assertQuery(target);
				});
			}
		}

		/** Instantiate disjunction and fill with given elements */
		private IqlElementDisjunction makeDisjunction(IqlElement...nested) {
			IqlElementDisjunction disjunction = new IqlElementDisjunction();
			Stream.of(nested).forEach(disjunction::addAlternative);
			return disjunction;
		}

		/** Instantiate sequence and fill with specified number of mocked elements */
		private IqlElementDisjunction makeDisjunction(int size) {
			IqlElementDisjunction disjunction = new IqlElementDisjunction();
			IntStream.range(0, size)
				.mapToObj(i -> new IqlNode())
				.forEach(disjunction::addAlternative);
			return disjunction;
		}

		@Nested
		class ForDisjunction {

			@ParameterizedTest
			@CsvSource({
				"1, 2",
				"2, 3",
				"10, 5",
				"5, 10",
			})
			public void testSizeMismatch(int expected, int given) {
				IqlElementDisjunction target = makeDisjunction(given);
				QueryConfig config = QueryConfig.sequence(filledArray(expected, QueryConfig.class));

				assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {
					config.assertQuery(target);
				});
			}

			@ParameterizedTest
			@CsvSource({
				"2, 0",
				"2, 1",
				"3, 0",
				"3, 1",
				"3, 2",
				"20, 4",
				"20, 13",
			})
			public void testElementMismatch(int size, int diffIndex) {
				IqlElement[] expected = filledArray(size, IqlElement.class);
				IqlElement[] given = expected.clone();
				given[diffIndex] = mock(IqlElement.class);

				QueryConfig config = QueryConfig.disjunction(Stream.of(expected)
						.map(branch -> mockConfig(branch))
						.toArray(QueryConfig[]::new));

				IqlElementDisjunction target = makeDisjunction(given);

				assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {
					config.assertQuery(target);
				});
			}
		}

		@Nested
		class ForFromQuery {

			@Test
			public void testNode() {
				QueryConfig config = QueryConfig.fromQuery("[]");
				IqlNode element = new IqlNode();

				config.assertQuery(element);

				assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {
					config.assertQuery(mock(IqlElement.class));
				});
			}

			//TODO test tree version once we implement tree matching

			@ParameterizedTest
			@ValueSource(ints = {2, 4, 10})
			public void testSequence(int size) {

				String[] items = new String[size];
				IqlSequence sequence = new IqlSequence();

				for (int i = 0; i < size; i++) {
					items[i] = "[]";
					sequence.addElement(new IqlNode());
				}

				QueryConfig config = QueryConfig.fromQuery(String.join("", items));

				// Assert successful check
				config.assertQuery(sequence);

				// Assert type mismatch
				assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {
					config.assertQuery(mock(IqlElement.class));
				});
			}

			@Test
			public void testGroupingSingular() {

				IqlGrouping grouping = makeGrouping(new IqlNode());

				QueryConfig config = QueryConfig.fromQuery("{[]}");

				// Assert successful check
				config.assertQuery(grouping);

				// Assert type mismatch
				assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {
					config.assertQuery(mock(IqlElement.class));
				});
			}

			@ParameterizedTest
			@ValueSource(ints = {2, 4, 10})
			public void testGrouping(int size) {

				StringBuilder sb = new StringBuilder();
				IqlNode[] nodes = new IqlNode[size];

				sb.append('{');
				for (int i = 0; i < size; i++) {
					sb.append("[]");
					nodes[i] = new IqlNode();
				}
				sb.append('}');

				IqlGrouping grouping = makeGrouping(makeSequence(nodes));

				QueryConfig config = QueryConfig.fromQuery(sb.toString());

				// Assert successful check
				config.assertQuery(grouping);

				// Assert type mismatch
				assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {
					config.assertQuery(mock(IqlElement.class));
				});
			}

			@ParameterizedTest
			@ValueSource(ints = {2, 4, 10})
			public void testGroupingWithContent(int size) {

				StringBuilder sb = new StringBuilder();
				IqlNode[] nodes = new IqlNode[size];

				sb.append('{');
				for (int i = 0; i < size; i++) {
					sb.append('[').append('x').append(i).append(']');
					nodes[i] = new IqlNode();
				}
				sb.append('}');

				IqlGrouping grouping = makeGrouping(makeSequence(nodes));

				QueryConfig config = QueryConfig.fromQuery(sb.toString());

				// Assert successful check
				config.assertQuery(grouping);

				// Assert type mismatch
				assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {
					config.assertQuery(mock(IqlElement.class));
				});
			}
		}
	}

	/** Encapsulates the info for expected matches and hits inside a single target sequence */
	abstract static class MatchConfig<C extends MatchConfig<C>> implements Consumer<State> {
		/** Index in target sequence to start the test search at */
		private Integer startPos;
		/** Expected success state */
		private Boolean expectedResult;
		/** Total number of matches we expect */
		private Integer expectedCount;
		/** Individual node info regarding last hits etc... */
		private final List<NodeConfig> nodes = new ArrayList<>();
		private final List<CacheConfig> caches = new ArrayList<>();
		/** Results expected to be dispatched */
		private final List<ResultConfig> results = new ArrayList<>();
		/** Pointer to the next expected result entry */
		private int nextResult = 0;
		/** Monitor to be used to track the state machine */
		private Monitor monitor;
		/** Flag to allow duplicate mappings in the overall result */
		private Boolean allowDuplicates;

		/** Intermediate mappings expected to exist during result assertion */
		private final ResultConfig mapping = new ResultConfig(-1);
		/** */
		private final Set<String> mappings = new ObjectOpenHashSet<>();

		@SuppressWarnings("unchecked")
		private C thisAsCast() { return (C) this; }

		/**
		 * Designate start position for matching. If root note is not scan capable
		 * no additional exploration will be performed besides that start value!
		 */
		C startPos(int startPos) {
			checkArgument("Start pos must be 0 or positive", startPos>=0);
			checkState("Start position already set", this.startPos==null);
			this.startPos = Integer.valueOf(startPos);
			return thisAsCast();
		}

		/** Designate overall result value. Does not influence expected match count. */
		C expectedResult(boolean expectedResult) {
			checkState("Expected result already set", this.expectedResult==null);
			this.expectedResult = Boolean.valueOf(expectedResult);
			return thisAsCast();
		}

		/** Designate expected result count. Does not influence expected result value. */
		C expectedCount(int expectedCount) {
			checkArgument("Expected count must be 0 or positive", expectedCount>=0);
			checkState("Expected count already set", this.expectedCount==null);
			this.expectedCount = Integer.valueOf(expectedCount);
			return thisAsCast();
		}

		/**
		 * Designate result value and count simultaneously. If {@code expectedCount}
		 * is geater than {@code 0} expected result value will be set to {@code true}.
		 */
		C expectMatches(int expectedCount) {
			return expectedResult(expectedCount>0).expectedCount(expectedCount);
		}

		/** Set expected result value to {@code true} and expected count to given value. */
		C expectSuccess(int expectedCount) {
			return expectedResult(true).expectedCount(expectedCount);
		}

		/** Set expected result value to {@code false} and expected count to {@code 0}. */
		C expectFail() {
			return expectedResult(false).expectedCount(0);
		}

		protected void assertBasicSettings() {
			assertThat(startPos).isNotNull();
			assertThat(expectedResult).isNotNull();
			assertThat(expectedCount).isNotNull();
		}

		protected int startPos() { return startPos.intValue(); }
		protected boolean expectedResult() { return expectedResult.booleanValue(); }
		protected int expectedCount() { return expectedCount.intValue(); }

		/** Specify if result is allowed to contain duplicate mapping sets. */
		C allowDuplicates(boolean allowDuplicates) {
			checkState("Allow duplicates flag already set", this.allowDuplicates==null);
			this.allowDuplicates = Boolean.valueOf(allowDuplicates);
			return thisAsCast();
		}

		/** Designate a monitor for tracking the state machine during mactching. */
		C monitor(Monitor monitor) { this.monitor = monitor; return thisAsCast(); }

		/** Add a config with assertions for a single node. */
		C node(NodeConfig node) { nodes.add(requireNonNull(node)); return thisAsCast(); }

		/** Add assertions for a cache. */
		C cache(CacheConfig cache) { caches.add(requireNonNull(cache)); return thisAsCast(); }

		/** Map set of indices to nodeId in active mapping */
		C map(int nodeId, int...indices) { mapping.map(nodeId, indices); return thisAsCast(); }

		/** Map region of indices to nodeId in active mapping */
		C map(int nodeId, Interval indices) { mapping.map(nodeId, indices); return thisAsCast(); }

		/** Conditionally map set of indices to nodeId in active mapping */
		C map(boolean condition, int nodeId, int...indices) { if(condition) map(nodeId, indices); return thisAsCast(); }

		/** Add complete result entry to assert */
		C result(ResultConfig result) { results.add(requireNonNull(result)); return thisAsCast(); }

		/** Map a number of new entries depending on complex consumer */
		C results(int count, ObjIntConsumer<ResultConfig> action) {
			for (int i = 0; i < count; i++) {
				ResultConfig result = StructurePatternTest.result(results.size());
				action.accept(result, i);
				results.add(result);
			}
			return thisAsCast();
		}

		/** Map all elements of given interval to specified nodeId in separate results */
		C results(int nodeId, Interval...regions) {
			for(Interval region : regions) {
				ResultConfig result = StructurePatternTest.result(results.size());
				for (int i = 0; i < region.size(); i++) {
					result.map(nodeId, region.indexAt(i));
				}
				results.add(result);
			}
			return thisAsCast();
		}

		/** Map all elements of given set to specified nodeId in separate results */
		C results(int nodeId, int...indices) {
			for (int i = 0; i < indices.length; i++) {
				ResultConfig result = StructurePatternTest.result(results.size());
				result.map(nodeId, indices[i]);
				results.add(result);
			}
			return thisAsCast();
		}

		/** Turns a 3-level hit matrix into a basic match config for testing */
		C results(int matches,
				// [node_id][match_id][hits]
				int[][][] hits) {
			// Format of 'hits' matrix: [node_id][match_id][hits]
			return results(matches, (r, i) -> {
						for(int j = 0; j<hits.length; j++) {
							// Make sure we handle "empty" assignments
							if(hits[j].length>0) {
								r.map(j, hits[j][i]);
							}
						}
					})
					.modResults(ResultConfig::unordered);
		}

		C modResults(Consumer<? super ResultConfig> action) {
			results.forEach(action);
			return thisAsCast();
		}

		/** Asserts the dispatched state based on the list of expected results */
		@Override
		public void accept(State state) {
			StateProxy proxy = new StateProxy(state);

			assertThat(nextResult)
				.as("No more results buffered - only expected %d: %s",
						_int(results.size()), proxy) // use proxy to delay serialization till construction of error message
				.isLessThan(results.size());

			if(allowDuplicates==null || !allowDuplicates.booleanValue()) {
				String entry = proxy.toString();
				// Ignore empty mappings for duplicity checks
				if(!"{}".equals(entry) && !mappings.add(proxy.toString()))
					throw new AssertionError("Duplicate mapping: "+proxy.toString());
			}

			results.get(nextResult++).assertMapping(state);
		}

		void assertState(State state) {
			assertThat(state.reported)
				.as("Total number of matches")
				.isEqualTo(expectedCount.intValue());

			for (CacheConfig cache : caches) {
				cache.assertResult(startPos.intValue(), state);
			}

			mapping.assertMapping(state);

			for (NodeConfig node : nodes) {
				node.assertResult(state);
			}
		}

		void prepareState(State state) {

			if(!results.isEmpty()) {
				state.resultConsumer(this);
			}

			if(monitor!=null) {
				state.monitor(monitor);
			}
		}
	}

	static class RawTestConfig extends MatchConfig<RawTestConfig> {
		private State state;
		private Node root;

		RawTestConfig state(State state) {
			checkState("State already set", this.state==null);
			this.state = requireNonNull(state);
			return this;
		}

		RawTestConfig setup(StateMachineSetup setup) {
			checkState("State already set", this.state==null);
			checkState("Root already set", this.root==null);
			state = new State(setup);
			root = setup.root;
			return this;
		}

		RawTestConfig root(Node root) {
			checkState("Root already set", this.root==null);
			this.root = requireNonNull(root);
			return this;
		}

		RawTestConfig target(String s) {
			checkState("State not set", this.state!=null);
			requireNonNull(s);

			state.tree.rootFrame.length = s.length();
			state.tree.rootFrame.to(s.length()-1);
			state.elements = IntStream.range(0, s.length())
					.mapToObj(i -> item(i, s.charAt(i)))
					.toArray(Item[]::new);

			return this;
		}

		RawTestConfig tree(String encodedHeads) {
			checkState("State not set", state!=null);
			requireNonNull(encodedHeads);
			int[] parents = EvaluationUtils.parseTree(encodedHeads, encodedHeads.contains(" "));
			return tree(parents);
		}

		RawTestConfig tree(int[] parents) {
			checkState("State not set", state!=null);
			requireNonNull(parents);
			applyTree(state, parents);
			return this;
		}

		void assertResult() {
			checkState("State not set", state!=null);
			checkState("Root not set", root!=null);
			assertBasicSettings();

			prepareState(state);

			/*
			 *  Verify correct result (this does not use the full matcher API,
			 *  so we don't need to worry about a state reset messing up our expectations)
			 */
			assertThat(root.match(state, startPos()))
				.as("Result for start position %d", _int(startPos()))
				.isEqualTo(expectedResult());

			// Now perform deep validation of the final (internal) matcher state
			assertState(state);
		}
	}

	static RawTestConfig rawTest() { return new RawTestConfig(); }

	private static void assignMappingIds(IqlElement element) {
		MutableInteger id = new MutableInteger(0);
		EvaluationUtils.visitNodes(element, node -> {
			assertThat(node.getMappingId()).isEqualTo(UNSET_INT);
			node.setMappingId(id.getAndIncrement());
		});
	}

	static class MatcherTestConfig extends MatchConfig<MatcherTestConfig> {
		private StructurePattern.Builder builder;
		private StructurePattern pattern;
		private String target;
		private String query;
		private IqlElement root;
		private Boolean expand;
		private Boolean promote;
		private QueryConfig queryConfig;
		private Set<Option> options = EnumSet.noneOf(Option.class);
		private int[] tree;
		private List<Consumer<? super StructurePattern.Builder>> builderMods = new ArrayList<>();

		MatcherTestConfig() {
			startPos(0);
		}

		private void checkNoBuilder() {
			checkState("Builder already set", builder==null);
		}

		private void checkNoPattern() {
			checkState("Pattern already set", pattern==null);
		}

		MatcherTestConfig target(String target) {
			checkState("Target already set", this.target==null);
			this.target = requireNonNull(target);
			return this;
		}

		MatcherTestConfig builder(StructurePattern.Builder builder) {
			checkNoPattern();
			checkState("Builder already set", this.builder==null);
			this.builder = requireNonNull(builder);
			return this;
		}

		MatcherTestConfig pattern(StructurePattern pattern) {
			checkNoBuilder();
			checkState("Pattern already set", this.pattern==null);
			this.pattern = requireNonNull(pattern);
			return this;
		}

		MatcherTestConfig query(String query) {
			checkNoBuilder();
			checkNoPattern();
			checkState("Query already set", this.query==null);
			checkState("Root element already set", this.root==null);
			this.query = requireNonNull(query);
			return this;
		}

		MatcherTestConfig root(IqlElement root) {
			checkNoBuilder();
			checkNoPattern();
			checkState("Root element already set", this.root==null);
			checkState("Query already set", this.query==null);
			this.root = requireNonNull(root);
			return this;
		}

		MatcherTestConfig expand(boolean expand) {
			checkState("Expand flag already set", this.expand==null);
			this.expand = Boolean.valueOf(expand);
			return this;
		}

		MatcherTestConfig promote(boolean promote) {
			checkState("Promote flag already set", this.promote==null);
			this.promote = Boolean.valueOf(promote);
			return this;
		}

		MatcherTestConfig queryConfig(QueryConfig queryConfig) {
			checkState("Query config already set", this.queryConfig==null);
			this.queryConfig = requireNonNull(queryConfig);
			return this;
		}

		MatcherTestConfig modBuilder(Consumer<? super StructurePattern.Builder> action) {
			builderMods.add(action);
			return this;
		}

		MatcherTestConfig options(Option...additionalOptions) {
			checkNoBuilder();
			checkNoPattern();
			checkArgument("Empty options set", additionalOptions.length>0);
			options.addAll(set(additionalOptions));
			return this;
		}

		MatcherTestConfig options(boolean condition, Option...additionalOptions) {
			if(condition) {
				options(additionalOptions);
			}
			return this;
		}

		MatcherTestConfig tree(String encodedHeads) {
			checkState("Tree already set", this.tree==null);
			requireNonNull(encodedHeads);
			tree = EvaluationUtils.parseTree(encodedHeads, encodedHeads.contains(" "));
			return this;
		}

		MatcherTestConfig tree(int[] parents) {
			checkState("Tree already set", this.tree==null);
			this.tree = requireNonNull(parents);
			return this;
		}

		private static boolean isSet(Boolean b) { return b!=null && b.booleanValue(); }

		private void makeBuilderFromQueryElement(int size) {
			checkState("Root element not set", root!=null);

			assignMappingIds(root);

			IqlLane lane = mock(IqlLane.class);
			when(lane.getElement()).thenReturn(root);

			builder = StructurePattern.builder();
			builder.source(lane);
			builder.id(1);
			RootContext rootContext = EvaluationContext.rootBuilder(QueryTestUtils.dummyCorpus())
					.addEnvironment(SharedUtilityEnvironments.all())
					.build();
			LaneContext context = rootContext.derive()
					.lane(QueryTestUtils.lane())
					.build();
			builder.context(context);
			builder.initialBufferSize(size*2);
		}

		private void makeBuilderFromQueryString(int size) {
			checkState("Query not set", query!=null);

			String payloadString = query;
			if(isSet(expand)) {
				payloadString = QueryTestUtils.expand(payloadString);
			}

			IqlPayload payload = new QueryProcessor(options).processPayload(payloadString);
			assertThat(payload).as("No payload").isNotNull();
			assertThat(payload.getQueryType()).isEqualTo(QueryType.SINGLE_LANE);
			assertThat(payload.getLanes()).as("Missing lane").isNotEmpty();
			IqlLane lane = payload.getLanes().get(0);
			if(lane.getLaneType()==LaneType.TREE) {
				assertThat(tree).as("Must provide tree structure for tree query").isNotNull();
			}

			assignMappingIds(lane.getElement());

			builder = StructurePattern.builder();
			builder.source(lane);
			builder.id(1);
			RootContext rootContext = EvaluationContext.rootBuilder(QueryTestUtils.dummyCorpus())
					.addEnvironment(SharedUtilityEnvironments.all())
					.build();
			LaneContext context = rootContext.derive()
					.lane(lane)
					.build();
			builder.context(context);
			lane.getLimit().ifPresent(builder::limit);
			lane.getFlags().forEach(builder::flag);
			if(isSet(promote)) {
				builder.nodeTransform(PROMOTE_NODE);
			}
			builder.initialBufferSize(size*2);
		}

		private Container makeContainer() {
			return mockContainer(IntStream.range(0, target.length())
					.mapToObj(i -> item(i, target.charAt(i)))
					.toArray(Item[]::new));
		}

		void assertResult() {

			checkState("Target not set", target!=null);

			Container container = makeContainer();

			if(pattern==null) {

				if(builder==null) {
					if(root!=null) {
						makeBuilderFromQueryElement(strictToInt(container.getItemCount()));
					} else if(query!=null) {
						makeBuilderFromQueryString(strictToInt(container.getItemCount()));
					}
				}

				checkState("Builder not set", builder!=null);

				if(!builderMods.isEmpty()) {
					builderMods.forEach(action -> action.accept(builder));
				}

				if(builder.geRole()==null) {
					builder.role(Role.SINGLETON);
				}

				pattern = builder.build();
			}

			checkState("Pattern not set", pattern!=null);

			if(queryConfig!=null) {
				queryConfig.assertQuery(pattern.getSource().getElement());
			}

			NonResettingMatcher matcher = pattern.matcherForTesting();

			prepareState(matcher);

			if(tree!=null) {
				applyTree(matcher, tree);
			}

			// Verify correct result
			assertThat(matcher.matches(0, container))
				.as("Result mismatch")
				.isEqualTo(expectedResult());

			/*
			 * Reset only the temporary utility stuff and external references.
			 * We still need the caches for our assertions!!
			 */
			matcher.softReset();

			// Now perform deep validation of the final (internal) matcher state
			assertState(matcher);
		}
	}

	static MatcherTestConfig matcherTest() { return new MatcherTestConfig(); }

	/** Create a expanding test for the given query and target string. */
	static MatcherTestConfig expandingMatcherTest(String query, String target) {
		return matcherTest()
				.target(target)
				.expand(true)
				.query(query);
	}

	/** Applies a tree consisting of parent links to the TreeFrame list in the given State */
	static void applyTree(State state, int[] parents) {
		TreeManager tree = state.tree;
		final TreeFrame[] frames = tree.frames;
		for (int i = 0; i < parents.length; i++) {
			TreeFrame frame = frames[i];
			frame.length = 0;
			frame.height = 0;
			frame.descendants = 0;
		}
		for (int i = 0; i < parents.length; i++) {
			int parentIndex = parents[i];
			TreeFrame frame = frames[i];
			frame.parent = parentIndex;
			frame.valid = true;

			if(parentIndex != UNSET_INT) {
				TreeFrame parent = frames[parentIndex];
				parent.indices[parent.length++] = i;
				parent.descendants++;
			} else {
				tree.roots[tree.rootCount++] = frame.index;
			}
		}

		final RootFrame rootFrame = tree.rootFrame;
		for (int i = 0; i < tree.rootCount; i++) {
			TreeFrame root = frames[tree.roots[i]];
			computeTreeData(tree, root, 1);
			rootFrame.height = Math.max(rootFrame.height, root.height+1);
			rootFrame.descendants += root.descendants + 1;
		}
	}

	private static void computeTreeData(TreeManager tree, TreeFrame frame, int depth) {
		frame.depth = depth;

		if(frame.length>0) {
			for (int i = 0; i < frame.length; i++) {
				TreeFrame child = tree.frames[frame.indices[i]];
				computeTreeData(tree, child, depth+1);
				frame.descendants += child.descendants + 1;
				frame.height = Math.max(frame.height, child.height+1);
			}
		}
	}

	interface NodeTest {
		StateMachineSetup setup();
	}

	private static class StateProxy {
		private final State state;
		private String text;
		StateProxy(State state) { this.state = requireNonNull(state); }
		@Override
		public String toString() {
			if(text==null) {
				StringBuilder sb = new StringBuilder();
				sb.append('{');
				for(int i=0; i<state.entry; i++) {
					if(i>0) {
						sb.append(',');
					}
					sb.append(state.m_node[i])
						.append("->")
						.append(state.m_index[i]);
				}
				sb.append('}');
				text = sb.toString();
			}
			return text;
		}
	}

	private static class MappingProxy {
		private final List<Pair<Integer, Integer>> mapping;
		private String text;
		MappingProxy(List<Pair<Integer, Integer>> mapping) { this.mapping = requireNonNull(mapping); }
		@Override
		public String toString() {
			if(text==null) {
				StringBuilder sb = new StringBuilder();
				sb.append('{');
				for(Iterator<Pair<Integer, Integer>> i = mapping.iterator(); i.hasNext();) {
					Pair<Integer, Integer> m = i.next();
					sb.append(m.first)
						.append("->")
						.append(m.second);
					if(i.hasNext()) {
						sb.append(',');
					}
				}
				sb.append('}');
				text = sb.toString();
			}
			return text;
		}
	}

	private static class CacheConfig {
		private final int cacheId;
		private final boolean expectWindowSet;
		/** The range of target elements we expect to be visited. If null, only startPos should be used. */
		private Interval[] window;
		private final IntSet hits = new IntOpenHashSet();
		private final IntSet set = new IntOpenHashSet();

		private CacheConfig(int cacheId, boolean expectWindowSet) {
			this.cacheId = cacheId;
			this.expectWindowSet = expectWindowSet;
		}

		CacheConfig window(Interval...window) { this.window = window; return this; }

		CacheConfig window(int from, int to) { window = new Interval[] {Interval.of(from, to)}; return this; }

		CacheConfig window(String target) { window = new Interval[] {Interval.of(0, target.length()-1)}; return this; }

		CacheConfig window(int spot) { window = new Interval[] {Interval.of(spot)}; return this; }

		CacheConfig hits(int...indices) { for(int i=0; i< indices.length; i++) hits.add(indices[i]); return this; }

		CacheConfig hits(int[]...indices) {
			Stream.of(indices).forEach(this::hits);
			return this;
		}

		CacheConfig hits(Interval...regions) {
			Stream.of(regions).map(Interval::asArray).forEach(this::hits);
			return this;
		}

		CacheConfig hits(boolean condition, int...indices) { if(condition) hits(indices); return this; }

		CacheConfig hitsForWindow() { return hits(requireNonNull(window, "Window undefined")); }

		CacheConfig set(int...indices) { for(int i=0; i< indices.length; i++) set.add(indices[i]); return this; }

		CacheConfig set(Interval...regions) {
			Stream.of(regions).map(Interval::asArray).forEach(this::set);
			return this;
		}

		CacheConfig set(boolean condition, int...indices) { if(condition) set(indices); return this; }

		CacheConfig setForWindow() { return set(requireNonNull(window, "Window undefined")); }

		CacheConfig unset(int...indices) { for(int i=0; i< indices.length; i++) set.remove(indices[i]); return this; }

		CacheConfig unset(Interval indices) { return unset(indices.asArray()); }

		CacheConfig hits(String s, CharPredicate pred) {
			for (int i = 0; i < s.length(); i++) {
				if(pred.test(s.charAt(i))) {
					hits.add(i);
				}
			}
			return this;
		}

		CacheConfig hitsForSet(String s, CharPredicate pred) {
			for (int i = 0; i < s.length(); i++) {
				if(set.contains(i) && pred.test(s.charAt(i))) {
					hits.add(i);
				}
			}
			return this;
		}

		CacheConfig hits(String s, Interval clip, CharPredicate pred) {
			for (int i = 0; i < s.length(); i++) {
				if(clip.contains(i) && pred.test(s.charAt(i))) {
					hits.add(i);
				}
			}
			return this;
		}

		CacheConfig hitsExcept(String s, int...exceptions) {
			for (int i = 0; i < s.length(); i++) {
				hits.add(i);
			}
			for (int exception : exceptions) {
				hits.remove(exception);
			}
			return this;
		}

		void assertResult(int startPos, State state) {
			Interval[] window = this.window;
			if(window==null) {
				window = new Interval[] {Interval.of(startPos)};
			}
			if(window.length==0) {
				return;
			}
			IntSet checked = new IntOpenHashSet();
			Cache cache = state.caches[cacheId];
			for(Interval iv : window) {
				if(iv.isEmpty()) {
					continue;
				}
				for (int i = iv.from; i <= iv.to; i++) {
					if(!checked.add(i)) {
						continue;
					}
					if(expectWindowSet) {
						assertThat(cache.isSet(i))
							.as("Cache %d slot not set for index %d", _int(cacheId), _int(i))
							.isTrue();
					} else {
						assertThat(cache.isSet(i))
							.as("Cache %d set mismatch for index %d", _int(cacheId), _int(i))
							.isEqualTo(set.contains(i));
					}
					assertThat(cache.getValue(i))
						.as("Cache %d mismatch at index %d", _int(cacheId), _int(i))
						.isEqualTo(hits.contains(i));
				}
			}
		}
	}

	static CacheConfig cache(int cacheId, boolean expectWindowSet) {
		return new CacheConfig(cacheId, expectWindowSet);
	}

	private static class NodeConfig {
		private final int nodeId;
		private int last = UNSET_INT;

		private NodeConfig(int nodeId) { this.nodeId = nodeId; }

		void assertResult(State state) {
			if(last!=UNSET_INT) {
				assertThat(state.hits.get(nodeId))
					.as("Last hit for node %d", _int(nodeId))
					.isEqualTo(last);
			}
		}

		NodeConfig last(int last) { this.last = last; return this; }

		NodeConfig last(boolean condition, int last) { if(condition) last(last); return this; }
	}

	static NodeConfig node(int nodeId) {
		return new NodeConfig(nodeId);
	}

	static class ResultConfig {
		private final int index;
		private final List<Pair<Integer, Integer>> mapping = new ArrayList<>();
		private boolean ignoreOrder = false;

		ResultConfig(int index) { this.index = index; }

		ResultConfig ignoreOrder(boolean ignoreOrder) { this.ignoreOrder = ignoreOrder; return this; }
		ResultConfig ordered() { return ignoreOrder(false); }
		ResultConfig unordered() { return ignoreOrder(true); }

		ResultConfig map(int nodeId, int...indices) {
			IntStream.of(indices).mapToObj(pos -> Pair.pair(nodeId, pos)).forEach(mapping::add);
			return this;
		}

		ResultConfig map(int nodeId, Interval...indices) {
			Stream.of(indices)
				.filter(i -> !i.isEmpty())
				.flatMapToInt(Interval::stream)
				.mapToObj(pos -> Pair.pair(nodeId, pos))
				.forEach(mapping::add);
			return this;
		}

		ResultConfig map(boolean condition, int nodeId, int...indices) { if(condition) map(nodeId, indices); return this;}
		ResultConfig map(boolean condition, int nodeId, Interval...indices) { if(condition) map(nodeId, indices); return this;}

		ResultConfig sortByNodeId() {
			mapping.sort((p1, p2) -> p1.first.compareTo(p2.first));
			return this;
		}

		ResultConfig sortByIndex() {
			mapping.sort((p1, p2) -> p1.second.compareTo(p2.second));
			return this;
		}

		void assertMapping(State state) {
			StateProxy stateProxy = new StateProxy(state);
			MappingProxy mappingProxy = new MappingProxy(mapping);

			assertThat(state.entry)
				.as("Incorrect number of mappings in result #%d:\nexpected: %s\ngiven: %s",
						_int(index), mappingProxy, stateProxy)
				.isEqualTo(mapping.size());

			if(ignoreOrder) {
				assertUnordered(state, stateProxy);
			} else {
				assertOrdered(state, stateProxy);
			}
		}

		private void assertUnordered(State state, StateProxy proxy) {
			Set<Pair<Integer, Integer>> entries = new LinkedHashSet<>(mapping);
			for (int i = 0; i < mapping.size(); i++) {
				Pair<Integer, Integer> m = Pair.pair(state.m_node[i], state.m_index[i]);
				if(!entries.remove(m))
					throw new AssertionError(String.format("Unexpected mapping at index %d in result #%d:\ngiven: %s\nexpected: %s]",
							_int(index), _int(i), proxy, mapping));
			}

			if(!entries.isEmpty())
				throw new AssertionError(String.format("Missing %d entries in result: %s", _int(entries.size()), entries));
		}

		private void assertOrdered(State state, StateProxy proxy) {
			for (int i = 0; i < mapping.size(); i++) {
				Pair<Integer, Integer> m = mapping.get(i);
				assertThat(state.m_node[i])
					.as("Node id mismatch in mapping at index %d in result #%d:\n%s",
							_int(i), _int(index), proxy)
					.isEqualTo(m.first.intValue());
				assertThat(state.m_index[i])
					.as("Position mismatch in mapping at index %d in result #%d for node %d:\n%s",
							_int(i), _int(index), _int(state.m_node[i]), proxy)
					.isEqualTo(m.second.intValue());
			}
		}
	}

	static ResultConfig result(int index) {
		return new ResultConfig(index);
	}

	static class SM_Config {
		private final List<SM_NodeConfig<?>> nodes = new ArrayList<>();
		//TODO for testing the structure of the state machine
	}

	static class SM_NodeConfig<N extends Node> {
		String label;
		int id = UNSET_INT;
		Class<N> type;
		BiConsumer<SM_Config,N> asserter;
		//TODO for testing the structure of the state machine
	}

	static class ProcessorUtils {
		//TODO utility functions to create asserters for verifying the state machine graph
	}

	// TESTS BEGIN HERE

	static class RepetitionUtils {

		interface ClosedBase extends NodeTest {

			@CsvSource({
				"-, 0, 0",
				"X, 0, 0",
				"X-, 0, 0-1",
				"-X, 0, 0",
				"XY, 0, 0-1",
				"XYX, 0, 0-1",
				"XXY, 1, 1-2",
			})
			@ParameterizedTest(name="{index}: X'{2,5}' in [{0}], start at {1}")
			default void testFail(String target, int startPos,
					@IntervalArg Interval window) {
				rawTest()
				.setup(setup())
				.target(target)
				.startPos(startPos)
				.expectFail()
				.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
				.assertResult();
			}

			@CsvSource({
				"XX, 0, 0, 1",
				"-XX, 1, 1-2, 2",
				"YXX, 1, 1-2, 2",
				"-XX-, 1, 1-3, 2",
			})
			@ParameterizedTest(name="{index}: X'{2,5}' in [{0}], start at {1}")
			default void testFindMinimum(String target, int startPos,
					@IntervalArg Interval window, int last) {
				rawTest()
				.setup(setup())
				.target(target)
				.startPos(startPos)
				.expectSuccess(1)
				.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
				.map(NODE_0, startPos, startPos+1)
				.node(node(NODE_0).last(last))
				.assertResult();
			}

			@CsvSource({
				"XXXXX, 0, 0-4, 4",
				"-XXXXX, 1, 1-5, 5",
				"YXXXXXZ, 1, 1-5, 5",
				"-XXXXXX, 1, 1-5, 5",
			})
			@ParameterizedTest(name="{index}: X'{2,5}' in [{0}], start at {1}")
			default void testFindMaximum(String target, int startPos,
					@IntervalArg Interval window, int last) {
				rawTest()
				.setup(setup())
				.target(target)
				.startPos(startPos)
				.expectSuccess(1)
				.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
				.map(NODE_0, Interval.of(startPos, startPos+4))
				.node(node(NODE_0).last(last))
				.assertResult();
			}
		}

		interface OpenBase extends NodeTest {

			@CsvSource({
				"-, 0, 0",
				"X, 0, 0",
				"X-, 0, 0-1",
				"-X, 0, 0",
				"XY, 0, 0-1",
				"XYX, 0, 0-1",
				"XXY, 1, 1-2",
			})
			@ParameterizedTest(name="{index}: X'{2,}' in [{0}], start at {1}")
			default void testFail(String target, int startPos,
					@IntervalArg Interval window) {
				rawTest()
				.setup(setup())
				.target(target)
				.startPos(startPos)
				.expectFail()
				.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
				.assertResult();
			}

			@CsvSource({
				"XX, 0, 0-1, 1",
				"-XX, 1, 1-2, 2",
				"YXX, 1, 1-2, 2",
				"-XX-, 1, 1-3, 2",
			})
			@ParameterizedTest(name="{index}: X'{2,}' in [{0}], start at {1}")
			default void testFindMinimum(String target, int startPos,
					@IntervalArg Interval window, int last) {
				rawTest()
				.setup(setup())
				.target(target)
				.startPos(startPos)
				.expectSuccess(1)
				.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
				.map(NODE_0, startPos, startPos+1)
				.node(node(NODE_0).last(last))
				.assertResult();
			}

			@CsvSource({
				"XXXXX, 0, 0-4, 0-4, 4",
				"-XXXXX, 1, 1-5, 1-5, 5",
				"YXXXXXZ, 1, 1-6, 1-5, 5",
				"-XXXXXXXX, 1, 1-8, 1-8, 8",
			})
			@ParameterizedTest(name="{index}: X'{2,}' in [{0}], start at {1}")
			default void testFindMaximum(String target, int startPos,
					@IntervalArg Interval window,
					@IntervalArg Interval matched, int last) {
				rawTest()
				.setup(setup())
				.target(target)
				.startPos(startPos)
				.expectSuccess(1)
				.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
				.map(NODE_0, matched)
				.node(node(NODE_0).last(last))
				.assertResult();
			}

		}
	}

	@Test
	void testBuilder() {
		assertThat(StructurePattern.builder()).isNotNull();
	}

	@Nested
	class ForUtilityClasses {

		/**
		 * Tests for {@link TreeManager}
		 */
		@Nested
		class ForTreeManager {

			private TreeManager tree;
			private Container host;

			@BeforeEach
			void setUp() {
				tree = new TreeManager(10);
				host = mockContainer();
			}

			@AfterEach
			void tearDown() {
				tree.reset(10);
				tree = null;
			}

			private void assertFrame(String msg, TreeFrame frame, int parent, int height, int depth, int desc,
					int...children) {
				assertThat(frame.valid).as(msg+" Valid").isTrue();
				assertThat(frame.parent).as(msg+" Parent").isEqualTo(parent);
				assertThat(frame.height).as(msg+" Height").isEqualTo(height);
				assertThat(frame.depth).as(msg+" Depth").isEqualTo(depth);
				assertThat(frame.descendants).as(msg+" Descendant count").isEqualTo(desc);
				assertThat(frame.length).as(msg+" Child count").isEqualTo(children.length);
				if(children.length>0) {
					assertThat(frame.indices).as(msg+" Children").startsWith(children);
				}
			}

			/**
			 * Test for {@link TreeManager#init(Structure)}
			 */
			@Test
			void testInit() {
				Structure s = TreeStructure.SINGLETON.apply(host);

				tree.init(s);

				assertThat(tree.rootCount).isEqualTo(1);
				assertThat(tree.roots[0]).isEqualTo(0);
			}

			/**
			 * Tests for {@link TreeManager#r}
			 */
			@Nested
			class ForForceRefreshAllFrames {

				@Test
				void testBalanced3NodeTree() {
					Structure s = TreeStructure.TREE_3_BALANCED.apply(host);
					tree.init(s);

					tree.forceRefreshAllFrames();

					assertFrame("root: ", tree.rootFrame, UNSET_INT, 2, 0, 3, 0);
					assertFrame("frame_0: ", tree.frames[0], UNSET_INT, 1, 1, 2, 1, 2);
					assertFrame("frame_1: ", tree.frames[1], 0, 0, 2, 0);
					assertFrame("frame_2: ", tree.frames[2], 0, 0, 2, 0);
				}

				@Test
				void test3NodeChain() {
					Structure s = TreeStructure.CHAIN_3.apply(host);
					tree.init(s);

					tree.forceRefreshAllFrames();

					assertFrame("root: ", tree.rootFrame, UNSET_INT, 3, 0, 3, 0);
					assertFrame("frame_0: ", tree.frames[0], UNSET_INT, 2, 1, 2, 1);
					assertFrame("frame_1: ", tree.frames[1], 0, 1, 2, 1, 2);
					assertFrame("frame_2: ", tree.frames[2], 1, 0, 3, 0);
				}
			}

			@Nested
			class ForIsOrderedStructure {

				void testNullManifest() {
					Structure s = mock(Structure.class);
					tree.init(s);
					assertThat(tree.isOrderedStructure()).isFalse();
				}

				void testUnorderedManifest() {
					Structure s = mock(Structure.class);
					when(s.getManifest()).thenReturn(mock(StructureManifest.class));
					tree.init(s);
					assertThat(tree.isOrderedStructure()).isFalse();
				}

				@SuppressWarnings("boxing")
				void testOrderedManifest() {
					Structure s = mock(Structure.class);
					StructureManifest m = mock(StructureManifest.class);
					when(s.getManifest()).thenReturn(m);
					when(m.isStructureFlagSet(eq(StructureFlag.ORDERED))).thenReturn(Boolean.TRUE);
					tree.init(s);
					assertThat(tree.isOrderedStructure()).isTrue();
				}
			}

			//TODO
		}

		/**
		 * Tests for {@link TreeFrame}
		 */
		@Nested
		class ForTreeFrame {

			private TreeFrame frame(int...indices) {
				TreeFrame frame = new TreeFrame(0, indices.length);
				System.arraycopy(indices, 0, frame.indices, 0, indices.length);
				frame.length = indices.length;
				frame.rewind();
				return frame;
			}

			@ParameterizedTest
			@CsvSource({
				"{0;1;2;3;4}, 1, 2, {1;2}",
				"{0;1;2;3;4}, 0, 2, {0;1;2}",
				"{1;2;3;4}, 0, 2, {0;1}",
				"{3;4}, 0, 2, -",
				"{0;3;4}, 1, 1, -",
				"{0;3;4}, 1, 2, -",
				"{0;3;4}, 5, 7, -",
			})
			void testRetainIndices(@IntArrayArg int[] indices,
					int from,  int to, @IntArrayArg int[] expected) {
				TreeFrame frame = frame(indices);
				assertThat(frame.retainIndices(Interval.of(from, to)))
					.as("Retain call result: %s", frame.window)
					.isEqualTo(expected.length > 0);

				assertThat(frame.window.asArray()).containsExactly(expected);
			}

			//TODO
		}

		/**
		 * Tests for {@link Cache}
		 */
		@Nested
		class ForCache {

			private Cache cache;

			@BeforeEach
			void setUp() { cache = new Cache(20); }

			@AfterEach
			void tearDown() { cache = null; }

			@Test
			@RandomizedTest
			void testIsSet(RandomGenerator rng) {
				int index = rng.nextInt(cache.size());
				boolean value = rng.nextBoolean();

				assertThat(cache.isSet(index))
					.as("Innit fail index %d", _int(index))
					.isFalse();

				cache.setValue(index, value);

				assertThat(cache.isSet(index))
					.as("Unset index %d", _int(index))
					.isTrue();
			}

			@Test
			@RandomizedTest
			void testGetValue(RandomGenerator rng) {
				int index = rng.nextInt(cache.size());
				boolean value = rng.nextBoolean();

				cache.setValue(index, value);

				assertThat(cache.getValue(index))
					.as("Value mismatch at index %d", _int(index))
					.isEqualTo(value);
			}

			//TODO test reset(int)?
		}

		/**
		 * Tests for {@link LevelFilter}
		 */
		@Nested
		class ForLevelFilter {
			//TODO
		}
	}

	/**
	 * Test family for raw {@link Node} implementations.
	 */
	@Nested
	class ForRawNodes {

		private int id = 0;

		private int id() { return id++; }

		@Nested
		class ForSingle {

			@Nested
			class WithOneNode implements NodeTest {

				@Override
				public StateMachineSetup setup() {
					StateMachineSetup sms = new StateMachineSetup();
					sms.mappedNodes = mockMappedNodes(1);
					sms.cacheCount = 1;
					sms.initialSize = 10;
					sms.root = seq(
							new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
							new Finish(id(), NO_LIMIT, false));
					sms.matchers = matchers(matcher(0, EQUALS_X));
					return sms;
				}

				@DisplayName("1 node with cache and mapping")
				@CsvSource({
					"-, 0, false",
					"X, 0, true",
					"-X-, 0, false",
					"-X-, 1, true",
					"-X-, 2, false",
				})
				@ParameterizedTest(name="{index}: X in [{0}], start at {1}, match={2}")
				void testFull(String target, int startPos, boolean expectedResult) {
					rawTest()
					.setup(setup())
					.target(target)
					.startPos(startPos)
					.expectedResult(expectedResult)
					.expectedCount(expectedResult ? 1 : 0)
					.cache(cache(CACHE_0, true).hits(expectedResult, startPos))
					.map(expectedResult, NODE_0, startPos)
					.node(node(NODE_0).last(expectedResult, startPos))
					.assertResult();
				}
			}

			@Nested
			class WithTwoNodes implements NodeTest {

				@Override
				public StateMachineSetup setup() {
					StateMachineSetup sms = new StateMachineSetup();
					sms.mappedNodes = mockMappedNodes(2);
					sms.cacheCount = 2;
					sms.initialSize = 10;
					sms.root = seq(
							new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
							new Single(id(), mock(IqlNode.class), MAP_1, NODE_1, CACHE_1, NO_MEMBER, NO_ANCHOR),
							new Finish(id(), NO_LIMIT, false));
					sms.matchers = matchers(matcher(0, EQUALS_X), matcher(1, EQUALS_Y));
					return sms;
				}

				@DisplayName("2 adjacent nodes with cache and mapping")
				@CsvSource({
					"--, 0, false, false, false",
					"X-, 0, false, true, false",
					"-Y, 0, false, false, false",
					"XY, 0, true, true, true",
					"-X--, 1, false, true, false",
					"--Y-, 1, false, false, false",
					"-XY-, 0, false, false, false",
					"-XY-, 1, true, true, true",
					"-XY-, 2, false, false, false",
				})
				@ParameterizedTest(name="{index}: XY in [{0}], start at {1}, match={2}")
				void testFull(String target, int startPos, boolean expectedResult,
						boolean node1Hit, boolean node2Hit) {
					rawTest()
					.setup(setup())
					.target(target)
					.startPos(startPos)
					.expectedResult(expectedResult)
					.expectedCount(expectedResult ? 1 : 0)
					.cache(cache(CACHE_0, true)
							.window(startPos)
							.hits(node1Hit, startPos))
					.cache(cache(CACHE_1, false)
							.window(startPos+1)
							.hits(node2Hit, startPos+1)
							.set(node1Hit, startPos+1))
					.map(node1Hit, NODE_0, startPos)
					.map(node2Hit, NODE_1, startPos+1)
					.node(node(NODE_0).last(expectedResult, startPos))
					.node(node(NODE_1).last(expectedResult, startPos+1))
					.assertResult();
				}
			}
		}

		@Nested
		class ForScan {

			@Nested
			class WithoutRegion {

				@Nested
				class ForwardSingle {

					StateMachineSetup setup(int limit) {
						StateMachineSetup sms = new StateMachineSetup();
						sms.mappedNodes = mockMappedNodes(1);
						sms.cacheCount = 1;
						sms.limit = limit;
						sms.initialSize = 10;
						sms.root = seq(
								new Exhaust(id(), true),
								new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
								new Finish(id(), limit, false));
						sms.matchers = matchers(matcher(0, EQUALS_X));
						return sms;
					}

					@CsvSource({
						"-, 0",
						"--, 0",
						"--, 1",
						"-X-, 2",
					})
					@ParameterizedTest(name="{index}: X in [{0}], start at {1}")
					void testFail(String target, int startPos) {
						rawTest()
						.setup(setup(NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectFail()
						.cache(cache(CACHE_0, true)
								.window(startPos, target.length()-1))
						.assertResult();
					}

					@DisplayName("scan of 1 match")
					@CsvSource({
						"X, 0, 0",

						"X--, 0, 0",
						"-X-, 0, 1",
						"--X, 0, 2",

						"-X-, 1, 1",
					})
					@ParameterizedTest(name="{index}: X in [{0}], start at {1}")
					void testFindSingle(String target, int startPos, int hit) {
						rawTest()
						.setup(setup(NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectSuccess(1)
						.cache(cache(CACHE_0, true)
								.window(startPos, target.length()-1)
								.hits(hit))
						.node(node(NODE_0).last(hit))
						.result(result(0).map(NODE_0, hit))
						.assertResult();
					}

					@DisplayName("scan of up to 3 matches")
					@CsvSource({
						"X--, 0, 1, 0",
						"XX-, 0, 2, 1",
						"XXX, 0, 3, 2",
						"-XX, 0, 2, 2",
						"-XX, 1, 2, 2",
						"XXX, 1, 2, 2",
						"X-X, 0, 2, 2",
					})
					@ParameterizedTest(name="{index}: X in [{0}], start at {1}, count={2}")
					void testMultiMatch(String target, int startPos, int matchCount, int last) {
						rawTest()
						.setup(setup(NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectSuccess(matchCount)
						.cache(cache(CACHE_0, true)
								.window(startPos, target.length()-1)
								.hits(target, EQUALS_X))
						.node(node(NODE_0).last(last))
						.assertResult();
					}

					@DisplayName("scan of up to 3 matches with limit")
					@CsvSource({
						"X--, 1, 0, 1, 0",

						"XX-, 1, 0, 1, 0",
						"XX-, 2, 0, 2, 1",

						"XXX, 1, 0, 1, 0",
						"XXX, 2, 0, 2, 1",
						"XXX, 3, 0, 3, 2",

						"-XX, 1, 0, 1, 1",
						"-XX, 2, 0, 2, 2",

						"-XX, 1, 1, 1, 1",
						"-XX, 2, 1, 2, 2",

						"XXX, 1, 1, 1, 1",
						"XXX, 2, 1, 2, 2",

						"X-X, 1, 0, 1, 0",
						"X-X, 2, 0, 2, 2",
					})
					@ParameterizedTest(name="{index}: X in [{0}], limit= {1}, start at {2}, count={3}")
					void testLimitedMultiMatch(String target, int limit, int startPos, int matchCount, int last) {
						rawTest()
						.setup(setup(limit))
						.target(target)
						.startPos(startPos)
						.expectSuccess(matchCount)
						.cache(cache(CACHE_0, true)
								.window(startPos, last)
								.hits(target, EQUALS_X))
						.node(node(NODE_0).last(last))
						.assertResult();
					}
				}

				@Deprecated
				@Nested
				class ForwardSingleCached {

					StateMachineSetup setup(int limit) {
						StateMachineSetup sms = new StateMachineSetup();
						sms.mappedNodes = mockMappedNodes(1);
						sms.cacheCount = 1;
						sms.limit = limit;
						sms.initialSize = 10;
						sms.root = seq(
								new Exhaust(id(), true),
								new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
								new Finish(id(), limit, false));
						sms.matchers = matchers(matcher(0, EQUALS_X));
						return sms;
					}

					@CsvSource({
						"-, 0",
						"---, 0",
						"-X-, 2",
					})
					@ParameterizedTest(name="{index}: X in [{0}], start at {1}")
					void testFail(String target, int startPos) {
						rawTest()
						.setup(setup(NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectFail()
						.cache(cache(CACHE_0, true)
								.window(startPos, target.length()-1))
						.assertResult();
					}

					@DisplayName("scan of 1 match")
					@CsvSource({
						"X, 0, 0",

						"X--, 0, 0",
						"-X-, 0, 1",
						"--X, 0, 2",

						"-X-, 1, 1",
					})
					@ParameterizedTest(name="{index}: X in [{0}], start at {1}")
					void testSingleMatch(String target, int startPos, int hit) {
						rawTest()
						.setup(setup(NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectSuccess(1)
						.cache(cache(CACHE_0, true)
								.window(startPos, target.length()-1)
								.hits(target, EQUALS_X))
						.node(node(NODE_0).last(hit))
						.result(result(0).map(NODE_0, hit))
						.assertResult();
					}

					@DisplayName("scan of up to 3 matches")
					@CsvSource({
						"X--, 0, 1, 0",
						"XX-, 0, 2, 1",
						"XXX, 0, 3, 2",
						"-XX, 0, 2, 2",
						"-XX, 1, 2, 2",
						"XXX, 1, 2, 2",
						"X-X, 0, 2, 2",
					})
					@ParameterizedTest(name="{index}: X in [{0}], start at {1}")
					void testMultiMatch(String target, int startPos, int matchCount, int last) {
						rawTest()
						.setup(setup(NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectSuccess(matchCount)
						.cache(cache(CACHE_0, true)
								.window(startPos, target.length()-1).
								hits(target, EQUALS_X))
						.node(node(NODE_0).last(last))
						.assertResult();
					}

					@DisplayName("scan of up to 3 matches with limit")
					@CsvSource({
						"X--, 1, 0, 1, 0",

						"XX-, 1, 0, 1, 0",
						"XX-, 2, 0, 2, 1",

						"XXX, 1, 0, 1, 0",
						"XXX, 2, 0, 2, 1",
						"XXX, 3, 0, 3, 2",

						"-XX, 1, 0, 1, 1",
						"-XX, 2, 0, 2, 2",

						"-XX, 1, 1, 1, 1",
						"-XX, 2, 1, 2, 2",

						"XXX, 1, 1, 1, 1",
						"XXX, 2, 1, 2, 2",

						"X-X, 1, 0, 1, 0",
						"X-X, 2, 0, 2, 2",
					})
					@ParameterizedTest(name="{index}: X in [{0}], limit= {1}, start at {2}, count={3}")
					void testLimitedMultiMatch(String target, int limit, int startPos,
							int matchCount, int last) {
						rawTest()
						.setup(setup(limit))
						.target(target)
						.startPos(startPos)
						.expectSuccess(matchCount)
						.cache(cache(CACHE_0, true)
								.window(startPos, last)
								.hits(target, EQUALS_X))
						.node(node(NODE_0).last(last))
						.assertResult();
					}
				}

				@Nested
				class BackwardSingle {

					StateMachineSetup setup(int  limit) {
						StateMachineSetup sms = new StateMachineSetup();
						sms.mappedNodes = mockMappedNodes(1);
						sms.cacheCount = 1;
						sms.limit = limit;
						sms.initialSize = 10;
						sms.root = seq(
								new Exhaust(id(), false),
								new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
								new Finish(id(), limit, false));
						sms.matchers = matchers(matcher(0, EQUALS_X));
						return sms;
					}

					@CsvSource({
						"-, 0",
						"---, 0",
						"-X-, 2",
					})
					@ParameterizedTest(name="{index}: X in [{0}], start at {1}")
					void testFail(String target, int startPos) {
						rawTest()
						.setup(setup(NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectFail()
						.cache(cache(CACHE_0, true)
								.window(startPos, target.length()-1))
						.assertResult();
					}

					@DisplayName("scan of 1 match")
					@CsvSource({
						"X, 0, 0",

						"X--, 0, 0",
						"-X-, 0, 1",
						"--X, 0, 2",

						"-X-, 1, 1",
					})
					@ParameterizedTest(name="{index}: X in [{0}], start at {1}")
					void testSingleMatch(String target, int startPos, int hit) {
						rawTest()
						.setup(setup(NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectSuccess(1)
						.cache(cache(CACHE_0, true)
								.window(startPos, target.length()-1)
								.hits(target, EQUALS_X))
						.node(node(NODE_0).last(hit))
						.result(result(0).map(NODE_0, hit))
						.assertResult();
					}

					@DisplayName("scan of up to 3 matches")
					@CsvSource({
						"X--, 0, 1, 0-2, 0",
						"XX-, 0, 2, 0-2, 0",
						"XXX, 0, 3, 0-2, 0",
						"-XX, 0, 2, 0-2, 1",
						"-XX, 1, 2, 1-2, 1",
						"XXX, 1, 2, 1-2, 1",
						"X-X, 0, 2, 0-2, 0",
					})
					@ParameterizedTest(name="{index}: X in [{0}], start at {1}, count={2}")
					void testMultiMatch(String target, int startPos, int matchCount,
							@IntervalArg Interval window, int last) {
						rawTest()
						.setup(setup(NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectSuccess(matchCount)
						.cache(cache(CACHE_0, true)
								.window(window)
								.hits(target, EQUALS_X))
						.node(node(NODE_0).last(last))
						.assertResult();
					}

					@DisplayName("scan of up to 3 matches with limit")
					@CsvSource({
						"X--, 1, 0, 1, 0",

						"XX-, 1, 0, 1, 1",
						"XX-, 2, 0, 2, 0",

						"XXX, 1, 0, 1, 2",
						"XXX, 2, 0, 2, 1",
						"XXX, 3, 0, 3, 0",

						"-XX, 1, 0, 1, 2",
						"-XX, 2, 0, 2, 1",

						"-XX, 1, 1, 1, 2",
						"-XX, 2, 1, 2, 1",

						"XXX, 1, 1, 1, 2",
						"XXX, 2, 1, 2, 1",

						"X-X, 1, 0, 1, 2",
						"X-X, 2, 0, 2, 0",
					})
					@ParameterizedTest(name="{index}: X in [{0}], limit= {1}, start at {2}, count={3}")
					void testLimitedMultiMatch(String target, int limit, int startPos, int matchCount, int last) {
						rawTest()
						.setup(setup(limit))
						.target(target)
						.startPos(startPos)
						.expectSuccess(matchCount)
						.cache(cache(CACHE_0, true)
								.window(last, target.length()-1)
								.hits(target, EQUALS_X))
						.node(node(NODE_0).last(last))
						.assertResult();
					}
				}

			}

			@Nested
			class WithRegion {

				@Nested
				class ForwardSingle {

					StateMachineSetup setup(Interval region, int limit) {
						StateMachineSetup sms = new StateMachineSetup();
						sms.mappedNodes = mockMappedNodes(1);
						sms.cacheCount = 1;
						sms.limit = limit;
						sms.initialSize = 10;
						sms.intervals = new Interval[]{ region };
						sms.root = seq(
								new DynamicClip(id(), mock(IqlMarkerCall.class), true, REGION_0),
								new Exhaust(id(), true),
								new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
								new Finish(id(), limit, false));
						sms.matchers = matchers(matcher(0, EQUALS_X));
						return sms;
					}

					@CsvSource({
						"-XXX, 0,   0, 0",
						"--XX, 0-1, 0, 0-1",
						"--XX, 0-1, 1, 1",
						"---X, 0-2, 0, 0-2",
						"---X, 1-2, 1, 1-2",
						"---X, 1-2, 2, 2",
						"---X, 0,   0, 0",
						"----, 0-3, 0, 0-3",
					})
					@ParameterizedTest(name="{index}: X in [{0}], clip {1}, start at {2}")
					void testFail(String target,
							@IntervalArg Interval region, int startPos,
							@IntervalArg Interval visited) {
						rawTest()
						.setup(setup(region, NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectFail()
						.cache(cache(CACHE_0, true).window(visited))
						.assertResult();
					}

					@DisplayName("scan of 1 match")
					@CsvSource({
						"X, 0, 0, 0",

						"X--, 0-2, 0, 0",
						"X--, 0-1, 0, 0",
						"X--, 0,   0, 0",
						"-X-, 0-1, 0, 1",
						"-X-, 0-1, 1, 1",
						"-X-, 1-2, 1, 1",
						"-X-, 0-2, 1, 1",
						"--X, 0-2, 0, 2",
						"--X, 0-2, 1, 2",
						"--X, 0-2, 2, 2",
						"--X, 1-2, 1, 2",
						"--X, 1-2, 2, 2",
					})
					@ParameterizedTest(name="{index}: X in [{0}], clip {1}, start at {2}")
					void testFindSingle(String target,
							@IntervalArg Interval region,
							int startPos, int hit) {
						rawTest()
						.setup(setup(region, NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectSuccess(1)
						.cache(cache(CACHE_0, true).hits(hit))
						.node(node(NODE_0).last(hit))
						.result(result(0).map(NODE_0, hit))
						.assertResult();
					}

					@DisplayName("scan of up to 3 matches")
					@CsvSource({
						"---X-, 1-3, 1, 1, 3",
						"--X--, 1-3, 1, 1, 2",
						"-X---, 1-3, 1, 1, 1",

						"-X-X-, 1-3, 1, 2, 3",
						"-XX--, 1-3, 1, 2, 2",
						"--XX-, 1-3, 1, 2, 3",

						"-XXX-, 1-2, 1, 2, 2",
						"-XX--, 1-2, 1, 2, 2",
						"--XX-, 1-2, 1, 1, 2",

						"-XXX-, 1-3, 2, 2, 3",
					})
					@ParameterizedTest(name="{index}: X in [{0}], clip {1}, start at {2}, count={3}")
					void testMultiMatch(String target,
							@IntervalArg Interval region,
							int startPos, int matchCount, int last) {
						rawTest()
						.setup(setup(region, NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectSuccess(matchCount)
						.cache(cache(CACHE_0, true).hits(target, EQUALS_X))
						.node(node(NODE_0).last(last))
						.assertResult();
					}

					@DisplayName("scan of up to 3 matches with limit")
					@CsvSource({
						"X---X-X, 1, 2-4, 2, 1, 4",
						"X--X--X, 1, 2-4, 2, 1, 3",
						"X-X---X, 1, 2-4, 2, 1, 2",
						"X---X-X, 2, 2-4, 2, 1, 4",
						"X--X--X, 2, 2-4, 2, 1, 3",
						"X-X---X, 2, 2-4, 2, 1, 2",

						"X-X-X-X, 1, 2-4, 2, 1, 2",
						"X-XX--X, 1, 2-4, 2, 1, 2",
						"X--XX-X, 1, 2-4, 2, 1, 3",
						"X-X-X-X, 2, 2-4, 2, 2, 4",
						"X-XX--X, 2, 2-4, 2, 2, 3",
						"X--XX-X, 2, 2-4, 2, 2, 4",

						"X-XXX-X, 1, 2-3, 2, 1, 2",
						"X-XX--X, 1, 2-3, 2, 1, 2",
						"X--XX-X, 1, 2-3, 2, 1, 3",
						"X-XXX-X, 2, 2-3, 2, 2, 3",
						"X-XX--X, 2, 2-3, 2, 2, 3",

						"X-XXX-X, 1, 2-4, 2, 1, 2",
						"X-XXX-X, 2, 2-4, 2, 2, 3",
						"X-XXX-X, 3, 2-4, 2, 3, 4",

						"X-XXX-X, 1, 2-4, 3, 1, 3",
						"X-XXX-X, 2, 2-4, 3, 2, 4",
						"X-XXX-X, 3, 2-4, 3, 2, 4",
					})
					@ParameterizedTest(name="{index}: X in [{0}], limit= {1}, clip {2}, start at {3}, count={4}")
					void testLimitedMultiMatch(String target, int limit,
							@IntervalArg Interval region,
							int startPos, int matchCount, int last) {
						rawTest()
						.setup(setup(region, limit))
						.target(target)
						.startPos(startPos)
						.expectSuccess(matchCount)
						.cache(cache(CACHE_0, true)
								.window(startPos, last)
								.hits(target, EQUALS_X))
						.node(node(NODE_0).last(last))
						.assertResult();
					}
				}

				@Deprecated
				@Nested
				class ForwardSingleCached {

					StateMachineSetup setup(Interval region, int limit) {
						StateMachineSetup sms = new StateMachineSetup();
						sms.mappedNodes = mockMappedNodes(1);
						sms.cacheCount = 1;
						sms.limit = limit;
						sms.initialSize = 10;
						sms.intervals = new Interval[]{ region };
						sms.root = seq(
								new DynamicClip(id(), mock(IqlMarkerCall.class), true, REGION_0),
								new Exhaust(id(), true),
								new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
								new Finish(id(), limit, false));
						sms.matchers = matchers(matcher(0, EQUALS_X));
						return sms;
					}

					@CsvSource({
						"-XXX, 0,   0",
						"--XX, 0-1, 0",
						"--XX, 0-1, 1",
						"---X, 0-2, 0",
						"---X, 1-2, 1",
						"---X, 1-2, 2",
						"---X, 0,   0",
						"----, 0-3, 0",
					})
					@ParameterizedTest(name="{index}: X in [{0}], clip {1}, start at {2}")
					void testFail(String target,
							@IntervalArg Interval region, int startPos) {
						rawTest()
						.setup(setup(region, NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectFail()
						.cache(cache(CACHE_0, true)
								.window(startPos, region.to))
						.assertResult();
					}

					@DisplayName("scan of 1 match")
					@CsvSource({
						//singleton
						"X, 0, 0, 0",
						//left clip
						"XX--, 1-3, 1, 1",
						"XX--, 0-3, 1, 1",
						"XX--, 0-2, 1, 1",
						//right clip
						"--XX, 0-2, 0, 2",
						"--XX, 0-2, 1, 2",
						"--XX, 1-2, 1, 2",
					})
					@ParameterizedTest(name="{index}: X in [{0}], clip {1}, start at {2}, hit= {4}")
					void testFindSingle(String target,
							@IntervalArg Interval region,
							int startPos, int hit) {
						rawTest()
						.setup(setup(region, NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectSuccess(1)
						.cache(cache(CACHE_0, true)
								.window(startPos, region.to)
								.hits(hit))
						.node(node(NODE_0).last(hit))
						.result(result(0).map(NODE_0, hit))
						.assertResult();
					}

					@DisplayName("scan of up to 3 matches")
					@CsvSource({
						"X---X-X, 2-4, 2, 1, 4",
						"X--X--X, 2-4, 2, 1, 3",
						"X-X---X, 2-4, 2, 1, 2",

						"X-X-X-X, 2-4, 2, 2, 4",
						"X-XX--X, 2-4, 2, 2, 3",
						"X--XX-X, 2-4, 2, 2, 4",

						"X-XXX-X, 2-3, 2, 2, 3",
						"X-XX--X, 2-3, 2, 2, 3",
						"X--XX-X, 2-3, 2, 1, 3",

						"X-XXX-X, 2-4, 3, 2, 4",
					})
					@ParameterizedTest(name="{index}: X in [{0}], clip {1}, start at {2}, count={3}")
					void testMultiMatch(String target,
							@IntervalArg Interval region,
							int startPos, int matchCount, int last) {
						rawTest()
						.setup(setup(region, NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectSuccess(matchCount)
						.cache(cache(CACHE_0, true)
								.window(startPos, region.to)
								.hits(target, region, EQUALS_X))
						.node(node(NODE_0).last(last))
						.assertResult();
					}

					@DisplayName("scan of up to 3 matches with limit")
					@CsvSource({
						"X---X-X, 1, 2-4, 2, 1, 4",
						"X--X--X, 1, 2-4, 2, 1, 3",
						"X-X---X, 1, 2-4, 2, 1, 2",
						"X---X-X, 2, 2-4, 2, 1, 4",
						"X--X--X, 2, 2-4, 2, 1, 3",
						"X-X---X, 2, 2-4, 2, 1, 2",

						"X-X-X-X, 1, 2-4, 2, 1, 2",
						"X-XX--X, 1, 2-4, 2, 1, 2",
						"X--XX-X, 1, 2-4, 2, 1, 3",
						"X-X-X-X, 2, 2-4, 2, 2, 4",
						"X-XX--X, 2, 2-4, 2, 2, 3",
						"X--XX-X, 2, 2-4, 2, 2, 4",

						"X-XXX-X, 1, 2-3, 2, 1, 2",
						"X-XX--X, 1, 2-3, 2, 1, 2",
						"X--XX-X, 1, 2-3, 2, 1, 3",
						"X-XXX-X, 2, 2-3, 2, 2, 3",
						"X-XX--X, 2, 2-3, 2, 2, 3",

						"X-XXX-X, 1, 2-4, 2, 1, 2",
						"X-XXX-X, 2, 2-4, 2, 2, 3",
						"X-XXX-X, 3, 2-4, 2, 3, 4",

						"X-XXX-X, 1, 2-4, 3, 1, 3",
						"X-XXX-X, 2, 2-4, 3, 2, 4",
						"X-XXX-X, 3, 2-4, 3, 2, 4",
					})
					@ParameterizedTest(name="{index}: X in [{0}], limit= {1}, clip {2}, start at {3}, count={4}")
					void testLimitedMultiMatch(String target, int limit,
							@IntervalArg Interval region,
							int startPos, int matchCount, int last) {
						rawTest()
						.setup(setup(region, limit))
						.target(target)
						.startPos(startPos)
						.expectSuccess(matchCount)
						.cache(cache(CACHE_0, true)
								.window(startPos, last)
								.hits(target, EQUALS_X))
						.node(node(NODE_0).last(last))
						.assertResult();
					}
				}

				@Nested
				class BackwardSingle {

					StateMachineSetup setup(Interval region, int limit) {
						StateMachineSetup sms = new StateMachineSetup();
						sms.mappedNodes = mockMappedNodes(1);
						sms.cacheCount = 1;
						sms.limit = limit;
						sms.initialSize = 10;
						sms.intervals = new Interval[]{ region };
						sms.root = seq(
								new DynamicClip(id(), mock(IqlMarkerCall.class), true, REGION_0),
								new Exhaust(id(), false),
								new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
								new Finish(id(), limit, false));
						sms.matchers = matchers(matcher(0, EQUALS_X));
						return sms;
					}

					@DisplayName("scan of 1 match")
					@CsvSource({
						"X-X,     1, 1",
						"X---X, 1-3, 1",
						"X---X, 1-3, 2",
						"X---X, 1-3, 3",
						"X-X-X, 1-3, 3",
					})
					@ParameterizedTest(name="{index}: X in [{0}], start at {1}")
					void testFail(String target,
							@IntervalArg Interval region,
							int startPos) {
						rawTest()
						.setup(setup(region, NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectFail()
						.cache(cache(CACHE_0, true)
								.window(startPos, region.to))
						.assertResult();
					}

					@DisplayName("scan of 1 match")
					@CsvSource({
						"XXX, 1, 1, 1",

						"XX--X, 1-3, 1, 1",
						"X-X-X, 1-3, 1, 2",
						"X--XX, 1-3, 1, 3",

						"X-X-X, 1-3, 2, 2",
					})
					@ParameterizedTest(name="{index}: X in [{0}], start at {1}")
					void testSingleMatch(String target,
							@IntervalArg Interval region,
							int startPos, int hit) {
						rawTest()
						.setup(setup(region, NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectSuccess(1)
						.cache(cache(CACHE_0, true)
								.window(startPos, region.to)
								.hits(target, region, EQUALS_X))
						.node(node(NODE_0).last(hit))
						.result(result(0).map(NODE_0, hit))
						.assertResult();
					}

					@DisplayName("scan of up to 3 matches")
					@CsvSource({
						"X---X-X, 2-4, 2, 1, 4",
						"X--X--X, 2-4, 2, 1, 3",
						"X-X---X, 2-4, 2, 1, 2",

						"X-X-X-X, 2-4, 2, 2, 2",
						"X-XX--X, 2-4, 2, 2, 2",
						"X--XX-X, 2-4, 2, 2, 3",

						"X-XXX-X, 2-3, 2, 2, 2",
						"X-XX--X, 2-3, 2, 2, 2",
						"X--XX-X, 2-3, 2, 1, 3",

						"X-XXX-X, 2-4, 3, 2, 3",
					})
					@ParameterizedTest(name="{index}: X in [{0}], clip {1}, start at {2}, count={3}")
					void testMultiMatch(String target,
							@IntervalArg Interval region,
							int startPos, int matchCount, int last) {
						rawTest()
						.setup(setup(region, NO_LIMIT))
						.target(target)
						.startPos(startPos)
						.expectSuccess(matchCount)
						.cache(cache(CACHE_0, true)
								.window(startPos, region.to)
								.hits(target, region, EQUALS_X))
						.node(node(NODE_0).last(last))
						.assertResult();
					}

					@DisplayName("scan of up to 3 matches with limit")
					@CsvSource({
						"X---X-X, 1, 2-4, 2, 1, 4",
						"X--X--X, 1, 2-4, 2, 1, 3",
						"X-X---X, 1, 2-4, 2, 1, 2",
						"X---X-X, 2, 2-4, 2, 1, 4",
						"X--X--X, 2, 2-4, 2, 1, 3",
						"X-X---X, 2, 2-4, 2, 1, 2",

						"X-X-X-X, 1, 2-4, 2, 1, 4",
						"X-XX--X, 1, 2-4, 2, 1, 3",
						"X--XX-X, 1, 2-4, 2, 1, 4",
						"X-X-X-X, 2, 2-4, 2, 2, 2",
						"X-XX--X, 2, 2-4, 2, 2, 2",
						"X--XX-X, 2, 2-4, 2, 2, 3",

						"X-XXX-X, 1, 2-3, 2, 1, 3",
						"X-XX--X, 1, 2-3, 2, 1, 3",
						"X--XX-X, 1, 2-3, 2, 1, 3",
						"X-XXX-X, 2, 2-3, 2, 2, 2",
						"X-XX--X, 2, 2-3, 2, 2, 2",

						"X-XXX-X, 1, 2-4, 2, 1, 4",
						"X-XXX-X, 2, 2-4, 2, 2, 3",
						"X-XXX-X, 3, 2-4, 2, 3, 2",

						"X-XXX-X, 1, 2-4, 3, 1, 4",
						"X-XXX-X, 2, 2-4, 3, 2, 3",
						"X-XXX-X, 3, 2-4, 3, 2, 3",
					})
					@ParameterizedTest(name="{index}: X in [{0}], limit= {1}, clip {2}, start at {3}, count={4}")
					void testLimitedMultiMatch(String target, int limit,
							@IntervalArg Interval region,
							int startPos, int matchCount, int last) {
						rawTest()
						.setup(setup(region, limit))
						.target(target)
						.startPos(startPos)
						.expectSuccess(matchCount)
						.cache(cache(CACHE_0, true)
								.window(last, region.to)
								.hits(target, EQUALS_X))
						.node(node(NODE_0).last(last))
						.assertResult();
					}
				}
			}
		}

		@Nested
		class ForRepetition {

			@Nested
			class ForClosedRepetition {

				private final int CMIN = 2;
				private final int CMAX = 5;

				@Nested
				class Greedy {

					@Nested
					class BasicBehavior implements ClosedBase {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.mappedNodes = mockMappedNodes(1);
							sms.cacheCount = 1;
							sms.bufferCount = 3;
							sms.initialSize = 10;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
											CMIN, CMAX, StructurePattern.GREEDY, BUFFER_0, BUFFER_1, BUFFER_2, -1),
									new Finish(id(), NO_LIMIT, false));
							sms.matchers = matchers(matcher(0, EQUALS_X));
							return sms;
						}

					}

					//TODO test case of X{2,5}Y on XXXB so that we can evaluate proper backtracking with fail

					@Nested
					class Expansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.mappedNodes = mockMappedNodes(2);
							sms.cacheCount = 2;
							sms.bufferCount = 3;
							sms.initialSize = 10;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
											CMIN, CMAX, StructurePattern.GREEDY, BUFFER_0, BUFFER_1, BUFFER_2, -1),
									new Single(id(), mock(IqlNode.class), MAP_1, NODE_1, CACHE_1, NO_MEMBER, NO_ANCHOR),
									new Finish(id(), NO_LIMIT, NO_STOP));
							sms.matchers = matchers(
									matcher(0, EQUALS_X_IC),
									matcher(1, EQUALS_X));
							return sms;
						}

						@CsvSource({
							"xxX, 0, 0-2, 0-1, 2, 2, 2, 2",
							"xxXX, 0, 0-3, 0-2, 3, 3, 3, 3",
							"xxXX-, 0, 0-4, 0-2, 3, 3-4, 3, 3",
							"xxxXX-, 0, 0-4, 0-3, 4, 4-5, 4, 4",
							"xxxxXX, 0, 0-4, 0-4, 4, 5, 5, 5",
						})
						@ParameterizedTest(name="{index}: [Xx]'{2,5}'X in [{0}], start at {1}")
						void testExpansion(String target, int startPos,
								@IntervalArg Interval visited1,
								@IntervalArg Interval matched1,
								int last1,
								@IntervalArg Interval visited2,
								@IntervalArg Interval matched2,
								int last2) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectSuccess(1)

							.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
							.map(NODE_0, matched1)
							.node(node(NODE_0).last(last1))

							.cache(cache(CACHE_1, true).window(visited2).hits(target, EQUALS_X))
							.map(NODE_1, matched2)
							.node(node(NODE_1).last(last2))
							.assertResult();
						}
					}
				}

				@Nested
				class Possessive {

					@Nested
					class BasicBehavior implements ClosedBase {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.mappedNodes = mockMappedNodes(1);
							sms.cacheCount = 1;
							sms.bufferCount = 3;
							sms.initialSize = 10;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
											CMIN, CMAX, StructurePattern.POSSESSIVE, BUFFER_0, BUFFER_1, BUFFER_2, -1),
									new Finish(id(), NO_LIMIT, false));
							sms.matchers = matchers(matcher(0, EQUALS_X));
							return sms;
						}

					}

					@Nested
					class Expansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.mappedNodes = mockMappedNodes(2);
							sms.cacheCount = 2;
							sms.bufferCount = 3;
							sms.initialSize = 10;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
											CMIN, CMAX, StructurePattern.POSSESSIVE, BUFFER_0, BUFFER_1, BUFFER_2, -1),
									new Single(id(), mock(IqlNode.class), MAP_1, NODE_1, CACHE_1, NO_MEMBER, NO_ANCHOR),
									new Finish(id(), NO_LIMIT, false));
							sms.matchers = matchers(
									matcher(0, EQUALS_X_IC),
									matcher(1, EQUALS_X));
							return sms;
						}

						@DisplayName("consume too much")
						@CsvSource({
							"xxX, 0, 0-2, 2, -",
							"xxXX, 0, 0-3, 3, -",
							"xxXX-, 0, 0-4, 3, 4",
							"xxxXX-, 0, 0-4, 4, 5",
						})
						@ParameterizedTest(name="{index}: [Xx]'{2,5}!'X in [{0}], start at {1}")
						void testFail(String target, int startPos,
								@IntervalArg Interval visited1, int last,
								@IntervalArg Interval visited2) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectFail()

							.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
							.node(node(NODE_0).last(last))

							.cache(cache(CACHE_1, true).window(visited2))
							.assertResult();
						}

						@CsvSource({
							"xxxxXX, 0, 0-4, 0-4, 4, 5, 5, 5",
						})
						@ParameterizedTest(name="{index}: [Xx]'{2,5}'X in [{0}], start at {1}")
						void testExpansion(String target, int startPos,
								@IntervalArg Interval visited1,
								@IntervalArg Interval matched1,
								int last1,
								@IntervalArg Interval visited2,
								@IntervalArg Interval matched2,
								int last2) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectSuccess(1)

							.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
							.map(NODE_0, matched1)
							.node(node(NODE_0).last(last1))

							.cache(cache(CACHE_1, true).window(visited2).hits(target, EQUALS_X))
							.map(NODE_1, matched2)
							.node(node(NODE_1).last(last2))
							.assertResult();
						}
					}
				}

				@Nested
				class Reluctant {

					@Nested
					class WithoutProxy implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.mappedNodes = mockMappedNodes(1);
							sms.cacheCount = 1;
							sms.bufferCount = 3;
							sms.initialSize = 10;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
											CMIN, CMAX, StructurePattern.RELUCTANT, BUFFER_0, BUFFER_1, BUFFER_2, -1),
									new Finish(id(), NO_LIMIT, false));
							sms.matchers = matchers(
									matcher(0, EQUALS_X));
							return sms;
						}

						@CsvSource({
							"-, 0, 0",
							"X, 0, 0",
							"X-, 0, 0-1",
							"-X, 0, 0",
							"XY, 0, 0-1",
							"XYX, 0, 0-1",
							"XXY, 1, 1-2",
						})
						@ParameterizedTest(name="{index}: X'{2,5}' in [{0}], start at {1}")
						void testFail(String target, int startPos,
								@IntervalArg Interval window) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectFail()
							.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
							.assertResult();
						}

						@CsvSource({
							"XX, 0, 0-1, 1",
							"-XX, 1, 1-2, 2",
							"YXX, 1, 1-2, 2",
							"-XX-, 1, 1-2, 2",
						})
						@ParameterizedTest(name="{index}: X'{2,5}' in [{0}], start at {1}")
						void testFindMinimum(String target, int startPos,
								@IntervalArg Interval window, int last) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectSuccess(1)
							.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
							.map(NODE_0, startPos, startPos+1)
							.node(node(NODE_0).last(last))
							.assertResult();
						}

						// Reluctant expansion will always stop after CMIN if we can produce a valid result
						@CsvSource({
							"XXXXX, 0, 0-1, 1",
							"-XXXXX, 1, 1-2, 2",
							"YXXXXXZ, 1, 1-2, 2",
							"-XXXXXX, 1, 1-2, 2",
						})
						@ParameterizedTest(name="{index}: X'{2,5}' in [{0}], start at {1}")
						void testFindMaximum(String target, int startPos,
								@IntervalArg Interval window, int last) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectSuccess(1)
							.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
							.map(NODE_0, Interval.of(startPos, startPos+1))
							.node(node(NODE_0).last(last))
							.assertResult();
						}

					}

					@Nested
					class WithProxy implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.mappedNodes = mockMappedNodes(1);
							sms.cacheCount = 1;
							sms.bufferCount = 3;
							sms.initialSize = 10;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
											CMIN, CMAX, StructurePattern.RELUCTANT, BUFFER_0, BUFFER_1, BUFFER_2, -1),
									new Proxy(NODE_1), // we need this to motivate the reluctant expansion
									new Finish(id(), NO_LIMIT, false));
							sms.matchers = matchers(
									matcher(0, EQUALS_X),
									matcher(1, EQUALS_NOT_X)); // this one enables the reluctant repetition to expand to the max
							return sms;
						}

						@CsvSource({
							"XX-, 0, 0-1, 1",
							"-XX-, 1, 1-2, 2",
							"YXXY, 1, 1-2, 2",
						})
						@ParameterizedTest(name="{index}: X'{2,5}'?^X in [{0}], start at {1}")
						void testFindMinimum(String target, int startPos,
								@IntervalArg Interval window, int last) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectSuccess(1)
							.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
							.map(NODE_0, startPos, startPos+1)
							.node(node(NODE_0).last(last))
							.assertResult();
						}

						/*
						 * We need the additional delimiter at the end of the XX sequence
						 * so that reluctant expansion can use another node for probing ahead.
						 * Otherwise we'd stop after CMIN occurrences of X and never reach CMAX.
						 */
						@CsvSource({
							"XXXXX-, 0, 0-4, 4",
							"-XXXXX-, 1, 1-5, 5",
							"YXXXXXZ, 1, 1-5, 5",
							"-XXXXXx, 1, 1-5, 5",
						})
						@ParameterizedTest(name="{index}: X'{2,5}'?^X in [{0}], start at {1}")
						void testFindMaximum(String target, int startPos,
								@IntervalArg Interval window, int last) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectSuccess(1)
							.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
							.map(NODE_0, Interval.of(startPos, startPos+4))
							.node(node(NODE_0).last(last))
							.assertResult();
						}

					}

					@Nested
					class Expansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.mappedNodes = mockMappedNodes(2);
							sms.cacheCount = 2;
							sms.bufferCount = 3;
							sms.initialSize = 10;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
											CMIN, CMAX, StructurePattern.RELUCTANT, BUFFER_0, BUFFER_1, BUFFER_2, -1),
									new Single(id(), mock(IqlNode.class), MAP_1, NODE_1, CACHE_1, NO_MEMBER, NO_ANCHOR),
									new Finish(id(), NO_LIMIT, false));
							sms.matchers = matchers(
									matcher(0, EQUALS_X_IC),
									matcher(1, EQUALS_X));
							return sms;
						}

						@DisplayName("consume too little")
						@CsvSource({
							"xX, 0, 0-1, 1, -",
							"xxxxxxX, 0, 0-4, 4, 5",
						})
						@ParameterizedTest(name="{index}: [Xx]'{2,5}'?X in [{0}], start at {1}")
						void testFail(String target, int startPos,
								@IntervalArg Interval visited1, int last,
								@IntervalArg Interval visited2) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectFail()
							.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
							.node(node(NODE_0).last(last))

							.cache(cache(CACHE_1, true).window(visited2))
							.assertResult();
						}

						@CsvSource({
							"xxXXXXX, 0, 0-1, 0-1, 1, 2, 2, 2",
							"xxxXXXX, 0, 0-2, 0-2, 2, 3, 3, 3",
							"xxxxxXX, 0, 0-4, 0-4, 4, 5, 5, 5",
						})
						@ParameterizedTest(name="{index}: [Xx]'{2,5}'?X in [{0}], start at {1}")
						void testExpansion(String target, int startPos,
								@IntervalArg Interval visited1,
								@IntervalArg Interval matched1,
								int last1,
								@IntervalArg Interval visited2,
								@IntervalArg Interval matched2,
								int last2) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectSuccess(1)
							.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
							.map(NODE_0, matched1)
							.node(node(NODE_0).last(last1))

							.cache(cache(CACHE_1, true).window(visited2).hits(target, EQUALS_X))
							.map(NODE_1, matched2)
							.node(node(NODE_1).last(last2))
							.assertResult();
						}
					}
				}
			}

			@Nested
			class ForOpenRepetition {

				private final int CMIN = 2;
				private final int CINF = Integer.MAX_VALUE;

				@Nested
				class Greedy {

					@Nested
					class BasicBehavior implements OpenBase {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.mappedNodes = mockMappedNodes(1);
							sms.cacheCount = 1;
							sms.bufferCount = 3;
							sms.initialSize = 10;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
											CMIN, CINF, StructurePattern.GREEDY, BUFFER_0, BUFFER_1, BUFFER_2, -1),
									new Finish(id(), NO_LIMIT, false));
							sms.matchers = matchers(matcher(0, EQUALS_X));
							return sms;
						}

					}

					@Nested
					class Expansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.mappedNodes = mockMappedNodes(2);
							sms.cacheCount = 2;
							sms.bufferCount = 3;
							sms.initialSize = 10;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
											CMIN, CINF, StructurePattern.GREEDY, BUFFER_0, BUFFER_1, BUFFER_2, -1),
									new Single(id(), mock(IqlNode.class), MAP_1, NODE_1, CACHE_1, NO_MEMBER, NO_ANCHOR),
									new Finish(id(), NO_LIMIT, false));
							sms.matchers = matchers(
									matcher(0, EQUALS_X_IC),
									matcher(1, EQUALS_X));
							return sms;
						}

						@CsvSource({
							"xxX, 0, 0-2, 0-1, 2, 2, 2, 2",
							"xxXX, 0, 0-3, 0-2, 3, 3, 3, 3",
							"xxXX-, 0, 0-4, 0-2, 3, 3-4, 3, 3",
							"xxxXX-, 0, 0-4, 0-3, 4, 4-5, 4, 4",
							"xxxxxXX, 0, 0-6, 0-5, 6, 6, 6, 6",
						})
						@ParameterizedTest(name="{index}: [Xx]'{2,}'X in [{0}], start at {1}")
						void testExpansion(String target, int startPos,
								@IntervalArg Interval visited1,
								@IntervalArg Interval matched1,
								int last1,
								@IntervalArg Interval visited2,
								@IntervalArg Interval matched2,
								int last2) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectSuccess(1)
							.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
							.map(NODE_0, matched1)
							.node(node(NODE_0).last(last1))

							.cache(cache(CACHE_1, true).window(visited2).hits(target, EQUALS_X))
							.map(NODE_1, matched2)
							.node(node(NODE_1).last(last2))
							.assertResult();
						}
					}
				}

				@Nested
				class Possessive {

					@Nested
					class BasicBehavior implements OpenBase {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.mappedNodes = mockMappedNodes(1);
							sms.cacheCount = 1;
							sms.bufferCount = 3;
							sms.initialSize = 10;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
											CMIN, CINF, StructurePattern.POSSESSIVE, BUFFER_0, BUFFER_1, BUFFER_2, -1),
									new Finish(id(), NO_LIMIT, false));
							sms.matchers = matchers(matcher(0, EQUALS_X));
							return sms;
						}

					}

					@Nested
					class OverExpansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.mappedNodes = mockMappedNodes(2);
							sms.cacheCount = 2;
							sms.bufferCount = 3;
							sms.initialSize = 10;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
											CMIN, CINF, StructurePattern.POSSESSIVE, BUFFER_0, BUFFER_1, BUFFER_2, -1),
									new Single(id(), mock(IqlNode.class), MAP_1, NODE_1, CACHE_1, NO_MEMBER, NO_ANCHOR),
									new Finish(id(), NO_LIMIT, false));
							sms.matchers = matchers(
									matcher(0, EQUALS_X_IC),
									matcher(1, EQUALS_X));
							return sms;
						}

						@DisplayName("consume too much")
						@CsvSource({
							"xxX, 0, 0-2, 2, -",
							"xxXX, 0, 0-3, 3, -",
							"xxXX-, 0, 0-4, 3, 4",
							"xxxxXX-, 0, 0-5, 5, 6",
						})
						@ParameterizedTest(name="{index}: [Xx]'{2,5}!'X in [{0}], start at {1}")
						void testFail(String target, int startPos,
								@IntervalArg Interval visited1, int last,
								@IntervalArg Interval visited2) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectFail()
							.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
							.node(node(NODE_0).last(last))

							.cache(cache(CACHE_1, true).window(visited2))
							.assertResult();
						}
					}

					@Nested
					class Expansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.mappedNodes = mockMappedNodes(2);
							sms.cacheCount = 2;
							sms.bufferCount = 3;
							sms.initialSize = 10;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
											CMIN, CINF, StructurePattern.POSSESSIVE, BUFFER_0, BUFFER_1, BUFFER_2, -1),
									new Single(id(), mock(IqlNode.class), MAP_1, NODE_1, CACHE_1, NO_MEMBER, NO_ANCHOR),
									new Finish(id(), NO_LIMIT, false));
							sms.matchers = matchers(
									matcher(0, EQUALS_X),
									matcher(1, EQUALS_Y));
							return sms;
						}

						@CsvSource({
							"XXXXXXXXYY, 0, 0-7, 0-7, 7, 8, 8, 8",
						})
						@ParameterizedTest(name="{index}: X'{2,}'Y in [{0}], start at {1}")
						void testExpansion(String target, int startPos,
								@IntervalArg Interval visited1,
								@IntervalArg Interval matched1,
								int last1,
								@IntervalArg Interval visited2,
								@IntervalArg Interval matched2,
								int last2) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectSuccess(1)
							.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X))
							.map(NODE_0, matched1)
							.node(node(NODE_0).last(last1))

							.cache(cache(CACHE_1, true).window(visited2).hits(target, EQUALS_Y))
							.map(NODE_1, matched2)
							.node(node(NODE_1).last(last2))
							.assertResult();
						}
					}
				}

				@Nested
				class Reluctant {

					@Nested
					class WithoutProxy implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.mappedNodes = mockMappedNodes(1);
							sms.cacheCount = 1;
							sms.bufferCount = 3;
							sms.initialSize = 10;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
											CMIN, CINF, StructurePattern.RELUCTANT, BUFFER_0, BUFFER_1, BUFFER_2, -1),
									new Finish(id(), NO_LIMIT, false));
							sms.matchers = matchers(
									matcher(0, EQUALS_X));
							return sms;
						}

						@CsvSource({
							"-, 0, 0",
							"X, 0, 0",
							"X-, 0, 0-1",
							"-X, 0, 0",
							"XY, 0, 0-1",
							"XYX, 0, 0-1",
							"XXY, 1, 1-2",
						})
						@ParameterizedTest(name="{index}: X'{2,}' in [{0}], start at {1}")
						void testFail(String target, int startPos,
								@IntervalArg Interval window) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectFail()
							.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
							.assertResult();
						}

						@CsvSource({
							"XX, 0, 0-1, 1",
							"-XX, 1, 1-2, 2",
							"YXX, 1, 1-2, 2",
							"-XX-, 1, 1-2, 2",
						})
						@ParameterizedTest(name="{index}: X'{2,}' in [{0}], start at {1}")
						void testFindMinimum(String target, int startPos,
								@IntervalArg Interval window, int last) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectSuccess(1)
							.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
							.map(NODE_0, startPos, startPos+1)
							.node(node(NODE_0).last(last))
							.assertResult();
						}

						// Reluctant expansion will always stop after CMIN if we can produce a valid result
						@CsvSource({
							"XXXXX, 0, 0-1, 1",
							"-XXXXX, 1, 1-2, 2",
							"YXXXXXZ, 1, 1-2, 2",
							"-XXXXXX, 1, 1-2, 2",
						})
						@ParameterizedTest(name="{index}: X'{2,}' in [{0}], start at {1}")
						void testFindMaximum(String target, int startPos,
								@IntervalArg Interval window, int last) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectSuccess(1)
							.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
							.map(NODE_0, Interval.of(startPos, startPos+1))
							.node(node(NODE_0).last(last))
							.assertResult();
						}

					}

					@Nested
					class WithProxy implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.mappedNodes = mockMappedNodes(1);
							sms.cacheCount = 1;
							sms.bufferCount = 3;
							sms.initialSize = 10;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
											CMIN, CINF, StructurePattern.RELUCTANT, BUFFER_0, BUFFER_1, BUFFER_2, -1),
									new Proxy(NODE_1), // we need this to motivate the reluctant expansion
									new Finish(id(), NO_LIMIT, false));
							sms.matchers = matchers(
									matcher(0, EQUALS_X),
									matcher(1, EQUALS_NOT_X)); // this one enables the reluctant repetition to expand to the max
							return sms;
						}

						@CsvSource({
							"XX-, 0, 0-1, 1",
							"-XX-, 1, 1-2, 2",
							"YXXY, 1, 1-2, 2",
						})
						@ParameterizedTest(name="{index}: X'{2,}'?^X in [{0}], start at {1}")
						void testFindMinimum(String target, int startPos,
								@IntervalArg Interval window, int last) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectSuccess(1)
							.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
							.map(NODE_0, startPos, startPos+1)
							.node(node(NODE_0).last(last))
							.assertResult();
						}

						/*
						 * We need the additional delimiter at the end of the XX sequence
						 * so that reluctant expansion can use another node for probing ahead.
						 * Otherwise we'd stop after CMIN occurrences of X and never reach further.
						 */
						@CsvSource({
							"XXXXX-, 0, 0-4, 0-4, 4",
							"-XXXXX-, 1, 1-5, 1-5, 5",
							"YXXXXZ-, 1, 1-4, 1-4, 4",
							"-XXXXXx, 1, 1-5, 1-5, 5",
						})
						@ParameterizedTest(name="{index}: X'{2,}'?^X in [{0}], start at {1}")
						void testFindMaximum(String target, int startPos,
								@IntervalArg Interval window,
								@IntervalArg Interval matched, int last) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectSuccess(1)
							.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
							.map(NODE_0, matched)
							.node(node(NODE_0).last(last))
							.assertResult();
						}

					}

					@Nested
					class Expansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.mappedNodes = mockMappedNodes(2);
							sms.cacheCount = 2;
							sms.bufferCount = 3;
							sms.initialSize = 10;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
											CMIN, CINF, StructurePattern.RELUCTANT, BUFFER_0, BUFFER_1, BUFFER_2, -1),
									new Single(id(), mock(IqlNode.class), MAP_1, NODE_1, CACHE_1, NO_MEMBER, NO_ANCHOR),
									new Finish(id(), NO_LIMIT, false));
							sms.matchers = matchers(
									matcher(0, EQUALS_X_IC),
									matcher(1, EQUALS_X));
							return sms;
						}

						@DisplayName("consume too little")
						@CsvSource({
							"xX, 0, 0-1, 1, -",
							"xxY, 0, 0-2, 1, 2",
						})
						@ParameterizedTest(name="{index}: [Xx]'{2,}'?X in [{0}], start at {1}")
						void testFail(String target, int startPos,
								@IntervalArg Interval visited1, int last,
								@IntervalArg Interval visited2) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectFail()
							.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
							.node(node(NODE_0).last(last))

							.cache(cache(CACHE_1, true).window(visited2))
							.assertResult();
						}

						@CsvSource({
							"xxXXXXX, 0, 0-1, 0-1, 1, 2, 2, 2",
							"xxxXXXX, 0, 0-2, 0-2, 2, 3, 3, 3",
							"xxxxxXX, 0, 0-4, 0-4, 4, 5, 5, 5",
							"xxxxxxX, 0, 0-5, 0-5, 5, 6, 6, 6",
						})
						@ParameterizedTest(name="{index}: [Xx]'{2,}'?X in [{0}], start at {1}")
						void testExpansion(String target, int startPos,
								@IntervalArg Interval visited1,
								@IntervalArg Interval matched1,
								int last1,
								@IntervalArg Interval visited2,
								@IntervalArg Interval matched2,
								int last2) {
							rawTest()
							.setup(setup())
							.target(target)
							.startPos(startPos)
							.expectSuccess(1)
							.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
							.map(NODE_0, matched1)
							.node(node(NODE_0).last(last1))

							.cache(cache(CACHE_1, true).window(visited2).hits(target, EQUALS_X))
							.map(NODE_1, matched2)
							.node(node(NODE_1).last(last2))
							.assertResult();
						}
					}
				}
			}
		}

		@Nested
		class ForBranch {

			@Nested
			class DualAlternatives implements NodeTest {

				@Override
				public StateMachineSetup setup() {
					StateMachineSetup sms = new StateMachineSetup();
					sms.mappedNodes = mockMappedNodes(2);
					sms.cacheCount = 2;
					sms.initialSize = 10;
					sms.root = seq(
							branch(0,
									new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
									new Single(id(), mock(IqlNode.class), MAP_1, NODE_1, CACHE_1, NO_MEMBER, NO_ANCHOR)),
							new Finish(id(), NO_LIMIT, false));
					sms.matchers = matchers(
							matcher(0, EQUALS_A),
							matcher(1, EQUALS_B));
					return sms;
				}

				@CsvSource({
					"-, 0",
					"X, 0",
					"XY, 1",
				})
				@ParameterizedTest(name="{index}: A|B in [{0}], start at {1}")
				void testFail(String target, int startPos) {
					rawTest()
					.setup(setup())
					.target(target)
					.startPos(startPos)
					.expectFail()
					.cache(cache(CACHE_0, true).window(startPos))

					.cache(cache(CACHE_1, true).window(startPos))
					.assertResult();
				}

				@CsvSource({
					"A, 0",
					"AA, 0",
					"AA, 1",
					"BA, 1",
				})
				@ParameterizedTest(name="{index}: A|B in [{0}], start at {1}")
				void testOptionA(String target, int startPos) {
					rawTest()
					.setup(setup())
					.target(target)
					.startPos(startPos)
					.expectSuccess(1)
					.cache(cache(CACHE_0, true).window(startPos).hits(startPos))
					.node(node(NODE_0).last(startPos))
					.cache(cache(CACHE_1, true).window(startPos))
					.result(result(0).map(NODE_0, startPos))
					.assertResult();
				}

				@CsvSource({
					"B, 0",
					"BB, 0",
					"BB, 1",
					"AB, 1",
				})
				@ParameterizedTest(name="{index}: A|B in [{0}], start at {1}")
				void testOptionB(String target, int startPos) {
					rawTest()
					.setup(setup())
					.target(target)
					.startPos(startPos)
					.expectSuccess(1)
					.cache(cache(CACHE_0, true).window(startPos)) // option A must have been visited

					.cache(cache(CACHE_1, true).window(startPos).hits(startPos))
					.node(node(NODE_1).last(startPos))
					.result(result(0).map(NODE_1, startPos))
					.assertResult();
				}
			}

			@Nested
			class GreedyOptional implements NodeTest {

				@Override
				public StateMachineSetup setup() {
					StateMachineSetup sms = new StateMachineSetup();
					sms.mappedNodes = mockMappedNodes(1);
					sms.cacheCount = 1;
					sms.limit = 1;
					sms.initialSize = 10;
					sms.root = seq(
							branch(0,
									new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR),
									null),
							new Finish(id(), 1, false));
					sms.matchers = matchers(
							matcher(0, EQUALS_A));
					return sms;
				}

				@CsvSource({
					"A, 0",
					"AA, 0",
					"AA, 1",
					"XA, 1",
				})
				@ParameterizedTest(name="{index}: A'{0,1}' in [{0}], start at {1}")
				void testGreedyPath(String target, int startPos) {
					rawTest()
					.setup(setup())
					.target(target)
					.startPos(startPos)
					.expectedResult(true)
					.expectedCount(1)
					.cache(cache(CACHE_0, true).window(startPos).hits(startPos))
					.node(node(NODE_0).last(startPos))
					.result(result(0).map(NODE_0, startPos))
					.assertResult();
				}

				@CsvSource({
					"B, 0",
					"BB, 0",
					"BB, 1",
					"AB, 1",
				})
				@ParameterizedTest(name="{index}: A'{0,1}' in [{0}], start at {1}")
				void testZeroWidthAssertion(String target, int startPos) {
					rawTest()
					.setup(setup())
					.target(target)
					.startPos(startPos)
					.expectSuccess(1)
					.cache(cache(CACHE_0, true).window(startPos)) // option A must have been visited
					.assertResult();
				}
			}

			@Nested
			class ReluctantOptional implements NodeTest {

				@Override
				public StateMachineSetup setup() {
					StateMachineSetup sms = new StateMachineSetup();
					sms.mappedNodes = mockMappedNodes(2);
					sms.cacheCount = 2;
					sms.initialSize = 10;
					sms.root = seq(
							branch(0,
									null,
									new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, NO_ANCHOR)),
							new Single(id(), mock(IqlNode.class), MAP_1, NODE_1, CACHE_1, NO_MEMBER, NO_ANCHOR), // needed to force reluctant expansion
							new Finish(id(), NO_LIMIT, false));
					sms.matchers = matchers(
							matcher(0, EQUALS_A),
							matcher(1, EQUALS_B));
					return sms;
				}

				@CsvSource({
					"AB, 0",
					"AAB, 1",
					"XAB, 1",
				})
				@ParameterizedTest(name="{index}: A'{0,1}'?B in [{0}], start at {1}")
				void testReluctantPath(String target, int startPos) {
					rawTest()
					.setup(setup())
					.target(target)
					.startPos(startPos)
					.expectSuccess(1)
					.cache(cache(CACHE_0, true).window(startPos).hits(startPos))
					.node(node(NODE_0).last(startPos))

					.cache(cache(CACHE_1, true).window(startPos, startPos+1).hits(startPos+1))
					.node(node(NODE_1).last(startPos+1))
					.result(result(0)
							.map(NODE_0, startPos)
							.map(NODE_1, startPos+1))
					.assertResult();
				}

				@CsvSource({
					"B, 0",
					"BB, 0",
					"BB, 1",
					"AB, 1",
				})
				@ParameterizedTest(name="{index}: A'{0,1}'?B in [{0}], start at {1}")
				void testZeroWidthAssertion(String target, int startPos) {
					rawTest()
					.setup(setup())
					.target(target)
					.startPos(startPos)
					.expectSuccess(1)
					.cache(cache(CACHE_1, true).window(startPos).hits(startPos))
					.node(node(NODE_1).last(startPos))
					.result(result(0).map(NODE_1, startPos))
					.assertResult();
				}
			}
		}

		@Nested
		class ForPermutation {
			//TODO add explicit tests for internal sof the permutation nodes

			@Nested
			class Adjacent {

			}

			@Nested
			class NonAdjacent {

			}
		}

		@Nested
		class ForStepInto {

			@Nested
			class Plain {


				private StateMachineSetup setup(CharPredicate...preds) {

					Single[] atoms = new Single[preds.length];
					@SuppressWarnings("unchecked")
					Matcher<Item>[] matchers = new Matcher[preds.length];
					for (int i = 0; i < preds.length; i++) {
						boolean hasChild = i<preds.length-1;
						matchers[i] = matcher(i, preds[i]);
						IqlTreeNode treeNode = mock(IqlTreeNode.class);
						atoms[i] = new Single(id(), treeNode, i, i, i, NO_MEMBER, hasChild ? i : UNSET_INT);
					}

					Node atom = atoms[atoms.length-1];
					Node tail = atom;

					for (int i = atoms.length-2; i >= 0; i--) {
						Single node = atoms[i];
						Node conn = tree(node, atom, () -> id());
						atom = node;
						tail = conn;
					}

					Node scan = new Exhaust(id(), true);
					Node finish = new Finish(id(), NO_LIMIT, false);

					scan.setNext(atom);
					tail.setNext(finish);

					// Enforce optimization
					scan.study(new TreeInfo());

					StateMachineSetup sms = new StateMachineSetup();
					sms.mappedNodes = mockMappedNodes(preds.length);
					sms.cacheCount = preds.length;
					sms.anchorCount = preds.length-1;
					sms.initialSize = 10;
					sms.root = scan;
					sms.matchers = matchers(matchers);
					return sms;
				}

				@ParameterizedTest
				@CsvSource({
					"AB, 1*",
					"ACB, *01",
				})
				@DisplayName("[$A [$B]] -> no matches")
				void testSimpleNestingFail(String target, String tree) {
					rawTest()
						.setup(setup(EQUALS_A, EQUALS_B))
						.target(target)
						.tree(tree)
						.startPos(0)
						.expectFail()
						.assertResult();
				}

				@ParameterizedTest
				@CsvSource({
					// Normal order - adjacent
					"AB, *0, 0, 1",
					"ABC, *00, 0, 1",
					"ABC, *01, 0, 1",
					// Normal order - mixed
					"ACB, *00, 0, 2",
					"ACB, *20, 0, 2",
					// Reversed order - adjacent
					"BA, 1*, 1, 0",
					"BAC, 1*1, 1, 0",
					"BAC, 1*0, 1, 0",
					"ACB, *20, 0, 2",
					// Reversed order - mixed
					"BCA, 22*, 2, 0",
					"BCA, 20*, 2, 0",
				})
				@DisplayName("[$A [$B]] -> 1 match")
				void testSimpleNesting(String target, String tree, int hitA, int hitB) {
					rawTest()
						.setup(setup(EQUALS_A, EQUALS_B))
						.target(target)
						.tree(tree)
						.startPos(0)
						.expectSuccess(1)
						.result(result(0)
								.map(NODE_0, hitA)
								.map(NODE_1, hitB))
						.assertResult();
				}

				@ParameterizedTest
				@CsvSource({
					"AB, 1*",
					"ABC, *00",
					"ACB, *01",
				})
				@DisplayName("[$A [$B [$C]]] -> no matches")
				void testDoubleNestingFail(String target, String tree) {
					rawTest()
						.setup(setup(EQUALS_A, EQUALS_B, EQUALS_C))
						.target(target)
						.tree(tree)
						.startPos(0)
						.expectFail()
						.assertResult();
				}

				@ParameterizedTest
				@CsvSource({
					// Normal order - adjacent
					"ABC, *01, 0, 1, 2",
					"ABCD, *010, 0, 1, 2",
					"ABCD, *011, 0, 1, 2",
					"ABCD, *012, 0, 1, 2",
					// Normal order - mixed
					"ACB, *20, 0, 2, 1",
					"ACBD, *200, 0, 2, 1",
					"ACBD, *201, 0, 2, 1",
					"ACBD, *202, 0, 2, 1",
					"ACDB, *300, 0, 3, 1",
					"ACDB, *310, 0, 3, 1",
					"ACDB, *330, 0, 3, 1",
					"ADCB, *030, 0, 3, 2",
					// Reversed order - adjacent
					"BAC, 1*0, 1, 0, 2",
					"BCA, 20*, 2, 0, 1",
					// Reversed order - mixed
					"DBAC, 12*1, 2, 1, 3",
					"DBAC, 22*1, 2, 1, 3",
					"DBAC, 32*1, 2, 1, 3",
					"BDAC, 20*0, 2, 0, 3",
					"BDAC, 22*0, 2, 0, 3",
					"BDAC, 23*0, 2, 0, 3",
					"BADC, 1*00, 1, 0, 3",
					"BADC, 1*10, 1, 0, 3",
					"BADC, 1*30, 1, 0, 3",
					"BACD, 1*00, 1, 0, 2",
					"BACD, 1*01, 1, 0, 2",
					"BACD, 1*02, 1, 0, 2",
				})
				@DisplayName("[$A [$B [$C]]] -> 1 match")
				void testDoubleNesting(String target, String tree, int hitA, int hitB, int hitC) {
					rawTest()
						.setup(setup(EQUALS_A, EQUALS_B, EQUALS_C))
						.target(target)
						.tree(tree)
						.startPos(0)
						.expectSuccess(1)
						.result(result(0)
							.map(NODE_0, hitA)
							.map(NODE_1, hitB)
							.map(NODE_2, hitC))
						.assertResult();
				}
			}
		}

		@Nested
		class ForTreeClosure {

			//TODO fix setup


			/** {@code [$X [isAnyGeneration, $Y]]} */
			private StateMachineSetup transitiveSingleChildSetup(LevelFilter levelFilter) {

				Node atom = seq(
						new TreeClosure(id(), mock(IqlMarkerCall.class), CLOSURE_0, levelFilter, CACHE_2, PING_0),
						new Single(id(), mock(IqlNode.class), MAP_1, NODE_1, CACHE_1, NO_MEMBER, UNSET_INT),
						new Ping(id(), PING_0)
				);

				IqlTreeNode treeNode = mock(IqlTreeNode.class);
				TreeConn conn = new TreeConn(id(), treeNode, ANCHOR_0);
				Tree tree = new Tree(id(), treeNode, ANCHOR_0, atom, conn);
				last(atom).setNext(conn);

				StateMachineSetup sms = new StateMachineSetup();
				sms.mappedNodes = mockMappedNodes(2);
				sms.cacheCount = 3; // 0 = first node, 1 = closure, 2 = second node
				sms.anchorCount = 1;
				sms.closureCount = 1;
				sms.pingCount = 1;
				sms.initialSize = 10;
				sms.root = seq(
						new Exhaust(id(), true),
						new Single(id(), mock(IqlNode.class), MAP_0, NODE_0, CACHE_0, NO_MEMBER, ANCHOR_0),
						tree,
						new Finish(id(), NO_LIMIT, NO_STOP)
				);
				sms.matchers = matchers(matcher(NODE_0, EQUALS_X), matcher(NODE_1, EQUALS_Y));
				return sms;
			}

			@ParameterizedTest
			@CsvSource({
				"XY, 1*",
				"XXX, *01",
				"YXX, *01",
			})
			@DisplayName("[$X [isAnyGeneration, $Y]] -> no matches in {0} [{1}]")
			void testDoubleNestingFail(String target, String tree) {
				rawTest()
					.setup(transitiveSingleChildSetup(LevelFilter.ALL))
					.target(target)
					.tree(tree)
					.startPos(0)
					.expectFail()
					.assertResult();
			}

			@ParameterizedTest
			@CsvSource({
				// direct child
				"XY, *0, 0, 0, 1",
				"YX, 1*, 1, 1, 0",
				// direct child with siblings
				"XABY, *000, 0, 0, 3",
				"XAYB, *000, 0, 0, 2",
				"XYAB, *000, 0, 0, 1",
				"AXBY, 1*11, 1, 1, 3",
				"AXYB, 1*11, 1, 1, 2",
				"YXAB, 1*11, 1, 1, 0",
				"ABXY, 22*2, 2, 2, 3",
				"AYXB, 22*2, 2, 2, 1",
				"YAXB, 22*2, 2, 2, 0",
				"ABYX, 333*, 3, 3, 2",
				"AYBX, 333*, 3, 3, 1",
				"YABX, 333*, 3, 3, 0",
				// 1 intermediate node
				"XAY, *01, 0, 0, 2",
				"XYA, *20, 0, 0, 1",
				"AXY, 1*0, 1, 1, 2",
				"YXA, 2*1, 1, 1, 0",
				"YAX, 12*, 2, 2, 0",
				"AYX, 20*, 2, 2, 1",
				// multiple intermediate nodes (list-degenerated tree)
				"XABY, *012, 0, 0, 3",
				"AXBY, 1*02, 1, 1, 3",
				"ABXY, 20*1, 2, 2, 3",
				"ABYX, 301*, 3, 3, 2",
				// multiple intermediate nodes (flat tree) with sibling
				"XABCY, *0122, 0, 0, 4",
				"XABYC, *0122, 0, 0, 3",
			})
			@DisplayName("[$X [isAnyGeneration, $Y]] -> 1 hit in {0} [{1}] at {2} (hitX={3}, hitY={4})")
			void testSingleHit(String target, String tree, int startPos, int hitX, int hitY) {
				rawTest()
					.setup(transitiveSingleChildSetup(LevelFilter.ALL))
					.target(target)
					.tree(tree)
					.startPos(startPos)
					.expectSuccess(1)
					.result(result(0)
							.map(NODE_0, hitX)
							.map(NODE_1, hitY))
					// Cache for parent node
					.cache(cache(CACHE_0, false).set(startPos).hits(hitX))
					// Cache for child node
					.cache(cache(CACHE_1, false)
							.window(target)
							.setForWindow()
							.unset(hitX)
							.hits(hitY))
					// Cache for closure
					.cache(cache(CACHE_2, false)
							.window(target)
							.setForWindow()
							.unset(hitX)
							.hits(hitY))
					.assertResult();
			}
		}

		@Nested
		class ForCombinations {

			@Nested
			class PlainSequence {
				private Node[] nodes(int limit, CharPredicate...predicates) {
					int id = 0;
					List<Node> nodes = new ArrayList<>();
					for (int i = 0; i < predicates.length; i++) {
						nodes.add(new Exhaust(id(), true));
						nodes.add(new Single(id++, mock(IqlNode.class), i, i, i, NO_MEMBER, NO_ANCHOR));
					}
					nodes.add(new Finish(id(), limit, false));
					return nodes.toArray(new Node[0]);
				}

				@SuppressWarnings("unchecked")
				private StateMachineSetup setup(int limit, CharPredicate...predicates) {
					int nodeCount = predicates.length;
					StateMachineSetup sms = new StateMachineSetup();
					sms.mappedNodes = mockMappedNodes(nodeCount);
					sms.cacheCount = nodeCount;
					sms.limit = limit;
					sms.initialSize = 10;
					sms.root = seq(nodes(limit, predicates));
					sms.matchers = matchers(IntStream.range(0, nodeCount)
								.mapToObj(i -> matcher(i, predicates[i]))
								.toArray(Matcher[]::new));
					return sms;
				}

				@DisplayName("fail to find 2 nodes")
				@CsvSource({
					// min size violation
					"B, 0, -, -, -",
					"A, 0, -, -, -",
					// missing partner
					"BB, 0, 0, -, -",
					"AA, 0, 0, 1, 0",
					"B--B, 0, 0-2, -, -",
					"A--A, 0, 0-2, 1-3, 0",
				})
				@ParameterizedTest(name="{index}: A B in [{0}], start at {1}")
				void testDualNodeFail(String target, int startPos,
						@IntervalArg Interval visitedA,
						@IntervalArg Interval visitedB,
						@IntervalArg Interval hitsA) {
					rawTest()
					.setup(setup(NO_LIMIT, EQUALS_A, EQUALS_B))
					.target(target)
					.startPos(startPos)
					.expectFail()
					.cache(cache(CACHE_0, true)
							.window(visitedA)
							.hits(hitsA))
					.cache(cache(CACHE_1, true)
							.window(visitedB))
					.assertResult();
				}

				@DisplayName("find 2 nodes exactly once")
				@CsvSource({
					"AB, 0, 0, 1, 0, 1",
					"A--B, 0, 0-2, 1-3, 0, 3",
					"-A--B, 0, 0-3, 2-4, 1, 4",
					"-A--B-, 0, 0-4, 2-4, 1, 4",
				})
				@ParameterizedTest(name="{index}: A B in [{0}], start at {1}")
				void testDualNodeSingleHit(String target, int startPos,
						@IntervalArg Interval visitedA,
						@IntervalArg Interval visitedB,
						int hitA, int hitB) {
					rawTest()
					.setup(setup(NO_LIMIT, EQUALS_A, EQUALS_B))
					.target(target)
					.startPos(startPos)
					.expectedResult(true)
					.expectedCount(1)
					.cache(cache(CACHE_0, true)
							.window(visitedA)
							.hits(hitA))
					.cache(cache(CACHE_1, true)
							.window(visitedB)
							.hits(hitB))
					.result(result(0)
							.map(NODE_0, hitA)
							.map(NODE_1, hitB))
					.assertResult();
				}

				@DisplayName("find 2 nodes and partial sequence")
				@CsvSource({
					"ABA-, 0, 0-2, 1-3, 0, 2, 1",
					"-A--B--A--, 0, 0-8, 2-9, 1, 7, 4",
				})
				@ParameterizedTest(name="{index}: A B in [{0}], start at {1}")
				void testDualNodeSequenceRestart(String target, int startPos,
						@IntervalArg Interval visitedA,
						@IntervalArg Interval visitedB,
						int hitA1, int hitA2, int hitB) {
					rawTest()
					.setup(setup(NO_LIMIT, EQUALS_A, EQUALS_B))
					.target(target)
					.startPos(startPos)
					.expectedResult(true)
					.expectedCount(1)
					.cache(cache(CACHE_0, true)
							.window(visitedA)
							.hits(hitA1)
							.hits(hitA2)) // 2nd hit for A only reflected in cache, no dispatched result
					.cache(cache(CACHE_1, true)
							.window(visitedB)
							.hits(hitB))
					.result(result(0)
							.map(NODE_0, hitA1)
							.map(NODE_1, hitB))
					.assertResult();
				}

				@DisplayName("find 2 sequences for 2 nodes (based on A)")
				@CsvSource({
					"AAB-, 0, 0-2, 1-3, 0, 1, 2",
					"-A--A--B--, 0, 0-8, 2-9, 1, 4, 7",
				})
				@ParameterizedTest(name="{index}: A B in [{0}], start at {1}")
				void testDualNodeDualResultFromA(String target, int startPos,
						@IntervalArg Interval visitedA,
						@IntervalArg Interval visitedB,
						int hitA1, int hitA2, int hitB) {
					rawTest()
					.setup(setup(NO_LIMIT, EQUALS_A, EQUALS_B))
					.target(target)
					.startPos(startPos)
					.expectedResult(true)
					.expectedCount(2)
					.cache(cache(CACHE_0, true)
							.window(visitedA)
							.hits(hitA1)
							.hits(hitA2))
					.cache(cache(CACHE_1, true)
							.window(visitedB)
							.hits(hitB))
					.result(result(0)
							.map(NODE_0, hitA1)
							.map(NODE_1, hitB))
					.result(result(1)
							.map(NODE_0, hitA2)
							.map(NODE_1, hitB))
					.assertResult();
				}

				@DisplayName("find 2 sequences for 2 nodes (based on B)")
				@CsvSource({
					"A-BB, 0, 0-2, 1-3, 0, 2, 3",
					"-A--B--B--, 0, 0-8, 2-9, 1, 4, 7",
				})
				@ParameterizedTest(name="{index}: A B in [{0}], start at {1}")
				void testDualNodeDualResultFromB(String target, int startPos,
						@IntervalArg Interval visitedA,
						@IntervalArg Interval visitedB,
						int hitA, int hitB1, int hitB2) {
					rawTest()
					.setup(setup(NO_LIMIT, EQUALS_A, EQUALS_B))
					.target(target)
					.startPos(startPos)
					.expectedResult(true)
					.expectedCount(2)
					.cache(cache(CACHE_0, true)
							.window(visitedA)
							.hits(hitA))
					.cache(cache(CACHE_1, true)
							.window(visitedB)
							.hits(hitB1)
							.hits(hitB2))
					.result(result(0)
							.map(NODE_0, hitA)
							.map(NODE_1, hitB1))
					.result(result(1)
							.map(NODE_0, hitA)
							.map(NODE_1, hitB2))
					.assertResult();
				}

				//TODO scan for multiple matches (at least 2)
			}

			@Nested
			class BranchAndRepetition {
				private StateMachineSetup setup(Node...options) {
					StateMachineSetup sms = new StateMachineSetup();
					sms.mappedNodes = mockMappedNodes(2);
					sms.cacheCount = 2;
					sms.initialSize = 10;
					sms.root = seq(
							branch(0, options),
							new Finish(id(), NO_LIMIT, false));
					sms.matchers = matchers(
							matcher(0, EQUALS_A),
							matcher(1, EQUALS_B));
					return sms;
				}


				//TODO
			}
		}

	}

	/**
	 * Test family for the {@link StructureQueryProcessor}'s creation methods.
	 */
	@Nested
	class ForProcessor {
		//TODO verify that IQLElement instances get processed into the correct node configurations
	}

	/**
	 * Test family for processing and evaluation of full {@link IqlElement} constructs.
	 */
	@Nested
	class ForFullQueryElements {

		@Nested
		class ForIqlNode {

			@ParameterizedTest(name="{index}: [X] in {0}")
			@CsvSource({
				"-",
				"A-",
				"--",
				"-A-",
				"--A",
			})
			@DisplayName("Single node with no matches")
			void testSingleNodeFail(String target) {
				matcherTest()
				.root(IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))))
				.target(target)
				.expectFail()
				.assertResult();
			}

			@ParameterizedTest(name="{index}: [X] in {0}, hit={1}")
			@CsvSource({
				"X,   0",
				"X-,  0",
				"-X,  1",
				"-X-, 1",
				"--X, 2",
			})
			@DisplayName("Single node at various positions")
			void testSingleNodeHit(String target, int hit) {
				matcherTest()
				.root(IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))))
				.target(target)
				.expectSuccess(1)
				.cache(cache(CACHE_0, true)
						.window(0, target.length()-1)
						.hits(hit))
				.result(result(0)
						.map(NODE_0, hit))
				.assertResult();
			}

			@Nested
			class WithMarker {

				@ParameterizedTest(name="{index}: [isFirst,X] in {0}, hit={1}")
				@CsvSource({
					"X",
					"XX",
					"XXX",
				})
				@DisplayName("Node at first position")
				void testIsFirst(String target) {
					matcherTest()
					.root(IqlTestUtils.node(NO_LABEL,
							mark("isFirst"),
							constraint(eq_exp('X'))))
					.target(target)
					.expectSuccess(1)
					.cache(cache(CACHE_0, false)
							.window(target)
							.set(0)
							.hits(0))
					.result(result(0).map(NODE_0, 0))
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isFirst,X] in {0}, hit={1}")
				@CsvSource({
					"-",
					"-X",
					"-XX",
				})
				@DisplayName("Node mismatch at first position")
				void testIsFirstFail(String target) {
					matcherTest()
					.root(IqlTestUtils.node(NO_LABEL,
							mark("isFirst"),
							constraint(eq_exp('X'))))
					.target(target)
					.expectFail()
					.cache(cache(CACHE_0, false)
							.window(target)
							.set(0))
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isLast,X] in {0}, hit={1}")
				@CsvSource({
					"X",
					"XX",
					"XXX",
				})
				@DisplayName("Node at last position")
				void testIsLast(String target) {
					final int last = target.length()-1;
					matcherTest()
					.root(IqlTestUtils.node(NO_LABEL,
							mark("isLast"),
							constraint(eq_exp('X'))))
					.target(target)
					.expectSuccess(1)
					.cache(cache(CACHE_0, false)
							.window(target)
							.set(last)
							.hits(last))
					.result(result(0).map(NODE_0, target.length()-1))
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isLast,X] in {0}, hit={1}")
				@CsvSource({
					"-",
					"X-",
					"XX-",
				})
				@DisplayName("Node mismatch at last position")
				void testIsLastFail(String target) {
					final int last = target.length()-1;
					matcherTest()
					.root(IqlTestUtils.node(NO_LABEL,
							mark("isLast"),
							constraint(eq_exp('X'))))
					.target(target)
					.expectFail()
					.cache(cache(CACHE_0, false)
							.window(target)
							.set(last))
					.result(result(0).map(NODE_0, target.length()-1))
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isAt,X] in {0}, hit={1}")
				@CsvSource({
					"X, 0",
					"XX, 0",
					"XX, 1",
					"XXX, 0",
					"XXX, 1",
					"XXX, 2",
				})
				@DisplayName("Node at specific position")
				void testIsAt(String target, int pos) {
					final int last = target.length()-1;
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isAt", _int(pos+1)),
									constraint(eq_exp('X'))))
					.target(target)
					.expectSuccess(1)
					.cache(cache(CACHE_0, false)
							.window(0, last)
							.set(pos)
							.hits(pos))
					.result(result(0).map(NODE_0, pos))
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isAt,X] in {0}, hit={1}")
				@CsvSource({
					"-, 0",
					"-X, 0",
					"X-, 1",
					"-XX, 0",
					"X-X, 1",
					"XX-, 2",
				})
				@DisplayName("Node mismatch at specific position")
				void testIsAtFail(String target, int pos) {
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isAt", _int(pos+1)),
									constraint(eq_exp('X'))))
					.target(target)
					.expectFail()
					.cache(cache(CACHE_0, false)
							.window(target)
							.set(pos))
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isAfter({1}),X] in {0}, hits={2}")
				@CsvSource({
					"XX, 1, {1}",
					"XXX, 1, {1;2}",
					"XXX, 2, {2}",
				})
				@DisplayName("Node after specific position")
				void testIsAfter(String target, int arg, @IntervalArrayArg Interval[] hits) {
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isAfter", _int(arg)),
									constraint(eq_exp('X'))))
					.target(target)
					.expectSuccess(hits.length)
					.cache(cache(CACHE_0, false)
							.window(0, target.length()-1)
							.set(hits)
							.hits(hits))
					.results(NODE_0, hits)
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isAfter({1}),X] in {0}, hits={2}")
				@CsvSource({
					"X-, 1",
					"X--, 1",
					"XX-, 2",
				})
				@DisplayName("Node mismatch after specific position")
				void testIsAfterFail(String target, int arg) {
					final int last = target.length()-1;
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isAfter", _int(arg)),
									constraint(eq_exp('X'))))
					.target(target)
					.expectFail()
					.cache(cache(CACHE_0, false)
							.window(target)
							.set(Interval.of(arg, last)))
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isBefore({1}),X] in {0}, hits={2}")
				@CsvSource({
					"XX, 2, {0}",
					"XXX, 2, {0}",
					"XXX, 3, {0;1}",
				})
				@DisplayName("Node before specific position")
				void testIsBefore(String target, int arg, @IntervalArrayArg Interval[] hits) {
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isBefore", _int(arg)),
									constraint(eq_exp('X'))))
					.target(target)
					.expectSuccess(hits.length)
					.cache(cache(CACHE_0, false)
							.window(0, target.length()-1)
							.set(hits)
							.hits(hits))
					.results(NODE_0, hits)
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isBefore({1}),X] in {0}, hits={2}")
				@CsvSource({
					"-X, 2",
					"-XX, 2",
					"--X, 3",
				})
				@DisplayName("Node mismatch before specific position")
				void testIsBeforeFail(String target, int arg) {
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isBefore", _int(arg)),
									constraint(eq_exp('X'))))
					.target(target)
					.expectFail()
					.cache(cache(CACHE_0, false)
							.window(target)
							.set(Interval.of(0, arg-2)))
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isNotAt({1}),X] in {0}, hits1={2}, hits2={3}")
				@CsvSource({
					"XX, 1, {1}",
					"XXX, 1, {1;2}",
					"XXX, 2, {0;2}",
					"XXX, 3, {0;1}",
				})
				@DisplayName("Node at any but specific position")
				void testIsNotAt(String target, int arg,
						@IntervalArrayArg Interval[] hits) {
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isNotAt", _int(arg)),
									constraint(eq_exp('X'))))
					.target(target)
					.expectSuccess(hits.length)
					.cache(cache(CACHE_0, false)
							.window(target)
							.set(Interval.of(0, arg-2))
							.set(Interval.of(arg, target.length()-1))
							.hits(hits))
					.results(NODE_0, hits)
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isNotAt({1}),X] in {0}, hits1={2}, hits2={3}")
				@CsvSource({
					"X-, 1",
					"X--, 1",
					"-X-, 2",
					"--X, 3",
				})
				@DisplayName("Node mismatch at any but specific position")
				void testIsNotAtFail(String target, int arg) {
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isNotAt", _int(arg)),
									constraint(eq_exp('X'))))
					.target(target)
					.expectFail()
					.cache(cache(CACHE_0, false)
							.window(target)
							.set(Interval.of(0, arg-2))
							.set(Interval.of(arg, target.length()-1)))
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isInside({1},{2}),X] in {0}")
				@CsvSource({
					"X, 1, 1",
					"XX, 1, 1",
					"XX, 1, 2",
					"XX, 2, 2",
					"XXX, 1, 1",
					"XXX, 1, 2",
					"XXX, 2, 2",
					"XXX, 2, 3",
					"XXX, 3, 3",
					"XXX, 1, 3",
				})
				@DisplayName("Node inside specific region [full region match]")
				void testIsInside(String target, int from, int to) {
					// Remember that markers use 1-based value space
					final Interval region = Interval.of(from-1, to-1);
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isInside", _int(from), _int(to)),
									constraint(eq_exp('X'))))
					.target(target)
					.expectSuccess(region.size())
					.cache(cache(CACHE_0, false)
							.window(target)
							.set(region)
							.hits(region))
					.results(NODE_0, region.asArray())
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isInside({1},{2}),X] in {0}")
				@CsvSource({
					"X-, 1, 2, 0",
					"-X, 1, 2, 1",
					"X-X, 1, 2, 0",
					"-XX, 1, 2, 1",
					"XX-, 2, 3, 1",
					"X-X, 2, 3, 2",
					"X--, 1, 3, 0",
					"-X-, 1, 3, 1",
					"--X, 1, 3, 2",
				})
				@DisplayName("Node inside specific region [single node match]")
				void testIsInsidePartial1(String target, int from, int to, int hit) {
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isInside", _int(from), _int(to)),
									constraint(eq_exp('X'))))
					.target(target)
					.expectSuccess(1)
					.cache(cache(CACHE_0, false)
							.window(target)
							.set(Interval.of(from-1, to-1))
							.hits(hit))
					.result(result(0).map(NODE_0, hit))
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isInside({1},{2}),X] in {0}")
				@CsvSource({
					"X-X, 1, 3, {0;2}",
					"-XX, 1, 3, {1;2}",
					"XX-, 1, 3, {0;1}",
				})
				@DisplayName("Node inside specific region [multiple matches]")
				void testIsInsidePartial2(String target, int from, int to,
						@IntArrayArg int[] hits) {
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isInside", _int(from), _int(to)),
									constraint(eq_exp('X'))))
					.target(target)
					.expectSuccess(2)
					.cache(cache(CACHE_0, true)
							.window(target)
							.hits(hits))
					.results(NODE_0, hits)
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isInside({1},{2}),X] in {0}")
				@CsvSource({
					"-, 1, 1",
					"-X, 1, 1",
					"--, 1, 2",
					"X-, 2, 2",
					"-XX, 1, 1",
					"--X, 1, 2",
					"X-X, 2, 2",
					"X--, 2, 3",
					"XX-, 3, 3",
					"---, 1, 3",
				})
				@DisplayName("Node mismatch inside specific region")
				void testIsInsideFail(String target, int from, int to) {
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isInside", _int(from), _int(to)),
									constraint(eq_exp('X'))))
					.target(target)
					.expectFail()
					.cache(cache(CACHE_0, false)
							.window(target)
							.set(Interval.of(from-1, to-1)))
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isOutside({1},{2}),X] in {0}")
				@CsvSource({
					"XX, 1, 1, {1}",
					"XX, 2, 2, {0}",
					"XXX, 1, 1, {1;2}",
					"XXX, 1, 2, {2}",
					"XXX, 2, 2, {0;2}",
					"XXX, 2, 3, {0}",
					"XXX, 3, 3, {0;1}",
				})
				@DisplayName("Node outside specific region [full region match]")
				void testIsOutside(String target, int from, int to,
						@IntervalArrayArg Interval[] hits) {
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isOutside", _int(from), _int(to)),
									constraint(eq_exp('X'))))
					.target(target)
					.expectSuccess(hits.length)
					.cache(cache(CACHE_0, false)
							.window(target)
							.setForWindow()
							.unset(Interval.of(from-1, to-1))
							.hits(hits))
					.results(NODE_0, hits)
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isOutside({1},{2}),X] in {0}")
				@CsvSource({
					"XX--, 1, 1, 1",
					"X-X-, 1, 1, 2",
					"X--X, 1, 1, 3",

					"XXX-, 1, 2, 2",
					"XX-X, 1, 2, 3",

					"XX--, 2, 2, 0",
					"-XX-, 2, 2, 2",
					"-X-X, 2, 2, 3",

					"XXX-, 2, 3, 0",
					"-XXX, 2, 3, 3",

					"X-X-, 3, 3, 0",
					"-XX-, 3, 3, 1",
					"--XX, 3, 3, 3",

					"X-XX, 3, 4, 0",
					"-XXX, 3, 4, 1",

					"X--X, 4, 4, 0",
					"-X-X, 4, 4, 1",
					"--XX, 4, 4, 2",
				})
				@DisplayName("Node outside specific region [single node match]")
				void testIsOutsidePartial1(String target, int from, int to, int hit) {
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isOutside", _int(from), _int(to)),
									constraint(eq_exp('X'))))
					.target(target)
					.expectSuccess(1)
					.cache(cache(CACHE_0, false)
							.window(target)
							.setForWindow()
							.unset(Interval.of(from-1, to-1))
							.hits(hit))
					.result(result(0).map(NODE_0, hit))
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isOutside({1},{2}),X] in {0}")
				@CsvSource({
					"XX-X, 1, 1, {1;3}",
					"X-XX, 1, 1, {2;3}",
					"XXX-, 1, 1, {1;2}",

					"XXX-, 2, 2, {0;2}",
					"XX-X, 2, 2, {0;3}",
					"-XXX, 2, 2, {2;3}",

					"XXX-, 3, 3, {0;1}",
					"X-XX, 3, 3, {0;3}",
					"-XXX, 3, 3, {1;3}",

					"XX-X, 4, 4, {0;1}",
					"X-XX, 4, 4, {0;2}",
					"-XXX, 4, 4, {1;2}",

					"XXXX-, 1, 2, {2;3}",
					"XXX-X, 1, 2, {2;4}",
					"XX-XX, 1, 2, {3;4}",

					"XXXX-, 2, 3, {0;3}",
					"XXX-X, 2, 3, {0;4}",
					"-XXXX, 2, 3, {3;4}",

					"XXXX-, 3, 4, {0;1}",
					"X-XXX, 3, 4, {0;4}",
					"-XXXX, 3, 4, {1;4}",

					"XX-XX, 4, 5, {0;1}",
					"X-XXX, 4, 5, {0;2}",
					"-XXXX, 4, 5, {1;2}",
				})
				@DisplayName("Node outside specific region [dual node match]")
				void testIsOutsidePartial2(String target, int from, int to,
						@IntArrayArg int[] hits) {
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isOutside", _int(from), _int(to)),
									constraint(eq_exp('X'))))
					.target(target)
					.expectSuccess(2)
					.cache(cache(CACHE_0, false)
							.window(target)
							.setForWindow()
							.unset(Interval.of(from-1, to-1))
							.hits(hits))
					.results(NODE_0, hits)
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isOutside({1},{2}),X] in {0}")
				@CsvSource({
					"X-, 1, 1",
					"-X, 2, 2",

					"X--, 1, 1",
					"XX-, 1, 2",
					"-X-, 2, 2",
					"-XX, 2, 3",
					"--X, 3, 3",
				})
				@DisplayName("Node mismatch outside specific region")
				void testIsOutsideFail(String target, int from, int to) {
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isOutside", _int(from), _int(to)),
									constraint(eq_exp('X'))))
					.target(target)
					.expectFail()
					.cache(cache(CACHE_0, false)
							.window(target)
							.setForWindow()
							.unset(Interval.of(from-1, to-1)))
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [isAfter({1}),X] in {0}")
				@CsvSource({
					"XX, 1, 1, {1}",
					"XXX, 1, 1-2, {1-2}",
					"X-X-, 1, 2, {1-3}",
					"XXXX, 2, 2-3, {2-3}",
				})
				@DisplayName("Verify that markers allow the inner scan to skip")
				void testSkip(String target, int loc, @IntervalArg Interval hits,
						@IntervalArrayArg Interval[] visited) {
					matcherTest()
					.root(// Remember that markers use 1-based value space
							IqlTestUtils.node(NO_LABEL,
									mark("isAfter", _int(loc)),
									constraint(eq_exp('X'))))
					// Make sure we can properly track the scan progress via forced cache!
					.modBuilder(CACHE_ALL)
					.target(target)
					.expectSuccess(hits.size())
					.cache(cache(CACHE_0, false)
							.window(target)
							.set(visited)
							.hits(hits))
					.assertResult();
				}
			}

			@Nested
			class WithQuantifier {

				@Nested
				class Negated {

					@ParameterizedTest(name="{index}: ![X] in {0}")
					@CsvSource({
						"-",
						"Y",
						"--",
						"-Y-",
					})
					@DisplayName("Negated node")
					void testNegated(String target) {
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										negated()))
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, true)
								.window(target))
						// Cache of the negated search
						.cache(cache(CACHE_1, true)
								.window(target)
								.hitsForWindow())
						.assertResult();
					}

					@ParameterizedTest(name="{index}: ![X] in {0}")
					@CsvSource({
						"X, 0",
						"-X, 1",
						"X-, 0",
						"-X-, 1",
						"--X, 2",
					})
					@DisplayName("Mismatch of negated node")
					void testNegatedFail(String target, int hit) {
						final Interval visited = Interval.of(0, hit);
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										negated()))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hit))
						// Cache of the negated search
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited)
								.hits(Interval.of(0, hit-1)))
						.assertResult();
					}
				}

				@Nested
				class All {

					@ParameterizedTest(name="{index}: *[X] in {0}")
					@CsvSource({
						"X",
						"XX",
						"XXX",
						"XXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
					})
					@DisplayName("Universally quantified node")
					void testUniversallyQuantified(String target) {
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										all()))
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, true)
								.window(target)
								.hitsForWindow())
						//TODO once we added flag to disable mapping for universal quantification, add check here against mapping
						.assertResult();
					}

					@ParameterizedTest(name="{index}: *[X] in {0}")
					@CsvSource({
						"Y, 0",
						"YX, 0",
						"XY, 1",
						"XYX, 1",
						"XXXXXXXXXXXXXX-XXXXXXXXXXXXXX, 14",
					})
					@DisplayName("Mismatch of universally quantified node")
					void testUniversallyQuantifiedFail(String target, int gap) {
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										all()))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, true)
								.window(Interval.of(0, gap))
								.hits(Interval.of(0, gap-1)))
						.assertResult();
					}
				}

				@Nested
				class Exact {

					@ParameterizedTest(name="{index}: <{1}>[X] in {0}")
					@CsvSource({
						"X, 1, 0, 0",
						"X-, 1, 0, 0-1",
						"-X, 1, 1, 0-1",
						"-X-, 1, 1, 0-2",
						"--X, 1, 2, 0-2",

						"XX, 2, 0-1, 0-1",
						"XX-, 2, 0-1, 0-2",
						"-XX, 2, 1-2, 0-2",
						"-XX-, 2, 1-2, 0-3",
						"--XX, 2, 2-3, 0-3",
						"XX--, 2, 0-1, 0-2",

						"--XXXXXXXXXX--, 10, 2-11, 0-12",
					})
					@DisplayName("Node with exact multiplicity [single hit]")
					void testExact(String target, int count,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, CONTINUOUS)))
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}>[X] in {0}")
					@CsvSource({
						"X, 1, {0}, 0",
						"X-, 1, {0}, 0-1",
						"-X, 1, {1}, 0-1",
						"-X-, 1, {1}, 0-2",
						"--X, 1, {2}, 0-2",

						"XX, 2, {0-1}, 0-1",
						"XX-, 2, {0-1}, 0-2",
						"-XX, 2, {1-2}, 0-2",
						"X-X, 2, {0;2}, 0-2",
						"-XX-, 2, {1-2}, 0-3",
						"--XX, 2, {2-3}, 0-3",
						"X-X-, 2, {0;2}, 0-3",
						"XX--, 2, {0-1}, 0-3",
						"-X-X, 2, {1;3}, 0-3",

						"--XXXXXXXXXX--, 10, {2-11}, 0-13",
						"--XXXXXXXX--XX, 10, {2-9;12-13}, 0-13",
					})
					@DisplayName("Node with exact multiplicity [single hit, discontinuous]")
					void testExactDiscontinuous(String target, int count,
							@IntervalArrayArg Interval[] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, DISCONTINUOUS)))
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}>[X] in {0}")
					@CsvSource({
						"-, 1, 0, -",
						"--, 1, 0-1, -",
						"-Y-, 1, 0-2, -",

						"--, 2, 0, -",
						"-X, 2, 0, -",
						"X-, 2, 0-1, 0",
						"X--, 2, 0-1, 0",
						"-X-, 2, 0-2, 1",
						"X-X, 2, 0-1, 0",

						"--XXXXXXXXX--X, 10, 0-11, 2-10",
					})
					@DisplayName("Node mismatch with exact multiplicity")
					void testExactFail(String target, int count,
							@IntervalArg Interval visited,
							@IntervalArg Interval hits) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, CONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}>[X] in {0}")
					@CsvSource({
						"-, 1, 0, -",
						"--, 1, 0-1, -",
						"-Y-, 1, 0-2, -",

						"--, 2, 0, -",
						"-X, 2, 0, -",
						"X-, 2, 0-1, {0}",
						"X--, 2, 0-2, {0}",
						"-X-, 2, 0-2, {1}",
						"--X, 2, 0-1, -",

						"--XXXXXXXX--X, 10, 0-12, {2-9;12}",
					})
					@DisplayName("Node mismatch with exact multiplicity [discontinuous]")
					void testExactFailDiscontinuous(String target, int count,
							@IntervalArg Interval visited,
							@IntervalArrayArg Interval[] hits) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, DISCONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}>[X] in {0}")
					@CsvSource({
						"X-X, 1, 0, 2, 0-2",
						"XX-, 1, 0, 1, 0-2",
						"-XX, 1, 1, 2, 0-2",

						"XXX, 2, 0-1, 1-2, 0-2",

						"XX-XX, 2, 0-1, 3-4, 0-4",
						"-XXX, 2, 1-2, 2-3, 0-3",
						"XXX-, 2, 0-1, 1-2, 0-3",

						"XX--XX, 2, 0-1, 4-5, 0-5",
						// verify that we don't look too far
						"XX--XX-, 2, 0-1, 4-5, 0-6",
						"XX--XX--, 2, 0-1, 4-5, 0-6",
					})
					@DisplayName("Node with exact multiplicity [2 hits]")
					void testExactMultiple(String target, int count,
							@IntervalArg Interval hits1,
							@IntervalArg Interval hits2,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, CONTINUOUS)))
						.target(target)
						.expectSuccess(2)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits1)
								.hits(hits2))
						.result(result(0).map(NODE_0, hits1))
						.result(result(1).map(NODE_0, hits2))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}>[X] in {0}")
					@CsvSource({
						"X-X, 1, {{0}{2}}, 0-2",
						"XX-, 1, {{0}{1}}, 0-2",
						"-XX, 1, {{1}{2}}, 0-2",

						"XXX, 2, {{0;1}{1;2}}, 0-2",

						"XX-XX, 2, {{0;1}{1;3}{3;4}}, 0-4",
						"-XXX, 2, {{1;2}{2;3}}, 0-3",
						"XXX-, 2, {{0;1}{1;2}}, 0-3",

						"XX--XX, 2, {{0;1}{1;4}{4;5}}, 0-5",
						// verify that we actually try to look ahead
						"XX--XX-, 2, {{0;1}{1;4}{4;5}}, 0-6",
						"XX--XX--, 2, {{0;1}{1;4}{4;5}}, 0-7",
					})
					@DisplayName("Node with exact multiplicity [multiple hits, discontinuous]")
					void testExactMultipleDiscontinuous(String target, int count,
							@IntMatrixArg int[][] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, DISCONTINUOUS)))
						.target(target)
						.expectSuccess(hits.length)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.results(hits.length, (r, i) -> r.map(NODE_0, hits[i]))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}>[X] in {0}, {2} matches")
					@CsvSource({
						"XXX, 2, 2, 0-2, 0-2",
						"XXXX, 2, 3, 0-3, 0-3",
						"XXXXX, 2, 4, 0-4, 0-4",
						"XXXX-, 2, 3, 0-3, 0-4",

						"XXXX, 3, 2, 0-3, 0-3",
						"XXXXX, 3, 3, 0-4, 0-4",
						"XXXX-, 3, 2, 0-3, 0-4",
						"XXXX--, 3, 2, 0-3, 0-4",

						"--XXXXXXXXXX--, 5, 6, 2-11, 0-12",
					})
					@DisplayName("Node with exact multiplicity [overlapping hits]")
					void testExactCascade(String target, int count, int matches,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, CONTINUOUS)))
						.target(target)
						.expectSuccess(matches)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.results(matches, (r, i) -> r.map(NODE_0, Interval.of(
								hits.from+i, hits.from+i+count-1)))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}>[X] in {0}")
					@CsvSource({
						"XXX, 2, {{0;1}{1;2}}, 0-2",
						"XXXX, 2, {{0;1}{1;2}{2;3}}, 0-3",
						"XXXXX, 2, {{0;1}{1;2}{2;3}{3;4}}, 0-4",
						"XXXX-, 2, {{0;1}{1;2}{2;3}}, 0-4",

						"XXX-X, 2, {{0;1}{1;2}{2;4}}, 0-4",
						"XX-XX, 2, {{0;1}{1;3}{3;4}}, 0-4",
						"X-XXX, 2, {{0;2}{2;3}{3;4}}, 0-4",

						"XXXX, 3, {{0;1;2}{1;2;3}}, 0-3",
						"XXXXX, 3, {{0;1;2}{1;2;3}{2;3;4}}, 0-4",
						"XXXX-, 3, {{0;1;2}{1;2;3}}, 0-4",
						"XXXX--, 3, {{0;1;2}{1;2;3}}, 0-5",

						"XXX-X, 3, {{0;1;2}{1;2;4}}, 0-4",
						"XXX-XX, 3, {{0;1;2}{1;2;4}{2;4;5}}, 0-5",
					})
					@DisplayName("Node with exact multiplicity [overlapping hits, discontinunous]")
					void testExactCascadeDiscontinuous(String target, int count,
							@IntMatrixArg int[][] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, DISCONTINUOUS)))
						.target(target)
						.expectSuccess(hits.length)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.results(hits.length, (r, i) -> r.map(NODE_0, hits[i]))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}>[X] in {0}, {2} matches")
					@CsvSource({
						"XX, 1, 2, 0-1, 0-1",
						"XXX, 1, 3, 0-2, 0-2",
						"XXXX, 1, 4, 0-3, 0-3",
						"XXX-, 1, 3, 0-2, 0-3",

						"XXX, 2, 1, 0-1, 0-1",
						"XXXX, 2, 2, 0-3, 0-3",
						"XXXXX, 2, 2, 0-3, 0-3",
						"XXXX-, 2, 2, 0-3, 0-3",
						"XXXX--, 2, 2, 0-3, 0-4",

						"XXXX, 3, 1, 0-2, 0-2",
						"XXXXX, 3, 1, 0-2, 0-2",
						"XXXXXX, 3, 2, 0-5, 0-5",
						"XXXXXXX, 3, 2, 0-5, 0-5",
						"XXXXXXX-, 3, 2, 0-5, 0-5",
						"XXXXXXX--, 3, 2, 0-6, 0-7",
						"XXXXXXXXX, 3, 3, 0-8, 0-8",
					})
					@DisplayName("Node with exact multiplicity [disjoint adjacent hits]")
					void testExactDisjoint(String target, int count, int matches,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, CONTINUOUS)))
						.modBuilder(DISJOINT)
						.target(target)
						.expectSuccess(matches)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.results(matches, (r, i) -> {
							int begin = hits.from+(i*count);
							int end = begin + count-1;
							r.map(NODE_0, Interval.of(begin, end));
						})
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}>[X] in {0}")
					@CsvSource({
						"XX, 1, {{0}{1}}, 0-1",
						"XXX, 1, {{0}{1}{2}}, 0-2",
						"XXXX, 1, {{0}{1}{2}{3}}, 0-3",
						"XXX-, 1, {{0}{1}{2}}, 0-3",
						"XX-X, 1, {{0}{1}{3}}, 0-3",
						"X-XX, 1, {{0}{2}{3}}, 0-3",
						"-XXX, 1, {{1}{2}{3}}, 0-3",

						"XXX, 2, {{0;1}}, 0-1",
						"XXXX, 2, {{0;1}{2;3}}, 0-3",
						"XXXXX, 2, {{0;1}{2;3}}, 0-3",
						"XXXX-, 2, {{0;1}{2;3}}, 0-3",
						"XXX-X, 2, {{0;1}{2;4}}, 0-4",
						"XX-XX, 2, {{0;1}{3;4}}, 0-4",
						"X-XXX, 2, {{0;2}{3;4}}, 0-4",
						"-XXXX, 2, {{1;2}{3;4}}, 0-4",
						"XXXX--, 2, {{0;1}{2;3}}, 0-4",

						"XXXX, 3, {{0;1;2}}, 0-2",
						"XXXXX, 3, {{0;1;2}}, 0-2",
						"XXXXXX, 3, {{0;1;2}{3;4;5}}, 0-5",
						"XXXXXXX, 3, {{0;1;2}{3;4;5}}, 0-5",
						"XXXXXXX-, 3, {{0;1;2}{3;4;5}}, 0-5",
						"XXXXXX-X, 3, {{0;1;2}{3;4;5}}, 0-5",
						"XXXXX-XX, 3, {{0;1;2}{3;4;6}}, 0-6",
						"XXXX-XXX, 3, {{0;1;2}{3;5;6}}, 0-6",
						"XXX-XXXX, 3, {{0;1;2}{4;5;6}}, 0-6",
						"XX-XXXXX, 3, {{0;1;3}{4;5;6}}, 0-6",
						"X-XXXXXX, 3, {{0;2;3}{4;5;6}}, 0-6",
						"-XXXXXXX, 3, {{1;2;3}{4;5;6}}, 0-6",
						"XXXXXXX--, 3, {{0;1;2}{3;4;5}}, 0-8",
						"XXXXXXXXX, 3, {{0;1;2}{3;4;5}{6;7;8}}, 0-8",
						"X-X--XX-X-X--X, 3, {{0;2;5}{6;8;10}}, 0-11",
					})
					@DisplayName("Node with exact multiplicity [disjoint adjacent hits, discontinuous]")
					void testExactDisjointDiscontinuous(String target, int count,
							@IntMatrixArg int[][] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, DISCONTINUOUS)))
						.modBuilder(DISJOINT)
						.target(target)
						.expectSuccess(hits.length)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(target, visited, EQUALS_X))
						.results(hits.length, (r, i) -> r.map(NODE_0, hits[i]))
						.assertResult();
					}

				}

				@Nested
				class AtLeast {

					@ParameterizedTest(name="{index}: <{1}+>[X] in {0}")
					@CsvSource({
						"X, 1, 0, 0",
						"X-, 1, 0, 0-1",
						"-X, 1, 1, 0-1",
						"XX-, 1, 0-1, 0-2",
						"-XX, 1, 1-2, 0-2",

						"XX, 2, 0-1, 0-1",
						"XX-, 2, 0-1, 0-2",
						"XXX, 2, 0-2, 0-2",
						"-XX, 2, 1-2, 0-2",
						"-XX-, 2, 1-2, 0-3",
						"-XXX, 2, 1-3, 0-3",
						"XXX-, 2, 0-2, 0-3",
						"--XX, 2, 2-3, 0-3",
						"XX--, 2, 0-1, 0-2",

						"--XXXXXXXXXX--, 10, 2-11, 0-12",
					})
					@DisplayName("Node with a minimum multiplicity [greedy mode, single hit, limit]")
					void testGreedy(String target, int count,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atLeastGreedy(count, CONTINUOUS)))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+^>[X] in {0}")
					@CsvSource({
						"X, 1, {0}, 0",
						"X-, 1, {0}, 0-1",
						"-X, 1, {1}, 0-1",
						"XX-, 1, {0-1}, 0-2",
						"-XX, 1, {1-2}, 0-2",
						"X-X, 1, {0;2}, 0-2",
						"X-X-, 1, {0;2}, 0-3",
						"X-X-X, 1, {0;2;4}, 0-4",
						"X-XX-XX, 1, {0;2-3;5-6}, 0-6",

						"XX, 2, {0-1}, 0-1",
						"XX-, 2, {0-1}, 0-2",
						"XXX, 2, {0-2}, 0-2",
						"X-X, 2, {0;2}, 0-2",
						"-XX, 2, {1-2}, 0-2",
						"-XX-, 2, {1-2}, 0-3",
						"-X-X, 2, {1;3}, 0-4",
						"-XXX, 2, {1-3}, 0-3",
						"XXX-, 2, {0-2}, 0-3",
						"--XX, 2, {2-3}, 0-3",
						"XX--, 2, {0-1}, 0-3",
						"XX-XX, 2, {0-1;3-4}, 0-4",
						"-XX-XX-, 2, {1-2;4-5}, 0-6",

						"--XXXXXXXXXX--, 10, {2-11}, 0-13",
					})
					@DisplayName("Node with a minimum multiplicity [greedy mode, single hit, limit, discontinuous]")
					void testGreedyDiscontinuous(String target, int count,
							@IntervalArrayArg Interval[] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atLeastGreedy(count, DISCONTINUOUS)))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+>[X] in {0}")
					@CsvSource({
						"X-, 2, 0-1, {0}",
						"-X, 2, 0, -", // early-abort from scan
						"-X-, 2, 0-2, {1}",
						"X-X, 2, 0-1, {0}",
					})
					@DisplayName("Mismatch with a minimum multiplicity [greedy mode]")
					void testGreedyFail(String target, int count,
							@IntervalArg Interval visited,
							@IntArrayArg int[] candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atLeastGreedy(count, CONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(candidates))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+^>[X] in {0}")
					@CsvSource({
						"X-, 2, 0-1, {0}",
						"-X, 2, 0, -", // early-abort from scan
						"-X-, 2, 0-2, {1}",
						"X--, 3, 0-2, {0}",
						"X-X-, 3, 0-3, {0;2}",
					})
					@DisplayName("Mismatch with a minimum multiplicity [greedy mode, discontinuous]")
					void testGreedyFailDiscontinuous(String target, int count,
							@IntervalArg Interval visited,
							@IntArrayArg int[] candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atLeastGreedy(count, DISCONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(candidates))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+>[X] in {0}")
					@CsvSource({
						"XX, 1, 0-1, 1, 0-1",
						"XX-, 1, 0-1, 1, 0-2",
						"-XX, 1, 1-2, 2, 0-2",
						"-XX-, 1, 1-2, 2, 0-3",

						"XXX, 2, 0-2, 1-2, 0-2",
						"-XXX, 2, 1-3, 2-3, 0-3",
						"XXX-, 2, 0-2, 1-2, 0-3",
						"-XXX-, 2, 1-3, 2-3, 0-4",

						"-XXXX--XXXX-, 4, 1-4, 7-10, 0-11",
					})
					@DisplayName("Node with a minimum multiplicity [greedy mode, 2 hits, limited]")
					void testGreedyMultiple(String target, int count,
							@IntervalArg Interval hits1,
							@IntervalArg Interval hits2,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atLeastGreedy(count, CONTINUOUS)))
						.target(target)
						.expectSuccess(2)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits1)
								.hits(hits2))
						.result(result(0)
								.map(NODE_0, hits1))
						.result(result(1)
								.map(NODE_0, hits2))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+^>[X] in {0}")
					@CsvSource({
						"XX, 1, {{0;1}{1}}, 0-1",
						"XX-, 1, {{0;1}{1}}, 0-2",
						"-XX, 1, {{1;2}{2}}, 0-2",
						"-XX-, 1, {{1;2}{2}}, 0-3",

						"XXX, 2, {{0;1;2}{1;2}}, 0-2",
						"X-XX, 2, {{0;2;3}{2;3}}, 0-3",
						"XX-X, 2, {{0;1;3}{1;3}}, 0-3",
						"-XXX, 2, {{1;2;3}{2;3}}, 0-3",
						"XXX-, 2, {{0;1;2}{1;2}}, 0-3",
						"-XXX-, 2, {{1;2;3}{2;3}}, 0-4",

						"-XXX--XXX-, 3, {{1;2;3;6;7;8}{2;3;6;7;8}{3;6;7;8}{6;7;8}}, 0-9",
					})
					@DisplayName("Node with a minimum multiplicity [greedy mode, multiple hits, discontinuous]")
					void testGreedyMultipleDiscontinuous(String target, int count,
							@IntMatrixArg int[][] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atLeastGreedy(count, DISCONTINUOUS)))
						.target(target)
						.expectSuccess(hits.length)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.results(hits.length, (r, i) -> r.map(NODE_0, hits[i]))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+>[x|X][x] in {0}")
					@CsvSource({
						// Expansion of size 1
						"Xx, 1, 0, 1, 0-1, 0-1, 1",
						"XXx, 1, 0-1, 2, 0-2, 0-2, 2",
						"XXx-, 1, 0-1, 2, 0-2, 0-3, 2-3",
						"-XXx, 1, 1-2, 3, 1-3, 0-3, 3",
						"-XXx-, 1, 1-2, 3, 1-3, 0-4, 3-4",
						"XxX, 1, 0, 1, 0-2, 0-2, 1-2",
						"XxX-, 1, 0, 1, 0-2, 0-3, 1-3",
						"-XxX, 1, 1, 2, 1-3, 0-3, 2-3",
						"-XxX-, 1, 1, 2, 1-3, 0-4, 2-4",
						// Expansion of size 2
						"XXx, 2, 0-1, 2, 0-2, 0-2, 2",
						"XXXx, 2, 0-2, 3, 0-3, 0-3, 3",
						"XXxX, 2, 0-1, 2, 0-3, 0-3, 2-3",
						"XXxX-, 2, 0-1, 2, 0-3, 0-4, 2-4",
						"-XXxX, 2, 1-2, 3, 1-4, 0-4, 3-4",
						"-XXxX-, 2, 1-2, 3, 1-4, 0-5, 3-5",
						// Consume first target for second node
						"XxXxX, 1, 0-2, 3, 0-4, 0-4, 3-4",
						"XxXxX-, 1, 0-2, 3, 0-4, 0-5, 3-5",
						"-XxXxX, 1, 1-3, 4, 1-5, 0-5, 4-5",
						"-XxXxX-, 1, 1-3, 4, 1-5, 0-6, 4-6",
						// Greediness
						"Xxx, 1, 0-1, 2, 0-2, 0-2, 2",
						"Xxxx, 1, 0-2, 3, 0-3, 0-3, 3",
						"Xxxx-, 1, 0-2, 3, 0-3, 0-4, 3-4",
						"Xxx, 2, 0-1, 2, 0-2, 0-2, 2",
						"Xxxx, 2, 0-2, 3, 0-3, 0-3, 3",
						"Xxxx-, 2, 0-2, 3, 0-3, 0-4, 3-4",
					})
					@DisplayName("verify greedy expansion with multiple nodes [limited]")
					void testGreedyCompetition(String target,
							int count, // argument for 'AtLeast' marker
							@IntervalArg Interval hits1, // reported hits for first node
							int hit2, // reported hit for second node
							@IntervalArg Interval candidates, // cached hits for first node
							@IntervalArg Interval visited1, // all slots visited for first node
							@IntervalArg Interval visited2) { // all slots visited for second node
						/*
						 * We expect NODE_1 to visit and greedily consume all the
						 * X and x slots and then back off until the first x is
						 * reached for NODE_0.
						 */
						matcherTest()
						.root(ordered(false,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atLeastGreedy(count, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(candidates))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.result(result(0)
								.map(NODE_0, hits1)
								.map(NODE_1, hit2))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+^>[x|X][x] in {0}")
					@CsvSource({
						// Expansion of size 1
						"Xx, 1, {0}, 1, 0-1, 1",
						"XXx, 1, {0;1}, 2, 0-2, 2",
						"X-Xx, 1, {0;2}, 3, 0-3, 3",
						"XXx-, 1, {0;1}, 2, 0-3, 2-3",
						"X-Xx-, 1, {0;2}, 3, 0-4, 3-4",
						"-XXx, 1, {1;2}, 3, 0-3, 3",
						"-X-Xx, 1, {1;3}, 4, 0-4, 4",
						"-XXx-, 1, {1;2}, 3, 0-4, 3-4",
						"XxX, 1, {0}, 1, 0-2, 1-2",
						"Xx-X, 1, {0}, 1, 0-3, 1-3",
						"XxX-, 1, {0}, 1, 0-3, 1-3",
						"-XxX, 1, {1}, 2, 0-3, 2-3",
						"-Xx-X, 1, {1}, 2, 0-4, 2-4",
						"-XxX-, 1, {1}, 2, 0-4, 2-4",
						"-Xx-X-, 1, {1}, 2, 0-5, 2-5",
						// Expansion of size 2
						"XXx, 2, {0;1}, 2, 0-2, 2",
						"X-Xx, 2, {0;2}, 3, 0-3, 3",
						"XXXx, 2, {0;1;2}, 3, 0-3, 3",
						"X-X-Xx, 2, {0;2;4}, 5, 0-5, 5",
						"XXxX, 2, {0;1}, 2, 0-3, 2-3",
						"X-XxX, 2, {0;2}, 3, 0-4, 3-4",
						"XXx-X, 2, {0;1}, 2, 0-4, 2-4",
						"XXxX-, 2, {0;1}, 2, 0-4, 2-4",
						"-XXxX, 2, {1;2}, 3, 0-4, 3-4",
						"-X-XxX, 2, {1;3}, 4, 0-5, 4-5",
						"-XXxX-, 2, {1;2}, 3, 0-5, 3-5",
						// Consume first target for second node
						"XxXxX, 1, {0;1;2}, 3, 0-4, 3-4",
						"Xx-XxX, 1, {0;1;3}, 4, 0-5, 4-5",
						"XxX-xX, 1, {0;1;2}, 4, 0-5, 3-5",
						"XxXxX-, 1, {0;1;2}, 3, 0-5, 3-5",
						"-XxXxX, 1, {1;2;3}, 4, 0-5, 4-5",
						"-XxXxX-, 1, {1;2;3}, 4, 0-6, 4-6",
						// Greediness
						"Xxx, 1, {0;1}, 2, 0-2, 2",
						"Xxxx, 1, {0;1;2}, 3, 0-3, 3",
						"Xxxx-, 1, {0;1;2}, 3, 0-4, 3-4",
						"Xxx, 2, {0;1}, 2, 0-2, 2",
						"Xxxx, 2, {0;1;2}, 3, 0-3, 3",
						"Xxxx-, 2, {0;1;2}, 3, 0-4, 3-4",
						//TODO add gaps for the last 6 cases?
					})
					@DisplayName("verify greedy expansion with multiple nodes [limited, discontinuous]")
					void testGreedyCompetitionDiscontinuous(String target,
							int count, // argument for 'AtLeast' marker
							@IntArrayArg int[] hits1, // reported hits for first node
							int hit2, // reported hit for second node
							@IntervalArg Interval visited1, // all slots visited for first node
							@IntervalArg Interval visited2) { // all slots visited for second node
						/*
						 * We expect NODE_1 to visit and greedily consume all the
						 * X and x slots and then back off until the first x is
						 * reached for NODE_0.
						 */
						matcherTest()
						.root(ordered(false,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atLeastGreedy(count, DISCONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(target, visited1, EQUALS_X_IC))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.result(result(0)
								.map(NODE_0, hits1)
								.map(NODE_1, hit2))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+?>[X] in {0}")
					@CsvSource({
						"X, 1, 0, 0",
						"X-, 1, 0, 0",
						"XX, 1, 0, 0",
						"-X, 1, 1, 0-1",
						"XX-, 1, 0, 0",
						"-XX, 1, 1, 0-1",

						"XX, 2, 0-1, 0-1",
						"XX-, 2, 0-1, 0-1",
						"XXX, 2, 0-1, 0-1",
						"-XX, 2, 1-2, 0-2",
						"-XX-, 2, 1-2, 0-2",
						"-XXX, 2, 1-2, 0-2",
						"XXX-, 2, 0-1, 0-1",
						"--XX, 2, 2-3, 0-3",
						"XX--, 2, 0-1, 0-1",

						"--XXXXXXXXXX--, 10, 2-11, 0-11",
						"--XXXXXXXXXXX--, 10, 2-11, 0-11",
					})
					@DisplayName("Node with a minimum multiplicity [reluctant mode, single hit, limit]")
					void testReluctant(String target, int count,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atLeastReluctant(count, CONTINUOUS)))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+?^>[X] in {0}")
					@CsvSource({
						"X, 1, {0}, 0",
						"X-, 1, {0}, 0",
						"XX, 1, {0}, 0",
						"-X, 1, {1}, 0-1",
						"XX-, 1, {0}, 0",
						"-XX, 1, {1}, 0-1",

						"XX, 2, {0;1}, 0-1",
						"X-X, 2, {0;2}, 0-2",
						"XX-, 2, {0;1}, 0-1",
						"XXX, 2, {0;1}, 0-1",
						"-XX, 2, {1;2}, 0-2",
						"-XX-, 2, {1;2}, 0-2",
						"-XXX, 2, {1;2}, 0-2",
						"XXX-, 2, {0;1}, 0-1",
						"--XX, 2, {2;3}, 0-3",
						"XX--, 2, {0;1}, 0-1",

						"--XXXX--, 4, {2;3;4;5}, 0-5",
						"--XXXXX--, 4, {2;3;4;5}, 0-5",
						"--X-XXX--, 4, {2;4;5;6}, 0-6",
						"--X-X--XXX--, 4, {2;4;7;8}, 0-8",
					})
					@DisplayName("Node with a minimum multiplicity [reluctant mode, single hit, limit, discontinuous]")
					void testReluctantDiscontinuous(String target, int count,
							@IntArrayArg int[] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atLeastReluctant(count, DISCONTINUOUS)))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+?>[X] in {0}")
					@CsvSource({
						"X-, 2, 0-1, 0",
						"-X, 2, 0, -", // early-abort from scan
						"-X-, 2, 0-2, 1",
					})
					@DisplayName("Mismatch with a minimum multiplicity [reluctant mode]")
					void testReluctantFail(String target, int count,
							@IntervalArg Interval visited,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atLeastReluctant(count, CONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(candidates))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+?^>[X] in {0}")
					@CsvSource({
						"X-, 2, 0-1, 0",
						"-X, 2, 0, -", // early-abort from scan
						"-X-, 2, 0-2, 1",
					})
					@DisplayName("Mismatch with a minimum multiplicity [reluctant mode, discontinuous]")
					void testReluctantFailDiscontinuous(String target, int count,
							@IntervalArg Interval visited,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atLeastReluctant(count, DISCONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(candidates))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+?>[X] in {0}")
					@CsvSource({
						"XX, 1, 0, 1, 0-1",
						"XX-, 1, 0, 1, 0-2",
						"-XX, 1, 1, 2, 0-2",
						"-XX-, 1, 1, 2, 0-3",

						"XXX, 2, 0-1, 1-2, 0-2",
						"-XXX, 2, 1-2, 2-3, 0-3",
						"XXX-, 2, 0-1, 1-2, 0-3",
						"-XXX-, 2, 1-2, 2-3, 0-4",

						"-XXXX--XXXX-, 4, 1-4, 7-10, 0-11",
					})
					@DisplayName("Node with a minimum multiplicity [reluctant mode, 2 hits, limited]")
					void testReluctantMultiple(String target, int count,
							@IntervalArg Interval hits1,
							@IntervalArg Interval hits2,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atLeastReluctant(count, CONTINUOUS)))
						.target(target)
						.expectSuccess(2)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits1)
								.hits(hits2))
						.result(result(0)
								.map(NODE_0, hits1))
						.result(result(1)
								.map(NODE_0, hits2))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+?^>[X] in {0}")
					@CsvSource({
						"XX, 1, {{0}{1}}, 0-1",
						"XX-, 1, {{0}{1}}, 0-2",
						"-XX, 1, {{1}{2}}, 0-2",
						"-XX-, 1, {{1}{2}}, 0-3",

						"XXX, 2, {{0;1}{1;2}}, 0-2",
						"-XXX, 2, {{1;2}{2;3}}, 0-3",
						"X-XX, 2, {{0;2}{2;3}}, 0-3",
						"XX-X, 2, {{0;1}{1;3}}, 0-3",
						"XXX-, 2, {{0;1}{1;2}}, 0-3",
						"-XXX-, 2, {{1;2}{2;3}}, 0-4",

						"-XXXX--XXXX-, 4, {{1;2;3;4}{2;3;4;7}{3;4;7;8}{4;7;8;9}{7;8;9;10}}, 0-11",
					})
					@DisplayName("Node with a minimum multiplicity [reluctant mode, 2 hits, limited, discontinuous]")
					void testReluctantMultipleDiscontinuous(String target, int count,
							@IntMatrixArg int[][] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atLeastReluctant(count, DISCONTINUOUS)))
						.target(target)
						.expectSuccess(hits.length)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.results(hits.length, (r, i) -> r.map(NODE_0, hits[i]))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{2}+?>[x|X][x] in {0}, adjacent={1}")
					@CsvSource({
						// Expansion of size 1 - ordered
						"Xx, false, 1, 0, 1, 0, 1",
						"XXx, false, 1, 0, 2, 0, 1-2",
						"XXx-, false, 1, 0, 2, 0, 1-2",
						"-XXx, false, 1, 1, 3, 0-1, 2-3",
						"-XXx-, false, 1, 1, 3, 0-1, 2-3",
						"XxX, false, 1, 0, 1, 0, 1",
						"XxX-, false, 1, 0, 1, 0, 1",
						"-XxX, false, 1, 1, 2, 0-1, 2",
						"-XxX-, false, 1, 1, 2, 0-1, 2",
						// Expansion of size 1 - adjacent
						"Xx, true, 1, 0, 1, 0, 1",
						"XXx, true, 1, 0-1, 2, 0-1, 1-2",
						"XXXx, true, 1, 0-2, 3, 0-2, 1-3",
						"XXx-, true, 1, 0-1, 2, 0-1, 1-2",
						"-XXx, true, 1, 1-2, 3, 0-2, 2-3",
						"-XXx-, true, 1, 1-2, 3, 0-2, 2-3",
						"XxX, true, 1, 0, 1, 0, 1",
						"XxX-, true, 1, 0, 1, 0, 1",
						"-XxX, true, 1, 1, 2, 0-1, 2",
						"-XxX-, true, 1, 1, 2, 0-1, 2",
						// Expansion of size 2 - ordered
						"XXx, false, 2, 0-1, 2, 0-1, 2",
						"XXXx, false, 2, 0-1, 3, 0-1, 2-3",
						"XXxX, false, 2, 0-1, 2, 0-1, 2",
						"XXxX-, false, 2, 0-1, 2, 0-1, 2",
						"-XXxX, false, 2, 1-2, 3, 0-2, 3",
						"-XXxX-, false, 2, 1-2, 3, 0-2, 3",
						// Expansion of size 2 - adjacent
						"XXx, true, 2, 0-1, 2, 0-1, 2",
						"XXXx, true, 2, 0-2, 3, 0-2, 2-3",
						"XXXXx, true, 2, 0-3, 4, 0-3, 2-4",
						"XXxX, true, 2, 0-1, 2, 0-1, 2",
						"XXxX-, true, 2, 0-1, 2, 0-1, 2",
						"-XXxX, true, 2, 1-2, 3, 0-2, 3",
						"-XXxX-, true, 2, 1-2, 3, 0-2, 3",
						// Reluctance - adjacent
						"Xxx, true, 1, 0, 1, 0, 1",
						"Xxxx, true, 1, 0, 1, 0, 1",
						"Xxxx-, true, 1, 0, 1, 0, 1",
						"Xxx, true, 2, 0-1, 2, 0-1, 2",
						"Xxxx, true, 2, 0-1, 2, 0-1, 2",
						"Xxxx-, true, 2, 0-1, 2, 0-1, 2",
					})
					@DisplayName("verify reluctant expansion with multiple nodes")
					void testReluctantCompetition(String target,
							boolean adjacent,
							int count, // argument for 'AtLeast' marker
							@IntervalArg Interval hits1, // reported hits for first node
							int hit2, // reported hit for second node
							@IntervalArg Interval visited1,  // all slots visited for first node
							@IntervalArg Interval visited2) { // all slots visited for second node
						/*
						 * We expect NODE_1 to only proceed with consumption of slots
						 * while NODE_0 does not already match the next one.
						 */
						matcherTest()
						.root(ordered(adjacent,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atLeastReluctant(count, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(hits1))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.result(result(0)
								.map(NODE_0, hits1)
								.map(NODE_1, hit2))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{2}+?^>[x|X][x] in {0}, adjacent={1}")
					@CsvSource({
						// Expansion of size 1 - ordered
						"Xx, false, 1, {0}, 1, 0, {1}",
						"XXx, false, 1, {0}, 2, 0, {1-2}",
						"XXx-, false, 1, {0}, 2, 0, {1-2}",
						"-XXx, false, 1, {1}, 3, 0-1, {2-3}",
						"-XXx-, false, 1, {1}, 3, 0-1, {2-3}",
						"XxX, false, 1, {0}, 1, 0, {1}",
						"XxX-, false, 1, {0}, 1, 0, {1}",
						"-XxX, false, 1,{1}, 2, 0-1, {2}",
						"-XxX-, false, 1, {1}, 2, 0-1, {2}",
						// Expansion of size 1 - adjacent
						"Xx, true, 1, {0}, 1, 0, {1}",
						"XXx, true, 1, {0;1}, 2, 0-1, {1-2}",
						"X-Xx, true, 1, {0;2}, 3, 0-2, {1;3}",
						"XXXx, true, 1, {0;1;2}, 3, 0-2, {1-3}",
						"XXx-, true, 1, {0;1}, 2, 0-1, {1-2}",
						"-XXx, true, 1, {1;2}, 3, 0-2, {2-3}",
						"-XXx-, true, 1, {1;2}, 3, 0-2, {2-3}",
						"XxX, true, 1, {0}, 1, 0, {1}",
						"XxX-, true, 1, {0}, 1, 0, {1}",
						"-XxX, true, 1, {1}, 2, 0-1, {2}",
						"-XxX-, true, 1, {1}, 2, 0-1, {2}",
						"X-xx, true, 1, {0;2}, 3, 0-2, {1;3}", // force "skip" of first 'x'
						// Expansion of size 2 - ordered
						"XXx, false, 2, {0;1}, 2, 0-1, {2}",
						"XXXx, false, 2, {0;1}, 3, 0-1, {2-3}",
						"XXxX, false, 2, {0;1}, 2, 0-1, {2}",
						"XXxX-, false, 2, {0;1}, 2, 0-1, {2}",
						"-XXxX, false, 2, {1;2}, 3, 0-2, {3}",
						"-XXxX-, false, 2, {1;2}, 3, 0-2, {3}",
						// Expansion of size 2 - adjacent
						"XXx, true, 2, {0;1}, 2, 0-1, {2}",
						"XXXx, true, 2, {0;1;2}, 3, 0-2, {2-3}",
						"XXXXx, true, 2, {0;1;2;3}, 4, 0-3, {2-4}",
						"XXxX, true, 2, {0;1}, 2, 0-1, {2}",
						"XXxX-, true, 2, {0;1}, 2, 0-1, {2}",
						"-XXxX, true, 2, {1;2}, 3, 0-2, {3}",
						"-XXxX-, true, 2, {1;2}, 3, 0-2, {3}",
						// Reluctance - adjacent
						"Xxx, true, 1, {0}, 1, 0, {1}",
						"Xxxx, true, 1, {0}, 1, 0, {1}",
						"Xxxx-, true, 1, {0}, 1, 0, {1}",
						"Xxx, true, 2, {0;1}, 2, 0-1, {2}",
						"Xxxx, true, 2, {0;1}, 2, 0-1, {2}",
						"Xxxx-, true, 2, {0;1}, 2, 0-1, {2}",
					})
					@DisplayName("verify reluctant expansion with multiple nodes [discontinuous]")
					void testReluctantCompetitionDiscontinuous(String target,
							boolean adjacent,
							int count, // argument for 'AtLeast' marker
							@IntArrayArg int[] hits1, // reported hits for first node
							int hit2, // reported hit for second node
							@IntervalArg Interval visited1,  // all slots visited for first node
							@IntervalArrayArg Interval[] visited2) { // all slots visited for second node
						/*
						 * We expect NODE_1 to only proceed with consumption of slots
						 * while NODE_0 does not already match the next one.
						 */
						matcherTest()
						.root(ordered(adjacent,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atLeastReluctant(count, DISCONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(hits1))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.result(result(0)
								.map(NODE_0, hits1)
								.map(NODE_1, hit2))
						.assertResult();
					}

					//TODO no discontinuous version of this one?
					@Test
					@DisplayName("verify reluctant expansion with multiple nodes and matches")
					void testReluctantExpansion() {
						final String target = "-XXxXXx-";
						matcherTest()
						.root(ordered(true,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atLeastReluctant(2, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.target(target)
						.expectSuccess(4)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(Interval.of(0, 7))
								.hits(Interval.of(1, 6)))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(Interval.of(3, 7))
								.hits(3, 6))
						// First normal-sized match
						.result(result(0)
								.map(NODE_0, 1, 2)
								.map(NODE_1, 3))
						// Intermediate match that forces NODE2 to consume a small'x'
						.result(result(1)
								.map(NODE_0, Interval.of(2, 5))
								.map(NODE_1, 6))
						// Intermediate match that forces NODE2 to start with small'x'
						.result(result(2)
								.map(NODE_0, Interval.of(3, 5))
								.map(NODE_1, 6))
						// Last normal-sized match
						.result(result(3)
								.map(NODE_0, 4, 5)
								.map(NODE_1, 6))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+!>[X] in {0}")
					@CsvSource({
						"X, 1, 0, 0",
						"X-, 1, 0, 0-1",
						"-X, 1, 1, 0-1",
						"XX-, 1, 0-1, 0-2",
						"-XX, 1, 1-2, 0-2",

						"XX, 2, 0-1, 0-1",
						"XX-, 2, 0-1, 0-2",
						"XXX, 2, 0-2, 0-2",
						"-XX, 2, 1-2, 0-2",
						"-XX-, 2, 1-2, 0-3",
						"-XXX, 2, 1-3, 0-3",
						"XXX-, 2, 0-2, 0-3",
						"--XX, 2, 2-3, 0-3",
						"XX--, 2, 0-1, 0-2",

						"--XXXXXXXXXX--, 10, 2-11, 0-12",
						"--XXXXXXXXXXXX--, 10, 2-13, 0-14",
					})
					@DisplayName("Node with a minimum multiplicity [possessive mode, single hit, limit]")
					void testPossessive(String target, int count,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atLeastPossessive(count, CONTINUOUS)))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+!^>[X] in {0}")
					@CsvSource({
						"X, 1, 0, 0",
						"X-, 1, 0, 0-1",
						"-X, 1, 1, 0-1",
						"XX-, 1, 0-1, 0-2",
						"-XX, 1, 1-2, 0-2",

						"XX, 2, 0-1, 0-1",
						"XX-, 2, 0-1, 0-2",
						"XXX, 2, 0-2, 0-2",
						"-XX, 2, 1-2, 0-2",
						"-XX-, 2, 1-2, 0-3",
						"-XXX, 2, 1-3, 0-3",
						"XXX-, 2, 0-2, 0-3",
						"--XX, 2, 2-3, 0-3",
						"XX--, 2, 0-1, 0-3",

						"--XXXXXXXXXX--, 10, 2-11, 0-13",
						"--XXXXXXXXXXXX--, 10, 2-13, 0-15",
					})
					@DisplayName("Node with a minimum multiplicity [possessive mode, single hit, limit, discontinuous]")
					void testPossessiveDiscontinuous(String target, int count,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atLeastPossessive(count, DISCONTINUOUS)))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+!>[X] in {0}")
					@CsvSource({
						"X-, 2, 0-1, 0",
						"-X, 2, 0, -", // early-abort from scan
						"-X-, 2, 0-2, 1",
					})
					@DisplayName("Mismatch with a minimum multiplicity [possessive mode]")
					void testPossessiveFail(String target, int count,
							@IntervalArg Interval visited,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atLeastPossessive(count, CONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(candidates))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+!^>[X] in {0}")
					@CsvSource({
						"X-, 2, 0-1, 0",
						"-X, 2, 0, -", // early-abort from scan
						"-X-, 2, 0-2, 1",
					})
					@DisplayName("Mismatch with a minimum multiplicity [possessive mode, discontinuous]")
					void testPossessiveFailDiscontinuous(String target, int count,
							@IntervalArg Interval visited,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atLeastPossessive(count, DISCONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(candidates))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+!>[X|x][x] in {0}")
					@CsvSource({
						"Xx, 1, 0-1, -, 0-1",
						"XXx, 1, 0-2, -, 0-2",
						"XXX-, 1, 0-3, 3, 0-2",
						"XXx-, 1, 0-3, 3, 0-2",
					})
					@DisplayName("Mismatch due to possessive consumption [ordered]")
					void testPossessiveFail2(String target, int count,
							@IntervalArg Interval visited1,
							@IntervalArg Interval visited2,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(ordered(false,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atLeastPossessive(count, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.target(target)
						.expectFail()
						// Underlying cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(candidates))
						// Underlying cache of second node
						.cache(cache(CACHE_1, false)
								.set(visited2)
								.window(target))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}+!^>[X|x][x] in {0}")
					@CsvSource({
						"Xx, 1, 0-1, -",
						"XXx, 1, 0-2, -",
						"X-Xx, 1, 0-3, -",
						"XX-x, 1, 0-3, -",
						"XXX-, 1, 0-3, 3",
						"X-XX-, 1, 0-4, 4",
						"XX-X-, 1, 0-4, 4",
						"XXx-, 1, 0-3, 3",
					})
					@DisplayName("Mismatch due to possessive consumption [ordered, discontinuous]")
					void testPossessiveFail2Discontinuous(String target, int count,
							@IntervalArg Interval visited1,
							@IntervalArg Interval visited2) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(ordered(false,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atLeastPossessive(count, DISCONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.target(target)
						.expectFail()
						// Underlying cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(target, visited1, EQUALS_X_IC))
						// Underlying cache of second node
						.cache(cache(CACHE_1, false)
								.set(visited2)
								.window(target))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: ADJACENT <{1}+!>[X|x][x] in {0}")
					@CsvSource({
						"Xx, 1, 0-1, -, 0-1",
						"XXx, 1, 0-2, -, 0-2",
						"XXX-, 1, 0-3, 3, 0-2",
						"XXx-, 1, 0-3, 3, 0-2",
						"XXx-x, 1, 0-3, 3, 0-2",
					})
					@DisplayName("Mismatch due to possessive consumption [adjacent]")
					void testPossessiveFail3(String target, int count,
							@IntervalArg Interval visited1,
							@IntervalArg Interval visited2,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(ordered(true,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atLeastPossessive(count, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.target(target)
						.expectFail()
						// Underlying cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(candidates))
						// Underlying cache of second node
						.cache(cache(CACHE_1, false)
								.set(visited2)
								.window(target))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: ADJACENT <{1}+!>[X|x][x] in {0}")
					@CsvSource({
						"Xx, 1, 0-1, -",
						"XXx, 1, 0-2, -",
						"XXX-, 1, 0-3, 3",
						"XXx-, 1, 0-3, 3",
						"XXx-x, 1, 0-4, -",
					})
					@DisplayName("Mismatch due to possessive consumption [adjacent, discontinuous]")
					void testPossessiveFail3Discontinuous(String target, int count,
							@IntervalArg Interval visited1,
							@IntervalArg Interval visited2) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(ordered(true,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atLeastPossessive(count, DISCONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.target(target)
						.expectFail()
						// Underlying cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(target, visited1, EQUALS_X_IC))
						// Underlying cache of second node
						.cache(cache(CACHE_1, false)
								.set(visited2)
								.window(target))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{3}+!>[x|X][{1}] in {0}, adjacent={2}")
					@CsvSource({
						// Expansion of size 1 - ordered
						"XY, Y, false, 1, {0}, {1}, {0-1}, {1}",
						"XXY, Y, false, 1, {0-1;1}, {2;2}, {0-2}, {2}",
						// Expansion of size 2 - ordered
						"XXX-X, X, false, 2, {0-2;1-2}, {4;4}, {0-3}, {3-4}",
						"XX-XX, X, false, 2, {0-1;0-1}, {3;4}, {0-2}, {2-4}",
						"XXx-x, x, false, 2, {0-2;1-2}, {4;4}, {0-3}, {3-4}",
						//TODO adjacent cases
						// Expansion of size 2 - adjacent
						"XXY, Y, true, 2, {0-1}, {2}, {0-2}, {2}",
					})
					@DisplayName("verify possessive expansion with multiple nodes")
					void testPossessiveCompetition(String target,
							char c2, // search symbol for second node
							boolean adjacent,
							int count, // argument for 'AtLeast' marker
							@IntervalArrayArg Interval[] hits1, // reported hits for first node
							@IntervalArrayArg Interval[] hit2, // reported hits for second node
							@IntervalArrayArg Interval[] visited1,  // all slots visited for first node
							@IntervalArrayArg Interval[] visited2) { // all slots visited for second node

						// Sanity check since we expect symmetric results here
						assertThat(hits1).hasSameSizeAs(hit2);

						/*
						 * We expect NODE_1 to aggressively consume slots with
						 * no regards for NODE_0, so that in contrast to reluctant mode
						 * we will miss some multi-match situations.
						 */
						matcherTest()
						.root(ordered(adjacent,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atLeastPossessive(count, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp(c2)))))
						.target(target)
						.expectSuccess(hits1.length)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(hits1))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.results(hits1.length, (r,i) -> r
								.map(NODE_0, hits1[i])
								.map(NODE_1, hit2[i]))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{3}+!^>[x|X][{1}] in {0}, adjacent={2}")
					@CsvSource({
						// Expansion of size 1 - ordered
						"XY, Y, false, 1, {{0}}, {1}, {0-1}, 1",
						"XXY, Y, false, 1, {{0;1}{1}}, {2;2}, {0-2}, 2",
						// Expansion of size 2 - ordered
						"XXX-XY, Y, false, 2, {{0;1;2;4}{1;2;4}{2;4}}, {5;5;5}, {0-5}, 5",
						"XX-XxY, Y, false, 2, {{0;1;3;4}{1;3;4}{3;4}}, {5;5;5}, {0-5}, 5",
						"XXx-xY, Y, false, 2, {{0;1;2;4}{1;2;4}{2;4}}, {5;5;5}, {0-5}, 5",
						//TODO adjacent cases
						// Expansion of size 2 - adjacent
						"XXY, Y, true, 2, {{0;1}}, {2}, {0-2}, 2",
					})
					@DisplayName("verify possessive expansion with multiple nodes [discontinuous]")
					void testPossessiveCompetitionDiscontinuous(String target,
							char c2, // search symbol for second node
							boolean adjacent,
							int count, // argument for 'AtLeast' marker
							@IntMatrixArg int[][] hits1, // reported hits for first node
							@IntervalArrayArg Interval[] hit2, // reported hits for second node
							@IntervalArrayArg Interval[] visited1,  // all slots visited for first node
							@IntervalArg Interval visited2) { // all slots visited for second node

						// Sanity check since we expect symmetric results here
						assertThat((Object[])hits1).hasSameSizeAs(hit2);

						/*
						 * We expect NODE_1 to aggressively consume slots with
						 * no regards for NODE_0, so that in contrast to reluctant mode
						 * we will miss some multi-match situations.
						 */
						matcherTest()
						.root(ordered(adjacent,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atLeastPossessive(count, DISCONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp(c2)))))
						.target(target)
						.expectSuccess(hits1.length)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(hits1))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.results(hits1.length, (r,i) -> r
								.map(NODE_0, hits1[i])
								.map(NODE_1, hit2[i]))
						.assertResult();
					}

				}

				@Nested
				class AtMost {

					@ParameterizedTest(name="{index}: <{1}->[X] in {0}")
					@CsvSource({
						"X, 1, 0, 0",
						"X-, 1, 0, 0",
						"-X, 1, 1, 0-1",
						"XX-, 1, 0, 0",
						"-XX, 1, 1, 0-1",

						"XX, 2, 0-1, 0-1",
						"X-, 2, 0, 0-1",
						"XX-, 2, 0-1, 0-1",
						"XXX, 2, 0-1, 0-1",
						"-X-, 2, 1, 0-2",
						"-XX, 2, 1-2, 0-2",
						"-XX-, 2, 1-2, 0-2",
						"-XXX, 2, 1-2, 0-2",
						"XXX-, 2, 0-1, 0-1",
						"--XX, 2, 2-3, 0-3",
						"XX--, 2, 0-1, 0-1",

						"--XXXXXXXXXX--, 10, 2-11, 0-11",
						"--XXXXXXXXXXX--, 10, 2-11, 0-11",
					})
					@DisplayName("Node with a maximum multiplicity [greedy mode, single hit, limit]")
					void testGreedy(String target, int count,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atMostGreedy(count, CONTINUOUS)))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-^>[X] in {0}")
					@CsvSource({
						"X, 1, 0, 0",
						"X-, 1, 0, 0",
						"-X, 1, 1, 0-1",
						"XX-, 1, 0, 0",
						"-XX, 1, 1, 0-1",

						"XX, 2, 0-1, 0-1",
						"X-, 2, 0, 0-1",
						"XX-, 2, 0-1, 0-1",
						"XXX, 2, 0-1, 0-1",
						"-X-, 2, 1, 0-2",
						"-XX, 2, 1-2, 0-2",
						"-XX-, 2, 1-2, 0-2",
						"-XXX, 2, 1-2, 0-2",
						"XXX-, 2, 0-1, 0-1",
						"--XX, 2, 2-3, 0-3",
						"XX--, 2, 0-1, 0-1",

						"--XXXXXXXXXX--, 10, 2-11, 0-11",
						"--XXXXXXXXXXX--, 10, 2-11, 0-11",
						// complete
					})
					@DisplayName("Node with a maximum multiplicity [greedy mode, single hit, limit, discontinuous]")
					void testGreedyDiscontinuous(String target, int count,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atMostGreedy(count, DISCONTINUOUS)))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}->[X] in {0}")
					@CsvSource({
						"-, 1, 0, -",
						"Y, 1, 0, -",
						"-Y, 2, 0-1, -",
						"-Y-, 2, 0-2, -",
					})
					@DisplayName("Mismatch with a maximum multiplicity [greedy mode]")
					void testGreedyFail(String target, int count,
							@IntervalArg Interval visited,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atMostGreedy(count, CONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(candidates))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-^>[X] in {0}")
					@CsvSource({
						"-, 1, 0, -",
						"Y, 1, 0, -",
						"-Y, 2, 0-1, -",
						"-Y-, 2, 0-2, -",
					})
					@DisplayName("Mismatch with a maximum multiplicity [greedy mode, discontinuous]")
					void testGreedyFailDiscontinuous(String target, int count,
							@IntervalArg Interval visited,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atMostGreedy(count, DISCONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(candidates))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}->[X] in {0}")
					@CsvSource({
						"XX, 1, {0;1}, 0-1",
						"XX-, 1, {0;1}, 0-2",
						"-XX, 1, {1;2}, 0-2",
						"-XX-, 1, {1;2}, 0-3",

						"XXX, 2, {0-1;1-2;2}, 0-2",
						"-XXX, 2, {1-2;2-3;3}, 0-3",
						"XXX-, 2, {0-1;1-2;2}, 0-3",
						"-XXX-, 2, {1-2;2-3;3}, 0-4",

						"-XXXX--XXXX-, 4, {1-4;2-4;3-4;4;7-10;8-10;9-10;10}, 0-11",
					})
					@DisplayName("Node with a maximum multiplicity [greedy mode]")
					void testGreedyMultiple(String target, int count,
							@IntervalArrayArg Interval[] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atMostGreedy(count, CONTINUOUS)))
						.target(target)
						.expectSuccess(hits.length)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.results(NODE_0, hits)
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-^>[X] in {0}")
					@CsvSource({
						"XX, 1, {{0}{1}}, 0-1",
						"XX-, 1, {{0}{1}}, 0-2",
						"-XX, 1, {{1}{2}}, 0-2",
						"X-X, 1, {{0}{2}}, 0-2",
						"-XX-, 1, {{1}{2}}, 0-3",
						"X--X, 1, {{0}{3}}, 0-3",

						"XXX, 2, {{0;1}{1;2}{2}}, 0-2",
						"X-X, 2, {{0;2}{2}}, 0-2",
						"-XXX, 2, {{1;2}{2;3}{3}}, 0-3",
						"X-XX, 2, {{0;2}{2;3}{3}}, 0-3",
						"XX-X, 2, {{0;1}{1;3}{3}}, 0-3",
						"XXX-, 2, {{0;1}{1;2}{2}}, 0-3",
						"-XXX-, 2, {{1;2}{2;3}{3}}, 0-4",

						"-XXX--XXX-, 3, {{1;2;3}{2;3;6}{3;6;7}{6;7;8}{7;8}{8}}, 0-9",
					})
					@DisplayName("Node with a maximum multiplicity [greedy mode, discontinuous]")
					void testGreedyMultipleDiscontinuous(String target, int count,
							@IntMatrixArg int[][] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atMostGreedy(count, DISCONTINUOUS)))
						.target(target)
						.expectSuccess(hits.length)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.results(hits.length, (r, i) -> r.map(NODE_0, hits[i]))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}->[x|X][x] in {0}")
					@CsvSource({
						// Expansion of size 2
						"Xx, 2, 0, 1, 0-1, 0-1, 1",
						"XXx, 2, 0-1, 2, 0-1, 0-1, 2",
						"XXxx, 2, 0-1, 2, 0-1, 0-1, 2",
						"XXx-, 2, 0-1, 2, 0-1, 0-1, 2",
						"-XXx, 2, 1-2, 3, 1-2, 0-2, 3",
						"-XXx-, 2, 1-2, 3, 1-2, 0-2, 3",
						"XxX, 2, 0, 1, 0-1, 0-1, 1-2",
						"XxX-, 2, 0, 1, 0-1, 0-1, 1-3",
						"-XxX, 2, 1, 2, 1-2, 0-2, 2-3",
						"-XxX-, 2, 1, 2, 1-2, 0-2, 2-4",
						// Expansion of size 3
						"XXx, 3, 0-1, 2, 0-2, 0-2, 2",
						"XXXx, 3, 0-2, 3, 0-2, 0-2, 3",
						"XXxX, 3, 0-1, 2, 0-2, 0-2, 2-3",
						"XXxX-, 3, 0-1, 2, 0-2, 0-2, 2-4",
						"-XXxX, 3, 1-2, 3, 1-3, 0-3, 3-4",
						"-XXxX-, 3, 1-2, 3, 1-3, 0-3, 3-5",
						// Consume first target for second node
						"XxXxX, 10, 0-2, 3, 0-4, 0-4, 3-4",
						"XxXxX-, 10, 0-2, 3, 0-4, 0-5, 3-5",
						"-XxXxX, 10, 1-3, 4, 1-5, 0-5, 4-5",
						"-XxXxX-, 10, 1-3, 4, 1-5, 0-6, 4-6",
						// Greediness
						"Xxx, 2, 0-1, 2, 0-1, 0-1, 2",
						"Xxxx, 2, 0-1, 2, 0-1, 0-1, 2",
						"Xxxx-, 2, 0-1, 2, 0-1, 0-1, 2",
						"Xxx, 3, 0-1, 2, 0-2, 0-2, 2",
						"Xxxx, 3, 0-2, 3, 0-2, 0-2, 3",
						"Xxxx-, 3, 0-2, 3, 0-2, 0-2, 3",
					})
					@DisplayName("verify greedy expansion with multiple nodes [limited]")
					void testGreedyCompetition(String target,
							int count, // argument for 'AtLeast' marker
							@IntervalArg Interval hits1, // reported hits for first node
							int hit2, // reported hit for second node
							@IntervalArg Interval candidates, // cached hits for first node
							@IntervalArg Interval visited1, // all slots visited for first node
							@IntervalArg Interval visited2) { // all slots visited for second node
						/*
						 * We expect NODE_1 to visit and greedily consume all the
						 * X and x slots and then back off until the first x is
						 * reached for NODE_0.
						 */
						matcherTest()
						.root(ordered(false,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atMostGreedy(count, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(candidates))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.result(result(0)
								.map(NODE_0, hits1)
								.map(NODE_1, hit2))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-^>[x|X][x] in {0}")
					@CsvSource({
						// Expansion of size 2
						"Xx, 2, 0, 1, 0-1, 0-1, 1",
						"XXx, 2, 0-1, 2, 0-1, 0-1, 2",
						"XXxx, 2, 0-1, 2, 0-1, 0-1, 2",
						"XXx-, 2, 0-1, 2, 0-1, 0-1, 2",
						"-XXx, 2, 1-2, 3, 1-2, 0-2, 3",
						"-XXx-, 2, 1-2, 3, 1-2, 0-2, 3",
						"XxX, 2, 0, 1, 0-1, 0-1, 1-2",
						"XxX-, 2, 0, 1, 0-1, 0-1, 1-3",
						"-XxX, 2, 1, 2, 1-2, 0-2, 2-3",
						"-XxX-, 2, 1, 2, 1-2, 0-2, 2-4",
						// Expansion of size 3
						"XXx, 3, 0-1, 2, 0-2, 0-2, 2",
						"XXXx, 3, 0-2, 3, 0-2, 0-2, 3",
						"XXxX, 3, 0-1, 2, 0-2, 0-2, 2-3",
						"XXxX-, 3, 0-1, 2, 0-2, 0-2, 2-4",
						"-XXxX, 3, 1-2, 3, 1-3, 0-3, 3-4",
						"-XXxX-, 3, 1-2, 3, 1-3, 0-3, 3-5",
						// Consume first target for second node
						"XxXxX, 10, 0-2, 3, 0-4, 0-4, 3-4",
						"XxXxX-, 10, 0-2, 3, 0-4, 0-5, 3-5",
						"-XxXxX, 10, 1-3, 4, 1-5, 0-5, 4-5",
						"-XxXxX-, 10, 1-3, 4, 1-5, 0-6, 4-6",
						// Greediness
						"Xxx, 2, 0-1, 2, 0-1, 0-1, 2",
						"Xxxx, 2, 0-1, 2, 0-1, 0-1, 2",
						"Xxxx-, 2, 0-1, 2, 0-1, 0-1, 2",
						"Xxx, 3, 0-1, 2, 0-2, 0-2, 2",
						"Xxxx, 3, 0-2, 3, 0-2, 0-2, 3",
						"Xxxx-, 3, 0-2, 3, 0-2, 0-2, 3",
					})
					@DisplayName("verify greedy expansion with multiple nodes [limited, discontinuous]")
					void testGreedyCompetitionDiscontinuous(String target,
							int count, // argument for 'AtLeast' marker
							@IntervalArg Interval hits1, // reported hits for first node
							int hit2, // reported hit for second node
							@IntervalArg Interval candidates, // cached hits for first node
							@IntervalArg Interval visited1, // all slots visited for first node
							@IntervalArg Interval visited2) { // all slots visited for second node
						/*
						 * We expect NODE_1 to visit and greedily consume all the
						 * X and x slots and then back off until the first x is
						 * reached for NODE_0.
						 */
						matcherTest()
						.root(ordered(false,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atMostGreedy(count, DISCONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(candidates))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.result(result(0)
								.map(NODE_0, hits1)
								.map(NODE_1, hit2))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-?>[X] in {0}")
					@CsvSource({
						"X, 1, 0, 0",
						"X-, 1, 0, 0",
						"XX, 1, 0, 0",
						"-X, 1, 1, 0-1",
						"XX-, 1, 0, 0",
						"-XX, 1, 1, 0-1",

						"XX, 2, 0, 0",
						"X-, 2, 0, 0",
						"XX-, 2, 0, 0",
						"XXX, 2, 0, 0",
						"-XX, 2, 1, 0-1",
						"-X-, 2, 1, 0-1",
						"-XX-, 2, 1, 0-1",
						"-XXX, 2, 1, 0-1",
						"XXX-, 2, 0, 0",
						"--XX, 2, 2, 0-2",
						"XX--, 2, 0, 0",

						"--XXXXXXXXXX--, 10, 2, 0-2",
					})
					@DisplayName("Node with a maximum multiplicity [reluctant mode, single hit, limit]")
					void testReluctant(String target, int count,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atMostReluctant(count, CONTINUOUS)))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-?^>[X] in {0}")
					@CsvSource({
						"X, 1, 0, 0",
						"X-, 1, 0, 0",
						"XX, 1, 0, 0",
						"-X, 1, 1, 0-1",
						"XX-, 1, 0, 0",
						"-XX, 1, 1, 0-1",

						"XX, 2, 0, 0",
						"X-, 2, 0, 0",
						"XX-, 2, 0, 0",
						"XXX, 2, 0, 0",
						"-XX, 2, 1, 0-1",
						"-X-, 2, 1, 0-1",
						"-XX-, 2, 1, 0-1",
						"-XXX, 2, 1, 0-1",
						"XXX-, 2, 0, 0",
						"--XX, 2, 2, 0-2",
						"XX--, 2, 0, 0",

						"--XXXXXXXXXX--, 10, 2, 0-2",
					})
					@DisplayName("Node with a maximum multiplicity [reluctant mode, single hit, limit, discontinuous]")
					void testReluctantDiscontinuous(String target, int count,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atMostReluctant(count, DISCONTINUOUS)))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-?>[X] in {0}")
					@CsvSource({
						"-, 1, 0, -",
						"Y, 1, 0, -",
						"-Y, 2, 0-1, -",
						"-Y-, 2, 0-2, -",
					})
					@DisplayName("Mismatch with a maximum multiplicity [reluctant mode]")
					void testReluctantFail(String target, int count,
							@IntervalArg Interval visited,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atMostReluctant(count, CONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(candidates))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-?^>[X] in {0}")
					@CsvSource({
						"-, 1, 0, -",
						"Y, 1, 0, -",
						"-Y, 2, 0-1, -",
						"-Y-, 2, 0-2, -",
					})
					@DisplayName("Mismatch with a maximum multiplicity [reluctant mode, discontinuous]")
					void testReluctantFailDiscontinuous(String target, int count,
							@IntervalArg Interval visited,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atMostReluctant(count, DISCONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(candidates))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-?>[X] in {0}")
					@CsvSource({
						"XX, 1, {0;1}, 0-1",
						"XX-, 1, {0;1}, 0-2",
						"-XX, 1, {1;2}, 0-2",
						"-XX-, 1, {1;2}, 0-3",

						"XXX, 2, {0;1;2}, 0-2",
						"-XXX, 2, {1;2;3}, 0-3",
						"XXX-, 2, {0;1;2}, 0-3",
						"-XXX-, 2, {1;2;3}, 0-4",

						"-XXXX--XXXX-, 4, {1;2;3;4;7;8;9;10}, 0-11",
					})
					@DisplayName("Node with a maximum multiplicity [reluctant mode, multiple hits]")
					void testReluctantMultiple(String target, int count,
							@IntervalArrayArg Interval[] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atMostReluctant(count, CONTINUOUS)))
						.target(target)
						.expectSuccess(hits.length)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.results(NODE_0, hits)
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-?^>[X] in {0}")
					@CsvSource({
						"XX, 1, {0;1}, 0-1",
						"XX-, 1, {0;1}, 0-2",
						"-XX, 1, {1;2}, 0-2",
						"-XX-, 1, {1;2}, 0-3",

						"XXX, 2, {0;1;2}, 0-2",
						"-XXX, 2, {1;2;3}, 0-3",
						"XXX-, 2, {0;1;2}, 0-3",
						"-XXX-, 2, {1;2;3}, 0-4",

						"-XXXX--XXXX-, 4, {1;2;3;4;7;8;9;10}, 0-11",
					})
					@DisplayName("Node with a maximum multiplicity [reluctant mode, multiple hits, discontinuous]")
					void testReluctantMultipleDiscontinuous(String target, int count,
							@IntervalArrayArg Interval[] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atMostReluctant(count, DISCONTINUOUS)))
						.target(target)
						.expectSuccess(hits.length)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.results(NODE_0, hits)
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{2}-?>[x|X][x] in {0}, adjacent={1}")
					@CsvSource({
						// Expansion of size 1 - ordered
						"Xx, false, 1, 0, 1, 0, 1, 0",
						"XXx, false, 1, 0, 2, 0, 1-2, 0",
						"XXx-, false, 1, 0, 2, 0, 1-2, 0",
						"-XXx, false, 1, 1, 3, 0-1, 2-3, 1",
						"-XXx-, false, 1, 1, 3, 0-1, 2-3, 1",
						"XxX, false, 1, 0, 1, 0, 1, 0",
						"XxX-, false, 1, 0, 1, 0, 1, 0",
						"-XxX, false, 1, 1, 2, 0-1, 2, 1",
						"-XxX-, false, 1, 1, 2, 0-1, 2, 1",
						// Expansion of size 1 - adjacent
						"Xx, true, 1, 0, 1, 0, 1, 0",
						"XXx, true, 1, 1, 2, 0-1, 1-2, 0-1",
						"XXXx, true, 1, 2, 3, 0-2, 1-3, 0-2",
						"XXx-, true, 1, 1, 2, 0-1, 1-2, 0-1",
						"-XXx, true, 1, 2, 3, 0-2, 2-3, 1-2",
						"-XXx-, true, 1, 2, 3, 0-2, 2-3, 1-2",
						"XxX, true, 1, 0, 1, 0, 1, 0",
						"XxX-, true, 1, 0, 1, 0, 1, 0",
						"-XxX, true, 1, 1, 2, 0-1, 2, 1",
						"-XxX-, true, 1, 1, 2, 0-1, 2, 1",
						// Expansion of size 2 - ordered
						"XXx, false, 2, 0, 2, 0, 1-2, 0",
						"XXXx, false, 2, 0, 3, 0, 1-3, 0",
						"XXxX, false, 2, 0, 2, 0, 1-2, 0",
						"XXxX-, false, 2, 0, 2, 0, 1-2, 0",
						"-XXxX, false, 2, 1, 3, 0-1, 2-3, 1",
						"-XXxX-, false, 2, 1, 3, 0-1, 2-3, 1",
						// Expansion of size 2 - adjacent
						"XXx, true, 2, 0-1, 2, 0-1, 1-2, 0-1",
						"XXXx, true, 2, 1-2, 3, 0-2, 1-3, 0-2",
						"XXXXx, true, 2, 2-3, 4, 0-3, 1-4, 0-3",
						"XXxX, true, 2, 0-1, 2, 0-1, 1-2, 0-1",
						"XXxX-, true, 2, 0-1, 2, 0-1, 1-2, 0-1",
						"-XXxX, true, 2, 1-2, 3, 0-2, 2-3, 1-2",
						"-XXxX-, true, 2, 1-2, 3, 0-2, 2-3, 1-2",
						// Reluctance - adjacent
						"Xxx, true, 1, 0, 1, 0, 1, 0",
						"Xxxx, true, 1, 0, 1, 0, 1, 0",
						"Xxxx-, true, 1, 0, 1, 0, 1, 0",
						"Xxx, true, 2, 0, 1, 0, 1, 0",
						"Xxxx, true, 2, 0, 1, 0, 1, 0",
						"Xxxx-, true, 2, 0, 1, 0, 1, 0",
					})
					@DisplayName("verify reluctant expansion with multiple nodes [limited]")
					void testReluctantCompetition(String target,
							boolean adjacent,
							int count, // argument for 'AtLeast' marker
							@IntervalArg Interval hits1, // reported hits for first node
							int hit2, // reported hit for second node
							@IntervalArg Interval visited1,  // all slots visited for first node
							@IntervalArg Interval visited2, // all slots visited for second node
							@IntervalArg Interval candidates1) { // slots marked as true for first node
						/*
						 * We expect NODE_1 to only proceed with consumption of slots
						 * while NODE_0 does not already match the next one.
						 */
						matcherTest()
						.root(ordered(adjacent,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atMostReluctant(count, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(candidates1))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.result(result(0)
								.map(NODE_0, hits1)
								.map(NODE_1, hit2))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{2}-?^>[x|X][x] in {0}, adjacent={1}")
					@CsvSource({
						// Expansion of size 1 - ordered
						"Xx, false, 1, 0, 1, 0, 1, 0",
						"XXx, false, 1, 0, 2, 0, 1-2, 0",
						"XXx-, false, 1, 0, 2, 0, 1-2, 0",
						"-XXx, false, 1, 1, 3, 0-1, 2-3, 1",
						"-XXx-, false, 1, 1, 3, 0-1, 2-3, 1",
						"XxX, false, 1, 0, 1, 0, 1, 0",
						"XxX-, false, 1, 0, 1, 0, 1, 0",
						"-XxX, false, 1, 1, 2, 0-1, 2, 1",
						"-XxX-, false, 1, 1, 2, 0-1, 2, 1",
						// Expansion of size 1 - adjacent
						"Xx, true, 1, 0, 1, 0, 1, 0",
						"XXx, true, 1, 1, 2, 0-1, 1-2, 0-1",
						"XXXx, true, 1, 2, 3, 0-2, 1-3, 0-2",
						"XXx-, true, 1, 1, 2, 0-1, 1-2, 0-1",
						"-XXx, true, 1, 2, 3, 0-2, 2-3, 1-2",
						"-XXx-, true, 1, 2, 3, 0-2, 2-3, 1-2",
						"XxX, true, 1, 0, 1, 0, 1, 0",
						"XxX-, true, 1, 0, 1, 0, 1, 0",
						"-XxX, true, 1, 1, 2, 0-1, 2, 1",
						"-XxX-, true, 1, 1, 2, 0-1, 2, 1",
						// Expansion of size 2 - ordered
						"XXx, false, 2, 0, 2, 0, 1-2, 0",
						"XXXx, false, 2, 0, 3, 0, 1-3, 0",
						"XXxX, false, 2, 0, 2, 0, 1-2, 0",
						"XXxX-, false, 2, 0, 2, 0, 1-2, 0",
						"-XXxX, false, 2, 1, 3, 0-1, 2-3, 1",
						"-XXxX-, false, 2, 1, 3, 0-1, 2-3, 1",
						// Expansion of size 2 - adjacent
						"XXx, true, 2, 0-1, 2, 0-1, 1-2, 0-1",
						"XXXx, true, 2, 1-2, 3, 0-2, 1-3, 0-2",
						"XXXXx, true, 2, 2-3, 4, 0-3, 1-4, 0-3",
						"XXxX, true, 2, 0-1, 2, 0-1, 1-2, 0-1",
						"XXxX-, true, 2, 0-1, 2, 0-1, 1-2, 0-1",
						"-XXxX, true, 2, 1-2, 3, 0-2, 2-3, 1-2",
						"-XXxX-, true, 2, 1-2, 3, 0-2, 2-3, 1-2",
						// Reluctance - adjacent
						"Xxx, true, 1, 0, 1, 0, 1, 0",
						"Xxxx, true, 1, 0, 1, 0, 1, 0",
						"Xxxx-, true, 1, 0, 1, 0, 1, 0",
						"Xxx, true, 2, 0, 1, 0, 1, 0",
						"Xxxx, true, 2, 0, 1, 0, 1, 0",
						"Xxxx-, true, 2, 0, 1, 0, 1, 0",
					})
					@DisplayName("verify reluctant expansion with multiple nodes [limited, discontinuous]")
					void testReluctantCompetitionDiscontinuous(String target,
							boolean adjacent,
							int count, // argument for 'AtLeast' marker
							@IntervalArg Interval hits1, // reported hits for first node
							int hit2, // reported hit for second node
							@IntervalArg Interval visited1,  // all slots visited for first node
							@IntervalArg Interval visited2, // all slots visited for second node
							@IntervalArg Interval candidates1) { // slots marked as true for first node
						/*
						 * We expect NODE_1 to only proceed with consumption of slots
						 * while NODE_0 does not already match the next one.
						 */
						matcherTest()
						.root(ordered(adjacent,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atMostReluctant(count, DISCONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(candidates1))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.result(result(0)
								.map(NODE_0, hits1)
								.map(NODE_1, hit2))
						.assertResult();
					}

					@Test
					@DisplayName("verify reluctant expansion with multiple nodes and matches")
					void testReluctantExpansion() {
						final String target = "-XXxXXx-";
						matcherTest()
						.root(ordered(true,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atMostReluctant(2, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.target(target)
						.expectSuccess(4)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(Interval.of(0, 7))
								.hits(Interval.of(1, 6)))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(Interval.of(2, 7))
								.hits(3, 6))
						// First normal-sized match
						.result(result(0)
								.map(NODE_0, 1, 2)
								.map(NODE_1, 3))
						// Intermediate match that only allows first node to consume 1 slot
						.result(result(1)
								.map(NODE_0, 2)
								.map(NODE_1, 3))
						// Last normal-sized match
						.result(result(3)
								.map(NODE_0, 4, 5)
								.map(NODE_1, 6))
						// Final minimum-sized match
						.result(result(4)
								.map(NODE_0, 5)
								.map(NODE_1, 6))
						.assertResult();
					}

					@Test
					@DisplayName("verify reluctant expansion with multiple nodes and matches [discontinuous]")
					void testReluctantExpansionDiscontinuous() {
						final String target = "-XXxXXx-";
						matcherTest()
						.root(ordered(true,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atMostReluctant(2, DISCONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.target(target)
						.expectSuccess(4)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(Interval.of(0, 7))
								.hits(Interval.of(1, 6)))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(Interval.of(2, 7))
								.hits(3, 6))
						// First normal-sized match
						.result(result(0)
								.map(NODE_0, 1, 2)
								.map(NODE_1, 3))
						// Intermediate match that only allows first node to consume 1 slot
						.result(result(1)
								.map(NODE_0, 2)
								.map(NODE_1, 3))
						// Last normal-sized match
						.result(result(3)
								.map(NODE_0, 4, 5)
								.map(NODE_1, 6))
						// Final minimum-sized match
						.result(result(4)
								.map(NODE_0, 5)
								.map(NODE_1, 6))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-!>[X] in {0}")
					@CsvSource({
						"X, 1, 0, 0",
						"X-, 1, 0, 0",
						"-X, 1, 1, 0-1",
						"XX-, 1, 0, 0",
						"-XX, 1, 1, 0-1",
						"-X-, 1, 1, 0-1",

						"XX, 2, 0-1, 0-1",
						"XX-, 2, 0-1, 0-1",
						"XXX, 2, 0-1, 0-1",
						"-XX, 2, 1-2, 0-2",
						"-XX-, 2, 1-2, 0-2",
						"-XXX, 2, 1-2, 0-2",
						"XXX-, 2, 0-1, 0-1",
						"--XX, 2, 2-3, 0-3",
						"XX--, 2, 0-1, 0-1",

						"XXX, 3, 0-2, 0-2",
						"-XXX, 3, 1-3, 0-3",
						"XXX-, 3, 0-2, 0-2",

						"--XXXXXXXXXX--, 10, 2-11, 0-11",
						"--XXXXXXXXXXXX--, 10, 2-11, 0-11",
					})
					@DisplayName("Node with a maximum multiplicity [possessive mode, single hit, limit]")
					void testPossessive(String target, int count,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atMostPossessive(count, CONTINUOUS)))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-!^>[X] in {0}")
					@CsvSource({
						"X, 1, 0, 0",
						"X-, 1, 0, 0",
						"-X, 1, 1, 0-1",
						"XX-, 1, 0, 0",
						"-XX, 1, 1, 0-1",
						"-X-, 1, 1, 0-1",

						"XX, 2, 0-1, 0-1",
						"XX-, 2, 0-1, 0-1",
						"XXX, 2, 0-1, 0-1",
						"-XX, 2, 1-2, 0-2",
						"-XX-, 2, 1-2, 0-2",
						"-XXX, 2, 1-2, 0-2",
						"XXX-, 2, 0-1, 0-1",
						"--XX, 2, 2-3, 0-3",
						"XX--, 2, 0-1, 0-1",

						"XXX, 3, 0-2, 0-2",
						"-XXX, 3, 1-3, 0-3",
						"XXX-, 3, 0-2, 0-2",

						"--XXXXXXXXXX--, 10, 2-11, 0-11",
						"--XXXXXXXXXXXX--, 10, 2-11, 0-11",
					})
					@DisplayName("Node with a maximum multiplicity [possessive mode, single hit, limit, discontinuous]")
					void testPossessiveDiscontinuous(String target, int count,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atMostPossessive(count, DISCONTINUOUS)))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-!>[X] in {0}")
					@CsvSource({
						"-, 1, 0, -",
						"Y, 1, 0, -",
						"-Y, 2, 0-1, -",
						"-Y-, 2, 0-2, -",
					})
					@DisplayName("Mismatch with a maximum multiplicity [possessive mode]")
					void testPossessiveFail(String target, int count,
							@IntervalArg Interval visited,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atMostPossessive(count, CONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(candidates))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-!^>[X] in {0}")
					@CsvSource({
						"-, 1, 0, -",
						"Y, 1, 0, -",
						"-Y, 2, 0-1, -",
						"-Y-, 2, 0-2, -",
					})
					@DisplayName("Mismatch with a maximum multiplicity [possessive mode, discontinuous]")
					void testPossessiveFailDiscontinuous(String target, int count,
							@IntervalArg Interval visited,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								atMostPossessive(count, DISCONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(candidates))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-!>[X|x][x] in {0}")
					@CsvSource({
						"Xx, 2, 0-1, -, 0-1",
						"XXX-, 2, 0-3, 2-3, 0-2",

						"Xx, 3, 0-1, -, 0-1",
						"XXx, 3, 0-2, -, 0-2",
						"XXX-, 3, 0-3, 3, 0-2",
						"XXx-, 3, 0-3, 3, 0-2",

						"XXX-, 4, 0-3, 3, 0-2",
						"XXx-, 4, 0-3, 3, 0-2",
						"XXXx, 4, 0-3, -, 0-3",
					})
					@DisplayName("Mismatch due to possessive consumption [ordered]")
					void testPossessiveFail2(String target, int count,
							@IntervalArg Interval visited1,
							@IntervalArg Interval visited2,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(ordered(false,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atMostPossessive(count, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.target(target)
						.expectFail()
						// Underlying cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(candidates))
						// Underlying cache of second node
						.cache(cache(CACHE_1, false)
								.set(visited2)
								.window(target))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-!^>[X|x][x] in {0}")
					@CsvSource({
						"Xx, 2, 0-1, -",
						"XXX-, 2, 0-3, 2-3",

						"Xx, 3, 0-1, -",
						"XXx, 3, 0-2, -",
						"XXX-, 3, 0-3, 3",
						"XXx-, 3, 0-3, 3",

						"XXX-, 4, 0-3, 3",
						"XXx-, 4, 0-3, 3",
						"XXXx, 4, 0-3, -",
						"XXX-x, 4, 0-4, -",
						"XXx-x, 4, 0-4, -",
					})
					@DisplayName("Mismatch due to possessive consumption [ordered, discontinuous]")
					void testPossessiveFail2Discontinuous(String target, int count,
							@IntervalArg Interval visited1,
							@IntervalArg Interval visited2) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(ordered(false,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atMostPossessive(count, DISCONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.target(target)
						.expectFail()
						// Underlying cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(target, visited1, EQUALS_X_IC))
						// Underlying cache of second node
						.cache(cache(CACHE_1, false)
								.set(visited2)
								.window(target))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-!>[X|x][x] in {0}")
					@CsvSource({
						"Xx, 2, 0-1, -, 0-1",
						"XXx, 3, 0-2, -, 0-2",
						"XXX-, 3, 0-3, 3, 0-2",
						"XXx-, 3, 0-3, 3, 0-2",
						"XXx-x, 3, 0-3, 3, 0-2",
					})
					@DisplayName("Mismatch due to possessive consumption [adjacent]")
					void testPossessiveFail3(String target, int count,
							@IntervalArg Interval visited1,
							@IntervalArg Interval visited2,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(ordered(true,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atMostPossessive(count, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.target(target)
						.expectFail()
						// Underlying cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(candidates))
						// Underlying cache of second node
						.cache(cache(CACHE_1, false)
								.set(visited2)
								.window(target))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}-!^>[X|x][x] in {0}")
					@CsvSource({
						"Xx, 2, 0-1, -",
						"XXx, 3, 0-2, -",
						"XXX-, 3, 0-3, 3",
						"XXx-, 3, 0-3, 3",
						"XXX-x, 3, 0-4, 3",
						"X-x-x, 3, 0-4, -",
						"X-x-x-x, 3, 0-6, 5",
					})
					@DisplayName("Mismatch due to possessive consumption [adjacent, discontinuous]")
					void testPossessiveFail3Discontinuous(String target, int count,
							@IntervalArg Interval visited1,
							@IntervalArg Interval visited2) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(ordered(true,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atMostPossessive(count, DISCONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.target(target)
						.expectFail()
						// Underlying cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(target, visited1, EQUALS_X_IC))
						// Underlying cache of second node
						.cache(cache(CACHE_1, false)
								.set(visited2)
								.window(target))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{3}+!>[x|X][{1}] in {0}, adjacent={2}")
					@CsvSource({
						// Expansion of size 1 - ordered
						"XY, Y, false, 1, {0}, {1}, {0}, {1}",
						"XXY, Y, false, 1, {0;1}, {2;2}, {0-1}, {1-2}",
						"XX-XX, X, false, 1, {0;0;0;1;1;3}, {1;3;4;3;4;4}, {0-3}, {1-4}",
						// Expansion of size 1 - adjacent
						"XY, Y, true, 1, {0}, {1}, {0}, {1}",
						"XXY, Y, true, 1, {1}, {2}, {0-1}, {1-2}",
						// Expansion of size 2 - ordered
						"XY, Y, false, 2, {0}, {1}, {0-1}, {1}",
						"XXY, Y, false, 2, {0-1;1}, {2;2}, {0-2}, {2}",
						"XXX-X, X, false, 2, {0-1;0-1;1-2;2}, {2;4;4;4}, {0-3}, {2-4}",
						"XX-XX, X, false, 2, {0-1;0-1;1;1}, {3;4;3;4}, {0-4}, {2-4}", // we miss the 5. match due to possessive expansion
						"XXx-x, x, false, 2, {0-1;0-1;1-2;2}, {2;4;4;4}, {0-3}, {2-4}",
						// Expansion of size 2 - adjacent
						"XXY, Y, true, 2, {0-1;1}, {2;2}, {0-2}, {2}",
						"XXX-X, X, true, 2, {0-1}, {2}, {0-3}, {2-3}",
						// Expansion of size 3 - ordered
						"XXX-X, X, false, 3, {0-2;1-2;2}, {4;4;4}, {0-3}, {3-4}",
						"XX-XX, X, false, 3, {0-1;0-1;1;1}, {3;4;3;4}, {0-4}, {2-4}",
					})
					@DisplayName("verify possessive expansion with multiple nodes")
					void testPossessiveCompetition(String target,
							char c2, // search symbol for second node
							boolean adjacent,
							int count, // argument for 'AtMost' marker
							@IntervalArrayArg Interval[] hits1, // reported hits for first node
							@IntervalArrayArg Interval[] hit2, // reported hits for second node
							@IntervalArrayArg Interval[] visited1,  // all slots visited for first node
							@IntervalArrayArg Interval[] visited2) { // all slots visited for second node

						// Sanity check since we expect symmetric results here
						assertThat(hits1).hasSameSizeAs(hit2);

						/*
						 * We expect NODE_1 to aggressively consume slots with
						 * no regards for NODE_0, so that in contrast to reluctant mode
						 * we will miss some multi-match situations.
						 */
						matcherTest()
						.root(ordered(adjacent,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atMostPossessive(count, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp(c2)))))
						.target(target)
						.expectSuccess(hits1.length)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hitsForSet(target, EQUALS_X_IC))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.results(hits1.length, (r,i) -> r
								.map(NODE_0, hits1[i])
								.map(NODE_1, hit2[i]))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{3}+!^>[x|X][{1}] in {0}, adjacent={2}")
					@CsvSource({
						// Expansion of size 1 - ordered
						"XY, Y, false, 1, {{0}}, {1}, {0}, {1}",
						"XXY, Y, false, 1, {{0}{1}}, {2;2}, {0-1}, {1-2}",
						"XX-XX, X, false, 1, {{0}{0}{0}{1}{1}{3}}, {1;3;4;3;4;4}, {0-3}, {1-4}",
						// Expansion of size 1 - adjacent
						"XY, Y, true, 1, {{0}}, {1}, {0}, {1}",
						"XXY, Y, true, 1, {{1}}, {2}, {0-1}, {1-2}",
						// Expansion of size 2 - ordered
						"XY, Y, false, 2, {{0}}, {1}, {0-1}, {1}",
						"XXY, Y, false, 2, {{0;1}{1}}, {2;2}, {0-2}, {2}",
						"XXX-X, X, false, 2, {{0;1}{0;1}{1;2}}, {2;4;4}, {0-4}, {2-4}",
						"XX-XX, X, false, 2, {{0;1}{0;1}{1;3}}, {3;4;4}, {0-4}, {2-4}", // we miss the 5. match due to possessive expansion
						"XXx-x, x, false, 2, {{0;1}{0;1}{1;2}}, {2;4;4}, {0-4}, {2-4}",
						// Expansion of size 2 - adjacent
						"XXY, Y, true, 2, {{0;1}{1}}, {2;2}, {0-2}, {2}",
						"XXX-X, X, true, 2, {{0;1}}, {2}, {0-4}, {2-3}",
						"XX-XX, X, true, 2, {{1;3}}, {4}, {0-4}, {2;4}",
						// Expansion of size 3 - ordered
						"XXX-X, X, false, 3, {{0;1;2}}, {4}, {0-4}, {3-4}",
						"XX-XX, X, false, 3, {{0;1;3}}, {4}, {0-4}, {4}",
						// Expansion of size 3 - adjacent
						"XXXXX, X, true, 3, {{0;1;2}{1;2;3}}, {3;4}, {0-4}, {3-4}",
						"XX-XX, X, true, 3, {{0;1;3}}, {4}, {0-4}, {4}",
					})
					@DisplayName("verify possessive expansion with multiple nodes")
					void testPossessiveCompetitionDiscontinuous(String target,
							char c2, // search symbol for second node
							boolean adjacent,
							int count, // argument for 'AtMost' marker
							@IntMatrixArg int[][] hits1, // reported hits for first node
							@IntArrayArg int[] hit2, // reported hits for second node
							@IntervalArrayArg Interval[] visited1,  // all slots visited for first node
							@IntervalArrayArg Interval[] visited2) {// all slots visited for second node

						// Sanity check since we expect symmetric results here
						assertThat((Object[])hits1).as("Different match counters").hasSameSizeAs(hit2);

						/*
						 * We expect NODE_1 to aggressively consume slots with
						 * no regards for NODE_0, so that in contrast to reluctant mode
						 * we will miss some multi-match situations.
						 * (remember: state machine gets built back to front)
						 */
						matcherTest()
						.root(ordered(adjacent,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), atMostPossessive(count, DISCONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp(c2)))))
						.target(target)
						.expectSuccess(hits1.length)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hitsForSet(target, EQUALS_X_IC))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.results(hits1.length, (r,i) -> r
								.map(NODE_0, hits1[i])
								.map(NODE_1, hit2[i]))
						.assertResult();
					}

				}

				@Nested
				class Range {

					@ParameterizedTest(name="{index}: <{1}..{2}>[X] in {0}")
					@CsvSource({
						// Optional
						"Y, 0, 1, {-}, 0, false",
						"Y-, 0, 1, {-;-}, 0-1, true",
						"X, 0, 1, {0}, 0, false",
						"-X, 0, 1, {-;1}, 0-1, false",
						"X-, 0, 1, {0;-}, 0-1, false",
						// Singular
						"X, 1, 1, {0}, 0, false",
						"X-, 1, 1, {0}, 0-1, false",
						"-X, 1, 1, {1}, 0-1, false",
						"XX-, 1, 1, {0;1}, 0-2, false",
						"-XX, 1, 1, {1;2}, 0-2, false",
						"X-X, 1, 1, {0;2}, 0-2, false",
						// Sequence of 1 to 2
						"XX, 1, 2, {0-1;1}, 0-1, false",
						"X-, 1, 2, {0}, 0-1, false",
						"XX-, 1, 2, {0-1;1}, 0-2, false",
						"XXX, 1, 2, {0-1;1-2;2}, 0-2, false",
						"-X-, 1, 2, {1}, 0-2, false",
						"-XX, 1, 2, {1-2;2}, 0-2, false",
						"-XX-, 1, 2, {1-2;2}, 0-3, false",
						"-XXX, 1, 2, {1-2;2-3;3}, 0-3, false",
						"XXX-, 1, 2, {0-1;1-2;2}, 0-3, false",
						"--XX, 1, 2, {2-3;3}, 0-3, false",
						"XX--, 1, 2, {0-1;1}, 0-3, false",
						"X-XX, 1, 2, {0;2-3;3}, 0-3, false",
						// Sequence of 2 to 3
						"X-XX, 2, 3, {2-3}, 0-3, false",

						"--XXXXXXXXXX--, 8, 10, {2-11;3-11;4-11}, 0-12, false",
						"--XXXXXXXXXXX--, 8, 10, {2-11;3-12;4-12;5-12}, 0-13, false",
					})
					@DisplayName("Node with a bounded multiplicity [greedy mode, multiple hits]")
					void testGreedy(String target, int min, int max,
							@IntervalArrayArg Interval[] hits,
							@IntervalArg Interval visited,
							boolean allowDuplicates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								rangeGreedy(min, max, CONTINUOUS)))
						.target(target)
						.expectSuccess(hits.length)
						.allowDuplicates(allowDuplicates)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hitsForSet(target, EQUALS_X))
						.results(NODE_0, hits)
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}^>[X] in {0}")
					@CsvSource({
						// Optional
						"Y, 0, 1, {{}}, 0, false",
						"Y-, 0, 1, {{}{}}, 0-1, true",
						"X, 0, 1, {{0}}, 0, false",
						"-X, 0, 1, {{}{1}}, 0-1, false",
						"X-, 0, 1, {{0}{}}, 0-1, false",
						// Singular
						"X, 1, 1, {{0}}, 0, false",
						"X-, 1, 1, {{0}}, 0-1, false",
						"-X, 1, 1, {{1}}, 0-1, false",
						"XX-, 1, 1, {{0}{1}}, 0-2, false",
						"-XX, 1, 1, {{1}{2}}, 0-2, false",
						"X-X, 1, 1, {{0}{2}}, 0-2, false",
						// Sequence of 1 to 2
						"XX, 1, 2, {{0;1}{1}}, 0-1, false",
						"X-, 1, 2, {{0}}, 0-1, false",
						"XX-, 1, 2, {{0;1}{1}}, 0-2, false",
						"X-X, 1, 2, {{0;2}{2}}, 0-2, false",
						"XXX, 1, 2, {{0;1}{1;2}{2}}, 0-2, false",
						"-X-, 1, 2, {{1}}, 0-2, false",
						"-XX, 1, 2, {{1;2}{2}}, 0-2, false",
						"-XX-, 1, 2, {{1;2}{2}}, 0-3, false",
						"-XXX, 1, 2, {{1;2}{2;3}{3}}, 0-3, false",
						"XXX-, 1, 2, {{0;1}{1;2}{2}}, 0-3, false",
						"--XX, 1, 2, {{2;3}{3}}, 0-3, false",
						"XX--, 1, 2, {{0;1}{1}}, 0-3, false",
						// Sequence of 2 to 3
						"X-XX, 2, 3, {{0;2;3}{2;3}}, 0-3, false",
						// Sequence of 2 to 4
						"X-XX, 2, 4, {{0;2;3}{2;3}}, 0-3, false",
						"X-XX-X, 2, 4, {{0;2;3;5}{2;3;5}{3;5}}, 0-5, false",

						"--XXX--XXX--, 4, 5, {{2;3;4;7;8}{3;4;7;8;9}{4;7;8;9}}, 0-11, false",

						"--XXXXXXXX--, 4, 6, {{2;3;4;5;6;7}{3;4;5;6;7;8}{4;5;6;7;8;9}{5;6;7;8;9}{6;7;8;9}}, 0-11, false",
						"--XXXXXXXXX--, 4, 6, {{2;3;4;5;6;7}{3;4;5;6;7;8}{4;5;6;7;8;9}{5;6;7;8;9;10}{6;7;8;9;10}{7;8;9;10}}, 0-12, false",
					})
					@DisplayName("Node with a bounded multiplicity [greedy mode, multiple hits, discontinuous]")
					void testGreedyDiscontinuous(String target, int min, int max,
							@IntMatrixArg int[][] hits,
							@IntervalArg Interval visited,
							boolean allowDuplicates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								rangeGreedy(min, max, DISCONTINUOUS)))
						.target(target)
						.expectSuccess(hits.length)
						.allowDuplicates(allowDuplicates)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hitsForSet(target, EQUALS_X))
						.results(hits.length, (r, i) -> r.map(NODE_0, hits[i]))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}>[X] in {0}")
					@CsvSource({
						"-, 1, 1, 0",
						"Y, 1, 1, 0",
						"-X, 2, 3, 0",
						"-X-, 2, 3, 0-2",
						"X-X, 2, 3, 0-1",
						"X-X-, 2, 3, 0-3",
						"XX-XX-X, 3, 5, 0-5",
					})
					@DisplayName("Mismatch with a bounded multiplicity [greedy mode]")
					void testGreedyFail(String target, int min, int max,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								rangeGreedy(min, max, CONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(target, visited, EQUALS_X))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}^>[X] in {0}")
					@CsvSource({
						"-, 1, 1, 0",
						"Y, 1, 1, 0",
						"-X, 2, 3, 0",
						"-X-, 2, 3, 0-2",
						"X-X, 3, 4, 0-2",
						"X-X-, 3, 3, 0-3",
					})
					@DisplayName("Mismatch with a bounded multiplicity [greedy mode, discontinuous]")
					void testGreedyFailDiscontinuous(String target, int min, int max,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								rangeGreedy(min, max, DISCONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(target, visited, EQUALS_X))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{3}..{4}>[x|X][{1}] in {0}, adjacent={2}")
					@CsvSource({
						// Optional, ordered
						"x, x, false, 0, 1, -, 0, 0, 0",
						"y, y, false, 0, 1, -, 0, 0, 0",
						"-x, x, false, 0, 1, -, 1, 0, 0-1",
						"--x, x, false, 0, 2, -, 2, 0, 0-2",
						"-y, y, false, 0, 1, -, 1, 0, 0-1",
						"Xx, x, false, 0, 1, {0}, 1, 0, 1",
						"xy, y, false, 0, 1, {0}, 1, 0, 1",
						"xy, y, false, 0, 2, {0}, 1, 0-1, 1",
						// Optional, adjacent
						"x, x, true, 0, 1, -, 0, 0, 0",
						"y, y, true, 0, 1, -, 0, 0, 0",
						"-x, x, true, 0, 1, -, 1, 0-1, 0-1",
						"-y, y, true, 0, 1, -, 1, 0-1, 0-1",
						"xy, y, true, 0, 1, {0}, 1, 0, 1",
						"xy, y, true, 0, 2, {0}, 1, 0-1, 1",
						// Expansion of size 1 to 2, ordered
						"Xx, x, false, 1, 2, {0}, 1, 0-1, 1",
						"XXx, x, false, 1, 2, {0;1}, 2, 0-1, 2",
						"XXxx, x, false, 1, 2, {0;1}, 2, 0-1, 2",
						"XXx-, x, false, 1, 2, {0;1}, 2, 0-1, 2",
						"-XXx, x, false, 1, 2, {1;2}, 3, 0-2, 3",
						"-XXx-, x, false, 1, 2, {1;2}, 3, 0-2, 3",
						"XxX, x, false, 1, 2, {0}, 1, 0-1, 1-2",
						"XxX-, x, false, 1, 2, {0}, 1, 0-1, 1-3",
						"XxXx-, x, false, 1, 2, {0;1}, 3, 0-1, 2-3",
						"-XxX, x, false, 1, 2, {1}, 2, 0-2, 2-3",
						"-XxX-, x, false, 1, 2, {1}, 2, 0-2, 2-4",
						// Expansion of size 1 to 2, adjacent
						"Xx, x, true, 1, 2, {0}, 1, 0-1, 1",
						"XXx, x, true, 1, 2, {0;1}, 2, 0-1, 2",
						"XXxx, x, true, 1, 2, {0;1}, 2, 0-1, 2",
						"XxX, x, true, 1, 2, {0}, 1, 0-1, 1-2",
						"XxXx-, x, true, 1, 2, {0}, 1, 0-1, 1-2",
						// Expansion of size 1 to 3, ordered
						"XXx, x, false, 1, 3, {0;1}, 2, 0-2, 2",
						"XXXx, x, false, 1, 3, {0;1;2}, 3, 0-2, 3",
						"XXXxx, x, false, 1, 3, {0;1;2}, 3, 0-2, 3",
						"XXX-x, x, false, 1, 3, {0;1;2}, 4, 0-2, 3-4",
						"XXxX, x, false, 1, 3, {0;1}, 2, 0-2, 2-3",
						"XXxX-, x, false, 1, 3, {0;1}, 2, 0-2, 2-4",
						"-XXxX, x, false, 1, 3, {1;2}, 3, 0-3, 3-4",
						"-XXxX-, x, false, 1, 3, {1;2}, 3, 0-3, 3-5",
						// Expansion of size 1 to 3, adjacent
						"XXx, x, true, 1, 3, {0;1}, 2, 0-2, 2",
						"XXXx, x, true, 1, 3, {0;1;2}, 3, 0-2, 3",
						"XXxX, x, true, 1, 3, {0;1}, 2, 0-2, 2-3",
						"XXxX-, x, true, 1, 3, {0;1}, 2, 0-2, 2-3",
						"-XXxX, x, true, 1, 3, {1;2}, 3, 0-3, 3-4",
						"-XXxX-, x, true, 1, 3, {1;2}, 3, 0-3, 3-4",
						// Consume first target for second node, ordered
						"XxXxX, x, false, 1, 10, {0;1;2}, 3, 0-4, 3-4",
						"XxXxX, x, false, 2, 10, {0;1;2}, 3, 0-4, 3-4",
						"XxXxX-, x, false, 2, 10, {0;1;2}, 3, 0-5, 3-5",
						"XxXxX-x, x, false, 2, 10, {0;1;2;3;4}, 6, 0-5, 5-6",
						"-XxXxX, x, false, 2, 10, {1;2;3}, 4, 0-5, 4-5",
						"-XxXxX-, x, false, 2, 10, {1;2;3}, 4, 0-6, 4-6",
						"-XxXxX-x, x, false, 2, 10, {1;2;3;4;5}, 7, 0-6, 6-7",
						// Consume first target for second node, adjacent
						"XxXxX, x, true, 1, 10, {0;1;2}, 3, 0-4, 3-4",
						"XxXxX, x, true, 2, 10, {0;1;2}, 3, 0-4, 3-4",
						"XxXxX-, x, true, 2, 10, {0;1;2}, 3, 0-5, 3-5",
						"XxXxX-x, x, true, 2, 10, {0;1;2}, 3, 0-5, 3-5",
						"-XxXxX, x, true, 2, 10, {1;2;3}, 4, 0-5, 4-5",
						"-XxXxX-, x, true, 2, 10, {1;2;3}, 4,  0-6, 4-6",
						"-XxXxX-x, x, true, 2, 10, {1;2;3}, 4, 0-6, 4-6",
						// Greediness, adjacent
						"xx, x, true, 1, 2, {0}, 1, 0-1, 1",
						"Xxx, x, true, 1, 2, {0;1}, 2, 0-1, 2",
						"Xxx, x, true, 1, 3, {0;1}, 2, 0-2, 2",
						"Xxxx, x, true, 1, 2, {0;1}, 2, 0-1, 2",
						"Xxxx, x, true, 1, 3, {0;1;2}, 3, 0-2, 3",
						"Xxxx, x, true, 1, 4, {0;1;2}, 3, 0-3, 3",
						"Xxxx-, x, true, 1, 2, {0;1}, 2, 0-1, 2",
						"Xxxx-, x, true, 1, 3, {0;1;2}, 3, 0-2, 3",
						"Xxxx-, x, true, 1, 4, {0;1;2}, 3, 0-3, 3-4",
						"Xxxx-, x, true, 1, 5, {0;1;2}, 3, 0-4, 3-4",
						"Xxx, x, true, 2, 3, {0;1}, 2, 0-2, 2",
						"Xxxx, x, true, 2, 3, {0;1;2}, 3, 0-2, 3",
						"Xxxx, x, true, 2, 4, {0;1;2}, 3, 0-3, 3",
						"Xxxx-, x, true, 2, 3, {0;1;2}, 3, 0-2, 3",
						"Xxxx-, x, true, 2, 4, {0;1;2}, 3, 0-3, 3-4",
					})
					@DisplayName("verify greedy expansion with multiple nodes [limited]")
					void testGreedyCompetition(String target,
							char c2,
							boolean adjacent,
							int min, int max, // arguments for 'Range' marker
							@IntArrayArg int[] hits1, // reported hits for first node
							int hit2, // reported hit for second node
							@IntervalArg Interval visited1, // all slots visited for first node
							@IntervalArg Interval visited2) { // all slots visited for second node
						/*
						 * We expect NODE_1 to visit and greedily consume all the
						 * X and x slots and then back off until the first x is
						 * reached for NODE_0.
						 */
						matcherTest()
						.root(ordered(adjacent,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), rangeGreedy(min, max, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp(c2)))))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(target, visited1, EQUALS_X_IC))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.result(result(0)
								.map(NODE_0, hits1)
								.map(NODE_1, hit2))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{3}..{4}^>[x|X][{1}] in {0}, adjacent={2}")
					@CsvSource({
						// Optional, ordered
						"x, x, false, 0, 1, -, 0, 0, 0",
						"y, y, false, 0, 1, -, 0, 0, 0",
						"-x, x, false, 0, 1, -, 1, 0, 0-1",
						"--x, x, false, 0, 2, -, 2, 0, 0-2",
						"-y, y, false, 0, 1, -, 1, 0, 0-1",
						"x-y, y, false, 0, 1, {0}, 2, 0, 1-2",
						"Xx, x, false, 0, 1, {0}, 1, 0, 1",
						"xy, y, false, 0, 1, {0}, 1, 0, 1",
						"xy, y, false, 0, 2, {0}, 1, 0-1, 1",
						// Optional, adjacent
						"x, x, true, 0, 1, -, 0, 0, 0",
						"y, y, true, 0, 1, -, 0, 0, 0",
						"-x, x, true, 0, 1, -, 1, 0-1, 0-1",
						"-y, y, true, 0, 1, -, 1, 0-1, 0-1",
						"xy, y, true, 0, 1, {0}, 1, 0, 1",
						"xy, y, true, 0, 2, {0}, 1, 0-1, 1",
						// Expansion of size 1 to 2, ordered
						"Xx, x, false, 1, 2, {0}, 1, 0-1, 1",
						"XXx, x, false, 1, 2, {0;1}, 2, 0-1, 2",
						"X-x, x, false, 1, 2, {0}, 2, 0-2, 1-2",
						"X-xx, x, false, 1, 2, {0;2}, 3, 0-2, 3",
						"X-xxx, x, false, 1, 2, {0;2}, 3, 0-2, 3",
						"X-x-x, x, false, 1, 2, {0;2}, 4, 0-2, 3-4",
						"XXxx, x, false, 1, 2, {0;1}, 2, 0-1, 2",
						"XXx-, x, false, 1, 2, {0;1}, 2, 0-1, 2",
						"-XXx, x, false, 1, 2, {1;2}, 3, 0-2, 3",
						"-XXx-, x, false, 1, 2, {1;2}, 3, 0-2, 3",
						"XxX, x, false, 1, 2, {0}, 1, 0-1, 1-2",
						"XxX-, x, false, 1, 2, {0}, 1, 0-1, 1-3",
						"XxXx-, x, false, 1, 2, {0;1}, 3, 0-1, 2-3",
						"-XxX, x, false, 1, 2, {1}, 2, 0-2, 2-3",
						"-XxX-, x, false, 1, 2, {1}, 2, 0-2, 2-4",
						// Expansion of size 1 to 2, adjacent
						"Xx, x, true, 1, 2, {0}, 1, 0-1, 1",
						"XXx, x, true, 1, 2, {0;1}, 2, 0-1, 2",
						"X-xx, x, true, 1, 2, {0;2}, 3, 0-2, 3",
						"X-xxx, x, true, 1, 2, {0;2}, 3, 0-2, 3",
						"XXxx, x, true, 1, 2, {0;1}, 2, 0-1, 2",
						"XxX, x, true, 1, 2, {0}, 1, 0-1, 1-2",
						"XxXx-, x, true, 1, 2, {0}, 1, 0-1, 1-2",
						// Expansion of size 1 to 3, ordered
						"XXx, x, false, 1, 3, {0;1}, 2, 0-2, 2",
						"XXXx, x, false, 1, 3, {0;1;2}, 3, 0-2, 3",
						"XXXxx, x, false, 1, 3, {0;1;2}, 3, 0-2, 3",
						"XXX-x, x, false, 1, 3, {0;1;2}, 4, 0-2, 3-4",
						"XXxX, x, false, 1, 3, {0;1}, 2, 0-2, 2-3",
						"XXxX-, x, false, 1, 3, {0;1}, 2, 0-2, 2-4",
						"-XXxX, x, false, 1, 3, {1;2}, 3, 0-3, 3-4",
						"-XXxX-, x, false, 1, 3, {1;2}, 3, 0-3, 3-5",
						"X-x, x, false, 1, 3, {0}, 2, 0-2, 1-2",
						"X-xx, x, false, 1, 3, {0;2}, 3, 0-3, 3",
						"X-xx-, x, false, 1, 3, {0;2}, 3, 0-3, 3-4",
						"X-xxx, x, false, 1, 3, {0;2;3}, 4, 0-3, 4",
						"X-xxx-, x, false, 1, 3, {0;2;3}, 4, 0-3, 4",
						"X-x-x, x, false, 1, 3, {0;2}, 4, 0-4, 3-4",
						"X-x-x-x, x, false, 1, 3, {0;2;4}, 6, 0-4, 5-6",
						// Expansion of size 1 to 3, adjacent
						"XXx, x, true, 1, 3, {0;1}, 2, 0-2, 2",
						"XXXx, x, true, 1, 3, {0;1;2}, 3, 0-2, 3",
						"XXxX, x, true, 1, 3, {0;1}, 2, 0-2, 2-3",
						"XXxX-, x, true, 1, 3, {0;1}, 2, 0-2, 2-3",
						"-XXxX, x, true, 1, 3, {1;2}, 3, 0-3, 3-4",
						"-XXxX-, x, true, 1, 3, {1;2}, 3, 0-3, 3-4",
						"X-xx, x, true, 1, 3, {0;2}, 3, 0-3, 3",
						"X-xx-, x, true, 1, 3, {0;2}, 3, 0-3, 3-4",
						"X-xxx, x, true, 1, 3, {0;2;3}, 4, 0-3, 4",
						"X-xxx-, x, true, 1, 3, {0;2;3}, 4, 0-3, 4",
						"X-x-xx, x, true, 1, 3, {0;2;4}, 5, 0-4, 5",
						// Consume first target for second node, ordered
						"XxXxX, x, false, 1, 10, {0;1;2}, 3, 0-4, 3-4",
						"X-xXxX, x, false, 1, 10, {0;2;3}, 4, 0-5, 4-5",
						"X-xX-xX, x, false, 1, 10, {0;2;3}, 5, 0-6, 4-6",
						"XxXxX, x, false, 2, 10, {0;1;2}, 3, 0-4, 3-4",
						"X-xXxX, x, false, 2, 10, {0;2;3}, 4, 0-5, 4-5",
						"X-xX-xX, x, false, 2, 10, {0;2;3}, 5, 0-6, 4-6",
						"XxXxX-, x, false, 2, 10, {0;1;2}, 3, 0-5, 3-5",
						"XxXxX-x, x, false, 2, 10, {0;1;2;3;4}, 6, 0-6, 5-6",
						"-XxXxX, x, false, 2, 10, {1;2;3}, 4, 0-5, 4-5",
						"-XxXxX-, x, false, 2, 10, {1;2;3}, 4, 0-6, 4-6",
						"-XxXxX-x, x, false, 2, 10, {1;2;3;4;5}, 7, 0-7, 6-7",
						// Consume first target for second node, adjacent
						"XxXxX, x, true, 1, 10, {0;1;2}, 3, 0-4, 3-4",
						"Xx-XxX, x, true, 1, 10, {0;1;3}, 4, 0-5, 4-5",
						"Xx-Xx-X, x, true, 1, 10, {0;1;3}, 4, 0-6, 4-5",
						"XxXxX, x, true, 2, 10, {0;1;2}, 3, 0-4, 3-4",
						"Xx-XxX, x, true, 2, 10, {0;1;3}, 4, 0-5, 4-5",
						"Xx-Xx-X, x, true, 2, 10, {0;1;3}, 4, 0-6, 4-5",
						"XxXxX-, x, true, 2, 10, {0;1;2}, 3, 0-5, 3-5",
						"XxXxX-x, x, true, 2, 10, {0;1;2}, 3, 0-6, 3-5",
						"-XxXxX, x, true, 2, 10, {1;2;3}, 4, 0-5, 4-5",
						"-XxXxX-, x, true, 2, 10, {1;2;3}, 4,  0-6, 4-6",
						"-XxXxX-x, x, true, 2, 10, {1;2;3}, 4, 0-7, 4-6",
						// Greediness, adjacent
						"xx, x, true, 1, 2, {0}, 1, 0-1, 1",
						"Xxx, x, true, 1, 2, {0;1}, 2, 0-1, 2",
						"Xxx, x, true, 1, 3, {0;1}, 2, 0-2, 2",
						"Xxxx, x, true, 1, 2, {0;1}, 2, 0-1, 2",
						"Xxxx, x, true, 1, 3, {0;1;2}, 3, 0-2, 3",
						"Xxxx, x, true, 1, 4, {0;1;2}, 3, 0-3, 3",
						"Xxxx-, x, true, 1, 2, {0;1}, 2, 0-1, 2",
						"Xxxx-, x, true, 1, 3, {0;1;2}, 3, 0-2, 3",
						"Xxxx-, x, true, 1, 4, {0;1;2}, 3, 0-3, 3-4",
						"Xxxx-, x, true, 1, 5, {0;1;2}, 3, 0-4, 3-4",
						"Xxx, x, true, 2, 3, {0;1}, 2, 0-2, 2",
						"Xxxx, x, true, 2, 3, {0;1;2}, 3, 0-2, 3",
						"Xxxx, x, true, 2, 4, {0;1;2}, 3, 0-3, 3",
						"Xxxx-, x, true, 2, 3, {0;1;2}, 3, 0-2, 3",
						"Xxxx-, x, true, 2, 4, {0;1;2}, 3, 0-3, 3-4",
					})
					@DisplayName("verify greedy expansion with multiple nodes [limited, discontinuous]")
					void testGreedyCompetitionDiscontinuous(String target,
							char c2,
							boolean adjacent,
							int min, int max, // arguments for 'Range' marker
							@IntArrayArg int[] hits1, // reported hits for first node
							int hit2, // reported hit for second node
							@IntervalArg Interval visited1, // all slots visited for first node
							@IntervalArg Interval visited2) { // all slots visited for second node
						/*
						 * We expect NODE_1 to visit and greedily consume all the
						 * X and x slots and then back off until the first x is
						 * reached for NODE_0.
						 */
						matcherTest()
						.root(ordered(adjacent,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), rangeGreedy(min, max, DISCONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp(c2)))))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(target, visited1, EQUALS_X_IC))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.result(result(0)
								.map(NODE_0, hits1)
								.map(NODE_1, hit2))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}>[X] in {0}")
					@CsvSource({
						"X, 1, 2, 0, 0",
						"X-, 1, 2, 0, 0",
						"XX, 1, 2, 0, 0",
						"-X, 1, 2, 1, 0-1",
						"XX-, 1, 2, 0, 0",
						"-XX, 1, 2, 1, 0-1",
						"-X-, 1, 2, 1, 0-1",

						"XX, 2, 4, 0-1, 0-1",
						"XX-, 2, 4, 0-1, 0-1",
						"XXX, 2, 4, 0-1, 0-1",
						"-XX, 2, 4, 1-2, 0-2",
						"-XX-, 2, 4, 1-2, 0-2",
						"-XXX, 2, 4, 1-2, 0-2",
						"XXX-, 2, 4, 0-1, 0-1",
						"--XX, 2, 4, 2-3, 0-3",
						"XX--, 2, 4, 0-1, 0-1",

						"XXX, 3, 4, 0-2, 0-2",
						"-XXX, 3, 4, 1-3, 0-3",
						"XXX-, 3, 4, 0-2, 0-2",

						"--XXXXXXXXXX--, 10, 12, 2-11, 0-11",
						"--XXXXXXXXXXX--, 10, 12, 2-11, 0-11",
					})
					@DisplayName("Node with a bounded multiplicity [reluctant mode, single hit, limit]")
					void testReluctant(String target, int min, int max,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								rangeReluctant(min, max, CONTINUOUS)))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}^>[X] in {0}")
					@CsvSource({
						"X, 1, 2, 0, 0",
						"X-, 1, 2, 0, 0",
						"XX, 1, 2, 0, 0",
						"-X, 1, 2, 1, 0-1",
						"XX-, 1, 2, 0, 0",
						"-XX, 1, 2, 1, 0-1",
						"-X-, 1, 2, 1, 0-1",

						"XX, 2, 4, 0-1, 0-1",
						"XX-, 2, 4, 0-1, 0-1",
						"XXX, 2, 4, 0-1, 0-1",
						"-XX, 2, 4, 1-2, 0-2",
						"-XX-, 2, 4, 1-2, 0-2",
						"-XXX, 2, 4, 1-2, 0-2",
						"XXX-, 2, 4, 0-1, 0-1",
						"--XX, 2, 4, 2-3, 0-3",
						"XX--, 2, 4, 0-1, 0-1",

						"XXX, 3, 4, 0-2, 0-2",
						"-XXX, 3, 4, 1-3, 0-3",
						"XXX-, 3, 4, 0-2, 0-2",

						"--XXXXXXXXXX--, 10, 12, 2-11, 0-11",
						"--XXXXXXXXXXX--, 10, 12, 2-11, 0-11",
					})
					@DisplayName("Node with a bounded multiplicity [reluctant mode, single hit, limit, discontinuous]")
					void testReluctantDiscontinuous(String target, int min, int max,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								rangeReluctant(min, max, DISCONTINUOUS)))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}?>[X] in {0}")
					@CsvSource({
						// can't test fail on zero-width assertion here
						"-, 1, 2, 0, -",
						"Y, 1, 2, 0, -",
						"-Y, 2, 3, 0, -",
						"-Y-, 2, 3, 0-1, -",
						"X, 2, 3, -, -", // early abort of scan
						"XY, 2, 3, 0-1, 0",
						"-X, 2, 3, 0, -",
						"-X-, 2, 3, 0-2, 1",
					})
					@DisplayName("Mismatch with a bounded multiplicity [reluctant mode]")
					void testReluctantFail(String target, int min, int max,
							@IntervalArg Interval visited,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								rangeReluctant(min, max, CONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(candidates))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}?^>[X] in {0}")
					@CsvSource({
						// can't test fail on zero-width assertion here
						"-, 1, 2, 0, -",
						"Y, 1, 2, 0, -",
						"-Y, 2, 3, 0, -",
						"-Y-, 2, 3, 0-1, -",
						"X, 2, 3, -, -", // early abort of scan
						"XY, 2, 3, 0-1, 0",
						"-X, 2, 3, 0, -",
						"-X-, 2, 3, 0-2, 1",
					})
					@DisplayName("Mismatch with a bounded multiplicity [reluctant mode, discontinuous]")
					void testReluctantFailDiscontinuous(String target, int min, int max,
							@IntervalArg Interval visited,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								rangeReluctant(min, max, DISCONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(candidates))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}?>[X] in {0}")
					@CsvSource({
						"XX, 1, 2, {0;1}, 0-1",
						"XX-, 1, 2, {0;1}, 0-2",
						"-XX, 1, 2, {1;2}, 0-2",
						"-XX-, 1, 2, {1;2}, 0-3",

						"XXX, 2, 3, {0-1;1-2}, 0-2",
						"-XXX, 2, 3, {1-2;2-3}, 0-3",
						"XXX-, 2, 3, {0-1;1-2}, 0-3",
						"-XXX-, 2, 3, {1-2;2-3}, 0-4",

						"-XXXX--XXXX-, 2, 4, {1-2;2-3;3-4;7-8;8-9;9-10}, 0-11",
					})
					@DisplayName("Node with a bounded multiplicity [reluctant mode, multiple hits]")
					void testReluctantMultiple(String target, int min, int max,
							@IntervalArrayArg Interval[] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								rangeReluctant(min, max, CONTINUOUS)))
						.target(target)
						.expectSuccess(hits.length)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.results(NODE_0, hits)
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}?^>[X] in {0}")
					@CsvSource({
						"XX, 1, 2, {{0}{1}}, 0-1",
						"XX-, 1, 2, {{0}{1}}, 0-2",
						"-XX, 1, 2, {{1}{2}}, 0-2",
						"-XX-, 1, 2, {{1}{2}}, 0-3",
						"X-X-, 1, 2, {{0}{2}}, 0-3",

						"XXX, 2, 3, {{0;1}{1;2}}, 0-2",
						"-XXX, 2, 3, {{1;2}{2;3}}, 0-3",
						"X-XX, 2, 3, {{0;2}{2;3}}, 0-3",
						"XX-X, 2, 3, {{0;1}{1;3}}, 0-3",
						"XXX-, 2, 3, {{0;1}{1;2}}, 0-3",
						"-XXX-, 2, 3, {{1;2}{2;3}}, 0-4",

						"-XXXX--XXXX-, 2, 4, {{1;2}{2;3}{3;4}{4;7}{7;8}{8;9}{9;10}}, 0-11",
					})
					@DisplayName("Node with a bounded multiplicity [reluctant mode, multiple hits, discontinuous]")
					void testReluctantMultipleDiscontinuous(String target, int min, int max,
							@IntMatrixArg int[][] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								rangeReluctant(min, max, DISCONTINUOUS)))
						.target(target)
						.expectSuccess(hits.length)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.results(hits.length, (r, i) -> r.map(NODE_0, hits[i]))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{2}-{3}?>[x|X][x] in {0}, adjacent={1}")
					@CsvSource({
						// Optional - ordered
						"x, false, 0, 1, -, 0, -, 0, -",
						"-x, false, 0, 1, -, 1, -, 0-1, -",
						"--x, false, 0, 1, -, 2, -, 0-2, -",
						// Optional - adjacent
						"x, true, 0, 1, -, 0, -, 0, -",
						"-x, true, 0, 1, -, 1, 0, 0-1, -",
						"--x, true, 0, 1, -, 2, 0-1, 0-2, -",
						// Expansion of size 1 to 2 - ordered
						"Xx, false, 1, 2, 0, 1, 0, 1, 0",
						"XXx, false, 1, 2, 0, 2, 0, 1-2, 0",
						"XXx-, false, 1, 2, 0, 2, 0, 1-2, 0",
						"-XXx, false, 1, 2, 1, 3, 0-1, 2-3, 1",
						"-XXx-, false, 1, 2, 1, 3, 0-1, 2-3, 1",
						"XxX, false, 1, 2, 0, 1, 0, 1, 0",
						"XxX-, false, 1, 2, 0, 1, 0, 1, 0",
						"-XxX, false, 1, 2, 1, 2, 0-1, 2, 1",
						"-XxX-, false, 1, 2, 1, 2, 0-1, 2, 1",
						// Expansion of size 1 to 2 - adjacent
						"Xx, true, 1, 2, 0, 1, 0, 1, 0",
						"XXx, true, 1, 2, 0-1, 2, 0-1, 1-2, 0-1",
						"XXXx, true, 1, 2, 1-2, 3, 0-2, 1-3, 0-2",
						"XXx-, true, 1, 2, 0-1, 2, 0-1, 1-2, 0-1",
						"-XXx, true, 1, 2, 1-2, 3, 0-2, 2-3, 1-2",
						"-XXx-, true, 1, 2, 1-2, 3, 0-2, 2-3, 1-2",
						"XxX, true, 1, 2, 0, 1, 0, 1, 0",
						"XxX-, true, 1, 2, 0, 1, 0, 1, 0",
						"-XxX, true, 1, 2, 1, 2, 0-1, 2, 1",
						"-XxX-, true, 1, 2, 1, 2, 0-1, 2, 1",
						// Expansion of size 2 to 3 - ordered
						"XXx, false, 2, 3, 0-1, 2, 0-1, 2, 0-1",
						"XXXx, false, 2, 3, 0-1, 3, 0-1, 2-3, 0-1",
						"XXxX, false, 2, 3, 0-1, 2, 0-1, 2, 0-1",
						"XXxX-, false, 2, 3, 0-1, 2, 0-1, 2, 0-1",
						"-XXxX, false, 2, 3, 1-2, 3, 0-2, 3, 1-2",
						"-XXxX-, false, 2, 3, 1-2, 3, 0-2, 3, 1-2",
						// Expansion of size 2 to 3 - adjacent
						"XXx, true, 2, 3, 0-1, 2, 0-1, 2, 0-1",
						"XXXx, true, 2, 3, 0-2, 3, 0-2, 2-3, 0-2",
						"XXXXx, true, 2, 3, 1-3, 4, 0-3, 2-4, 0-3",
						"XXxX, true, 2, 3, 0-1, 2, 0-1, 2, 0-1",
						"XXxX-, true, 2, 3, 0-1, 2, 0-1, 2, 0-1",
						"-XXxX, true, 2, 3, 1-2, 3, 0-2, 3, 1-2",
						"-XXxX-, true, 2, 3, 1-2, 3, 0-2, 3, 1-2",
						// Reluctance - adjacent
						"Xxx, true, 1, 3, 0, 1, 0, 1, 0",
						"Xxxx, true, 1, 3, 0, 1, 0, 1, 0",
						"Xxxx-, true, 1, 3, 0, 1, 0, 1, 0",
						"Xxx, true, 2, 3, 0-1, 2, 0-1, 2, 0-1",
						"Xxxx, true, 2, 3, 0-1, 2, 0-1, 2, 0-1",
						"Xxxx-, true, 2, 3, 0-1, 2, 0-1, 2, 0-1",
					})
					@DisplayName("verify reluctant expansion with multiple nodes [limited]")
					void testReluctantCompetition(String target,
							boolean adjacent,
							int min, int max, // arguments for 'Range' marker
							@IntervalArg Interval hits1, // reported hits for first node
							int hit2, // reported hit for second node
							@IntervalArg Interval visited1,  // all slots visited for first node
							@IntervalArg Interval visited2, // all slots visited for second node
							@IntervalArg Interval candidates1) { // slots marked as true for first node
						/*
						 * We expect NODE_1 to only proceed with consumption of slots
						 * while NODE_0 does not already match the next one.
						 */
						matcherTest()
						.root(ordered(adjacent,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), rangeReluctant(min, max, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(candidates1))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.result(result(0)
								.map(NODE_0, hits1)
								.map(NODE_1, hit2))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{2}-{3}?^>[x|X][x] in {0}, adjacent={1}")
					@CsvSource({
						// Optional - ordered
						"x, false, 0, 1, -, 0, -, 0, -",
						"-x, false, 0, 1, -, 1, -, 0-1, -",
						"--x, false, 0, 1, -, 2, -, 0-2, -",
						// Optional - adjacent
						"x, true, 0, 1, -, 0, -, 0, -",
						"-x, true, 0, 1, -, 1, 0, 0-1, -",
						"--x, true, 0, 1, -, 2, 0-1, 0-2, -",
						// Expansion of size 1 to 2 - ordered
						"Xx, false, 1, 2, 0, 1, 0, 1, 0",
						"XXx, false, 1, 2, 0, 2, 0, 1-2, 0",
						"XXx-, false, 1, 2, 0, 2, 0, 1-2, 0",
						"-XXx, false, 1, 2, 1, 3, 0-1, 2-3, 1",
						"-XXx-, false, 1, 2, 1, 3, 0-1, 2-3, 1",
						"XxX, false, 1, 2, 0, 1, 0, 1, 0",
						"XxX-, false, 1, 2, 0, 1, 0, 1, 0",
						"-XxX, false, 1, 2, 1, 2, 0-1, 2, 1",
						"-XxX-, false, 1, 2, 1, 2, 0-1, 2, 1",
						// Expansion of size 1 to 2 - adjacent
						"Xx, true, 1, 2, 0, 1, 0, 1, 0",
						"XXx, true, 1, 2, 0-1, 2, 0-1, 1-2, 0-1",
						"XXXx, true, 1, 2, 1-2, 3, 0-2, 1-3, 0-2",
						"XXx-, true, 1, 2, 0-1, 2, 0-1, 1-2, 0-1",
						"-XXx, true, 1, 2, 1-2, 3, 0-2, 2-3, 1-2",
						"-XXx-, true, 1, 2, 1-2, 3, 0-2, 2-3, 1-2",
						"XxX, true, 1, 2, 0, 1, 0, 1, 0",
						"XxX-, true, 1, 2, 0, 1, 0, 1, 0",
						"-XxX, true, 1, 2, 1, 2, 0-1, 2, 1",
						"-XxX-, true, 1, 2, 1, 2, 0-1, 2, 1",
						// Expansion of size 2 to 3 - ordered
						"XXx, false, 2, 3, 0-1, 2, 0-1, 2, 0-1",
						"XXXx, false, 2, 3, 0-1, 3, 0-1, 2-3, 0-1",
						"XXxX, false, 2, 3, 0-1, 2, 0-1, 2, 0-1",
						"XXxX-, false, 2, 3, 0-1, 2, 0-1, 2, 0-1",
						"-XXxX, false, 2, 3, 1-2, 3, 0-2, 3, 1-2",
						"-XXxX-, false, 2, 3, 1-2, 3, 0-2, 3, 1-2",
						// Expansion of size 2 to 3 - adjacent
						"XXx, true, 2, 3, 0-1, 2, 0-1, 2, 0-1",
						"XXXx, true, 2, 3, 0-2, 3, 0-2, 2-3, 0-2",
						"XXXXx, true, 2, 3, 1-3, 4, 0-3, 2-4, 0-3",
						"XXxX, true, 2, 3, 0-1, 2, 0-1, 2, 0-1",
						"XXxX-, true, 2, 3, 0-1, 2, 0-1, 2, 0-1",
						"-XXxX, true, 2, 3, 1-2, 3, 0-2, 3, 1-2",
						"-XXxX-, true, 2, 3, 1-2, 3, 0-2, 3, 1-2",
						// Reluctance - adjacent
						"Xxx, true, 1, 3, 0, 1, 0, 1, 0",
						"Xxxx, true, 1, 3, 0, 1, 0, 1, 0",
						"Xxxx-, true, 1, 3, 0, 1, 0, 1, 0",
						"Xxx, true, 2, 3, 0-1, 2, 0-1, 2, 0-1",
						"Xxxx, true, 2, 3, 0-1, 2, 0-1, 2, 0-1",
						"Xxxx-, true, 2, 3, 0-1, 2, 0-1, 2, 0-1",
					})
					@DisplayName("verify reluctant expansion with multiple nodes [limited, discontinuous]")
					void testReluctantCompetitionDiscontinuous(String target,
							boolean adjacent,
							int min, int max, // arguments for 'Range' marker
							@IntervalArg Interval hits1, // reported hits for first node
							int hit2, // reported hit for second node
							@IntervalArg Interval visited1,  // all slots visited for first node
							@IntervalArg Interval visited2, // all slots visited for second node
							@IntervalArg Interval candidates1) { // slots marked as true for first node
						/*
						 * We expect NODE_1 to only proceed with consumption of slots
						 * while NODE_0 does not already match the next one.
						 */
						matcherTest()
						.root(ordered(adjacent,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), rangeReluctant(min, max, DISCONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(candidates1))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.result(result(0)
								.map(NODE_0, hits1)
								.map(NODE_1, hit2))
						.assertResult();
					}

					@Test
					@DisplayName("verify reluctant expansion with multiple nodes and matches")
					void testReluctantExpansion() {
						//TODO add discontinuous counterpart?
						final String target = "-XXxXXx-";
						matcherTest()
						.root(ordered(true,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), rangeReluctant(1, 2, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.target(target)
						.expectSuccess(4)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(Interval.of(0, 7))
								.hits(Interval.of(1, 6)))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(Interval.of(2, 7))
								.hits(3, 6))
						// First normal-sized match
						.result(result(0)
								.map(NODE_0, 1, 2)
								.map(NODE_1, 3))
						// Intermediate match that only allows first node to consume 1 slot
						.result(result(1)
								.map(NODE_0, 2)
								.map(NODE_1, 3))
						// Last normal-sized match
						.result(result(3)
								.map(NODE_0, 4, 5)
								.map(NODE_1, 6))
						// Final minimum-sized match
						.result(result(4)
								.map(NODE_0, 5)
								.map(NODE_1, 6))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}!>[X] in {0}")
					@CsvSource({
						"X, 0, 1, 0, 0",
						"X-, 0, 1, 0, 0",
						"-X, 0, 1, -, 0",
						"-X-, 0, 1, -, 0",

						"X-, 0, 2, 0, 0-1",
						"-X-, 0, 2, -, 0",
						"XX, 0, 2, 0-1, 0-1",
						"XXX, 0, 2, 0-1, 0-1",
						"XX-, 0, 2, 0-1, 0-1",
						"-XX-, 0, 2, -, 0",
						"-XXX, 0, 2, -, 0",

						"XXX, 0, 3, 0-2, 0-2",
						"XXXX, 0, 3, 0-2, 0-2",
						"XX-, 0, 3, 0-1, 0-2",
						"-XX-, 0, 3, -, 0",

						"X, 1, 2, 0, 0",
						"X-, 1, 2, 0, 0-1",
						"-X, 1, 2, 1, 0-1",
						"XX-, 1, 2, 0-1, 0-1",
						"-XX, 1, 2, 1-2, 0-2",
						"-X-, 1, 2, 1, 0-2",

						"XX, 2, 3, 0-1, 0-1",
						"XX-, 2, 3, 0-1, 0-2",
						"XXX, 2, 3, 0-2, 0-2",
						"-XX, 2, 3, 1-2, 0-2",
						"-XX-, 2, 3, 1-2, 0-3",
						"-XXX, 2, 3, 1-3, 0-3",
						"XXX-, 2, 3, 0-2, 0-2",
						"XXXX, 2, 3, 0-2, 0-2",
						"--XX, 2, 3, 2-3, 0-3",
						"XX--, 2, 3, 0-1, 0-2",

						"XXX, 3, 4, 0-2, 0-2",
						"XXXX, 3, 4, 0-3, 0-3",
						"XXXX-, 3, 4, 0-3, 0-3",
						"-XXX, 3, 4, 1-3, 0-3",
						"XXX-, 3, 4, 0-2, 0-3",

						"--XXXXX--, 2, 10, 2-6, 0-7",
						"--XXXXXXXXXX--, 2, 10, 2-11, 0-11",
						"--XXXXXXXXXXXX--, 2, 10, 2-11, 0-11",
					})
					@DisplayName("Node with a bounded multiplicity [possessive mode, single hit, limit]")
					void testPossessive(String target, int min, int max,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								rangePossessive(min, max, CONTINUOUS)))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}!^>[X] in {0}")
					@CsvSource({
						"X, 0, 1, {0}, 0",
						"X-, 0, 1, {0}, 0",
						"-X, 0, 1, -, 0",
						"-X-, 0, 1, -, 0",

						"X-, 0, 2, {0}, 0-1",
						"-X-, 0, 2, -, 0",
						"XX, 0, 2, {0;1}, 0-1",
						"XXX, 0, 2, {0;1}, 0-1",
						"XX-, 0, 2, {0;1}, 0-1",
						"X-X, 0, 2, {0;2}, 0-2",
						"-XX-, 0, 2, -, 0",
						"-XXX, 0, 2, -, 0",

						"XXX, 0, 3, {0;1;2}, 0-2",
						"XXXX, 0, 3, {0;1;2}, 0-2",
						"XX-, 0, 3, {0;1}, 0-2",
						"-XX-, 0, 3, -, 0",

						"X, 1, 2, {0}, 0",
						"X-, 1, 2, {0}, 0-1",
						"-X, 1, 2, {1}, 0-1",
						"XX-, 1, 2, {0;1}, 0-1",
						"X-X, 1, 2, {0;2}, 0-2",
						"-XX, 1, 2, {1;2}, 0-2",
						"-X-, 1, 2, {1}, 0-2",

						"XX, 2, 3, {0;1}, 0-1",
						"XX-, 2, 3, {0;1}, 0-2",
						"XXX, 2, 3, {0;1;2}, 0-2",
						"-XX, 2, 3, {1;2}, 0-2",
						"-XX-, 2, 3, {1;2}, 0-3",
						"-XXX, 2, 3, {1;2;3}, 0-3",
						"XXX-, 2, 3, {0;1;2}, 0-2",
						"XXXX, 2, 3, {0;1;2}, 0-2",
						"XX-X, 2, 3, {0;1;3}, 0-3",
						"X-XX, 2, 3, {0;2;3}, 0-3",
						"--XX, 2, 3, {2;3}, 0-3",
						"XX--, 2, 3, {0;1}, 0-3",

						"XXX, 3, 4, {0;1;2}, 0-2",
						"XXXX, 3, 4, {0;1;2;3}, 0-3",
						"XXXX-, 3, 4, {0;1;2;3}, 0-3",
						"-XXX, 3, 4, {1;2;3}, 0-3",
						"XXX-, 3, 4, {0;1;2}, 0-3",

						"--XXXXX--, 2, 10, {2;3;4;5;6}, 0-8",
						"--XXXXXXXXXX--, 2, 10, {2;3;4;5;6;7;8;9;10;11}, 0-11",
						"--XXXXXXXXXXXX--, 2, 10, {2;3;4;5;6;7;8;9;10;11}, 0-11",
					})
					@DisplayName("Node with a bounded multiplicity [possessive mode, single hit, limited, discontinuous]")
					void testPossessiveDiscontinnuous(String target, int min, int max,
							@IntArrayArg int[] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								rangePossessive(min, max, DISCONTINUOUS)))
						.modBuilder(LIMIT_SINGLE)
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(hits))
						.result(result(0)
								.map(NODE_0, hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}!>[X] in {0}")
					@CsvSource({
						"-, 1, 2, 0, -",
						"Y, 1, 2, 0, -",
						"-Y, 2, 3, 0, -",  // early abort by scan
						"-Y-, 2, 3, 0-1, -",
						"X-x-, 2, 3, 0-2, 0",
						"-X-, 2, 3, 0-2, 1",
					})
					@DisplayName("Mismatch with a bounded multiplicity [possessive mode]")
					void testPossessiveFail(String target, int min, int max,
							@IntervalArg Interval visited,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								rangePossessive(min, max, CONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(candidates))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}!^>[X] in {0}")
					@CsvSource({
						"-, 1, 2, 0, -",
						"Y, 1, 2, 0, -",
						"-Y, 2, 3, 0, -",  // early abort by scan
						"-Y-, 2, 3, 0-1, -",
						"X-x-, 2, 3, 0-3, 0",
						"-X-, 2, 3, 0-2, 1",
					})
					@DisplayName("Mismatch with a bounded multiplicity [possessive mode, discontinuous]")
					void testPossessiveFailDiscontinuous(String target, int min, int max,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X'))),
								rangePossessive(min, max, DISCONTINUOUS)))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited)
								.hits(target, visited, EQUALS_X))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}!>[X|x][x] in {0}")
					@CsvSource({
						"Xx, 1, 2, 0-1, -, 0-1",
						"XXX-, 1, 2, 0-3, 2-3, 0-2",

						"Xx, 1, 3, 0-1, -, 0-1",
						"XXx, 1, 3, 0-2, -, 0-2",
						"XXX-, 1, 3, 0-3, 3, 0-2",
						"XXx-, 1, 3, 0-3, 3, 0-2",

						"XXX-, 1, 4, 0-3, 3, 0-2",
						"XXx-, 1, 4, 0-3, 3, 0-2",
						"XXXx, 1, 4, 0-3, -, 0-3",
					})
					@DisplayName("Mismatch due to possessive consumption [ordered]")
					void testPossessiveFail2(String target, int min, int max,
							@IntervalArg Interval visited1,
							@IntervalArg Interval visited2,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(ordered(false,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), rangePossessive(min, max, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.target(target)
						.expectFail()
						// Underlying cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(candidates))
						// Underlying cache of second node
						.cache(cache(CACHE_1, false)
								.set(visited2)
								.window(target))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}!^>[X|x][x] in {0}")
					@CsvSource({
						"Xx, 1, 2, 0-1, -, 0-1",
						"XXX-, 1, 2, 0-3, 2-3, 0-2",

						"Xx, 1, 3, 0-1, -, 0-1",
						"XXx, 1, 3, 0-2, -, 0-2",
						"XXX-, 1, 3, 0-3, 3, 0-2",
						"XXx-, 1, 3, 0-3, 3, 0-2",

						"XXX-, 1, 4, 0-3, 3, 0-2",
						"XXx-, 1, 4, 0-3, 3, 0-2",
						"XXXx, 1, 4, 0-3, -, 0-3",
					})
					@DisplayName("Mismatch due to possessive consumption [ordered, discontinuous]")
					void testPossessiveFail2Discontinuous(String target, int min, int max,
							@IntervalArg Interval visited1,
							@IntervalArg Interval visited2,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(ordered(false,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), rangePossessive(min, max, DISCONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.target(target)
						.expectFail()
						// Underlying cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(candidates))
						// Underlying cache of second node
						.cache(cache(CACHE_1, false)
								.set(visited2)
								.window(target))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}!>[X|x][x] in {0}")
					@CsvSource({
						"Xx, 1, 2, 0-1, -, 0-1",
						"XXx, 1, 3, 0-2, -, 0-2",
						"XXX-, 1, 3, 0-3, 3, 0-2",
						"XXx-, 1, 3, 0-3, 3, 0-2",
						"XXx-x, 1, 3, 0-3, 3, 0-2",
					})
					@DisplayName("Mismatch due to possessive consumption [adjacent]")
					void testPossessiveFail3(String target, int min, int max,
							@IntervalArg Interval visited1,
							@IntervalArg Interval visited2,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(ordered(true,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), rangePossessive(min, max, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.target(target)
						.expectFail()
						// Underlying cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(candidates))
						// Underlying cache of second node
						.cache(cache(CACHE_1, false)
								.set(visited2)
								.window(target))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{1}..{2}!^>[X|x][x] in {0}")
					@CsvSource({
						"Xx, 1, 2, 0-1, -",
						"XXx, 1, 3, 0-2, -",
						"XXX-, 1, 3, 0-3, 3",
						"XXx-, 1, 3, 0-3, 3",
						"X-Xx-, 1, 3, 0-4, 4",
						"XX-x-, 1, 3, 0-4, 4",
						"XXx-x, 1, 3, 0-4, 3",
						"X-Xx-, 2, 3, 0-4, 4",
						"XX-x-, 2, 3, 0-4, 4",
						"XXx-x, 2, 3, 0-4, 3",
					})
					@DisplayName("Mismatch due to possessive consumption [adjacent, discontinuous]")
					void testPossessiveFail3Discontinuous(String target, int min, int max,
							@IntervalArg Interval visited1,
							@IntervalArg Interval visited2) {
						// 'Repetition' node sets minSize so that scan can abort early
						matcherTest()
						.root(ordered(true,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), rangePossessive(min, max, DISCONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x')))))
						.target(target)
						.expectFail()
						// Underlying cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(target, visited1, EQUALS_X_IC))
						// Underlying cache of second node
						.cache(cache(CACHE_1, false)
								.set(visited2)
								.window(target))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{3}..{4}!>[x|X][{1}] in {0}, adjacent={2}")
					@CsvSource({
						// Optional - ordered
						"Y, Y, false, 0, 1, {-}, {0}, {0}, {0}, -, false",
						"-Y, Y, false, 0, 1, {-;-}, {1;1}, {0-1}, {0-1}, -, true",
						"XY, Y, false, 0, 1, {0;-}, {1;1}, {0-1}, {1}, {0}, false",
						"XXY, Y, false, 0, 1, {0;1;-}, {2;2;2}, {0-2}, {1-2}, {0-1}, false",
						"XXY, Y, false, 0, 2, {0-1;1;-}, {2;2;2}, {0-2}, {2}, {0-1}, false",
						// Optional - adjacent
						"Y, Y, true, 0, 1, {-}, {0}, {0}, {0}, -, false",
						"-Y, Y, true, 0, 1, {-}, {1}, {0-1}, {0-1}, -, false",
						"XY, Y, true, 0, 1, {0;-}, {1;1}, {0-1}, {1}, {0}, false",
						"XXY, Y, true, 0, 1, {1;-}, {2;2}, {0-2}, {1-2}, {0-1}, false",
						// Expansion of size 1 to 2 - ordered
						"XY, Y, false, 1, 2, {0}, {1}, {0-1}, {1}, {0}, false",
						"XXY, Y, false, 1, 2, {0-1;1}, {2;2}, {0-2}, {2}, {0-1}, false",
						"XX-XX, X, false, 1, 2, {0-1;0-1;1;1}, {3;4;3;4}, {0-4}, {2-4}, {0-1;3-4}, false",
						// Expansion of size 1 to 2 - adjacent
						"XY, Y, true, 1, 2, {0}, {1}, {0-1}, {1}, {0}, false",
						"XXY, Y, true, 1, 2, {0-1;1}, {2;2}, {0-2}, {2}, {0-1}, false",
						// Expansion of size 1 to 3 - ordered
						"XY, Y, false, 1, 3, {0}, {1}, {0-1}, {1}, {0}, false",
						"XXY, Y, false, 1, 3, {0-1;1}, {2;2}, {0-2}, {2}, {0-1}, false",
						"XXX-X, X, false, 1, 3, {0-2;1-2;2}, {4;4;4}, {0-3}, {3-4}, {0-2}, false",
						"XX-XX, X, false, 1, 3, {0-1;0-1;1;1}, {3;4;3;4}, {0-4}, {2-4}, {0-1;3-4}, false", // we miss the 5. match due to possessive expansion
						"XXx-x, x, false, 1, 3, {0-2;1-2;2}, {4;4;4}, {0-3}, {3-4}, {0-2}, false",
						// Expansion of size 1 to 3 - adjacent
						"XXY, Y, true, 1, 3, {0-1;1}, {2;2}, {0-2}, {2}, {0-1}, false",
						"XXXX, X, true, 1, 3, {0-2}, {3}, {0-3}, {3}, {0-3}, false",
						// Expansion of size 2 to 3 - ordered
						"XXX-X, X, false, 2, 3, {0-2;1-2}, {4;4;}, {0-3}, {3-4}, {0-2}, false",
						"XX-XX, X, false, 2, 3, {0-1;0-1}, {3;4}, {0-2}, {2-4}, {0-1}, false",
					})
					@DisplayName("verify possessive expansion with multiple nodes")
					void testPossessiveCompetition(String target,
							char c2, // search symbol for second node
							boolean adjacent,
							int min, int max, // arguments for 'Range' marker
							@IntervalArrayArg Interval[] hits1, // reported hits for first node
							@IntervalArrayArg Interval[] hit2, // reported hits for second node
							@IntervalArrayArg Interval[] visited1,  // all slots visited for first node
							@IntervalArrayArg Interval[] visited2, // all slots visited for second node
							@IntervalArrayArg Interval[] candidates1,
							boolean allowDuplicates) {

						// Sanity check since we expect symmetric results here
						assertThat(hits1).hasSameSizeAs(hit2);

						/*
						 * We expect NODE_1 to aggressively consume slots with
						 * no regards for NODE_0, so that in contrast to reluctant mode
						 * we will miss some multi-match situations.
						 */
						matcherTest()
						.root(ordered(adjacent,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), rangePossessive(min, max, CONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp(c2)))))
						.target(target)
						.expectSuccess(hits1.length)
						.allowDuplicates(allowDuplicates)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hits(candidates1))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.results(hits1.length, (r,i) -> r
								.map(NODE_0, hits1[i])
								.map(NODE_1, hit2[i]))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: <{3}..{4}!^>[x|X][{1}] in {0}, adjacent={2}")
					@CsvSource({
						// Optional - ordered
						"Y, Y, false, 0, 1, {{}}, {0}, {0}, {0}, false",
						"-Y, Y, false, 0, 1, {{}{}}, {1;1}, {0-1}, {0-1}, true",
						"XY, Y, false, 0, 1, {{0}{}}, {1;1}, {0-1}, {1}, false",
						"XXY, Y, false, 0, 1, {{0}{1}{}}, {2;2;2}, {0-2}, {1-2}, false",
						"XXY, Y, false, 0, 2, {{0;1}{1}{}}, {2;2;2}, {0-2}, {2}, false",
						// Optional - adjacent
						"Y, Y, true, 0, 1, {{}}, {0}, {0}, {0}, false",
						"-Y, Y, true, 0, 1, {{}}, {1}, {0-1}, {0-1}, false",
						"XY, Y, true, 0, 1, {{0}{}}, {1;1}, {0-1}, {1}, false",
						"XXY, Y, true, 0, 1, {{1}{}}, {2;2}, {0-2}, {1-2}, false",
						// Expansion of size 1 to 2 - ordered
						"XY, Y, false, 1, 2, {{0}}, {1}, {0-1}, {1}, false",
						"XXY, Y, false, 1, 2, {{0;1}{1}}, {2;2}, {0-2}, {2}, false",
						"XX-XX, X, false, 1, 2, {{0;1}{0;1}{1;3}}, {3;4;4}, {0-4}, {2-4}, false",
						// Expansion of size 1 to 2 - adjacent
						"XY, Y, true, 1, 2, {{0}}, {1}, {0-1}, {1}, false",
						"XXY, Y, true, 1, 2, {{0;1}{1}}, {2;2}, {0-2}, {2}, false",
						// Expansion of size 1 to 3 - ordered
						"XY, Y, false, 1, 3, {{0}}, {1}, {0-1}, {1}, false",
						"XXY, Y, false, 1, 3, {{0;1}{1}}, {2;2}, {0-2}, {2}, false",
						"XXX-X, X, false, 1, 3, {{0;1;2}}, {4}, {0-4}, {3-4}, false",
						"XX-XX, X, false, 1, 3, {{0;1;3}}, {4}, {0-4}, {4}, false",
						"XX-XX-X, X, false, 1, 3, {{0;1;3}{0;1;3}{1;3;4}}, {4;6;6}, {0-6}, {4-6}, false",
						"XXx-x, x, false, 1, 3, {{0;1;2}}, {4}, {0-4}, {3-4}, false",
						// Expansion of size 1 to 3 - adjacent
						"XXY, Y, true, 1, 3, {{0;1}{1}}, {2;2}, {0-2}, {2}, false",
						"XXXX, X, true, 1, 3, {{0;1;2}}, {3}, {0-3}, {3}, false",
						"XX-XX, X, true, 1, 3, {{0;1;3}}, {4}, {0-4}, {4}, false",
						// Expansion of size 2 to 3 - ordered
						"XXX-X, X, false, 2, 3, {{0;1;2}}, {4;}, {0-4}, {3-4}, false",
						"XX-XX, X, false, 2, 3, {{0;1;3}}, {4}, {0-4}, {4}, false",
					})
					@DisplayName("verify possessive expansion with multiple nodes")
					void testPossessiveCompetitionDiscontinuous(String target,
							char c2, // search symbol for second node
							boolean adjacent,
							int min, int max, // arguments for 'Range' marker
							@IntMatrixArg int[][] hits1, // reported hits for first node
							@IntervalArrayArg Interval[] hit2, // reported hits for second node
							@IntervalArrayArg Interval[] visited1,  // all slots visited for first node
							@IntervalArrayArg Interval[] visited2, // all slots visited for second node
							boolean allowDuplicates) {

						// Sanity check since we expect symmetric results here
						assertThat((Object[])hits1).as("Different match counts").hasSameSizeAs(hit2);

						/*
						 * We expect NODE_1 to aggressively consume slots with
						 * no regards for NODE_0, so that in contrast to reluctant mode
						 * we will miss some multi-match situations.
						 */
						matcherTest()
						.root(ordered(adjacent,
								quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(ic_exp('X'))), rangePossessive(min, max, DISCONTINUOUS)),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp(c2)))))
						.target(target)
						.expectSuccess(hits1.length)
						.allowDuplicates(allowDuplicates)
						// Cache of first node
						.cache(cache(CACHE_0, false)
								.window(target)
								.set(visited1)
								.hitsForSet(target, EQUALS_X_IC))
						// Cache of second node
						.cache(cache(CACHE_1, false)
								.window(target)
								.set(visited2)
								.hits(hit2))
						.results(hits1.length, (r,i) -> r
								.map(NODE_0, hits1[i])
								.map(NODE_1, hit2[i]))
						.assertResult();
					}

				}
			}

			@Nested
			class WithQantifierAndMarker {

				@ParameterizedTest(name="{index}: {0} in {1} - {2} matches")
				@CsvSource({
					"'*[isAfter(2) && isBefore(5), $X]', XXX, 1, 2, 2",
					"'*[isAfter(2) && isBefore(5), $X]', XXXX, 1, 2-3, 2-3",
					"'*[isAfter(2) && isBefore(5), $X]', XXXXX, 1, 2-3, 2-3",
				})
				@DisplayName("all-quantifier and markers")
				void testUniversalQuantifierWithMarker(String query, String target, int matches,
						@IntervalArg Interval hits,
						@IntervalArg Interval visited) {
					// 'Repetition' node sets minSize so that scan can abort early
					expandingMatcherTest(query, target)
					.expectSuccess(matches)
					.queryConfig(QueryConfig.of(IqlNode.class))
					// Underlying cache of atom node
					.cache(cache(CACHE_0, false)
							.window(target)
							.set(visited)
							.hits(hits))
					.result(result(0)
							.map(NODE_0, hits))
					.assertResult();
				}
			}
		}

		@Nested
		class ForIqlSequence {

			@DisplayName("node arrangement UNORDERED")
			@Nested
			class WhenUnordered {

				@ParameterizedTest(name="{index}: [X][Y] in {0}")
				@CsvSource({
					"-",
					"--",

					"--X",
					"X--",
					"-X-",

					"--Y",
					"Y--",
					"-Y-",

					"-YY",
					"-XX",
					"Y-Y",
					"X-X",
					"YY-",
					"XX-",
				})
				@DisplayName("Two nodes with no matches")
				void testDualNodeFail(String target) {
					matcherTest()
					.root(unordered(false,
							IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
							IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y')))))
					.target(target)
					.expectFail()
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [X][Y] in {0}, hitX={1}, hitY={2}")
				@CsvSource({
					"XY,  0, 1",
					"X-Y,  0, 2",
					"-XY,  1, 2",
					"-X-Y, 1, 3",
					"--XY, 2, 3",
					"-XY-, 1, 2",
					"XY--, 0, 1",
					"X--Y, 0, 3",
				})
				@DisplayName("Two nodes at various positions")
				void testDualNodeHits(String target, int hitX, int hitY) {
					matcherTest()
					.root(unordered(false,
							IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
							IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y')))))
					.target(target)
					.expectSuccess(1)
					// scan won't use cache if content is only a simple node
					.cache(cache(CACHE_0, true)
							.window(0, target.length()-2)
							.hits(hitX))
					.cache(cache(CACHE_1, true)
							.window(hitX+1, target.length()-1)
							.hits(hitY))
					.result(result(0)
							.map(NODE_0, hitX)
							.map(NODE_1, hitY))
					.assertResult();
				}

				@ParameterizedTest(name="{index}: [X][Y] in {0}, hitX={1}, hitY={2}")
				@CsvSource({
					"-YX, 2, 1",
					"Y-X, 2, 0",
					"YX-, 1, 0",
				})
				@DisplayName("Two nodes at reversed positions")
				void testDualNodeHits2(String target, int hitX, int hitY) {
					matcherTest()
					.root(unordered(false,
							IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
							IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y')))))
					.target(target)
					.expectSuccess(1)
					// scan won't use cache if content is only a simple node
					.cache(cache(CACHE_0, true)
							.window(0, target.length()-2)
							.hits(hitX))
					.cache(cache(CACHE_1, true)
							.window(hitX+1, target.length()-1)
							.hits(hitY))
					.result(result(0)
							.map(NODE_1, hitY)
							.map(NODE_0, hitX))
					.assertResult();
				}
			}

			@DisplayName("node arrangement ORDERED")
			@Nested
			class WhenOrdered {

				@ParameterizedTest(name="{index}: ORDERED [X][Y] in {0}")
				@CsvSource({
					"-",
					"--",

					"--X",
					"X--",
					"-X-",

					"--Y",
					"Y--",
					"-Y-",

					"-YX",
					"Y-X",
					"YX-",
				})
				@DisplayName("Two nodes with no matches")
				void testDualNodeFail(String target) {
					matcherTest()
					.root(ordered(false,
							IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
							IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y')))))
					.target(target)
					.expectFail()
					.assertResult();
				}

				@ParameterizedTest(name="{index}: ORDERED [X][Y] in {0}, hitX={1}, hitY={2}")
				@CsvSource({
					"XY,  0, 1",
					"X-Y,  0, 2",
					"-XY,  1, 2",
					"-X-Y, 1, 3",
					"--XY, 2, 3",
					"-XY-, 1, 2",
					"XY--, 0, 1",
					"X--Y, 0, 3",
				})
				@DisplayName("Two nodes at various positions")
				void testDualNodeHits(String target, int hitX, int hitY) {
					matcherTest()
					.root(ordered(false,
							IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
							IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y')))))
					.target(target)
					.expectSuccess(1)
					// scan won't use cache if content is only a simple node
					.cache(cache(CACHE_0, true)
							.window(0, target.length()-2)
							.hits(hitX))
					.cache(cache(CACHE_1, true)
							.window(hitX+1, target.length()-1)
							.hits(hitY))
					.result(result(0)
							.map(NODE_0, hitX)
							.map(NODE_1, hitY))
					.assertResult();
				}

			}

			@DisplayName("node arrangement ADJACENT")
			@Nested
			class WhenAdjacent {

				@ParameterizedTest(name="{index}: ADJACENT [X][Y] in {0}")
				@CsvSource({
					"-",
					"--",
					"X-",
					"Y-",

					"--X",
					"X--",
					"X-Y",
					"-X-",

					"--Y",
					"Y--",
					"-Y-",

					"-YX",
					"Y-X",
					"YX-",
				})
				@DisplayName("Two nodes with no matches")
				void testDualNodeFail(String target) {
					matcherTest()
					.root(ordered(true,
							IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
							IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y')))))
					.target(target)
					.expectFail()
					.assertResult();
				}

				@ParameterizedTest(name="{index}: ADJACENT [X][Y] in {0}, hitX={1}, hitY={2}")
				@CsvSource({
					"XY,  0, 1",
					"-XY,  1, 2",
					"--XY, 2, 3",
					"-XY-, 1, 2",
					"XY--, 0, 1",
				})
				@DisplayName("Two nodes at various positions")
				void testDualNodeHits(String target, int hitX, int hitY) {
					matcherTest()
					.root(ordered(true,
							IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
							IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y')))))
					.target(target)
					.expectSuccess(1)
					.cache(cache(CACHE_0, true)
							.window(0, target.length()-2)
							.hits(hitX))
					.cache(cache(CACHE_1, true)
							.window(hitX+1)
							.hits(hitY))
					.result(result(0)
							.map(NODE_0, hitX)
							.map(NODE_1, hitY))
					.assertResult();
				}
			}
		}

		@Nested
		class ForIqlGrouping {

			@ParameterizedTest(name="{index}: [X][Y] in {0}")
			@CsvSource({
				"-",
				"X",
				"Y",
				"--",
				"X-",
				"-X",
				"Y-",
				"-Y",
				"-X-",
				"--Y",
				"YX",
				"Y-X",
			})
			@DisplayName("Node pair with no matches")
			void testDoubleNodeFail(String target) {
				matcherTest()
				.root(grouping(ordered(false,
						IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
						IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y'))))))
				.target(target)
				.expectFail()
				.assertResult();
			}

			@ParameterizedTest(name="{index}: [X][Y] in {0}")
			@CsvSource({
				"--",
				"X",
				"Y",
				"Z",
				"--",
				"X-",
				"-X",
				"Y-",
				"-Y",
				"Z-",
				"-Z",
				"XY-",
				"X-Z",
				"Y-X",
				"ZYX",
			})
			@DisplayName("Node triplet with no matches")
			void testTripleNodeFail(String target) {
				matcherTest()
				.root(grouping(ordered(false,
						IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
						IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y'))),
						IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Z')))
						)))
				.target(target)
				.expectFail()
				.assertResult();
			}

			@ParameterizedTest(name="{index}: [X][Y][Z] in {0}")
			@CsvSource({
				"XYZ, {0}, {1}, {2},  0, 1, 2,  {0}, {1}, {2}",
				"-XYZ, {1}, {2}, {3},  0-1, 2, 3,  {1}, {2}, {3}",
				"X-YZ, {0}, {2}, {3},  0-1, 1-2, 3,  {0}, {2}, {3}",
				"XY-Z, {0}, {1}, {3},  0-1, 1-2, 2-3,  {0}, {1}, {3}",
				"XYZ-, {0}, {1}, {2},  0-1, 1, 2,  {0}, {1}, {2}",

				"-X-Y-Z-, {1}, {3}, {5},  0-4, 2-5, 4-6,  {1}, {3}, {5}",
				"-X-Y-ZY-, {1}, {3}, {5},  0-5, 2-6, 4-7,  {1}, {3;6}, {5}",
				"-X-Y-XZY-, {1}, {3}, {6},  0-6, 2-7, 4-8,  {1;5}, {3;7}, {6}",

				"XXYZ, {0;1}, {2;2}, {3;3},  0-1, 2, 3,  {0-1}, {2}, {3}",
				"XYYZ, {0;0}, {1;2}, {3;3},  0, 1-2, 3,  {0}, {1-2}, {3}",
				"XYZZ, {0;0}, {1;1}, {2;3},  0, 1, 2-3,  {0}, {1}, {2-3}",

				"-X-Y-Z-X-Y-, {1}, {3}, {5},  0-8, 2-9, 4-10,  {1;7}, {3;9}, {5}",
				"-X-Y-Z-X-Z-, {1;1}, {3;3}, {5;9},  0-8, 2-9, 4-10,  {1;7}, {3}, {5;9}",
				"-X-Y-Z-X-Y-Z-, {1;1;1;7}, {3;3;9;9}, {5;11;11;11},  0-10, 2-11, 4-12,  {1;7}, {3;9}, {5;11}",
			})
			@DisplayName("Node triplet at various positions")
			void testTripleNodeHit(String target,
					@IntervalArrayArg Interval[] hits1, @IntervalArrayArg Interval[] hits2, @IntervalArrayArg Interval[] hits3,
					@IntervalArg Interval visited1, @IntervalArg Interval visited2, @IntervalArg Interval visited3,
					@IntervalArrayArg Interval[] candidates1, @IntervalArrayArg Interval[] candidates2, @IntervalArrayArg Interval[] candidates3) {
				assertThat(hits1).hasSameSizeAs(hits2);
				assertThat(hits1).hasSameSizeAs(hits3);

				matcherTest()
				.root(grouping(ordered(false,
						IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
						IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y'))),
						IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Z')))
						)))
				.target(target)
				.expectSuccess(hits1.length)
				// Cache for 3rd node
				.cache(cache(CACHE_2, true)
						.window(visited3)
						.hits(candidates3))
				// Cache for 2nd node
				.cache(cache(CACHE_1, true)
						.window(visited2)
						.hits(candidates2))
				// Cache for 1st node
				.cache(cache(CACHE_0, true)
						.window(visited1)
						.hits(candidates1))
				.results(hits1.length, (r, i) -> r
						.map(NODE_0, hits1[i])
						.map(NODE_1, hits2[i])
						.map(NODE_2, hits3[i]))
				.assertResult();
			}

			@Nested
			class WithQuantifier {

				@Nested
				class Negated {

					@ParameterizedTest(name="{index}: !'{[X]}' in {0}")
					@CsvSource({
						"X, {0}, 0",
						"-X, {1}, 0-1",
						"X-, {0}, 0",
						"-X-, {1}, 0-1",
						"X-X, {0;2}, 0",
					})
					@DisplayName("Mismatch for negated node")
					void testNegatedFail(String target, @IntArrayArg int[] hits,
							@IntervalArg Interval visitedAtom) {
						matcherTest()
						.root(quantify(grouping(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X')))),
								negated()))
						.target(target)
						.expectFail()
						// Underlying cache of atom node
						.cache(cache(CACHE_0, true)
								.window(visitedAtom)
								.hits(hits))
						.assertResult();
					}

					@ParameterizedTest(name="{index}: !'{[X]}' in {0}")
					@CsvSource({
						"-",
						"Y",
						"--",
						"-Y-",
					})
					@DisplayName("Negated node")
					void testNegatedSingle(String target) {
						matcherTest()
						.root(quantify(grouping(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X')))),
								negated()))
						.target(target)
						.expectSuccess(1)
						// Underlying cache of atom node
						.cache(cache(CACHE_0, true)
								.window(target))
						// Cache of the negated search
						.cache(cache(CACHE_1, true)
								.window(target)
								.hitsForWindow())
						.assertResult();
					}

					@ParameterizedTest(name="{index}: !'{[X][Y]}' in {0}")
					@CsvSource({
						"--, 0, -",
						"-X-, 0-1, 2",
						"-Y-, 0-1, -",
						"-YX-, 0-2, 3",
						"-Y-X, 0-2, -",
						"-XX-, 0-2, -",
					})
					@DisplayName("Negation of 2 nodes")
					void testNegatedDual(String target,
							@IntervalArg Interval visited1, @IntervalArg Interval visited2) {
						matcherTest()
						.root(quantify(grouping(ordered(false,
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y')))
								)),
								negated()))
						.target(target)
						.expectSuccess(1)
						// Underlying cache of first atom node
						.cache(cache(CACHE_0, true)
								.window(visited1)
								.hits(target, visited1, EQUALS_X))
						// Underlying cache of second atom node
						.cache(cache(CACHE_1, true)
								.window(visited2)
								.hits(target, visited2, EQUALS_Y))
						// Cache of the negated search
						.cache(cache(CACHE_2, true)
								.window(target)
								.hitsForWindow())
						.assertResult();
					}

				}

				@Nested
				class All {

				}

				@Nested
				class Exact {

				}

				@Nested
				class AtLeast {

				}

				@Nested
				class AtMost {

				}

				@Nested
				class Ranged {

				}
			}

		}

		@Nested
		class ForIqlElementDisjunction {

			@Nested
			class Simple {

				@ParameterizedTest(name="{index}: [X] or [Y] in {0}")
				@CsvSource({
					"-, -, -, {0}, {0}",
					"Z, -, -, {0}, {0}",
					"---, -, -, {0-2}, {0-2}",

					"X, {0}, {-}, {0}, -",
					"Y, {-}, {0}, {0}, {0}",

					"-X-, {1}, {-}, {0-2}, -",
					"-Y-, {-}, {1}, {0}, {0}",

					"XX, {0;1}, {-;-}, {0-1}, -",
					"YY, {-;-}, {0;1}, {0-1}, {0-1}",
					"XY, {0;-}, {-;1}, {0-1}, {1}",
					"YX, {-;1}, {0;-}, {0-1}, {0}",

					"X-X, {0;2}, {-;-}, {0-2}, {1}",
					"Y-Y, {-;-}, {0;2}, {0-2}, {0-2}",
					"X-Y, {0;-}, {-;2}, {0-2}, {1-2}",
					"Y-X, {-;2}, {0;-}, {0-2}, {0-1}",
				})
				@DisplayName("Binary node disjunction")
				void testBinaryOption(String target,
						@IntervalArrayArg Interval[] hits1, @IntervalArrayArg Interval[] hits2,
						@IntervalArrayArg Interval[] visited1, @IntervalArrayArg Interval[] visited2) {
					int len = Math.max(hits1.length,  hits2.length);
					matcherTest()
					.root(disjunction(
							IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
							IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y')))))
					.target(target)
					.expectMatches(len)
					.cache(cache(CACHE_0, true)
							.window(visited1)
							.hits(target, EQUALS_X))
					.cache(cache(CACHE_1, true)
							.window(visited2)
							.hits(target, EQUALS_Y))
					.results(len, (r, i) -> r
							.map(NODE_0, hits1[i])
							.map(NODE_1, hits2[i]))
					.assertResult();
				}

			}

		}
	}

	/**
	 * Test family for raw textual queries against full sequences.
	 * Primarily we test here that complex queries get parsed into the correct node configuration
	 * for the state machine in contrast to the tests above that check nodes and evaluation
	 * in isolation.
	 *
	 * <table border="1">
	 * <tr><th>&nbsp;</th><th>{@link IqlNode node}</th><th>{@link IqlGrouping grouping}</th>
	 * 		<th>{@link IqlSequence sequence}</th><th>{@link IqlElementDisjunction branch}</th><th>{@link IqlTreeNode tree}</th></tr>
	 * <tr><th>{@link IqlNode node}</th><td>-</td><td>{@link NodeInGrouping X}</td>
	 * 		<td>{@link NodeInSet X}</td><td>{@link NodeInBranch X}</td><<td>{@link NodeInTreeNode X}</td>/tr>
	 * <tr><th>{@link IqlGrouping grouping}</th><td>-</td><td>{@link GroupingInGrouping X}</td>
	 * 		<td>{@link GroupingInSequence X}</td><td>{@link GroupingInBranch X}</td><td>{@link GroupingInTreeNode X}</td></tr>
	 * <tr><th>{@link IqlSequence sequence}</th><td>-</td><td>{@link SequenceInGrouping X}</td>
	 * 		<td>-</td><td>{@link SequenceInBranch X}</td><td>{@link SequenceInTreeNode X}</td></tr>
	 * <tr><th>{@link IqlElementDisjunction branch}</th><td>-</td><td>{@link BranchInGrouping X}</td>
	 * 		<td>{@link BranchInSequence X}</td><td>{@link BranchInBranch X}</td><td>{@link BranchInTreeNode X}</td></tr>
	 * <tr><th>{@link IqlTreeNode tree}</th><td>-</td><td>{@link TreeNodeInGrouping X}</td>
	 * 		<td>{@link TreeNodeInSequence X}</td><td>{@link TreeNodeInBranch X}</td><td>{@link TreeNodeInTreeNode X}</td></tr>
	 * </table>
	 * <p>
	 * Row nested in column.
	 * <p>
	 * Note that for reasons of simplicity we switch result assertions to ignore the exact
	 * order of mappings via {@link ResultConfig#unordered()}. This way we don't have to
	 * split test cases needlessly or introduce additional parameters to control the assertion
	 * modus. Strictly speaking this results in a slight loss of control when it comes to
	 * verifying result mappings, but we also still verify the same overall mapping state.
	 */
	@Nested
	class ForRawQueries {

		/** Generate basic test config with expansion and basic result settings */
		private MatcherTestConfig rawQueryTest(String query, String target,
				int matches,
				// [node_id][match_id][hits]
				@IntMatrixArg int[][][] hits) {
			return expandingMatcherTest(query, target)
					.expectMatches(matches)
					.results(matches, hits);
		}

		@Nested
		class IllegalQueryConstructs {

			@ParameterizedTest
			@ValueSource(strings = {
					// Dummy nodes
					"[?]",
					"[*]",
					// Pure singular reluctance
					"{[?]}",
					// Pure expanded reluctance
					"{[*]}",
					// Plain quantification
					"<0..1?>[]",
					"<0..2?>[]",
					// Nested groupings
					"{{[?]}}",
					"{{[*]}}",
					// Sequence of dummy nodes
					"ORDERED [?][?]",
					"ORDERED [?][*]",
					// Grouping of dummy nodes
					"{ORDERED [?][?]}",
					"{ORDERED [?][*]}",
			})
			void testZeroWidthAssertion(String query) {

				IqlPayload payload = new QueryProcessor(Collections.emptySet()).processPayload("FIND "+query);
				assertThat(payload).as("No payload").isNotNull();
				assertThat(payload.getQueryType()).isEqualTo(QueryType.SINGLE_LANE);
				assertThat(payload.getLanes()).as("Missing lane").isNotEmpty();
				IqlLane lane = payload.getLanes().get(0);

				assignMappingIds(lane.getElement());

				StructurePattern.Builder builder = StructurePattern.builder();
				builder.source(lane);
				builder.id(1);
				RootContext rootContext = EvaluationContext.rootBuilder(QueryTestUtils.dummyCorpus())
						.addEnvironment(SharedUtilityEnvironments.all())
						.build();
				LaneContext context = rootContext.derive()
						.lane(lane)
						.build();
				builder.context(context);
				lane.getLimit().ifPresent(builder::limit);
				lane.getFlags().forEach(builder::flag);
				builder.initialBufferSize(10);
				builder.role(Role.SINGLETON);

				assertQueryException(QueryErrorCode.INCORRECT_USE, builder::build);
			}
		}

		@Nested
		class StandaloneNode {

			private final QueryConfig QUERY_CONFIG = QueryConfig.of(IqlNode.class);

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'[]', '', 0, -",
				"'[]', XYZ, 3, { {{0}{1}{2}} }",
			})
			@DisplayName("blank node")
			void testBlank(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				/* Note that "optional reluctant" nodes don't get added to a mapping
				 * unless the context forces an expansion.
				 */

				// Mandatory dummy node with reluctant expansion
				"'[+]', '', 0, -",
				"'[+]', X, 1, { {{0}} }",
				"'[+]', XY, 2, { {{0}{1}} }",
				"'[+]', XYZ, 3, { {{0}{1}{2}} }",
			})
			@DisplayName("dummy node")
			void testDummyNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				// Singleton markers
				"'[isAt(2), $X]', XXX, 1, { {{1}} }",
				"'[isNotAt(2), $X]', XXX, 2, { {{0}{2}} }",
				"'[isAfter(2), $X]', XXX, 1, { {{2}} }",
				"'[isBefore(2), $X]', XXX, 1, { {{0}} }",
				"'[isInside(2,4), $X]', XXXXX, 3, { {{1}{2}{3}} }",
				"'[isOutside(2,4), $X]', XXXXX, 2, { {{0}{4}} }",
				// Marker intersection
				"'[isFirst && isLast, $X]', X, 1, { {{0}} }",
				"'[isFirst && isLast, $X]', Y, 0, -",
				"'[isFirst && isLast, $X]', XX, 0, -",
				"'[isNotAt(2) && isLast, $X]', XX, 0, -",
				// Marker union
				"'[isFirst || isLast, $X]', X, 1, { {{0}} }",
				"'[isFirst || isLast, $X]', XX, 2, { {{0}{1}} }",
				"'[isFirst || isLast, $X]', XXX, 2, { {{0}{2}} }",
				"'[isAt(2) || isLast, $X]', XXX, 2, { {{1}{2}} }",
				// Complex marker nesting
				"'[isFirst || (isNotAt(3) && isBefore(4)), $X]', XXXX, 2, { {{0}{1}} }",
				//TODO add some of the other markers for completeness?
			})
			@DisplayName("node with markers")
			void testMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} [{2}] -> {3} matches")
			@CsvSource({
				"'[isRoot, $X]', YXX, *00, 0, -",
				"'[isRoot, $X]', XYY, *00, 1, { {{0}} }",
				"'[isRoot, $X]', XYX, *00, 1, { {{0}} }",
				"'[isRoot, $X]', YXY, 1*1, 1, { {{1}} }",
				"'[isRoot, $X]', YYX, 22*, 1, { {{2}} }",

				"'[isNotRoot, $X]', XYY, *00, 0, -",
				"'[isNotRoot, $X]', XYX, *00, 1, { {{2}} }",
				"'[isNotRoot, $X]', XXY, *00, 1, { {{1}} }",
				"'[isNotRoot, $X]', XYX, 1*1, 2, { {{0}{2}} }",
				"'[isNotRoot, $X]', XXY, 22*, 2, { {{0}{1}} }",

				"'[isLeaf, $X]', XYY, *00, 0, -",
				"'[isLeaf, $X]', YXX, *00, 2, { {{1}{2}} }",
				"'[isLeaf, $X]', YXX, *01, 1, { {{2}} }",

				"'[isNoLeaf, $X]', YXX, *00, 0, -",
				"'[isNoLeaf, $X]', XXX, *00, 1, { {{0}} }",
				"'[isNoLeaf, $X]', XXX, *01, 2, { {{0}{1}} }",

				"'[isIntermediate, $X]', XYY, *00, 0, -",
				"'[isIntermediate, $X]', YXX, *00, 0, -",
				"'[isIntermediate, $X]', YYX, *01, 0, -",
				"'[isIntermediate, $X]', YXX, *01, 1, { {{1}} }",
			})
			@DisplayName("node with frame-based markers")
			void testFrameMarkers(String query, String target, String tree, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.tree(tree)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<3+>[$X]', XX, 0, -",
				"'<3+>[$X]', XXX, 1, { {{0;1;2}} }",
			})
			@DisplayName("quantified node")
			void testQuantifiedNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<2>[isAfter(1),]', XX, 0, -",
				"'<2>[isAfter(1), $X]', XXX, 1, { {{1;2}} }",
			})
			@DisplayName("grouping of quantified node with markers")
			void testQuantifiedNodesWithMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.assertResult();
			}
		}

		/**
		 * Test special combinations of {@link IqlLane.MatchFlag} on queries.
		 */
		@Nested
		class ForMatchFlags {

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'CONSECUTIVE [$X][$Y]', XXYXY, 2, { {{0}{3}} {{2}{4}} }", // skip X at 1
				"'REVERSE CONSECUTIVE [$X][$Y]', XXYXY, 2, { {{3}{1}} {{4}{2}} }",
			})
			@DisplayName("consecutive matches")
			void testConsecutiveHorizontal(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.options(Option.DEFAULT_ORDERED_SEQUENCE)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'DISJOINT [$X][$Y]', XXYXY, 2, { {{0}{1}} {{2}{4}} }", // skip X at 3
				"'REVERSE DISJOINT [$X][$Y]', XXYXY, 2, { {{3}{1}} {{4}{2}} }",
			})
			@DisplayName("disjoint matches")
			void testDisjointHorizontal(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.options(Option.DEFAULT_ORDERED_SEQUENCE)
				.assertResult();
			}
		}

		/**
		 * {@link IqlNode} nested inside {@link IqlGrouping}
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>blank nodes</li>
		 * <li>dummy nodes</li>
		 * <li>marker on node</li>
		 * <li>quantifier on node</li>
		 * <li>multiple nodes</li>
		 * <li>quantifier on grouping</li>
		 * </ul>
		 */
		@Nested
		class NodeInGrouping {

			private final QueryConfig QUERY_CONFIG = QueryConfig.grouping(QueryConfig.of(IqlNode.class));

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{[]}', XYZ, 3, { {{0}{1}{2}} } ",
			})
			@DisplayName("grouping of blank node")
			void testBlank(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				// Mandatory dummy node with reluctant expansion
				"'{[+]}', X, 1, { {{0}} }",
				"'{[+]}', XY, 2, { {{0}{1}} }",
				"'{[+]}', XYZ, 3, { {{0}{1}{2}} }",

				// Cannot force expansion with non-adjacent sequence, we leave that to the NodeInSequence group

				//TODO we assume the "reluctance" property is sufficiently tested in $ForIqlNode$WithQuantifier$AtLeast
			})
			@DisplayName("grouping of dummy node(s)")
			void testDummyNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				// Singleton markers
				"'{[isAt(2), $X]}', XXX, 1, { {{1}} }",
				"'{[isNotAt(2), $X]}', XXX, 2, { {{0}{2}} }",
				"'{[isAfter(2), $X]}', XXX, 1, { {{2}} }",
				"'{[isBefore(2), $X]}', XXX, 1, { {{0}} }",
				"'{[isInside(2,4), $X]}', XXXXX, 3, { {{1}{2}{3}} }",
				"'{[isOutside(2,4), $X]}', XXXXX, 2, { {{0}{4}} }",
				// Marker intersection
				"'{[isFirst && isLast, $X]}', X, 1, { {{0}} }",
				"'{[isFirst && isLast, $X]}', Y, 0, -",
				"'{[isFirst && isLast, $X]}', XX, 0, -",
				"'{[isNotAt(2) && isLast, $X]}', XX, 0, -",
				// Marker union
				"'{[isFirst || isLast, $X]}', X, 1, { {{0}} }",
				"'{[isFirst || isLast, $X]}', XX, 2, { {{0}{1}} }",
				"'{[isFirst || isLast, $X]}', XXX, 2, { {{0}{2}} }",
				"'{[isAt(2) || isLast, $X]}', XXX, 2, { {{1}{2}} }",
				// Complex marker nesting
				"'{[isFirst || (isNotAt(3) && isBefore(4)), $X]}', XXXX, 2, { {{0}{1}} }",
				//TODO add some of the other markers for completeness?
			})
			@DisplayName("grouping of node(s) with markers")
			void testMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{<3+>[$X]}', XX, 0, -",
				"'{<3+>[$X]}', XXX, 1, { {{0;1;2}} }",
			})
			@DisplayName("grouping of quantified node(s)")
			void testQuantifiedNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{<2>[isAfter(1),]}', XX, 0, -",
				"'{<2>[isAfter(1), $X]}', XXX, 1, { {{1;2}} }",
			})
			@DisplayName("grouping of quantified node(s) with markers")
			void testQuantifiedNodesWithMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<3+>{[$X]}', XX, 0, -",
				"'<1..3>{[$X]}', X-Z, 1, { {{0}} }",
				"'<1..3>{[$X]}', XY, 1, { {{0}} }",
				"'<1..3>{[$X]}', XYXY, 2, { {{0}{2}} }",
				"'<1..3>{[$X]}', XY-XY, 2, { {{0}{3}} }",
				"'<1..3>{[$X]}', XY-XY--XY, 3, { {{0}{3}{7}} }",
				"'<1..3^>{[$X]}', XY-XY--XY, 3, { {{0;3;7}{3;7}{7}} }",
			})
			@DisplayName("quantified grouping of blank nodes")
			void testQuantifiedGrouping(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<1+>{[isFirst, $X]}', X, 1, { {{0}} }",
				"'<1+>{[isFirst, $X]}', XX, 1, { {{0}} }",
				"'<1+>{[isBefore(3), $X]}', X, 1, { {{0}} }",
				"'<1+>{[isBefore(3), $X]}', XX, 2, { {{0;1}{1}} }",
				"'<1+>{[isBefore(3), $X]}', XXX, 2, { {{0;1}{1}} }",
				"'<1+>{[isBefore(3), $X]}', XXXXXX, 2, { {{0;1}{1}} }",
				"'<1+>{[isLast, $X]}', X, 1, { {{0}} }",
				"'<1+>{[isLast, $X]}', XX, 1, { {{1}} }",
				"'<1+>{[isAfter(2), $X]}', X, 0, -",
				"'<1+>{[isAfter(2), $X]}', XX, 0, -",
				"'<1+>{[isAfter(2), $X]}', XXX, 1, { {{2}} }",
				"'<1+>{[isAfter(2), $X]}', XXXX, 2, { {{2;3}{3}} }",

				"'<2+>{[isFirst, $X]}', X, 0, -",
				"'<2+>{[isFirst, $X]}', XX, 0, -",
				"'<2+>{[isBefore(3), $X]}', X, 0, -",
				"'<2+>{[isBefore(3), $X]}', XX, 1, { {{0;1}} }",
				"'<2+>{[isBefore(3), $X]}', XXX, 1, { {{0;1}} }",
				"'<2+>{[isBefore(4), $X]}', XXXXXX, 2, { {{0;1;2}{1;2}} }",
				"'<2+>{[isLast, $X]}', X, 0, -",
				"'<2+>{[isLast, $X]}', XX, 0, -",
				"'<2+>{[isAfter(2), $X]}', X, 0, -",
				"'<2+>{[isAfter(2), $X]}', XX, 0, -",
				"'<2+>{[isAfter(2), $X]}', XXXX-, 1, { {{2;3}} }",
				"'<2+>{[isAfter(2), $X]}', XXXXX-XX, 3, { {{2;3;4}{3;4}{6;7}} }",

				"'<1..2>{[isOutside(2,6) || isAt(4), $X]}', X, 1, { {{0}} }",
				"'<1..2>{[isOutside(2,6) || isAt(4), $X]}', XXXX, 2, { {{0}{3}} }",
				"'<1..2>{[isOutside(2,6) || isAt(4), $X]}', XXXXXX, 2, { {{0}{3}} }",
				"'<1..2>{[isOutside(2,6) || isAt(4), $X]}', XXXXXXX, 3, { {{0}{6}{3}} }",
				"'<1..2>{[isAfter(2) && isBefore(5), $X]}', X, 0, -",
				"'<1..2>{[isAfter(2) && isBefore(5), $X]}', X, 0, -",
			})
			@DisplayName("quantified grouping of nodes with markers")
			void testQuantifiedGroupingWithMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<1+>{<2+>[isAfter(1), $X]}', X, 0, -",
				"'<1+>{<2+>[isAfter(1), $X]}', XXY, 0, -",
				"'<1+>{<2+>[isAfter(1), $X]}', XXXY, 1, { {{1;2}} }",
				"'<1+>{<2+>[isAfter(1), $X]}', XXXXXY-, 3, { {{1;2;3;4}{2;3;4}{3;4}} }",
				// outer quantifier only reinforces existential quantification, so group search is exhaustive
				"'<1>{<2+>[isAfter(1), $X]}', XXXXYY-, 2, { {{1;2;3}{2;3}} }",
				// nodes within quantified grouping are reduced to "find" scan
				"'<1+>{<2+>[isAfter(1), $X]}', XXXXYY-, 2, { {{1;2;3}{2;3}} }",

				"'<2+>{<2+>[isAfter(1), $X]}', XXXY, 0, -",
				"'<2+>{<2..3>[isAfter(1), $X]}', XXXXXXY, 1, { {{1;2;3;4;5}} }",
			})
			@DisplayName("quantified grouping of quantified nodes with markers")
			void testQuantifiedGroupingWithQuantifiedNodesWithMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}
		}

		/**
		 * {@link IqlNode} nested inside {@link IqlSequence}
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>blank nodes</li>
		 * <li>dummy nodes</li>
		 * <li>marker on node</li>
		 * <li>quantifier on node</li>
		 * <li>multiple nodes</li>
		 * <li>arrangement on sequence</li>
		 * </ul>
		 * <p>
		 * Note that we always need at least {@code 2} nodes in a sequence
		 * to force the creation of a {@link IqlSequence} wrapper!
		 */
		@Nested
		class NodeInSequence {

			private QueryConfig sequenceConfig(String query) {
				int length = 0;
				for (int i = 0; i < query.length(); i++) {
					if(query.charAt(i) == '[') length++;
				}
				return QueryConfig.sequence(IntStream.range(0, length)
								.mapToObj(i -> QueryConfig.of(IqlNode.class))
								.toArray(QueryConfig[]::new));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'ORDERED [][]', XX, 1, { {{0}} {{1}} }",
				"'ORDERED [][]', XXX, 3, { {{0}{0}{1}} {{1}{2}{2}} }",
				"'ORDERED [][][]', XXX, 1, { {{0}} {{1}} {{2}} }",
				"'ORDERED [][][][]', XXXX, 1, { {{0}} {{1}} {{2}} {{3}} }",
			})
			@DisplayName("set of blank nodes")
			void testBlank(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(sequenceConfig(query))
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				/* Note that "optional reluctant" nodes don't get added to a mapping
				 * unless the context forces an expansion.
				 */

				// Mandatory node with following optional
				"'ORDERED [][?]', XY, 2, { {{0}{1}} {{}{}} }, false",
				"'ORDERED [][?]', XYZ, 4, { {{0}{0}{1}{2}} {{}{}{}{}} }, true",

				// Mandatory node with following optional expansion
				"'ORDERED [][*]', XY, 2, { {{0}{1}} {{}{}} }, false",
				"'ORDERED [][*]', XYZ, 4, { {{0}{0}{1}{2}} {{}{}{}{}} }, true",

				// Mandatory node with following dummy node with reluctant expansion
				"'ORDERED [][+]', XY, 1, { {{0}} {{1}} }, false",
				"'ORDERED [][+]', XYZ, 3, { {{0}{0}{1}} {{1}{2}{2}} }, false",

				// Mandatory node after optional
				"'ORDERED [?][]', XY, 3, { {{}{}{}} {{0}{1}{1}} }, true",
				"'ORDERED [?][]', XYZ, 6, { {{}{}{}{}{}{}} {{0}{1}{2}{1}{2}{2}} }, true",

				// Mandatory node after optional expansion
				"'ORDERED [*][]', XY, 3, { {{}{}{}} {{0}{1}{1}} }, true",
				"'ORDERED [*][]', XYZ, 6, { {{}{}{}{}{}{}} {{0}{1}{2}{1}{2}{2}} }, true",

				// Mandatory node after dummy node with reluctant expansion
				"'ORDERED [+][]', XY, 1, { {{0}} {{1}} }, false",
				"'ORDERED [+][]', XYZ, 3, { {{0}{0}{1}} {{1}{2}{2}} }, false",

				// Mandatory nodes surrounding intermediate optional
				"'ORDERED [][?][]', XY, 1, { {{0}} {{}} {{1}} }, false",
				"'ORDERED [][?][]', XYZ, 4, { {{0}{0}{0}{1}} {{}{}{}{}} {{1}{2}{2}{2}} }, true",

				// Mandatory nodes surrounding intermediate optional expansion
				"'ORDERED [][*][]', XY, 1, { {{0}} {{}} {{1}} }, false",
				"'ORDERED [][*][]', XYZ, 4, { {{0}{0}{0}{1}} {{}{}{}{}} {{1}{2}{2}{2}} }, true",

				// Mandatory nodes surrounding intermediate dummy node with reluctant expansion
				"'[][+][]', XY, 0, -, false",
				"'ORDERED [][+][]', XYZ, 1, { {{0}} {{1}} {{2}} }, false",
			})
			@DisplayName("set of dummy nodes")
			void testDummyNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits,
					boolean allowDuplicates) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(sequenceConfig(query))
				.promote(true)
				.allowDuplicates(allowDuplicates)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'[isFirst,$X][isLast,$X]', XX, 1, { {{0}} {{1}} }",
				"'[isFirst,$X][isLast,$X]', XXX, 1, { {{0}} {{2}} }",
				"'[isFirst,$X][isLast,$X]', XX-, 0, -",
				"'[isAfter(1),$X][isLast,$X]', XXX, 1, { {{1}} {{2}} }",
				"'[isAfter(1),$X][isLast,$X]', XXXX, 2, { {{1}{2}} {{3}{3}} }",
				"'[isFirst,$X][isInside(3,5),$X][isLast,$X]', XX-XX-XX, 2, { {{0}{0}} {{3}{4}} {{7}{7}} }",
			})
			@DisplayName("set of nodes with markers")
			void testMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(sequenceConfig(query))
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<2+>[$X]<2..3>[$Y][$Z]', XX, 0, -",
				"'<2+>[$X]<2..3>[$Y][$Z]', XXYY, 0, -",
				"'<2+>[$X]<2..3>[$Y][$Z]', XXYYZ, 1, { {{0;1}} {{2;3}} {{4}} }",
				"'<2+>[$X]<2..3>[$Y][$Z]', XXXXYYZ, 3, { {{0;1;2;3}{1;2;3}{2;3}} {{4;5}{4;5}{4;5}} {{6}{6}{6}} }",
				"'<2+>[$X]<2..3>[$Y][$Z]', XX-YY-Z, 1, { {{0;1}} {{3;4}} {{6}} }",
				"'<2+>[$X]<2..3>[$Y][$Z]', XXYYYZ, 2, { {{0;1}{0;1}} {{2;3;4}{3;4}} {{5}{5}} }",
				"'<2+>[$X]<2..3>[$Y][$Z]', XXYYYYZ, 3, { {{0;1}{0;1}{0;1}} {{2;3;4}{3;4;5}{4;5}} {{6}{6}{6}} }",
			})
			@DisplayName("set of quantified nodes")
			void testQuantifiedNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(sequenceConfig(query))
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<2+>[isAfter(1),$X]<2..3>[$Y][$Z]', XXYYZ, 0, -",
				"'<2+>[isAfter(1),$X]<2..3>[$Y][$Z]', XXXYYZ, 1, { {{1;2}} {{3;4}} {{5}} }",
				"'<2+>[isAfter(1),$X]<2..3>[$Y][isBefore(-1),$Z]', XXXYYZ, 0, -",
				"'<2+>[isAfter(1),$X]<2..3>[$Y][isBefore(-1),$Z]', XXXYYZZ, 1, { {{1;2}} {{3;4}} {{5}} }",
				"'<2+>[isAfter(1),$X]<2..3>[$Y][isBefore(-1),$Z]', XXXXYYYZZ, 4, { {{1;2;3}{1;2;3}{2;3}{2;3}} {{4;5;6}{5;6}{4;5;6}{5;6}} {{7}{7}{7}{7}} }",
			})
			@DisplayName("set of quantified nodes with markers")
			void testQuantifiedNodesWithmarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(sequenceConfig(query))
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'ADJACENT ORDERED [][]', XYZ, 2, { {{0}{1}} {{1}{2}} }",
				"'ADJACENT ORDERED [][][]', XYZ, 1, { {{0}} {{1}} {{2}} }",
				"'ADJACENT [$X][$Y]', XY, 1, { {{0}} {{1}} }",
				"'ADJACENT [$X][$Y]', XXY, 1, { {{1}} {{2}} }",
				"'ADJACENT [$X][$Y][$Z]', XYZ, 1, { {{0}} {{1}} {{2}} }",
				"'ADJACENT [$X][$Y][$Z]', XYYZ, 0, -",
				"'ADJACENT ORDERED [$X][$Y][$Z]', XYYZXXYZZ, 1, { {{5}} {{6}} {{7}} }",

				// Dummy nodes and forcing reluctant expansion

				// Mandatory node after optional
				"'ADJACENT ORDERED [?][]', XY, 2, { {{}{}} {{0}{1}} }",
				"'ADJACENT ORDERED [?][]', XYZ, 3, { {{}{}{}} {{0}{1}{2}} }",
				"'ADJACENT ORDERED [?][$Y]', XYZ, 2, { {{0}{}} {{1}{1}} }",
				"'ADJACENT ORDERED [?][$Y]', XXYZ, 2, { {{1}{}} {{2}{2}} }",

				// Mandatory node after optional expansion
				"'ADJACENT ORDERED [*][]', XY, 2, { {{}{}} {{0}{1}} }",
				"'ADJACENT ORDERED [*][]', XYZ, 3, { {{}{}{}} {{0}{1}{2}} }",
				"'ADJACENT ORDERED [*][$Y]', XYZ, 2, { {{0}{}} {{1}{1}} }",
				"'ADJACENT ORDERED [*][$Y]', XXYZ, 3, { {{0;1}{1}{}} {{2}{2}{2}} }",

				// Mandatory node after dummy node with reluctant expansion
				"'ADJACENT ORDERED [+][]', XY, 1, { {{0}} {{1}} }",
				"'ADJACENT ORDERED [+][]', XYZ, 2, { {{0}{1}} {{1}{2}} }",
				"'ADJACENT [+][$Y]', XYZ, 2, { {{0}{2}} {{1}{1}} }",
				"'ADJACENT ORDERED [+][$Y]', XXYZ, 2, { {{0;1}{1}} {{2}{2}} }",

				// Mandatory nodes surrounding intermediate optional
				"'ADJACENT ORDERED [][?][]', XY, 1, { {{0}} {{}} {{1}} }",
				"'ADJACENT ORDERED [][?][]', XYZ, 2, { {{0}{1}} {{}{}} {{1}{2}} }",
				"'ADJACENT ORDERED [$X][?][$Y]', XY, 1, { {{0}} {{}} {{1}} }",
				"'ADJACENT ORDERED [$X][?][$Y]', X-Y, 1, { {{0}} {{1}} {{2}} }",
				"'ADJACENT [$X][?][$Y]', X--Y, 0, -",

				// Mandatory nodes surrounding intermediate optional expansion
				"'ADJACENT ORDERED [][*][]', XY, 1, { {{0}} {{}} {{1}} }",
				"'ADJACENT ORDERED [][*][]', XYZ, 2, { {{0}{1}} {{}{}} {{1}{2}} }",
				"'ADJACENT ORDERED [$X][*][$Y]', XY, 1, { {{0}} {{}} {{1}} }",
				"'ADJACENT ORDERED [$X][*][$Y]', X-Y, 1, { {{0}} {{1}} {{2}} }",
				"'ADJACENT ORDERED [$X][*][$Y]', X--Y, 1, { {{0}} {{1;2}} {{3}} }",

				// Mandatory nodes surrounding intermediate dummy node with reluctant expansion
				"'ADJACENT [][+][]', XY, 0, -",
				"'ADJACENT ORDERED [][+][]', XYZ, 1, { {{0}} {{1}} {{2}} }",
				"'ADJACENT [$X][+][$Y]', XY, 0, -",
				"'ADJACENT [$X][+][$Y]', X-Y, 1, { {{0}} {{1}} {{2}} }",
				"'ADJACENT ORDERED [$X][+][$Y]', X--Y, 1, { {{0}} {{1;2}} {{3}} }",
			})
			@DisplayName("set with explicit arrangement")
			void testArrangement(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(sequenceConfig(query))
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'ADJACENT [isFirst,$X][isLast,$X]', XX, 1, { {{0}} {{1}} }",
				"'ADJACENT [isFirst,$X][isLast,$X]', XXX, 0, -",
				"'ADJACENT [isFirst,$X][isLast,$X]', XX-, 0, -",
				"'ADJACENT [isAfter(1),$X][isLast,$X]', XXX, 1, { {{1}} {{2}} }",
				"'ADJACENT [isAfter(1),$X][isLast,$X]', XXXX, 1, { {{2}} {{3}} }",
				"'ADJACENT [isFirst,$X][isInside(2,5),$X][isLast,$X]', XX-XX-XX, 0, -",
				//TODO complete with some more edge cases
			})
			@DisplayName("set with explicit arrangement and nodes with markers")
			void testArrangementWithMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(sequenceConfig(query))
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'ADJACENT [isFirst,$X]<2+>[isInside(2,5),$X][isLast,$X]', XX-XX-XX, 0, -",
				"'ADJACENT [isFirst,$X]<2+>[isInside(2,5),$Y][isLast,$Z]', XYYYYZ, 1, { {{0}} {{1;2;3;4}} {{5}} }",
				"'ADJACENT <1..3>[$X][*][isAfter(4),$Y]<2+>[$Z]', XYZZ, 0, -",
				"'ADJACENT ORDERED <1..3>[$X][*][isAfter(4),$Y]<2+>[$Z]', ---XYZZ, 1, { {{3}} {-} {{4}} {{5;6}} }",
				"'ADJACENT ORDERED <1..3>[$X][*][isAfter(4),$Y]<2+>[$Z]', X---YZZ, 1, { {{0}} {-} {{4}} {{5;6}} }",
				"'ADJACENT ORDERED <1..3>[$X][*][isAfter(4),$Y]<2+>[$Z]', -XXXYZZ, 3, { {{1;2;3}{2;3}{3}} {-} {{4}{4}{4}} {{5;6}{5;6}{5;6}} }",
				"'ADJACENT ORDERED <1..3>[$X][*][isAfter(4),$Y]<2+>[$Z]', XXXXYZZ, 4, { {{0;1;2}{1;2;3}{2;3}{3}} {-} {{4}{4}{4}{4}} {{5;6}{5;6}{5;6}{5;6}} }",
			})
			@DisplayName("set with explicit arrangement and quantified nodes with markers")
			void testArrangementWithQuantifiedNodesWithMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(sequenceConfig(query))
				.assertResult();
			}

		}

		/**
		 * {@link IqlNode} nested inside {@link IqlElementDisjunction}
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>dummy nodes</li>
		 * <li>marker on node</li>
		 * <li>quantifier on node</li>
		 * <li>multiple nodes</li>
		 * <li>"nested" disjunction</li>
		 * </ul>
		 */
		@Nested
		class NodeInBranch {

			private QueryConfig branchConfig(int count) {
				return QueryConfig.disjunction(IntStream.range(0, count)
								.mapToObj(i -> QueryConfig.of(IqlNode.class))
								.toArray(QueryConfig[]::new));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				// Not too many variations possible here
				"'[] or []', X, 2, { {{0}{}} {{}{0}} }",
				"'[] or []', XX, 4, { {{0}{}{1}{}} {{}{0}{}{1}} }",
			})
			@DisplayName("disjunction of blank nodes")
			void testBlank(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(branchConfig(2))
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				// Same as with blanks: not too much variation
				"'[?] or [?]', X, 2, { {{}{}} {{}{}} }",
				"'[?] or [?]', XX, 4, { {{}{}{}{}} {{}{}{}{}} }",
				"'[*] or [*]', X, 2, { {{}{}} {{}{}} }",
				"'[*] or [*]', XX, 4, { {{}{}{}{}} {{}{}{}{}} }",
				"'[+] or [+]', X, 2, { {{0}{}} {{}{0}} }",
				"'[+] or [+]', XX, 4, { {{0}{}{1}{}} {{}{0}{}{1}} }",
			})
			@DisplayName("disjunction of dummy nodes")
			void testDummyNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(branchConfig(2))
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'[isFirst,$X] or [isLast,$X]', X, 2, { {{0}{}} {{}{0}} }",
				"'[isFirst,$X] or [isLast,$Y]', X, 1, { {{0}} {{}} }",
				"'[isFirst,$X] or [isLast,$X]', XX, 2, { {{0}{}} {{}{1}} }",
				"'[isBefore(3),$X] or [isAfter(4),$X]', XXXXX, 3, { {{0}{1}{}} {{}{}{4}} }",
				"'[isBefore(3),$X] or [isAfter(4),$X]', X-XXXX, 3, { {{0}{}{}} {{}{4}{5}} }",
				"'[isBefore(3) || isAfter(4),$X] or [isLast,$X]', XXXXX, 4, { {{0}{1}{4}{}} {{}{}{}{4}} }",
			})
			@DisplayName("disjunction of nodes with markers")
			void testMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(branchConfig(2))
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'[$X] or <2+!>[$X]', X, 1, { {{0}} {{}} }",
				"'[$X] or <2+>[$X]', XX, 3, { {{0}{}{1}} {{}{0;1}{}} }",
				"'[$X] or <2+!>[$X]', XX, 3, { {{0}{}{1}} {{}{0;1}{}} }",
				"'[$X] or <2+?>[$X]', XX, 3, { {{0}{}{1}} {{}{0;1}{}} }",
				"'[$X] or <2+>[$X]', XXX, 5, { {{0}{}{1}{}{2}} {{}{0;1;2}{}{1;2}{}} }",
				//TODO add more variation, especially branches that fail
			})
			@DisplayName("disjunction of quantified nodes")
			void testQuantifiedNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(branchConfig(2))
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<2+>[isNotAt(1), $X] or [isLast,$Y] or [isFirst,$Z]', 3, X, 0, -",
				"'<2+>[isNotAt(1), $X] or [isLast,$Y] or [isFirst,$Z]', 3, Z, 1, { {{}} {{}} {{0}} }",
				"'<2+>[isNotAt(1), $X] or [isLast,$Y] or [isFirst,$Z]', 3, Y, 1, { {{}} {{0}} {{}} }",
				"'<2+>[isNotAt(1), $X] or [isLast,$Y] or [isFirst,$Z]', 3, -XX, 1, { {{1;2}} {{}} {{}} }",
				"'<2+>[isNotAt(1), $X] or [isLast,$Y] or [isFirst,$Z]', 3, -XXX, 2, { {{1;2;3}{2;3}} {{}{}} {{}{}} }",
				"'<2+>[isNotAt(1), $X] or [isLast,$Y] or [isFirst,$Z]', 3, ZXX, 2, { {{}{1;2}} {{}{}} {{0}{}} }",
				"'<2+>[isNotAt(1), $X] or [isLast,$Y] or [isFirst,$Z]', 3, ZY, 2, { {{}{}} {{}{1}} {{0}{}} }",
				"'<2+>[isNotAt(1), $X] or <2..3>[isBefore(5),$Y]', 2, X, 0, -",
				"'<2+>[isNotAt(1), $X] or <2..3>[isBefore(5),$Y]', 2, XY, 0, -",
				"'<2+>[isNotAt(1), $X] or <2..3>[isBefore(5),$Y]', 2, X---YY, 0, -",
				"'<2+>[isNotAt(1), $X] or <2..3>[isBefore(5),$Y]', 2, YY, 1, { {{}} {{0;1}} }",
			})
			@DisplayName("disjunction of quantified nodes with markers")
			void testQuantifiedNodesWithMarkers(String query, int count, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(branchConfig(count))
				.assertResult();
			}
		}

		/**
		 * {@link IqlNode} nested inside {@link IqlTreeNode}
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>blank node</li>
		 * <li>dummy node</li>
		 * <li>quantifier on node</li>
		 * <li>markers on node</li>
		 * <li>tree markers on node</li>
		 * </ul>
		 */
		@Nested
		class NodeInTreeNode {

			private final QueryConfig QUERY_CONFIG = QueryConfig.tree(QueryConfig.of(IqlNode.class));

			@ParameterizedTest(name="{index}: {0} in {1} [{2}] -> {3} matches")
			@CsvSource({
				"'[[]]', X, *, 0, -",
				"'[[]]', XX, *0, 1, { {{0}} {{1}} }",
				"'[[]]', XX, 1*, 1, { {{1}} {{0}} }",
				"'[[]]', XXX, *00, 2, { {{0}{0}} {{1}{2}} }",
				"'[[]]', XXX, *01, 2, { {{0}{1}} {{1}{2}} }",
				"'[[]]', XXX, *20, 2, { {{0}{2}} {{2}{1}} }",
				"'[[]]', XXX, 1*1, 2, { {{1}{1}} {{0}{2}} }",
				"'[[]]', XXX, 2*1, 2, { {{1}{2}} {{2}{0}} }",
				"'[[]]', XXX, 1*0, 2, { {{0}{1}} {{2}{0}} }",
				"'[[]]', XXXX, *012, 3, { {{0}{1}{2}} {{1}{2}{3}} }",
			})
			@DisplayName("blank child node")
			void testBlank(String query, String target, String tree, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.tree(tree)
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} [{2}] -> {3} matches")
			@CsvSource({
				// Optional single child
				"'[[?]]', X, *, 1, false, { {{0}} }",
				"'[[?]]', XX, *0, 2, false, { {{0}{1}} {-} }",
				"'[[?]]', XX, 1*, 2, false, { {{0}{1}} {-} }",
				"'[[?]]', XXX, *00, 4, true, { {{0}{0}{1}{2}} {-} }",
				"'[[?]]', XXX, *01, 3, false, { {{0}{1}{2}} {-} }",
				"'[[?]]', XXX, *20, 3, false, { {{0}{1}{2}} {-} }",
				"'[[?]]', XXX, 1*1, 4, true, { {{0}{1}{1}{2}} {-} }",
				"'[[?]]', XXX, 2*1, 3, false, { {{0}{1}{2}} {-} }",
				"'[[?]]', XXX, 1*0, 3, false, { {{0}{1}{2}} {-} }",
				"'[[?]]', XXX, 22*, 4, true, { {{0}{1}{2}{2}} {-} }",
				"'[[?]]', XXX, 20*, 3, false, { {{0}{1}{2}} {-} }",
				"'[[?]]', XXX, 12*, 3, false, { {{0}{1}{2}} {-} }",

				// Mandatory child with  expansion
				"'[[+]]', X, *, 0, false, -",
				"'[[+]]', XX, *0, 1, false, { {{0}} {{1}} }",
				"'[[+]]', XX, 1*, 1, false, { {{1}} {{0}} }",
				"'[[+]]', XXX, *00, 2, false, { {{0}{0}} {{1}{2}} }",
				"'[[+]]', XXX, *01, 2, false, { {{0}{1}} {{1}{2}} }",
				"'[[+]]', XXX, *20, 2, false, { {{0}{2}} {{2}{1}} }",
				"'[[+]]', XXX, 1*1, 2, false, { {{1}{1}} {{0}{2}} }",
				"'[[+]]', XXX, 2*1, 2, false, { {{1}{2}} {{2}{0}} }",
				"'[[+]]', XXX, 1*0, 2, false, { {{0}{1}} {{2}{0}} }",
				"'[[+]]', XXX, 22*, 2, false, { {{2}{2}} {{0}{1}} }",
				"'[[+]]', XXX, 20*, 2, false, { {{0}{2}} {{1}{0}} }",
				"'[[+]]', XXX, 12*, 2, false, { {{1}{2}} {{0}{1}} }",

				// Optional child with expansion
				"'[[*]]', X, *, 1, false, { {{0}} }",
				"'[[*]]', XX, *0, 2, false, { {{0}{1}} {-} }",
				"'[[*]]', XX, 1*, 2, false, { {{0}{1}} {-} }",
				"'[[*]]', XXX, *00, 4, true, { {{0}{0}{1}{2}} {-} }",
				"'[[*]]', XXX, *01, 3, false, { {{0}{1}{2}} {-} }",
				"'[[*]]', XXX, *20, 3, false, { {{0}{1}{2}} {-} }",
				"'[[*]]', XXX, 1*1, 4, true, { {{0}{1}{1}{2}} {-} }",
				"'[[*]]', XXX, 2*1, 3, false, { {{0}{1}{2}} {-} }",
				"'[[*]]', XXX, 1*0, 3, false, { {{0}{1}{2}} {-} }",
				"'[[*]]', XXX, 22*, 4, true, { {{0}{1}{2}{2}} {-} }",
				"'[[*]]', XXX, 20*, 3, false, { {{0}{1}{2}} {-} }",
				"'[[*]]', XXX, 12*, 3, false, { {{0}{1}{2}} {-} }",
			})
			@DisplayName("dummy child node")
			void testDummyNode(String query, String target, String tree,
					int matches, boolean allowDuplicates,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.tree(tree)
				.promote(true)
				.allowDuplicates(allowDuplicates)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} [{2}] -> {3} matches")
			@CsvSource({
				"'[$X <2+>[$Y]]', XY, *0, 0, -",
				"'[$X <2+>[$Y]]', XYY, *00, 1, { {{0}} {{1;2}} }",
				"'[$X <2+>[$Y]]', XYYYY, *0000, 3, { {{0}{0}{0}} {{1;2;3;4}{2;3;4}{3;4}} }",
				"'[$X <2+>[$Y]]', XYY-YY, *00000, 2, { {{0}{0}} {{1;2}{4;5}} }",
				"'[$X <2+^>[$Y]]', XYY-YY, *00000, 3, { {{0}{0}{0}} {{1;2;4;5}{2;4;5}{4;5}} }",
				"'[$X <3+^>[$Y]]', XYY-YY, *00000, 2, { {{0}{0}} {{1;2;4;5}{2;4;5}} }",
				"'[$X <2+>[$Y]]', XYYXYY, *00033, 2, { {{0}{3}} {{1;2}{4;5}} }",
				"'[$X <3+>[$Y]]', XYYXYY, *00033, 0, -",
				"'[$X <2+>[$Y]]', XYYXYY, *33000, 2, { {{0}{3}} {{4;5}{1;2}} }",
				//TODO
			})
			@DisplayName("child node with quantification")
			void testQuantifiedChild(String query, String target, String tree, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.tree(tree)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} [{2}] -> {3} matches")
			@CsvSource({
				"'[$X [IsAt(1),$Y]]', XY, *0, 0, -",
				"'[$X [IsAt(1),$Y]]', YX, 1*, 1, { {{1}} {{0}} }",

				"'[$X [IsNotAt(1),$Y]]', YX, 1*, 0, -",
				"'[$X [IsNotAt(1),$Y]]', YYXY, 22*2, 2, { {{2}{2}} {{1}{3}} }",
				"'[$X [IsNotAt(2),$Y]]', YYXY, 22*2, 2, { {{2}{2}} {{0}{3}} }",
				//TODO add at least 1 fail/success test for each sequence marker
			})
			@DisplayName("child node with sequence marker")
			void testSequenceMarkerOnChild(String query, String target, String tree, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.tree(tree)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} [{2}] -> {3} matches")
			@CsvSource({
				"'[$X [IsChildAt(2),$Y]]', XY, *0, 0, -",
				"'[$X [IsChildAt(1),$Y]]', XXY, *00, 0, -",
				"'[$X [IsChildAt(1),$Y]]', YX, 1*, 1, { {{1}} {{0}} }",
				"'[$X [IsChildAt(1),$Y]]', XY, *0, 1, { {{0}} {{1}} }",

				"'[$X [IsFirstChild,$Y]]', XXY, *00, 0, -",
				"'[$X [IsFirstChild,$Y]]', YX, 1*, 1, { {{1}} {{0}} }",
				"'[$X [IsFirstChild,$Y]]', XY, *0, 1, { {{0}} {{1}} }",
				"'[$X [IsFirstChild,$Y]]', YXY, 1*1, 1, { {{1}} {{0}} }",
				"'[$X [IsFirstChild,$Y]]', XYY, *00, 1, { {{0}} {{1}} }",
				"'[$X [IsFirstChild,$Y]]', XXY, *01, 1, { {{1}} {{2}} }",

				"'[$X [IsLastChild,$Y]]', XYX, *00, 0, -",
				"'[$X [IsLastChild,$Y]]', YX, 1*, 1, { {{1}} {{0}} }",
				"'[$X [IsLastChild,$Y]]', XY, *0, 1, { {{0}} {{1}} }",
				"'[$X [IsLastChild,$Y]]', YXY, 1*1, 1, { {{1}} {{2}} }",
				"'[$X [IsLastChild,$Y]]', XYY, *00, 1, { {{0}} {{2}} }",

				"'[$X [IsNotChildAt(1),$Y]]', XYX, *00, 0, -",
				"'[$X [IsNotChildAt(1),$Y]]', YYXY, 22*2, 2, { {{2}{2}} {{1}{3}} }",
				"'[$X [IsNotChildAt(2),$Y]]', YYXY, 22*2, 2, { {{2}{2}} {{0}{3}} }",
				"'[$X [IsNotChildAt(3),$Y]]', YYXY, 22*2, 2, { {{2}{2}} {{0}{1}} }",
				"'[$X [IsNotChildAt(4),$Y]]', YYXY, 22*2, 3, { {{2}{2}{2}} {{0}{1}{3}} }",

				"'[$X [IsChildAfter(1),$Y]]', XYX, *00, 0, -",
				"'[$X [IsChildAfter(1),$Y]]', YYXY, 22*2, 2, { {{2}{2}} {{1}{3}} }",
				"'[$X [IsChildAfter(2),$Y]]', YYXY, 22*2, 1, { {{2}} {{3}} }",

				"'[$X [IsChildBefore(2),$Y]]', XXY, *00, 0, -",
				"'[$X [IsChildBefore(2),$Y]]', YYXY, 22*2, 1, { {{2}} {{0}} }",
				"'[$X [IsChildBefore(3),$Y]]', YYXY, 22*2, 2, { {{2}{2}} {{0}{1}} }",

				"'[$X [IsChildInside(2,3),$Y]]', XYX, *00, 0, -",
				"'[$X [IsChildInside(2,4),$Y]]', YYXY, 22*2, 2, { {{2}{2}} {{1}{3}} }",
				"'[$X [IsChildInside(1,2),$Y]]', YYXY, 22*2, 2, { {{2}{2}} {{0}{1}} }",
				"'[$X [IsChildInside(1,2),$Y]]', YYXY, 12*2, 2, { {{2}{2}} {{1}{3}} }",

				"'[$X [IsChildOutside(2,3),$Y]]', XXY, *00, 0, -",
				"'[$X [IsChildOutside(2,4),$Y]]', YYXY, 22*2, 1, { {{2}} {{0}} }",
				"'[$X [IsChildOutside(2,3),$Y]]', YYXYY, 22*22, 2, { {{2}{2}} {{0}{4}} }",
			})
			@DisplayName("child node with tree hierarchy marker")
			void testTreeHierarchyMarkerOnChild(String query, String target, String tree, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.tree(tree)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} [{2}] -> {3} matches")
			@CsvSource({
				"'[$X [IsBefore(3) && IsChildAfter(1),$Y]]', XY, *0, 0, -",
				"'[$X [IsBefore(3) && IsChildAfter(1),$Y]]', XYY, *00, 0, -",
				"'[$X [IsBefore(3) && IsChildAfter(1),$Y]]', YYX, 22*, 1, { {{2}} {{1}} }",
			})
			@DisplayName("child node with sequence and tree hierarchy marker")
			void testMixedHorizontalMarkersOnChild(String query, String target, String tree, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.tree(tree)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} [{2}] -> {3} matches")
			@CsvSource({
				// Fails
				"'[$X [IsAnyGeneration,$Y]]', XY, 1*, 0, -",
				// Simple nesting
				"'[$X [IsAnyGeneration,$Y]]', XY, *0, 1, { {{0}} {{1}} }",
				"'[$X [IsAnyGeneration,$Y]]', YX, 1*, 1, { {{1}} {{0}} }",
				// Simple nesting with siblings
				"'[$X [IsAnyGeneration,$Y]]', XYY, *00, 2, { {{0}{0}} {{1}{2}} }",
				"'[$X [IsAnyGeneration,$Y]]', YXY, 1*1, 2, { {{1}{1}} {{0}{2}} }",
				"'[$X [IsAnyGeneration,$Y]]', YYX, 22*, 2, { {{2}{2}} {{0}{1}} }",
				// Simple nesting with non-matching siblings
				"'[$X [IsAnyGeneration,$Y]]', XAYB, *000, 1, { {{0}} {{2}} }",
				"'[$X [IsAnyGeneration,$Y]]', XYAY, *000, 2, { {{0}{0}} {{1}{3}} }",
				// Cascaded nesting
				"'[$X [IsAnyGeneration,$Y]]', XYYY, *012, 3, { {{0}{0}{0}} {{1}{2}{3}} }",
				"'[$X [IsAnyGeneration,$Y]]', XYYY, *302, 3, { {{0}{0}{0}} {{2}{3}{1}} }",

				//TODO add tests for other generation markers once we implement the level filter
			})
			@DisplayName("child node with generation marker")
			void testGenerationMarkerOnChild(String query, String target, String tree, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.tree(tree)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} [{2}] -> {3} matches")
			@CsvSource({
				// Fails
				"'[$X [IsRoot,$Y]]', XY, *0, 0, -",

				//TODO add tests for other frame-based markers
			})
			@DisplayName("child node with frame-based generation marker")
			void testFrameMarkerOnChild(String query, String target, String tree, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.tree(tree)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} [{2}] -> {3} matches")
			@CsvSource({
				/*
				 *  Many tests are doubled with varying order of markers to
				 *  test that the query processor properly sorts the different
				 *  types of markers.
				 */

				// Fails
				"'[$X [IsAnyGeneration && IsFirst,$Y]]', XY, *0, 0, -",
				"'[$X [IsFirst && IsAnyGeneration,$Y]]', XY, *0, 0, -",
				"'[$X [IsAnyGeneration && IsLast,$Y]]', YX, 1*, 0, -",
				"'[$X [IsLast && IsAnyGeneration,$Y]]', YX, 1*, 0, -",
				// Sequence and generation markers
				"'[$X [IsAnyGeneration && IsFirst,$Y]]', YX, 1*, 1, { {{1}} {{0}} }",
				"'[$X [IsFirst && IsAnyGeneration,$Y]]', YX, 1*, 1, { {{1}} {{0}} }",
				"'[$X [IsAnyGeneration && IsLast,$Y]]', XY, *0, 1, { {{0}} {{1}} }",
				"'[$X [IsLast && IsAnyGeneration,$Y]]', XY, *0, 1, { {{0}} {{1}} }",
				"'[$X [IsAnyGeneration && IsNotAt(2),$Y]]', XYY, *00, 1, { {{0}} {{2}} }",
				"'[$X [IsNotAt(2) && IsAnyGeneration,$Y]]', XYY, *00, 1, { {{0}} {{2}} }",
				"'[$X [IsAnyGeneration && IsNotAt(3),$Y]]', XYYY, *001, 2, { {{0}{0}} {{1}{3}} }",
				"'[$X [IsNotAt(3) && IsAnyGeneration,$Y]]', XYYY, *001, 2, { {{0}{0}} {{1}{3}} }",
				//TODO complete
				// Tree hierarchy and generation markers
				//TODO complete
				// All types of markers intermixed
				"'[$X [IsAnyGeneration && IsNotAt(3) && IsChildAfter(1),$Y]]', XYYY, *000, 1, { {{0}} {{3}} }",
				"'[$X [IsNotAt(3) && IsAnyGeneration && IsChildAfter(1),$Y]]', XYYY, *000, 1, { {{0}} {{3}} }",

				/* Complex marker expression. Note that the transformation process
				 * reorders markers, so we also get a different order of overall results.
				 */
				"'[$X [(IsNotAt(3) || IsChildAfter(1)) && (IsGenerationBefore(3) || IsLeaf),$Y]]', XYYY, *000, 3, { {{0}{0}{0}} {{1}{3}{2}} }",

				//TODO add tests for other generation markers once we implement the level filter
			})
			@DisplayName("child node with horizontal and generation markers (conjunctive)")
			void testMixedMarkersOnChild(String query, String target, String tree, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_CONFIG)
				.tree(tree)
				.assertResult();
			}

			//TODO test with mixed markers (currently we support conjunctive mix)
		}

		/**
		 * {@link IqlGrouping} nested inside {@link IqlGrouping}
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>blank nodes</li>
		 * <li>dummy nodes</li>
		 * <li>quantifier on inner grouping</li>
		 * <li>quantifier on outer grouping</li>
		 * <li>multiple groupings</li>
		 * <li>markers on nodes on various nesting depths</li>
		 * <li>quantifiers on nodes on various nesting depths</li>
		 * </ul>
		 */
		@Nested
		class GroupingInGrouping {

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{{[][]}}', XX, 1, { {{0}} {{1}} }",
				"'{[] {[][]}}', XX, 0, -",
				"'{[] {[][]}}', XXX, 1, { {{0}} {{1}} {{2}} }",
				"'{[] {[] {[]}}}', XXX, 1, { {{0}} {{1}} {{2}} }",
				"'{{[][]} []}', XX, 0, -",
				"'{{[][]} []}', XXX, 1, { {{0}} {{1}} {{2}} }",
				"'{{{[]} []} []}', XXX, 1, { {{0}} {{1}} {{2}} }",
				"'{{[][]} [] {[][]}}', XXXXX, 1, { {{0}} {{1}} {{2}} {{3}} {{4}} }",
			})
			@DisplayName("nested groups of blank nodes")
			void testBlank(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.promote(true)
				.options(Option.KEEP_REDUNDANT_GROUPING, Option.DEFAULT_ORDERED_SEQUENCE)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{{[?]}[]}', X, 1, { {{}} {{0}} }, false",
				"'{[]{[?]}}', X, 1, { {{0}} {{}} }, false",

				"'{{[*]}[]}', X, 1, { {{}} {{0}} }, false",
				"'{[]{[*]}}', X, 1, { {{0}} {{}} }, false",

				"'{{[+]}}', X, 1, { {{0}} }, false",
				"'{{[+]}[]}', X, 0, -, false",
				"'{{[+]}[]}', XX, 1, { {{0}} {{1}} }, false",
				"'{[]{[+]}}', X, 0, -, false",
				"'{[]{[+]}}', XX, 1, { {{0}} {{1}} }, false",
				"'{{[+]}[*]}', X, 1, { {{0}} {{}} }, false",
				"'{{[+]}[*]}', XX, 2, { {{0}{1}} {{}{}} }, false",
				"'{[*]{[+]}}', X, 1, { {{}} {{0}} }, false",
				"'{[*]{[+]}}', XX, 3, { {{}{}{}} {{0}{1}{1}} }, true",
			})
			@DisplayName("nested groups of dummy nodes")
			void testDummyNodesWithGrouping(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits,
					boolean allowDuplicates) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.promote(true)
				.options(Option.KEEP_REDUNDANT_GROUPING, Option.DEFAULT_ORDERED_SEQUENCE)
				.allowDuplicates(allowDuplicates)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				// Double wrapping

				// Singleton markers
				"'{{[isAt(2), $X]}}', XXX, 1, { {{1}} }",
				"'{{[isNotAt(2), $X]}}', XXX, 2, { {{0}{2}} }",
				"'{{[isAfter(2), $X]}}', XXX, 1, { {{2}} }",
				"'{{[isBefore(2), $X]}}', XXX, 1, { {{0}} }",
				"'{{[isInside(2,4), $X]}}', XXXXX, 3, { {{1}{2}{3}} }",
				"'{{[isOutside(2,4), $X]}}', XXXXX, 2, { {{0}{4}} }",
				// Marker intersection
				"'{{[isFirst && isLast, $X]}}', X, 1, { {{0}} }",
				"'{{[isFirst && isLast, $X]}}', Y, 0, -",
				"'{{[isFirst && isLast, $X]}}', XX, 0, -",
				"'{{[isNotAt(2) && isLast, $X]}}', XX, 0, -",
				// Marker union
				"'{{[isFirst || isLast, $X]}}', X, 1, { {{0}} }",
				"'{{[isFirst || isLast, $X]}}', XX, 2, { {{0}{1}} }",
				"'{{[isFirst || isLast, $X]}}', XXX, 2, { {{0}{2}} }",
				"'{{[isAt(2) || isLast, $X]}}', XXX, 2, { {{1}{2}} }",
				// Complex marker nesting
				"'{{[isFirst || (isNotAt(3) && isBefore(4)), $X]}}', XXXX, 2, { {{0}{1}} }",

				// Proper nesting
				"'{{[isAt(2), $X][]}[isNotAt(3),$X]}', XXX, 0, -",
				"'{{[isAt(2), $X][]}[isNotAt(3),$X]}', XXXX, 1, { {{1}} {{2}} {{3}} }",
				"'{{[isAt(2) || isFirst, $X][]}[isNotAt(3) || isLast,$X]}', XXXX, 3, { {{1}{0}{0}} {{2}{1}{2}} {{3}{3}{3}} }",
			})
			@DisplayName("nested groups with markers on nodes")
			void testMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.promote(true)
				.options(Option.KEEP_REDUNDANT_GROUPING, Option.DEFAULT_ORDERED_SEQUENCE)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{{2+[$X][$Y]}[$Z]}', XYZ, 0, -",
				"'{{2+[$X][$Y]}[$Z]}', XXYZ, 1, { {{0;1}} {{2}} {{3}} }",
				"'{{2+[$X][$Y]}[$Z]}', XXXYZZ, 4, { {{0;1;2}{0;1;2}{1;2}{1;2}} {{3}{3}{3}{3}} {{4}{5}{4}{5}} }",
				"'{{2+[$X][$Y]}2+[$Z]}', XXYZ, 0, -",
				"'{{2+[$X][$Y]}2+[$Z]}', XXYZZ, 1, { {{0;1}} {{2}} {{3;4}} }",
			})
			@DisplayName("quantified nodes in nested groups")
			void testQuantifiedNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.promote(true)
				.options(Option.KEEP_REDUNDANT_GROUPING, Option.DEFAULT_ORDERED_SEQUENCE)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{<2+>{[$X][$Y]}{[$Z][+]}}', XYZ, 0, -",
				"'{<2+>{[$X][$Y]}{[$Z][+]}}', XYZZ, 0, -",
				"'{<2+>{[$X][$Y]}{[$Z][+]}}', XYXYZ-, 1, { {{0;2}} {{1;3}} {{4}} }",
				"'{<2+>{[$X][$Y]}{[$Z][+]}}', XYXYXYZ-, 2, { {{0;2;4}{2;4}} {{1;3;5}{3;5}} {{6}{6}} }",
				"'{<2+>{[$X][$Y]}<2->{[$Z][+]}}', XYXYZ-, 1, { {{0;2}} {{1;3}} {{4}} }",
				"'{<2+>{[$X][$Y]}<2->{[$Z][+]}}', XYXYZ-Z-, 2, { {{0;2}{0;2}} {{1;3}{1;3}} {{4;6}{6}} }",
			})
			@DisplayName("nested quantified group")
			void testInnerQuantification(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.options(Option.KEEP_REDUNDANT_GROUPING, Option.DEFAULT_ORDERED_SEQUENCE)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<2+>{{[$X][+]}[$Y]}', XY, 0, -",
				"'<2+>{{[$X][+]}[$Y]}', X-Y, 0, -",
				"'<2+>{{[$X][+]}[$Y]}', X-YX-Y, 1, { {{0;3}} {-} {{2;5}} }",
				"'<2+>{[$X]{[+][$Y]}}', X-YX-Y, 1, { {{0;3}} {-} {{2;5}} }",
			})
			@DisplayName("group nested in quantified group")
			void testOuterQuantification(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.options(Option.KEEP_REDUNDANT_GROUPING, Option.DEFAULT_ORDERED_SEQUENCE)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<2+>{<1+>{[$X][$Y]}[]}', XY, 0, -",
				"'<2+>{<1+>{[$X][$Y]}[]}', XY-XY-, 1, { {{0;3}} {{1;4}} }",
				"'<2+>{<1+>{[$X][$Y]}[]}', XYXY-XY-, 2, { {{0;2;5}{2;5}} {{1;3;6}{3;6}} }",
				"'<2+>{<1+>{[$X][$Y]}2+[$Z]}', XYZZXYZZZ, 1, { {{0;4}} {{1;5}} {{2;3;6;7;8}} }",
			})
			@DisplayName("quantified group nested in quantified group")
			void testFullQuantification(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.options(Option.KEEP_REDUNDANT_GROUPING, Option.DEFAULT_ORDERED_SEQUENCE)
				.assertResult();
			}
		}

		/**
		 * {@link IqlGrouping} nested inside {@link IqlSequence}
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>blank nodes</li>
		 * <li>dummy nodes</li>
		 * <li>arrangement on set</li>
		 * <li>quantifier on grouping</li>
		 * <li>multiple groupings</li>
		 * <li>markers on nodes on various nesting depths</li>
		 * <li>quantifiers on nodes on various nesting depths</li>
		 * </ul>
		 */
		@Nested
		class GroupingInSequence {

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'[] {[][]}', XX, 0, -",
				"'[] {[][]}', XXX, 4, { {{0}{0}{2}{2}} {{1}{2}{0}{1}} {{2}{1}{1}{0}} }",
				"'[] {[] {[]}}', XXX, 4, { {{0}{0}{2}{2}} {{1}{2}{0}{1}} {{2}{1}{1}{0}} }",
				"'{[][]} []', XX, 0, -",
				"'{[][]} []', XXX, 4, { {{0}{1}{1}{2}} {{1}{0}{2}{1}} {{2}{2}{0}{0}} }",
				"'{{[]} []} []', XXX, 4, { {{0}{1}{1}{2}} {{1}{0}{2}{1}} {{2}{2}{0}{0}} }",
				"'4 HITS {[][]} [] {[][]}', XXXXX, 4, { {{0}{0}{1}{1}} {{1}{1}{0}{0}} {{2}{2}{2}{2}} {{3}{4}{3}{4}} {{4}{3}{4}{3}} }",
			})
			@DisplayName("nested groups of blank nodes")
			void testBlank(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.promote(true)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{[?]}[]', X, 2, true, { {{}{}} {{0}{0}} }",
				"'ORDERED []{[?]}', X, 1, false, { {{0}} {{}} }",

				"'{[*]}[]', X, 2, true, { {{}{}} {{0}{0}} }",
				"'ORDERED []{[*]}', X, 1, false, { {{0}} {{}} }",

				"'{[+]}[]', X, 0, false, -",
				"'ORDERED {[+]}[]', XX, 1, false, { {{0}} {{1}} }",
				"'[]{[+]}', X, 0, false, -",
				"'ORDERED []{[+]}', XX, 1, false, { {{0}} {{1}} }",
				"'ORDERED {[+]}[*]', X, 1, false, { {{0}} {{}} }",
				"'ORDERED {[+]}[*]', XX, 2, false, { {{0}{1}} {{}{}} }",
				"'[*]{[+]}', X, 2, true, { {{}{}} {{0}{0}} }",
				"'ORDERED [*]{[+]}', XX, 3, true, { {{}{}{}} {{0}{1}{1}} }",
			})
			@DisplayName("nested groups of dummy nodes")
			void testDummyNodes(String query, String target, int matches,
					boolean allowDuplicates,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.promote(true)
				.allowDuplicates(allowDuplicates)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				// Singleton markers
				"'ORDERED {ORDERED [isAt(2), $X][]}[]', XXX, 0, -",
				"'ORDERED {[isAt(2), $X][]}[]', XXX, 1, { {{1}} {{0}} {{2}} }",
				"'{[isAt(2), $X][]}[]', XXX, 2, { {{1}{1}} {{0}{2}} {{2}{0}} }",
				"'{ORDERED [isAt(2), $X][]}[]', XXX, 1, { {{1}} {{2}} {{0}} }",
				"'ORDERED {[isAt(2), $X][]}[]', XXXX, 3, { {{1}{1}{1}} {{0}{0}{2}} {{2}{3}{3}} }",
				"'ORDERED {[isNotAt(2), $X][]}[]', XXX, 1, { {{0}} {{1}} {{2}} }",
				"'{[isNotAt(2), $X][]}[]', XXX, 2, { {{0}{2}} {{1}{1}} {{2}{0}} }",
				"'ORDERED {[isAfter(2), $X][]}[]', XXXX, 2, { {{2}{2}} {{0}{1}} {{3}{3}} }",
				"'{ORDERED [isAfter(2), $X][]}[]', XXXX, 2, { {{2}{2}} {{3}{3}} {{0}{1}} }",
				"'ORDERED {[isBefore(2), $X][]}[]', XXX, 1, { {{0}} {{1}} {{2}} }",
				"'{[isBefore(2), $X][]}[]', XXX, 1, { {{0}} {{1}} {{2}} }",
				"'ORDERED {[isInside(2,4), $X][]}[]', XXXX, 5, { {{1}{1}{2}{1}{2}} {{0}{0}{0}{2}{1}} {{2}{3}{3}{3}{3}} }",
				"'7 HITS {[isInside(2,4), $X][]}[]', XXXXX, 7, { {{1}{1}{1}{2}{2}{3}{1}} {{0}{0}{0}{0}{0}{0}{2}} {{2}{3}{4}{3}{4}{4}{0}} }",
				"'ORDERED []{[isOutside(3,4), $X][$Y]}', XXYXXY, 8, { {{0}{0}{0}{0}{1}{1}{2}{3}} {{1}{1}{4}{4}{4}{4}{4}{4}} {{2}{5}{2}{5}{2}{5}{5}{5}} }",
				"'[]{[isOutside(3,4), $X][$Y]}', XXYXX, 7, { {{0}{0}{3}{4}{1}{3}{4}} {{1}{4}{0}{0}{4}{1}{1}} {{2}{2}{2}{2}{2}{2}{2}} }",
				// Marker intersection
				"'[]{[][isNotAt(2) && isLast, $X]}', XX, 0, -",
				"'ORDERED []{[][isNotAt(2) && isLast, $X]}', XXX, 1, { {{0}} {{1}} {{2}} }",
				// Marker union
				"'ORDERED {[isFirst || isLast, $X][]}[]', XXX, 1, { {{0}} {{1}} {{2}} }",
				"'ORDERED []{[][isFirst || isLast, $X]}', XXX, 1, { {{0}} {{1}} {{2}} }",
				// Complex marker nesting
				"'{[isFirst || (isNotAt(3) && isBefore(4)), $X]}', XXXX, 2, { {{0}{1}}}",

				// Proper nesting
				"'ORDERED {[isAt(2), $X][]}[isNotAt(3),$X]', XXX, 0, -",
				"'ORDERED {[isAt(2), $X][]}[isNotAt(3),$X]', XXXX, 2, { {{1}{1}} {{0}{2}} {{3}{3}} }",
				"'ORDERED {[isAt(2) || isFirst, $X][]}[isNotAt(3) || isLast,$X]', XXXX, 4, { {{0}{0}{1}{1}} {{1}{2}{0}{2}} {{3}{3}{3}{3}} }",
			})
			@DisplayName("nested groups of nodes with markers")
			void testMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.promote(true)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{2+[$X][$Y]}[$Z]', XYZ, 0, -",
				"'ORDERED {2+[$X][$Y]}[$Z]', XXYZ, 1, { {{0;1}} {{2}} {{3}} }",
				"'ORDERED {2+[$X][$Y]}[$Z]', XXXYZZ, 4, { {{0;1;2}{0;1;2}{1;2}{1;2}} {{3}{3}{3}{3}} {{4}{5}{4}{5}} }",
				"'{2+[$X][$Y]}2+[$Z]', XXYZ, 0, -",
				"'ORDERED {2+[$X][$Y]}2+[$Z]', XXYZZ, 1, { {{0;1}} {{2}} {{3;4}} }",
			})
			@DisplayName("nested groups of quantified nodes")
			void testQuantifiedNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<1+>{[$X]<2+>[$Y]}[]', XY, 0, -",
				"'<1+>{ORDERED [$X]<2+>[$Y]}[]', XYYX, 1, { {{0}} {{1;2}} }",
				"'<1+>{[$X]<2+>[$Y]}[]', XYYX, 2, { {{0}{3}} {{1;2}{1;2}} }",
				"'<1+>{ORDERED [$X]<2+>[$Y]}', XYY-XYYY-, 2, { {{0}{4}} {{1;2}{5;6;7}} }",
				"'<1+>{[$X]<2+>[$Y]}', XYY-XYYY-, 3, { {{0}{4}{4}} {{1;2}{1;2}{5;6;7}} }",
				"'<1+>{ORDERED [$X]<2+>[$Y]}', XYYXYYY-, 2, { {{0;3}{3}} {{1;2;4;5;6}{4;5;6}} }",
				"'<1+>{[$X]<2+>[$Y]}', XYYXYYY-, 3, { {{0;3}{3}{3}} {{1;2;4;5;6}{1;2}{4;5;6}} }",
				"'<1+>{ORDERED[$X]<2+>[$Y]}', XYYXY-XYY-, 3, { {{0;3}{3}{6}} {{1;2;7;8}{7;8}{7;8}} }",
				"'<1+>{[$X]<2+>[$Y]}', XYYXY-XYY-, 4, { {{0;3}{3}{3}{6}} {{1;2;7;8}{1;2}{7;8}{7;8}} }",
				"'<1+>{[$X]<2+>[$Y]}<2+>[$Z]', XYZZXYZZZ, 0, -",
				"'ORDERED <1+>{ORDERED [$X]<2+>[$Y]}<2+>[$Z]', XYZZXYYZZZ, 4, { {{0}{0}{4}{4}} {{5;6}{5;6}{5;6}{5;6}} {{7;8;9}{8;9}{7;8;9}{8;9}} }",
				"'<1+>{[$X]<2+>[$Y]}<2+>[$Z]', XYZZXYYZZZ, 5, { {{0}{0}{4}{4}{4}} {{5;6}{5;6}{5;6}{5;6}{5;6}} {{7;8;9}{8;9}{2;3}{7;8;9}{8;9}} }",
			})
			@DisplayName("quantified groups of quantified nodes")
			void testFullQuantification(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'ADJACENT {[$X][$Y]}[$Z]', XY-Z, 0, -",
				"'ADJACENT {[$X][$Y]}[$Z]', XYZ, 1, { {{0}} {{1}} {{2}} }",
				"'ADJACENT {[$X][$Y]}{[$Y][$Z]}', XY-YZ, 0, -",
				"'ADJACENT {[$X][$Y]}{[$Y][$Z]}', XYYZ, 1, { {{0}} {{1}} {{2}} {{3}} }",
				"'ADJACENT {[$X]<1+>[$Y]}{[$Y][$Z]}', XYYZ, 1, { {{0}} {{1}} {{2}} {{3}} }",
				"'ADJACENT {[$X]<1+>[$Y]}{[$Y][$Z]}', X-YYZ, 1, { {{0}} {{2}} {{3}} {{4}} }",
				"'ADJACENT {[$X]<1+>[$Y]}{[$Y][$Z]}', X-YY--Z, 1, { {{0}} {{2}} {{3}} {{6}} }",
				"'ADJACENT {[$X]<1+>[$Y]}{<2..3>[$Y][$Z]}', X-YYY--Z, 1, { {{0}} {{2}} {{3;4}} {{7}} }",
				"'ADJACENT {[$X]<1+>[$Y]}{[$Y][$Z]}', XYYYZ, 2, { {{0}{0}} {{1;2}{2}} {{3}{3}} {{4}{4}} }",
			})
			@DisplayName("explicit arrangement")
			void testArrangement(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}
		}

		/**
		 * {@link IqlGrouping} nested inside {@link IqlElementDisjunction}
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>blank nodes</li>
		 * <li>dummy nodes</li>
		 * <li>quantifier on grouping</li>
		 * <li>more than 2 groupings</li>
		 * <li>markers on nodes on various nesting depths</li>
		 * <li>quantifiers on nodes on various nesting depths</li>
		 * </ul>
		 */
		@Nested
		class GroupingInBranch {

			private final QueryConfig QUERY_2 = QueryConfig.disjunction(
					QueryConfig.of(IqlGrouping.class),
					QueryConfig.of(IqlGrouping.class));

			private final QueryConfig QUERY_3 = QueryConfig.disjunction(
					QueryConfig.of(IqlGrouping.class),
					QueryConfig.of(IqlGrouping.class),
					QueryConfig.of(IqlGrouping.class));

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{[][]} or {[][][]}', X, 0, -",
				"'{[][]} or {[][][]}', XX, 2, { {{0}{1}} {{1}{0}} }",
				"'{ORDERED [][]} or {[][][]}', XX, 1, { {{0}} {{1}} }",
				"'{[][][]} or {[][]}', XX, 2, { {-} {-} {-} {{0}{1}} {{1}{0}} }",
				"'{[][][]} or {ORDERED [][]}', XX, 1, { {-} {-} {-} {{0}} {{1}} }",
			})
			@DisplayName("disjunction of blank nodes")
			void testBlank(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_2)
				.promote(true)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<2+>{[$X][$Y]} or {<2>[$X][$Z]}', X, 0, -",
				"'<2+>{[$X][$Y]} or {<2>[$X][$Z]}', XY, 0, -",
				"'<2+>{ORDERED [$X][$Y]} or {<2>[$X][$Z]}', XYXY, 1, { {{0;2}} {{1;3}} }",
				"'<2+>{[$X][$Y]} or {<2>[$X][$Z]}', XXXZY, 2, { {-} {-} {{0;1}{1;2}} {{3}{3}} }",
			})
			@DisplayName("quantified groupings and nodes")
			void testQuantified(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_2)
				.promote(true)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{[$X][isAfter(2), $Y]} or {[isOutside(2,5), $X][$Z]}', XY, 0, -",
				"'{[$X][isAfter(2), $Y]} or {[isOutside(2,5), $X][$Z]}', -XZ, 0, -",
				"'{[$X][isAfter(2), $Y]} or {[isOutside(2,5), $X][$Z]}', X-Y, 1, { {{0}} {{2}} }",
				"'{[$X][isAfter(2), $Y]} or {[isOutside(2,5), $X][$Z]}', -XY, 1, { {{1}} {{2}} }",
				"'{ORDERED [$X][isAfter(2), $Y]} or {ORDERED [isOutside(2,5), $X][$Z]}', XZ, 1, { {-} {-} {{0}} {{1}} }",
				"'{ORDERED [$X][isAfter(2), $Y]} or {ORDERED [isOutside(2,5), $X][$Z]}', X--Z, 1, { {-} {-} {{0}} {{3}} }",
				"'{ORDERED [$X][isAfter(2), $Y]} or {ORDERED [isOutside(2,5), $X][$Z]}', X-ZY, 2, { {{0}{}} {{3}{}} {{}{0}} {{}{2}} }",
			})
			@DisplayName("nodes with markers")
			void testMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_2)
				.promote(true)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{[$X][$Y]} or {[$Y][$Z]} or {[$Z][$Y]}', X, 0, -",
				"'{[$X][$Y]} or {[$Y][$Z]} or {[$Z][$Y]}', -XY-, 1, { {{1}} {{2}} }",
				"'{[$X][$Y]} or {[$Y][$Z]} or {[$Z][$Y]}', -YX-, 1, { {{2}} {{1}} }",
				"'{ORDERED [$X][$Y]} or {[$Y][$Z]} or {[$Z][$Y]}', -XY-, 1, { {{1}} {{2}} }",
				"'{ORDERED [$X][$Y]} or {[$Y][$Z]} or {[$Z][$Y]}', -YX-, 0, -",
				"'{[$X][$Y]} or {[$Y][$Z]} or {[$Z][$Y]}', -YZ-, 2, { {-} {-} {{1}{}} {{2}{}} {{}{2}} {{}{1}} }",
				"'{[$X][$Y]} or {[$Y][$Z]} or {[$Z][$Y]}', -XYZ-, 3, { {{1}{}{}} {{2}{}{}} {{}{2}{}} {{}{3}{}} {{}{}{3}} {{}{}{2}} }",
				"'{[$X][$Y]} or {[$Y][$Z]} or {[$Z][$Y]}', -ZY-, 2, { {-} {-} {{2}{}} {{1}{}} {{}{1}} {{}{2}} }",
			})
			@DisplayName("multiple disjunctions")
			void testMultipleBranches(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_3)
				.promote(true)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}
		}

		/**
		 * {@link IqlGrouping} nested inside {@link IqlTreeNode}
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>blank nodes</li>
		 * <li>dummy nodes</li>
		 * <li>quantifier on grouping</li>
		 * <li>markers on nodes on various nesting depths</li>
		 * <li>quantifiers on nodes on various nesting depths</li>
		 * </ul>
		 */
		@Nested
		class GroupingInTreeNode {

			@ParameterizedTest(name="{index}: {0} in {1} [{2}] -> {3} matches")
			@CsvSource({
				"'[{[][]}]', XX, *0, 0, -",
				"'[{[][]}]', XXX, *01, 0, -",
				"'[{[][]}]', XXX, *00, 2, { {{0}{0}} {{1}{2}} {{2}{1}} }",
				"'[{[][][]}]', XXXXX, *0010, 6, { {{0}{0}{0}{0}{0}{0}} {{1}{1}{2}{4}{2}{4}} {{2}{4}{1}{1}{4}{2}} {{4}{2}{4}{2}{1}{1}} }",
				"'[{[][][]}]', XXXXX, 133*3, 6, { {{3}{3}{3}{3}{3}{3}} {{1}{1}{2}{4}{2}{4}} {{2}{4}{1}{1}{4}{2}} {{4}{2}{4}{2}{1}{1}} }",
			})
			@DisplayName("child group of blank nodes")
			void testBlank(String query, String target, String tree, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.tree(tree)
				.promote(true)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} [{2}] -> {3} matches")
			@CsvSource({
				"'[{[][?]}]', XX, **, 0, false, -",
				"'[{[][?]}]', XX, *0, 2, true, { {{0}{0}} {{1}{1}} }",
				"'[{[][?]}]', XX, 1*, 2, true, { {{1}{1}} {{0}{0}} }",
				"'[{[?][]}]', XX, **, 0, false, -",
				"'[{[?][]}]', XX, *0, 2, true, { {{0}{0}} {{}{}} {{1}{1}} }",
				"'[{[?][]}]', XX, 1*, 2, true, { {{1}{1}} {{}{}} {{0}{0}} }",

				"'[{[][*]}]', XX, **, 0, false, -",
				"'[{[][*]}]', XX, *0, 2, true, { {{0}{0}} {{1}{1}} }",
				"'[{[][*]}]', XX, 1*, 2, true, { {{1}{1}} {{0}{0}} }",
				"'[{[*][]}]', XX, **, 0, false, -",
				"'[{[*][]}]', XX, *0, 2, true, { {{0}{0}} {{}{}} {{1}{1}} }",
				"'[{[*][]}]', XX, 1*, 2, true, { {{1}{1}} {{}{}} {{0}{0}} }",

				"'[{[][+]}]', XXX, *00, 2, false, { {{0}{0}} {{1}{2}} {{2}{1}} }",
				"'[{[][+][]}]', XXXXX, *0010, 6, false, { {{0}{0}{0}{0}{0}{0}} {{1}{1}{2}{4}{2}{4}} {{2}{4}{1}{1}{4}{2}} {{4}{2}{4}{2}{1}{1}} }",
				"'[{[][+][]}]', XXXXX, 133*3, 6, false, { {{3}{3}{3}{3}{3}{3}} {{1}{1}{2}{4}{2}{4}} {{2}{4}{1}{1}{4}{2}} {{4}{2}{4}{2}{1}{1}} }",
			})
			@DisplayName("child group with dummy nodes")
			void testDummyNodes(String query, String target, String tree, int matches,
					boolean allowDuplicates,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.tree(tree)
				.promote(true)
				.allowDuplicates(allowDuplicates)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} [{2}] -> {3} matches")
			@CsvSource({
				"'[<1+>{[$Y][$Z]}]', XYZ, **1, 0, -",
				"'[<1+>{[$Y][$Z]}]', XYZ, *00, 1, { {{0}} {{1}} {{2}} }",
				"'[<1+>{[$Y][$Z]}]', XYZY, *000, 1, { {{0}} {{1}} {{2}} }",
				"'[<1+>{[$Y][$Z]}]', XYZYZ, *0000, 2, { {{0}{0}} {{1;3}{3}} {{2;4}{4}} }",
				"'[<2+>{[$Y][$Z]}]', XYZY, *000, 0, -",
				"'[<2+>{[$Y][$Z]}]', XYZYZ, *0000, 1, { {{0}} {{1;3}} {{2;4}} }",
				//TODO
			})
			@DisplayName("child group with outer quantification")
			void testQuantifiedGroup(String query, String target, String tree, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.tree(tree)
				.promote(true)
				.options(Option.KEEP_REDUNDANT_GROUPING, Option.DEFAULT_ORDERED_SEQUENCE)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} [{2}] -> {3} matches")
			@CsvSource({
				// Use dummy nodes as quantification
				"'[$X {ADJACENT [$Y][*][$Z]}]', XYY, *00, 0, -",
				"'[$X {ADJACENT [$Y][*][$Z]}]', XYZ, *00, 1, { {{0}} {{1}} {-} {{2}} }",
				"'[$X {ADJACENT [$Y][*][$Z]}]', XY--Z, *0000, 1, { {{0}} {{1}} {-} {{4}} }",
				"'[$X {ADJACENT <2+>[$Y][*][$Z]}]', XY--Z, *0000, 0, -",
				"'[$X {ADJACENT <2+>[$Y][*][$Z]}]', XYY--Z, *00000, 1, { {{0}} {{1;2}} {-} {{5}} }",
				"'[$X {ADJACENT <2+>[$Y][*][$Z]}]', XYYY--Z, *000000, 2, { {{0}{0}} {{1;2;3}{2;3}} {-} {{6}{6}} }",
				"'[$X {ADJACENT <2+>[$Y][*][$Z]}]', XYY--Z, *00500, 0, -",
				//TODO
			})

			@DisplayName("child group with inner quantification")
			void testQuantifiedNodes(String query, String target, String tree, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.tree(tree)
				.options(Option.KEEP_REDUNDANT_GROUPING, Option.DEFAULT_ORDERED_SEQUENCE)
				.assertResult();
			}

			//TODO add markers on inner nodes
		}

		/**
		 * {@link IqlSequence} nested inside {@link IqlGrouping}
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>quantifier on grouping</li>
		 * <li>more than 2 groupings</li>
		 * <li>arrangement on sequence(s)</li>
		 * <li>markers on nodes on various nesting depths</li>
		 * <li>quantifiers on nodes on various nesting depths</li>
		 * </ul>
		 */
		@Nested
		class SequenceInGrouping {

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{ORDERED [][]}', XYZ, 3, { {{0}{0}{1}} {{1}{2}{2}} }", // not adjacent, so can expand multiple  times
				"'{ORDERED [][][]}', XYZ, 1, { {{0}} {{1}} {{2}} }",
				"'{[][]}', XX, 2, { {{0}{1}} {{1}{0}} }",
				"'{[][][]}', XXX, 6, { {{0}{0}{1}{2}{1}{2}} {{1}{2}{0}{0}{2}{1}} {{2}{1}{2}{1}{0}{0}} }",
			})
			@DisplayName("grouping of blank node(s)")
			void testBlank(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.promote(true)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				/* Note that "optional reluctant" nodes don't get added to a mapping
				 * unless the context forces an expansion.
				 */

				// Mandatory node with following optional
				"'{ORDERED [][?]}', XY, 2, { {{0}{1}} {{}{}} }, false",
				"'{ORDERED [][?]}', XYZ, 4, { {{0}{0}{1}{2}} {{}{}{}{}} }, true",

				// Mandatory node with following optional expansion
				"'{ORDERED [][*]}', XY, 2, { {{0}{1}} {{}{}} }, false",
				"'{ORDERED [][*]}', XYZ, 4, { {{0}{0}{1}{2}} {{}{}{}{}} }, true",

				// Mandatory node with following dummy node with reluctant expansion
				"'{ORDERED [][+]}', XY, 1, { {{0}} {{1}} }, false",
				"'{ORDERED [][+]}', XYZ, 3, { {{0}{0}{1}} {{1}{2}{2}} }, false",

				// Mandatory node after optional
				"'{ORDERED [?][]}', XY, 3, { {{}{}{}} {{0}{1}{1}} }, true",
				"'{ORDERED [?][]}', XYZ, 6, { {{}{}{}{}{}{}} {{0}{1}{2}{1}{2}{2}} }, true",

				// Mandatory node after optional expansion
				"'{ORDERED [*][]}', XY, 3, { {{}{}{}} {{0}{1}{1}} }, true",
				"'{ORDERED [*][]}', XYZ, 6, { {{}{}{}{}{}{}} {{0}{1}{2}{1}{2}{2}} }, true",

				// Mandatory node after dummy node with reluctant expansion
				"'{ORDERED [+][]}', XY, 1, { {{0}} {{1}} }, false",
				"'{ORDERED [+][]}', XYZ, 3, { {{0}{0}{1}} {{1}{2}{2}} }, false",

				// Mandatory nodes surrounding intermediate optional
				"'{ORDERED [][?][]}', XY, 1, { {{0}} {{}} {{1}} }, false",
				"'{ORDERED [][?][]}', XYZ, 4, { {{0}{0}{0}{1}} {{}{}{}{}} {{1}{2}{2}{2}} }, true",

				// Mandatory nodes surrounding intermediate optional expansion
				"'{ORDERED [][*][]}', XY, 1, { {{0}} {{}} {{1}} }, false",
				"'{ORDERED [][*][]}', XYZ, 4, { {{0}{0}{0}{1}} {{}{}{}{}} {{1}{2}{2}{2}} }, true",

				// Mandatory nodes surrounding intermediate dummy node with reluctant expansion
				"'{[][+][]}', XY, 0, -, false",
				"'{ORDERED [][+][]}', XYZ, 1, { {{0}} {{1}} {{2}} }, false",

				// Cannot force expansion with non-adjacent sequence, we leave that to the NodeInSet group
			})
			@DisplayName("grouping of dummy node(s)")
			void testDummyNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits,
					boolean allowDuplicates) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.promote(true)
				.allowDuplicates(allowDuplicates)
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				// Singleton markers
				"'{[isAt(2), $X]}', XXX, 1, { {{1}} }",
				"'{[isNotAt(2), $X]}', XXX, 2, { {{0}{2}} }",
				"'{[isAfter(2), $X]}', XXX, 1, { {{2}} }",
				"'{[isBefore(2), $X]}', XXX, 1, { {{0}} }",
				"'{[isInside(2,4), $X]}', XXXXX, 3, { {{1}{2}{3}} }",
				"'{[isOutside(2,4), $X]}', XXXXX, 2, { {{0}{4}} }",
				// Marker intersection
				"'{[isFirst && isLast, $X]}', X, 1, { {{0}} }",
				"'{[isFirst && isLast, $X]}', Y, 0, -",
				"'{[isFirst && isLast, $X]}', XX, 0, -",
				"'{[isNotAt(2) && isLast, $X]}', XX, 0, -",
				// Marker union
				"'{[isFirst || isLast, $X]}', X, 1, { {{0}} }",
				"'{[isFirst || isLast, $X]}', XX, 2, { {{0}{1}} }",
				"'{[isFirst || isLast, $X]}', XXX, 2, { {{0}{2}} }",
				"'{[isAt(2) || isLast, $X]}', XXX, 2, { {{1}{2}} }",
				// Complex marker nesting
				"'{[isFirst || (isNotAt(3) && isBefore(4)), $X]}', XXXX, 2, { {{0}{1}} }",
				//TODO add some of the other markers for completeness?
			})
			@DisplayName("grouping of node(s) with markers")
			void testMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{<2+>[$X] <2+>[$Y][$Z]}', XXY, 0, -",
				"'{<2+>[$X] <2+>[$Y][$Z]}', XXYYZ, 1, { {{0;1}} {{2;3}} {{4}} }",
				"'{<2+>[$X] <2+>[$Y][$Z]}', XXXYY-Z, 2, { {{0;1;2}{1;2}} {{3;4}{3;4}} {{6}{6}} }",
			})
			@DisplayName("grouping of quantified node(s)")
			void testQuantifiedNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{<2>[isAfter(1),]}', XX, 0, -",
				"'{<2>[isAfter(1), $X]}', XXX, 1, { {{1;2}} }",
			})
			@DisplayName("grouping of quantified node(s) with markers")
			void testQuantifiedNodesWithMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<1..3>{[$X][$Y]}', XX, 0, -",
				"'<1..3>{[$X][$Y]}', X-Z, 0, -",
				"'<1..3>{ORDERED [$X][$Y]}', XY, 1, { {{0}} {{1}} }",
				"'<1..3>{ORDERED [$X][$Y]}', XYXY, 2, { {{0;2}{2}} {{1;3}{3}} }",
				"'<1..3>{ORDERED [$X][$Y]}', XY-XY, 2, { {{0}{3}} {{1}{4}} }",
				"'<1..3>{ORDERED [$X][$Y]}', XY-XY--XY, 3, { {{0}{3}{7}} {{1}{4}{8}} }",
				"'<1..3^>{ORDERED [$X][$Y]}', XY-XY--XY, 3, { {{0;3;7}{3;7}{7}} {{1;4;8}{4;8}{8}} }",
			})
			@DisplayName("quantified grouping of blank nodes")
			void testQuantifiedGrouping(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<1+>{[isFirst, $X]}', X, 1, { {{0}} }",
				"'<1+>{[isFirst, $X]}', XX, 1, { {{0}} }", //FIXME SM ignores  marker here, maybe hoisting issue?
				"'<1+>{[isBefore(3), $X]}', X, 1, { {{0}} }",
				"'<1+>{[isBefore(3), $X]}', XX, 2, { {{0;1}{1}} }",
				"'<1+>{[isBefore(3), $X]}', XXX, 2, { {{0;1}{1}} }",
				"'<1+>{[isBefore(3), $X]}', XXXXXX, 2, { {{0;1}{1}} }",
				"'<1+>{[isLast, $X]}', X, 1, { {{0}} }",
				"'<1+>{[isLast, $X]}', XX, 1, { {{1}} }",
				"'<1+>{[isAfter(2), $X]}', X, 0, -",
				"'<1+>{[isAfter(2), $X]}', XX, 0, -",
				"'<1+>{[isAfter(2), $X]}', XXX, 1, { {{2}} }",
				"'<1+>{[isAfter(2), $X]}', XXXX, 2, { {{2;3}{3}} }",

				"'<2+>{[isFirst, $X]}', X, 0, -",
				"'<2+>{[isFirst, $X]}', XX, 0, -",
				"'<2+>{[isBefore(3), $X]}', X, 0, -",
				"'<2+>{[isBefore(3), $X]}', XX, 1, { {{0;1}} }",
				"'<2+>{[isBefore(3), $X]}', XXX, 1, { {{0;1}} }",
				"'<2+>{[isBefore(4), $X]}', XXXXXX, 2, { {{0;1;2}{1;2}} }",
				"'<2+>{[isLast, $X]}', X, 0, -",
				"'<2+>{[isLast, $X]}', XX, 0, -",
				"'<2+>{[isAfter(2), $X]}', X, 0, -",
				"'<2+>{[isAfter(2), $X]}', XX, 0, -",
				"'<2+>{[isAfter(2), $X]}', XXXX-, 1, { {{2;3}} }",
				"'<2+>{[isAfter(2), $X]}', XXXXX-XX, 3, { {{2;3;4}{3;4}{6;7}} }",

				"'<1..2>{[isOutside(2,6) || isAt(4), $X]}', X, 1, { {{0}} }",
				"'<1..2>{[isOutside(2,6) || isAt(4), $X]}', XXXX, 2, { {{0}{3}} }",
				"'<1..2>{[isOutside(2,6) || isAt(4), $X]}', XXXXXX, 2, { {{0}{3}} }",
				"'<1..2>{[isOutside(2,6) || isAt(4), $X]}', XXXXXXX, 3, { {{0}{6}{3}} }",
				"'<1..2>{[isAfter(2) && isBefore(5), $X]}', X, 0, -",
				"'<1..2>{[isAfter(2) && isBefore(5), $X]}', X, 0, -",
			})
			@DisplayName("quantified grouping of nodes with markers")
			void testQuantifiedGroupingWithMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<1+>{<2+>[isAfter(1), $X][$Y]}', X, 0, -",
				"'<1+>{<2+>[isAfter(1), $X][$Y]}', XXY, 0, -",
				"'<1+>{ORDERED <2+>[isAfter(1), $X][$Y]}', XXXY, 1, { {{1;2}} {{3}} }",
				"'<1+>{ORDERED <2+>[isAfter(1), $X][$Y]}', XXXXXY-, 3, { {{1;2;3;4}{2;3;4}{3;4}} {{5}{5}{5}} }",
				// outer quantifier only reinforces existential quantification, so group search is exhaustive
				"'<1>{<2+>[isAfter(1), $X][$Y]}', XXXXYY-, 4, { {{1;2;3}{1;2;3}{2;3}{2;3}} {{4}{5}{4}{5}} }",
				// nodes within quantified grouping are reduced to "find" scan
				"'<1+>{ORDERED <2+>[isAfter(1), $X][$Y]}', XXXXYY-, 2, { {{1;2;3}{2;3}} {{4}{4}} }",

				"'<2+>{<2+>[isAfter(1), $X][$Y]}', XXXY, 0, -",
				"'<2+>{ORDERED <2+>[isAfter(1), $X][$Y]}', XXXYXXY, 1, { {{1;2;4;5}} {{3;6}} }",
			})
			@DisplayName("quantified grouping of quantified nodes with markers")
			void testQuantifiedGroupingWithQuantifiedNodesWithMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.assertResult();
			}

		}

		/**
		 * Strictly speaking this combination makes no sense and can also
		 * not be forced to exist via the IQL query grammar.
		 * <p>
		 * Any sequence of structural constraints will be parsed into a single
		 * {@link IqlSequence} object. The only way to "nest" other sequences
		 * is to wrap them into {@link IqlGrouping groups} without additional
		 * quantifiers. But this will also not directly nest sequences within
		 * each other and also the {@link StructurePattern} will ignore groupings
		 * without quantifiers in the absence of advanced {@link NodeArrangement}
		 * declarations.
		 */
		@Nested
		class SequenceInSequence {
			// no-op
		}

		/**
		 * The sequence rule in IQL is of higher priority than the branching
		 * one, so there is no way to directly nest sequences. An additional
		 * step of wrapping via a grouping expression is required. As long as
		 * the grouping does not contain explicit quantification and the
		 * {@link Option#KEEP_REDUNDANT_GROUPING} is not set, those wrapper
		 * groupings will be dropped during parsing.
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>blank nodes</li>
		 * <li>dummy nodes</li>
		 * <li>arrangement on sequence(s)</li>
		 * <li>markers on nodes on various nesting depths</li>
		 * <li>quantifiers on nodes on various nesting depths</li>
		 * </ul>
		 */
		@Nested
		class SequenceInBranch {

			private final QueryConfig QUERY_2 = QueryConfig.disjunction(
				QueryConfig.of(IqlSequence.class),
				QueryConfig.of(IqlSequence.class));
			private final QueryConfig QUERY_3 = QueryConfig.disjunction(
				QueryConfig.of(IqlSequence.class),
				QueryConfig.of(IqlSequence.class),
				QueryConfig.of(IqlSequence.class));

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{[][]} or {[][][]}', X, 0, -",
				"'{[][]} or {[][][]}', XX, 2, { {{0}{1}} {{1}{0}} }",
				"'{[][][]} or {[][]}', XX, 2, { {-} {-} {-} {{0}{1}} {{1}{0}} }",
			})
			@DisplayName("disjunction of blank nodes")
			void testBlank(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_2)
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{[$X]<2+>[$Y]} or {<2>[$X][$Z]}', X, 0, -",
				"'{[$X]<2+>[$Y]} or {<2>[$X][$Z]}', XY, 0, -",
				"'{[$X]<2+>[$Y]} or {<2>[$X][$Z]}', XYY, 1, { {{0}} {{1;2}} }",
				"'{[$X]<2+>[$Y]} or {<2>[$X][$Z]}', XXXZY, 2, { {-} {-} {{0;1}{1;2}} {{3}{3}} }",
			})
			@DisplayName("quantified groupings and nodes")
			void testQuantified(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_2)
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{[$X][isAfter(2), $Y]} or {[isOutside(2,5), $X][$Z]}', XY, 0, -",
				"'{[$X][isAfter(2), $Y]} or {[isOutside(2,5), $X][$Z]}', -XZ, 0, -",
				"'{[$X][isAfter(2), $Y]} or {[isOutside(2,5), $X][$Z]}', X-Y, 1, { {{0}} {{2}} }",
				"'{[$X][isAfter(2), $Y]} or {[isOutside(2,5), $X][$Z]}', -XY, 1, { {{1}} {{2}} }",
				"'{ORDERED [$X][isAfter(2), $Y]} or {ORDERED [isOutside(2,5), $X][$Z]}', XZ, 1, { {-} {-} {{0}} {{1}} }",
				"'{ORDERED [$X][isAfter(2), $Y]} or {ORDERED [isOutside(2,5), $X][$Z]}', X--Z, 1, { {-} {-} {{0}} {{3}} }",
				"'{ORDERED [$X][isAfter(2), $Y]} or {ORDERED [isOutside(2,5), $X][$Z]}', X-ZY, 2, { {{0}{}} {{3}{}} {{}{0}} {{}{2}} }",
			})
			@DisplayName("nodes with markers")
			void testMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_2)
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{[$X][$Y]} or {[$Y][$Z]} or {[$Z][$Y]}', X, 0, -",
				"'{[$X][$Y]} or {[$Y][$Z]} or {[$Z][$Y]}', -XY-, 1, { {{1}} {{2}} }",
				"'{[$X][$Y]} or {[$Y][$Z]} or {[$Z][$Y]}', -YZ-, 2, { {{}{}} {{}{}} {{1}{}} {{2}{}} {{}{2}} {{}{1}} }",
				"'{[$X][$Y]} or {[$Y][$Z]} or {[$Z][$Y]}', -XYZ-, 3, { {{1}{}{}} {{2}{}{}} {{}{2}{}} {{}{3}{}} {{}{}{3}} {{}{}{2}} }",
				"'{[$X][$Y]} or {[$Y][$Z]} or {[$Z][$Y]}', -ZY-, 2, { {{}{}} {{}{}} {{2}{}} {{1}{}} {{}{1}} {{}{2}} }",
			})
			@DisplayName("multiple disjunctions")
			void testMultipleBranches(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QUERY_3)
				.promote(true)
				.assertResult();
			}

			//TODO add marker tests
		}

		@Nested
		class SequenceInTreeNode {

			private QueryConfig flatTreeConfig(int childCount) {
				assertThat(childCount).as("must have at least 2 children").isGreaterThan(1);
				return QueryConfig.tree(QueryConfig.sequence(
						IntStream.range(0, childCount)
						.mapToObj(i -> QueryConfig.of(IqlNode.class))
						.toArray(QueryConfig[]::new)));
			}

			//TODO
		}

		/**
		 * {@link IqlElementDisjunction} nested inside {@link IqlGrouping}
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>quantifier on grouping</li>
		 * <li>markers on nodes on various nesting depths</li>
		 * <li>quantifiers on nodes on various nesting depths</li>
		 * </ul>
		 */
		@Nested
		class BranchInGrouping {

			/** Make a {@link QueryConfig} with {@code size} elements
			 * and a branch at {@code branchPos}. Branch index is 1-based. */
			private QueryConfig groupingConfig(int size, int branchPos) {
				QueryConfig[] elements = new QueryConfig[size];
				for (int i = 0; i < elements.length; i++) {
					if(i==branchPos-1) {
						elements[i] = QueryConfig.of(IqlElementDisjunction.class);
					} else {
						elements[i] = QueryConfig.of(IqlNode.class);
					}
				}
				QueryConfig element = size==1 ? elements[0] : QueryConfig.sequence(elements);
				return QueryConfig.grouping(element);
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{[][][] or [][]}', X, 0, -, 4, 3",
				"'{ORDERED [] or [][][][]}', XXXX, 2, { {{0}{-}} {{-}{0}} {{1}{1}} {{2}{2}} {{3}{3}} }, 4, 1",
				"'{ORDERED [][] or [][][]}', XXXX, 2, { {{0}{0}} {{1}{-}} {{-}{1}} {{2}{2}} {{3}{3}} }, 4, 2",
				"'{ORDERED [][][] or [][]}', XXXX, 2, { {{0}{0}} {{1}{1}} {{2}{}} {{}{2}} {{3}{3}} }, 4, 3",
				"'{ORDERED [][][][] or []}', XXXX, 2, { {{0}{0}} {{1}{1}} {{2}{2}} {{3}{-}} {{-}{3}} }, 4, 4",
			})
			@DisplayName("blank nodes")
			void testBlank(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits, int nodeCount, int branchPos) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(groupingConfig(nodeCount, branchPos))
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{[$X][$Y][$A] or [$B][$Z]}', X, 0, -, 4, 3",
				"'{[$X][$Y][$A] or [$B][$Z]}', XYCZ, 0, -, 4, 3",
				"'{ORDERED [$X][$Y][$A] or [$B][$Z]}', XYAZ, 1, { {{0}} {{1}} {{2}} {-} {{3}} }, 4, 3",
				"'{ORDERED [$X][$Y][$A] or [$B][$Z]}', XYBZ, 1, { {{0}} {{1}} {-} {{2}} {{3}} }, 4, 3",

				"'{ORDERED [$X][$Y][$A] or [$B][$Z]}', X-YAAZ, 2, { {{0}{0}} {{2}{2}} {{3}{4}} {-} {{5}{5}} }, 4, 3",
				"'{ORDERED [$X][$Y][$A] or [$B][$Z]}', X-YBAZ, 2, { {{0}{0}} {{2}{2}} {{}{4}} {{3}{}} {{5}{5}} }, 4, 3",
			})
			@DisplayName("nodes with constraints")
			void testContent(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits, int nodeCount, int branchPos) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(groupingConfig(nodeCount, branchPos))
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{ADJACENT [$X]<1+>[$Y]<2+>[$A] or <1+>[$B][$Z]}', XYAZ, 0, -, 4, 3",
				"'{ADJACENT ORDERED [$X]<1+>[$Y]<2+>[$A] or <1+>[$B][$Z]}', XYBZ, 1, { {{0}} {{1}} {-} {{2}} {{3}} }, 4, 3",
				"'{ADJACENT ORDERED [$X]<1+>[$Y]<2+>[$A] or <1+>[$B][$Z]}', XYBBBZ, 1, { {{0}} {{1}} {-} {{2;3;4}} {{5}} }, 4, 3",
				"'{ADJACENT ORDERED [$X]<1+>[$Y]<2+>[$A] or <1+>[$B][$Z]}', XYAAZ, 1, { {{0}} {{1}} {{2;3}} {-} {{4}} }, 4, 3",
				"'{ADJACENT ORDERED [$X]<1+>[$Y]<2+>[$A] or <1+>[$B][$Z]}', XYYYAAAAZ, 1, { {{0}} {{1;2;3}} {{4;5;6;7}} {-} {{8}} }, 4, 3",
			})
			@DisplayName("nodes with quantification")
			void testQuantifiedNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits, int nodeCount, int branchPos) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(groupingConfig(nodeCount, branchPos))
				.options(Option.KEEP_REDUNDANT_GROUPING)
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<2+>{[$X][$Y]<2+>[$A] or <1+>[$B][$Z]}', XYAAZ, 0, -, 4, 3",
				"'<2+>{[$X][$Y]<2+>[$A] or <1+>[$B][$Z]}', XYBZ, 0, -, 4, 3",
				"'<2+>{ORDERED [$X][$Y]<2+>[$A] or <1+>[$B][$Z]}', XYAAZXYAAZ, 1, { {{0;5}} {{1;6}} {{2;3;7;8}} {-} {{4;9}} }, 4, 3",
				"'<2+>{ORDERED [$X][$Y]<2+>[$A] or <1+>[$B][$Z]}', XYAAZXYBZ, 1, { {{0;5}} {{1;6}} {{2;3}} {{7}} {{4;8}} }, 4, 3",
				"'<2+>{ORDERED [$X][$Y]<2+>[$A] or <1+>[$B][$Z]}', XYBZXYBZ, 1, { {{0;4}} {{1;5}} {-} {{2;6}} {{3;7}} }, 4, 3",
				"'<2+>{ORDERED [$X][$Y]<2+>[$A] or <1+>[$B][$Z]}', XYBZXYAAZ, 1, { {{0;4}} {{1;5}} {{6;7}} {{2}} {{3;8}} }, 4, 3",
				"'<2+>{ORDERED [$X][$Y]<2+>[$A] or <1+>[$B][$Z]}', XYBZXYAAZXYBBBZ, 2, { {{0;4;9}{4;9}} {{1;5;10}{5;10}} {{6;7}{6;7}} {{2;11;12;13}{11;12;13}} {{3;8;14}{8;14}} }, 4, 3",
			})
			@DisplayName("quantified group")
			void testQuantified(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits, int nodeCount, int branchPos) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(groupingConfig(nodeCount, branchPos))
				.promote(true)
				.assertResult();
			}

			//TODO add marker tests
		}

		/**
		 * {@link IqlElementDisjunction} nested inside {@link IqlSequence}
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>arrangement on sequence(s)</li>
		 * <li>markers on nodes on various nesting depths</li>
		 * <li>quantifiers on nodes on various nesting depths</li>
		 * </ul>
		 */
		@Nested
		class BranchInSequence {

			/** Make a {@link QueryConfig} with {@code size} elements
			 * and a branch at {@code branchPos}. Branch index is 1-based. */
			private QueryConfig sequenceConfig(int size, int branchPos) {
				QueryConfig[] elements = new QueryConfig[size];
				for (int i = 0; i < elements.length; i++) {
					if(i==branchPos-1) {
						elements[i] = QueryConfig.of(IqlElementDisjunction.class);
					} else {
						elements[i] = QueryConfig.of(IqlNode.class);
					}
				}
				return QueryConfig.sequence(elements);
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'[][][] or [][]', X, 0, -, 4, 3",
				"'ORDERED [] or [][][][]', XXXX, 2, { {{0}{-}} {{-}{0}} {{1}{1}} {{2}{2}} {{3}{3}} }, 4, 1",
				"'ORDERED [][] or [][][]', XXXX, 2, { {{0}{0}} {{1}{-}} {{-}{1}} {{2}{2}} {{3}{3}} }, 4, 2",
				"'ORDERED [][][] or [][]', XXXX, 2, { {{0}{0}} {{1}{1}} {{2}{}} {{}{2}} {{3}{3}} }, 4, 3",
				"'ORDERED [][][][] or []', XXXX, 2, { {{0}{0}} {{1}{1}} {{2}{2}} {{3}{-}} {{-}{3}} }, 4, 4",
			})
			@DisplayName("blank nodes")
			void testBlank(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits, int nodeCount, int branchPos) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(sequenceConfig(nodeCount, branchPos))
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'[$X][$Y][$A] or [$B][$Z]', X, 0, -, 4, 3",
				"'[$X][$Y][$A] or [$B][$Z]', XYCZ, 0, -, 4, 3",
				"'ORDERED [$X][$Y][$A] or [$B][$Z]', XYAZ, 1, { {{0}} {{1}} {{2}} {-} {{3}} }, 4, 3",
				"'ORDERED [$X][$Y][$A] or [$B][$Z]', XYBZ, 1, { {{0}} {{1}} {-} {{2}} {{3}} }, 4, 3",

				"'ORDERED [$X][$Y][$A] or [$B][$Z]', X-YAAZ, 2, { {{0}{0}} {{2}{2}} {{3}{4}} {-} {{5}{5}} }, 4, 3",
				"'ORDERED [$X][$Y][$A] or [$B][$Z]', X-YBAZ, 2, { {{0}{0}} {{2}{2}} {{}{4}} {{3}{}} {{5}{5}} }, 4, 3",
			})
			@DisplayName("nodes with constraints")
			void testContent(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits, int nodeCount, int branchPos) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(sequenceConfig(nodeCount, branchPos))
				.promote(true)
				.assertResult();
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'ADJACENT [$X]<1+>[$Y]<2+>[$A] or <1+>[$B][$Z]', XYAZ, 0, -, 4, 3",
				"'ADJACENT ORDERED [$X]<1+>[$Y]<2+>[$A] or <1+>[$B][$Z]', XYBZ, 1, { {{0}} {{1}} {-} {{2}} {{3}} }, 4, 3",
				"'ADJACENT ORDERED [$X]<1+>[$Y]<2+>[$A] or <1+>[$B][$Z]', XYBBBZ, 1, { {{0}} {{1}} {-} {{2;3;4}} {{5}} }, 4, 3",
				"'ADJACENT ORDERED [$X]<1+>[$Y]<2+>[$A] or <1+>[$B][$Z]', XYAAZ, 1, { {{0}} {{1}} {{2;3}} {-} {{4}} }, 4, 3",
				"'ADJACENT ORDERED [$X]<1+>[$Y]<2+>[$A] or <1+>[$B][$Z]', XYYYAAAAZ, 1, { {{0}} {{1;2;3}} {{4;5;6;7}} {-} {{8}} }, 4, 3",
			})
			@DisplayName("nodes with quantification")
			void testQuantified(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits, int nodeCount, int branchPos) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(sequenceConfig(nodeCount, branchPos))
				.promote(true)
				.assertResult();
			}

		}

		/**
		 * Strictly speaking this combination makes no sense and can also
		 * not be forced to exist via the IQL query grammar.
		 * <p>
		 * Multiple consecutive branch expressions in IQL will be parsed
		 * associatively and collected into a single instance of
		 * {@link IqlElementDisjunction}.
		 */
		@Nested
		@Disabled("not possible to construct test cases")
		class BranchInBranch {
			// no-op
		}

		@Nested
		class BranchInTreeNode {

		}

		@Nested
		class TreeNodeInGrouping {

		}

		@Nested
		class TreeNodeInSequence {

		}

		@Nested
		class TreeNodeInBranch {

		}

		/**
		 * {@link IqlTreeNode} nested inside {@link IqlTreeNode}
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>blank nodes</li>
		 * <li>dummy nodes</li>
		 * <li>more than 3 nested levels</li>
		 * <li>markers on nodes on various nesting depths</li>
		 * <li>quantifiers on nodes on various nesting depths</li>
		 * </ul>
		 *
		 * Note that blank nodes produce no mappings, so we are using the
		 * {@link StructurePattern.Builder#nodeTransform(Function)} method
		 * to inject artificial node labels after the query has been parsed,
		 * causing the final matcher to actually create mappings we can verify.
		 */
		@Nested
		class TreeNodeInTreeNode {

			@ParameterizedTest(name="{index}: {0} in {1} [{2}] -> {3} matches")
			@CsvSource({
				"'[[[]]]', XXX, *00, 0, -",
				// Permutate all simple cases
				"'[[[]]]', XXX, *01, 1, { {{0}} {{1}} {{2}} }",
				"'[[[]]]', XXX, *20, 1, { {{0}} {{2}} {{1}} }",
				"'[[[]]]', XXX, 1*0, 1, { {{1}} {{0}} {{2}} }",
				"'[[[]]]', XXX, 2*1, 1, { {{1}} {{2}} {{0}} }",
				"'[[[]]]', XXX, 20*, 1, { {{2}} {{0}} {{1}} }",
				"'[[[]]]', XXX, 12*, 1, { {{2}} {{1}} {{0}} }",
				// Permutate intermediate dead-end children
				"'[[[]]]', XYXX, 233*, 1, { {{3}} {{2}} {{0}} }",
				// Full tree
				"'[[[]]]', XXXXX, 20*01, 3, { {{0}{2}{2}} {{1}{0}{0}} {{4}{1}{3}} }",
				//TODO complete
			})
			@DisplayName("blank trees")
			void testBlank(String query, String target, String tree, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				rawQueryTest(query, target, matches, hits)
				.queryConfig(QueryConfig.fromQuery(query))
				.tree(tree)
				.promote(true)
				.assertResult();
			}

		}

		@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
		@CsvSource({
			// Mismatches - ordered
			"'<1+>{[$X][$Y]}', -, 0, -",
			"'<1+>{[$X][$Y]}', --, 0, -",

			// Simple matches - ordered
			"'<1+>{ORDERED [$X][$Y]}', XY, 1, {{{0}}{{1}}}",
			"'<1+>{ORDERED [$X][$Y]}', X-Y, 1, {{{0}}{{2}}}",

			// Multiple matches - ordered
			"'FIRST <1+>{ORDERED [$X][$Y]}', XYXY, 1, {{{0;2}}{{1;3}}}",
			"'<1+>{ORDERED [$X][$Y]}', XYXY, 2, {{{0;2}{2}}{{1;3}{3}}}",

			// Mismatches - adjacent
			"'<1+>{ADJACENT [$X][$Y]}', -, 0, -",
			"'<1+>{ADJACENT [$X][$Y]}', --, 0, -",
			"'<1+>{ADJACENT [$X][$Y]}', X-Y, 0, -",

			// Simple match - adjacent
			"'<1+>{ADJACENT [$X][$Y]}', XY, 1, {{{0}}{{1}}}",

			// Multiple matches - adjacent
			"'FIRST <1+>{ADJACENT [$X][$Y]}', XYXY, 1, {{{0;2}}{{1;3}}}",
			// separate matches
			"'<1+>{ADJACENT [$X][$Y]}', XYXY, 3, {{{0;2}{2}{2}}{{1;3}{1}{3}}}",
			"'<1+>{ADJACENT [$X][$Y]}', XY-XY, 2, {{{0}{3}}{{1}{4}}}",
			"'<1+^>{ADJACENT [$X][$Y]}', XY-XY, 2, {{{0;3}{3}}{{1;4}{4}}}",
			"'ADJACENT <1+>{ADJACENT [$X][$Y]} [$Z]', XYZ-XY-XYXYZ, 3, { {{0}{7;9}{9}} {{1}{8;10}{10}} {{2}{11}{11}} }",

			// Mismatches
			"'<2+>{ADJACENT [$X][$Y]}', XY, 0, -",
			"'<2+>{ADJACENT [$X][$Y]}', XYX-Y, 0, -",
			"'<2+>{ADJACENT [$X][$Y]}', X-YXY, 0, -",

			// Simple (and multiple continuous) matches - adjacent
			"'<2+>{ADJACENT [$X][$Y]}', XY-XY, 0, -",
			"'<2+>{ADJACENT [$X][$Y]}', XY---XY, 0, -",
			"'<2+>{ADJACENT [$X][$Y]}', XYXY, 1, { {{0;2}} {{1;3}} }",
			"'<2+>{ADJACENT [$X][$Y]}', XYXYXY, 3, { {{0;2;4}{2;4}{2;4}} {{1;3;5}{1;3}{3;5}} }",
			"'<2+>{ADJACENT [$X][$Y]}', XYXYXYXY, 5, { {{0;2;4;6}{2;4;6}{2;4;6}{4;6}{4;6}} {{1;3;5;7}{1;3;5}{3;5;7}{3;5}{5;7}} }",

			// Multiple discontinuous matches - adjacent
			"'<2+^>{ADJACENT [$X][$Y]}', XY-XY, 1, {{{0;3}}{{1;4}}}",
			"'<2+^>{ADJACENT [$X][$Y]}', XY---XY, 1, {{{0;5}}{{1;6}}}",

			// Nested quantification
			"'<2+>{<2+>[$X][$Y]}', XYY, 0, -",
			"'<2+>{<2+>[$X][$Y]}', XXY, 0, -",
			"'<2+>{ORDERED <2+>[$X][$Y]}', XXYXXY, 1, {{{0;1;3;4}}{{2;5}}}",
			"'<2+>{ORDERED <2+>[$X][$Y]}', XXXXYXXY, 3, {{{0;1;2;3;5;6}{1;2;3;5;6}{2;3;5;6}}{{4;7}{4;7}{4;7}}}",
			"'<2+>{ORDERED <2+>[$X][$Y]}', XXYXXXXY, 1, {{{0;1;3;4;5;6}}{{2;7}}}",

			// Repetition with follow-up node
			"'ADJACENT <3+>{ADJACENT [$X][$Y]} [$Z]', XYXYXYZ, 1, {{{0;2;4}}{{1;3;5}}{{6}}}",
			// Full adjacency on nested group
			"'<3+>{ADJACENT [$X][$Y]} [$Z]', XYXYXYZ, 1, {{{0;2;4}}{{1;3;5}}{{6}}}",
			// Inner adjacency on nested group
			"'<3+>{ADJACENT [$X][$Y]} [$Z]', XYXYXY-Z, 1, {{{0;2;4}}{{1;3;5}}{{7}}}",
			// Inner adjacency of discontinuous repetition
			"'<3+^>{ADJACENT [$X][$Y]} [$Z]', XY-XYXYZ, 1, {{{0;3;5}}{{1;4;6}}{{7}}}",
			"'<3+^>{ADJACENT [$X][$Y]} [$Z]', XYXY-XYZ, 1, {{{0;2;5}}{{1;3;6}}{{7}}}",
			"'<3+^>{ADJACENT [$X][$Y]} [$Z]', XYXYXY-Z, 1, {{{0;2;4}}{{1;3;5}}{{7}}}",
			"'<3+^>{ADJACENT [$X][$Y]} [$Z]', XY-XY-XY-Z, 1, {{{0;3;6}}{{1;4;7}}{{9}}}",
		})
		@DisplayName("Repetition of adjacent sequences")
		void testRepetitionAdjacent(String query, String target, int matches,
				// [node_id][match_id][hits]
				@IntMatrixArg int[][][] hits) {
			expandingMatcherTest(query, target)
			.expectMatches(matches)
			.results(matches, hits)
			.queryConfig(QueryConfig.fromQuery(query))
			.options(Option.KEEP_REDUNDANT_GROUPING)
			.assertResult();
		}


		@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
		@CsvSource({
			"'<1+>[$X] or {[$Y] or [$Z]}', XYXXXZY, 7, { {{0}{}{2;3;4}{3;4}{4}{}{}} {{}{1}{}{}{}{}{6}} {{}{}{}{}{}{5}{}} }",
			"'<2+>[$X] or {[$Y] or [$Z]}', XYXXXZY, 5, { {{}{2;3;4}{3;4}{}{}} {{1}{}{}{}{6}} {{}{}{}{5}{}} }",
		})
		@DisplayName("Nesting of disjunctions")
		void testNestedBranches(String query, String target, int matches,
				// [node_id][match_id][hits]
				@IntMatrixArg int[][][] hits) {
			expandingMatcherTest(query, target)
			.expectMatches(matches)
			.results(matches, hits)
			.queryConfig(QueryConfig.of(IqlElementDisjunction.class))
			.assertResult();
		}


		@Disabled
		//TODO copy and enable to add further complex tests
		@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
		@CsvSource({
			"'QUERY_2', TARGET, MATCH_COUNT, { {HITS_1} {HITS_2} {HITS_3} }",
		})
		@DisplayName("DUMMY NAME")
		void test__Template(String query, String target, int matches,
				// [node_id][match_id][hits]
				@IntMatrixArg int[][][] hits) {
			expandingMatcherTest(query, target)
			.expectMatches(matches)
			.results(matches, hits)
			.queryConfig(QueryConfig.fromQuery(query))
			.assertResult();
		}
	}
}
