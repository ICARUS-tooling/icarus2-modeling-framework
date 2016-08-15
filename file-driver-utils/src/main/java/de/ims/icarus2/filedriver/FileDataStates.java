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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.function.ObjIntConsumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.io.sets.FileSet;
import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.util.Counter;

/**
 * @author Markus Gärtner
 *
 */
public class FileDataStates {

	private final ElementInfo globalInfo = new ElementInfo();

	// File states and meta info
	private final TIntObjectMap<FileInfo> fileInfos = new TIntObjectHashMap<>();

	// Layer states and meta info
	private final TIntObjectMap<LayerInfo> layerInfos = new TIntObjectHashMap<>();

	public FileDataStates(FileDriver driver) {

		// Collect data files
		FileSet dataFiles = driver.getDataFiles();
		for(int fileIndex=0; fileIndex<dataFiles.getFileCount(); fileIndex++) {
			fileInfos.put(fileIndex, new FileInfo(fileIndex));
		}

//		List<LayerManifest> layers
		//FIXME

		//TODO populate other maps/lookups!!!
	}

	public ElementInfo getGlobalInfo() {
		return globalInfo;
	}

	public void forEachFile(ObjIntConsumer<FileInfo> action) {
		fileInfos.forEachEntry(new TIntObjectProcedure<FileInfo>() {

			@Override
			public boolean execute(int fileIndex, FileInfo info) {
				action.accept(info, fileIndex);
				return true;
			}
		});
	}

	public void forEachValidFile(ObjIntConsumer<FileInfo> action) {
		fileInfos.forEachEntry(new TIntObjectProcedure<FileInfo>() {

			@Override
			public boolean execute(int fileIndex, FileInfo info) {
				if(info.isValid()) {
					action.accept(info, fileIndex);
				}
				return true;
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

	public LayerInfo getLayerInfo(LayerManifest layer) {
		LayerInfo info = layerInfos.get(layer.getUID());
		if(info==null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"No info available for layer: "+layer.getId());

		return info;
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class ElementInfo implements ModelConstants {
		private EnumSet<ElementFlag> state = EnumSet.noneOf(ElementFlag.class);

		public void updateFlag(ElementFlag flag, boolean active) {
			if(active) {
				state.add(flag);
			} else {
				state.remove(flag);
			}
		}

		public void setFlag(ElementFlag flag) {
			state.add(flag);
		}

		public void unsetFlag(ElementFlag flag) {
			state.remove(flag);
		}

		public boolean isFlagSet(ElementFlag flag) {
			return state.contains(flag);
		}

		public boolean isValid() {
			return !state.contains(ElementFlag.CORRUPTED)
					&& !state.contains(ElementFlag.MISSING)
					&& !state.contains(ElementFlag.UNUSABLE);
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

		private TIntObjectMap<LayerCoverage> stats = new TIntObjectHashMap<>();

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

		public void setPath(Path file) {
			this.path = file;
		}

		public void setChecksum(FileChecksum checksum) {
			this.checksum = checksum;
		}

		private LayerCoverage getCoverage(ItemLayerManifest layer, boolean createIfMissing) {
			int key = layer.getUID();
			LayerCoverage cov = stats.get(key);

			if(cov==null && createIfMissing) {
				cov = new LayerCoverage(NO_INDEX, NO_INDEX, 0L);
				stats.put(key, cov);
			}

			return cov;
		}

		public long getItemCount(ItemLayerManifest layer) {
			LayerCoverage cov = getCoverage(layer, false);
			return cov==null ? 0L : cov.count;
		}

		public long getBeginIndex(ItemLayerManifest layer) {
			LayerCoverage cov = getCoverage(layer, false);
			return cov==null ? NO_INDEX : cov.first;
		}

		public long getEndIndex(ItemLayerManifest layer) {
			LayerCoverage cov = getCoverage(layer, false);
			return cov==null ? NO_INDEX : cov.last;
		}

		public void setItemCount(ItemLayerManifest layer, long itemCount) {
			getCoverage(layer, true).count = itemCount;
		}

		public void setBeginIndex(ItemLayerManifest layer, long beginIndex) {
			getCoverage(layer, true).first = beginIndex;
		}

		public void setEndIndex(ItemLayerManifest layer, long endIndex) {
			getCoverage(layer, true).last = endIndex;
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

	private static class NumericalStats {
		long min, max;
		double avg;
	}

	public static class LayerInfo extends ElementInfo {
		private final ItemLayerManifest layer;

		private long size = NO_INDEX;

		private Counter<ContainerType> containerTypeCount;
		private Counter<StructureType> structureTypeCount;
		private NumericalStats spanSize, itemCount, edgeCount, height, branching;

		public LayerInfo(ItemLayerManifest layer) {
			this.layer = layer;
		}

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}
	}
}
