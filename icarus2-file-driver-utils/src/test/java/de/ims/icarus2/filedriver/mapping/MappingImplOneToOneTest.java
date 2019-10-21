/**
 *
 */
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.test.util.Pair.pair;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.filedriver.io.RUBlockCache;
import de.ims.icarus2.filedriver.io.UnlimitedBlockCache;
import de.ims.icarus2.filedriver.mapping.AbstractStoredMappingTest.AbstractConfig;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.mapping.WritableMappingTest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.util.io.resource.VirtualIOResource;

/**
 * @author Markus Gärtner
 *
 */
class MappingImplOneToOneTest implements WritableMappingTest<MappingImplOneToOne> {

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplOneToOne#builder()}.
	 */
	@Test
	void testBuilder() {
		assertNotNull(MappingImplOneToOne.builder());
	}

	@Override
	public Stream<Config<MappingImplOneToOne>> configurations() {
		return
			Stream.of(IndexValueType.values()).flatMap(valueType ->
			IntStream.of(3, 6, 10).boxed().flatMap(blockPower ->
			Stream.of(pair("unlimited", new UnlimitedBlockCache()),
					pair("LRU", RUBlockCache.newLeastRecentlyUsedCache())).flatMap(pCache ->
			IntStream.of(BlockCache.MIN_CAPACITY, 1024).boxed().map(cacheSize -> {
				ConfigImpl config = new ConfigImpl();
				config.label = String.format("type=%s blockPower=%d cache=%s cacheSize=%d",
						valueType, blockPower, pCache.first, cacheSize);

				config.blockCache = pCache.second;
				config.blockPower = blockPower.intValue();
				config.cacheSize = cacheSize.intValue();
				config.valueType = valueType;
				config.resource = new VirtualIOResource();

				config.driver = mock(Driver.class);
				config.sourceLayer = mock(ItemLayerManifestBase.class);
				config.targetLayer = mock(ItemLayerManifestBase.class);
				config.manifest = mock(MappingManifest.class);

				return config;
			}))));
	}

	@Nested
	class ForBuilder {

		//TODO verify builder methods and check below 2 methods for consistency

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplOneToOne#getBlockPower()}.
		 */
		@Test
		void testGetBlockPower() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplOneToOne#getEntriesPerBlock()}.
		 */
		@Test
		void testGetEntriesPerBlock() {
			fail("Not yet implemented"); // TODO
		}

	}

	static class ConfigImpl extends AbstractConfig<MappingImplOneToOne> {

		public int blockPower;
		public IndexValueType valueType;

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingTest.Config#create()
		 */
		@Override
		public MappingImplOneToOne create() {
			return MappingImplOneToOne.builder()
					.blockCache(blockCache)
					.blockPower(blockPower)
					.cacheSize(cacheSize)
					.driver(driver)
					.manifest(manifest)
					.resource(resource)
					.sourceLayer(sourceLayer)
					.targetLayer(targetLayer)
					.valueType(valueType)
					.build();
		}

	}
}
