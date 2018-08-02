/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.api;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
public interface ManifestRegistryTest {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#createUID()}.
	 */
	@Test
	default void testCreateUID() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#resetUIDs()}.
	 */
	@Test
	default void testResetUIDs() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#forEachLayerType(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLayerType() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getLayerTypes()}.
	 */
	@Test
	default void testGetLayerTypes() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getLayerTypes(java.util.function.Predicate)}.
	 */
	@Test
	default void testGetLayerTypesPredicateOfQsuperLayerType() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getLayerType(java.lang.String)}.
	 */
	@Test
	default void testGetLayerType() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#addLayerType(de.ims.icarus2.model.manifest.api.LayerType)}.
	 */
	@Test
	default void testAddLayerType() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#removeLayerType(de.ims.icarus2.model.manifest.api.LayerType)}.
	 */
	@Test
	default void testRemoveLayerType() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getOverlayLayerType()}.
	 */
	@Test
	default void testGetOverlayLayerType() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#addCorpusManifest(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testAddCorpusManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#removeCorpusManifest(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testRemoveCorpusManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getCorpusSources()}.
	 */
	@Test
	default void testGetCorpusSources() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getCorpusManifest(java.lang.String)}.
	 */
	@Test
	default void testGetCorpusManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#isLocked(de.ims.icarus2.model.manifest.api.Manifest)}.
	 */
	@Test
	default void testIsLocked() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#forEachCorpus(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachCorpus() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getCorpusIds()}.
	 */
	@Test
	default void testGetCorpusIds() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getCorpusManifests()}.
	 */
	@Test
	default void testGetCorpusManifests() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getCorpusManifests(java.util.function.Predicate)}.
	 */
	@Test
	default void testGetCorpusManifestsPredicateOfQsuperCorpusManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getCorpusManifestsForSource(de.ims.icarus2.model.manifest.api.ManifestLocation)}.
	 */
	@Test
	default void testGetCorpusManifestsForSource() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#hasTemplate(java.lang.String)}.
	 */
	@Test
	default void testHasTemplate() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getTemplate(java.lang.String)}.
	 */
	@Test
	default void testGetTemplate() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#addContextManifest(de.ims.icarus2.model.manifest.api.CorpusManifest, de.ims.icarus2.model.manifest.api.ContextManifest)}.
	 */
	@Test
	default void testAddContextManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#removeContextManifest(de.ims.icarus2.model.manifest.api.CorpusManifest, de.ims.icarus2.model.manifest.api.ContextManifest)}.
	 */
	@Test
	default void testRemoveContextManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#corpusManifestChanged(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testCorpusManifestChanged() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#contextManifestChanged(de.ims.icarus2.model.manifest.api.ContextManifest)}.
	 */
	@Test
	default void testContextManifestChanged() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#forEachTemplate(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachTemplate() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getTemplates()}.
	 */
	@Test
	default void testGetTemplates() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getTemplates(java.util.function.Predicate)}.
	 */
	@Test
	default void testGetTemplatesPredicateOfQsuperM() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getContextTemplates()}.
	 */
	@Test
	default void testGetContextTemplates() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getTemplatesOfType(de.ims.icarus2.model.manifest.api.ManifestType)}.
	 */
	@Test
	default void testGetTemplatesOfType() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getTemplatesOfClass(java.lang.Class)}.
	 */
	@Test
	default void testGetTemplatesOfClass() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getTemplatesForSource(de.ims.icarus2.model.manifest.api.ManifestLocation)}.
	 */
	@Test
	default void testGetTemplatesForSource() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getRootContextTemplates()}.
	 */
	@Test
	default void testGetRootContextTemplates() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getTemplateSources()}.
	 */
	@Test
	default void testGetTemplateSources() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#addTemplate(de.ims.icarus2.model.manifest.api.Manifest)}.
	 */
	@Test
	default void testAddTemplate() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#addTemplates(java.util.Collection)}.
	 */
	@Test
	default void testAddTemplates() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#removeTemplate(de.ims.icarus2.model.manifest.api.Manifest)}.
	 */
	@Test
	default void testRemoveTemplate() {
		fail("Not yet implemented");
	}

}
