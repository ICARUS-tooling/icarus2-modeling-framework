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

 * $Revision: 422 $
 * $Date: 2015-08-19 15:38:58 +0200 (Mi, 19 Aug 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/layers/item/DefaultStructureLayer.java $
 *
 * $LastChangedDate: 2015-08-19 15:38:58 +0200 (Mi, 19 Aug 2015) $
 * $LastChangedRevision: 422 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.layers.item;

import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.manifest.StructureLayerManifest;

/**
 * @author Markus Gärtner
 * @version $Id: DefaultStructureLayer.java 422 2015-08-19 13:38:58Z mcgaerty $
 *
 */
public class DefaultStructureLayer extends DefaultItemLayer implements StructureLayer {

	/**
	 *
	 * @param manifest
	 */
	public DefaultStructureLayer(StructureLayerManifest manifest) {
		super(manifest);
	}

	/**
	 * @see de.ims.icarus2.model.api.standard.layer.AbstractLayer#getManifest()
	 */
	@Override
	public StructureLayerManifest getManifest() {
		return (StructureLayerManifest) super.getManifest();
	}
}
