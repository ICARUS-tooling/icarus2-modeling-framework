/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 */
package de.ims.icarus2.model.manifest.api;



/**
 * @author Markus Gärtner
 *
 */
public interface VersionManifest extends Lockable, TypedManifest {

	public static final String DEFAULT_VERSION_FORMAT_ID = "major-minor-patch";

	/**
	 * Returns the format id that serves as a URI for a certain type
	 * of version format.
	 */
	String getFormatId();

	String getVersionString();

	/**
	 * Equality of a version manifest requires equality of both the
	 * format id and the actual version string.
	 */
	@Override
	boolean equals(Object obj);

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifest#getManifestType()
	 */
	@Override
	default public ManifestType getManifestType() {
		return ManifestType.VERSION;
	}

	// Modification methods

	void setFormatId(String formatId);

	void setVersionString(String versionString);
}
