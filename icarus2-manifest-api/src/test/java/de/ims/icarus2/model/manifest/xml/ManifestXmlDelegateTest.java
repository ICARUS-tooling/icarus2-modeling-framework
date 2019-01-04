/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.model.manifest.xml;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestLocation;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestRegistry;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.TestUtils.ILLEGAL_STATE_CHECK;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertCollectionNotEmpty;
import static de.ims.icarus2.test.TestUtils.assertRestrictedGetter;
import static de.ims.icarus2.test.TestUtils.assertRestrictedSetter;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.ManifestGenerator;
import de.ims.icarus2.model.manifest.ManifestGenerator.Config;
import de.ims.icarus2.model.manifest.ManifestGenerator.IncrementalBuild;
import de.ims.icarus2.model.manifest.api.ManifestFactory;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.model.manifest.standard.DefaultManifestFactory;
import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;

/**
 * Test facility for {@link ManifestXmlDelegate} implementations.
 *
 *
 * @author Markus Gärtner
 *
 */
public interface ManifestXmlDelegateTest<M extends TypedManifest, D extends ManifestXmlDelegate<? super M>>
	extends GenericTest<D> {

	ManifestType getHandledType();

	default M mockManifest() {
		return mockTypedManifest(getHandledType());
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	@Provider
	default D createTestInstance(TestSettings settings) {
		try {
			return settings.process(createNoArgs());
		} catch (Exception e) {
			throw new AssertionError("Failed to call no-args constructur", e);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.xml.ManifestXmlDelegate#setInstance(java.lang.Object)}.
	 */
	@Test
	default void testSetInstance() {
		assertRestrictedSetter(create(),
				ManifestXmlDelegate::setInstance,
				mockManifest(), mockManifest(),
				NPE_CHECK, ILLEGAL_STATE_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.xml.ManifestXmlDelegate#getInstance()}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	default void testGetInstance() {
		assertRestrictedGetter(create(),
				mockManifest(),
				ILLEGAL_STATE_CHECK,
				d -> (M) d.getInstance(),
				ManifestXmlDelegate::setInstance);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.xml.ManifestXmlDelegate#reset()}.
	 */
	@Test
	default void testReset() {
		D delegate = create();

		M manifest = mockManifest();

		delegate.setInstance(manifest);
		assertSame(manifest, delegate.getInstance());

		delegate.reset();
		ILLEGAL_STATE_CHECK.accept(() -> delegate.getInstance(), "Testing retrieval of null manifest");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.xml.ManifestXmlDelegate#reset(java.lang.Object)}.
	 */
	@Test
	@DisplayName("test reset() with new instance")
	default void testResetM() {
		D delegate = create();

		M manifest1 = mockManifest();
		M manifest2 = mockManifest();

		delegate.setInstance(manifest1);
		assertSame(manifest1, delegate.getInstance());

		assertSame(delegate, delegate.reset(manifest2));
		assertSame(manifest2, delegate.getInstance());
	}

	/**
	 * Produces {@link Config} instances that will each trigger a separate {@link DynamicTest}
	 * being created by the {@link #testWriteXml()} method.
	 * <p>
	 * The returned list must contain at least {@code 1} entry!
	 *
	 * @return
	 */
	default List<Config> configurations() {
		return list(ManifestGenerator.config().label("<basic>"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.xml.ManifestXmlDelegate#writeXml(de.ims.icarus2.util.xml.XmlSerializer)}.
	 * @throws Exception
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	@DisplayName("test writeXml() with simple value types")
	default Stream<DynamicTest> testWriteXml() throws Exception {
		final ManifestType type = getHandledType();

		final ManifestFactory factory = new DefaultManifestFactory(
				mockManifestLocation(type.isSupportTemplating()),
				mockManifestRegistry());

		final ManifestGenerator generator = new ManifestGenerator(factory);

		List<Config> configurations = configurations();
		assertCollectionNotEmpty(configurations);

		/*
		 * Create 1 test case per configuration.
		 * Within the test case we still check every scenario given by the
		 * incremental build stages separately. But it is much clearer to have
		 * them wrapped this way instead of hundreds of almost identically named
		 * dynamic tests...
		 */
		return configurations.stream()
			.map(config -> dynamicTest(config.getLabel(), () -> {

				final IncrementalBuild<M> build = ManifestGenerator.generateOnce(
					generator, type, config);

				/*
				 *  do-while loop is used since the build can be static (i.e. have no
				 *  extra construction steps) but we still need to run a test for the
				 *  "blank" instance in any case.
				 */
				int step = 1;
				do {
					D delegate = create();
					M original = build.getInstance();
					M target = generator.generatePlainCopy(type, original, config);

					String label = String.format("%s: %s - step %d/%d",
							build.currentLabel(), type, step++, build.getChangeCount()+1);

					ManifestXmlTestUtils.assertSerializationEquals(
							label, original, target, delegate,
							true,
							true); // dump intermediary xml representation
				} while(build.applyNextChange());
			}));
	}

}
