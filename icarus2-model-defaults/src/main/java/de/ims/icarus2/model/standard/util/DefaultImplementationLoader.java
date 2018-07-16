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
 *
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
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.util.Messages;

/**
 * @author Markus Gärtner
 *
 */
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
		final String message = getMessage();
		final boolean isFactory = manifest.isUseFactory();

		final Class<?> clazz = loadClass();

		Object instance = null;

		try {
			instance = clazz.newInstance();
		} catch (InstantiationException e) {
			throw new ModelException(corpus, ManifestErrorCode.IMPLEMENTATION_ERROR,
					message+"Unable to instantiate custom implementation: "+getName(getEnvironment()), e);
		} catch (IllegalAccessException e) {
			throw new ModelException(corpus, ManifestErrorCode.IMPLEMENTATION_NOT_ACCESSIBLE,
					message+"Cannot access custom implementation: "+getName(getEnvironment()), e);
		}

		if(isFactory) {
			ImplementationManifest.Factory factory = (ImplementationManifest.Factory)instance;

			try {
				instance = factory.create(resultClass, manifest, this);
			} catch (Exception e) {
				throw new ModelException(getCorpus(), ManifestErrorCode.IMPLEMENTATION_FACTORY,
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
		checkState("Cannot use custom constructor signature with factory", manifest.isUseFactory());

		final Class<?> clazz = loadClass();
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
					message+Messages.mismatchMessage("Incompatible types", resultClass, instance.getClass()));

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

		if(message == null) {
			message = "";
		} else if(!message.endsWith(" ")) {
			message += " ";
		}

//		if(!(owner instanceof String)) {
//			owner = notNull(owner==null ? manifest.getId() :getName(owner), "<undefined>");
//		}

		final SourceType sourceType = manifest.getSourceType();
		final String source = manifest.getSource();

		ClassLoader classLoader = manifest.getManifestLocation().getClassLoader();
		String classname = manifest.getClassname();

		Class<?> clazz = null;

		switch (sourceType) {
		case EXTENSION: {
			try {
				clazz = getCorpusManager().resolveExtension(source);
			} catch(Exception e) {
				throw new ModelException(corpus, ManifestErrorCode.IMPLEMENTATION_NOT_FOUND,
						message+"Unable to resolve extension uid: "+source, e); //$NON-NLS-1$
			}
		} break;

		case PLUGIN: {
			try {
				classLoader = getCorpusManager().getPluginClassLoader(source);
			} catch(Exception e) {
				throw new ModelException(corpus, ManifestErrorCode.IMPLEMENTATION_NOT_FOUND,
						message+"Unknown plugin uid: "+source, e); //$NON-NLS-1$
			}
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
				throw new ModelException(corpus, ManifestErrorCode.IMPLEMENTATION_NOT_FOUND,
						message+"Failed to find custom implementation: "+getName(environment), e); //$NON-NLS-1$
			}
		}

		return clazz;
	}
}
