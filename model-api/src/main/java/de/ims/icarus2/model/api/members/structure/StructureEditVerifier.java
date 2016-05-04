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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/members/structure/StructureEditVerifier.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.members.structure;

import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * Specifies a verification mechanism that can be used to check preconditions
 * for any modification action on a structure. Each instance of such a verifier
 * is bound to a single {@link #getSource() structure} and can not be used for
 * any other.
 * <p>
 * Note that like most other parts of the lower level framework specification
 * this interface too does not make any guarantees for multi-threaded access.
 * If client code does not enforce synchronization when accessing a verifier,
 * the returned values cannot be taken as promise that the target structure will
 * actually be able to carry out a desired modification!
 *
 * @author Markus Gärtner
 * @version $Id: StructureEditVerifier.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public interface StructureEditVerifier extends ContainerEditVerifier {

	@Override
	Structure getSource();

	boolean canAddEdge(long index, Edge edge);

	boolean canAddEdges(long index, DataSequence<? extends Edge> edges);

	boolean canRemoveEdge(long index);

	boolean canRemoveEdges(long index0, long index1);

	boolean canMoveEdge(long index0, long index1);

	boolean canSetTerminal(Edge edge, Item terminal, boolean isSource);

	boolean canCreateEdge(Item source, Item target);
}
