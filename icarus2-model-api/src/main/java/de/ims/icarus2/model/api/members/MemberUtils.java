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
package de.ims.icarus2.model.api.members;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.ContainerFlag;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
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

	public static Container checkSingleBaseContainer(Container container) {
		requireNonNull(container);
		DataSet<Container> baseContainers = container.getBaseContainers();

		if(baseContainers.entryCount()!=1)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Container must have exactly one base container: "+getName(container));

		return baseContainers.entryAt(0);
	}

	public static void checkContainerFlagsSet(Container container, ContainerFlag...flags) {
		requireNonNull(container);

		ContainerManifestBase<?> manifest = container.getManifest();
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
		requireNonNull(container);

		ContainerManifestBase<?> manifest = container.getManifest();
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

	/**
	 * Throw {@link ModelException} of type {@link ModelErrorCode#MODEL_ILLEGAL_LINKING}
	 * if the container's {@link ContainerManifest manifest} has set the
	 * {@link ContainerFlag#NON_STATIC} flag or if the container is not a
	 * {@link Container#isProxy() proxy}.
	 *
	 * @param container
	 */
	public static void checkStaticContainer(Container container) {
		requireNonNull(container);

		if(container.isProxy()) {
			return;
		}

		ContainerManifestBase<?> manifest = container.getManifest();
		if(manifest.isContainerFlagSet(ContainerFlag.NON_STATIC))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_LINKING,
					"Container must be static in terms of items: "+getName(container));
	}

	/**
	 * Throw {@link ModelException} of type {@link ModelErrorCode#MODEL_ILLEGAL_LINKING}
	 * if the container's {@link ContainerManifest manifest} has set the
	 * {@link ContainerFlag#EMPTY} flag.
	 *
	 * @param container
	 */
	public static void checkNonEmptyContainer(Container container) {
		requireNonNull(container);

		ContainerManifestBase<?> manifest = container.getManifest();
		if(manifest.isContainerFlagSet(ContainerFlag.EMPTY))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_LINKING,
					"Container must not be allowed to be empty: "+getName(container));
	}

	/**
	 * Throw {@link ModelException} of type {@link ModelErrorCode#MODEL_ILLEGAL_LINKING}
	 * if the structure's {@link StructureManifest manifest} has set the
	 * {@link StructureFlag#EMPTY} flag.
	 *
	 * @param container
	 */
	public static void checkNonEmptyStructure(Structure structure) {
		requireNonNull(structure);

		StructureManifest manifest = requireNonNull(structure.getManifest());
		if(manifest.isStructureFlagSet(StructureFlag.EMPTY))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_LINKING,
					"Structure must not be allowed to be empty: "+getName(structure));
	}

	/**
	 * Throw {@link ModelException} of type {@link ModelErrorCode#MODEL_ILLEGAL_LINKING}
	 * if the structure's {@link StructureManifest manifest} has set the
	 * {@link StructureFlag#PARTIAL} flag.
	 *
	 * @param container
	 */
	public static void checkNonPartialStructure(Structure structure) {
		requireNonNull(structure);

		StructureManifest manifest = requireNonNull(structure.getManifest());
		if(manifest.isStructureFlagSet(StructureFlag.PARTIAL))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_LINKING,
					"Structure must not be allowed to be partial: "+getName(structure));
	}

	/**
	 * Throw {@link ModelException} of type {@link ModelErrorCode#MODEL_ILLEGAL_LINKING}
	 * if the structure's {@link StructureManifest manifest} has set the
	 * {@link StructureFlag#LOOPS} flag.
	 *
	 * @param container
	 */
	public static void checkNoLoopsStructure(Structure structure) {
		requireNonNull(structure);

		StructureManifest manifest = requireNonNull(structure.getManifest());
		if(manifest.isStructureFlagSet(StructureFlag.LOOPS))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_LINKING,
					"Structure must not be allowed to contain loops: "+getName(structure));
	}

	/**
	 * Throw {@link ModelException} of type {@link ModelErrorCode#MODEL_ILLEGAL_MEMBER}
	 * if the {@link Edge#getStructure() host} of the given {@code edge} does not match the
	 * {@code expectedHost} structure.
	 *
	 * @param container
	 */
	public static void checkHostStructure(Edge edge, Structure expectedHost) {
		requireNonNull(edge);
		requireNonNull(expectedHost);
		requireNonNull(edge.getStructure());

		if(edge.getStructure()!=expectedHost)
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					Messages.mismatch("Foreign hosting structure for edge "+getName(edge),
							getName(expectedHost), getName(edge.getStructure())));
	}

	/**
	 * Throw {@link ModelException} of type {@link ModelErrorCode#MODEL_ILLEGAL_MEMBER}
	 * if the specified {@code structure} does not {@link Structure#containsEdge(Edge) contain}
	 * the given {@code edge}.
	 *
	 * @param container
	 */
	public static void checkContainsEdge(Structure structure, Edge edge) {
		requireNonNull(structure);
		requireNonNull(edge);

		if(!structure.containsEdge(edge))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					Messages.noSuchElement("Unknown edge",
							getName(edge), getName(structure)));
	}

	/**
	 * Throw {@link ModelException} of type {@link ModelErrorCode#MODEL_ILLEGAL_MEMBER}
	 * if the specified {@code structure} does {@link Structure#containsEdge(Edge) contain}
	 * the given {@code edge}.
	 *
	 * @param container
	 */
	public static void checkNotContainsEdge(Structure structure, Edge edge) {
		requireNonNull(structure);
		requireNonNull(edge);

		if(structure.containsEdge(edge))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					Messages.unexpectedElement("Edge already present",
							getName(edge), getName(structure)));
	}

	/**
	 * Throw {@link ModelException} of type {@link ModelErrorCode#MODEL_ILLEGAL_MEMBER}
	 * if the specified {@code container} does not {@link Container#containsItem(Item) contain}
	 * the given {@code item}.
	 *
	 * @param container
	 */
	public static void checkContainsItem(Container container, Item item) {
		requireNonNull(container);
		requireNonNull(item);

		if(!container.containsItem(item))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					Messages.noSuchElement("Unknown item",
							getName(item), getName(container)));
	}

	/**
	 * Throw {@link ModelException} of type {@link ModelErrorCode#MODEL_ILLEGAL_MEMBER}
	 * if the specified {@code container} does {@link Container#containsItem(Item) contain}
	 * the given {@code item}.
	 *
	 * @param container
	 */
	public static void checkNotContainsItem(Container container, Item item) {
		requireNonNull(container);
		requireNonNull(item);

		if(container.containsItem(item))
			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
					Messages.unexpectedElement("Item already present",
							getName(item), getName(container)));
	}

//	public static void checkNonVirtualRoot(Item node, Structure structure) {
//		if(structure.getVirtualRoot()==node)
//			throw new ModelException(ModelErrorCode.MODEL_ILLEGAL_MEMBER,
//					"Method not designed for virtual root node");
//	}
}
