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

 * $Revision: 447 $
 * $Date: 2016-01-14 11:34:47 +0100 (Do, 14 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/manifest/DocumentationImpl.java $
 *
 * $LastChangedDate: 2016-01-14 11:34:47 +0100 (Do, 14 Jan 2016) $
 * $LastChangedRevision: 447 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.manifest;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.manifest.Documentation;
import de.ims.icarus2.util.classes.ClassUtils;

/**
 * @author Markus Gärtner
 * @version $Id: DocumentationImpl.java 447 2016-01-14 10:34:47Z mcgaerty $
 *
 */
public class DocumentationImpl extends DefaultModifiableIdentity implements Documentation {

	private String content;

	private final List<Resource> resources = new ArrayList<>();

	public DocumentationImpl() {
		// no-op
	}

	public DocumentationImpl(String content) {
		setContent(content);
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.DefaultModifiableIdentity#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 1;

		if(getId()!=null) {
			hash *= getId().hashCode();
		}

		return hash;
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.DefaultModifiableIdentity#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof Documentation) {
			Documentation other = (Documentation) obj;
			return ClassUtils.equals(getId(), other.getId());
		}
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.DefaultModifiableIdentity#toString()
	 */
	@Override
	public String toString() {
		return "Documentation@"+String.valueOf(getId()); //$NON-NLS-1$
	}

//	/**
//	 * @param target the target to set
//	 */
//	@Override
//	public void setTarget(Documentable target) {
//		this.target = target;
//	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.Documentation#getContent()
	 */
	@Override
	public String getContent() {
		return content;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.Documentation#forEachResource(java.util.function.Consumer)
	 */
	@Override
	public void forEachResource(Consumer<? super Resource> action) {
		resources.forEach(action);
	}

	/**
	 * @param content the content to set
	 */
	@Override
	public void setContent(String content) {
		checkNotLocked();

		setContent0(content);
	}

	protected void setContent0(String content) {
		this.content = content;
	}

	@Override
	public void addResource(Resource resource) {
		checkNotLocked();

		addResource0(resource);
	}

	protected void addResource0(Resource resource) {
		checkNotNull(resource);

		resources.add(resource);
	}

	@Override
	public void removeResource(Resource resource) {
		checkNotLocked();

		removeResource0(resource);
	}

	protected void removeResource0(Resource resource) {
		checkNotNull(resource);

		resources.remove(resource);
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractLockable#lock()
	 */
	@Override
	public void lock() {
		super.lock();

		for(Resource resource : resources) {
			resource.lock();
		}
	}

	public static class ResourceImpl extends DefaultModifiableIdentity implements Resource {

		private URI uri;

		public ResourceImpl() {
			// Default constructor
		}

		public ResourceImpl(String id, URI uri) {
			setId0(id);
			setUri0(uri);
		}

		/**
		 * @see de.ims.icarus2.model.api.manifest.Documentation.Resource#getUri()
		 */
		@Override
		public URI getUri() {
			return uri;
		}

		/**
		 * @param uri the uri to set
		 */
		@Override
		public void setUri(URI uri) {
			checkNotLocked();

			setUri0(uri);
		}

		protected void setUri0(URI uri) {
			checkNotNull(uri);

			this.uri = uri;
		}

	}
}
