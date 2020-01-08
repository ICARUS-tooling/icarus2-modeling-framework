/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(ImplementationLoader.class)
public class DefaultImplementationLoader extends ImplementationLoader<DefaultImplementationLoader> {

	private static final long serialVersionUID = 7686835827294851192L;

	protected transient Corpus corpus;

	private final CorpusManager corpusManager;

	/**
	 * @param corpusManager
	 */
	public DefaultImplementationLoader(CorpusManager corpusManager) {
		requireNonNull(corpusManager);

		this.corpusManager = corpusManager;
	}

	public final CorpusManager getCorpusManager() {
		return corpusManager;
	}

	public Corpus getCorpus() {
		Corpus corpus =  this.corpus;

		if(corpus==null) {
			Object environment = getEnvironment();
			if(environment instanceof Corpus) {
				corpus = (Corpus) environment;
			}
		}

		return corpus;
	}

	/**
	 *
	 * @param corpus the surrounding corpus of whatever is to be instantiated. Can be {@code null}.
	 * @return
	 */
	public DefaultImplementationLoader corpus(Corpus corpus) {
		requireNonNull(corpus);
		checkState(this.corpus==null);

		this.corpus = corpus;

		return this;
	}

	// Instantiation and Loading

	private String prepareMessage() {

		String message = getMessage();

		if(message == null) {
			message = "";
		} else if(!message.endsWith(" ")) {
			message += " ";
		}

		return message;
	}

	/**
	 *
	 * @param resultClass expected (super) class of the instantiated object
	 * @return a new instance of the class described by the {@code manifest} argument
	 *
	 * @throws ModelException if any kind of error is encountered during instantiation. The error
	 * will then be wrapped into a {@link ModelException} with one of the {@code IMPLEMENTATION_XXX}
	 * {@link ModelError error} types.
	 */
	@Override
	public <T extends Object> T instantiate(Class<T> resultClass) {
		requireNonNull(resultClass);
		checkState("Cannot use no-args constructor when constructor signature was defined", signature==null);

		final ImplementationManifest manifest = getManifest();
		final String message = prepareMessage();
		final boolean isFactory = manifest.isUseFactory();

		final Class<?> clazz = loadClass();

		Object instance = null;

		try {
			instance = clazz.newInstance();
		} catch (InstantiationException e) {
			throw new ModelException(getCorpus(), ManifestErrorCode.IMPLEMENTATION_ERROR,
					message+"Unable to instantiate custom implementation: "+getName(getEnvironment()), e);
		} catch (IllegalAccessException e) {
			throw new ModelException(getCorpus(), ManifestErrorCode.IMPLEMENTATION_NOT_ACCESSIBLE,
					message+"Cannot access custom implementation: "+getName(getEnvironment()), e);
		}

		if(isFactory) {
			ImplementationManifest.Factory factory = (ImplementationManifest.Factory)instance;

			try {
				instance = factory.create(resultClass, manifest, this);
			} catch (Exception e) { // Usually it's bad to catch all exceptions, but we gonna wrap it anyway
				throw new ModelException(ManifestErrorCode.IMPLEMENTATION_FACTORY,
						message+"Delegated instatiation via factory failed: "+getName(getEnvironment()), e);
			}
		}

		return ensureCompatibility(instance, resultClass);
	}

	/**
	 *
	 * @param resultClass expected (super) class of the instantiated object
	 * @param params parameter list to be passed to the custom constructor
	 * @return a new instance of the class described by the {@code manifest} argument
	 *
	 * @throws ModelException if any kind of error is encountered during instantiation. The error
	 * will then be wrapped into a {@link ModelException} with one of the {@code IMPLEMENTATION_XXX}
	 * {@link ModelError error} types.
	 *
	 * @see #signature(Class...)
	 */
	@Override
	public <T extends Object> T instantiate(Class<T> resultClass, Object...params) {
		requireNonNull(resultClass);
		requireNonNull(params);
		checkArgument("Must provide 1 or more constructor arguments", params.length>0);

		checkState("No constructor signature defined", signature!=null);
		checkState("Cannot use custom constructor signature with factory", !manifest.isUseFactory());

		final Class<?> clazz = loadClass();
		final String message = prepareMessage();
		Constructor<?> constructor;

		try {
			constructor = clazz.getConstructor(signature);
		} catch (NoSuchMethodException e) {
			throw new ModelException(getCorpus(), ManifestErrorCode.IMPLEMENTATION_NOT_FOUND,
					message+"Missing custom constructor: "+getName(getEnvironment()), e);
		} catch (SecurityException e) {
			throw new ModelException(getCorpus(), ManifestErrorCode.IMPLEMENTATION_NOT_ACCESSIBLE,
					message+"Cannot access custom constructor: "+getName(getEnvironment()), e);
		}

		Object instance = null;

		try {
			instance = constructor.newInstance(params);
		} catch (InstantiationException | InvocationTargetException e) {
			throw new ModelException(getCorpus(), ManifestErrorCode.IMPLEMENTATION_ERROR,
					message+"Unable to instantiate custom implementation: "+getName(getEnvironment()), e);
		} catch (IllegalAccessException e) {
			throw new ModelException(getCorpus(), ManifestErrorCode.IMPLEMENTATION_NOT_ACCESSIBLE,
					message+"Cannot access custom implementation: "+getName(getEnvironment()), e);
		} catch (IllegalArgumentException e) {
			throw new ModelException(getCorpus(), ManifestErrorCode.IMPLEMENTATION_ERROR,
					message+"Provided arguments are illegal for custom constructor: "+getName(getEnvironment()), e);
		}

		return ensureCompatibility(instance, resultClass);
	}

	private <T extends Object> T ensureCompatibility(Object instance, Class<T> resultClass) {

		if(instance==null)
			throw new ModelException(corpus, ManifestErrorCode.IMPLEMENTATION_ERROR,
					message+"Instance is null: "+getName(environment));

		if(!resultClass.isInstance(instance))
			throw new ModelException(corpus, ManifestErrorCode.IMPLEMENTATION_INCOMPATIBLE,
					message+Messages.mismatch("Incompatible types", resultClass, instance.getClass()));

		return resultClass.cast(instance);
	}

	/**
	 *
	 * @return
	 *
	 * @throws ModelException if any kind of error is encountered during instantiation. The error
	 * will then be wrapped into a {@link ModelException} with one of the {@code IMPLEMENTATION_XXX}
	 * {@link ModelError error} types.
	 */
	@Override
	public Class<?> loadClass() {

		checkState("Implementation manifest missing", manifest!=null);

		final String message = prepareMessage();

		final SourceType sourceType = manifest.getSourceType().orElse(ImplementationManifest.DEFAULT_SOURCE_TYPE);
		final String source = manifest.getSource().orElse(null);

		ClassLoader classLoader = getCorpusManager().getImplementationClassLoader(manifest);
		String classname = manifest.getClassname().orElse(null);

		Class<?> clazz = null;

		switch (sourceType) {
		case EXTENSION: {
			try {
				clazz = getCorpusManager().resolveExtension(source);
			} catch(ClassNotFoundException e) {
				throw new ModelException(getCorpus(), ManifestErrorCode.IMPLEMENTATION_NOT_FOUND,
						message+"Unable to resolve extension uid: "+source, e);
			}
		} break;

		case PLUGIN: {
			classLoader = getCorpusManager().getPluginClassLoader(source);
			if(classLoader==null)
				throw new ModelException(getCorpus(), ManifestErrorCode.IMPLEMENTATION_NOT_FOUND,
						message+"Unknown plugin uid: "+source);
		} break;

		case EXTERN: {
			if(source==null) {
				//TODO
			} else {
				//TODO
			}

			throw new UnsupportedOperationException();
		} //break;

		default:
			break;
		}

		if(clazz==null) {
			try {
				clazz = classLoader.loadClass(classname);
			} catch (ClassNotFoundException e) {
				throw new ModelException(getCorpus(), ManifestErrorCode.IMPLEMENTATION_NOT_FOUND,
						message+"Failed to find custom implementation: "+classname, e);
			}
		}

		return clazz;
	}
}
