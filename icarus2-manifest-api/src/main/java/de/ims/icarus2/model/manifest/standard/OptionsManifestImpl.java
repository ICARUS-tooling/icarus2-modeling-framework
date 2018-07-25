/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.OptionsManifest;
import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.api.ValueSet;
import de.ims.icarus2.model.manifest.types.UnsupportedValueTypeException;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.id.UnknownIdentifierException;

/**
 * @author Markus Gärtner
 *
 */
public class OptionsManifestImpl extends AbstractManifest<OptionsManifest> implements OptionsManifest {

	private final Set<Identity> groupIdentifiers = new HashSet<>();
	private final Map<String, Option> options = new HashMap<>();

	private final MemberManifest memberManifest;

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	public OptionsManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		super(manifestLocation, registry);

		memberManifest = null;
	}

	public OptionsManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, MemberManifest memberManifest) {
		super(manifestLocation, registry);

		verifyEnvironment(manifestLocation, memberManifest, MemberManifest.class);

		this.memberManifest = memberManifest;
	}

	public OptionsManifestImpl(MemberManifest memberManifest) {
		this(memberManifest.getManifestLocation(), memberManifest.getRegistry(), memberManifest);
	}

	@Override
	public MemberManifest getMemberManifest() {
		return memberManifest;
	}

	@Override
	protected boolean isTopLevel() {
		return getHost()==null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLockable#lock()
	 */
	@Override
	public void lock() {
		super.lock();

		options.values().forEach(Option::lock);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Manifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.OPTIONS_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return options.isEmpty() && groupIdentifiers.isEmpty();
	}

	@Override
	public void forEachOption(Consumer<? super Option> action) {
		if(hasTemplate()) {
			getTemplate().forEachOption(action);
		}
		forEachLocalOption(action);
	}

	@Override
	public boolean hasOption(String id) {
		return options.containsKey(id) || (hasTemplate() && getTemplate().hasOption(id));
	}

	@Override
	public Option getOption(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$

		Option option = options.get(id);

		if(option==null && hasTemplate()) {
			option = getTemplate().getOption(id);
		}

		if(option==null)
			throw new UnknownIdentifierException("No such option: "+id); //$NON-NLS-1$

		return option;
	}

	@Override
	public void addOption(Option option) {
		checkNotLocked();

		addOption0(option);
	}

	protected void addOption0(Option option) {
		requireNonNull(option);

		String id = option.getId();

		if(id==null)
			throw new IllegalArgumentException("Option does not declare a valid id"); //$NON-NLS-1$

		if(options.containsKey(id))
			throw new IllegalArgumentException("Duplicate option id: "+id); //$NON-NLS-1$

		options.put(id, option);
	}

	@Override
	public void removeOption(Option option) {
		checkNotLocked();

		removeOption0(option);
	}

	protected void removeOption0(Option option) {
		requireNonNull(option);

		String id = option.getId();

		if(id==null)
			throw new IllegalArgumentException("Option does not declare a valid id"); //$NON-NLS-1$

		if(!options.remove(id, option))
			throw new ManifestException(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "Provided option is not mapped to its id: "+id);
	}

	@Override
	public void forEachGroupIdentifier(Consumer<? super Identity> action) {
		if(hasTemplate()) {
			getTemplate().forEachGroupIdentifier(action);
		}
		forEachLocalGroupIdentifier(action);
	}

	@Override
	public void addGroupIdentifier(Identity identity) {
		checkNotLocked();

		addGroupIdentifier0(identity);
	}

	protected void addGroupIdentifier0(Identity identity) {
		requireNonNull(identity);

		if(identity.getId()==null)
			throw new IllegalArgumentException("Supplied identity declares null id"); //$NON-NLS-1$

		if(!ManifestUtils.isValidId(identity.getId()))
			throw new IllegalArgumentException("Supplied identity declares invalid id: "+identity.getId()); //$NON-NLS-1$

		if(groupIdentifiers.contains(identity))
			throw new IllegalArgumentException("Duplicate group identifier: "+identity); //$NON-NLS-1$

		groupIdentifiers.add(identity);
	}

	@Override
	public void removeGroupIdentifier(Identity identity) {
		checkNotLocked();

		removeGroupIdentifier0(identity);
	}

	protected void removeGroupIdentifier0(Identity identity) {
		requireNonNull(identity);

		if(!groupIdentifiers.remove(identity))
			throw new ManifestException(ManifestErrorCode.MANIFEST_ERROR, "No such group identifier: "+identity);
	}

	@Override
	public boolean isLocalGroupIdentifier(Identity id) {
		return groupIdentifiers.contains(id);
	}

	@Override
	public boolean hasLocalGroupIdentifiers() {
		return !groupIdentifiers.isEmpty();
	}

	@Override
	public void forEachLocalGroupIdentifier(Consumer<? super Identity> action) {
		groupIdentifiers.forEach(action);
	}

	@Override
	public void forEachLocalOption(Consumer<? super Option> action) {
		options.values().forEach(action);
	}

	@Override
	public boolean isLocalOption(String id) {
		return options.containsKey(id);
	}

	@Override
	public boolean hasLocalOptions() {
		return !options.isEmpty();
	}

	public static class OptionImpl extends DefaultModifiableIdentity implements Option {
		private Object defaultValue;
		private ValueType valueType;
		private String group;
		private ValueSet values;
		private ValueRange range;
		private String extensionPointUid;
		private boolean published = DEFAULT_PUBLISHED_VALUE;
		private boolean multivalue = DEFAULT_MULTIVALUE_VALUE;
		private boolean allowNull = DEFAULT_ALLOW_NULL;

		private static final Set<ValueType> supportedValueTypes = ValueType.filterWithout(
				ValueType.UNKNOWN,
				ValueType.CUSTOM,
				ValueType.IMAGE_RESOURCE,
				ValueType.URL_RESOURCE);

		public static boolean isSupportedValueType(ValueType valueType) {
			return supportedValueTypes.contains(valueType);
		}

		public OptionImpl() {
			// for parsing
		}

		public OptionImpl(String id, ValueType valueType) {
			setId0(id);
			setValueType0(valueType);
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return getId().hashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof Option) {
				return getId().equals(((Option)obj).getId());
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Option@"+getId(); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.OptionsManifest.Option#getDefaultValue()
		 */
		@Override
		public Object getDefaultValue() {
			return defaultValue;
		}
		/**
		 * @see de.ims.icarus2.model.manifest.api.OptionsManifest.Option#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return valueType;
		}
		/**
		 * @see de.ims.icarus2.model.manifest.api.OptionsManifest.Option#getSupportedValues()
		 */
		@Override
		public ValueSet getSupportedValues() {
			return values;
		}
		/**
		 * @see de.ims.icarus2.model.manifest.api.OptionsManifest.Option#getSupportedRange()
		 */
		@Override
		public ValueRange getSupportedRange() {
			return range;
		}
		/**
		 * @see de.ims.icarus2.model.manifest.api.OptionsManifest.Option#getExtensionPointUid()
		 */
		@Override
		public String getExtensionPointUid() {
			return extensionPointUid;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.OptionsManifest.Option#getOptionGroupId()
		 */
		@Override
		public String getOptionGroupId() {
			return group;
		}
		/**
		 * @see de.ims.icarus2.model.manifest.api.OptionsManifest.Option#isPublished()
		 */
		@Override
		public boolean isPublished() {
			return published;
		}
		/**
		 * @see de.ims.icarus2.model.manifest.api.OptionsManifest.Option#isMultiValue()
		 */
		@Override
		public boolean isMultiValue() {
			return multivalue;
		}

		@Override
		public boolean isAllowNull() {
			return allowNull;
		}

		/**
		 * @param defaultValue the defaultValue to set
		 */
		@Override
		public Option setDefaultValue(Object defaultValue) {
			checkNotLocked();

			return setDefaultValue0(defaultValue);
		}

		protected OptionImpl setDefaultValue0(Object defaultValue) {

			if(defaultValue!=null) {
				valueType.checkValue(defaultValue);
			}

			this.defaultValue = defaultValue;

			return this;
		}

		/**
		 * @param valueType the valueType to set
		 */
		@Override
		public Option setValueType(ValueType valueType) {
			checkNotLocked();

			return setValueType0(valueType);
		}

		protected OptionImpl setValueType0(ValueType valueType) {
			requireNonNull(valueType);

			if(!supportedValueTypes.contains(valueType))
				throw new UnsupportedValueTypeException(valueType);

			this.valueType = valueType;

			return this;
		}

		/**
		 * @param group the group to set
		 */
		@Override
		public Option setOptionGroup(String group) {
			checkNotLocked();

			return setOptionGroup0(group);
		}

		protected OptionImpl setOptionGroup0(String group) {
			if(group!=null && !ManifestUtils.isValidId(group))
				throw new IllegalArgumentException("Supplied group id is not valid: "+group); //$NON-NLS-1$

			this.group = group;

			return this;
		}

		/**
		 * @param values the values to set
		 */
		@Override
		public Option setSupportedValues(ValueSet values) {
			checkNotLocked();

			return setSupportedValues0(values);
		}

		protected OptionImpl setSupportedValues0(ValueSet values) {

			if(values!=null && !valueType.equals(values.getValueType()))
				throw new IllegalArgumentException("Incompatible value type defined for value set: expected "+valueType+" - got "+values.getValueType()); //$NON-NLS-1$ //$NON-NLS-2$

			this.values = values;

			return this;
		}

		/**
		 * @param range the range to set
		 */
		@Override
		public Option setSupportedRange(ValueRange range) {
			checkNotLocked();

			return setSupportedRange0(range);
		}

		protected OptionImpl setSupportedRange0(ValueRange range) {

			if(range!=null && !valueType.equals(range.getValueType()))
				throw new IllegalArgumentException("Incompatible value type defined for range: expected "+valueType+" - got "+range.getValueType()); //$NON-NLS-1$ //$NON-NLS-2$

			this.range = range;

			return this;
		}

		/**
		 * @param extensionPoint the extensionPointUid to set
		 */
		@Override
		public Option setExtensionPointUid(String extensionPointUid) {
			checkNotLocked();

			return setExtensionPointUid0(extensionPointUid);
		}

		protected OptionImpl setExtensionPointUid0(String extensionPointUid) {
			if(extensionPointUid!=null && !valueType.equals(ValueType.EXTENSION))
				throw new IllegalArgumentException("Cannot define extension point for value type other than " //$NON-NLS-1$
						+ValueType.EXTENSION.getStringValue()+": "+valueType.getStringValue()); //$NON-NLS-1$

			this.extensionPointUid = extensionPointUid;

			return this;
		}

		/**
		 * @param published the published to set
		 */
		@Override
		public Option setPublished(boolean published) {
			checkNotLocked();

			return setPublished0(published);
		}

		protected OptionImpl setPublished0(boolean published) {
			this.published = published;

			return this;
		}

		/**
		 * @param multivalue the multivalue to set
		 */
		@Override
		public Option setMultiValue(boolean multivalue) {
			checkNotLocked();

			return setMultiValue0(multivalue);
		}

		protected OptionImpl setMultiValue0(boolean multivalue) {
			this.multivalue = multivalue;

			return this;
		}

		/**
		 * @param allowNull the allowNull to set
		 */
		@Override
		public Option setAllowNull(boolean allowNull) {
			checkNotLocked();

			return setAllowNull0(allowNull);
		}

		protected OptionImpl setAllowNull0(boolean allowNull) {
			this.allowNull = allowNull;

			return this;
		}
	}
}
