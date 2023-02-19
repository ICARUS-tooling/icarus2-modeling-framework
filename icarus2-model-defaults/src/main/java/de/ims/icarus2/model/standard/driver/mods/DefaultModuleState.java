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
package de.ims.icarus2.model.standard.driver.mods;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;

import de.ims.icarus2.model.api.driver.mods.ModuleState;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.id.StaticIdentity;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(ModuleState.class)
public class DefaultModuleState extends StaticIdentity implements ModuleState {

	private boolean indeterminate;
	private int progress;
	private int goal;

	private Object[] arguments;

	/**
	 * @param source
	 */
	public DefaultModuleState(Identity source) {
		super(source);
	}

	/**
	 * @param id
	 */
	public DefaultModuleState(String id) {
		super(id);
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

	public DefaultModuleState setIndeterminate(boolean indeterminate) {
		this.indeterminate = indeterminate;
		return this;
	}

	public DefaultModuleState setProgress(int progress) {
		checkArgument(progress>=0);
		this.progress = progress;
		return this;
	}

	public DefaultModuleState setGoal(int goal) {
		checkArgument(goal>=0);
		this.goal = goal;
		return this;
	}

	public DefaultModuleState setArguments(Object[] arguments) {
		this.arguments = arguments;
		return this;
	}

	public DefaultModuleState setFormatted(String id, Object...args) {
		setId(id);
		setName(null);
		setDescription(null);
		setArguments(args);
		return this;
	}

	public DefaultModuleState setState(String id) {
		setId(id);
		setName(null);
		setDescription(null);
		setArguments(null);
		return this;
	}

	public DefaultModuleState setState(String id, String name, String description, Object...args) {
		setId(id);
		setName(name);
		setDescription(description);
		setArguments(args);
		return this;
	}

	public DefaultModuleState setProgress(int progress, int goal) {
		setGoal(goal);
		setProgress(progress);
		return this;
	}

	public void step() {
		checkState(progress<goal);
		progress++;
	}

}
