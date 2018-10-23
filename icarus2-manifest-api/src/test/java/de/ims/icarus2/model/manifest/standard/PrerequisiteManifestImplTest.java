/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;

import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.PrerequisiteManifestTest;
import de.ims.icarus2.model.manifest.standard.ContextManifestImpl.PrerequisiteManifestImpl;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class PrerequisiteManifestImplTest implements PrerequisiteManifestTest<PrerequisiteManifestImpl> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends PrerequisiteManifestImpl> getTestTargetClass() {
		return PrerequisiteManifestImpl.class;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public PrerequisiteManifestImpl createTestInstance(TestSettings settings) {
		ContextManifest contextManifest = mockTypedManifest(ManifestType.CONTEXT_MANIFEST);
		return settings.process(new PrerequisiteManifestImpl(contextManifest, "prereq1"));
	}
}
