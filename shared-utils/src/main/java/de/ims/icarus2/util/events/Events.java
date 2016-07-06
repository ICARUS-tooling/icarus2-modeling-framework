/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
 *
 * $Revision: 380 $
 *
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
