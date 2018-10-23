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
package de.ims.icarus2.model.manifest.api.binding;

import java.util.Optional;

import de.ims.icarus2.util.Multiplicity;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.lang.ClassUtils;

/**
 * Models a requirement an entity can publish to let others know what kinds of layers
 * it is able to consume or requires to work properly.
 *
 * @author Markus Gärtner
 *
 */
public interface LayerPrerequisite {

	/**
	 * Returns the id of the target layer or an empty {@link Optional} if an exact id match
	 * is not required or the prerequisite has not yet been fully resolved.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<String> getLayerId();

	/**
	 * Returns the id of the context which should be used to resolve the required
	 * layer (specified by the {@link #getLayerId() method}) or an empty {@link Optional} if no
	 * exact match is required or the prerequisite has not yet been fully resolved.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<String> getContextId();

	/**
	 * If this layer only requires <i>some</i> layer of a certain type to be present
	 * this method provides the mechanics to tell this. When the returned value is
	 * {@link Optional#isPresent() present} it is considered to be the exact name of a previously
	 * defined layer type.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<String> getTypeId();

	/**
	 * Returns the id the required layer should be assigned once resolved. This links
	 * the result of an abstract prerequisite declaration to a boundary or base definition
	 * in a template. In addition a prerequisite's alias serves as its identifier. Therefore
	 * an alias must be unique within the same context!
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	String getAlias();

	/**
	 * Returns a brief description of this prerequisite, usable as a hint for user interfaces
	 * when asking the user to resolve ambiguous references.
	 * <p>
	 * This is an optional method.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<String> getDescription();

	/**
	 * Returns the number of allowed bindings assigned to this prerequisite.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Multiplicity getMultiplicity();


	public static boolean defaultEquals(LayerPrerequisite p1, LayerPrerequisite p2) {
		return ClassUtils.equals(p1.getAlias(), p2.getAlias())
				&& ClassUtils.equals(p1.getContextId(), p2.getContextId())
				&& ClassUtils.equals(p1.getLayerId(), p2.getLayerId())
				&& ClassUtils.equals(p1.getTypeId(), p2.getTypeId())
				&& ClassUtils.equals(p1.getDescription(), p2.getDescription())
				&& ClassUtils.equals(p1.getMultiplicity(), p2.getMultiplicity());

	}
}
