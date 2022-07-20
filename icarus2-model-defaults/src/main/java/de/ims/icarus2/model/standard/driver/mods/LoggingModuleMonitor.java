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
package de.ims.icarus2.model.standard.driver.mods;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import org.slf4j.Logger;

import de.ims.icarus2.model.api.driver.mods.DriverModule;
import de.ims.icarus2.model.api.driver.mods.ModuleMonitor;

/**
 * @author Markus Gärtner
 *
 */
public class LoggingModuleMonitor implements ModuleMonitor {

	private final Logger log;
	private final boolean isPreparation;
	private final Function<DriverModule, String> nameGen;

	public LoggingModuleMonitor(Logger log, boolean isPreparation,
			Function<DriverModule, String> nameGen) {
		this.log = requireNonNull(log);
		this.isPreparation = isPreparation;
		this.nameGen = requireNonNull(nameGen);
	}

	private String task() {
		return isPreparation ? "preparing" : "resetting" ;
	}

	private String nameFor(DriverModule module) {
		String name = nameGen.apply(module);
		if(name==null) {
			name = "unknown@"+module.getClass().getName();
		}
		return name;
	}

	@Override
	public void start(DriverModule module) {
		log.info("Started {} module '{}'", task(), nameFor(module));
	}

	@Override
	public void progress(DriverModule module) {
		log.info("Progressed {} module '{}'", task(), nameFor(module));
	}

	@Override
	public void end(DriverModule module) {
		log.info("Finished {}  module '{}'", task(), nameFor(module));
	}

	@Override
	public void error(DriverModule module, Exception e) {
		log.error("Error {} module '{}': {}", task(), nameFor(module), e.getMessage(), e);
	}

}
