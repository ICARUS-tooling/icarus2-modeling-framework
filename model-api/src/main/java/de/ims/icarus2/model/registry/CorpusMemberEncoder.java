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
package de.ims.icarus2.model.registry;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;

import java.io.IOException;
import java.util.function.IntConsumer;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.path.CorpusPath;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class CorpusMemberEncoder {


	protected final LayerLookup config;

	protected CorpusMemberEncoder(LayerLookup config) {
		checkNotNull(config);

		this.config = config;
	}

	public final LayerLookup config() {
		return config;
	}

	public abstract String writePath(CorpusPath path);

	public abstract void writePath(CorpusPath path, Appendable out)
			throws IOException;

	public abstract void writePath(CorpusPath path, IntConsumer out);

	public abstract String writeItem(Item item);

	public abstract void writeItem(Item item, Appendable out)
			throws IOException;

	public abstract void writeItem(Item item, IntConsumer out);

}
