/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.model.api.ModelTestUtils.set;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.assertIAE;
import static de.ims.icarus2.test.TestUtils.random;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.RandomAccessIndexSetTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class DelegatingSpanIndexSetTest implements RandomAccessIndexSetTest<DelegatingSpanIndexSet> {

	private static Function<Config, IndexSet> constructor = config -> {
		long[] indices = config.getIndices();
		int size = indices.length;

		IndexValueType type = config.getValueType();
		Object array = type.newArray(size*2);
		type.copyFrom(indices, 0, array, size/2, size);

		IndexSet source;
		if(config.isSorted()) {
			source = new ArrayIndexSet(type, array, 0, size*2 - 1, true);
		} else {
			source = new ArrayIndexSet(type, array);
		}

		return new DelegatingSpanIndexSet(source, size/2, size/2 + size-1);
	};

	private static int randomSize() {
		return random(10, 100);
	}

	@Override
	public Stream<Config> configurations() {
		Config base = new Config()
				.features(IndexSet.DEFAULT_FEATURES);

		return Stream.of(IndexValueType.values())
				.map(type -> base.clone().valueType(type))
				.flatMap(config -> Stream.of(
						// Sorted version
						config.clone()
							.label(config.getValueType()+" sorted")
							.sortedIndices(randomSize())
							.sorted(true)
							.set(constructor),
						// Random version
						config.clone()
							.label(config.getValueType()+" random")
							.randomIndices(randomSize())
							.set(constructor)
						));
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<?> getTestTargetClass() {
		return DelegatingSpanIndexSet.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public DelegatingSpanIndexSet createTestInstance(TestSettings settings) {
		return settings.process(new DelegatingSpanIndexSet(set(0, 1, 2)));
	}

	@Nested
	class Constructors {

		int size;
		IndexSet source;

		@SuppressWarnings("boxing")
		@BeforeEach
		void setUp() {
			size = random(20, Integer.MAX_VALUE);
			source = mock(IndexSet.class);
			when(source.size()).thenReturn(size);
		}

		@AfterEach
		void tearDown() {
			source = null;
		}

		@Test
		void noArgs() {
			assertNotNull(new DelegatingSpanIndexSet(source));
		}

		@Test
		void spanFull() {
			DelegatingSpanIndexSet set = new DelegatingSpanIndexSet(source, 0, size-1);
			assertEquals(size, set.size());
		}

		@RepeatedTest(RUNS)
		void spanPartial() {
			int from = random(0, size);
			int to = random(from, size);

			DelegatingSpanIndexSet set = new DelegatingSpanIndexSet(source, from, to);
			assertEquals(from, set.getBeginIndex());
			assertEquals(to, set.getEndIndex());
			assertEquals(to-from+1, set.size());
		}

		@Test
		void invalidBeginIndex() {
			assertIAE(() -> new DelegatingSpanIndexSet(source, -1, size-1));
		}

		@ParameterizedTest
		@ValueSource(ints = {0, 1})
		void invalidEndIndex(int index) {
			assertIAE(() -> new DelegatingSpanIndexSet(source, 0, size+index));
		}
	}
}
