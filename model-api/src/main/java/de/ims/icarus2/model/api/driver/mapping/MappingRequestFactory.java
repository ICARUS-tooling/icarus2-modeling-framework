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
package de.ims.icarus2.model.api.driver.mapping;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.concurrent.Callable;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.standard.driver.indices.ArrayIndexSet;

/**
 * A factory that creates {@link Callable} objects for lookup operations on a given
 * {@link MappingReader}.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MappingRequestFactory {

	private MappingReader reader;
	private RequestSettings settings;

	public MappingRequestFactory reader(MappingReader reader) {
		checkNotNull(reader);

		this.reader = reader;

		return this;
	}

	public MappingRequestFactory settings(RequestSettings settings) {
		this.settings = settings;

		return this;
	}

	public MappingReader getReader() {
		return reader;
	}

	public RequestSettings getSettings() {
		return settings;
	}

	private void checkReaderSet() {
		if(reader==null)
			throw new ModelException(ModelErrorCode.ILLEGAL_STATE, "No reader set");
	}

	public MappingRequest forIndex(long index) {
		checkReaderSet();

		return new MappingRequest.SingleValueRequest(reader, settings, index);
	}

	private MappingRequest forIndices0(IndexSet[] indices) {
		checkReaderSet();

		return new MappingRequest.MultiValueRequest(reader, settings, indices);
	}

	public MappingRequest forIndices(IndexSet...indices) {
		checkNotNull(indices);

		return forIndices0(indices);
	}

	public MappingRequest forIndices(long...indices) {
		checkNotNull(indices);

		IndexSet set = new ArrayIndexSet(indices);

		return forIndices0(IndexUtils.wrap(set));
	}

	public MappingRequest forIndices(int...indices) {
		checkNotNull(indices);

		IndexSet set = new ArrayIndexSet(indices);

		return forIndices0(IndexUtils.wrap(set));
	}

	public MappingRequest forIndices(short...indices) {
		checkNotNull(indices);

		IndexSet set = new ArrayIndexSet(indices);

		return forIndices0(IndexUtils.wrap(set));
	}

	public MappingRequest forIndices(byte...indices) {
		checkNotNull(indices);

		IndexSet set = new ArrayIndexSet(indices);

		return forIndices0(IndexUtils.wrap(set));
	}
}
