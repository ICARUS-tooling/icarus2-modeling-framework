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
package de.ims.icarus2.model.nio;

import static de.ims.icarus2.model.standard.util.CorpusUtils.ensureIntegerValueRange;
import static de.ims.icarus2.model.util.Conditions.checkArgument;
import static de.ims.icarus2.model.util.Conditions.checkNotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import de.ims.icarus2.model.api.ModelError;
import de.ims.icarus2.model.api.ModelException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ByteChannelCharacterSequence implements CharSequence {

	private final SeekableByteChannel channel;
	private final int start, end;

	private transient ByteBuffer buffer = ByteBuffer.allocate(2);

	public ByteChannelCharacterSequence(SeekableByteChannel channel) {
		checkNotNull(channel);

		this.channel = channel;

		try {
			int len = ensureIntegerValueRange(channel.size()>>1);

			start = 0;
			end = len;
		} catch (IOException e) {
			throw new ModelException(ModelError.IO_ERROR, "Failed to fetch size of channel", e);
		}
	}

	private ByteChannelCharacterSequence(SeekableByteChannel channel, int start, int end) {

		this.channel = channel;
		this.start = start;
		this.end = end;
	}

	/**
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length() {
		return end-start;
	}

	/**
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@Override
	public char charAt(int index) {
		if(index<0 || index>=length())
			throw new IndexOutOfBoundsException();

		long idx = (start+(long)index)<<1;

		try {
			channel.position(idx);
			if(channel.read(buffer)!=2)
				throw new IndexOutOfBoundsException();

			return (char)((buffer.get(0) << 8) | (buffer.get(1) & 0xff));
		} catch (IOException e) {
			throw new ModelException(ModelError.IO_ERROR, "Failed to read channel", e);
		}
	}

	/**
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	@Override
	public CharSequence subSequence(int start, int end) {
		checkArgument(start<=end);
		if(start<0 || end<0 || end>=length())
			throw new IndexOutOfBoundsException();

		return new ByteChannelCharacterSequence(channel, this.start+start, this.start+end);
	}
}
