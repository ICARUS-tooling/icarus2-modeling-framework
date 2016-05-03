/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/structure/AbstractImmutableStructure.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.structure;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.model.standard.members.container.AbstractImmutableContainer;
import de.ims.icarus2.util.collections.DataSequence;

/**
 * @author Markus Gärtner
 * @version $Id: AbstractImmutableStructure.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public abstract class AbstractImmutableStructure extends AbstractImmutableContainer implements Structure {

	private <T extends Object> T signalUnsupportedOperation() {
		throw new ModelException(getCorpus(), ModelErrorCode.UNSUPPORTED_OPERATION,
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

	@Override
	public long addEdge(long index, Edge edge) {
		return signalUnsupportedOperation();
	}

	@Override
	public void addEdges(long index, DataSequence<? extends Edge> edges) {
		signalUnsupportedOperation();
	}

	@Override
	public Edge removeEdge(long index) {
		return signalUnsupportedOperation();
	}

	@Override
	public DataSequence<? extends Edge> removeEdges(long index0, long index1) {
		return signalUnsupportedOperation();
	}

	@Override
	public void moveEdge(long index0, long index1) {
		signalUnsupportedOperation();
	}

	@Override
	public void setTerminal(Edge edge, Item item, boolean isSource) {
		signalUnsupportedOperation();
	}

	@Override
	public Edge newEdge(Item source, Item target) {
		return signalUnsupportedOperation();
	}

}
