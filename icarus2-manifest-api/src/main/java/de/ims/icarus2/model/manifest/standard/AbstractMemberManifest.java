/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.Category;
import de.ims.icarus2.model.manifest.api.Documentable;
import de.ims.icarus2.model.manifest.api.Documentation;
import de.ims.icarus2.model.manifest.api.Embedded;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.OptionsManifest;
import de.ims.icarus2.model.manifest.api.OptionsManifest.Option;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.lang.ClassUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;

/**
 *
 * This class is not thread-safe!
 *
 * @author Markus Gärtner
 *
 */
//FIXME currently implementing Embedded on this level is a simplification
public abstract class AbstractMemberManifest<M extends MemberManifest<M>, H extends TypedManifest>
		extends AbstractManifest<M> implements MemberManifest<M>, Embedded {

	private Optional<String> name = Optional.empty();
	private Optional<String> description = Optional.empty();
	private final Optional<H> host;

	private final Set<Category> categories = new ObjectOpenCustomHashSet<>(Category.HASH_STRATEGY);

	private final Map<String, Property> properties = new HashMap<>();
	private Optional<OptionsManifest> optionsManifest = Optional.empty();
	private Optional<Documentation> documentation = Optional.empty();

	/**
	 * Fetch the {@link Manifest} to be used instead of the {@code host} environment
	 * and throw exception if not present.
	 *
	 * @param host
	 * @param properSource
	 * @return
	 */
	private static <H extends TypedManifest> Manifest properSource(H host,
			Function<H, ? extends Manifest> properSource) {
		requireNonNull(host);
		requireNonNull(properSource);

		Manifest source = properSource.apply(host);
		if(source==null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_INVALID_ENVIRONMENT,
					"Failed to obtain proper source for manifest location and registry from host: "+ManifestUtils.getName(host));

		return source;
	}

	private static <H extends TypedManifest> ManifestRegistry registryFromSource(H host,
			Function<H, ? extends Manifest> properRegistrySource) {
		return properSource(host, properRegistrySource).getRegistry();
	}

	private static <H extends TypedManifest> ManifestLocation locationFromSource(H host,
			Function<H, ? extends Manifest> properRegistrySource) {
		return properSource(host, properRegistrySource).getManifestLocation();
	}

	protected static final <H extends Manifest> Function<H, Manifest> hostIdentity() {
		return host -> host;
	}

	/**
	 * Constructor for stand-alone manifests without a host environment.
	 *
	 * @param manifestLocation obligatory {@link ManifestLocation} for this manifest
	 * @param registry obligatory {@link ManifestRegistry} for this manifest
	 */
	protected AbstractMemberManifest(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		super(manifestLocation, registry);

		this.host = Optional.empty();
	}

	/**
	 * Constructor for situations where the host environment is optional.
	 *
	 * @param manifestLocation obligatory {@link ManifestLocation} for this manifest
	 * @param registry obligatory {@link ManifestRegistry} for this manifest
	 * @param host optional host environment
	 */
	protected AbstractMemberManifest(ManifestLocation manifestLocation,
			ManifestRegistry registry, H host,
			Class<? super H> expectedHostClass) {
		super(manifestLocation, registry);

		Manifest.verifyEnvironment(manifestLocation, host, expectedHostClass);

		this.host = Optional.ofNullable(host);
	}

	/**
	 * Constructor solely relying on the presence of a properly initialized
	 * host environment. Due to the fact that not every {@link TypedManifest}
	 * necessarily provides direct access to a valid {@link ManifestRegistry}
	 * and {@link ManifestLocation}, this constructor additionally expects
	 * a function that retrieves the closest {@link Manifest} in the existing
	 * hierarchy to obtain instances of those two.
	 *
	 * @param host  the obligatory host environment
	 * @param properRegistrySource the means of obtaining the closest proper
	 * {@link Manifest} instance in the existing hierarchy
	 */
	protected AbstractMemberManifest(H host, Function<H, Manifest> properRegistrySource,
			Class<? super H> expectedHostClass) {
		super(locationFromSource(host, properRegistrySource),
				registryFromSource(host, properRegistrySource));

		Manifest.verifyEnvironment(locationFromSource(host, properRegistrySource),
				host, expectedHostClass);

		this.host = Optional.of(host);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Embedded#getHost()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends TypedManifest> Optional<T> getHost() {
		return (Optional<T>) host;
	}

	@Override
	protected boolean isTopLevel() {
		return !getHost().isPresent();
	}

	@Override
	public boolean isEmpty() {
		return properties.isEmpty() && !optionsManifest.isPresent() && !documentation.isPresent();
	}

	/**
	 * @return the documentation
	 */
	@Override
	public Optional<Documentation> getDocumentation() {
		return getDerivable(documentation, Documentable::getDocumentation);
	}

	/**
	 * @param documentation the documentation to set
	 */
	@Override
	public M setDocumentation(Documentation documentation) {
		checkNotLocked();

		setDocumentation0(documentation);

		return thisAsCast();
	}

	protected void setDocumentation0(Documentation documentation) {
		this.documentation = Optional.of(documentation);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ModifiableManifest#getOptionsManifest()
	 */
	@Override
	public Optional<OptionsManifest> getOptionsManifest() {
		return getDerivable(optionsManifest, MemberManifest::getOptionsManifest);
	}

	/**
	 * @param optionsManifest the optionsManifest to set
	 */
	@Override
	public M setOptionsManifest(@Nullable OptionsManifest optionsManifest) {
		checkNotLocked();

		setOptionsManifest0(optionsManifest);

		return thisAsCast();
	}

	protected void setOptionsManifest0(@Nullable OptionsManifest optionsManifest) {
		// Former contract contained a null check

		this.optionsManifest = Optional.ofNullable(optionsManifest);
	}

	@Override
	public void forEachProperty(Consumer<? super Property> action) {
		requireNonNull(action);

		// Derived properties first
		if(hasTemplate()) {
			getTemplate().forEachProperty(action);
		}

		// Now local properties
		forEachLocalProperty(action);
	}

	@Override
	public void forEachLocalProperty(Consumer<? super Property> action) {
		requireNonNull(action);

		properties.values().forEach(action);
	}

	@Override
	public boolean isLocalProperty(String name) {
		requireNonNull(name);

		return properties.containsKey(name);
	}

	public boolean isInheritedProperty(String name) {
		requireNonNull(name);

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
	public Optional<Property> getProperty(String name) {
		requireNonNull(name);

		return getDerivable(Optional.ofNullable(properties.get(name)), t -> t.getProperty(name));
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

		optionsManifest.ifPresent(m -> {
			if(m.hasOption(name)) {
				Option option = m.getOption(name).get();

				if(valueType!=option.getValueType())
					throw new ManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
							Messages.mismatch("Value type not compatible with declared option manifest",
									option.getValueType(), valueType));

				property.setOption(option);
			}
		});

		property.setValueType(valueType);
		property.setMultiValue(multiValue);
		property.setValue(value);

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
	public M setPropertyValue(String name, @Nullable Object value) {
		checkNotLocked();

		setPropertyValue0(name, value);

		return thisAsCast();
	}

	protected void setPropertyValue0(String name, Object value) {

		Property property = getProperty(name)
				.orElseThrow(Manifest.unknownId("No such property: "+name));

		/* Make sure that we only ever modify local properties!
		 * Side effect of this strategy is that after modifying
		 * a property it effectively becomes a local one.
		 *
		 * TODO: introduce flag on property when it got "adopted" and upon setting it to null, remove again from local pool
		 */
		if(!isLocalProperty(name)) {
			property = property.clone();
			addProperty(property);
		}

		property.setValue(value);
	}

	@Override
	public <V extends Object> Optional<V> getPropertyValue(String name) {
		return getProperty(name).flatMap(Property::getValue);
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
	public Optional<String> getName() {
		return getDerivable(name, Identity::getName);
	}

	@Override
	public M setName(@Nullable String name) {
		checkNotLocked();

		setName0(name);

		return thisAsCast();
	}

	protected void setName0(String name) {
		this.name = Optional.ofNullable(name);
	}

	@Override
	public Optional<String> getDescription() {
		return getDerivable(description, Identity::getDescription);
	}

	@Override
	public M setDescription(@Nullable String description) {
		checkNotLocked();

		setDescription0(description);

		return thisAsCast();
	}

	protected void setDescription0(String description) {
		this.description = Optional.ofNullable(description);
	}

	@Override
	protected void lockNested() {
		super.lockNested();

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
		private Optional<Object> value = Optional.empty();
		private ValueType valueType;
		private Optional<Option> option = Optional.empty();
		private boolean multiValue;

		public PropertyImpl(String name, ValueType valueType) {
			setName(name);
			setValueType(valueType);
		}

		public PropertyImpl(Property source) {
			requireNonNull(source);

			setValueType(source.getValueType());
			this.option = source.getOption();
			setName(source.getName());
			this.value = source.getValue();
			setMultiValue(source.isMultiValue());
		}

		@Override
		public Property clone() {
			try {
				Property result = (Property) super.clone();

				if(value!=null) {
					result.setValue(value.map(ClassUtils::tryClone).orElse(null));
				}

				return result;
			} catch (CloneNotSupportedException e) {
				throw new ManifestException(GlobalErrorCode.DELEGATION_FAILED,
						"Failed to clone property "+getName(), e);
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
		@SuppressWarnings("unchecked")
		@Override
		public <V extends Object> Optional<V> getValue() {
			Optional<Object> result = value;
			if(!result.isPresent()) {
				result = option.flatMap(Option::getDefaultValue);
			}

			return (Optional<V>) result;
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

		private void checkValue(Object value) {
			if(value==null) {
				return;
			}

			ValueType valueType = this.valueType;
			if(valueType==null) {
				valueType = option.map(Option::getValueType).orElse(null);
			}

			if(valueType!=null) {
				if(value instanceof Collection) {
					for(Object val : (Collection<?>) value) {
						valueType.checkValue(val);
					}
				} else {
					valueType.checkValue(value);
				}
			}
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.MemberManifest.Property#setValue(java.lang.Object)
		 */
		@Override
		public Property setValue(@Nullable Object value) {
			checkNotLocked();
			checkValue(value);

			this.value = Optional.ofNullable(value);

			return this;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.MemberManifest.Property#getOption()
		 */
		@Override
		public Optional<Option> getOption() {
			return option;
		}

		public void setOption(@Nullable Option option) {
			checkNotLocked();
			this.option = Optional.ofNullable(option);

			checkValue(value.orElse(null));
		}

		/**
		 *
		 * @param name
		 *
		 * @see ManifestUtils#checkId(String)
		 */
		public void setName(String name) {
			checkNotLocked();
			requireNonNull(name);
			ManifestUtils.checkId(name);
			this.name = name;
		}

		public void setValueType(@Nullable ValueType valueType) {
			checkNotLocked();
			requireNonNull(valueType);
			this.valueType = valueType;

			checkValue(value.orElse(null));
		}

		@Override
		public Property setMultiValue(boolean multiValue) {
			checkNotLocked();
			this.multiValue = multiValue;

			return this;
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, value, valueType);
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
