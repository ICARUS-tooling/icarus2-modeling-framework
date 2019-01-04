/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.Optional;

import javax.annotation.Nullable;
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

	private final Optional<String> id;
	private Optional<String> name = Optional.empty();
	private Optional<String> namespace = Optional.empty();
	private Optional<String> description = Optional.empty();
	private Optional<Icon> icon = Optional.empty();

	private Optional<String> layerId = Optional.empty();
	private Optional<LayerManifest<?>> sharedManifest = Optional.empty();

	private final ManifestRegistry registry;

	public LazyLayerType(String id) {
		this.registry = null;
		this.id = Optional.of(id);
	}

	public LazyLayerType(ManifestRegistry registry, Category category, String layerId) {
		requireNonNull(registry);
		requireNonNull(category);
		requireNonNull(layerId);

		if(!category.getId().isPresent())
			throw new IllegalArgumentException("Missing 'id' calue from identity"); //$NON-NLS-1$

		this.registry = registry;

		id = category.getId();
		namespace = category.getNamespace();
		name = category.getName();
		description = category.getDescription();
		icon = category.getIcon();

		this.layerId = Optional.of(layerId);
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getId()
	 */
	@Override
	public Optional<String> getId() {
		return id;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Category#getNamespace()
	 */
	@Override
	public Optional<String> getNamespace() {
		return namespace;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getName()
	 */
	@Override
	public Optional<String> getName() {
		return name;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getDescription()
	 */
	@Override
	public Optional<String> getDescription() {
		return description;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getIcon()
	 */
	@Override
	public Optional<Icon> getIcon() {
		return icon;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LayerType#getSharedManifest()
	 */
	@Override
	public Optional<LayerManifest<?>> getSharedManifest() {
		if(!sharedManifest.isPresent() && layerId.isPresent()) {
			sharedManifest = registry.getTemplate(layerId.get());
		}

		return sharedManifest;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = Optional.of(name);
	}

	/**
	 * @param namespace the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = Optional.of(namespace);
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(@Nullable String description) {
		this.description = Optional.ofNullable(description);
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(@Nullable Icon icon) {
		this.icon = Optional.ofNullable(icon);
	}

	/**
	 * @param layerId the layerId to set
	 */
	public void setLayerId(String layerId) {
		this.layerId = Optional.of(layerId);
	}

}
