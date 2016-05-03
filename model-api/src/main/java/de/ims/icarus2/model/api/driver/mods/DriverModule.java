/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.api.driver.mods;

import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.util.Part;
import de.ims.icarus2.util.id.Identifiable;

/**
 * @author Markus Gärtner
 * @version $Id$
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

	void prepare(ModuleMonitor monitor) throws InterruptedException;

	void reset(ModuleMonitor monitor) throws InterruptedException;

	/**
	 * In case the module is busy, cancels the current task.
	 */
	void cancel();
}
