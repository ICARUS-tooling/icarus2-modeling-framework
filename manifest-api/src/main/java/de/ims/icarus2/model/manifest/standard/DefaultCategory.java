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
 */
package de.ims.icarus2.model.manifest.standard;

import de.ims.icarus2.model.manifest.api.Category;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultCategory extends DefaultModifiableIdentity implements Category {

	private String namespace;

	/**
	 * @see de.ims.icarus2.model.manifest.api.Category#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		checkNotLocked();

		setNamespace0(namespace);
	}

	protected void setNamespace0(String namespace) {
		this.namespace = namespace;
	}

}
