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

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Cache;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Finish;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Node;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Proxy;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Repetition;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Scan;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.Single;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.State;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.StateMachineSetup;
import de.ims.icarus2.query.api.engine.matcher.SequencePattern.TreeInfo;
import de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.RepetitionUtils.ClosedBase;
import de.ims.icarus2.query.api.engine.matcher.SequencePatternTest.RepetitionUtils.OpenBase;
import de.ims.icarus2.query.api.engine.matcher.mark.Interval;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.test.util.Pair;
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

	/** Match given character exactly */
	private static CharPredicate eq(char sentinel) {
		return c -> c==sentinel;
	}

	/** Match anything but given character */
	private static CharPredicate neq(char sentinel) {
		return c -> c!=sentinel;
	}

	/** Match given character while ignoring case */
	private static CharPredicate ic(char sentinel) {
		return c -> c==Character.toLowerCase(sentinel) || c==Character.toUpperCase(sentinel);
	}

	/** Match no character -> always return false */
	private static CharPredicate none() {
		return c -> false;
	}

	private static Supplier<Matcher<Item>> sup(Matcher<Item> m) { return () -> m; }

	@SafeVarargs
	private static Supplier<Matcher<Item>>[] nodeDefs(Matcher<Item>...matchers) {
		return Stream.of(matchers).map(SequencePatternTest::sup).toArray(Supplier[]::new);
	}

	private static Item item(int index, char c) {
		Item item = mockItem();
		when(_long(item.getIndex())).thenReturn(_long(index));
		when(item.toString()).thenReturn(String.valueOf(c));
		return item;
	}

	private static Node seq(Node...nodes) {
		for (int i = 1; i < nodes.length; i++) {
			nodes[i-1].next = nodes[i];
		}
		nodes[0].study(new TreeInfo());
		return nodes[0];
	}

	private static final CharPredicate EQUALS_X = eq('X');
	private static final CharPredicate EQUALS_NOT_X = neq('X');
	private static final CharPredicate EQUALS_X_IC = ic('x');
	private static final CharPredicate EQUALS_Y = eq('Y');
	private static final int NODE_1 = 0;
	private static final int NODE_2 = 1;
	private static final int CACHE_1 = 0;
	private static final int CACHE_2 = 1;
	private static final int BUFFER_1 = 0;
	private static final int BUFFER_2 = 1;

	@Test
	void testBuilder() {
		assertThat(SequencePattern.builder()).isNotNull();
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
				sms.root = seq(new Single(0, NODE_1, CACHE_1), new Finish(UNSET_LONG));
				sms.nodeDefs = nodeDefs(matcher(0, EQUALS_X));
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
				assertResult(target, match(startPos, expectedResult, expectedResult ? 1 : 0)
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
						new Single(0, NODE_1, CACHE_1),
						new Single(1, NODE_2, CACHE_2),
						new Finish(UNSET_LONG));
				sms.nodeDefs = nodeDefs(matcher(0, EQUALS_X), matcher(1, EQUALS_Y));
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
				assertResult(target, match(startPos, expectedResult, expectedResult ? 1 : 0)
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
		class ForwardSingle implements NodeTest {

			@Override
			public StateMachineSetup setup() {
				StateMachineSetup sms = new StateMachineSetup();
				sms.nodes = new IqlNode[1];
				sms.cacheCount = 1;
				sms.root = seq(
						new Scan(0, UNSET_INT, true),
						new Single(1, NODE_1, CACHE_1),
						new Finish(UNSET_LONG));
				sms.nodeDefs = nodeDefs(matcher(0, EQUALS_X));
				return sms;
			}

			@DisplayName("scan of 1 match")
			@CsvSource({
				"-, 0",
				"--, 0",
				"--, 1",
				"-X-, 2",
			})
			@ParameterizedTest(name="{index}: X in [{0}], start at {1}")
			void testFail(String target, int startPos) {
				assertResult(target, match(startPos, false, 0)
						.cache(cache(CACHE_1, true))
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
				assertResult(target, match(startPos, true, 1)
						.cache(cache(CACHE_1, true).hits(hit))
						.node(node(NODE_1).last(hit))
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
			})
			@ParameterizedTest(name="{index}: X in [{0}], start at {1}, count={2}")
			void testMultiMatch(String target, int startPos, int matchCount, int last) {
				assertResult(target, match(startPos, true, matchCount)
						.cache(cache(CACHE_1, true).hits(target, EQUALS_X))
						.node(node(NODE_1).last(last))
				);
			}
		}

		@Nested
		class ForwardSingleCached implements NodeTest {

			@Override
			public StateMachineSetup setup() {
				StateMachineSetup sms = new StateMachineSetup();
				sms.nodes = new IqlNode[1];
				sms.cacheCount = 2;
				sms.root = seq(
						new Scan(0, CACHE_1, true),
						new Single(1, NODE_1, CACHE_2),
						new Finish(UNSET_LONG));
				sms.nodeDefs = nodeDefs(matcher(0, EQUALS_X));
				return sms;
			}

			@DisplayName("scan of 1 match")
			@CsvSource({
				"-, 0",
				"---, 0",
				"-X-, 2",
			})
			@ParameterizedTest(name="{index}: X in [{0}], start at {1}")
			void testFail(String target, int startPos) {
				assertResult(target, match(startPos, false, 0)
						.cache(cache(CACHE_1, true))
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
			void testSingleMatch(String target, int startPos, int last) {
				assertResult(target, match(startPos, true, 1)
						.cache(cache(CACHE_1, true).hits(target, EQUALS_X))
						.node(node(NODE_1).last(last))
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
			})
			@ParameterizedTest(name="{index}: X in [{0}], start at {1}")
			void testMultiMatch(String target, int startPos, int matchCount, int last) {
				assertResult(target, match(startPos, true, matchCount)
						.cache(cache(CACHE_1, true).hits(target, EQUALS_X))
						.node(node(NODE_1).last(last))
				);
			}
		}

		@Nested
		class BackwardSingle implements NodeTest {

			@Override
			public StateMachineSetup setup() {
				StateMachineSetup sms = new StateMachineSetup();
				sms.nodes = new IqlNode[1];
				sms.cacheCount = 1;
				sms.root = seq(
						new Scan(0, UNSET_INT, false),
						new Single(1, NODE_1, CACHE_1),
						new Finish(UNSET_LONG));
				sms.nodeDefs = nodeDefs(matcher(0, EQUALS_X));
				return sms;
			}

			@DisplayName("scan of 1 match")
			@CsvSource({
				"-, 0",
				"---, 0",
				"-X-, 2",
			})
			@ParameterizedTest(name="{index}: X in [{0}], start at {1}")
			void testFail(String target, int startPos) {
				assertResult(target, match(startPos, false, 0)
						.cache(cache(CACHE_1, true))
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
			void testSingleMatch(String target, int startPos, int last) {
				assertResult(target, match(startPos, true, 1)
						.cache(cache(CACHE_1, true).hits(target, EQUALS_X))
						.node(node(NODE_1).last(last))
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
			})
			@ParameterizedTest(name="{index}: X in [{0}], start at {1}, count={2}")
			void testMultiMatch(String target, int startPos, int matchCount,
					@ConvertWith(IntervalConverter.class) Interval window, int last) {
				assertResult(target, match(startPos, true, matchCount)
						.cache(cache(CACHE_1, true)
								.window(window)
								.hits(target, EQUALS_X))
						.node(node(NODE_1).last(last))
				);
			}
		}
	}

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
					@ConvertWith(IntervalConverter.class) Interval window) {
				assertResult(target, match(startPos, false, 0)
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
					@ConvertWith(IntervalConverter.class) Interval window, int last) {
				assertResult(target, match(startPos, true, 1)
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
					@ConvertWith(IntervalConverter.class) Interval window, int last) {
				assertResult(target, match(startPos, true, 1)
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
					@ConvertWith(IntervalConverter.class) Interval window) {
				assertResult(target, match(startPos, false, 0)
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
					@ConvertWith(IntervalConverter.class) Interval window, int last) {
				assertResult(target, match(startPos, true, 1)
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
					@ConvertWith(IntervalConverter.class) Interval window,
					@ConvertWith(IntervalConverter.class) Interval matched, int last) {
				assertResult(target, match(startPos, true, 1)
						.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
						.map(NODE_1, matched.asArray())
						.node(node(NODE_1).last(last))
				);
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
								new Repetition(0, new Single(1, NODE_1, CACHE_1),
										CMIN, CMAX, SequencePattern.GREEDY, BUFFER_1, BUFFER_2),
								new Finish(UNSET_LONG));
						sms.nodeDefs = nodeDefs(matcher(0, EQUALS_X));
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
								new Repetition(0, new Single(1, NODE_1, CACHE_1),
										CMIN, CMAX, SequencePattern.GREEDY, BUFFER_1, BUFFER_2),
								new Single(1, NODE_2, CACHE_2),
								new Finish(UNSET_LONG));
						sms.nodeDefs = nodeDefs(
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
							@ConvertWith(IntervalConverter.class) Interval visited1,
							@ConvertWith(IntervalConverter.class) Interval matched1,
							int last1,
							@ConvertWith(IntervalConverter.class) Interval visited2,
							@ConvertWith(IntervalConverter.class) Interval matched2,
							int last2) {

						assertResult(target, match(startPos, true, 1)
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
								new Repetition(0, new Single(1, NODE_1, CACHE_1),
										CMIN, CMAX, SequencePattern.POSSESSIVE, BUFFER_1, BUFFER_2),
								new Finish(UNSET_LONG));
						sms.nodeDefs = nodeDefs(matcher(0, EQUALS_X));
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
								new Repetition(0, new Single(1, NODE_1, CACHE_1),
										CMIN, CMAX, SequencePattern.POSSESSIVE, BUFFER_1, BUFFER_2),
								new Single(1, NODE_2, CACHE_2),
								new Finish(UNSET_LONG));
						sms.nodeDefs = nodeDefs(
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
							@ConvertWith(IntervalConverter.class) Interval visited1, int last,
							@ConvertWith(IntervalConverter.class) Interval visited2) {

						assertResult(target, match(startPos, false, 0)
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
							@ConvertWith(IntervalConverter.class) Interval visited1,
							@ConvertWith(IntervalConverter.class) Interval matched1,
							int last1,
							@ConvertWith(IntervalConverter.class) Interval visited2,
							@ConvertWith(IntervalConverter.class) Interval matched2,
							int last2) {

						assertResult(target, match(startPos, true, 1)
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
								new Repetition(0, new Single(1, NODE_1, CACHE_1),
										CMIN, CMAX, SequencePattern.RELUCTANT, BUFFER_1, BUFFER_2),
								new Finish(UNSET_LONG));
						sms.nodeDefs = nodeDefs(
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
							@ConvertWith(IntervalConverter.class) Interval window) {
						assertResult(target, match(startPos, false, 0)
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
							@ConvertWith(IntervalConverter.class) Interval window, int last) {
						assertResult(target, match(startPos, true, 1)
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
							@ConvertWith(IntervalConverter.class) Interval window, int last) {
						assertResult(target, match(startPos, true, 1)
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
								new Repetition(0, new Single(1, NODE_1, CACHE_1),
										CMIN, CMAX, SequencePattern.RELUCTANT, BUFFER_1, BUFFER_2),
								new Proxy(NODE_2), // we need this to motivate the reluctant expansion
								new Finish(UNSET_LONG));
						sms.nodeDefs = nodeDefs(
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
							@ConvertWith(IntervalConverter.class) Interval window, int last) {
						assertResult(target, match(startPos, true, 1)
								.cache(cache(CACHE_1, true).window(window).hits(target, EQUALS_X))
								.map(NODE_1, startPos, startPos+1)
								.node(node(NODE_1).last(last))
						);
					}

					/*
					 * We need the additional delimiter at the end of the XXX sequence
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
							@ConvertWith(IntervalConverter.class) Interval window, int last) {
						assertResult(target, match(startPos, true, 1)
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
								new Repetition(0, new Single(1, NODE_1, CACHE_1),
										CMIN, CMAX, SequencePattern.RELUCTANT, BUFFER_1, BUFFER_2),
								new Single(2, NODE_2, CACHE_2),
								new Finish(UNSET_LONG));
						sms.nodeDefs = nodeDefs(
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
							@ConvertWith(IntervalConverter.class) Interval visited1, int last,
							@ConvertWith(IntervalConverter.class) Interval visited2) {

						assertResult(target, match(startPos, false, 0)
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
							@ConvertWith(IntervalConverter.class) Interval visited1,
							@ConvertWith(IntervalConverter.class) Interval matched1,
							int last1,
							@ConvertWith(IntervalConverter.class) Interval visited2,
							@ConvertWith(IntervalConverter.class) Interval matched2,
							int last2) {

						assertResult(target, match(startPos, true, 1)
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
								new Repetition(0, new Single(1, NODE_1, CACHE_1),
										CMIN, CINF, SequencePattern.GREEDY, BUFFER_1, BUFFER_2),
								new Finish(UNSET_LONG));
						sms.nodeDefs = nodeDefs(matcher(0, EQUALS_X));
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
								new Repetition(0, new Single(1, NODE_1, CACHE_1),
										CMIN, CINF, SequencePattern.GREEDY, BUFFER_1, BUFFER_2),
								new Single(1, NODE_2, CACHE_2),
								new Finish(UNSET_LONG));
						sms.nodeDefs = nodeDefs(
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
							@ConvertWith(IntervalConverter.class) Interval visited1,
							@ConvertWith(IntervalConverter.class) Interval matched1,
							int last1,
							@ConvertWith(IntervalConverter.class) Interval visited2,
							@ConvertWith(IntervalConverter.class) Interval matched2,
							int last2) {

						assertResult(target, match(startPos, true, 1)
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
								new Repetition(0, new Single(1, NODE_1, CACHE_1),
										CMIN, CINF, SequencePattern.POSSESSIVE, BUFFER_1, BUFFER_2),
								new Finish(UNSET_LONG));
						sms.nodeDefs = nodeDefs(matcher(0, EQUALS_X));
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
								new Repetition(0, new Single(1, NODE_1, CACHE_1),
										CMIN, CINF, SequencePattern.POSSESSIVE, BUFFER_1, BUFFER_2),
								new Single(1, NODE_2, CACHE_2),
								new Finish(UNSET_LONG));
						sms.nodeDefs = nodeDefs(
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
							@ConvertWith(IntervalConverter.class) Interval visited1, int last,
							@ConvertWith(IntervalConverter.class) Interval visited2) {

						assertResult(target, match(startPos, false, 0)
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
								new Repetition(0, new Single(1, NODE_1, CACHE_1),
										CMIN, CINF, SequencePattern.POSSESSIVE, BUFFER_1, BUFFER_2),
								new Single(1, NODE_2, CACHE_2),
								new Finish(UNSET_LONG));
						sms.nodeDefs = nodeDefs(
								matcher(0, EQUALS_X),
								matcher(1, EQUALS_Y));
						return sms;
					}

					@CsvSource({
						"XXXXXXXXYY, 0, 0-7, 0-7, 7, 8, 8, 8",
					})
					@ParameterizedTest(name="{index}: X'{2,}'Y in [{0}], start at {1}")
					void testExpansion(String target, int startPos,
							@ConvertWith(IntervalConverter.class) Interval visited1,
							@ConvertWith(IntervalConverter.class) Interval matched1,
							int last1,
							@ConvertWith(IntervalConverter.class) Interval visited2,
							@ConvertWith(IntervalConverter.class) Interval matched2,
							int last2) {

						assertResult(target, match(startPos, true, 1)
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

			//TODO add tests for reluctant repetition
		}

	}

	interface NodeTest {
		StateMachineSetup setup();

//		State state(String s) {
//			assertThat(s).isNotEmpty();
//
//			StateMachineSetup setup = setup();
//
//			State state = new State(setup);
//			state.size = s.length();
//			state.elements = IntStream.range(0, s.length())
//					.mapToObj(i -> item(i, s.charAt(i)))
//					.toArray(Item[]::new);
//
//			return state;
//		}

		default void assertResult(String s, MatchConfig config) {
			assertThat(s).isNotEmpty();

			StateMachineSetup setup = setup();

			State state = new State(setup);
			state.size = s.length();
			state.to = s.length()-1;
			state.elements = IntStream.range(0, s.length())
					.mapToObj(i -> item(i, s.charAt(i)))
					.toArray(Item[]::new);

			config.assertResult(setup.root, state);
		}
	}

	static MatchConfig match(int startPos, boolean expectedResult, int expectedCount) {
		return new MatchConfig(startPos, expectedResult, expectedCount);
	}

	/** Encapsulates the info for expected matches and hits inside a single target sequence */
	static class MatchConfig {
		/** Index in target sequence to start the test search at */
		private final int startPos;
		/** Expected success state */
		private final boolean expectedResult;
		/** Total number of matches we expect */
		private final int expectedCount;
		/** Individual node info regarding last hits etc... */
		private final List<NodeConfig> nodes = new ArrayList<>();
		private final List<CacheConfig> caches = new ArrayList<>();
		/**  */
		private final List<Pair<Integer, Integer>> mapping = new ArrayList<>();

		MatchConfig(int startPos, boolean expectedResult, int expectedCount) {
			assertThat(startPos).as("Negative position").isGreaterThanOrEqualTo(0);
			assertThat(expectedCount).as("Negative expected count").isGreaterThanOrEqualTo(0);
			this.startPos = startPos;
			this.expectedResult = expectedResult;
			this.expectedCount = expectedCount;
		}

		MatchConfig node(NodeConfig node) { nodes.add(requireNonNull(node)); return this; }

		MatchConfig cache(CacheConfig cache) { caches.add(requireNonNull(cache)); return this; }

		MatchConfig map(int nodeId, int...indices) {
			IntStream.of(indices).mapToObj(pos -> Pair.pair(nodeId, pos)).forEach(mapping::add);
			return this;
		}

		MatchConfig map(boolean condition, int nodeId, int...indices) { if(condition) map(nodeId, indices); return this;}

		void assertResult(Node root, State state) {
			// Verify correct result
			assertThat(root.match(state, startPos))
				.as("Result for start position %d", _int(startPos))
				.isEqualTo(expectedResult);

			assertThat(state.reported)
				.as("Total number of matches")
				.isEqualTo(expectedCount);

			for (CacheConfig cache : caches) {
				cache.assertResult(startPos, state);
			}

			assertMapping(state);

			for (NodeConfig node : nodes) {
				node.assertResult(state);
			}
		}

		private void assertMapping(State state) {
			int size = mapping.size();
			assertThat(state.entry)
				.as("Incorrect number of mappings")
				.isEqualTo(size);

			for (int i = 0; i < size; i++) {
				Pair<Integer, Integer> m = mapping.get(i);
				assertThat(state.m_node[i])
					.as("Node id mismatch in mapping at index %d", _int(i))
					.isEqualTo(m.first.intValue());
				assertThat(state.m_pos[i])
					.as("Position mismatch in mapping at index %d", _int(i))
					.isEqualTo(m.second.intValue());
			}
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
}
