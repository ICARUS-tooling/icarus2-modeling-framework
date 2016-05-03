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

 * $Revision: 435 $
 * $Date: 2015-10-21 18:01:32 +0200 (Mi, 21 Okt 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/layers/type/LazyLayerType.java $
 *
 * $LastChangedDate: 2015-10-21 18:01:32 +0200 (Mi, 21 Okt 2015) $
 * $LastChangedRevision: 435 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.layers.type;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import javax.swing.Icon;

import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerType;
import de.ims.icarus2.model.registry.CorpusRegistry;
import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id: LazyLayerType.java 435 2015-10-21 16:01:32Z mcgaerty $
 *
 */
public class LazyLayerType implements LayerType {

	private final String id;
	private String name;
	private String description;
	private Icon icon;

	private String layerId;
	private LayerManifest sharedManifest;

	private final CorpusRegistry registry;

	public LazyLayerType(String id) {
		checkNotNull(id);

		this.registry = null;
		this.id = id;
	}

	public LazyLayerType(CorpusRegistry registry, Identity identity, String layerId) {
		checkNotNull(registry);
		checkNotNull(identity);
		checkNotNull(layerId);

		if(identity.getId()==null)
			throw new IllegalArgumentException("Missing 'id' calue from identity"); //$NON-NLS-1$

		this.registry = registry;

		id = identity.getId();
		name = identity.getName();
		description = identity.getDescription();
		icon = identity.getIcon();

		this.layerId = layerId;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return icon;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LayerType#getSharedManifest()
	 */
	@Override
	public LayerManifest getSharedManifest() {
		if(sharedManifest==null && layerId!=null) {
			sharedManifest = (LayerManifest) registry.getTemplate(layerId);
		}

		return sharedManifest;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		checkNotNull(name);

		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		checkNotNull(description);

		this.description = description;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(Icon icon) {
		checkNotNull(icon);

		this.icon = icon;
	}

	/**
	 * @param layerId the layerId to set
	 */
	public void setLayerId(String layerId) {
		checkNotNull(layerId);

		this.layerId = layerId;
	}

}
