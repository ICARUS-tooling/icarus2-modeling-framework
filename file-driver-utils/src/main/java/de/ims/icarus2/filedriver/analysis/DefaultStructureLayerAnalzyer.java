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

import de.ims.icarus2.filedriver.FileDataStates;
import de.ims.icarus2.filedriver.FileDataStates.LayerInfo;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.LongCounter;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultStructureLayerAnalzyer extends DefaultItemLayerAnalyzer {

	private StatsBuffer structureSizes = new StatsBuffer();
	private StatsBuffer rootCounts = new StatsBuffer();

	private StatsBuffer treeHeights = new StatsBuffer();
	private StatsBuffer branching = new StatsBuffer();

	private LongCounter<StructureType> structureTypeCount = new LongCounter<>();

	public DefaultStructureLayerAnalzyer(FileDataStates states, StructureLayer layer, int fileIndex) {
		super(states, layer, fileIndex);
	}

	/**
	 * @see de.ims.icarus2.filedriver.analysis.DefaultItemLayerAnalyzer#getLayer()
	 */
	@Override
	protected StructureLayer getLayer() {
		return (StructureLayer) super.getLayer();
	}

	/**
	 * @see de.ims.icarus2.filedriver.analysis.DefaultItemLayerAnalyzer#writeStates(de.ims.icarus2.filedriver.FileDataStates)
	 */
	@Override
	protected void writeStates(FileDataStates states) {
		super.writeStates(states);

		LayerInfo layerInfo = states.getLayerInfo(getLayer().getManifest());

		structureSizes.saveTo(layerInfo.getEdgeCountStats());
		rootCounts.saveTo(layerInfo.getRootStats());
		treeHeights.saveTo(layerInfo.getHeightStats());
		branching.saveTo(layerInfo.getBranchingStats());
	}

	/**
	 * Flag used during the traversal of a structure's nodes
	 */
	private boolean includeHeightStats = false;

	/**
	 * @see java.util.function.ObjLongConsumer#accept(java.lang.Object, long)
	 */
	@Override
	public void accept(Item item, long index) {
		super.accept(item, index);

		if(ModelUtils.isStructure(item)) {
			Structure structure = (Structure) item;

			structureSizes.accept(structure.getEdgeCount());
			rootCounts.accept(structure.getOutgoingEdgeCount(structure.getVirtualRoot()));

			// Refresh info flag for height checks
			includeHeightStats = isHeightAwareStructure(structure);

			// Collect per-node info
			structure.forEachNode(this::collectNodeInfo);

			structureTypeCount.increment(structure.getStructureType());
		}
	}

	private static boolean isHeightAwareStructure(Structure structure) {
		return structure.getStructureType()==StructureType.TREE
				|| structure.getStructureType()==StructureType.CHAIN;
	}

	private void collectNodeInfo(Structure structure, Item node) {
		branching.accept(structure.getOutgoingEdgeCount(node));
		if(includeHeightStats) {
			treeHeights.accept(structure.getHeight(node));
		}
	}
}
