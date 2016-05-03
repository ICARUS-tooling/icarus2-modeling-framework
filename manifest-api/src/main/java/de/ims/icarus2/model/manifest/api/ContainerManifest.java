/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 443 $
 * $Date: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/manifest/ContainerManifest.java $
 *
 * $LastChangedDate: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 443 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.api;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;

/**
 * A manifest that describes a container and its content.
 *
 * @author Markus Gärtner
 * @version $Id: ContainerManifest.java 443 2016-01-11 11:31:11Z mcgaerty $
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface ContainerManifest extends MemberManifest {

	/**
	 * Returns the manifest of the {@code ItemLayer} the container
	 * is hosted in.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	ItemLayerManifest getLayerManifest();

	@Override
	default ManifestFragment getHost() {
		return getLayerManifest();
	}

	/**
	 * Returns the type of this container. This provides
	 * information about how contained {@code Item}s are ordered and
	 * if they represent a continuous subset of the corpus. Note that the
	 * container type of a {@link Structure} object is undefined and therefore
	 * this method is optional when this container is a structure.
	 *
	 * @return The {@code ContainerType} of this {@code Container}
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
	default ContainerManifest getParentManifest() {
		ItemLayerManifest hostManifest = getLayerManifest();
		int index = hostManifest==null ? -1 : hostManifest.indexOfContainerManifest(this);

		if(index<=0) {
			return null;
		} else {
			return hostManifest.getContainerManifest(index-1);
		}
	}

	@AccessRestriction(AccessMode.READ)
	default ContainerManifest getElementManifest() {
		ItemLayerManifest hostManifest = getLayerManifest();
		if(hostManifest==null) {
			return null;
		}

		int index = hostManifest.indexOfContainerManifest(this);

		if(index>=hostManifest.getContainerDepth()-1) {
			return null;
		} else {
			return hostManifest.getContainerManifest(index+1);
		}
	}

	// Modification methods

	void setContainerType(ContainerType containerType);

	void setContainerFlag(ContainerFlag flag, boolean active);
}
