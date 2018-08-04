/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifestTest;
import de.ims.icarus2.model.manifest.api.Embedded;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestTest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.TypedManifest;

/**
 * @author Markus Gärtner
 *
 */
class ContainerManifestImplTest implements ContainerManifestTest {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.ContainerManifestImpl#ContainerManifestImpl(de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry, de.ims.icarus2.model.manifest.api.ItemLayerManifest)}.
	 */
	@Test
	void testContainerManifestImplManifestLocationManifestRegistryItemLayerManifest() {
		ItemLayerManifest layerManifest = ManifestTestUtils.mockTypedManifest(ManifestType.ITEM_LAYER_MANIFEST);
		ManifestRegistry registry = ManifestTest.mockManifestRegistry();
		ManifestLocation location = ManifestTest.mockManifestLocation(false);

		ContainerManifest manifest = new ContainerManifestImpl(location, registry, layerManifest);

		assertSame(layerManifest, manifest.getLayerManifest());
		assertSame(registry, manifest.getRegistry());
		assertSame(location, manifest.getManifestLocation());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.ContainerManifestImpl#ContainerManifestImpl(de.ims.icarus2.model.manifest.api.ManifestLocation, de.ims.icarus2.model.manifest.api.ManifestRegistry)}.
	 */
	@Test
	void testContainerManifestImplManifestLocationManifestRegistry() {
		ManifestRegistry registry = ManifestTest.mockManifestRegistry();
		ManifestLocation location = ManifestTest.mockManifestLocation(true);

		ContainerManifest manifest = new ContainerManifestImpl(location, registry);

		assertSame(registry, manifest.getRegistry());
		assertSame(location, manifest.getManifestLocation());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.ContainerManifestImpl#ContainerManifestImpl(de.ims.icarus2.model.manifest.api.ItemLayerManifest)}.
	 */
	@Test
	void testContainerManifestImplItemLayerManifest() {
		ItemLayerManifest layerManifest = ManifestTestUtils.mockTypedManifest(ManifestType.ITEM_LAYER_MANIFEST);
		ContainerManifest manifest = new ContainerManifestImpl(layerManifest);
		assertSame(layerManifest, manifest.getLayerManifest());
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestTest#createUnlocked(de.ims.icarus2.model.manifest.api.ManifestRegistry, de.ims.icarus2.model.manifest.api.ManifestLocation)
	 */
	@Override
	public ContainerManifest createUnlocked(ManifestRegistry registry, ManifestLocation location) {
		// IMPORTANT: only force non-null host when we don't need a template!
		ItemLayerManifest layerManifest = location.isTemplate() ? null
				: ManifestTestUtils.mockTypedManifest(ManifestType.ITEM_LAYER_MANIFEST);
		return new ContainerManifestImpl(location, registry, layerManifest);
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
		return set(ManifestType.ITEM_LAYER_MANIFEST, ManifestType.STRUCTURE_LAYER_MANIFEST);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#createEmbedded(de.ims.icarus2.model.manifest.api.TypedManifest)
	 */
	@Override
	public Embedded createEmbedded(TypedManifest host) {
		return new ContainerManifestImpl((ItemLayerManifest) host);
	}

}
