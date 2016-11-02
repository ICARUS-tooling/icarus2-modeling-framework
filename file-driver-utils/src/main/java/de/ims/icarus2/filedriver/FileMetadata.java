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

import java.nio.file.Path;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.filedriver.io.RUBlockCache;
import de.ims.icarus2.filedriver.io.UnlimitedBlockCache;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndex;
import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.api.registry.SubRegistry;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.standard.driver.AbstractDriver;

/**
 * Defines keys and key generation functions for the storage of file metadata.
 * <p>
 * The basic {@link FileDriver} and it's modules use the following set of keys
 * to store metadata (subclasses might define additional keys):
 * <p>
 * <b>Driver metadata "&lt;key&gt"</b>
 * <table border="1">
 * <tr><th>Key</th><th>Type</th><th>Description</th></tr>
 * <tr>
 * <td>fileCount</td>
 * <td>int</td>
 * <td>number of files expected by the driver</td>
 * </tr>
 * <tr>
 * <td>metadataFolder</td>
 * <td>string</td>
 * <td>full or relative {@link Path#toString() path} pointing to the root folder used for metadata storage. This information can be used to pick locations for chunk and content indexes.</td>
 * </tr>
 * <tr>
 * <td></td>
 * <td></td>
 * <td></td>
 * </tr>
 * </table>
 * <p>
 * <b>File metadata "[file-index].&lt;key&gt"</b>
 * <table border="1">
 * <tr><th>Key</th><th>Type</th><th>Description</th></tr>
 * <tr>
 * <td>path</td>
 * <td>string</td>
 * <td>full {@link Path#toString() string} path of the {@link Path file} object, used to check whether a file still points to the same location (note that it is up to the client to decide whether or not to use relative or absolute paths!)</td>
 * </tr>
 * <tr>
 * <td>checksum</td>
 * <td>string</td>
 * <td>serialized {@link FileChecksum}, used to track external changes to the file</td>
 * </tr>
 * <tr>
 * <td>[layerId].items</td>
 * <td>long</td>
 * <td>number of {@link Item items} in a {@link ItemLayer layer's} top level container in the file</td>
 * </tr>
 * <tr>
 * <td>[layerId].begin</td>
 * <td>long</td>
 * <td>id of the first {@link Item item} in a {@link ItemLayer layer's} top level container in the file</td>
 * </tr>
 * <tr>
 * <td>[layerId].end</td>
 * <td>long</td>
 * <td>id of the last {@link Item item} in a {@link ItemLayer layer's} top level container in the file</td>
 * </tr>
 * <tr>
 * <td></td>
 * <td></td>
 * <td></td>
 * </tr>
 * </table>
 * <p>
 * <b>Layer metadata "[layer-id].&lt;key&gt"</b>
 * <table border="1">
 * <tr><th>Key</th><th>Type</th><th>Description</th></tr>
 * <tr>
 * <td>items</td>
 * <td>long</td>
 * <td>total number of items in the layer's top level container</td>
 * </tr>
 * <tr>
 * <td>chunkIndex.path</td>
 * <td>string</td>
 * <td>full path to the {@link ChunkIndex} used for the given layer</td>
 * </tr>
 * <tr>
 * <td>chunkIndex.valueType</td>
 * <td>string</td>
 * <td>serialized form of {@link IndexValueType} that should be used for the chunk index</td>
 * </tr>
 * <tr>
 * <td>chunkIndex.blockPower</td>
 * <td>int</td>
 * <td>Exponent used to calculate the number of entries in a single block of data in the chunk index</td>
 * </tr>
 * <tr>
 * <td>chunkIndex.cacheSize</td>
 * <td>int</td>
 * <td>Size of the internal block cache for the chunk index</td>
 * </tr>
 * <tr>
 * <td>chunkIndex.blockCache</td>
 * <td>string</td>
 * <td>Either the fully qualified class name of the {@link BlockCache} implementation to be used for the chunk index
 * (must be accessible via the classloader that loaded this storage instance) or one of the predefined constants
 * {LRU,MRU,UNLIMITED} which stand for either one of the two {@link RUBlockCache} variants or {@link UnlimitedBlockCache}</td>
 * </tr>
 * </table>
 * <p>
 * <b>Container/Structure metadata "[layer-id].[level].&lt;key&gt"</b>
 * <table border="1">
 * <tr><th>Key</th><th>Type</th><th>Description</th></tr>
 * <tr>
 * <td>[containerType].count</td>
 * <td>long</td>
 * <td>number of times the specified {@link ContainerType} was encountered for containers of this level</td>
 * </tr>
 * <tr>
 * <td>[structureType].count</td>
 * <td>long</td>
 * <td>number of times the specified {@link StructureType} was encountered for structures of this level</td>
 * </tr>
 * <tr>
 * <td>minItemCount</td>
 * <td>long</td>
 * <td>smallest number of {@link Item items} encountered in containers of this level (does not include the artificial root nodes present in every structure!)</td>
 * </tr>
 * <tr>
 * <td>maxItemCount</td>
 * <td>long</td>
 * <td>largest number of {@link Item items} encountered in containers of this level (does not include the artificial root nodes present in every structure!)</td>
 * </tr>
 * <tr>
 * <td>avgItemCount</td>
 * <td>double</td>
 * <td>average number of {@link Item items} encountered in containers of this level (does not include the artificial root nodes present in every structure!)</td>
 * </tr>
 * <tr>
 * <td>minSpan</td>
 * <td>long</td>
 * <td>smallest number of continuous {@link Item items} encountered in containers of this level (does not include the artificial root nodes present in every structure!)</td>
 * </tr>
 * <tr>
 * <td>maxSpan</td>
 * <td>long</td>
 * <td>largest number of continuous {@link Item items} encountered in containers of this level (does not include the artificial root nodes present in every structure!)</td>
 * </tr>
 * <tr>
 * <td>avgSpan</td>
 * <td>double</td>
 * <td>average number of continuous {@link Item items} encountered in containers of this level (does not include the artificial root nodes present in every structure!)</td>
 * </tr>
 * <tr>
 * <td>minEdgeCount</td>
 * <td>long</td>
 * <td>smallest number of {@link Edge edges} encountered in structures of this level</td>
 * </tr>
 * <tr>
 * <td>maxEdgeCount</td>
 * <td>long</td>
 * <td>largest number of {@link Edge edges} encountered in structures of this level</td>
 * </tr>
 * <tr>
 * <td>avgEdgeCount</td>
 * <td>double</td>
 * <td>average number of {@link Edge edges} encountered in structures of this level</td>
 * </tr>
 * <tr>
 * <td>minHeight</td>
 * <td>int</td>
 * <td>minimum encountered {@link Structure#getHeight() height} encountered for any given {@link Structure}</td>
 * </tr>
 * <tr>
 * <td>maxHeight</td>
 * <td>int</td>
 * <td>maximum encountered {@link Structure#getHeight() height} encountered for any given {@link Structure}</td>
 * </tr>
 * <tr>
 * <td>avgHeight</td>
 * <td>double</td>
 * <td>average encountered {@link Structure#getHeight() height} encountered for any given {@link Structure}</td>
 * </tr>
 * <tr>
 * <td>minBranching</td>
 * <td>int</td>
 * <td>minimum encountered {@link Structure#getEdgeCount(Item, boolean) branching factor} (number of outgoing edges) encountered for any given node in all {@link Structure}s</td>
 * </tr>
 * <tr>
 * <td>maxBranching</td>
 * <td>int</td>
 * <td>maximum encountered {@link Structure#getEdgeCount(Item, boolean) branching factor} (number of outgoing edges) encountered for any given node in all {@link Structure}s</td>
 * </tr>
 * <tr>
 * <td>avgBranching</td>
 * <td>double</td>
 * <td>average encountered {@link Structure#getEdgeCount(Item, boolean) branching factor} (number of outgoing edges) encountered for any given node in all {@link Structure}s</td>
 * </tr>
 * </table>
 * <p>
 * <b>Mapping metadata "mapping.[source-layer-id]-[target-layer-id].&lt;key&gt"</b>
 * <table border="1">
 * <tr><th>Key</th><th>Type</th><th>Description</th></tr>
 * <tr>
 * <td>path</td>
 * <td>string</td>
 * <td>full path to the physical location of the mapping</td>
 * </tr>
 * <tr>
 * <td>valueType</td>
 * <td>string</td>
 * <td>serialized form of {@link IndexValueType} that should be used for the mapping</td>
 * </tr>
 * <tr>
 * <td>cacheSize</td>
 * <td>int</td>
 * <td>Size of the internal block cache for the mapping</td>
 * </tr>
 * <tr>
 * <td>blockCache</td>
 * <td>string</td>
 * <td>Either the fully qualified class name of the {@link BlockCache} implementation to be used for the mapping
 * (must be accessible via the classloader that loaded this storage instance) or one of the predefined constants
 * {LRU,MRU,UNLIMITED} which stand for either one of the two {@link RUBlockCache} variants or {@link UnlimitedBlockCache}</td>
 * </tr>
 * <tr>
 * <td>blockPower</td>
 * <td>int</td>
 * <td>Exponent used to calculate the number of entries in a single block of data in the mapping</td>
 * </tr>
 * <tr>
 * <td>groupPower</td>
 * <td>int</td>
 * <td>Exponent used to calculate the number of entries to be grouped together in the mapping</td>
 * </tr>
 * </table>
 *
 * @author Markus Gärtner
 *
 */
public class FileMetadata implements ModelConstants {

	// PREFIXES

	private static final String FILE_PREFIX = "file_";
	private static final String LEVEL_PREFIX = "level_";
	private static final String MAPPING_PREFIX = "mapping";
	private static final String CHUNK_INDEX_PREFIX = "chunkIndex";
	private static final char ID_PREFIX = '$';
	private static final char _SEP_ = '.';
	private static final char LINK = '-';

	// SUFFIXES


	public static interface MetadataKey {
		String name();

		ValueType getType();
	}

	public static enum DriverKey implements MetadataKey {

		/**
		 * Number of content files for the corpus expected by the driver
		 */
		FILE_COUNT("fileCount", ValueType.INTEGER),

		/**
		 * Root folder for storage of metadata. This information is used for decisions regarding
		 * physical location of chunk and content indices.
		 */
		METADATA_FOLDER("metadataFolder", ValueType.STRING),

		/**
		 * Size of all the data files in bytes.
		 * <p>
		 * Used by driver implementations to determine necessity of certain optional
		 * modules like {@link ChunkIndex} instances which might only be feasible for
		 * rather large sets of data.
		 */
		SIZE("size", ValueType.LONG),

		;

		private final String key;
		private final ValueType type;

		private DriverKey(String key, ValueType type) {
			this.key = key;
			this.type = type;
		}

		public String getKey() {
			return key;
		}

		@Override
		public ValueType getType() {
			return type;
		}
	}

	/**
	 * Metadata keys for files.
	 * The {@link #isLayerSubKey()} method signals whether or not a key is additionally
	 * associated with a {@link ItemLayerManifest layer}.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static enum FileKey implements MetadataKey {

		/**
		 * 16 Byte checksum of the file, in string form
		 */
		CHECKSUM("checksum", ValueType.STRING, false),

		/**
		 * Path to the physical file as specified by client code (may be absolute or relative)
		 */
		PATH("path", ValueType.STRING, false),

		/**
		 * Size of the data file in bytes.
		 */
		SIZE("size", ValueType.LONG, false),

		/**
		 * FLag indicating whether the file has been scanned completely
		 */
		SCANNED("scanned", ValueType.BOOLEAN, false),

		/**
		 * Number of items in the root container of a given layer (in this file)
		 */
		ITEMS("items", ValueType.LONG, true),

		/**
		 * Index of the first item in a layer's root container that occurs in this file
		 */
		BEGIN("begin", ValueType.LONG, true),

		/**
		 * Index of the last item in a layer's root container that occurs in this file
		 */
		END("end", ValueType.LONG, true),
		;

		private final String suffix;
		private final ValueType type;
		private final boolean isLayerSubKey;

		private FileKey(String suffix, ValueType type, boolean isLayerSubKey) {
			this.suffix = suffix;
			this.type = type;
			this.isLayerSubKey = isLayerSubKey;
		}

		public boolean isLayerSubKey() {
			return isLayerSubKey;
		}

		public String getSuffix() {
			return suffix;
		}

		public String getKey(int fileIndex) {
			if(isLayerSubKey)
				throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
						"Key requires layer context: "+suffix);
			return getFileKey(fileIndex, suffix);
		}

		public String getKey(int fileIndex, ItemLayerManifest layer) {
			if(!isLayerSubKey)
				throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
						"Key does not allow layer context: "+suffix);
			return getFileKey(fileIndex, layer, suffix);
		}

		@Override
		public ValueType getType() {
			return type;
		}

		public void deleteFileData(MetadataRegistry metadataRegistry, int fileIndex) {
			// Use the SubRegistry mechanics to erase part of the given metadata registry
			try(SubRegistry sub = new SubRegistry(metadataRegistry, getFileKey(fileIndex, ""))){
				sub.delete();
			}
		}
	}

	/**
	 * Metadata keys for {@link ItemLayerManifest layers}.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static enum ItemLayerKey implements MetadataKey {

		/**
		 * Total number of items in the layer's root container
		 */
		ITEMS("items", ValueType.LONG),

		/**
		 * Flag indicating that the layer has been scanned completely (meaning all files
		 * containing items for it have also been scanned)
		 */
		SCANNED("scanned", ValueType.BOOLEAN),

		/**
		 * Flag indicating whether or not there exists a {@link ChunkIndex} for this layer
		 * (or whether one should be used at all). Note that especially for smaller corpora
		 * it is often not worth the management overhead to have a chunk index.
		 */
		USE_CHUNK_INDEX("useChunkIndex", ValueType.BOOLEAN),
		;

		private final String suffix;
		private final ValueType type;

		private ItemLayerKey(String suffix, ValueType type) {
			this.suffix = suffix;
			this.type = type;
		}

		public String getSuffix() {
			return suffix;
		}

		public String getKey(ItemLayerManifest layer) {
			return getLayerKey(layer, suffix);
		}

		@Override
		public ValueType getType() {
			return type;
		}

		public void deleteLayerData(MetadataRegistry metadataRegistry, ItemLayerManifest layer) {
			delete(metadataRegistry, getLayerKey(layer, ""));
		}
	}

	/**
	 * Metadata keys for {@link ChunkIndex} instances.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static enum ChunkIndexKey implements MetadataKey {

		/**
		 * Path to the physical location of the chunk index.
		 */
		PATH("path", ValueType.STRING),

		/**
		 * The actual {@link IndexValueType} in string form to be used
		 * for the chunk index. If no type is specified the most general
		 * {@link IndexValueType#LONG long} type is being used, which in most cases
		 * means a waste of memory.
		 */
		VALUE_TYPE("valueType", ValueType.STRING),

		/**
		 * Exponent of the <i>power-of-2</i> block size to be used for the chunk index
		 */
		BLOCK_POWER("blockPower", ValueType.INTEGER),

		/**
		 * Hint on what cache implementation/strategy should be used for the chunk index.
		 * Value is to be interpreted as follows:
		 * <table border="1">
		 * <tr><th>Value</th><th>Description</th></tr>
		 * <tr><td>{@link AbstractDriver#HINT_LRU_CACHE LRU}</td><td>Hint to use a {@link RUBlockCache#newLeastRecentlyUsedCache() least recently used} caching strategy</td></tr>
		 * <tr><td>{@link AbstractDriver#HINT_MRU_CACHE MRU}</td><td>Hint to use a {@link RUBlockCache#newMostRecentlyUsedCache() most recently used} caching strategy</td></tr>
		 * <tr><td>{@link AbstractDriver#HINT_UNLIMITED_CACHE UNLIMITED}</td><td>Hint to use a cache that {@link UnlimitedBlockCache never} discards blocks</td></tr>
		 * <tr><td><i>Any other String</i></td><td>Interpreted as the fully qualified class name of the {@link BlockCache} implementation to be used</td></tr>
		 * </table>
		 */
		BLOCK_CACHE("blockCache", ValueType.STRING),

		/**
		 * Size of the internal {@link BlockCache} of a chunk index. This value together
		 * with the {@link #VALUE_TYPE value type} and {@link #BLOCK_POWER block power}
		 * defines the maximum memory consumption of the chunk index.
		 */
		CACHE_SIZE("cacheSize", ValueType.INTEGER),
		;

		private final String suffix;
		private final ValueType type;

		private ChunkIndexKey(String suffix, ValueType type) {
			this.suffix = suffix;
			this.type = type;
		}

		public String getSuffix() {
			return suffix;
		}

		public String getKey(ItemLayerManifest layer) {
			return getChunkIndexKey(layer, suffix);
		}

		@Override
		public ValueType getType() {
			return type;
		}

		public void deleteChunkIndexData(MetadataRegistry metadataRegistry, ItemLayerManifest layer) {
			delete(metadataRegistry, getChunkIndexKey(layer, ""));
		}
	}

	/**
	 * Metadata keys for {@link ContainerManifest containers}.
	 * <p>
	 * The {@link #isTypeSubKey()} signals whether a key is additionally
	 * associated with a {@link ContainerType type} information.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static enum ContainerKey implements MetadataKey {

		/**
		 * Number of instances of a particular {@link ContainerType type} of container in the current level
		 */
		COUNT("count", ValueType.LONG, true),

		/**
		 * Sum of the {@link Container#getItemCount() sizes} of all containers in the current level
		 */
		TOTAL_ITEM_COUNT("totalItemCount", ValueType.LONG, false),

		/**
		 * Smallest {@link Container#getItemCount() size} of a container encountered in the current level
		 */
		MIN_ITEM_COUNT("minItemCount", ValueType.LONG, false),

		/**
		 * Greatest {@link Container#getItemCount() size} of a container encountered in the current level
		 */
		MAX_ITEM_COUNT("maxItemCount", ValueType.LONG, false),

		/**
		 * Average {@link Container#getItemCount() size} of all containers in the current level
		 */
		AVG_ITEM_COUNT("avgItemCount", ValueType.DOUBLE, false),

		// Optional metadata

		/**
		 * Minimum number of continuous items (i.e. items that are adjacent in a common host layer)
		 * contained in a container in the current level.
		 * <p>
		 * Note that this information is a redundant form of {@link #MIN_ITEM_COUNT} in case all
		 * containers in the current level are of type {@link ContainerType#SPAN span}.
		 */
		MIN_SPAN("minSpan", ValueType.LONG, false),

		/**
		 * Maximum number of continuous items (i.e. items that are adjacent in a common host layer)
		 * contained in a container in the current level.
		 * <p>
		 * Note that this information is a redundant form of {@link #MAX_ITEM_COUNT} in case all
		 * containers in the current level are of type {@link ContainerType#SPAN span}.
		 */
		MAX_SPAN("maxSpan", ValueType.LONG, false),

		/**
		 * Average number of continuous items (i.e. items that are adjacent in a common host layer)
		 * of all containers in the current level.
		 * <p>
		 * Note that this information is a redundant form of {@link #AVG_ITEM_COUNT} in case all
		 * containers in the current level are of type {@link ContainerType#SPAN span}.
		 */
		AVG_SPAN("avgSpan", ValueType.DOUBLE, false),
		;

		private final String suffix;
		private final ValueType type;
		private final boolean isTypeSubKey;

		private ContainerKey(String suffix, ValueType type, boolean isTypeSubKey) {
			this.suffix = suffix;
			this.type = type;
			this.isTypeSubKey = isTypeSubKey;
		}

		public boolean isTypeSubKey() {
			return isTypeSubKey;
		}

		public String getSuffix() {
			return suffix;
		}

		public String getKey(ItemLayerManifest layer, int level) {
			if(isTypeSubKey)
				throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
						"Key requires type context: "+suffix);
			return getContainerKey(layer, level, suffix);
		}

		public String getKey(ItemLayerManifest layer, int level, ContainerType type) {
			if(!isTypeSubKey)
				throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
						"Key does not allow type context: "+suffix);
			return getContainerTypeKey(layer, level, type, suffix);
		}

		@Override
		public ValueType getType() {
			return type;
		}

		public void deleteContainerData(MetadataRegistry metadataRegistry, ItemLayerManifest layer, int level) {
			delete(metadataRegistry, getContainerKey(layer, level, ""));
		}
	}

	/**
	 * Metadata keys for {@link StructureManifest containers}.
	 * <p>
	 * The {@link #isTypeSubKey()} signals whether a key is additionally
	 * associated with a {@link StructureType type} information.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static enum StructureKey implements MetadataKey {
		COUNT("count", ValueType.LONG, true),
		// Edge counts
		TOTAL_EDGE_COUNT("totalEdgeCount", ValueType.LONG, false),
		MIN_EDGE_COUNT("minEdgeCount", ValueType.LONG, false),
		MAX_EDGE_COUNT("maxEdgeCount", ValueType.LONG, false),
		AVG_EDGE_COUNT("avgEdgeCount", ValueType.DOUBLE, false),

		// Optional metadata

		// Height
		MIN_HEIGHT("minHeight", ValueType.LONG, false),
		MAX_HEIGHT("maxHeight", ValueType.LONG, false),
		AVG_HEIGHT("avgHeight", ValueType.DOUBLE, false),
		// Branching
		MIN_BRANCHING_FACTOR("minEdgeCount", ValueType.LONG, false),
		MAX_BRANCHING_FACTOR("maxEdgeCount", ValueType.LONG, false),
		AVG_BRANCHING_FACTOR("avgEdgeCount", ValueType.DOUBLE, false),
		;

		private final String suffix;
		private final ValueType type;
		private final boolean isTypeSubKey;

		private StructureKey(String suffix, ValueType type, boolean isTypeSubKey) {
			this.suffix = suffix;
			this.type = type;
			this.isTypeSubKey = isTypeSubKey;
		}

		public boolean isTypeSubKey() {
			return isTypeSubKey;
		}

		public String getSuffix() {
			return suffix;
		}

		public String getKey(StructureLayerManifest layer, int level) {
			return getStructureKey(layer, level, suffix);
		}

		public String getKey(StructureLayerManifest layer, int level, StructureType type) {
			return getStructureTypeKey(layer, level, type, suffix);
		}

		@Override
		public ValueType getType() {
			return type;
		}

		public void deleteStructureData(MetadataRegistry metadataRegistry, StructureLayerManifest layer, int level) {
			delete(metadataRegistry, getStructureKey(layer, level, ""));
		}
	}

	/**
	 * Metadata keys for {@link Mapping mappings}.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static enum MappingKey implements MetadataKey {
		PATH("path", ValueType.STRING),
		VALUE_TYPE("valueType", ValueType.STRING),
		BLOCK_POWER("blockPower", ValueType.INTEGER),
		GROUP_POWER("groupPower", ValueType.INTEGER),
		BLOCK_CACHE("blockCache", ValueType.STRING),
		CACHE_SIZE("cacheSize", ValueType.INTEGER),
		;

		private final String suffix;
		private final ValueType type;

		private MappingKey(String suffix, ValueType type) {
			this.suffix = suffix;
			this.type = type;
		}

		public String getSuffix() {
			return suffix;
		}

		public String getKey(ItemLayerManifest source, ItemLayerManifest target) {
			return getMappingKey(source, target, suffix);
		}

		@Override
		public ValueType getType() {
			return type;
		}

		public void deleteMappingData(MetadataRegistry metadataRegistry, ItemLayerManifest source, ItemLayerManifest target) {
			delete(metadataRegistry, getMappingKey(source, target, ""));
		}
	}

	private static void delete(MetadataRegistry metadataRegistry, String prefix) {

		// Use the SubRegistry mechanics to erase part of the given metadata registry
		try(SubRegistry sub = new SubRegistry(metadataRegistry, prefix)){
			sub.delete();
		}
	}

	private static String getFileKey(int fileIndex, String suffix) {
		return FILE_PREFIX+String.valueOf(fileIndex)+_SEP_+suffix;
	}

	private static String getFileKey(int fileIndex, ItemLayerManifest layer, String suffix) {
		return FILE_PREFIX+String.valueOf(fileIndex)+_SEP_+ID_PREFIX+layer.getId()+_SEP_+suffix;
	}

	private static String getLayerKey(ItemLayerManifest layer, String suffix) {
		return ID_PREFIX+layer.getId()+_SEP_+suffix;
	}

	private static String getChunkIndexKey(ItemLayerManifest layer, String suffix) {
		return ID_PREFIX+layer.getId()+_SEP_+CHUNK_INDEX_PREFIX+_SEP_+suffix;
	}

	private static String getContainerKey(ItemLayerManifest layer, int level, String suffix) {
		return ID_PREFIX+layer.getId()+_SEP_+LEVEL_PREFIX+String.valueOf(level)+_SEP_+suffix;
	}

	private static String getContainerTypeKey(ItemLayerManifest layer, int level, ContainerType type, String suffix) {
		return ID_PREFIX+layer.getId()+_SEP_+LEVEL_PREFIX+String.valueOf(level)+_SEP_+type.name()+_SEP_+suffix;
	}

	private static String getStructureKey(StructureLayerManifest layer, int level, String suffix) {
		return ID_PREFIX+layer.getId()+_SEP_+LEVEL_PREFIX+String.valueOf(level)+_SEP_+suffix;
	}

	private static String getStructureTypeKey(StructureLayerManifest layer, int level, StructureType type, String suffix) {
		return ID_PREFIX+layer.getId()+_SEP_+LEVEL_PREFIX+String.valueOf(level)+_SEP_+type.name()+_SEP_+suffix;
	}

	private static String getMappingKey(ItemLayerManifest source, ItemLayerManifest target, String suffix) {
		return MAPPING_PREFIX+_SEP_+ID_PREFIX+source.getId()+LINK+ID_PREFIX+target.getId()+_SEP_+suffix;
	}
}