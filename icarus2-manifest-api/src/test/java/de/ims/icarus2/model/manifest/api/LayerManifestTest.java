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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.GenericTest.NO_DEFAULT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 *
 */
public interface LayerManifestTest<M extends LayerManifest> extends MemberManifestTest<M> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	default Set<ManifestType> getAllowedHostTypes() {
		return Collections.singleton(ManifestType.LAYER_GROUP_MANIFEST);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#getContextManifest()}.
	 */
	@Test
	default void testGetContextManifest() {
		assertNotNull(createUnlocked().getContextManifest());
		assertNull(createTemplate().getContextManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#getGroupManifest()}.
	 */
	@Test
	default void testGetGroupManifest() {
		assertNotNull(createUnlocked().getGroupManifest());
		assertNull(createTemplate().getGroupManifest());
	}

	public static LayerType mockLayerType(String id) {
		LayerType type = mock(LayerType.class);
		when(type.getId()).thenReturn(id);
		return type;
	}

	public static <M extends LayerManifest> M mockLayerManifest(Class<M> clazz, String id) {
		M manifest = mockTypedManifest(clazz);
		when(manifest.getId()).thenReturn(id);
		return manifest;
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
	public static <M extends LayerManifest> BiConsumer<M, LayerType> inject_setLayerTypeId() {
		// Ensure our mocked LayerType is available from the registry
		return (m, type) -> {
			ManifestRegistry mockedRegistry = TestUtils.assertMock(m.getRegistry());
			String id = type.getId();
			when(mockedRegistry.getLayerType(id)).thenReturn(type);

			m.setLayerTypeId(id);
		};
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#getLayerType()}.
	 */
	@Test
	default void testGetLayerType() {
		assertDerivativeGetter(mockLayerType("type1"), mockLayerType("type2"), NO_DEFAULT(),
				LayerManifest::getLayerType, inject_setLayerTypeId());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#isLocalLayerType()}.
	 */
	@Test
	default void testIsLocalLayerType() {
		assertDerivativeIsLocal(mockLayerType("type1"), mockLayerType("type2"),
				LayerManifest::isLocalLayerType, inject_setLayerTypeId());
	}

	/**
	 * Creates a wrapper around the given {@code forEachGen} that
	 * provides a {@code forEach} signature for {@code String} values instead of
	 * {@link TargetLayerManifest}. This way it can be used together with the modifier methods
	 * such as {@link LayerManifest#addBaseLayerId(String)} for testing.
	 *
	 * @return
	 */
	public static <M extends LayerManifest, C extends Consumer<String>, A extends Consumer<? super TargetLayerManifest>>
			Function<M, Consumer<C>> inject_forEachTargetLayerManifest(Function<M, Consumer<A>> forEachGen) {
		return m -> LazyCollection.<String>lazyList()
				.addFromForEach(forEachGen.apply(m), t -> t.getLayerId())
				::forEach;
	}

	/**
	 * Creates a wrapper around a {@code creator} function that produces {@link TargetLayerManifest}
	 * objects. The created {@link BiFunction function} takes a {@link String} and calls the specified
	 * {@code creator}. Subsequently the created {@link TargetLayerManifest} is tested for various
	 * consistency predicates.
	 * <p>
	 * This method expects the {@link LayerManifest} supplied to the created function to have a
	 * {@link Mockito#mock(Class) mocked} instance of {@link ContextManifest} associated with it.
	 *
	 * @param creator
	 * @return
	 */
	public static <M extends LayerManifest> BiConsumer<M, String> inject_createTargetLayerManifest(
			BiFunction<M, String, TargetLayerManifest> creator) {
		return (m, id) -> {
			LayerManifest target = mock(LayerManifest.class);
			ContextManifest contextManifest = TestUtils.assertMock(m.getContextManifest());

			when(contextManifest.getLayerManifest(id)).thenReturn(target);

			TargetLayerManifest targetLayerManifest = creator.apply(m, id);
			assertNotNull(targetLayerManifest);
			assertEquals(id, targetLayerManifest.getLayerId());
			assertSame(m, targetLayerManifest.getLayerManifest());
			assertSame(m, targetLayerManifest.getHost());

			assertSame(target, targetLayerManifest.getResolvedLayerManifest());
		};
	}

	public static <M extends LayerManifest, K extends Object, T extends Object> BiConsumer<M, K> inject_genericSetter(
			BiConsumer<M, T> setter, Function<K, T> transform) {
		return (m, val) -> {
			setter.accept(m, transform.apply(val));
		};
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#forEachBaseLayerManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachBaseLayerManifest() {
		assertDerivativeForEach("layer1", "layer2",
				inject_forEachTargetLayerManifest(m -> m::forEachBaseLayerManifest),
				inject_createTargetLayerManifest(LayerManifest::addBaseLayerId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#forEachLocalBaseLayerManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalBaseLayerManifest() {
		assertDerivativeForEachLocal("layer1", "layer2",
				inject_forEachTargetLayerManifest(m -> m::forEachLocalBaseLayerManifest),
				inject_createTargetLayerManifest(LayerManifest::addBaseLayerId));
	}

	/**
	 * Creates a wrapper around a generic getter method that returns a collection
	 * and transforms the result based on the specified {@code transform} function.
	 *
	 * @return
	 */
	public static <M extends LayerManifest, T extends Object, K extends Object> Function<M, List<K>> transform_genericCollectionGetter(
			Function<M, ? extends Collection<T>> getter, Function<T, K> transform) {
		return m -> {
			return LazyCollection.<K>lazyList()
					.addAll(getter.apply(m), transform)
					.getAsList();
		};
	}

	public static <M extends LayerManifest, T extends Object, K extends Object> Function<M, K> transform_genericValue(
			Function<M, T> getter, Function<T, K> transform) {
		return m -> {
			return transform.apply(getter.apply(m));
		};
	}

	/**
	 * Helper function to be used for consistency.
	 * Transforms a {@link TargetLayerManifest} into a {@link String} by using
	 * the manifest's {@link TargetLayerManifest#getLayerId() layer id}.
	 */
	public static Function<TargetLayerManifest, String> transform_targetLayerId() {
		return t -> t.getLayerId();
	}

	/**
	 * Helper function to be used for consistency.
	 * Transforms a {@link LayerManifest} into a {@link String} by using
	 * the manifest's {@link LayerManifest#getId() id}.
	 */
	public static <M extends LayerManifest> Function<M, String> transform_layerManifestId(){
		return m -> m.getId();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#getBaseLayerManifests()}.
	 */
	@Test
	default void testGetBaseLayerManifests() {
		assertDerivativeAccumulativeGetter("layer1", "layer2",
				transform_genericCollectionGetter(LayerManifest::getBaseLayerManifests, transform_targetLayerId()),
				inject_createTargetLayerManifest(LayerManifest::addBaseLayerId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#getLocalBaseLayerManifests()}.
	 */
	@Test
	default void testGetLocalBaseLayerManifests() {
		assertDerivativeAccumulativeGetter("layer1", "layer2",
				transform_genericCollectionGetter(LayerManifest::getLocalBaseLayerManifests, transform_targetLayerId()),
				inject_createTargetLayerManifest(LayerManifest::addBaseLayerId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#setLayerTypeId(java.lang.String)}.
	 */
	@Test
	default void testSetLayerTypeId() {
		assertLockableSetter(LayerManifest::setLayerTypeId, ManifestTestUtils.getLegalIdValues(),
				true, ILLEGAL_ID_CHECK, ManifestTestUtils.getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#addBaseLayerId(java.lang.String)}.
	 */
	@Test
	default void testAddBaseLayerId() {
		assertLockableAccumulativeAdd(
				inject_createTargetLayerManifest(LayerManifest::addBaseLayerId),
				ManifestTestUtils.getIllegalIdValues(), ILLEGAL_ID_CHECK,
				true, true, ManifestTestUtils.getLegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LayerManifest#removeBaseLayerId(java.lang.String)}.
	 */
	@Test
	default void testRemoveBaseLayerId() {
		assertLockableAccumulativeRemove(LayerManifest::addBaseLayerId,
				LayerManifest::removeBaseLayerId,
				transform_genericCollectionGetter(LayerManifest::getBaseLayerManifests, transform_targetLayerId()), true, true,
				"layer1", "layer2", "layer3");
	}

}
