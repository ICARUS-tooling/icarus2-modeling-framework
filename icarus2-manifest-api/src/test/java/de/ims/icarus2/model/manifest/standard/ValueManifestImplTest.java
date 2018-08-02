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
