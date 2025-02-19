/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.driver.mods;

import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 *
 */
public interface ModuleState extends Identity {

	boolean isIndeterminate();

	int getProgress();

	int getGoal();

	/**
	 * Returns arguments to be passed to facilities that
	 * create localized texts for the user. Modules should
	 * carefully outline the exact order of arguments they
	 * wish to pass to client code!
	 *
	 * @return
	 */
	Object[] getArguments();
}
