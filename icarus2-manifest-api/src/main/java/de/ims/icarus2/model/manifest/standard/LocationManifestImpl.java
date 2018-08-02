/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.PathResolverManifest;
import de.ims.icarus2.util.lang.ClassUtils;

/**
 * @author Markus Gärtner
 *
 */
public class LocationManifestImpl extends AbstractManifest<LocationManifest> implements LocationManifest {
	private String rootPath;
	private PathType rootPathType;
	private PathResolverManifest pathResolverManifest;

	private CharSequence inlineData;
	private Boolean inline;

	private final List<PathEntry> pathEntries = new ArrayList<>();

	public LocationManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		super(manifestLocation, registry, false);
	}

	public LocationManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, String path) {
		super(manifestLocation, registry, false);
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
		return rootPath==null && pathResolverManifest==null && pathEntries.isEmpty() && inlineData==null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LocationManifest#getInlineData()
	 */
	@Override
	public CharSequence getInlineData() {
		checkInline();

		return inlineData;
	}

	@Override
	public ManifestType getManifestType() {
		return ManifestType.LOCATION_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LocationManifest#getRootPath()
	 */
	@Override
	public String getRootPath() {
		checkNotInline();

		return rootPath;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LocationManifest#getPathResolverManifest()
	 */
	@Override
	public PathResolverManifest getPathResolverManifest() {
		checkNotInline();

		return pathResolverManifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LocationManifest#setIsInline(boolean)
	 */
	@Override
	public void setIsInline(boolean value) {
		checkNotLocked();

		setIsInline0(value);
	}

	protected void setIsInline0(boolean value) {
		this.inline = value==DEFAULT_IS_INLINE ? null : Boolean.valueOf(value);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LocationManifest#setInlineData(java.lang.CharSequence)
	 */
	@Override
	public void setInlineData(CharSequence data) {
		checkNotLocked();
		checkInline();

		setInlineData0(data);
	}

	protected void setInlineData0(CharSequence data) {
		requireNonNull(data);
		checkArgument(data.length()>0);
		checkInline();

		inlineData = data;
	}

	/**
	 * @param rootPath the rootPath to set
	 */
	@Override
	public void setRootPath(String rootPath) {
		checkNotLocked();

		setRootPath0(rootPath);
	}

	protected void setRootPath0(String rootPath) {
		requireNonNull(rootPath);
		checkArgument(!rootPath.isEmpty());
		checkNotInline();

		this.rootPath = rootPath;
	}

	@Override
	public PathType getRootPathType() {
		checkNotInline();

		return rootPathType==null ? DEFAULT_ROOT_PATH_TYPE : rootPathType;
	}

	@Override
	public void setRootPathType(PathType type) {
		checkNotLocked();

		setRootPathType0(type);
	}

	protected void setRootPathType0(PathType type) {
		requireNonNull(type);
		checkNotInline();

		if(type==DEFAULT_ROOT_PATH_TYPE) {
			type = null;
		}

		rootPathType = type;
	}

	@Override
	public void setPathResolverManifest(PathResolverManifest pathResolverManifest) {
		checkNotLocked();

		setPathResolverManifest0(pathResolverManifest);
	}

	protected void setPathResolverManifest0(PathResolverManifest pathResolverManifest) {
		// NOTE setting the rootPath resolver manifest to null is legal
		// since the framework will use a default file based resolver in that case!
//		if (pathResolverManifest == null)
//			throw new NullPointerException("Invalid pathResolverManifest"); //$NON-NLS-1$
		checkNotInline();

		this.pathResolverManifest = pathResolverManifest;
	}

	@Override
	public void forEachPathEntry(Consumer<? super PathEntry> action) {
		checkNotInline();
		pathEntries.forEach(action);
	}

	@Override
	public void addPathEntry(PathEntry entry) {
		checkNotLocked();

		addPathEntry0(entry);
	}

	protected void addPathEntry0(PathEntry entry) {
		requireNonNull(entry);
		checkNotInline();

		pathEntries.add(entry);
	}

	@Override
	public void removePathEntry(PathEntry entry) {
		checkNotLocked();

		removePathEntry0(entry);
	}

	protected void removePathEntry0(PathEntry entry) {
		requireNonNull(entry);
		checkNotInline();

		pathEntries.remove(entry);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#setIsTemplate(boolean)
	 */
	@Override
	public void setIsTemplate(boolean isTemplate) {
		throw new ManifestException(ManifestErrorCode.MANIFEST_ILLEGAL_TEMPLATE_STATE,
				"Location manifest cannot be a template");
	}

	public static class PathEntryImpl implements PathEntry {

		private final PathType type;
		private final String value;

		public PathEntryImpl(PathType type, String value) {
			if (type == null)
				throw new NullPointerException("Invalid type"); //$NON-NLS-1$
			if (value == null)
				throw new NullPointerException("Invalid value"); //$NON-NLS-1$

			this.type = type;
			this.value = value;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			int hash = 1;
			if(type!=null) {
				hash *= type.hashCode();
			}
			if(value!=null) {
				hash *= value.hashCode();
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
			} if(obj instanceof PathEntry) {
				PathEntry other = (PathEntry) obj;
				return ClassUtils.equals(type, other.getType())
						&& ClassUtils.equals(value, other.getValue());
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "PathEntry["+(type==null ? "<no_type>" : type.getStringValue())+"]@"+(value==null ? "<no_value>" : value); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
