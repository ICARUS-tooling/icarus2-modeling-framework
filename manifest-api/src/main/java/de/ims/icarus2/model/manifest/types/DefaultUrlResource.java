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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/types/DefaultUrlResource.java $
 *
 * $LastChangedDate: 2015-05-29 11:29:49 +0200 (Fr, 29 Mai 2015) $
 * $LastChangedRevision: 398 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.types;



/**
 * @author Markus Gärtner
 * @version $Id: DefaultUrlResource.java 398 2015-05-29 09:29:49Z mcgaerty $
 *
 */
public class DefaultUrlResource implements UrlResource {

	private final Url url;
	private final String title;
	private final String description;

	public DefaultUrlResource(Url url, String title, String description) {
		if (url == null)
			throw new NullPointerException("Invalid url"); //$NON-NLS-1$
		if (title == null)
			throw new NullPointerException("Invalid title"); //$NON-NLS-1$

		if(title.isEmpty())
			throw new IllegalArgumentException("Empty title"); //$NON-NLS-1$

		this.url = url;
		this.title = title;
		this.description = description;
	}

	public DefaultUrlResource(Url url, String title) {
		this(url, title, null);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.types.UrlResource#getUrl()
	 */
	@Override
	public Url getUrl() {
		return url;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.types.UrlResource#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.types.UrlResource#getDescription()
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
		return title.hashCode() * url.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof UrlResource) {
			UrlResource other = (UrlResource) obj;

			return title.equals(other.getTitle()) && url.equals(other.getUrl());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UrlResource@"+title; //$NON-NLS-1$
	}

}
