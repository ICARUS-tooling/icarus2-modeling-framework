/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubId;
import static de.ims.icarus2.test.TestUtils.assertMock;
import static de.ims.icarus2.test.TestUtils.assertNotPresent;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static de.ims.icarus2.test.TestUtils.assertPresent;
import static de.ims.icarus2.test.TestUtils.settings;
import static de.ims.icarus2.test.TestUtils.wrapForEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.func.TriConsumer;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 *
 */
public interface LayerManifestTest<M extends LayerManifest<?>> extends EmbeddedMemberManifestTest<M> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	default Set<ManifestType> getAllowedHostTypes() {
		return Collections.singleton(ManifestType.LAYER_GROUP_MANIFEST);
	}

	/**
	 *
	 * @see de.ims.icarus2.model.manifest.api.MemberManifestTest#createMockedHost(de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry, de.ims.icarus2.model.manifest.api.ManifestType)
	 */
	@Provider
	@Override
	default TypedManifest createMockedHost(ManifestLocation location, ManifestRegistry registry, ManifestType preferredType) {
		assertEquals(ManifestType.LAYER_GROUP_MANIFEST, preferredType);

		LayerGroupManifest manifest = mockTypedManifest(preferredType, true);

		// Additional handling: ensure the proxy methods around getHost() return valid data
		doCallRealMethod().when(manifest).getContextManifest();

		return manifest;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#getContextManifest()}.
	 */
	@Test
	default void testGetContextManifest() {
		assertPresent(createUnlocked().getContextManifest());
		assertNotPresent(createTemplate(settings()).getContextManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#getGroupManifest()}.
	 */
	@Test
	default void testGetGroupManifest() {
		assertPresent(createUnlocked().getGroupManifest());
		assertNotPresent(createTemplate(settings()).getGroupManifest());
	}

	public static LayerType mockLayerType(String id) {
		LayerType type = mock(LayerType.class);
		when(type.getId()).thenReturn(Optional.of(id));
		return type;
	}

	public static <M extends LayerManifest<?>> M mockLayerManifest(Class<M> clazz, String id) {
		return stubId(mockTypedManifest(clazz), id);
	}

	@SuppressWarnings("unchecked")
	public static <M extends LayerManifest<M>> M mockLayerManifest(String id) {
		return (M) mockLayerManifest(LayerManifest.class, id);
	}

	@SuppressWarnings("unchecked")
	public static <M extends ItemLayerManifest> M mockItemLayerManifest(String id) {
		return (M) mockLayerManifest(ItemLayerManifest.class, id);
	}

	@SuppressWarnings("unchecked")
	public static <M extends StructureLayerManifest> M mockStructureLayerManifest(String id) {
		return (M) mockLayerManifest(StructureLayerManifest.class, id);
	}

	@SuppressWarnings("unchecked")
	public static <M extends FragmentLayerManifest> M mockFragmentLayerManifest(String id) {
		return (M) mockLayerManifest(FragmentLayerManifest.class, id);
	}

	@SuppressWarnings("unchecked")
	public static <M extends AnnotationLayerManifest> M mockAnnotationLayerManifest(String id) {
		return (M) mockLayerManifest(AnnotationLayerManifest.class, id);
	}

	@SuppressWarnings("unchecked")
	public static <M extends HighlightLayerManifest> M mockHighlightLayerManifest(String id) {
		return (M) mockLayerManifest(HighlightLayerManifest.class, id);
	}

	/**
	 * Creates a wrapper around {@link LayerManifest#setLayerTypeId(String)} to turn it into
	 * a {@link BiConsumer} that takes a {@link LayerManifest} and {@link LayerType} and
	 * internally accessing the underlying mocked {@link ManifestRegistry} to ensure the
	 * given {@link LayerType} is {@link ManifestRegistry#getLayerType(String) available}.
	 *
	 * @return
	 */
	public static <M extends LayerManifest<?>> BiConsumer<M, LayerType> inject_setLayerTypeId() {
		// Ensure our mocked LayerType is available from the registry
		return (m, type) -> {
			ManifestRegistry mockedRegistry = TestUtils.assertMock(m.getRegistry());
			String id = type.getId().get();
			when(mockedRegistry.getLayerType(id)).thenReturn(Optional.of(type));

			m.setLayerTypeId(id);
		};
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#getLayerType()}.
	 */
	@Test
	default void testGetLayerType() {
		assertDerivativeOptGetter(settings(),
				mockLayerType("type1"), mockLayerType("type2"),
				TestUtils.NO_DEFAULT(),
				LayerManifest::getLayerType, inject_setLayerTypeId());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#isLocalLayerType()}.
	 */
	@Test
	default void testIsLocalLayerType() {
		assertDerivativeIsLocal(settings(),
				mockLayerType("type1"), mockLayerType("type2"),
				LayerManifest::isLocalLayerType, inject_setLayerTypeId());
	}

	/**
	 * Creates a wrapper around the given {@code forEachGen} that
	 * provides a {@code forEach} signature for {@code String} values instead of
	 * {@link TargetLayerManifest}. This way it can be used together with the modifier methods
	 * such as {@link LayerManifest#addAndGetBaseLayer(String)} for testing.
	 *
	 * @return
	 */
	public static <M extends LayerManifest<?>> BiConsumer<M, Consumer<? super String>> inject_forEachTargetLayerManifest(
					BiConsumer<M, Consumer<? super TargetLayerManifest>> forEach) {
		return (m, action) -> LazyCollection.<String>lazyList()
				.<TargetLayerManifest, Consumer<? super TargetLayerManifest>>addFromForEachTransformed(wrapForEach(m, forEach), t -> t.getLayerId())
				.forEach(action);
	}

	/**
	 * Creates a wrapper around a {@code creator} function that produces {@link TargetLayerManifest}
	 * objects. The created {@link BiFunction function} takes a {@link String} and calls the specified
	 * {@code creator}. Subsequently the created {@link TargetLayerManifest} is tested for various
	 * consistency predicates.
	 *
	 * @param creator
	 * @return
	 */
	public static <M extends LayerManifest<?>> BiConsumer<M, String> inject_createTargetLayerManifest(
			BiFunction<M, String, TargetLayerManifest> creator) {
		return (m, id) -> {
			TargetLayerManifest targetLayerManifest = creator.apply(m, id);
			assertNotNull(targetLayerManifest);
			assertEquals(id, targetLayerManifest.getLayerId());
			assertSame(m, targetLayerManifest.getHostManifest());
			assertOptionalEquals(m, targetLayerManifest.getHost());
		};
	}

	/**
	 * Creates a wrapper around a {@code creator} function that produces {@link TargetLayerManifest}
	 * objects and passes them to a {@link Consumer}. The created {@link BiFunction function} takes
	 * a {@link String} and calls the specified {@code creator} with a local consumer to fetch the
	 * result. Subsequently the {@link TargetLayerManifest} obtained this way is tested for
	 * various consistency predicates.
	 * <p>
	 * This method expects the {@link LayerManifest} supplied to the created function to have a
	 * {@link Mockito#mock(Class) mocked} instance of {@link ContextManifest} associated with it.
	 *
	 * @param creator
	 * @return
	 */
	public static <M extends LayerManifest<?>> BiConsumer<M, String> inject_consumeTargetLayerManifest(
			TriConsumer<M, String, Consumer<? super TargetLayerManifest>> creator) {
		return (m, id) -> {
			TargetLayerManifest targetLayerManifest =
					IcarusUtils.extractSupplied(action -> creator.accept(m, id, action));
			assertNotNull(targetLayerManifest);
			assertEquals(id, targetLayerManifest.getLayerId());
			assertSame(m, targetLayerManifest.getHostManifest());
			assertOptionalEquals(m, targetLayerManifest.getHost());
		};
	}

	@SuppressWarnings("unchecked")
	public static <M extends Object, L extends LayerManifest<?>> BiConsumer<M, L> inject_setLayerId(
			BiConsumer<M, String> setter, Function<M, Optional<ContextManifest>> contextGetter) {
		return (m, layerManifest) -> {

			String id = layerManifest.getId().get();
			assertNotNull(id);

			ContextManifest contextManifest = assertMock(contextGetter.apply(m));
			when((Optional<L>)contextManifest.getLayerManifest(id)).thenReturn(Optional.of(layerManifest));

			setter.accept(m, id);
		};
	}

	@SuppressWarnings("unchecked")
	public static <M extends Object, L extends LayerManifest<?>> BiConsumer<M, L> inject_layerLookup(
			BiConsumer<M, L> setter, Function<M, Optional<ContextManifest>> contextGetter) {
		return (m, layerManifest) -> {

			String id = layerManifest.getId().get();
			assertNotNull(id);

			ContextManifest contextManifest = assertMock(contextGetter.apply(m));
			when((Optional<L>)contextManifest.getLayerManifest(id)).thenReturn(Optional.of(layerManifest));

			setter.accept(m, layerManifest);
		};
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#forEachBaseLayerManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachBaseLayerManifest() {
		assertDerivativeForEach(settings(),
				"layer1", "layer2",
				inject_forEachTargetLayerManifest(LayerManifest::forEachBaseLayerManifest),
				inject_createTargetLayerManifest(LayerManifest::addAndGetBaseLayer));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#forEachLocalBaseLayerManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalBaseLayerManifest() {
		assertDerivativeForEachLocal(settings(),
				"layer1", "layer2",
				inject_forEachTargetLayerManifest(LayerManifest::forEachLocalBaseLayerManifest),
				inject_createTargetLayerManifest(LayerManifest::addAndGetBaseLayer));
	}

	/**
	 * Helper function to be used for consistency.
	 * Transforms a {@link TargetLayerManifest} into a {@link String} by using
	 * the manifest's {@link TargetLayerManifest#getLayerId() layer id}.
	 */
	public static Function<TargetLayerManifest, String> transform_targetLayerId() {
		return t -> t==null ? null : t.getLayerId();
	}

	/**
	 * Helper function to be used for consistency.
	 * Transforms a {@link LayerManifest} into a {@link String} by using
	 * the manifest's {@link LayerManifest#getId() id}.
	 */
	public static <M extends LayerManifest<?>> Function<M, String> transform_layerManifestId(){
		return m -> m==null ? null : m.getId().get();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#getBaseLayerManifests()}.
	 */
	@Test
	default void testGetBaseLayerManifests() {
		assertDerivativeAccumulativeGetter(settings(),
				"layer1", "layer2",
				TestUtils.transform_genericCollectionGetter(LayerManifest::getBaseLayerManifests, transform_targetLayerId()),
				inject_createTargetLayerManifest(LayerManifest::addAndGetBaseLayer));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#getLocalBaseLayerManifests()}.
	 */
	@Test
	default void testGetLocalBaseLayerManifests() {
		assertDerivativeAccumulativeLocalGetter(settings(),
				"layer1", "layer2",
				TestUtils.transform_genericCollectionGetter(LayerManifest::getLocalBaseLayerManifests, transform_targetLayerId()),
				inject_createTargetLayerManifest(LayerManifest::addAndGetBaseLayer));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#setLayerTypeId(java.lang.String)}.
	 */
	@Test
	default void testSetLayerTypeId() {
		assertLockableSetterBatch(settings(),
				LayerManifest::setLayerTypeId, ManifestTestUtils.getLegalIdValues(),
				true, ManifestTestUtils.INVALID_ID_CHECK, ManifestTestUtils.getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#addAndGetBaseLayer(java.lang.String)}.
	 */
	@Test
	default void testAddAndGetBaseLayer() {
		assertLockableAccumulativeAdd(settings(),
				LayerManifest::addAndGetBaseLayer,
				ManifestTestUtils.getIllegalIdValues(), ManifestTestUtils.INVALID_ID_CHECK,
				true, ManifestTestUtils.DUPLICATE_ID_CHECK, ManifestTestUtils.getLegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#addBaseLayerId(String, Consumer)}.
	 */
	@Test
	default void testAddBaseLayerId() {
		assertLockableAccumulativeAdd(settings(),
				inject_consumeTargetLayerManifest(LayerManifest::addBaseLayerId),
				ManifestTestUtils.getIllegalIdValues(), ManifestTestUtils.INVALID_ID_CHECK,
				true, ManifestTestUtils.DUPLICATE_ID_CHECK, ManifestTestUtils.getLegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#removeBaseLayerId(java.lang.String)}.
	 */
	@Test
	default void testRemoveBaseLayerId() {
		assertLockableAccumulativeRemove(
				settings(),LayerManifest::addAndGetBaseLayer,
				LayerManifest::removeBaseLayerId,
				TestUtils.transform_genericCollectionGetter(LayerManifest::getBaseLayerManifests, transform_targetLayerId()),
				true, ManifestTestUtils.UNKNOWN_ID_CHECK,
				"layer1", "layer2", "layer3");
	}

}
