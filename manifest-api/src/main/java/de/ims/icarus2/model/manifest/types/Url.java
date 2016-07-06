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
 *
 */
package de.ims.icarus2.model.manifest.types;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Markus Gärtner
 *
 */
public class Url {

	private final URL url;

	public Url(URL url) {
		if (url == null)
			throw new NullPointerException("Invalid url"); //$NON-NLS-1$

		this.url = url;
	}

	public Url(String spec) throws MalformedURLException {
		this.url = new URL(spec);
	}

	/**
	 * @return the url
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return url.toExternalForm().hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof Url) {
			Url other = (Url) obj;
			return toString().equals(other.toString());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return url.toExternalForm();
	}
}
