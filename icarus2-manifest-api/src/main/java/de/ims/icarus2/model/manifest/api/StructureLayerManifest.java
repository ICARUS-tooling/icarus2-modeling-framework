/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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

import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;




/**
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface StructureLayerManifest extends ItemLayerManifest {

	/**
	 * Returns the manifest for the members (structures) of the top-level
	 * container in this layer. This is effectively the same as calling
	 * {@link Hierarchy#atLevel(int)} with a {@code level} value
	 * of {@code 1} on the underlying {@link #getContainerHierarchy() container hierarchy}
	 * and casting the result to {@code StructureManifest}.
	 * Note that a structure layer always contains a regular container
	 * as root of its container hierarchy. Only on the subsequent levels
	 * the structures themselves are hosted!
	 *
	 * @return
	 * @see #getRootContainerManifest()
	 * @see #getContextManifest()
	 */
	@AccessRestriction(AccessMode.READ)
	StructureManifest getRootStructureManifest();
}
