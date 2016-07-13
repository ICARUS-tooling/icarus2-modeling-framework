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
	default boolean isValidTemplate() {
		return isTemplate() && getRegistry().hasTemplate(getId());
	}

	/**
	 * Throws a {@link ManifestException} with {@link ManifestErrorCode#MANIFEST_ILLEGAL_TEMPLATE_STATE}
	 * in case the {@link #getManifestLocation() location} of this manifest is declared to be
	 * a storage for "live" manifests, i.e. its {@link ManifestLocation#isTemplate()} method returns {@code true}.
	 */
	default void checkNotLive() {
		if(!getManifestLocation().isTemplate())
			throw new ManifestException(ManifestErrorCode.MANIFEST_ILLEGAL_TEMPLATE_STATE,
					"Manifest is not a template: "+getId());
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
	 * templating or {@code null} otherwise.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Manifest getTemplate();

	/**
	 * Returns {@code true} if and only if this manifest does not contain
	 * any complex sub-elements of its own but instead derives them from
	 * potentially defined templates.
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
	void setId(String id);

	/**
	 * Marks a manifest to be a template. Note that templates must be hosted within
	 * a {@link ManifestLocation} that supports templating and only top-level manifests
	 * can be marked as templates!
	 *
	 * @param isTemplate
	 */
	void setIsTemplate(boolean isTemplate);

	/**
	 * Changes the template (identified by its {@code id}) used for this manifest.
	 * A value of {@link null} means that this manifest should not use any templating.
	 *
	 * @param templateId
	 */
	void setTemplateId(String templateId);

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
	 * If no version manifest has been set, this method returns {@code null}
	 *
	 * @return
	 */
	VersionManifest getVersionManifest();

	/**
	 * Sets the version of this manifest. The general contract is that once
	 * the version of a manifest is set, it cannot be changed again. This means that
	 * all but the first invocation of this method on a given manifest instance will
	 * fail.
	 *
	 * @param versionManifest
	 */
	void setVersionManifest(VersionManifest versionManifest);
}
