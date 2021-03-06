/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.ObjIntConsumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.io.sets.ResourceSet;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.LongCounter;
import de.ims.icarus2.util.stat.Histogram;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * Centralized storage of (virtual) metadata for resources managed by
 * a {@link FileDriver}.
 *
 * Not thread-safe!
 *
 * @author Markus Gärtner
 *
 */
public class FileDataStates {

	private final ElementInfo globalInfo = new ElementInfo();

	// File states and meta info
	private final Int2ObjectMap<FileInfo> fileInfos = new Int2ObjectOpenHashMap<>();

	// Layer states and meta info
	private final Int2ObjectMap<LayerInfo> layerInfos = new Int2ObjectOpenHashMap<>();

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

		// Collect data files
		ResourceSet dataFiles = driver.getDataFiles();
		for(int fileIndex=0; fileIndex<dataFiles.getResourceCount(); fileIndex++) {
			fileInfos.put(fileIndex, new FileInfo(fileIndex));
		}

		// Collect layers
		ContextManifest contextManifest = ManifestUtils.requireHost(driver.getManifest());
		for(LayerManifest<?> layerManifest : contextManifest.getLayerManifests()) {

			if(ManifestUtils.isItemLayerManifest(layerManifest)) {
				layerInfos.put(layerManifest.getUID(), new LayerInfo((ItemLayerManifestBase<?>)layerManifest));
			}
		}

		//TODO populate other maps/lookups!!!
	}

	public ElementInfo getGlobalInfo() {
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
					"No info available for layer: "+layerManifest.getId());

		return info;
	}

	/**
	 * Basic info type that only provides a set of {@link ElementFlag flags}
	 * and simple properties for the associated resource.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class ElementInfo {
		private EnumSet<ElementFlag> state = EnumSet.noneOf(ElementFlag.class);
		private Map<String, String> properties;

		public String getProperty(String key) {
			return properties==null ? null : properties.get(key);
		}

		public void setProperty(String key, String value) {
			if(value==null) {
				return;
			}

			if(properties==null) {
				properties = new Object2ObjectOpenHashMap<>();
			}

			properties.put(key, value);
		}

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

	/**
	 * @author Markus Gärtner
	 *
	 */
	public static class FileInfo extends ElementInfo {

		private final int index;
		private Path path;

		private FileChecksum checksum;
		private long size;

		private Int2ObjectMap<LayerCoverage> stats = new Int2ObjectOpenHashMap<>();

		private FileInfo(int index) {
			this.index = index;
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
			this.path = file;
		}

		public void setChecksum(FileChecksum checksum) {
			this.checksum = checksum;
		}

		private LayerCoverage getCoverage(ItemLayerManifestBase<?> layer, boolean createIfMissing) {
			int key = layer.getUID();
			LayerCoverage cov = stats.get(key);

			if(cov==null && createIfMissing) {
				cov = new LayerCoverage(IcarusUtils.UNSET_LONG, IcarusUtils.UNSET_LONG, 0L);
				stats.put(key, cov);
			}

			return cov;
		}

		public long getItemCount(ItemLayerManifestBase<?> layer) {
			LayerCoverage cov = getCoverage(layer, false);
			return cov==null ? IcarusUtils.UNSET_LONG : cov.count;
		}

		public long getBeginIndex(ItemLayerManifestBase<?> layer) {
			LayerCoverage cov = getCoverage(layer, false);
			return cov==null ? IcarusUtils.UNSET_LONG : cov.first;
		}

		public long getEndIndex(ItemLayerManifestBase<?> layer) {
			LayerCoverage cov = getCoverage(layer, false);
			return cov==null ? IcarusUtils.UNSET_LONG : cov.last;
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
		long first, last, count;

		LayerCoverage(long first, long last, long count) {
			this.first = first;
			this.last = last;
			this.count = count;
		}
	}

	public static class NumericalStats {
		private long min = IcarusUtils.UNSET_LONG;
		private long max = IcarusUtils.UNSET_LONG;
		private double avg = IcarusUtils.UNSET_DOUBLE;
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
		public void reset() {
			min = IcarusUtils.UNSET_LONG;
			max = IcarusUtils.UNSET_LONG;
			avg = IcarusUtils.UNSET_DOUBLE;
		}
		public void copyFrom(Histogram source) {
			reset();
			if(source.entries()>0L) {
				min = source.min();
				max = source.max();
				avg = source.average();
			}
		}
	}

	public static class LayerInfo extends ElementInfo {
		private final ItemLayerManifestBase<?> layer;

		// Total number of elements in top-level container
		private long size = IcarusUtils.UNSET_LONG;

		private LongCounter<ContainerType> containerTypeCount;
		private LongCounter<StructureType> structureTypeCount;
		private NumericalStats itemCount, spanSize, edgeCount, height, branching, roots;

		public LayerInfo(ItemLayerManifestBase<?> layer) {
			this.layer = requireNonNull(layer);
		}

		public ItemLayerManifestBase<?> getLayer() {
			return layer;
		}

		// TOTAL SIZE

		public long getSize() {
			return size==IcarusUtils.UNSET_LONG ? 0L : size;
		}

		public void setSize(long size) {
			this.size = size;
		}

		// CONTAINER TYPE

		private LongCounter<ContainerType> containerTypeCount() {
			if(containerTypeCount==null) {
				containerTypeCount = new LongCounter<>();
			}
			return containerTypeCount;
		}

		public long getCountForContainerType(ContainerType type) {
			return containerTypeCount==null ? 0L : containerTypeCount.getCount(type);
		}

		public void setCountForContainerType(ContainerType type, long count) {
			containerTypeCount().setCount(type, count);
		}

		public void addCountForContainerType(ContainerType type, long count) {
			containerTypeCount().add(type, count);
		}

		public void addCountsForContainerTypes(LongCounter<ContainerType> counts) {
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
			return structureTypeCount==null ? 0L : structureTypeCount.getCount(type);
		}

		public void setCountForStructureType(StructureType type, long count) {
			structureTypeCount().setCount(type, count);
		}

		public void addCountForStructureType(StructureType type, long count) {
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
}
