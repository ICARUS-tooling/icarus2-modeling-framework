/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.query.api.engine.ext;

import javax.annotation.Nullable;

import de.ims.icarus2.query.api.exp.Environment;

/**
 * @author Markus Gärtner
 *
 */
public interface EngineConfigurator {

	/**
	 * Set or override the specified property with a new value.
	 * Using a {@code null} value will remove the property from
	 * the configuration.
	 *
	 * @param key
	 * @param value
	 */
	EngineConfigurator setProperty(String key, @Nullable Object value);

	/**
	 * Switch on or off the specified switch.
	 * @param name
	 * @param active
	 */
	EngineConfigurator setSwitch(String name, boolean active);

	/**
	 * Adds a set of new environments.
	 *
	 * @param environments non-empty set of new environments to add
	 */
	EngineConfigurator addEnvironment(Environment...environments);
}
