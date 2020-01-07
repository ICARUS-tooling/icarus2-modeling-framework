/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.util.Flag;
import de.ims.icarus2.util.LazyStore;
import de.ims.icarus2.util.strings.StringResource;

/**
 *
 * @author Markus Gärtner
 *
 */
public enum StructureFlag implements StringResource, Flag {

	/**
	 * Specifies whether edges are allowed to be virtual (i.e. they
	 * may have virtual items assigned to them as terminals). Note
	 * that this restriction only applies to edges that are <b>not</b>
	 * attached to the virtual root node of a structure!
	 */
	VIRTUAL("virtual"),

	/**
	 * Specifies whether or not structures may contain additional nodes
	 * (virtual or not) that are not already part of their respective base
	 * containers. For example constituency parses contain constituents
	 * as nodes besides the terminals defined by the basic tokens in the
	 * underlying sentences.
	 *
	 * @see #VIRTUAL
	 * @see ContainerFlag#VIRTUAL
	 */
	AUGMENTED("augmented"),

	/**
	 * Specifies whether or not structures are allowed
	 * to have an edge count of {@code 0}, i.e. being empty.
	 */
	EMPTY("empty"),

	/**
	 * Arrangement of edges in a structure can be altered by the user.
	 * <p>
	 * Note that the default assumption is that edges are immutable to
	 * avoid verbose declaration of this flag (since in most cases it
	 * is indeed safe to assume immutable data, which prevents driver
	 * implementations from having to deal with complexity of mutable
	 * corpus data).
	 */
	NON_STATIC("non-static"),

	/**
	 * Signals that edges in a structure are allowed to have the same
	 * item assigned as source and target terminal.
	 */
	LOOPS("loops"),

	/**
	 * Signals that for a given pair of terminals there may exist more than
	 * one edge between them with the same direction.
	 */
	PARALLEL("parallel"),

	/**
	 * Specifies whether or not a structure requires its edges to be arranged according
	 * to the default item ordering defined by the model.
	 */
	ORDERED("ordered"),

	/**
	 * Signals that a structure is not required to use all its nodes.
	 * If set a structure is not allowed to host nodes for which the
	 * {@link Structure#getEdgeCount(de.ims.icarus2.model.api.members.Item) edge count}
	 * is {@code 0}. This property exists to enable optimization for very compact
	 * implementations of certain structure types like {@link StructureType#CHAIN chains}
	 * where the total number of possible edges is fixed by the number of nodes.
	 */
	PARTIAL("partial"),

	/**
	 * Specifies whether or not a structure may have more than {@code 1} edge
	 * assigned to its virtual root node (effectively meaning that it has in fact
	 * several "real" root nodes).
	 */
	MULTI_ROOT("multi-root"),

	/**
	 * TODO explain projectivity (within the context of dependency relations?)
	 */
	PROJECTIVE("projective"),

	;
	private final String xmlForm;

	private StructureFlag(String xmlForm) {
		this.xmlForm = xmlForm;
	}

	/**
	 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return xmlForm;
	}


	private static LazyStore<StructureFlag, String> store = LazyStore.forStringResource(StructureFlag.class);

	public static StructureFlag parseStructureFlag(String s) {
		return store.lookup(s);
	}
}