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
package de.ims.icarus2.model.standard.members;

import static de.ims.icarus2.model.util.ModelUtils.getName;

import de.ims.icarus2.GlobalErrorCode;
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
 *
 */
public class MemberUtils {

	public static void checkSingleBaseContainer(Container container) {
		DataSet<Container> baseContainers = container.getBaseContainers();

		if(baseContainers.entryCount()>1)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
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
