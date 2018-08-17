/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import de.ims.icarus2.model.manifest.api.LocationManifest.PathType;
import de.ims.icarus2.model.manifest.api.PathEntryTest;
import de.ims.icarus2.model.manifest.standard.LocationManifestImpl.PathEntryImpl;

/**
 * @author Markus Gärtner
 *
 */
class PathEntryImplTest implements PathEntryTest<PathEntryImpl> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.PathEntryTest#createInstance(de.ims.icarus2.model.manifest.api.LocationManifest.PathType, java.lang.String)
	 */
	@Override
	public PathEntryImpl createInstance(PathType type, String value) {
		return new PathEntryImpl(type, value);
	}

}
