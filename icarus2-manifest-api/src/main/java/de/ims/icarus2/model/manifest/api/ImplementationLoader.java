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
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Constructor;

import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
public abstract class ImplementationLoader<L extends ImplementationLoader<L>> extends Options {

	private static final long serialVersionUID = -5553129801313520868L;

	protected transient Object environment;
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

//	/**
//	 *
//	 * @param environment the manifest surrounding the implementation manifest
//	 * @return
//	 */
//	public L environment(MemberManifest environment) {
//		checkNotNull(environment);
//		checkState(this.environment==null);
//
//		this.environment = environment;
//
//		return thisAsCast();
//	}

	/**
	 *
	 * @param environment the directly surrounding "thing" (typically a {@link CorpusMember}) that initiated
	 * the instantiation.
	 * @return
	 */
	public L environment(Object environment) {
		requireNonNull(environment);
		checkState(this.environment==null);

		this.environment = environment;

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

	public Object getEnvironment() {
		return environment;
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
}
