package de.ims.icarus2.util.compiler;

import static org.junit.Assert.assertTrue;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;

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

		InMemoryCompiler compiler = new InMemoryCompiler(null);

		compiler.addInputFile(className, code);

		DiagnosticCollector<?> collector = new DiagnosticCollector<>();

		assertTrue("Compilation failed", compiler.compile(collector));

		for(Diagnostic<?> diagnostic : collector.getDiagnostics()) {
			System.out.println(diagnostic);
		}

		ClassLoader classLoader = compiler.getFileManager().getClassLoader(null);

		System.out.println(classLoader.loadClass(className).newInstance());
	}
}