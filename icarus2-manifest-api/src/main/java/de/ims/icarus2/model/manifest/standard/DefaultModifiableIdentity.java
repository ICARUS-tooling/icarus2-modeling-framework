/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.swing.Icon;

import de.ims.icarus2.model.manifest.api.ModifiableIdentity;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultModifiableIdentity<I extends ModifiableIdentity>
		extends AbstractLockable implements ModifiableIdentity {

	private Optional<String> id = Optional.empty();
	private Optional<String> name = Optional.empty();
	private Optional<String> description = Optional.empty();
	private Optional<Icon> icon = Optional.empty();

	public DefaultModifiableIdentity() {
		// default constructor
	}

	public DefaultModifiableIdentity(String id, @Nullable String name,
			@Nullable String description, @Nullable Icon icon) {
		setId(id);
		setName(name);
		setDescription(description);
		setIcon(icon);
	}

	public DefaultModifiableIdentity(String id, @Nullable String description,
			@Nullable Icon icon) {
		this(id, null, description, icon);
	}

	public DefaultModifiableIdentity(String id, @Nullable String description) {
		this(id, null, description, null);
	}

	protected final I thisAsCast() {
		@SuppressWarnings("unchecked")
		I result = (I) this;
		return result;
	}

	/**
	 * @return the id
	 */
	@Override
	public Optional<String> getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	@Override
	public Optional<String> getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	@Override
	public Optional<String> getDescription() {
		return description;
	}

	/**
	 * @return the icon
	 */
	@Override
	public Optional<Icon> getIcon() {
		return icon;
	}

	/**
	 * @param id the id to set
	 */
	@Override
	public I setId(String id) {
		checkNotLocked();

		setId0(id);

		return thisAsCast();
	}

	protected void setId0(String id) {
		requireNonNull(id);

		ManifestUtils.checkId(id);

		this.id = Optional.of(id);
	}

	/**
	 * @param name the name to set
	 */
	@Override
	public I setName(String name) {
		checkNotLocked();

		setName0(name);

		return thisAsCast();
	}

	protected void setName0(String name) {
		this.name = Optional.ofNullable(name);
	}

	/**
	 * @param description the description to set
	 */
	@Override
	public I setDescription(String description) {
		checkNotLocked();

		setDescription0(description);

		return thisAsCast();
	}

	protected void setDescription0(String description) {
		this.description = Optional.ofNullable(description);
	}

	/**
	 * @param icon the icon to set
	 */
	@Override
	public I setIcon(Icon icon) {
		checkNotLocked();

		setIcon0(icon);

		return thisAsCast();
	}

	protected void setIcon0(Icon icon) {
		this.icon = Optional.ofNullable(icon);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id==null ? 0 : id.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof Identity) {
			Identity other = (Identity) obj;
			return id==null ? other.getId()==null :
				id.equals(other.getId());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ModifiableIdentity@"+(id.orElse("<unnamed>"));
	}
}
