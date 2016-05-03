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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/manifest/StructureManifestImpl.java $
 *
 * $LastChangedDate: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 443 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.manifest;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;

import java.util.EnumSet;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.manifest.ManifestLocation;
import de.ims.icarus2.model.api.manifest.ManifestRegistry;
import de.ims.icarus2.model.api.manifest.ManifestType;
import de.ims.icarus2.model.api.manifest.StructureFlag;
import de.ims.icarus2.model.api.manifest.StructureLayerManifest;
import de.ims.icarus2.model.api.manifest.StructureManifest;
import de.ims.icarus2.model.api.manifest.StructureType;

/**
 * @author Markus Gärtner
 * @version $Id: StructureManifestImpl.java 443 2016-01-11 11:31:11Z mcgaerty $
 *
 */
public class StructureManifestImpl extends ContainerManifestImpl implements StructureManifest {

	private StructureType structureType;

	private EnumSet<StructureFlag> structureFlags;

	public StructureManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, StructureLayerManifest layerManifest) {
		super(manifestLocation, registry, layerManifest);

		structureFlags = EnumSet.noneOf(StructureFlag.class);
	}

	public StructureManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		this(manifestLocation, registry, null);
	}

	public StructureManifestImpl(StructureLayerManifest layerManifest) {
		this(layerManifest.getManifestLocation(), layerManifest.getRegistry(), layerManifest);
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() && structureFlags.isEmpty();
	}

	/**
	 * @see de.ims.icarus2.model.api.standard.manifest.AbstractManifest#getTemplate()
	 */
	@Override
	public synchronized StructureManifest getTemplate() {
		return (StructureManifest) super.getTemplate();
	}

	/**
	 * @see de.ims.icarus2.model.api.standard.manifest.ContainerManifestImpl#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.STRUCTURE_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.StructureManifest#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		StructureType result = structureType;
		if(result==null && hasTemplate()) {
			result = getTemplate().getStructureType();
		}

		if(result==null) {
			result = StructureType.SET;
		}

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.StructureManifest#isLocalStructureType()
	 */
	@Override
	public boolean isLocalStructureType() {
		return structureType!=null;
	}

	/**
	 * @see de.ims.icarus2.model.api.standard.manifest.ContainerManifestImpl#getResolvedLayerManifest()
	 */
	@Override
	public StructureLayerManifest getLayerManifest() {
		return (StructureLayerManifest) super.getLayerManifest();
	}

	@Override
	public void setStructureType(StructureType structureType) {
		checkNotLocked();

		setStructureType0(structureType);
	}

	protected void setStructureType0(StructureType structureType) {
		checkNotNull(structureType);

		this.structureType = structureType;
	}

	@Override
	public boolean isStructureFlagSet(StructureFlag flag) {
		return structureFlags.contains(flag) || (hasTemplate() && getTemplate().isStructureFlagSet(flag));
	}

	@Override
	public void setStructureFlag(StructureFlag flag, boolean active) {
		checkNotLocked();

		setStructureFlag0(flag, active);
	}

	protected void setStructureFlag0(StructureFlag flag, boolean active) {
		checkNotNull(flag);

		if(active) {
			structureFlags.add(flag);
		} else {
			structureFlags.remove(flag);
		}
	}

	@Override
	public void forEachActiveStructureFlag(
			Consumer<? super StructureFlag> action) {
		if(hasTemplate()) {
			getTemplate().forEachActiveStructureFlag(action);
		}
		structureFlags.forEach(action);
	}

	@Override
	public void forEachActiveLocalStructureFlag(
			Consumer<? super StructureFlag> action) {
		structureFlags.forEach(action);
	}
}
