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
package de.ims.icarus2.util;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <i>Publisher</i> counter-part of the {@link ChangeListener} interface.
 * A class implementing {@code Changeable} models an entity with at least one
 * aspect that has observable state changes.
 * <p>
 * Note that the <i>subscriber</i> part of the contract does not specify any
 * way to communicate the content or nature of the change that occurred but only
 * signals the event of the change itself.
 *
 * @author Markus Gärtner
 *
 */
public interface Changeable {

	/**
	 * Adds a new listener to receive {@link ChangeEvent events} from this publisher.
	 * Note that implementations should make sure that internal listener lists don't
	 * contain duplicates.
	 *
	 * @param listener
	 */
	void addChangeListener(ChangeListener listener);

	void removeChangeListener(ChangeListener listener);
}
