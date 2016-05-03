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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/manifest/AnnotationLayerManifestImpl.java $
 *
 * $LastChangedDate: 2016-01-11 17:33:05 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 445 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.manifest;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.manifest.AnnotationFlag;
import de.ims.icarus2.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus2.model.api.manifest.AnnotationManifest;
import de.ims.icarus2.model.api.manifest.LayerGroupManifest;
import de.ims.icarus2.model.api.manifest.ManifestLocation;
import de.ims.icarus2.model.api.manifest.ManifestRegistry;
import de.ims.icarus2.model.api.manifest.ManifestType;

/**
 * @author Markus Gärtner
 * @version $Id: AnnotationLayerManifestImpl.java 445 2016-01-11 16:33:05Z mcgaerty $
 *
 */
public class AnnotationLayerManifestImpl extends AbstractLayerManifest<AnnotationLayerManifest> implements AnnotationLayerManifest {

	private final Map<String, AnnotationManifest> annotationManifests = new LinkedHashMap<>();
	private String defaultKey;

	private EnumSet<de.ims.icarus2.model.api.manifest.AnnotationFlag> annotationFlags;

	/**
	 * @param manifestLocation
	 * @param registry
	 * @param layerGroupManifest
	 */
	public AnnotationLayerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestLocation, registry, layerGroupManifest);

		annotationFlags = EnumSet.noneOf(AnnotationFlag.class);
	}

	public AnnotationLayerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		this(manifestLocation, registry, null);
	}

	public AnnotationLayerManifestImpl(LayerGroupManifest layerGroupManifest) {
		this(layerGroupManifest.getContextManifest().getManifestLocation(),
				layerGroupManifest.getContextManifest().getRegistry(), layerGroupManifest);
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractLayerManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && annotationManifests.isEmpty() && annotationFlags.isEmpty();
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.ANNOTATION_LAYER_MANIFEST;
	}

	@Override
	public void forEachAnnotationManifest( Consumer<? super AnnotationManifest> action) {
		if(hasTemplate()) {
			getTemplate().forEachAnnotationManifest(action);
		}
		annotationManifests.values().forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.AnnotationLayerManifest#forEachLocalAnnotationManifest(java.util.function.Consumer)
	 */
	@Override
	public void forEachLocalAnnotationManifest(
			Consumer<? super AnnotationManifest> action) {
		annotationManifests.values().forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.AnnotationLayerManifest#getAnnotationManifest(java.lang.String)
	 */
	@Override
	public AnnotationManifest getAnnotationManifest(String key) {
		checkNotNull(key);

		AnnotationManifest manifest = annotationManifests.get(key);

		if(manifest==null && hasTemplate()) {
			manifest = getTemplate().getAnnotationManifest(key);
		}

		if(manifest==null)
			throw new IllegalArgumentException("Unknown annotation key: "+key); //$NON-NLS-1$

		return manifest;
	}

	@Override
	public void addAnnotationManifest(AnnotationManifest manifest) {
		checkNotLocked();

		addAnnotationManifest0(manifest);
	}

	protected void addAnnotationManifest0(AnnotationManifest manifest) {
		checkNotNull(manifest);

		String key = manifest.getKey();

		if(annotationManifests.containsKey(key))
			throw new IllegalArgumentException("Duplicate manifest for annotation key: "+key); //$NON-NLS-1$

		annotationManifests.put(key, manifest);
	}

	@Override
	public void removeAnnotationManifest(AnnotationManifest manifest) {
		checkNotLocked();

		removeAnnotationManifest0(manifest);
	}

	protected void removeAnnotationManifest0(AnnotationManifest manifest) {
		checkNotNull(manifest);

		String key = manifest.getKey();

		if(annotationManifests==null || annotationManifests.remove(key)==null)
			throw new IllegalArgumentException("Unknown annotation manifest: "+key); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.AnnotationLayerManifest#getDefaultKey()
	 */
	@Override
	public String getDefaultKey() {
		String result = defaultKey;
		if(result==null && hasTemplate()) {
			result = getTemplate().getDefaultKey();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.AnnotationLayerManifest#isLocalDefaultKey()
	 */
	@Override
	public boolean isLocalDefaultKey() {
		return defaultKey!=null;
	}

	/**
	 * @param defaultAnnotationManifest the defaultAnnotationManifest to set
	 */
	@Override
	public void setDefaultKey(String defaultKey) {
		checkNotLocked();

		setDefaultKey0(defaultKey);
	}

	protected void setDefaultKey0(String defaultKey) {
		checkNotNull(defaultKey);

		this.defaultKey = defaultKey;
	}

	@Override
	public boolean isAnnotationFlagSet(AnnotationFlag flag) {
		return annotationFlags.contains(flag) || (hasTemplate() && getTemplate().isAnnotationFlagSet(flag));
	}

	@Override
	public void setAnnotationFlag(AnnotationFlag flag, boolean active) {
		checkNotLocked();

		setAnnotationFlag0(flag, active);
	}

	protected void setAnnotationFlag0(AnnotationFlag flag, boolean active) {
		checkNotNull(flag);

		if(active) {
			annotationFlags.add(flag);
		} else {
			annotationFlags.remove(flag);
		}
	}

	@Override
	public void forEachActiveAnnotationFlag(Consumer<? super AnnotationFlag> action) {
		if(hasTemplate()) {
			getTemplate().forEachActiveAnnotationFlag(action);
		}

		annotationFlags.forEach(action);
	}

	@Override
	public void forEachActiveLocalAnnotationFlag(Consumer<? super AnnotationFlag> action) {
		annotationFlags.forEach(action);
	}

	@Override
	public void lock() {
		super.lock();

		lockNested(annotationManifests.values());
	}
}
