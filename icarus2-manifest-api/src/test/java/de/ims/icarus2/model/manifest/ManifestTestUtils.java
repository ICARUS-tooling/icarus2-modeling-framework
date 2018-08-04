/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.model.manifest.types.DefaultIconLink;
import de.ims.icarus2.model.manifest.types.DefaultLink;
import de.ims.icarus2.model.manifest.types.DefaultUrlResource;
import de.ims.icarus2.model.manifest.types.Url;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.icon.IconWrapper;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.nio.ByteArrayChannel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
@SuppressWarnings("boxing")
public class ManifestTestUtils {

	private static final Map<ValueType, TestInfo> testValues = new Object2ObjectOpenHashMap<>();

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

	private static class TestInfo {
		Object illegalValue;
		Object[] legalValues;
		/**
		 * @param illegalValue
		 * @param legalValues
		 */
		public TestInfo(Object illegalValue, Object[] legalValues) {
			super();
			this.illegalValue = illegalValue;
			this.legalValues = legalValues;
		}
	}

	private static void addTestValues(ValueType type, Object illegalValue, Object...values) {
		testValues.put(type, new TestInfo(illegalValue, values));
	}
	static {
		addTestValues(ValueType.STRING, -1, "test1", "test2", "test3");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		addTestValues(ValueType.INTEGER, "illegal", 1, 20, 300);
		addTestValues(ValueType.LONG, "illegal", 1L, 20L, 300L);
		addTestValues(ValueType.FLOAT, "illegal", 1.1F, 2.5F, 3F);
		addTestValues(ValueType.DOUBLE, "illegal", 1.765324D, 2.56789D, -3D);
		addTestValues(ValueType.BOOLEAN, "illegal", true, false);
		addTestValues(ValueType.ENUM, "illegal", (Object[]) TestEnum.values());
		addTestValues(ValueType.IMAGE, "illegal",
				new IconWrapper("testIconName1"), //$NON-NLS-1$
				new IconWrapper("testIconName2"), //$NON-NLS-1$
				new IconWrapper("testIconName3")); //$NON-NLS-1$

		addTestValues(ValueType.BINARY_STREAM, 1,
				ByteArrayChannel.fromChars("this is a test"),
				ByteArrayChannel.fromChars("this is another slightly longer test...\n still a test"),
				ByteArrayChannel.fromChars("this is the third and final test"));

		try {
			addTestValues(ValueType.URL, "illegal",
					new Url("http://www.uni-stuttgart.de"), //$NON-NLS-1$
					new Url("http://www.uni-stuttgart.de/linguistik"), //$NON-NLS-1$
					new Url("http://www.dict.cc")); //$NON-NLS-1$
		} catch(MalformedURLException e) {
			// ignore
		}

		try {
			addTestValues(ValueType.URI, "illegal",
					new URI("mailto:xzy"), //$NON-NLS-1$
					new URI("/ref/some/relative/data"), //$NON-NLS-1$
					new URI("http://www.dict.cc#marker")); //$NON-NLS-1$
		} catch(URISyntaxException e) {
			// ignore
		}

		try {
			addTestValues(ValueType.URL_RESOURCE, "illegal",
					new DefaultUrlResource(new Url("http://www.uni-stuttgart.de"), "Url-Link 1", "Some test url link"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					new DefaultUrlResource(new Url("http://www.uni-stuttgart.de/linguistik"), "Url-Link 2", "Another url link"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					new DefaultUrlResource(new Url("http://www.dict.cc"), "Url-Link 3 (no desciption)")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch(MalformedURLException e) {
			// ignore
		}

		try {
			addTestValues(ValueType.LINK, "illegal",
					new DefaultLink(new URI("mailto:xzy"), "Link 1", "Some test link"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					new DefaultLink(new URI("/ref/some/relative/data"), "Link 2", "Another link"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					new DefaultLink(new URI("http://www.dict.cc#marker"), "Link 3 (no desciption)")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch(URISyntaxException e) {
			// ignore
		}

		addTestValues(ValueType.IMAGE_RESOURCE, "illegal",
				new DefaultIconLink(new IconWrapper("testIconName1"), "Icon-Link 1", "Some test icon link"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				new DefaultIconLink(new IconWrapper("testIconName2"), "Icon-Link 2", "Some test icon link"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				new DefaultIconLink(new IconWrapper("testIconName3"), "Icon-Link 3 (no description)")); //$NON-NLS-1$ //$NON-NLS-2$$

		addTestValues(ValueType.UNKNOWN, null, new Object(), new int[3], 456);
		addTestValues(ValueType.CUSTOM, null, new Dummy(), new Dummy(), new Dummy());

		addTestValues(ValueType.EXTENSION, -1,
				"my.plugin@extension1",
				"my.plugin@extension2",
				"my.plugin2@extension1");

		addTestValues(ValueType.FILE, "illegal",
				Paths.get("someFile"),
				Paths.get("anotherFile"),
				Paths.get("some","path","with","a","file.txt"));

		//FIXME add some test values for the other more complex types!
	}

	public static Set<ValueType> getAvailableTestTypes() {
		return Collections.unmodifiableSet(testValues.keySet());
	}

	public static Object[] getTestValues(ValueType type) {
		TestInfo info = testValues.get(type);
		if(info==null)
			throw new IllegalArgumentException("No test values for type: "+type);

		return info.legalValues;
	}

	public static Object getTestValue(ValueType type) {
		TestInfo info = testValues.get(type);
		if(info==null)
			throw new IllegalArgumentException("No test values for type: "+type);

		return info.legalValues[0];
	}

	public static Object getIllegalValue(ValueType type) {
		TestInfo info = testValues.get(type);
		if(info==null)
			throw new IllegalArgumentException("No test values for type: "+type);

		return info.illegalValue;
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
				assertEquals(templateValue, instanceValue, "Method '"+method+"' ignored template value"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				// Use identity for all objects
				assertSame(templateValue, instanceValue, "Method '"+method+"' ignored template value"); //$NON-NLS-1$ //$NON-NLS-2$
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

	public static <M extends TypedManifest> M mockTypedManifest(ManifestType type) {
		Class<? extends TypedManifest> clazz = type.getBaseClass();
		if(clazz==null)
			throw new InternalError("Cannot create mock for manifest type: "+type);

		@SuppressWarnings("unchecked")
		M manifest = (M) mock(clazz);

		if(Manifest.class.isAssignableFrom(clazz)) {
			Manifest fullManifest = (Manifest) manifest;
			ManifestRegistry registry = Mockito.mock(ManifestRegistry.class);
			ManifestLocation location = Mockito.mock(ManifestLocation.class);

			when(fullManifest.getManifestLocation()).thenReturn(location);
			when(fullManifest.getRegistry()).thenReturn(registry);
		}

		return manifest;
	}

	public static void assertMalformedId(Manifest manifest, String id) {
		ManifestException exception = assertThrows(ManifestException.class, () -> manifest.setId(id));
		assertEquals(ManifestErrorCode.MANIFEST_INVALID_ID, exception.getErrorCode());
	}

	public static void assertValidId(Manifest manifest, String id) {
		manifest.setId(id);
		assertEquals(id, manifest.getId());
	}

	/**
	 * {@link #assertManifestException(ManifestErrorCode, Executable) Assert} {@link ManifestErrorCode#MANIFEST_TYPE_CAST}
	 * @param executable
	 */
	public static void assertIllegalValue(Executable executable) {
		assertManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST, executable);
	}

	public static void assertManifestException(ErrorCode errorCode, Executable executable) {
		ManifestException exception = assertThrows(ManifestException.class, executable);
		assertEquals(errorCode, exception.getErrorCode());
	}
}
