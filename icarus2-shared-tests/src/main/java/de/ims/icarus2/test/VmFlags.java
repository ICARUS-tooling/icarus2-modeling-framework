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

	private static final String PREFIX = "-XX:+";

	public static final String PRINT_COMPILATION = PREFIX+"PrintCompilation";

	/**
	 * Requires {@link #UNLOCK_DIAGNOSTIC}
	 */
	public static final String PRINT_INLINING = PREFIX+"PrintInlining";

	public static final String UNLOCK_DIAGNOSTIC = PREFIX+"UnlockDiagnosticVMOptions";
}
