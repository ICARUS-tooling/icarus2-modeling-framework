/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.test.reflect;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Markus Gärtner
 *
 */
public class RefUtils {

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

	public static String toSimpleString(Executable executable) {
		StringBuilder sb = new StringBuilder();
		if(executable instanceof Method) {
			Method m = (Method)executable;
			sb.append(m.getReturnType().getSimpleName()).append(" ");
//			sb.append(m.getDeclaringClass().getName()).append(" ");
		}

		String name = executable.getName();
		int split = name.lastIndexOf('.');

		sb.append(name.substring(split+1));
		sb.append('(');
		sb.append(String.join(", ",
				Stream.of(executable.getParameterTypes())
				.map(Class::getSimpleName)
				.collect(Collectors.toList())));
		sb.append(')');

		return sb.toString();
	}
}
