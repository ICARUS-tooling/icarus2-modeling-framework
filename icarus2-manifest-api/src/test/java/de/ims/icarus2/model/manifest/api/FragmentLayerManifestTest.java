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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.getIllegalIdValues;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.getLegalIdValues;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.inject_createTargetLayerManifest;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.transform_targetLayerId;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.settings;
import static de.ims.icarus2.test.TestUtils.transform_genericValue;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
public interface FragmentLayerManifestTest<M extends FragmentLayerManifest> extends LayerManifestTest<M> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.FRAGMENT_LAYER_MANIFEST;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.FragmentLayerManifest#getValueLayerManifest()}.
	 */
	@Test
	default void testGetValueLayerManifest() {
		assertDerivativeGetter(settings(),
				"layer1",
				"layer2",
				NO_DEFAULT(),
				transform_genericValue(FragmentLayerManifest::getValueLayerManifest, transform_targetLayerId()),
				inject_createTargetLayerManifest(FragmentLayerManifest::setValueLayerId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.FragmentLayerManifest#isLocalValueLayerManifest()}.
	 */
	@Test
	default void testIsLocalValueLayerManifest() {
		assertDerivativeIsLocal(settings(),
				"layer1",
				"layer2",
				FragmentLayerManifest::isLocalValueLayerManifest,
				FragmentLayerManifest::setValueLayerId);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.FragmentLayerManifest#getAnnotationKey()}.
	 */
	@Test
	default void testGetAnnotationKey() {
		assertDerivativeGetter(settings(),
				"key1",
				"key2",
				NO_DEFAULT(),
				FragmentLayerManifest::getAnnotationKey,
				FragmentLayerManifest::setAnnotationKey);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.FragmentLayerManifest#isLocalAnnotationKey()}.
	 */
	@Test
	default void testIsLocalAnnotationKey() {
		assertDerivativeIsLocal(settings(),
				"key1",
				"key2",
				FragmentLayerManifest::isLocalAnnotationKey,
				FragmentLayerManifest::setAnnotationKey);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.FragmentLayerManifest#getRasterizerManifest()}.
	 */
	@Test
	default void testGetRasterizerManifest() {
		assertDerivativeGetter(settings(),
				mockTypedManifest(ManifestType.RASTERIZER_MANIFEST),
				mockTypedManifest(ManifestType.RASTERIZER_MANIFEST),
				NO_DEFAULT(),
				FragmentLayerManifest::getRasterizerManifest,
				FragmentLayerManifest::setRasterizerManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.FragmentLayerManifest#isLocalRasterizerManifest()}.
	 */
	@Test
	default void testIsLocalRasterizerManifest() {
		assertDerivativeIsLocal(settings(),
				mockTypedManifest(ManifestType.RASTERIZER_MANIFEST),
				mockTypedManifest(ManifestType.RASTERIZER_MANIFEST),
				FragmentLayerManifest::isLocalRasterizerManifest,
				FragmentLayerManifest::setRasterizerManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.FragmentLayerManifest#setValueLayerId(java.lang.String)}.
	 */
	@Test
	default void testSetValueLayerId() {
		assertLockableSetterBatch(settings(),
				FragmentLayerManifest::setValueLayerId,
				getLegalIdValues(), true,
				INVALID_ID_CHECK, getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.FragmentLayerManifest#setAnnotationKey(java.lang.String)}.
	 */
	@Test
	default void testSetAnnotationKey() {
		assertLockableSetter(settings(),
				FragmentLayerManifest::setAnnotationKey,
				"key1", true, INVALID_INPUT_CHECK, "");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.FragmentLayerManifest#setRasterizerManifest(de.ims.icarus2.model.manifest.api.RasterizerManifest)}.
	 */
	@Test
	default void testSetRasterizerManifest() {
		assertLockableSetter(settings(),
				FragmentLayerManifest::setRasterizerManifest,
				mockTypedManifest(ManifestType.RASTERIZER_MANIFEST),
				true, NO_CHECK);
	}

}
