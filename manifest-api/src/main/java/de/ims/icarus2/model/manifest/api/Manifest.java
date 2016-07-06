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
 *
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

	default boolean isValidTemplate() {
		return isTemplate() && getRegistry().hasTemplate(getId());
	}

	default void checkNotLive() {
		if(!getManifestLocation().isTemplate())
			throw new ManifestException(ManifestErrorCode.MANIFEST_ILLEGAL_TEMPLATE_STATE,
					"Manifest is not a template: "+getId());
	}

	default void checkNotTemplate() {
		if(isTemplate())
			throw new ManifestException(ManifestErrorCode.MANIFEST_ILLEGAL_TEMPLATE_STATE,
					"Manifest is a template: "+getId());
	}

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

	ManifestRegistry getRegistry();

	ManifestLocation getManifestLocation();

	VersionManifest getVersionManifest();

	/**
	 * Sets the version of this manifest. The general contract is that once
	 * the version of a manifest is set, it cannot be changed again. This emans that
	 * all but the first invocation of this method on a given manifest instance will
	 * fail.
	 *
	 * @param versionManifest
	 */
	void setVersionManifest(VersionManifest versionManifest);
}
