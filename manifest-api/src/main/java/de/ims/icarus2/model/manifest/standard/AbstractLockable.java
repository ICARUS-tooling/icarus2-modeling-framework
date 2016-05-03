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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.manifest.standard;

import java.util.Collection;

import de.ims.icarus2.model.manifest.api.Lockable;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractLockable implements Lockable {

	private boolean locked = false;

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestFragment#lock()
	 */
	@Override
	public void lock() {
		locked = true;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestFragment#isLocked()
	 */
	@Override
	public boolean isLocked() {
		return locked;
	}

	protected void lockNested(Lockable...items) {
		for(Lockable item : items) {
			if(item!=null) {
				item.lock();
			}
		}
	}

	protected void lockNested(Collection<? extends Lockable> items) {
		for(Lockable item : items) {
			if(item!=null) {
				item.lock();
			}
		}
	}
}
