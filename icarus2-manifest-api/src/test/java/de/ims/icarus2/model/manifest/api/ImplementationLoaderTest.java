/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.TestUtils.ILLEGAL_STATE_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_VALUE;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertMock;
import static de.ims.icarus2.test.TestUtils.assertRestrictedSetter;
import static de.ims.icarus2.test.TestUtils.settings;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType;
import de.ims.icarus2.test.Dummy;
import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.fail.FailOnConstructor;

/**
 * Test suite for {@link ImplementationLoader}
 *
 * @author Markus Gärtner
 *
 */
public interface ImplementationLoaderTest<L extends ImplementationLoader<L>> extends GenericTest<L> {

	/**
	 * Allows actual test implementations to specify which source types their
	 * respective loader implementations are expected to support.
	 *
	 * @return
	 */
	Set<SourceType> getSupportedSourceTypes();

	/**
	 * Creates an instance of the {@link ImplementationLoader} under test that is
	 * properly initialized with a mocked environment (if needed), so that it can
	 * serve in tests for the following method:
	 * <ul>
	 * <li>{@link ImplementationLoader#loadClass()}</li>
	 * <li>{@link ImplementationLoader#instantiate(Class)}</li>
	 * <li>{@link ImplementationLoader#instantiate(Class, Object...)}</li>
	 * </ul>
	 *
	 * @return
	 */
	@Provider
	L createForLoading(TestSettings settings, Class<?> classToLoad,
			String source, String classname);

	@SuppressWarnings("boxing")
	default L createPreparedInstanceForLoading(TestSettings settings, Class<?> classToLoad,
			SourceType sourceType, String source, String classname, Class<?>[] constructorSig, boolean factory) {

		// Mock some utility parts

		ImplementationManifest manifest = mock(ImplementationManifest.class);
		when(manifest.getClassname()).thenReturn(Optional.of(classname));
		when(manifest.getSource()).thenReturn(Optional.of(source));
		when(manifest.getSourceType()).thenReturn(sourceType);
		when(manifest.isUseFactory()).thenReturn(factory);

		MemberManifest host = mock(MemberManifest.class);
		when(host.getName()).thenReturn(Optional.of("host"));

		when(manifest.getHost()).thenReturn(Optional.of(host));

		// Use same loader for all calls under this source type
		L instance = createForLoading(settings(), classToLoad, source, classname);

		instance.manifest(manifest);
		instance.environment(host);

		if(constructorSig!=null) {
			instance.signature(constructorSig);
		}

		return instance;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationLoader#instantiate(java.lang.Class)}.
	 */
	@Test
	default void testInstantiateClassOfT() {
		Set<SourceType> supportedSourceTypes = getSupportedSourceTypes();

		final Class<?> classToLoad = NoArgsDummy.class;
		final String classname = classToLoad.getName();
		final String realSource = "sourceToTest";

		// Test valid and discoverable classes
		for(SourceType sourceType : SourceType.values()) {

			// Use same loader for all regular calls under this source type
			L instance = createPreparedInstanceForLoading(settings(),
					classToLoad, sourceType, realSource, classname, null, false);

			// Basic check
			if(supportedSourceTypes.contains(sourceType)) {
				Object createdObject = instance.instantiate(classToLoad);
				assertNotNull(createdObject);
				assertTrue(classToLoad.isInstance(createdObject));


				ImplementationManifest manifest = assertMock(instance.getManifest());

				//----------------------------------------
				// Now force and expect some errors
				//----------------------------------------

				// Force ClassCastException
				assertIcarusException(ManifestErrorCode.IMPLEMENTATION_INCOMPATIBLE,
						() -> instance.instantiate(Dummy.class),
						"Testing unfindable class for source type: "+sourceType);

				// Force ClassNotFoundException
				when(manifest.getClassname()).thenReturn(Optional.of("notTheRealClassName"));
				if(sourceType==SourceType.EXTENSION) {
					when(manifest.getSource()).thenReturn(Optional.of("notTheRealSource"));
				} else {
					when(manifest.getSource()).thenReturn(Optional.of(realSource));
				}
				assertIcarusException(ManifestErrorCode.IMPLEMENTATION_NOT_FOUND,
						() -> instance.instantiate(classToLoad),
						"Testing undiscoverable class for source type: "+sourceType);

				// Force InstantiationException
				Class<?> failInstantiationClass = FailOnConstructor.createClassForException(
						"", "FailInstantiationClass", null, InstantiationException.class);
				L failInstantiationLoader = createPreparedInstanceForLoading(settings(),
						failInstantiationClass, sourceType, realSource,
						failInstantiationClass.getName(), null, false);
				assertIcarusException(ManifestErrorCode.IMPLEMENTATION_ERROR,
						() -> failInstantiationLoader.instantiate(failInstantiationClass),
						"Testing forced InstantiationException for source type: "+sourceType);

				// Force IllegalAccessException
				Class<?> illegalAccessClass = FailOnConstructor.createClassForException(
						"", "IllegalAccessClass", null, IllegalAccessException.class);
				L illegalAccessLoader = createPreparedInstanceForLoading(settings(),
						illegalAccessClass, sourceType, realSource,
						illegalAccessClass.getName(), null, false);
				assertIcarusException(ManifestErrorCode.IMPLEMENTATION_NOT_ACCESSIBLE,
						() -> illegalAccessLoader.instantiate(illegalAccessClass),
						"Testing forced IllegalAccessException for source type: "+sourceType);

				//----------------------------------------
				// Verify proper delegation to factory
				//----------------------------------------

				// Valid factory creation, make sure the loader properly delegates arguments
				L validFactoryLoader = createPreparedInstanceForLoading(settings(),
						TestFactory.class, sourceType, realSource,
						TestFactory.class.getName(), null, true);
				TestFactory factory = validFactoryLoader.instantiate(TestFactory.class);
				assertSame(validFactoryLoader, factory.environment);
				assertSame(validFactoryLoader.getManifest(), factory.manifest);
				assertSame(TestFactory.class, factory.resultClass);

				// Force factory to create null result
				L nullFactoryLoader = createPreparedInstanceForLoading(settings(),
						NullFactory.class, sourceType, realSource,
						NullFactory.class.getName(), null, true);
				assertIcarusException(ManifestErrorCode.IMPLEMENTATION_ERROR,
						() -> nullFactoryLoader.instantiate(Dummy.class),
						"Testing forced null return of factory for source type: "+sourceType);

				// Force factory to create null result
				L failFactoryLoader = createPreparedInstanceForLoading(settings(),
						FailFactory.class, sourceType, realSource,
						FailFactory.class.getName(), null, true);
				assertIcarusException(ManifestErrorCode.IMPLEMENTATION_FACTORY,
						() -> failFactoryLoader.instantiate(Dummy.class),
						"Testing forced failure of factory for source type: "+sourceType);

			} else {
				assertThrows(UnsupportedOperationException.class,
						() -> instance.instantiate(classToLoad));
			}
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationLoader#instantiate(java.lang.Class, java.lang.Object[])}.
	 */
	@Test
	default void testInstantiateClassOfTObjectArray() {
		Set<SourceType> supportedSourceTypes = getSupportedSourceTypes();

		final Class<StringIntStringDummy> classToLoad = StringIntStringDummy.class;
		final Class<?>[] constructorSig = {String.class, Integer.class, String.class};
		@SuppressWarnings("boxing")
		final Object[] constructorArgs = {"String1", 999, "String2"};
		final String classname = classToLoad.getName();
		final String realSource = "sourceToTest";

		// Test valid and undiscoverable classes
		for(SourceType sourceType : SourceType.values()) {

			// Use same loader for all regular calls under this source type
			L instance = createPreparedInstanceForLoading(settings(),
					classToLoad, sourceType, realSource,
					classname, constructorSig, false);

			// Basic check
			if(supportedSourceTypes.contains(sourceType)) {
				StringIntStringDummy createdObject = instance.instantiate(classToLoad, constructorArgs);
				assertNotNull(createdObject);
				assertTrue(classToLoad.isInstance(createdObject));

				// Verify passing of constructor arguments
				assertEquals(constructorArgs[0], createdObject.arg0);
				assertEquals(constructorArgs[1], createdObject.arg1);
				assertEquals(constructorArgs[2], createdObject.arg2);

				ImplementationManifest manifest = assertMock(instance.getManifest());

				//----------------------------------------
				// Now force and expect some errors
				//----------------------------------------

				// Force ClassCastException
				assertIcarusException(ManifestErrorCode.IMPLEMENTATION_INCOMPATIBLE,
						() -> instance.instantiate(Dummy.class, constructorArgs),
						"Testing unfindable class for source type: "+sourceType);

				// Force ClassNotFoundException
				when(manifest.getClassname()).thenReturn(Optional.of("notTheRealClassName"));
				if(sourceType==SourceType.EXTENSION) {
					when(manifest.getSource()).thenReturn(Optional.of("notTheRealSource"));
				} else {
					when(manifest.getSource()).thenReturn(Optional.of(realSource));
				}
				assertIcarusException(ManifestErrorCode.IMPLEMENTATION_NOT_FOUND,
						() -> instance.instantiate(classToLoad, constructorArgs),
						"Testing undiscoverable class for source type: "+sourceType);

				// Force InstantiationException
				Class<?> failInstantiationClass = FailOnConstructor.createClassForException(
						"", "FailInstantiationClass", null, InstantiationException.class, constructorSig);
				L failInstantiationLoader = createPreparedInstanceForLoading(settings(),
						failInstantiationClass, sourceType, realSource,
						failInstantiationClass.getName(), constructorSig, false);
				assertIcarusException(ManifestErrorCode.IMPLEMENTATION_ERROR,
						() -> failInstantiationLoader.instantiate(failInstantiationClass, constructorArgs),
						"Testing forced InstantiationException for source type: "+sourceType);

				// Force InvocationTargetException
				Class<?> failInvocationClass = FailOnConstructor.createClassForException(
						"", "FailInvocationClass", null, Exception.class, constructorSig);
				L failInvocationLoader = createPreparedInstanceForLoading(settings(),
						failInvocationClass, sourceType, realSource,
						failInvocationClass.getName(), constructorSig, false);
				assertIcarusException(ManifestErrorCode.IMPLEMENTATION_ERROR,
						() -> failInvocationLoader.instantiate(failInvocationClass, constructorArgs),
						"Testing forced InvocationTargetException for source type: "+sourceType);

				// Force NoSuchMethodException
				L noSuchMethodLoader = createPreparedInstanceForLoading(settings(),
						PrivateStringIntStringDummy.class, sourceType, realSource,
						PrivateStringIntStringDummy.class.getName(), constructorSig, false);
				assertIcarusException(ManifestErrorCode.IMPLEMENTATION_NOT_FOUND,
						() -> noSuchMethodLoader.instantiate(PrivateStringIntStringDummy.class, constructorArgs),
						"Testing forced NoSuchMethodException for source type: "+sourceType);

				// Force IllegalArgumentException
				L illegalArgumentLoader = createPreparedInstanceForLoading(settings(),
						StringIntStringDummy.class, sourceType, realSource,
						StringIntStringDummy.class.getName(), constructorSig, false);
				assertIcarusException(ManifestErrorCode.IMPLEMENTATION_ERROR,
						() -> illegalArgumentLoader.instantiate(StringIntStringDummy.class, new Object()),
						"Testing forced IllegalArgumentException for source type: "+sourceType);

			} else {
				assertThrows(UnsupportedOperationException.class,
						() -> instance.instantiate(classToLoad, constructorArgs));
			}
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationLoader#loadClass()}.
	 */
	@Test
	default void testLoadClass() {
		Set<SourceType> supportedSourceTypes = getSupportedSourceTypes();

		final Class<?> classToLoad = NoArgsDummy.class;
		final String classname = classToLoad.getName();
		final String realSource = "sourceToTest";

		// Test valid and undiscoverable classes
		for(SourceType sourceType : SourceType.values()) {

			// Use same loader for all calls under this source type
			L instance = createPreparedInstanceForLoading(settings(),
					classToLoad, sourceType, realSource, classname, null, false);

			// Basic check
			if(supportedSourceTypes.contains(sourceType)) {
				assertEquals(classToLoad, instance.loadClass());


				ImplementationManifest manifest = assertMock(instance.getManifest());

				//----------------------------------------
				// Now force and expect some errors
				//----------------------------------------

				// Make the class undiscoverable
				when(manifest.getClassname()).thenReturn(Optional.of("notTheRealClassName"));
				if(sourceType==SourceType.EXTENSION) {
					when(manifest.getSource()).thenReturn(Optional.of("notTheRealSource"));
				} else {
					when(manifest.getSource()).thenReturn(Optional.of(realSource));
				}
				assertIcarusException(ManifestErrorCode.IMPLEMENTATION_NOT_FOUND,
						() -> instance.loadClass(),
						"Testing undiscoverable class for source type: "+sourceType);

			} else {
				assertThrows(UnsupportedOperationException.class,
						() -> instance.loadClass());
			}
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationLoader#manifest(de.ims.icarus2.model.manifest.api.ImplementationManifest)}.
	 */
	@Test
	default void testManifest() {
		assertRestrictedSetter(create(),
				ImplementationLoader::manifest,
				(ImplementationManifest)mockTypedManifest(ManifestType.IMPLEMENTATION_MANIFEST),
				(ImplementationManifest)mockTypedManifest(ManifestType.IMPLEMENTATION_MANIFEST),
				NPE_CHECK, ILLEGAL_STATE_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationLoader#environment(java.lang.Object)}.
	 */
	@Test
	default void testEnvironment() {
		assertRestrictedSetter(create(),
				ImplementationLoader::environment,
				mock(Object.class),
				mock(Object.class),
				NPE_CHECK, ILLEGAL_STATE_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationLoader#message(java.lang.String)}.
	 */
	@Test
	default void testMessage() {
		assertRestrictedSetter(create(),
				ImplementationLoader::message,
				"my first test message",
				"my second test message",
				NPE_CHECK, ILLEGAL_STATE_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationLoader#signature(java.lang.Class<?>[])}.
	 */
	@Test
	default void testSignature() {
		assertRestrictedSetter(create(),
				ImplementationLoader::signature,
				new Class[] {String.class, Object.class, Integer.class},
				new Class[] {Class.class},
				NPE_CHECK, ILLEGAL_STATE_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationLoader#getEnvironment()}.
	 */
	@Test
	default void testGetEnvironment() {
		assertGetter(create(),
				mock(Object.class), NO_VALUE(),
				NO_DEFAULT(),
				ImplementationLoader::getEnvironment,
				ImplementationLoader::environment);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationLoader#getMessage()}.
	 */
	@Test
	default void testGetMessage() {
		assertGetter(create(),
				"my message 1", NO_VALUE(),
				NO_DEFAULT(),
				ImplementationLoader::getMessage,
				ImplementationLoader::message);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationLoader#getSignature()}.
	 */
	@Test
	default void testGetSignature() {
		assertGetter(create(),
				new Class[] {String.class, Object.class, Integer.class},
				NO_VALUE(),
				NO_DEFAULT(),
				ImplementationLoader::getSignature,
				ImplementationLoader::signature);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationLoader#getManifest()}.
	 */
	@Test
	default void testGetManifest() {
		assertGetter(create(),
				(ImplementationManifest)mockTypedManifest(ManifestType.IMPLEMENTATION_MANIFEST),
				NO_VALUE(),
				NO_DEFAULT(),
				ImplementationLoader::getManifest,
				ImplementationLoader::manifest);
	}

	public static class NoArgsDummy {
		// dummy class for testing class loading
	}

	public static class StringIntStringDummy extends NoArgsDummy {
		public final String arg0;
		public final Integer arg1;
		public final String arg2;

		public StringIntStringDummy(String arg0, Integer arg1, String arg2) {
			this.arg0 = requireNonNull(arg0);
			this.arg1 = requireNonNull(arg1);
			this.arg2 = requireNonNull(arg2);
		}
	}

	public static class PrivateStringIntStringDummy extends NoArgsDummy {

		private PrivateStringIntStringDummy(String arg0, Integer arg1, String arg2) {
			// no-op
		}
	}

	public static class TestFactory implements ImplementationManifest.Factory {

		public Class<?> resultClass;
		public ImplementationManifest manifest;
		public ImplementationLoader<?> environment;

		/**
		 * @see de.ims.icarus2.model.manifest.api.ImplementationManifest.Factory#create(java.lang.Class, de.ims.icarus2.model.manifest.api.ImplementationManifest, de.ims.icarus2.model.manifest.api.ImplementationLoader)
		 */
		@Override
		public <T> T create(Class<T> resultClass, ImplementationManifest manifest, ImplementationLoader<?> environment)
				throws ClassNotFoundException, IllegalAccessException, InstantiationException, ClassCastException {

			// Save arguments for later assertions
			this.resultClass = resultClass;
			this.manifest = manifest;
			this.environment = environment;

			return resultClass.cast(this);
		}

	}

	public static class NullFactory implements ImplementationManifest.Factory {

		/**
		 * @see de.ims.icarus2.model.manifest.api.ImplementationManifest.Factory#create(java.lang.Class, de.ims.icarus2.model.manifest.api.ImplementationManifest, de.ims.icarus2.model.manifest.api.ImplementationLoader)
		 */
		@Override
		public <T> T create(Class<T> resultClass, ImplementationManifest manifest, ImplementationLoader<?> environment)
				throws ClassNotFoundException, IllegalAccessException, InstantiationException, ClassCastException {
			return null;
		}

	}

	public static class FailFactory implements ImplementationManifest.Factory {

		/**
		 * @see de.ims.icarus2.model.manifest.api.ImplementationManifest.Factory#create(java.lang.Class, de.ims.icarus2.model.manifest.api.ImplementationManifest, de.ims.icarus2.model.manifest.api.ImplementationLoader)
		 */
		@Override
		public <T> T create(Class<T> resultClass, ImplementationManifest manifest, ImplementationLoader<?> environment)
				throws ClassNotFoundException, IllegalAccessException, InstantiationException, ClassCastException {
			throw new ClassNotFoundException();
		}

	}
}
