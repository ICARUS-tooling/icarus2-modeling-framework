/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import java.util.function.Function;

/**
 * @author Markus Gärtner
 *
 */
class LocationTypeTest implements StringResourceTest<LocationType> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.StringResourceTest#createStringResources()
	 */
	@Override
	public LocationType[] createStringResources() {
		return LocationType.values();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.StringResourceTest#createParser()
	 */
	@Override
	public Function<String, LocationType> createParser() {
		return LocationType::parseLocationType;
	}

}
