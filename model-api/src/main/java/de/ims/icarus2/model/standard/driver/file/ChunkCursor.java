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
package de.ims.icarus2.model.standard.driver.file;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;
import static de.ims.icarus2.model.util.Conditions.checkState;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import de.ims.icarus2.io.ByteChannelBlockStream;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.standard.driver.io.sets.FileSet;
import de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkIndex;
import de.ims.icarus2.model.standard.driver.mapping.chunks.ChunkIndexReader;

/**
 *
 * Not thread-safe!
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ChunkCursor implements Closeable {

	// Initialization stuff
	private final FileSet dataFiles;
	private final IndexSet[] indices;
	private final ChunkIndex chunkIndex;
	private final ByteChannelBlockStream _stream;

	// Cursor helpers
	private int _set;
	private int _pos;
	private boolean _empty;
	private SeekableByteChannel _channel;
	private Path _path;
	private boolean _ready;
	// random access mode
	private boolean _ram;

	private ChunkIndexReader reader;

	private long currentIndex;
	private int fileIndex;
	private long chunkBegin;
	private long chunkEnd;

	public ChunkCursor(FileSet dataFiles, IndexSet[] indices, ChunkIndex chunkIndex, int bufferSize) {
		checkNotNull(dataFiles);
		checkNotNull(indices);
		checkNotNull(chunkIndex);

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
		_path = dataFiles.getFileAt(fileIndex);
		_channel = Files.newByteChannel(_path, StandardOpenOption.READ);
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
