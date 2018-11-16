/**
 *
 */
package de.ims.icarus2.model.manifest.xml.delegates;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Streams;

import de.ims.icarus2.model.manifest.ManifestGenerator;
import de.ims.icarus2.model.manifest.ManifestGenerator.Config;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathType;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest;

/**
 * @author Markus Gärtner
 *
 */
class LocationManifestXmlDelegateTest implements ManifestXmlDelegateTest<LocationManifest,
		LocationManifestXmlDelegate> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends LocationManifestXmlDelegate> getTestTargetClass() {
		return LocationManifestXmlDelegate.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#getHandledType()
	 */
	@Override
	public ManifestType getHandledType() {
		return ManifestType.LOCATION_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateTest#configurations()
	 */
	@Override
	public List<Config> configurations() {
		return Streams.concat(
				Stream.of(PathType.values())
					.map(type -> ManifestGenerator.config()
							.preprocessor(ManifestType.LOCATION_MANIFEST,
									m -> ((LocationManifest)m).setRootPathType(type))
							.label(type.toString())),
				Stream.of(ManifestGenerator.config()
						.preprocessor(ManifestType.LOCATION_MANIFEST,
								m -> ((LocationManifest)m).setIsInline(true))
						.label("inline data"))
			).collect(Collectors.toList());

	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	public void testMandatoryConstructors() throws Exception {
		createNoArgs();
	}

	/**
	 * @see LocationManifestXmlDelegate#reset(de.ims.icarus2.model.manifest.api.ContextManifest)
	 */
	@Test
	public void testResetContextManifest() {
		ContextManifest contextManifest = mockTypedManifest(ManifestType.CONTEXT_MANIFEST);
		assertNotNull(create().reset(contextManifest).getInstance());
	}
}
