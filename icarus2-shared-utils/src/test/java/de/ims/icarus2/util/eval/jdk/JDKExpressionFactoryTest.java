/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.test.TestTags.RANDOMIZED;
import static de.ims.icarus2.test.TestTags.SLOW;
import static de.ims.icarus2.test.TestUtils.EMOJI;
import static de.ims.icarus2.test.TestUtils.LOREM_IPSUM_ASCII;
import static de.ims.icarus2.test.TestUtils.LOREM_IPSUM_CHINESE;
import static de.ims.icarus2.test.TestUtils.LOREM_IPSUM_ISO;
import static de.ims.icarus2.test.TestUtils.random;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.util.eval.Expression;

/**
 * @author Markus Gärtner
 *
 */
@SuppressWarnings("boxing")
public class JDKExpressionFactoryTest {

	@Nested
	class WithSystemOut {

		ByteArrayOutputStream buffer;
		PrintStream stdOut;

		@BeforeEach
		void setUp() {
			stdOut = System.out;
			buffer = new ByteArrayOutputStream();
			System.setOut(new PrintStream(buffer));
		}

		@AfterEach
		void tearDown() throws IOException {
			System.setOut(stdOut);
			stdOut = null;
			buffer.close();
			buffer = null;
		}

		@Test
		public void testNoVariablesExpression() throws Exception {
			JDKExpressionFactory factory = new JDKExpressionFactory();

			factory.setCode("System.out.print(\"Hello World\"); return null;");
			factory.setReturnType(Void.TYPE);

			Expression expression = factory.compile();

			expression.evaluate();

			assertEquals("Hello World", buffer.toString());
		}

		@ParameterizedTest
		@ValueSource(strings = {
				"",
				"test",
				"this is a test",
				"this is a test\nwith\nbreaks",
				EMOJI,
				LOREM_IPSUM_ASCII,
				LOREM_IPSUM_ISO,
				LOREM_IPSUM_CHINESE
		})
		public void testInputVariableExpression(String value) throws Exception {
			JDKExpressionFactory factory = new JDKExpressionFactory();

			factory.addInputVariable("input", String.class);
			factory.setCode("System.out.print(\"Hello World: \"+@input); return null;");
			factory.setReturnType(Void.TYPE);

			Expression expression = factory.compile();

			expression.getVariables().setValue("input", value);
			expression.evaluate();

			assertEquals("Hello World: "+value, buffer.toString());
		}
	}

	@Test
	public void testOutputExpression() throws Exception {
		JDKExpressionFactory factory = new JDKExpressionFactory();

		String test = "This is a test...";

		factory.setCode("return \""+test+"\";");
		factory.setReturnType(String.class);

		Expression expression = factory.compile();
		Object result = expression.evaluate();

		assertEquals(test, result);
	}

	@Test
	public void testInputOutputExpression() throws Exception {
		JDKExpressionFactory factory = new JDKExpressionFactory();

		factory.addInputVariable("a", int.class);
		factory.addInputVariable("b", int.class);
		factory.setCode("return @a+@b;");
		factory.setReturnType(int.class);

		Expression expression = factory.compile();
		expression.getVariables().setInteger("a", 2);
		expression.getVariables().setInteger("b", 10);
		Object result = expression.evaluate();

		assertEquals(12, result);
	}

	@Test
	public void testMethodExpression() throws Exception {
		JDKExpressionFactory factory = new JDKExpressionFactory();

		factory.addInputVariable("input", String.class);
		factory.setCode("return @input.substring(@input.indexOf('x')+1);");
		factory.setReturnType(String.class);

		Expression expression = factory.compile();
		expression.getVariables().setValue("input", "abcxcba");
		Object result = expression.evaluate();

		assertEquals("cba", result);
	}

	private double doThings(double a, double b, int iterations) {
		double x = 0;
		while(iterations-->0) {
			if(iterations%2==0) {
				x += Math.sinh(a);
			} else {
				x += Math.cos(b);
			}

			x *= Math.log(a);
		}

		return x;
	}

	@Test
	@Tag(SLOW)
	@Tag(RANDOMIZED)
	public void testExpressionPerformance(TestReporter reporter) throws Exception {
		JDKExpressionFactory factory = new JDKExpressionFactory();

		factory.addInputVariable("a", double.class);
		factory.addInputVariable("b", double.class);
		factory.addInputVariable("iterations", int.class);
		factory.setCode(
		"double x = 0;\n"
		+"while(iterations-->0) {\n"
		+"	if(iterations%2==0) {\n"
		+"		x += Math.sinh(a);\n"
		+"	} else {\n"
		+"		x += Math.cos(b);\n"
		+"	}\n"
		+"\n"
		+"	x *= Math.log(a);\n"
		+"}\n"
		+"\n"
		+"return x;");
		factory.setReturnType(double.class);

		Expression expression = factory.compile();

		int runs = 10_000_000;

		//for default testing use smaller value
		runs = 100_000;

		long runtime_raw = 0L;
		long runtime_exp = 0L;

		Random r = random();

		for(int i=0; i<runs; i++) {
			double a = r.nextDouble();
			double b = r.nextDouble();
			int iterations = 100;

			// Raw
			long l1 = System.nanoTime();
			double result_raw = doThings(a, b, iterations);
			long l2 = System.nanoTime();

			runtime_raw += Math.abs(l2-l1);

			// Expression
			l1 = System.nanoTime();
			expression.getVariables().setValue("a", a);
			expression.getVariables().setValue("b", b);
			expression.getVariables().setValue("iterations", iterations);
			double result_exp = (double) expression.evaluate();
			l2 = System.nanoTime();

			runtime_exp += Math.abs(l2-l1);
			assertEquals(result_raw, result_exp, 0.001);
		}

		reporter.publishEntry(String.format("Raw time (%d runs): %.03fs\n",
				runs, 0.001*(double)runtime_raw/runs));
		reporter.publishEntry(String.format("Compiled time (%d runs): %.03fs\n",
				runs, 0.001*(double)runtime_exp/runs));
	}
}
