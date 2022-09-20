/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.query.api.engine.result.io;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.util.function.Consumer;

import de.ims.icarus2.query.api.engine.result.Match;
import de.ims.icarus2.query.api.engine.result.MatchImpl;
import de.ims.icarus2.util.io.IOUtil;
import de.ims.icarus2.util.io.resource.IOResource;
import de.ims.icarus2.util.strings.CharLineBuffer;
import de.ims.icarus2.util.strings.Splitable;
import de.ims.icarus2.util.strings.StringPrimitives;

/**
 * Writes and reads a match in a TSV format:
 * <ol>
 * <li>Index of the match</li>
 * <li>Entry count of the match</li>
 * <li>All the N entries in N*2 columns, with node and index for every entry</li>
 * </ol>
 * @author Markus Gärtner
 *
 */
public class TabularMatchCodec implements MatchCodec {

	@Override
	public MatchWriter newWriter(IOResource resource) throws IOException {
		return new WriterImpl(Channels.newWriter(resource.getWriteChannel(), IOUtil.DEFAULT_CHARSET_NAME));
	}

	@Override
	public MatchReader newReader(IOResource resource) throws IOException {
		return new ReaderImpl(Channels.newReader(resource.getReadChannel(), IOUtil.DEFAULT_CHARSET_NAME));
	}

	private static final char SEP = '\t';

	public static final class WriterImpl implements MatchCodec.MatchWriter {

		private final Writer writer;

		WriterImpl(Writer writer) {
			this.writer = requireNonNull(writer);
		}

		@Override
		public void write(Match match) throws IOException {
			writer.write(String.valueOf(match.getIndex()));
			writer.write(SEP);
			final int size = match.getMapCount();
			writer.write(String.valueOf(size));
			for (int i = 0; i < size; i++) {
				writer.write(SEP);
				writer.write(String.valueOf(match.getNode(i)));
				writer.write(SEP);
				writer.write(String.valueOf(match.getIndex(i)));
			}
			writer.write('\n');
		}

		@Override
		public void close() throws IOException {
			writer.close();
		}

	}

	public static final class ReaderImpl implements MatchCodec.MatchReader {

		private final CharLineBuffer line;

		ReaderImpl(Reader reader) throws IOException {
			line = new CharLineBuffer();
			line.startReading(reader);
		}

		@Override
		public boolean next() throws IOException {
			return line.next();
		}

		private int readInt(int col) {
			Splitable c = line.getSplitCursor(col);
			int v = StringPrimitives.parseInt(c);
			c.recycle();
			return v;
		}

		private long readLong(int col) {
			Splitable c = line.getSplitCursor(col);
			long v = StringPrimitives.parseLong(c);
			c.recycle();
			return v;
		}

		@Override
		public Match read() throws IOException {
			int cols = line.split(SEP);
			assert cols >=2 : "require at least 2 data columns";
			assert cols%2==0 : "require an even number of data columns";
			int col = 0;
			long index = readLong(col++);
			int size = readInt(col++);
			int[] m_node = new int[size], m_index = new int[size];

			for (int i = 0; i < size; i++) {
				m_node[i] = readInt(col++);
				m_index[i] = readInt(col++);
			}

			return MatchImpl.of(index, m_node, m_index);
		}

		@Override
		public void readAll(Consumer<? super Match> action) throws IOException {
			while(next()) {
				action.accept(read());
			}
		}

		@Override
		public void close() throws IOException {
			line.close();
		}

	}
}
