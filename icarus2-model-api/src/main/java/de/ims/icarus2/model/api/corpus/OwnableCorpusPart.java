/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.corpus;

import java.util.Set;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.util.Changeable;
import de.ims.icarus2.util.Part;

/**
 * @author Markus Gärtner
 *
 */
public interface OwnableCorpusPart extends AutoCloseable, Changeable, Part<Corpus> {

	// Destruction support

	/**
	 * Attempts to acquire shared ownership of this corpus part by the given {@code owner}.
	 * If the given owner already holds shared ownership of this corpus part, the method
	 * simply returns.
	 *
	 * @param owner
	 * @throws NullPointerException if the {@code owner} argument is {@code null}.
	 * @throws ModelException of type {@link ModelErrorCode#VIEW_CLOSED}
	 * 			if {@link #close()} has already been called on this
	 * 			corpus part and it's in the process of releasing its data.
	 */
	void acquire(CorpusOwner owner);

	/**
	 * Removes the given {@code owner}'s shared ownership on this corpus part. If no
	 * more owners are registered to this corpus part, a subsequent call to {@link #closable()}
	 * will return {@code true}.
	 *
	 * @param owner
	 * @throws NullPointerException if the {@code owner} argument is {@code null}.
	 * @throws ModelException of type {@link ModelErrorCode#VIEW_CLOSED}
	 * 			if {@link #close()} has already been called on this
	 * 			corpus part and it's in the process of releasing its data.
	 * @throws ModelException of type {@link GlobalErrorCode#INVALID_INPUT}
	 * 			if the given owner does not hold shared ownership of this corpus part.
	 */
	void release(CorpusOwner owner);

	/**
	 * Returns an immutable set view of all the owners currently registered with this corpus part.
	 */
	Set<CorpusOwner> getOwners();

	/**
	 * Checks whether or not the corpus part is currently closable, i.e.
	 * there are no more registered owners expressing interest in this part.
	 * A return value of {@code true} means that a subsequent call to {@link #close()}
	 * will <i>most likely</i> succeed and not have to ask owners to {@link #release(CorpusOwner)}
	 * their hold on this part.
	 *
	 * @return
	 */
	boolean closable();

	/**
	 * Returns {@code true} if and only if this part is neither closed nor in the process of closing.
	 * @return
	 */
	boolean isActive();

	@Override
	void close();
}
