/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/manifest/OptionsManifest.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.api;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.id.UnknownIdentifierException;

/**
 * Helper manifest (not describing a corpus member/entity of its own)
 * to specify possible properties the user can set on another manifest.
 * <p>
 * Note that option manifests do not have upwards links to their surrounding
 * manifests and therefore can be shared across many hosts.
 *
 * @author Markus Gärtner
 * @version $Id: OptionsManifest.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface OptionsManifest extends Manifest, Embedded {

	MemberManifest getMemberManifest();

	@Override
	default public ManifestFragment getHost() {
		return getMemberManifest();
	};

	/**
	 * Returns the names of all available options for the target
	 * manifest. This {@code Set} is guaranteed to be non-null and
	 * non-empty.
	 *
	 * @return The names of all available options as a {@code Set}
	 */
	@AccessRestriction(AccessMode.READ)
	default Set<String> getOptionIds() {
		LazyCollection<String> result = LazyCollection.lazySet();

		forEachOption(m -> result.add(m.getId()));

		return result.getAsSet();
	}

	boolean isLocalGroupIdentifier(Identity id);

	@AccessRestriction(AccessMode.READ)
	void forEachGroupIdentifier(Consumer<? super Identity> action);

	@AccessRestriction(AccessMode.READ)
	default void forEachLocalGroupIdentifier(Consumer<? super Identity> action) {
		forEachGroupIdentifier(i -> {
			if(isLocalGroupIdentifier(i)) {
				action.accept(i);
			}
		});
	}

	@AccessRestriction(AccessMode.READ)
	default boolean hasLocalGroupIdentifiers() {
		MutableInteger counter = new MutableInteger();

		forEachLocalGroupIdentifier(i -> counter.increment());

		return counter.intValue()>0;
	}

	/**
	 * Returns a collection of dedicated identifiers for groups in this options manifest.
	 * Note that a group used as result of {@link #getOptionGroup(String)} is not required
	 * to have a matching identity implementation in the returned set of this method. The
	 * returned identifiers are merely an additional chunk of localization and/or visualization
	 * hints for user interfaces.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default Set<Identity> getGroupIdentifiers() {
		LazyCollection<Identity> result = LazyCollection.lazyLinkedSet();

		forEachGroupIdentifier(result);

		return result.getAsSet();
	}

	@AccessRestriction(AccessMode.READ)
	default Set<Identity> getLocalGroupIdentifiers() {
		LazyCollection<Identity> result = LazyCollection.lazyLinkedSet();

		forEachLocalGroupIdentifier(result);

		return result.getAsSet();
	}

	/**
	 *
	 * @param id
	 * @return
	 *
	 * @throws UnknownIdentifierException if no option can be found for the given id
	 */
	@AccessRestriction(AccessMode.READ)
	Option getOption(String id);

	@AccessRestriction(AccessMode.READ)
	void forEachOption(Consumer<? super Option> action);

	@AccessRestriction(AccessMode.READ)
	default void forEachLocalOption(Consumer<? super Option> action) {
		forEachOption(o -> {
			if(isLocalOption(o.getId())) {
				action.accept(o);
			}
		});
	}

	@AccessRestriction(AccessMode.READ)
	default List<Option> getOptions() {
		LazyCollection<Option> result = LazyCollection.lazyList();

		forEachOption(result);

		return result.getAsList();
	}

	@AccessRestriction(AccessMode.READ)
	default List<Option> getLocalOptions() {
		LazyCollection<Option> result = LazyCollection.lazyList();

		forEachLocalOption(result);

		return result.getAsList();
	}

	@AccessRestriction(AccessMode.READ)
	boolean hasOption(String id);

	@AccessRestriction(AccessMode.READ)
	boolean isLocalOption(String id);

	@AccessRestriction(AccessMode.READ)
	default boolean hasLocalOptions() {
		MutableInteger counter = new MutableInteger();

		forEachLocalOption(o -> {counter.increment();});

		return counter.intValue()>0;
	}

	// Modification methods

	void addOption(Option option);

	void removeOption(Option option);

	void addGroupIdentifier(Identity identity);

	void removeGroupIdentifier(Identity identity);

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id: OptionsManifest.java 457 2016-04-20 13:08:11Z mcgaerty $
	 *
	 */
	public interface Option extends ModifiableIdentity, Lockable, TypedManifest {

		public static final boolean DEFAULT_PUBLISHED_VALUE = true;
		public static final boolean DEFAULT_MULTIVALUE_VALUE = false;
		public static final boolean DEFAULT_ALLOW_NULL = false;

		/**
		 * @see de.ims.icarus2.model.manifest.api.TypedManifest#getManifestType()
		 */
		@Override
		default public ManifestType getManifestType() {
			return ManifestType.OPTION;
		}

		@Override
		@AccessRestriction(AccessMode.READ)
		String getId();

		/**
		 * Returns the default value for the property specified by the
		 * {@code name} argument
		 *
		 * @param name The {@code name} of the property for which the
		 * default value should be returned
		 * @return The default value for the specified property or {@code null}
		 * if the property has no default value assigned to it
		 * @throws NullPointerException if the {@code name} argument
		 * is {@code null}
		 */
		@AccessRestriction(AccessMode.READ)
		Object getDefaultValue();

		/**
		 * Returns the type of the specified property. This method never
		 * returns {@code null}.
		 *
		 * @param name The {@code name} of the property for which the
		 * type should be returned
		 * @return The type for the specified property
		 * @throws NullPointerException if the {@code name} argument
		 * is {@code null}
		 */
		@AccessRestriction(AccessMode.READ)
		ValueType getValueType();

		/**
		 * Returns a localized name string of the specified property, that
		 * is suitable for presentation in user interfaces.
		 *
		 * @param name The {@code name} of the property for which a
		 * localized name should be returned
		 * @return A localized name string for the specified property
		 * @throws NullPointerException if the {@code name} argument
		 * is {@code null}
		 */
		@Override
		@AccessRestriction(AccessMode.READ)
		String getName();

		/**
		 * Returns a localized description string of the specified property, that
		 * is suitable for presentation in user interfaces.
		 * <p>
		 * This is an optional method
		 *
		 * @param name The {@code name} of the property for which a
		 * localized description should be returned
		 * @return A localized description string for the specified property
		 * or {@code null} if there is no description available for it
		 * @throws NullPointerException if the {@code name} argument
		 * is {@code null}
		 */
		@Override
		@AccessRestriction(AccessMode.READ)
		String getDescription();

		/**
		 *
		 * @param name
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		ValueSet getSupportedValues();

		/**
		 *
		 * @param name
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		ValueRange getSupportedRange();

		/**
		 * If the type of this option is {@link ValueType#EXTENSION} then this method
		 * returns the {@link ExtensionPoint} which defines the collection of valid
		 * extensions to be used as values. Note that this method only returns the
		 * globally unique id of the extension-point in question. It is up to the client
		 * code to actually delegate the lookup to the current plugin registry!
		 *
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		String getExtensionPointUid();

		/**
		 * To support graphical visualizations in their job of presenting configuration
		 * options, those options can be grouped together in logical collections. For reasons
		 * of simplicity there are no dedicated data structures to represent those groups, but
		 * the group's identifier is simply attached to an option as an id property. If further
		 * localization or additional complexity is required, the {@link #getGroupIdentifiers()}
		 * method can be used to obtain groups for this options manifest in the form of
		 * {@link Identity} implementations.
		 * <p>
		 * Note that is legal to assign groups to an option that have no dedicated identifier
		 * registered.
		 *
		 * @param name
		 * @return
		 * @see #getOptionIds()
		 */
		@AccessRestriction(AccessMode.READ)
		String getOptionGroupId();

		/**
		 * Returns whether or not the option in question should be published
		 * to the user so he can modify it. Unpublished or <i>hidden</i> options
		 * are meant as a way of configuring implementations without allowing
		 * interference from the user.
		 *
		 * @param name
		 * @return
		 * @throws NullPointerException if the {@code name} argument
		 * is {@code null}
		 */
		@AccessRestriction(AccessMode.READ)
		boolean isPublished();

		/**
		 * Returns whether an option is allowed to be assigned multiple values.
		 * This can be the case when the option in question presents the user a
		 * selective choice with several values.
		 *
		 * @param name
		 * @return
		 * @throws NullPointerException if the {@code name} argument
		 * is {@code null}
		 */
		@AccessRestriction(AccessMode.READ)
		boolean isMultiValue();

		/**
		 * Returns whether an option value is allowed to be null.
		 *
		 * @param name
		 * @return
		 * @throws NullPointerException if the {@code name} argument
		 * is {@code null}
		 */
		@AccessRestriction(AccessMode.READ)
		boolean isAllowNull();

		public Option setAllowNull(boolean allowNull);

		public Option setMultiValue(boolean multivalue);

		public Option setPublished(boolean published);

		public Option setExtensionPointUid(String extensionPointUid);

		public Option setSupportedRange(ValueRange range);

		public Option setSupportedValues(ValueSet values);

		public Option setOptionGroup(String group);

		public Option setValueType(ValueType valueType);

		public Option setDefaultValue(Object defaultValue);
	}
}
