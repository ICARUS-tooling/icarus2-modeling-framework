/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.xml.delegates;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;
import de.ims.icarus2.test.annotations.OverrideTest;

/**
 * @author Markus Gärtner
 *
 */
class ItemLayerManifestXmlDelegateTest
		implements ManifestXmlDelegateTest<ItemLayerManifest, ItemLayerManifestXmlDelegate> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends ItemLayerManifestXmlDelegate> getTestTargetClass() {
		return ItemLayerManifestXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.ITEM_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	@OverrideTest
	@Test
	public void testMandatoryConstructors() throws Exception {
		createNoArgs();

		ItemLayerManifest layerManifest = mockManifest();
		assertEquals(layerManifest, new ItemLayerManifestXmlDelegate(layerManifest).getInstance());

		LayerGroupManifest groupManifest = mockTypedManifest(ManifestType.LAYER_GROUP_MANIFEST, true);
		assertOptionalEquals(groupManifest, new ItemLayerManifestXmlDelegate(groupManifest).getInstance().getGroupManifest());
	}

	@Test
	void testResetLayerGroup() {
		ItemLayerManifestXmlDelegate delegate = create();
		LayerGroupManifest groupManifest = mockTypedManifest(ManifestType.LAYER_GROUP_MANIFEST, true);
		assertOptionalEquals(groupManifest, delegate.reset(groupManifest).getInstance().getGroupManifest());
	}
}
