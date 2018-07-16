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

public class SubSequence extends AbstractString {

	private final CharSequence source;
	private final int offset;
	private final int len;

	public SubSequence(CharSequence source, int offset, int len) {
		if (source == null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$
		if(offset<0 || offset>=source.length())
			throw new IndexOutOfBoundsException("offset"); //$NON-NLS-1$
		if(len<0 || len>=source.length()-offset)
			throw new IllegalArgumentException("length"); //$NON-NLS-1$

		this.source = source;
		this.offset = offset;
		this.len = len;
	}

	/**
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length() {
		return len;
	}

	/**
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@Override
	public char charAt(int index) {
		if(index<0 || index>=len)
			throw new IndexOutOfBoundsException();

		return source.charAt(offset+index);
	}
}