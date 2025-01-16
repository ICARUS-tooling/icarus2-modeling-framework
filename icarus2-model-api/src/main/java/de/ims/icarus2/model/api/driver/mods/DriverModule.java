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

import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest;
import de.ims.icarus2.util.Part;
import de.ims.icarus2.util.id.Identifiable;

/**
 * Models a  single module for a {@link Driver} that can be used to
 * perform specific tasks.
 *
 * @author Markus Gärtner
 *
 */
public interface DriverModule extends Identifiable, Part<Driver> {

	/**
	 * Returns the hosting driver instance
	 *
	 * @return
	 */
	Driver getDriver();

	/**
	 * Returns {@code true} iff this module is fully initialized and
	 * prepared.
	 *
	 * @return
	 */
	boolean isReady();

	/**
	 * Returns whether or not the module is carrying out an extensive background
	 * task. Being busy means that each call to {@link #prepare(ModuleMonitor)}
	 * is going to result in an exception. On the other hand only a busy module
	 * can have its current task {@link #cancel() cancelled}.
	 *
	 * @return
	 */
	boolean isBusy();

	/**
	 * Returns information about the current state of the module, suitable to
	 * generated localized user feedback.
	 *
	 * @return
	 */
	ModuleState getState();

	void prepare(ModuleManifest manifest, ModuleMonitor monitor) throws InterruptedException;

	void reset(ModuleMonitor monitor) throws InterruptedException;

	/**
	 * In case the module is busy, cancels the current task. This method may cause a
	 * thread interruption if the module is running on a separate thread.
	 */
	void cancel();

	/**
	 * Marker interface to signal that a implementing module should be preloaded
	 * as soon as possible
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface PreloadedModule extends DriverModule {
		// marker interface
	}
}
