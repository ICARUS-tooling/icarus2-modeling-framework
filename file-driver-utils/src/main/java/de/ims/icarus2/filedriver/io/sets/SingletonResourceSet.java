/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
 */
package de.ims.icarus2.filedriver.io.sets;

import static java.util.Objects.requireNonNull;
import de.ims.icarus2.model.api.io.resources.IOResource;

/**
 * @author Markus Gärtner
 *
 */
public final class SingletonResourceSet implements ResourceSet {

	private final IOResource resource;

	/**
	 * Creates a new {@code SingletonResourceSet} that points to the given {@code resource}.
	 *
	 * @param resource
	 * @param storage
	 */
	public SingletonResourceSet(IOResource resource) {
		requireNonNull(resource);

		this.resource = resource;
	}

	@Override
	public String toString() {
		return "SingletonResourceSet [resource="+resource+"]";
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.sets.ResourceSet#getResourceCount()
	 */
	@Override
	public int getResourceCount() {
		return 1;
	}

	private void checkIndex(int resourceIndex) {
		if(resourceIndex!=0)
			throw new IllegalArgumentException("Invalid resource index: "+resourceIndex+" - only legal value is 0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.sets.ResourceSet#getResourceAt(int)
	 */
	@Override
	public IOResource getResourceAt(int resourceIndex) {
		checkIndex(resourceIndex);

		return resource;
	}
}
