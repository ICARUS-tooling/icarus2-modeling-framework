/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import static de.ims.icarus2.util.collections.CollectionUtils.set;

import java.util.Set;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifestTest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.TypedManifest;

/**
 * @author Markus Gärtner
 *
 */
class ContainerManifestImplTest implements ContainerManifestTest {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends ContainerManifest> getTestTargetClass() {
		return ContainerManifestImpl.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifestTest#createHosted(de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry, de.ims.icarus2.model.manifest.api.TypedManifest)
	 */
	@Override
	public ContainerManifest createHosted(ManifestLocation manifestLocation, ManifestRegistry registry,
			TypedManifest host) {
		return new ContainerManifestImpl(manifestLocation, registry, (ItemLayerManifest)host);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestTest#createUnlocked(de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry)
	 */
	@Override
	public ContainerManifest createUnlocked(ManifestLocation location, ManifestRegistry registry) {
		// IMPORTANT: only force non-null host when we don't need a template!
		ItemLayerManifest layerManifest = location.isTemplate() ? null
				: ManifestTestUtils.mockTypedManifest(ManifestType.ITEM_LAYER_MANIFEST);
		return createHosted(location, registry, layerManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	public ManifestType getExpectedType() {
		return ManifestType.CONTAINER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	public Set<ManifestType> getAllowedHostTypes() {
		return set(ManifestType.ITEM_LAYER_MANIFEST);
	}

}
