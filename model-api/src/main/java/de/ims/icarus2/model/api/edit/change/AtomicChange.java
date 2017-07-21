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
 */
package de.ims.icarus2.model.api.edit.change;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.CorpusMember;

/**
 * Describes an atomic change to the content of a corpus. As a general
 * rule a change should check its preconditions and fail without any
 * modifications when they are not entirely met.
 *
 * @author Markus Gärtner
 *
 */
public interface AtomicChange {

	/**
	 * Executes the change and modifies it internal information
	 * so that the next call to this method reverts the result.
	 * If a change requires a specific set of preconditions to be
	 * carried out, it should check them first or at least try to
	 * fail before making any permanent changes to the model, should
	 * any of those conditions be unfulfilled.
	 *
	 * @throws ModelException if the preconditions of this
	 * change are not met
	 */
	void execute();

	/**
	 * Returns the {@code CorpusMember} that this change affected.
	 * This is used to decide whether or not changes or entire edits
	 * should be purged from the undo history in the event of higher order
	 * changes like the removal of an entire context.
	 * @return
	 */
	CorpusMember getAffectedMember();

	//TODO why keep this method? (currently unused anyway)
	default boolean canReverse() {
		return true;
	}
}