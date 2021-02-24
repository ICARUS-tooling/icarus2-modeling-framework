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
package de.ims.icarus2.model.standard.members.structure;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * Implements an {@link EdgeStorage} that cannot modified by the user.
 * This implementation will throw a {@link ModelException} with an
 * {@link ModelErrorCode#UNSUPPORTED_OPERATION unsupported operation} error
 * on each attempt to modify the content via one of the official modification
 * methods defined in the {@code EdgeStorage} interface.
 * Furthermore the returned {@link StructureEditVerifier} for this implementation
 * will always be a {@link ImmutableStructureEditVerifier} instance that allows
 * no modifications.
 *
 * @author Markus Gärtner
 *
 */
public abstract class AbstractImmutableEdgeStorage implements EdgeStorage {

	protected <T extends Object> T signalUnsupportedOperation(Structure context) {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Edge storage is immutable");
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#addEdge(de.ims.icarus2.model.api.members.structure.Structure, long, de.ims.icarus2.model.api.members.item.Edge)
	 */
	@SuppressWarnings("boxing")
	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public long addEdge(Structure context, long index, Edge edge) {
		return signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#addEdges(de.ims.icarus2.model.api.members.structure.Structure, long, de.ims.icarus2.util.collections.seq.DataSequence)
	 */
	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public void addEdges(Structure context, long index,
			DataSequence<? extends Edge> edges) {
		signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#removeEdge(de.ims.icarus2.model.api.members.structure.Structure, long)
	 */
	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public Edge removeEdge(Structure context, long index) {
		return signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#removeEdges(de.ims.icarus2.model.api.members.structure.Structure, long, long)
	 */
	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public DataSequence<? extends Edge> removeEdges(Structure context,
			long index0, long index1) {
		return signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#swapEdges(de.ims.icarus2.model.api.members.structure.Structure, long, long)
	 */
	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public void swapEdges(Structure context, long index0, long index1) {
		signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#setTerminal(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge, de.ims.icarus2.model.api.members.item.Item, boolean)
	 */
	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public void setTerminal(Structure context, Edge edge, Item item,
			boolean isSource) {
		signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#newEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public Edge newEdge(Structure context, Item source, Item target) {
		return signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#createEditVerifier(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.container.ContainerEditVerifier)
	 */
	@Override
	public StructureEditVerifier createEditVerifier(Structure context,
			@Nullable ContainerEditVerifier containerEditVerifier) {
		return new ImmutableStructureEditVerifier(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#isDirty(de.ims.icarus2.model.api.members.structure.Structure)
	 */
	@Override
	public boolean isDirty(@Nullable Structure context) {
		return false;
	}
}
