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

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;

/**
 * Extension of a mere fragment, a "real" manifest comes with full support for
 * templating, i.e. inheritance of almost all declared properties or fields.
 * In addition manifests can have versions assigned to them to better support
 * compatibility or suitability checks.
 *
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface Manifest extends ManifestFragment {

	public static Supplier<ManifestException> invalidId(String message) {
		return ManifestException.error(ManifestErrorCode.MANIFEST_INVALID_ID, message);
	}

	public static Supplier<ManifestException> unknownId(String message) {
		return ManifestException.error(ManifestErrorCode.MANIFEST_UNKNOWN_ID, message);
	}

	/**
	 * Returns a special integer id created by the framework that is
	 * used for fast lookup or mapping operations involving manifests. Note
	 * that such an uid is only valid for the duration of the current session.
	 * It is not guaranteed (in fact it is very unlikely) that a manifest gets
	 * assigned the same uid across multiple sessions, since the assignment
	 * is performed on construction time of the manifest, which happens when the
	 * user starts to work with it. The returned value is always positive and not
	 * {@code 0}!
	 * <p>
	 * Note further that this value is not meant to be contained in any persistent
	 * form of a manifest (e.g. XML) and should only be created at runtime.
	 *
	 * @return
	 */
	int getUID();

	/**
	 * Returns whether or not this {@code Manifest} is meant to be a template,
	 * i.e. an abstract base description that cannot be used to directly instantiate
	 * objects from. A manifest is a template if it is a top-level manifest hosted within
	 * a {@link ManifestLocation} that supports templates and has previously been
	 * {@link #setIsTemplate(boolean) marked as template}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	boolean isTemplate();

	/**
	 * Checks if this manifest {@link #isTemplate() is a template} and also if it
	 * {@link ManifestRegistry#hasTemplate(String) has been registered} as a template
	 * with the {@link #getRegistry() registry} that manages it.
	 * @return
	 */
	@SuppressWarnings("boxing")
	default boolean isValidTemplate() {
		return isTemplate() && getId().map(id -> getRegistry().hasTemplate(id)).orElse(Boolean.FALSE);
	}

	/**
	 * @return {@code true} iff the {@link Embedded#getHost() host} chain of this
	 * manifest contains a {@link Manifest#isValidTemplate() valid template} or if this
	 * manifest is a {@link #isValidTemplate() template itself}.
	 *
	 * @see ManifestUtils#hasTemplateContext(TypedManifest)
	 */
	default boolean hasTemplateContext() {
		return ManifestUtils.hasTemplateContext(this);
	}

	/**
	 * Throws a {@link ManifestException} with {@link ManifestErrorCode#MANIFEST_ILLEGAL_TEMPLATE_STATE}
	 * in case this manifest is declared to be a {@link #isTemplate() template}.
	 */
	default void checkNotTemplate() {
		if(isTemplate())
			throw new ManifestException(ManifestErrorCode.MANIFEST_ILLEGAL_TEMPLATE_STATE,
					"Manifest is a template: "+getId());
	}

	/**
	 * Returns {@code true} iff this manifest has a valid template assigned to it.
	 *
	 * @return
	 */
	boolean hasTemplate();

	/**
	 * If derived from another template, this method returns the object used for
	 * templating. Otherwise an exception is thrown.
	 *
	 * @return
	 * @throws ManifestException of type {@link GlobalErrorCode#ILLEGAL_STATE} iff
	 * this manifest is not declared to {@link #hasTemplate() have been assigned a template}
	 *
	 * @see #hasTemplate()
	 */
	@AccessRestriction(AccessMode.READ)
	Manifest getTemplate();

	/**
	 * Similar to {@link #getTemplate()} but returns an {@link Optional}. Does not throw
	 * an exception in case there is no template definition.
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default <M extends Manifest> Optional<M> tryGetTemplate() {
		return Optional.ofNullable(hasTemplate() ? (M)getTemplate() : null);
	}

	/**
	 * Returns {@code true} if and only if this manifest does not contain
	 * any complex sub-elements of its own but instead derives them from
	 * potentially defined templates.
	 * <p>
	 * This method is a remnant of when the model part of the manifest
	 * framework was still been tied to the default serialization facilities.
	 *
	 * @return
	 */
	boolean isEmpty();

	// Modification methods

	/**
	 * Changes the identifier used for this manifest to the given {@code id} which
	 * must not be {@code null}.
	 * @param id
	 */
	Manifest setId(String id);

	/**
	 * Marks a manifest to be a template. Note that templates must be hosted within
	 * a {@link ManifestLocation} that supports templating and only top-level manifests
	 * or those embedded in other manifests can be marked as templates!
	 * <p>
	 * Marking a manifest as template doesn't automatically make it available for
	 * templating. It has to be registered with its {@link ManifestRegistry} for
	 * proper retrieval!
	 *
	 * @param isTemplate
	 */
	Manifest setIsTemplate(boolean isTemplate);

	/**
	 * Changes the template (identified by its {@code id}) used for this manifest.
	 * A value of {@link null} means that this manifest should not use any templating.
	 *
	 * @param templateId
	 */
	Manifest setTemplateId(@Nullable String templateId);

	/**
	 * Returns the registry that is managing this manifest.
	 *
	 * @return
	 */
	ManifestRegistry getRegistry();

	/**
	 * Returns the physical or virtual location this manifest has been loaded from
	 * or will stored at.
	 *
	 * @return
	 */
	ManifestLocation getManifestLocation();

	/**
	 * Returns the optional version (in form of a manifest) that has been assigned to
	 * this manifest.
	 * <p>
	 * If no version manifest has been set, this method returns an empty {@link Optional}
	 *
	 * @return
	 */
	Optional<VersionManifest> getVersionManifest();

	/**
	 * Sets the version of this manifest. The general contract is that once
	 * the version of a manifest is set, it cannot be changed again. This means that
	 * all but the first invocation of this method on a given manifest instance will
	 * fail.
	 *
	 * @param versionManifest
	 */
	@Guarded(restricted=true)
	Manifest setVersionManifest(VersionManifest versionManifest);

	public static void verifyEnvironment(ManifestLocation manifestLocation, Object environment, Class<?> expected) {
		if(!manifestLocation.isTemplate() && !expected.isInstance(environment))
			throw new ManifestException(ManifestErrorCode.MANIFEST_MISSING_ENVIRONMENT,
					"Missing environment of type "+expected.getName());
	}
}
