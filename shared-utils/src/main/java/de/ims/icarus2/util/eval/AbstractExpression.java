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

import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractExpression implements Expression {

	private Class<?> returnType;
	private Environment environment;
	private String code;

	public void setReturnType(Class<?> returnType) {
		checkNotNull(returnType);
		checkState("Return type already defined", this.returnType==null);

		this.returnType = returnType;
	}

	public void setCode(String code) {
		checkNotNull(code);
		checkState("Code already defined", this.code==null);

		this.code = code;
	}

	public void setEnvironment(Environment environment) {
		checkNotNull(environment);
		checkState("Environment already defined", this.environment==null);

		this.environment = environment;
	}

	/**
	 * @return the returnType
	 */
	@Override
	public Class<?> getReturnType() {
		return returnType;
	}

	/**
	 * @return the environment
	 */
	@Override
	public Environment getEnvironment() {
		return environment;
	}

	/**
	 * @return the code
	 */
	@Override
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
		} else if(obj instanceof AbstractExpression) {
			return code.equals(((AbstractExpression)obj).code);
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
