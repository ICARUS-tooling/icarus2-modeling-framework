/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.util.nio;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.strings.StringUtil;

/**
 * Implements a {@link CharSequence} that reads its content from an underlying
 * {@link SeekableByteChannel}. Individual characters are read by combining the
 * respective two bytes in the stream into a {@code char} value.
 *
 * @author Markus Gärtner
 *
 */
public class ByteChannelCharacterSequence implements CharSequence {

	private final SeekableByteChannel channel;
	private final int start, end;

	private transient ByteBuffer buffer = ByteBuffer.allocate(2);

	public ByteChannelCharacterSequence(SeekableByteChannel channel) {
		requireNonNull(channel);

		this.channel = channel;

		try {
			int len = IcarusUtils.ensureIntegerValueRange(channel.size()>>1);

			start = 0;
			end = len;
		} catch (IOException e) {
			throw new IcarusRuntimeException(GlobalErrorCode.IO_ERROR, "Failed to fetch size of channel", e);
		}
	}

	/**
	 * Private constructor for sub-sequencing
	 * @param channel
	 * @param start
	 * @param end
	 */
	private ByteChannelCharacterSequence(SeekableByteChannel channel, int start, int end) {

		this.channel = channel;
		this.start = start;
		this.end = end;
	}

	/**
	 * @return the channel
	 */
	public SeekableByteChannel getChannel() {
		return channel;
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

		long position = -1;
		try {
			position = channel.position();

			channel.position(idx);
			if(channel.read(buffer)!=2)
				throw new IndexOutOfBoundsException();

			buffer.flip();

			return (char)((buffer.get(0) << 8) | (buffer.get(1) & 0xff));
		} catch (IOException e) {
			throw new IcarusRuntimeException(GlobalErrorCode.IO_ERROR, "Failed to read channel", e);
		} finally {
			buffer.clear();
			if(position!=-1) {
				try {
					channel.position(position);
				} catch (IOException e) {
					throw new IcarusRuntimeException(GlobalErrorCode.IO_ERROR, "Failed to reset channel position", e);
				}
			}
		}
	}

	/**
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	@Override
	public CharSequence subSequence(int start, int end) {
		checkArgument(start<=end);
		if(start<0 || end<0 || end>length())
			throw new IndexOutOfBoundsException();

		return new ByteChannelCharacterSequence(channel, this.start+start, this.start+end);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}
