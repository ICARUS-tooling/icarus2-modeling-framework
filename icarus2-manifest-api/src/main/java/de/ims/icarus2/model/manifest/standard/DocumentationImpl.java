/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.api.Documentation;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.util.lang.ClassUtils;

/**
 * @author Markus Gärtner
 *
 */
public class DocumentationImpl extends DefaultModifiableIdentity implements Documentation {

	private Optional<String> content = Optional.empty();

	private final List<Resource> resources = new ArrayList<>();

	public DocumentationImpl() {
		// no-op
	}

	public DocumentationImpl(String content) {
		setContent(content);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.DefaultModifiableIdentity#hashCode()
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
	 * @see de.ims.icarus2.model.manifest.standard.DefaultModifiableIdentity#equals(java.lang.Object)
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
	 * @see de.ims.icarus2.model.manifest.standard.DefaultModifiableIdentity#toString()
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
	 * @see de.ims.icarus2.model.manifest.api.Documentation#getContent()
	 */
	@Override
	public Optional<String> getContent() {
		return content;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Documentation#forEachResource(java.util.function.Consumer)
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
		this.content = Optional.ofNullable(content);
	}

	@Override
	public void addResource(Resource resource) {
		checkNotLocked();

		addResource0(resource);
	}

	protected void addResource0(Resource resource) {
		requireNonNull(resource);

		if(resources.contains(resource))
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT, "Resource already added: "+resource);

		resources.add(resource);
	}

	@Override
	public void removeResource(Resource resource) {
		checkNotLocked();

		removeResource0(resource);
	}

	protected void removeResource0(Resource resource) {
		requireNonNull(resource);

		if(!resources.contains(resource))
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT, "Unknown resource: "+resource);

		resources.remove(resource);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLockable#lock()
	 */
	@Override
	protected void lockNested() {
		super.lockNested();

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
		 * @see de.ims.icarus2.model.manifest.api.Documentation.Resource#getUri()
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
			requireNonNull(uri);

			this.uri = uri;
		}

	}
}
