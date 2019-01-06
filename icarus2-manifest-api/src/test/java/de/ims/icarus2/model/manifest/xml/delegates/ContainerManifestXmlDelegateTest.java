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
package de.ims.icarus2.model.manifest.xml.delegates;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestGenerator;
import de.ims.icarus2.model.manifest.ManifestGenerator.Config;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;
import de.ims.icarus2.test.annotations.OverrideTest;

/**
 * @author Markus Gärtner
 *
 */
class ContainerManifestXmlDelegateTest implements ManifestXmlDelegateTest<ContainerManifest, ContainerManifestXmlDelegate> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends ContainerManifestXmlDelegate> getTestTargetClass() {
		return ContainerManifestXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.CONTAINER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#configurations()
	 */
	@Override
	public List<Config> configurations() {
		return Stream.of(ContainerType.values())
				.filter(ct -> ct!=ContainerType.PROXY) //TODO get rid of the PROXY type
				.map(ct -> ManifestGenerator.config()
							.preprocessor(ManifestType.CONTAINER_MANIFEST,
									m -> ((ContainerManifest)m).setContainerType(ct))
							.label(ct.toString()))
				.collect(Collectors.toList());
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	@OverrideTest
	@Test
	public void testMandatoryConstructors() throws Exception {
		createNoArgs();

		ContainerManifest manifest = mockManifest();
		assertEquals(manifest, new ContainerManifestXmlDelegate(manifest).getInstance());

		ItemLayerManifestBase<?> layerManifest = mockTypedManifest(ManifestType.ITEM_LAYER_MANIFEST);
		assertOptionalEquals(layerManifest, new ContainerManifestXmlDelegate(layerManifest).getInstance().getLayerManifest());
	}

	@Test
	void testResetItemLayerManifest() {
		ContainerManifestXmlDelegate delegate = create();
		ItemLayerManifestBase<?> layerManifest = mockTypedManifest(ManifestType.ITEM_LAYER_MANIFEST);
		assertOptionalEquals(layerManifest, delegate.reset(layerManifest).getInstance().getLayerManifest());
	}
}
