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

 * $Revision: 396 $
 * $Date: 2015-05-20 11:11:11 +0200 (Mi, 20 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/util/AbstractPart.java $
 *
 * $LastChangedDate: 2015-05-20 11:11:11 +0200 (Mi, 20 Mai 2015) $
 * $LastChangedRevision: 396 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;

/**
 * @author Markus Gärtner
 * @version $Id: AbstractPart.java 396 2015-05-20 09:11:11Z mcgaerty $
 *
 */
public class AbstractPart<O extends Object> implements Part<O> {

	private O owner;

	protected O getOwner() {
		return owner;
	}

	protected void checkAdded() {
		if(!isAdded())
			throw new IcarusException(GlobalErrorCode.ILLEGAL_STATE,
					"No owner set");
	}

	/**
	 * @see de.ims.icarus2.util.Part#addNotify(java.lang.Object)
	 */
	@Override
	public void addNotify(O owner) {
		if(this.owner!=null)
			throw new IcarusException(GlobalErrorCode.ILLEGAL_STATE,
					"Owner of part already set");

		this.owner = owner;
	}

	/**
	 * @see de.ims.icarus2.util.Part#removeNotify(java.lang.Object)
	 */
	@Override
	public void removeNotify(O owner) {
		if(this.owner==null)
			throw new IcarusException(GlobalErrorCode.ILLEGAL_STATE,
					"Owner of part not yet set");
		if(this.owner!=owner)
			throw new IcarusException(GlobalErrorCode.INVALID_INPUT,
					"Cannot remove foreign owner");

		this.owner = null;
	}

	/**
	 * @see de.ims.icarus2.util.Part#isAdded()
	 */
	@Override
	public boolean isAdded() {
		return owner!=null;
	}

}
