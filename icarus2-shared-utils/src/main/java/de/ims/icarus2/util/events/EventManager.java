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
package de.ims.icarus2.util.events;

/**
 * @author Markus Gärtner
 *
 */
public interface EventManager {

	default void addListener(SimpleEventListener listener) {
		addListener(listener, null);
	}

	void addListener(SimpleEventListener listener, String eventName);


	/**
	 * Removes the given {@code SimpleEventListener} from all events
	 * it was previously registered for.
	 * @param listener the {@code SimpleEventListener} to be removed
	 */
	default void removeListener(SimpleEventListener listener) {
		removeListener(listener, null);
	}

	void removeListener(SimpleEventListener listener, String eventName);
}
