/**
 *
 */
package de.ims.icarus2.test.refelct;

import java.lang.reflect.Executable;
import java.util.Comparator;

/**
 * @author Markus Gärtner
 *
 */
class RefUtils {

	public static Comparator<Executable> INHERITANCE_ORDER = (e1, e2) -> {
		Class<?> c1 = e1.getDeclaringClass();
		Class<?> c2 = e2.getDeclaringClass();

		if(c1.equals(c2)) {
			return 0;
		} else if(c1.isAssignableFrom(c2)) {
			return 1;
		} else {
			return -1;
		}
	};
}
