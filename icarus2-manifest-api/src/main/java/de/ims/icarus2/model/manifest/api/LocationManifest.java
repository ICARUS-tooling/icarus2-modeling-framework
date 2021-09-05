/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.api;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.util.LazyStore;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.strings.StringResource;


/**
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface LocationManifest extends Manifest {

	public static final PathType DEFAULT_ROOT_PATH_TYPE = PathType.FILE;
	public static final boolean DEFAULT_IS_INLINE = false;

	@Override
	default ManifestType getManifestType() {
		return ManifestType.LOCATION_MANIFEST;
	}

//	LocationType getType();

	/**
	 * Returns {@code true} iff this location manifest contains inline data,
	 * i.e. it doesn't point to a physical resource but already hosts the
	 * actual content.
	 *
	 * @return
	 */
	boolean isInline();

	/**
	 * If this manifest is declared to host {@link #isInline() inline} data
	 * returns this content, otherwise invoking this method will throw an
	 * exception.
	 *
	 * @return
	 *
	 * @throws ManifestException in case the manifest is {@link #isInline() not declared}
	 * to host inline data.
	 */
	@Unguarded("Method depends on 'isInline' flag")
	Optional<CharSequence> getInlineData();

	/**
	 * Returns the "root" path to the location described by this manifest.
	 * Depending on the exact location type, the meaning of this root path
	 * may vary. It can denote a single corpus file, an entire folder or the
	 * identifier of a database, for example.
	 * <p>
	 * Returns an empty {@link Optional} if this manifest is declared to host
	 * {@link #isInline() inline} data.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<String> getRootPath();

	/**
	 * Returns information on how to interpret the {@link #getRootPath() root path}
	 * of this location.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<PathType> getRootPathType();

	/**
	 * If the data source is distributed then this method returns the manifest
	 * that describes the resolver to use when accessing different chunks of data.
	 * If the data is not of distributed nature this method returns an empty {@link Optional}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<PathResolverManifest> getPathResolverManifest();

	@AccessRestriction(AccessMode.READ)
	void forEachPathEntry(Consumer<? super PathEntry> action);

	@AccessRestriction(AccessMode.READ)
	default List<PathEntry> getPathEntries() {
		return LazyCollection.<PathEntry>lazyList()
				.addFromForEach(this::forEachPathEntry)
				.getAsList();
	}

	// Modification methods

	LocationManifest setIsInline(boolean value);

	@Unguarded("Method depends on 'isInline' flag")
	LocationManifest setInlineData(CharSequence data);

	LocationManifest setRootPath(String path);

	LocationManifest setRootPathType(PathType type);

	LocationManifest setPathResolverManifest(@Nullable PathResolverManifest pathResolverManifest);

	LocationManifest addPathEntry(PathEntry entry);

	LocationManifest removePathEntry(PathEntry entry);

	public enum PathType implements StringResource {

		/**
		 * Describes direct pointers to a data file.
		 */
		FILE("file"),

		/**
		 * Points to a resource that is available via {@link ClassLoader#getResource(String)}.
		 *
		 * Note that there is currently some uncertainty as to which class loader to use for
		 * accessing the resource.
		 */
		RESOURCE("resource"), //TODO specify default protocol for accessing resources

		/**
		 * Describes a pointer to some data folder from which to pick the actual data files.
		 * Usually this is accompanied by a {@link PathResolverManifest} declaration in the
		 * hosting {@link LocationManifest} as a means of filtering files or defining the
		 * required file ending, etc...
		 */
		FOLDER("folder"),

		/**
		 * Currently unused type.
		 *
		 */
		PATTERN("pattern"),

		/**
		 * Defines an abstract pointer to some arbitrary resource. It is the responsibility of the
		 * respective path resolver to manage access to that resource. Format and meaning of this
		 * path type is thus resolver implementation dependent.
		 */
		IDENTIFIER("identifier"),

		/**
		 * Signals that all information on how to access the location's data is implemented directly
		 * by the path resolver used to access it.
		 */
		CUSTOM("custom");

		private final String xmlForm;

		private PathType(String xmlForm) {
			this.xmlForm = xmlForm;
		}

		/**
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}

		/**
		 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
		 */
		@Override
		public String getStringValue() {
			return xmlForm;
		}

		private static LazyStore<PathType, String> store = LazyStore.forStringResource(
				PathType.class, true);

		public static PathType parsePathType(String s) {
			return store.lookup(s);
		}
	}

	/**
	 * Models an abstract path to be interpreted relative to the {@link LocationManifest#getRootPath() root path}
	 * of the hosting {@link LocationManifest}.
	 *
	 * @author Markus Gärtner
	 *
	 */
	@AccessControl(AccessPolicy.DENY)
	public interface PathEntry {

		@AccessRestriction(AccessMode.READ)
		PathType getType();

		@AccessRestriction(AccessMode.READ)
		String getValue();
	}
}
