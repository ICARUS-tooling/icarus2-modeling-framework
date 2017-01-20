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
 */
package de.ims.icarus2.model.api.io;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

import de.ims.icarus2.model.manifest.api.CorpusManifest;

/**
 * @author Markus Gärtner
 *
 */
public interface FileManager {

	/**
	 * Returns a common root folder that acts as a working directory for
	 * this instance of the model framework.
	 *
	 * @return
	 */
	Path getRootFolder();

	/**
	 * Returns a shared folder, usually named for some kind of task.
	 * <p>
	 * A file manager implementation should at least support all the
	 * folder names specified in {@link CommonFolders}, but can of course
	 * declare additional folder names usable for identification.
	 * <p>
	 * Note that it is not required to reflect the {@code name} used
	 * to identify a certain folder in that folder's actual name. It
	 * is also not mandatory that a folder returned by this method
	 * is a subfolder of the global {@link #getRootFolder() root folder}.
	 *
	 * @param name
	 * @return
	 */
	Path getSharedFolder(String name);

	/**
	 * Returns a kind of corpus specific <i>root folder</i> that allows
	 * storage of metadata, indices and other data related to that corpus
	 * at a central location.
	 * <p>
	 * If the file manager implementation does not support corpus specific
	 * folders, then this method schould return {@code null}.
	 *
	 * @param manifest
	 * @return
	 */
	Path getCorpusFolder(CorpusManifest manifest);

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public enum CommonFolders {

		/**
		 * Shared folder for storing metadata, chunk indices, content indices etc...
		 *
		 * Used when those kinds of informations are not split into individual corpus
		 * specific folders.
		 */
		METADATA("metadata"),

		/**
		 * Temporary files that can be deleted at the end of each session without causing
		 * issues.
		 */
		TEMP("temp"),
		;

		private final String folderName;

		private CommonFolders(String folderName) {
			requireNonNull(folderName);
			this.folderName = folderName;
		}

		public String getId() {
			return folderName;
		}
	}
}
