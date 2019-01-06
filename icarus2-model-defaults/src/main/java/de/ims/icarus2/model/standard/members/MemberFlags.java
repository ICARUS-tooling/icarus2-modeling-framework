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
package de.ims.icarus2.model.standard.members;

import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.item.Item;

/**
 * Provides global constants and methods to access and modify the flags of a corpus member.
 * Members are divided into two groups, namely {@code layers} and {@code elements}. The {@code layers}
 * group includes all types of members which fall into the {@link Layer} hierarchy, and {@code elements}
 * are all (indirect) derivations of {@link Item}.
 * <p>
 * Overview of bits
 * <table>
 * <tr><th>Bit-interval</th><th>Description</th></tr>
 * <tr><td>0-4</td><td></td></tr>
 * </table>
 * <p>
 * Meanings of individual bits
 * <table>
 * <tr><th>Bit-index</th><th>Layer</th><th>Element</th></tr>
 * <tr><td>0 (LSB)</td><td></td><td></td></tr>
 * <tr><td>1</td><td></td><td></td></tr>
 * <tr><td>2</td><td></td><td></td></tr>
 * <tr><td>3</td><td></td><td></td></tr>
 * <tr><td>4</td><td></td><td></td></tr>
 * <tr><td>5</td><td></td><td></td></tr>
 * </table>
 *
 * @author Markus Gärtner
 *
 */
public class MemberFlags {

	public static final int EMPTY_FLAGS = 0;

	// Item flags
	public static final int ITEM_ALIVE = (1<<1);
	public static final int ITEM_LOCKED = (1<<2);
	public static final int ITEM_DIRTY = (1<<3);

	// Container flags
	public static final int ITEMS_COMPLETE = (1<<4);

	// Structure flags
	public static final int EDGES_COMPLETE = (1<<5);
	public static final int STRUCTURE_AUGMENTED = (1<<6);

	public static final int ITEM_DEFAULT_FLAGS = ITEM_ALIVE;

	// Read methods

	public static boolean isItemAlive(int b) {
		return (b & ITEM_ALIVE) == ITEM_ALIVE;
	}

	public static boolean isItemLocked(int b) {
		return (b & ITEM_LOCKED) == ITEM_LOCKED;
	}

	public static boolean isItemDirty(int b) {
		return (b & ITEM_DIRTY) == ITEM_DIRTY;
	}

	public static boolean isItemUsable(int b) {
		return (b & ITEM_DEFAULT_FLAGS) == ITEM_DEFAULT_FLAGS;
	}

	public static boolean isItemsComplete(int b) {
		return (b & ITEMS_COMPLETE) == ITEMS_COMPLETE;
	}

	public static boolean isEdgesComplete(int b) {
		return (b & EDGES_COMPLETE) == EDGES_COMPLETE;
	}

	public static boolean isStructureAugmented(int b) {
		return (b & STRUCTURE_AUGMENTED) == STRUCTURE_AUGMENTED;
	}

	// Write methods

//	private static int setFlag(int b, int flag, boolean active) {
//		return (int) (active ? (b|flag) : (b & ~flag));
//	}
//
//	public static int setItemAlive(int b, boolean alive) {
//		return setFlag(b, ITEM_ALIVE, alive);
//	}
//
//	public static int setItemLocked(int b, boolean locked) {
//		return setFlag(b, ITEM_LOCKED, locked);
//	}
//
//	public static int setItemDirty(int b, boolean dirty) {
//		return setFlag(b, ITEM_DIRTY, dirty);
//	}
//
//	public static int setItemsComplete(int b, boolean complete) {
//		return setFlag(b, ITEMS_COMPLETE, complete);
//	}
//
//	public static int setEdgesComplete(int b, boolean complete) {
//		return setFlag(b, EDGES_COMPLETE, complete);
//	}
//
//	public static int setStructureAugmented(int b, boolean complete) {
//		return setFlag(b, STRUCTURE_AUGMENTED, complete);
//	}
}
