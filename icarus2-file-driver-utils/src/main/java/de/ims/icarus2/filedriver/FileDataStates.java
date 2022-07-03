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

import static de.ims.icarus2.util.IcarusUtils.UNSET_DOUBLE;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.ObjIntConsumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.FileDriverMetadata.ChunkIndexKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.ContainerKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.ContainerKeyBase;
import de.ims.icarus2.filedriver.FileDriverMetadata.DriverKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.FileKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.ItemLayerKey;
import de.ims.icarus2.filedriver.FileDriverMetadata.StructureKey;
import de.ims.icarus2.filedriver.io.sets.ResourceSet;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.Hierarchy;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.LongCounter;
import de.ims.icarus2.util.Syncable;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.stat.Histogram;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Centralized storage of (virtual) metadata for resources managed by
 * a {@link FileDriver}.
 *
 * Not thread-safe!
 *
 * @author Markus Gärtner
 *
 */
public class FileDataStates implements Syncable<MetadataRegistry> {

	private final GlobalInfo globalInfo = new GlobalInfo();

	// File states and meta info
	private final Int2ObjectMap<FileInfo> fileInfos = new Int2ObjectOpenHashMap<>();

	// Layer states and meta info
	private final Int2ObjectMap<LayerInfo> layerInfos = new Int2ObjectOpenHashMap<>();
	// Layer states and meta info
	private final Int2ObjectMap<ChunkIndexInfo> chunkIndexInfos = new Int2ObjectOpenHashMap<>();

	@SuppressWarnings("rawtypes")
	private final ItemLayerManifestBase[] layers;

	/**
	 * Initializes all {@link ElementInfo elements} based on data from the
	 * given {@code driver}.
	 * <p>
	 * Note that this does <b>not</b> include reading the actual metadata
	 * associated with the driver. This still remains the driver's own
	 * responsibility.
	 *
	 * @param driver
	 */
	public FileDataStates(FileDriver driver) {

		List<ItemLayerManifestBase<?>> layers = new ObjectArrayList<>();

		// Collect layers
		ContextManifest contextManifest = ManifestUtils.requireHost(driver.getManifest());
		for(LayerManifest<?> layerManifest : contextManifest.getLayerManifests()) {

			if(ManifestUtils.isAnyItemLayerManifest(layerManifest)) {
				ItemLayerManifestBase<?> itemLayer = (ItemLayerManifestBase<?>)layerManifest;
				layerInfos.put(layerManifest.getUID(), new LayerInfo(itemLayer));
				layers.add(itemLayer);
			}
			//TODO do we need to allow chunking for structure layers?
			if(ManifestUtils.isItemLayerManifest(layerManifest)) {
				chunkIndexInfos.put(layerManifest.getUID(), new ChunkIndexInfo((ItemLayerManifestBase<?>)layerManifest));
			}
		}

		// Collect data files
		ResourceSet dataFiles = driver.getDataFiles();
		for(int fileIndex=0; fileIndex<dataFiles.getResourceCount(); fileIndex++) {
			fileInfos.put(fileIndex, new FileInfo(fileIndex));
		}

		//TODO populate other maps/lookups!!!

		this.layers = CollectionUtils.toArray(layers, ItemLayerManifestBase[]::new);
	}

	@Override
	public void syncTo(MetadataRegistry registry) {
		globalInfo.syncTo(registry);
		fileInfos.forEach((index, info) -> info.syncTo(registry));
		layerInfos.forEach((index, info) -> info.syncTo(registry));
		chunkIndexInfos.forEach((index, info) -> info.syncTo(registry));
	}

	@Override
	public void syncFrom(MetadataRegistry registry) {
		globalInfo.syncFrom(registry);
		fileInfos.forEach((index, info) -> info.syncFrom(registry));
		layerInfos.forEach((index, info) -> info.syncFrom(registry));
		chunkIndexInfos.forEach((index, info) -> info.syncFrom(registry));
	}

	public GlobalInfo getGlobalInfo() {
		return globalInfo;
	}

	public void forEachFile(ObjIntConsumer<FileInfo> action) {
		fileInfos.int2ObjectEntrySet().forEach(entry -> {
			action.accept(entry.getValue(), entry.getIntKey());
		});
	}

	public void forEachValidFile(ObjIntConsumer<FileInfo> action) {
		fileInfos.int2ObjectEntrySet().forEach(entry -> {
			if(entry.getValue().isValid()) {
				action.accept(entry.getValue(), entry.getIntKey());
			}
		});
	}

	public FileInfo getFileInfo(int fileIndex) {
		FileInfo info = fileInfos.get(fileIndex);
		if(info==null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"No info available for index: "+fileIndex);

		return info;
	}

	public LayerInfo getLayerInfo(LayerManifest<?> layerManifest) {
		LayerInfo info = layerInfos.get(layerManifest.getUID());
		if(info==null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"No info available for layer: "+layerManifest.getUniqueId());

		return info;
	}

	public ChunkIndexInfo getChunkIndexInfo(LayerManifest<?> layerManifest) {
		ChunkIndexInfo info = chunkIndexInfos.get(layerManifest.getUID());
		if(info==null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"No info available for chunk index of layer: "+layerManifest.getUniqueId());

		return info;
	}

	private static boolean verify(MetadataRegistry registry, String key, long value) {
		long savedValue = registry.getLongValue(key, UNSET_LONG);
		return savedValue==UNSET_LONG || savedValue==value;
	}

	/**
	 * Basic info type that only provides a set of {@link ElementFlag flags}
	 * and simple properties for the associated resource.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public abstract static class ElementInfo {
		private EnumSet<ElementFlag> state = EnumSet.noneOf(ElementFlag.class);

//		private Map<String, String> properties;

//		public String getProperty(String key) {
//			return properties==null ? null : properties.get(key);
//		}
//
//		public void setProperty(String key, String value) {
//			if(value==null) {
//				return;
//			}
//
//			if(properties==null) {
//				properties = new Object2ObjectOpenHashMap<>();
//			}
//
//			properties.put(key, value);
//		}

		public void updateFlag(ElementFlag flag, boolean active) {
			if(active) {
				state.add(flag);
			} else {
				state.remove(flag);
			}
		}

		public void setFlag(ElementFlag flag) {
			if(flag==ElementFlag.CORRUPTED) {
				System.out.println();
			}
			state.add(flag);
		}

		public void unsetFlag(ElementFlag flag) {
			state.remove(flag);
		}

		public boolean isFlagSet(ElementFlag flag) {
			return state.contains(flag);
		}

		public boolean isAnyFlagSet(ElementFlag...flags) {
			for(ElementFlag flag : flags) {
				if(isFlagSet(flag)) {
					return true;
				}
			}

			return false;
		}

		public boolean isNoFlagSet(ElementFlag...flags) {
			for(ElementFlag flag : flags) {
				if(isFlagSet(flag)) {
					return false;
				}
			}

			return true;
		}

		public boolean isValid() {
			return !state.contains(ElementFlag.CORRUPTED)
					&& !state.contains(ElementFlag.MISSING)
					&& !state.contains(ElementFlag.UNUSABLE);
		}

		public String states2String() {
			return state.toString();
		}
	}

	/** Manages data for {@link DriverKey} entries. */
	public class GlobalInfo extends ElementInfo implements Syncable<MetadataRegistry> {

		private long size = UNSET_LONG;


		@Override
		public void syncTo(MetadataRegistry registry) {
			registry.changeLongValue(DriverKey.SIZE.getKey(), size, UNSET_LONG);
		}

		@Override
		public void syncFrom(MetadataRegistry registry) {
			setSize(registry.getLongValue(DriverKey.SIZE.getKey(), UNSET_LONG));
		}

		public boolean verifySize(MetadataRegistry registry) {
			return verify(registry, DriverKey.SIZE.getKey(), size);
		}

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}
	}

	/** Manages data for {@link FileKey} entries. */
	public class FileInfo extends ElementInfo implements Syncable<MetadataRegistry> {

		private final int index;
		private Path path = null;

		private FileChecksum checksum = null;
		private long size = UNSET_LONG;

		private Int2ObjectMap<LayerCoverage> stats = new Int2ObjectOpenHashMap<>();

		private FileInfo(int index) {
			this.index = index;
		}

		@Override
		public void syncTo(MetadataRegistry registry) {
			registry.setValue(FileKey.PATH.getKey(index), path==null ? null : path.toString());
			registry.setValue(FileKey.CHECKSUM.getKey(index), checksum==null ? null : checksum.toString());
			registry.changeLongValue(FileKey.SIZE.getKey(index), size, UNSET_LONG);
			registry.setBooleanValue(FileKey.SCANNED.getKey(index), isFlagSet(ElementFlag.SCANNED));

			for (int i = 0; i < layers.length; i++) {
				LayerCoverage coverage = getCoverage(layers[i], false);
				if(coverage==null) {
					registry.setValue(FileKey.ITEMS.getKey(index), null);
					registry.setValue(FileKey.BEGIN.getKey(index), null);
					registry.setValue(FileKey.END.getKey(index), null);
				} else {
					registry.changeLongValue(FileKey.ITEMS.getKey(index), coverage.count, UNSET_LONG);
					registry.changeLongValue(FileKey.BEGIN.getKey(index), coverage.first, UNSET_INT);
					registry.changeLongValue(FileKey.END.getKey(index), coverage.last, UNSET_INT);
				}
			}
		}

		@Override
		public void syncFrom(MetadataRegistry registry) {
			path = null;
			checksum = null;

			Optional.ofNullable(registry.getValue(FileKey.PATH.getKey(index))).map(Paths::get).ifPresent(this::setPath);
			Optional.ofNullable(registry.getValue(FileKey.CHECKSUM.getKey(index))).map(FileChecksum::parse).ifPresent(this::setChecksum);
			setSize(registry.getLongValue(FileKey.SIZE.getKey(index), UNSET_LONG));
			updateFlag(ElementFlag.SCANNED, registry.getBooleanValue(FileKey.SCANNED.getKey(index), false));

			for (int i = 0; i < layers.length; i++) {
				long count = registry.getLongValue(FileKey.ITEMS.getKey(index), UNSET_LONG);
				long first = registry.getLongValue(FileKey.BEGIN.getKey(index), UNSET_LONG);
				long last = registry.getLongValue(FileKey.END.getKey(index), UNSET_LONG);

				if(count==UNSET_LONG && first==UNSET_LONG && last==UNSET_LONG) {
					removeCoverage(layers[i]);
				} else {
					LayerCoverage coverage = getCoverage(layers[i], true);
					coverage.count = count;
					coverage.first = first;
					coverage.last = last;
				}
			}
		}

		public int getIndex() {
			return index;
		}

		public Path getPath() {
			return path;
		}

		public FileChecksum getChecksum() {
			return checksum;
		}

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}

		public void setPath(Path file) {
			this.path = requireNonNull(file);
		}

		public void setChecksum(FileChecksum checksum) {
			this.checksum = requireNonNull(checksum);
		}

		private LayerCoverage getCoverage(ItemLayerManifestBase<?> layer, boolean createIfMissing) {
			int key = layer.getUID();
			LayerCoverage cov = stats.get(key);

			if(cov==null && createIfMissing) {
				cov = new LayerCoverage();
				stats.put(key, cov);
			}

			return cov;
		}

		private void removeCoverage(ItemLayerManifestBase<?> layer) {
			LayerCoverage coverage = stats.remove(layer.getUID());
			if(coverage!=null) {
				coverage.clear();
			}
		}

		public long getItemCount(ItemLayerManifestBase<?> layer) {
			LayerCoverage cov = getCoverage(layer, false);
			return cov==null ? UNSET_LONG : cov.count;
		}

		public long getBeginIndex(ItemLayerManifestBase<?> layer) {
			LayerCoverage cov = getCoverage(layer, false);
			return cov==null ? UNSET_LONG : cov.first;
		}

		public long getEndIndex(ItemLayerManifestBase<?> layer) {
			LayerCoverage cov = getCoverage(layer, false);
			return cov==null ? UNSET_LONG : cov.last;
		}

		public void setItemCount(ItemLayerManifestBase<?> layer, long itemCount) {
			getCoverage(layer, true).count = itemCount;
		}

		public void setBeginIndex(ItemLayerManifestBase<?> layer, long beginIndex) {
			getCoverage(layer, true).first = beginIndex;
		}

		public void setEndIndex(ItemLayerManifestBase<?> layer, long endIndex) {
			getCoverage(layer, true).last = endIndex;
		}

		public void setCoverage(ItemLayerManifestBase<?> layer, long itemCount, long beginIndex, long endIndex) {
			LayerCoverage coverage = getCoverage(layer, true);
			coverage.count = itemCount;
			coverage.first = beginIndex;
			coverage.last = endIndex;
		}
	}

	private static class LayerCoverage {
		long first = UNSET_LONG, last = UNSET_LONG, count = UNSET_LONG;

		void clear() {
			first = last = count = UNSET_LONG;
		}
	}

	/** Manages data for {@link ItemLayerKey} entries. */
	public static class LayerInfo extends ElementInfo implements Syncable<MetadataRegistry> {

		private final ItemLayerManifestBase<?> layer;

		private final Int2ObjectMap<ContainerInfo> containerInfos = new Int2ObjectOpenHashMap<>();

		// Total number of elements in top-level container
		private long size = UNSET_LONG;

		/*
		 *  We use "true" as default value to catch an explicitly saved value
		 *  of "false". This way if no entry exists for this key we automatically
		 *  continue to creating the respective metadata later during driver connection.
		 */
		private boolean useChunkIndex = true;

		public LayerInfo(ItemLayerManifestBase<?> layer) {
			this.layer = requireNonNull(layer);

			layer.getContainerHierarchy().ifPresent(h -> h.forEachItem((container, level) -> {
				containerInfos.put(container.getUID(), new ContainerInfo(container, level));
			}));
		}

		@Override
		public void syncTo(MetadataRegistry registry) {
			registry.changeLongValue(ItemLayerKey.ITEMS.getKey(layer), size, UNSET_LONG);
			registry.setBooleanValue(ItemLayerKey.SCANNED.getKey(layer), isFlagSet(ElementFlag.SCANNED));
			registry.setBooleanValue(ItemLayerKey.USE_CHUNK_INDEX.getKey(layer), useChunkIndex);

			//TODO
		}

		@Override
		public void syncFrom(MetadataRegistry registry) {
			setSize(registry.getLongValue(ItemLayerKey.ITEMS.getKey(layer), UNSET_LONG));
			updateFlag(ElementFlag.SCANNED, registry.getBooleanValue(ItemLayerKey.SCANNED.getKey(layer), false));
			setUseChunkIndex(registry.getBooleanValue(ItemLayerKey.USE_CHUNK_INDEX.getKey(layer), true));
			//TODO
		}

		public ItemLayerManifestBase<?> getLayer() {
			return layer;
		}

		// TOTAL SIZE

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}

		// CHUNK INDEX

		public boolean isUseChunkIndex() {
			return useChunkIndex;
		}

		public void setUseChunkIndex(boolean useChunkIndex) {
			this.useChunkIndex = useChunkIndex;
		}

		public ContainerInfo getRootContainerInfo() {
			return getContainerInfo(layer.getContainerHierarchy().map(Hierarchy::getRoot)
					.orElseThrow(ManifestException.noElement(layer, "container")));
		}

		public ContainerInfo getContainerInfo(ContainerManifestBase<?> container) {
			ContainerInfo info = containerInfos.get(container.getUID());
			if(info==null)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT,
						"No info available for container: "+container.getUniqueId());

			return info;
		}
	}

	private static final ContainerType[] c_types = ContainerType.values();
	private static final StructureType[] s_types = StructureType.values();

	public static class NumericalStats {
		private long min = UNSET_LONG;
		private long max = UNSET_LONG;
		private double avg = UNSET_DOUBLE;
		public long getMin() {
			return min;
		}
		public long getMax() {
			return max;
		}
		public double getAvg() {
			return avg;
		}
		public void setMin(long min) {
			this.min = min;
		}
		public void setMax(long max) {
			this.max = max;
		}
		public void setAvg(double avg) {
			this.avg = avg;
		}
		public void copyFrom(Histogram source) {
			clear();
			if(source.entries()>0L) {
				min = source.min();
				max = source.max();
				avg = source.average();
			}
		}
		void clear() {
			min = max = UNSET_LONG;
			avg = UNSET_DOUBLE;
		}
		boolean isUndefined() {
			return min==UNSET_LONG && max==UNSET_LONG && avg==UNSET_DOUBLE;
		}
	}

	public static class ContainerInfo implements Syncable<MetadataRegistry> {

		private final ContainerManifestBase<?> container;
		private final int level;

		private LongCounter<ContainerType> containerTypeCount;
		private LongCounter<StructureType> structureTypeCount;
		// Container stats
		private NumericalStats itemCount;
		private NumericalStats spanSize;
		// Structure stats
		private NumericalStats edgeCount;
		private NumericalStats height;
		private NumericalStats branching;
		private NumericalStats roots;

		public ContainerInfo(ContainerManifestBase<?> container, int level) {
			this.container = container;
			this.level = level;
		}

		@Override
		public void syncTo(MetadataRegistry registry) {
			ItemLayerManifestBase<?> layer = ManifestUtils.requireHost(container);

			for (int i = 0; i < c_types.length; i++) {
				registry.changeLongValue(ContainerKey.COUNT.getKey(layer, level, c_types[i]),
						getCountForContainerType(c_types[i]), 0L);
			}

			syncStatsTo(registry, layer, itemCount, ContainerKey.MIN_ITEM_COUNT, ContainerKey.MAX_ITEM_COUNT, ContainerKey.AVG_ITEM_COUNT);
			syncStatsTo(registry, layer, spanSize, ContainerKey.MIN_SPAN, ContainerKey.MAX_SPAN, ContainerKey.AVG_SPAN);

			if(ManifestUtils.isStructureLayerManifest(layer)) {
				StructureLayerManifest s_layer = (StructureLayerManifest)layer;

				for (int i = 0; i < s_types.length; i++) {
					registry.changeLongValue(StructureKey.COUNT.getKey(s_layer, level, s_types[i]),
							getCountForStructureType(s_types[i]), 0L);
				}

				syncStatsTo(registry, s_layer, edgeCount, StructureKey.MIN_EDGE_COUNT, StructureKey.MAX_EDGE_COUNT, StructureKey.AVG_EDGE_COUNT);
				syncStatsTo(registry, s_layer, height, StructureKey.MIN_HEIGHT, StructureKey.MAX_HEIGHT, StructureKey.AVG_HEIGHT);
				syncStatsTo(registry, s_layer, branching, StructureKey.MIN_BRANCHING_FACTOR, StructureKey.MAX_BRANCHING_FACTOR, StructureKey.AVG_BRANCHING_FACTOR);
				syncStatsTo(registry, s_layer, roots, StructureKey.MIN_ROOTS, StructureKey.MAX_ROOTS, StructureKey.AVG_ROOTS);
			}
		}

		private <L extends ItemLayerManifestBase<?>> void syncStatsTo(MetadataRegistry registry,
				L layer, NumericalStats stats,
				ContainerKeyBase<L> MIN, ContainerKeyBase<L> MAX, ContainerKeyBase<L> AVG) {
			if(stats==null || stats.isUndefined()) {
				registry.setValue(MIN.getKey(layer, level), null);
				registry.setValue(MAX.getKey(layer, level), null);
				registry.setValue(AVG.getKey(layer, level), null);
			} else {
				registry.changeLongValue(MIN.getKey(layer, level), stats.getMin(), UNSET_LONG);
				registry.changeLongValue(MAX.getKey(layer, level), stats.getMax(), UNSET_LONG);
				registry.changeDoubleValue(AVG.getKey(layer, level), stats.getAvg(), UNSET_DOUBLE);
			}
		}

		@Override
		public void syncFrom(MetadataRegistry registry) {
			ItemLayerManifestBase<?> layer = ManifestUtils.requireHost(container);

			for (int i = 0; i < c_types.length; i++) {
				setCountForContainerType(c_types[i], registry.getLongValue(ContainerKey.COUNT.getKey(layer, level, c_types[i]), 0));
			}

			itemCount = syncStatsFrom(registry, layer, itemCount, ContainerKey.MIN_ITEM_COUNT, ContainerKey.MAX_ITEM_COUNT, ContainerKey.AVG_ITEM_COUNT);
			spanSize = syncStatsFrom(registry, layer, spanSize, ContainerKey.MIN_SPAN, ContainerKey.MAX_SPAN, ContainerKey.AVG_SPAN);

			if(ManifestUtils.isStructureLayerManifest(layer)) {
				StructureLayerManifest s_layer = (StructureLayerManifest)layer;

				for (int i = 0; i < s_types.length; i++) {
					setCountForStructureType(s_types[i], registry.getLongValue(StructureKey.COUNT.getKey(s_layer, level, s_types[i]), 0));
				}

				edgeCount = syncStatsFrom(registry, s_layer, edgeCount, StructureKey.MIN_EDGE_COUNT, StructureKey.MAX_EDGE_COUNT, StructureKey.AVG_EDGE_COUNT);
				height = syncStatsFrom(registry, s_layer, height, StructureKey.MIN_HEIGHT, StructureKey.MAX_HEIGHT, StructureKey.AVG_HEIGHT);
				branching = syncStatsFrom(registry, s_layer, branching, StructureKey.MIN_BRANCHING_FACTOR, StructureKey.MAX_BRANCHING_FACTOR, StructureKey.AVG_BRANCHING_FACTOR);
				roots = syncStatsFrom(registry, s_layer, roots, StructureKey.MIN_ROOTS, StructureKey.MAX_ROOTS, StructureKey.AVG_ROOTS);
			}

		}

		private <L extends ItemLayerManifestBase<?>> NumericalStats syncStatsFrom(MetadataRegistry registry,
				L layer, NumericalStats stats,
				ContainerKeyBase<L> MIN, ContainerKeyBase<L> MAX, ContainerKeyBase<L> AVG) {
			long min = registry.getLongValue(MIN.getKey(layer, level), UNSET_LONG);
			long max = registry.getLongValue(MAX.getKey(layer, level), UNSET_LONG);
			double avg = registry.getDoubleValue(AVG.getKey(layer, level), UNSET_DOUBLE);

			if(min!=UNSET_LONG || max!=UNSET_LONG || Double.compare(avg, UNSET_DOUBLE)!=0) {
				if(stats==null) {
					stats = new NumericalStats();
				}
				stats.min = min;
				stats.max = max;
				stats.avg = avg;
			}

			return stats;
		}

		// CONTAINER TYPE

		private LongCounter<ContainerType> containerTypeCount() {
			if(containerTypeCount==null) {
				containerTypeCount = new LongCounter<>();
			}
			return containerTypeCount;
		}

		public long getCountForContainerType(ContainerType type) {
			requireNonNull(type);
			return containerTypeCount==null ? 0L : containerTypeCount.getCount(type);
		}

		public void setCountForContainerType(ContainerType type, long count) {
			requireNonNull(type);
			containerTypeCount().setCount(type, count);
		}

		public void addCountForContainerType(ContainerType type, long count) {
			requireNonNull(type);
			containerTypeCount().add(type, count);
		}

		public void addCountsForContainerTypes(LongCounter<ContainerType> counts) {
			requireNonNull(counts);
			if(!counts.isEmpty()) {
				containerTypeCount().addAll(counts);
			}
		}

		public Set<ContainerType> getEncounteredContainerTypes() {
			Set<ContainerType> result = Collections.emptySet();
			if(containerTypeCount!=null && !containerTypeCount.isEmpty()) {
				result = containerTypeCount.getItems();
			}
			return result;
		}

		// STRUCTURE TYPE

		private LongCounter<StructureType> structureTypeCount() {
			if(structureTypeCount==null) {
				structureTypeCount = new LongCounter<>();
			}
			return structureTypeCount;
		}

		public long getCountForStructureType(StructureType type) {
			requireNonNull(type);
			return structureTypeCount==null ? 0L : structureTypeCount.getCount(type);
		}

		public void setCountForStructureType(StructureType type, long count) {
			requireNonNull(type);
			structureTypeCount().setCount(type, count);
		}

		public void addCountForStructureType(StructureType type, long count) {
			requireNonNull(type);
			structureTypeCount().add(type, count);
		}

		public Set<StructureType> getEncounteredStructureTypes() {
			Set<StructureType> result = Collections.emptySet();
			if(structureTypeCount!=null && !structureTypeCount.isEmpty()) {
				result = structureTypeCount.getItems();
			}
			return result;
		}

		public void addCountsForStructureTypes(LongCounter<StructureType> counts) {
			requireNonNull(counts);
			if(!counts.isEmpty()) {
				structureTypeCount().addAll(counts);
			}
		}

		// ITEM COUNTS

		/**
		 * Fetches the stats for counting the {@link Container#getItemCount() size}
		 * of containers.
		 *
		 * @return
		 */
		public NumericalStats getItemCountStats() {
			if(itemCount==null) {
				itemCount = new NumericalStats();
			}

			return itemCount;
		}

		// SPAN SIZE

		/**
		 * Fetches the stats for tracking the span size of containers.
		 * The span size is the area covered by a container's {@link Container#getBeginOffset() begin}
		 * and {@link Container#getEndOffset() end} offsets.
		 *
		 * @return
		 */
		public NumericalStats getSpanSizeStats() {
			if(spanSize==null) {
				spanSize = new NumericalStats();
			}

			return spanSize;
		}

		// EDGE COUNTS

		/**
		 * Fetches the stats for counting the {@link Structure#getEdgeCount()number of edges}
		 * of structures.
		 *
		 * @return
		 */
		public NumericalStats getEdgeCountStats() {
			if(edgeCount==null) {
				edgeCount = new NumericalStats();
			}

			return edgeCount;
		}

		// BRANCHING

		/**
		 * Fetches the stats for tracking the branching factor of structures.
		 *
		 * @return
		 */
		public NumericalStats getBranchingStats() {
			if(branching==null) {
				branching = new NumericalStats();
			}

			return branching;
		}

		// HEIGHT

		/**
		 * Fetches the stats for tracking the {@link Structure#getHeight() height}
		 * of structures.
		 *
		 * @return
		 */
		public NumericalStats getHeightStats() {
			if(height==null) {
				height = new NumericalStats();
			}

			return height;
		}

		// ROOTS

		/**
		 * Fetches the stats for tracking the number of {@link Structure#isRoot(Item) root}
		 * nodes of structures.
		 *
		 * @return
		 */
		public NumericalStats getRootStats() {
			if(roots==null) {
				roots = new NumericalStats();
			}

			return roots;
		}

	}

	/** Manages data for {@link ChunkIndexKey} entries. */
	public class ChunkIndexInfo implements Syncable<MetadataRegistry> {

		private final ItemLayerManifestBase<?> layer;

		private Path path = null;
		private IndexValueType valueType = null;
		private int blockPower = UNSET_INT;
		private String blockCache = null;
		private int cacheSize = UNSET_INT;
		private int minChunkSize = UNSET_INT;
		private int maxChunkSize = UNSET_INT;

		public ChunkIndexInfo(ItemLayerManifestBase<?> layer) {
			this.layer = layer;
		}

		@Override
		public void syncTo(MetadataRegistry registry) {
			registry.setValue(ChunkIndexKey.PATH.getKey(layer), path==null ? null : path.toString());
			registry.setValue(ChunkIndexKey.BLOCK_CACHE.getKey(layer), blockCache==null ? null : blockCache.toString());
			registry.setValue(ChunkIndexKey.VALUE_TYPE.getKey(layer), valueType==null ? null : valueType.getStringValue());
			registry.changeIntValue(ChunkIndexKey.BLOCK_POWER.getKey(layer), blockPower, UNSET_INT);
			registry.changeIntValue(ChunkIndexKey.CACHE_SIZE.getKey(layer), cacheSize, UNSET_INT);
			registry.changeIntValue(ChunkIndexKey.MIN_CHUNK_SIZE.getKey(layer), minChunkSize, UNSET_INT);
			registry.changeIntValue(ChunkIndexKey.MAX_CHUNK_SIZE.getKey(layer), maxChunkSize, UNSET_INT);
		}

		@Override
		public void syncFrom(MetadataRegistry registry) {
			path = null;
			valueType = null;
			blockCache = null;

			Optional.ofNullable(registry.getValue(ChunkIndexKey.PATH.getKey(layer))).map(Paths::get).ifPresent(this::setPath);
			Optional.ofNullable(registry.getValue(ChunkIndexKey.BLOCK_CACHE.getKey(layer))).ifPresent(this::setBlockCache);
			Optional.ofNullable(registry.getValue(ChunkIndexKey.VALUE_TYPE.getKey(layer))).map(IndexValueType::parseIndexValueType).ifPresent(this::setValueType);

			setBlockPower(registry.getIntValue(ChunkIndexKey.BLOCK_POWER.getKey(layer), UNSET_INT));
			setCacheSize(registry.getIntValue(ChunkIndexKey.CACHE_SIZE.getKey(layer), UNSET_INT));
			setMinChunkSize(registry.getIntValue(ChunkIndexKey.MIN_CHUNK_SIZE.getKey(layer), UNSET_INT));
			setMaxChunkSize(registry.getIntValue(ChunkIndexKey.MAX_CHUNK_SIZE.getKey(layer), UNSET_INT));
		}

		public Path getPath() {
			return path;
		}

		public IndexValueType getValueType() {
			return valueType;
		}

		public int getBlockPower() {
			return blockPower;
		}

		public String getBlockCache() {
			return blockCache;
		}

		public int getCacheSize() {
			return cacheSize;
		}

		public int getMinChunkSize() {
			return minChunkSize;
		}

		public int getMaxChunkSize() {
			return maxChunkSize;
		}

		public void setPath(Path path) {
			this.path = path;
		}

		public void setValueType(IndexValueType valueType) {
			this.valueType = requireNonNull(valueType);
		}

		public void setBlockPower(int blockPower) {
			this.blockPower = blockPower;
		}

		public void setBlockCache(String blockCache) {
			this.blockCache = requireNonNull(blockCache);
		}

		public void setCacheSize(int cacheSize) {
			this.cacheSize = cacheSize;
		}

		public void setMinChunkSize(int minChunkSize) {
			this.minChunkSize = minChunkSize;
		}

		public void setMaxChunkSize(int maxChunkSize) {
			this.maxChunkSize = maxChunkSize;
		}

	}
}
