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
package de.ims.icarus2.util.compiler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.tools.DiagnosticListener;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Markus Gärtner
 *
 */
public class InMemoryCompilerTest {

	@Test
	public void testCompile() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String code = "public class DynaClass {\n"
	        +"    public String toString() {\n"
	        +"        return \"Hello, I am \" + "
	        +"this.getClass().getSimpleName();\n"
	        +"    }\n"
	        +"}\n";
		String className = "DynaClass";

		InMemoryCompiler compiler = InMemoryCompiler.newInstance();

		compiler.addInputFile(className, code);

		DiagnosticListener<?> collector = d -> System.out.println(d);

		assertTrue(compiler.compile(collector), "Compilation failed");

		ClassLoader classLoader = compiler.getFileManager().getSharedClassLoader();

		System.out.println(classLoader.loadClass(className).newInstance());
	}
}