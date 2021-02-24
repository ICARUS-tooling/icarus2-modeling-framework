/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.eval;

import java.util.concurrent.ExecutionException;

import de.ims.icarus2.util.eval.var.VariableSet;


/**
 * @author Markus Gärtner
 *
 */
public interface Expression {

	Class<?> getReturnType();

	String getCode();

	Environment getEnvironment();

	VariableSet getVariables();

	default boolean hasVariables() {
		return getVariables()!=null;
	}

	/**
	 * Executes the code stored in this expression and returns the result.
	 *
	 * @return
	 */
	//TODO determine a sensible exception type
	public Object evaluate() throws ExecutionException;
}
