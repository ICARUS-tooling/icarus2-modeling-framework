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
package de.ims.icarus2.filedriver;

import static de.ims.icarus2.util.Conditions.checkArgument;

import java.nio.file.Paths;

import de.ims.icarus2.filedriver.FileDriverMetadata.MappingKey;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.filedriver.io.RUBlockCache;
import de.ims.icarus2.filedriver.io.UnlimitedBlockCache;
import de.ims.icarus2.filedriver.mapping.DefaultMappingFactory;
import de.ims.icarus2.filedriver.mapping.MappingFactory;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.io.resources.FileResource;
import de.ims.icarus2.model.api.io.resources.IOResource;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.IcarusUtils;
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
	 * <p>
	 * The resulting {@code String} can be used to query
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
		return value==IcarusUtils.UNSET_INT ? null : Integer.valueOf(value);
	}

	public static Long toLong(long value) {
		return value==IcarusUtils.UNSET_LONG ? null : Long.valueOf(value);
	}

	public static IndexValueType toValueType(String s) {
		if(s==null) {
			return null;
		}

		return IndexValueType.parseIndexValueType(s);
	}

	/**
	 * If {@code s} is not {@code null} this method will create a new {@link FileResource}
	 * interpreting {@code s} as the path to a physical file.
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

		ContextManifest contextManifest = ManifestUtils.requireGrandHost(mappingManifest);

		ItemLayerManifestBase<?> source = mappingManifest.getSourceLayerId()
				.flatMap(id -> contextManifest.<ItemLayerManifestBase<?>>getLayerManifest(id))
				.orElseThrow(ManifestException.missing(mappingManifest, "resolvable source layer"));
		ItemLayerManifestBase<?> target = mappingManifest.getTargetLayerId()
				.flatMap(id -> contextManifest.<ItemLayerManifestBase<?>>getLayerManifest(id))
				.orElseThrow(ManifestException.missing(mappingManifest, "resolvable target layer"));

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
