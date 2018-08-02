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
package de.ims.icarus2.model.manifest.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.ValueManifest;
import de.ims.icarus2.model.manifest.api.ValueManifestTest;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
class ValueManifestImplTest implements ValueManifestTest {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.ValueManifestImpl#ValueManifestImpl(de.ims.icarus2.model.manifest.types.ValueType)}.
	 */
	@Test
	void testValueManifestImpl() {
		ValueManifestImpl impl = new ValueManifestImpl(ValueType.STRING);
		assertEquals(ValueType.STRING, impl.getValueType());
		assertNull(impl.getValue());
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ValueManifestTest#createWithType(de.ims.icarus2.model.manifest.types.ValueType)
	 */
	@Override
	public ValueManifest createWithType(ValueType valueType) {
		return new ValueManifestImpl(valueType);
	}

}
