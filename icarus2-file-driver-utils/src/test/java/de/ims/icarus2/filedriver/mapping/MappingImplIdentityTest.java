/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.model.api.ModelTestUtils.assertIndicesEqualsExact;
import static de.ims.icarus2.model.api.ModelTestUtils.matcher;
import static de.ims.icarus2.model.api.ModelTestUtils.set;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.firstIndex;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.lastIndex;
import static de.ims.icarus2.model.api.driver.indices.IndexUtils.span;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.filedriver.mapping.MappingImplIdentity.Builder;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReaderTest;
import de.ims.icarus2.model.api.driver.mapping.MappingTest;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.api.MappingManifest.Relation;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.BuilderTest;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * @author Markus Gärtner
 *
 */
class MappingImplIdentityTest implements MappingTest<MappingImplIdentity, MappingImplIdentityTest.ConfigImpl> {

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.mapping.MappingImplIdentity#builder()}.
	 */
	@Test
	void testBuilder() {
		assertThat(MappingImplIdentity.builder()).isNotNull();
	}

	@Override
	public Stream<ConfigImpl> configurations() {
		return Stream.of(IndexValueType.values()).map(valueType -> config(valueType));
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mapping.MappingTest#basicConfiguration()
	 */
	@Override
	public ConfigImpl basicConfiguration() {
		return config(IndexValueType.INTEGER);
	}

	private static ConfigImpl config(IndexValueType valueType) {
		ConfigImpl config = new ConfigImpl();

		config.label = String.format("valueType=%s", valueType);

		config.valueType = valueType;

		config.driver = mock(Driver.class);
		config.sourceLayer = mock(ItemLayerManifestBase.class);
		config.targetLayer = mock(ItemLayerManifestBase.class);
		config.manifest = mock(MappingManifest.class);

		config.prepareManifest(Coverage.PARTIAL);

		return config;
	}

	@SuppressWarnings("boxing")
	@RandomizedTest
	@TestFactory
	Stream<DynamicNode> testSingleValueLookups(RandomGenerator rng) {
		return readerTests((config, reader) -> {
			RequestSettings settings = RequestSettings.none();

			LongStream fixed = LongStream.of(0, 1, 100, 999_999);
			LongStream random = rng.longs(10, 0, config.valueType.maxValue());

			for(long value : LongStream.concat(fixed, random).toArray()) {
				assertThat(reader.getIndicesCount(value, settings))
					.as("Single map size for %d", value).isEqualTo(1);

				IndexSet[] indices = reader.lookup(value, settings);
				// Single lookup
				assertThat(indices).hasSize(1)
					.as("Single lookup for %d", value).allMatch(matcher(value));

				// Boundaries
				assertThat(reader.getBeginIndex(value, settings))
					.as("Single lookup (begin) for %d", value).isEqualTo(value);
				assertThat(reader.getEndIndex(value, settings))
					.as("Single lookup (end) for %d", value).isEqualTo(value);

				// Collector
				LongList list1 = new LongArrayList();
				assertThat(reader.lookup(value, list1::add, settings))
					.as("Single lookup (collector) for %d", value).isTrue();
				assertThat(list1.toLongArray()).containsExactly(value);

				// Reverse lookup
				assertThat(reader.find(value, value, value, settings))
					.as("Single reverse lookup for %d", value).isEqualTo(value);
			}
		});
	}

	@RandomizedTest
	@TestFactory
	Stream<DynamicNode> testBatchLookups(RandomGenerator rng) {
		return readerTests((config, reader) -> {
			RequestSettings settings = RequestSettings.none();

			IndexSet set1 = set(0, 1, 2, 3, 4);
			IndexSet set2 = set(10, 100, 999, 999_999_999);
			IndexSet set3 = span(1000, 1300);

			IndexSet[] indices = {set1, set2, set3};

			// Batch lookup
			assertIndicesEqualsExact(indices, reader.lookup(indices, settings));

			// Batch begin/end
			assertThat(reader.getBeginIndex(indices, settings)).isEqualTo(firstIndex(indices));
			assertThat(reader.getEndIndex(indices, settings)).isEqualTo(lastIndex(indices));

			// Batch collector
			LongList list1 = new LongArrayList();
			reader.lookup(indices, list1::add, settings);
			assertIndicesEqualsExact(indices, list1.iterator());

			// Batch reverse lookup
			assertIndicesEqualsExact(indices, reader.find(0, Long.MAX_VALUE, indices, settings));

			// Batch reverse collector
			LongList list2 = new LongArrayList();
			reader.find(0, Long.MAX_VALUE, indices, list2::add, settings);
			assertIndicesEqualsExact(indices, list2.iterator());
		});
	}

	private MappingImplIdentity createMapping() {
		MappingManifest manifest = mock(MappingManifest.class);
		when(manifest.getCoverage()).thenReturn(Optional.of(Coverage.PARTIAL));
		return MappingImplIdentity.builder()
				.driver(mock(Driver.class))
				.sourceLayer(mock(ItemLayerManifestBase.class))
				.targetLayer(mock(ItemLayerManifestBase.class))
				.manifest(manifest)
				.valueType(IndexValueType.LONG)
				.build();
	}

	@Nested
	class ForReaderApi implements MappingReaderTest {

		/**
		 * @see de.ims.icarus2.model.api.io.SynchronizedAccessorTest#createSource()
		 */
		@Override
		public Mapping createSource() {
			return createMapping();
		}

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return MappingImplIdentity.Reader.class;
		}

	}

	@Nested
	class ForBuilder implements BuilderTest<MappingImplIdentity, MappingImplIdentity.Builder> {

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
			return settings.process(MappingImplIdentity.builder());
		}
	}

	static class ConfigImpl extends MappingTest.Config<MappingImplIdentity> {

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingTest.Config#create()
		 */
		@Override
		public MappingImplIdentity create() {
			return MappingImplIdentity.builder()
					.driver(driver)
					.manifest(manifest)
					.sourceLayer(sourceLayer)
					.targetLayer(targetLayer)
					.valueType(valueType)
					.build();
		}

		/**
		 * @see de.ims.icarus2.model.api.driver.mapping.MappingTest.Config#relation()
		 */
		@Override
		protected Relation relation() {
			return Relation.ONE_TO_ONE;
		}

	}
}
