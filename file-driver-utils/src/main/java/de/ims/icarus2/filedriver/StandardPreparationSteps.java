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
package de.ims.icarus2.filedriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.filedriver.FileDriver.PreparationStep;
import de.ims.icarus2.filedriver.FileDataStates.FileInfo;
import de.ims.icarus2.filedriver.FileDataStates.LayerInfo;
import de.ims.icarus2.filedriver.FileMetadata.ChunkIndexKey;
import de.ims.icarus2.filedriver.FileMetadata.FileKey;
import de.ims.icarus2.filedriver.FileMetadata.ItemLayerKey;
import de.ims.icarus2.filedriver.io.sets.FileSet;
import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
public enum StandardPreparationSteps implements PreparationStep, ModelConstants {

	CHECK_FILE_METADATA {

		@Override
		public boolean apply(FileDriver driver, Options env) throws Exception {

			FileSet dataFiles = driver.getDataFiles();
			MetadataRegistry metadataRegistry = driver.getMetadataRegistry();

			int fileCount = dataFiles.getFileCount();

			// Verify that our metadata holds the correct file count
			String fileCountKey = FileMetadata.DriverKey.FILE_COUNT.getKey();
			int storedFileCount = metadataRegistry.getIntValue(fileCountKey, -1);
			if(storedFileCount!=-1 && storedFileCount!=fileCount) {
				log.error("Corrupted file count in metadata: expected {} - got {} as stored value", fileCount, storedFileCount);
				return false;
			} else {
				metadataRegistry.setIntValue(fileCountKey, fileCount);
			}

			int invalidFiles = 0;

			for(int fileIndex = 0; fileIndex < fileCount; fileIndex++) {

				Path path = dataFiles.getFileAt(fileIndex);
				FileInfo fileInfo = driver.getFileDriverStates().getFileInfo(fileIndex);

				if(path.getNameCount()==0) {
					log.error("Invalid path with 0 name elements for file index {}", fileIndex);
					invalidFiles++;
					continue;
				}

//				state.setFormatted("fileConnector.checkFileMetadata", fileIndex, fileCount, path.getFileName().toString());

				String pathString = path.toString();
				String pathKey = FileKey.PATH.getKey(fileIndex);
				String savedPath = metadataRegistry.getValue(pathKey);

				if(savedPath==null) {
					metadataRegistry.setValue(pathKey, pathString);
				} else if(!pathString.equals(savedPath)) {
					log.error("Corrupted metadata for file index {}: expected '{}' - got '{}'",
							fileIndex, savedPath, pathString);
					fileInfo.setFlag(ElementFlag.CORRUPTED);
					invalidFiles++;
				}

				fileInfo.setPath(path);
			}

			return invalidFiles==0;
		}

	},

	CHECK_FILE_EXISTENCE {

		@Override
		public boolean apply(FileDriver driver, Options env) throws Exception {

			FileSet dataFiles = driver.getDataFiles();
			ContextManifest manifest = driver.getManifest().getContextManifest();

			int fileCount = dataFiles.getFileCount();
			int invalidFiles = 0;

			for(int fileIndex = 0; fileIndex < fileCount; fileIndex++) {

				FileInfo fileInfo = driver.getFileDriverStates().getFileInfo(fileIndex);
				Path path = fileInfo.getPath();

//				state.setFormatted("fileConnector.checkFileExistence", fileIndex, fileCount, path.getFileName().toString());

				if(!Files.exists(path)) {

					// If context is editable, we allow for missing files and create them here
					if(manifest.isEditable()) {
						Files.createFile(path);
					} else {
						fileInfo.setFlag(ElementFlag.MISSING);
						// Signal error, since non-editable data MUST be present
						log.error("Missing file for index {}: {}", fileIndex, path);
						invalidFiles++;
					}
				}
			}

			return invalidFiles==0;
		}

	},

	CHECK_FILE_CHECKSUM {

		@Override
		public boolean apply(FileDriver driver, Options env) throws Exception {

			MetadataRegistry metadataRegistry = driver.getMetadataRegistry();
			FileSet dataFiles = driver.getDataFiles();

			int fileCount = dataFiles.getFileCount();
			int invalidFiles = 0;

			for(int fileIndex = 0; fileIndex < fileCount; fileIndex++) {

				FileInfo fileInfo = driver.getFileDriverStates().getFileInfo(fileIndex);
				Path path = fileInfo.getPath();

//				state.setFormatted("fileConnector.checkFileChecksum", fileIndex, fileCount, path.getFileName().toString());

				FileChecksum checksum;

				try {
					checksum = FileChecksum.compute(path);
				} catch (IOException e) {
					log.error("Failed to compute checksum for file at index {} : {}", fileIndex, path, e);
					invalidFiles++;
					continue;
				}

				String checksumKey = FileKey.CHECKSUM.getKey(fileIndex);
				String checksumString = checksum.toString();
				String savedChecksum = metadataRegistry.getValue(checksumKey);

				// Signal corrupted 'checksum' metadata
				if(savedChecksum==null) {
					metadataRegistry.setValue(checksumKey, checksumString);
				} else if(!checksumString.equals(savedChecksum)) {
					log.error("Invalid checksum stored for file index {}: expected '{}' - got '{}'",
							fileIndex, savedChecksum, checksumString);
					fileInfo.setFlag(ElementFlag.CORRUPTED);
					invalidFiles++;
				}

				// Refresh 'checksum' metadata
				fileInfo.setChecksum(checksum);
			}

			return invalidFiles==0;
		}

	},

	CHECK_LAYER_METADATA {

		@Override
		public boolean apply(FileDriver driver, Options env)
				throws Exception {

			MetadataRegistry metadataRegistry = driver.getMetadataRegistry();
			FileSet dataFiles = driver.getDataFiles();
			ContextManifest manifest = driver.getManifest().getContextManifest();
			List<ItemLayerManifest> layers = manifest.getLayerManifests(ModelUtils::isItemLayer);

			int fileCount = dataFiles.getFileCount();

			for(ItemLayerManifest layer : layers) {

//				state.setFormatted("fileConnector.checkLayerMetadata", layer.getId());

				LayerInfo layerInfo = driver.getFileDriverStates().getLayerInfo(layer);

				int scannedFileCount = 0;

				for(int fileIndex = 0; fileIndex < fileCount; fileIndex++) {
					FileInfo fileInfo = driver.getFileDriverStates().getFileInfo(fileIndex);

					String scannedKey = FileKey.SCANNED.getKey(fileIndex);
					boolean savedScanned = metadataRegistry.getBooleanValue(scannedKey, false);

					if(savedScanned) {
						fileInfo.setFlag(ElementFlag.SCANNED);
						scannedFileCount++;
					}
				}

				String scannedKey = ItemLayerKey.SCANNED.getKey(layer);
				boolean savedScanned = metadataRegistry.getBooleanValue(scannedKey, false);

				if(savedScanned) {
					layerInfo.setFlag(ElementFlag.SCANNED);
				}

				if(scannedFileCount>0) {
					layerInfo.setFlag(ElementFlag.PARTIALLY_SCANNED);
				}
			}

			return true;
		}

	},

	CHECK_LAYER_CHUNK_INDEX {

		@Override
		public boolean apply(FileDriver driver, Options env) throws Exception {

			MetadataRegistry metadataRegistry = driver.getMetadataRegistry();
			ContextManifest manifest = driver.getManifest().getContextManifest();
			List<ItemLayerManifest> layers = manifest.getLayerManifests(ModelUtils::isItemLayer);

			int invalidLayers = 0;

			for(ItemLayerManifest layer : layers) {

//				state.setFormatted("fileConnector.checkLayerChunkIndex", layer.getId());

				LayerInfo layerInfo = driver.getFileDriverStates().getLayerInfo(layer);

				String useChunkIndexKey = ItemLayerKey.USE_CHUNK_INDEX.getKey(layer);
				boolean savedUseChunkIndex = metadataRegistry.getBooleanValue(useChunkIndexKey, false);

				if(!savedUseChunkIndex) {
					continue;
				}

				String pathKey = ChunkIndexKey.PATH.getKey(layer);
				String savedPath = metadataRegistry.getValue(pathKey);

				Path path = Paths.get(savedPath);

				if(!Files.exists(path)) {

					layerInfo.setFlag(ElementFlag.MISSING);
					// Signal error, since non-editable data MUST be present
					log.error("Missing file for chunk index  of layer {}: {}", layer.getId(), savedPath);
					invalidLayers++;
				}
			}

			return invalidLayers==0;
		}

	},

	CHECK_LAYER_CONTINUITY {

		private static final String LAST_END_INDEX_KEY = "lastEndIndex";

		/**
		 * Verifies that the index values for the specified layer in the given file
		 * matches the global continuity condition and returns the {@code endIndex}
		 * stored in the metadata.
		 */
		private boolean verifyFileMetadataForLayer(ItemLayerManifest layer, int fileIndex,
				FileDriver driver, Options env) {

//			driver.getModuleState().setFormatted("fileConnector.checkLayerContinuity", layer.getId(), fileIndex);

			final MetadataRegistry metadataRegistry = driver.getMetadataRegistry();
			final FileDataStates states = driver.getFileDriverStates();

			long lastEndIndex = env.getLong(LAST_END_INDEX_KEY, NO_INDEX);

			// Load current metadata
			String countKey = FileKey.ITEMS.getKey(fileIndex, layer);
			String beginKey = FileKey.BEGIN.getKey(fileIndex, layer);
			String endKey = FileKey.END.getKey(fileIndex, layer);
			long itemCount = metadataRegistry.getLongValue(countKey, NO_INDEX);
			long beginIndex = metadataRegistry.getLongValue(beginKey, NO_INDEX);
			long endIndex = metadataRegistry.getLongValue(endKey, NO_INDEX);

			if(itemCount==NO_INDEX && beginIndex==NO_INDEX && endIndex==NO_INDEX) {
				// Nothing saved for the layer+file, so ignore it
				return true;
			}

			if(itemCount==NO_INDEX || beginIndex==NO_INDEX || endIndex==NO_INDEX) {
				log.error("Indicies partly missing in metadata for layer {} in file {}", layer.getId(), fileIndex);
				return false;
			}

			boolean isValid = true;

			// Verify correct span definition and total number of items
			if(itemCount!=(endIndex-beginIndex+1)) {
				log.error("Total number of items declared for layer {} in file {}  does not match defined span {} to {}",
						layer.getId(), fileIndex, beginIndex, endIndex);
				isValid = false;
			}

			// Make sure the index values form a continuous span over all files
			if(lastEndIndex!=NO_INDEX && beginIndex!=(lastEndIndex+1)) {
				log.error("Non-continuous item indices for layer {} in file {}: expected {} as begin index, but got {}",
						layer.getId(), fileIndex, lastEndIndex+1, beginIndex);
				isValid = false;
			}

			// First file must always start at item index 0!!!
			if(lastEndIndex==NO_INDEX && fileIndex==0 && beginIndex!=0L) {
				log.error("First file for layer {} in set must start at item index 0 - got start value {}",
						layer.getId(), beginIndex);
				isValid = false;
			}

			// Refresh info
			FileInfo fileInfo = states.getFileInfo(fileIndex);
			fileInfo.setBeginIndex(layer, beginIndex);
			fileInfo.setEndIndex(layer, endIndex);
			fileInfo.setItemCount(layer, itemCount);

			env.put(LAST_END_INDEX_KEY, endIndex);

			return isValid;
		}

		@Override
		public boolean apply(FileDriver driver, Options env) throws Exception {

			ContextManifest manifest = driver.getManifest().getContextManifest();
			FileSet dataFiles = driver.getDataFiles();
			List<ItemLayerManifest> layers = manifest.getLayerManifests(ModelUtils::isItemLayer);

			int fileCount = dataFiles.getFileCount();
			int invalidLayers = 0;

			env.put(LAST_END_INDEX_KEY, NO_INDEX);

			for(ItemLayerManifest layer : layers) {

				LayerInfo layerInfo = driver.getFileDriverStates().getLayerInfo(layer);

				for(int fileIndex = 0; fileIndex < fileCount; fileIndex++) {
					FileInfo fileInfo = driver.getFileDriverStates().getFileInfo(fileIndex);

					if(!verifyFileMetadataForLayer(layer, fileIndex, driver, env)) {
						fileInfo.setFlag(ElementFlag.CORRUPTED);
						layerInfo.setFlag(ElementFlag.CORRUPTED);
						invalidLayers++;
					}
				}
			}

			env.remove(LAST_END_INDEX_KEY);

			return invalidLayers==0;
		}

	},

	SCAN_FILES {

		@Override
		public boolean apply(FileDriver driver, Options env) throws Exception {

			MetadataRegistry metadataRegistry = driver.getMetadataRegistry();
			ContextManifest manifest = driver.getManifest().getContextManifest();
			FileSet dataFiles = driver.getDataFiles();

			int fileCount = dataFiles.getFileCount();
			int invalidFiles = 0;

			for(int fileIndex = 0; fileIndex < fileCount; fileIndex++) {

				FileInfo fileInfo = driver.getFileDriverStates().getFileInfo(fileIndex);

				if(!fileInfo.isFlagSet(ElementFlag.SCANNED)) {

					boolean fileValid = true;

					try {
						driver.scanFile(fileIndex);
					} catch(IOException e) {
						log.error("Failed to scan file {} at index {}", fileInfo.getPath(), fileIndex, e);
						fileValid = false;
					}

					// Update metadata entry
					metadataRegistry.changeBooleanValue(FileKey.SCANNED.getKey(fileIndex), fileValid, false);

					// Update info
					fileInfo.updateFlag(ElementFlag.SCANNED, fileValid);
					if(!fileValid) {
						invalidFiles++;
						break;
					}
				}
			}

			// Update layer info and metadata
			List<ItemLayerManifest> layers = manifest.getLayerManifests(ModelUtils::isItemLayer);
			ElementFlag flag = invalidFiles==0 ? ElementFlag.SCANNED : ElementFlag.PARTIALLY_SCANNED;
			if(invalidFiles==fileCount) {
				flag = ElementFlag.CORRUPTED;
			}

			for(ItemLayerManifest layer : layers) {

				LayerInfo layerInfo = driver.getFileDriverStates().getLayerInfo(layer);
				layerInfo.setFlag(flag);

				String scannedKey = ItemLayerKey.SCANNED.getKey(layer);
				metadataRegistry.changeBooleanValue(scannedKey, layerInfo.isValid(), false);
			}

			return invalidFiles==0;
		}
	},

	;

	private static Logger log = LoggerFactory.getLogger(StandardPreparationSteps.class);
}
