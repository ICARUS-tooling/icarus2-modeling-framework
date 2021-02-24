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
package de.ims.icarus2.model.api.edit.io;

import java.io.IOException;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.edit.change.AtomicChange;
import de.ims.icarus2.model.api.edit.change.AtomicChangeType;

/**
 * @author Markus Gärtner
 *
 */
public interface SerializableAtomicChange extends AtomicChange {

	AtomicChangeType getType();

	/**
	 * Serialize the content of this change object to the specified {@link ChangeWriter writer}.
	 * @param writer the writer to use, never {@code null}
	 * @throws IOException in case an unrecoverable error happens during serialization.
	 * @throws ModelException of type {@link ModelErrorCode#MODEL_CORRUPTED_STATE} iff the
	 * internal state prevents serialization.
	 */
	void writeChange(ChangeWriter writer) throws IOException;

	/**
	 * Deserialize the content of this change fom the given [{@link ChangeReader reader}.
	 * @param reader the reader to use, never {@code null}
	 * @throws IOException in case an unrecoverable error happens during serialization.
	 * @throws ModelException of type {@link ModelErrorCode#MODEL_CORRUPTED_STATE} iff
	 * the state from the given reader is compromised.
	 */
	void readChange(ChangeReader reader) throws IOException;
}
