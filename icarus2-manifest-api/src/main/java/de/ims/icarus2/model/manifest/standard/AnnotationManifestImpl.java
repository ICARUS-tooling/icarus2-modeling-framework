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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
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
public class AnnotationManifestImpl extends AbstractMemberManifest<AnnotationManifest> implements AnnotationManifest {

	private final AnnotationLayerManifest layerManifest;

	private String key;
	private final List<String> aliases = new ArrayList<>(3);
	private ValueType valueType;
	private ValueSet values;
	private ValueRange valueRange;
	private ContentType contentType;
	private Object noEntryValue = null;
	private Boolean allowUnknownValues;

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	public AnnotationManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, AnnotationLayerManifest layerManifest) {
		super(manifestLocation, registry);

		verifyEnvironment(manifestLocation, layerManifest, AnnotationLayerManifest.class);

		this.layerManifest = layerManifest;
	}

	public AnnotationManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		super(manifestLocation, registry);

		this.layerManifest = null;
	}

	public AnnotationManifestImpl(AnnotationLayerManifest layerManifest) {
		this(layerManifest.getManifestLocation(), layerManifest.getRegistry(), layerManifest);
	}

	@Override
	public AnnotationLayerManifest getLayerManifest() {
		return layerManifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && aliases.isEmpty() && values==null && valueRange==null && noEntryValue==null;
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
		int hash = 1;
		if(getId()!=null) {
			hash *= getId().hashCode();
		}
		if(key!=null) {
			hash *= key.hashCode();
		}

		return hash;
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
	public String getKey() {
		String result = key;
		if(result==null && hasTemplate()) {
			result = getTemplate().getKey();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalKey()
	 */
	@Override
	public boolean isLocalKey() {
		return key!=null;
	}

	/**
	 * @param key the key to set
	 */
	@Override
	public void setKey(String key) {
		checkNotLocked();

		setKey0(key);
	}

	private void setKey0(String key) {
		requireNonNull(key);

		this.key = key;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.ANNOTATION_MANIFEST;
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
	public void addAlias(String alias) {
		checkNotLocked();

		addAlias0(alias);
	}

	private void addAlias0(String alias) {
		requireNonNull(alias);

		if(aliases.contains(alias))
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT, "Alias already registered: "+alias); //$NON-NLS-1$

		aliases.add(alias);
	}

	@Override
	public void removeAlias(String alias) {
		checkNotLocked();

		removeAlias0(alias);
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
	public void setAllowUnknownValues(boolean allowUnknownValues) {
		checkNotLocked();

		setAllowUnknownValues0(allowUnknownValues);
	}

	private void setAllowUnknownValues0(boolean allowUnknownValues) {
		this.allowUnknownValues = (allowUnknownValues == DEFAULT_ALLOW_UNKNOWN_VALUES && !hasTemplate()) ? null : Boolean.valueOf(allowUnknownValues);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#getValueRange()
	 */
	@Override
	public ValueRange getValueRange() {
		ValueRange result = valueRange;
		if(result==null && hasTemplate()) {
			result = getTemplate().getValueRange();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalValueRange()
	 */
	@Override
	public boolean isLocalValueRange() {
		return valueRange!=null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#getValueSet()
	 */
	@Override
	public ValueSet getValueSet() {
		ValueSet result = values;
		if(result==null && hasTemplate()) {
			result = getTemplate().getValueSet();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalValueSet()
	 */
	@Override
	public boolean isLocalValueSet() {
		return values!=null;
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
	public ContentType getContentType() {
		ContentType result = contentType;
		if(result==null && hasTemplate()) {
			result = getTemplate().getContentType();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalContentType()
	 */
	@Override
	public boolean isLocalContentType() {
		return contentType!=null;
	}

	@Override
	public Object getNoEntryValue() {
		Object result = noEntryValue;
		if(result==null && hasTemplate()) {
			result = getTemplate().getNoEntryValue();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#isLocalNoEntryValue()
	 */
	@Override
	public boolean isLocalNoEntryValue() {
		return noEntryValue!=null;
	}

	@Override
	public void setNoEntryValue(Object noEntryValue) {
		checkNotLocked();

		setNoEntryValue0(noEntryValue);
	}

	private void setNoEntryValue0(Object noEntryValue) {
		this.noEntryValue = noEntryValue;
	}

	/**
	 * @param contentType the contentType to set
	 */
	@Override
	public void setContentType(ContentType contentType) {
		checkNotLocked();

		setContentType0(contentType);
	}

	private void setContentType0(ContentType contentType) {
		this.contentType = contentType;
	}

	/**
	 * @param valueType the valueType to set
	 */
	@Override
	public void setValueType(ValueType valueType) {
		checkNotLocked();

		setValueType0(valueType);
	}

	private void setValueType0(ValueType valueType) {
		requireNonNull(valueType);

		this.valueType = valueType;
	}

	@Override
	public void setValueSet(ValueSet values) {
		checkNotLocked();

		setValueSet0(values);
	}

	private void setValueSet0(ValueSet values) {
		this.values = values;
	}

	/**
	 * @param valueRange the valueRange to set
	 */
	@Override
	public void setValueRange(ValueRange valueRange) {
		checkNotLocked();

		setValueRange0(valueRange);
	}

	private void setValueRange0(ValueRange valueRange) {
		this.valueRange = valueRange;
	}
}
