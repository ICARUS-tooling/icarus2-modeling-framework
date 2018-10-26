/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;
import de.ims.icarus2.util.compiler.InMemoryCompiler;
import de.ims.icarus2.util.eval.Expression;
import de.ims.icarus2.util.eval.ExpressionFactory;
import de.ims.icarus2.util.eval.var.VariableDescriptor;
import de.ims.icarus2.util.lang.Primitives;

/**
 * @author Markus Gärtner
 *
 */
public class JDKExpressionFactory extends ExpressionFactory {

	private static final Logger log = LoggerFactory.getLogger(JDKExpressionFactory.class);

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
				.addException(Exception.class)
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
				newClazz = compiler.getFileManager().getSharedClassLoader().loadClass(PACKAGE_NAME+"."+className);
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
						call = "get"+Primitives.wrap(type).getSimpleName();
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

		final String code = sb.toString();

		if(log.isDebugEnabled()) {
			log.debug("Code to compile: {}", code);
		}

//		builder.beginControlFlow("inner_statement:");
		builder.add(code);
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
					call = "set"+Primitives.wrap(type).getSimpleName();
				}

				builder.add("__variables__.$L($S, $L);\n", call, variable.getName(), variable.getName());
			}

			builder.add("//----end footer----\n");
		}

		return builder.build();
	}
}
