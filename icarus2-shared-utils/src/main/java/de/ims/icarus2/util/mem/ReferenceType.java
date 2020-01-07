/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.mem;

/**
 * @author Markus Gärtner
 *
 */
public enum ReferenceType {

	/**
	 * Reference to another object that <i>owns</i> this object
	 * and as such should have already been part of the memory
	 * footprint calculation.
	 */
	UPLINK,
	/**
	 * Reference to another object that <i>is owned</i> by this
	 * object, i.e. a private buffer list.
	 */
	DOWNLINK,
	/**
	 * No special information is available.
	 */
	UNDEFINED;
}
