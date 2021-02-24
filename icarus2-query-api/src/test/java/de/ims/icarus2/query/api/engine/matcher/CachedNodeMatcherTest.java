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

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.util.lang.Primitives._boolean;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.standard.members.item.DefaultItem;
import de.ims.icarus2.query.api.exp.Assignable;
import de.ims.icarus2.query.api.exp.Expression;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.Mutable.MutableObject;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
class CachedNodeMatcherTest implements ApiGuardedTest<CachedNodeMatcher> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<?> getTestTargetClass() { return CachedNodeMatcher.class; }

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@SuppressWarnings("boxing")
	@Override
	public CachedNodeMatcher createTestInstance(TestSettings settings) {
		Assignable<? extends Item> element = mock(Assignable.class);
		Expression<?> constraint = mock(Expression.class);
		when(constraint.isBoolean()).thenReturn(Boolean.TRUE);

		return settings.process(new CachedNodeMatcher(1, element, constraint));
	}

	@Test
	void testUnknown() {
		CachedNodeMatcher matcher = create();
		for (int i = 0; i < matcher.cacheSize(); i++) {
			assertThat(matcher.hasEntry(i)).isFalse();
		}
	}

	@Provider
	private CachedNodeMatcher create(Item...items) {
		final MutableObject<?> current = new MutableObject<>();
		final Set<Item> lookup = new ReferenceOpenHashSet<>();
		Collections.addAll(lookup, items);

		Assignable<? extends Item> element = mock(Assignable.class);
		doAnswer(invoc -> {
			current.set(invoc.getArgument(0));
			return null;
		}).when(element).assign(any());

		Expression<?> constraint = mock(Expression.class);
		when(_boolean(constraint.isBoolean())).thenReturn(Boolean.TRUE);
		when(_boolean(constraint.computeAsBoolean())).thenAnswer(invoc -> {
			Item item = (Item) current.get();
			return _boolean(item!=null && lookup.contains(item));
		});

		return new CachedNodeMatcher(1, element, constraint);
	}

	@Test
	@RandomizedTest
	void testRememberFalse(RandomGenerator rng) {
		Item item = mockItem();
		CachedNodeMatcher matcher = create();
		int index = rng.nextInt(matcher.cacheSize());

		assertThat(matcher.matches(index, item)).isFalse();
		assertThat(matcher.hasEntry(index)).isTrue();
		assertThat(matcher.getCachedValue(index)).isFalse();
	}

	@Test
	@RandomizedTest
	void testRememberTrue(RandomGenerator rng) {
		Item item = mockItem();
		CachedNodeMatcher matcher = create(item);
		int index = rng.nextInt(matcher.cacheSize());

		assertThat(matcher.matches(index, item)).isTrue();
		assertThat(matcher.hasEntry(index)).isTrue();
		assertThat(matcher.getCachedValue(index)).isTrue();
	}

	@Test
	void testGrowCache() {
		CachedNodeMatcher matcher = create();
		int initialSize = matcher.cacheSize();

		matcher.matches(matcher.cacheSize()+1, mockItem());

		assertThat(matcher.cacheSize())
			.as("Cache did not grow")
			.isGreaterThan(initialSize);
	}

	@Test
	@RandomizedTest
	void testReset(RandomGenerator rng) {
		CachedNodeMatcher matcher = create();
		int index = rng.nextInt(matcher.cacheSize());
		Item item = mockItem();

		matcher.matches(index, item);
		assertThat(matcher.hasEntry(index)).isTrue();

		matcher.resetCache();
		assertThat(matcher.hasEntry(index)).isFalse();
	}

	@Test
	@RandomizedTest
	void testFull(RandomGenerator rng) {
		int size = 1000;

		final boolean[] data = rng.randomBooleans(size);

		Item[] items = IntStream.range(0, size)
				.mapToObj(DefaultItem::new)
				.toArray(Item[]::new);

		Item[] contained = Stream.of(items)
				.filter(item -> data[strictToInt(item.getId())])
				.toArray(Item[]::new);

		CachedNodeMatcher matcher = create(contained);

		// First pass -> fill everything
		for(int index = 0; index < size; index++) {
			assertThat(matcher.hasEntry(index))
				.as("Entry at index %d already occupied", _int(index))
				.isFalse();
			assertThat(matcher.matches(index, items[index]))
				.as("Matcher produced unexpected cache entry for index %d", _int(index))
				.isEqualTo(data[index]);
		}

		// Second path -> ensure consistency
		for(int index = 0; index < size; index++) {
			assertThat(matcher.hasEntry(index))
				.as("Entry at index %d still empty", _int(index))
				.isTrue();
			// Make sure correct value is stored
			assertThat(matcher.getCachedValue(index))
				.as("Entry at index %d corrupted", _int(index))
				.isEqualTo(data[index]);
			// Make sure it stays the same
			assertThat(matcher.matches(index, items[index]))
				.as("Inconsistent result at index %d", _int(index))
				.isEqualTo(data[index]);
		}
	}
}
