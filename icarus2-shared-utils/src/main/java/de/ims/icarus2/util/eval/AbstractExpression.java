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
package de.ims.icarus2.util.eval;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractExpression implements Expression {

	private Class<?> returnType;
	private Environment environment;
	private String code;

	public void setReturnType(Class<?> returnType) {
		requireNonNull(returnType);
		checkState("Return type already defined", this.returnType==null);

		this.returnType = returnType;
	}

	public void setCode(String code) {
		requireNonNull(code);
		checkState("Code already defined", this.code==null);

		this.code = code;
	}

	public void setEnvironment(Environment environment) {
		requireNonNull(environment);
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
