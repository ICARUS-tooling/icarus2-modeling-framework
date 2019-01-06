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
package de.ims.icarus2.model.manifest.types;



/**
 * @author Markus Gärtner
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
