/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver;

import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.ReportBuilder;
import de.ims.icarus2.filedriver.FileDataStates.ElementInfo;
import de.ims.icarus2.filedriver.FileDataStates.FileInfo;
import de.ims.icarus2.filedriver.FileDataStates.LayerInfo;
import de.ims.icarus2.filedriver.FileDriver.PreparationStep;
import de.ims.icarus2.filedriver.FileDriverMetadata.ChunkIndexKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.DriverKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.FileKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.ItemLayerKey;
import de.ims.icarus2.filedriver.io.sets.ResourceSet;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.mods.DriverModule;
import de.ims.icarus2.model.api.driver.mods.EmptyModuleMonitor;
import de.ims.icarus2.model.api.driver.mods.ModuleMonitor;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.io.resource.ResourceProvider;

/**
 * @author Markus Gärtner
 *
 */
public enum StandardPreparationSteps implements PreparationStep {

	/**
	 * Verify that metadata holds the correct number of entries for physical files
	 * and that the actual paths match.
	 */
	CHECK_FILE_METADATA {

		@Override
		public boolean apply(FileDriver driver, ReportBuilder<ReportItem> reportBuilder, Options env) throws Exception {

			ResourceSet dataFiles = driver.getDataFiles();
			MetadataRegistry metadataRegistry = driver.getMetadataRegistry();

			int fileCount = dataFiles.getResourceCount();

			// Verify that our metadata holds the correct file count
			String fileCountKey = FileDriverMetadata.DriverKey.FILE_COUNT.getKey();
			int storedFileCount = metadataRegistry.getIntValue(fileCountKey, -1);
			if(storedFileCount!=-1 && storedFileCount!=fileCount) {
				reportBuilder.addError(ModelErrorCode.DRIVER_METADATA_CORRUPTED,
						"Corrupted file count in metadata: expected {} - got {} as stored value",
						_int(fileCount), _int(storedFileCount));
				return false;
			}

			metadataRegistry.setIntValue(fileCountKey, fileCount);

			int invalidFiles = 0;

			for(int fileIndex = 0; fileIndex < fileCount; fileIndex++) {

				Path path = dataFiles.getResourceAt(fileIndex).getPath();
				if(path==null) {
					reportBuilder.addError(ModelErrorCode.DRIVER_ERROR,
							"Resource at index {1} is not a local file - cannot compute file metadata", _int(fileIndex));
					continue;
				}
				FileInfo fileInfo = driver.getFileStates().getFileInfo(fileIndex);

				if(path.getNameCount()==0) {
					reportBuilder.addError(ModelErrorCode.DRIVER_METADATA_CORRUPTED,
							"Invalid path with 0 name elements for file index {}", _int(fileIndex));
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
					reportBuilder.addError(ModelErrorCode.DRIVER_METADATA_CORRUPTED,
							"Corrupted metadata for file index {}: expected '{}' - got '{}'",
							_int(fileIndex), savedPath, pathString);
					fileInfo.setFlag(ElementFlag.CORRUPTED);
					invalidFiles++;
				}

				fileInfo.setPath(path);
			}

			return invalidFiles==0;
		}

		@Override
		public Collection<? extends PreparationStep> getPreconditions() {
			return Collections.emptyList();
		}

	},

	/**
	 * Verify that the physical corpus files exist.
	 * <p>
	 * If context is editable missing files will be created as empty ones.
	 */
	CHECK_FILE_EXISTENCE {

		@Override
		public boolean apply(FileDriver driver, ReportBuilder<ReportItem> reportBuilder, Options env) throws Exception {

			ResourceSet dataFiles = driver.getDataFiles();
			ContextManifest manifest = getContextManifest(driver);
			ResourceProvider resourceProvider = driver.getResourceProvider();

			int fileCount = dataFiles.getResourceCount();
			int invalidFiles = 0;

			for(int fileIndex = 0; fileIndex < fileCount; fileIndex++) {

				FileInfo fileInfo = driver.getFileStates().getFileInfo(fileIndex);
				Path path = fileInfo.getPath();

//				state.setFormatted("fileConnector.checkFileExistence", fileIndex, fileCount, path.getFileName().toString());

				if(!resourceProvider.exists(path)) {

					// If context is editable, we allow for missing files and create them here
					if(manifest.isEditable()) {
						resourceProvider.create(path, false);
					} else {
						fileInfo.setFlag(ElementFlag.MISSING);
						// Signal error, since non-editable data MUST be present
						reportBuilder.addError(ModelErrorCode.DRIVER_METADATA_CORRUPTED,
								"Missing file for index {}: {}", _int(fileIndex), path);
						invalidFiles++;
					}
				}
			}

			return invalidFiles==0;
		}

		@Override
		public Collection<? extends PreparationStep> getPreconditions() {
			return Arrays.asList(CHECK_FILE_METADATA);
		}

	},

	/**
	 * Verify file integrity via {@link FileChecksum checksums}.
	 * <p>
	 * Since using "real" checksums that analyze the entire byte content
	 * of a file we rather use a simplistic minimal checksum comprised of
	 * size of the file and date of last change.
	 */
	CHECK_FILE_CHECKSUM {

		@Override
		public boolean apply(FileDriver driver, ReportBuilder<ReportItem> reportBuilder, Options env) throws Exception {

			MetadataRegistry metadataRegistry = driver.getMetadataRegistry();
			ResourceSet dataFiles = driver.getDataFiles();
			ResourceProvider resourceProvider = driver.getResourceProvider();

			int fileCount = dataFiles.getResourceCount();
			int invalidFiles = 0;

			for(int fileIndex = 0; fileIndex < fileCount; fileIndex++) {

				FileInfo fileInfo = driver.getFileStates().getFileInfo(fileIndex);
				Path path = fileInfo.getPath();

//				state.setFormatted("fileConnector.checkFileChecksum", fileIndex, fileCount, path.getFileName().toString());

				FileChecksum checksum;

				try {
					checksum = FileChecksum.compute(resourceProvider, path);
				} catch (IOException e) {
					reportBuilder.addError(GlobalErrorCode.IO_ERROR,
							"Failed to compute checksum for file at index {} : {}", _int(fileIndex), path, e);
					invalidFiles++;
					continue;
				}

				String checksumKey = FileKey.CHECKSUM.getKey(fileIndex);
				String checksumString = checksum.toString();
				String savedChecksum = metadataRegistry.getValue(checksumKey);

				// Signal corrupted 'checksum' metadata in case the existing value is different from freshly computed one
				if(savedChecksum==null) {
					metadataRegistry.setValue(checksumKey, checksumString);
				} else if(!checksumString.equals(savedChecksum)) {
					reportBuilder.addError(ModelErrorCode.DRIVER_METADATA_CORRUPTED,
							"Invalid checksum stored for file index {}: expected '{}' - got '{}'",
							_int(fileIndex), checksumString, savedChecksum);
					fileInfo.setFlag(ElementFlag.CORRUPTED);
					invalidFiles++;
				}

				// Refresh 'checksum' metadata
				fileInfo.setChecksum(checksum);
			}

			return invalidFiles==0;
		}

		@Override
		public Collection<? extends PreparationStep> getPreconditions() {
			return Arrays.asList(CHECK_FILE_EXISTENCE);
		}

	},

	/**
	 * Verify total and individual byte size of all files that are managed by the driver.
	 */
	CHECK_TOTAL_SIZE {

		@Override
		public boolean apply(FileDriver driver, ReportBuilder<ReportItem> reportBuilder, Options env) throws Exception {

			MetadataRegistry metadataRegistry = driver.getMetadataRegistry();
			ResourceSet dataFiles = driver.getDataFiles();
			ResourceProvider resourceProvider = driver.getResourceProvider();

			int fileCount = dataFiles.getResourceCount();
			int invalidFiles = 0;

			long totalBytes = 0L;

			for(int fileIndex = 0; fileIndex < fileCount; fileIndex++) {

				FileInfo fileInfo = driver.getFileStates().getFileInfo(fileIndex);
				Path path = fileInfo.getPath();

				long fileBytes = 0L;

				try {
					fileBytes = resourceProvider.getResource(path).size();
				} catch (IOException e) {
					reportBuilder.addError(GlobalErrorCode.IO_ERROR,
							"Failed to fetch size for file at index {} : {}", _int(fileIndex), path, e);
					invalidFiles++;
					continue;
				}

				String sizeKey = FileKey.SIZE.getKey(fileIndex);
				String sizeString = String.valueOf(fileBytes);
				String savedSize = metadataRegistry.getValue(sizeKey);

				// Signal corrupted 'checksum' metadata in case the saved value is different from current size
				if(savedSize==null) {
					metadataRegistry.setValue(sizeKey, sizeString);
				} else if(!sizeString.equals(savedSize)) {
					reportBuilder.addError(ModelErrorCode.DRIVER_METADATA_CORRUPTED,
							"Invalid size stored for file index {}: expected '{}' - got '{}'",
							_int(fileIndex), sizeString, savedSize);
					fileInfo.setFlag(ElementFlag.CORRUPTED);
					invalidFiles++;
				}

				// Refresh 'size' metadata
				fileInfo.setSize(fileBytes);

				totalBytes += fileBytes;
			}

			String totalSizeKey = DriverKey.SIZE.getKey();
			String totalSizeString = String.valueOf(totalBytes);
			String savedTotalSize = metadataRegistry.getValue(totalSizeKey);

			ElementInfo globalInfo = driver.getFileStates().getGlobalInfo();
			globalInfo.setProperty(totalSizeKey, totalSizeString);

			boolean totalBytesValid = true;

			// Signal corrupted global metadata in case the saved value is different from current size
			if(savedTotalSize==null) {
				metadataRegistry.setValue(totalSizeKey, totalSizeString);
			} else if(!totalSizeString.equals(savedTotalSize)) {
				reportBuilder.addError(ModelErrorCode.DRIVER_METADATA_CORRUPTED,
						"Invalid total size stored for driver: expected '{}' - got '{}'",
						totalSizeString, savedTotalSize);
				globalInfo.setFlag(ElementFlag.CORRUPTED);
				totalBytesValid = false;
			}

			return invalidFiles==0 && totalBytesValid;
		}

		@Override
		public Collection<? extends PreparationStep> getPreconditions() {
			return Arrays.asList(CHECK_FILE_CHECKSUM);
		}

	},

	/**
	 * Verify that the 'scanned' properties for individual files and the layers
	 * TODO
	 */
	CHECK_LAYER_METADATA {

		@Override
		public boolean apply(FileDriver driver, ReportBuilder<ReportItem> reportBuilder, Options env)
				throws Exception {

			MetadataRegistry metadataRegistry = driver.getMetadataRegistry();
			ResourceSet dataFiles = driver.getDataFiles();
			ContextManifest manifest = getContextManifest(driver);
			List<ItemLayerManifestBase<?>> layers = manifest.getLayerManifests(ModelUtils::isItemLayer);

			int fileCount = dataFiles.getResourceCount();

			int scannedFileCount = 0;

			for(int fileIndex = 0; fileIndex < fileCount; fileIndex++) {
				FileInfo fileInfo = driver.getFileStates().getFileInfo(fileIndex);

				String scannedKey = FileKey.SCANNED.getKey(fileIndex);
				boolean savedScanned = metadataRegistry.getBooleanValue(scannedKey, false);

				if(savedScanned) {
					fileInfo.setFlag(ElementFlag.SCANNED);
					scannedFileCount++;
				}
			}

			for(ItemLayerManifestBase<?> layer : layers) {

//				state.setFormatted("fileConnector.checkLayerMetadata", layer.getId());

				LayerInfo layerInfo = driver.getFileStates().getLayerInfo(layer);

				String scannedKey = ItemLayerKey.SCANNED.getKey(layer);
				boolean savedScanned = metadataRegistry.getBooleanValue(scannedKey, false);

				if(savedScanned) {
					layerInfo.setFlag(ElementFlag.SCANNED);
				}

				// Reset to 'partially scanned' if there are files not marked as being scanned
				if(scannedFileCount>0 && scannedFileCount<fileCount) {
					layerInfo.setFlag(ElementFlag.PARTIALLY_SCANNED);
				}
			}

			return true;
		}

		@Override
		public Collection<? extends PreparationStep> getPreconditions() {
			return Arrays.asList(CHECK_TOTAL_SIZE);
		}

	},

	/**
	 *
	 */
	PREPARE_MODULES {
		/**
		 * @see de.ims.icarus2.filedriver.FileDriver.PreparationStep#apply(de.ims.icarus2.filedriver.FileDriver, de.ims.icarus2.ReportBuilder, de.ims.icarus2.util.Options)
		 */
		@Override
		public boolean apply(FileDriver driver, ReportBuilder<ReportItem> reportBuilder, Options env) throws Exception {

			MutableInteger exceptionCounter = new MutableInteger();

			// We're only interested in failures here
			ModuleMonitor monitor = new EmptyModuleMonitor(){
				@Override
				public synchronized void error(DriverModule module, Exception e) {
					exceptionCounter.incrementAndGet();
					reportBuilder.addError(GlobalErrorCode.DELEGATION_FAILED, "Error while preparing modules", e);
				}
			};

			driver.prepareModules(monitor);

			return exceptionCounter.intValue()==0;
		}

		/**
		 * @see de.ims.icarus2.filedriver.FileDriver.PreparationStep#getPreconditions()
		 */
		@Override
		public Collection<? extends PreparationStep> getPreconditions() {
			return Arrays.asList(CHECK_LAYER_METADATA);
		}
	},

	/**
	 * Delegate to the driver's {@link FileDriver#scanFile(int)} method for every file
	 * that hasn't been scanned before.
	 * This is done in increasing order of the respective {@code file index} to ensure that
	 * for every file all the metadata of previous files is already available.
	 * <p>
	 * If an error occurs during scanning the associated metadata will mark the layers in
	 * question {@link ElementFlag#UNUSABLE unusable}.
	 */
	SCAN_FILES {

		@Override
		public boolean apply(FileDriver driver, ReportBuilder<ReportItem> reportBuilder, Options env) throws Exception {

			MetadataRegistry metadataRegistry = driver.getMetadataRegistry();
			ContextManifest manifest = getContextManifest(driver);
			ResourceSet dataFiles = driver.getDataFiles();

			int fileCount = dataFiles.getResourceCount();
			int invalidFiles = 0;

			for(int fileIndex = 0; fileIndex < fileCount; fileIndex++) {

				FileInfo fileInfo = driver.getFileStates().getFileInfo(fileIndex);

				if(!fileInfo.isFlagSet(ElementFlag.SCANNED)) {

					boolean fileValid = true;

					try {
						driver.scanFile(fileIndex);
					} catch(IOException | InterruptedException e) { //TODO maybe change back to catch general Exception
						reportBuilder.addError(GlobalErrorCode.IO_ERROR,
								"Failed to scan file {} at index {}", fileInfo.getPath(), _int(fileIndex), e);
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
			List<ItemLayerManifestBase<?>> layers = manifest.getLayerManifests(ModelUtils::isItemLayer);
			ElementFlag flag = invalidFiles==0 ? ElementFlag.SCANNED : ElementFlag.PARTIALLY_SCANNED;
			if(invalidFiles==fileCount) {
				flag = ElementFlag.UNUSABLE;
			}

			for(ItemLayerManifestBase<?> layer : layers) {

				LayerInfo layerInfo = driver.getFileStates().getLayerInfo(layer);
				layerInfo.setFlag(flag);

				String scannedKey = ItemLayerKey.SCANNED.getKey(layer);
				metadataRegistry.changeBooleanValue(scannedKey, layerInfo.isValid(), false);
			}

			return invalidFiles==0;
		}

		@Override
		public Collection<? extends PreparationStep> getPreconditions() {
			return Arrays.asList(PREPARE_MODULES);
		}
	},

	CHECK_LAYER_CHUNK_INDEX {

		@Override
		public boolean apply(FileDriver driver, ReportBuilder<ReportItem> reportBuilder, Options env) throws Exception {

			MetadataRegistry metadataRegistry = driver.getMetadataRegistry();
			ResourceProvider resourceProvider = driver.getResourceProvider();
			ContextManifest manifest = getContextManifest(driver);
			List<ItemLayerManifestBase<?>> layers = manifest.getLayerManifests(ModelUtils::isItemLayer);

			int invalidLayers = 0;

			for(ItemLayerManifestBase<?> layer : layers) {

//				state.setFormatted("fileConnector.checkLayerChunkIndex", layer.getId());

				LayerInfo layerInfo = driver.getFileStates().getLayerInfo(layer);

				String useChunkIndexKey = ItemLayerKey.USE_CHUNK_INDEX.getKey(layer);
				boolean savedUseChunkIndex = metadataRegistry.getBooleanValue(useChunkIndexKey, false);

				if(!savedUseChunkIndex) {
					continue;
				}

				String pathKey = ChunkIndexKey.PATH.getKey(layer);
				String savedPath = metadataRegistry.getValue(pathKey);

				Path path = Paths.get(savedPath);

				if(!resourceProvider.exists(path)) {

					layerInfo.setFlag(ElementFlag.MISSING);
					// Signal error, since non-editable data MUST be present
					reportBuilder.addError(ModelErrorCode.DRIVER_METADATA_CORRUPTED,
							"Missing file for chunk index  of layer {}: {}", layer.getId(), savedPath);
					invalidLayers++;
				}
			}

			return invalidLayers==0;
		}

		@Override
		public Collection<? extends PreparationStep> getPreconditions() {
			return Arrays.asList(SCAN_FILES);
		}

	},

	/**
	 *
	 */
	CHECK_LAYER_CONTINUITY {

		private static final String LAST_END_INDEX_KEY = "lastEndIndex";

		/**
		 * Verifies that the index values for the specified layer in the given file
		 * matches the global continuity condition and returns the {@code endIndex}
		 * stored in the metadata.
		 */
		private boolean verifyFileMetadataForLayer(ItemLayerManifestBase<?> layer, int fileIndex,
				FileDriver driver, ReportBuilder<ReportItem> reportBuilder, Options env) {

//			driver.getModuleState().setFormatted("fileConnector.checkLayerContinuity", layer.getId(), fileIndex);

			final MetadataRegistry metadataRegistry = driver.getMetadataRegistry();
			final FileDataStates states = driver.getFileStates();

			long lastEndIndex = env.getLong(LAST_END_INDEX_KEY, IcarusUtils.UNSET_LONG);

			// Load current metadata
			String countKey = FileKey.ITEMS.getKey(fileIndex, layer);
			String beginKey = FileKey.BEGIN.getKey(fileIndex, layer);
			String endKey = FileKey.END.getKey(fileIndex, layer);
			long itemCount = metadataRegistry.getLongValue(countKey, IcarusUtils.UNSET_LONG);
			long beginIndex = metadataRegistry.getLongValue(beginKey, IcarusUtils.UNSET_LONG);
			long endIndex = metadataRegistry.getLongValue(endKey, IcarusUtils.UNSET_LONG);

			if(itemCount==IcarusUtils.UNSET_LONG && beginIndex==IcarusUtils.UNSET_LONG && endIndex==IcarusUtils.UNSET_LONG) {
				// Nothing saved for the layer+file, so ignore it
				return true;
			}

			if(itemCount==IcarusUtils.UNSET_LONG || beginIndex==IcarusUtils.UNSET_LONG || endIndex==IcarusUtils.UNSET_LONG) {
				reportBuilder.addError(ModelErrorCode.DRIVER_METADATA_CORRUPTED,
						"Indicies partly missing in metadata for layer {} in file {}", layer.getId(), _int(fileIndex));
				return false;
			}

			boolean isValid = true;

			// Verify correct span definition and total number of items
			if(itemCount!=(endIndex-beginIndex+1)) {
				reportBuilder.addError(ModelErrorCode.DRIVER_METADATA_CORRUPTED,
						"Total number of items declared for layer {} in file {}  does not match defined span {} to {}",
						layer.getId(), _int(fileIndex), _long(beginIndex), _long(endIndex));
				isValid = false;
			}

			// Make sure the index values form a continuous span over all files
			if(lastEndIndex!=IcarusUtils.UNSET_LONG && beginIndex!=(lastEndIndex+1)) {
				reportBuilder.addError(ModelErrorCode.DRIVER_METADATA_CORRUPTED,
						"Non-continuous item indices for layer {} in file {}: expected {} as begin index, but got {}",
						layer.getId(), _int(fileIndex), _long(lastEndIndex+1), _long(beginIndex));
				isValid = false;
			}

			// First file must always start at item index 0!!!
			if(lastEndIndex==IcarusUtils.UNSET_LONG && fileIndex==0 && beginIndex!=0L) {
				reportBuilder.addError(ModelErrorCode.DRIVER_METADATA_CORRUPTED,
						"First file for layer {} in set must start at item index 0 - got start value {}",
						layer.getId(), _long(beginIndex));
				isValid = false;
			}

			// Refresh info
			FileInfo fileInfo = states.getFileInfo(fileIndex);
			fileInfo.setBeginIndex(layer, beginIndex);
			fileInfo.setEndIndex(layer, endIndex);
			fileInfo.setItemCount(layer, itemCount);

			env.put(LAST_END_INDEX_KEY, _long(endIndex));

			return isValid;
		}

		@Override
		public boolean apply(FileDriver driver, ReportBuilder<ReportItem> reportBuilder, Options env) throws Exception {

			ContextManifest manifest = getContextManifest(driver);
			ResourceSet dataFiles = driver.getDataFiles();
			List<ItemLayerManifestBase<?>> layers = manifest.getLayerManifests(ModelUtils::isItemLayer);

			int fileCount = dataFiles.getResourceCount();
			int invalidLayers = 0;

			env.put(LAST_END_INDEX_KEY, _long(IcarusUtils.UNSET_LONG));

			for(ItemLayerManifestBase<?> layer : layers) {

				LayerInfo layerInfo = driver.getFileStates().getLayerInfo(layer);

				for(int fileIndex = 0; fileIndex < fileCount; fileIndex++) {
					FileInfo fileInfo = driver.getFileStates().getFileInfo(fileIndex);

					if(!verifyFileMetadataForLayer(layer, fileIndex, driver, reportBuilder, env)) {
						fileInfo.setFlag(ElementFlag.CORRUPTED);
						layerInfo.setFlag(ElementFlag.CORRUPTED);
						invalidLayers++;
					}
				}
			}

			env.remove(LAST_END_INDEX_KEY);

			return invalidLayers==0;
		}

		@Override
		public Collection<? extends PreparationStep> getPreconditions() {
			return Arrays.asList(CHECK_LAYER_CHUNK_INDEX);
		}

	},

	;

	private static ContextManifest getContextManifest(Driver driver) {
		return driver.getManifest().getContextManifest()
				.orElseThrow(ManifestException.noHost(driver.getManifest()));
	}
}
