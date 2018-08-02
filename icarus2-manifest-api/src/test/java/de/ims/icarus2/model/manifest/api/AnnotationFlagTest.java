/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import java.util.function.Function;

/**
 * @author Markus Gärtner
 *
 */
class AnnotationFlagTest implements FlagTest<AnnotationFlag>, StringResourceTest<AnnotationFlag> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.FlagTest#createFlags()
	 */
	@Override
	public AnnotationFlag[] createFlags() {
		return AnnotationFlag.values();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FlagTest#createParser()
	 */
	@Override
	public Function<String, AnnotationFlag> createParser() {
		return AnnotationFlag::parseAnnotationFlag;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.StringResourceTest#createStringResources()
	 */
	@Override
	public AnnotationFlag[] createStringResources() {
		return AnnotationFlag.values();
	}
}
