/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import de.ims.icarus2.filedriver.FileDataStates.ContainerInfo;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.LongCounter;
import de.ims.icarus2.util.stat.Histogram;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultStructureLayerAnalyzer extends DefaultItemLayerAnalyzer {

	private static class StructureStats extends ContainerStats {

		private Histogram structureSizes = Histogram.openHistogram(100);
		private Histogram rootCounts = Histogram.openHistogram(100);
		private Histogram treeHeights = Histogram.openHistogram(100);
		private Histogram branching = Histogram.openHistogram(100);
		private LongCounter<StructureType> structureTypeCount = new LongCounter<>();

		protected StructureStats(StructureManifest manifest, int level) {
			super(manifest, level);
		}

	}

	public DefaultStructureLayerAnalyzer(FileDataStates states, StructureLayer layer, int fileIndex) {
		super(states, layer, fileIndex);
	}

	@Override
	protected StructureLayer getLayer() {
		return (StructureLayer) super.getLayer();
	}

	@Override
	protected ContainerStats createStats(ContainerManifestBase<?> manifest, int level) {
		if(ManifestUtils.isStructureManifest(manifest)) {
			return new StructureStats((StructureManifest) manifest, level);
		}
		return super.createStats(manifest, level);
	}

	@Override
	protected void writeContainerStats(ContainerStats stats, ContainerInfo info) {
		super.writeContainerStats(stats, info);

		if(stats.getClass()==StructureStats.class) {
			StructureStats sstats = (StructureStats) stats;

			info.getEdgeCountStats().copyFrom(sstats.structureSizes);
			info.getRootStats().copyFrom(sstats.rootCounts);
			info.getHeightStats().copyFrom(sstats.treeHeights);
			info.getBranchingStats().copyFrom(sstats.branching);
		}
	}

	@Override
	protected void collectStats(Container container, ContainerStats stats) {
		super.collectStats(container, stats);

		if(ModelUtils.isStructure(container) && stats.getClass()==StructureStats.class) {
			Structure structure = (Structure) container;
			StructureStats sstats = (StructureStats) stats;

			sstats.structureSizes.accept(structure.getEdgeCount());
			sstats.rootCounts.accept(structure.getOutgoingEdgeCount(structure.getVirtualRoot()));

			// Refresh info flag for height checks
			boolean includeHeightStats = isHeightAwareStructure(structure);

			// Collect per-node info
			structure.forEachNode((s, node) -> {
				sstats.branching.accept(structure.getOutgoingEdgeCount(node));
				if(includeHeightStats) {
					sstats.treeHeights.accept(structure.getHeight(node));
				}
			});

			sstats.structureTypeCount.increment(structure.getStructureType());
		}
	}

	private static boolean isHeightAwareStructure(Structure structure) {
		return structure.getStructureType()==StructureType.TREE
				|| structure.getStructureType()==StructureType.CHAIN;
	}
}
