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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.eval.var.VariableDescriptor;

/**
 * @author Markus Gärtner
 *
 */
public abstract class ExpressionFactory {

	private Map<String, VariableDescriptor> variables = new LinkedHashMap<>();

	private String code;
	private Environment environment;
	private Class<?> returnType;

	private final String name;

	/**
	 * @param name The unique name used for this type of factory
	 */
	protected ExpressionFactory(String name) {
		checkNotNull(name);

		this.name = name;
	}

	public final String getName() {
		return name;
	}

	public VariableDescriptor addVariable(String id, boolean nullable, Class<?> namespace) {
		checkNotNull(id);
		checkNotNull(namespace);

		if(variables.containsKey(id))
			throw new IcarusException(GlobalErrorCode.INVALID_INPUT, "Duplicate variable id: "+id);

		VariableDescriptor variable = new VariableDescriptor(id, namespace, nullable);

		variables.put(id, variable);

		return variable;
	}

	public VariableDescriptor addVariable(String id, Class<?> namespace) {
		return addVariable(id, false, namespace);
	}

	public Collection<VariableDescriptor> getVariables() {
		return CollectionUtils.getCollectionProxy(variables.values());
	}

	/**
	 * Compiles the {@link #getCode() code} currently stored in this factory
	 * into an executable {@link Expression expression}.
	 *
	 * @return
	 * @throws Exception
	 */
	public abstract Expression compile() throws Exception;

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
		checkNotNull(code);

		this.code = code;
	}

	/**
	 * @param environment the environment to set
	 */
	public void setEnvironment(Environment environment) {
		checkNotNull(environment);

		this.environment = environment;
	}

	/**
	 * @return the returnType
	 */
	public Class<?> getReturnType() {
		return returnType;
	}

	/**
	 * @param returnType the returnType to set
	 */
	public void setReturnType(Class<?> returnType) {
		checkNotNull(returnType);

		this.returnType = returnType;
	}
}
