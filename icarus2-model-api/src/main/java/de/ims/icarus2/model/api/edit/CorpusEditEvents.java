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
package de.ims.icarus2.model.api.edit;

import javax.swing.event.UndoableEditListener;

/**
 * @author Markus Gärtner
 *
 */
public class CorpusEditEvents {

	/**
	 * Fired when the update level is increased
	 * <p>
	 * The "edit" property contains the edit in progress.<br>
	 * The "level" property contains the current update level
	 * which will be {@code 1} for the first update event being
	 * fired for an edit.
	 */
	public static final String BEGIN_UPDATE = "beginUpdate"; //$NON-NLS-1$

	/**
	 * Fired when the update level is decreased.
	 * <p>
	 * The "edit" property contains the edit in progress.<br>
	 * The "level" property contains the current update level
	 * which will be {@code 1} for the last update event being
	 * fired for an edit.
	 */
	public static final String END_UPDATE = "endUpdate"; //$NON-NLS-1$

	/**
	 * Fired when an atomic change is executed for the <b>first</b> time.
	 * <p>
	 * The "change" property contains the executed atomic change.
	 */
	public static final String EXECUTE = "execute"; //$NON-NLS-1$

	/**
	 * Fired after an edit has been executed but before it is
	 * dispatched to the {@link UndoableEditListener}s.
	 * <p>
	 * The "edit" property contains the undoable edit that
	 * is about to be committed.
	 */
	public static final String BEFORE_UNDO = "beforeUndo"; //$NON-NLS-1$

	/**
	 * Fired when an edit is committed and dispatched.
	 * <p>
	 * The "edit" property contains the undoable edit that
	 * was committed.
	 */
	public static final String UNDO = "undo"; //$NON-NLS-1$

	/**
	 * Fired when an undoable edit is executed, either via its
	 * {@link UndoableCorpusEdit#undo()} or {@link UndoableCorpusEdit#redo()}
	 * method.
	 * <p>
	 * The "edit" property contains the undoable edit that
	 * has been undone or redone.
	 */
	public static final String CHANGE = "change"; //$NON-NLS-1$
}
