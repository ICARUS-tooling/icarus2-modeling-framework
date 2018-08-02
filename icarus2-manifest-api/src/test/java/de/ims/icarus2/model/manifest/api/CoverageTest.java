/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import java.util.function.Function;

import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;

/**
 * @author Markus Gärtner
 *
 */
class CoverageTest implements StringResourceTest<Coverage> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.StringResourceTest#createStringResources()
	 */
	@Override
	public Coverage[] createStringResources() {
		return Coverage.values();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.StringResourceTest#createParser()
	 */
	@Override
	public Function<String, Coverage> createParser() {
		return Coverage::parseCoverage;
	}

}
