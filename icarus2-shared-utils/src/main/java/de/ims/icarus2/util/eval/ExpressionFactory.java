/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.eval.var.VariableDescriptor;
import de.ims.icarus2.util.eval.var.VariableDescriptor.Mode;

/**
 * @author Markus Gärtner
 *
 */
public abstract class ExpressionFactory extends Environment {

	private Map<String, VariableDescriptor> variables = new LinkedHashMap<>();

	private String code;
	private Environment environment;
	private Class<?> returnType;

	private final String name;

	/**
	 * @param name The unique name used for this type of factory
	 */
	protected ExpressionFactory(String name) {
		requireNonNull(name);

		this.name = name;
	}

	public final String getName() {
		return name;
	}

	public VariableDescriptor addVariable(String id, Class<?> namespace, Mode mode, boolean nullable) {
		requireNonNull(id);
		requireNonNull(namespace);
		requireNonNull(mode);

		if(variables.containsKey(id))
			throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT, "Duplicate variable id: "+id);

		VariableDescriptor variable = new VariableDescriptor(id, namespace, mode, nullable);

		variables.put(id, variable);

		return variable;
	}

	public VariableDescriptor addVariable(String id, Class<?> namespace, Mode mode) {
		return addVariable(id, namespace, mode, false);
	}

	public VariableDescriptor addInputVariable(String id, Class<?> namespace) {
		return addVariable(id, namespace, Mode.IN, false);
	}

	public VariableDescriptor addOutputVariable(String id, Class<?> namespace) {
		return addVariable(id, namespace, Mode.OUT, false);
	}

	public VariableDescriptor addInOutVariable(String id, Class<?> namespace) {
		return addVariable(id, namespace, Mode.IN_OUT, false);
	}

	public Collection<VariableDescriptor> getVariables() {
		return CollectionUtils.getCollectionProxy(variables.values());
	}

	public VariableDescriptor getVariable(String name) {
		return variables.get(name);
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
		return environment==null ? this : environment;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		requireNonNull(code);

		this.code = code;
	}

	/**
	 * @param environment the environment to set
	 */
	public void setEnvironment(Environment environment) {
		requireNonNull(environment);

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
		requireNonNull(returnType);

		this.returnType = returnType;
	}
}
