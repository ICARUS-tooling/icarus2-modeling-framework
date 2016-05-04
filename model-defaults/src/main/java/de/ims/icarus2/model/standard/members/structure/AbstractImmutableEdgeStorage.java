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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/structure/AbstractImmutableEdgeStorage.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.structure;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.util.collections.DataSequence;

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
 * @version $Id: AbstractImmutableEdgeStorage.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public abstract class AbstractImmutableEdgeStorage implements EdgeStorage {

	protected <T extends Object> T signalUnsupportedOperation(Structure context) {
		throw new ModelException(context.getCorpus(), ModelErrorCode.UNSUPPORTED_OPERATION,
				"Edge storage is immutable");
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#addEdge(de.ims.icarus2.model.api.members.structure.Structure, long, de.ims.icarus2.model.api.members.item.Edge)
	 */
	@Override
	public long addEdge(Structure context, long index, Edge edge) {
		return signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#addEdges(de.ims.icarus2.model.api.members.structure.Structure, long, de.ims.icarus2.util.collections.DataSequence)
	 */
	@Override
	public void addEdges(Structure context, long index,
			DataSequence<? extends Edge> edges) {
		signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#removeEdge(de.ims.icarus2.model.api.members.structure.Structure, long)
	 */
	@Override
	public Edge removeEdge(Structure context, long index) {
		return signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#removeEdges(de.ims.icarus2.model.api.members.structure.Structure, long, long)
	 */
	@Override
	public DataSequence<? extends Edge> removeEdges(Structure context,
			long index0, long index1) {
		return signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#moveEdge(de.ims.icarus2.model.api.members.structure.Structure, long, long)
	 */
	@Override
	public void moveEdge(Structure context, long index0, long index1) {
		signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#setTerminal(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Edge, de.ims.icarus2.model.api.members.item.Item, boolean)
	 */
	@Override
	public void setTerminal(Structure context, Edge edge, Item item,
			boolean isSource) {
		signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#newEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public Edge newEdge(Structure context, Item source, Item target) {
		return signalUnsupportedOperation(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#createEditVerifier(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.container.ContainerEditVerifier)
	 */
	@Override
	public StructureEditVerifier createEditVerifier(Structure context,
			ContainerEditVerifier containerEditVerifier) {
		return new ImmutableStructureEditVerifier(context);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.EdgeStorage#isDirty(de.ims.icarus2.model.api.members.structure.Structure)
	 */
	@Override
	public boolean isDirty(Structure context) {
		return false;
	}
}
