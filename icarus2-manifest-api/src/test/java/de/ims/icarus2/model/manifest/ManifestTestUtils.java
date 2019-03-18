/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.test.TestUtils.assertMock;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.api.Embedded;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestFactory;
import de.ims.icarus2.model.manifest.api.ManifestFragment;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.model.manifest.standard.DefaultManifestFactory;
import de.ims.icarus2.model.manifest.standard.DefaultManifestRegistry;
import de.ims.icarus2.model.manifest.types.DefaultIconLink;
import de.ims.icarus2.model.manifest.types.DefaultLink;
import de.ims.icarus2.model.manifest.types.DefaultUrlResource;
import de.ims.icarus2.model.manifest.types.Ref;
import de.ims.icarus2.model.manifest.types.Url;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.util.eval.Expression;
import de.ims.icarus2.util.icon.IconWrapper;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.nio.ByteArrayChannel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
@SuppressWarnings({ "boxing", "deprecation" })
public class ManifestTestUtils {

	public static final ManifestFactory MANIFEST_FACTORY = new DefaultManifestFactory(
			ManifestLocation.newBuilder()
				.readOnly()
				.virtual()
				.build(),
			new DefaultManifestRegistry());

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
		addTestValues(ValueType.BOOLEAN, "illegal", true, false, true);
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

		addTestValues(ValueType.REF, null, Ref.emptyRef(), new DummyRef(), new DummyRef());

		//FIXME add some test values for the other more complex types!
	}

	private static class DummyRef implements Ref<Object> {

		/**
		 * @see de.ims.icarus2.model.manifest.types.Ref#hasTarget()
		 */
		@Override
		public boolean hasTarget() {
			return false;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.types.Ref#getTarget()
		 */
		@Override
		public Object getTarget() {
			return null;
		}

	}

	public static boolean hasTestValues(ValueType type) {
		return testValues.containsKey(type);
	}

	public static Set<ValueType> getAvailableTestTypes() {
		return Collections.unmodifiableSet(testValues.keySet());
	}

	public static Object getOrMockTestValue(ValueType type) {
		if(hasTestValues(type)) {
			return getTestValue(type);
		} else {
			return mock(type.getBaseClass());
		}
	}

	public static Object[] getTestValues(ValueType type) {
		TestInfo info = testValues.get(type);
		if(info==null)
			throw new InternalError("No test values for type: "+type);

		return info.legalValues;
	}

	public static Object getTestValue(ValueType type) {
		TestInfo info = testValues.get(type);
		if(info==null)
			throw new InternalError("No test values for type: "+type);

		return info.legalValues[0];
	}

	public static Object tryGetTestValue(ValueType type) {
		TestInfo info = testValues.get(type);

		return info==null ? null : info.legalValues[0];
	}

	public static Object getIllegalValue(ValueType type) {
		TestInfo info = testValues.get(type);
		if(info==null)
			throw new InternalError("No test values for type: "+type);

		return info.illegalValue;
	}

	public static Object[] getIllegalValues(ValueType type) {
		return new Object[] {getIllegalValue(type)};
	}

	@SuppressWarnings("unchecked")
	public static Expression mockExpression(ValueType valueType) {
		@SuppressWarnings("rawtypes")
		Class clazz = valueType.getBaseClass();
		Expression expression = mock(Expression.class);
		when(expression.getReturnType()).thenReturn(clazz);

		return expression;
	}

	private static final String[] legalIdValues = {
			"abc",
			"abc123",
			"abcdef",
			"abc-def",
			"abc_def",
			"abc.def",
			"abc:def",
			"abc-def123",
			"abc_def123",
			"abc.def123",
			"abc:def123",
	};

	private static final String[] illegalIdValues = {
			"",
			"a",
			"aa",
			"123",
			"123abc",
			"%$§!()",
			"abc/def",
			"abc@def",
			"abc+def",
			"abc~def",
			"abc#def",
			"abc'def",
			"abc*def",
			"abc=def",
			"abc&def",
			"abc%def",
			"abc$def",
			"abc§def",
			"abc\"def",
			"abc!def",
			"abc{def",
			"abc}def",
			"abc[def",
			"abc]def",
			"abc(def",
			"abc)def",
			"abc\\def",
			"abc`def",
			"abc´def",
			"abc°def",
			"abc^def",
			"abc<def",
			"abc>def",
			"abc|def",
			"abc;def",
			"abc"+TestUtils.EMOJI+"def",
	};

	public static String[] getIllegalIdValues() {
		return illegalIdValues.clone();
	}

	public static String[] getLegalIdValues() {
		return legalIdValues.clone();
	}

	public static class Dummy {
		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Dummy@"+hashCode(); //$NON-NLS-1$
		}
	}

	/**
	 * Attempts to set the host manifest at the given embedding depth to
	 * be a {@link Manifest#setIsTemplate(boolean) template}.
	 *
	 * @param manifest
	 * @param embeddingDepth
	 * @return
	 */
	public static <M extends Manifest> boolean stubTemplateContext(M manifest, int embeddingDepth) {
		assertNotNull(manifest);
		assertTrue(embeddingDepth>0, "Cannot mock the live stage");

		// No mock assertion here as there first instance is allowed to be live
		TypedManifest host = manifest;

		while(embeddingDepth-->0 && host!=null) {
			host = (host instanceof Embedded) ?
				((Embedded)host).getHost().orElse(null) : null;
		}

		if(host!=null && host instanceof Manifest) {
			stubIsTemplate((Manifest) assertMock(host));

			return true;
		}

		return false;
	}


	public static <M extends Manifest> boolean stubTemplateContext(M manifest) {
		assertNotNull(manifest);

		// No mock assertion here as there first instance is allowed to be live
		TypedManifest host = manifest;

		boolean stubbed = false;

		Manifest root = null;

		while(host!=null) {
			host = (host instanceof Embedded) ?
				((Embedded)host).getHost().orElse(null) : null;

			if(host!=null && host instanceof Manifest) {
				Manifest m = (Manifest) assertMock(host);
				stubIsTemplate(m);
				stubbed = true;
				root = m;
			}
		}

		if(root!=null) {
			stubHasManifest(root);
		}

		return stubbed;
	}

	public static <M extends Manifest> M stubHasManifest(M manifest) {
		String id = manifest.getId().get();
		assertNotNull(id);

		ManifestRegistry registry = assertMock(manifest.getRegistry());

		when(registry.hasTemplate(id)).thenReturn(Boolean.TRUE);

		return manifest;
	}

	public static <M extends Manifest> M stubIsTemplate(M manifest) {
		assertMock(manifest);
		when(manifest.isTemplate()).thenReturn(Boolean.TRUE);
		when(manifest.isValidTemplate()).thenReturn(Boolean.TRUE);
		return manifest;
	}

	public static <M extends ManifestFragment> M stubId(M manifest, String id) {
		requireNonNull(id);
		assertMock(manifest);
		when(manifest.getId()).thenReturn(Optional.of(id));
		return manifest;
	}

	public static <I extends Identity> I stubIdentity(I identity, String id) {
		requireNonNull(id);
		assertMock(identity);
		when(identity.getId()).thenReturn(Optional.of(id));
		return identity;
	}

	public static <M extends TypedManifest> M stubType(M manifets, ManifestType type) {
		requireNonNull(type);
		when(manifets.getManifestType()).thenReturn(type);
		return manifets;
	}

	public static ManifestLocation mockManifestLocation(boolean template) {
		ManifestLocation location = mock(ManifestLocation.class);
		when(location.isTemplate()).thenReturn(template);
		return location;
	}

	public static ManifestLocation getOrMockManifestLocation(TypedManifest source, boolean template) {
		if(source instanceof Manifest) {
			return ((Manifest)source).getManifestLocation();
		}

		ManifestLocation location = mock(ManifestLocation.class);
		when(location.isTemplate()).thenReturn(template);
		return location;
	}

	public static ManifestRegistry mockManifestRegistry() {
		ManifestRegistry registry = mock(ManifestRegistry.class);
		AtomicInteger uuidGen = new AtomicInteger(0);
		when(registry.createUID()).then(invocation -> uuidGen.incrementAndGet());
		return registry;
	}

	public static ManifestRegistry getOrMockManifestRegistry(TypedManifest source) {
		if(source instanceof Manifest) {
			return ((Manifest)source).getRegistry();
		}

		ManifestRegistry registry = mock(ManifestRegistry.class);
		AtomicInteger uuidGen = new AtomicInteger(0);
		when(registry.createUID()).then(invocation -> uuidGen.incrementAndGet());
		return registry;
	}

	public static <M extends TypedManifest> M mockTypedManifest(ManifestType type) {
		return mockTypedManifestRaw(type, false, false);
	}

	public static <M extends Manifest> M mockTypedManifest(ManifestType type, String id) {
		return stubId(mockTypedManifestRaw(type, false, false), id);
	}

	public static <M extends TypedManifest> M mockTypedManifestWithId(ManifestType type) {
		return mockTypedManifestRaw(type, false, true);
	}

	public static <M extends TypedManifest> M mockTypedManifest(ManifestType type, boolean mockHierarchy) {
		return mockTypedManifestRaw(type, mockHierarchy, false);
	}

	private static <M extends TypedManifest> M mockTypedManifestRaw(ManifestType type, boolean mockHierarchy, boolean mockId) {
		Class<? extends TypedManifest> clazz = type.getBaseClass();
		if(clazz==null)
			throw new InternalError("Cannot create mock for manifest type: "+type);

		M manifest = mockTypedManifest(clazz, mockId);

		when(manifest.getManifestType()).thenReturn(type);

		if(mockHierarchy) {
			mockHierarchy(type, manifest);
		}

		return manifest;
	}

	public static <M extends TypedManifest> M mockTypedManifest(Class<M> clazz) {
		return mockTypedManifest(clazz, false);
	}

	public static <M extends TypedManifest> M mockTypedManifest(Class<? extends TypedManifest> clazz, boolean mockId) {
		@SuppressWarnings("unchecked")
		M manifest = (M) mock(clazz, withSettings().defaultAnswer(CALLS_REAL_METHODS));

		if(Manifest.class.isAssignableFrom(clazz)) {
			Manifest fullManifest = (Manifest) manifest;
			ManifestRegistry registry = mock(ManifestRegistry.class);
			ManifestLocation location = mock(ManifestLocation.class);

			when(fullManifest.getManifestLocation()).thenReturn(location);
			when(fullManifest.getRegistry()).thenReturn(registry);
		}

		if(mockId) {
			if(Identity.class.isAssignableFrom(clazz)) {
				stubIdentity((Identity)manifest, createId(clazz));
			} else if(ManifestFragment.class.isAssignableFrom(clazz)) {
				stubId((ManifestFragment)manifest, createId(clazz));
			}
		}

		return manifest;
	}

	private static String createId(Class<? extends TypedManifest> clazz) {
		return clazz.getSimpleName();
	}

	private static <M extends TypedManifest> void mockHierarchy(ManifestType type, M manifest) {
		TypedManifest current = manifest;
		ManifestType currentType = type;

		ManifestType[] envTypes = null;
		while(Embedded.class.isAssignableFrom(currentType.getBaseClass())
				&& (envTypes = currentType.getRequiredEnvironment()) != null) {
			ManifestType hostType = envTypes[0];
			TypedManifest host = mockTypedManifestRaw(hostType, false, true);

			when(((Embedded)current).getHost()).thenReturn(Optional.of(host));

			current = host;
			currentType = hostType;
		}
	}

	// ASSERTIONS FOR CONTENT

	/**
	 * {@link #assertManifestException(ManifestErrorCode, Executable) Assert} {@link ManifestErrorCode#MANIFEST_TYPE_CAST}
	 * @param executable
	 */
	public static void assertIllegalValue(Executable executable, Object value) {
		assertManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST, executable,
				TestUtils.forValue("Testing illegal value", value));
	}

	public static void assertIllegalValue(BiConsumer<Executable, String> legalityCheck, Executable executable, String msg) {

		if(legalityCheck==null) {
			legalityCheck = ManifestTestUtils::assertIllegalValue;
		}

		legalityCheck.accept(executable, msg);
	}

	/**
	 * {@link #assertManifestException(ManifestErrorCode, Executable) Assert} {@link ManifestErrorCode#MANIFEST_TYPE_CAST}
	 * @param executable
	 */
	public static void assertIllegalId(Executable executable, String msg) {
		assertManifestException(ManifestErrorCode.MANIFEST_INVALID_ID, executable, msg);
	}

	public static void assertManifestException(ErrorCode errorCode, Executable executable, String msg) {
		ManifestException exception = assertThrows(ManifestException.class, executable, msg);
		assertEquals(errorCode, exception.getErrorCode(), msg);
	}

	public static void assertManifestException(ErrorCode errorCode, Executable executable) {
		ManifestException exception = assertThrows(ManifestException.class, executable);
		assertEquals(errorCode, exception.getErrorCode());
	}

	// ASSERTIONS FOR METHOD PATTERNS

	/**
	 * Helper function to be used for consistency.
	 * Transforms a {@link Identity} into a {@link String} by using
	 * its {@link Identity#getId() id}.
	 */
	public static <I extends Identity> Function<I, String> transform_id(){
		return i -> i.getId().orElseThrow(Manifest.invalidId(
				"Identity declares null id"));
	}

	public static final BiConsumer<Executable, String> TYPE_CAST_CHECK = ManifestTestUtils::assertIllegalValue;

	public static final BiConsumer<Executable, String> INVALID_ID_CHECK = (executable, msg) ->
	assertManifestException(ManifestErrorCode.MANIFEST_INVALID_ID, executable, msg);

	public static final BiConsumer<Executable, String> INVALID_INPUT_CHECK = (executable, msg) ->
	assertManifestException(GlobalErrorCode.INVALID_INPUT, executable, msg);

	public static final BiConsumer<Executable, String> DUPLICATE_ID_CHECK = (executable, msg) ->
	assertManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID, executable, msg);

	public static final BiConsumer<Executable, String> UNKNOWN_ID_CHECK = (executable, msg) ->
	assertManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID, executable, msg);
}
