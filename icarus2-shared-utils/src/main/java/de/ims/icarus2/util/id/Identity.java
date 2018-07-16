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
 * $Revision: 380 $
 *
 */
package de.ims.icarus2.util.id;

import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

import javax.swing.Icon;

import de.ims.icarus2.util.strings.StringUtil;
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

	/**
	 * Returns the raw identifier used for this identity.
	 * This value does not necessarily have to be human
	 * readable.
	 * <p>
	 * Note that at the very least every identity <b>must</b>
	 * always provide a valid id!
	 *
	 * @see StringUtil#is
	 */
	String getId();

	/**
	 * Returns the human readable identifier for this identity.
	 * <p>
	 * This method is allowed to return names based on the current
	 * {@link Locale} settings.
	 * @return
	 */
	String getName();

	/**
	 * Returns a more verbose description for this
	 * identity.
	 *
	 * @return
	 */
	String getDescription();

	/**
	 * Returns the optional graphical representation of this
	 * identity
	 * @return
	 */
	Icon getIcon();

	/**
	 * Returns the optional entity that this identity is
	 * associated with.
	 * @return
	 */
	Object getOwner();

	/**
	 * Name based comparator
	 */
	public static final Comparator<Identity> COMPARATOR = new Comparator<Identity>() {

		@Override
		public int compare(Identity i1, Identity i2) {
			String name1 = i1.getName();
			String name2 = i2.getName();
			if(name1!=null && name2!=null) {
				return name1.compareTo(name2);
			} else {
				return i1.getId().compareTo(i2.getId());
			}
		}

	};

	/**
	 * Prioritizing comparator that uses names if available
	 * and defaults to ids otherwise.
	 */
	public static final Comparator<Identity> NAME_COMPARATOR = new Comparator<Identity>() {

		@Override
		public int compare(Identity i1, Identity i2) {
			String name1 = i1.getName();
			String name2 = i2.getName();

			if(name1==name2) {
				return 0;
			} else if(name1==null) {
				return -1;
			} else if(name2==null) {
				return 1;
			} else {
				return name1.compareTo(name2);
			}
		}

	};

	/**
	 * Id based comparator
	 */
	public static final Comparator<Identity> ID_COMPARATOR = new Comparator<Identity>() {

		@Override
		public int compare(Identity i1, Identity i2) {
			String id1 = i1.getId();
			String id2 = i2.getId();

			if(id1==id2) {
				return 0;
			} else if(id1==null) {
				return -1;
			} else if(id2==null) {
				return 1;
			} else {
				return id1.compareTo(id2);
			}
		}

	};

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
}
