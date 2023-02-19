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

	/**
	 * CHanges are expected to provide a simple human-readable representation
	 * of their configuration via the {@code toString} method.
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	String toString();
}