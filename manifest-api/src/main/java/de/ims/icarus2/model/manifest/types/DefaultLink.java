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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/types/DefaultLink.java $
 *
 * $LastChangedDate: 2015-05-29 11:29:49 +0200 (Fr, 29 Mai 2015) $
 * $LastChangedRevision: 398 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.types;

import java.net.URI;



/**
 * @author Markus Gärtner
 * @version $Id: DefaultLink.java 398 2015-05-29 09:29:49Z mcgaerty $
 *
 */
public class DefaultLink implements Link {

	private final URI uri;
	private final String title;
	private final String description;

	public DefaultLink(URI uri, String title, String description) {
		if (uri == null)
			throw new NullPointerException("Invalid uri"); //$NON-NLS-1$
		if (title == null)
			throw new NullPointerException("Invalid title"); //$NON-NLS-1$

		if(title.isEmpty())
			throw new IllegalArgumentException("Empty title"); //$NON-NLS-1$

		this.uri = uri;
		this.title = title;
		this.description = description;
	}

	public DefaultLink(URI uri, String title) {
		this(uri, title, null);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.types.Link#getUri()
	 */
	@Override
	public URI getUri() {
		return uri;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.types.Link#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.types.Link#getDescription()
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
		return title.hashCode() * uri.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof Link) {
			Link other = (Link) obj;

			return title.equals(other.getTitle()) && uri.equals(other.getUri());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Link@"+title; //$NON-NLS-1$
	}

}
