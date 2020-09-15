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

import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockContext;
import static de.ims.icarus2.model.api.ModelTestUtils.mockCorpus;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockLayer;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.BUFFER_1;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.BUFFER_2;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.CACHE_1;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.CACHE_2;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.CACHE_3;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.EQUALS_A;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.EQUALS_B;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.EQUALS_NOT_X;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.EQUALS_X;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.EQUALS_X_IC;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.EQUALS_Y;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.NODE_1;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.NODE_2;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.NO_CACHE;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.NO_LABEL;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.NO_LIMIT;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.NO_MARKER;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.NO_MEMBER;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.REGION_1;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.branch;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.constraint;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.eq_exp;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.item;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.mark;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.matchers;
import static de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.Utils.seq;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import de.ims.icarus2.query.api.engine.matcher.IntervalConverter.IntervalArg;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Branch;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.BranchConn;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Cache;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.DynamicClip;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Finish;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Node;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Repetition;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Scan;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.SequenceMatcher;
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
import de.ims.icarus2.query.api.iql.IqlConstraint;
import de.ims.icarus2.query.api.iql.IqlConstraint.IqlPredicate;
import de.ims.icarus2.query.api.iql.IqlElement;
import de.ims.icarus2.query.api.iql.IqlElement.IqlGrouping;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlElement.IqlSequence;
import de.ims.icarus2.query.api.iql.IqlExpression;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlMarker;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerCall;
import de.ims.icarus2.query.api.iql.IqlMarker.IqlMarkerExpression;
import de.ims.icarus2.query.api.iql.IqlMarker.MarkerExpressionType;
import de.ims.icarus2.query.api.iql.IqlQuantifier;
import de.ims.icarus2.query.api.iql.IqlUnique;
import de.ims.icarus2.query.api.iql.NodeArrangement;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.collections.set.ArraySet;
import de.ims.icarus2.util.collections.set.DataSet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
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

	private interface CharPredicate {
		boolean test(char c);
	}

	static class Utils {

		static final CharPredicate EQUALS_X = eq('X');
		static final CharPredicate EQUALS_NOT_X = neq('X');
		static final CharPredicate EQUALS_X_IC = ic('x');
		static final CharPredicate EQUALS_Y = eq('Y');

		static final CharPredicate EQUALS_A = eq('A');
		static final CharPredicate EQUALS_B = eq('B');
		static final CharPredicate EQUALS_C = eq('C');

		static final int NODE_1 = 0;
		static final int NODE_2 = 1;
		static final int CACHE_1 = 0;
		static final int CACHE_2 = 1;
		static final int CACHE_3 = 2;
		static final int CACHE_4 = 3;
		static final int BUFFER_1 = 0;
		static final int BUFFER_2 = 1;
		static final int REGION_1 = 0;
		static final int REGION_2 = 1;

		static final int NO_CACHE = UNSET_INT;
//		static final int NO_INTERVAL = UNSET_INT;
		static final int NO_LIMIT = UNSET_INT;
		static final int NO_MEMBER = UNSET_INT;
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
				nodes[i-1].next = nodes[i];
			}
			// Enforce optimization
			nodes[0].study(new TreeInfo());
			return nodes[0];
		}
		static Branch branch(int id, BranchConn conn, Node...atoms) {
			for (Node atom : atoms) {
				if(atom!=null) {
					atom.next = conn;
				}
			}
			return new Branch(id, conn, atoms);
		}

		static final String NO_LABEL = null;
		static final IqlMarker NO_MARKER = null;
		static final IqlConstraint NO_CONSTRAINT = null;
		static final String ID = "_id_";

		private static void setId(IqlUnique unique) {
			unique.setId(ID);
		}

		static IqlNode node(@Nullable String label, @Nullable IqlMarker marker,
				@Nullable IqlConstraint constraint) {
			IqlNode node = new IqlNode();
			setId(node);
			Optional.ofNullable(label).ifPresent(node::setLabel);
			Optional.ofNullable(marker).ifPresent(node::setMarker);
			Optional.ofNullable(constraint).ifPresent(node::setConstraint);
			return node;
		}

		static <Q extends IqlQuantifier.Quantifiable> Q quantiy(Q target, IqlQuantifier quantifiers) {
			Stream.of(quantifiers).forEach(target::addQuantifier);
			return target;
		}

		static IqlGrouping grouping(IqlElement...elements) {
			IqlGrouping grouping = new IqlGrouping();
			setId(grouping);
			Stream.of(elements).forEach(grouping::addElement);
			return grouping;
		}

		static IqlSequence sequence(IqlElement...elements) {
			IqlSequence sequence = new IqlSequence();
			setId(sequence);
			Stream.of(elements).forEach(sequence::addElement);
			return sequence;
		}

		static IqlSequence adjacent(IqlElement...elements) {
			IqlSequence sequence = sequence(elements);
			sequence.setArrangement(NodeArrangement.ADJACENT);
			return sequence;
		}

		static IqlSequence ordered(IqlElement...elements) {
			IqlSequence sequence = sequence(elements);
			sequence.setArrangement(NodeArrangement.ORDERED);
			return sequence;
		}

		static IqlConstraint constraint(String content) {
			IqlExpression expression = new IqlExpression();
			expression.setContent(content);
			IqlPredicate predicate = new IqlPredicate();
			setId(predicate);
			predicate.setExpression(expression);
			return predicate;
		}

		static String eq_exp(char c) { return "$.toString()==\""+c+"\""; }

		static String and(String...items) { return "(" + String.join(" && ", items) + ")"; }

		static String or(String...items) { return "(" + String.join(" || ", items) + ")"; }

		static IqlMarker mark(String name, Number...args) {
			IqlMarkerCall call = new IqlMarkerCall();
			call.setName(name);
			if(args.length>0) {
				call.setArguments(args);
			}
			return call;
		}

		static IqlMarker and(IqlMarker...markers) {
			IqlMarkerExpression exp = new IqlMarkerExpression();
			exp.setExpressionType(MarkerExpressionType.CONJUNCTION);
			Stream.of(markers).forEach(exp::addItem);
			return exp;
		}

		static IqlMarker or(IqlMarker...markers) {
			IqlMarkerExpression exp = new IqlMarkerExpression();
			exp.setExpressionType(MarkerExpressionType.DISJUNCTION);
			Stream.of(markers).forEach(exp::addItem);
			return exp;
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
			info.offset++;
			return next.study(info);
		}
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

		SequenceMatcher matcher = pattern.matcher();

		config.assertResult(matcher, target);
	}

	interface NodeTest {
		StateMachineSetup setup();
	}

	static MatchConfig match(int startPos, boolean expectedResult, int expectedCount) {
		return new MatchConfig(startPos, expectedResult, expectedCount);
	}

	static MatchConfig match(boolean expectedResult, int expectedCount) {
		return new MatchConfig(expectedResult, expectedCount);
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

		MatchConfig node(NodeConfig node) { nodes.add(requireNonNull(node)); return this; }

		MatchConfig cache(CacheConfig cache) { caches.add(requireNonNull(cache)); return this; }

		MatchConfig map(int nodeId, int...indices) { mapping.map(nodeId, indices); return this; }

		MatchConfig map(boolean condition, int nodeId, int...indices) { if(condition) map(nodeId, indices); return this;}

		MatchConfig result(ResultConfig result) { results.add(requireNonNull(result)); return this; }
		MatchConfig results(int count, ObjIntConsumer<ResultConfig> action) {
			for (int i = 0; i < count; i++) {
				ResultConfig result = SequencePatternTest.result(results.size());
				action.accept(result, i);
				results.add(result);
			}
			return this;
		}

		@Override
		public void accept(State state) {
			assertThat(nextResult)
				.as("No more resutls buffered - only got %d", _int(results.size()))
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

		void assertResult(Node root, State state) {
			assertThat(startPos).as("Negative position").isGreaterThanOrEqualTo(0);

			if(!results.isEmpty()) {
				state.resultHandler = this;
			}

			// Verify correct result
			assertThat(root.match(state, startPos))
				.as("Result for start position %d", _int(startPos))
				.isEqualTo(expectedResult);

			assertState(state);
		}

		void assertResult(SequenceMatcher matcher, Container target) {
			if(!results.isEmpty()) {
				matcher.resultHandler = this;
			}

			// Verify correct result
			assertThat(matcher.matches(0, target))
				.as("Result mismatch")
				.isEqualTo(expectedResult);

			assertState(matcher);
		}
	}

	private static class CacheConfig {
		private final int cacheId;
		private final boolean expectWindowSet;
		/** The range of target elements we expect to be visited. If null, only startPos should be used. */
		private Interval window;
		private final IntSet hits = new IntOpenHashSet();
		private final IntSet set = new IntOpenHashSet();

		private CacheConfig(int cacheId, boolean expectWindowSet) {
			this.cacheId = cacheId;
			this.expectWindowSet = expectWindowSet;
		}

		CacheConfig window(Interval window) { this.window = window; return this; }

		CacheConfig window(int from, int to) { window = Interval.of(from, to); return this; }

		CacheConfig window(String target) { window = Interval.of(0, target.length()-1); return this; }

		CacheConfig window(int spot) { window = Interval.of(spot); return this; }

		CacheConfig hits(int...indices) { for(int i=0; i< indices.length; i++) hits.add(indices[i]); return this; }

		CacheConfig hits(boolean condition, int...indices) { if(condition) hits(indices); return this; }

		CacheConfig set(int...indices) { for(int i=0; i< indices.length; i++) set.add(indices[i]); return this; }

		CacheConfig set(boolean condition, int...indices) { if(condition) set(indices); return this; }

		CacheConfig hits(String s, CharPredicate pred) {
			for (int i = 0; i < s.length(); i++) {
				if(pred.test(s.charAt(i))) {
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
			Interval window = this.window;
			if(window==null) {
				window = Interval.of(startPos);
			}
			if(window.isEmpty()) {
				return;
			}
			Cache cache = state.caches[cacheId];
			for (int i = window.from; i <= window.to; i++) {
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

		ResultConfig map(int nodeId, Interval indices) {
			indices.stream().mapToObj(pos -> Pair.pair(nodeId, pos)).forEach(mapping::add);
			return this;
		}

		ResultConfig map(boolean condition, int nodeId, int...indices) { if(condition) map(nodeId, indices); return this;}
		ResultConfig map(boolean condition, int nodeId, Interval indices) { if(condition) map(nodeId, indices); return this;}

		void assertMapping(State state) {
			int size = mapping.size();
			assertThat(state.entry)
				.as("Incorrect number of mappings in result #%d", _int(index))
				.isEqualTo(size);

			for (int i = 0; i < size; i++) {
				Pair<Integer, Integer> m = mapping.get(i);
				assertThat(state.m_node[i])
					.as("Node id mismatch in mapping at index %d in result #%d", _int(i), _int(index))
					.isEqualTo(m.first.intValue());
				assertThat(state.m_pos[i])
					.as("Position mismatch in mapping at index %d in result #%d", _int(i), _int(index))
					.isEqualTo(m.second.intValue());
			}
		}
	}

	static ResultConfig result(int index) {
		return new ResultConfig(index);
	}

	static class SM_Config {
		private final List<SM_NodeConfig<?>> nodes = new ArrayList<>();
		//TODO
	}

	static class SM_NodeConfig<N extends Node> {
		String label;
		int id = UNSET_INT;
		Class<N> type;
		BiConsumer<SM_Config,N> asserter;
		//TODO
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
						.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
				);
			}

			@CsvSource({
				"XX, 0, 0-1, 1",
				"-XX, 1, 1-2, 2",
				"YXX, 1, 1-2, 2",
				"-XX-, 1, 1-3, 2",
			})
			@ParameterizedTest(name="{index}: X'{2,5}' in [{0}], start at {1}")
			default void testFindMinimum(String target, int startPos,
					@IntervalArg Interval window, int last) {
				assertResult(target, setup(), match(startPos, true, 1)
						.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
						.map(NODE_1, startPos, startPos+1)
						.node(node(NODE_1).last(last))
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
						.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
						.map(NODE_1, Interval.of(startPos, startPos+4).asArray())
						.node(node(NODE_1).last(last))
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
						.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
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
						.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
						.map(NODE_1, startPos, startPos+1)
						.node(node(NODE_1).last(last))
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
						.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
						.map(NODE_1, matched.asArray())
						.node(node(NODE_1).last(last))
				);
			}

		}
	}

	@Test
	void testBuilder() {
		assertThat(SequencePattern.builder()).isNotNull();
	}

	@Nested
	class ForIndividualNodes {

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
					sms.nodes = new IqlNode[1];
					sms.cacheCount = 1;
					sms.root = seq(
							new Single(0, NODE_1, CACHE_1, NO_MEMBER),
							new Finish(UNSET_LONG));
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
							.cache(cache(CACHE_1, true).hits(expectedResult, startPos))
							.map(expectedResult, NODE_1, startPos)
							.node(node(NODE_1).last(expectedResult, startPos))
					);
				}
			}

			@Nested
			class WithTwoNodes implements NodeTest {

				@Override
				public StateMachineSetup setup() {
					StateMachineSetup sms = new StateMachineSetup();
					sms.nodes = new IqlNode[2];
					sms.cacheCount = 2;
					sms.root = seq(
							new Single(0, NODE_1, CACHE_1, NO_MEMBER),
							new Single(1, NODE_2, CACHE_2, NO_MEMBER),
							new Finish(UNSET_LONG));
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
							.cache(cache(CACHE_1, true)
									.window(startPos)
									.hits(node1Hit, startPos))
							.cache(cache(CACHE_2, false)
									.window(startPos+1)
									.hits(node2Hit, startPos+1)
									.set(node1Hit, startPos+1))
							.map(node1Hit, NODE_1, startPos)
							.map(node2Hit, NODE_2, startPos+1)
							.node(node(NODE_1).last(expectedResult, startPos))
							.node(node(NODE_2).last(expectedResult, startPos+1))
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
						sms.nodes = new IqlNode[1];
						sms.cacheCount = 1;
						sms.limit = limit;
						sms.root = seq(
								new Scan(0, NO_CACHE, true),
								new Single(1, NODE_1, CACHE_1, NO_MEMBER),
								new Finish(limit));
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
								.cache(cache(CACHE_1, true)
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
								.cache(cache(CACHE_1, true)
										.window(startPos, target.length()-1)
										.hits(hit))
								.node(node(NODE_1).last(hit))
								.result(result(0).map(NODE_1, hit))
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
								.cache(cache(CACHE_1, true)
										.window(startPos, target.length()-1)
										.hits(target, EQUALS_X))
								.node(node(NODE_1).last(last))
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
								.cache(cache(CACHE_1, true)
										.window(startPos, last)
										.hits(target, EQUALS_X))
								.node(node(NODE_1).last(last))
						);
					}
				}

				@Nested
				class ForwardSingleCached {

					StateMachineSetup setup(int limit) {
						StateMachineSetup sms = new StateMachineSetup();
						sms.nodes = new IqlNode[1];
						sms.cacheCount = 2;
						sms.limit = limit;
						sms.root = seq(
								new Scan(0, CACHE_1, true),
								new Single(1, NODE_1, CACHE_2, NO_MEMBER),
								new Finish(limit));
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
								.cache(cache(CACHE_1, true)
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
								.cache(cache(CACHE_1, true)
										.window(startPos, target.length()-1)
										.hits(target, EQUALS_X))
								.node(node(NODE_1).last(hit))
								.result(result(0).map(NODE_1, hit))
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
								.cache(cache(CACHE_1, true)
										.window(startPos, target.length()-1)
										.hits(target, EQUALS_X))
								.cache(cache(CACHE_2, true)
										.window(startPos, target.length()-1).
										hits(target, EQUALS_X))
								.node(node(NODE_1).last(last))
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
								.cache(cache(CACHE_1, true)
										.window(startPos, last)
										.hits(target, EQUALS_X))
								.cache(cache(CACHE_2, true)
										.window(startPos, last)
										.hits(target, EQUALS_X))
								.node(node(NODE_1).last(last))
						);
					}
				}

				@Nested
				class BackwardSingle {

					StateMachineSetup setup(int  limit) {
						StateMachineSetup sms = new StateMachineSetup();
						sms.nodes = new IqlNode[1];
						sms.cacheCount = 1;
						sms.limit = limit;
						sms.root = seq(
								new Scan(0, NO_CACHE, false),
								new Single(1, NODE_1, CACHE_1, NO_MEMBER),
								new Finish(limit));
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
								.cache(cache(CACHE_1, true)
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
								.cache(cache(CACHE_1, true)
										.window(startPos, target.length()-1)
										.hits(target, EQUALS_X))
								.node(node(NODE_1).last(hit))
								.result(result(0).map(NODE_1, hit))
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
								.cache(cache(CACHE_1, true)
										.window(window)
										.hits(target, EQUALS_X))
								.node(node(NODE_1).last(last))
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
								.cache(cache(CACHE_1, true)
										.window(last, target.length()-1)
										.hits(target, EQUALS_X))
								.node(node(NODE_1).last(last))
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
						sms.nodes = new IqlNode[1];
						sms.cacheCount = 1;
						sms.limit = limit;
						sms.intervals = new Interval[]{ region };
						sms.root = seq(
								new DynamicClip(REGION_1),
								new Scan(0, NO_CACHE, true),
								new Single(1, NODE_1, CACHE_1, NO_MEMBER),
								new Finish(limit));
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
								.cache(cache(CACHE_1, true).window(visited))
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
								.cache(cache(CACHE_1, true).hits(hit))
								.node(node(NODE_1).last(hit))
								.result(result(0).map(NODE_1, hit))
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
								.cache(cache(CACHE_1, true).hits(target, EQUALS_X))
								.node(node(NODE_1).last(last))
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
								.cache(cache(CACHE_1, true)
										.window(startPos, last)
										.hits(target, EQUALS_X))
								.node(node(NODE_1).last(last))
						);
					}
				}

				@Nested
				class ForwardSingleCached {

					StateMachineSetup setup(Interval region, int limit) {
						StateMachineSetup sms = new StateMachineSetup();
						sms.nodes = new IqlNode[1];
						sms.cacheCount = 2;
						sms.limit = limit;
						sms.intervals = new Interval[]{ region };
						sms.root = seq(
								new DynamicClip(REGION_1),
								new Scan(0, CACHE_1, true),
								new Single(1, NODE_1, CACHE_2, NO_MEMBER),
								new Finish(limit));
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
								.cache(cache(CACHE_1, true)
										.window(startPos, region.to))
								.cache(cache(CACHE_2, true)
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
								.cache(cache(CACHE_1, true)
										.window(startPos, region.to)
										.hits(hit))
								.cache(cache(CACHE_2, true)
										.window(startPos, region.to)
										.hits(hit))
								.node(node(NODE_1).last(hit))
								.result(result(0).map(NODE_1, hit))
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
								.cache(cache(CACHE_1, true)
										.window(startPos, region.to)
										.hits(target, region, EQUALS_X))
								.cache(cache(CACHE_2, true)
										.window(startPos, region.to)
										.hits(target, region, EQUALS_X))
								.node(node(NODE_1).last(last))
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
								.cache(cache(CACHE_1, true)
										.window(startPos, last)
										.hits(target, EQUALS_X))
								.cache(cache(CACHE_2, true)
										.window(startPos, last)
										.hits(target, EQUALS_X))
								.node(node(NODE_1).last(last))
						);
					}
				}

				@Nested
				class BackwardSingle {

					StateMachineSetup setup(Interval region, int limit) {
						StateMachineSetup sms = new StateMachineSetup();
						sms.nodes = new IqlNode[1];
						sms.cacheCount = 1;
						sms.limit = limit;
						sms.intervals = new Interval[]{ region };
						sms.root = seq(
								new DynamicClip(REGION_1),
								new Scan(0, NO_CACHE, false),
								new Single(1, NODE_1, CACHE_1, NO_MEMBER),
								new Finish(limit));
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
								.cache(cache(CACHE_1, true)
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
								.cache(cache(CACHE_1, true)
										.window(startPos, region.to)
										.hits(target, region, EQUALS_X))
								.node(node(NODE_1).last(hit))
								.result(result(0).map(NODE_1, hit))
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
								.cache(cache(CACHE_1, true)
										.window(startPos, region.to)
										.hits(target, region, EQUALS_X))
								.node(node(NODE_1).last(last))
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
								.cache(cache(CACHE_1, true)
										.window(last, region.to)
										.hits(target, EQUALS_X))
								.node(node(NODE_1).last(last))
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
							sms.nodes = new IqlNode[1];
							sms.cacheCount = 1;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(0, new Single(1, NODE_1, CACHE_1, NO_MEMBER),
											CMIN, CMAX, SequencePattern.GREEDY, BUFFER_1, BUFFER_2),
									new Finish(UNSET_LONG));
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
							sms.nodes = new IqlNode[2];
							sms.cacheCount = 2;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(0, new Single(1, NODE_1, CACHE_1, NO_MEMBER),
											CMIN, CMAX, SequencePattern.GREEDY, BUFFER_1, BUFFER_2),
									new Single(1, NODE_2, CACHE_2, NO_MEMBER),
									new Finish(UNSET_LONG));
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
									.cache(cache(CACHE_1, true).window(visited1).hits(target, EQUALS_X_IC))
									.map(NODE_1, matched1.asArray())
									.node(node(NODE_1).last(last1))

									.cache(cache(CACHE_2, true).window(visited2).hits(target, EQUALS_X))
									.map(NODE_2, matched2.asArray())
									.node(node(NODE_2).last(last2))
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
							sms.nodes = new IqlNode[1];
							sms.cacheCount = 1;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(0, new Single(1, NODE_1, CACHE_1, NO_MEMBER),
											CMIN, CMAX, SequencePattern.POSSESSIVE, BUFFER_1, BUFFER_2),
									new Finish(UNSET_LONG));
							sms.matchers = matchers(matcher(0, EQUALS_X));
							return sms;
						}

					}

					@Nested
					class Expansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.nodes = new IqlNode[2];
							sms.cacheCount = 2;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(0, new Single(1, NODE_1, CACHE_1, NO_MEMBER),
											CMIN, CMAX, SequencePattern.POSSESSIVE, BUFFER_1, BUFFER_2),
									new Single(1, NODE_2, CACHE_2, NO_MEMBER),
									new Finish(UNSET_LONG));
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
									.cache(cache(CACHE_1, true).window(visited1).hits(target, EQUALS_X_IC))
									.node(node(NODE_1).last(last))

									.cache(cache(CACHE_2, true).window(visited2))
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
									.cache(cache(CACHE_1, true).window(visited1).hits(target, EQUALS_X_IC))
									.map(NODE_1, matched1.asArray())
									.node(node(NODE_1).last(last1))

									.cache(cache(CACHE_2, true).window(visited2).hits(target, EQUALS_X))
									.map(NODE_2, matched2.asArray())
									.node(node(NODE_2).last(last2))
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
							sms.nodes = new IqlNode[1];
							sms.cacheCount = 1;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(0, new Single(1, NODE_1, CACHE_1, NO_MEMBER),
											CMIN, CMAX, SequencePattern.RELUCTANT, BUFFER_1, BUFFER_2),
									new Finish(UNSET_LONG));
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
									.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
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
									.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
									.map(NODE_1, startPos, startPos+1)
									.node(node(NODE_1).last(last))
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
									.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
									.map(NODE_1, Interval.of(startPos, startPos+1).asArray())
									.node(node(NODE_1).last(last))
							);
						}

					}

					@Nested
					class WithProxy implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.nodes = new IqlNode[1];
							sms.cacheCount = 1;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(0, new Single(1, NODE_1, CACHE_1, NO_MEMBER),
											CMIN, CMAX, SequencePattern.RELUCTANT, BUFFER_1, BUFFER_2),
									new Proxy(NODE_2), // we need this to motivate the reluctant expansion
									new Finish(UNSET_LONG));
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
									.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
									.map(NODE_1, startPos, startPos+1)
									.node(node(NODE_1).last(last))
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
									.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
									.map(NODE_1, Interval.of(startPos, startPos+4).asArray())
									.node(node(NODE_1).last(last))
							);
						}

					}

					@Nested
					class Expansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.nodes = new IqlNode[2];
							sms.cacheCount = 2;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(0, new Single(1, NODE_1, CACHE_1, NO_MEMBER),
											CMIN, CMAX, SequencePattern.RELUCTANT, BUFFER_1, BUFFER_2),
									new Single(2, NODE_2, CACHE_2, NO_MEMBER),
									new Finish(UNSET_LONG));
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
									.cache(cache(CACHE_1, true).window(visited1).hits(target, EQUALS_X_IC))
									.node(node(NODE_1).last(last))

									.cache(cache(CACHE_2, true).window(visited2))
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
									.cache(cache(CACHE_1, true).window(visited1).hits(target, EQUALS_X_IC))
									.map(NODE_1, matched1.asArray())
									.node(node(NODE_1).last(last1))

									.cache(cache(CACHE_2, true).window(visited2).hits(target, EQUALS_X))
									.map(NODE_2, matched2.asArray())
									.node(node(NODE_2).last(last2))
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
							sms.nodes = new IqlNode[1];
							sms.cacheCount = 1;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(0, new Single(1, NODE_1, CACHE_1, NO_MEMBER),
											CMIN, CINF, SequencePattern.GREEDY, BUFFER_1, BUFFER_2),
									new Finish(UNSET_LONG));
							sms.matchers = matchers(matcher(0, EQUALS_X));
							return sms;
						}

					}

					@Nested
					class Expansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.nodes = new IqlNode[2];
							sms.cacheCount = 2;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(0, new Single(1, NODE_1, CACHE_1, NO_MEMBER),
											CMIN, CINF, SequencePattern.GREEDY, BUFFER_1, BUFFER_2),
									new Single(1, NODE_2, CACHE_2, NO_MEMBER),
									new Finish(UNSET_LONG));
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
									.cache(cache(CACHE_1, true).window(visited1).hits(target, EQUALS_X_IC))
									.map(NODE_1, matched1.asArray())
									.node(node(NODE_1).last(last1))

									.cache(cache(CACHE_2, true).window(visited2).hits(target, EQUALS_X))
									.map(NODE_2, matched2.asArray())
									.node(node(NODE_2).last(last2))
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
							sms.nodes = new IqlNode[1];
							sms.cacheCount = 1;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(0, new Single(1, NODE_1, CACHE_1, NO_MEMBER),
											CMIN, CINF, SequencePattern.POSSESSIVE, BUFFER_1, BUFFER_2),
									new Finish(UNSET_LONG));
							sms.matchers = matchers(matcher(0, EQUALS_X));
							return sms;
						}

					}

					@Nested
					class OverExpansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.nodes = new IqlNode[2];
							sms.cacheCount = 2;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(0, new Single(1, NODE_1, CACHE_1, NO_MEMBER),
											CMIN, CINF, SequencePattern.POSSESSIVE, BUFFER_1, BUFFER_2),
									new Single(1, NODE_2, CACHE_2, NO_MEMBER),
									new Finish(UNSET_LONG));
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
									.cache(cache(CACHE_1, true).window(visited1).hits(target, EQUALS_X_IC))
									.node(node(NODE_1).last(last))

									.cache(cache(CACHE_2, true).window(visited2))
							);
						}
					}

					@Nested
					class Expansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.nodes = new IqlNode[2];
							sms.cacheCount = 2;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(0, new Single(1, NODE_1, CACHE_1, NO_MEMBER),
											CMIN, CINF, SequencePattern.POSSESSIVE, BUFFER_1, BUFFER_2),
									new Single(1, NODE_2, CACHE_2, NO_MEMBER),
									new Finish(UNSET_LONG));
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
									.cache(cache(CACHE_1, true).window(visited1).hits(target, EQUALS_X))
									.map(NODE_1, matched1.asArray())
									.node(node(NODE_1).last(last1))

									.cache(cache(CACHE_2, true).window(visited2).hits(target, EQUALS_Y))
									.map(NODE_2, matched2.asArray())
									.node(node(NODE_2).last(last2))
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
							sms.nodes = new IqlNode[1];
							sms.cacheCount = 1;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(0, new Single(1, NODE_1, CACHE_1, NO_MEMBER),
											CMIN, CINF, SequencePattern.RELUCTANT, BUFFER_1, BUFFER_2),
									new Finish(UNSET_LONG));
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
									.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
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
									.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
									.map(NODE_1, startPos, startPos+1)
									.node(node(NODE_1).last(last))
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
									.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
									.map(NODE_1, Interval.of(startPos, startPos+1).asArray())
									.node(node(NODE_1).last(last))
							);
						}

					}

					@Nested
					class WithProxy implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.nodes = new IqlNode[1];
							sms.cacheCount = 1;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(0, new Single(1, NODE_1, CACHE_1, NO_MEMBER),
											CMIN, CINF, SequencePattern.RELUCTANT, BUFFER_1, BUFFER_2),
									new Proxy(NODE_2), // we need this to motivate the reluctant expansion
									new Finish(UNSET_LONG));
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
									.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
									.map(NODE_1, startPos, startPos+1)
									.node(node(NODE_1).last(last))
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
									.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
									.map(NODE_1, matched.asArray())
									.node(node(NODE_1).last(last))
							);
						}

					}

					@Nested
					class Expansion implements NodeTest {

						@Override
						public StateMachineSetup setup() {
							StateMachineSetup sms = new StateMachineSetup();
							sms.nodes = new IqlNode[2];
							sms.cacheCount = 2;
							sms.bufferCount = 2;
							sms.root = seq(
									new Repetition(0, new Single(1, NODE_1, CACHE_1, NO_MEMBER),
											CMIN, CINF, SequencePattern.RELUCTANT, BUFFER_1, BUFFER_2),
									new Single(2, NODE_2, CACHE_2, NO_MEMBER),
									new Finish(UNSET_LONG));
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
									.cache(cache(CACHE_1, true).window(visited1).hits(target, EQUALS_X_IC))
									.node(node(NODE_1).last(last))

									.cache(cache(CACHE_2, true).window(visited2))
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
									.cache(cache(CACHE_1, true).window(visited1).hits(target, EQUALS_X_IC))
									.map(NODE_1, matched1.asArray())
									.node(node(NODE_1).last(last1))

									.cache(cache(CACHE_2, true).window(visited2).hits(target, EQUALS_X))
									.map(NODE_2, matched2.asArray())
									.node(node(NODE_2).last(last2))
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
					sms.nodes = new IqlNode[2];
					sms.cacheCount = 2;
					BranchConn conn = new BranchConn();
					sms.root = seq(
							branch(0, conn,
									new Single(1, NODE_1, CACHE_1, NO_MEMBER),
									new Single(2, NODE_2, CACHE_2, NO_MEMBER)),
							conn,
							new Finish(UNSET_LONG));
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
							.cache(cache(CACHE_1, true).window(startPos))

							.cache(cache(CACHE_2, true).window(startPos))
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
							.cache(cache(CACHE_1, true).window(startPos).hits(startPos))
							.node(node(NODE_1).last(startPos))
							.map(NODE_1, startPos)

							.cache(cache(CACHE_2, true).window(startPos))
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
							.cache(cache(CACHE_1, true).window(startPos)) // option A must have been visited

							.cache(cache(CACHE_2, true).window(startPos).hits(startPos))
							.node(node(NODE_2).last(startPos))
							.map(NODE_2, startPos)
					);
				}
			}

			@Nested
			class GreedyOptional implements NodeTest {

				@Override
				public StateMachineSetup setup() {
					StateMachineSetup sms = new StateMachineSetup();
					sms.nodes = new IqlNode[1];
					sms.cacheCount = 1;
					sms.limit = 1;
					BranchConn conn = new BranchConn();
					sms.root = seq(
							branch(0, conn,
									new Single(1, NODE_1, CACHE_1, NO_MEMBER),
									null),
							conn,
							new Finish(1));
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
							.cache(cache(CACHE_1, true).window(startPos).hits(startPos))
							.node(node(NODE_1).last(startPos))
							.map(NODE_1, startPos)
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
							.cache(cache(CACHE_1, true).window(startPos)) // option A must have been visited
					);
				}
			}

			@Nested
			class ReluctantOptional implements NodeTest {

				@Override
				public StateMachineSetup setup() {
					StateMachineSetup sms = new StateMachineSetup();
					sms.nodes = new IqlNode[2];
					sms.cacheCount = 2;
					BranchConn conn = new BranchConn();
					sms.root = seq(
							branch(0, conn,
									null,
									new Single(1, NODE_1, CACHE_1, NO_MEMBER)),
							conn,
							new Single(2, NODE_2, CACHE_2, NO_MEMBER), // needed to force reluctant expansion
							new Finish(UNSET_LONG));
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
				@ParameterizedTest(name="{index}: A'{0,1}'? in [{0}], start at {1}")
				void testReluctantPath(String target, int startPos) {

					assertResult(target, setup(), match(startPos, true, 1)
							.cache(cache(CACHE_1, true).window(startPos).hits(startPos))
							.node(node(NODE_1).last(startPos))
							.map(NODE_1, startPos)

							.cache(cache(CACHE_2, true).window(startPos, startPos+1).hits(startPos+1))
							.node(node(NODE_2).last(startPos+1))
							.map(NODE_2, startPos+1)
					);
				}

				@CsvSource({
					"B, 0",
					"BB, 0",
					"BB, 1",
					"AB, 1",
				})
				@ParameterizedTest(name="{index}: A'{0,1}'? in [{0}], start at {1}")
				void testZeroWidthAssertion(String target, int startPos) {

					assertResult(target, setup(), match(startPos, true, 1)
							.cache(cache(CACHE_2, true).window(startPos).hits(startPos))
							.node(node(NODE_2).last(startPos))
							.map(NODE_2, startPos)
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
						nodes.add(new Scan(id++, NO_CACHE, true));
						nodes.add(new Single(id++, i, i, NO_MEMBER));
					}
					nodes.add(new Finish(limit));
					return nodes.toArray(new Node[0]);
				}

				@SuppressWarnings("unchecked")
				private StateMachineSetup setup(int limit, CharPredicate...predicates) {
					int nodeCount = predicates.length;
					StateMachineSetup sms = new StateMachineSetup();
					sms.nodes = new IqlNode[nodeCount];
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
							.cache(cache(CACHE_1, true)
									.window(visitedA)
									.hits(hitsA.asArray()))
							.cache(cache(CACHE_2, true)
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
							.cache(cache(CACHE_1, true)
									.window(visitedA)
									.hits(hitA))
							.cache(cache(CACHE_2, true)
									.window(visitedB)
									.hits(hitB))
							.result(result(0)
									.map(NODE_1, hitA)
									.map(NODE_2, hitB))
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
							.cache(cache(CACHE_1, true)
									.window(visitedA)
									.hits(hitA1)
									.hits(hitA2)) // 2nd hit for A only reflected in cache, no dispatched result
							.cache(cache(CACHE_2, true)
									.window(visitedB)
									.hits(hitB))
							.result(result(0)
									.map(NODE_1, hitA1)
									.map(NODE_2, hitB))
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
							.cache(cache(CACHE_1, true)
									.window(visitedA)
									.hits(hitA1)
									.hits(hitA2))
							.cache(cache(CACHE_2, true)
									.window(visitedB)
									.hits(hitB))
							.result(result(0)
									.map(NODE_1, hitA1)
									.map(NODE_2, hitB))
							.result(result(1)
									.map(NODE_1, hitA2)
									.map(NODE_2, hitB))
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
							.cache(cache(CACHE_1, true)
									.window(visitedA)
									.hits(hitA))
							.cache(cache(CACHE_2, true)
									.window(visitedB)
									.hits(hitB1)
									.hits(hitB2))
							.result(result(0)
									.map(NODE_1, hitA)
									.map(NODE_2, hitB1))
							.result(result(1)
									.map(NODE_1, hitA)
									.map(NODE_2, hitB2))
					);
				}

				//TODO scan for multiple matches (at least 2)
			}

			@Nested
			class BranchAndRepetition {
				private StateMachineSetup setup(Node...options) {
					StateMachineSetup sms = new StateMachineSetup();
					sms.nodes = new IqlNode[2];
					sms.cacheCount = 2;
					BranchConn conn = new BranchConn();
					sms.root = seq(
							branch(0, conn, options),
							conn,
							new Finish(UNSET_LONG));
					sms.matchers = matchers(
							matcher(0, EQUALS_A),
							matcher(1, EQUALS_B));
					return sms;
				}


				//TODO
			}
		}

	}

	@Nested
	class ForProcessor {

	}

	@Nested
	class ForFullPattern {

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
						builder(Utils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X')))).build(),
						match(false, 0));
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
						builder(Utils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X')))).build(),
						match(true, 1)
							.cache(cache(CACHE_1, true)
									.window(0, target.length()-1)
									.hits(hit))
							.result(result(0)
									.map(NODE_1, hit))
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
							builder(Utils.node(NO_LABEL, mark("isFirst"), constraint(eq_exp('X')))).build(),
							match(true, 1)
								.cache(cache(CACHE_1, false)
										.window(target)
										.set(0)
										.hits(0))
								.result(result(0).map(NODE_1, 0))
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
							builder(Utils.node(NO_LABEL, mark("isFirst"), constraint(eq_exp('X')))).build(),
							match(false, 0)
							.cache(cache(CACHE_1, false)
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
							builder(Utils.node(NO_LABEL, mark("isLast"), constraint(eq_exp('X')))).build(),
							match(true, 1)
								.cache(cache(CACHE_1, false)
										.window(target)
										.set(last)
										.hits(last))
								.result(result(0).map(NODE_1, target.length()-1))
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
							builder(Utils.node(NO_LABEL, mark("isLast"), constraint(eq_exp('X')))).build(),
							match(false, 0)
								.cache(cache(CACHE_1, false)
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
							builder(Utils.node(NO_LABEL, mark("isAt", _int(pos+1)), constraint(eq_exp('X')))).build(),
							match(true, 1)
								.cache(cache(CACHE_1, false)
										.window(0, last)
										.set(pos)
										.hits(pos))
								.result(result(0).map(NODE_1, pos))
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
							builder(Utils.node(NO_LABEL, mark("isAt", _int(pos+1)), constraint(eq_exp('X')))).build(),
							match(false, 0)
								.cache(cache(CACHE_1, false)
										.window(target)
										.set(pos))
					);
				}

				@ParameterizedTest(name="{index}: [isAfter({1}),X] in {0}, hits={2}")
				@CsvSource({
					"XX, 1, 1",
					"XXX, 1, 1-2",
					"XXX, 2, 2",
				})
				@DisplayName("Node after specific position")
				void testIsAfter(String target, int arg, @IntervalArg Interval hits) {
					assertResult(target,
							// Remember that markers use 1-based value space
							builder(Utils.node(NO_LABEL, mark("isAfter", _int(arg)), constraint(eq_exp('X')))).build(),
							match(true, hits.size())
								.cache(cache(CACHE_1, false)
										.window(0, target.length()-1)
										.set(hits.asArray())
										.hits(hits.asArray()))
								.results(hits.size(), (r, i) -> r.map(NODE_1, hits.indexAt(i)))
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
							builder(Utils.node(NO_LABEL, mark("isAfter", _int(arg)), constraint(eq_exp('X')))).build(),
							match(false, 0)
								.cache(cache(CACHE_1, false)
										.window(target)
										.set(Interval.of(arg, last).asArray()))
					);
				}

				@ParameterizedTest(name="{index}: [isBefore({1}),X] in {0}, hits={2}")
				@CsvSource({
					"XX, 2, 0",
					"XXX, 2, 0",
					"XXX, 3, 0-1",
				})
				@DisplayName("Node before specific position")
				void testIsBefore(String target, int arg, @IntervalArg Interval hits) {
					assertResult(target,
							// Remember that markers use 1-based value space
							builder(Utils.node(NO_LABEL, mark("isBefore", _int(arg)), constraint(eq_exp('X')))).build(),
							match(true, hits.size())
								.cache(cache(CACHE_1, false)
										.window(0, target.length()-1)
										.set(hits.asArray())
										.hits(hits.asArray()))
								.results(hits.size(), (r, i) -> r.map(NODE_1, hits.indexAt(i)))
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
							builder(Utils.node(NO_LABEL, mark("isBefore", _int(arg)), constraint(eq_exp('X')))).build(),
							match(false, 0)
								.cache(cache(CACHE_1, false)
										.window(target)
										.set(Interval.of(0, arg-2).asArray()))
					);
				}

				@ParameterizedTest(name="{index}: [isNotAt({1}),X] in {0}, hits1={2}, hits2={3}")
				@CsvSource({
					"XX, 1, -, 1",
					"XXX, 1, -, 1-2",
					"XXX, 2, 0, 2",
				})
				@DisplayName("Node at any but specific position")
				void testIsNotAt(String target, int arg,
						@IntervalArg Interval hits1, @IntervalArg Interval hits2) {
					final int last = target.length()-1;
					assertResult(target,
							// Remember that markers use 1-based value space
							builder(Utils.node(NO_LABEL, mark("isNotAt", _int(arg)), constraint(eq_exp('X')))).build(),
							match(true, hits1.size() + hits2.size())
								.cache(cache(CACHE_1, false)
										.window(target)
										.set(Interval.of(0, arg-2).asArray())
										.set(Interval.of(arg, last).asArray())
										.hits(hits1.asArray())
										.hits(hits2.asArray()))
								.results(hits1.size(), (r, i) -> r.map(NODE_1, hits1.indexAt(i)))
								.results(hits2.size(), (r, i) -> r.map(NODE_1, hits2.indexAt(i)))
					);
				}
			}
		}

		@Nested
		class ForIqlSequence {

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
						builder(Utils.sequence(
								Utils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
								Utils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y'))))
								).build(),
						match(false, 0));
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
						builder(Utils.sequence(
								Utils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('X'))),
								Utils.node(NO_LABEL, NO_MARKER, constraint(eq_exp('Y'))))
								).build(),
						match(true, 1)
							//  remember that our state machine is built back to front
							.cache(cache(CACHE_1, true)
									.window(hitX+1, target.length()-1)
									.hits(hitY))
							.cache(cache(CACHE_3, true)
									.window(0, target.length()-2)
									.hits(hitX))
							.result(result(0)
									.map(NODE_2, hitX)
									.map(NODE_1, hitY))
				);
			}
		}

		@Nested
		class ForIqlGrouping {

		}

		@Nested
		class ForIqlElementDisjunction {

		}
	}
}
