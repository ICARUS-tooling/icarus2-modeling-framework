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
package de.ims.icarus2.util;

/**
 * Models an environment-aware object that receives notifications about said environment
 * when it is being added or removed. Each such object can have at most one environment
 * associated with it!
 * <p>
 * Note that no method exists in this interface to actually fetch the environment instance.
 * The decision whether or not to expose the associated environment is left to the respective
 * implementation or derived interfaces.
 *
 * @param <O> type of the owning context (environment)
 *
 * @author Markus Gärtner
 *
 */
public interface Part<O extends Object> {

	void addNotify(O owner);

	void removeNotify(O owner);

	boolean isAdded();
}
