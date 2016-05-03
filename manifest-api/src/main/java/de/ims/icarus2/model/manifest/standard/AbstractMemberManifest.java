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

 * $Revision: 443 $
 * $Date: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/manifest/AbstractMemberManifest.java $
 *
 * $LastChangedDate: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 443 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.standard;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.Icon;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.api.Documentation;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.OptionsManifest;
import de.ims.icarus2.model.manifest.api.OptionsManifest.Option;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.util.classes.ClassUtils;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 *
 * This class is not thread-safe!
 *
 * @author Markus Gärtner
 * @version $Id: AbstractMemberManifest.java 443 2016-01-11 11:31:11Z mcgaerty $
 *
 */
public abstract class AbstractMemberManifest<M extends MemberManifest> extends AbstractManifest<M> implements MemberManifest {

	private String name;
	private String description;
	private Icon icon;

	private final Map<String, Property> properties = new HashMap<>();
	private OptionsManifest optionsManifest;
	private Documentation documentation;

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	protected AbstractMemberManifest(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		super(manifestLocation, registry);
	}

	@Override
	protected boolean isTopLevel() {
		return getHost()==null;
	}

	@Override
	public boolean isEmpty() {
		return properties.isEmpty() && optionsManifest==null && documentation==null;
	}

	/**
	 * @return the documentation
	 */
	@Override
	public Documentation getDocumentation() {
		Documentation documentation = this.documentation;
		if(documentation==null && hasTemplate()) {
			documentation = getTemplate().getDocumentation();
		}
		return documentation;
	}

	/**
	 * @param documentation the documentation to set
	 */
	@Override
	public void setDocumentation(Documentation documentation) {
		checkNotLocked();

		setDocumentation0(documentation);
	}

	protected void setDocumentation0(Documentation documentation) {
		checkNotNull(documentation);
		if(this.documentation!=null)
			throw new ManifestException(GlobalErrorCode.ILLEGAL_STATE, "Documentation already set for manifest: "+this);

		this.documentation = documentation;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ModifiableManifest#getOptionsManifest()
	 */
	@Override
	public OptionsManifest getOptionsManifest() {
		OptionsManifest result = this.optionsManifest;
		if(result==null && hasTemplate()) {
			result = getTemplate().getOptionsManifest();
		}
		return result;
	}

	/**
	 * @param optionsManifest the optionsManifest to set
	 */
	@Override
	public void setOptionsManifest(OptionsManifest optionsManifest) {
		checkNotLocked();

		setOptionsManifest0(optionsManifest);
	}

	protected void setOptionsManifest0(OptionsManifest optionsManifest) {
		checkNotNull(optionsManifest);

		this.optionsManifest = optionsManifest;
	}

	@Override
	public void forEachProperty(Consumer<? super Property> action) {
		// Derived properties first
		if(hasTemplate()) {
			getTemplate().forEachProperty(action);
		}

		// Now local properties
		forEachLocalProperty(action);
	}

	@Override
	public void forEachLocalProperty(Consumer<? super Property> action) {
		properties.values().forEach(action);
	}

	@Override
	public boolean isLocalProperty(String name) {
		checkNotNull(name);

		return properties.containsKey(name);
	}

	public boolean isInheritedProperty(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name");

		return hasTemplate() && getTemplate().hasProperty(name);
	}

	@Override
	public boolean hasProperty(String name) {
		checkNotNull(name);

		return properties.containsKey(name) || (hasTemplate() && getTemplate().hasProperty(name));
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifest#getProperty(java.lang.String)
	 */
	@Override
	public Property getProperty(String name) {
		checkNotNull(name);

		Property property = properties.get(name);

		if(property==null && hasTemplate()) {
			property = getTemplate().getProperty(name);
		}

		if(property==null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID, "No such property: "+name);

		return property;
	}

	@Override
	public Property addProperty(String name, ValueType valueType,
			boolean multiValue, Object value) {
		checkNotLocked();

		return addProperty0(name, valueType, multiValue, value);
	}

	protected Property addProperty0(String name, ValueType valueType,
			boolean multiValue, Object value) {
		checkNotNull(name);
		checkNotNull(valueType);
		checkNotNull(value);

		if(properties.containsKey(name))
			throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID, "Duplicate property name: "+name);

		PropertyImpl property = new PropertyImpl(name, valueType);

		if(optionsManifest!=null && optionsManifest.hasOption(name)) {
			Option option = optionsManifest.getOption(name);

			if(valueType!=option.getValueType())
				throw new ManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
						Messages.mismatchMessage("Value type not compatible with declared option manifest",
								option.getValueType(), valueType));

			property.setOption(option);
		}

		property.setValueType(valueType);
		property.setValue(value);
		property.setMultiValue(multiValue);

		addProperty0(property);

		return property;
	}

	@Override
	public void addProperty(Property property) {
		checkNotLocked();

		addProperty0(property);
	}

	protected void addProperty0(Property property) {
		checkNotNull(property);

		String name = property.getName();

		if(properties.containsKey(name))
			throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID, "Duplicate property name: "+name);

		properties.put(name, property);
	}

	@Override
	public void setPropertyValue(String name, Object value) {
		checkNotLocked();

		setPropertyValue0(name, value);
	}

	protected void setPropertyValue0(String name, Object value) {

		Property property = getProperty(name);

		if(!isLocalProperty(name)) {
			property = property.clone();
			addProperty(property);
		}

		property.setValue(value);
	}

	@Override
	public Object getPropertyValue(String name) {
		Property property = getProperty(name);

		return property.getValue();
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		String result = name;
		if(result==null && hasTemplate()) {
			result = getTemplate().getName();
		}
		return result;
	}

	/**
	 * @return the description
	 */
	@Override
	public String getDescription() {
		String result = description;
		if(result==null && hasTemplate()) {
			result = getTemplate().getDescription();
		}
		return result;
	}

	/**
	 * @return the icon
	 */
	@Override
	public Icon getIcon() {
		Icon result = icon;
		if(result==null && hasTemplate()) {
			result = getTemplate().getIcon();
		}
		return result;
	}

	/**
	 * @param name the name to set
	 */
	@Override
	public void setName(String name) {
		checkNotLocked();

		setName0(name);
	}

	protected void setName0(String name) {
		checkNotNull(name);

		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	@Override
	public void setDescription(String description) {
		checkNotLocked();

		setDescription0(description);
	}

	protected void setDescription0(String description) {
		checkNotNull(description);

		this.description = description;
	}

	/**
	 * @param icon the icon to set
	 */
	@Override
	public void setIcon(Icon icon) {
		checkNotLocked();

		setIcon0(icon);
	}

	protected void setIcon0(Icon icon) {
		checkNotNull(icon);

		this.icon = icon;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}

	@Override
	public void lock() {
		super.lock();

		if(optionsManifest!=null) {
			optionsManifest.lock();
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id: AbstractMemberManifest.java 443 2016-01-11 11:31:11Z mcgaerty $
	 *
	 */
	public static class PropertyImpl extends AbstractLockable implements Property {

		private String name;
		private Object value;
		private ValueType valueType;
		private Option option;
		private boolean multiValue;

		public PropertyImpl(String name, ValueType valueType) {
			setName(name);
			setValueType(valueType);
		}

		public PropertyImpl(Property source) {
			checkNotNull(source);

			setValueType(source.getValueType());
			setOption(source.getOption());
			setName(source.getName());
			setValue(source.getValue());
			setMultiValue(source.isMultiValue());
		}

		@Override
		public Property clone() {
			try {
				Property result = (Property) super.clone();

				if(value!=null) {
					result.setValue(ClassUtils.clone(value));
				}

				return result;
			} catch (CloneNotSupportedException e) {
				throw new IllegalStateException();
			}
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.MemberManifest.Property#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return valueType;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.MemberManifest.Property#getValue()
		 */
		@Override
		public Object getValue() {
			Object result = value;
			if(result==null && option!=null) {
				result = option.getDefaultValue();
			}

			return result;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.MemberManifest.Property#getName()
		 */
		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean isMultiValue() {
			return multiValue;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.MemberManifest.Property#setValue(java.lang.Object)
		 */
		@Override
		public void setValue(Object value) {
			//FIXME use option link and value type to verify correct data
			this.value = value;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.MemberManifest.Property#getOption()
		 */
		@Override
		public Option getOption() {
			return option;
		}

		public void setOption(Option option) {
			this.option = option;
		}

		public void setName(String name) {
			checkNotNull(name);
			this.name = name;
		}

		public void setValueType(ValueType valueType) {
			this.valueType = valueType;
		}

		@Override
		public void setMultiValue(boolean multiValue) {
			this.multiValue = multiValue;
		}

		@Override
		public int hashCode() {
			int hash = name.hashCode();

			if(value!=null) {
				hash *= value.hashCode();
			}

			if(valueType!=null) {
				hash *= valueType.hashCode();
			}

			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} else if(obj instanceof Property) {
				Property other = (Property) obj;
				return name.equals(other.getName())
						&& ClassUtils.equals(valueType, other.getValueType())
						&& multiValue==other.isMultiValue()
						&& multiValue ? equalsMultiValues(value, other.getValue()) : ClassUtils.equals(value, other.getValue());
			}
			return false;
		}

		private static boolean equalsMultiValues(Object obj1, Object obj2) {
			Collection<?> c1 = (Collection<?>) obj1;
			Collection<?> c2 = (Collection<?>) obj2;

			return CollectionUtils.equals(c1, c2);
		}

		@Override
		public String toString() {
			return "Property@"+name;
		}
	}
}
