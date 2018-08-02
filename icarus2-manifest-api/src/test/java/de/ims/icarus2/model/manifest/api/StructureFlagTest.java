/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import java.util.function.Function;

/**
 * @author Markus Gärtner
 *
 */
class StructureFlagTest implements FlagTest<StructureFlag>, StringResourceTest<StructureFlag> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.FlagTest#createFlags()
	 */
	@Override
	public StructureFlag[] createFlags() {
		return StructureFlag.values();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FlagTest#createParser()
	 */
	@Override
	public Function<String, StructureFlag> createParser() {
		return StructureFlag::parseStructureFlag;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.StringResourceTest#createStringResources()
	 */
	@Override
	public StructureFlag[] createStringResources() {
		return StructureFlag.values();
	}

}
