/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.List;

import javax.annotation.Nullable;

import de.ims.icarus2.model.api.driver.mods.DriverModule;
import de.ims.icarus2.model.api.driver.mods.ModuleMonitor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
public class SimpleModuleMonitor implements ModuleMonitor {

	private final @Nullable ModuleMonitor monitor;
	private List<DriverModule> failedModules = new ObjectArrayList<>();

	public SimpleModuleMonitor() {
		monitor = null;
	}

	public SimpleModuleMonitor(ModuleMonitor monitor) {
		this.monitor = requireNonNull(monitor);
	}

	@Override
	public void start(DriverModule module) {
		if(monitor!=null) {
			monitor.start(module);
		}
	}

	@Override
	public void progress(DriverModule module) {
		if(monitor!=null) {
			monitor.progress(module);
		}
	}

	@Override
	public void end(DriverModule module) {
		if(monitor!=null) {
			monitor.end(module);
		}
	}

	@Override
	public void error(DriverModule module, Exception e) {
		failedModules.add(module);
		if(monitor!=null) {
			monitor.error(module, e);
		}
	}

	/** Returns the list of failed modules as-is, so without defensive copying. */
	public List<DriverModule> getFailedModules() {
		return failedModules;
	}

	public boolean hasFailedModules() { return !failedModules.isEmpty(); }

	public void reset() {
		failedModules.clear();
	}
}
