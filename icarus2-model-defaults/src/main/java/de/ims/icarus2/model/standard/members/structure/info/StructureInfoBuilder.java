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
package de.ims.icarus2.model.standard.members.structure.info;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.IcarusUtils.UNSET_DOUBLE;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureInfo;
import de.ims.icarus2.model.api.members.structure.StructureInfoField;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.util.IcarusUtils;

/**
 * This class is not thread-safe in general, but using the static
 * {@link #createInfo(Structure)} method will result in a {@link ThreadLocal
 * thread-local} instance being used for computation.
 *
 * @author Markus Gärtner
 *
 */
public class StructureInfoBuilder {

	/**
	 * Stored locally for performance reasons.
	 */
	private static final StructureInfoField[] _fields = StructureInfoField.values();

	/**
	 * Package-private so that {@link StructureInfoBuilder} can access it for
	 * consistency.
	 */
	private static int index(StructureInfoField field) {
		return field.ordinal();
	}

	/**
	 * Computes and returns a fresh new {@link StructureInfo} or {@code null} in
	 * case the given {@code structure} is {@link Structure#isEmpty() empty}.
	 *
	 * @param structure
	 * @return
	 */
	public static StructureInfo createInfo(Structure structure) {
		if (structure.isEmpty()) {
			return null;
		}

		return _builders.get().build(structure);
	}

	private static ThreadLocal<StructureInfoBuilder> _builders
		= ThreadLocal.withInitial(StructureInfoBuilder::new);

	/**
	 * The structure to compute metadata for.
	 */
	private Structure structure;
	/**
	 * Stores final average values for individual fields.
	 */
	private double[] avgValues = new double[_fields.length];
	/**
	 * Stores final combination of minimum and maximum values for individual fields.
	 */
	private long[] minMaxValues = new long[_fields.length*2];

	/**
	 * Summed up values for individual fields.
	 */
	private final long[] sums = new long[_fields.length];

	/**
	 * Number of nodes encountered for individual fields.
	 */
	private final long[] counts = new long[_fields.length];

	public StructureInfoBuilder() {
		// no-op
	}

	/**
	 * Initializes this builder with the given {@code Structure}.
	 *
	 * @param structure
	 */
	private void reset(Structure structure) {
		requireNonNull(structure);

		this.structure = structure;

		Arrays.fill(avgValues, UNSET_DOUBLE);
		Arrays.fill(minMaxValues, UNSET_LONG);
		Arrays.fill(sums, UNSET_LONG);
		Arrays.fill(counts, UNSET_LONG);
	}

	public StructureInfo build(Structure structure) {
		reset(structure);

		try {
			final StructureType type = structure.getStructureType();
			final boolean isGraph = type == StructureType.GRAPH;

			for (StructureInfoField field : _fields) {
				if (field.isTypeSupported(type)) {
					sums[index(field)] = 0;
					counts[index(field)] = 0;
					avgValues[avgIndex(field)] = 0D;
					minMaxValues[minIndex(field)] = Long.MAX_VALUE;
					minMaxValues[maxIndex(field)] = Long.MIN_VALUE;
				} else {
					avgValues[avgIndex(field)] = IcarusUtils.UNSET_DOUBLE;
					minMaxValues[minIndex(field)] = IcarusUtils.UNSET_LONG;
					minMaxValues[maxIndex(field)] = IcarusUtils.UNSET_LONG;
				}
			}

			// Do random traversal in whatever order the structure prefers
			if (isGraph) {
				structure.forEachItem(this::computeGraphNodeData);
			} else {
				structure.forEachItem(this::computeNodeData);
			}

			// Finalize stuff
			for (StructureInfoField field : _fields) {
				if (field.isTypeSupported(type)) {
					int index = index(field);
					if (counts[index] > 0L) {
						avgValues[avgIndex(field)] = (double) sums[index] / (double) counts[index];
					}
				}
			}

			return new DefaultStructureInfo(avgValues, minMaxValues);
		} finally {
			clear();
		}
	}

	private void adjust(StructureInfoField field, long value) {
		// Adjust min
		int index = minIndex(field);
		if (value < minMaxValues[index]) {
			minMaxValues[index] = value;
		}
		// Adjust max
		index = maxIndex(field);
		if (value > minMaxValues[index]) {
			minMaxValues[index] = value;
		}

		sums[index(field)] += value;
		counts[index(field)]++;
	}

	private void computeGraphNodeData(Item node) {

		// Only real metadata computable for graphs are the individual edge counts for
		// nodes
		adjust(StructureInfoField.OUTGOING_EDGES, structure.getOutgoingEdgeCount(node));
		adjust(StructureInfoField.INCOMING_EDGES, structure.getIncomingEdgeCount(node));
	}

	private void computeNodeData(Item node) {

		// All the general metadata will be computed in the graph-based method
		computeGraphNodeData(node);

		// Do tree-related stuff here
		long depth = structure.getDepth(node);
		if (depth != IcarusUtils.UNSET_LONG) {
			adjust(StructureInfoField.DEPTH, depth);
		}
		long height = structure.getHeight(node);
		if (height != IcarusUtils.UNSET_LONG) {
			adjust(StructureInfoField.HEIGHT, height);
		}
		long descendants = structure.getDescendantCount(node);
		if (descendants != IcarusUtils.UNSET_LONG) {
			adjust(StructureInfoField.DESCENDANTS, descendants);
		}
	}

	/**
	 * Performs maintenance work after a build has been performed or failed. This
	 * method should always be placed in the {@code finally} block of the
	 * {@code try} statement that contains the call to {@link #build()} to ensure
	 * proper cleanup.
	 */
	private void clear() {
		structure = null;
	}

	private static int avgIndex(StructureInfoField field) {
		return field.ordinal();
	}

	private static int minIndex(StructureInfoField field) {
		return field.ordinal() << 1;
	}

	private static int maxIndex(StructureInfoField field) {
		return (field.ordinal() << 1) + 1;
	}

	/**
	 * @author Markus Gärtner
	 *
	 */
	private static class DefaultStructureInfo implements StructureInfo {

		private static final long serialVersionUID = -2562709334248640742L;

		/**
		 * Use {@link StructureInfoField#ordinal() ordinal} to get index for entry
		 */
		private final double[] avgValues;

		/**
		 * Use {@link StructureInfoField#ordinal() ordinal*2} to get index for min entry
		 * and {@code ordinal*2 + 1} for max entry.
		 */
		private final long[] minMaxValues;

		DefaultStructureInfo(double[] avgValues, long[] minMaxValues) {
			requireNonNull(avgValues);
			requireNonNull(minMaxValues);

			checkArgument(Messages.mismatch("Incorrect number of entries in avgValues", _int(_fields.length),
					_int(avgValues.length)), avgValues.length == _fields.length);

			checkArgument(Messages.mismatch("Incorrect number of entries in minMaxValues", _int(_fields.length * 2),
					_int(minMaxValues.length)), minMaxValues.length == _fields.length * 2);

			// Defensive copying needed in case the same builder is re-used
			this.avgValues = avgValues.clone();
			this.minMaxValues = minMaxValues.clone();
		}

		/**
		 * @see de.ims.icarus2.model.api.members.structure.StructureInfo#getAvg(de.ims.icarus2.model.api.members.structure.StructureInfoField)
		 */
		@Override
		public double getAvg(StructureInfoField field) {
			return avgValues[StructureInfoBuilder.avgIndex(field)];
		}

		/**
		 * @see de.ims.icarus2.model.api.members.structure.StructureInfo#getMin(de.ims.icarus2.model.api.members.structure.StructureInfoField)
		 */
		@Override
		public long getMin(StructureInfoField field) {
			return minMaxValues[StructureInfoBuilder.minIndex(field)];
		}

		/**
		 * @see de.ims.icarus2.model.api.members.structure.StructureInfo#getMax(de.ims.icarus2.model.api.members.structure.StructureInfoField)
		 */
		@Override
		public long getMax(StructureInfoField field) {
			return minMaxValues[StructureInfoBuilder.maxIndex(field)];
		}
	}
}
