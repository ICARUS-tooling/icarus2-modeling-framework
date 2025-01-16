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
package de.ims.icarus2.model.standard.members.structure;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.model.standard.members.container.AbstractImmutableContainer;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractImmutableStructure extends AbstractImmutableContainer implements Structure {

	private <T extends Object> T signalUnsupportedOperation() {
		throw new ModelException(getCorpus(), GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Structure is immutable");
	}

	@Override
	public MemberType getMemberType() {
		return MemberType.STRUCTURE;
	}

	@Override
	public StructureEditVerifier createEditVerifier() {
		return new ImmutableStructureEditVerifier(this);
	}

	@SuppressWarnings("boxing")
	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public long addEdge(long index, Edge edge) {
		return signalUnsupportedOperation();
	}

	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public void addEdges(long index, DataSequence<? extends Edge> edges) {
		signalUnsupportedOperation();
	}

	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public Edge removeEdge(long index) {
		return signalUnsupportedOperation();
	}

	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public DataSequence<? extends Edge> removeEdges(long index0, long index1) {
		return signalUnsupportedOperation();
	}

	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public void swapEdges(long index0, long index1) {
		signalUnsupportedOperation();
	}

	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public void setTerminal(Edge edge, Item item, boolean isSource) {
		signalUnsupportedOperation();
	}

	@Override
	@Unguarded(Unguarded.UNSUPPORTED)
	public Edge newEdge(Item source, Item target) {
		return signalUnsupportedOperation();
	}

}
