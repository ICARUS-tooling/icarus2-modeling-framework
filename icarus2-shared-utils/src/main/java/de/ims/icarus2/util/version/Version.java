/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.util.version;

import static java.util.Objects.requireNonNull;

/**
 * @author Markus Gärtner
 *
 */
public abstract class Version implements Cloneable {

	private final String versionString;
	private final VersionFormat versionFormat;

	public Version(String versionString, VersionFormat versionFormat) {
		requireNonNull(versionString);
		requireNonNull(versionFormat);

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
		return getVersionString();
	}
}
