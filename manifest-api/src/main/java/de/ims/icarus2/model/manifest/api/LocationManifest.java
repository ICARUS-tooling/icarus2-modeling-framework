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

 * $Revision: 445 $
 * $Date: 2016-01-11 17:33:05 +0100 (Mo, 11 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/manifest/LocationManifest.java $
 *
 * $LastChangedDate: 2016-01-11 17:33:05 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 445 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.strings.StringResource;


/**
 * @author Markus Gärtner
 * @version $Id: LocationManifest.java 445 2016-01-11 16:33:05Z mcgaerty $
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface LocationManifest extends Manifest {

	public static final PathType DEFAULT_ROOT_PATH_TYPE = PathType.FILE;

//	LocationType getType();

	/**
	 * Returns the "root" path to the location described by this manifest.
	 * Depending on the exact location type, the meaning of this root path
	 * may vary. It can denote a single corpus file, an entire folder or the
	 * identifier of a database, for example.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	String getRootPath();

	@AccessRestriction(AccessMode.READ)
	PathType getRootPathType();

	/**
	 * If the data source is distributed then this method returns the manifest
	 * that describes the resolver to use when accessing different chunks of data.
	 * If the data is not of distributed nature this method returns {@code null}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	PathResolverManifest getPathResolverManifest();

	@AccessRestriction(AccessMode.READ)
	void forEachPathEntry(Consumer<? super PathEntry> action);

	@AccessRestriction(AccessMode.READ)
	default List<PathEntry> getPathEntries() {
		LazyCollection<PathEntry> result = LazyCollection.lazyList();

		forEachPathEntry(result);

		return result.getAsList();
	}

	// Modification methods

	void setRootPath(String path);

	void setRootPathType(PathType type);

	void setPathResolverManifest(PathResolverManifest pathResolverManifest);

	void addPathEntry(PathEntry entry);

	void removePathEntry(PathEntry entry);

	public enum PathType implements StringResource {

		/**
		 * Describes direct pointers to a data file.
		 */
		FILE("file"), //$NON-NLS-1$

		/**
		 * Describes a pointer to some data folder from which to pick the actual data files.
		 * Usually this is accompanied by a {@link PathResolverManifest} declaration in the
		 * hosting {@link LocationManifest} as a means of filtering files or defining the
		 * required file ending, etc...
		 */
		FOLDER("folder"), //$NON-NLS-1$

		/**
		 * Currently unused type.
		 *
		 */
		PATTERN("pattern"), //$NON-NLS-1$

		/**
		 * Defines an abstract pointer to some arbitrary resource. It is the responsibility of the
		 * respective path resolver to manage access to that resource. Format and meaning of this
		 * path type is thus resolver implementation dependent.
		 */
		IDENTIFIER("identifier"), //$NON-NLS-1$

		/**
		 * Signals that all information on how to access the location's data is implemented directly
		 * by the path resolver used to access it.
		 */
		CUSTOM("custom"); //$NON-NLS-1$

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

		private static Map<String, PathType> xmlLookup;

		public static PathType parsePathType(String s) {
			if(xmlLookup==null) {
				Map<String, PathType> map = new HashMap<>();
				for(PathType type : values()) {
					map.put(type.xmlForm, type);
				}
				xmlLookup = map;
			}

			return xmlLookup.get(s);
		}
	}

	/**
	 * Models an abstract path to be interpreted relative to the {@link LocationManifest#getRootPath() root path}
	 * of the hosting {@link LocationManifest}.
	 *
	 * @author Markus Gärtner
	 * @version $Id: LocationManifest.java 445 2016-01-11 16:33:05Z mcgaerty $
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
