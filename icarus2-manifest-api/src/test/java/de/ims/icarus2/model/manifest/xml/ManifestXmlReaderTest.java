/**
 *
 */
package de.ims.icarus2.model.manifest.xml;

import static de.ims.icarus2.test.TestUtils.filledArray;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader.Builder;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.guard.ApiGuard;
import de.ims.icarus2.test.util.Triple;
import de.ims.icarus2.util.BuilderTest;

/**
 * @author Markus Gärtner
 *
 */
class ManifestXmlReaderTest {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.xml.ManifestXmlReader#getDefaultSchema()}.
	 */
	@Test
	void testGetDefaultSchema() {
		assertThat(ManifestXmlReader.getDefaultSchema()).isNotNull();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.xml.ManifestXmlReader#builder()}.
	 */
	@Test
	void testBuilder() {
		assertThat(ManifestXmlReader.builder()).isNotNull();
	}

	@Nested
	class ForBuilder implements BuilderTest<ManifestXmlReader, ManifestXmlReader.Builder> {

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return Builder.class;
		}

		/**
		 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
		 */
		@Override
		public Builder createTestInstance(TestSettings settings) {
			return settings.process(ManifestXmlReader.builder());
		}

		/**
		 * @see de.ims.icarus2.test.ApiGuardedTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
		 */
		@Override
		public void configureApiGuard(ApiGuard<Builder> apiGuard) {
			BuilderTest.super.configureApiGuard(apiGuard);

			apiGuard.parameterResolver(ManifestLocation[].class, b -> new ManifestLocation[2]);
		}

		@Test
		void testUseImplementationDefaults() {
			Builder builder = create();
			builder.useImplementationDefaults();

			assertThat(builder.getDelegateFactory()).isNotNull();
			assertThat(builder.getNamespacePrefix()).isNotNull().isNotEmpty();
			assertThat(builder.getNamespaceUri()).isNotNull().isNotEmpty();
		}

		/**
		 * @see de.ims.icarus2.util.BuilderTest#invalidOps()
		 */
		@Override
		public List<Triple<String, Class<? extends Throwable>, Consumer<? super Builder>>> invalidOps() {
			return list(
					Triple.triple("empty sources", IllegalArgumentException.class,
							b -> b.source(new ManifestLocation[0]))
			);
		}

		@Test
		void testSource() {
			Builder builder = create();
			ManifestLocation[] sources = filledArray(10, ManifestLocation.class);

			for (int i = 0; i < sources.length; i++) {
				builder.source(sources[i]);
				assertThat(builder.getSources()).containsExactlyInAnyOrder(
						Arrays.copyOfRange(sources, 0, i+1));
			}
		}
	}
}
