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
package de.ims.icarus2.model.standard.io;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import de.ims.icarus2.model.api.io.FileManager;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultFileManager implements FileManager {

	private final static Map<String, String> sharedFolderLookup;
	static {
		Map<String, String> map = new Object2ObjectOpenHashMap<>();

		map.put(CommonFolders.METADATA.getId(), DefaultFileStructure.METADATA_FOLDER_NAME);
		map.put(CommonFolders.TEMP.getId(), DefaultFileStructure.TEMP_FOLDER_NAME);

		sharedFolderLookup = Collections.unmodifiableMap(map);
	}

	private final Path rootFolder;

	/**
	 * @param rootFolder
	 */
	public DefaultFileManager(Path rootFolder) {
		requireNonNull(rootFolder);
		checkArgument("Root path must be a directory", Files.isDirectory(rootFolder));
		//TODO maybe check if folder actually exists, is not a link, etc...

		this.rootFolder = rootFolder;
	}

	/**
	 * @see de.ims.icarus2.model.api.io.FileManager#getRootFolder()
	 */
	@Override
	public Path getRootFolder() {
		return rootFolder;
	}

	private Path getToplevelPath(String folder) {
		return rootFolder.resolve(folder);
	}

	/**
	 * @see de.ims.icarus2.model.api.io.FileManager#getSharedFolder(java.lang.String)
	 */
	@Override
	public Path getSharedFolder(String name) {
		String storedName = sharedFolderLookup.get(name);
		if(storedName==null) {
			return null;
		}

		return getToplevelPath(storedName);
	}

	/**
	 * @see de.ims.icarus2.model.api.io.FileManager#getCorpusFolder(de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public Path getCorpusFolder(CorpusManifest manifest) {
		String folderName = DefaultFileStructure.CORPUS_PREFIX+manifest.getId(); //TODO clean folder name
		Path metadataFolder = getSharedFolder(CommonFolders.METADATA.getId());

		return metadataFolder.resolve(folderName);
	}
}
