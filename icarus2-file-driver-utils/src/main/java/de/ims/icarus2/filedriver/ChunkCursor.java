/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.filedriver;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;

import de.ims.icarus2.filedriver.io.sets.ResourceSet;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndex;
import de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexReader;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.io.resources.IOResource;
import de.ims.icarus2.util.nio.ByteChannelBlockStream;

/**
 *
 * Not thread-safe!
 *
 * @author Markus Gärtner
 *
 */
@Deprecated
public class ChunkCursor implements Closeable {

	// Initialization stuff
	private final ResourceSet dataFiles;
	private final IndexSet[] indices;
	private final ChunkIndex chunkIndex;
	private final ByteChannelBlockStream _stream;

	// Cursor helpers
	private int _set;
	private int _pos;
	private boolean _empty;
	private SeekableByteChannel _channel;
	private IOResource _resource;
	private boolean _ready;
	// random access mode
	private boolean _ram;

	private ChunkIndexReader reader;

	private long currentIndex;
	private int fileIndex;
	private long chunkBegin;
	private long chunkEnd;

	public ChunkCursor(ResourceSet dataFiles, IndexSet[] indices, ChunkIndex chunkIndex, int bufferSize) {
		requireNonNull(dataFiles);
		requireNonNull(indices);
		requireNonNull(chunkIndex);

		this.dataFiles = dataFiles;
		this.indices = indices;
		this.chunkIndex = chunkIndex;

		_stream = new ByteChannelBlockStream(bufferSize);

		_set = -1;
		_pos = -1;
	}

	@Override
	public void close() throws IOException {
		// tbh we don't care which IOException (if any) is propagated up,
		// as long as both IO items get a chance to close
		try {
			if(reader!=null) {
				reader.close();
			}
		} finally {
			if(_channel!=null) {
				_channel.close();
			}
		}
	}

	private ChunkIndexReader getReader() {
		if(reader==null) {
			reader = chunkIndex.newReader();
		}

		return reader;
	}

	private void refresh(long index) throws IOException {
		currentIndex = index;

		ChunkIndexReader reader = getReader();

		int oldFileIndex = fileIndex;

		reader.begin();
		try {
			fileIndex = reader.getFileId(index);
			chunkBegin = reader.getBeginOffset(index);
			chunkEnd = reader.getEndOffset(index);
		} finally {
			reader.end();
		}

		if(oldFileIndex!=fileIndex) {
			refreshFile(fileIndex);
		}

		_stream.setChannel(_channel);
		_stream.reload(chunkBegin, chunkEnd-chunkBegin+1);

		_ready = true;
	}

	private void refreshFile(int fileIndex) throws IOException {
		_resource = dataFiles.getResourceAt(fileIndex);
		_channel = _resource.getReadChannel();
	}

	public boolean moveTo(long index) throws IOException {
		_ram = true;
		_set = -1;
		_pos = -1;

		refresh(index);

		_ready = true;

		return true;
	}

	public boolean next() throws IOException {
		checkState(!_ram);

		if(_empty) {
			return false;
		}

		if(_set==-1) {
			_set = 0;
			_pos = 0;
		} else {
			_pos++;
			if(_pos>=indices[_set].size()) {
				_set++;
				_pos = 0;
			}
		}

		if(_set>=indices.length || _pos>=indices[_set].size()) {
			_empty = true;
			_ready = false;
			return false;
		}

		long index = indices[_set].indexAt(_pos);
		refresh(index);

		_ready = true;

		return true;
	}

	public InputStream getStream() {
		checkState(_ready);
		return _stream;
	}

	public long getCurrentIndex() {
		checkState(_ready);
		return currentIndex;
	}

	public int getFileIndex() {
		checkState(_ready);
		return fileIndex;
	}

	public long getChunkBegin() {
		checkState(_ready);
		return chunkBegin;
	}

	public long getChunkEnd() {
		checkState(_ready);
		return chunkEnd;
	}
}
