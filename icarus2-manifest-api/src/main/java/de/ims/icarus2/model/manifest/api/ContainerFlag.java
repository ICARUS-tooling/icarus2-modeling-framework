/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
public enum ContainerFlag implements StringResource, Flag {

	/**
	 * Containers are allowed to contain virtual items
	 * (i.e. those items have no fixed <i>physical location</i> in the surrounding
	 * abstract corpus resource).
	 */
	VIRTUAL("virtual"),

	/**
	 * Specifies whether or not containers are allowed
	 * to have an item count of {@code 0}, i.e. being empty.
	 * Note that this restriction is only used when the framework verifies a container
	 * constructed by a driver implementation or when checking whether or not an
	 * attempted (user-)action is feasible.
	 */
	EMPTY("empty"),

	/**
	 * Arrangement of items in this container can be altered by the user.
	 */
	NON_STATIC("non-static"),

	/**
	 * Specifies whether a container requires all its contained items to be unique.
	 * Note that this is only relevant for aggregating containers which recruit
	 * their content from foreign containers. The items owned by a container (i.e.
	 * the one it introduces to the corpus framework) are <b>always</b> unique!
	 */
	DUPLICATES("duplicates"),

	/**
	 * Specifies whether or not a container requires its content to be arranged according
	 * to the default item ordering defined by the model. Note that this is only of
	 * interest for aggregating containers, since the top level elements of each layer
	 * must always be ordered!
	 */
	ORDERED("ordered"),

	;
	private final String xmlForm;

	private ContainerFlag(String xmlForm) {
		this.xmlForm = xmlForm;
	}

	/**
	 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return xmlForm;
	}

	private static LazyStore<ContainerFlag,String> store = LazyStore.forStringResource(
			ContainerFlag.class, true);

	public static ContainerFlag parseContainerFlag(String s) {
		return store.lookup(s);
	}
}