/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
 */
package de.ims.icarus2.util.eval;

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
	public Object evaluate();
}
