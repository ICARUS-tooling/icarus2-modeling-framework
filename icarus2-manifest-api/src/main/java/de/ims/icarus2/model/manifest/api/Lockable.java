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


/**
 * A one-way lock mechanism.
 * Objects implementing this interface can be locked or be locked from the point of their
 * creation, but can never be unlocked after they have been locked.
 *
 * @author Markus Gärtner
 *
 */
public interface Lockable {

	/**
	 * Irreversibly locks this object. This includes all directly hosted sub-parts
	 * that are also lockable. So if for example a {@link ContextManifest} gets locked
	 * it must also lock all {@link LayerGroupManifest group manifests} that are hosted
	 * directly, i.e. those not derived via templates.
	 * <p>
	 * This method does nothing if the object has already been locked.
	 */
	void lock();

	/**
	 * Returns {@code true} iff this object has previously been {@link #lock() locked}.
	 * @return
	 */
	boolean isLocked();

	/**
	 * If this object is {@link #lock() locked} throws a {@link ManifestException}
	 * with {@link ManifestErrorCode#MANIFEST_LOCKED}.
	 */
	default void checkNotLocked() {
		if(isLocked())
			throw new ManifestException(ManifestErrorCode.MANIFEST_LOCKED,
					"Manifest is locked: "+this);
	}

}
