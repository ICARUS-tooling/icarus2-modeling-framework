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
package de.ims.icarus2.util.strings;

/**
 * Helper interface to model objects that have a defined and
 * usually constant identifier or name. Avoids having to rely
 * on the {@link Object#toString()} method for simple log texts
 * or messages.
 *
 * @author Markus Gärtner
 *
 */
public interface NamedObject {

	/**
	 * Returns the {@code non-null} name or identifier of this object.
	 * @return
	 */
	String getName();
}
