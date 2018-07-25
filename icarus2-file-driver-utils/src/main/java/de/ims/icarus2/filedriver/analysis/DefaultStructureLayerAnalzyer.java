/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
