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
package de.ims.icarus2.model.api.edit.io;

import de.ims.icarus2.model.api.edit.change.AtomicChange;

/**
 * @author Markus Gärtner
 *
 */
public interface SerializableAtomicChange extends AtomicChange {


	/**
	 * Optional method for creating a unified serializable representation of this
	 * change. The change implementation is to create a blank new proxy and fill its
	 * fields with the appropriate contextual information for the type of change it
	 * models.
	 *
	 * @return a filled out {@link AtomicChangeProxy} instance containing all the information
	 * required to reproduce this change or {@code null} if creating such a proxy is not supported.
	 */
	AtomicChangeProxy toProxy();
}
