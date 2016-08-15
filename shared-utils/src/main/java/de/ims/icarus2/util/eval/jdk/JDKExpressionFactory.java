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

import java.util.concurrent.atomic.AtomicLong;

import javax.lang.model.element.Modifier;
import javax.tools.DiagnosticCollector;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import de.ims.icarus2.util.compiler.InMemoryCompiler;
import de.ims.icarus2.util.eval.Expression;
import de.ims.icarus2.util.eval.ExpressionFactory;

/**
 * @author Markus Gärtner
 *
 */
public class JDKExpressionFactory extends ExpressionFactory {

	private static final AtomicLong expressionCounter = new AtomicLong(0L);

	private static final InMemoryCompiler compiler = InMemoryCompiler.newInstance();

	private static final String PACKAGE_NAME = "de.ims.icarus2.util.eval.jdk.tmp";
	private static final String CLASS_NAME_BASE = "JDKExpression";
	private static final String METHOD_NAME = "executeCode";

	protected static String getNewExpressionClassName() {
		long id = expressionCounter.incrementAndGet();
		return CLASS_NAME_BASE+id;
	}

	public JDKExpressionFactory() {
		super(JDKExpressionFactoryProvider.PROVIDER_NAME);
	}

	/**
	 * @see de.ims.icarus2.util.eval.ExpressionFactory#compile()
	 */
	@Override
	public Expression compile() throws Exception {

		MethodSpec exec = MethodSpec.methodBuilder(METHOD_NAME)
				.addModifiers(Modifier.PROTECTED, Modifier.FINAL)
				.returns(TypeName.OBJECT)
				.addCode(createCodeblock())
				.build();

		final String className = getNewExpressionClassName();

		TypeSpec typeSpec = TypeSpec.classBuilder(className)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addMethod(exec)
				.build();

		JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, typeSpec).build();

		DiagnosticCollector<?> diagnostics = new DiagnosticCollector<>();
		boolean success = false;

		compiler.lock();
		try {
			compiler.addInputFile(className, javaFile.toString());

			success = compiler.compile(diagnostics);
		} finally {
			compiler.unlock();
		}


		// TODO use custom implementations for virtual files and use the javax.tools.JavaCompiler to compile handlers
		return null;
	}

	protected CodeBlock createCodeblock() {
		CodeBlock.Builder builder = CodeBlock.builder();

		//TODO

		return builder.build();
	}
}
