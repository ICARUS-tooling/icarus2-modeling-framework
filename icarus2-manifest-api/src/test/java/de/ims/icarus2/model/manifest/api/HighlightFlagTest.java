/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import java.util.function.Function;

/**
 * @author Markus Gärtner
 *
 */
class HighlightFlagTest implements FlagTest<HighlightFlag>, StringResourceTest<HighlightFlag> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.FlagTest#createFlags()
	 */
	@Override
	public HighlightFlag[] createFlags() {
		return HighlightFlag.values();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FlagTest#createParser()
	 */
	@Override
	public Function<String, HighlightFlag> createParser() {
		return HighlightFlag::parseHighlightFlag;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.StringResourceTest#createStringResources()
	 */
	@Override
	public HighlightFlag[] createStringResources() {
		return HighlightFlag.values();
	}

}
