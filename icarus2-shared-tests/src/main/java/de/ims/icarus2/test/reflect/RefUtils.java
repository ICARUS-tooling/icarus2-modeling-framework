/**
 *
 */
package de.ims.icarus2.test.reflect;

import java.lang.reflect.Executable;
import java.util.Comparator;

/**
 * @author Markus Gärtner
 *
 */
class RefUtils {

	private static final int INTERFACE = 1;
	private static final int CLASS = 0;

	private static int type(Class<?> clazz) {
		return clazz.isInterface() ? INTERFACE : CLASS;
	}

	public static Comparator<Class<?>> INHERITANCE_ORDER = (c1, c2) -> {
		int diff = type(c1)-type(c2);
		if(diff!=0) {
			return diff;
		}

		if(c1.equals(c2)) {
			return 0;
		} else if(c1.isAssignableFrom(c2)) {
			return 1;
		} else if(c2.isAssignableFrom(c1)) {
			return -1;
		}

		return 0;
	};

	public static Comparator<Executable> METHOD_INHERITANCE_ORDER = (e1, e2) -> {
		Class<?> c1 = e1.getDeclaringClass();
		Class<?> c2 = e2.getDeclaringClass();

		return INHERITANCE_ORDER.compare(c1, c2);
	};
}
