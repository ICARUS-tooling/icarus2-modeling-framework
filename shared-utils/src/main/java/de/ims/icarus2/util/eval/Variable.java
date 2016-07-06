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

/**
 * @author Markus Gärtner
 *
 */
public class Variable {

	private final Expression expression;
	private final String name;

	private final Class<?> namespaceClass;

	private Object value;


	Variable(String name, Expression expression, Class<?> namespaceClass) {
		if (expression == null)
			throw new NullPointerException("Invalid expression"); //$NON-NLS-1$

		//TODO further sanity checks?

		this.expression = expression;
		this.name = name;
		this.namespaceClass = namespaceClass;
	}


	/**
	 * @return the expression
	 */
	public Expression getExpression() {
		return expression;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @return the namespaceClass
	 */
	public Class<?> getNamespaceClass() {
		return namespaceClass;
	}


	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	void setValue(Object value) {
		if (value == null)
			throw new NullPointerException("Invalid value"); //$NON-NLS-1$

		this.value = value;
	}
}
