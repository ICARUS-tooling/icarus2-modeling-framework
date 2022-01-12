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
package de.ims.icarus2.model.manifest.api;

import java.util.Objects;
import java.util.Optional;

import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.lang.ClassUtils;
import it.unimi.dsi.fastutil.Hash.Strategy;

/**
 * As the most simple variation of a manifest, a {@code ManifestFragment} only
 * has to declare a namespace wide unique {@link #getId() id}, a valid {@link #getManifestType() type}
 * and provide an implementation of the locking mechanism defined in {@link Lockable}.
 * <p>
 * Note that "namespace" usually refers to the entity a manifest is hosted in. This can either be
 * another manifest or the entirety of a {@link ManifestRegistry registry}.
 *
 * @author Markus Gärtner
 *
 */
public interface ManifestFragment extends Lockable, TypedManifest {

	/**
	 * Returns the namespace wide unique id of this manifest fragment.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<String> getId();

	/**
	 * Returns a globally unique identifier that is comprised of the locally unique {@link #getId() id} and
	 * the ids of the designated host environment if it exists. In the most simple case
	 * the globally unique id is equal to {@link #getId()}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default String getUniqueId() {
		return ManifestUtils.getUniqueId(this);
	}

	/**
	 * Equality based on {@link #getManifestType() type} and {@link #getId() id.
	 *
	 * @param m1
	 * @param m2
	 * @return
	 */
	public static boolean defaultEquals(ManifestFragment m1, ManifestFragment m2) {
		return TypedManifest.defaultEquals(m1, m2)
				&& ClassUtils.equals(m1.getId(), m2.getId());
	}

	public static final Strategy<ManifestFragment> HASH_STRATEGY = new Strategy<ManifestFragment>() {

		@Override
		public int hashCode(ManifestFragment frag) {
			return Objects.hash(frag.getId());
		}

		@Override
		public boolean equals(ManifestFragment frag0, ManifestFragment frag1) {
			return Objects.equals(frag0.getId(), frag1.getId());
		}
	};
}
