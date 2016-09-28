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

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import de.ims.icarus2.util.eval.Expression;

/**
 * @author Markus Gärtner
 *
 */
public class JDKExpressionFactoryTest {

	@Test
	public void testNoVariablesExpression() throws Exception {
		JDKExpressionFactory factory = new JDKExpressionFactory();

		factory.setCode("System.out.println(\"Hello World\"); return null;");
		factory.setReturnType(Void.TYPE);

		Expression expression = factory.compile();

		expression.evaulate();
	}

	@Test
	public void testInputVariableExpression() throws Exception {
		JDKExpressionFactory factory = new JDKExpressionFactory();

		factory.addInputVariable("input", String.class);
		factory.setCode("System.out.println(\"Hello World: \"+@input); return null;");
		factory.setReturnType(Void.TYPE);

		Expression expression = factory.compile();

		expression.getVariables().setValue("input", "this is a test");
		expression.evaulate();
	}

	@Test
	public void testOutputExpression() throws Exception {
		JDKExpressionFactory factory = new JDKExpressionFactory();

		String test = "This is a test...";

		factory.setCode("return \""+test+"\";");
		factory.setReturnType(String.class);

		Expression expression = factory.compile();
		Object result = expression.evaulate();

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
		Object result = expression.evaulate();

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
		Object result = expression.evaulate();

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
	public void testExpressionPerformance() throws Exception {
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

		long runtime_raw = 0L;
		long runtime_exp = 0L;

		Random r = new Random(System.currentTimeMillis());

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
			double result_exp = (double) expression.evaulate();
			l2 = System.nanoTime();

			runtime_exp += Math.abs(l2-l1);
			assertEquals(result_raw, result_exp, 0.001);
		}

		System.out.printf("Raw time (%d runs): %.03fs\n", runs, 0.001*(double)runtime_raw/runs);
		System.out.printf("Compiled time (%d runs): %.03fs\n", runs, 0.001*(double)runtime_exp/runs);
	}
}
