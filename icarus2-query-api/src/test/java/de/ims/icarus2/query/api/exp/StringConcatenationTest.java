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
/**
 *
 */
package de.ims.icarus2.query.api.exp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.query.api.exp.Expression;
import de.ims.icarus2.query.api.exp.Literals;
import de.ims.icarus2.query.api.exp.StringConcatenation;
import de.ims.icarus2.query.api.exp.ExpressionTest.TextExpressionTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.Mutable;
import de.ims.icarus2.util.Mutable.MutableObject;
import de.ims.icarus2.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 *
 */
class StringConcatenationTest implements TextExpressionTest {

	private static Expression<CharSequence> literal(String s) {
		return Literals.of(s);
	}

	@Override
	public CharSequence constant() {return "x1 x2 x3"; }

	/**
	 * @see de.ims.icarus2.query.api.exp.ExpressionTest.Expression<CharSequence>Test#random(de.ims.icarus2.test.random.RandomGenerator)
	 */
	@Override
	public CharSequence random(RandomGenerator rng) {
		int size = rng.random(3, 6);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			if(i>0) {
				sb.append(' ');
			}
			String s = rng.randomString(10);
			s = s.replace(' ', '_');
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * @see de.ims.icarus2.query.api.exp.ExpressionTest#createWithValue(java.lang.Object)
	 */
	@Override
	public Expression<CharSequence> createWithValue(CharSequence value) {
		List<Expression<CharSequence>> buffer = new ArrayList<>();

		String[] blocks = value.toString().split(" ");
		for (int i = 0; i < blocks.length; i++) {
			if(i>0) {
				buffer.add(Literals.of(" "));
			}
			buffer.add(Literals.of(blocks[i]));
		}

		@SuppressWarnings("unchecked")
		Expression<CharSequence>[] elements = new Expression[buffer.size()];
		buffer.toArray(elements);

		return StringConcatenation.concat(elements);
	}

	@Override
	public boolean nativeConstant() { return false; }

	@Override
	public Class<?> getTestTargetClass() { return StringConcatenation.class; }

	@Override
	public boolean equals(CharSequence x, CharSequence y) {
		return StringUtil.equals(x, y);
	}

	@Nested
	class WithData {

		@SuppressWarnings("unchecked")
		private Expression<CharSequence>[] values(Expression<?>...expressions) {
			return (Expression<CharSequence>[])expressions;
		}

		@Test
		void testPartialOptimization() {
			Expression<CharSequence>[] elements = values(
				ExpressionTestUtils.fixed("begin"),
				ExpressionTestUtils.optimizable(" "),
				literal("end")
			);

			StringConcatenation instance = (StringConcatenation) StringConcatenation.concat(elements);

			assertThat(instance.getElements()).isEqualTo(elements);

			Expression<CharSequence> optimized = instance.optimize(context());
			assertThat(optimized).isInstanceOf(StringConcatenation.class);

			Expression<CharSequence>[] newElements = ((StringConcatenation)optimized).getElements();
			assertThat(newElements).hasSize(2);
			assertThat(newElements[0]).isSameAs(elements[0]);
			assertThat(Literals.isLiteral(newElements[1])).isTrue();

			assertThat(optimized.compute()).hasToString("begin end");
		}

		@Test
		void testIntermediateAggregation() {
			Expression<CharSequence>[] elements = values(
				ExpressionTestUtils.optimizable("begin"),
				ExpressionTestUtils.optimizable(" "),
				ExpressionTestUtils.fixed("end")
			);

			StringConcatenation instance = (StringConcatenation) StringConcatenation.concat(elements);

			assertThat(instance.getElements()).isEqualTo(elements);

			Expression<CharSequence> optimized = instance.optimize(context());
			assertThat(optimized).isInstanceOf(StringConcatenation.class);

			Expression<CharSequence>[] newElements = ((StringConcatenation)optimized).getElements();
			assertThat(newElements).hasSize(2);
			assertThat(Literals.isLiteral(newElements[0])).isTrue();
			assertThat(newElements[1]).isSameAs(elements[2]);

			assertThat(optimized.compute()).hasToString("begin end");
		}

		@Test
		void testNonOptimizable() {
			Expression<CharSequence>[] elements = values(
				ExpressionTestUtils.fixed("begin"),
				ExpressionTestUtils.fixed(" "),
				ExpressionTestUtils.fixed("end")
			);

			StringConcatenation instance = (StringConcatenation) StringConcatenation.concat(elements);
			assertThat(instance.optimize(context())).isSameAs(instance);
		}

		@Test
		void testDynamic() {
			Mutable<String> buffer = new MutableObject<>(" ");

			Expression<CharSequence>[] elements = values(
				ExpressionTestUtils.optimizable("begin"),
				ExpressionTestUtils.dynamicText(() -> buffer),
				ExpressionTestUtils.fixed("end")
			);

			StringConcatenation instance = (StringConcatenation) StringConcatenation.concat(elements);
			assertThat(instance.compute()).hasToString("begin end");

			buffer.set("...");
			assertThat(instance.compute()).hasToString("begin...end");
		}
	}
}
