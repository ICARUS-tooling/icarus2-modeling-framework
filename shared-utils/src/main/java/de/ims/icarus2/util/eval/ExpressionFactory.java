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

 * $Revision: 380 $
 * $Date: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/eval/ExpressionFactory.java $
 *
 * $LastChangedDate: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $LastChangedRevision: 380 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.eval;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Markus Gärtner
 * @version $Id: ExpressionFactory.java 380 2015-04-02 01:28:48Z mcgaerty $
 *
 */
public class ExpressionFactory {

	private Map<String, Class<?>> variables = new LinkedHashMap<>();

	private String code;
	private Environment environment;

	private Expression expression;

	public void addVariable(String id, Class<?> namespace) {
		if (id == null)
			throw new NullPointerException("Invalid id");
		if (namespace == null)
			throw new NullPointerException("Invalid namespace");

		if(variables.containsKey(id))
			throw new IllegalArgumentException("Duplicate variable id: "+id);

		variables.put(id, namespace);
	}

	public Expression build() {
		if(expression==null) {
			expression = new Expression(code, 0);

			for(Entry<String, Class<?>> entry : variables.entrySet()) {
				expression.addVariable(entry.getKey(), entry.getValue());
			}
		}

		return expression;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return the environment
	 */
	public Environment getEnvironment() {
		return environment;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		if (code == null)
			throw new NullPointerException("Invalid code");

		this.code = code;
	}

	/**
	 * @param environment the environment to set
	 */
	public void setEnvironment(Environment environment) {
		if (environment == null)
			throw new NullPointerException("Invalid environment");

		this.environment = environment;
	}
}
