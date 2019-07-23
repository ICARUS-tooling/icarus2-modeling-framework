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
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.PathResolverManifest;
import de.ims.icarus2.util.lang.ClassUtils;

/**
 * @author Markus Gärtner
 *
 */
public class LocationManifestImpl extends AbstractManifest<LocationManifest> implements LocationManifest {
	private Optional<String> rootPath = Optional.empty();
	private Optional<PathType> rootPathType = Optional.empty();
	private Optional<PathResolverManifest> pathResolverManifest = Optional.empty();

	private Optional<CharSequence> inlineData = Optional.empty();
	private Boolean inline;

	private final List<PathEntry> pathEntries = new ArrayList<>();

	public LocationManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		super(manifestLocation, registry);
	}

	public LocationManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, String path) {
		this(manifestLocation, registry);
		setRootPath(path);
	}

	public LocationManifestImpl(ContextManifest contextManifest) {
		this(contextManifest.getManifestLocation(), contextManifest.getRegistry());
	}

	protected void checkInline() {
		if(!isInline())
			throw new ManifestException(ManifestErrorCode.MANIFEST_ERROR,
					"Location not declared to host inline data");
	}

	protected void checkNotInline() {
		if(isInline())
			throw new ManifestException(ManifestErrorCode.MANIFEST_ERROR,
					"Location declared to host inline data");
	}

	@Override
	protected boolean isTopLevel() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LocationManifest#isInline()
	 */
	@Override
	public boolean isInline() {
		return inline==null ? DEFAULT_IS_INLINE : inline.booleanValue();
	}

	@Override
	public int hashCode() {
		int h = super.hashCode();

		if(rootPath!=null) {
			h *= rootPath.hashCode();
		}
		if(rootPathType!=null) {
			h *= rootPathType.hashCode();
		}

		return h;
	}

	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} else if(obj instanceof LocationManifest) {
			LocationManifest other = (LocationManifest) obj;
			return ClassUtils.equals(getRootPath(), other.getRootPath())
					&& ClassUtils.equals(getRootPathType(), other.getRootPathType())
					&& super.equals(obj);
		}
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return !rootPath.isPresent() && !pathResolverManifest.isPresent()
				&& pathEntries.isEmpty() && !inlineData.isPresent();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LocationManifest#getInlineData()
	 */
	@Override
	public Optional<CharSequence> getInlineData() {
		checkInline();

		return inlineData;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LocationManifest#getRootPath()
	 */
	@Override
	public Optional<String> getRootPath() {
		checkNotInline();

		return rootPath;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LocationManifest#getPathResolverManifest()
	 */
	@Override
	public Optional<PathResolverManifest> getPathResolverManifest() {
		checkNotInline();

		return pathResolverManifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LocationManifest#setIsInline(boolean)
	 */
	@Override
	public LocationManifest setIsInline(boolean value) {
		checkNotLocked();

		setIsInline0(value);

		return this;
	}

	protected void setIsInline0(boolean value) {
		this.inline = value==DEFAULT_IS_INLINE ? null : Boolean.valueOf(value);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LocationManifest#setInlineData(java.lang.CharSequence)
	 */
	@Override
	public LocationManifest setInlineData(CharSequence data) {
		checkNotLocked();
		checkInline();

		setInlineData0(data);

		return this;
	}

	protected void setInlineData0(CharSequence data) {
		checkInline();

		inlineData = Optional.of(data);
	}

	/**
	 * @param rootPath the rootPath to set
	 */
	@Override
	public LocationManifest setRootPath(String rootPath) {
		checkNotLocked();

		setRootPath0(rootPath);

		return this;
	}

	protected void setRootPath0(String rootPath) {
		requireNonNull(rootPath);
		if(rootPath.isEmpty())
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT, "Root path must not be empty");
		checkNotInline();

		this.rootPath = Optional.of(rootPath);
	}

	@Override
	public Optional<PathType> getRootPathType() {
		checkNotInline();

		return rootPathType;
	}

	@Override
	public LocationManifest setRootPathType(PathType type) {
		checkNotLocked();

		setRootPathType0(type);

		return this;
	}

	protected void setRootPathType0(PathType type) {
		requireNonNull(type);
		checkNotInline();

		rootPathType = Optional.of(type);
	}

	@Override
	public LocationManifest setPathResolverManifest(@Nullable PathResolverManifest pathResolverManifest) {
		checkNotLocked();

		setPathResolverManifest0(pathResolverManifest);

		return this;
	}

	protected void setPathResolverManifest0(PathResolverManifest pathResolverManifest) {
		// NOTE setting the rootPath resolver manifest to null is legal
		// since the framework will use a default file based resolver in that case!
//		if (pathResolverManifest == null)
//			throw new NullPointerException("Invalid pathResolverManifest"); //$NON-NLS-1$
		checkNotInline();

		this.pathResolverManifest = Optional.ofNullable(pathResolverManifest);
	}

	@Override
	public void forEachPathEntry(Consumer<? super PathEntry> action) {
		checkNotInline();
		pathEntries.forEach(action);
	}

	@Override
	public LocationManifest addPathEntry(PathEntry entry) {
		checkNotLocked();

		addPathEntry0(entry);

		return this;
	}

	protected void addPathEntry0(PathEntry entry) {
		requireNonNull(entry);
		checkNotInline();

		if(pathEntries.contains(entry))
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
					"Path entry already present: "+entry);

		pathEntries.add(entry);
	}

	@Override
	public LocationManifest removePathEntry(PathEntry entry) {
		checkNotLocked();

		removePathEntry0(entry);

		return this;
	}

	protected void removePathEntry0(PathEntry entry) {
		requireNonNull(entry);
		checkNotInline();

		if(!pathEntries.contains(entry))
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
					"Unknown path entry: "+entry);

		pathEntries.remove(entry);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#setIsTemplate(boolean)
	 */
	@Override
	public LocationManifest setIsTemplate(boolean isTemplate) {
		throw new ManifestException(ManifestErrorCode.MANIFEST_ILLEGAL_TEMPLATE_STATE,
				"Location manifest does not support templates");
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#setTemplateId(java.lang.String)
	 */
	@Override
	public LocationManifest setTemplateId(@Nullable String templateId) {
		throw new ManifestException(ManifestErrorCode.MANIFEST_ILLEGAL_TEMPLATE_STATE,
				"Location manifest does not support templates");
	}

	public static class PathEntryImpl implements PathEntry {

		private final PathType type;
		private final String value;

		public PathEntryImpl(PathType type, String value) {
			this.type = requireNonNull(type);
			this.value = requireNonNull(value);
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(type, value);
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof PathEntry) {
				PathEntry other = (PathEntry) obj;
				return Objects.equals(type, other.getType())
						&& Objects.equals(value, other.getValue());
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "PathEntry["+(type==null ? "<no_type>" : type.getStringValue())+"]@"
						+(value==null ? "<no_value>" : value);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.LocationManifest.PathEntry#getType()
		 */
		@Override
		public PathType getType() {
			return type;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.LocationManifest.PathEntry#getValue()
		 */
		@Override
		public String getValue() {
			return value;
		}

	}
}
