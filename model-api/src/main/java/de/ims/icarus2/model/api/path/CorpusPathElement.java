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

 * $Revision: 451 $
 * $Date: 2016-02-03 12:33:06 +0100 (Mi, 03 Feb 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/path/CorpusPathElement.java $
 *
 * $LastChangedDate: 2016-02-03 12:33:06 +0100 (Mi, 03 Feb 2016) $
 * $LastChangedRevision: 451 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.path;

import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 * @version $Id: CorpusPathElement.java 451 2016-02-03 11:33:06Z mcgaerty $
 *
 */
public interface CorpusPathElement {

	PathElementType getType();

	Layer getLayer();

	Item getItem();

	int getIndex();

	public enum PathElementType {

		/**
		 * Top level frame
		 */
		LAYER,

		/**
		 * Content of previous path element, which had to be a container/structure.
		 * <p>
		 * Since containers are allowed to contain duplicated, depending on the manifest
		 * settings, we don't point to the elements in a path, but rather list the index
		 * in the container or structure!
		 */
		INDEX,

		/**
		 * Edge hosted within previous path element, which had to be a structure
		 */
		EDGE_INDEX,
		;
	}
}
