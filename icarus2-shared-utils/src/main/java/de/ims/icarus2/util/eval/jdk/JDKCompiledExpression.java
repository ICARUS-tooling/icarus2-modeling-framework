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
package de.ims.icarus2.util.eval.jdk;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import de.ims.icarus2.util.Mutable;
import de.ims.icarus2.util.eval.AbstractExpression;
import de.ims.icarus2.util.eval.var.SimpleVariableSet;
import de.ims.icarus2.util.eval.var.VariableDescriptor;
import de.ims.icarus2.util.eval.var.VariableSet;

/**
 * @author Markus Gärtner
 *
 */
public abstract class JDKCompiledExpression extends AbstractExpression {

	protected VariableSet __variables__;
	protected Object __result__;


	public void setVariables(Collection<? extends VariableDescriptor> variableDescriptors) {
		requireNonNull(variableDescriptors);
//		checkArgument(!variableDescriptors.isEmpty());
		checkState("Variables already defined", this.__variables__==null);

		this.__variables__ = createVariableSet(variableDescriptors);
	}

	/**
	 * Creates the internal storage used for __variables__.
	 * <p>
	 * The default implementation returns a new instance of {@link SimpleVariableSet}.
	 *
	 * @param variableDescriptors
	 * @return
	 */
	protected VariableSet createVariableSet(Collection<? extends VariableDescriptor> variableDescriptors) {
		return new SimpleVariableSet(variableDescriptors);
	}

	/**
	 * Creates the storage object used for the final result of the computation.
	 *
	 * @param returnType
	 * @return
	 */
	protected Mutable<?> createResultStorage(Class<?> returnType) {
		return Mutable.forClass(returnType);
	}

	/**
	 * @see de.ims.icarus2.util.eval.Expression#getVariables()
	 */
	@Override
	public VariableSet getVariables() {
		return __variables__;
	}

	/**
	 * @see de.ims.icarus2.util.eval.Expression#hasVariables()
	 */
	@Override
	public boolean hasVariables() {
		return __variables__!=null;
	}

	protected abstract void executeCode() throws Exception;

	/**
	 * @see de.ims.icarus2.util.eval.Expression#evaluate()
	 */
	@Override
	public Object evaluate() throws ExecutionException {

		//TODO init variables for inner execution

		try {
			executeCode();
		} catch (Exception e) {
			throw new ExecutionException(e);
		}

		return __result__;
	}
}
