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

import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.PathResolverManifest;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;
import de.ims.icarus2.test.annotations.OverrideTest;

/**
 * @author Markus Gärtner
 *
 */
class PathResolverManifestXmlDelegateTest
		implements ManifestXmlDelegateTest<PathResolverManifest, PathResolverManifestXmlDelegate> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends PathResolverManifestXmlDelegate> getTestTargetClass() {
		return PathResolverManifestXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.PATH_RESOLVER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	@OverrideTest
	@Test
	public void testMandatoryConstructors() throws Exception {
		createNoArgs();

		PathResolverManifest manifest = mockManifest();
		assertEquals(manifest, new PathResolverManifestXmlDelegate(manifest).getInstance());

		LocationManifest locationManifest = mockTypedManifest(ManifestType.LOCATION_MANIFEST);
		assertOptionalEquals(locationManifest, new PathResolverManifestXmlDelegate(locationManifest).getInstance().getLocationManifest());
	}

	@Test
	void testResetStructureLayerManifest() {
		PathResolverManifestXmlDelegate delegate = create();
		LocationManifest locationManifest = mockTypedManifest(ManifestType.LOCATION_MANIFEST);
		assertOptionalEquals(locationManifest, delegate.reset(locationManifest).getInstance().getLocationManifest());
	}
}
