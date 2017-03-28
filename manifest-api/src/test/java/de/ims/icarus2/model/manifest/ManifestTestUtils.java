package de.ims.icarus2.model.manifest;
/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 */


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.types.DefaultIconLink;
import de.ims.icarus2.model.manifest.types.DefaultLink;
import de.ims.icarus2.model.manifest.types.DefaultUrlResource;
import de.ims.icarus2.model.manifest.types.Url;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.IconWrapper;
import de.ims.icarus2.util.id.Identity;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class ManifestTestUtils {

	private static final Map<ValueType, Object[]> testValues = new Object2ObjectOpenHashMap<>();

	public enum TestEnum {
		TEST1,
		TEST2,
		TEST3
	}

//	public static final ValueType EXTENSION_TYPE = spy(ValueType.EXTENSION);
//	static {
//		doReturn(extensions[0]).when(EXTENSION_TYPE).parse("extension1", null); //$NON-NLS-1$
//		doReturn(extensions[1]).when(EXTENSION_TYPE).parse("extension2", null); //$NON-NLS-1$
//		doReturn(extensions[2]).when(EXTENSION_TYPE).parse("extension3", null); //$NON-NLS-1$
//	}

	private static void addTestValues(ValueType type, Object...values) {
		testValues.put(type, values);
	}
	static {
		addTestValues(ValueType.STRING, "test1", "test2", "test3");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		addTestValues(ValueType.INTEGER, 1, 20, 300);
		addTestValues(ValueType.LONG, 1L, 20L, 300L);
		addTestValues(ValueType.FLOAT, 1.1F, 2.5F, 3F);
		addTestValues(ValueType.DOUBLE, 1.765324D, 2.56789D, -3D);
		addTestValues(ValueType.BOOLEAN, true, false);
		addTestValues(ValueType.ENUM, (Object[]) TestEnum.values());
		addTestValues(ValueType.IMAGE,
				new IconWrapper("testIconName1"), //$NON-NLS-1$
				new IconWrapper("testIconName2"), //$NON-NLS-1$
				new IconWrapper("testIconName3")); //$NON-NLS-1$

		try {
			addTestValues(ValueType.URL,
					new Url("http://www.uni-stuttgart.de"), //$NON-NLS-1$
					new Url("http://www.uni-stuttgart.de/linguistik"), //$NON-NLS-1$
					new Url("http://www.dict.cc")); //$NON-NLS-1$
		} catch(MalformedURLException e) {
			// ignore
		}

		try {
			addTestValues(ValueType.URI,
					new URI("mailto:xzy"), //$NON-NLS-1$
					new URI("/ref/some/relative/data"), //$NON-NLS-1$
					new URI("http://www.dict.cc#marker")); //$NON-NLS-1$
		} catch(URISyntaxException e) {
			// ignore
		}

		try {
			addTestValues(ValueType.URL_RESOURCE,
					new DefaultUrlResource(new Url("http://www.uni-stuttgart.de"), "Url-Link 1", "Some test url link"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					new DefaultUrlResource(new Url("http://www.uni-stuttgart.de/linguistik"), "Url-Link 2", "Another url link"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					new DefaultUrlResource(new Url("http://www.dict.cc"), "Url-Link 3 (no desciption)")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch(MalformedURLException e) {
			// ignore
		}

		try {
			addTestValues(ValueType.LINK,
					new DefaultLink(new URI("mailto:xzy"), "Link 1", "Some test link"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					new DefaultLink(new URI("/ref/some/relative/data"), "Link 2", "Another link"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					new DefaultLink(new URI("http://www.dict.cc#marker"), "Link 3 (no desciption)")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch(URISyntaxException e) {
			// ignore
		}

		addTestValues(ValueType.IMAGE_RESOURCE,
				new DefaultIconLink(new IconWrapper("testIconName1"), "Icon-Link 1", "Some test icon link"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				new DefaultIconLink(new IconWrapper("testIconName2"), "Icon-Link 2", "Some test icon link"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				new DefaultIconLink(new IconWrapper("testIconName3"), "Icon-Link 3 (no description)")); //$NON-NLS-1$ //$NON-NLS-2$$

		addTestValues(ValueType.UNKNOWN, new Object(), new int[3], 456);
		addTestValues(ValueType.CUSTOM, new Dummy(), new Dummy(), new Dummy());

		addTestValues(ValueType.EXTENSION,
				"my.plugin@extension1",
				"my.plugin@extension2",
				"my.plugin2@extension1");

		addTestValues(ValueType.FILE,
				Paths.get("someFile"),
				Paths.get("anotherFile"),
				Paths.get("some","path","with","a","file.txt"));

		//FIXME add some test values for the other more complex types!
	}

	public static Object[] getTestValues(ValueType type) {
		Object[] values = testValues.get(type);
		if(values==null)
			throw new IllegalArgumentException("No test values for type: "+type);

		return values;
	}

	public static Object getTestValue(ValueType type) {
		Object[] values = testValues.get(type);
		if(values==null)
			throw new IllegalArgumentException("No test values for type: "+type);

		return values[0];
	}

	private static final Set<String> methodBlacklist = new HashSet<>();
	static {
		methodBlacklist.add("getId"); //$NON-NLS-1$
		methodBlacklist.add("getOwner"); //$NON-NLS-1$
		methodBlacklist.add("getTemplate"); //$NON-NLS-1$
	}

	private static String getId(Object manifest) {
		String id = null;

		if(manifest instanceof Manifest) {
			id = ((Manifest)manifest).getId();
		} else if(manifest instanceof Identity) {
			id = ((Identity)manifest).getId();
		}

		if(id==null) {
			id = manifest.getClass()+"@<unnamed>"; //$NON-NLS-1$
		}
		return id;
	}

	public static void assertTemplateGetters(Class<?> interfaceClass, Object instance, Object template) throws Exception {

		for(Method method : interfaceClass.getMethods()) {

			// Ignore getters that rely on parameters
			if(method.isVarArgs() || method.getParameterTypes().length>0) {
				continue;
			}

			// Ignore non getter methods and the getUID method (since uids are volatile data)
			if(methodBlacklist.contains(method.getName())
					|| !method.getName().startsWith("get")
					|| method.getName().startsWith("getLocal")
					|| method.getName().equals("getUID")) { //$NON-NLS-1$
				continue;
			}

			Class<?> resultType = method.getReturnType();

			Object instanceValue = null, templateValue = null;

			try {
				instanceValue = method.invoke(instance);
			} catch(Exception e) {
				failForInvocation(method, instance, e);
			}

			try {
				templateValue = method.invoke(template);
			} catch(Exception e) {
				failForInvocation(method, instance, e);
			}

			if(resultType.isPrimitive() || Collection.class.isAssignableFrom(resultType)) {
				// Use equality for wrapped primitives or collection types
				assertEquals("Method '"+method+"' ignored template value", templateValue, instanceValue); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				// Use identity for all objects
				assertSame("Method '"+method+"' ignored template value", templateValue, instanceValue); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	private static void failForInvocation(Method method, Object target, Exception exception) {
		String message = " Invoking method '"+method+"' on target '"+getId(target)+"' failed: \n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//		message += Exceptions.stackTraceToString(exception);

		throw new AssertionError(message, exception);
	}

	private static class Dummy {
		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Dummy@"+hashCode(); //$NON-NLS-1$
		}
	}

}
