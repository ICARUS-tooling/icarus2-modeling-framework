/*
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
package de.ims.icarus2.query.api.eval;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.ims.icarus2.query.api.eval.Expression.TextExpression;
import de.ims.icarus2.util.strings.CodePointBuffer;
import de.ims.icarus2.util.strings.CodePointSequence;

public class StringConcatenation implements TextExpression {

	public static StringConcatenation concat(TextExpression[] elements) {
		return new StringConcatenation(elements.clone()); // defensive copy
	}

	/** Stores accumulated characters */
	private final StringBuilder buffer;
	/** Provides the unicode codepoint view on the aggregated text */
	private final CodePointBuffer translator;
	/** Raw elements to aggregate */
	private final TextExpression[] elements;

	StringConcatenation(TextExpression[] elements) {
		requireNonNull(elements);
		checkArgument("Must have at least 2 elements to concatenat", elements.length>1);

		this.elements = elements;
		buffer = new StringBuilder(100);
		translator = new CodePointBuffer();
	}

	/**
	 * Returns the raw elements of this concatenation. Package-private visibility
	 * so that {@link ExpressionFactory} can optimize nested concatenation expressions.
	 */
	TextExpression[] getElements() {
		return elements;
	}

	@Override
	public TypeInfo getResultType() { return TypeInfo.STRING; }

	@Override
	public CodePointSequence compute() {
		translator.set(computeAsChars());
		return translator;
	}

	@Override
	public CharSequence computeAsChars() {
		buffer.setLength(0);
		for (int i = 0; i < elements.length; i++) {
			buffer.append(elements[i].compute());
		}
		return buffer;
	}

	/**
	 * @see de.ims.icarus2.query.api.eval.Expression#duplicate(de.ims.icarus2.query.api.eval.EvaluationContext)
	 */
	@Override
	public StringConcatenation duplicate(EvaluationContext context) {
		return new StringConcatenation(Stream.of(elements)
				.map(ex -> ex.duplicate(context))
				.toArray(TextExpression[]::new));
	}

	/**
	 * Optimizes all the nested {@link TextExpression} elements and tries to
	 * collapse as many of them into constants as possible.
	 *
	 * @see de.ims.icarus2.query.api.eval.Expression#optimize(de.ims.icarus2.query.api.eval.EvaluationContext)
	 */
	@Override
	public TextExpression optimize(EvaluationContext context) {
		List<TextExpression> newElements = new ArrayList<>(elements.length);
		StringBuilder buffer = new StringBuilder();
		boolean hasChanged = false;

		for (int i = 0; i < elements.length; i++) {
			TextExpression original = elements[i];
			TextExpression optimized = (TextExpression) original.optimize(context);

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

		if(newElements.size()==1) {
			return newElements.get(0);
		} else if(hasChanged || newElements.size()<elements.length) {
			return new StringConcatenation(newElements.toArray(new TextExpression[newElements.size()]));
		}

		return this;
	}
}