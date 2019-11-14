/**
 *
 */
package de.ims.icarus2.model.api.driver.mapping;

import static de.ims.icarus2.test.TestUtils.assertMock;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.api.MappingManifest.Relation;
import de.ims.icarus2.test.func.ThrowingBiConsumer;
import de.ims.icarus2.test.util.TestConfig;

/**
 * @author Markus Gärtner
 *
 */
public interface MappingTest<M extends Mapping, C extends MappingTest.Config<M>> {

	Stream<C> configurations();

	default Stream<Coverage> coverages() {
		return Stream.of(Coverage.values());
	}

	default Stream<Relation> relations() {
		return Stream.of(Relation.values());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.Mapping#getDriver()}.
	 */
	@TestFactory
	default Stream<DynamicNode> testGetDriver() {
		return basicTests((config, instance) -> assertSame(config.driver, instance.getDriver()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.Mapping#getSourceLayer()}.
	 */
	@TestFactory
	default Stream<DynamicNode> testGetSourceLayer() {
		return basicTests((config, instance) -> assertSame(config.sourceLayer, instance.getSourceLayer()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.Mapping#getTargetLayer()}.
	 */
	@TestFactory
	default Stream<DynamicNode> testGetTargetLayer() {
		return basicTests((config, instance) -> assertSame(config.targetLayer, instance.getTargetLayer()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.Mapping#getManifest()}.
	 */
	@TestFactory
	default Stream<DynamicNode> testGetManifest() {
		return basicTests((config, instance) -> assertSame(config.manifest, instance.getManifest()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.Mapping#newReader()}.
	 */
	@TestFactory
	default Stream<DynamicNode> testNewReader() {
		return basicTests((config, instance) -> {
			MappingReader reader = instance.newReader();
			assertNotNull(reader);
			assertSame(instance, reader.getSource());
		});
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.Mapping#close()}.
	 */
	@SuppressWarnings("resource")
	@TestFactory
	default Stream<DynamicNode> testClose() {
		return configurations().map(config -> dynamicTest(config.label, () -> {
			M mapping = config.create();
			mapping.close();
		}));
	}

	default Stream<DynamicNode> basicTests(ThrowingBiConsumer<C, M> task) {
		return configurations().map(config -> dynamicTest(config.label, () -> {
			try(M mapping = config.create()) {
				task.accept(config, mapping);
			}
		}));
	}

	default Stream<DynamicNode> readerTests(ThrowingBiConsumer<C, MappingReader> task) {
		return configurations().map(config -> dynamicTest(config.label, () -> {
			try(M mapping = config.create();
					MappingReader reader = mapping.newReader()) {
				task.accept(config, reader);
			}
		}));
	}

	default Stream<DynamicNode> batchTests(ThrowingBiConsumer<C, M> task) { //TODO
		return configurations().map(config -> dynamicTest(config.label, () -> {
			try(M mapping = config.create()) {
				task.accept(config, mapping);
			}
		}));
	}

	public static abstract class Config<M extends Mapping> implements TestConfig {
		public String label;
		public MappingManifest manifest;
		public Driver driver;
		public ItemLayerManifestBase<?> sourceLayer;
		public ItemLayerManifestBase<?> targetLayer;
		public IndexValueType valueType;

		public abstract M create();

		protected abstract Relation relation();

		public Config<M> prepareManifest(Coverage coverage) {
			assertMock(manifest);
			when(manifest.getCoverage()).thenReturn(Optional.of(coverage));
			when(manifest.getRelation()).thenReturn(Optional.of(relation()));
			return this;
		}

		@Override
		public void close() {
			label = null;
			manifest = null;
			driver = null;
			sourceLayer = targetLayer = null;
			valueType = null;
		}
	}
}
