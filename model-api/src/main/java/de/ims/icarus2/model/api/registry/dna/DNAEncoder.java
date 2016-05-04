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
package de.ims.icarus2.model.api.registry.dna;

import java.io.IOException;
import java.util.function.IntConsumer;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.path.CorpusPath;
import de.ims.icarus2.model.api.path.CorpusPath.PathElementType;
import de.ims.icarus2.model.api.registry.CorpusMemberEncoder;
import de.ims.icarus2.model.api.registry.LayerLookup;
import de.ims.icarus2.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DNAEncoder extends CorpusMemberEncoder implements DNAConstants {

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
		out.accept(delimiter);
	}

	private void flush(Appendable out, char delimiter) throws IOException {
		out.append(cs);
		out.append(delimiter);
	}

	@Override
	public void writeItem(Item item, IntConsumer out) {
		writeLayer0(item.getLayer());
		flush(out, _SEP_MID_);

		writeItem0(item);
		flush(out, _SEP_END_);
	}

	@Override
	public void writeItem(Item item, Appendable out) throws IOException {
		writeLayer0(item.getLayer());
		flush(out, _SEP_MID_);

		writeItem0(item);
		flush(out, _SEP_END_);
	}

	@Override
	public String writeItem(Item item) {
		sb.setLength(0);
		try {
			writeItem(item, sb);
		} catch (IOException e) {
			throw new ModelException(ModelErrorCode.INTERNAL_ERROR,
					"Unexpected IOException in non-IO context", e);
		}
		return sb.toString();
	}

	@Override
	public void writePath(CorpusPath path, IntConsumer out) {
		out.accept(_PATH_BEGIN_);

		int count = path.getElementCount();
		for(int i=0; i<count; i++) {
			PathElementType type = path.getType(i);

			out.accept(Character.forDigit(type.ordinal(), 16));

			switch (type) {
			case LAYER:
				writeLayer0(path.getLayer(i));
				flush(out, _SEP_END_);
				break;

			case EDGE:
			case ITEM:
				writeItem(path.getItem(i), out);
				break;

			case EDGE_INDEX:
			case INDEX:
				writeLong(path.getIndex(i));
				flush(out, _SEP_END_);
				break;

			default:
				throw new ModelException(ModelErrorCode.INVALID_INPUT, "Unknown path element type: "+type);
			}
		}

		out.accept(_PATH_END_);
	}

	@Override
	public void writePath(CorpusPath path, Appendable out) throws IOException {
		out.append(_PATH_BEGIN_);

		int count = path.getElementCount();
		for(int i=0; i<count; i++) {
			PathElementType type = path.getType(i);

			out.append(Character.forDigit(type.ordinal(), 16));

			switch (type) {
			case LAYER:
				writeLayer0(path.getLayer(i));
				flush(out, _SEP_END_);
				break;

			case EDGE:
			case ITEM:
				writeItem(path.getItem(i), out);
				break;

			case EDGE_INDEX:
			case INDEX:
				writeLong(path.getIndex(i));
				flush(out, _SEP_END_);
				break;

			default:
				throw new ModelException(ModelErrorCode.INVALID_INPUT, "Unknown path element type: "+type);
			}
		}

		out.append(_PATH_END_);
	}

	@Override
	public String writePath(CorpusPath path) {
		sb.setLength(0);
		try {
			writePath(path, sb);
		} catch (IOException e) {
			throw new ModelException(ModelErrorCode.INTERNAL_ERROR,
					"Unexpected IOException in non-IO context", e);
		}
		return sb.toString();
	}
}
