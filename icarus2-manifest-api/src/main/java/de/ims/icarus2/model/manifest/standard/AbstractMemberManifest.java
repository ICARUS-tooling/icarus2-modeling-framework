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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.Icon;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.Category;
import de.ims.icarus2.model.manifest.api.Documentation;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.OptionsManifest;
import de.ims.icarus2.model.manifest.api.OptionsManifest.Option;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.lang.ClassUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;

/**
 *
 * This class is not thread-safe!
 *
 * @author Markus Gärtner
 *
 */
public abstract class AbstractMemberManifest<M extends MemberManifest> extends AbstractManifest<M> implements MemberManifest {

	private String name;
	private String description;
	private Icon icon;

	private final Set<Category> categories = new ObjectOpenCustomHashSet<>(Category.HASH_STRATEGY);

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
		requireNonNull(documentation);
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
		// Former contract contained a null check

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
		requireNonNull(name);

		return properties.containsKey(name);
	}

	public boolean isInheritedProperty(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name");

		return hasTemplate() && getTemplate().hasProperty(name);
	}

	@Override
	public boolean hasProperty(String name) {
		requireNonNull(name);

		return properties.containsKey(name) || (hasTemplate() && getTemplate().hasProperty(name));
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifest#getProperty(java.lang.String)
	 */
	@Override
	public Property getProperty(String name) {
		requireNonNull(name);

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
		requireNonNull(name);
		requireNonNull(valueType);
		requireNonNull(value);

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

		//FIXME handle multiValue flag for type check here!
		valueType.checkValue(value);

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
		requireNonNull(property);

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

		/* Make sure that we only ever modify local properties!
		 * Side effect of this strategy is that after modifying
		 * a property it effectively becomes a local one.
		 */
		if(!isLocalProperty(name)) {
			property = property.clone();
			addProperty(property);
		}

		property.setValue(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends Object> V getPropertyValue(String name) {
		if(!hasProperty(name)) {
			return null;
		}

		Property property = getProperty(name);

		return (V) property.getValue();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Categorizable#hasCategory(de.ims.icarus2.model.manifest.api.Category)
	 */
	@Override
	public boolean hasCategory(Category category) {
		return categories.contains(requireNonNull(category));
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Categorizable#addCategory(de.ims.icarus2.model.manifest.api.Category)
	 */
	@Override
	public boolean addCategory(Category category) {
		checkNotLocked();

		return addCategory0(category);
	}

	protected boolean addCategory0(Category category) {
		requireNonNull(category);

		return categories.add(category);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Categorizable#removeCategory(de.ims.icarus2.model.manifest.api.Category)
	 */
	@Override
	public boolean removeCategory(Category category) {
		checkNotLocked();

		return removeCategory0(category);
	}

	protected boolean removeCategory0(Category category) {
		requireNonNull(category);

		return categories.remove(category);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Categorizable#forEachCategory(java.util.function.Consumer)
	 */
	@Override
	public void forEachCategory(Consumer<? super Category> action) {
		categories.forEach(action);
	}

	@Override
	public String getName() {
		String name = this.name;
		if(name==null && hasTemplate()) {
			name = getTemplate().getName();
		}
		return name;
	}

	@Override
	public void setName(String name) {
		checkNotLocked();

		setName0(name);
	}

	protected void setName0(String name) {
		this.name = requireNonNull(name);
	}

	@Override
	public String getDescription() {
		String description = this.description;
		if(description==null && hasTemplate()) {
			description = getTemplate().getDescription();
		}
		return description;
	}

	@Override
	public void setDescription(String description) {
		checkNotLocked();

		setDescription0(description);
	}

	protected void setDescription0(String description) {
		this.description = requireNonNull(description);
	}

	@Override
	public Icon getIcon() {
		Icon icon = this.icon;
		if(icon==null && hasTemplate()) {
			icon = getTemplate().getIcon();
		}
		return icon;
	}

	@Override
	public void setIcon(Icon icon) {
		checkNotLocked();

		setIcon0(icon);
	}

	protected void setIcon0(Icon icon) {
		this.icon = requireNonNull(icon);
	}

	@Override
	public void lock() {
		super.lock();

		lockNested(optionsManifest, documentation);

		lockNested(properties.values());
	}

	/**
	 *
	 * @author Markus Gärtner
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
			requireNonNull(source);

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
			requireNonNull(name);
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
