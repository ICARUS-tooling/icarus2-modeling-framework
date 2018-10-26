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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.id.Identity;

/**
 * Helper manifest (not describing a corpus member/entity of its own)
 * to specify possible properties the user can set for another manifest.
 *
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface OptionsManifest extends Manifest, Embedded {

	default <M extends MemberManifest> Optional<M> getMemberManifest() {
		return getHost();
	}

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

		forEachOption(m -> result.add(m.getId().orElseThrow(Manifest.invalidId(
				"Optiona does not declare a valid id: "+ManifestUtils.getName(m)))));

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

		forEachLocalGroupIdentifier(i -> counter.incrementAndGet());

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
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<Option> getOption(String id);

	@AccessRestriction(AccessMode.READ)
	void forEachOption(Consumer<? super Option> action);

	@AccessRestriction(AccessMode.READ)
	default void forEachLocalOption(Consumer<? super Option> action) {
		forEachOption(o -> {
			if(isLocalOption(o.getId().orElseThrow(Manifest.invalidId(
					"Option does not provide a proper id: "+ManifestUtils.getName(o))))) {
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

		forEachLocalOption(o -> {counter.incrementAndGet();});

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
	 *
	 */
	public interface Option extends ModifiableIdentity, Lockable, TypedManifest {

		public static final boolean DEFAULT_PUBLISHED_VALUE = true;
		public static final boolean DEFAULT_MULTIVALUE_VALUE = false;
		public static final boolean DEFAULT_ALLOW_NULL = false;

		public static final Set<ValueType> SUPPORTED_VALUE_TYPES =
					Collections.unmodifiableSet(ValueType.filterWithout(
				ValueType.UNKNOWN,
				ValueType.CUSTOM,
				ValueType.IMAGE_RESOURCE,
				ValueType.URL_RESOURCE,
				ValueType.REF));
		/**
		 * @see de.ims.icarus2.model.manifest.api.TypedManifest#getManifestType()
		 */
		@Override
		default public ManifestType getManifestType() {
			return ManifestType.OPTION;
		}

		/**
		 * Returns the default value for the property specified for this option
		 *
		 * @return The default value for this property or {@code null}
		 * if the property has no default value assigned to it
		 */
		@AccessRestriction(AccessMode.READ)
		Optional<Object> getDefaultValue();

		/**
		 * Returns the type of this property. This method never
		 * returns {@code null}.
		 *
		 * @return The type for this property
		 */
		@AccessRestriction(AccessMode.READ)
		ValueType getValueType();

		/**
		 * Returns a localized name string of this property, that
		 * is suitable for presentation in user interfaces.
		 *
		 * @return A localized name string for this property
		 */
		@Override
		@AccessRestriction(AccessMode.READ)
		Optional<String> getName();

		/**
		 * Returns a localized description string of this property, that
		 * is suitable for presentation in user interfaces.
		 * <p>
		 * This is an optional method
		 *
		 * @return A localized description string for this property
		 * or {@code null} if there is no description available for it
		 */
		@Override
		@AccessRestriction(AccessMode.READ)
		Optional<String> getDescription();

		/**
		 *
		 * @param name
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		Optional<ValueSet> getSupportedValues();

		/**
		 *
		 * @param name
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		Optional<ValueRange> getSupportedRange();

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
		Optional<String> getExtensionPointUid();

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
		Optional<String> getOptionGroupId();

		/**
		 * Returns whether or not the option in question should be published
		 * to the user so he can modify it. Unpublished or <i>hidden</i> options
		 * are meant as a way of configuring implementations without allowing
		 * interference from the user.
		 *
		 * @param name
		 * @return
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
		 */
		@AccessRestriction(AccessMode.READ)
		boolean isMultiValue();

		/**
		 * Returns whether an option value is allowed to be null.
		 *
		 * @param name
		 * @return
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
