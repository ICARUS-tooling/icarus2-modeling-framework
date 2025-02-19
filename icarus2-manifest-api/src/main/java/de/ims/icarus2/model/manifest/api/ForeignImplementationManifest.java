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
package de.ims.icarus2.model.manifest.api;

import java.util.Optional;

import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessRestriction;

/**
 * @author Markus Gärtner
 *
 */
public interface ForeignImplementationManifest<F extends ForeignImplementationManifest<F>> extends MemberManifest<F> {


	/**
	 * Returns the manifest that specifies the actual implementation.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<ImplementationManifest> getImplementationManifest();

	/**
	 * Returns {@code true} if the present implementation manifest
	 * is actually hosted by this manifest and not derived via some template.
	 */
	boolean isLocalImplementation();

	// Modification methods

	F setImplementationManifest(ImplementationManifest implementationManifest);
}
