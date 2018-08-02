/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import java.util.function.Function;

/**
 * @author Markus Gärtner
 *
 */
class ContainerFlagTest implements FlagTest<ContainerFlag>, StringResourceTest<ContainerFlag> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.FlagTest#createFlags()
	 */
	@Override
	public ContainerFlag[] createFlags() {
		return ContainerFlag.values();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FlagTest#createParser()
	 */
	@Override
	public Function<String, ContainerFlag> createParser() {
		return ContainerFlag::parseContainerFlag;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.StringResourceTest#createStringResources()
	 */
	@Override
	public ContainerFlag[] createStringResources() {
		return ContainerFlag.values();
	}

}
