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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/manifest/AnnotationManifestImpl.java $
 *
 * $LastChangedDate: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 443 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.standard;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.api.ValueSet;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.classes.ClassUtils;
import de.ims.icarus2.util.data.ContentType;

/**
 * FIXME missing value type when parsing might break the implementation (maybe use lazy value parsing?)
 *
 * @author Markus Gärtner
 * @version $Id: AnnotationManifestImpl.java 443 2016-01-11 11:31:11Z mcgaerty $
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
		checkNotNull(key);

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
		return aliases.contains(alias);
	}

	@Override
	public void addAlias(String alias) {
		checkNotLocked();

		addAlias0(alias);
	}

	protected void addAlias0(String alias) {
		checkNotNull(alias);

		if(aliases.contains(alias))
			throw new IllegalArgumentException("Alias already registered: "+alias); //$NON-NLS-1$

		aliases.add(alias);
	}

	@Override
	public void removeAlias(String alias) {
		checkNotLocked();

		removeAlias0(alias);
	}

	protected void removeAlias0(String alias) {
		checkNotNull(alias);

		if(aliases==null || !aliases.remove(alias))
			throw new IllegalArgumentException("Unknown alias: "+alias); //$NON-NLS-1$
	}

	/**
	 * This implementation returns {@code true} when at least one of
	 * {@link ValueRange} or {@link ValueSet} previously
	 * assigned to this manifest is non-null.
	 *
	 * @see de.ims.icarus2.model.manifest.api.AnnotationManifest#isValuesLimited()
	 */
	@Override
	public boolean isValuesLimited() {
		return valueRange!=null || values!=null || (hasTemplate() && getTemplate().isValuesLimited());
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
		return noEntryValue;
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
		checkNotNull(valueType);

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
