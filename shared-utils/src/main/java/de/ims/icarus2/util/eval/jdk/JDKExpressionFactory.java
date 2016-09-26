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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;
import de.ims.icarus2.util.classes.ClassUtils;
import de.ims.icarus2.util.compiler.InMemoryCompiler;
import de.ims.icarus2.util.eval.Expression;
import de.ims.icarus2.util.eval.ExpressionFactory;
import de.ims.icarus2.util.eval.var.VariableDescriptor;

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

		final CodeBlock code = createCodeblock();

		// Execution method
		MethodSpec exec = MethodSpec.methodBuilder(METHOD_NAME)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.returns(TypeName.VOID)
				.addAnnotation(ClassName.get(Override.class))
				.addCode(code)
				.build();

		final String className = getNewExpressionClassName();

		// Virtual type
		TypeSpec typeSpec = TypeSpec.classBuilder(className)
				.superclass(TypeName.get(JDKCompiledExpression.class))
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addMethod(exec)
				.build();

		JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, typeSpec).build();
		final String classCode = javaFile.toString();

//		System.out.println(classCode);

		@SuppressWarnings("rawtypes")
		DiagnosticCollector diagnostics = new DiagnosticCollector<>();
		boolean success = false;

		Class<?> newClazz = null;

		compiler.lock();
		try {
			compiler.addInputFile(className, classCode);

			success = compiler.compile(diagnostics);

			if(success) {
				newClazz = compiler.getFileManager().getClassLoader(null).loadClass(PACKAGE_NAME+"."+className);
			}
		} finally {
			compiler.unlock();
		}

		@SuppressWarnings("rawtypes")
		List<Diagnostic> diagnosticEntries = diagnostics.getDiagnostics();

		// Throw a detailed error message
		//FIXME only report actual errors, no warnings!!!
		if(!diagnosticEntries.isEmpty()) {
			StringBuilder sb = new StringBuilder("Errors during compilation of expression:\n");
			sb.append("=====================EXPRESSION CODE=====================\n");
			sb.append(classCode);
			sb.append("=======================DIAGNOSTICS=======================\n");
			for(@SuppressWarnings("rawtypes") Diagnostic diagnostic : diagnosticEntries) {
				sb.append(diagnostic).append("\n");
			}
			sb.append("=========================================================\n");

			throw new IcarusException(GlobalErrorCode.DELEGATION_FAILED, sb.toString());
		}


		if(newClazz==null)
			throw new IcarusException(GlobalErrorCode.INTERNAL_ERROR, "Failed to load freshly compiled expression class: "+className);

		JDKCompiledExpression expression = (JDKCompiledExpression) newClazz.newInstance();

		// Finalize expression settings
		prepareExpression(expression, code.toString());

		return expression;
	}

	protected void prepareExpression(JDKCompiledExpression expression, String code) {
		expression.setCode(code);
		expression.setEnvironment(getEnvironment());
		expression.setReturnType(getReturnType());
		expression.setVariables(getVariables());
	}

	private static final Matcher matcher = Pattern.compile("return|@([\\p{Alpha}][\\w]*)").matcher("");

	/**
	 * Wraps the originally supplied code so that it will contain a header section for import
	 *
	 * @return
	 */
	protected CodeBlock createCodeblock() {
		Collection<VariableDescriptor> variables = getVariables();

		CodeBlock.Builder builder = CodeBlock.builder();

		if(!variables.isEmpty()) {
			builder.add("//---begin header---\n");

			// Create the "copy in" part where we inject the external values to the
			for(VariableDescriptor variable : variables) {

				Class<?> type = variable.getNamespaceClass();

				builder.add("$T $L", type, variable.getName());

				// If we have an input variable we need to initialize it with the respective value from our backend storage
				if(variable.getMode().isIn()) {
					builder.add(" = ");

					if(!type.isPrimitive()) {
						builder.add("($T)", type);
					}

					String call = "getValue";

					if(type.isPrimitive()) {
						call = "get"+ClassUtils.wrap(type).getSimpleName();
					}

					builder.add("__variables__.$L($S);\n", call, variable.getName());
				} else {
					// For output variables it's sufficient to just declare them without initialization
					builder.add(";\n");
				}
			}
			builder.add("//----end header----\n");
		}

		// Process the raw code and remove our "@varname" style

		String rawCode = getCode();
		StringBuilder sb = new StringBuilder(rawCode.length());
		matcher.reset(rawCode);

		int lastEnd = 0;
		while(matcher.find()) {
			if(matcher.start() > lastEnd) {
				sb.append(rawCode, lastEnd, matcher.start());
			}

			if('@'==rawCode.charAt(matcher.start())) {
				// Variable notation -> remove '@'
				sb.append(rawCode, matcher.start(1), matcher.end(1));
			} else {
				// 'return' statement -> delegate to our result storage
				sb.append("__result__ =");
			}

			lastEnd = matcher.end();
		}

		if(lastEnd<rawCode.length()) {
			sb.append(rawCode, lastEnd, rawCode.length());
		}

//		System.out.println(sb.toString());

//		builder.beginControlFlow("inner_statement:");
		builder.add(sb.toString());
//		builder.endControlFlow();

		if(!variables.isEmpty()) {
			builder.add("\n");
			builder.add("//---begin footer---\n");

			// Create the "write back" part where local variable values are copied back into external __variables__
			for(VariableDescriptor variable : variables) {
				if(!variable.getMode().isOut()) {
					continue;
				}

				Class<?> type = variable.getNamespaceClass();

				String call = "setValue";

				if(type.isPrimitive()) {
					call = "set"+ClassUtils.wrap(type).getSimpleName();
				}

				builder.add("__variables__.$L($S, $L);\n", call, variable.getName(), variable.getName());
			}

			builder.add("//----end footer----\n");
		}

		return builder.build();
	}
}
