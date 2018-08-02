/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import java.util.function.Function;

import de.ims.icarus2.model.manifest.api.LocationManifest.PathType;

/**
 * @author Markus Gärtner
 *
 */
class PathTypeTest implements StringResourceTest<PathType> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.StringResourceTest#createStringResources()
	 */
	@Override
	public PathType[] createStringResources() {
		return PathType.values();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.StringResourceTest#createParser()
	 */
	@Override
	public Function<String, PathType> createParser() {
		return PathType::parsePathType;
	}


}
