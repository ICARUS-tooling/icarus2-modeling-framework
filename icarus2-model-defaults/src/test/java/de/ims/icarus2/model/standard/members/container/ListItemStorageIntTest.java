/**
 *
 */
package de.ims.icarus2.model.standard.members.container;

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class ListItemStorageIntTest implements ItemStorageTest<ListItemStorageInt> {

	@Nested
	class Constructors {

		@Test
		void noArgs() {
			new ListItemStorageInt();
		}

		@Test
		void withCapacity() {
			new ListItemStorageInt(100);
		}

		@Test
		void withDefaultCapacity() {
			new ListItemStorageInt(UNSET_INT);
		}

		@Test
		void invalidCapacity() {
			IcarusRuntimeException exception = assertThrows(IcarusRuntimeException.class,
					() -> new ListItemStorageInt(-10));
			assertEquals(GlobalErrorCode.INVALID_INPUT, exception.getErrorCode());
		}
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends ListItemStorageInt> getTestTargetClass() {
		return ListItemStorageInt.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public ListItemStorageInt createTestInstance(TestSettings settings) {
		return settings.process(new ListItemStorageInt());
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorageTest#getExpectedContainerType()
	 */
	@Override
	public ContainerType getExpectedContainerType() {
		return ContainerType.LIST;
	}

	//TODO add tests for actual storage logic
}
