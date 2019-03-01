/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.transfer.dna;

import java.io.IOException;
import java.util.function.IntConsumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.path.CorpusPath;
import de.ims.icarus2.model.api.path.CorpusPath.PathElementType;
import de.ims.icarus2.model.api.registry.LayerLookup;
import de.ims.icarus2.model.api.transfer.spi.CorpusMemberEncoder;
import de.ims.icarus2.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 *
 */
public class DNAEncoder extends CorpusMemberEncoder {

	/**
	 * Serialized form of long ids in hexadecimal form
	 */
	private final char[] buffer = new char[16];

	/**
	 * Number of characters used in the buffer
	 */
	private int length = 0;

	/**
	 * Internal wrapper for using the char buffer
	 * as a {@link CharSequence}
	 */
	private final CharSequence cs = new CharSequence() {

		@Override
		public CharSequence subSequence(int start, int end) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int length() {
			return length;
		}

		@Override
		public char charAt(int index) {
			if(index<0 || index>=length)
				throw new IndexOutOfBoundsException();

			return buffer[index];
		}
	};

	private final StringBuilder sb = new StringBuilder();

	public DNAEncoder(LayerLookup config) {
		super(config);
	}

	private void writeLong(long id) {
		length = StringUtil.writeHexString(id, buffer, 0);
	}

	private void writeLayer0(Layer layer) {
		writeLong(config.getUID(layer));
	}

	private void writeItem0(Item item) {
		writeLong(item.getIndex());
	}

	private void flush(IntConsumer out, char delimiter) {
		for(int i=0; i<length; i++) {
			out.accept(buffer[i]);
		}
		//TODO check necessity of null character check
		if(delimiter!='\0') {
			out.accept(delimiter);
		}
	}

	private void flush(Appendable out, char delimiter) throws IOException {
		out.append(cs);
		out.append(delimiter);
	}

	@Override
	public void writeItem(Item item, IntConsumer out) {
		writeLayer0(item.getLayer());
		flush(out, DNAConstants._SEP_MID_);

		writeItem0(item);
		flush(out, DNAConstants._SEP_END_);
	}

	@Override
	public void writeItem(Item item, Appendable out) throws IOException {
		writeLayer0(item.getLayer());
		flush(out, DNAConstants._SEP_MID_);

		writeItem0(item);
		flush(out, DNAConstants._SEP_END_);
	}

	@Override
	public String writeItem(Item item) {
		sb.setLength(0);
		try {
			writeItem(item, sb);
		} catch (IOException e) {
			throw new ModelException(GlobalErrorCode.INTERNAL_ERROR,
					"Unexpected IOException in non-IO context", e);
		}
		return sb.toString();
	}

	@Override
	public void writePath(CorpusPath path, IntConsumer out) {
		out.accept(DNAConstants._PATH_BEGIN_);

		int count = path.getElementCount();
		for(int i=0; i<count; i++) {
			PathElementType type = path.getType(i);

			out.accept(Character.forDigit(type.ordinal(), 16));

			switch (type) {
			case LAYER:
				writeLayer0(path.getLayer(i));
				flush(out, DNAConstants._SEP_END_);
				break;

			case EDGE:
			case ITEM:
				writeItem(path.getItem(i), out);
				break;

			case EDGE_INDEX:
			case ITEM_INDEX:
				writeLong(path.getIndex(i));
				flush(out, DNAConstants._SEP_END_);
				break;

			default:
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Unknown path element type: "+type);
			}
		}

		out.accept(DNAConstants._PATH_END_);
	}

	@Override
	public void writePath(CorpusPath path, Appendable out) throws IOException {
		out.append(DNAConstants._PATH_BEGIN_);

		int count = path.getElementCount();
		for(int i=0; i<count; i++) {
			PathElementType type = path.getType(i);

			out.append(Character.forDigit(type.ordinal(), 16));

			switch (type) {
			case LAYER:
				writeLayer0(path.getLayer(i));
				flush(out, DNAConstants._SEP_END_);
				break;

			case EDGE:
			case ITEM:
				writeItem(path.getItem(i), out);
				break;

			case EDGE_INDEX:
			case ITEM_INDEX:
				writeLong(path.getIndex(i));
				flush(out, DNAConstants._SEP_END_);
				break;

			default:
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Unknown path element type: "+type);
			}
		}

		out.append(DNAConstants._PATH_END_);
	}

	@Override
	public String writePath(CorpusPath path) {
		sb.setLength(0);
		try {
			writePath(path, sb);
		} catch (IOException e) {
			throw new ModelException(GlobalErrorCode.INTERNAL_ERROR,
					"Unexpected IOException in non-IO context", e);
		}
		return sb.toString();
	}
}
