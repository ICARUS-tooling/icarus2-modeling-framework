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
package de.ims.icarus2.util.version;

import static de.ims.icarus2.util.Conditions.checkNotNull;

/**
 * @author Markus Gärtner
 *
 */
public class Version implements Cloneable {

	private final String versionString;
	private final VersionFormat versionFormat;

	public Version(String versionString, VersionFormat versionFormat) {
		checkNotNull(versionString);
		checkNotNull(versionFormat);

		this.versionString = versionString;
		this.versionFormat = versionFormat;
	}

	public String getVersionString() {
		return versionString;
	}

	public VersionFormat getVersionFormat() {
		return versionFormat;
	}

	@Override
	public int hashCode() {
		return versionString.hashCode() * versionFormat.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} else if(obj instanceof Version) {
			Version other = (Version) obj;
			return versionString.equals(other.getVersionString())
					&& versionFormat.equals(other.versionFormat);
		}
		return false;
	}

	@Override
	protected Version clone() {
		try {
			return (Version) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Not cloneable");
		}
	}

	@Override
	public String toString() {
		return versionString;
	}
}
