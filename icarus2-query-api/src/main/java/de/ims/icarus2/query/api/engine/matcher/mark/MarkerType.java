/*
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
/**
 *
 */
package de.ims.icarus2.query.api.engine.matcher.mark;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;

/**
 * @author Markus Gärtner
 *
 */
public enum MarkerType {

	SEQUENCE("sequence", Container.class, true, false, true),
	TREE_HIERARCHY("tree-hierarchy", Item.class, true, false, true),
	TREE_GENERATION("tree-generation", Item.class, false, true, true),
	TREE_PATH("tree-path", Structure.class, true, true, false)
	;

	private final String label;
	private final Class<? extends Item> hostClass;
	private final boolean horizontal, vertical, range;

	private MarkerType(String label, Class<? extends Item> hostClass, boolean horizontal,
			boolean vertical, boolean range) {
		this.label = checkNotEmpty(label);
		this.hostClass = requireNonNull(hostClass);
		this.horizontal = horizontal;
		this.vertical = vertical;
		this.range = range;
	}

	/** Unique label to identify this type */
	public String getLabel() { return label; }

	/** Type of host that defines the context for this marker. */
	public Class<? extends Item> getHostClass() { return hostClass; }

	/** Flag to signal whether this marker controls horizontal positioning aspects. */
	public boolean isHorizontal() { return horizontal; }

	/** Flag to signal whether this marker controls vertical positioning aspects. */
	public boolean isVertical() { return vertical; }

	/**
	 * Flag to signal whether this marker describes an actual range of legal positions
	 * along a single dimension (horizontal or vertical). Markers that do so can be
	 * effectively used to produce index intervals to restrict the search space.
	 * Markers that do <b>not</b>, require more sophisticated handling and might incur
	 * significant additional pre-processing cost to evaluate.
	 */
	public boolean isRange() { return range; }
}
