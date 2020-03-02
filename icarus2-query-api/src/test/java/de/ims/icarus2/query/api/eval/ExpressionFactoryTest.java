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
/**
 *
 */
package de.ims.icarus2.query.api.eval;

import static de.ims.icarus2.query.api.eval.ExpressionTestUtils.assertExpression;
import static de.ims.icarus2.query.api.iql.AntlrUtils.createParser;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.ims.icarus2.query.api.iql.antlr.IQLParser;
import de.ims.icarus2.query.api.iql.antlr.IQLParser.StandaloneExpressionContext;

/**
 * @author Markus Gärtner
 *
 */
class ExpressionFactoryTest {

	@Nested
	class Constructor {
		@Test
		void testWithNull() {
			assertNPE(() -> new ExpressionFactory(null));
		}

		@Test
		void testWithValid() {
			EvaluationContext ctx = mock(EvaluationContext.class);
			ExpressionFactory factory = new ExpressionFactory(ctx);
			assertThat(factory.getContext()).isSameAs(ctx);
		}
	}

	@Nested
	class ConstantInput {

		private EvaluationContext context;
		private ExpressionFactory factory;

		@BeforeEach
		void setUp() {
			context = mock(EvaluationContext.class);
			factory = new ExpressionFactory(context);
		}

		@AfterEach
		void tearDown() {
			context = null;
			factory = null;
		}

		private Expression<?> parse(String s) {
			IQLParser parser = createParser(s, "expression");
			StandaloneExpressionContext ctx = parser.standaloneExpression();
			return factory.processExpression(ctx.expression());
		}

		@ParameterizedTest
		@CsvSource({
			// Simple formulas
			"1, 1",
			"1+2, 3",
			"8*11, 88",
			"123/3, 41",
			"123%4, 3",

			// Nested formulas
			"2+3*4, 14",
			"2-3*4, -10",
			"12/3+2, 6"
			//TODO
		})
		void testMathematicalInteger(String input, long result) {
			Expression<?> exp = parse(input);
			assertThat(exp.isInteger()).isTrue();
			assertExpression(exp, context, result);
		}

		@ParameterizedTest
		@CsvSource(delimiter=';', value={
			"2 in {1, 2, 3, 4,-10}; true",
			"2 in {1, 3, 4,-10}; false",
			"2 not in {1, 3, 4,-10}; true",
			"2 not in {1, 2, 3, 4,-10}; false",
			//TODO find easy way to test the 'all in' expression with multiple source values
		})
		void testSetPredicate(String input, boolean result) {
			Expression<?> exp = parse(input);
			assertThat(exp.isBoolean()).isTrue();
			assertExpression(exp, context, result);
		}
 	}
}
