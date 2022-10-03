/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine;

import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.stubIndex;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.lang.Primitives._long;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.LongFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.query.api.engine.LaneBridge.Uncached;
import de.ims.icarus2.query.api.engine.matcher.Matcher;
import de.ims.icarus2.query.api.engine.result.Match;
import de.ims.icarus2.query.api.engine.result.MatchCollector;
import de.ims.icarus2.test.util.Pair;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
class LaneBridgeTest {

	private static final Match[] NO_MATCH = {};

	@Nested
	class ForCached {

	}

	@Nested
	class ForUncached {

		@Test
		public void testSingle() throws Exception {

			LaneData lane = new LaneData(0);
			lane.add(lane.maker(0).match(1).match(2).make());
			lane.add(lane.maker(1).match(4).make());
			lane.addNoMatch(2);
			lane.add(lane.maker(3).match(5).match(2).make());

			Stack<Pair<Long, Container>> pending = lane.expectedCalls(lane.indicesWithMatches());
//			System.out.println(pending);

			Uncached bridge = Uncached.builder()
					.bufferSize(10)
					.threadVerifier(ThreadVerifier.forCurrentThread("test"))
					.itemLookup(lane.createItemLookup())
					.laneMapper(lane.identityMapper())
					.matcherGen(lane.createMatcherGen())
					.next(expectAll(pending))
					.build();

			boolean[] matched = lane.matchAll(bridge);
			assertThat(matched).containsExactly(true, true, false, true);
		}

		@Test
		public void testDualLane() throws Exception {

			LaneData lane1 = new LaneData(0);
			lane1.add(lane1.maker(0).match(1).match(2).make());
			lane1.add(lane1.maker(1).match(4).make());
			lane1.addNoMatch(2);

			LaneData lane2 = new LaneData(0);
			// Mapped from 0 on lane1
			lane2.add(lane2.maker(0).match(0).match(1).make());
			lane2.addNoMatch(1);
			// Mapped from 1 on lane1
			lane2.add(lane2.maker(2).match(1).match(2).make());
			lane2.addNoMatch(3);

			LaneMapper laneMapper = LaneMapper.fixedBuilder()
					.map(0, 0, 1)
					.map(1, 2, 3)
					.build();

			LongFunction<Container> items2 = lane2.createItemLookup();
			Stack<Pair<Long, Container>> pending = new ObjectArrayList<>();
			pending.push(Pair.pair(_long(2), items2.apply(2)));
			pending.push(Pair.pair(_long(2), items2.apply(2)));
			pending.push(Pair.pair(_long(0), items2.apply(0)));
			pending.push(Pair.pair(_long(0), items2.apply(0)));
			pending.push(Pair.pair(_long(0), items2.apply(0)));
			pending.push(Pair.pair(_long(0), items2.apply(0)));

			ThreadVerifier threadVerifier = ThreadVerifier.forCurrentThread("test");

			Uncached bridge2 = Uncached.builder()
					.bufferSize(10)
					.threadVerifier(threadVerifier)
					.itemLookup(lane2.createItemLookup())
					.laneMapper(lane2.identityMapper())
					.matcherGen(lane2.createMatcherGen())
					.next(expectAll(pending))
					.build();

			Uncached bridge1 = Uncached.builder()
					.bufferSize(10)
					.threadVerifier(threadVerifier)
					.itemLookup(lane2.createItemLookup())
					.laneMapper(laneMapper)
					.matcherGen(lane1.createMatcherGen())
					.next(bridge2)
					.build();

			boolean[] matched = lane1.matchAll(bridge1);
			assertThat(matched).containsExactly(true, true, false);
		}
	}

	private static Matcher<Container> expectAll(Stack<Pair<Long, Container>> pending) {
		return new Matcher<Container>() {

			@Override
			public boolean matches(long index, Container target) {

				Pair<Long, Container> expected = pending.pop();
				assertThat(index).as("unexpected match index").isEqualTo(expected.first);
				assertThat(target).as("unexpected target container").isSameAs(expected.second);

				return true;
			}

			@Override
			public int id() { return UNSET_INT; }
		};
	}

	private static class LaneData {
		final List<Container> targets = new ObjectArrayList<>();
		final List<Match[]> matches = new ObjectArrayList<>();
		final int id;

		LaneData(int id) {
			this.id = id;
		}

		MatchMaker maker(long index) { return new MatchMaker(id, index); }

		boolean[] matchAll(Matcher<Container> matcher) {
			boolean[] result = new boolean[targets.size()];
			for (int i = 0; i < targets.size(); i++) {
				result[i] = matcher.matches(i, targets.get(i));
			}
			return result;
		}

		int[] indicesWithMatches() {
			return IntStream.range(0, targets.size())
					.filter(i -> matches.get(i).length > 0)
					.toArray();
		}

		LongFunction<Container> createItemLookup() {
			return index -> targets.get(strictToInt(index));
		}

		BiFunction<ThreadVerifier, MatchCollector, Matcher<Container>> createMatcherGen() {
			return (threadVerifier, collector) -> new Matcher<Container>() {

				@Override
				public boolean matches(long index, Container target) {
					int idx = strictToInt(index);
					assertThat(target).as("foreign container").isSameAs(targets.get(idx));

					Match[] matches = LaneData.this.matches.get(idx);
					if(matches!=null && matches.length>0) {
						for (Match m : matches) {
							if(!collector.collect(m)) {
								break;
							}
						}

						return true;
					}

					return false;
				}

				@Override
				public int id() { return id; }
			};
		}

		void checkSync() {
			checkState("lists for targets and matches out of sync", targets.size() == matches.size());
		}

		LaneData add(Container target, Match...matches) {
			checkSync();

			targets.add(target);
			this.matches.add(matches);
			return this;
		}

		LaneData add(Match...matches) {
			long[] indices = Stream.of(matches)
					.mapToLong(Match::getIndex)
					.distinct()
					.toArray();
			assertThat(indices).hasSize(1);
			Container target = stubIndex(mockContainer(), indices[0]);

			return add(target, matches);
		}

		LaneData addNoMatch(long index) {
			return add(stubIndex(mockContainer(), index), NO_MATCH);
		}

		LaneMapper identityMapper() {
			LaneMapper.FixedBuilder builder = LaneMapper.fixedBuilder();
			for (int i = 0; i < targets.size(); i++) {
				builder.map(i, i);
			}
			return builder.build();
		}

		Stack<Pair<Long, Container>> expectedCalls(int...indices) {
			Stack<Pair<Long, Container>> pending = new ObjectArrayList<>();
			for(int i=indices.length-1; i>=0; i--) {
				int index = indices[i];
				for (int j = 0; j < matches.get(index).length; j++) {
					pending.push(Pair.pair(_long(index), targets.get(index)));
				}
			}
			return pending;
		}
	}
}
