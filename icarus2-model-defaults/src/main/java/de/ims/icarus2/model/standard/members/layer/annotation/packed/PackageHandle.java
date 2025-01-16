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
package de.ims.icarus2.model.standard.members.layer.annotation.packed;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.mem.ByteAllocator;
import de.ims.icarus2.util.strings.ToStringBuilder;

/**
 * Bundles all the information needed by the {@link PackedDataManager} to map
 * annotation values for individual {@link Item items} to byte slots of a
 * {@link ByteAllocator}.
 *
 * @author Markus Gärtner
 *
 */
public class PackageHandle {

	/**
	 * Key used for accessing this info
	 */
	final Object source;

	final Object noEntryValue;

	/**
	 * The converter used to translate between raw byte data
	 * and the actual annotation types in case of complex types.
	 */
	final BytePackConverter converter;

	/**
	 * Utility objects used by the converter to
	 * process raw byte data. Also used for synchronization
	 * when present.
	 * <p>
	 * In case a converter does not {@link BytePackConverter#createContext() provide}
	 * his own context, it is assumed to be stateless.
	 */
	private final Object converterContext;

	/**
	 * Position in the lookup table.
	 */
	private int index = IcarusUtils.UNSET_INT;

	/**
	 * Byte offset within chunk of raw data.
	 */
	private int offset = IcarusUtils.UNSET_INT;

	/**
	 * For packed boolean values this indicates the bit within a single
	 * byte that is used for storing the annotation value for this handle.
	 */
	private int bit = IcarusUtils.UNSET_INT;

	/**
	 * @param key
	 * @param noEntryValue
	 * @param converter
	 */
	public PackageHandle(Object source, Object noEntryValue, BytePackConverter converter) {
		this.source = requireNonNull(source);
		this.converter = requireNonNull(converter);
		this.noEntryValue = noEntryValue;

		converterContext = converter.createContext();

	}

	public Object getSource() {
		return source;
	}

	public Object getNoEntryValue() {
		return noEntryValue;
	}

	public BytePackConverter getConverter() {
		return converter;
	}

	public Object getConverterContext() {
		return converterContext;
	}

	public int getIndex() {
		return index;
	}

	public int getOffset() {
		return offset;
	}

	public int getBit() {
		return bit;
	}

	void setOffset(int offset) {
		this.offset = offset;
	}

	void setIndex(int index) {
		this.index = index;
	}

	void setBit(int bit) {
		this.bit = bit;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return source.hashCode();
	}

	/**
	 * Equality of handles is based on the reference identity of their
	 * {@link #getSource() sources}.
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj==this) {
			return true;
		} else if(obj instanceof PackageHandle) {
			return source == ((PackageHandle)obj).source;
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.create(this)
				.add("source", source)
				.add("noEntryValue", noEntryValue)
				.add("converter", converter)
				.add("context", converterContext)
				.add("index", index)
				.add("offset", offset)
				.add("bit", bit)
				.build();
	}
}