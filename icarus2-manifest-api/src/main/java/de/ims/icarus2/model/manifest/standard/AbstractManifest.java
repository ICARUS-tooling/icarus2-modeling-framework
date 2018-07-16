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
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.manifest.api.LayerType;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.VersionManifest;
import de.ims.icarus2.model.manifest.standard.Links.Link;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.lang.ClassUtils;



/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractManifest<T extends Manifest> extends AbstractLockable implements Manifest {

	private TemplateLink<T> template;

	private transient int uid;

	private String id;
	private boolean isTemplate;
	private VersionManifest versionManifest;

	private transient final ManifestLocation manifestLocation;
	private transient final ManifestRegistry registry;

	public static void verifyEnvironment(ManifestLocation manifestLocation, Object environment, Class<?> expected) {
		if(!manifestLocation.isTemplate() && !expected.isInstance(environment))
			throw new ManifestException(ManifestErrorCode.MANIFEST_MISSING_ENVIRONMENT,
					"Missing environment of type "+expected.getName()); //$NON-NLS-1$
	}

	protected AbstractManifest(ManifestLocation manifestLocation, ManifestRegistry registry) {
		this(manifestLocation, registry, true);
	}

	protected AbstractManifest(ManifestLocation manifestLocation, ManifestRegistry registry, boolean isAllowedTemplate) {
		requireNonNull(manifestLocation);
		requireNonNull(registry);

//		if(!isAllowedTemplate && manifestLocation.isTemplate())
//			throw new ModelException(ModelError.MANIFEST_INVALID_LOCATION, "Template environment not supported by manifest: "+xmlTag());

		this.manifestLocation = manifestLocation;
		this.registry = registry;
		uid = registry.createUID();
	}

	/**
	 * Queries the {@link CorpusRegistry registry} this manifest is managed by for the
	 * {@link CorpusRegistry#isLocked(Manifest) locked} status of this manifest.
	 * @return
	 */
	@Override
	public boolean isLocked() {
		return super.isLocked() || getRegistry().isLocked(this);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLockable#lock()
	 */
	@Override
	public void lock() {
		super.lock();

		lockNested(versionManifest);
	}

	@Override
	public int getUID() {
		return uid;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = getManifestType().hashCode()*registry.hashCode();

		if(getId()!=null) {
			hash *= getId().hashCode();
		}

		return hash;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof Manifest) {
			Manifest other = (Manifest) obj;

			//FIXME currently manifest location is excluded from equality check
			return getManifestType().equals(other.getManifestType())
//					&& manifestLocation.equals(other.getManifestLocation())
					&& registry.equals(other.getRegistry())
					&& ClassUtils.equals(getId(), other.getId());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String s = getManifestType().toString();

		if(getId()!=null) {
			s += "@"+getId(); //$NON-NLS-1$
		}

		return s;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Manifest#getManifestLocation()
	 */
	@Override
	public ManifestLocation getManifestLocation() {
		return manifestLocation;
	}

	/**
	 * @return the registry
	 */
	@Override
	public ManifestRegistry getRegistry() {
		return registry;
	}

	/**
	 * @return the id
	 */
	@Override
	public final String getId() {
		return id;
	}

	/**
	 * Creates an identifier that can be used for this manifest in the case
	 * of a missing {@code id} declaration.
	 * @return
	 */
	protected String createDummyId() {
		return getClass().getSimpleName()+"@"+"<unnamed>";
	}

	/**
	 * @return the versionManifest
	 */
	@Override
	public VersionManifest getVersionManifest() {
		return versionManifest;
	}

	/**
	 * @param versionManifest the versionManifest to set
	 */
	@Override
	public void setVersionManifest(VersionManifest versionManifest) {
		requireNonNull(versionManifest);
		if(this.versionManifest!=null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "Version already set on manifest: "+this);

		this.versionManifest = versionManifest;
	}

	/**
	 * @param id the id to set
	 */
	@Override
	public void setId(String id) {
		checkNotLocked();

		setId0(id);
	}

	protected void setId0(String id) {
		requireNonNull(id);
		if(!ManifestUtils.isValidId(id))
			throw new ManifestException(ManifestErrorCode.MANIFEST_INVALID_ID,
					"Id format not supported: "+id); //$NON-NLS-1$

		this.id = id;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Manifest#setTemplateId(java.lang.String)
	 */
	@Override
	public void setTemplateId(String templateId) {
		checkNotLocked();

		setTemplateId0(templateId);
	}

	protected void setTemplateId0(String templateId) {
//		checkNotNull(templateId);

		template = templateId==null ? null : new TemplateLink<>(templateId);
	}

	@Override
	public T getTemplate() {
		return template==null ? null : template.get();
	}

	@Override
	public boolean hasTemplate() {
		return template!=null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Manifest#isTemplate()
	 */
	@Override
	public boolean isTemplate() {
		return isTemplate;
	}

	/**
	 * @param isTemplate the isTemplate to set
	 */
	@Override
	public void setIsTemplate(boolean isTemplate) {
		if(!manifestLocation.isTemplate())
			throw new ManifestException(ManifestErrorCode.MANIFEST_ERROR,
					"Manifest location does not allow templates: "+ManifestUtils.getName(this));

		if(!isTopLevel())
			throw new ManifestException(ManifestErrorCode.MANIFEST_ERROR,
					"Manifest  is not top-level - cannot use as template: "+ManifestUtils.getName(this));

		this.isTemplate = isTemplate;
	}

	protected abstract boolean isTopLevel();

	protected class TemplateLink<D extends Manifest> extends Link<D> {

		/**
		 * @param abstractDerivable
		 * @param id
		 */
		public TemplateLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest.Link#resolve()
		 */
		@SuppressWarnings("unchecked")
		@Override
		protected D resolve() {
			return (D) registry.getTemplate(getId());
		}

	}

	protected class LayerTypeLink extends Link<LayerType> {

		/**
		 * @param id
		 */
		public LayerTypeLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.Links.Link#resolve()
		 */
		@Override
		protected LayerType resolve() {
			return registry.getLayerType(getId());
		}

	}
}
