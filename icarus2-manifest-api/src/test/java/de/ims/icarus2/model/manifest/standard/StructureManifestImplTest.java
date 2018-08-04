/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import java.util.Collections;
import java.util.Set;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureManifestTest;
import de.ims.icarus2.model.manifest.api.TypedManifest;

/**
 * @author Markus Gärtner
 *
 */
class StructureManifestImplTest implements StructureManifestTest<StructureManifest> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifestTest#createHosted(de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry, de.ims.icarus2.model.manifest.api.TypedManifest)
	 */
	@Override
	public StructureManifest createHosted(ManifestLocation manifestLocation, ManifestRegistry registry,
			TypedManifest host) {
		return new StructureManifestImpl(manifestLocation, registry, (StructureLayerManifest)host);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestTest#createUnlocked(de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry)
	 */
	@Override
	public StructureManifest createUnlocked(ManifestLocation location, ManifestRegistry registry) {
		// IMPORTANT: only force non-null host when we don't need a template!
		ItemLayerManifest layerManifest = location.isTemplate() ? null
				: ManifestTestUtils.mockTypedManifest(ManifestType.STRUCTURE_LAYER_MANIFEST);
		return createHosted(location, registry, layerManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	public ManifestType getExpectedType() {
		return ManifestType.STRUCTURE_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends StructureManifest> getTestTargetClass() {
		return StructureManifestImpl.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	public Set<ManifestType> getAllowedHostTypes() {
		return Collections.singleton(ManifestType.STRUCTURE_LAYER_MANIFEST);
	}

}
