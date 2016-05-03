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

 * $Revision: 398 $
 * $Date: 2015-05-29 11:29:49 +0200 (Fr, 29 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/types/DefaultIconLink.java $
 *
 * $LastChangedDate: 2015-05-29 11:29:49 +0200 (Fr, 29 Mai 2015) $
 * $LastChangedRevision: 398 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.types;

import javax.swing.Icon;

/**
 * @author Markus Gärtner
 * @version $Id: DefaultIconLink.java 398 2015-05-29 09:29:49Z mcgaerty $
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
	 * @see de.ims.icarus2.model.types.IconLink#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return icon;
	}

	/**
	 * @see de.ims.icarus2.model.types.IconLink#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * @see de.ims.icarus2.model.types.IconLink#getDescription()
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
