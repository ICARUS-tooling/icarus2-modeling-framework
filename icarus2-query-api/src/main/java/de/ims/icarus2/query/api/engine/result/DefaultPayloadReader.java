/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine.result;

import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import java.util.function.IntFunction;

import javax.annotation.Nullable;

import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.engine.result.Extractor.BooleanExtractor;
import de.ims.icarus2.query.api.exp.TypeInfo;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultPayloadReader implements PayloadReader {

	private final TypeInfo[] types;
	private final IntFunction<CharSequence> decoder;

	public DefaultPayloadReader(TypeInfo[] types, @Nullable IntFunction<CharSequence> decoder) {
		this.types = requireNonNull(types);
		this.decoder = decoder;
	}

	private void checkType(int index, TypeInfo type) {
		if(types[index]!=type)
			throw new QueryException(QueryErrorCode.TYPE_MISMATCH,
					Messages.mismatch("Payload type mismatch at index "+index, types[index], type));
	}

	@Override
	public int payloadSize() { return types.length; }

	@Override
	public TypeInfo getType(int index) { return types[index]; }

	@Override
	public long getInteger(ResultEntry entry, int index) {
		checkType(index, TypeInfo.INTEGER);
		return entry.payloadAt(index);
	}

	@Override
	public double getFloatingPoint(ResultEntry entry, int index) {
		checkType(index, TypeInfo.FLOATING_POINT);
		return Extractor.decode(entry.payloadAt(index));
	}

	@Override
	public boolean getBoolean(ResultEntry entry, int index) {
		checkType(index, TypeInfo.BOOLEAN);
		return entry.payloadAt(index) == BooleanExtractor.TRUE;
	}

	@Override
	public CharSequence getText(ResultEntry entry, int index) {
		checkType(index, TypeInfo.TEXT);
		return decoder.apply(strictToInt(entry.payloadAt(index)));
	}
}
