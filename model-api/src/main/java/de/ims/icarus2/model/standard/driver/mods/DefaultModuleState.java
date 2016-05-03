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
package de.ims.icarus2.model.standard.driver.mods;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import de.ims.icarus2.model.api.driver.mods.ModuleState;
import de.ims.icarus2.util.id.StaticIdentity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultModuleState extends StaticIdentity implements ModuleState {

	private boolean indeterminate;
	private int progress;
	private int goal;

	private Object[] arguments;

	/**
	 * @param id
	 * @param owner
	 */
	public DefaultModuleState(Object owner) {
		super(owner);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mods.ModuleState#isIndeterminate()
	 */
	@Override
	public boolean isIndeterminate() {
		return indeterminate;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mods.ModuleState#getProgress()
	 */
	@Override
	public int getProgress() {
		return progress;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mods.ModuleState#getGoal()
	 */
	@Override
	public int getGoal() {
		return goal;
	}

	@Override
	public Object[] getArguments() {
		return arguments;
	}

	public void setIndeterminate(boolean indeterminate) {
		this.indeterminate = indeterminate;
	}

	public void setProgress(int progress) {
		checkArgument(progress>=0);
		this.progress = progress;
	}

	public void setGoal(int goal) {
		checkArgument(goal>=0);
		this.goal = goal;
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	public void setFormatted(String id, Object...args) {
		setId(id);
		setName(null);
		setDescription(null);
		setArguments(args);
	}

	public void setState(String id) {
		setId(id);
		setName(null);
		setDescription(null);
		setArguments(null);
	}

	public void setState(String id, String name, String description, Object...args) {
		setId(id);
		setName(name);
		setDescription(description);
		setArguments(args);
	}

	public void setProgress(int progress, int goal) {
		setGoal(goal);
		setProgress(progress);
	}

	public void step() {
		checkState(progress<goal);
		progress++;
	}

}
