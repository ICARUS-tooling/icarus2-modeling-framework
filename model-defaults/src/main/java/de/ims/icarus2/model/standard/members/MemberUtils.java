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

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/MemberUtils.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.ContainerFlag;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.StructureFlag;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 * @version $Id: MemberUtils.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public class MemberUtils {

	public static void checkSingleBaseContainer(Container container) {
		DataSet<Container> baseContainers = container.getBaseContainers();

		if(baseContainers.entryCount()>1)
			throw new ModelException(ModelErrorCode.INVALID_INPUT,
					"Container must not have more than one base container: "+getName(container));
	}

	public static void checkContainerFlagsSet(Container container, ContainerFlag...flags) {
		ContainerManifest manifest = container.getManifest();
		LazyCollection<ContainerFlag> missingFlags = LazyCollection.lazySet(flags.length);

		for(ContainerFlag flag : flags) {
			if(!manifest.isContainerFlagSet(flag)) {
				missingFlags.add(flag);
			}
		}

		if(!missingFlags.isEmpty()) {
			String s = missingFlags.toString();
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_LINKING,
					"Container '"+getName(container)+"' is missing flags: "+s);
		}
	}

	public static void checkContainerFlagsNotSet(Container container, ContainerFlag...flags) {
		ContainerManifest manifest = container.getManifest();
		LazyCollection<ContainerFlag> forbiddenFlags = LazyCollection.lazySet(flags.length);

		for(ContainerFlag flag : flags) {
			if(manifest.isContainerFlagSet(flag)) {
				forbiddenFlags.add(flag);
			}
		}

		if(!forbiddenFlags.isEmpty()) {
			String s = forbiddenFlags.toString();
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_LINKING,
					"Container '"+getName(container)+"' has forbidden flags: "+s);
		}
	}

	public static void checkStaticContainer(Container container) {
		ContainerManifest manifest = container.getManifest();
		if(manifest.isContainerFlagSet(ContainerFlag.NON_STATIC))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_LINKING,
					"Container must be static in terms of items: "+getName(container));
	}

	public static void checkNonEmptyContainer(Container container) {
		ContainerManifest manifest = container.getManifest();
		if(manifest.isContainerFlagSet(ContainerFlag.EMPTY))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_LINKING,
					"Container must not be allowed to be empty: "+getName(container));
	}

	public static void checkNonEmptyStructure(Structure structure) {
		StructureManifest manifest = structure.getManifest();
		if(manifest.isStructureFlagSet(StructureFlag.EMPTY))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_LINKING,
					"Structure must not be allowed to be empty: "+getName(structure));
	}

	public static void checkNonPartialStructure(Structure structure) {
		StructureManifest manifest = structure.getManifest();
		if(manifest.isStructureFlagSet(StructureFlag.PARTIAL))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_LINKING,
					"Structure must not be allowed to be partial: "+getName(structure));
	}

	public static void checkNoLoopsStructure(Structure structure) {
		StructureManifest manifest = structure.getManifest();
		if(manifest.isStructureFlagSet(StructureFlag.LOOPS))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_LINKING,
					"Structure must not be allowed to contain loops: "+getName(structure));
	}

	public static void checkHostStructure(Edge edge, Structure expectedHost) {
		if(edge.getStructure()!=expectedHost)
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					Messages.mismatchMessage("Foreign hosting structure for edge "+getName(edge),
							getName(expectedHost), getName(edge.getStructure())));
	}

	public static void checkNonVirtualRoot(Item node, Structure structure) {
		if(structure.getVirtualRoot()==node)
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					"Method not designed for virtual root node");
	}
}
