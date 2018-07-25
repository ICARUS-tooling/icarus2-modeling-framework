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
package de.ims.icarus2.util.id;

import static java.util.Objects.requireNonNull;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * @author Markus Gärtner
 *
 */
public class StaticIdentity implements Identity {

	protected final Object owner;
	protected String id;
	protected String name;
	protected String description;
	protected URL iconLocation;
	protected Icon icon;

	public StaticIdentity(Identity source) {
		requireNonNull(source);

		this.owner = requireNonNull(source.getOwner());

		setId(source.getId());
		setName(source.getName());
		setDescription(source.getDescription());
		setIcon(source.getIcon());
	}

	public StaticIdentity(Object owner) {
		requireNonNull(owner);

		this.owner = owner;
	}

	public StaticIdentity(String id) {
		this.owner = null;

		setId(id);
	}

	public StaticIdentity(String id, Object owner) {
		requireNonNull(owner);

		this.owner = owner;

		setId(id);
	}

	public StaticIdentity(String id, String description, Icon icon) {
		this.owner = this;

		setId(id);
		setDescription(description);
		setIcon(icon);
	}

	public StaticIdentity(String id, String name, String description, Icon icon) {
		this.owner = this;

		setId(id);
		setName(name);
		setDescription(description);
		setIcon(icon);
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
		Icon icon = this.icon;

		if(icon==null && iconLocation!=null) {
			icon = new ImageIcon(iconLocation);
		}

		return icon;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return owner==null ? this : owner;
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
		return getId();
	}

	/**
	 * @return the iconLocation
	 */
	public URL getIconLocation() {
		return iconLocation;
	}

	public void setId(String id) {
		requireNonNull(id);

		this.id = id;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
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
		this.icon = icon;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
