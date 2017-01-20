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
package de.ims.icarus2.util.eval.jdk;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Collection;

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

	protected abstract void executeCode();

	/**
	 * @see de.ims.icarus2.util.eval.Expression#evaluate()
	 */
	@Override
	public Object evaluate() {

		//TODO init variables for inner execution

		executeCode();

		return __result__;
	}
}
