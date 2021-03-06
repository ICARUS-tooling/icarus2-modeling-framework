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
package de.ims.icarus2.query.api.engine.matcher;

import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockContext;
import static de.ims.icarus2.model.api.ModelTestUtils.mockCorpus;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockLayer;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.BUFFER_0;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.BUFFER_1;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.CACHE_0;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.CACHE_1;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.CACHE_2;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.CACHE_3;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.CONTINUOUS;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.DISCONTINUOUS;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.EQUALS_A;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.EQUALS_B;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.EQUALS_NOT_X;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.EQUALS_X;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.EQUALS_X_IC;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.EQUALS_Y;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.NODE_0;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.NODE_1;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.NODE_2;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.NO_CACHE;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.NO_LIMIT;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.NO_MEMBER;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.REGION_0;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.branch;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.item;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.matchers;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.seq;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.NO_LABEL;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.NO_MARKER;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.adjacent;
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
import static de.ims.icarus2.query.api.iql.IqlTestUtils.set;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.unordered;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.lang.Primitives._boolean;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.model.api.view.ScopeBuilder;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.query.api.engine.QueryProcessor;
import de.ims.icarus2.query.api.engine.matcher.IntervalConverter.IntervalArg;
import de.ims.icarus2.query.api.engine.matcher.IntervalConverter.IntervalArrayArg;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Branch;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.BranchConn;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Cache;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.DynamicClip;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Exhaust;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Finish;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Monitor;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Node;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.NodeInfo;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.NonResettingMatcher;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Repetition;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.SequenceQueryProcessor;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Single;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.State;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.StateMachineSetup;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.TreeInfo;
import de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.RepetitionUtils.ClosedBase;
import de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.RepetitionUtils.OpenBase;
import de.ims.icarus2.query.api.engine.matcher.mark.Interval;
import de.ims.icarus2.query.api.exp.EvaluationContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.LaneContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.RootContext;
import de.ims.icarus2.query.api.exp.env.SharedUtilityEnvironments;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlPredicate;
import de.ims.icarus2.query.api.iql.IqlElement;
import de.ims.icarus2.query.api.iql.IqlElement.IqlElementDisjunction;
import de.ims.icarus2.query.api.iql.IqlElement.IqlGrouping;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlElement.IqlSequence;
import de.ims.icarus2.query.api.iql.IqlExpression;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlLane.LaneType;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerCall;
import de.ims.icarus2.query.api.iql.IqlObjectIdGenerator;
import de.ims.icarus2.query.api.iql.IqlPayload;
import de.ims.icarus2.query.api.iql.IqlPayload.MatchFlag;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryType;
import de.ims.icarus2.query.api.iql.IqlQuantifier;
import de.ims.icarus2.query.api.iql.IqlQueryElement;
import de.ims.icarus2.query.api.iql.IqlTestUtils;
import de.ims.icarus2.test.annotations.IntArrayArg;
import de.ims.icarus2.test.annotations.IntMatrixArg;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.collections.set.ArraySet;
import de.ims.icarus2.util.collections.set.DataSet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * IMPORTANT NOTE:
 * <p>
 * Many test methods in this class state it explicitly, some omit it, but here the
 * information is once again:
 * <br>
 * <b>OUTDATED INFO</b> TODO fix
 * The state machine behind {@link SequencePattern} is built <b>BACK TO FRONT</b>!
 * This means that all cache ids, node ids and other identifying properties of nodes
 * inside the state machine that "count incrementally" also start with the node
 * farthest from the query begin as {@code 0}. To simplify test code and make it more
 * intuitive, a collection of utility methods exists, taking care of automatically
 * reversing manually defined expected mappings for result assertions. Unless a test
 * method explicitly states otherwise, it should always be assumed that the arguments
 * for testing can be given in the "natural" order of elements in the query and proper
 * conversion will happen under the hood.
 *
 * <p>
 * This test class is one big unification of test suites for various aspects of the
 * {@link SequencePattern} state machine for IQL. Strictly speaking we should divide
 * this instantiation of anti-patterns into a small selection of test suites (in also
 * separate files) for distinct aspects of the state machine. But currently it is just
 * simpler to keep it in one place as both the state machine processor, evaluation
 * and test suite are still in a rather volatile state.
 *
 * @author Markus Gärtner
 *
 */
class SequencePatternTest {

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
					System.out.printf("%d->%d",_int(state.m_node[i]), _int(state.m_pos[i]));
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
		static final int CACHE_0 = 0;
		static final int CACHE_1 = 1;
		static final int CACHE_2 = 2;
		static final int CACHE_3 = 3;
		static final int BUFFER_0 = 0;
		static final int BUFFER_1 = 1;
		static final int REGION_0 = 0;
		static final int REGION_1 = 1;

		static final int NO_FIND = UNSET_INT;

		static final int NO_CACHE = UNSET_INT;
		static final int NO_LIMIT = UNSET_INT;
		static final int NO_MEMBER = UNSET_INT;

		static final boolean CONTINUOUS = false;
		static final boolean DISCONTINUOUS = true;

		/** Match given character exactly */
		static SequencePatternTest.CharPredicate eq(char sentinel) {
			return c -> c==sentinel;
		}
		/** Match anything but given character */
		static SequencePatternTest.CharPredicate neq(char sentinel) {
			return c -> c!=sentinel;
		}
		/** Match given character while ignoring case */
		static SequencePatternTest.CharPredicate ic(char sentinel) {
			return c -> c==Character.toLowerCase(sentinel) || c==Character.toUpperCase(sentinel);
		}
		/** Match no character -> always return false */
		static SequencePatternTest.CharPredicate none() {
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

		static final String LANE_NAME = "test_lane";
		static final String ITEMS_NAME = "test_items";

		static ItemLayer layer(Context context, String name, ItemLayer...baseLayers) {
			ItemLayer layer = mockLayer(ItemLayer.class, context);
			ItemLayerManifest manifest = mockTypedManifest(ManifestType.ITEM_LAYER_MANIFEST, name);
			when(layer.getManifest()).thenAnswer(invoc -> manifest);
			final DataSet<ItemLayer> bl = new ArraySet<>(baseLayers);
			when(layer.getBaseLayers()).thenReturn(bl);
			return layer;
		}

		static Scope scope() {
			Corpus corpus = mockCorpus();
			Context context = mockContext(corpus);
			ItemLayer items = layer(context, ITEMS_NAME);
			ItemLayer lane = layer(context, LANE_NAME, items);
			Scope scope = ScopeBuilder.of(corpus)
					.addContext(context)
					.addLayer(items)
					.addLayer(lane)
					.setPrimaryLayer(lane)
					.build();
			return scope;
		}

		static IqlLane lane() {
			IqlLane lane = new IqlLane();
			lane.setName(LANE_NAME);
			return lane;
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
			if(pos>state.to) {
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

	static SequencePattern.Builder builder(IqlElement root) {
		Scope scope = Utils.scope();

		SequencePattern.Builder builder = SequencePattern.builder();
		builder.root(root);
		builder.id(1);
		RootContext rootContext = EvaluationContext.rootBuilder()
				.corpus(scope.getCorpus())
				.scope(scope)
				.environment(SharedUtilityEnvironments.all())
				.build();
		LaneContext context = rootContext.derive()
				.lane(Utils.lane())
				.build();
		builder.context(context);

		return builder;
	}

	private static final Pattern NODE = Pattern.compile("\\$([A-Za-z])");

	/** Expand {@code $X} expressions to proper constraints */
	static String expand(String rawQuery) {
		int lastAppend = 0;
		java.util.regex.Matcher m = NODE.matcher(rawQuery);
		StringBuilder sb = new StringBuilder();
		if(!rawQuery.startsWith("FIND ")) {
			sb.append("FIND ");
		}
		while(m.find()) {
			String content = m.group(1);
			assertThat(content).as("node content not a single character").hasSize(1);
			char c = content.charAt(0);
			// Can't use Matcher.appendReplacement, as we use $ in the expression
			sb.append(rawQuery, lastAppend, m.start())
				.append(eq_exp(c));
			lastAppend = m.end();
		}
		sb.append(rawQuery, lastAppend, rawQuery.length());
		return sb.toString();
	}

	static SequencePattern.Builder builder(String rawPayload) {

		IqlPayload payload = new QueryProcessor(false).processPayload(rawPayload);
		assertThat(payload).as("No payload").isNotNull();
		assertThat(payload.getQueryType()).isEqualTo(QueryType.SINGLE_LANE);
		assertThat(payload.getLanes()).as("Missing lane").isNotEmpty();
		IqlLane lane = payload.getLanes().get(0);
		assertThat(lane.getLaneType()).isEqualTo(LaneType.SEQUENCE);
		IqlElement root = lane.getElement();

		Scope scope = Utils.scope();

		SequencePattern.Builder builder = SequencePattern.builder();
		builder.root(root);
		builder.id(1);
		RootContext rootContext = EvaluationContext.rootBuilder()
				.corpus(scope.getCorpus())
				.scope(scope)
				.environment(SharedUtilityEnvironments.all())
				.build();
		LaneContext context = rootContext.derive()
				.lane(lane)
				.build();
		builder.context(context);
		payload.getLimit().ifPresent(builder::limit);

		return builder;
	}


	static void assertResult(String s, StateMachineSetup setup, MatchConfig config) {
		assertThat(s).isNotEmpty();

		State state = new State(setup);
		state.size = s.length();
		state.to = s.length()-1;
		state.elements = IntStream.range(0, s.length())
				.mapToObj(i -> item(i, s.charAt(i)))
				.toArray(Item[]::new);

		config.assertResult(setup.root, state);
	}

	static void assertResult(String s, SequencePattern pattern, MatchConfig config) {
		assertThat(s).isNotEmpty();

		Container target = mockContainer(IntStream.range(0, s.length())
				.mapToObj(i -> item(i, s.charAt(i)))
				.toArray(Item[]::new));

		NonResettingMatcher matcher = pattern.matcherForTesting();

		config.assertResult(matcher, target);
	}

	interface NodeTest {
		StateMachineSetup setup();
	}

	static MatchConfig match(int startPos, boolean expectedResult, int expectedCount) {
		return new MatchConfig(startPos, expectedResult, expectedCount);
	}

	static MatchConfig match(int expectedCount) {
		return new MatchConfig(true, expectedCount);
	}

	static MatchConfig match(boolean expectedResult, int expectedCount) {
		return new MatchConfig(expectedResult, expectedCount);
	}

	static MatchConfig mismatch() {
		return new MatchConfig(false, 0);
	}

	/** Encapsulates the info for expected matches and hits inside a single target sequence */
	static class MatchConfig implements Consumer<State> {
		/** Index in target sequence to start the test search at */
		private final int startPos;
		/** Expected success state */
		private final boolean expectedResult;
		/** Total number of matches we expect */
		private final int expectedCount;
		/** Individual node info regarding last hits etc... */
		private final List<NodeConfig> nodes = new ArrayList<>();
		private final List<CacheConfig> caches = new ArrayList<>();
		/** Results expected to be dispatched */
		private final List<ResultConfig> results = new ArrayList<>();
		/** Pointer to the next expected result entry */
		private int nextResult = 0;
		/** Monitor to be used to track the state machine */
		private Monitor monitor;

		/** Intermediate mappings expected to exist during result assertion */
		private final ResultConfig mapping = new ResultConfig(-1);

		MatchConfig(int startPos, boolean expectedResult, int expectedCount) {
			assertThat(startPos).as("Negative position").isGreaterThanOrEqualTo(0);
			assertThat(expectedCount).as("Negative expected count").isGreaterThanOrEqualTo(0);
			this.startPos = startPos;
			this.expectedResult = expectedResult;
			this.expectedCount = expectedCount;
		}

		MatchConfig(boolean expectedResult, int expectedCount) {
			assertThat(expectedCount).as("Negative expected count").isGreaterThanOrEqualTo(0);
			this.startPos = UNSET_INT;
			this.expectedResult = expectedResult;
			this.expectedCount = expectedCount;
		}

		MatchConfig monitor(Monitor monitor) { this.monitor = monitor; return this; }

		MatchConfig node(NodeConfig node) { nodes.add(requireNonNull(node)); return this; }

		MatchConfig cache(CacheConfig cache) { caches.add(requireNonNull(cache)); return this; }

		/** Map set of indices to nodeId in active mapping */
		MatchConfig map(int nodeId, int...indices) { mapping.map(nodeId, indices); return this; }

		/** Map region of indices to nodeId in active mapping */
		MatchConfig map(int nodeId, Interval indices) { mapping.map(nodeId, indices); return this; }

		/** Conditionally map set of indices to nodeId in active mapping */
		MatchConfig map(boolean condition, int nodeId, int...indices) { if(condition) map(nodeId, indices); return this;}

		/** Add complete result entry to assert */
		MatchConfig result(ResultConfig result) { results.add(requireNonNull(result)); return this; }

		/** Map a number of new entries depending on complex consumer */
		MatchConfig results(int count, ObjIntConsumer<ResultConfig> action) {
			for (int i = 0; i < count; i++) {
				ResultConfig result = SequencePatternTest.result(results.size());
				action.accept(result, i);
				results.add(result);
			}
			return this;
		}

		/** Map all elements of given interval to specified nodeId in separate results */
		MatchConfig results(int nodeId, Interval...regions) {
			for(Interval region : regions) {
				ResultConfig result = SequencePatternTest.result(results.size());
				for (int i = 0; i < region.size(); i++) {
					result.map(nodeId, region.indexAt(i));
				}
				results.add(result);
			}
			return this;
		}

		/** Map all elements of given set to specified nodeId in separate results */
		MatchConfig results(int nodeId, int...indices) {
			for (int i = 0; i < indices.length; i++) {
				ResultConfig result = SequencePatternTest.result(results.size());
				result.map(nodeId, indices[i]);
				results.add(result);
			}
			return this;
		}

		private static String state2Mapping(State state) {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			for(int i=0; i<state.entry; i++) {
				if(i>0) {
					sb.append(',');
				}
				sb.append(state.m_node[i])
					.append("->")
					.append(state.m_pos[i]);
			}
			sb.append('}');
			return sb.toString();
		}

		/** Asserts the dispatched state based on the list of expected results */
		@Override
		public void accept(State state) {
			assertThat(nextResult)
				.as("No more results buffered - only expected %d: %s",
						_int(results.size()), state2Mapping(state)) //FIXME very inefficient, as we create the error message ALWAYS
				.isLessThan(results.size());

			results.get(nextResult++).assertMapping(state);
		}

		private void assertState(State state) {
			assertThat(state.reported)
				.as("Total number of matches")
				.isEqualTo(expectedCount);

			for (CacheConfig cache : caches) {
				cache.assertResult(startPos, state);
			}

			mapping.assertMapping(state);

			for (NodeConfig node : nodes) {
				node.assertResult(state);
			}
		}

		private void prepareState(State state) {

			if(!results.isEmpty()) {
				state.resultHandler(this);
			}

			if(monitor!=null) {
				state.monitor(monitor);
			}
		}

		void assertResult(Node root, State state) {
			assertThat(startPos).as("Negative position").isGreaterThanOrEqualTo(0);
			prepareState(state);

			/*
			 *  Verify correct result (this does not use the full matcher API,
			 *  so we don't need to worry about a state reset messing up our expectations)
			 */
			assertThat(root.match(state, startPos))
				.as("Result for start position %d", _int(startPos))
				.isEqualTo(expectedResult);

			// Now perform deep validation of the final (internal) matcher state
			assertState(state);
		}

		void assertResult(NonResettingMatcher matcher, Container target) {
			prepareState(matcher);

			// Verify correct result
			assertThat(matcher.matches(0, target))
				.as("Result mismatch")
				.isEqualTo(expectedResult);

			/*
			 * Reset only the temporary utility stuff and external references.
			 * We still need the caches for our assertions!!
			 */
			matcher.softReset();

			// Now perform deep validation of the final (internal) matcher state
			assertState(matcher);
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
				assertThat(state.hits[nodeId])
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

		ResultConfig(int index) { this.index = index; }

		ResultConfig map(int nodeId, int...indices) {
			IntStream.of(indices).mapToObj(pos -> Pair.pair(nodeId, pos)).forEach(mapping::add);
			return this;
		}

		ResultConfig map(int nodeId, Interval...indices) {
			Stream.of(indices)
				.flatMapToInt(Interval::stream)
				.mapToObj(pos -> Pair.pair(nodeId, pos))
				.forEach(mapping::add);
			return this;
		}

		ResultConfig map(boolean condition, int nodeId, int...indices) { if(condition) map(nodeId, indices); return this;}
		ResultConfig map(boolean condition, int nodeId, Interval...indices) { if(condition) map(nodeId, indices); return this;}

		void assertMapping(State state) {
			int size = mapping.size();
			assertThat(state.entry)
				.as("Incorrect number of mappings in result #%d", _int(index))
				.isEqualTo(size);

			// Process mappings in their natural occurrence order
			List<Pair<Integer, Integer>> entries = new ObjectArrayList<>(mapping);
			entries.sort((p1, p2) -> p1.second.compareTo(p2.second));

			for (int i = 0; i < size; i++) {
				Pair<Integer, Integer> m = entries.get(i);
				assertThat(state.m_node[i])
					.as("Node id mismatch in mapping at index %d in result #%d",
							_int(i), _int(index))
					.isEqualTo(m.first.intValue());
				assertThat(state.m_pos[i])
					.as("Position mismatch in mapping at index %d in result #%d for node %d",
							_int(i), _int(index), _int(state.m_node[i]))
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
				assertResult(target, setup(), match(startPos, false, 0)
						.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
				);
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
				assertResult(target, setup(), match(startPos, true, 1)
						.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
						.map(NODE_0, startPos, startPos+1)
						.node(node(NODE_0).last(last))
				);
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
				assertResult(target, setup(), match(startPos, true, 1)
						.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
						.map(NODE_0, Interval.of(startPos, startPos+4))
						.node(node(NODE_0).last(last))
				);
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
				assertResult(target, setup(), match(startPos, false, 0)
						.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
				);
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
				assertResult(target, setup(), match(startPos, true, 1)
						.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
						.map(NODE_0, startPos, startPos+1)
						.node(node(NODE_0).last(last))
				);
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
				assertResult(target, setup(), match(startPos, true, 1)
						.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
						.map(NODE_0, matched)
						.node(node(NODE_0).last(last))
				);
			}

		}
	}

	@Test
	void testBuilder() {
		assertThat(SequencePattern.builder()).isNotNull();
	}

	/**
	 * Test family for raw {@link Node} implementations.
	 */
	@Nested
	class ForRawNodes {

		private int id = 0;

		private int id() { return id++; }

		@Nested
		class ForCache {

			private Cache cache;

			@BeforeEach
			void setUp() { cache = new Cache(); }

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

		@Nested
		class ForSingle {

			@Nested
			class WithOneNode implements NodeTest {

				@Override
				public StateMachineSetup setup() {
					StateMachineSetup sms = new StateMachineSetup();
					sms.rawNodes = new IqlNode[1];
					sms.cacheCount = 1;
					sms.root = seq(
							new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
							new Finish(id(), UNSET_LONG, false));
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
					assertResult(target, setup(), match(startPos, expectedResult, expectedResult ? 1 : 0)
							.cache(cache(CACHE_0, true).hits(expectedResult, startPos))
							.map(expectedResult, NODE_0, startPos)
							.node(node(NODE_0).last(expectedResult, startPos))
					);
				}
			}

			@Nested
			class WithTwoNodes implements NodeTest {

				@Override
				public StateMachineSetup setup() {
					StateMachineSetup sms = new StateMachineSetup();
					sms.rawNodes = new IqlNode[2];
					sms.cacheCount = 2;
					sms.root = seq(
							new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
							new Single(id(), mock(IqlNode.class), NODE_1, CACHE_1, NO_MEMBER),
							new Finish(id(), UNSET_LONG, false));
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
					assertResult(target, setup(), match(startPos, expectedResult, expectedResult ? 1 : 0)
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
					);
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
						sms.rawNodes = new IqlNode[1];
						sms.cacheCount = 1;
						sms.limit = limit;
						sms.root = seq(
								new Exhaust(id(), NO_CACHE, true),
								new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
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
						assertResult(target, setup(NO_LIMIT), match(startPos, false, 0)
								.cache(cache(CACHE_0, true)
										.window(startPos, target.length()-1))
						);
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
						assertResult(target, setup(NO_LIMIT), match(startPos, true, 1)
								.cache(cache(CACHE_0, true)
										.window(startPos, target.length()-1)
										.hits(hit))
								.node(node(NODE_0).last(hit))
								.result(result(0).map(NODE_0, hit))
						);
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
						assertResult(target, setup(NO_LIMIT), match(startPos, true, matchCount)
								.cache(cache(CACHE_0, true)
										.window(startPos, target.length()-1)
										.hits(target, EQUALS_X))
								.node(node(NODE_0).last(last))
						);
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
						assertResult(target, setup(limit), match(startPos, true, matchCount)
								.cache(cache(CACHE_0, true)
										.window(startPos, last)
										.hits(target, EQUALS_X))
								.node(node(NODE_0).last(last))
						);
					}
				}

				@Nested
				class ForwardSingleCached {

					StateMachineSetup setup(int limit) {
						StateMachineSetup sms = new StateMachineSetup();
						sms.rawNodes = new IqlNode[1];
						sms.cacheCount = 2;
						sms.limit = limit;
						sms.root = seq(
								new Exhaust(id(), CACHE_0, true),
								new Single(id(), mock(IqlNode.class), NODE_0, CACHE_1, NO_MEMBER),
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
						assertResult(target, setup(NO_LIMIT), match(startPos, false, 0)
								.cache(cache(CACHE_0, true)
										.window(startPos, target.length()-1))
						);
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
						assertResult(target, setup(NO_LIMIT), match(startPos, true, 1)
								.cache(cache(CACHE_0, true)
										.window(startPos, target.length()-1)
										.hits(target, EQUALS_X))
								.node(node(NODE_0).last(hit))
								.result(result(0).map(NODE_0, hit))
						);
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
						assertResult(target, setup(NO_LIMIT), match(startPos, true, matchCount)
								.cache(cache(CACHE_0, true)
										.window(startPos, target.length()-1)
										.hits(target, EQUALS_X))
								.cache(cache(CACHE_1, true)
										.window(startPos, target.length()-1).
										hits(target, EQUALS_X))
								.node(node(NODE_0).last(last))
						);
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
						assertResult(target, setup(limit), match(startPos, true, matchCount)
								.cache(cache(CACHE_0, true)
										.window(startPos, last)
										.hits(target, EQUALS_X))
								.cache(cache(CACHE_1, true)
										.window(startPos, last)
										.hits(target, EQUALS_X))
								.node(node(NODE_0).last(last))
						);
					}
				}

				@Nested
				class BackwardSingle {

					StateMachineSetup setup(int  limit) {
						StateMachineSetup sms = new StateMachineSetup();
						sms.rawNodes = new IqlNode[1];
						sms.cacheCount = 1;
						sms.limit = limit;
						sms.root = seq(
								new Exhaust(id(), NO_CACHE, false),
								new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
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
						assertResult(target, setup(NO_LIMIT), match(startPos, false, 0)
								.cache(cache(CACHE_0, true)
										.window(startPos, target.length()-1))
						);
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
						assertResult(target, setup(NO_LIMIT), match(startPos, true, 1)
								.cache(cache(CACHE_0, true)
										.window(startPos, target.length()-1)
										.hits(target, EQUALS_X))
								.node(node(NODE_0).last(hit))
								.result(result(0).map(NODE_0, hit))
						);
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
						assertResult(target, setup(NO_LIMIT), match(startPos, true, matchCount)
								.cache(cache(CACHE_0, true)
										.window(window)
										.hits(target, EQUALS_X))
								.node(node(NODE_0).last(last))
						);
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
						assertResult(target, setup(limit), match(startPos, true, matchCount)
								.cache(cache(CACHE_0, true)
										.window(last, target.length()-1)
										.hits(target, EQUALS_X))
								.node(node(NODE_0).last(last))
						);
					}
				}

			}

			@Nested
			class WithRegion {

				@Nested
				class ForwardSingle {

					StateMachineSetup setup(Interval region, int limit) {
						StateMachineSetup sms = new StateMachineSetup();
						sms.rawNodes = new IqlNode[1];
						sms.cacheCount = 1;
						sms.limit = limit;
						sms.intervals = new Interval[]{ region };
						sms.root = seq(
								new DynamicClip(id(), mock(IqlMarkerCall.class), REGION_0),
								new Exhaust(id(), NO_CACHE, true),
								new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
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
						assertResult(target, setup(region, NO_LIMIT), match(startPos, false, 0)
								.cache(cache(CACHE_0, true).window(visited))
						);
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
						assertResult(target, setup(region, NO_LIMIT), match(startPos, true, 1)
								.cache(cache(CACHE_0, true).hits(hit))
								.node(node(NODE_0).last(hit))
								.result(result(0).map(NODE_0, hit))
						);
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
						assertResult(target, setup(region, NO_LIMIT), match(startPos, true, matchCount)
								.cache(cache(CACHE_0, true).hits(target, EQUALS_X))
								.node(node(NODE_0).last(last))
						);
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
						assertResult(target, setup(region, limit), match(startPos, true, matchCount)
								.cache(cache(CACHE_0, true)
										.window(startPos, last)
										.hits(target, EQUALS_X))
								.node(node(NODE_0).last(last))
						);
					}
				}

				@Nested
				class ForwardSingleCached {

					StateMachineSetup setup(Interval region, int limit) {
						StateMachineSetup sms = new StateMachineSetup();
						sms.rawNodes = new IqlNode[1];
						sms.cacheCount = 2;
						sms.limit = limit;
						sms.intervals = new Interval[]{ region };
						sms.root = seq(
								new DynamicClip(id(), mock(IqlMarkerCall.class), REGION_0),
								new Exhaust(id(), CACHE_0, true),
								new Single(id(), mock(IqlNode.class), NODE_0, CACHE_1, NO_MEMBER),
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
						assertResult(target, setup(region, NO_LIMIT), match(startPos, false, 0)
								.cache(cache(CACHE_0, true)
										.window(startPos, region.to))
								.cache(cache(CACHE_1, true)
										.window(startPos, region.to))
						);
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
						assertResult(target, setup(region, NO_LIMIT), match(startPos, true, 1)
								.cache(cache(CACHE_0, true)
										.window(startPos, region.to)
										.hits(hit))
								.cache(cache(CACHE_1, true)
										.window(startPos, region.to)
										.hits(hit))
								.node(node(NODE_0).last(hit))
								.result(result(0).map(NODE_0, hit))
						);
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
						assertResult(target, setup(region, NO_LIMIT), match(startPos, true, matchCount)
								.cache(cache(CACHE_0, true)
										.window(startPos, region.to)
										.hits(target, region, EQUALS_X))
								.cache(cache(CACHE_1, true)
										.window(startPos, region.to)
										.hits(target, region, EQUALS_X))
								.node(node(NODE_0).last(last))
						);
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
						assertResult(target, setup(region, limit), match(startPos, true, matchCount)
								.cache(cache(CACHE_0, true)
										.window(startPos, last)
										.hits(target, EQUALS_X))
								.cache(cache(CACHE_1, true)
										.window(startPos, last)
										.hits(target, EQUALS_X))
								.node(node(NODE_0).last(last))
						);
					}
				}

				@Nested
				class BackwardSingle {

					StateMachineSetup setup(Interval region, int limit) {
						StateMachineSetup sms = new StateMachineSetup();
						sms.rawNodes = new IqlNode[1];
						sms.cacheCount = 1;
						sms.limit = limit;
						sms.intervals = new Interval[]{ region };
						sms.root = seq(
								new DynamicClip(id(), mock(IqlMarkerCall.class), REGION_0),
								new Exhaust(id(), NO_CACHE, false),
								new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
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
						assertResult(target, setup(region, NO_LIMIT), match(startPos, false, 0)
								.cache(cache(CACHE_0, true)
										.window(startPos, region.to))
						);
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
						assertResult(target, setup(region, NO_LIMIT), match(startPos, true, 1)
								.cache(cache(CACHE_0, true)
										.window(startPos, region.to)
										.hits(target, region, EQUALS_X))
								.node(node(NODE_0).last(hit))
								.result(result(0).map(NODE_0, hit))
						);
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
						assertResult(target, setup(region, NO_LIMIT), match(startPos, true, matchCount)
								.cache(cache(CACHE_0, true)
										.window(startPos, region.to)
										.hits(target, region, EQUALS_X))
								.node(node(NODE_0).last(last))
						);
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
						assertResult(target, setup(region, limit), match(startPos, true, matchCount)
								.cache(cache(CACHE_0, true)
										.window(last, region.to)
										.hits(target, EQUALS_X))
								.node(node(NODE_0).last(last))
						);
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
							sms.rawNodes = new IqlNode[1];
							sms.cacheCount = 1;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
											CMIN, CMAX, SequencePattern.GREEDY, BUFFER_0, BUFFER_1, -1),
									new Finish(id(), UNSET_LONG, false));
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
							sms.rawNodes = new IqlNode[2];
							sms.cacheCount = 2;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
											CMIN, CMAX, SequencePattern.GREEDY, BUFFER_0, BUFFER_1, -1),
									new Single(id(), mock(IqlNode.class), NODE_1, CACHE_1, NO_MEMBER),
									new Finish(id(), UNSET_LONG, false));
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

							assertResult(target, setup(), match(startPos, true, 1)
									.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
									.map(NODE_0, matched1)
									.node(node(NODE_0).last(last1))

									.cache(cache(CACHE_1, true).window(visited2).hits(target, EQUALS_X))
									.map(NODE_1, matched2)
									.node(node(NODE_1).last(last2))
							);
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
							sms.rawNodes = new IqlNode[1];
							sms.cacheCount = 1;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
											CMIN, CMAX, SequencePattern.POSSESSIVE, BUFFER_0, BUFFER_1, -1),
									new Finish(id(), UNSET_LONG, false));
							sms.matchers = matchers(matcher(0, EQUALS_X));
							return sms;
						}

					}

					@Nested
					class Expansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.rawNodes = new IqlNode[2];
							sms.cacheCount = 2;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
											CMIN, CMAX, SequencePattern.POSSESSIVE, BUFFER_0, BUFFER_1, -1),
									new Single(id(), mock(IqlNode.class), NODE_1, CACHE_1, NO_MEMBER),
									new Finish(id(), UNSET_LONG, false));
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

							assertResult(target, setup(), match(startPos, false, 0)
									.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
									.node(node(NODE_0).last(last))

									.cache(cache(CACHE_1, true).window(visited2))
							);
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

							assertResult(target, setup(), match(startPos, true, 1)
									.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
									.map(NODE_0, matched1)
									.node(node(NODE_0).last(last1))

									.cache(cache(CACHE_1, true).window(visited2).hits(target, EQUALS_X))
									.map(NODE_1, matched2)
									.node(node(NODE_1).last(last2))
							);
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
							sms.rawNodes = new IqlNode[1];
							sms.cacheCount = 1;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
											CMIN, CMAX, SequencePattern.RELUCTANT, BUFFER_0, BUFFER_1, -1),
									new Finish(id(), UNSET_LONG, false));
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
							assertResult(target, setup(), match(startPos, false, 0)
									.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
							);
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
							assertResult(target, setup(), match(startPos, true, 1)
									.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
									.map(NODE_0, startPos, startPos+1)
									.node(node(NODE_0).last(last))
							);
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
							assertResult(target, setup(), match(startPos, true, 1)
									.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
									.map(NODE_0, Interval.of(startPos, startPos+1))
									.node(node(NODE_0).last(last))
							);
						}

					}

					@Nested
					class WithProxy implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.rawNodes = new IqlNode[1];
							sms.cacheCount = 1;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
											CMIN, CMAX, SequencePattern.RELUCTANT, BUFFER_0, BUFFER_1, -1),
									new Proxy(NODE_1), // we need this to motivate the reluctant expansion
									new Finish(id(), UNSET_LONG, false));
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
							assertResult(target, setup(), match(startPos, true, 1)
									.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
									.map(NODE_0, startPos, startPos+1)
									.node(node(NODE_0).last(last))
							);
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
							assertResult(target, setup(), match(startPos, true, 1)
									.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
									.map(NODE_0, Interval.of(startPos, startPos+4))
									.node(node(NODE_0).last(last))
							);
						}

					}

					@Nested
					class Expansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.rawNodes = new IqlNode[2];
							sms.cacheCount = 2;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
											CMIN, CMAX, SequencePattern.RELUCTANT, BUFFER_0, BUFFER_1, -1),
									new Single(id(), mock(IqlNode.class), NODE_1, CACHE_1, NO_MEMBER),
									new Finish(id(), UNSET_LONG, false));
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

							assertResult(target, setup(), match(startPos, false, 0)
									.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
									.node(node(NODE_0).last(last))

									.cache(cache(CACHE_1, true).window(visited2))
							);
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

							assertResult(target, setup(), match(startPos, true, 1)
									.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
									.map(NODE_0, matched1)
									.node(node(NODE_0).last(last1))

									.cache(cache(CACHE_1, true).window(visited2).hits(target, EQUALS_X))
									.map(NODE_1, matched2)
									.node(node(NODE_1).last(last2))
							);
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
							sms.rawNodes = new IqlNode[1];
							sms.cacheCount = 1;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
											CMIN, CINF, SequencePattern.GREEDY, BUFFER_0, BUFFER_1, -1),
									new Finish(id(), UNSET_LONG, false));
							sms.matchers = matchers(matcher(0, EQUALS_X));
							return sms;
						}

					}

					@Nested
					class Expansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.rawNodes = new IqlNode[2];
							sms.cacheCount = 2;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
											CMIN, CINF, SequencePattern.GREEDY, BUFFER_0, BUFFER_1, -1),
									new Single(id(), mock(IqlNode.class), NODE_1, CACHE_1, NO_MEMBER),
									new Finish(id(), UNSET_LONG, false));
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

							assertResult(target, setup(), match(startPos, true, 1)
									.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
									.map(NODE_0, matched1)
									.node(node(NODE_0).last(last1))

									.cache(cache(CACHE_1, true).window(visited2).hits(target, EQUALS_X))
									.map(NODE_1, matched2)
									.node(node(NODE_1).last(last2))
							);
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
							sms.rawNodes = new IqlNode[1];
							sms.cacheCount = 1;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
											CMIN, CINF, SequencePattern.POSSESSIVE, BUFFER_0, BUFFER_1, -1),
									new Finish(id(), UNSET_LONG, false));
							sms.matchers = matchers(matcher(0, EQUALS_X));
							return sms;
						}

					}

					@Nested
					class OverExpansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.rawNodes = new IqlNode[2];
							sms.cacheCount = 2;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
											CMIN, CINF, SequencePattern.POSSESSIVE, BUFFER_0, BUFFER_1, -1),
									new Single(id(), mock(IqlNode.class), NODE_1, CACHE_1, NO_MEMBER),
									new Finish(id(), UNSET_LONG, false));
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

							assertResult(target, setup(), match(startPos, false, 0)
									.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
									.node(node(NODE_0).last(last))

									.cache(cache(CACHE_1, true).window(visited2))
							);
						}
					}

					@Nested
					class Expansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.rawNodes = new IqlNode[2];
							sms.cacheCount = 2;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
											CMIN, CINF, SequencePattern.POSSESSIVE, BUFFER_0, BUFFER_1, -1),
									new Single(id(), mock(IqlNode.class), NODE_1, CACHE_1, NO_MEMBER),
									new Finish(id(), UNSET_LONG, false));
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

							assertResult(target, setup(), match(startPos, true, 1)
									.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X))
									.map(NODE_0, matched1)
									.node(node(NODE_0).last(last1))

									.cache(cache(CACHE_1, true).window(visited2).hits(target, EQUALS_Y))
									.map(NODE_1, matched2)
									.node(node(NODE_1).last(last2))
							);
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
							sms.rawNodes = new IqlNode[1];
							sms.cacheCount = 1;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
											CMIN, CINF, SequencePattern.RELUCTANT, BUFFER_0, BUFFER_1, -1),
									new Finish(id(), UNSET_LONG, false));
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
							assertResult(target, setup(), match(startPos, false, 0)
									.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
							);
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
							assertResult(target, setup(), match(startPos, true, 1)
									.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
									.map(NODE_0, startPos, startPos+1)
									.node(node(NODE_0).last(last))
							);
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
							assertResult(target, setup(), match(startPos, true, 1)
									.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
									.map(NODE_0, Interval.of(startPos, startPos+1))
									.node(node(NODE_0).last(last))
							);
						}

					}

					@Nested
					class WithProxy implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.rawNodes = new IqlNode[1];
							sms.cacheCount = 1;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
											CMIN, CINF, SequencePattern.RELUCTANT, BUFFER_0, BUFFER_1, -1),
									new Proxy(NODE_1), // we need this to motivate the reluctant expansion
									new Finish(id(), UNSET_LONG, false));
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
							assertResult(target, setup(), match(startPos, true, 1)
									.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
									.map(NODE_0, startPos, startPos+1)
									.node(node(NODE_0).last(last))
							);
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
							assertResult(target, setup(), match(startPos, true, 1)
									.cache(cache(CACHE_0, true).window(window).hits(target, EQUALS_X))
									.map(NODE_0, matched)
									.node(node(NODE_0).last(last))
							);
						}

					}

					@Nested
					class Expansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.rawNodes = new IqlNode[2];
							sms.cacheCount = 2;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(id(), mock(IqlQuantifier.class),
											new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
											CMIN, CINF, SequencePattern.RELUCTANT, BUFFER_0, BUFFER_1, -1),
									new Single(id(), mock(IqlNode.class), NODE_1, CACHE_1, NO_MEMBER),
									new Finish(id(), UNSET_LONG, false));
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

							assertResult(target, setup(), match(startPos, false, 0)
									.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
									.node(node(NODE_0).last(last))

									.cache(cache(CACHE_1, true).window(visited2))
							);
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

							assertResult(target, setup(), match(startPos, true, 1)
									.cache(cache(CACHE_0, true).window(visited1).hits(target, EQUALS_X_IC))
									.map(NODE_0, matched1)
									.node(node(NODE_0).last(last1))

									.cache(cache(CACHE_1, true).window(visited2).hits(target, EQUALS_X))
									.map(NODE_1, matched2)
									.node(node(NODE_1).last(last2))
							);
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
					sms.rawNodes = new IqlNode[2];
					sms.cacheCount = 2;
					sms.root = seq(
							branch(0,
									new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
									new Single(id(), mock(IqlNode.class), NODE_1, CACHE_1, NO_MEMBER)),
							new Finish(id(), UNSET_LONG, false));
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

					assertResult(target, setup(), match(startPos, false, 0)
							.cache(cache(CACHE_0, true).window(startPos))

							.cache(cache(CACHE_1, true).window(startPos))
					);
				}

				@CsvSource({
					"A, 0",
					"AA, 0",
					"AA, 1",
					"BA, 1",
				})
				@ParameterizedTest(name="{index}: A|B in [{0}], start at {1}")
				void testOptionA(String target, int startPos) {

					assertResult(target, setup(), match(startPos, true, 1)
							.cache(cache(CACHE_0, true).window(startPos).hits(startPos))
							.node(node(NODE_0).last(startPos))
							.cache(cache(CACHE_1, true).window(startPos))
							.result(result(0).map(NODE_0, startPos))
					);
				}

				@CsvSource({
					"B, 0",
					"BB, 0",
					"BB, 1",
					"AB, 1",
				})
				@ParameterizedTest(name="{index}: A|B in [{0}], start at {1}")
				void testOptionB(String target, int startPos) {

					assertResult(target, setup(), match(startPos, true, 1)
							.cache(cache(CACHE_0, true).window(startPos)) // option A must have been visited

							.cache(cache(CACHE_1, true).window(startPos).hits(startPos))
							.node(node(NODE_1).last(startPos))
							.result(result(0).map(NODE_1, startPos))
					);
				}
			}

			@Nested
			class GreedyOptional implements NodeTest {

				@Override
				public StateMachineSetup setup() {
					StateMachineSetup sms = new StateMachineSetup();
					sms.rawNodes = new IqlNode[1];
					sms.cacheCount = 1;
					sms.limit = 1;
					sms.root = seq(
							branch(0,
									new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER),
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

					assertResult(target, setup(), match(startPos, true, 1)
							.cache(cache(CACHE_0, true).window(startPos).hits(startPos))
							.node(node(NODE_0).last(startPos))
							.result(result(0).map(NODE_0, startPos))
					);
				}

				@CsvSource({
					"B, 0",
					"BB, 0",
					"BB, 1",
					"AB, 1",
				})
				@ParameterizedTest(name="{index}: A'{0,1}' in [{0}], start at {1}")
				void testZeroWidthAssertion(String target, int startPos) {

					assertResult(target, setup(), match(startPos, true, 1)
							.cache(cache(CACHE_0, true).window(startPos)) // option A must have been visited
					);
				}
			}

			@Nested
			class ReluctantOptional implements NodeTest {

				@Override
				public StateMachineSetup setup() {
					StateMachineSetup sms = new StateMachineSetup();
					sms.rawNodes = new IqlNode[2];
					sms.cacheCount = 2;
					sms.root = seq(
							branch(0,
									null,
									new Single(id(), mock(IqlNode.class), NODE_0, CACHE_0, NO_MEMBER)),
							new Single(id(), mock(IqlNode.class), NODE_1, CACHE_1, NO_MEMBER), // needed to force reluctant expansion
							new Finish(id(), UNSET_LONG, false));
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

					assertResult(target, setup(), match(startPos, true, 1)
							.cache(cache(CACHE_0, true).window(startPos).hits(startPos))
							.node(node(NODE_0).last(startPos))

							.cache(cache(CACHE_1, true).window(startPos, startPos+1).hits(startPos+1))
							.node(node(NODE_1).last(startPos+1))
							.result(result(0)
									.map(NODE_0, startPos)
									.map(NODE_1, startPos+1))
					);
				}

				@CsvSource({
					"B, 0",
					"BB, 0",
					"BB, 1",
					"AB, 1",
				})
				@ParameterizedTest(name="{index}: A'{0,1}'?B in [{0}], start at {1}")
				void testZeroWidthAssertion(String target, int startPos) {

					assertResult(target, setup(), match(startPos, true, 1)
							.cache(cache(CACHE_1, true).window(startPos).hits(startPos))
							.node(node(NODE_1).last(startPos))
							.result(result(0).map(NODE_1, startPos))
					);
				}
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
						nodes.add(new Exhaust(id(), NO_CACHE, true));
						nodes.add(new Single(id++, mock(IqlNode.class), i, i, NO_MEMBER));
					}
					nodes.add(new Finish(id(), limit, false));
					return nodes.toArray(new Node[0]);
				}

				@SuppressWarnings("unchecked")
				private StateMachineSetup setup(int limit, CharPredicate...predicates) {
					int nodeCount = predicates.length;
					StateMachineSetup sms = new StateMachineSetup();
					sms.rawNodes = new IqlNode[nodeCount];
					sms.cacheCount = nodeCount;
					sms.limit = limit;
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

					assertResult(target, setup(NO_LIMIT, EQUALS_A, EQUALS_B),
							match(startPos, false, 0)
							.cache(cache(CACHE_0, true)
									.window(visitedA)
									.hits(hitsA))
							.cache(cache(CACHE_1, true)
									.window(visitedB))
					);
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

					assertResult(target, setup(NO_LIMIT, EQUALS_A, EQUALS_B),
							match(startPos, true, 1)
							.cache(cache(CACHE_0, true)
									.window(visitedA)
									.hits(hitA))
							.cache(cache(CACHE_1, true)
									.window(visitedB)
									.hits(hitB))
							.result(result(0)
									.map(NODE_0, hitA)
									.map(NODE_1, hitB))
					);
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

					assertResult(target, setup(NO_LIMIT, EQUALS_A, EQUALS_B),
							match(startPos, true, 1)
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
					);
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

					assertResult(target, setup(NO_LIMIT, EQUALS_A, EQUALS_B),
							match(startPos, true, 2)
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
					);
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

					assertResult(target, setup(NO_LIMIT, EQUALS_A, EQUALS_B),
							match(startPos, true, 2)
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
					);
				}

				//TODO scan for multiple matches (at least 2)
			}

			@Nested
			class BranchAndRepetition {
				private StateMachineSetup setup(Node...options) {
					StateMachineSetup sms = new StateMachineSetup();
					sms.rawNodes = new IqlNode[2];
					sms.cacheCount = 2;
					sms.root = seq(
							branch(0, options),
							new Finish(id(), UNSET_LONG, false));
					sms.matchers = matchers(
							matcher(0, EQUALS_A),
							matcher(1, EQUALS_B));
					return sms;
				}


				//TODO
			}
		}

	}

	//TODO verify that IQLElement instances get processed into the correct node configurations
	/**
	 * Test family for the {@link SequenceQueryProcessor}'s creation methods.
	 */
	@Nested
	class ForProcessor {

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
				assertResult(target,
						builder(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X')))).build(),
						mismatch());
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
				assertResult(target,
						builder(IqlTestUtils.node(NO_LABEL, NO_MARKER,
								constraint(eq_exp('X')))).build(),
						match(1)
							.cache(cache(CACHE_0, true)
									.window(0, target.length()-1)
									.hits(hit))
							.result(result(0)
									.map(NODE_0, hit))
				);
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
					assertResult(target,
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isFirst"),
									constraint(eq_exp('X')))).build(),
							match(1)
								.cache(cache(CACHE_0, false)
										.window(target)
										.set(0)
										.hits(0))
								.result(result(0).map(NODE_0, 0))
					);
				}

				@ParameterizedTest(name="{index}: [isFirst,X] in {0}, hit={1}")
				@CsvSource({
					"-",
					"-X",
					"-XX",
				})
				@DisplayName("Node mismatch at first position")
				void testIsFirstFail(String target) {
					assertResult(target,
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isFirst"),
									constraint(eq_exp('X')))).build(),
							mismatch()
							.cache(cache(CACHE_0, false)
									.window(target)
									.set(0))
					);
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
					assertResult(target,
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isLast"),
									constraint(eq_exp('X')))).build(),
							match(1)
								.cache(cache(CACHE_0, false)
										.window(target)
										.set(last)
										.hits(last))
								.result(result(0).map(NODE_0, target.length()-1))
					);
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
					assertResult(target,
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isLast"),
									constraint(eq_exp('X')))).build(),
							mismatch()
								.cache(cache(CACHE_0, false)
										.window(target)
										.set(last))
					);
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
					assertResult(target,
							// Remember that markers use 1-based value space
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isAt", _int(pos+1)),
									constraint(eq_exp('X')))).build(),
							match(1)
								.cache(cache(CACHE_0, false)
										.window(0, last)
										.set(pos)
										.hits(pos))
								.result(result(0).map(NODE_0, pos))
					);
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
					assertResult(target,
							// Remember that markers use 1-based value space
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isAt", _int(pos+1)),
									constraint(eq_exp('X')))).build(),
							mismatch()
								.cache(cache(CACHE_0, false)
										.window(target)
										.set(pos))
					);
				}

				@ParameterizedTest(name="{index}: [isAfter({1}),X] in {0}, hits={2}")
				@CsvSource({
					"XX, 1, {1}",
					"XXX, 1, {1;2}",
					"XXX, 2, {2}",
				})
				@DisplayName("Node after specific position")
				void testIsAfter(String target, int arg, @IntervalArrayArg Interval[] hits) {
					assertResult(target,
							// Remember that markers use 1-based value space
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isAfter", _int(arg)),
									constraint(eq_exp('X')))).build(),
							match(hits.length)
								.cache(cache(CACHE_0, false)
										.window(0, target.length()-1)
										.set(hits)
										.hits(hits))
								.results(NODE_0, hits)
					);
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
					assertResult(target,
							// Remember that markers use 1-based value space
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isAfter", _int(arg)),
									constraint(eq_exp('X')))).build(),
							mismatch()
								.cache(cache(CACHE_0, false)
										.window(target)
										.set(Interval.of(arg, last)))
					);
				}

				@ParameterizedTest(name="{index}: [isBefore({1}),X] in {0}, hits={2}")
				@CsvSource({
					"XX, 2, {0}",
					"XXX, 2, {0}",
					"XXX, 3, {0;1}",
				})
				@DisplayName("Node before specific position")
				void testIsBefore(String target, int arg, @IntervalArrayArg Interval[] hits) {
					assertResult(target,
							// Remember that markers use 1-based value space
							builder(IqlTestUtils.node(NO_LABEL, mark("isBefore", _int(arg)), constraint(eq_exp('X')))).build(),
							match(hits.length)
								.cache(cache(CACHE_0, false)
										.window(0, target.length()-1)
										.set(hits)
										.hits(hits))
								.results(NODE_0, hits)
					);
				}

				@ParameterizedTest(name="{index}: [isBefore({1}),X] in {0}, hits={2}")
				@CsvSource({
					"-X, 2",
					"-XX, 2",
					"--X, 3",
				})
				@DisplayName("Node mismatch before specific position")
				void testIsBeforeFail(String target, int arg) {
					assertResult(target,
							// Remember that markers use 1-based value space
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isBefore", _int(arg)),
									constraint(eq_exp('X')))).build(),
							mismatch()
								.cache(cache(CACHE_0, false)
										.window(target)
										.set(Interval.of(0, arg-2)))
					);
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
					assertResult(target,
							// Remember that markers use 1-based value space
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isNotAt", _int(arg)),
									constraint(eq_exp('X')))).build(),
							match(hits.length)
								.cache(cache(CACHE_0, false)
										.window(target)
										.set(Interval.of(0, arg-2))
										.set(Interval.of(arg, target.length()-1))
										.hits(hits))
								.results(NODE_0, hits)
					);
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
					assertResult(target,
							// Remember that markers use 1-based value space
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isNotAt", _int(arg)),
									constraint(eq_exp('X')))).build(),
							mismatch()
								.cache(cache(CACHE_0, false)
										.window(target)
										.set(Interval.of(0, arg-2))
										.set(Interval.of(arg, target.length()-1)))
					);
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
					assertResult(target,
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isInside", _int(from), _int(to)),
									constraint(eq_exp('X')))).build(),
							match(region.size())
								.cache(cache(CACHE_0, false)
										.window(target)
										.set(region)
										.hits(region))
								.results(NODE_0, region.asArray())
					);
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
					// Remember that markers use 1-based value space
					assertResult(target,
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isInside", _int(from), _int(to)),
									constraint(eq_exp('X')))).build(),
							match(1)
								.cache(cache(CACHE_0, false)
										.window(target)
										.set(Interval.of(from-1, to-1))
										.hits(hit))
								.result(result(0).map(NODE_0, hit))
					);
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
					// Remember that markers use 1-based value space
					assertResult(target,
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isInside", _int(from), _int(to)),
									constraint(eq_exp('X')))).build(),
							match(2)
								.cache(cache(CACHE_0, true)
										.window(target)
										.hits(hits))
								.results(NODE_0, hits)
					);
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
					// Remember that markers use 1-based value space
					assertResult(target,
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isInside", _int(from), _int(to)),
									constraint(eq_exp('X')))).build(),
							mismatch()
								.cache(cache(CACHE_0, false)
										.window(target)
										.set(Interval.of(from-1, to-1)))
					);
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
					// Remember that markers use 1-based value space
					assertResult(target,
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isOutside", _int(from), _int(to)),
									constraint(eq_exp('X')))).build(),
							match(hits.length)
								.cache(cache(CACHE_0, false)
										.window(target)
										.setForWindow()
										.unset(Interval.of(from-1, to-1))
										.hits(hits))
								.results(NODE_0, hits)
					);
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
					// Remember that markers use 1-based value space
					assertResult(target,
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isOutside", _int(from), _int(to)),
									constraint(eq_exp('X')))).build(),
							match(1)
								.cache(cache(CACHE_0, false)
										.window(target)
										.setForWindow()
										.unset(Interval.of(from-1, to-1))
										.hits(hit))
								.result(result(0).map(NODE_0, hit))
					);
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
					// Remember that markers use 1-based value space
					assertResult(target,
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isOutside", _int(from), _int(to)),
									constraint(eq_exp('X')))).build(),
							match(2)
								.cache(cache(CACHE_0, false)
										.window(target)
										.setForWindow()
										.unset(Interval.of(from-1, to-1))
										.hits(hits))
								.results(NODE_0, hits)
					);
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
					// Remember that markers use 1-based value space
					assertResult(target,
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isOutside", _int(from), _int(to)),
									constraint(eq_exp('X')))).build(),
							mismatch()
								.cache(cache(CACHE_0, false)
										.window(target)
										.setForWindow()
										.unset(Interval.of(from-1, to-1)))
					);
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
					// Remember that markers use 1-based value space
					assertResult(target,
							builder(IqlTestUtils.node(NO_LABEL,
									mark("isAfter", _int(loc)),
									constraint(eq_exp('X'))))
							// Make sure we can properly track the scan progress via forced cache!
							.cacheAll(true).build(),
							match(hits.size())
								// Use the scan cache for verification
								.cache(cache(CACHE_1, false)
										.window(target)
										.set(visited)
										.hits(hits))
					);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										negated()
										)).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, true)
											.window(target))
									// Cache of the negated search
									.cache(cache(CACHE_1, true)
											.window(target)
											.hitsForWindow())
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										negated()
										)).build(),
								mismatch()
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
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										all()
										)).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, true)
											.window(target)
											.hitsForWindow())
									//TODO once we added flag to disable mapping for universal quantification, add check here against mapping
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										all()
										)).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, true)
											.window(Interval.of(0, gap))
											.hits(Interval.of(0, gap-1)))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, CONTINUOUS)
										)).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, DISCONTINUOUS)
										)).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, CONTINUOUS)
										)).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, DISCONTINUOUS)
										)).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, CONTINUOUS)
										)).build(),
								match(2)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits1)
											.hits(hits2))
									.result(result(0).map(NODE_0, hits1))
									.result(result(1).map(NODE_0, hits2))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, DISCONTINUOUS)
										)).build(),
								match(hits.length)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.results(hits.length, (r, i) -> r.map(NODE_0, hits[i]))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, CONTINUOUS)
										)).build(),
								match(matches)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.results(matches, (r, i) -> r.map(NODE_0, Interval.of(
											hits.from+i, hits.from+i+count-1)))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, DISCONTINUOUS)
										)).build(),
								match(hits.length)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.results(hits.length, (r, i) -> r.map(NODE_0, hits[i]))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, CONTINUOUS)
										)).flag(MatchFlag.DISJOINT).build(),
								match(matches)
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
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										exact(count, DISCONTINUOUS)
										)).flag(MatchFlag.DISJOINT).build(),
								match(hits.length)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(target, visited, EQUALS_X))
									.results(hits.length, (r, i) -> r.map(NODE_0, hits[i]))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atLeastGreedy(count, CONTINUOUS)
										)
								).limit(1).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atLeastGreedy(count, DISCONTINUOUS)
										)
								).limit(1).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atLeastGreedy(count, CONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(candidates))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atLeastGreedy(count, DISCONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(candidates))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atLeastGreedy(count, CONTINUOUS)
										)
								).build(),
								match(2)
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
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atLeastGreedy(count, DISCONTINUOUS)
										)
								).build(),
								match(hits.length)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.results(hits.length, (r, i) -> r.map(NODE_0, hits[i]))
						);
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
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(unordered(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atLeastGreedy(count, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).limit(1).build(), // we don't need multiple matches for confirmation
								match(1)
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
						);
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
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(unordered(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atLeastGreedy(count, DISCONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).limit(1).build(), // we don't need multiple matches for confirmation
								match(1)
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
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atLeastReluctant(count, CONTINUOUS)
										)
								).limit(1).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atLeastReluctant(count, DISCONTINUOUS)
										)
								).limit(1).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atLeastReluctant(count, CONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(candidates))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atLeastReluctant(count, DISCONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(candidates))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atLeastReluctant(count, CONTINUOUS)
										)
								).build(),
								match(2)
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
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atLeastReluctant(count, DISCONTINUOUS)
										)
								).build(),
								match(hits.length)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.results(hits.length, (r, i) -> r.map(NODE_0, hits[i]))
						);
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
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(set(adjacent,
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atLeastReluctant(count, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).limit(1).build(), // we don't need multiple matches for confirmation
								match(1)
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
						);
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
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(set(adjacent,
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atLeastReluctant(count, DISCONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).limit(1).build(), // we don't need multiple matches for confirmation
								match(1)
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
						);
					}

					//TODO no discontinuous version of this one?
					@Test
					@DisplayName("verify reluctant expansion with multiple nodes and matches")
					void testReluctantExpansion() {
						final String target = "-XXxXXx-";
						assertResult(target,
								builder(adjacent(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atLeastReluctant(2, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).build(),
								match(4)
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
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atLeastPossessive(count, CONTINUOUS)
										)
								).limit(1).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						//TODO complete
					})
					@DisplayName("Node with a minimum multiplicity [possessive mode, single hit, limit, discontinuous]")
					void testPossessiveDiscontinuous(String target, int count,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atLeastPossessive(count, DISCONTINUOUS)
										)
								).limit(1).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atLeastPossessive(count, CONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(candidates))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atLeastPossessive(count, DISCONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(candidates))
						);
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
						assertResult(target,
								builder(ordered(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atLeastPossessive(count, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).build(),
								mismatch()
									// Underlying cache of first node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited1)
											.hits(candidates))
									// Underlying cache of second node
									.cache(cache(CACHE_1, false)
											.set(visited2)
											.window(target))
						);
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
						//TODO complete
					})
					@DisplayName("Mismatch due to possessive consumption [ordered, discontinuous]")
					void testPossessiveFail2Discontinuous(String target, int count,
							@IntervalArg Interval visited1,
							@IntervalArg Interval visited2) {
						// 'Repetition' node sets minSize so that scan can abort early
						assertResult(target,
								builder(ordered(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atLeastPossessive(count, DISCONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).build(),
								mismatch()
									// Underlying cache of first node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited1)
											.hits(target, visited1, EQUALS_X_IC))
									// Underlying cache of second node
									.cache(cache(CACHE_1, false)
											.set(visited2)
											.window(target))
						);
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
						assertResult(target,
								builder(adjacent(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atLeastPossessive(count, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).build(),
								mismatch()
									// Underlying cache of first node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited1)
											.hits(candidates))
									// Underlying cache of second node
									.cache(cache(CACHE_1, false)
											.set(visited2)
											.window(target))
						);
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
						assertResult(target,
								builder(adjacent(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atLeastPossessive(count, DISCONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).build(),
								mismatch()
									// Underlying cache of first node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited1)
											.hits(target, visited1, EQUALS_X_IC))
									// Underlying cache of second node
									.cache(cache(CACHE_1, false)
											.set(visited2)
											.window(target))
						);
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
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(set(adjacent,
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atLeastPossessive(count, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp(c2))))
								).build(), // we don't need multiple matches for confirmation
								match(hits1.length)
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
						);
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
						assertThat(hits1).hasSameSizeAs(hit2);

						/*
						 * We expect NODE_1 to aggressively consume slots with
						 * no regards for NODE_0, so that in contrast to reluctant mode
						 * we will miss some multi-match situations.
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(set(adjacent,
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atLeastPossessive(count, DISCONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp(c2))))
								).build(), // we don't need multiple matches for confirmation
								match(hits1.length)
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
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atMostGreedy(count, CONTINUOUS)
										)
								).limit(1).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atMostGreedy(count, DISCONTINUOUS)
										)
								).limit(1).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atMostGreedy(count, CONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(candidates))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atMostGreedy(count, DISCONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(candidates))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atMostGreedy(count, CONTINUOUS)
										)
								).build(),
								match(hits.length)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.results(NODE_0, hits)
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atMostGreedy(count, DISCONTINUOUS)
										)
								).build(),
								match(hits.length)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.results(hits.length, (r, i) -> r.map(NODE_0, hits[i]))
						);
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
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(unordered(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atMostGreedy(count, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).limit(1).build(), // we don't need multiple matches for confirmation
								match(1)
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
						);
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
						//TODO complete
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
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(unordered(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atMostGreedy(count, DISCONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).limit(1).build(), // we don't need multiple matches for confirmation
								match(1)
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
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atMostReluctant(count, CONTINUOUS)
										)
								).limit(1).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						//TODO complete
					})
					@DisplayName("Node with a maximum multiplicity [reluctant mode, single hit, limit, discontinuous]")
					void testReluctantDiscontinuous(String target, int count,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atMostReluctant(count, DISCONTINUOUS)
										)
								).limit(1).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atMostReluctant(count, CONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(candidates))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atMostReluctant(count, DISCONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(candidates))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atMostReluctant(count, CONTINUOUS)
										)
								).build(),
								match(hits.length)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.results(NODE_0, hits)
						);
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
						//TODO complete
					})
					@DisplayName("Node with a maximum multiplicity [reluctant mode, multiple hits, discontinuous]")
					void testReluctantMultipleDiscontinuous(String target, int count,
							@IntervalArrayArg Interval[] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atMostReluctant(count, DISCONTINUOUS)
										)
								).build(),
								match(hits.length)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.results(NODE_0, hits)
						);
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
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(set(adjacent,
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atMostReluctant(count, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).limit(1).build(), // we don't need multiple matches for confirmation
								match(1)
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
						);
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
						//TODO complete
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
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(set(adjacent,
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atMostReluctant(count, DISCONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).limit(1).build(), // we don't need multiple matches for confirmation
								match(1)
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
						);
					}

					@Test
					@DisplayName("verify reluctant expansion with multiple nodes and matches")
					void testReluctantExpansion() {
						final String target = "-XXxXXx-";
						assertResult(target,
								builder(adjacent(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atMostReluctant(2, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).build(),
								match(4)
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
						);
					}

					@Test
					@DisplayName("verify reluctant expansion with multiple nodes and matches [discontinuous]")
					void testReluctantExpansionDiscontinuous() {
						//TODO complete
						final String target = "-XXxXXx-";
						assertResult(target,
								builder(adjacent(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atMostReluctant(2, DISCONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).build(),
								match(4)
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
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atMostPossessive(count, CONTINUOUS)
										)
								).limit(1).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						//TODO complete
					})
					@DisplayName("Node with a maximum multiplicity [possessive mode, single hit, limit, discontinuous]")
					void testPossessiveDiscontinuous(String target, int count,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atMostPossessive(count, DISCONTINUOUS)
										)
								).limit(1).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atMostPossessive(count, CONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(candidates))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										atMostPossessive(count, DISCONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(candidates))
						);
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
						assertResult(target,
								builder(ordered(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atMostPossessive(count, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).build(),
								mismatch()
									// Underlying cache of first node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited1)
											.hits(candidates))
									// Underlying cache of second node
									.cache(cache(CACHE_1, false)
											.set(visited2)
											.window(target))
						);
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
						assertResult(target,
								builder(ordered(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atMostPossessive(count, DISCONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).build(),
								mismatch()
									// Underlying cache of first node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited1)
											.hits(target, visited1, EQUALS_X_IC))
									// Underlying cache of second node
									.cache(cache(CACHE_1, false)
											.set(visited2)
											.window(target))
						);
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
						assertResult(target,
								builder(adjacent(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atMostPossessive(count, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).build(),
								mismatch()
									// Underlying cache of first node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited1)
											.hits(candidates))
									// Underlying cache of second node
									.cache(cache(CACHE_1, false)
											.set(visited2)
											.window(target))
						);
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
						assertResult(target,
								builder(adjacent(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atMostPossessive(count, DISCONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).build(),
								mismatch()
									// Underlying cache of first node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited1)
											.hits(target, visited1, EQUALS_X_IC))
									// Underlying cache of second node
									.cache(cache(CACHE_1, false)
											.set(visited2)
											.window(target))
						);
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
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(set(adjacent,
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atMostPossessive(count, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp(c2))))
								).build(),
								match(hits1.length)
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
						);
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
						assertThat(hits1).as("Different match counters").hasSameSizeAs(hit2);

						/*
						 * We expect NODE_1 to aggressively consume slots with
						 * no regards for NODE_0, so that in contrast to reluctant mode
						 * we will miss some multi-match situations.
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(set(adjacent,
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), atMostPossessive(count, DISCONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp(c2))))
								).build(), // we don't need multiple matches for confirmation
								match(hits1.length)
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
						);
					}

				}

				@Nested
				class Range {

					@ParameterizedTest(name="{index}: <{1}..{2}>[X] in {0}")
					@CsvSource({
						// Optional
						"Y, 0, 1, {-}, 0",
						"Y-, 0, 1, {-;-}, 0-1",
						"X, 0, 1, {0}, 0",
						"-X, 0, 1, {-;1}, 0-1",
						"X-, 0, 1, {0;-}, 0-1",
						// Singular
						"X, 1, 1, {0}, 0",
						"X-, 1, 1, {0}, 0-1",
						"-X, 1, 1, {1}, 0-1",
						"XX-, 1, 1, {0;1}, 0-2",
						"-XX, 1, 1, {1;2}, 0-2",
						"X-X, 1, 1, {0;2}, 0-2",
						// Sequence of 1 to 2
						"XX, 1, 2, {0-1;1}, 0-1",
						"X-, 1, 2, {0}, 0-1",
						"XX-, 1, 2, {0-1;1}, 0-2",
						"XXX, 1, 2, {0-1;1-2;2}, 0-2",
						"-X-, 1, 2, {1}, 0-2",
						"-XX, 1, 2, {1-2;2}, 0-2",
						"-XX-, 1, 2, {1-2;2}, 0-3",
						"-XXX, 1, 2, {1-2;2-3;3}, 0-3",
						"XXX-, 1, 2, {0-1;1-2;2}, 0-3",
						"--XX, 1, 2, {2-3;3}, 0-3",
						"XX--, 1, 2, {0-1;1}, 0-3",
						"X-XX, 1, 2, {0;2-3;3}, 0-3",
						// Sequence of 2 to 3
						"X-XX, 2, 3, {2-3}, 0-3",

						"--XXXXXXXXXX--, 8, 10, {2-11;3-11;4-11}, 0-12",
						"--XXXXXXXXXXX--, 8, 10, {2-11;3-12;4-12;5-12}, 0-13",
					})
					@DisplayName("Node with a bounded multiplicity [greedy mode, multiple hits]")
					void testGreedy(String target, int min, int max,
							@IntervalArrayArg Interval[] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										rangeGreedy(min, max, CONTINUOUS)
										)
								).build(),
								match(hits.length)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hitsForSet(target, EQUALS_X))
									.results(NODE_0, hits)
						);
					}

					@ParameterizedTest(name="{index}: <{1}..{2}^>[X] in {0}")
					@CsvSource({
						// Optional
						"Y, 0, 1, {{}}, 0",
						"Y-, 0, 1, {{}{}}, 0-1",
						"X, 0, 1, {{0}}, 0",
						"-X, 0, 1, {{}{1}}, 0-1",
						"X-, 0, 1, {{0}{}}, 0-1",
						// Singular
						"X, 1, 1, {{0}}, 0",
						"X-, 1, 1, {{0}}, 0-1",
						"-X, 1, 1, {{1}}, 0-1",
						"XX-, 1, 1, {{0}{1}}, 0-2",
						"-XX, 1, 1, {{1}{2}}, 0-2",
						"X-X, 1, 1, {{0}{2}}, 0-2",
						// Sequence of 1 to 2
						"XX, 1, 2, {{0;1}{1}}, 0-1",
						"X-, 1, 2, {{0}}, 0-1",
						"XX-, 1, 2, {{0;1}{1}}, 0-2",
						"X-X, 1, 2, {{0;2}{2}}, 0-2",
						"XXX, 1, 2, {{0;1}{1;2}{2}}, 0-2",
						"-X-, 1, 2, {{1}}, 0-2",
						"-XX, 1, 2, {{1;2}{2}}, 0-2",
						"-XX-, 1, 2, {{1;2}{2}}, 0-3",
						"-XXX, 1, 2, {{1;2}{2;3}{3}}, 0-3",
						"XXX-, 1, 2, {{0;1}{1;2}{2}}, 0-3",
						"--XX, 1, 2, {{2;3}{3}}, 0-3",
						"XX--, 1, 2, {{0;1}{1}}, 0-3",
						// Sequence of 2 to 3
						"X-XX, 2, 3, {{0;2;3}{2;3}}, 0-3",
						// Sequence of 2 to 4
						"X-XX, 2, 4, {{0;2;3}{2;3}}, 0-3",
						"X-XX-X, 2, 4, {{0;2;3;5}{2;3;5}{3;5}}, 0-5",

						"--XXX--XXX--, 4, 5, {{2;3;4;7;8}{3;4;7;8;9}{4;7;8;9}}, 0-11",

						"--XXXXXXXX--, 4, 6, {{2;3;4;5;6;7}{3;4;5;6;7;8}{4;5;6;7;8;9}{5;6;7;8;9}{6;7;8;9}}, 0-11",
						"--XXXXXXXXX--, 4, 6, {{2;3;4;5;6;7}{3;4;5;6;7;8}{4;5;6;7;8;9}{5;6;7;8;9;10}{6;7;8;9;10}{7;8;9;10}}, 0-12",
					})
					@DisplayName("Node with a bounded multiplicity [greedy mode, multiple hits, discontinuous]")
					void testGreedyDiscontinuous(String target, int min, int max,
							@IntMatrixArg int[][] hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										rangeGreedy(min, max, DISCONTINUOUS)
										)
								).build(),
								match(hits.length)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hitsForSet(target, EQUALS_X))
									.results(hits.length, (r, i) -> r.map(NODE_0, hits[i]))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										rangeGreedy(min, max, CONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(target, visited, EQUALS_X))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										rangeGreedy(min, max, DISCONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(target, visited, EQUALS_X))
						);
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
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(set(adjacent,
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), rangeGreedy(min, max, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp(c2))))
								).limit(1).build(), // we don't need multiple matches for confirmation
								match(1)
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
						);
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
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(set(adjacent,
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), rangeGreedy(min, max, DISCONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp(c2))))
								).limit(1).build(), // we don't need multiple matches for confirmation
								match(1)
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
						);
					}

					@ParameterizedTest(name="{index}: <{1}..{2}>[X] in {0}")
					@CsvSource({
						// Optional
						"X, 0, 1, -, -",
						"XX, 0, 2, -, -",

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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										rangeReluctant(min, max, CONTINUOUS)
										)
								).limit(1).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
					}

					@ParameterizedTest(name="{index}: <{1}..{2}^>[X] in {0}")
					@CsvSource({
						// Optional
						"X, 0, 1, -, -",
						"XX, 0, 2, -, -",

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
						//TODO complete
					})
					@DisplayName("Node with a bounded multiplicity [reluctant mode, single hit, limit, discontinuous]")
					void testReluctantDiscontinuous(String target, int min, int max,
							@IntervalArg Interval hits,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										rangeReluctant(min, max, DISCONTINUOUS)
										)
								).limit(1).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										rangeReluctant(min, max, CONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(candidates))
						);
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
						//TODO complete
					})
					@DisplayName("Mismatch with a bounded multiplicity [reluctant mode, discontinuous]")
					void testReluctantFailDiscontinuous(String target, int min, int max,
							@IntervalArg Interval visited,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										rangeReluctant(min, max, DISCONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(candidates))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										rangeReluctant(min, max, CONTINUOUS)
										)
								).build(),
								match(hits.length)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.results(NODE_0, hits)
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										rangeReluctant(min, max, DISCONTINUOUS)
										)
								).build(),
								match(hits.length)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.results(hits.length, (r, i) -> r.map(NODE_0, hits[i]))
						);
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
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(set(adjacent,
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), rangeReluctant(min, max, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).limit(1).build(), // we don't need multiple matches for confirmation
								match(1)
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
						);
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
						//TODO complete
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
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(set(adjacent,
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), rangeReluctant(min, max, DISCONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).limit(1).build(), // we don't need multiple matches for confirmation
								match(1)
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
						);
					}

					@Test
					@DisplayName("verify reluctant expansion with multiple nodes and matches")
					void testReluctantExpansion() {
						//TODO add discontinuous counterpart?
						final String target = "-XXxXXx-";
						assertResult(target,
								builder(adjacent(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), rangeReluctant(1, 2, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).build(),
								match(4)
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
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										rangePossessive(min, max, CONTINUOUS)
										)
								).limit(1).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										rangePossessive(min, max, DISCONTINUOUS)
										)
								).limit(1).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(hits))
									.result(result(0)
											.map(NODE_0, hits))
						);
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
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										rangePossessive(min, max, CONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(candidates))
						);
					}

					@ParameterizedTest(name="{index}: <{1}..{2}!^>[X] in {0}")
					@CsvSource({
						"-, 1, 2, 0, -",
						"Y, 1, 2, 0, -",
						"-Y, 2, 3, 0, -",  // early abort by scan
						"-Y-, 2, 3, 0-1, -",
						"X-x-, 2, 3, 0-3, 0",
						"-X-, 2, 3, 0-2, 1",
						//TODO complete
					})
					@DisplayName("Mismatch with a bounded multiplicity [possessive mode, discontinuous]")
					void testPossessiveFailDiscontinuous(String target, int min, int max,
							@IntervalArg Interval visited) {
						// 'Repetition' node sets minSize so that scan can abort early
						assertResult(target,
								builder(quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X'))),
										rangePossessive(min, max, DISCONTINUOUS)
										)
								).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited)
											.hits(target, visited, EQUALS_X))
						);
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
						assertResult(target,
								builder(ordered(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), rangePossessive(min, max, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).build(),
								mismatch()
									// Underlying cache of first node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited1)
											.hits(candidates))
									// Underlying cache of second node
									.cache(cache(CACHE_1, false)
											.set(visited2)
											.window(target))
						);
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
						//TODO complete
					})
					@DisplayName("Mismatch due to possessive consumption [ordered, discontinuous]")
					void testPossessiveFail2Discontinuous(String target, int min, int max,
							@IntervalArg Interval visited1,
							@IntervalArg Interval visited2,
							@IntervalArg Interval candidates) {
						// 'Repetition' node sets minSize so that scan can abort early
						assertResult(target,
								builder(ordered(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), rangePossessive(min, max, DISCONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).build(),
								mismatch()
									// Underlying cache of first node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited1)
											.hits(candidates))
									// Underlying cache of second node
									.cache(cache(CACHE_1, false)
											.set(visited2)
											.window(target))
						);
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
						assertResult(target,
								builder(adjacent(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), rangePossessive(min, max, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).build(),
								mismatch()
									// Underlying cache of first node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited1)
											.hits(candidates))
									// Underlying cache of second node
									.cache(cache(CACHE_1, false)
											.set(visited2)
											.window(target))
						);
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
						assertResult(target,
								builder(adjacent(
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), rangePossessive(min, max, DISCONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('x'))))
								).build(),
								mismatch()
									// Underlying cache of first node
									.cache(cache(CACHE_0, false)
											.window(target)
											.set(visited1)
											.hits(target, visited1, EQUALS_X_IC))
									// Underlying cache of second node
									.cache(cache(CACHE_1, false)
											.set(visited2)
											.window(target))
						);
					}

					@ParameterizedTest(name="{index}: <{3}..{4}!>[x|X][{1}] in {0}, adjacent={2}")
					@CsvSource({
						// Optional - ordered
						"Y, Y, false, 0, 1, {-}, {0}, {0}, {0}, -",
						"-Y, Y, false, 0, 1, {-;-}, {1;1}, {0-1}, {0-1}, -",
						"XY, Y, false, 0, 1, {0;-}, {1;1}, {0-1}, {1}, {0}",
						"XXY, Y, false, 0, 1, {0;1;-}, {2;2;2}, {0-2}, {1-2}, {0-1}",
						"XXY, Y, false, 0, 2, {0-1;1;-}, {2;2;2}, {0-2}, {2}, {0-1}",
						// Optional - adjacent
						"Y, Y, true, 0, 1, {-}, {0}, {0}, {0}, -",
						"-Y, Y, true, 0, 1, {-}, {1}, {0-1}, {0-1}, -",
						"XY, Y, true, 0, 1, {0;-}, {1;1}, {0-1}, {1}, {0}",
						"XXY, Y, true, 0, 1, {1;-}, {2;2}, {0-2}, {1-2}, {0-1}",
						// Expansion of size 1 to 2 - ordered
						"XY, Y, false, 1, 2, {0}, {1}, {0-1}, {1}, {0}",
						"XXY, Y, false, 1, 2, {0-1;1}, {2;2}, {0-2}, {2}, {0-1}",
						"XX-XX, X, false, 1, 2, {0-1;0-1;1;1}, {3;4;3;4}, {0-4}, {2-4}, {0-1;3-4}",
						// Expansion of size 1 to 2 - adjacent
						"XY, Y, true, 1, 2, {0}, {1}, {0-1}, {1}, {0}",
						"XXY, Y, true, 1, 2, {0-1;1}, {2;2}, {0-2}, {2}, {0-1}",
						// Expansion of size 1 to 3 - ordered
						"XY, Y, false, 1, 3, {0}, {1}, {0-1}, {1}, {0}",
						"XXY, Y, false, 1, 3, {0-1;1}, {2;2}, {0-2}, {2}, {0-1}",
						"XXX-X, X, false, 1, 3, {0-2;1-2;2}, {4;4;4}, {0-3}, {3-4}, {0-2}",
						"XX-XX, X, false, 1, 3, {0-1;0-1;1;1}, {3;4;3;4}, {0-4}, {2-4}, {0-1;3-4}", // we miss the 5. match due to possessive expansion
						"XXx-x, x, false, 1, 3, {0-2;1-2;2}, {4;4;4}, {0-3}, {3-4}, {0-2}",
						// Expansion of size 1 to 3 - adjacent
						"XXY, Y, true, 1, 3, {0-1;1}, {2;2}, {0-2}, {2}, {0-1}",
						"XXXX, X, true, 1, 3, {0-2}, {3}, {0-3}, {3}, {0-3}",
						// Expansion of size 2 to 3 - ordered
						"XXX-X, X, false, 2, 3, {0-2;1-2}, {4;4;}, {0-3}, {3-4}, {0-2}",
						"XX-XX, X, false, 2, 3, {0-1;0-1}, {3;4}, {0-2}, {2-4}, {0-1}",
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
							@IntervalArrayArg Interval[] candidates1) {

						// Sanity check since we expect symmetric results here
						assertThat(hits1).hasSameSizeAs(hit2);

						/*
						 * We expect NODE_1 to aggressively consume slots with
						 * no regards for NODE_0, so that in contrast to reluctant mode
						 * we will miss some multi-match situations.
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(set(adjacent,
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), rangePossessive(min, max, CONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp(c2))))
								).build(), // we don't need multiple matches for confirmation
								match(hits1.length)
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
						);
					}

					@ParameterizedTest(name="{index}: <{3}..{4}!^>[x|X][{1}] in {0}, adjacent={2}")
					@CsvSource({
						// Optional - ordered
						"Y, Y, false, 0, 1, {{}}, {0}, {0}, {0}",
						"-Y, Y, false, 0, 1, {{}{}}, {1;1}, {0-1}, {0-1}",
						"XY, Y, false, 0, 1, {{0}{}}, {1;1}, {0-1}, {1}",
						"XXY, Y, false, 0, 1, {{0}{1}{}}, {2;2;2}, {0-2}, {1-2}",
						"XXY, Y, false, 0, 2, {{0;1}{1}{}}, {2;2;2}, {0-2}, {2}",
						// Optional - adjacent
						"Y, Y, true, 0, 1, {{}}, {0}, {0}, {0}",
						"-Y, Y, true, 0, 1, {{}}, {1}, {0-1}, {0-1}",
						"XY, Y, true, 0, 1, {{0}{}}, {1;1}, {0-1}, {1}",
						"XXY, Y, true, 0, 1, {{1}{}}, {2;2}, {0-2}, {1-2}",
						// Expansion of size 1 to 2 - ordered
						"XY, Y, false, 1, 2, {{0}}, {1}, {0-1}, {1}",
						"XXY, Y, false, 1, 2, {{0;1}{1}}, {2;2}, {0-2}, {2}",
						"XX-XX, X, false, 1, 2, {{0;1}{0;1}{1;3}}, {3;4;4}, {0-4}, {2-4}",
						// Expansion of size 1 to 2 - adjacent
						"XY, Y, true, 1, 2, {{0}}, {1}, {0-1}, {1}",
						"XXY, Y, true, 1, 2, {{0;1}{1}}, {2;2}, {0-2}, {2}",
						// Expansion of size 1 to 3 - ordered
						"XY, Y, false, 1, 3, {{0}}, {1}, {0-1}, {1}",
						"XXY, Y, false, 1, 3, {{0;1}{1}}, {2;2}, {0-2}, {2}",
						"XXX-X, X, false, 1, 3, {{0;1;2}}, {4}, {0-4}, {3-4}",
						"XX-XX, X, false, 1, 3, {{0;1;3}}, {4}, {0-4}, {4}",
						"XX-XX-X, X, false, 1, 3, {{0;1;3}{0;1;3}{1;3;4}}, {4;6;6}, {0-6}, {4-6}",
						"XXx-x, x, false, 1, 3, {{0;1;2}}, {4}, {0-4}, {3-4}",
						// Expansion of size 1 to 3 - adjacent
						"XXY, Y, true, 1, 3, {{0;1}{1}}, {2;2}, {0-2}, {2}",
						"XXXX, X, true, 1, 3, {{0;1;2}}, {3}, {0-3}, {3}",
						"XX-XX, X, true, 1, 3, {{0;1;3}}, {4}, {0-4}, {4}",
						// Expansion of size 2 to 3 - ordered
						"XXX-X, X, false, 2, 3, {{0;1;2}}, {4;}, {0-4}, {3-4}",
						"XX-XX, X, false, 2, 3, {{0;1;3}}, {4}, {0-4}, {4}",
						//TODO complete
					})
					@DisplayName("verify possessive expansion with multiple nodes")
					void testPossessiveCompetitionDiscontinuous(String target,
							char c2, // search symbol for second node
							boolean adjacent,
							int min, int max, // arguments for 'Range' marker
							@IntMatrixArg int[][] hits1, // reported hits for first node
							@IntervalArrayArg Interval[] hit2, // reported hits for second node
							@IntervalArrayArg Interval[] visited1,  // all slots visited for first node
							@IntervalArrayArg Interval[] visited2) { // all slots visited for second node

						// Sanity check since we expect symmetric results here
						assertThat(hits1).as("Different match counts").hasSameSizeAs(hit2);

						/*
						 * We expect NODE_1 to aggressively consume slots with
						 * no regards for NODE_0, so that in contrast to reluctant mode
						 * we will miss some multi-match situations.
						 * (remember: state machine gets built back to front)
						 */
						assertResult(target,
								builder(set(adjacent,
										quantify(IqlTestUtils.node(NO_LABEL, NO_MARKER,
												constraint(ic_exp('X'))), rangePossessive(min, max, DISCONTINUOUS)),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp(c2))))
								).build(), // we don't need multiple matches for confirmation
								match(hits1.length)
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
						);
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
					assertResult(target,
							builder(expand(query)).build(),
							match(matches)
								// Underlying cache of atom node
								.cache(cache(CACHE_0, false)
										.window(target)
										.set(visited)
										.hits(hits))
								.result(result(0)
										.map(NODE_0, hits))
					);
				}
			}
		}

		@Nested
		class ForIqlSet {

			@DisplayName("no specified node arrangement")
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

					"-YX",
					"Y-X",
					"YX-",
				})
				@DisplayName("Two nodes with no matches")
				void testDualNodeFail(String target) {
					assertResult(target,
							builder(unordered(
									IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
									IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y'))))
									).build(),
							mismatch());
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
					assertResult(target,
							builder(unordered(
									IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
									IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y'))))
									).build(),
							match(1)
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
					);
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
					assertResult(target,
							builder(ordered(
									IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
									IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y'))))
									).build(),
							mismatch());
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
					assertResult(target,
							builder(ordered(
									IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
									IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y'))))
									).build(),
							match(1)
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
					);
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
					assertResult(target,
							builder(adjacent(
									IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
									IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y'))))
									).build(),
							mismatch());
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
					assertResult(target,
							builder(adjacent(
									IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
									IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y'))))
									).build(),
							match(1)
								.cache(cache(CACHE_0, true)
										.window(0, target.length()-2)
										.hits(hitX))
								.cache(cache(CACHE_1, true)
										.window(hitX+1)
										.hits(hitY))
								.result(result(0)
										.map(NODE_0, hitX)
										.map(NODE_1, hitY))
					);
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
				assertResult(target,
						builder(grouping(
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y')))
								)
						).build(),
						mismatch());
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
				assertResult(target,
						builder(grouping(
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y'))),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Z')))
								)
						).build(),
						mismatch());
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

				assertResult(target,
						builder(grouping(
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y'))),
								IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Z')))
								)
						).build(),
						match(hits1.length)
							// Cache for 3rd node
							.cache(cache(CACHE_3, true)
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
				);
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
						assertResult(target,
								builder(quantify(grouping(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X')))),
										negated()
										)).build(),
								mismatch()
									// Underlying cache of atom node
									.cache(cache(CACHE_0, true)
											.window(visitedAtom)
											.hits(hits))
						);
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
						assertResult(target,
								builder(quantify(grouping(IqlTestUtils.node(NO_LABEL, NO_MARKER,
										constraint(eq_exp('X')))),
										negated()
										)).build(),
								match(1)
									// Underlying cache of atom node
									.cache(cache(CACHE_0, true)
											.window(target))
									// Cache of the negated search
									.cache(cache(CACHE_1, true)
											.window(target)
											.hitsForWindow())
						);
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
						assertResult(target,
								builder(quantify(grouping(
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
										IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y')))
										),
										negated()
										)).build(),
								match(1)
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
						);
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
					assertResult(target,
							builder(disjunction(
									IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
									IqlTestUtils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y')))
									)
							).build(),
							match(hits1.length>0, hits1.length)
								.cache(cache(CACHE_0, true)
										.window(visited1)
										.hits(target, EQUALS_X))
								.cache(cache(CACHE_1, true)
										.window(visited2)
										.hits(target, EQUALS_Y))
								.results(hits1.length, (r, i) -> r
										.map(NODE_0, hits1[i])
										.map(NODE_1, hits2[i]))
							);
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
	 * 		<th>{@link IqlSequence set}</th><th>{@link IqlElementDisjunction branch}</th></tr>
	 * <tr><th>{@link IqlNode node}</th><td>-</td><td>{@link NodeInGrouping X}</td>
	 * 		<td>{@link NodeInSet X}</td><td>{@link NodeInBranch X}</td></tr>
	 * <tr><th>{@link IqlGrouping grouping}</th><td>-</td><td>{@link GroupingInGrouping X}</td><td>{@link GroupingInSet X}</td><td>{@link GroupingInBranch X}</td></tr>
	 * <tr><th>{@link IqlSequence set}</th><td>-</td><td>{@link SetInGrouping X}</td><td>{@link SetInSet X}</td><td>{@link SetInBranch X}</td></tr>
	 * <tr><th>{@link IqlElementDisjunction branch}</th><td>-</td><td>{@link BranchInGrouping X}</td><td>{@link BranchInSet X}</td><td>{@link BranchInBranch X}</td></tr>
	 * </table>
	 *
	 * Row nested in column.
	 */
	@Nested
	class ForRawQueries {

		/** Turns a 3-level hit matrix into a basic match config for testing */
		private MatchConfig config(int matches,
				// [node_id][match_id][hits]
				int[][][] hits) {
			return match(matches>0, matches)
					// Format of 'hits' matrix: [node_id][match_id][hits]
					.results(matches, (r, i) -> {
						for(int j = 0; j<hits.length; j++) {
							// Make sure we handle "empty" assignments
							if(hits[j].length>0) {
								r.map(j, hits[j][i]);
							}
						}
					});
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
		 *
		 * Note that blank nodes produce no mappings, so we are using the
		 * {@link SequencePattern.Builder#nodeTransform(Function)} method
		 * to inject artificial node labels after the query has been parsed,
		 * causing the final matcher to actually create mappings we can verify.
		 */
		@Nested
		class NodeInGrouping {

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{[]}', XYZ, 3, { {{0}{1}{2}} } ",
				"'{[][]}', XYZ, 3, { {{0}{0}{1}} {{1}{2}{2}} }", // not adjacent, so can expand multiple  times
				"'{[][][]}', XYZ, 1, { {{0}} {{1}} {{2}} }",
			})
			@DisplayName("grouping of blank node(s)")
			void testBlank(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).nodeTransform(PROMOTE_NODE).build(), config(matches, hits));
				assertResult(target, builder(expand(query)).nodeTransform(PROMOTE_NODE).build(), config(matches, hits));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				/* Note that "optional reluctant" nodes don't get added to a mapping
				 * unless the context forces an expansion.
				 */

				// Pure singular reluctance
				"'{[?]}', X, 1, { {{}} }",
				"'{[?][?]}', X, 1, { {{}} }",
				"'{[?][?]}', XY, 3, { {{}{}{}} {{}{}{}} }",

				// Pure expanded reluctance
				"'{[*]}', X, 1, { {{}} }",
				"'{[*]}', XY, 2, { {{}{}} }",
				"'{[*]}', XYZ, 3, { {{}{}{}} }",

				// Mandatory dummy node with reluctant expansion
				"'{[+]}', X, 1, { {{0}} }",
				"'{[+]}', XY, 2, { {{0}{1}} }",
				"'{[+]}', XYZ, 3, { {{0}{1}{2}} }",

				// Mandatory node with following optional
				"'{[][?]}', XY, 2, { {{0}{1}} {{}{}} }",
				"'{[][?]}', XYZ, 4, { {{0}{0}{1}{2}} {{}{}{}{}} }",

				// Mandatory node with following optional expansion
				"'{[][*]}', XY, 2, { {{0}{1}} {{}{}} }",
				"'{[][*]}', XYZ, 4, { {{0}{0}{1}{2}} {{}{}{}{}} }",

				// Mandatory node with following dummy node with reluctant expansion
				"'{[][+]}', XY, 1, { {{0}} {{1}} }",
				"'{[][+]}', XYZ, 3, { {{0}{0}{1}} {{1}{2}{2}} }",

				// Mandatory node after optional
				"'{[?][]}', XY, 3, { {{}{}{}} {{0}{1}{1}} }",
				"'{[?][]}', XYZ, 6, { {{}{}{}{}{}{}} {{0}{1}{2}{1}{2}{2}} }",

				// Mandatory node after optional expansion
				"'{[*][]}', XY, 3, { {{}{}{}} {{0}{1}{1}} }",
				"'{[*][]}', XYZ, 6, { {{}{}{}{}{}{}} {{0}{1}{2}{1}{2}{2}} }",

				// Mandatory node after dummy node with reluctant expansion
				"'{[+][]}', XY, 1, { {{0}} {{1}} }",
				"'{[+][]}', XYZ, 3, { {{0}{0}{1}} {{1}{2}{2}} }",

				// Mandatory nodes surrounding intermediate optional
				"'{[][?][]}', XY, 1, { {{0}} {{}} {{1}} }",
				"'{[][?][]}', XYZ, 4, { {{0}{0}{0}{1}} {{}{}{}{}} {{1}{2}{2}{2}} }",

				// Mandatory nodes surrounding intermediate optional expansion
				"'{[][*][]}', XY, 1, { {{0}} {{}} {{1}} }",
				"'{[][*][]}', XYZ, 4, { {{0}{0}{0}{1}} {{}{}{}{}} {{1}{2}{2}{2}} }",

				// Mandatory nodes surrounding intermediate dummy node with reluctant expansion
				"'{[][+][]}', XY, 0, -",
				"'{[][+][]}', XYZ, 1, { {{0}} {{1}} {{2}} }",

				// Cannot force expansion with non-adjacent sequence, we leave that to the NodeInSet group

				//TODO we assume the "reluctance" property is sufficiently tested in $ForIqlNode$WithQuantifier$AtLeast
			})
			@DisplayName("grouping of dummy node(s)")
			void testDummyNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).nodeTransform(PROMOTE_NODE).build(), config(matches, hits));
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
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{<3+>[$X]}', XX, 0, -",
				"'{<3+>[$X]}', XXX, 1, { {{0;1;2}} }",
				"'{<2+>[$X] <2+>[$Y][$Z]}', XXY, 0, -",
				"'{<2+>[$X] <2+>[$Y][$Z]}', XXYYZ, 1, { {{0;1}} {{2;3}} {{4}} }",
				"'{<2+>[$X] <2+>[$Y][$Z]}', XXXYY-Z, 2, { {{0;1;2}{1;2}} {{3;4}{3;4}} {{6}{6}} }",
				//TODO complete
			})
			@DisplayName("grouping of quantified node(s)")
			void testQuantifiedNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				//TODO complete
				"'{<2>[isAfter(1),]}', XX, 0, -",
				"'{<2>[isAfter(1), $X]}', XXX, 1, { {{1;2}} }",
			})
			@DisplayName("grouping of quantified node(s) with markers")
			void testQuantifiedNodesWithMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<1..3>{[$X][$Y]}', XX, 0, -",
				"'<1..3>{[$X][$Y]}', X-Z, 0, -",
				"'<1..3>{[$X][$Y]}', XY, 1, { {{0}} {{1}} }",
				"'<1..3>{[$X][$Y]}', XYXY, 2, { {{0;2}{2}} {{1;3}{3}} }",
				"'<1..3>{[$X][$Y]}', XY-XY, 2, { {{0}{3}} {{1}{4}} }",
				"'<1..3>{[$X][$Y]}', XY-XY--XY, 3, { {{0}{3}{7}} {{1}{4}{8}} }",
				"'<1..3^>{[$X][$Y]}', XY-XY--XY, 3, { {{0;3;7}{3;7}{7}} {{1;4;8}{4;8}{8}} }",
				//TODO complete
			})
			@DisplayName("quantified grouping of blank nodes")
			void testQuantifiedGrouping(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
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
				//TODO complete
			})
			@DisplayName("quantified grouping of nodes with markers")
			void testQuantifiedGroupingWithMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<1+>{<2+>[isAfter(1), $X][$Y]}', X, 0, -",
				"'<1+>{<2+>[isAfter(1), $X][$Y]}', XXY, 0, -",
				"'<1+>{<2+>[isAfter(1), $X][$Y]}', XXXY, 1, { {{1;2}} {{3}} }",
				"'<1+>{<2+>[isAfter(1), $X][$Y]}', XXXXXY-, 3, { {{1;2;3;4}{2;3;4}{3;4}} {{5}{5}{5}} }",
				// outer quantifier only reinforces existential quantification, so group search is exhaustive
				"'<1>{<2+>[isAfter(1), $X][$Y]}', XXXXYY-, 4, { {{1;2;3}{1;2;3}{2;3}{2;3}} {{4}{5}{4}{5}} }",
				// nodes within quantified grouping are reduced to "find" scan
				"'<1+>{<2+>[isAfter(1), $X][$Y]}', XXXXYY-, 2, { {{1;2;3}{2;3}} {{4}{4}} }",

				"'<2+>{<2+>[isAfter(1), $X][$Y]}', XXXY, 0, -",
				"'<2+>{<2+>[isAfter(1), $X][$Y]}', XXXYXXY, 1, { {{1;2;4;5}} {{3;6}} }",
				//TODO complete
			})
			@DisplayName("quantified grouping of quantified nodes with markers")
			void testQuantifiedGroupingWithQuantifiedNodesWithMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
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
		 * <li>arrangement on set</li>
		 * </ul>
		 *
		 * Note that blank nodes produce no mappings, so we are using the
		 * {@link SequencePattern.Builder#nodeTransform(Function)} method
		 * to inject artificial node labels after the query has been parsed,
		 * causing the final matcher to actually create mappings we can verify.
		 * <p>
		 * Note further that we always need at least {@code 2} nodes in a sequence
		 * to force the creation of a {@link IqlSequence} wrapper!
		 */
		@Nested
		class NodeInSet {

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'[][]', XX, 1, { {{0}} {{1}} }",
				"'[][]', XXX, 3, { {{0}{0}{1}} {{1}{2}{2}} }",
				"'[][][]', XXX, 1, { {{0}} {{1}} {{2}} }",
				"'[][][][]', XXXX, 1, { {{0}} {{1}} {{2}} {{3}} }",
			})
			@DisplayName("set of blank nodes")
			void testBlank(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).nodeTransform(PROMOTE_NODE).build(), config(matches, hits));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				/* Note that "optional reluctant" nodes don't get added to a mapping
				 * unless the context forces an expansion.
				 */

				// Pure singular reluctance
				"'[?][?]', X, 1, { {{}} }",
				"'[?][?]', XY, 3, { {{}{}{}} {{}{}{}} }",

				// Mandatory node with following optional
				"'[][?]', XY, 2, { {{0}{1}} {{}{}} }",
				"'[][?]', XYZ, 4, { {{0}{0}{1}{2}} {{}{}{}{}} }",

				// Mandatory node with following optional expansion
				"'[][*]', XY, 2, { {{0}{1}} {{}{}} }",
				"'[][*]', XYZ, 4, { {{0}{0}{1}{2}} {{}{}{}{}} }",

				// Mandatory node with following dummy node with reluctant expansion
				"'[][+]', XY, 1, { {{0}} {{1}} }",
				"'[][+]', XYZ, 3, { {{0}{0}{1}} {{1}{2}{2}} }",

				// Mandatory node after optional
				"'[?][]', XY, 3, { {{}{}{}} {{0}{1}{1}} }",
				"'[?][]', XYZ, 6, { {{}{}{}{}{}{}} {{0}{1}{2}{1}{2}{2}} }",

				// Mandatory node after optional expansion
				"'[*][]', XY, 3, { {{}{}{}} {{0}{1}{1}} }",
				"'[*][]', XYZ, 6, { {{}{}{}{}{}{}} {{0}{1}{2}{1}{2}{2}} }",

				// Mandatory node after dummy node with reluctant expansion
				"'[+][]', XY, 1, { {{0}} {{1}} }",
				"'[+][]', XYZ, 3, { {{0}{0}{1}} {{1}{2}{2}} }",

				// Mandatory nodes surrounding intermediate optional
				"'[][?][]', XY, 1, { {{0}} {{}} {{1}} }",
				"'[][?][]', XYZ, 4, { {{0}{0}{0}{1}} {{}{}{}{}} {{1}{2}{2}{2}} }",

				// Mandatory nodes surrounding intermediate optional expansion
				"'[][*][]', XY, 1, { {{0}} {{}} {{1}} }",
				"'[][*][]', XYZ, 4, { {{0}{0}{0}{1}} {{}{}{}{}} {{1}{2}{2}{2}} }",

				// Mandatory nodes surrounding intermediate dummy node with reluctant expansion
				"'[][+][]', XY, 0, -",
				"'[][+][]', XYZ, 1, { {{0}} {{1}} {{2}} }",
			})
			@DisplayName("set of dummy nodes")
			void testDummyNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).nodeTransform(PROMOTE_NODE).build(), config(matches, hits));
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
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
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
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
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
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'ADJACENT [][]', XYZ, 2, { {{0}{1}} {{1}{2}} }",
				"'ADJACENT [][][]', XYZ, 1, { {{0}} {{1}} {{2}} }",
				"'ADJACENT [$X][$Y]', XY, 1, { {{0}} {{1}} }",
				"'ADJACENT [$X][$Y]', XXY, 1, { {{1}} {{2}} }",
				"'ADJACENT [$X][$Y][$Z]', XYZ, 1, { {{0}} {{1}} {{2}} }",
				"'ADJACENT [$X][$Y][$Z]', XYYZ, 0, -",
				"'ADJACENT [$X][$Y][$Z]', XYYZXXYZZ, 1, { {{5}} {{6}} {{7}} }",

				// Dummy nodes and forcing reluctant expansion

				// Mandatory node after optional
				"'ADJACENT [?][]', XY, 2, { {{}{}} {{0}{1}} }",
				"'ADJACENT [?][]', XYZ, 3, { {{}{}{}} {{0}{1}{2}} }",
				"'ADJACENT [?][$Y]', XYZ, 2, { {{0}{}} {{1}{1}} }",
				"'ADJACENT [?][$Y]', XXYZ, 2, { {{1}{}} {{2}{2}} }",

				// Mandatory node after optional expansion
				"'ADJACENT [*][]', XY, 2, { {{}{}} {{0}{1}} }",
				"'ADJACENT [*][]', XYZ, 3, { {{}{}{}} {{0}{1}{2}} }",
				"'ADJACENT [*][$Y]', XYZ, 2, { {{0}{}} {{1}{1}} }",
				"'ADJACENT [*][$Y]', XXYZ, 3, { {{0;1}{1}{}} {{2}{2}{2}} }",

				// Mandatory node after dummy node with reluctant expansion
				"'ADJACENT [+][]', XY, 1, { {{0}} {{1}} }",
				"'ADJACENT [+][]', XYZ, 2, { {{0}{1}} {{1}{2}} }",
				"'ADJACENT [+][$Y]', XYZ, 1, { {{0}} {{1}} }",
				"'ADJACENT [+][$Y]', XXYZ, 2, { {{0;1}{1}} {{2}{2}} }",

				// Mandatory nodes surrounding intermediate optional
				"'ADJACENT [][?][]', XY, 1, { {{0}} {{}} {{1}} }",
				"'ADJACENT [][?][]', XYZ, 2, { {{0}{1}} {{}{}} {{1}{2}} }",
				"'ADJACENT [$X][?][$Y]', XY, 1, { {{0}} {{}} {{1}} }",
				"'ADJACENT [$X][?][$Y]', X-Y, 1, { {{0}} {{1}} {{2}} }",
				"'ADJACENT [$X][?][$Y]', X--Y, 0, -",

				// Mandatory nodes surrounding intermediate optional expansion
				"'ADJACENT [][*][]', XY, 1, { {{0}} {{}} {{1}} }",
				"'ADJACENT [][*][]', XYZ, 2, { {{0}{1}} {{}{}} {{1}{2}} }",
				"'ADJACENT [$X][*][$Y]', XY, 1, { {{0}} {{}} {{1}} }",
				"'ADJACENT [$X][*][$Y]', X-Y, 1, { {{0}} {{1}} {{2}} }",
				"'ADJACENT [$X][*][$Y]', X--Y, 1, { {{0}} {{1;2}} {{3}} }",

				// Mandatory nodes surrounding intermediate dummy node with reluctant expansion
				"'ADJACENT [][+][]', XY, 0, -",
				"'ADJACENT [][+][]', XYZ, 1, { {{0}} {{1}} {{2}} }",
				"'ADJACENT [$X][+][$Y]', XY, 0, -",
				"'ADJACENT [$X][+][$Y]', X-Y, 1, { {{0}} {{1}} {{2}} }",
				"'ADJACENT [$X][+][$Y]', X--Y, 1, { {{0}} {{1;2}} {{3}} }",
			})
			@DisplayName("set with explicit arrangement")
			void testArrangement(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).nodeTransform(PROMOTE_NODE).build(), config(matches, hits));
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
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'ADJACENT [isFirst,$X]<2+>[isInside(2,5),$X][isLast,$X]', XX-XX-XX, 0, -",
				"'ADJACENT [isFirst,$X]<2+>[isInside(2,5),$Y][isLast,$Z]', XYYYYZ, 1, { {{0}} {{1;2;3;4}} {{5}} }",
				"'ADJACENT <1..3>[$X][*][isAfter(4),$Y]<2+>[$Z]', XYZZ, 0, -",
				"'ADJACENT <1..3>[$X][*][isAfter(4),$Y]<2+>[$Z]', ---XYZZ, 1, { {{3}} {{4}} {{5;6}} }",
				"'ADJACENT <1..3>[$X][*][isAfter(4),$Y]<2+>[$Z]', X---YZZ, 1, { {{0}} {{4}} {{5;6}} }",
				"'ADJACENT <1..3>[$X][*][isAfter(4),$Y]<2+>[$Z]', -XXXYZZ, 3, { {{1;2;3}{2;3}{3}} {{4}{4}{4}} {{5;6}{5;6}{5;6}} }",
				"'ADJACENT <1..3>[$X][*][isAfter(4),$Y]<2+>[$Z]', XXXXYZZ, 4, { {{0;1;2}{1;2;3}{2;3}{3}} {{4}{4}{4}{4}} {{5;6}{5;6}{5;6}{5;6}} }",
			})
			@DisplayName("set with explicit arrangement and quantified nodes with markers")
			void testArrangementWithQuantifiedNodesWithMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
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
		 *
		 * Note that blank nodes produce no mappings, so we are using the
		 * {@link SequencePattern.Builder#nodeTransform(Function)} method
		 * to inject artificial node labels after the query has been parsed,
		 * causing the final matcher to actually create mappings we can verify.
		 */
		@Nested
		class NodeInBranch {

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
				assertResult(target, builder(expand(query)).nodeTransform(PROMOTE_NODE).build(), config(matches, hits));
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
				assertResult(target, builder(expand(query)).nodeTransform(PROMOTE_NODE).build(), config(matches, hits));
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
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
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
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<2+>[isNotAt(1), $X] or [isLast,$Y] or [isFirst,$Z]', X, 0, -",
				"'<2+>[isNotAt(1), $X] or [isLast,$Y] or [isFirst,$Z]', Z, 1, { {{}} {{}} {{0}} }",
				"'<2+>[isNotAt(1), $X] or [isLast,$Y] or [isFirst,$Z]', Y, 1, { {{}} {{0}} {{}} }",
				"'<2+>[isNotAt(1), $X] or [isLast,$Y] or [isFirst,$Z]', -XX, 1, { {{1;2}} {{}} {{}} }",
				"'<2+>[isNotAt(1), $X] or [isLast,$Y] or [isFirst,$Z]', -XXX, 2, { {{1;2;3}{2;3}} {{}{}} {{}{}} }",
				"'<2+>[isNotAt(1), $X] or [isLast,$Y] or [isFirst,$Z]', ZXX, 2, { {{}{1;2}} {{}{}} {{0}{}} }",
				"'<2+>[isNotAt(1), $X] or [isLast,$Y] or [isFirst,$Z]', ZY, 2, { {{}{}} {{}{1}} {{0}{}} }",
				"'<2+>[isNotAt(1), $X] or <2..3>[isBefore(5),$Y]', X, 0, -",
				"'<2+>[isNotAt(1), $X] or <2..3>[isBefore(5),$Y]', XY, 0, -",
				"'<2+>[isNotAt(1), $X] or <2..3>[isBefore(5),$Y]', X---YY, 0, -",
				"'<2+>[isNotAt(1), $X] or <2..3>[isBefore(5),$Y]', YY, 1, { {{}} {{0;1}} }",
			})
			@DisplayName("disjunction of quantified nodes with markers")
			void testQuantifiedNodesWithMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
			}
		}

		/**
		 * {@link IqlGrouping} nested inside {@link IqlGrouping}
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>quantifier on inner grouping</li>
		 * <li>quantifier on outer grouping</li>
		 * <li>multiple groupings</li>
		 * <li>markers on nodes on various nesting depths</li>
		 * <li>quantifiers on nodes on various nesting depths</li>
		 * </ul>
		 *
		 * Note that blank nodes produce no mappings, so we are using the
		 * {@link SequencePattern.Builder#nodeTransform(Function)} method
		 * to inject artificial node labels after the query has been parsed,
		 * causing the final matcher to actually create mappings we can verify.
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
				assertResult(target, builder(expand(query)).nodeTransform(PROMOTE_NODE).build(), config(matches, hits));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{{[?]}}', X, 1, { {{}} }",

				"'{{[?]}[]}', X, 1, { {{}} {{0}} }",
				"'{[]{[?]}}', X, 1, { {{0}} {{}} }",

				"'{{[*]}}', X, 1, { {{}} }",
				"'{{[*]}[]}', X, 1, { {{}} {{0}} }",
				"'{[]{[*]}}', X, 1, { {{0}} {{}} }",

				"'{{[+]}}', X, 1, { {{0}} }",
				"'{{[+]}[]}', X, 0, -",
				"'{{[+]}[]}', XX, 1, { {{0}} {{1}} }",
				"'{[]{[+]}}', X, 0, -",
				"'{[]{[+]}}', XX, 1, { {{0}} {{1}} }",
				"'{{[+]}[*]}', X, 1, { {{0}} {{}} }",
				"'{{[+]}[*]}', XX, 2, { {{0}{1}} {{}{}} }",
				"'{[*]{[+]}}', X, 1, { {{}} {{0}} }",
				"'{[*]{[+]}}', XX, 3, { {{}{}{}} {{0}{1}{1}} }",
			})
			@DisplayName("nested groups of dummy nodes")
			void testDummyNodesWithGrouping(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).nodeTransform(PROMOTE_NODE).build(), config(matches, hits));
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
				//TODO complete
			})
			@DisplayName("nested groups with markers on nodes")
			void testMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).nodeTransform(PROMOTE_NODE).build(),
						config(matches, hits));
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
				assertResult(target, builder(expand(query)).nodeTransform(PROMOTE_NODE).build(),
						config(matches, hits));
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
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<2+>{{[$X][+]}[$Y]}', XY, 0, -",
				"'<2+>{{[$X][+]}[$Y]}', X-Y, 0, -",
				"'<2+>{{[$X][+]}[$Y]}', X-YX-Y, 1, { {{0;3}} {{2;5}} }",
				"'<2+>{[$X]{[+][$Y]}}', X-YX-Y, 1, { {{0;3}} {{2;5}} }",
			})
			@DisplayName("group nested in quantified group")
			void testOuterQuantification(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
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
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
			}
		}

		/**
		 * {@link IqlGrouping} nested inside {@link IqlSequence}
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>arrangement on set</li>
		 * <li>quantifier on grouping</li>
		 * <li>multiple groupings</li>
		 * <li>markers on nodes on various nesting depths</li>
		 * <li>quantifiers on nodes on various nesting depths</li>
		 * </ul>
		 *
		 * Note that blank nodes produce no mappings, so we are using the
		 * {@link SequencePattern.Builder#nodeTransform(Function)} method
		 * to inject artificial node labels after the query has been parsed,
		 * causing the final matcher to actually create mappings we can verify.
		 */
		@Nested
		class GroupingInSet {

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'[] {[][]}', XX, 0, -",
				"'[] {[][]}', XXX, 1, { {{0}} {{1}} {{2}} }",
				"'[] {[] {[]}}', XXX, 1, { {{0}} {{1}} {{2}} }",
				"'{[][]} []', XX, 0, -",
				"'{[][]} []', XXX, 1, { {{0}} {{1}} {{2}} }",
				"'{{[]} []} []', XXX, 1, { {{0}} {{1}} {{2}} }",
				"'{[][]} [] {[][]}', XXXXX, 1, { {{0}} {{1}} {{2}} {{3}} {{4}} }",
			})
			@DisplayName("nested groups of blank nodes")
			void testBlank(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).nodeTransform(PROMOTE_NODE).build(), config(matches, hits));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{[?]}[]', X, 1, { {{}} {{0}} }",
				"'[]{[?]}', X, 1, { {{0}} {{}} }",

				"'{[*]}[]', X, 1, { {{}} {{0}} }",
				"'[]{[*]}', X, 1, { {{0}} {{}} }",

				"'{[+]}[]', X, 0, -",
				"'{[+]}[]', XX, 1, { {{0}} {{1}} }",
				"'[]{[+]}', X, 0, -",
				"'[]{[+]}', XX, 1, { {{0}} {{1}} }",
				"'{[+]}[*]', X, 1, { {{0}} {{}} }",
				"'{[+]}[*]', XX, 2, { {{0}{1}} {{}{}} }",
				"'[*]{[+]}', X, 1, { {{}} {{0}} }",
				"'[*]{[+]}', XX, 3, { {{}{}{}} {{0}{1}{1}} }",
			})
			@DisplayName("nested groups of dummy nodes")
			void testDummyNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).nodeTransform(PROMOTE_NODE).build(), config(matches, hits));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				// Singleton markers
				"'{[isAt(2), $X][]}[]', XXX, 0, -",
				"'{[isAt(2), $X][]}[]', XXXX, 1, { {{1}} }",
				"'{[isNotAt(2), $X][]}[]', XXX, 1, { {{0}} }",
				"'{[isAfter(2), $X][]}[]', XXXXX, 1, { {{2}} }",
				"'{[isBefore(2), $X][]}[]', XXX, 1, { {{0}} }",
				"'{[isInside(2,4), $X][]}[]', XXXXX, 4, { {{1}{1}{1}{2}} }",
				"'[]{[isOutside(3,4), $X][$Y]}', XXYXXY, 6, { {{1}{1}{4}{4}{4}{4}} {{2}{5}{5}{5}{5}{5}} }",
				// Marker intersection
				"'[]{[][isNotAt(2) && isLast, $X]}', XX, 0, -",
				"'[]{[][isNotAt(2) && isLast, $X]}', XXX, 1, { {{2}} }",
				// Marker union
				"'{[isFirst || isLast, $X][]}[]', XXX, 1, { {{0}} }",
				"'[]{[][isFirst || isLast, $X]}', XXX, 1, { {{2}} }",
				// Complex marker nesting
				"'{[isFirst || (isNotAt(3) && isBefore(4)), $X]}', XXXX, 2, { {{0}{1}} }",

				// Proper nesting
				"'{[isAt(2), $X][]}[isNotAt(3),$X]', XXX, 0, -",
				"'{[isAt(2), $X][]}[isNotAt(3),$X]', XXXX, 1, { {{1}} {{3}} }",
				"'{[isAt(2) || isFirst, $X][]}[isNotAt(3) || isLast,$X]', XXXX, 3, { {{1}{0}{0}} {{3}{3}{3}} }",
				//TODO complete
			})
			@DisplayName("nested groups of nodes with markers")
			void testMarkers(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{2+[$X][$Y]}[$Z]', XYZ, 0, -",
				"'{2+[$X][$Y]}[$Z]', XXYZ, 1, { {{0;1}} {{2}} {{3}} }",
				"'{2+[$X][$Y]}[$Z]', XXXYZZ, 4, { {{0;1;2}{0;1;2}{1;2}{1;2}} {{3}{3}{3}{3}} {{4}{5}{4}{5}} }",
				"'{2+[$X][$Y]}2+[$Z]', XXYZ, 0, -",
				"'{2+[$X][$Y]}2+[$Z]', XXYZZ, 1, { {{0;1}} {{2}} {{3;4}} }",
			})
			@DisplayName("nested groups of quantified nodes")
			void testQuantifiedNodes(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
			}

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'<1+>{[$X]<2+>[$Y]}[]', XY, 0, -",
				"'<1+>{[$X]<2+>[$Y]}[]', XYYX, 1, { {{0}} {{1;2}} }",
				"'<1+>{[$X]<2+>[$Y]}', XYY-XYYY-, 2, { {{0}{4}} {{1;2}{5;6;7}} }",
				"'<1+>{[$X]<2+>[$Y]}', XYYXYYY-, 2, { {{0;3}{3}} {{1;2;4;5;6}{4;5;6}} }",
				"'<1+>{[$X]<2+>[$Y]}', XYYXY-XYY-, 3, { {{0;3}{3}{6}} {{1;2;7;8}{7;8}{7;8}} }",
				"'<1+>{[$X]<2+>[$Y]}2+[$Z]', XYZZXYZZZ, 0, -",
				"'<1+>{[$X]<2+>[$Y]}2+[$Z]', XYZZXYYZZZ, 4, { {{0}{0}{4}{4}} {{5;6}{5;6}{5;6}{5;6}} {{7;8;9}{8;9}{7;8;9}{8;9}} }",
			})
			@DisplayName("quantified groups of quantified nodes")
			void testFullQuantification(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
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
				//TODO complete
			})
			@DisplayName("explicit arangement on set")
			void testArrangement(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).build(), config(matches, hits));
			}
		}

		/**
		 * {@link IqlGrouping} nested inside {@link IqlElementDisjunction}
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>quantifier on grouping</li>
		 * <li>more than 2 groupings</li>
		 * <li>markers on nodes on various nesting depths</li>
		 * <li>quantifiers on nodes on various nesting depths</li>
		 * </ul>
		 *
		 * Note that blank nodes produce no mappings, so we are using the
		 * {@link SequencePattern.Builder#nodeTransform(Function)} method
		 * to inject artificial node labels after the query has been parsed,
		 * causing the final matcher to actually create mappings we can verify.
		 */
		@Nested
		class GroupingInBranch {

			@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
			@CsvSource({
				"'{[][]} or {[][][]}', X, 0, -",
				"'{[][]} or {[][][]}', XX, 1, { {{0}} {{1}} }",
				"'{[][][]} or {[][]}', XX, 1, { {-} {-} {-} {{0}} {{1}} }",
			})
			@DisplayName("nested groups of blank nodes")
			void testBlank(String query, String target, int matches,
					// [node_id][match_id][hits]
					@IntMatrixArg int[][][] hits) {
				assertResult(target, builder(expand(query)).nodeTransform(PROMOTE_NODE).build(), config(matches, hits));
			}

		}

		/**
		 * {@link IqlSequence} nested inside {@link IqlGrouping}
		 * <p>
		 * Aspects to cover:
		 * <ul>
		 * <li>quantifier on grouping</li>
		 * <li>more than 2 groupings</li>
		 * <li>arrangement on set(s)</li>
		 * <li>markers on nodes on various nesting depths</li>
		 * <li>quantifiers on nodes on various nesting depths</li>
		 * </ul>
		 *
		 * Note that blank nodes produce no mappings, so we are using the
		 * {@link SequencePattern.Builder#nodeTransform(Function)} method
		 * to inject artificial node labels after the query has been parsed,
		 * causing the final matcher to actually create mappings we can verify.
		 */
		@Nested
		class SetInGrouping {

		}

		@Nested
		class SetInSet {

		}

		@Nested
		class SetInBranch {

		}

		@Nested
		class BranchInGrouping {

		}

		@Nested
		class BranchInSet {

		}

		@Nested
		class BranchInBranch {

		}

		@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
		@CsvSource({
			// Mismatches - ordered
			"'<1+>{[$X][$Y]}', -, 0, -",
			"'<1+>{[$X][$Y]}', --, 0, -",

			// Simple matches - ordered
			"'<1+>{[$X][$Y]}', XY, 1, {{{0}}{{1}}}",
			"'<1+>{[$X][$Y]}', X-Y, 1, {{{0}}{{2}}}",

			// Multiple matches - ordered
			"'FIRST <1+>{[$X][$Y]}', XYXY, 1, {{{0;2}}{{1;3}}}",
			"'<1+>{[$X][$Y]}', XYXY, 2, {{{0;2}{2}}{{1;3}{3}}}",

			// Mismatches - adjacent
			"'<1+>{ADJACENT [$X][$Y]}', -, 0, -",
			"'<1+>{ADJACENT [$X][$Y]}', --, 0, -",
			"'<1+>{ADJACENT [$X][$Y]}', X-Y, 0, -",

			// Simple match - adjacent
			"'<1+>{ADJACENT [$X][$Y]}', XY, 1, {{{0}}{{1}}}",

			// Multiple matches - adjacent
			"'FIRST <1+>{ADJACENT [$X][$Y]}', XYXY, 1, {{{0;2}}{{1;3}}}",
			// separate matches
			"'<1+>{ADJACENT [$X][$Y]}', XYXY, 2, {{{0;2}{2}}{{1;3}{3}}}",
			"'<1+>{ADJACENT [$X][$Y]}', XY-XY, 2, {{{0}{3}}{{1}{4}}}",
			"'<1+^>{ADJACENT [$X][$Y]}', XY-XY, 2, {{{0;3}{3}}{{1;4}{4}}}",
			"'ADJACENT <1+>{ADJACENT [$X][$Y]}', XY-XY, 2, {{{0}{3}}{{1}{4}}}",

			// Mismatches
			"'<2+>{ADJACENT [$X][$Y]}', XY, 0, -",
			"'<2+>{ADJACENT [$X][$Y]}', XYX-Y, 0, -",
			"'<2+>{ADJACENT [$X][$Y]}', X-YXY, 0, -",

			// Simple (and multiple continuous) matches - adjacent
			"'<2+>{ADJACENT [$X][$Y]}', XY-XY, 0, -",
			"'<2+>{ADJACENT [$X][$Y]}', XY---XY, 0, -",
			"'<2+>{ADJACENT [$X][$Y]}', XYXY, 1, {{{0;2}}{{1;3}}}",
			"'<2+>{ADJACENT [$X][$Y]}', XYXYXY, 2, {{{0;2;4}{2;4}}{{1;3;5}{3;5}}}",
			"'<2+>{ADJACENT [$X][$Y]}', XYXYXYXY, 3, {{{0;2;4;6}{2;4;6}{4;6}}{{1;3;5;7}{3;5;7}{5;7}}}",

			// Multiple discontinuous matches - adjacent
			"'<2+^>{ADJACENT [$X][$Y]}', XY-XY, 1, {{{0;3}}{{1;4}}}",
			"'<2+^>{ADJACENT [$X][$Y]}', XY---XY, 1, {{{0;5}}{{1;6}}}",

			// Nested quantification
			"'<2+>{<2+>[$X][$Y]}', XYY, 0, -",
			"'<2+>{<2+>[$X][$Y]}', XXY, 0, -",
			"'<2+>{<2+>[$X][$Y]}', XXYXXY, 1, {{{0;1;3;4}}{{2;5}}}",
			"'<2+>{<2+>[$X][$Y]}', XXXXYXXY, 3, {{{0;1;2;3;5;6}{1;2;3;5;6}{2;3;5;6}}{{4;7}{4;7}{4;7}}}",
			"'<2+>{<2+>[$X][$Y]}', XXYXXXXY, 1, {{{0;1;3;4;5;6}}{{2;7}}}",

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
			assertResult(target, builder(expand(query)).build(), config(matches, hits));
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
			assertResult(target, builder(expand(query)).build(), config(matches, hits));
		}


		@Disabled
		//TODO copy and enable to add further complex tests
		@ParameterizedTest(name="{index}: {0} in {1} -> {2} matches")
		@CsvSource({
			"'QUERY', TARGET, MATCH_COUNT, { {HITS_1} {HITS_2} {HITS_3} }",
		})
		@DisplayName("NAME")
		void test__Template(String query, String target, int matches,
				// [node_id][match_id][hits]
				@IntMatrixArg int[][][] hits) {
			assertResult(target, builder(expand(query)).build(), config(matches, hits));
		}
	}
}
