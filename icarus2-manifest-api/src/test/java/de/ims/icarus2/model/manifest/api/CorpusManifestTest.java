/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestLocation;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestRegistry;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubId;
import static de.ims.icarus2.test.TestUtils.DEFAULT;
import static de.ims.icarus2.test.TestUtils.IDENTITY;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_ILLEGAL;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertAccumulativeGetter;
import static de.ims.icarus2.test.TestUtils.assertAccumulativeLookupContains;
import static de.ims.icarus2.test.TestUtils.assertAccumulativeOptLookup;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertNotPresent;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static de.ims.icarus2.test.TestUtils.settings;
import static de.ims.icarus2.test.TestUtils.unwrapGetter;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestFeature;
import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.CorpusManifest.Note;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.guard.ApiGuard;

/**
 * @author Markus Gärtner
 *
 */
public interface CorpusManifestTest extends MemberManifestTest<CorpusManifest> {

	public static ContextManifest mockContextManifest(String id) {
		return stubId(mockTypedManifest(ContextManifest.class), id);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.CORPUS_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.ManifestApiTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<CorpusManifest> apiGuard) {
		MemberManifestTest.super.configureApiGuard(apiGuard);

		apiGuard.defaultReturnValue("editable",
				Boolean.valueOf(CorpusManifest.DEFAULT_EDITABLE_VALUE));
		apiGuard.defaultReturnValue("parallel",
				Boolean.valueOf(CorpusManifest.DEFAULT_PARALLEL_VALUE));
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default CorpusManifest createTestInstance(TestSettings settings) {
		return createTestInstance(settings,
				mockManifestLocation(settings.hasFeature(ManifestTestFeature.TEMPLATE)),
				mockManifestRegistry());
	}

	public static <M extends CorpusManifest> BiConsumer<TestSettings, M> processor_makeParallel() {
		return (settings, corpus) -> {
			corpus.setParallel(true);
		};
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#forEachRootContextManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachRootContextManifest() {
		TestUtils.<CorpusManifest, ContextManifest>assertForEach(
				createUnlocked(settings().processor(processor_makeParallel())),
				mockContextManifest("context1"),
				mockContextManifest("context2"),
				CorpusManifest::forEachRootContextManifest,
				CorpusManifest::addRootContextManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#getRootContextManifests()}.
	 */
	@Test
	default void testGetRootContextManifests() {
		assertAccumulativeGetter(
				createUnlocked(settings().processor(processor_makeParallel())),
				mockContextManifest("context1"),
				mockContextManifest("context2"),
				CorpusManifest::getRootContextManifests,
				CorpusManifest::addRootContextManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#getRootContextManifest()}.
	 */
	@Test
	default void testGetRootContextManifest() {

		ContextManifest context1 = mockContextManifest("context1");

		ContextManifest root1 = mockContextManifest("root1");
		ContextManifest root2 = mockContextManifest("root2");

		CorpusManifest instance = createUnlocked();

		assertNotPresent(instance.getRootContextManifest());

		instance.addRootContextManifest(root1);
		assertOptionalEquals(root1, instance.getRootContextManifest());

		instance.addCustomContextManifest(context1);
		assertOptionalEquals(root1, instance.getRootContextManifest());

		instance.setParallel(true);

		instance.addRootContextManifest(root2);

		assertNotPresent(instance.getRootContextManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#isRootContext(de.ims.icarus2.model.manifest.api.ContextManifest)}.
	 */
	@Test
	default void testIsRootContext() {

		ContextManifest context1 = mockContextManifest("context1");

		ContextManifest root1 = mockContextManifest("root1");
		ContextManifest root2 = mockContextManifest("root2");

		CorpusManifest instance = createUnlocked();

		assertFalse(instance.isRootContext(root1));
		assertFalse(instance.isRootContext(root2));
		assertFalse(instance.isRootContext(context1));

		instance.addRootContextManifest(root1);
		assertTrue(instance.isRootContext(root1));

		instance.addCustomContextManifest(context1);
		assertTrue(instance.isRootContext(root1));

		instance.setParallel(true);

		instance.addRootContextManifest(root2);
		assertTrue(instance.isRootContext(root2));
		assertTrue(instance.isRootContext(root1));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#forEachCustomContextManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachCustomContextManifest() {
		TestUtils.<CorpusManifest, ContextManifest>assertForEach(
				createUnlocked(settings()),
				mockContextManifest("context1"),
				mockContextManifest("context2"),
				CorpusManifest::forEachCustomContextManifest,
				CorpusManifest::addCustomContextManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#getCustomContextManifests()}.
	 */
	@Test
	default void testGetCustomContextManifests() {
		assertAccumulativeGetter(
				createUnlocked(settings()),
				mockContextManifest("context1"),
				mockContextManifest("context2"),
				CorpusManifest::getCustomContextManifests,
				CorpusManifest::addCustomContextManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#forEachContextManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachContextManifest() {
		ContextManifest root = mockContextManifest("root");

		BiConsumer<CorpusManifest, ContextManifest> adder = (corpus, context) -> {
			if(context==root) {
				corpus.addRootContextManifest(context);
			} else {
				corpus.addCustomContextManifest(context);
			}
		};

		TestUtils.<CorpusManifest, ContextManifest>assertForEach(
				createUnlocked(settings()),
				root,
				mockContextManifest("context2"),
				CorpusManifest::forEachContextManifest,
				adder);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#isCustomContext(de.ims.icarus2.model.manifest.api.ContextManifest)}.
	 */
	@Test
	default void testIsCustomContext() {
		assertAccumulativeLookupContains(createUnlocked(),
				mockContextManifest("context1"),
				mockContextManifest("context2"),
				CorpusManifest::isCustomContext,
				NPE_CHECK,
				CorpusManifest::addCustomContextManifest,
				IDENTITY());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#getContextManifest(java.lang.String)}.
	 */
	@Test
	default void testGetContextManifest() {
		assertAccumulativeOptLookup(
				createUnlocked(),
				mockContextManifest("context1"),
				mockContextManifest("context2"),
				CorpusManifest::getContextManifest,
				NPE_CHECK,
				CorpusManifest::addCustomContextManifest,
				unwrapGetter(ContextManifest::getId),
				"layer3", "layer4");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#getLayerManifest(java.lang.String)}.
	 */
	@Test
	default void testGetLayerManifest() {

		final Map<String, LayerManifest<?>> layers = new HashMap<>();

		ContextManifest context = mockContextManifest("context");

		when(context.getLayerManifest(anyString())).thenAnswer(invocation -> {
			String id = invocation.getArgument(0);

			requireNonNull(id);

			LayerManifest<?> layer = layers.get(id);

//			if(layer==null)
//				throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID, "TEST EXCEPTION !!!");

			return Optional.ofNullable(layer);
		});

		BiConsumer<CorpusManifest, LayerManifest<?>> adder = (corpus, layer) -> {
			layers.put(layer.getId().get(), layer);
		};

		CorpusManifest instance = createUnlocked();
		instance.addRootContextManifest(context);

		TestUtils.<CorpusManifest, LayerManifest<?>, String>assertAccumulativeOptLookup(
				instance,
				LayerManifestTest.mockLayerManifest("layer1"),
				LayerManifestTest.mockLayerManifest("layer2"),
				(m, id) -> m.getLayerManifest(id).map( l -> (LayerManifest<?>)l),
				NPE_CHECK,
				adder,
				TestUtils.<LayerManifest<?>, String>unwrapGetter(LayerManifest::getId),
				"layer3", "layer4");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#isEditable()}.
	 */
	@Test
	default void testIsEditable() {
		assertGetter(createUnlocked(),
				Boolean.TRUE,
				Boolean.FALSE,
				DEFAULT(CorpusManifest.DEFAULT_EDITABLE_VALUE),
				CorpusManifest::isEditable,
				CorpusManifest::setEditable);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#isParallel()}.
	 */
	@Test
	default void testIsParallel() {
		assertGetter(createUnlocked(),
				Boolean.TRUE,
				Boolean.FALSE,
				DEFAULT(CorpusManifest.DEFAULT_PARALLEL_VALUE),
				CorpusManifest::isParallel,
				CorpusManifest::setParallel);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#forEachNote(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachNote() {
		TestUtils.<CorpusManifest, Note>assertForEach(
				createUnlocked(),
				mock(Note.class),
				mock(Note.class),
				CorpusManifest::forEachNote,
				CorpusManifest::addNote);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#getNotes()}.
	 */
	@Test
	default void testGetNotes() {
		assertAccumulativeGetter(
				createUnlocked(),
				mock(Note.class),
				mock(Note.class),
				CorpusManifest::getNotes,
				CorpusManifest::addNote);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#addRootContextManifest(de.ims.icarus2.model.manifest.api.ContextManifest)}.
	 */
	@Test
	default void testAddRootContextManifest() {
		assertLockableAccumulativeAdd(
				settings().processor(processor_makeParallel()),
				CorpusManifest::addRootContextManifest,
				NO_ILLEGAL(), NO_CHECK, NPE_CHECK, ManifestTestUtils.DUPLICATE_ID_CHECK,
				mockContextManifest("context1"),
				mockContextManifest("context2"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#removeRootContextManifest(de.ims.icarus2.model.manifest.api.ContextManifest)}.
	 */
	@Test
	default void testRemoveRootContextManifest() {
		assertLockableAccumulativeRemove(
				settings().processor(processor_makeParallel()),
				CorpusManifest::addRootContextManifest,
				CorpusManifest::removeRootContextManifest,
				CorpusManifest::getRootContextManifests,
				NPE_CHECK, ManifestTestUtils.UNKNOWN_ID_CHECK,
				mockContextManifest("context1"),
				mockContextManifest("context2"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#addCustomContextManifest(de.ims.icarus2.model.manifest.api.ContextManifest)}.
	 */
	@Test
	default void testAddCustomContextManifest() {
		assertLockableAccumulativeAdd(
				settings(),
				CorpusManifest::addCustomContextManifest,
				NO_ILLEGAL(), NO_CHECK, NPE_CHECK, ManifestTestUtils.DUPLICATE_ID_CHECK,
				mockContextManifest("context1"),
				mockContextManifest("context2"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#removeCustomContextManifest(de.ims.icarus2.model.manifest.api.ContextManifest)}.
	 */
	@Test
	default void testRemoveCustomContextManifest() {
		assertLockableAccumulativeRemove(
				settings(),
				CorpusManifest::addCustomContextManifest,
				CorpusManifest::removeCustomContextManifest,
				CorpusManifest::getCustomContextManifests,
				NPE_CHECK, ManifestTestUtils.UNKNOWN_ID_CHECK,
				mockContextManifest("context1"),
				mockContextManifest("context2"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#addNote(de.ims.icarus2.model.manifest.api.CorpusManifest.Note)}.
	 */
	@Test
	default void testAddNote() {
		assertLockableAccumulativeAdd(
				settings(),
				CorpusManifest::addNote,
				NO_ILLEGAL(), NO_CHECK, NPE_CHECK, ManifestTestUtils.DUPLICATE_ID_CHECK,
				mock(Note.class),
				mock(Note.class));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#removeNote(de.ims.icarus2.model.manifest.api.CorpusManifest.Note)}.
	 */
	@Test
	default void testRemoveNote() {
		assertLockableAccumulativeRemove(
				settings(),
				CorpusManifest::addNote,
				CorpusManifest::removeNote,
				CorpusManifest::getNotes,
				NPE_CHECK, ManifestTestUtils.UNKNOWN_ID_CHECK,
				mock(Note.class),
				mock(Note.class));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#setEditable(boolean)}.
	 */
	@Test
	default void testSetEditable() {
		assertLockableSetter(settings(), CorpusManifest::setEditable);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.CorpusManifest#setParallel(boolean)}.
	 */
	@Test
	default void testSetParallel() {
		assertLockableSetter(settings(), CorpusManifest::setParallel);
	}

}
