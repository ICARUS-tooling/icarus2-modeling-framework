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
package de.ims.icarus2.model.standard.io;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import de.ims.icarus2.model.api.io.FileManager;
import de.ims.icarus2.model.manifest.api.CorpusManifest;

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
		String folderName = DefaultFileStructure.CORPUS_PREFIX+manifest.getId();
		Path metadataFolder = getSharedFolder(CommonFolders.METADATA.getId());

		return metadataFolder.resolve(folderName);
	}
}
