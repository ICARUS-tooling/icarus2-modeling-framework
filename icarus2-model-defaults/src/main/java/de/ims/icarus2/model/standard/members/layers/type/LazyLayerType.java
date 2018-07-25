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
package de.ims.icarus2.model.standard.members.layers.type;

import static java.util.Objects.requireNonNull;

import javax.swing.Icon;

import de.ims.icarus2.model.manifest.api.Category;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerType;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;

/**
 * @author Markus Gärtner
 *
 */
public class LazyLayerType implements LayerType {

	private final String id;
	private String name;
	private String namespace;
	private String description;
	private Icon icon;

	private String layerId;
	private LayerManifest sharedManifest;

	private final ManifestRegistry registry;

	public LazyLayerType(String id) {
		requireNonNull(id);

		this.registry = null;
		this.id = id;
	}

	public LazyLayerType(ManifestRegistry registry, Category category, String layerId) {
		requireNonNull(registry);
		requireNonNull(category);
		requireNonNull(layerId);

		if(category.getId()==null)
			throw new IllegalArgumentException("Missing 'id' calue from identity"); //$NON-NLS-1$

		this.registry = registry;

		id = category.getId();
		namespace = category.getNamespace();
		name = category.getName();
		description = category.getDescription();
		icon = category.getIcon();

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
	 * @see de.ims.icarus2.model.manifest.api.Category#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return namespace;
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
		requireNonNull(name);

		this.name = name;
	}

	/**
	 * @param namespace the namespace to set
	 */
	public void setNamespace(String namespace) {
		requireNonNull(namespace);

		this.namespace = namespace;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		requireNonNull(description);

		this.description = description;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(Icon icon) {
		requireNonNull(icon);

		this.icon = icon;
	}

	/**
	 * @param layerId the layerId to set
	 */
	public void setLayerId(String layerId) {
		requireNonNull(layerId);

		this.layerId = layerId;
	}

}
