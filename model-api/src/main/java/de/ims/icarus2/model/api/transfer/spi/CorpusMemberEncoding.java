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
package de.ims.icarus2.model.api.transfer.spi;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.ServiceLoader;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.registry.LayerLookup;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 *
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class CorpusMemberEncoding {

	/**
	 * Returns the unique identifier of this encoding
	 *
	 * @return
	 */
	public abstract String getName();

	public abstract CorpusMemberEncoder newEncoder(LayerLookup lookup);

	public abstract CorpusMemberDecoder newDecoder(LayerLookup lookup);

	private static final ServiceLoader<CorpusMemberEncoding> serviceLoader = ServiceLoader.load(CorpusMemberEncoding.class);

	public static CorpusMemberEncoding getEncoding(String name) {
		checkNotNull(name);

		synchronized (serviceLoader) {
			for(CorpusMemberEncoding encoding : serviceLoader) {
				if(name.equals(encoding.getName())) {
					return encoding;
				}
			}
		}

		throw new ModelException(GlobalErrorCode.INVALID_INPUT, "No encoding available for name: "+name);
	}

	public static CorpusMemberEncoding[] availableEncodings() {
		LazyCollection<CorpusMemberEncoding> result = LazyCollection.lazyList();

		synchronized (serviceLoader) {
			serviceLoader.forEach(result);
		}

		return result.getAsArray(new CorpusMemberEncoding[result.size()]);
	}
}
