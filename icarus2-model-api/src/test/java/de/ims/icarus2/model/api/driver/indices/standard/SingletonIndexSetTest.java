/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.TestUtils.random;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.RandomAccessIndexSetTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class SingletonIndexSetTest implements RandomAccessIndexSetTest<SingletonIndexSet> {

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.SingletonIndexSet#SingletonIndexSet(long)}.
		 */
		@Test
		void testSingletonIndexSet() {
			assertNotNull(SingletonIndexSet.of(random(0, Long.MAX_VALUE)));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.SingletonIndexSet#SingletonIndexSet(long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {-1, Long.MIN_VALUE})
		void testSingletonIndexSetInvalidIndex(long index) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingletonIndexSet(index));
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSetTest#configurations()
	 */
	@Override
	public Stream<Config> configurations() {
		return Stream.of(IndexValueType.values())
				.map(type -> new Config()
						.valueType(type)
						.label(type.name())
						.sorted(true)
						.indices(type.maxValue()-1)
						.set(config -> new SingletonIndexSet(config.getIndices()[0]))
						.features(SingletonIndexSet.features));
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<?> getTestTargetClass() {
		return SingletonIndexSet.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public SingletonIndexSet createTestInstance(TestSettings settings) {
		return settings.process(new SingletonIndexSet(random(0, Long.MAX_VALUE)));
	}

}
