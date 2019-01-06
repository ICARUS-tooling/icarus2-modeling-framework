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
package de.ims.icarus2.model.manifest.xml.delegates;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;
import de.ims.icarus2.test.annotations.OverrideTest;

/**
 * @author Markus Gärtner
 *
 */
class LayerGroupManifestXmlDelegateTest
		implements ManifestXmlDelegateTest<LayerGroupManifest, LayerGroupManifestXmlDelegate> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends LayerGroupManifestXmlDelegate> getTestTargetClass() {
		return LayerGroupManifestXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.LAYER_GROUP_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	@OverrideTest
	@Test
	public void testMandatoryConstructors() throws Exception {
		createNoArgs();

		LayerGroupManifest groupManifest = mockManifest();
		assertEquals(groupManifest, new LayerGroupManifestXmlDelegate(groupManifest).getInstance());

		ContextManifest contextManifest = mockTypedManifest(ManifestType.CONTEXT_MANIFEST);
		assertOptionalEquals(contextManifest, new LayerGroupManifestXmlDelegate(contextManifest).getInstance().getContextManifest());
	}

	@Test
	void testResetContextManifest() {
		LayerGroupManifestXmlDelegate delegate = create();
		ContextManifest contextManifest = mockTypedManifest(ManifestType.CONTEXT_MANIFEST);
		assertOptionalEquals(contextManifest, delegate.reset(contextManifest).getInstance().getContextManifest());
	}
}
