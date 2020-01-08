/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.model.api.driver.mapping;

import static de.ims.icarus2.test.TestUtils.assertMock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
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

	C basicConfiguration();

	default Stream<Coverage> coverages() {
		return Stream.of(Coverage.values());
	}

	default Stream<Relation> relations() {
		return Stream.of(Relation.values());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.Mapping#getDriver()}.
	 */
	@Test
	default void testGetDriver() {
		C config = basicConfiguration();
		assertThat(config.create().getDriver()).isSameAs(config.driver);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.Mapping#getSourceLayer()}.
	 */
	@Test
	default void testGetSourceLayer() {
		C config = basicConfiguration();
		assertThat(config.create().getSourceLayer()).isSameAs(config.sourceLayer);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.Mapping#getTargetLayer()}.
	 */
	@Test
	default void testGetTargetLayer() {
		C config = basicConfiguration();
		assertThat(config.create().getTargetLayer()).isSameAs(config.targetLayer);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.Mapping#getManifest()}.
	 */
	@Test
	default void testGetManifest() {
		C config = basicConfiguration();
		assertThat(config.create().getManifest()).isSameAs(config.manifest);
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
	@Test
	default void testClose() {
		C config = basicConfiguration();
		M mapping = config.create();
		mapping.close();
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
