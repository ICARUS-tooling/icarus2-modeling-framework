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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestLocation;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubId;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubIdentity;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_ILLEGAL;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertAccumulativeAdd;
import static de.ims.icarus2.test.TestUtils.assertAccumulativeGetter;
import static de.ims.icarus2.test.TestUtils.assertAccumulativeLookupContains;
import static de.ims.icarus2.test.TestUtils.assertAccumulativeOptLookup;
import static de.ims.icarus2.test.TestUtils.assertAccumulativeRemove;
import static de.ims.icarus2.test.TestUtils.assertCollectionEmpty;
import static de.ims.icarus2.test.TestUtils.assertCollectionEquals;
import static de.ims.icarus2.test.TestUtils.assertForEach;
import static de.ims.icarus2.test.TestUtils.assertMock;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.assertSetter;
import static de.ims.icarus2.test.TestUtils.inject_genericSetter;
import static de.ims.icarus2.test.TestUtils.settings;
import static de.ims.icarus2.test.TestUtils.unwrapGetter;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestFrameworkTest;
import de.ims.icarus2.model.manifest.api.events.ManifestEvents;
import de.ims.icarus2.model.manifest.standard.DefaultLayerTypeIds;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.util.events.EventManagerTest;
import de.ims.icarus2.util.events.EventObject;
import de.ims.icarus2.util.events.SimpleEventListener;

/**
 * @author Markus Gärtner
 *
 */
public interface ManifestRegistryTest<R extends ManifestRegistry>
		extends ManifestFrameworkTest<R>, EventManagerTest<R> {

	public static CorpusManifest mockCorpusManifest(String id) {
		return (CorpusManifest) stubId(
				(Manifest)mockTypedManifest(ManifestType.CORPUS_MANIFEST), id);
	}

	public static CorpusManifest mockCorpusManifest(String id, ManifestLocation location) {
		CorpusManifest manifest = mockCorpusManifest(id);

		when(manifest.getManifestLocation()).thenReturn(location);

		return manifest;
	}

	public static LayerType mockLayerType(String id) {
		return stubIdentity(mock(LayerType.class), id);
	}

	@SuppressWarnings("boxing")
	public static Manifest mockTemplate(String id) {
		Manifest template = stubId(mock(Manifest.class), id);

		when(template.getManifestType()).thenReturn(ManifestType.DUMMY_MANIFEST);
		when(template.isTemplate()).thenReturn(Boolean.TRUE);

		return template;
	}

	public static Manifest mockTemplate(String id, ManifestLocation location) {
		Manifest template = mockTemplate(id);
		when(template.getManifestLocation()).thenReturn(location);

		return template;
	}

	@SuppressWarnings("boxing")
	public static Manifest mockTemplate(String id, Class<? extends Manifest> clazz) {
		Manifest template = mockTypedManifest(clazz);
		stubId((Manifest)template, id);
		when(template.isTemplate()).thenReturn(Boolean.TRUE);
		when(template.getManifestType()).thenReturn(ManifestType.DUMMY_MANIFEST);
		return template;
	}

	@SuppressWarnings("boxing")
	public static <M extends Manifest> M mockTemplate(String id, ManifestType type) {
		M template = mockTypedManifest(type);
		stubId((Manifest)template, id);
		when(template.isTemplate()).thenReturn(Boolean.TRUE);
		return template;
	}

	@SuppressWarnings("boxing")
	public static ContextManifest mockContextTemplate(String id, boolean independent) {
		ContextManifest manifest = mockTypedManifest(ManifestType.CONTEXT_MANIFEST);
		stubId((Manifest)manifest, id);
		when(manifest.isTemplate()).thenReturn(Boolean.TRUE);
		when(manifest.isIndependentContext()).thenReturn(independent);
		return manifest;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#createUID()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testCreateUID() {
		Set<Integer> oldUIDs = new HashSet<>();

		final int iterations = 50_000;

		R instance = createTestInstance(settings());

		for(int i=0; i<iterations; i++) {
			int uid = instance.createUID();

			assertFalse(oldUIDs.contains(uid));

			oldUIDs.add(uid);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#resetUIDs()}.
	 */
	@Test
	default void testResetUIDs() {
		R instance = createTestInstance(settings());

		instance.createUID();

		instance.resetUIDs();

		instance.createUID();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#forEachLayerType(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLayerType() {
		assertForEach(
				createTestInstance(settings()),
				mockLayerType("type1"),
				mockLayerType("type2"),
				(Function<R, Consumer<Consumer<? super LayerType>>>)m -> m::forEachLayerType,
				ManifestRegistry::addLayerType);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getLayerTypes()}.
	 */
	@Test
	default void testGetLayerTypes() {
		assertAccumulativeGetter(
				createTestInstance(settings()),
				mockLayerType("type1"),
				mockLayerType("type2"),
				ManifestRegistry::getLayerTypes,
				ManifestRegistry::addLayerType);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getLayerTypes(java.util.function.Predicate)}.
	 */
	@Test
	default void testGetLayerTypesPredicateOfQsuperLayerType() {
		R registry = createTestInstance(settings());

		Predicate<LayerType> pAll = m -> true;
		Predicate<LayerType> pNone = m -> false;
		@SuppressWarnings("boxing")
		Predicate<LayerType> pId5 = m -> m.getId().map(id -> id.endsWith("5")).orElse(Boolean.FALSE);

		assertNPE(() -> registry.getLayerTypes(null));

		assertCollectionEmpty(registry.getLayerTypes(pAll));

		LayerType type1 = mockLayerType("type1");
		LayerType type2 = mockLayerType("type2");
		LayerType type3 = mockLayerType("type3");
		LayerType type4 = mockLayerType("type4");
		LayerType type5 = mockLayerType("type5");
		LayerType type6 = mockLayerType("type6");

		registry.addLayerType(type1);
		registry.addLayerType(type2);
		registry.addLayerType(type3);
		registry.addLayerType(type4);
		registry.addLayerType(type5);
		registry.addLayerType(type6);

		assertCollectionEquals(registry.getLayerTypes(pAll),
				type1, type2, type3, type4, type5, type6);

		assertCollectionEmpty(registry.getLayerTypes(pNone));

		assertCollectionEquals(registry.getLayerTypes(pId5), type5);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getLayerType(java.lang.String)}.
	 */
	@Test
	default void testGetLayerType() {
		assertAccumulativeOptLookup(
				createTestInstance(settings()),
				mockLayerType("type1"),
				mockLayerType("type2"),
				ManifestRegistry::getLayerType,
				NPE_CHECK,
				ManifestRegistry::addLayerType,
				unwrapGetter(LayerType::getId),
				"type3", "type4");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#addLayerType(de.ims.icarus2.model.manifest.api.LayerType)}.
	 */
	@Test
	default void testAddLayerType() {
		assertSetter(
				createTestInstance(settings()),
				ManifestRegistry::addLayerType,
				mockLayerType("type1"),
				NPE_CHECK, DUPLICATE_ID_CHECK,
				mockLayerType("type1"));

//		assertSetter(
//				createTestInstance(settings()),
//				ManifestRegistry::addLayerType,
//				mockLayerType("type1"),
//				NPE_CHECK, INVALID_ID_CHECK,
//				mockLayerType(null),
//				mockLayerType("§$%&/("),
//				mockLayerType(""),
//				mockLayerType("x"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#removeLayerType(de.ims.icarus2.model.manifest.api.LayerType)}.
	 */
	@Test
	default void testRemoveLayerType() {
		assertAccumulativeRemove(
				createTestInstance(settings()),
				ManifestRegistry::addLayerType,
				ManifestRegistry::removeLayerType,
				ManifestRegistry::getLayerTypes,
				NPE_CHECK, UNKNOWN_ID_CHECK,
				mockLayerType("type1"),
				mockLayerType("type2"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getOverlayLayerType()}.
	 */
	@Test
	default void testGetOverlayLayerType() {
		R instance = createTestInstance(settings());

		// No registry implementation is expected to provide its own overlay layer type, so we need to add it first
		instance.addLayerType(mockLayerType(DefaultLayerTypeIds.ITEM_LAYER_OVERLAY));

		assertNotNull(instance.getOverlayLayerType());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#addCorpusManifest(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testAddCorpusManifest() {
		assertAccumulativeAdd(
				createTestInstance(settings()),
				ManifestRegistry::addCorpusManifest,
				NO_ILLEGAL(), NO_CHECK,
				NPE_CHECK, DUPLICATE_ID_CHECK,
				mockCorpusManifest("corpus1"),
				mockCorpusManifest("corpus2"),
				mockCorpusManifest("corpus3"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#removeCorpusManifest(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testRemoveCorpusManifest() {
		assertAccumulativeRemove(
				createTestInstance(settings()),
				ManifestRegistry::addCorpusManifest,
				ManifestRegistry::removeCorpusManifest,
				ManifestRegistry::getCorpusManifests,
				NPE_CHECK, UNKNOWN_ID_CHECK,
				mockCorpusManifest("corpus1"),
				mockCorpusManifest("corpus2"),
				mockCorpusManifest("corpus3"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getCorpusSources()}.
	 */
	@Test
	default void testGetCorpusSources() {
		R registry = createTestInstance(settings());

		assertCollectionEmpty(registry.getCorpusSources());

		ManifestLocation location1 = mockManifestLocation(true);
		ManifestLocation location2 = mockManifestLocation(true);
		ManifestLocation location3 = mockManifestLocation(true);

		registry.addCorpusManifest(mockCorpusManifest("orpus1", location1));

		assertCollectionEquals(registry.getCorpusSources(), location1);

		registry.addCorpusManifest(mockCorpusManifest("corpus2", location1));
		registry.addCorpusManifest(mockCorpusManifest("corpus3", location2));
		registry.addCorpusManifest(mockCorpusManifest("corpus4", location2));
		registry.addCorpusManifest(mockCorpusManifest("corpus5", location2));
		registry.addCorpusManifest(mockCorpusManifest("corpus6", location3));

		assertCollectionEquals(registry.getCorpusSources(),
				location1, location2, location3);

		for(CorpusManifest manifest : registry.getCorpusManifests()) {
			registry.removeCorpusManifest(manifest);
		}

		assertCollectionEmpty(registry.getCorpusSources());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getCorpusManifest(java.lang.String)}.
	 */
	@Test
	default void testGetCorpusManifest() {
		assertAccumulativeOptLookup(
				createTestInstance(settings()),
				mockCorpusManifest("corpus1"),
				mockCorpusManifest("corpus2"),
				ManifestRegistry::getCorpusManifest,
				NPE_CHECK,
				ManifestRegistry::addCorpusManifest,
				unwrapGetter(CorpusManifest::getId));
	}

	/**
	 * Create a new {@link ManifestRegistry} instance and adds the given {@link Manifest}
	 * as either a {@link ManifestRegistry#addTemplate(Manifest) template} or
	 * {@link ManifestRegistry#addCorpusManifest(CorpusManifest) corpus}, depending on
	 * the {@code template} parameter.
	 * <p>
	 * Subsequently, all {@code lockedManifest} objects, if present, are checked and
	 * asserted to be {@link ManifestRegistry#isLocked(Manifest) locked}.
	 *
	 * @param settings
	 * @param manifest
	 * @param template
	 * @param lockedManifests
	 */
	default void assertLocked(TestSettings settings, Manifest manifest,
			boolean template, Manifest...lockedManifests) {

		// Run every assertion with a brand new registry
		R registry = createTestInstance(settings.clone());

		// Phase 1: register manifest with registry and check that all subs are locked

		if(template) {
			registry.addTemplate(manifest);
		} else {
			registry.addCorpusManifest((CorpusManifest) manifest);
		}

		assertFalse(registry.isLocked(manifest), settings.getMessage());

		for(Manifest lockedManifest : lockedManifests) {
			assertTrue(registry.isLocked(lockedManifest), settings.getMessage());
		}

		assertFalse(registry.isLocked(manifest), settings.getMessage());

		// Phase 2: unregister manifest again and check that all subs are unlocked again

		if(template) {
			registry.removeTemplate(manifest);
		} else {
			registry.removeCorpusManifest((CorpusManifest) manifest);
		}

		assertFalse(registry.isLocked(manifest), settings.getMessage());

		for(Manifest lockedManifest : lockedManifests) {
			assertFalse(registry.isLocked(lockedManifest), settings.getMessage());
		}

		assertFalse(registry.isLocked(manifest), settings.getMessage());
	}

	@SuppressWarnings("boxing")
	public static <M extends Manifest> M stubTemplate(M manifest, M template) {
		assertMock(manifest);
		assertMock(template);

		when(template.isTemplate()).thenReturn(Boolean.TRUE);
		when(manifest.getTemplate()).thenReturn(template);
		when(manifest.hasTemplate()).thenReturn(Boolean.TRUE);

		return manifest;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#isLocked(de.ims.icarus2.model.manifest.api.Manifest)}.
	 */
	@SuppressWarnings({ "boxing", "unchecked" })
	@Test
	default void testIsLocked() {

		// Treat corpus manifests special
		CorpusManifest corpus = mockCorpusManifest("corpus");
		assertLocked(settings(), corpus, false);

		// Try ALL types that support templating
		for(ManifestType type : ManifestType.values()) {
			if(type.isSupportTemplating()) {
				Manifest template = mockTemplate("template"+type, type);
				Manifest manifest = stubTemplate(mockTemplate("manifest"+type, type), template);
				assertLocked(settings().message("Testing simple templating for type: "+type),
						manifest, true, template);
			}
		}

		// Test special constructions

		// OptionsManifest instances referenced by a MemberManifest
		for(ManifestType type : ManifestType.getMemberTypes()) {
			MemberManifest manifest = mockTypedManifest(type, "manifest"+type);
			if(type.isSupportTemplating()) {
				when(manifest.isTemplate()).thenReturn(Boolean.TRUE);
			}
			OptionsManifest optionsManifest = mockTemplate("options", ManifestType.OPTIONS_MANIFEST);
			when(manifest.getOptionsManifest()).thenReturn(Optional.of(optionsManifest));
			assertLocked(settings().message("Testing options manifest for type: "+type),
					manifest, type.isSupportTemplating(), optionsManifest);
		}

		// LayerManifest instances referenced by a live LayerType
		for(ManifestType type : ManifestType.getLayerTypes()) {
			LayerManifest sharedManifest = mockTemplate("sharedManifest"+type, type);
			LayerType layerType = mockLayerType("type");
			when(layerType.getSharedManifest()).thenReturn(Optional.of(sharedManifest));
			LayerManifest manifest = mockTemplate("template"+type, type);
			when(manifest.getLayerType()).thenReturn(Optional.of(layerType));
			assertLocked(settings().message("Testing referenced layer type for type: "+type),
					manifest, true, sharedManifest);

		}

		// ContextManifest instances inside a CorpusManifest
		CorpusManifest corpusManifestForContextTest = mockCorpusManifest("corpus");
		ContextManifest contextManifest = mockTemplate("context", ManifestType.CONTEXT_MANIFEST);
		doAnswer(invocation -> {
			((Consumer<? super ContextManifest>)invocation.getArgument(0)).accept(contextManifest);
			return null;
		}).when(corpusManifestForContextTest).forEachContextManifest(any());
		assertLocked(settings().message("Testing context inside corpus"),
				corpusManifestForContextTest, false, contextManifest);

		// LayerManifest instances inside a ContextManifest
		for(ManifestType type : ManifestType.getLayerTypes()) {
			ContextManifest contextManifestForLayerTest = mockTemplate("context", ManifestType.CONTEXT_MANIFEST);
			LayerManifest layerManifest = mockTemplate("template"+type, type);
			doAnswer(invocation -> {
				((Consumer<? super LayerManifest>)invocation.getArgument(0)).accept(layerManifest);
				return null;
			}).when(contextManifestForLayerTest).forEachLayerManifest(any());
			assertLocked(settings().message("Testing layer manifest inside context: "+type),
					contextManifestForLayerTest, true, layerManifest);

		}

		// DriverManifest instances inside a ContextManifest
		ContextManifest contextManifestForDriverTest = mockTemplate("context", ManifestType.CONTEXT_MANIFEST);
		DriverManifest driverManifest = mockTemplate("driver", ManifestType.DRIVER_MANIFEST);
		when(contextManifestForDriverTest.getDriverManifest()).thenReturn(Optional.of(driverManifest));
		assertLocked(settings().message("Testing driver inside context"),
				contextManifestForDriverTest, true, driverManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#forEachCorpus(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachCorpus() {
		assertForEach(
				createTestInstance(settings()),
				mockCorpusManifest("corpus1"),
				mockCorpusManifest("corpus2"),
				(Function<R, Consumer<Consumer<? super CorpusManifest>>>)reg -> reg::forEachCorpus,
				ManifestRegistry::addCorpusManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getCorpusIds()}.
	 */
	@Test
	default void testGetCorpusIds() {
		assertAccumulativeGetter(
				createTestInstance(settings()),
				"corpus1",
				"corpus2",
				ManifestRegistry::getCorpusIds,
				inject_genericSetter(
						ManifestRegistry::addCorpusManifest,
						ManifestRegistryTest::mockCorpusManifest));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getCorpusManifests()}.
	 */
	@Test
	default void testGetCorpusManifests() {
		assertAccumulativeGetter(
				createTestInstance(settings()),
				mockCorpusManifest("corpus1"),
				mockCorpusManifest("corpus2"),
				ManifestRegistry::getCorpusManifests,
				ManifestRegistry::addCorpusManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getCorpusManifests(java.util.function.Predicate)}.
	 */
	@Test
	default void testGetCorpusManifestsPredicateOfQsuperCorpusManifest() {
		R registry = createTestInstance(settings());

		ManifestLocation location1 = mockManifestLocation(false);
		ManifestLocation location2 = mockManifestLocation(false);
		ManifestLocation location3 = mockManifestLocation(false);

		Predicate<CorpusManifest> pAll = m -> true;
		Predicate<CorpusManifest> pNone = m -> false;
		Predicate<CorpusManifest> pLoc2 = m -> m.getManifestLocation()==location2;
		@SuppressWarnings("boxing")
		Predicate<CorpusManifest> pId5 = m -> m.getId().map(id -> id.endsWith("5")).orElse(Boolean.FALSE);

		assertNPE(() -> registry.getCorpusManifests(null));

		assertCollectionEmpty(registry.getCorpusManifests(pAll));

		CorpusManifest corpus1 = mockCorpusManifest("corpus1", location1);
		CorpusManifest corpus2 = mockCorpusManifest("corpus2", location1);
		CorpusManifest corpus3 = mockCorpusManifest("corpus3", location2);
		CorpusManifest corpus4 = mockCorpusManifest("corpus4", location2);
		CorpusManifest corpus5 = mockCorpusManifest("corpus5", location2);
		CorpusManifest corpus6 = mockCorpusManifest("corpus6", location3);

		registry.addCorpusManifest(corpus1);
		registry.addCorpusManifest(corpus2);
		registry.addCorpusManifest(corpus3);
		registry.addCorpusManifest(corpus4);
		registry.addCorpusManifest(corpus5);
		registry.addCorpusManifest(corpus6);

		assertCollectionEquals(registry.getCorpusManifests(pAll),
				corpus1, corpus2, corpus3, corpus4, corpus5, corpus6);

		assertCollectionEmpty(registry.getCorpusManifests(pNone));

		assertCollectionEquals(registry.getCorpusManifests(pLoc2),
				corpus3, corpus4, corpus5);

		assertCollectionEquals(registry.getCorpusManifests(pId5), corpus5);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getCorpusManifestsForSource(de.ims.icarus2.model.manifest.api.ManifestLocation)}.
	 */
	@Test
	default void testGetCorpusManifestsForSource() {
		R registry = createTestInstance(settings());

		assertNPE(() -> registry.getCorpusManifestsForSource(null));

		assertCollectionEmpty(registry.getCorpusManifestsForSource(mockManifestLocation(true)));

		ManifestLocation location1 = mockManifestLocation(false);
		ManifestLocation location2 = mockManifestLocation(false);
		ManifestLocation location3 = mockManifestLocation(false);

		CorpusManifest corpus1 = mockCorpusManifest("corpus1", location1);
		CorpusManifest corpus2 = mockCorpusManifest("corpus2", location1);
		CorpusManifest corpus3 = mockCorpusManifest("corpus3", location2);
		CorpusManifest corpus4 = mockCorpusManifest("corpus4", location2);
		CorpusManifest corpus5 = mockCorpusManifest("corpus5", location2);
		CorpusManifest corpus6 = mockCorpusManifest("corpus6", location3);

		registry.addCorpusManifest(corpus1);
		registry.addCorpusManifest(corpus2);
		registry.addCorpusManifest(corpus3);
		registry.addCorpusManifest(corpus4);
		registry.addCorpusManifest(corpus5);
		registry.addCorpusManifest(corpus6);

		assertCollectionEquals(registry.getCorpusManifestsForSource(location1), corpus1, corpus2);
		assertCollectionEquals(registry.getCorpusManifestsForSource(location2), corpus3, corpus4, corpus5);
		assertCollectionEquals(registry.getCorpusManifestsForSource(location3), corpus6);
		assertCollectionEmpty(registry.getCorpusManifestsForSource(mockManifestLocation(true)));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#hasTemplate(java.lang.String)}.
	 */
	@Test
	default void testHasTemplate() {
		assertAccumulativeLookupContains(
				createTestInstance(settings()),
				mockTemplate("template1"),
				mockTemplate("template2"),
				ManifestRegistry::hasTemplate,
				NPE_CHECK,
				ManifestRegistry::addTemplate,
				unwrapGetter(Manifest::getId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getTemplate(java.lang.String)}.
	 */
	@Test
	default void testGetTemplate() {
		assertAccumulativeOptLookup(
				createTestInstance(settings()),
				mockTemplate("template1"),
				mockTemplate("template2"),
				ManifestRegistry::getTemplate,
				NPE_CHECK,
				ManifestRegistry::addTemplate,
				unwrapGetter(Manifest::getId),
				"template3");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#corpusManifestChanged(de.ims.icarus2.model.manifest.api.CorpusManifest)}.
	 */
	@Test
	default void testCorpusManifestChanged() {
		R registry = createTestInstance(settings());

		CorpusManifest manifest = mockCorpusManifest("corpus1");

		SimpleEventListener listener = mock(SimpleEventListener.class);

		registry.addListener(listener, null);

		registry.corpusManifestChanged(manifest);

		verify(listener, times(1)).invoke(any(), any());

		doAnswer(invocation -> {
			assertSame(registry, invocation.getArgument(0));

			EventObject event = invocation.getArgument(1);
			assertNotNull(event);

			assertEquals(ManifestEvents.CHANGED_CORPUS, event.getName());

			assertSame(manifest, event.getProperty("corpus"));

			return null;
		}).when(listener).invoke(any(), any());

		registry.corpusManifestChanged(manifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#contextManifestChanged(de.ims.icarus2.model.manifest.api.ContextManifest)}.
	 */
	@Test
	default void testContextManifestChanged() {
		R registry = createTestInstance(settings());

		CorpusManifest corpusManifest = mockCorpusManifest("corpus1");
		ContextManifest contextManifest = mockTypedManifest(ManifestType.CONTEXT_MANIFEST);
		when(contextManifest.getCorpusManifest()).thenReturn(Optional.of(corpusManifest));

		SimpleEventListener listener = mock(SimpleEventListener.class);

		registry.addListener(listener, null);

		registry.contextManifestChanged(contextManifest);

		verify(listener, times(1)).invoke(any(), any());

		doAnswer(invocation -> {
			assertSame(registry, invocation.getArgument(0));

			EventObject event = invocation.getArgument(1);
			assertNotNull(event);

			assertEquals(ManifestEvents.CHANGED_CONTEXT, event.getName());

			assertSame(corpusManifest, event.getProperty("corpus"));
			assertSame(contextManifest, event.getProperty("context"));

			return null;
		}).when(listener).invoke(any(), any());

		registry.contextManifestChanged(contextManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#forEachTemplate(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachTemplate() {
		assertForEach(
				createTestInstance(settings()),
				mockTemplate("template1"),
				mockTemplate("template2"),
				(Function<R, Consumer<Consumer<? super Manifest>>>)reg -> reg::forEachTemplate,
				ManifestRegistry::addTemplate);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getTemplates()}.
	 */
	@Test
	default void testGetTemplates() {
		assertAccumulativeGetter(
				createTestInstance(settings()),
				mockTemplate("template1"),
				mockTemplate("template2"),
				ManifestRegistry::getTemplates,
				ManifestRegistry::addTemplate);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getTemplates(java.util.function.Predicate)}.
	 */
	@Test
	default void testGetTemplatesPredicateOfQsuperM() {
		R registry = createTestInstance(settings());

		Predicate<Manifest> pAll = m -> true;
		Predicate<Manifest> pNone = m -> false;
		Predicate<Manifest> pDriver = m -> m.getManifestType()==ManifestType.DRIVER_MANIFEST;
		@SuppressWarnings("boxing")
		Predicate<Manifest> pId5 = m -> m.getId().map(id -> id.endsWith("5")).orElse(Boolean.FALSE);

		assertNPE(() -> registry.getTemplates(null));

		assertCollectionEmpty(registry.getTemplates(pAll));

		ManifestType type1 = ManifestType.CONTEXT_MANIFEST;
		ManifestType type2 = ManifestType.ITEM_LAYER_MANIFEST;
		ManifestType type3 = ManifestType.DRIVER_MANIFEST;

		Manifest template1 = mockTemplate("template1", type1);
		Manifest template2 = mockTemplate("template2", type1);
		Manifest template3 = mockTemplate("template3", type2);
		Manifest template4 = mockTemplate("template4", type2);
		Manifest template5 = mockTemplate("template5", type2);
		Manifest template6 = mockTemplate("template6", type3);

		registry.addTemplates(list(template1, template2, template3,
				template4, template5, template6));

		assertCollectionEquals(registry.getTemplates(pAll), template1, template2, template3,
				template4, template5, template6);

		assertCollectionEmpty(registry.getTemplates(pNone));

		assertCollectionEquals(registry.getTemplates(pDriver), template6);

		assertCollectionEquals(registry.getTemplates(pId5), template5);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getContextTemplates()}.
	 */
	@Test
	default void testGetContextTemplates() {
		assertAccumulativeGetter(
				createTestInstance(settings()),
				mockContextTemplate("context1", true),
				mockContextTemplate("context2", false),
				ManifestRegistry::getContextTemplates,
				ManifestRegistry::addTemplate);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getTemplatesOfType(de.ims.icarus2.model.manifest.api.ManifestType)}.
	 */
	@Test
	default void testGetTemplatesOfType() {
		R registry = createTestInstance(settings());

		assertNPE(() -> registry.getTemplatesOfType(null));

		assertCollectionEmpty(registry.getTemplatesOfType(ManifestType.CONTEXT_MANIFEST));

		ManifestType type1 = ManifestType.CONTEXT_MANIFEST;
		ManifestType type2 = ManifestType.ITEM_LAYER_MANIFEST;
		ManifestType type3 = ManifestType.DRIVER_MANIFEST;

		Manifest template1 = mockTemplate("template1", type1);
		Manifest template2 = mockTemplate("template2", type1);
		Manifest template3 = mockTemplate("template3", type2);
		Manifest template4 = mockTemplate("template4", type2);
		Manifest template5 = mockTemplate("template5", type2);
		Manifest template6 = mockTemplate("template6", type3);

		registry.addTemplates(list(template1, template2, template3,
				template4, template5, template6));

		assertCollectionEquals(registry.getTemplatesOfType(type1), template1, template2);
		assertCollectionEquals(registry.getTemplatesOfType(type2), template3, template4, template5);
		assertCollectionEquals(registry.getTemplatesOfType(type3), template6);
		assertCollectionEmpty(registry.getTemplatesOfType(ManifestType.FRAGMENT_LAYER_MANIFEST));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getTemplatesOfClass(java.lang.Class)}.
	 */
	@Test
	default void testGetTemplatesOfClass() {
		R registry = createTestInstance(settings());

		assertNPE(() -> registry.getTemplatesOfClass(null));

		assertCollectionEmpty(registry.getTemplatesOfClass(Manifest.class));

		Class<? extends Manifest> clazz1 = ContextManifest.class;
		Class<? extends Manifest> clazz2 = ItemLayerManifest.class;
		Class<? extends Manifest> clazz3 = DriverManifest.class;

		Manifest template1 = mockTemplate("template1", clazz1);
		Manifest template2 = mockTemplate("template2", clazz1);
		Manifest template3 = mockTemplate("template3", clazz2);
		Manifest template4 = mockTemplate("template4", clazz2);
		Manifest template5 = mockTemplate("template5", clazz2);
		Manifest template6 = mockTemplate("template6", clazz3);

		registry.addTemplates(list(template1, template2, template3,
				template4, template5, template6));

		assertCollectionEquals(registry.getTemplatesOfClass(clazz1), template1, template2);
		assertCollectionEquals(registry.getTemplatesOfClass(clazz2), template3, template4, template5);
		assertCollectionEquals(registry.getTemplatesOfClass(clazz3), template6);
		assertCollectionEmpty(registry.getTemplatesOfClass(FragmentLayerManifest.class));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getTemplatesForSource(de.ims.icarus2.model.manifest.api.ManifestLocation)}.
	 */
	@Test
	default void testGetTemplatesForSource() {
		R registry = createTestInstance(settings());

		assertNPE(() -> registry.getTemplatesForSource(null));

		assertCollectionEmpty(registry.getTemplatesForSource(mockManifestLocation(true)));

		ManifestLocation location1 = mockManifestLocation(true);
		ManifestLocation location2 = mockManifestLocation(true);
		ManifestLocation location3 = mockManifestLocation(true);

		Manifest template1 = mockTemplate("template1", location1);
		Manifest template2 = mockTemplate("template2", location1);
		Manifest template3 = mockTemplate("template3", location2);
		Manifest template4 = mockTemplate("template4", location2);
		Manifest template5 = mockTemplate("template5", location2);
		Manifest template6 = mockTemplate("template6", location3);

		registry.addTemplates(list(template1, template2, template3,
				template4, template5, template6));

		assertCollectionEquals(registry.getTemplatesForSource(location1), template1, template2);
		assertCollectionEquals(registry.getTemplatesForSource(location2), template3, template4, template5);
		assertCollectionEquals(registry.getTemplatesForSource(location3), template6);
		assertCollectionEmpty(registry.getTemplatesForSource(mockManifestLocation(true)));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getRootContextTemplates()}.
	 */
	@Test
	default void testGetRootContextTemplates() {
		R registry = createTestInstance(settings());

		assertCollectionEmpty(registry.getRootContextTemplates());

		ContextManifest root1 = mockContextTemplate("context1", true);
		ContextManifest root2 = mockContextTemplate("context2", true);
		ContextManifest context1 = mockContextTemplate("context3", false);
		ContextManifest context2 = mockContextTemplate("context4", false);

		registry.addTemplates(list(root1, context1, root2, context2));

		assertCollectionEquals(registry.getRootContextTemplates(), root1, root2);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#getTemplateSources()}.
	 */
	@Test
	default void testGetTemplateSources() {
		R registry = createTestInstance(settings());

		assertCollectionEmpty(registry.getTemplateSources());

		ManifestLocation location1 = mockManifestLocation(true);
		ManifestLocation location2 = mockManifestLocation(true);
		ManifestLocation location3 = mockManifestLocation(true);

		registry.addTemplate(mockTemplate("template1", location1));

		assertCollectionEquals(registry.getTemplateSources(), location1);

		registry.addTemplate(mockTemplate("template2", location1));
		registry.addTemplate(mockTemplate("template3", location2));
		registry.addTemplate(mockTemplate("template4", location2));
		registry.addTemplate(mockTemplate("template5", location2));
		registry.addTemplate(mockTemplate("template6", location3));

		assertCollectionEquals(registry.getTemplateSources(),
				location1, location2, location3);

		for(Manifest template : registry.getTemplates()) {
			registry.removeTemplate(template);
		}

		assertCollectionEmpty(registry.getTemplateSources());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#addTemplate(de.ims.icarus2.model.manifest.api.Manifest)}.
	 */
	@Test
	default void testAddTemplate() {
		assertAccumulativeAdd(
				createTestInstance(settings()),
				ManifestRegistry::addTemplate,
				NO_ILLEGAL(), NO_CHECK,
				NPE_CHECK, DUPLICATE_ID_CHECK,
				mockTemplate("template1"),
				mockTemplate("template2"),
				mockTemplate("template3"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#addTemplates(java.util.Collection)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	default void testAddTemplates() {
		assertAccumulativeAdd(
				createTestInstance(settings()),
				ManifestRegistry::addTemplates,
				NO_ILLEGAL(), NO_CHECK,
				NPE_CHECK, DUPLICATE_ID_CHECK,
				set(mockTemplate("template1"),
						mockTemplate("template2")),
				list(mockTemplate("template3"),
						mockTemplate("template4"),
						mockTemplate("template5")));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ManifestRegistry#removeTemplate(de.ims.icarus2.model.manifest.api.Manifest)}.
	 */
	@Test
	default void testRemoveTemplate() {
		assertAccumulativeRemove(
				createTestInstance(settings()),
				ManifestRegistry::addTemplate,
				ManifestRegistry::removeTemplate,
				ManifestRegistry::getTemplates,
				NPE_CHECK, UNKNOWN_ID_CHECK,
				mockTemplate("template1"),
				mockTemplate("template2"),
				mockTemplate("template3"));
	}

}
