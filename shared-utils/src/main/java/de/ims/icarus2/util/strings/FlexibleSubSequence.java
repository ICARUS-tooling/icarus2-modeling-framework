/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
 *
 */
package de.ims.icarus2.util.strings;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;

public class FlexibleSubSequence extends AbstractString {

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
		checkNotNull(source);

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
}