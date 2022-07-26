/**
 *
 */
package de.ims.icarus2.util.cli;

import static de.ims.icarus2.util.lang.Primitives._int;

import java.util.concurrent.Callable;

/**
 * @author Markus Gärtner
 *
 */
public abstract class CliCommand implements Callable<Integer> {

	public static final Integer FAILED = _int(1);
	public static final Integer SUCCESS = _int(0);
}
