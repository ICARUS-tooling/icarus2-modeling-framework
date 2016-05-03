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

 * $Revision: 445 $
 * $Date: 2016-01-11 17:33:05 +0100 (Mo, 11 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/manifest/OptionsManifestImpl.java $
 *
 * $LastChangedDate: 2016-01-11 17:33:05 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 445 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.standard;

import static de.ims.icarus2.util.Conditions.checkNotNull;

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
 * @version $Id: OptionsManifestImpl.java 445 2016-01-11 16:33:05Z mcgaerty $
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
		checkNotNull(option);

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
		checkNotNull(option);

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
		checkNotNull(identity);

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
		checkNotNull(identity);

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
			checkNotNull(valueType);

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
