/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.test.DelegateTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.factories.DelegateTestFactory;

/**
 * @author Markus Gärtner
 *
 */
class SynchronizedIndexSetTest implements DelegateTest<SynchronizedIndexSet, IndexSet> {

	@Test
	void testConstructorNull() {
		assertNPE(() -> new SynchronizedIndexSet(null));
	}

	@Override
	public Class<?> getTestTargetClass() {
		return SynchronizedIndexSet.class;
	}

	@Override
	public SynchronizedIndexSet createTestInstance(TestSettings settings) {
		return settings.process(new SynchronizedIndexSet(mock(IndexSet.class, CALLS_REAL_METHODS)));
	}

	@Override
	public void configure(DelegateTestFactory<SynchronizedIndexSet, IndexSet> factory) {
		factory.sourceClass(IndexSet.class);
		factory.delegateGenerator(SynchronizedIndexSet::new);
		factory.methodFilter(m -> Modifier.isSynchronized(m.getModifiers())
				&& !"getFeatures".equals(m.getName()));
		factory.sourceProcessor(set -> doReturn(IndexSet.DEFAULT_FEATURES).when(set).getFeatures());
	}
}
