/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
 *
 */
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.api.AnnotationFlag;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;

/**
 * @author Markus Gärtner
 *
 */
public class AnnotationLayerManifestImpl extends AbstractLayerManifest<AnnotationLayerManifest> implements AnnotationLayerManifest {

	private final Map<String, AnnotationManifest> annotationManifests = new LinkedHashMap<>();
	private String defaultKey;

	private EnumSet<de.ims.icarus2.model.manifest.api.AnnotationFlag> annotationFlags;

	private final List<TargetLayerManifest> referenceLayerManifests = new ArrayList<>(3);

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
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && annotationManifests.isEmpty() && annotationFlags.isEmpty();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.ANNOTATION_LAYER_MANIFEST;
	}

	@Override
	public void forEachReferenceLayerManifest(Consumer<? super TargetLayerManifest> action) {
		if(hasTemplate()) {
			getTemplate().forEachReferenceLayerManifest(action);
		}
		referenceLayerManifests.forEach(action);
	}

	@Override
	public void forEachLocalReferenceLayerManifest(Consumer<? super TargetLayerManifest> action) {
		referenceLayerManifests.forEach(action);
	}

	@Override
	public void forEachAnnotationManifest( Consumer<? super AnnotationManifest> action) {
		if(hasTemplate()) {
			getTemplate().forEachAnnotationManifest(action);
		}
		annotationManifests.values().forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#forEachLocalAnnotationManifest(java.util.function.Consumer)
	 */
	@Override
	public void forEachLocalAnnotationManifest(
			Consumer<? super AnnotationManifest> action) {
		annotationManifests.values().forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#getAnnotationManifest(java.lang.String)
	 */
	@Override
	public AnnotationManifest getAnnotationManifest(String key) {
		requireNonNull(key);

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
		requireNonNull(manifest);

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
		requireNonNull(manifest);

		String key = manifest.getKey();

		if(annotationManifests==null || annotationManifests.remove(key)==null)
			throw new IllegalArgumentException("Unknown annotation manifest: "+key); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#getDefaultKey()
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
	 * @see de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#isLocalDefaultKey()
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
		requireNonNull(defaultKey);

		this.defaultKey = defaultKey;
	}

	@Override
	public TargetLayerManifest addReferenceLayerId(String referenceLayerId) {
		checkNotLocked();

		return addReferenceLayerId0(referenceLayerId);
	}

	protected TargetLayerManifest addReferenceLayerId0(String referenceLayerId) {
		requireNonNull(referenceLayerId);

		checkAllowsTargetLayer();
		TargetLayerManifest targetLayerManifest = new TargetLayerManifestImpl(referenceLayerId);
		referenceLayerManifests.add(targetLayerManifest);
		return targetLayerManifest;
	}

	@Override
	public void removeReferenceLayerId(String referenceLayerId) {
		checkNotLocked();

		removeReferenceLayerId0(referenceLayerId);
	}

	protected void removeReferenceLayerId0(String referenceLayerId) {
		requireNonNull(referenceLayerId);

		checkAllowsTargetLayer();

		for(Iterator<TargetLayerManifest> it = referenceLayerManifests.iterator(); it.hasNext();) {
			if(referenceLayerId.equals(it.next().getLayerId())) {
				it.remove();
				return;
			}
		}

		throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID, "No reference layer manifest defined for id: "+referenceLayerId);
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
		requireNonNull(flag);

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
