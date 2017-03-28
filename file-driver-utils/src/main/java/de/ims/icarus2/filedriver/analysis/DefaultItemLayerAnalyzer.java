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
package de.ims.icarus2.filedriver.analysis;

import static de.ims.icarus2.util.classes.Primitives._int;
import static de.ims.icarus2.util.strings.StringUtil.getName;
import static java.util.Objects.requireNonNull;

import java.util.function.LongConsumer;

import de.ims.icarus2.filedriver.FileDataStates;
import de.ims.icarus2.filedriver.FileDataStates.FileInfo;
import de.ims.icarus2.filedriver.FileDataStates.LayerInfo;
import de.ims.icarus2.filedriver.FileDataStates.NumericalStats;
import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.LongCounter;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultItemLayerAnalyzer extends AbstractFileDriverAnalyzer implements ItemLayerAnalyzer, ModelConstants {

	/**
	 * Layer this analyzer is collecting information for
	 */
	private final ItemLayer layer;

	/**
	 * Index of file (if multiple) the data is being extracted from.
	 * This is relevant for writing back coverage information into
	 * the matching {@link FileInfo} object.
	 */
	protected final int fileIndex;

	/**
	 * Total number of elements encountered for given layer during
	 * analysis. Does not take into account numbers from other files.
	 */
	private long elementCount = 0L;

	/**
	 * Total number of containers encountered for given layer during
	 * analysis. Does not take into account numbers from other files.
	 *
	 * Summed up {@link Container#getItemCount() size} of all containers
	 * in the given layer during analysis.
	 *
	 * Smallest {@link Container#getItemCount() size} of a container
	 * encountered during analysis.
	 *
	 * Largest {@link Container#getItemCount() size} of a container
	 * encountered during analysis.
	 */
	private final StatsBuffer containerSizes = new StatsBuffer();

	private final StatsBuffer spanSizes = new StatsBuffer();

	/**
	 * Individual counts for every {@link ContainerType type} of
	 * container encountered during analysis.
	 */
	private LongCounter<ContainerType> containerTypeCount = new LongCounter<>();

	public DefaultItemLayerAnalyzer(FileDataStates states, ItemLayer layer, int fileIndex) {
		super(states);

		this.layer = requireNonNull(layer);
		this.fileIndex = fileIndex;
	}

	protected ItemLayer getLayer() {
		return layer;
	}

	/**
	 * @see de.ims.icarus2.filedriver.analysis.AbstractFileDriverAnalyzer#writeStates(de.ims.icarus2.filedriver.FileDataStates)
	 */
	@Override
	protected void writeStates(FileDataStates states) {
		ItemLayerManifest layerManifest = layer.getManifest();

		// Refresh file info
		FileInfo fileInfo = states.getFileInfo(fileIndex);
		long beginIndex = fileInfo.getBeginIndex(layerManifest);
		if(beginIndex==UNSET_LONG) {
			if(fileIndex==0) {
				beginIndex = 0L;
			} else {
				FileInfo previousInfo = states.getFileInfo(fileIndex-1);
				long previousEndIndex = previousInfo.getEndIndex(layerManifest);
				if(previousEndIndex==UNSET_LONG)
					throw new ManifestException(ModelErrorCode.DRIVER_METADATA_CORRUPTED,
							String.format("Missing information of end index for layer %s in file %d",
									getName(layerManifest), _int(fileIndex)));

				beginIndex = previousEndIndex+1;
			}
		}
		long endIndex = beginIndex + elementCount;

		fileInfo.setCoverage(layerManifest, elementCount, beginIndex, endIndex);

		// Refresh layer info
		LayerInfo layerInfo = states.getLayerInfo(layer.getManifest());
		layerInfo.setSize(layerInfo.getSize() + elementCount);

		containerSizes.saveTo(layerInfo.getItemCountStats());

		if(!containerTypeCount.isEmpty()) {
			layerInfo.addCountsForContainerTypes(containerTypeCount);
		}

		spanSizes.saveTo(layerInfo.getSpanSizeStats());
	}

	/**
	 * @see java.util.function.ObjLongConsumer#accept(java.lang.Object, long)
	 */
	@Override
	public void accept(Item item, long index) {
		elementCount++;

		if(ModelUtils.isContainerOrStructure(item)) {
			Container container = (Container) item;
			containerSizes.accept(container.getItemCount());

			long spanSize = container.getSpan();
			if(spanSize!=UNSET_LONG) {
				spanSizes.accept(spanSize);
			}

			containerTypeCount.increment(container.getContainerType());
		}
	}

	public static class StatsBuffer implements LongConsumer {
		private long sum = 0L;
		private long count = 0L;
		private long min = Long.MAX_VALUE;
		private long max = Long.MIN_VALUE;

		/**
		 * @see java.util.function.LongConsumer#accept(long)
		 */
		@Override
		public void accept(long value) {
			if(value<0) { // Takes care of UNSET_LONG and any other invalid values
				return;
			}

			count++;
			sum += value;
			min = Math.min(min, value);
			max = Math.max(max, value);
		}

		public long getCount() {
			return count;
		}

		public boolean isEmpty() {
			return count==0L;
		}

		public void saveTo(NumericalStats target) {
			if(isEmpty()) {
				target.reset();
			} else {
				target.setMin(min);
				target.setMax(max);
				target.setAvg(sum/(double)count);
			}
		}
	}
}
