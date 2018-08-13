/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import java.util.Set;

import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.OptionsManifestTest;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class OptionsManifestImplTest implements OptionsManifestTest<OptionsManifestImpl>{


	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	public ManifestType getExpectedType() {
		return ManifestType.OPTIONS_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends OptionsManifestImpl> getTestTargetClass() {
		return OptionsManifestImpl.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	public Set<ManifestType> getAllowedHostTypes() {
		return ManifestType.getMemberTypes();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#createEmbedded(de.ims.icarus2.model.manifest.api.TypedManifest)
	 */
	@Override
	public OptionsManifestImpl createEmbedded(TypedManifest host) throws Exception {
		return new OptionsManifestImpl((MemberManifest) host);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestTest#createTestInstance(de.ims.icarus2.test.TestSettings, de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry)
	 */
	@Override
	public OptionsManifestImpl createTestInstance(TestSettings settings, ManifestLocation location,
			ManifestRegistry registry) {
		return new OptionsManifestImpl(location, registry);
	}

}
