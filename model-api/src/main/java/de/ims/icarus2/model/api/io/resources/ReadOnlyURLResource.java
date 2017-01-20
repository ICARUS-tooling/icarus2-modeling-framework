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
package de.ims.icarus2.model.api.io.resources;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.SeekableByteChannel;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;

/**
 * @author Markus Gärtner
 *
 */
public class ReadOnlyURLResource implements IOResource {

	private final URL url;

	/**
	 * @param url
	 */
	public ReadOnlyURLResource(URL url) {
		requireNonNull(url);
		this.url = url;
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#getWriteChannel()
	 */
	@Override
	public SeekableByteChannel getWriteChannel() throws IOException {
		throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Cant write to URL resource");
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#getReadChannel()
	 */
	@Override
	public SeekableByteChannel getReadChannel() throws IOException {
		//TODO establish handle and buffer
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#delete()
	 */
	@Override
	public void delete() throws IOException {
		//TODO close handle
	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#prepare()
	 */
	@Override
	public void prepare() throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus2.model.api.io.resources.IOResource#size()
	 */
	@Override
	public long size() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
