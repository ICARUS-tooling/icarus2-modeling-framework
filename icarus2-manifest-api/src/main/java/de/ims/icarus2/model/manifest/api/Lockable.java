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
package de.ims.icarus2.model.manifest.api;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.util.ManifestUtils;

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
					"Manifest is locked: "+ManifestUtils.getName(this));
	}

}
