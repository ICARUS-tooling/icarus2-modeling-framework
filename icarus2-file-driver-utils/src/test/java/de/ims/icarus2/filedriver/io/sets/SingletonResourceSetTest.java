/**
 *
 */
package de.ims.icarus2.filedriver.io.sets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.util.io.resource.IOResource;

/**
 * @author Markus Gärtner
 *
 */
class SingletonResourceSetTest implements ResourceSetTest<SingletonResourceSet> {

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.sets.SingletonResourceSet#SingletonResourceSet(de.ims.icarus2.util.io.resource.IOResource)}.
	 */
	@Test
	void testSingletonResourceSet() {
		IOResource resource = mock(IOResource.class);
		SingletonResourceSet set = new SingletonResourceSet(resource);
		assertThat(set.getResourceCount()).isEqualTo(1);
		assertThat(set.getResourceAt(0)).isSameAs(resource);
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<?> getTestTargetClass() {
		return SingletonResourceSet.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public SingletonResourceSet createTestInstance(TestSettings settings) {
		return settings.process(createFilled());
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.sets.ResourceSetTest#createFilled()
	 */
	@Override
	public SingletonResourceSet createFilled() {
		return new SingletonResourceSet(mock(IOResource.class));
	}

}
