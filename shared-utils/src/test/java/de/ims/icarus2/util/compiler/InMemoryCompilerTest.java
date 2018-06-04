package de.ims.icarus2.util.compiler;

import static org.junit.Assert.assertTrue;

import javax.tools.DiagnosticListener;

import org.junit.Test;

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

		assertTrue("Compilation failed", compiler.compile(collector));

		ClassLoader classLoader = compiler.getFileManager().getClassLoader(null);

		System.out.println(classLoader.loadClass(className).newInstance());
	}
}