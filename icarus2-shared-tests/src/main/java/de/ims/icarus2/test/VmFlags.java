/**
 *
 */
package de.ims.icarus2.test;

/**
 * @author Markus Gärtner
 *
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html">
 * https://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html</a>
 */
public class VmFlags {

	public static final String PRINT_COMPILATION = "-XX:+PrintCompilation";

	/**
	 * Requires {@link #UNLOCK_DIAGNOSTIC}
	 */
	public static final String PRINT_INLINING = "-XX:+PrintInlining";

	public static final String UNLOCK_DIAGNOSTIC = "-XX:+UnlockDiagnosticVMOptions";
}
