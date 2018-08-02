/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import java.util.function.Function;

import de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType;

/**
 * @author Markus Gärtner
 *
 */
class SourceTypeTest implements StringResourceTest<SourceType> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.StringResourceTest#createStringResources()
	 */
	@Override
	public SourceType[] createStringResources() {
		return SourceType.values();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.StringResourceTest#createParser()
	 */
	@Override
	public Function<String, SourceType> createParser() {
		return SourceType::parseSourceType;
	}

}
