/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.Conditions.checkArgument;

import java.io.IOException;
import java.io.Reader;
import java.util.function.IntSupplier;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.path.CorpusPath;
import de.ims.icarus2.model.api.path.CorpusPath.PathElementType;
import de.ims.icarus2.model.api.path.CorpusPathBuilder;
import de.ims.icarus2.model.api.registry.LayerLookup;
import de.ims.icarus2.model.api.transfer.spi.CorpusMemberDecoder;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.strings.CharSequenceReader;
import de.ims.icarus2.util.strings.StringUtil;

/**
 *
 * <b>Serialization formats:<b>
 * <p>
 * <table border="1">
 * <tr><th>Content</th><th>Format</th><th>Description</th></tr>
 * <tr><td>{@link Item item}</td><td>context_id:layer_id:item_index</td><td>points to a single top-level item</td></tr>
 * <tr><td>{@link }</td><td></td><td></td></tr>
 * <tr><td></td><td></td><td></td></tr>
 * <tr><td></td><td></td><td></td></tr>
 * </table>
 *
 * @author Markus Gärtner
 *
 */
public class DNADecoder extends CorpusMemberDecoder implements DNAConstants {


	private final char[] buffer = new char[16];

	private int length = 0;

	private final CorpusPathBuilder builder;

	private final CharSequenceReader reader = new CharSequenceReader();

	public DNADecoder(LayerLookup config) {
		super(config);

		builder = new CorpusPathBuilder(config.getView());
	}

	private long readLong0() {
		return StringUtil.parseHexString(buffer, 0, length);
	}

	private Layer readLayer0() {
		long uid = readLong0();

		return config.getLayer(IcarusUtils.ensureIntegerValueRange(uid));
	}

	private Item readItem0(ItemLayer layer) {
		long index = readLong0();

		Driver driver = layer.getContext().getDriver();

		return driver.getItem(layer, index);
	}

	private void fill(IntSupplier in, char delimiter) {
		int c;
		length = 0;
		while((c = in.getAsInt())!=delimiter) {
			if(c==-1)
				throw new ModelException("Unexpected end of stream");
			buffer[length++] = (char) c;
		}
	}

	private void fill(Reader in, char delimiter) throws IOException {
		int c;
		length = 0;
		while((c = in.read())!=delimiter) {
			if(c==-1)
				throw new IOException("Unexpected end of stream");

			buffer[length++] = (char) c;
		}
	}

	@Override
	public Item readItem(IntSupplier in) {
		fill(in, _SEP_MID_);
		Layer layer = readLayer0();

		checkArgument("Not an ItemLayer", ModelUtils.isItemLayer(layer));

		fill(in, _SEP_END_);

		return readItem0((ItemLayer) layer);
	}

	@Override
	public Item readItem(Reader in) throws IOException {
		fill(in, _SEP_MID_);
		Layer layer = readLayer0();

		checkArgument("Not an ItemLayer", ModelUtils.isItemLayer(layer));

		fill(in, _SEP_END_);

		return readItem0((ItemLayer) layer);
	}

	@Override
	public Item readItem(CharSequence s) {

		reader.setSource(s);

		try {
			return readItem(reader);
		} catch (IOException e) {
			throw new ModelException(GlobalErrorCode.INTERNAL_ERROR,
					"Unexpected IOException in non-IO context", e);
		} finally {
			reader.clear();
		}
	}

	@Override
	public CorpusPath readPath(IntSupplier in) {
		fill(in, _PATH_BEGIN_);

		builder.reset();

		char next;

		while((next = (char) in.getAsInt()) != _PATH_END_) {
			PathElementType type = PathElementType.forOrdinal(Character.digit(next, 16));

			switch (type) {
			case LAYER:
				fill(in, _SEP_END_);
				builder.appendLayer(readLayer0());
				break;

			case ITEM:
				builder.appendItem(readItem(in));
				break;

			case EDGE:
				builder.appendEdge((Edge)readItem(in));
				break;

			case ITEM_INDEX:
				fill(in, _SEP_END_);
				builder.appendItemIndex(readLong0());
				break;

			case EDGE_INDEX:
				fill(in, _SEP_END_);
				builder.appendEdgeIndex(readLong0());
				break;

			default:
				throw new ModelException(GlobalErrorCode.INTERNAL_ERROR, "Unknown path element type: "+type);
			}
		}

		return builder.createPath();
	}

	@Override
	public CorpusPath readPath(Reader in) throws IOException {
		fill(in, _PATH_BEGIN_);

		builder.reset();

		char next;

		while((next = (char) in.read()) != _PATH_END_) {
			PathElementType type = PathElementType.forOrdinal(Character.digit(next, 16));

			switch (type) {
			case LAYER:
				fill(in, _SEP_END_);
				builder.appendLayer(readLayer0());
				break;

			case ITEM:
				builder.appendItem(readItem(in));
				break;

			case EDGE:
				builder.appendEdge((Edge)readItem(in));
				break;

			case ITEM_INDEX:
				fill(in, _SEP_END_);
				builder.appendItemIndex(readLong0());
				break;

			case EDGE_INDEX:
				fill(in, _SEP_END_);
				builder.appendEdgeIndex(readLong0());
				break;

			default:
				throw new ModelException(GlobalErrorCode.INTERNAL_ERROR, "Unknown path element type: "+type);
			}
		}

		return builder.createPath();
	}

	@Override
	public CorpusPath readPath(CharSequence s) {

		reader.setSource(s);

		try {
			return readPath(reader);
		} catch (IOException e) {
			throw new ModelException(GlobalErrorCode.INTERNAL_ERROR,
					"Unexpected IOException in non-IO context", e);
		} finally {
			reader.clear();
		}
	}
}
