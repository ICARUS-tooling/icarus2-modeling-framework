/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertManifestException;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubIdentity;
import static de.ims.icarus2.test.TestUtils.DEFAULT;
import static de.ims.icarus2.test.TestUtils.IGNORE_DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_ILLEGAL;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertAccumulativeFilter;
import static de.ims.icarus2.test.TestUtils.assertAccumulativeGetter;
import static de.ims.icarus2.test.TestUtils.assertCollectionNotEmpty;
import static de.ims.icarus2.test.TestUtils.assertMock;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static de.ims.icarus2.test.TestUtils.assertPresent;
import static de.ims.icarus2.test.TestUtils.settings;
import static de.ims.icarus2.test.TestUtils.transform_genericCollectionGetter;
import static de.ims.icarus2.test.TestUtils.wrapForEach;
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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.model.manifest.api.binding.BindableTest;
import de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.func.TriConsumer;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.Multiplicity;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 *
 */
public interface ContextManifestTest extends EmbeddedMemberManifestTest<ContextManifest>, BindableTest<ContextManifest> {

	public static PrerequisiteManifest mockPrerequisiteManifest(String alias) {
		PrerequisiteManifest manifest = mock(PrerequisiteManifest.class);
		when(manifest.getAlias()).thenReturn(alias);
		return manifest;
	}

	public static LocationManifest mockLocationManifest() {
		return mockTypedManifest(LocationManifest.class);
	}

	public static LayerGroupManifest mockGroupManifest(String id) {
		return stubIdentity(mockTypedManifest(ManifestType.LAYER_GROUP_MANIFEST), id);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.binding.BindableTest#getSupportedBindingMultiplicities()
	 */
	@Override
	default Set<Multiplicity> getSupportedBindingMultiplicities() {
		return Collections.singleton(Multiplicity.ONE);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.binding.BindableTest#createWithBindingEndpoints(de.ims.icarus2.test.TestSettings, java.util.Set)
	 */
	@Override
	default ContextManifest createWithBindingEndpoints(TestSettings settings,
			Set<LayerPrerequisite> bindingEndpoints) {
		assertCollectionNotEmpty(bindingEndpoints);

		ContextManifest manifest = createUnlocked(settings);

		for(LayerPrerequisite binding : bindingEndpoints) {
			assertTrue(binding.getMultiplicity()==Multiplicity.ONE);

			PrerequisiteManifest prerequisite = manifest.addAndGetPrerequisite(binding.getAlias());

			assertEquals(binding.getAlias(), prerequisite.getAlias());

			Supplier<AssertionError> exGen = AssertionError::new;

			prerequisite.setContextId(binding.getContextId().orElseThrow(exGen));
			prerequisite.setLayerId(binding.getLayerId().orElseThrow(exGen));
			prerequisite.setDescription(binding.getDescription().orElseThrow(exGen));
			prerequisite.setTypeId(binding.getTypeId().orElseThrow(exGen));
		}

		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.CONTEXT_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	default Set<ManifestType> getAllowedHostTypes() {
		return set(ManifestType.CORPUS_MANIFEST);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#getCorpusManifest()}.
	 */
	@Test
	default void testGetCorpusManifest() {
		assertHostGetter(ContextManifest::getCorpusManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#getDriverManifest()}.
	 */
	@Test
	default void testGetDriverManifest() {
		assertDerivativeOptGetter(settings(),
				mockTypedManifest(ManifestType.DRIVER_MANIFEST),
				mockTypedManifest(ManifestType.DRIVER_MANIFEST),
				NO_DEFAULT(),
				ContextManifest::getDriverManifest,
				ContextManifest::setDriverManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#isLocalDriverManifest()}.
	 */
	@Test
	default void testIsLocalDriverManifest() {
		assertDerivativeIsLocal(settings(),
				mockTypedManifest(ManifestType.DRIVER_MANIFEST),
				mockTypedManifest(ManifestType.DRIVER_MANIFEST),
				ContextManifest::isLocalDriverManifest,
				ContextManifest::setDriverManifest);
	}

	/**
	 * Creates a wrapper around the given {@code forEach} that
	 * provides a {@code forEach} signature for {@code String} values instead of
	 * {@link PrerequisiteManifest}. This way it can be used together with the modifier methods
	 * such as {@link ContextManifest#addAndGetPrerequisite(String)} for testing.
	 *
	 * @return
	 */
	public static <M extends ContextManifest>
			BiConsumer<M, Consumer<? super String>> inject_forEachPrerequisiteManifest(
					BiConsumer<M, Consumer<? super PrerequisiteManifest>> forEach) {
		return (m, action) -> LazyCollection.<String>lazyList()
				.addFromForEachTransformed(wrapForEach(m, forEach), t -> t.getAlias())
				.forEach(action);
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
	public static <M extends ContextManifest> BiConsumer<M, String> inject_createPrerequisiteManifest(
			TriConsumer<M, String, Consumer<? super PrerequisiteManifest>> creator) {
		return (m, alias) -> {
			PrerequisiteManifest prerequisiteManifest =
					IcarusUtils.extractSupplied(action -> creator.accept(m,  alias, action));
			assertNotNull(prerequisiteManifest);
			assertEquals(alias, prerequisiteManifest.getAlias());
			assertSame(m, prerequisiteManifest.getContextManifest());
		};
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#forEachPrerequisite(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachPrerequisite() {
		assertDerivativeForEach(settings(),
				"alias1", "alias2",
				inject_forEachPrerequisiteManifest(ContextManifest::forEachPrerequisite),
				inject_createPrerequisiteManifest(ContextManifest::addPrerequisite));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#forEachLocalPrerequisite(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalPrerequisite() {
		assertDerivativeForEachLocal(settings(),
				"alias1", "alias2",
				inject_forEachPrerequisiteManifest(ContextManifest::forEachLocalPrerequisite),
				inject_createPrerequisiteManifest(ContextManifest::addPrerequisite));
	}

	/**
	 * Helper function to be used for consistency.
	 * Transforms a {@link PrerequisiteManifest} into a {@link String} by using
	 * the manifest's {@link PrerequisiteManifest#getAlias() alias}.
	 */
	public static Function<PrerequisiteManifest, String> transform_prerequisiteAlias() {
		return t -> t==null ? null : t.getAlias();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#getPrerequisites()}.
	 */
	@Test
	default void testGetPrerequisites() {
		assertDerivativeAccumulativeGetter(settings(),
				"alias1", "alias2",
				TestUtils.transform_genericCollectionGetter(ContextManifest::getPrerequisites, transform_prerequisiteAlias()),
				inject_createPrerequisiteManifest(ContextManifest::addPrerequisite));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#getLocalPrerequisites()}.
	 */
	@Test
	default void testGetLocalPrerequisites() {
		assertDerivativeAccumulativeLocalGetter(settings(),
				"alias1", "alias2",
				TestUtils.transform_genericCollectionGetter(ContextManifest::getLocalPrerequisites, transform_prerequisiteAlias()),
				inject_createPrerequisiteManifest(ContextManifest::addPrerequisite));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#getPrerequisite(java.lang.String)}.
	 */
	@Test
	default void testGetPrerequisite() {

		BiFunction<ContextManifest, String, Optional<String>> lookup = (context, alias) -> {
			Optional<PrerequisiteManifest> prerequisite = context.getPrerequisite(alias);

			if(prerequisite.isPresent()) {
				assertOptionalEquals(alias, prerequisite.map(PrerequisiteManifest::getAlias));
			}

			return prerequisite.map(PrerequisiteManifest::getAlias);
		};

		assertDerivativeAccumulativeOptLookup(
				settings(),
				"alias1", "alias2",
				lookup,
				NPE_CHECK,
				ContextManifest::addAndGetPrerequisite,
				TestUtils.<String>IDENTITY(),
				ManifestTestUtils.getIllegalIdValues());
	}

	/**
	 * Creates a {@link TestSettings#processor(BiConsumer) processor} usable to initialize
	 * {@link ContextManifest} instances with a stubbed {@link LayerGroupManifest} that does nothing
	 * besides forwarding layer related calls for adding and traversal to an external {@link List list}.
	 *
	 * @return
	 */
	public static <M extends ContextManifest> BiConsumer<TestSettings, M> processor_stubLayerGroup() {
		return (settings, manifest) -> {
			LayerGroupManifest layerGroup = mock(LayerGroupManifest.class);
			when(layerGroup.getHost()).thenReturn(Optional.of(manifest));

			final List<LayerManifest<?>> layers = new ArrayList<>();

			// Intercept adding of new layers
			doAnswer((Answer<Void>) invocation -> {
				layers.add(invocation.getArgument(0));
				return null;
			}).when(layerGroup).addLayerManifest(any());

			// Intercept for-each traversal of layers
			doAnswer((Answer<Void>) invocation -> {
				layers.forEach(invocation.getArgument(0));
				return null;
			}).when(layerGroup).forEachLayerManifest(any());

			// Make sure the real context uses the stubbed group!
			manifest.addLayerGroup(layerGroup);
		};
	}

	/**
	 * Creates a setter proxy for {@link LayerManifest} instances that accesses
	 * the {@link LayerGroupManifest layer group} associated with the given
	 * {@code groupIndex} and delegates to its {@link LayerGroupManifest#addLayerManifest(LayerManifest)}
	 * method after {@link TestUtils#assertMock(Object) verifying} that it is a mock object.
	 *
	 * @param groupIndex
	 * @return
	 */
	public static <M extends ContextManifest> BiConsumer<M, LayerManifest<?>> inject_addLayerManifest(int groupIndex) {
		return (context, layer) -> {
			assertPresent(layer.getId());

			// Make sure we operate on our own mock and then add the layer
			LayerGroupManifest groupManifest = assertMock(context.getLocalGroupManifests().get(groupIndex));

			groupManifest.addLayerManifest(layer);
			when((Optional<LayerManifest<?>>)groupManifest.getLayerManifest(layer.getId().get()))
					.thenReturn(Optional.of(layer));
		};
	}

	/**
	 * Index of the default group to operate on for testing layer related methods.
	 */
	public static final int DEFAULT_GROUP = 0;

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#forEachLayerManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLayerManifest() {
		EmbeddedMemberManifestTest.super.<LayerManifest<?>>assertDerivativeForEach(
				settings().processor(processor_stubLayerGroup()),
				LayerManifestTest.mockLayerManifest("layer1"),
				LayerManifestTest.mockLayerManifest("layer2"),
				ContextManifest::forEachLayerManifest,
				inject_addLayerManifest(DEFAULT_GROUP));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#forEachLocalLayerManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalLayerManifest() {
		EmbeddedMemberManifestTest.super.<LayerManifest<?>>assertDerivativeForEachLocal(
				settings().processor(processor_stubLayerGroup()),
				LayerManifestTest.mockLayerManifest("layer1"),
				LayerManifestTest.mockLayerManifest("layer2"),
				ContextManifest::forEachLocalLayerManifest,
				inject_addLayerManifest(DEFAULT_GROUP));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#getLayerManifests()}.
	 */
	@Test
	default void testGetLayerManifests() {
		this.<LayerManifest<?>>assertDerivativeAccumulativeGetter(
				settings().processor(processor_stubLayerGroup()),
				LayerManifestTest.mockLayerManifest("layer1"),
				LayerManifestTest.mockLayerManifest("layer2"),
				ContextManifest::getLayerManifests,
				inject_addLayerManifest(DEFAULT_GROUP));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#getLocalLayerManifests()}.
	 */
	@Test
	default void testGetLocalLayerManifests() {
		this.<LayerManifest<?>>assertDerivativeAccumulativeLocalGetter(
				settings().processor(processor_stubLayerGroup()),
				LayerManifestTest.mockLayerManifest("layer1"),
				LayerManifestTest.mockLayerManifest("layer2"),
				ContextManifest::getLocalLayerManifests,
				inject_addLayerManifest(DEFAULT_GROUP));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#getLayerManifests(java.util.function.Predicate)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	default void testGetLayerManifestsPredicateOfQsuperLayerManifest() {

		Predicate<LayerManifest<?>> all = m -> true;
		Predicate<LayerManifest<?>> none = m -> true;
		Predicate<LayerManifest<?>> onlyLayer2 = m -> "layer2".equals(m.getId().orElse(null));

		for(Predicate<LayerManifest<?>> filter : list(all, none, onlyLayer2)) {
			assertAccumulativeFilter(
					settings().processor(processor_stubLayerGroup()).process(createUnlocked()),
					inject_addLayerManifest(DEFAULT_GROUP),
					ContextManifest::getLayerManifests,
					filter,
					LayerManifestTest.mockLayerManifest("layer1"),
					LayerManifestTest.mockLayerManifest("layer2"),
					LayerManifestTest.mockLayerManifest("layer3"));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#forEachGroupManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachGroupManifest() {
		EmbeddedMemberManifestTest.super.<LayerGroupManifest>assertDerivativeForEach(
				settings(),
				mockGroupManifest("group1"),
				mockGroupManifest("group2"),
				ContextManifest::forEachGroupManifest,
				ContextManifest::addLayerGroup);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#forEachLocalGroupManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalGroupManifest() {
		EmbeddedMemberManifestTest.super.<LayerGroupManifest>assertDerivativeForEachLocal(
				settings(),
				mockGroupManifest("group1"),
				mockGroupManifest("group2"),
				ContextManifest::forEachLocalGroupManifest,
				ContextManifest::addLayerGroup);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#getGroupManifests()}.
	 */
	@Test
	default void testGetGroupManifests() {
		assertDerivativeAccumulativeGetter(settings(),
				mockGroupManifest("group1"),
				mockGroupManifest("group2"),
				ContextManifest::getGroupManifests,
				ContextManifest::addLayerGroup);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#getLocalGroupManifests()}.
	 */
	@Test
	default void testGetLocalGroupManifests() {
		assertDerivativeAccumulativeLocalGetter(settings(),
				mockGroupManifest("group1"),
				mockGroupManifest("group2"),
				ContextManifest::getLocalGroupManifests,
				ContextManifest::addLayerGroup);
	}

	/**
	 * Creates a setter proxy for {@link LayerManifest} instances that follows the contract of
	 * {@link #inject_addLayerManifest(int)} and then uses the specified {@code idSetter} on the
	 * active {@link ContextManifest} with the new layers {@link LayerManifest#getId() id}.
	 *
	 * @param groupIndex
	 * @return
	 * @see #inject_addLayerManifest(int)
	 */
	public static <M extends ContextManifest, L extends LayerManifest<?>> BiConsumer<M, L>
			inject_addLayerManifestAndSetId(
					int groupIndex, BiConsumer<M, String> idSetter) {
		final BiConsumer<M, LayerManifest<?>> adder = inject_addLayerManifest(groupIndex);

		return (context, layer) -> {
			adder.accept(context, layer);
			idSetter.accept(context, layer.getId().get());
		};
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#getPrimaryLayerManifest()}.
	 */
	@Test
	default void testGetPrimaryLayerManifest() {
		assertDerivativeOptGetter(settings().processor(processor_stubLayerGroup()),
				LayerManifestTest.mockItemLayerManifest("layer1"),
				LayerManifestTest.mockItemLayerManifest("layer2"),
				IGNORE_DEFAULT(),
				c -> c.getPrimaryLayerManifest(),
				inject_addLayerManifestAndSetId(DEFAULT_GROUP, ContextManifest::setPrimaryLayerId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#isLocalPrimaryLayerManifest()}.
	 */
	@Test
	default void testIsLocalPrimaryLayerManifest() {
		assertDerivativeIsLocal(settings().processor(processor_stubLayerGroup()),
				LayerManifestTest.mockItemLayerManifest("layer1"),
				LayerManifestTest.mockItemLayerManifest("layer1"),
				ContextManifest::isLocalPrimaryLayerManifest,
				inject_addLayerManifestAndSetId(DEFAULT_GROUP, ContextManifest::setPrimaryLayerId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#getFoundationLayerManifest()}.
	 */
	@Test
	default void testGetFoundationLayerManifest() {
		assertDerivativeOptGetter(settings().processor(processor_stubLayerGroup()),
				LayerManifestTest.mockItemLayerManifest("layer1"),
				LayerManifestTest.mockItemLayerManifest("layer2"),
				IGNORE_DEFAULT(),
				ContextManifest::getFoundationLayerManifest,
				inject_addLayerManifestAndSetId(DEFAULT_GROUP, ContextManifest::setFoundationLayerId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#isLocalFoundationLayerManifest()}.
	 */
	@Test
	default void testIsLocalFoundationLayerManifest() {
		assertDerivativeIsLocal(settings().processor(processor_stubLayerGroup()),
				LayerManifestTest.mockItemLayerManifest("layer1"),
				LayerManifestTest.mockItemLayerManifest("layer1"),
				ContextManifest::isLocalFoundationLayerManifest,
				inject_addLayerManifestAndSetId(DEFAULT_GROUP, ContextManifest::setFoundationLayerId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#getLayerManifest(java.lang.String)}.
	 */
	@Test
	default void testGetLayerManifest() {
		this.<LayerManifest<?>, String>assertDerivativeAccumulativeOptLookup(
				settings().processor(processor_stubLayerGroup()),
				LayerManifestTest.mockLayerManifest("layer1"),
				LayerManifestTest.mockLayerManifest("layer2"),
				(m, id) -> m.getLayerManifest(id).map(l -> (LayerManifest<?>)l),
				NPE_CHECK,
				inject_addLayerManifest(DEFAULT_GROUP),
				TestUtils.<LayerManifest<?>, String>unwrapGetter(LayerManifest::getId),
				"layer3", "layer4");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#getLocationManifests()}.
	 */
	@Test
	default void testGetLocationManifests() {
		//ATTENTION: getLocationManifests() is not a derivative method
		assertAccumulativeGetter(
				createUnlocked(),
				mockLocationManifest(),
				mockLocationManifest(),
				ContextManifest::getLocationManifests,
				ContextManifest::addLocationManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#isIndependentContext()}.
	 */
	@Test
	default void testIsIndependentContext() {
		assertDerivativeGetter(settings(),
				Boolean.TRUE, Boolean.FALSE,
				DEFAULT(Boolean.valueOf(ContextManifest.DEFAULT_INDEPENDENT_VALUE)),
				ContextManifest::isIndependentContext,
				ContextManifest::setIndependentContext);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#isLocalIndependentContext()}.
	 */
	@Test
	default void testIsLocalIndependentContext() {
		assertDerivativeIsLocal(settings(),
				Boolean.TRUE, Boolean.FALSE,
				ContextManifest::isLocalIndependentContext,
				ContextManifest::setIndependentContext);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#isRootContext()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testIsRootContext() {
		assertFalse(createUnlocked().isRootContext());

		CorpusManifest corpusManifest = mockTypedManifest(ManifestType.CORPUS_MANIFEST);

		ContextManifest instance = createEmbedded(settings(), corpusManifest);

		assertFalse(instance.isRootContext());

		when(corpusManifest.isRootContext(instance)).thenReturn(Boolean.TRUE);

		assertTrue(instance.isRootContext());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#isEditable()}.
	 */
	@Test
	default void testIsEditable() {
		assertDerivativeGetter(settings(),
				Boolean.TRUE, Boolean.FALSE,
				DEFAULT(Boolean.valueOf(ContextManifest.DEFAULT_EDITABLE_VALUE)),
				ContextManifest::isEditable,
				ContextManifest::setEditable);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#isLocalEditable()}.
	 */
	@Test
	default void testIsLocalEditable() {
		assertDerivativeIsLocal(settings(),
				Boolean.TRUE, Boolean.FALSE,
				ContextManifest::isLocalEditable,
				ContextManifest::setEditable);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#setEditable(boolean)}.
	 */
	@Test
	default void testSetEditable() {
		assertLockableSetter(settings(), ContextManifest::setEditable);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#setDriverManifest(de.ims.icarus2.model.manifest.api.DriverManifest)}.
	 */
	@Test
	default void testSetDriverManifest() {
		assertLockableSetter(settings(),
				ContextManifest::setDriverManifest,
				mockTypedManifest(ManifestType.DRIVER_MANIFEST),
				NPE_CHECK, NO_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#setPrimaryLayerId(java.lang.String)}.
	 */
	@Test
	default void testSetPrimaryLayerId() {
		assertLockableSetter(settings(),
				ContextManifest::setPrimaryLayerId,
				"layer1", NPE_CHECK, INVALID_ID_CHECK,
				ManifestTestUtils.getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#setFoundationLayerId(java.lang.String)}.
	 */
	@Test
	default void testSetFoundationLayerId() {
		assertLockableSetter(settings(),
				ContextManifest::setFoundationLayerId,
				"layer1", NPE_CHECK, INVALID_ID_CHECK,
				ManifestTestUtils.getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#setIndependentContext(boolean)}.
	 */
	@Test
	default void testSetIndependentContext() {
		assertLockableSetter(settings(), ContextManifest::setIndependentContext);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#addAndGetPrerequisite(java.lang.String)}.
	 */
	@Test
	default void testAddAndGetPrerequisite() {
		assertLockableAccumulativeAdd(settings(),
				ContextManifest::addAndGetPrerequisite,
				ManifestTestUtils.getIllegalIdValues(),
				INVALID_ID_CHECK, NPE_CHECK, DUPLICATE_ID_CHECK,
				"layer1", "layer2", "layerxyz123456789");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#addPrerequisite(String, Consumer)}.
	 */
	@Test
	default void testAddPrerequisite() {
		assertLockableAccumulativeAdd(settings(),
				inject_createPrerequisiteManifest(ContextManifest::addPrerequisite),
				ManifestTestUtils.getIllegalIdValues(),
				INVALID_ID_CHECK, NPE_CHECK, DUPLICATE_ID_CHECK,
				"layer1", "layer2", "layerxyz123456789");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#removePrerequisite(de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest)}.
	 */
	@Test
	default void testRemovePrerequisite() {
		final Map<String, PrerequisiteManifest> prerequisites = new IdentityHashMap<>();

		// Make a specialized adder to ensure we save any created PrerequisiteManifest instances in our lookup
		BiConsumer<ContextManifest, String> adder = (context, alias) -> {
			PrerequisiteManifest prerequisite = context.addAndGetPrerequisite(alias);
			assertNotNull(prerequisite);
			assertEquals(alias, prerequisite.getAlias());
			assertSame(context, prerequisite.getContextManifest());

			prerequisites.put(alias, prerequisite);
		};

		// Specialized remover to use our lookup to fetch actual PrerequisiteManifest instances for a given alias
		BiConsumer<ContextManifest, String> remover = (context, alias) -> {
			// Make sure we directly delegate null values
			if(alias==null) {
				context.removePrerequisite(null);
			}

			PrerequisiteManifest prerequisite = prerequisites.get(alias);

			// Unknown alias, so we need to create a prerequisite manifest that is also unknown!
			if(prerequisite==null) {
				prerequisite = mockPrerequisiteManifest(alias);
			}

			context.removePrerequisite(prerequisite);
		};

		assertLockableAccumulativeRemove(settings(),
				adder,
				remover,
				transform_genericCollectionGetter(
						ContextManifest::getPrerequisites, transform_prerequisiteAlias()),
				NPE_CHECK, INVALID_INPUT_CHECK,
				"layer1", "layer2", "layer3");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#addLayerGroup(de.ims.icarus2.model.manifest.api.LayerGroupManifest)}.
	 */
	@Test
	default void testAddLayerGroup() {
		assertLockableAccumulativeAdd(settings(),
				ContextManifest::addLayerGroup,
				NO_ILLEGAL(), NO_CHECK,
				NPE_CHECK, DUPLICATE_ID_CHECK,
				mockGroupManifest("group1"),
				mockGroupManifest("group2"),
				mockGroupManifest("group3"));

		ContextManifest instance = createUnlocked();
		ContextManifest other = createUnlocked();
		LayerGroupManifest groupManifest = mockGroupManifest("group1");
		when(groupManifest.getContextManifest()).thenReturn(Optional.of(other));

		assertManifestException(ManifestErrorCode.MANIFEST_CORRUPTED_STATE,
				() -> instance.addLayerGroup(groupManifest),
				"Test foreign context on layer group");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#removeLayerGroup(de.ims.icarus2.model.manifest.api.LayerGroupManifest)}.
	 */
	@Test
	default void testRemoveLayerGroup() {
		assertLockableAccumulativeRemove(settings(),
				ContextManifest::addLayerGroup,
				ContextManifest::removeLayerGroup,
				ContextManifest::getGroupManifests,
				NPE_CHECK, INVALID_INPUT_CHECK,
				mockGroupManifest("group1"),
				mockGroupManifest("group2"),
				mockGroupManifest("group3"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#addLocationManifest(de.ims.icarus2.model.manifest.api.LocationManifest)}.
	 */
	@Test
	default void testAddLocationManifest() {
		assertLockableAccumulativeAdd(settings(),
				ContextManifest::addLocationManifest,
				NO_ILLEGAL(), NO_CHECK,
				NPE_CHECK, INVALID_INPUT_CHECK,
				mockLocationManifest(),
				mockLocationManifest(),
				mockLocationManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest#removeLocationManifest(de.ims.icarus2.model.manifest.api.LocationManifest)}.
	 */
	@Test
	default void testRemoveLocationManifest() {
		assertLockableAccumulativeRemove(settings(),
				ContextManifest::addLocationManifest,
				ContextManifest::removeLocationManifest,
				ContextManifest::getLocationManifests,
				NPE_CHECK, INVALID_INPUT_CHECK,
				mockLocationManifest(),
				mockLocationManifest(),
				mockLocationManifest());
	}

}
