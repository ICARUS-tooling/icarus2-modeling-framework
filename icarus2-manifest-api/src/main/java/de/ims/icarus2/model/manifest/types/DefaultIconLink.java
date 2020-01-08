/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.types;

import javax.swing.Icon;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultIconLink implements IconLink {

	private final Icon icon;
	private final String title;
	private final String description;

	public DefaultIconLink(Icon icon, String title, String description) {
		if (icon == null)
			throw new NullPointerException("Invalid icon"); //$NON-NLS-1$
		if (title == null)
			throw new NullPointerException("Invalid title"); //$NON-NLS-1$

		if(title.isEmpty())
			throw new IllegalArgumentException("Empty title"); //$NON-NLS-1$

		this.icon = icon;
		this.title = title;
		this.description = description;
	}

	public DefaultIconLink(Icon icon, String title) {
		this(icon, title, null);
	}


	/**
	 * @see de.ims.icarus2.model.manifest.types.IconLink#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return icon;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.types.IconLink#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.types.IconLink#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return title.hashCode()*icon.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof IconLink) {
			IconLink other = (IconLink) obj;
			return title.equals(other.getTitle()) && icon.equals(other.getIcon());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IconLink@"+title; //$NON-NLS-1$
	}

}
