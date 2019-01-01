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
package de.ims.icarus2.model.api.members.structure;

import de.ims.icarus2.model.api.edit.EditAction;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 *
 */
public enum StructureEditAction implements EditAction<Structure> {

	ADD_EDGE(EditType.ADD, false),
	ADD_EDGE_SET(EditType.ADD, true),
	REMOVE_EDGE(EditType.REMOVE, false),
	REMOVE_EDGE_SET(EditType.REMOVE, true),
	MOVE_EDGE(EditType.MOVE, false),
	CHANGE_SOURCE_TERMINAL(EditType.CHANGE_TERMINAL, false),
	CHANGE_TARGET_TERMINAL(EditType.CHANGE_TERMINAL, false),
	;

	private final EditType type;
	private final boolean batch;

	private StructureEditAction(EditType type, boolean batch) {
		this.type = type;
		this.batch = batch;
	}

	/**
	 * @see de.ims.icarus2.model.api.edit.EditAction#getSourceClass()
	 */
	@Override
	public Class<Structure> getSourceClass() {
		return Structure.class;
	}

	/**
	 * @see de.ims.icarus2.model.api.edit.EditAction#getElementClass()
	 */
	@Override
	public Class<? extends Item> getElementClass() {
		return type==EditType.CHANGE_TERMINAL ? Item.class : Edge.class;
	}

	/**
	 * @see de.ims.icarus2.model.api.edit.EditAction#getType()
	 */
	@Override
	public EditType getType() {
		return type;
	}

	/**
	 * @see de.ims.icarus2.model.api.edit.EditAction#isBatch()
	 */
	@Override
	public boolean isBatch() {
		return batch;
	}
}
