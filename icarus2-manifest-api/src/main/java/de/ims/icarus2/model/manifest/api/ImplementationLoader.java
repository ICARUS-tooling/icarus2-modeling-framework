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
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.strings.StringUtil.getName;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import de.ims.icarus2.model.manifest.api.ImplementationManifest.Factory;
import de.ims.icarus2.util.Options;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

/**
 * Utility class for loading actual implementations for individual
 * components of the model framework. Intended for one-shot usage!
 *
 * @author Markus Gärtner
 *
 */
@NotThreadSafe
public abstract class ImplementationLoader<L extends ImplementationLoader<L>> extends Options {

	private static final long serialVersionUID = -5553129801313520868L;

	protected transient Map<Class<?>, Object> environment;
	protected transient String message;
	@SuppressWarnings("rawtypes")
	protected transient Class[] signature;

	protected transient ImplementationManifest manifest;

	/**
	 * Creates a new instance of the class defined in the previously set {@link ImplementationManifest manifest}.
	 * It is the responsibility of the loader to either do the instantiation itself or delegate to a new
	 * {@link Factory factory} instance, depending on the data in the manifest. In addition it has to make
	 * sure that the returned instance is assignment compatible with the specified {@code resultClass}!
	 *
	 * @param resultClass
	 * @return
	 */
	public abstract <T extends Object> T instantiate(Class<T> resultClass);

	/**
	 * Creates a new instance of the class defined in the previously set {@link ImplementationManifest manifest}.
	 * Unlike the simpler {@link #instantiate(Class)} method this version does not support delegation to a
	 * {@link Factory factory}. Note that this method <b>must</b> be used in conjunction with a custom signature
	 * set prior to calling this method via {@link #signature(Class...)}! Instantiation will then be done by
	 * looking up the respective {@link Constructor} object and invoking its {@link Constructor#newInstance(Object...)}
	 * method with the given {@code arguments}.
	 *
	 * @param resultClass
	 * @param arguments
	 * @return
	 *
	 * @see #instantiate(Class)
	 */
	public abstract <T extends Object> T instantiate(Class<T> resultClass, Object...arguments);

	public abstract Class<?> loadClass();

	// Construction

	@SuppressWarnings("unchecked")
	private L thisAsCast() {
		return (L) this;
	}

	/**
	 *
	 * @param manifest the manifest describing the implementation that should be loaded and instantiated
	 * @return
	 */
	public L manifest(ImplementationManifest manifest) {
		requireNonNull(manifest);
		checkState(this.manifest==null);

		this.manifest = manifest;

		return thisAsCast();
	}

	/**
	 *
	 * @param environment the directly surrounding "thing" (typically a {@link CorpusMember}) that initiated
	 * the instantiation.
	 * @return
	 */
	public <T> L environment(Class<T> clazz, T environment) {
		requireNonNull(clazz);
		requireNonNull(environment);

		if(this.environment==null) {
			this.environment = new Object2ObjectArrayMap<>();
		}

		checkState("environment already set for type "+clazz, !this.environment.containsKey(clazz));

		this.environment.put(clazz, environment);

		return thisAsCast();
	}

	/**
	 *
	 * @param message optional message to be used as prefix when creating error messages or log entries.
	 * Can be {@code null}.
	 * @return
	 */
	public L message(String message) {
		requireNonNull(message);
		checkState(this.message==null);

		this.message = message;

		return thisAsCast();
	}

	public L signature(Class<?>...signature) {
		requireNonNull(signature);
		checkState(this.signature==null);

		this.signature = signature;

		return thisAsCast();
	}

	public @Nullable <T> T getEnvironment(Class<T> clazz) {
		requireNonNull(clazz);
		if(environment==null) {
			return null;
		}
		return clazz.cast(environment.get(clazz));
	}

	public String getMessage() {
		return message;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getSignature() {
		return signature;
	}

	public ImplementationManifest getManifest() {
		return manifest;
	}

	protected String getEnvironmentDescription() {
		if(environment==null) {
			return "[]";
		}

		StringBuilder sb = new StringBuilder();
		for(Iterator<Map.Entry<Class<?>, Object>> it = environment.entrySet().iterator(); it.hasNext();) {
			Map.Entry<Class<?>, Object> e = it.next();
			sb.append(e.getKey().getName()).append('=').append(getName(e.getValue()));
			if(it.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(']');

		return sb.toString();
	}
}
