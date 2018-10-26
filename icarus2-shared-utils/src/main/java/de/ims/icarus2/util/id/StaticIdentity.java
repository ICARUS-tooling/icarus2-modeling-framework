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
package de.ims.icarus2.util.id;

import static java.util.Objects.requireNonNull;

import java.net.URL;
import java.util.Optional;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * @author Markus Gärtner
 *
 */
public class StaticIdentity implements Identity {

	protected Optional<String> id;
	protected Optional<String> name = Optional.empty();
	protected Optional<String> description = Optional.empty();
	protected URL iconLocation;
	protected Optional<Icon> icon = Optional.empty();

	public StaticIdentity(Identity source) {
		requireNonNull(source);

		id = source.getId();
		name = source.getName();
		description = source.getDescription();
		icon = source.getIcon();
	}

	public StaticIdentity(String id) {
		setId(id);
	}

	public StaticIdentity(String id, String description, Icon icon) {
		setId(id);
		setDescription(description);
		setIcon(icon);
	}

	public StaticIdentity(String id, String name, String description, Icon icon) {
		setId(id);
		setName(name);
		setDescription(description);
		setIcon(icon);
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getId()
	 */
	@Override
	public Optional<String> getId() {
		return id;
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
		Optional<Icon> icon = this.icon;

		if(!icon.isPresent() && iconLocation!=null) {
			icon = Optional.of(new ImageIcon(iconLocation));
		}

		return icon;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof Identity) {
			Identity other = (Identity)obj;
			return getId().equals(other.getId());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getId().orElseGet(() -> Identity.defaultCreateUnnamedId(this));
	}

	/**
	 * @return the iconLocation
	 */
	public URL getIconLocation() {
		return iconLocation;
	}

	public void setId(String id) {
		this.id = Optional.of(id);
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = Optional.ofNullable(name);
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = Optional.ofNullable(description);
	}

	/**
	 * @param iconLocation the iconLocation to set
	 */
	public void setIconLocation(URL iconLocation) {
		this.iconLocation = iconLocation;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(Icon icon) {
		this.icon = Optional.ofNullable(icon);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
