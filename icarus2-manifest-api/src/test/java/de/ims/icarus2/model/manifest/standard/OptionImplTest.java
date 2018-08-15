/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.ModifiableIdentity;
import de.ims.icarus2.model.manifest.api.OptionTest;
import de.ims.icarus2.model.manifest.standard.OptionsManifestImpl.OptionImpl;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class OptionImplTest implements OptionTest<OptionImpl> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends OptionImpl> getTestTargetClass() {
		return OptionImpl.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	public ManifestType getExpectedType() {
		return ManifestType.OPTION;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.OptionTest#createWithType(de.ims.icarus2.test.TestSettings, de.ims.icarus2.model.manifest.types.ValueType)
	 */
	@Override
	public OptionImpl createWithType(TestSettings settings, ValueType valueType) {
		return new OptionImpl("optionX", valueType);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ModifiableIdentityTest#createEmpty()
	 */
	@Override
	public ModifiableIdentity createEmpty() {
		return new OptionImpl();
	}
}
