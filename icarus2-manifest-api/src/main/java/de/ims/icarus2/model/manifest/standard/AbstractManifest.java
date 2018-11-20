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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestFragment;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.VersionManifest;
import de.ims.icarus2.model.manifest.standard.Links.Link;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.IcarusUtils;



/**
 * Implementation note: This class obtains {@link #getUID() uids} lazily when client
 * code actually requests them. See the documentation for {@link #getUID()} for details.
 *
 * @author Markus Gärtner
 *
 */
public abstract class AbstractManifest<T extends Manifest> extends AbstractLockable implements Manifest {

	private TemplateLink<T> template;

	private transient volatile int uid = IcarusUtils.UNSET_INT;

	private Optional<String> id = Optional.empty();
	private boolean isTemplate;
	private Optional<VersionManifest> versionManifest = Optional.empty();

	private transient final ManifestLocation manifestLocation;
	private transient final ManifestRegistry registry;

	protected AbstractManifest(ManifestLocation manifestLocation, ManifestRegistry registry) {
		requireNonNull(manifestLocation);
		requireNonNull(registry);

		this.manifestLocation = manifestLocation;
		this.registry = registry;
	}

	@SuppressWarnings("unchecked")
	protected final <V extends Object, I extends T> Optional<V> getDerivable(
			Optional<V> localValue, Function<I, Optional<V>> getter) {
		Optional<V> result = localValue;
		if(!result.isPresent() && hasTemplate()) {
			result = getter.apply((I)getTemplate());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	protected final <V extends Object, I extends T> Optional<V> getWrappedDerivable(
			Optional<V> localValue, Function<I, V> getter) {
		Optional<V> result = localValue;
		if(!result.isPresent() && hasTemplate()) {
			result = Optional.of(getter.apply((I)getTemplate()));
		}
		return result;
	}

	/**
	 * Queries the {@link CorpusRegistry registry} this manifest is managed by for the
	 * {@link CorpusRegistry#isLocked(Manifest) locked} status of this manifest.
	 * @return
	 */
	@Override
	protected boolean isNestedLocked() {
		return super.isNestedLocked() || getRegistry().isLocked(this);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLockable#lock()
	 */
	@Override
	protected void lockNested() {
		super.lockNested();

		lockNested(versionManifest);
	}

	/**
	 * Lazily creates this manifest's uid in case it hasn't been
	 * initialized yet. This behavior allows long running registry instances
	 * to keep their uid creation conflict free for a longer time. Manifests
	 * that are only created temporarily without ever actually being added to
	 * their registry run the risk of wasting uid space. Since the contract for
	 * {@link ManifestRegistry#resetUIDs()} states that only registered manifests
	 * should be considered when resetting the uid generation process, lazy creation
	 * of uids allows for a slower growth in used up uid space.
	 *
	 * @see de.ims.icarus2.model.manifest.api.Manifest#getUID()
	 */
	@Override
	public int getUID() {
		int uid;
		if((uid = this.uid)==IcarusUtils.UNSET_INT) {
			synchronized (registry) {
				if((uid = this.uid)==IcarusUtils.UNSET_INT) {
					uid = this.uid = registry.createUID();
				}
			}
		}
		return uid;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getManifestLocation(), getRegistry(), getId());
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
//			return getManifestType().equals(other.getManifestType())
//					&& manifestLocation.equals(other.getManifestLocation())
//					&& registry.equals(other.getRegistry())
//					&& ClassUtils.equals(getId(), other.getId());

			return ManifestFragment.defaultEquals(this, other);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getManifestType().toString()
				+"@"+ getId().orElse("<unnamed>");
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

	@Override
	public final Optional<String> getId() {
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
	public Optional<VersionManifest> getVersionManifest() {
		return versionManifest;
	}

	/**
	 * @param versionManifest the versionManifest to set
	 */
	@Override
	public void setVersionManifest(VersionManifest versionManifest) {
		checkNotLocked();

		setVersionManifest0(versionManifest);
	}

	protected void setVersionManifest0(VersionManifest versionManifest) {
		requireNonNull(versionManifest);
		if(this.versionManifest.isPresent())
			throw new ManifestException(ManifestErrorCode.MANIFEST_CORRUPTED_STATE,
					"Version already set on manifest: "+this);

		this.versionManifest = Optional.of(versionManifest);
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
		ManifestUtils.checkId(id);

		this.id = Optional.of(id);
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
		if(!hasTemplate())
			throw new ManifestException(GlobalErrorCode.ILLEGAL_STATE,
					"Manifest not assigned a template link - cannot fetch template: "+ManifestUtils.getName(this));
		return (T) template.get();
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
		checkNotLocked();

		setIsTemplate0(isTemplate);
	}

	protected void setIsTemplate0(boolean isTemplate) {

		if(!manifestLocation.isTemplate())
			throw new ManifestException(ManifestErrorCode.MANIFEST_ERROR,
					"Manifest location does not allow templates: "+ManifestUtils.getName(this));

		if(isTemplate && !isTopLevel() && !hasTemplateContext())
			throw new ManifestException(ManifestErrorCode.MANIFEST_ERROR,
					"Manifest is not top-level or hosted inside a template - cannot use as template: "+ManifestUtils.getName(this));

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
		 * @see de.ims.icarus2.model.manifest.standard.Links.Link#getMissingLinkDescription()
		 */
		@Override
		protected String getMissingLinkDescription() {
			return "Missing template for id: "+getId();
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest.Link#resolve()
		 */
		@SuppressWarnings("unchecked")
		@Override
		protected Optional<D> resolve() {
			return (Optional<D>) getRegistry().getTemplate(getId());
		}

	}
}
