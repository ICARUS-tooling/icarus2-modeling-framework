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
package de.ims.icarus2.util.strings;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

public class FlexibleSubSequence extends AbstractString implements AutoCloseable {

	private CharSequence source;
	private int offset;
	private int length;

	public FlexibleSubSequence(CharSequence source, int offset, int len) {
		setSource(source);
		setOffset(offset);
		setLength(len);
	}

	public FlexibleSubSequence(CharSequence source) {
		setSource(source);
	}

	public FlexibleSubSequence() {
	}

	/**
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length() {
		return length;
	}

	/**
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@Override
	public char charAt(int index) {
		if(index<0 || index>=length)
			throw new IndexOutOfBoundsException();

		return source.charAt(offset+index);
	}

	/**
	 * @return the source
	 */
	public CharSequence getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(CharSequence source) {
		requireNonNull(source);

		this.source = source;
	}

	/**
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * @param offset the offset to set
	 */
	public void setOffset(int offset) {
		checkState(source!=null);
		checkArgument(offset>=0 && offset<source.length());
		this.offset = offset;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		checkState(source!=null);
		checkArgument(length>=0);
		this.length = length;
	}

	public void setRange(int begin, int end) {
		checkState(source!=null);
		checkArgument(begin>=0 && begin<source.length());
		checkArgument(end>=begin && end<source.length());

		offset = begin;
		length = end-begin+1;
	}

	/**
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		source = null;
		offset = 0;
		length = 0;
	}
}