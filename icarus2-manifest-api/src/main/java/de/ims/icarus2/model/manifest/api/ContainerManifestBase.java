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

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;

/**
 * A manifest that describes a container and its content.
 *
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface ContainerManifestBase<M extends ContainerManifestBase<M>> extends MemberManifest<M>, Embedded {

	public static final ContainerType DEFAULT_CONTAINER_TYPE = ContainerType.LIST;

	/**
	 * Returns the manifest of the {@code ItemLayer} the container
	 * is hosted in.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default <L extends ItemLayerManifestBase<?>> Optional<L> getLayerManifest() {
		return getHost();
	}

	/**
	 * Returns the type of this container. This provides
	 * information about how contained {@code Item}s are ordered and
	 * if they represent a continuous subset of the corpus. Note that the
	 * container type of a {@link Structure} object is undefined and therefore
	 * this method is optional when this container is a structure.
	 *
	 * @return The {@code ContainerType} of this {@code Container}
	 * @throws ManifestException of type {@link GlobalErrorCode#ILLEGAL_STATE} if
	 * the container type is not set.
	 * @see ContainerType
	 */
	@AccessRestriction(AccessMode.READ)
	ContainerType getContainerType();

	boolean isLocalContainerType();

	/**
	 * Checks whether the given {@code flag} is set for this manifest.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	boolean isContainerFlagSet(ContainerFlag flag);

	@AccessRestriction(AccessMode.READ)
	void forEachActiveContainerFlag(Consumer<? super ContainerFlag> action);

	@AccessRestriction(AccessMode.READ)
	void forEachActiveLocalContainerFlag(Consumer<? super ContainerFlag> action);

	default Set<ContainerFlag> getActiveContainerFlags() {
		EnumSet<ContainerFlag> result = EnumSet.noneOf(ContainerFlag.class);

		forEachActiveContainerFlag(result::add);

		return result;
	}

	default Set<ContainerFlag> getActiveLocalContainerFlags() {
		EnumSet<ContainerFlag> result = EnumSet.noneOf(ContainerFlag.class);

		forEachActiveLocalContainerFlag(result::add);

		return result;
	}

	@AccessRestriction(AccessMode.READ)
	default Optional<ContainerManifestBase<?>> getParentManifest() {
		return getLayerManifest()
				.flatMap(ItemLayerManifestBase::getContainerHierarchy)
				.flatMap(h -> h.adjacent(this, Hierarchy.Direction.ABOVE));
	}

	@AccessRestriction(AccessMode.READ)
	default Optional<ContainerManifestBase<?>> getElementManifest() {
		return getLayerManifest()
				.flatMap(ItemLayerManifestBase::getContainerHierarchy)
				.flatMap(h -> h.adjacent(this, Hierarchy.Direction.BELOW));
	}

	// Modification methods

	M setContainerType(ContainerType containerType);

	M setContainerFlag(ContainerFlag flag, boolean active);
}
