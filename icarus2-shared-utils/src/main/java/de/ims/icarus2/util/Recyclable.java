/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.util;

/**
 * @author Markus Gärtner
 *
 */
public interface Recyclable {

	/**
	 * Erases all internal states that might be affected by foreign objects and
	 * prepares the item for being added to an object pool.
	 */
	void recycle();

	/**
	 * Verifies the internal state of the item after it has been revived from an
	 * object pool. Returns {@code true} if and only if all internal properties are
	 * properly initialized and the item is in a valid state.
	 * <p>
	 * All internal refreshing logic should go into this method to ensure a pooled
	 * object gets revived properly. Note that this method should be called <b>after</b>
	 * a pooled object has been filled with new data!
	 * <p>
	 * It is perfectly legal for an object to remain in an inconsistent state once this
	 * method has detected a corrupted property and returned {@code false}. Once the
	 * consistency check failed, surrounding client code should treat the object as
	 * garbage and either discard it or recycle it again. Under no circumstances should
	 * such an object be used in the regular lifecycle of a client!
	 *
	 * @return
	 */
	boolean revive();
}
