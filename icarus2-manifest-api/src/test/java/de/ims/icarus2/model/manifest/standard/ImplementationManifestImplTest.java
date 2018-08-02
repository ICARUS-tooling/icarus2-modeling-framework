/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import java.util.Set;

import de.ims.icarus2.model.manifest.api.Embedded;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifestTest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.TypedManifest;

/**
 * @author Markus Gärtner
 *
 */
class ImplementationManifestImplTest implements ImplementationManifestTest {


	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestTest#createUnlocked(de.ims.icarus2.model.manifest.api.ManifestRegistry, de.ims.icarus2.model.manifest.api.ManifestLocation)
	 */
	@Override
	public ImplementationManifest createUnlocked(ManifestRegistry registry, ManifestLocation location) {
		return new ImplementationManifestImpl(location, registry, null);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#createEmbedded(de.ims.icarus2.model.manifest.api.TypedManifest)
	 */
	@Override
	public Embedded createEmbedded(TypedManifest host) {
		return new ImplementationManifestImpl((MemberManifest) host);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	public ManifestType getExpectedType() {
		return ManifestType.IMPLEMENTATION_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	public Set<ManifestType> getAllowedHostTypes() {
		return ManifestType.getMemberTypes();
	}

}
