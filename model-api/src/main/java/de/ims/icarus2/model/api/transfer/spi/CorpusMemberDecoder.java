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

import java.io.IOException;
import java.io.Reader;
import java.util.function.IntSupplier;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.path.CorpusPath;
import de.ims.icarus2.model.api.registry.LayerLookup;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class CorpusMemberDecoder {

	protected final LayerLookup config;

	protected CorpusMemberDecoder(LayerLookup config) {
		checkNotNull(config);

		this.config = config;
	}

	public final LayerLookup config() {
		return config;
	}

	public abstract CorpusPath readPath(CharSequence s);


	public abstract CorpusPath readPath(Reader in) throws IOException;


	public abstract CorpusPath readPath(IntSupplier in);


	public abstract Item readItem(CharSequence s);


	public abstract Item readItem(Reader in) throws IOException;


	public abstract Item readItem(IntSupplier in);
}
