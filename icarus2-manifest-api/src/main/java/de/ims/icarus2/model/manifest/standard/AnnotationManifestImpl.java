/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.api.ValueSet;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.data.ContentType;
import de.ims.icarus2.util.lang.ClassUtils;

/**
 * FIXME missing value type when parsing might break the implementation (maybe use lazy value parsing?)
 *
 * @author Markus Gärtner
 *
 */
public class AnnotationManifestImpl extends AbstractMemberManifest<AnnotationManifest, AnnotationLayerManifest>
		implements AnnotationManifest {

	private Optional<String> key = Optional.empty();
	private final List<String> aliases = new ArrayList<>(3);
	private ValueType valueType;
	private Optional<ValueSet> values = Optional.empty();
	private Optional<ValueRange> valueRange = Optional.empty();
	private Optional<ContentType> contentType = Optional.empty();
	private Optional<Object> noEntryValue = Optional.empty();
	private Boolean allowUnknownValues;

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	public AnnotationManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, @Nullable AnnotationLayerManifest layerManifest) {
		super(manifestLocation, registry, layerManifest, AnnotationLayerManifest.class);
	}

	public AnnotationManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		super(manifestLocation, registry);
	}

	public AnnotationManifestImpl(AnnotationLayerManifest layerManifest) {
		super(layerManifest, hostIdentity(), AnnotationLayerManifest.class);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && aliases.isEmpty()
				&& !values.isPresent() && !valueRange.isPresent()
				&& !noEntryValue.isPresent();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#toString()
	 */
	@Override
	public String toString() {
		return "AnnotationManifest@"+(key==null ? "<no-key>" : key); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getId(), getKey());
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof AnnotationManifest) {
			AnnotationManifest other = (AnnotationManifest) obj;

			return ClassUtils.equals(getId(), other.getId())
					&& ClassUtils.equals(key, other.getKey());
		}

		return false;
	}

	/**
	 * @return the key
	 */
	@Override
	public Optional<String> getKey() {
		return getDerivable(key, AnnotationManifest::getKey);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalKey()
	 */
	@Override
	public boolean isLocalKey() {
		return key.isPresent();
	}

	/**
	 * @param key the key to set
	 */
	@Override
	public AnnotationManifest setKey(String key) {
		checkNotLocked();

		setKey0(key);

		return this;
	}

	private void setKey0(String key) {
		this.key = Optional.of(key);
	}

	@Override
	public void forEachAlias(Consumer<? super String> action) {
		if(hasTemplate()) {
			getTemplate().forEachAlias(action);
		}

		aliases.forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#forEachLocalAlias(java.util.function.Consumer)
	 */
	@Override
	public void forEachLocalAlias(Consumer<? super String> action) {
		aliases.forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalAlias(java.lang.String)
	 */
	@Override
	public boolean isLocalAlias(String alias) {
		return aliases.contains(requireNonNull(alias));
	}

	@Override
	public AnnotationManifest addAlias(String alias) {
		checkNotLocked();

		addAlias0(alias);

		return this;
	}

	private void addAlias0(String alias) {
		requireNonNull(alias);

		if(aliases.contains(alias))
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT, "Alias already registered: "+alias); //$NON-NLS-1$

		aliases.add(alias);
	}

	@Override
	public AnnotationManifest removeAlias(String alias) {
		checkNotLocked();

		removeAlias0(alias);

		return this;
	}

	private void removeAlias0(String alias) {
		requireNonNull(alias);

		if(aliases==null || !aliases.remove(alias))
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT, "Unknown alias: "+alias); //$NON-NLS-1$
	}

	/**
	 *
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#isAllowUnknownValues()
	 */
	@Override
	public boolean isAllowUnknownValues() {
		if(allowUnknownValues!=null) {
			return allowUnknownValues.booleanValue();
		} else if(hasTemplate()) {
			return getTemplate().isAllowUnknownValues();
		} else {
			return DEFAULT_ALLOW_UNKNOWN_VALUES;
		}
	}

	@Override
	public AnnotationManifest setAllowUnknownValues(boolean allowUnknownValues) {
		checkNotLocked();

		setAllowUnknownValues0(allowUnknownValues);

		return this;
	}

	private void setAllowUnknownValues0(boolean allowUnknownValues) {
		this.allowUnknownValues = (allowUnknownValues == DEFAULT_ALLOW_UNKNOWN_VALUES && !hasTemplate()) ? null : Boolean.valueOf(allowUnknownValues);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#getValueRange()
	 */
	@Override
	public Optional<ValueRange> getValueRange() {
		return getDerivable(valueRange, AnnotationManifest::getValueRange);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalValueRange()
	 */
	@Override
	public boolean isLocalValueRange() {
		return valueRange.isPresent();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#getValueSet()
	 */
	@Override
	public Optional<ValueSet> getValueSet() {
		return getDerivable(values, AnnotationManifest::getValueSet);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalValueSet()
	 */
	@Override
	public boolean isLocalValueSet() {
		return values.isPresent();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#getValueType()
	 */
	@Override
	public ValueType getValueType() {
		ValueType result = valueType;
		if(result==null) {
			result =  hasTemplate() ? getTemplate().getValueType() : ValueType.STRING;
		}

//		if(result==null)
//			throw new ModelException(ModelError.MANIFEST_MISSING_TYPE,
//					"No value type available for annotation manifest: "+getId()); //$NON-NLS-1$

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalValueType()
	 */
	@Override
	public boolean isLocalValueType() {
		return valueType!=null;
	}

	/**
	 * @return the contentType
	 */
	@Override
	public Optional<ContentType> getContentType() {
		return getDerivable(contentType, AnnotationManifest::getContentType);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalContentType()
	 */
	@Override
	public boolean isLocalContentType() {
		return contentType.isPresent();
	}

	@Override
	public Optional<Object> getNoEntryValue() {
		return getDerivable(noEntryValue, AnnotationManifest::getNoEntryValue);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalNoEntryValue()
	 */
	@Override
	public boolean isLocalNoEntryValue() {
		return noEntryValue.isPresent();
	}

	@Override
	public AnnotationManifest setNoEntryValue(@Nullable Object noEntryValue) {
		checkNotLocked();

		setNoEntryValue0(noEntryValue);

		return this;
	}

	private void setNoEntryValue0(Object noEntryValue) {
		this.noEntryValue = Optional.ofNullable(noEntryValue);
	}

	/**
	 * @param contentType the contentType to set
	 */
	@Override
	public AnnotationManifest setContentType(@Nullable ContentType contentType) {
		checkNotLocked();

		setContentType0(contentType);

		return this;
	}

	private void setContentType0(ContentType contentType) {
		this.contentType = Optional.ofNullable(contentType);
	}

	/**
	 * @param valueType the valueType to set
	 */
	@Override
	public AnnotationManifest setValueType(ValueType valueType) {
		checkNotLocked();

		setValueType0(valueType);

		return this;
	}

	private void setValueType0(ValueType valueType) {
		requireNonNull(valueType);

		this.valueType = valueType;
	}

	@Override
	public AnnotationManifest setValueSet(@Nullable ValueSet values) {
		checkNotLocked();

		setValueSet0(values);

		return this;
	}

	private void setValueSet0(ValueSet values) {
		this.values = Optional.ofNullable(values);
	}

	/**
	 * @param valueRange the valueRange to set
	 */
	@Override
	public AnnotationManifest setValueRange(@Nullable ValueRange valueRange) {
		checkNotLocked();

		setValueRange0(valueRange);

		return this;
	}

	private void setValueRange0(ValueRange valueRange) {
		this.valueRange = Optional.ofNullable(valueRange);
	}
}
