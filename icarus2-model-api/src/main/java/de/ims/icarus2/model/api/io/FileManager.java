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
package de.ims.icarus2.model.api.io;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

import de.ims.icarus2.model.api.io.resources.ResourceProvider;
import de.ims.icarus2.model.manifest.api.CorpusManifest;

/**
 * Models access to resources associated to corpora or the
 * modeling framework in general. Note that this interface is
 * only meant to provide the <b>locations</b> of those resources,
 * not their actual content!
 *
 * For accessing the content of resources located via this interface
 * client code should use an instance of {@link ResourceProvider} associated
 * with the entity that provided the {@code FileManager}.
 *
 * @author Markus Gärtner
 *
 * @see ResourceProvider
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
	 * folders, then this method should return {@code null}.
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
