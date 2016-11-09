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

import static de.ims.icarus2.util.Conditions.checkArgument;

import java.nio.file.Paths;

import de.ims.icarus2.filedriver.FileDriverMetadata.MappingKey;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.filedriver.io.RUBlockCache;
import de.ims.icarus2.filedriver.io.UnlimitedBlockCache;
import de.ims.icarus2.filedriver.mapping.DefaultMappingFactory;
import de.ims.icarus2.filedriver.mapping.MappingFactory;
import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.io.resources.FileResource;
import de.ims.icarus2.model.api.io.resources.IOResource;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
public class FileDriverUtils {

	static final String SHARED_PROPERTY_PREFIX = ModelUtils.SHARED_PROPERTY_PREFIX+".fileDriver";

	public static final String FILE_SIZE_THRESHOLD_FOR_CHUNKING_PROPERTY = SHARED_PROPERTY_PREFIX+".fileSizeThresholdForChunking";

	public static final String CACHE_SIZE_FOR_CHUNKING_PROPERTY = SHARED_PROPERTY_PREFIX+".cacheSizeForChunking";

	public static final String BLOCK_POWER_FOR_CHUNKING_PROPERTY = SHARED_PROPERTY_PREFIX+".blockPowerForChunking";

	/**
	 * Suffix to be appended to a {@link LayerGroupManifest}'s {@link LayerGroupManifest#getId() id}
	 * in order to access saved estimates about that groups individual chunks (i.e. elements of the
	 * group's {@link LayerGroupManifest#getPrimaryLayerManifest() primary layer}).
	 */
	public static final String ESTIMATED_CHUNK_SIZE_SUFFIX = ".chunkSize";

	public static final String CHUNK_INDEX_FILE_ENDING = ".chk";


	/**
	 * Defines legal property keys used by methods in this utility class and the
	 * {@link DefaultMappingFactory} implementation when creating mapping objects.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static enum MappingProperty {

		UNARY_FUNCTION("unaryFunction", "function", "unary"),
		BATCH_FUNCTION("batchFunction", "batch"),
		RESOURCE("resource", "storage"),
		CAPACITY("capacity"),
		VALUE_TYPE("valueType", "indexValueType"),
		CACHE_SIZE("cacheSize"),
		BLOCK_CACHE("blockCache", "cache"),
		BLOCK_POWER("blockPower"),

		/**
		 *
		 */
		GROUP_POWER("groupPower"),
		;

		private final String[] keys;

		private MappingProperty(String...keys) {
			checkArgument(keys.length>0);

			this.keys = keys;
		}

		public String key() {
			return keys[0];
		}

		public String[] getKeys() {
			return keys.clone();
		}

		public Object getValue(Options options) {
			return options.firstSet(keys);
		}
	}

	/**
	 * Constant identifying a {@link RUBlockCache} implementation obtained via
	 * {@link RUBlockCache#newLeastRecentlyUsedCache()}.
	 */
	public static final String HINT_LRU_CACHE = "LRU";

	/**
	 * Constant identifying a {@link RUBlockCache} implementation obtained via
	 * {@link RUBlockCache#newMostRecentlyUsedCache()}.
	 */
	public static final String HINT_MRU_CACHE = "MRU";

	/**
	 * Constant identifying a {@link UnlimitedBlockCache} implementation.
	 */
	public static final String HINT_UNLIMITED_CACHE = "UNLIMITED";

	/**
	 * Instantiates and returns a new {@link BlockCache} object based on the given string {@code s}.
	 * <p>
	 * The strategy is as follows:
	 * <ol>
	 * <li>If {@code s} equals {@link #HINT_LRU_CACHE} it will call {@link RUBlockCache#newLeastRecentlyUsedCache()}</li>
	 * <li>If {@code s} equals {@link #HINT_MRU_CACHE} it will call {@link RUBlockCache#newMostRecentlyUsedCache()()}</li>
	 * <li>If {@code s} equals {@link #HINT_UNLIMITED_CACHE} it will create a new instance of {@link UnlimitedBlockCache}</li>
	 * <li>Otherwise {@code s} is expected to be the fully qualified class name of a cache implementation</li>
	 * </ol>
	 *
	 * If all of the above steps fail the method will throw a {@link ModelException} of type {@link ManifestErrorCode#IMPLEMENTATION_ERROR}.
	 *
	 * @param s
	 * @return
	 */
	public static BlockCache toBlockCache(String s) {
		if(s==null) {
			return null;
		}

		if(HINT_LRU_CACHE.equals(s)) {
			return RUBlockCache.newLeastRecentlyUsedCache();
		} else if(HINT_MRU_CACHE.equals(s)) {
			return RUBlockCache.newMostRecentlyUsedCache();
		} else if(HINT_UNLIMITED_CACHE.equals(s)) {
			return new UnlimitedBlockCache();
		} else {
			try {
				return (BlockCache) Class.forName(s).newInstance();
			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException e) {
				throw new ModelException(ManifestErrorCode.IMPLEMENTATION_ERROR,
						"Unable to instantiate block cache: "+s, e);
			}
		}
	}

	public static Integer toInteger(int value) {
		return value==ModelConstants.NO_INDEX_INT ? null : Integer.valueOf(value);
	}

	public static Long toLong(long value) {
		return value==ModelConstants.NO_INDEX ? null : Long.valueOf(value);
	}

	public static IndexValueType toValueType(String s) {
		if(s==null) {
			return null;
		}

		return IndexValueType.parseIndexValueType(s);
	}

	/**
	 * If {@code s} is not {@code null} this method will create a new {@link FileResource}
	 * with interpreting {@code s} as the path to a physical file.
	 *
	 * @param s
	 * @return
	 */
	public static IOResource toResource(String s) {
		if(s==null) {
			return null;
		}

		return new FileResource(Paths.get(s));
	}

	/**
	 * Shared factory method to create mapping objects for file drivers.
	 * This implementation augments the given {@link Options options} with values for the following properties:
	 * <ul>
	 * <li>{@link MappingProperty#BLOCK_CACHE}</li>
	 * <li>{@link MappingProperty#CACHE_SIZE}</li>
	 * <li>{@link MappingProperty#BLOCK_POWER}</li>
	 * <li>{@link MappingProperty#GROUP_POWER}</li>
	 * <li>{@link MappingProperty#VALUE_TYPE}</li>
	 * <li>{@link MappingProperty#RESOURCE}</li>
	 * </ul>
	 *
	 * The values are created by querying the {@link MetadataRegistry} of this driver for a set of {@link MappingKey mapping-keys}
	 * and processing those stored values. The augmented options are then passed to the super method to do the actual instantiation
	 * and verification work.
	 *
	 * @param mappingFactory
	 * @param mappingManifest
	 * @param options
	 * @return
	 *
	 * @see #toBlockCache(String)
	 * @see #toInteger(int)
	 * @see #toResource(String)
	 * @see #toValueType(String)
	 */
	public static Mapping createMapping(MappingFactory mappingFactory, MappingManifest mappingManifest, MetadataRegistry metadataRegistry, Options options) {
		// Populate options

		ContextManifest contextManifest = mappingManifest.getDriverManifest().getContextManifest();

		ItemLayerManifest source = (ItemLayerManifest) contextManifest.getLayerManifest(mappingManifest.getSourceLayerId());
		ItemLayerManifest target = (ItemLayerManifest) contextManifest.getLayerManifest(mappingManifest.getTargetLayerId());

		options.put(MappingProperty.BLOCK_CACHE.key(), toBlockCache(metadataRegistry.getValue(MappingKey.BLOCK_CACHE.getKey(source, target))));
		options.put(MappingProperty.CACHE_SIZE.key(), toInteger(metadataRegistry.getIntValue(MappingKey.CACHE_SIZE.getKey(source, target), -1)));
		options.put(MappingProperty.BLOCK_POWER.key(), toInteger(metadataRegistry.getIntValue(MappingKey.BLOCK_POWER.getKey(source, target), -1)));
		options.put(MappingProperty.GROUP_POWER.key(), toInteger(metadataRegistry.getIntValue(MappingKey.GROUP_POWER.getKey(source, target), -1)));
		options.put(MappingProperty.VALUE_TYPE.key(), toValueType(metadataRegistry.getValue(MappingKey.VALUE_TYPE.getKey(source, target))));
		options.put(MappingProperty.RESOURCE.key(), toResource(metadataRegistry.getValue(MappingKey.PATH.getKey(source, target))));

		// Populate options further

		Mapping mapping = mappingFactory.createMapping(mappingManifest, options);

		//TODO do verification?

		return mapping;
	}
}
