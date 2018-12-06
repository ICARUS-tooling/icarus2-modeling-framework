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
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.transform_id;
import static de.ims.icarus2.test.TestUtils.assertNotPresent;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.OptionsManifest.Option;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 *
 */
public interface OptionsManifestTest<M extends OptionsManifest> extends ManifestTest<M>, EmbeddedTest<M> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 *
	 * @see MemberManifestTest#createTestInstance(TestSettings)
	 */
	@Provider
	@Override
	default M createTestInstance(TestSettings settings) {
		return ManifestTest.super.createTestInstance(settings);
	}

	public static Option mockOption(String id) {
		Option option = mock(Option.class);
		when(option.getId()).thenReturn(Optional.of(id));
		return option;
	}

	public static Identity mockIdentity(String id) {
		Identity identity = mock(Identity.class);
		when(identity.getId()).thenReturn(Optional.of(id));
		return identity;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.OPTIONS_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	default Set<ManifestType> getAllowedHostTypes() {
		return ManifestType.getMemberTypes();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#getMemberManifest()}.
	 * @throws Exception
	 */
	@Test
	default void testGetMemberManifest() throws Exception {
		assertNotPresent(createUnlocked().getMemberManifest());
		assertNotPresent(createTemplate(settings()).getMemberManifest());

		MemberManifest host = mockTypedManifest(MemberManifest.class);
		assertOptionalEquals(host, createEmbedded(settings(), host).getMemberManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#getOptionIds()}.
	 */
	@Test
	default void testGetOptionIds() {
		assertDerivativeAccumulativeGetter(settings(),
				"id1", "id2",
				OptionsManifest::getOptionIds,
				TestUtils.inject_genericSetter(OptionsManifest::addOption, OptionsManifestTest::mockOption));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#isLocalGroupIdentifier(de.ims.icarus2.util.id.Identity)}.
	 */
	@Test
	default void testIsLocalGroupIdentifier() {
		assertDerivativeAccumulativeIsLocal(settings(),
				mockIdentity("group1"), mockIdentity("group2"),
				OptionsManifest::isLocalGroupIdentifier,
				OptionsManifest::addGroupIdentifier);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#forEachGroupIdentifier(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachGroupIdentifier() {
		assertDerivativeForEach(settings(),
				mockIdentity("group1"), mockIdentity("group2"),
				OptionsManifest::forEachGroupIdentifier,
				OptionsManifest::addGroupIdentifier);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#forEachLocalGroupIdentifier(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalGroupIdentifier() {
		assertDerivativeForEachLocal(settings(),
				mockIdentity("group1"), mockIdentity("group2"),
				OptionsManifest::forEachLocalGroupIdentifier,
				OptionsManifest::addGroupIdentifier);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#hasLocalGroupIdentifiers()}.
	 */
	@Test
	default void testHasLocalGroupIdentifiers() {
		assertDerivativeIsLocal(settings(),
				mockIdentity("group1"), mockIdentity("group2"),
				OptionsManifest::hasLocalGroupIdentifiers,
				OptionsManifest::addGroupIdentifier);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#getGroupIdentifiers()}.
	 */
	@Test
	default void testGetGroupIdentifiers() {
		assertDerivativeAccumulativeGetter(settings(),
				mockIdentity("group1"), mockIdentity("group2"),
				OptionsManifest::getGroupIdentifiers,
				OptionsManifest::addGroupIdentifier);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#getLocalGroupIdentifiers()}.
	 */
	@Test
	default void testGetLocalGroupIdentifiers() {
		assertDerivativeAccumulativeLocalGetter(settings(),
				mockIdentity("group1"), mockIdentity("group2"),
				OptionsManifest::getLocalGroupIdentifiers,
				OptionsManifest::addGroupIdentifier);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#getOption(java.lang.String)}.
	 */
	@Test
	default void testGetOption() {
		assertDerivativeAccumulativeOptLookup(
				settings(),
				mockOption("id1"), mockOption("id2"),
				OptionsManifest::getOption,
				true,
				OptionsManifest::addOption,
				transform_id(),
				"unknown1");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#forEachOption(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachOption() {
		assertDerivativeForEach(settings(),
				mockOption("id1"), mockOption("ids2"),
				OptionsManifest::forEachOption,
				OptionsManifest::addOption);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#forEachLocalOption(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalOption() {
		assertDerivativeForEachLocal(settings(),
				mockOption("id1"), mockOption("ids2"),
				OptionsManifest::forEachLocalOption,
				OptionsManifest::addOption);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#getOptions()}.
	 */
	@Test
	default void testGetOptions() {
		assertDerivativeAccumulativeGetter(settings(),
				mockOption("id1"), mockOption("ids2"),
				OptionsManifest::getOptions,
				OptionsManifest::addOption);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#getLocalOptions()}.
	 */
	@Test
	default void testGetLocalOptions() {
		assertDerivativeAccumulativeLocalGetter(settings(),
				mockOption("id1"), mockOption("ids2"),
				OptionsManifest::getLocalOptions,
				OptionsManifest::addOption);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#hasOption(java.lang.String)}.
	 */
	@Test
	default void testHasOption() {
		assertDerivativeAccumulativeLookupContains(
				settings(),
				mockOption("id1"), mockOption("ids2"),
				OptionsManifest::hasOption, true,
				OptionsManifest::addOption,
				transform_id());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#isLocalOption(java.lang.String)}.
	 */
	@Test
	default void testIsLocalOption() {
		assertDerivativeAccumulativeIsLocal(settings(),
				"id1", "id2",
				OptionsManifest::isLocalOption,
				TestUtils.inject_genericSetter(OptionsManifest::addOption, OptionsManifestTest::mockOption));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#hasLocalOptions()}.
	 */
	@Test
	default void testHasLocalOptions() {
		assertDerivativeIsLocal(settings(),
				mockOption("id1"), mockOption("ids2"),
				OptionsManifest::hasLocalOptions,
				OptionsManifest::addOption);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#addOption(de.ims.icarus2.model.manifest.api.OptionsManifest.Option)}.
	 */
	@Test
	default void testAddOption() {
		assertLockableAccumulativeAdd(
				settings(),
				OptionsManifest::addOption, TestUtils.NO_ILLEGAL(),
				TestUtils.NO_CHECK, true, DUPLICATE_ID_CHECK,
				mockOption("id1"), mockOption("ids2"), mockOption("id3"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#removeOption(de.ims.icarus2.model.manifest.api.OptionsManifest.Option)}.
	 */
	@Test
	default void testRemoveOption() {
		assertLockableAccumulativeRemove(
				settings(),
				OptionsManifest::addOption, OptionsManifest::removeOption,
				OptionsManifest::getOptions,
				true, UNKNOWN_ID_CHECK,
				mockOption("id1"), mockOption("ids2"), mockOption("id3"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#addGroupIdentifier(de.ims.icarus2.util.id.Identity)}.
	 */
	@Test
	default void testAddGroupIdentifier() {
		assertLockableAccumulativeAdd(
				settings(),
				OptionsManifest::addGroupIdentifier, TestUtils.NO_ILLEGAL(),
				TestUtils.NO_CHECK, true, DUPLICATE_ID_CHECK,
				mockIdentity("id1"), mockIdentity("ids2"), mockIdentity("id3"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.OptionsManifest#removeGroupIdentifier(de.ims.icarus2.util.id.Identity)}.
	 */
	@Test
	default void testRemoveGroupIdentifier() {
		assertLockableAccumulativeRemove(
				settings(),
				OptionsManifest::addGroupIdentifier, OptionsManifest::removeGroupIdentifier,
				OptionsManifest::getGroupIdentifiers,
				true, UNKNOWN_ID_CHECK,
				mockIdentity("id1"), mockIdentity("ids2"), mockIdentity("id3"));
	}

}
