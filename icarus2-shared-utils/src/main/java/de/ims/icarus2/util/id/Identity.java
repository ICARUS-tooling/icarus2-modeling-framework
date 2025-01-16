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
package de.ims.icarus2.util.id;

import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import de.ims.icarus2.util.lang.ClassUtils;
import it.unimi.dsi.fastutil.Hash.Strategy;

/**
 * Models a set of common attributes and aspects associated with
 * the concept of identification and identity representation.
 * <p>
 * Note that certain environments might impose limitations or
 * obligatory schemes on what is considered a legal id.
 *
 * @author Markus Gärtner
 *
 */
public interface Identity {

	public static final String NO_NAME = null;
	public static final String NO_DESCRIPTION = null;

	/**
	 * Returns the raw identifier used for this identity.
	 * This value does not necessarily have to be human
	 * readable.
	 * <p>
	 * Note that at the very least every identity <b>should</b>
	 * always provide a valid id!
	 * This was previously enforced by requiring implementations to throw
	 * an exception instead of the {@link Optional} return value.
	 */
	Optional<String> getId();

	public static String defaultCreateUnnamedId(Identity identity) {
		return "unnamed@"+identity.getClass().getName();
	}

	/**
	 * Returns the human readable identifier for this identity.
	 * <p>
	 * This method is allowed to return names based on the current
	 * {@link Locale} settings.
	 * @return
	 */
	Optional<String> getName();

	/**
	 * Returns a more verbose description for this
	 * identity.
	 *
	 * @return
	 */
	Optional<String> getDescription();

	/**
	 * Prioritizing comparator that uses names if available for bot identities
	 * and defaults to ids otherwise.
	 */
	public static final Comparator<Identity> COMPARATOR = new Comparator<Identity>() {

		@Override
		public int compare(Identity i1, Identity i2) {
			Optional<String> name1 = i1.getName();
			Optional<String> name2 = i2.getName();
			if(name1.isPresent() && name2.isPresent()) {
				return name1.get().compareTo(name2.get());
			}

			return ID_COMPARATOR.compare(i1, i2);
		}

	};

	/**
	 * Name based comparator
	 */
	public static final Comparator<Identity> NAME_COMPARATOR = new Comparator<Identity>() {

		@Override
		public int compare(Identity i1, Identity i2) {
			Optional<String> name1 = i1.getName();
			Optional<String> name2 = i2.getName();

			if(Objects.equals(name1, name2)) {
				return 0;
			} if(!name1.isPresent()) {
				return -1;
			} else if(!name2.isPresent()) {
				return 1;
			} else {
				return name1.get().compareTo(name2.get());
			}
		}

	};

	/**
	 * Id based comparator
	 */
	public static final Comparator<Identity> ID_COMPARATOR = new Comparator<Identity>() {

		@Override
		public int compare(Identity i1, Identity i2) {
			Optional<String> id1 = i1.getId();
			Optional<String> id2 = i2.getId();

			if(Objects.equals(id1, id2)) {
				return 0;
			} else if(!id1.isPresent()) {
				return -1;
			} else if(!id2.isPresent()) {
				return 1;
			} else {
				return id1.get().compareTo(id2.get());
			}
		}

	};

	/**
	 * A {@link Strategy} implementation based on an identity's {@link #getId() id}
	 */
	public static final Strategy<Identity> HASH_STRATEGY = new Strategy<Identity>() {

		@Override
		public int hashCode(Identity id) {
			return Objects.hash(id.getId());
		}

		@Override
		public boolean equals(Identity id0, Identity id1) {
			return Objects.equals(id0.getId(), id1.getId());
		}
	};

	public static boolean defaultEquals(Identity id1, Identity id2) {
		return ClassUtils.equals(id1.getId(), id2.getId())
				&& ClassUtils.equals(id1.getName(), id2.getName())
				&& ClassUtils.equals(id1.getDescription(), id2.getDescription());
	}
}
