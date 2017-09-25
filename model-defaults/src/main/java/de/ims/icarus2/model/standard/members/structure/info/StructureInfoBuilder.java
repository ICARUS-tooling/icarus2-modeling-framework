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
package de.ims.icarus2.model.standard.members.structure.info;

import static de.ims.icarus2.model.standard.members.structure.info.DefaultStructureInfo.avgIndex;
import static de.ims.icarus2.model.standard.members.structure.info.DefaultStructureInfo.maxIndex;
import static de.ims.icarus2.model.standard.members.structure.info.DefaultStructureInfo.minIndex;
import static java.util.Objects.requireNonNull;

import java.util.Set;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureInfo;
import de.ims.icarus2.model.api.members.structure.StructureInfoField;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.util.IcarusUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * This class is not thread-safe in general, but using the static {@link #createInfo(Structure)} method
 * will result in a {@link ThreadLocal thread-local} instance being used for computation.
 *
 * @author Markus Gärtner
 *
 */
public class StructureInfoBuilder {

	/**
	 * Computes and returns a fresh new {@link StructureInfo} or {@code null}
	 * in case the given {@code structure} is {@link Structure#isEmpty() empty}.
	 *
	 * @param structure
	 * @return
	 */
	public static StructureInfo createInfo(Structure structure) {
		if(structure.isEmpty()) {
			return null;
		}

		StructureInfoBuilder builder = _builders.get();

		try {
			builder.reset(structure);

			return builder.build();
		} finally {
			builder.clear();
		}
	}

	private static ThreadLocal<StructureInfoBuilder> _builders = ThreadLocal.withInitial(StructureInfoBuilder::new);

	private static final StructureInfoField[] _fields = StructureInfoField.values();

	/**
	 * The structure to compute metadata for.
	 */
	private Structure structure;
	/**
	 * Stores final average values for individual fields.
	 */
	private double[] avgValues;
	/**
	 * Stores final combination of minimum and maximum values for individual fields.
	 */
	private long[] minMaxValues;

	/**
	 * Summed up values for individual fields.
	 */
	private final long[] sums = new long[_fields.length];

	/**
	 * Number of nodes encountered for individual fields.
	 */
	private final long[] counts = new long[_fields.length];

	private final Set<Item> visitedNodes = new ObjectOpenHashSet<>(200);

	public StructureInfoBuilder() {
		// no-op
	}

	/**
	 * Initializes this builder with the given {@code Structure}.
	 *
	 * @param structure
	 */
	public void reset(Structure structure) {
		requireNonNull(structure);

		this.structure = structure;

		avgValues = new double[_fields.length];
		minMaxValues = new long[_fields.length*2];
	}

	public StructureInfo build() {
		final StructureType type = structure.getStructureType();
		final boolean isGraph = type==StructureType.GRAPH || type==StructureType.DIRECTED_GRAPH;

		for(StructureInfoField field : _fields) {
			if(field.isTypeSupported(type)) {
				sums[field.ordinal()] = 0;
				counts[field.ordinal()] = 0;
				avgValues[avgIndex(field)] = 0D;
				minMaxValues[minIndex(field)] = Long.MAX_VALUE;
				minMaxValues[maxIndex(field)] = Long.MIN_VALUE;
			} else {
				avgValues[avgIndex(field)] = IcarusUtils.UNSET_DOUBLE;
				minMaxValues[minIndex(field)] = IcarusUtils.UNSET_LONG;
				minMaxValues[maxIndex(field)] = IcarusUtils.UNSET_LONG;
			}
		}

		final Item root = structure.getVirtualRoot();

		// Do recursive traversal
		//TODO switch to in-place traversal via loop (we can't make assumptions regarding "depths" of the structure and fitting stack size limit in the jVM)
		if(isGraph) {
			structure.forEachOutgoingEdge(root, e -> visitGraphNode(e.getTarget()));
		} else {
			structure.forEachOutgoingEdge(root, e -> visitNode(e.getTarget()));
		}

		// Finalize stuff
		for(StructureInfoField field : _fields) {
			if(field.isTypeSupported(type)) {
				int index = field.ordinal();
				if(counts[index]>0L) {
					avgValues[avgIndex(field)] = (double)sums[index] / (double)counts[index];
				}
			}
		}

		return new DefaultStructureInfo(avgValues, minMaxValues);
	}

	private void adjust(StructureInfoField field, long value) {
		// Adjust min
		int index = minIndex(field);
		if(value<minMaxValues[index]) {
			minMaxValues[index] = value;
		}
		// Adjust max
		index = maxIndex(field);
		if(value>minMaxValues[index]) {
			minMaxValues[index] = value;
		}

		sums[field.ordinal()] += value;
		counts[field.ordinal()] ++;
	}

	private void computeGraphNodeData(Item node) {

		// Only real metadata computable for graphs are the individual edge counts for nodes
		adjust(StructureInfoField.OUTGOING_EDGES, structure.getOutgoingEdgeCount(node));
		adjust(StructureInfoField.INCOMING_EDGES, structure.getIncomingEdgeCount(node));
	}

	private void computeNodeData(Item node) {

		// All the general metadata will be computed in the graph-based method
		computeGraphNodeData(node);

		// Do tree-related stuff here
		long depth = structure.getDepth(node);
		if(depth!=IcarusUtils.UNSET_LONG) {
			adjust(StructureInfoField.DEPTH, depth);
		}
		long height = structure.getHeight(node);
		if(height!=IcarusUtils.UNSET_LONG) {
			adjust(StructureInfoField.HEIGHT, height);
		}
		long descendants = structure.getDescendantCount(node);
		if(descendants!=IcarusUtils.UNSET_LONG) {
			adjust(StructureInfoField.DESCENDANTS, descendants);
		}
	}

	/**
	 * Computes metadata for the given node and then continues with all the outgoing edges
	 */
	private void visitNode(Item node) {
		computeNodeData(node);

		structure.forEachOutgoingEdge(node, e -> visitNode(e.getTarget()));
	}


	/**
	 * Computes metadata for the given node and then continues with all the outgoing edges
	 * leading to nodes that haven't been visited previously.
	 * <p>
	 * The current node is added to the list of visited nodes after computing its metadata
	 * but before continuing with outgoing edges.
	 */
	private void visitGraphNode(Item node) {
		computeGraphNodeData(node);

		visitedNodes.add(node);

		structure.forEachOutgoingEdge(node, e -> {
			Item target = e.getTarget();
			if(!visitedNodes.contains(target)) {
				visitGraphNode(target);
			}
		});
	}

	/**
	 * Performs maintenance work after a build has been performed or failed.
	 * This method should always be placed in the {@code finally} block of the
	 * {@code try} statement that contains the call to {@link #build()} to ensure
	 * proper cleanup.
	 */
	public void clear() {
		structure = null;
		avgValues = null;
		minMaxValues = null;
		visitedNodes.clear();
	}
}
