/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.swing.Icon;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
public interface ValueManifestTest extends DocumentableTest, ModifiableIdentityTest, TypedManifestTest {

	ValueManifest createWithType(ValueType valueType);

	/**
	 * @see de.ims.icarus2.model.manifest.api.ModifiableIdentityTest#createEmpty()
	 */
	@Override
	default ModifiableIdentity createEmpty() {
		return createWithType(ValueType.STRING);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ModifiableIdentityTest#createFromIdentity(java.lang.String, java.lang.String, java.lang.String, javax.swing.Icon)
	 */
	@Override
	default ModifiableIdentity createFromIdentity(String id, String name, String description, Icon icon) {
		ModifiableIdentity modifiableIdentity = createWithType(ValueType.STRING);
		modifiableIdentity.setId(id);
		modifiableIdentity.setName(name);
		modifiableIdentity.setDescription(description);
		modifiableIdentity.setIcon(icon);
		return modifiableIdentity;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.DocumentableTest#createUnlocked()
	 */
	@Override
	default ValueManifest createUnlocked() {
		return createWithType(ValueType.STRING);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.VALUE_MANIFEST;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueManifest#getValue()}.
	 */
	@Test
	default void testGetValue() {
		for(ValueType valueType : ValueManifest.SUPPORTED_VALUE_TYPES) {

			ValueManifest empty = createWithType(valueType);
			assertNull(empty.getValue());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueManifest#getValueType()}.
	 */
	@Test
	default void testGetValueType() {
		for(ValueType valueType : ValueManifest.SUPPORTED_VALUE_TYPES) {

			ValueManifest empty = createWithType(valueType);
			assertEquals(valueType, empty.getValueType());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueManifest#setValue(java.lang.Object)}.
	 */
	@Test
	default void testSetValue() {
		for(ValueType valueType : ValueManifest.SUPPORTED_VALUE_TYPES) {

			ValueManifest manifest = createWithType(valueType);

			Object testValue = ManifestTestUtils.getTestValue(valueType);

			manifest.setValue(testValue);
			assertEquals(testValue, manifest.getValue());

			Object illegalValue = ManifestTestUtils.getIllegalValue(valueType);
			if(illegalValue!=null) {
				ManifestTestUtils.assertIllegalValue(() -> manifest.setValue(illegalValue));
			}

			manifest.lock();
			LockableTest.assertLocked(() -> manifest.setValue(null));
		}
	}

}
