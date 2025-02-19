/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.exp;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

public class StringConcatenation implements Expression<CharSequence> {

	public static Expression<CharSequence> concat(Expression<CharSequence>[] elements) {
		return new StringConcatenation(elements.clone()); // defensive copy
	}

	/** Stores accumulated characters */
	private final StringBuilder buffer;
	/** Raw elements to aggregate */
	private final Expression<CharSequence>[] elements;

	StringConcatenation(Expression<CharSequence>[] elements) {
		requireNonNull(elements);
		checkArgument("Must have at least 2 elements to concatenat", elements.length>1);

		this.elements = elements;
		buffer = new StringBuilder(100);
	}

	/**
	 * Returns the raw elements of this concatenation. Package-private visibility
	 * so that {@link ExpressionFactory} can optimize nested concatenation expressions.
	 */
	Expression<CharSequence>[] getElements() {
		return elements;
	}

	@Override
	public TypeInfo getResultType() { return TypeInfo.TEXT; }

	@Override
	public CharSequence compute() {
		buffer.setLength(0);
		for (int i = 0; i < elements.length; i++) {
			buffer.append(elements[i].compute());
		}
		return buffer;
	}

	/**
	 * @see de.ims.icarus2.query.api.exp.EvaluationUtils#duplicate(Expression[], EvaluationContext)
	 */
	@Override
	public StringConcatenation duplicate(EvaluationContext context) {
		requireNonNull(context);
		return new StringConcatenation(EvaluationUtils.duplicate(elements, context));
	}

	/**
	 * Optimizes all the nested {@link Expression<CharSequence>} elements and tries to
	 * collapse as many of them into constants as possible.
	 *
	 * @see de.ims.icarus2.query.api.exp.Expression#optimize(de.ims.icarus2.query.api.exp.EvaluationContext)
	 */
	@Override
	public Expression<CharSequence> optimize(EvaluationContext context) {
		requireNonNull(context);
		List<Expression<CharSequence>> newElements = new ArrayList<>(elements.length);
		StringBuilder buffer = new StringBuilder();
		boolean hasChanged = false;

		for (int i = 0; i < elements.length; i++) {
			Expression<CharSequence> original = elements[i];
			Expression<CharSequence> optimized = original.optimize(context);

			if(optimized.isConstant()) {
				buffer.append(optimized.compute());
			} else {
				// Shift accumulated text into a single literal
				if(buffer.length()>0) {
					newElements.add(Literals.of(buffer.toString()));
					buffer.setLength(0);
				}

				// Nothing changed
				newElements.add(optimized);
				hasChanged = optimized!=original;
			}
		}

		// Append remaining aggregated constant text
		if(buffer.length()>0) {
			newElements.add(Literals.of(buffer.toString()));
		}

		assert newElements.size()<=elements.length;
		hasChanged |= newElements.size()!=elements.length;

		if(newElements.isEmpty()) {
			return Literals.emptyString();
		} if(newElements.size()==1) {
			return newElements.get(0);
		} else if(hasChanged || newElements.size()<elements.length) {
			return new StringConcatenation(newElements.toArray(new Expression[newElements.size()]));
		}

		return this;
	}
}