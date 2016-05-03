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

 * $Revision: 454 $
 * $Date: 2016-02-10 12:44:15 +0100 (Mi, 10 Feb 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/util/IconWrapper.java $
 *
 * $LastChangedDate: 2016-02-10 12:44:15 +0100 (Mi, 10 Feb 2016) $
 * $LastChangedRevision: 454 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.util;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import de.ims.icarus2.util.IconRegistry;

public class IconWrapper implements Icon, StringResource {
	private transient Icon source;
	private final String name;

	public IconWrapper(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		this.name = name;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof IconWrapper) {
			return name.equals(((IconWrapper)obj).name);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IconWrapper@"+name; //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return name;
	}

	private Icon getSource() {
		if(source==null) {
			source = IconRegistry.getGlobalRegistry().getIcon(name);
		}
		return source;
	}

	/**
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if(getSource()!=null) {
			source.paintIcon(c, g, x, y);
		}
	}

	/**
	 * @see javax.swing.Icon#getIconWidth()
	 */
	@Override
	public int getIconWidth() {
		return getSource()==null ? 0 : source.getIconWidth();
	}

	/**
	 * @see javax.swing.Icon#getIconHeight()
	 */
	@Override
	public int getIconHeight() {
		return getSource()==null ? 0 : source.getIconHeight();
	}
}