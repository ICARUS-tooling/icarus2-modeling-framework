/**
 *
 */
package de.ims.icarus2.model.api.driver.id;

import static de.ims.icarus2.test.TestUtils.settings;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface IdManagerTest<M extends IdManager> extends GenericTest<M> {

	@Provider
	M createForManifest(ItemLayerManifestBase<?> manifest, TestSettings settings);

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default M createTestInstance(TestSettings settings) {
		ItemLayerManifestBase<?> manifest = mock(ItemLayerManifestBase.class);
		return createForManifest(manifest, settings);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.id.IdManager#getLayerManifest()}.
	 */
	@Test
	default void testGetLayerManifest() {
		assertNotNull(create().getLayerManifest());

		ItemLayerManifestBase<?> manifest = mock(ItemLayerManifestBase.class);

		M instance = createForManifest(manifest, settings());

		assertSame(manifest, instance.getLayerManifest());
	}

	public static class MappingConsistencyTester {

		private final int size;
		private final long[] indices, values;

		private int pos;

		public MappingConsistencyTester(int size) {
			checkArgument(size>0);

			this.size = size;

			indices = new long[size];
			values = new long[size];

			pos = 0;
		}

		private void checkCapacity(int slots) {
			assumeTrue(size-slots>=pos);
		}

		public MappingConsistencyTester map(long index, long value) {
			checkCapacity(1);
			indices[pos] = index;
			values[pos] = value;
			pos++;
			return this;
		}

		/**
		 * Shuffles the order of all previously added mappings
		 * @return
		 */
		public MappingConsistencyTester shuffle() {


			return this;
		}

		//TODO add methods for filling the arrays and then test the entire thing

		public void test() {
			//TODO
		}
	}
}
