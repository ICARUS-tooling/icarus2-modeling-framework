/**
 *
 */
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.test.TestUtils.mockDelegate;
import static de.ims.icarus2.test.util.Triple.triple;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.filedriver.io.UnlimitedBlockCache;
import de.ims.icarus2.filedriver.mapping.MappingImplSpanManyToOne.Builder;
import de.ims.icarus2.filedriver.mapping.StoredMappingTest.AbstractConfig;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReaderTest;
import de.ims.icarus2.model.api.driver.mapping.MappingWriterTest;
import de.ims.icarus2.model.api.driver.mapping.WritableMapping;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.api.MappingManifest.Relation;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.util.Triple;
import de.ims.icarus2.util.io.resource.IOResource;
import de.ims.icarus2.util.io.resource.VirtualIOResource;

/**
 * @author Markus Gärtner
 *
 */
class MappingImplSpanManyToOneTest {

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplSpanManyToOne#builder()}.
	 */
	@Test
	void testBuilder() {
		assertThat(MappingImplSpanManyToOne.builder()).isNotNull();
	}

	private MappingImplSpanOneToMany createInverseMapping() {
		MappingManifest manifest = mock(MappingManifest.class);
		when(manifest.getCoverage()).thenReturn(Optional.of(Coverage.PARTIAL));

		return MappingImplSpanOneToMany.builder()
				.blockCache(new UnlimitedBlockCache())
				.blockPower(4)
				.driver(mock(Driver.class))
				.sourceLayer(mock(ItemLayerManifestBase.class))
				.targetLayer(mock(ItemLayerManifestBase.class))
				.manifest(manifest)
				.resource(new VirtualIOResource())
				.valueType(IndexValueType.LONG)
				.cacheSize(100)
				.build();
	}

	private MappingImplSpanManyToOne createMapping() {
		MappingManifest manifest = mock(MappingManifest.class);
		when(manifest.getCoverage()).thenReturn(Optional.of(Coverage.PARTIAL));

		return MappingImplSpanManyToOne.builder()
				.blockCache(new UnlimitedBlockCache())
				.blockPower(4)
				.groupPower(4)
				.driver(mock(Driver.class))
				.sourceLayer(mock(ItemLayerManifestBase.class))
				.targetLayer(mock(ItemLayerManifestBase.class))
				.manifest(manifest)
				.resource(new VirtualIOResource())
				.valueType(IndexValueType.LONG)
				.cacheSize(100)
				.inverseMapping(createInverseMapping())
				.build();
	}

	class Internals {

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplSpanManyToOne#getBlockStorage()}.
		 */
		@Test
		void testGetBlockStorage() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplSpanManyToOne#getInverseMapping()}.
		 */
		@Test
		void testGetInverseMapping() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplSpanManyToOne#getEntriesPerBlock()}.
		 */
		@Test
		void testGetEntriesPerBlock() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplSpanManyToOne#getEntriesPerGroup()}.
		 */
		@Test
		void testGetEntriesPerGroup() {
			fail("Not yet implemented"); // TODO
		}

	}

	@Nested
	class ForBuilder implements StoredMappingBuilderTest<MappingImplSpanManyToOne, MappingImplSpanManyToOne.Builder> {

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return MappingImplSpanManyToOne.Builder.class;
		}

		/**
		 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
		 */
		@Override
		public Builder createTestInstance(TestSettings settings) {
			return settings.process(MappingImplSpanManyToOne.builder());
		}

		/**
		 * @see de.ims.icarus2.util.BuilderTest#invalidOps()
		 */
		@Override
		public List<Triple<String, Class<? extends Throwable>, Consumer<? super Builder>>> invalidOps() {
			return list(
					triple("zero cacheSize", IllegalArgumentException.class, b -> b.cacheSize(0)),
					triple("negative cacheSize ", IllegalArgumentException.class, b -> b.cacheSize(-12345)),

					triple("zero blockPower", IllegalArgumentException.class, b -> b.blockPower(0)),
					triple("negative blockPower", IllegalArgumentException.class, b -> b.blockPower(-12367))
			);
		}
	}

	//READER TESTS



	// WRITER TESTS



	// WRITER TESTS

	@Nested
	class ForReaderApi implements MappingReaderTest {

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return MappingImplSpanManyToOne.Reader.class;
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessorTest#createSource()
		 */
		@Override
		public Mapping createSource() {
			return createMapping();
		}
	}

	@Nested
	class ForWriterApi implements MappingWriterTest {

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return MappingImplSpanManyToOne.Writer.class;
		}

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessorTest#createSource()
		 */
		@Override
		public WritableMapping createSource() {
			return createMapping();
		}
	}

	static class ConfigImpl extends AbstractConfig<MappingImplSpanManyToOne> {

		public int blockPower;
		public int groupPower;
		public Supplier<Mapping> inverseMappingGen;

		@Override
		protected Relation relation() { return Relation.MANY_TO_ONE; }

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingTest.Config#create()
		 */
		@Override
		public MappingImplSpanManyToOne create() {
			return MappingImplSpanManyToOne.builder()
					.blockPower(blockPower)
					.groupPower(groupPower)
					.cacheSize(cacheSize)
					.driver(driver)
					.manifest(manifest)
					.sourceLayer(sourceLayer)
					.targetLayer(targetLayer)
					.valueType(valueType)
					// Dynamic "per invocation" fields
					.resource(mockDelegate(IOResource.class, resourceGen.get()))
					.blockCache(blockCacheGen.get())
					.inverseMapping(inverseMappingGen.get())
					.build();
		}

	}

}
