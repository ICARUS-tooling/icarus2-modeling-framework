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
 *
 */
package de.ims.icarus2.util.eval;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class Expression {

	private Class<?> returnType;
	private Environment environment;
	private final String code;
	private final List<Variable> variables = new ArrayList<>();

	Expression(String text, int flags) {
		if (text == null)
			throw new NullPointerException("Invalid text"); //$NON-NLS-1$

		code = text;
		//TODO apply flags
	}

	void addVariable(String name, Class<?> namespaceClass) {
		variables.add(new Variable(name, this, namespaceClass));
	}

	//FIXME specify exception type
	void compile() throws Exception {
	}

	public List<Variable> getVariables() {
		return CollectionUtils.getListProxy(variables);
	}

	/**
	 * @return the returnType
	 */
	public Class<?> getReturnType() {
		return returnType;
	}

	/**
	 * @return the environment
	 */
	public Environment getEnvironment() {
		return environment;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return code.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} else if(obj instanceof Expression) {
			return code.equals(((Expression)obj).code);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Expression@"+returnType.getName()+" ["+code.length()+" code characters]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
