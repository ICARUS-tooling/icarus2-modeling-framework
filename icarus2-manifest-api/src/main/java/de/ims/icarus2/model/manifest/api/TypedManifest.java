/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.api;

import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessRestriction;

/**
 * @author Markus Gärtner
 *
 */
public interface TypedManifest {

	/**
	 * Returns the {@code type} of this manifest, i.e. that is
	 * what kind of member in a corpus it describes. If type-specific
	 * behavior is modeled, one should always use this method rather than
	 * doing multiple {@code instanceof} checks.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	ManifestType getManifestType();

	public static boolean defaultEquals(TypedManifest m1, TypedManifest m2) {
		return m1.getManifestType().equals(m2.getManifestType());
	}
}
