/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.util;

import static de.ims.icarus2.test.TestUtils.ILLEGAL_STATE_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_VALUE;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.assertRestrictedSetter;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.api.ImplementationLoaderTest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType;
import de.ims.icarus2.test.Dummy;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class DefaultImplementationLoaderTest implements ImplementationLoaderTest {

	@Override
	public Class<? extends DefaultImplementationLoader> getTestTargetClass() {
		return DefaultImplementationLoader.class;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ImplementationLoaderTest#getSupportedSourceTypes()
	 */
	@Override
	public Set<SourceType> getSupportedSourceTypes() {
		return set(SourceType.DEFAULT, SourceType.EXTENSION, SourceType.PLUGIN);
	}

	@Override
	public DefaultImplementationLoader createTestInstance(TestSettings settings) {
		CorpusManager manager = mock(CorpusManager.class);
		return settings.process(new DefaultImplementationLoader(manager));
	}

	private static CorpusManager mockCorpusManager(Class<?> classToLoad,
			String source, String classname) throws ClassNotFoundException {

		// Special class loader for the class to load
		ClassLoader classLoader = mock(ClassLoader.class);
		doAnswer(invocation -> {
			String name = invocation.getArgument(0);
			if(classname.equals(name)) {
				return classToLoad;
			} else if(Dummy.CLASS_NAME.equals(name)) {
				return Dummy.class;
			} else
				throw new ClassNotFoundException("No such class: "+classname);
		}).when(classLoader).loadClass(anyString());

		CorpusManager manager = mock(CorpusManager.class);

		// Define behavior for extensions
		doAnswer(invocation -> {
			String name = invocation.getArgument(0);
			if(source.equals(name)) {
				return classToLoad;
			} else if(Dummy.CLASS_NAME.equals(name)) {
				return Dummy.class;
			} else
				throw new ClassNotFoundException("No such extension: "+classname);
		}).when(manager).resolveExtension(anyString());

		// Define behavior for plugins
		doAnswer(invocation -> {
			String name = invocation.getArgument(0);
			if(source.equals(name)) {
				return classLoader;
			} else if(Dummy.CLASS_NAME.equals(name)) {
				return Dummy.class.getClassLoader();
			} else
				return null;
		}).when(manager).getPluginClassLoader(anyString());

		// Define behavior for plugins
		when(manager.getImplementationClassLoader(any())).thenReturn(classLoader);

		return manager;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ImplementationLoaderTest#createForLoading(de.ims.icarus2.test.TestSettings, java.lang.Class)
	 */
	@Override
	public ImplementationLoader<?> createForLoading(
			TestSettings settings, Class<?> classToLoad,
			String source, String classname) {

		CorpusManager manager;
		try {
			manager = mockCorpusManager(classToLoad, source, classname);
		} catch (ClassNotFoundException e) {
			throw new AssertionError("Failed to mock functional corpus manager", e);
		}

		DefaultImplementationLoader loader = new DefaultImplementationLoader(manager);
		loader.corpus(mock(Corpus.class));

		return settings.process(loader);
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	public void testMandatoryConstructors() throws Exception {
		ImplementationLoaderTest.super.testMandatoryConstructors();

		// Make sure the implementation doesn't accept null managers
		assertNPE(() -> new DefaultImplementationLoader(null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.util.DefaultImplementationLoader#getCorpusManager()}.
	 */
	@Test
	void testGetCorpusManager() {
		assertNotNull(((DefaultImplementationLoader)create()).getCorpusManager());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.util.DefaultImplementationLoader#getCorpus()}.
	 */
	@Test
	void testGetCorpus() {
		assertGetter((DefaultImplementationLoader)create(),
				mock(Corpus.class), NO_VALUE(),
				NO_DEFAULT(),
				DefaultImplementationLoader::getCorpus,
				DefaultImplementationLoader::corpus);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.util.DefaultImplementationLoader#corpus(de.ims.icarus2.model.api.corpus.Corpus)}.
	 */
	@Test
	void testCorpus() {
		assertRestrictedSetter((DefaultImplementationLoader)create(),
				DefaultImplementationLoader::corpus,
				mock(Corpus.class),
				mock(Corpus.class),
				NPE_CHECK, ILLEGAL_STATE_CHECK);

		// Test special behavior if corpus isn't set directly
		DefaultImplementationLoader instance = (DefaultImplementationLoader)create();
		assertNull(instance.getCorpus());

		// Assert that loader derived corpus from environment
		Corpus corpus1 = mock(Corpus.class);
		instance.environment(Corpus.class, corpus1);
		assertSame(corpus1, instance.getCorpus());

		// Assert that explicitly set corpus overrides environment
		Corpus corpus2 = mock(Corpus.class);
		instance.corpus(corpus2);
		assertSame(corpus2, instance.getCorpus());
	}

}
