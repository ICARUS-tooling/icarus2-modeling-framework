/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.standard;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.RandomAccessIndexSetTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class VirtualIndexSetTest implements RandomAccessIndexSetTest<VirtualIndexSet> {

	@Override
	public Stream<Config> configurations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getTestTargetClass() {
		return VirtualIndexSet.class;
	}

	@Override
	public VirtualIndexSet createTestInstance(TestSettings settings) {
		return settings.process(new VirtualIndexSet(0, 10,
				IndexValueType.INTEGER, (x, i) -> i, true));
	}

	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.VirtualIndexSet#VirtualIndexSet(long, int, de.ims.icarus2.model.api.driver.indices.IndexValueType, java.util.function.LongBinaryOperator, boolean)}.
		 */
		@Test
		void testVirtualIndexSetLongIntIndexValueTypeLongBinaryOperatorBoolean() {
			fail("Not yet implemented"); // TODO
		}

	}

}
