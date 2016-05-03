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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/manifest/FragmentLayerManifestImpl.java $
 *
 * $LastChangedDate: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 443 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.manifest;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;
import de.ims.icarus2.model.api.manifest.FragmentLayerManifest;
import de.ims.icarus2.model.api.manifest.LayerGroupManifest;
import de.ims.icarus2.model.api.manifest.ManifestLocation;
import de.ims.icarus2.model.api.manifest.ManifestRegistry;
import de.ims.icarus2.model.api.manifest.ManifestType;
import de.ims.icarus2.model.api.manifest.RasterizerManifest;

/**
 * @author Markus Gärtner
 * @version $Id: FragmentLayerManifestImpl.java 443 2016-01-11 11:31:11Z mcgaerty $
 *
 */
public class FragmentLayerManifestImpl extends ItemLayerManifestImpl implements FragmentLayerManifest {

	private TargetLayerManifest valueManifest;
	private String annotationKey;
	private RasterizerManifest rasterizerManifest;

	/**
	 * @param manifestLocation
	 * @param registry
	 * @param layerGroupManifest
	 */
	public FragmentLayerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestLocation, registry, layerGroupManifest);
	}

	public FragmentLayerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		this(manifestLocation, registry, null);
	}

	public FragmentLayerManifestImpl(LayerGroupManifest layerGroupManifest) {
		this(layerGroupManifest.getContextManifest().getManifestLocation(),
				layerGroupManifest.getContextManifest().getRegistry(), layerGroupManifest);
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && rasterizerManifest==null && valueManifest==null;
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.ItemLayerManifestImpl#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.FRAGMENT_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractManifest#getTemplate()
	 */
	@Override
	public FragmentLayerManifest getTemplate() {
		return (FragmentLayerManifest) super.getTemplate();
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.FragmentLayerManifest#getValueLayerManifest()
	 */
	@Override
	public TargetLayerManifest getValueLayerManifest() {
		TargetLayerManifest result = valueManifest;

		if(result==null && hasTemplate()) {
			result = getTemplate().getValueLayerManifest();
		}

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.FragmentLayerManifest#isLocalValueLayerManifest()
	 */
	@Override
	public boolean isLocalValueLayerManifest() {
		return valueManifest!=null;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.FragmentLayerManifest#getAnnotationKey()
	 */
	@Override
	public String getAnnotationKey() {
		String result = annotationKey;
		if(result==null && hasTemplate()) {
			result = getTemplate().getAnnotationKey();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.FragmentLayerManifest#isLocalAnnotationKey()
	 */
	@Override
	public boolean isLocalAnnotationKey() {
		return annotationKey!=null;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.FragmentLayerManifest#getRasterizerManifest()
	 */
	@Override
	public RasterizerManifest getRasterizerManifest() {
		RasterizerManifest result = rasterizerManifest;
		if(result==null && hasTemplate()) {
			result = getTemplate().getRasterizerManifest();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.FragmentLayerManifest#isLocalRasterizerManifest()
	 */
	@Override
	public boolean isLocalRasterizerManifest() {
		return rasterizerManifest!=null;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.FragmentLayerManifest#setValueLayerManifest(de.ims.icarus2.model.api.manifest.LayerManifest.TargetLayerManifest)
	 */
	@Override
	public TargetLayerManifest setValueLayerId(String valueLayerId) {
		checkNotLocked();

		return setValueLayerId0(valueLayerId);
	}

	protected TargetLayerManifest setValueLayerId0(String valueLayerId) {
		checkNotNull(valueLayerId);

		checkAllowsTargetLayer();
		TargetLayerManifest manifest = this.new TargetLayerManifestImpl(valueLayerId);
		valueManifest = manifest;
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.FragmentLayerManifest#setAnnotationKey(java.lang.String)
	 */
	@Override
	public void setAnnotationKey(String key) {
		checkNotLocked();

		setAnnotationKey0(key);
	}

	protected void setAnnotationKey0(String key) {
		checkNotNull(key);

		annotationKey = key;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.FragmentLayerManifest#setRasterizerManifest(de.ims.icarus2.model.api.manifest.RasterizerManifest)
	 */
	@Override
	public void setRasterizerManifest(RasterizerManifest rasterizerManifest) {
		checkNotLocked();

		setRasterizerManifest0(rasterizerManifest);
	}

	protected void setRasterizerManifest0(RasterizerManifest rasterizerManifest) {
		checkNotNull(rasterizerManifest);

		this.rasterizerManifest = rasterizerManifest;
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractMemberManifest#lock()
	 */
	@Override
	public void lock() {
		super.lock();

		if(rasterizerManifest!=null) {
			rasterizerManifest.lock();
		}
	}

}
