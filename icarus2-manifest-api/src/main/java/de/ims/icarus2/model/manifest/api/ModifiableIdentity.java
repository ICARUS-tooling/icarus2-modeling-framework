/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import javax.swing.Icon;

import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface ModifiableIdentity extends Identity {

	@Override
	@AccessRestriction(AccessMode.READ)
	Optional<String> getId();

	@Override
	@AccessRestriction(AccessMode.READ)
	Optional<String> getName();

	@Override
	@AccessRestriction(AccessMode.READ)
	Optional<String> getDescription();

	@Override
	@AccessRestriction(AccessMode.READ)
	Optional<Icon> getIcon();

	// Modification methods

	/**
	 *
	 * @param id
	 *
	 * @throws NullPointerException if the given {@code id} is {@code null}
	 */
	void setId(String id);

	void setName(String name);

	void setDescription(String description);

	void setIcon(Icon icon);
}
