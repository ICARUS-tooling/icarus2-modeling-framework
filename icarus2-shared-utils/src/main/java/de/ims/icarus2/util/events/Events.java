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
package de.ims.icarus2.util.events;

/**
 * @author Markus Gärtner
 *
 */
public interface Events {

	public static final String PROPERTY = "property"; //$NON-NLS-1$
	public static final String ADD = "add"; //$NON-NLS-1$
	public static final String ADDED = "added"; //$NON-NLS-1$
	public static final String REMOVE = "remove"; //$NON-NLS-1$
	public static final String REMOVED = "removed"; //$NON-NLS-1$
	public static final String INSERT = "insert"; //$NON-NLS-1$
	public static final String INSERTED = "inserted"; //$NON-NLS-1$
	public static final String MOVE = "move"; //$NON-NLS-1$
	public static final String MOVED = "moved"; //$NON-NLS-1$
	public static final String CLEAN = "clean"; //$NON-NLS-1$
	public static final String CLEANED = "cleaned"; //$NON-NLS-1$
	public static final String DELETE = "delete"; //$NON-NLS-1$
	public static final String DELETED = "deleted"; //$NON-NLS-1$
	public static final String CHANGE = "change"; //$NON-NLS-1$
	public static final String CHANGED = "changed"; //$NON-NLS-1$

	public static final String CLEAR = "clear"; //$NON-NLS-1$
	public static final String UNDO = "undo"; //$NON-NLS-1$
	public static final String REDO = "redo"; //$NON-NLS-1$

	public static final String LOAD = "load"; //$NON-NLS-1$
	public static final String LOADED = "loaded"; //$NON-NLS-1$
	public static final String SAVE = "save"; //$NON-NLS-1$
	public static final String SAVED = "saved"; //$NON-NLS-1$

	public static final String SELECTION_CHANGED = "selectionChanged"; //$NON-NLS-1$
}
